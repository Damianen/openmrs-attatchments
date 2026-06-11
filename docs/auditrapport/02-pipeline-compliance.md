# Mini-complianceverslag pipeline

## 1. Doel en scope

Dit verslag legt vast in welke mate de CI/CD-pipeline en repository-inrichting van de OpenMRS Attachments Module voldoen aan een selectie van NEN-7510 / ISO 27002 controls. De beoordeling volgt de keten: control, maatregel, bewijs en status.

- **Bronnen uit de projectmap:** `Documents/Sprints & Deliverables/Sprint1.docx`, `Documents/LU2/Projectopdracht.docx`, `Documents/Software security en compliance/Hardening  Secure CICD/Voorbereiding.docx` en de repositorybestanden in `openmrs-attatchments/`.
- **In scope:** GitHub repository, branch protection / ruleset, GitHub Actions, Dependabot, CodeQL/code scanning, secret scanning, SBOM, GitHub Environments en Docker Compose configuratie.
- **Out of scope:** echte productie-infrastructuur, organisatiebrede policies buiten deze repository en runtimebeheer van een productie-OpenMRS-installatie.
- **Bewijs:** screenshots in `docs/auditrapport/bewijs/`, configuratiebestanden in de repository en teamafspraken.

De Sprint 1-opdracht vraagt om twee hoofdonderdelen: een gap-analyse op drie NEN-7510-controls en het inrichten van projectorganisatie/ontwikkelproces. Dit verslag gaat over het tweede onderdeel. De codegerichte gap-analyse staat in `01-gap-analyse.md`.

## 2. Afstemming met Sprint 1-opdracht

| Sprint 1-eis | Invulling in deze repository | Bewijs | Status |
|---|---|---|---|
| Minimaal test- en productieomgevingen aanmaken | GitHub Environments `test` en `prod` bestaan. Daarnaast is ook `dev` ingericht. | `bewijs/environments.png` | Aanwezig |
| Gescheiden configuratie | Docker Compose gebruikt een basisbestand en aparte overrides voor test en prod. Per omgeving wordt een eigen `.env` bestand gebruikt. | `docker/docker-compose.yml`; `docker/docker-compose.test.yml`; `docker/docker-compose.prod.yml`; `docker/.env.template` | Aanwezig |
| Gescheiden secrets | Echte secrets worden niet in Git opgeslagen. De `.env.template` beschrijft welke secrets per omgeving nodig zijn. GitHub environment secrets zijn nog niet ingericht, omdat er nog geen echte secrets beschikbaar zijn. | `docker/.env.template`; README | Gedeeltelijk aanwezig |
| GitHub Environments configureren | `dev`, `test` en `prod` zijn ingericht. Sprint 1 vereist minimaal test en productie. | `bewijs/environments.png` | Aanwezig |
| Protection rules en approval-gates | `prod` heeft required reviewer, wait timer en protected branches/tags. | `bewijs/environment-rules.png` | Aanwezig |
| NEN-7510-controls voor CI/CD inrichten | Branch protection, MFA, Dependabot, CodeQL/code scanning, secret scanning en SBOM zijn ingericht als CI/CD-maatregelen. | `bewijs/ruleset.png`; `bewijs/security.png`; `bewijs/dependabot.png`; `bewijs/codeql.png`; `bewijs/sbom-action-success.png` | Gedeeltelijk aanwezig |
| README beschrijft hoe omgevingen zijn ingericht | README bevat `dev`, `test` en `prod` met bijbehorende Docker Compose-bestanden. | `README.md` | Aanwezig |
| README beschrijft hoe testdata niet in productie terechtkomt | README verwijst naar testdatabeleid en beschrijft dat productiedata niet naar dev/test mag. | `README.md`; `03-testdatabeleid.md` | Aanwezig |
| README beschrijft hoe een nieuwe ontwikkelaar met de omgeving werkt | README bevat stappen voor clone, Java/Maven/Docker, `.env` en Docker Compose. | `README.md` | Aanwezig |

## 3. Compliance-overzicht

| NEN-7510 control | Doel | Pipeline-/repositorymaatregel | Bewijs | Status |
|---|---|---|---|---|
| **A.8.3 Toegangsbeveiliging** | Ongecontroleerde wijzigingen voorkomen. | Ruleset `protect-main` is ingericht voor `main`. Wijzigingen lopen via branches en pull requests. | `bewijs/ruleset.png`; teamafspraak werkwijze | Aanwezig |
| **A.8.5 Authenticatie** | Betrouwbare verificatie van gebruikers. | Teamleden gebruiken persoonlijke GitHub-accounts met MFA. | `bewijs/mfa-*.png`; teamverklaring | Aanwezig |
| **A.8.8 Kwetsbaarheidsbeheer** | Kwetsbaarheden tijdig signaleren en opvolgen. | Dependabot alerts zijn actief voor Maven dependencies. SBOM wordt gegenereerd met CycloneDX en als artifact opgeslagen. | `.github/dependabot.yml`; `.github/workflows/sbom.yml`; `bewijs/dependabot.png`; `bewijs/sbom-action-success.png`; `bewijs/sbom-artifact.png` | Aanwezig |
| **A.8.15 Logging** | Gebeurtenissen en wijzigingen navolgbaar maken. | GitHub bewaart auditsporen via commits, pull requests, workflow-runs en security findings. De codegerichte loggingrisico's staan in de gap-analyse. | GitHub activiteit; workflow-runs; `01-gap-analyse.md` | Gedeeltelijk aanwezig |
| **A.8.25 Veilige ontwikkelcyclus** | Security in het ontwikkelproces opnemen. | GitHub Environments `dev`, `test` en `prod` bestaan. `prod` heeft protection rules met required reviewer, wait timer en protected branches/tags. Docker Compose heeft aparte overrides voor test en prod. GitHub environment secrets en deployment-workflow ontbreken nog. | `bewijs/environments.png`; `bewijs/environment-rules.png`; `docker/docker-compose*.yml` | Gedeeltelijk aanwezig |
| **A.8.28 Veilig coderen** | Onveilige code vroeg detecteren. | CodeQL / code scanning is actief en uploadt scanresultaten. | `bewijs/codeql.png`; `bewijs/code-scanning.png`; `bewijs/security.png` | Aanwezig |
| **A.8.29 Beveiligingstests** | Software toetsen op beveiligingseisen. | Dependabot, CodeQL en SBOM ondersteunen security testing. Open scanbevindingen worden meegenomen in de security backlog en Sprint 2 risk assessment. | `bewijs/dependabot.png`; `bewijs/code-scanning.png`; `bewijs/sbom-action-success.png` | Gedeeltelijk aanwezig |
| **A.8.33 Testdata** | Voorkomen dat gevoelige productiegegevens in testomgevingen komen. | Testdatabeleid is vastgelegd. Docker-configuratie gebruikt gescheiden `.env` bestanden per omgeving. | `03-testdatabeleid.md`; `docker/.env.template` | Aanwezig |

## 4. Belangrijkste bewijsstukken

- **GitHub Environments:** `dev`, `test` en `prod` zijn zichtbaar in `bewijs/environments.png`.
- **Production protection:** `prod` heeft required reviewers, een wait timer en protected branches/tags in `bewijs/environment-rules.png`.
- **Dependabot:** Maven dependency alerts zijn zichtbaar in `bewijs/dependabot.png`.
- **Code scanning:** CodeQL is succesvol uitgevoerd en findings zijn zichtbaar in `bewijs/codeql.png` en `bewijs/code-scanning.png`.
- **Secret scanning:** secret scanning alerts staan ingeschakeld volgens `bewijs/security.png`.
- **SBOM:** de SBOM-workflow is succesvol uitgevoerd en levert een artifact op volgens `bewijs/sbom-action-success.png` en `bewijs/sbom-artifact.png`.
- **Testdata:** het beleid staat in `03-testdatabeleid.md`.

## 5. Beperkingen en open punten

| Open punt | Control(s) | Status |
|---|---|---|
| GitHub environment secrets zijn nog niet ingericht, omdat er nog geen echte secrets beschikbaar zijn. | A.8.25 | Nog in te richten |
| Er is nog geen deployment-workflow die expliciet `environment: test` of `environment: prod` gebruikt. | A.8.25 | Nog in te richten |
| De Maven/securitytests zijn nog geen verplichte PR quality gate. | A.8.29 | Nog aan te scherpen |
| Code scanning en Dependabot tonen open findings. Deze moeten worden beoordeeld in de security backlog en het risk assessment. | A.8.8, A.8.28, A.8.29 | Meenemen in Sprint 2 |

## 6. Conclusie

De repository-inrichting voldoet voor Sprint 1 aan een belangrijk deel van de gevraagde Secure SDLC-maatregelen. De projectorganisatie is ingericht met GitHub Environments, production protection rules, Dependabot, CodeQL/code scanning, secret scanning, SBOM-generatie en een testdatabeleid.

De belangrijkste resterende beperking vanuit de Sprint 1-opdracht is dat echte GitHub environment secrets nog niet aantoonbaar zijn ingericht. Dat is bewust als open punt opgenomen, omdat er op dit moment nog geen echte secrets beschikbaar zijn. Een deployment-workflow is daarnaast nuttig voor verdere onderbouwing van de Secure SDLC, maar het ontbreken daarvan wordt meegenomen als vervolgpunt.
