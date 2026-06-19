# Validatie van de A3-refactor — onderhoudbaarheid zonder regressie

## 1. Doel en context

Dit is de **A4-stap** ("Validatie /20"). Hij toont aan dat de A3-refactor
(zie [`bewijs/aangepast-ontwerp.md`](bewijs/aangepast-ontwerp.md) en PR #33,
commit `9948669`) de onderhoudbaarheid en security van de module heeft verbeterd
**zonder regressie** in de bestaande functionaliteit.

A3 verwijderde de **hardcoded storage-credentials** uit `AttachmentsActivator.java`
en leest ze voortaan uit de runtime-omgeving via een kleine, single-responsibility
`StorageConfig` (met `EnvironmentSource` als DIP-seam). De hele wijziging zit in de
**`api`**-module; de `omod`-module is door A3 **niet** aangeraakt.

Eerlijke verwachting vooraf: een *secrets-naar-omgeving*-refactor verschuift de
complexiteitscijfers nauwelijks. De meetbare winst zit niet in een complexiteitsdaling,
maar in (a) het **verdwijnen van de hardcoded-secret-bevinding** die SonarCloud als
security hotspot/vulnerability markeert, en (b) **betere modifieerbaarheid**: credentials
zijn nu te roteren via de omgeving zonder codewijziging, en staan niet meer in version
control of de applicatiedatabase. Dit document rapporteert de cijfers recht-toe-recht-aan
en benoemt expliciet wat *niet* is verbeterd (sectie 8).

## 2. Reproduceerbare commando's

De build vereist **JDK 8** (de PowerMock-tests breken op nieuwere JDK's).

```bash
# Volledige suite + JaCoCo, identiek aan wat CI draait (formatter overgeslagen
# zodat de werkboom schoon blijft):
JAVA_HOME=/usr/lib/jvm/java-8-openjdk \
  mvn -B "-Dformatter.skip=true" clean verify

# JaCoCo-rapporten (HTML + XML) per module, gegenereerd in de verify-fase:
#   api/target/site/jacoco/index.html      (en jacoco.xml)
#   omod/target/site/jacoco/index.html     (en jacoco.xml)
```

Optioneel, om de `omod`-coverage 1-op-1 met de Sprint 3-baseline (de security-subset)
te vergelijken — dit was het commando uit
[`security/10-coverage-quality-gate.md`](../security/10-coverage-quality-gate.md) §2:

```bash
JAVA_HOME=/usr/lib/jvm/java-8-openjdk mvn -pl omod -am \
  "-Dformatter.skip=true" \
  "-Dtest=AttachmentResourceTest,AttachmentRestControllerTest,AttachmentBytesResourceTest,ObsByConceptListSearchHandlerTest" \
  "-Dsurefire.failIfNoSpecifiedTests=false" \
  test jacoco:report
```

Meetdatum van de cijfers in dit document: **2026-06-18**, lokale run, `BUILD SUCCESS`,
totale buildtijd ~29 s.

## 3. Lokaal meetbaar: before → after

### 3.1 Testaantallen (no-regression-bewijs)

| Module | Baseline (vóór A3) | Na A3 | Δ | Resultaat |
|---|---:|---:|---:|---|
| `api` | 13 tests | **15 tests** | +2 | alle groen (0 failures, 0 errors) |
| `omod` (volledige suite) | 57 tests (security-subset) | **60 tests** | — | alle groen (0 failures, 0 errors) |

De +2 in `api` zijn de twee `StorageConfigTest`-tests die A3 toevoegde
(credentials komen uit de omgeving; ontbrekende variabele faalt fail-fast).
De `omod`-suite (60 tests) bevat de 57 tests van de Sprint 3-security-subset
plus de 3 integratietests van `AttachmentResourceIntegrationTest`; A3 wijzigde
hier geen code, dus dit is puur een regressiecontrole.

Per testklasse (na A3):

| Module | Testklasse | Tests |
|---|---|---:|
| `api` | `AttachmentsContextTest` | 2 |
| `api` | `AttachmentsServiceImplTest` | 1 |
| `api` | `StorageConfigTest` *(nieuw in A3)* | 2 |
| `api` | `obs.DefaultAttachmentHandlerTest` | 3 |
| `api` | `obs.ImageAttachmentHandlerTest` | 6 |
| `api` | `obs.ValueComplexTest` | 1 |
| `omod` | `rest.AttachmentBytesResourceTest` | 4 |
| `omod` | `rest.AttachmentResourceIntegrationTest` | 3 |
| `omod` | `rest.AttachmentResourceTest` | 19 |
| `omod` | `rest.AttachmentRestControllerTest` | 23 |
| `omod` | `rest.ObsByConceptListSearchHandlerTest` | 11 |
| | **Totaal** | **75** |

### 3.2 Coverage (JaCoCo)

| Module | Metric | Baseline (vóór A3) | Na A3 | Beoordeling |
|---|---|---:|---:|---|
| `api` | Line | 74% | **74,3%** | stabiel — geen regressie; nieuwe code is gedekt |
| `api` | Instruction | 75% | **75,2%** | stabiel |
| `api` | Branch | 28–29% | **30,0%** | onveranderd zwak punt (zie §8) |
| `omod` | Line | 66% (CI-artifact) / 79% (lokaal) | **79,2%** | geen regressie; `omod` ongewijzigd door A3 |
| `omod` | Branch | 56% / 70% | **69,8%** | geen regressie |

> Toelichting op de `omod`-baseline: `security/10-coverage-quality-gate.md` noemt
> twee getallen — 66% (ouder CI-screenshot) en 79,2% (recentere lokale baseline,
> ná toevoeging van de concept-search loggingtest). Onze meting (79,2%) komt overeen
> met de recentere lokale baseline. Omdat A3 geen `omod`-code raakt, is hier per
> definitie geen coverage-effect te verwachten; de waarde dient alleen als
> regressiecontrole.

**Coverage van de nieuwe A3-code (`api`):**

| Klasse | Line coverage | Toelichting |
|---|---:|---|
| `StorageConfig` | 10/12 (83%) | Alle logica gedekt: `require()` fail-fast 4/4, alle drie de getters 1/1, test-seam-constructor 3/3. De 2 ontbrekende regels zijn de **productie-constructor** die `System.getenv` aanroept (zie §8). |
| `AttachmentsActivator` | 43/53 (81%) | De hardcoded-secret-velden zijn verwijderd; resterende dekking onveranderd. |
| `EnvironmentSource` | interface | Geen uitvoerbare regels. |

## 4. SonarCloud: before → after

> Organisatie `damianen`, project `Damianen_openmrs-attatchments`.
> Geoogst **ná** het mergen van PR #33 op `main` (commit `2931cf42`); de PR-cijfers komen
> van de analyse op commit `99486699` (branch `security/storage-credentials-env`).

**Meetlacune (eerlijk):** de **vóór-staat** is niet als snapshot in SonarCloud bewaard —
de Activity-grafiek gaf een fout terug en er is geen historisch datapunt zichtbaar voor de
commit vóór #33. De conclusie dat de security-bevinding is verdwenen, steunt daarom op twee
directe waarnemingen: (a) de PR #33-analyse rapporteerde **0 new issues / 0 new hotspots**,
en (b) de huidige `main` toont **0 open security issues** en **0 hotspots "to review"**.
De "vóór"-waarden hieronder zijn dus afgeleid (≥1), geen gescreenshotte meting.

### 4.1 De kern-bevinding: hardcoded secret

| Item | Vóór A3 (afgeleid) | Na A3 (`main`, commit `2931cf42`) | Bron-screenshot |
|---|---|---|---|
| Hardcoded-credentials-bevinding op `AttachmentsActivator.java` (AWS access/secret key) | aanwezig (≥1) | **verdwenen** | `sonar-na-hotspots-leeg.png`, `sonar-na-issues-security-0.png` |
| Open Security issues | ≥1 | **0** | `sonar-na-issues-security-0.png` |
| Security Hotspots "to review" | ≥1 | **0** (100% reviewed) | `sonar-na-hotspots-leeg.png` |
| Security Rating | onbekend | **A** | `sonar-na-main-summary.png` |

### 4.2 PR #33 — eigen analyse (commit `99486699`)

| Metric | Waarde op de PR |
|---|---|
| Quality Gate | ✅ Passed |
| New Issues | 0 |
| New Security Hotspots | 0 |
| New Lines | 0 |
| Duplications (estimated after merge) | 0,0% |

Bron-screenshot: `sonar-pr33-summary.png`.

### 4.3 Overige dashboard-metrics op `main` (no-regression op kwaliteit)

Huidige waarden op `main`. Ze tonen dat A3 geen kwaliteit verslechterd heeft — en, zoals
verwacht voor een secrets→env-refactor, ook niet structureel verbeterd heeft (zie §8).

| Metric | Na A3 (`main`) | Beoordeling |
|---|---|---|
| Quality Gate | ✅ Passed (Sonar way) | — |
| Security Rating / open issues | A / 0 | verbeterd (bevinding weg) |
| Reliability Rating / open issues | C / 5 | ongewijzigd (zie §8) |
| Maintainability Rating / open issues | A / 116 | ongewijzigd (zie §8) |
| Duplications | 0,0% (op 5,1k regels) | al schoon vóór A3 |
| Coverage | – (niet gemeten in SonarCloud) | zie §4.4 |
| Lines of Code | 3,8k | — |

### 4.4 Coverage-lacune in SonarCloud

SonarCloud toont **geen** coverage (`–`): de JaCoCo-rapporten worden in de Sonar-workflow
niet gegenereerd vóór de scan, dus SonarCloud krijgt geen coveragedata. De coveragecijfers
in §3.2 komen daarom uit de **lokale `mvn verify` + JaCoCo**-run (stap 3–4 van de
reproduceercommando's); dat is voor dit rapport de enige coveragebron. Wil je coverage ook
in SonarCloud zien, laat de Sonar-workflow dan eerst `test jacoco:report` draaien zodat
`jacoco.xml` bestaat vóór de scan (de paden staan al in de parent-`pom.xml` via
`sonar.coverage.jacoco.xmlReportPaths`).

## 5. Narratief: waarom dit aan A3 toe te schrijven is

- **De security-bevinding verdwijnt direct door de codewijziging.** A3 verwijderde
  de drie `private static final` credential-velden (de `AKIA-…`-key, de secret key en
  de bucket) uit `AttachmentsActivator.java`. Dat zijn precies de regels die SonarCloud
  via de secrets-regels (hardcoded credentials) markeert. Geen velden → geen bevinding.
- **Modifieerbaarheid verbetert structureel.** Credentials zijn nu te roteren via de
  omgeving zonder code te wijzigen of te herbouwen; ze staan niet meer in version control
  én niet in de OpenMRS global properties (database). De `EnvironmentSource`-abstractie
  (Dependency Inversion) maakt de credential-resolutie bovendien testbaar zonder de echte
  procesomgeving te muteren — daar komen de twee nieuwe `StorageConfigTest`-tests vandaan.
- **Single Responsibility.** De activator hoeft niets meer van credentials te weten;
  `StorageConfig` is de enige bron van waarheid. Dit is de onderhoudbaarheidswinst die
  het [aangepaste ontwerp](bewijs/aangepast-ontwerp.md) beoogde.

Wat A3 bewust **niet** doet, is de algehele complexiteit of technische schuld van de
legacy-module omlaag brengen — dat was ook niet het doel van deze refactor.

## 6. No-regression-verklaring

> De volledige testsuite (`mvn verify`, JDK 8) eindigt op **2026-06-18** met
> **`BUILD SUCCESS`**: **75 tests, 0 failures, 0 errors, 0 skipped** (15 in `api`,
> 60 in `omod`). De `api`-line-coverage blijft 74,3% en de `omod`-line-coverage 79,2%;
> beide liggen boven de 60%-startnorm uit Sprint 3 en vertonen geen daling.
> De twee nieuwe tests dekken de nieuwe A3-code (`StorageConfig` 83% line, alle logica
> 100%). Hiermee is aangetoond dat de A3-refactor de bestaande functionaliteit niet
> heeft gebroken: een volledig groene run is het regressiebewijs.

## 7. Welke SonarCloud-screenshots verzamelen

Sla alle afbeeldingen op in [`bewijs/validatie/`](bewijs/validatie/) (checklist in
[`bewijs/validatie/README.md`](bewijs/validatie/README.md)). De cijfers in sectie 4 zijn
al ingevuld op basis van de oogst; deze screenshots zijn het visuele bewijs daarbij.

| # | Wat te screenshotten | URL | Bestandsnaam |
|---|---|---|---|
| 1 | Ná-staat: Summary `main` (Quality Gate Passed, Security A, 0 issues) | `https://sonarcloud.io/summary/overall?id=Damianen_openmrs-attatchments` | `sonar-na-main-summary.png` |
| 2 | Ná-staat: Security Hotspots `main` (100% reviewed, 0 "to review") | `https://sonarcloud.io/project/security_hotspots?id=Damianen_openmrs-attatchments` | `sonar-na-hotspots-leeg.png` |
| 3 | Ná-staat: Issues `main` gefilterd op Security (0 open security issues) | `https://sonarcloud.io/project/issues?issueStatuses=OPEN%2CCONFIRMED&id=Damianen_openmrs-attatchments` | `sonar-na-issues-security-0.png` |
| 4 | PR #33 Summary (Quality Gate Passed, 0 New Issues, 0 Hotspots) | `https://sonarcloud.io/summary/new_code?id=Damianen_openmrs-attatchments&pullRequest=33` | `sonar-pr33-summary.png` |
| 5 | *(optioneel/historisch)* Vóór-staat: Activity-datapunt vóór commit `99486699` | `https://sonarcloud.io/project/activity?id=Damianen_openmrs-attatchments&branch=main` | `sonar-voor-activity.png` |

> Screenshot 5 is op dit moment **niet beschikbaar**: de Activity-pagina geeft een fout
> terug, dus de vóór-staat kan niet als snapshot worden vastgelegd (zie de meetlacune in
> sectie 4).

## 8. Eerlijke kanttekeningen — wat (nog) niet verbeterde

We rapporteren dit recht-toe-recht-aan in plaats van te overclaimen:

- **Cyclomatische complexiteit — ongewijzigd.** De refactor verplaatste alleen waarden
  naar omgevingsvariabelen; er is geen structuurwijziging die de complexiteit verlaagt.
- **Maintainability — ongewijzigd.** SonarCloud toont op `main` **116 open code smells**
  (Rating A); A3 raakte geen maintainability-paden.
- **Reliability — ongewijzigd.** Rating **C** met **5 open bugs**; A3 loste deze niet op
  en claimt dat ook niet.
- **Duplications — al schoon.** 0,0% vóór én na A3; hier is geen verbetering te claimen.
- **Coverage in SonarCloud — niet gemeten.** SonarCloud krijgt geen coveragedata (zie
  §4.4); de lokale JaCoCo-cijfers (§3.2) zijn de enige coveragebron.
- **`api`-branch-coverage blijft laag (~30%).** Bestaand verbeterpunt van de legacy-module,
  vóór A3 al zo; line-coverage blijft wél stabiel.
- **De productie-constructor van `StorageConfig` is niet ge-unit-test** (de 2 ontbrekende
  regels: de anonieme `EnvironmentSource` die `System.getenv` aanroept). Dat is bewust:
  de DIP-seam bestaat juist zodat tests een stub injecteren in plaats van de echte
  procesomgeving te muteren (wat in JUnit 4 niet zonder extra libraries kan). Alle
  *logica* — `require()` fail-fast en de getters — is 100% gedekt.
- **`omod` is ongewijzigd door A3.** De `omod`-cijfers tonen daarom alleen aan dat er
  geen regressie optreedt; ze zijn geen bewijs van een A3-verbetering.
- **Vóór-staat niet als snapshot bewaard** (Activity-grafiek-fout); de "bevinding weg"-
  conclusie is afgeleid uit de PR-analyse (0 new issues) + huidige `main` (0 security
  issues), niet uit een gescreenshotte vóór-meting.

## 9. Conclusie

De A3-refactor is gevalideerd. **Lokaal:** de volledige suite draait groen (75 tests,
0 fouten) en de coverage daalt niet — het regressiebewijs. **SonarCloud** (`main`, commit
`2931cf42`): Quality Gate **Passed**, **Security Rating A**, **0 open security issues** en
**0 hotspots "to review"**; de PR #33-analyse introduceerde **0 nieuwe issues**. Daarmee is
de hardcoded-secret-bevinding aantoonbaar verdwenen. De winst zit in security + een beter
modifieerbaar, testbaar ontwerp (SRP + DIP), niet in complexiteit, reliability of
maintainability — die blijven, zoals verwacht voor een secrets→env-refactor, ongewijzigd
(zie §8). De enige openstaande meetlacune is de niet-bewaarde vóór-staat in SonarCloud en
het ontbreken van coverage in SonarCloud (§4); beide zijn hierboven expliciet benoemd.
