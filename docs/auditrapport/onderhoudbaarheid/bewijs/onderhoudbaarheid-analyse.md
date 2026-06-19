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

> **Let op:** Onderstaande tabellen bevatten de placeholders voor de meetwaarden uit de eerste SonarCloud-scan (Taak A1). Zodra de scan is voltooid, worden deze cijfers definitief ingevuld.

### 3.1 Algemene Code-Grootte en Structuur

| Metriek             | API Module (api) | OMOD Module (omod) | Totaal Project |
| ------------------- | ---------------- | ------------------ | -------------- |
| Lines of Code (LOC) | [Plaatscijfer]   | [Plaatscijfer]     | [Plaatscijfer] |
| Aantal Klassen      | [Plaatscijfer]   | [Plaatscijfer]     | [Plaatscijfer] |

### 3.2 ISO 25010 Metriekenoverzicht (SonarCloud Data)

| ISO 25010 Karakteristiek | Specifieke Tool-Metriek                | Gemeten Waarde   | Status vs. Target    |
| ------------------------ | -------------------------------------- | ---------------- | -------------------- |
| Analyseerbaarheid        | Gedupliceerde Regels (%)               | [Plaatscijfer] % | [Oordeel (bijv. OK)] |
| Analyseerbaarheid        | Cyclomatische Complexiteit (V(G))      | [Plaatscijfer]   | [Oordeel]            |
| Analyseerbaarheid        | Cognitieve Complexiteit                | [Plaatscijfer]   | [Oordeel]            |
| Aanpasbaarheid           | Totaal aantal Code Smells              | [Plaatscijfer]   | [Oordeel]            |
| Aanpasbaarheid           | Technische Schuld (Tijd in uren/dagen) | [Plaatscijfer]   | [Oordeel]            |
| Aanpasbaarheid           | SonarCloud Maintainability Rating      | [Rating A-E]     | [Oordeel]            |
| Testbaarheid             | Regelfoutdekking (Line Coverage)       | [Plaatscijfer] % | [Oordeel]            |
| Testbaarheid             | Takdekking (Branch Coverage)           | [Plaatscijfer] % | [Oordeel]            |

---

## 4. Initiële Triage en Analyse van Technische Schuld

> Zodra de SonarCloud-dashboards groen uitslaan, wordt hier op basis van de hotspots een kwalitatieve analyse geschreven. Dit vormt de directe input voor de geprioriteerde verbeterlijst in het Verbeteringen-document.

### Hotspots in Complexiteit

[Beschrijf hier morgen welke klassen of methoden door SonarCloud als "Highly Complex" zijn gemarkeerd, bijvoorbeeld in de bestands-streaming of thumbnail-afhandeling.]

### Geconstateerde Code Smells

[Noteer hier de meest voorkomende typen code smells, zoals ongebruikte imports, deprecated API-aanroepen of te diep geneste loops.]

### Architectonische Koppeling

[Analyseer of er sprake is van ongewenste tight-coupling tussen de REST-laag en de database-entities.]
