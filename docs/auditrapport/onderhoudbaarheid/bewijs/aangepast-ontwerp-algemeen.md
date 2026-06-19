# Architectuur en Aangepast Ontwerp

Tijdens de beveiligingsaudit kwamen fundamentele ontwerpfouten aan het licht, zoals hardcoded AWS-sleutels en ontbrekende input-validatie (Path Traversal/LFI). Om deze kwetsbaarheden niet oppervlakkige te verbeteren, is het systeem gerefactored en is het ontwerp aangepast. Hierbij is nadrukkelijk gebruikgemaakt van objectgeoriënteerde ontwerppatronen (OO-patterns) en de SOLID-principes.

## 1. Het Aangepaste Ontwerp en Ontwerppatronen

In het aangepaste ontwerp voor de verwerking van attachments is bewust gekozen voor het **Template Method Pattern**.

De abstracte basisklasse `AbstractAttachmentHandler` definieert de template voor CRUD-operaties via `final` methoden. De specifieke invulling van het opslaan van complexe data wordt overgelaten aan subklassen zoals de `ImageAttachmentHandler`.

### Codevoorbeeld (Template Method)

```java
@Override
final public Obs saveObs(Obs obs) {
    AttachmentComplexData complexData = getAttachmentComplexData(obs.getComplexData());

    // Stap 1: Specifieke logica wordt gedelegeerd naar de subklasse
    ValueComplex valueComplex = saveComplexData(obs, complexData);

    // Stap 2: Vaste en veilige afhandeling voor het hele systeem
    obs.setValueComplex(valueComplex.getValueComplex());
    return obs;
}
```

### Alternatieven

Tijdens het herontwerp zijn alternatieven overwogen.

#### Alternatief (Afgewezen): Strategy Pattern

Voor de verwerking van verschillende bestandstypen had ook het **Strategy Pattern** gebruikt kunnen worden, waarbij voor elk bestandstype (image, pdf, text) tijdens runtime een aparte strategie geïnjecteerd zou worden.

#### Motivatie voor Template Method

Er is specifiek voor het Template Method Pattern gekozen omdat de verwerkingsstappen voor alle bestandstypen binnen OpenMRS grotendeels identiek zijn (ophalen, databasetransactie starten, rechten checken) en slechts in één specifieke stap (zoals thumbnail-generatie) verschillen.

Het Strategy Pattern zou hier leiden tot onnodige code duplicatie in de implementaties. De Template Method borgt de standaard OpenMRS-flow centraal en dwingt structuur af, wat direct de kans op security-fouten (zoals het vergeten van input-validatie) in individuele handlers minimaliseert.

## 2. Toepassing van SOLID-principes in het Aangepaste Ontwerp

Het aangepaste ontwerp rust op de vijf SOLID-principes als architectonisch fundament. Hieronder wordt elk principe kritisch geanalyseerd, onderbouwd met codevoorbeelden uit de module en verdedigd tegenover alternatieve ontwerpkeuzes.

---

### 2.1 Single Responsibility Principle (SRP)

**Ontwerpkeuze:**
Een klasse of interface mag slechts één verantwoordelijkheid hebben en dus maar één reden hebben om te veranderen. In het aangepaste ontwerp is de verwerking van UI-weergaven (views) rigoureus gescheiden van de core bestandsafhandeling door de introductie van de `ComplexViewHelper` interface.

#### Codevoorbeeld

```java id="srp1"
package org.openmrs.module.attachments.obs;

import org.openmrs.Obs;

public interface ComplexViewHelper {
    // Sole responsibility: resolve and translate version-specific OpenMRS view arguments
    public String getView(Obs obs, String view);
}
```

#### Alternatief en Motivatie

Het alternatief was om deze logica direct binnen de `AbstractAttachmentHandler` te plaatsen. Dit is afgewezen omdat de handler zich bezig moet houden met bestandstransacties, niet met OpenMRS-versiecompatibiliteit. Door SRP toe te passen via een externe helper, heeft een wijziging in de OpenMRS UI-architectuur geen enkele impact op de core bestandsverwerking, wat de **Maintainability** aanzienlijk verhoogt.

---

### 2.2 Open / Closed Principle (OCP)

**Ontwerpkeuze:**
Software-entiteiten moeten openstaan voor uitbreiding, maar gesloten zijn voor modificatie. De `AbstractAttachmentHandler` dwingt dit principe af door de centrale OpenMRS-lifecycle hooks te markeren als `final`. Uitbreiding wordt gefaciliteerd via abstracte protected hooks.

#### Codevoorbeeld

```java id="ocp1"
// Binnen AbstractAttachmentHandler.java

@Override
final public Obs saveObs(Obs obs) {
    AttachmentComplexData complexData = getAttachmentComplexData(obs.getComplexData());

    // Open for extension: delegates specific format handling to subclasses
    ValueComplex valueComplex = saveComplexData(obs, complexData);

    obs.setValueComplex(valueComplex.getValueComplex());
    return obs;
}

// Abstract hook to enforce safe implementation in subclasses
abstract protected ValueComplex saveComplexData(
        Obs obs,
        AttachmentComplexData complexData
);
```

#### Alternatief en Motivatie

Een alternatief was één monolithische handler-klasse met `if/else` of `switch`-constructies op basis van MIME-types. Dit is afgewezen omdat elke toevoeging van een bestandstype aanpassingen in reeds geteste code vereist. OCP minimaliseert regressie-bugs en versterkt de **Security**, omdat de geharde core-logica onwrikbaar blijft.

---

### 2.3 Liskov’s Substitution Principle (LSP)

**Ontwerpkeuze:**
Subklassen moeten naadloos de plaats kunnen innemen van hun basisklasse zonder dat de correctheid van het programma in gevaar komt. De `ImageAttachmentHandler` respecteert het gedrag, de types en de exceptie-contracten van de basisklasse volledig.

#### Codevoorbeeld

```java id="lsp1"
// Binnen ImageAttachmentHandler.java

public class ImageAttachmentHandler extends AbstractAttachmentHandler {

    @Override
    protected ValueComplex saveComplexData(
            Obs obs,
            AttachmentComplexData complexData
    ) {
        // Enhances behavior by generating thumbnails, but strictly obeys base contracts
        obs = getParent().saveObs(obs);
        File savedFile = AbstractHandler.getComplexDataFile(obs);
        String savedFileName = savedFile.getName();

        try {
            savedFileName = saveThumbnailOrRename(savedFile, imageHeight, imageWidth);
        } catch (APIException e) {
            getParent().purgeComplexData(obs);
            throw new APIException("A thumbnail file could not be saved...", e);
        }

        return new ValueComplex(
                complexData.getInstructions(),
                complexData.getMimeType(),
                savedFileName
        );
    }
}
```

#### Alternatief en Motivatie

Een veelvoorkomende LSP-schending is het gooien van `UnsupportedOperationException` in subklassen. Dit is bewust voorkomen. De `ImageAttachmentHandler` implementeert alle abstracte operaties op een voorspelbare manier. Dit garandeert de **Robustness** van het systeem: de OpenMRS-core kan elke handler polymorf aanroepen zonder runtime-crashes.

---

### 2.4 Interface Segregation Principle (ISP)

**Ontwerpkeuze:**
Clients mogen niet gedwongen worden afhankelijk te zijn van methoden die zij niet gebruiken. De `AttachmentsService` is daarom opgesplitst rond specifieke attachment-functionaliteit.

#### Codevoorbeeld

```java id="isp1"
package org.openmrs.module.attachments;

import java.util.List;
import org.openmrs.Patient;
import org.openmrs.module.attachments.obs.Attachment;

public interface AttachmentsService {

    List<Attachment> getAttachments(
            Patient patient,
            boolean includeVoided
    );

    List<Attachment> getEncounterlessAttachments(
            Patient patient,
            boolean includeVoided
    );

    Attachment save(Attachment attachment, String reason);
}
```

#### Alternatief en Motivatie

Het alternatief was integratie in `ObsService` of `PatientService`. Dit zou zorgen voor onnodige koppeling tussen domeinen. ISP voorkomt deze 'fat interface' en verhoogt de **Testability**, omdat unit-tests klein en gefocust blijven.

---

### 2.5 Dependency Inversion Principle (DIP)

**Ontwerpkeuze:**
High-level modules mogen niet afhankelijk zijn van low-level implementaties; beide moeten afhankelijk zijn van abstracties. In `AbstractAttachmentHandler` worden dependencies via Spring injectie toegevoegd.

#### Codevoorbeeld

```java id="dip1"
// Binnen AbstractAttachmentHandler.java

@Autowired
@Qualifier(AttachmentsConstants.COMPONENT_COMPLEXDATA_HELPER)
private ComplexDataHelper complexDataHelper;

@Autowired
@Qualifier(AttachmentsConstants.COMPONENT_COMPLEXVIEW_HELPER)
private ComplexViewHelper complexViewHelper;
```

#### Alternatief en Motivatie

Het alternatief was directe instantiatie (`new ComplexDataHelperImpl()`). Dit leidt tot tight coupling en maakt testen moeilijk. DIP voorkomt dit volledig, verhoogt **Modifiability** en maakt 100% geïsoleerde unit-tests mogelijk via mocks.

## 3. Motivatie op basis van Kwaliteitseisen (ISO 25010)

Het aangepaste ontwerp en de gemaakte OO-keuzes dragen direct bij aan de volgende kwaliteitsattributen:

### Security (Beveiliging)

Door het afdwingen van het Template Method Pattern verlopen alle bestandsoperaties via één gecentraliseerde flow. De in de pentest gevonden Path Traversal (LFI) kwetsbaarheid kan hierdoor op één centrale plek gevalideerd worden, in plaats van verspreid over tientallen endpoints.

### Maintainability (Onderhoudbaarheid)

Door het toepassen van SRP en OCP is de code extreem modulair geworden. Aanpassingen in de configuratie (zoals het verplaatsen van de hardcoded AWS-secrets naar environment variables) konden worden doorgevoerd zonder dat de onderliggende logica voor bestandsverwerking geraakt werd.

### Testability (Testbaarheid)

De keuze voor Dependency Inversion (DIP) in plaats van directe instantiatie maakt het mogelijk om alle componenten geïsoleerd te testen, wat resulteert in betrouwbaardere en veiligere software-releases.
