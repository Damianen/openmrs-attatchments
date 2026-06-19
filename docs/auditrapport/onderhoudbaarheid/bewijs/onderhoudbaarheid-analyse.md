# Statische Code-Analyse en Onderhoudbaarheidsmeting

Dit document bevat de nulmeting en de kwalitatieve analyse van het kwaliteitsattribuut **Onderhoudbaarheid (Maintainability)** voor de OpenMRS Attachments-module. Aan de hand van de internationale ISO 25010-standaard en geautomatiseerde analysetools (SonarCloud en JaCoCo) is de technische schuld van de codebase in kaart gebracht om de lange-termijn beheerbaarheid van de software te evalueren.

---

## 1. Non-Functional Requirements (NFR's) volgens ISO 25010

Om de onderhoudbaarheid objectief meetbaar te maken, zijn op basis van de ISO 25010-standaard drie specifieke subkarakteristieken gedefinieerd als Non-Functional Requirements (NFR's).

### NFR-1: Analyseerbaarheid (Analysability)

**Definitie:**
De mate waarin effectief en efficiënt kan worden beoordeeld wat de impact is van een voorgenomen wijziging, of hoe defecten in de code kunnen worden gelokaliseerd.

**Doelstelling:**
De codebase moet een lage cognitieve en cyclomatische complexiteit per methode behouden. Code-duplicatie moet worden geminimaliseerd tot een strikt minimum (<3%) om te voorkomen dat bugs zich over meerdere bestanden verspreiden.

### NFR-2: Aanpasbaarheid (Modifiability)

**Definitie:**
De mate waarin een softwareproduct effectief en efficiënt kan worden gewijzigd zonder dat dit leidt tot defecten of kwaliteitsverlies in de overige componenten.

**Doelstelling:**
Klassen moeten losgekoppeld (*loose coupling*) zijn en voldoen aan het Single Responsibility Principle. Technische schuld (*code smells*) moet proactief worden gesaneerd om de gemiddelde tijd die nodig is om een functionele wijziging door te voeren (*Maintainability Rating A*) te borgen.

### NFR-3: Testbaarheid (Testability)

**Definitie:**
De mate waarin effectief en efficiënt kan worden vastgesteld of een component of systeem aan de gestelde eisen voldoet.

**Doelstelling:**
De code moet ontworpen zijn volgens het Dependency Inversion Principle om isolatie via mocks/stubs mogelijk te maken. De geautomatiseerde testsuite moet een stabiele regeldekking (*Line Coverage*) behouden die voldoet aan de vastgestelde Quality Gate-baselines per module.

---

## 2. Methodologie van de Analyse

De onderhoudbaarheidsanalyse combineert twee complementaire benaderingen om een holistisch beeld van de codekwaliteit te verkrijgen.

```text
[ Codebase ] ───► Statische Analyse (SonarCloud)  ───► Complexiteit, Duplicatie, Code Smells
               └──► Dynamische Analyse (JaCoCo)   ───► Code Coverage Baseline
```

### Statische Code-Analyse (SonarCloud)

De broncode van zowel de `api`- als de `omod`-module wordt geautomatiseerd gescand door SonarCloud via een dedicated GitHub Actions CI-pipeline (`sonar.yml`).

SonarCloud voert abstracte syntax-boom (AST) analyses uit om code smells, duplicatie, architecturale koppeling (*coupling*) en complexiteit te meten tegenover ingebouwde Java-kwaliteitsregels.

### Dynamische Dekkingsanalyse (JaCoCo)

Tijdens de Maven test-lifecycle (`mvn verify`) meet de JaCoCo-agent welke codepaden daadwerkelijk worden geraakt door de JUnit-testsuite.

Dit resultaat wordt geëxporteerd als een XML-rapport (`target/site/jacoco/jacoco.xml`) en ingelezen door SonarCloud om de relatie tussen code-architectuur en testdekking te visualiseren.

---

## 3. Onderhoudbaarheidsmetrieken (Nulmeting Baseline)

> **Nulmeting (eerste SonarCloud-scan, 2026-06-18).** Onderstaande tabellen bevatten de meetwaarden uit de *eerste* SonarCloud-scan van het project — de nulmeting voor Taak A1. De waarden zijn projecttotalen zoals gerapporteerd op het SonarCloud-dashboard (project `Damianen_openmrs-attatchments`).
>
> **Disclaimer test-dekking:** in deze eerste scan is géén code coverage vastgelegd (`line`/`branch` = geen data), waarschijnlijk omdat de tests in die eerste CI-run nog niet met JaCoCo-instrumentatie zijn uitgevoerd. De tweede scan (2026-06-19) bevat wél dekking — zie §3.3 hieronder.

### 3.1 Algemene Code-Grootte en Structuur

| Metriek             | API Module (api) | OMOD Module (omod) | Totaal Project |
| ------------------- | ---------------- | ------------------ | -------------- |
| Lines of Code (LOC) | —¹               | —¹                 | 3.801          |
| Aantal Klassen      | —¹               | —¹                 | 28             |

¹ De eerste scan registreerde alleen het projecttotaal; een uitsplitsing per module is niet apart vastgelegd.

### 3.2 ISO 25010 Metriekenoverzicht (SonarCloud Data)

| ISO 25010 Karakteristiek | Specifieke Tool-Metriek                | Gemeten Waarde        | Status vs. Target                |
| ------------------------ | -------------------------------------- | --------------------- | -------------------------------- |
| Analyseerbaarheid        | Gedupliceerde Regels (%)               | 0,0 %                 | ✅ OK (ruim binnen norm < 3 %)    |
| Analyseerbaarheid        | Cyclomatische Complexiteit (V(G))      | 400                   | ℹ️ Projecttotaal; triage in §4   |
| Analyseerbaarheid        | Cognitieve Complexiteit                | 255                   | ℹ️ Projecttotaal; triage in §4   |
| Aanpasbaarheid           | Totaal aantal Code Smells              | 116                   | ⚠️ Aandachtspunt (zie §4)        |
| Aanpasbaarheid           | Technische Schuld (Tijd in uren/dagen) | 868 min (≈ 14 u 28 m) | ⚠️ ± 1,8 werkdag technische schuld |
| Aanpasbaarheid           | SonarCloud Maintainability Rating      | A (1.0)               | ✅ Doel (Rating A) behaald        |
| Testbaarheid             | Regelfoutdekking (Line Coverage)       | n.v.t. (geen data)    | ⚠️ Niet gemeten in scan 1        |
| Testbaarheid             | Takdekking (Branch Coverage)           | n.v.t. (geen data)    | ⚠️ Niet gemeten in scan 1        |

### 3.3 Tweede scan (2026-06-19, ter referentie)

> **Disclaimer:** onderstaande cijfers komen uit de *tweede* SonarCloud-scan (2026-06-19) en dienen uitsluitend ter referentie. De nulmeting hierboven (§3.1–§3.2) blijft de officiële baseline voor Taak A1. De verschillen ontstaan doordat de tweede scan ná verdere wijzigingen draaide en, anders dan de eerste scan, wél test-dekking registreerde. Het project kent in totaal twee scans; dit is de meest recente.

| Metriek                          | Eerste scan (nulmeting) | Tweede scan (2026-06-19) |
| -------------------------------- | ----------------------- | ------------------------ |
| Lines of Code (LOC)              | 3.801                   | 3.469                    |
| Totaal aantal Code Smells        | 116                     | 186                      |
| Technische Schuld                | 868 min                 | 1.226 min                |
| Regelfoutdekking (Line Coverage) | n.v.t. (geen data)      | 75,3 %                   |
| Takdekking (Branch Coverage)     | n.v.t. (geen data)      | 49,4 %                   |

---

## 4. Initiële Triage en Analyse van Technische Schuld

> Op basis van de nulmeting (§3) en een doorsnede van de broncode is hieronder een kwalitatieve triage gemaakt van de grootste bronnen van technische schuld. Dit vormt de directe input voor de geprioriteerde verbeterlijst in het Verbeteringen-document. De exacte per-regel-uitsplitsing van code smells en de precies als *Highly Complex* gemarkeerde methoden zijn te raadplegen op het SonarCloud-dashboard (project `Damianen_openmrs-attatchments`).

### Hotspots in Complexiteit

De cyclomatische complexiteit van het project bedraagt **400** en de cognitieve complexiteit **255**, verdeeld over 3.801 regels code. Dat is een gematigd profiel en verklaart mede waarom de Maintainability Rating op **A** blijft staan: geen enkele methode domineert het geheel. De complexiteit concentreert zich echter in een klein aantal grote klassen:

- **`AttachmentsContext`** (api, 1.093 regels) is veruit de grootste klasse en fungeert als centrale facade/hub; door zijn omvang en vertakte logica is dit de meest waarschijnlijke drager van de hoogste per-methode-complexiteit.
- **`AttachmentResource`** (omod, 609 regels) is de grootste REST-resource en bundelt validatie, upload- en downloadafhandeling in één klasse.
- De bestands- en beeldverwerking (streaming, thumbnails) zit in de handler-keten `AbstractAttachmentHandler` → `ImageAttachmentHandler` / `DefaultAttachmentHandler` plus `ComplexObsSaver`; dit is functioneel de meest vertakte code (conditionele logica op content-type).

### Geconstateerde Code Smells

SonarCloud telt **116 code smells** met een geschatte hersteltijd van **868 minuten (≈ 14 u 28 min)**. Omdat de Maintainability Rating desondanks **A** is, gaat het overwegend om laag-severity smells en niet om blocker- of critical-issues. De smells concentreren zich logischerwijs in de grootste klassen (`AttachmentsContext`, `AttachmentResource`). Een concreet, in de broncode verifieerbaar voorbeeld van *self-admitted technical debt* (Sonar-regel `java:S1135`) staat in `AttachmentsContext.java:383` (`// TODO: Figure out if this is good enough`). De volledige per-regel-verdeling (o.a. ongebruikte imports, deprecated API-aanroepen, te diep geneste logica) is op het dashboard onder *Issues → Code Smell* in te zien.

### Architectonische Koppeling

Twee koppelingspatronen verdienen aandacht:

1. **REST-laag ↔ domein/persistentie.** `AttachmentResource` (omod) importeert rechtstreeks OpenMRS-domeinentiteiten (`Obs`, `Patient`, `Encounter`) en roept daarnaast `org.openmrs.api.context.Context` en services (o.a. `EncounterService`) aan. De REST-laag werkt dus deels direct met persistente entiteiten in plaats van uitsluitend via de `AttachmentsContext`-facade — een vorm van tight coupling tussen de presentatie- en datalaag.
2. **Centrale hub.** `AttachmentsContext` wordt door 10 klassen in beide modules gebruikt. Deze hoge afferente koppeling maakt de klasse een single point of change: een wijziging erin raakt een groot deel van de codebase. Het opsplitsen van deze facade is daarmee de meest impactvolle modifieerbaarheids-verbetering (zie Verbeteringen-document).
