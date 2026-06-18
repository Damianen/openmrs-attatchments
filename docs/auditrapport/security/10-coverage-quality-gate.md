# Coverage en quality gate - Sprint 3

## 1. Doel

Sprint 3 vraagt om testcoverage inzichtelijk te maken en te onderbouwen welk coveragepercentage redelijk is. Dit document beschrijft hoe coverage wordt gemeten, hoe het rapport uit CI komt en welke ondergrens voor dit project passend is.

Coverage is geen volledige securitygarantie. Het bewijst vooral dat belangrijke codepaden door tests geraakt worden. Daarom combineren we coverage met security regressietests, CodeQL, Snyk, SBOM en review.

## 2. Tooling

| Onderdeel | Keuze |
|---|---|
| Coverage tool | JaCoCo Maven Plugin |
| Build | Maven, Java 8 |
| CI | GitHub Actions `Maven Tests` |
| Rapportvorm | HTML en XML rapport per module |
| Artifact | `api-coverage-report` en `omod-security-coverage-report` |

De JaCoCo plugin staat in de parent `pom.xml`. De workflow draait:

- `mvn -pl api "-Dformatter.skip=true" test jacoco:report`
- `mvn -pl omod "-Dformatter.skip=true" "-Dtest=AttachmentResourceTest,AttachmentRestControllerTest,AttachmentBytesResourceTest" "-Dsurefire.failIfNoSpecifiedTests=false" test jacoco:report`

Daarna uploadt GitHub Actions de rapporten als artifacts.

## 3. Scope van coverage

| Module | Wat wordt gemeten | Waarom |
|---|---|---|
| `api` | Service-, context- en handlerlogica | Hier zitten file storage, logging en attachment helperlogica. |
| `omod` | REST-resource en security regressietests | Hier zitten upload, download, autorisatie, MIME-validatie en REST-ingangen. |

De `omod` coverage is bewust gekoppeld aan de security regressietests. Dat geeft vooral inzicht in de dekking van de risicovolle REST-codepaden, niet in elke UI- of frameworkroute.

## 4. Gekozen coveragepercentage

Voor dit project is een startnorm van **60% line coverage op de security-relevante modulecode** redelijk.

Onderbouwing:

- De module is legacy/OpenMRS-code en sterk afhankelijk van OpenMRS framework, Spring REST en complex obs integration.
- 100% coverage is niet realistisch of nuttig binnen deze sprint.
- De belangrijkste risico's zitten in specifieke codepaden: uploadvalidatie, download-autorisatie, file access, base64 parsing, patientbinding en veilige logging.
- Voor deze risicopaden zijn gerichte regressietests belangrijker dan brede oppervlakkige coverage.
- Als coverage onder 60% blijft, moet worden uitgelegd welke code buiten scope valt en welke kritieke paden wel getest zijn.

Deze 60% is een startpunt. Voor nieuwe securitygevoelige code geldt een hogere verwachting: nieuwe mitigaties moeten met gerichte tests worden toegevoegd.

## 5. Lokale baseline

De workflowcommando's zijn lokaal uitgevoerd met JaCoCo. Dit geeft de volgende eerste baseline:

| Module | Tests | Line coverage | Branch coverage | Beoordeling |
|---|---|---:|---:|---|
| `api` | 13 tests | 74.3% | 29.0% | Boven de 60% startnorm voor line coverage; branch coverage blijft verbeterpunt. |
| `omod` security subset | 46 tests | 66.6% | 57.0% | Boven de 60% startnorm voor de security-relevante OMOD testset. |

De CI-run moet deze baseline nog bevestigen met artifacts uit GitHub Actions.

## 6. Quality gate

| Check | Status | Toelichting |
|---|---|---|
| API tests | Ingericht | Draait in `Maven Tests` workflow. |
| OMOD security regressietests | Ingericht | Draait gericht op attachment security tests. |
| Coverage rapport | Ingericht | JaCoCo rapport wordt gegenereerd. |
| Coverage artifact | Ingericht | GitHub Actions uploadt rapporten als artifact. |
| Hard coverage fail threshold | Nog niet hard afgedwongen | Eerst meten en beoordelen; daarna eventueel minimum afdwingen. |

We kiezen eerst voor meten en rapporteren. Een harde fail threshold wordt pas toegevoegd nadat de eerste CI-run laat zien wat de realistische baseline is. Dit voorkomt dat legacy-code zonder analyse de pipeline blokkeert, terwijl de securityrelevante testdekking wel zichtbaar wordt.

## 7. Bewijs

De eerste succesvolle GitHub Actions run is vastgelegd in:

- `bewijs/scanning/maven-tests-coverage-artifacts-success.png`

Deze screenshot toont:

- groene `Maven Tests` workflow;
- artifact `api-coverage-report`;
- artifact `omod-security-coverage-report`;
- beide jobs `api-tests` en `omod-security-tests` succesvol afgerond.

Eventueel kan later nog een extra screenshot van het JaCoCo HTML-overzicht worden toegevoegd, maar het belangrijkste CI-artifactbewijs is aanwezig.

## 8. Open acties

| Actie | Status |
|---|---|
| Eerste CI-run met coverage artifacts uitvoeren | Uitgevoerd; bewijs in `bewijs/scanning/maven-tests-coverage-artifacts-success.png` |
| Coveragepercentages uit artifact vergelijken met lokale baseline | Gedeeltelijk; artifacts zijn aanwezig, lokale baseline staat in dit document |
| Beslissen of harde minimumdrempel nodig is | Moet nog gedaan worden na baseline |
| Screenshots toevoegen aan bewijsmap | Uitgevoerd voor workflow en artifacts |

## 9. Conclusie

JaCoCo maakt de testcoverage zichtbaar en de GitHub Actions workflow bewaart de rapporten als artifact. Daarmee is Sprint 3 beter aantoonbaar: testresultaten zijn niet alleen groen, maar ook meetbaar. De volgende stap is bepalen of een harde coverage threshold nodig is nadat de coverage artifacts inhoudelijk zijn beoordeeld.
