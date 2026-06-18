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

## 4. SonarCloud: before → after (in te vullen)

> Organisatie `damianen`, project `Damianen_openmrs-attatchments`.
> Vul de getallen hieronder in vanaf het SonarCloud-dashboard en plak de screenshots
> volgens de checklist in sectie 7. "Vóór A3" = een analyse op een commit/branch
> vóór de A3-merge; "Na A3" = de analyse op `main` ná de A3-merge.

### 4.1 De kern-bevinding: hardcoded secret

| Item | Vóór A3 | Na A3 | Bron-screenshot |
|---|---|---|---|
| Hardcoded-credentials hotspot/issue op `AttachmentsActivator.java` (AWS access/secret key) | _(aanwezig — bv. status To Review / Open)_ | _(verdwenen / status Fixed)_ | `sonar-hotspot-before.png` → `sonar-hotspot-after.png` |
| Aantal open Security Hotspots | _(in te vullen)_ | _(in te vullen)_ | idem |
| Security Review Rating | _(in te vullen, bv. E)_ | _(in te vullen, bv. A)_ | overview |

### 4.2 Overige dashboard-metrics (no-regression op kwaliteit)

| Metric | Vóór A3 | Na A3 | Verwachting |
|---|---|---|---|
| Quality Gate status | _(in te vullen)_ | _(Passed)_ | moet **Passed** zijn |
| Bugs | _(in te vullen)_ | _(in te vullen)_ | gelijk of lager |
| Vulnerabilities | _(in te vullen)_ | _(in te vullen)_ | gelijk of lager |
| Code Smells | _(in te vullen)_ | _(in te vullen)_ | gelijk of lager |
| Maintainability (SQALE) Rating | _(in te vullen)_ | _(in te vullen)_ | geen verslechtering |
| Reliability Rating | _(in te vullen)_ | _(in te vullen)_ | geen verslechtering |
| Security Rating | _(in te vullen)_ | _(in te vullen)_ | gelijk of beter |
| Duplications % | _(in te vullen)_ | _(in te vullen)_ | gelijk of lager |
| Coverage | _(in te vullen)_ | _(in te vullen)_ | gelijk of hoger |

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

## 7. Welke SonarCloud-cijfers en screenshots te verzamelen

Sla alle afbeeldingen op in [`bewijs/validatie/`](bewijs/validatie/) (zie de checklist in
[`bewijs/validatie/README.md`](bewijs/validatie/README.md)). Maak telkens een **vóór-A3**-
en een **na-A3**-opname zodat het verschil zichtbaar is.

Handige URL's:

- Overview: `https://sonarcloud.io/project/overview?id=Damianen_openmrs-attatchments`
- Security Hotspots: `https://sonarcloud.io/project/security_hotspots?id=Damianen_openmrs-attatchments`
- Issues: `https://sonarcloud.io/project/issues?id=Damianen_openmrs-attatchments`
- Activity (historie): `https://sonarcloud.io/project/activity?id=Damianen_openmrs-attatchments`

| # | Screenshot | Wat moet erop staan | Bestandsnaam |
|---|---|---|---|
| 1 | **Hotspot vóór A3** | Security Hotspots-tab op een commit/branch vóór A3: de hardcoded-credentials-hotspot op `AttachmentsActivator.java` (regel met `AKIA-…`), status open/To Review. | `sonar-hotspot-before.png` |
| 2 | **Hotspot na A3** | Security Hotspots-tab op `main` ná A3: de hotspot is weg (0 open) of staat op Fixed; `AttachmentsActivator.java` wordt niet meer gemarkeerd. | `sonar-hotspot-after.png` |
| 3 | **Overview na A3** | Projecthome met **Quality Gate Passed** en de measure-tegels (Security/Reliability/Maintainability rating, Security Hotspots Reviewed 100%, Coverage, Duplications). | `sonar-overview-after.png` |
| 4 | **Overview vóór A3** *(optioneel)* | Dezelfde tegels op de pre-A3-analyse, voor een directe vergelijking van de ratings. | `sonar-overview-before.png` |
| 5 | **Activity/historie** *(optioneel)* | De activiteitengrafiek waarin het aantal Security Hotspots / Vulnerabilities daalt rond de A3-analyse. | `sonar-activity-hotspots.png` |

Noteer bij het maken van de screenshots ook de getallen zelf en vul daarmee de tabellen
in sectie 4 in (dashboard-tegels: Bugs, Vulnerabilities, Security Hotspots, Code Smells,
Coverage, Duplications en de drie ratings).

## 8. Eerlijke kanttekeningen — wat (nog) niet verbeterde

We rapporteren dit recht-toe-recht-aan in plaats van te overclaimen:

- **Complexiteit / technische schuld dalen niet.** Een secrets-naar-omgeving-refactor
  raakt de cyclomatische complexiteit van de module niet noemenswaardig. De winst is
  security + modifieerbaarheid, niet een lager complexiteitscijfer.
- **`api`-branch-coverage blijft laag (~30%).** Dit was vóór A3 al zo (28–29%) en is
  een bestaand verbeterpunt van de legacy-module; A3 lost dit niet op en claimt dat ook
  niet. Line-coverage blijft wél stabiel.
- **De productie-constructor van `StorageConfig` is niet ge-unit-test** (de 2 ontbrekende
  regels: de anonieme `EnvironmentSource` die `System.getenv` aanroept). Dat is bewust:
  de DIP-seam bestaat juist zodat tests een stub injecteren in plaats van de echte
  procesomgeving te muteren (wat in JUnit 4 niet zonder extra libraries kan). Alle
  *logica* — `require()` fail-fast en de getters — is 100% gedekt.
- **`omod` is ongewijzigd door A3.** De `omod`-cijfers tonen daarom alleen aan dat er
  geen regressie optreedt; ze zijn geen bewijs van een A3-verbetering.

## 9. Conclusie

De A3-refactor is gevalideerd: de volledige suite draait groen (75 tests, 0 fouten),
de coverage daalt niet en de nieuwe security-code is gedekt. De aantoonbare verbetering
zit in het **verdwijnen van de hardcoded-secret-bevinding** in SonarCloud en in een
**beter modifieerbaar, testbaar ontwerp** (SRP + DIP). De before/after-cijfers voor
SonarCloud worden ingevuld zodra de screenshots uit sectie 7 zijn gemaakt.
