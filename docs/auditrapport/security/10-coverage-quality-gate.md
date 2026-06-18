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
- `mvn -pl omod "-Dformatter.skip=true" "-Dtest=AttachmentResourceTest,AttachmentRestControllerTest,AttachmentBytesResourceTest,ObsByConceptListSearchHandlerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test jacoco:report`

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
| `omod` security subset | 57 tests | 79.2% | 69.8% | Boven de 60% startnorm voor de security-relevante OMOD testset. |

De CI-run bevestigt dat beide rapporten als artifact worden bewaard. De gedownloade HTML-overzichten zijn daarna inhoudelijk bekeken en als bewijs toegevoegd. Na het toevoegen van de concept-search loggingtest is de lokale OMOD-baseline hoger dan het eerder vastgelegde CI-screenshot; de volgende PR-run ververst het artifact opnieuw.

## 6. CI artifact baseline

De GitHub Actions artifacts zijn geopend via het JaCoCo HTML-overzicht. Dit geeft de volgende afgeronde waarden:

| Artifact | Screenshot | Instruction coverage | Line coverage | Branch coverage | Beoordeling |
|---|---|---:|---:|---:|---|
| `api-coverage-report` | `bewijs/scanning/jacoco-api-coverage-overview.png` | 75% | 74% | 28% | Boven de 60% startnorm; branch coverage blijft verbeterpunt. |
| `omod-security-coverage-report` | `bewijs/scanning/jacoco-omod-security-coverage-overview.png` | 66% | 66% | 56% | Boven de 60% startnorm voor de security-relevante OMOD testset. |

## 7. Quality gate

| Check | Status | Toelichting |
|---|---|---|
| API tests | Ingericht | Draait in `Maven Tests` workflow. |
| OMOD security regressietests | Ingericht | Draait gericht op attachment security tests. |
| Coverage rapport | Ingericht | JaCoCo rapport wordt gegenereerd. |
| Coverage artifact | Ingericht | GitHub Actions uploadt rapporten als artifact. |
| Hard coverage fail threshold | Bewust niet hard afgedwongen in Sprint 3 | Coverage wordt beoordeeld via de 60% reviewnorm, JaCoCo artifacts en PR-review. |

We kiezen in Sprint 3 voor meten, rapporteren en reviewen in plaats van een harde technische fail threshold. De reden is dat dit een legacy/OpenMRS-module is met frameworkafhankelijkheden. Een harde grens kan zonder langere baseline onnodig PR's blokkeren, terwijl gerichte security regressietests belangrijker zijn dan een enkel totaalpercentage.

Het besluit voor Sprint 3 is daarom:

- 60% line coverage blijft de startnorm voor security-relevante code;
- API en OMOD security coverage zitten boven deze norm;
- JaCoCo artifacts blijven onderdeel van het PR-bewijs;
- een harde Maven/JaCoCo fail threshold wordt pas later toegevoegd als het team dit wil en de baseline stabiel genoeg is.

## 8. Bewijs

De eerste succesvolle GitHub Actions run is vastgelegd in:

- `bewijs/scanning/maven-tests-coverage-artifacts-success.png`
- `bewijs/scanning/jacoco-api-coverage-overview.png`
- `bewijs/scanning/jacoco-omod-security-coverage-overview.png`

Deze screenshots tonen:

- groene `Maven Tests` workflow;
- artifact `api-coverage-report`;
- artifact `omod-security-coverage-report`;
- beide jobs `api-tests` en `omod-security-tests` succesvol afgerond.
- API JaCoCo-overzicht met 74% line coverage;
- OMOD JaCoCo-overzicht met 66% line coverage.

## 9. Open acties

| Actie | Status |
|---|---|
| Eerste CI-run met coverage artifacts uitvoeren | Uitgevoerd; bewijs in `bewijs/scanning/maven-tests-coverage-artifacts-success.png` |
| Coveragepercentages uit artifact vergelijken met lokale baseline | Uitgevoerd; JaCoCo HTML-screenshots staan in de bewijsmap |
| Beslissen of harde minimumdrempel nodig is | Besloten: niet hard afdwingen in Sprint 3; wel 60% reviewnorm en JaCoCo artifact |
| Screenshots toevoegen aan bewijsmap | Uitgevoerd voor workflow, artifacts en JaCoCo HTML-overzichten |

## 10. Conclusie

JaCoCo maakt de testcoverage zichtbaar en de GitHub Actions workflow bewaart de rapporten als artifact. Daarmee is Sprint 3 beter aantoonbaar: testresultaten zijn niet alleen groen, maar ook meetbaar. De artifactrapporten zijn inhoudelijk beoordeeld en zitten boven de gekozen 60% startnorm. Voor Sprint 3 is besloten om geen harde technische coverage-threshold af te dwingen, maar coverage te beoordelen via artifactbewijs en PR-review.
