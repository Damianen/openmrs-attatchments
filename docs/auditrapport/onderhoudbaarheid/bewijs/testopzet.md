# Testopzet en Testarchitectuur (Kwaliteitsattribuut: Testbaarheid)

Dit document beschrijft de testarchitectuur en de inrichting van de geautomatiseerde testsuite voor de OpenMRS Attachments-module. Een gestructureerde testopzet is een fundamentele voorwaarde voor het waarborgen van de ISO 25010-kwaliteitseisen, met name **Testbaarheid (Testability)** en **Aanpasbaarheid (Modifiability)**.

De suite stelt ontwikkelaars in staat om functionele wijzigingen en security-mitigaties (zoals de uitgevoerde secrets-refactor en invoervalidatie) direct geautomatiseerd te verifiëren op regressie-bugs.

---

# 1. JaCoCo Dekkingsbaseline (Metrics Baseline)

Om de effectiviteit en diepgang van de testsuite objectief te meten, wordt gebruikgemaakt van de JaCoCo (Java Code Coverage) runtime-agent tijdens de Maven build-lifecycle.

Dit levert de volgende dekkingsbaseline op:

| Module                        | Aantal Geteste Klassen/Elementen | Regeldekkingspercentage (Line Coverage) | Doelstelling Quality Gate                 |
| ----------------------------- | -------------------------------- | --------------------------------------- | ----------------------------------------- |
| Core API Module (`api`)       | 13 klassen                       | 74%                                     | Voldoet aan de vereiste baseline van >70% |
| Web REST OMOD Module (`omod`) | 57 klassen / elementen (subset)  | 66%                                     | Voldoet aan de vereiste baseline van >65% |

Deze percentages tonen aan dat de kritieke paden van de module — met name de service-transacties, de REST-endpoints en de complexe datahandlers — intensief worden geraakt door de geautomatiseerde testsuite.

---

# 2. Classificatie van de Testtypen

De testsuite is strikt opgedeeld in twee complementaire testtypen. Deze scheiding is een bewuste architectonische keuze om zowel executiesnelheid (snelle feedback) als functionele integratie (diepgaande validatie) te garanderen.

## Type 1: Geïsoleerde Unit Tests (JUnit)

Dit type test test een specifieke klasse of methode in volledige isolatie van het OpenMRS-framework, de database en de Spring-context. Afhankelijkheden worden weggewerkt via stubs of handmatige mocks.

**Voordeel:** Extreem korte executietijd (milliseconden) en onafhankelijk van de infrastructuur.

**Toegepast in o.a.:**

* `StorageConfigTest`
* `ValueComplexTest`

`StorageConfigTest` maakt bijvoorbeeld gebruik van de `EnvironmentSource` abstractie om configuratiewaarden te testen zonder de daadwerkelijke omgevingsvariabelen van het proces te hoeven muteren.

## Type 2: Context-Sensitive Componenten- & Integratietests

Dit type test maakt gebruik van de OpenMRS testing infrastructuur door te overerven van `BaseModuleContextSensitiveTest` of `BaseContextSensitiveTest`.

Hierbij wordt:

* een in-memory H2-database geïnitieerd;
* de Spring component-scanning uitgevoerd;
* database-schema's via Liquibase geladen;
* testdata declaratief ingeladen via XML-datasets (zoals `test-dataset.xml`).

**Voordeel:** Valideert de daadwerkelijke samenwerking met het OpenMRS-ecosysteem, inclusief database-constraints, Hibernate OR-mapping en Spring-autorisatie-annotaties (zoals `@Authorized`).

**Toegepast in o.a.:**

* `AttachmentsServiceImplTest`
* `AttachmentResourceIntegrationTest`

---

# 3. Wat wordt gecovered

De suite bestaat uit **10 actieve testbestanden** (en 1 helperklasse) die strategisch verdeeld zijn over de functionele lagen van de applicatie.

## 3.1 Core API Module (`api/src/test/java/`)

### `StorageConfigTest`

Pure unit-test. Garandeert de foutloze werking van de gecentreerde secrets-refactor. Verifieert dat geldige keys correct uit de omgevingsabstractie worden gelezen en dwingt af dat een ontbrekende key resulteert in een fail-fast `IllegalStateException`.

### `AttachmentsContextTest`

Componententest die valideert of de module-context en Spring-beans correct worden geïnitieerd en geregistreerd binnen het applicatie-lifecycle framework.

### `AttachmentsServiceImplTest`

Integratietest. Valideert de core business-logica van de `AttachmentsService` interface. Test het opvragen van bijlagen per patiënt, ontmoeting (encounter) of bezoek (visit), inclusief de filtering van verwijderde (voided) data.

### `obs/ValueComplexTest`

Unit-test gericht op de string-parsing logica van metadata (MIME-types, instructies en bestandsnamen) binnen de OpenMRS `value_complex` databasevelden.

### `obs/DefaultAttachmentHandlerTest` & `ImageAttachmentHandlerTest`

Integratietests die controleren of de via het Template Method Pattern gestructureerde handlers bestanden correct opslaan, inlezen en purgen.

`ImageAttachmentHandlerTest` verifieert specifiek de automatische schaling en generatie van thumbnails via Thumbnailator.

## 3.2 Web REST OMOD Module (`omod/src/test/java/`)

### `rest/AttachmentRestControllerTest`

Integratietest van de HTTP REST-laag. Controleert of de endpoints correct reageren op inkomende JSON/XML-payloads en HTTP-statuscodes borgen.

### `rest/AttachmentResourceTest` & `AttachmentResourceIntegrationTest`

Valideren de REST Web Services mapping van OpenMRS. Ze controleren of OpenMRS-domeinobjecten (zoals `Obs`) correct worden omgezet naar REST-vriendelijke resources en vice versa.

### `rest/AttachmentBytesResourceTest`

Integratietest die specifiek focust op de streaming van ruwe byte-arrays (file-downloads).

Deze test is na de pentest kritiek om te verifiëren dat invoervalidatie tegen Path Traversal correct functioneert en ongeautoriseerde bestandstoegang blokkeert.

### `rest/ObsByConceptListSearchHandlerTest`

Richt zich specifiek op de custom REST-zoekfunctionaliteit om bijlagen efficiënt te filteren op basis van medische concept-lijsten.

---

# 4. Hoe de Testsuite de Testbaarheid (ISO 25010) ondersteunt

De inrichting van deze testsuite verhoogt het kwaliteitsattribuut **Testbaarheid** op drie fundamentele manieren.

## Testen via Seams (Ingebedde test-ingangen)

De architectuur maakt dankzij het Dependency Inversion Principle (DIP) gebruik van interfaces (zoals `EnvironmentSource`).

Dit creëert een zogenaamde *test seam*: een opening in de code waar de testsuite een gecontroleerde stub kan inschuiven. Hierdoor hoeven tests nooit de echte backend-omgeving te manipuleren.

## Determinisme via Database Isolatie

Doordat de integratietests gebruikmaken van een geïsoleerde in-memory H2-database die vóór elke testmethode via `@Before` in een schone staat wordt hersteld (rollback-mechanisme), is er sprake van 100% deterministische tests.

Tests hebben geen last van "flakiness" door restdata van eerdere runs.

## Snelle Regressie-detectie bij Security-Fixes

Wanneer er een security-patch wordt doorgevoerd (zoals invoervalidatie op het download-endpoint), kan de ontwikkelaar de gehele suite binnen circa twee minuten lokaal uitvoeren via:

```bash
mvn clean test
```

Dit verlaagt de drempel om code aan te passen drastisch, aangezien onbedoelde neveneffecten onmiddellijk aan het licht komen voordat de code naar productie migreert.
