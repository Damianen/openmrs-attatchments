# Mini-complianceverslag pipeline

## 1. Doel en scope

Dit verslag legt vast in welke mate de CI/CD-pipeline en repository-inrichting van de OpenMRS Attachments Module voldoen aan een selectie van NEN-7510 / ISO 27002 controls. De beoordeling volgt de keten: control, maatregel, bewijs en status.

- **Bronnen uit de projectmap:** `Documents/Sprints & Deliverables/Sprint1.docx`, `Documents/LU2/Projectopdracht.docx`, `Documents/Software security en compliance/Hardening  Secure CICD/Voorbereiding.docx` en de repositorybestanden in `openmrs-attatchments/`.
- **In scope:** GitHub repository, branch protection / ruleset, GitHub Actions, Dependabot, CodeQL/code scanning, secret scanning, SBOM, GitHub Environments en Docker Compose configuratie.
- **Out of scope:** echte productie-infrastructuur, organisatiebrede policies buiten deze repository en runtimebeheer van een productie-OpenMRS-installatie.
- **Bewijs:** screenshots in `docs/auditrapport/security/bewijs/`, configuratiebestanden in de repository en teamafspraken.

De Sprint 1-opdracht vraagt om twee hoofdonderdelen: een gap-analyse op drie NEN-7510-controls en het inrichten van projectorganisatie/ontwikkelproces. Dit verslag gaat over het tweede onderdeel. De codegerichte gap-analyse staat in `01-gap-analyse.md`.

## 2. Afstemming met Sprint 1-opdracht

| Sprint 1-eis | Invulling in deze repository | Bewijs | Status |
|---|---|---|---|
| Minimaal test- en productieomgevingen aanmaken | GitHub Environments `test` en `prod` bestaan. Daarnaast is ook `dev` ingericht. | `bewijs/repository-access/github-environments.png` | Aanwezig |
| Gescheiden configuratie | Docker Compose gebruikt een basisbestand en aparte overrides voor test en prod. Per omgeving wordt een eigen `.env` bestand gebruikt. | `docker/docker-compose.yml`; `docker/docker-compose.test.yml`; `docker/docker-compose.prod.yml`; `docker/.env.template` | Aanwezig |
| Gescheiden secrets | Echte secrets worden niet in Git opgeslagen. De `.env.template` beschrijft welke secrets per omgeving nodig zijn. GitHub environment secrets zijn nog niet ingericht, omdat er nog geen echte secrets beschikbaar zijn. | `docker/.env.template`; README | Gedeeltelijk aanwezig |
| GitHub Environments configureren | `dev`, `test` en `prod` zijn ingericht. Sprint 1 vereist minimaal test en productie. | `bewijs/repository-access/github-environments.png` | Aanwezig |
| Protection rules en approval-gates | `prod` heeft required reviewer, wait timer en protected branches/tags. | `bewijs/repository-access/production-environment-rules.png` | Aanwezig |
| NEN-7510-controls voor CI/CD inrichten | Branch protection, MFA, Dependabot, CodeQL/code scanning, secret scanning en SBOM zijn ingericht als CI/CD-maatregelen. | `bewijs/repository-access/main-branch-ruleset.png`; `bewijs/scanning/github-security-settings.png`; `bewijs/sbom-sca/dependabot-alerts-overview.png`; `bewijs/scanning/codeql-workflow.png`; `bewijs/sbom-sca/sbom-workflow-success.png` | Gedeeltelijk aanwezig |
| README beschrijft hoe omgevingen zijn ingericht | README bevat `dev`, `test` en `prod` met bijbehorende Docker Compose-bestanden. | `README.md` | Aanwezig |
| README beschrijft hoe testdata niet in productie terechtkomt | README verwijst naar testdatabeleid en beschrijft dat productiedata niet naar dev/test mag. | `README.md`; `03-testdatabeleid.md` | Aanwezig |
| README beschrijft hoe een nieuwe ontwikkelaar met de omgeving werkt | README bevat stappen voor clone, Java/Maven/Docker, `.env` en Docker Compose. | `README.md` | Aanwezig |

## 3. Compliance-overzicht

| NEN-7510 control | Doel | Pipeline-/repositorymaatregel | Bewijs | Status |
|---|---|---|---|---|
| **A.8.3 Toegangsbeveiliging** | Ongecontroleerde wijzigingen voorkomen. | Ruleset `protect-main` is ingericht voor `main`. Direct pushen naar `main` is geblokkeerd; wijzigingen lopen via branches en pull requests met minimaal 1 review. | `bewijs/repository-access/main-branch-ruleset.png`; `bewijs/repository-access/dependabot-prs-review-required.png`; teamafspraak werkwijze | Aanwezig |
| **A.8.5 Authenticatie** | Betrouwbare verificatie van gebruikers. | Teamleden gebruiken persoonlijke GitHub-accounts met MFA. | `bewijs/repository-access/mfa-*.png`; teamverklaring | Aanwezig |
| **A.8.8 Kwetsbaarheidsbeheer** | Kwetsbaarheden tijdig signaleren en opvolgen. | Dependabot alerts zijn actief voor Maven dependencies. SBOM wordt gegenereerd met CycloneDX en als artifact opgeslagen. | `.github/dependabot.yml`; `.github/workflows/sbom.yml`; `bewijs/sbom-sca/dependabot-alerts-overview.png`; `bewijs/sbom-sca/sbom-workflow-success.png`; `bewijs/sbom-sca/sbom-artifact-overview.png` | Aanwezig |
| **A.8.15 Logging** | Gebeurtenissen en wijzigingen navolgbaar maken. | GitHub bewaart auditsporen via commits, pull requests, workflow-runs en security findings. De codegerichte loggingrisico's staan in de gap-analyse. | GitHub activiteit; workflow-runs; `01-gap-analyse.md` | Gedeeltelijk aanwezig |
| **A.8.25 Veilige ontwikkelcyclus** | Security in het ontwikkelproces opnemen. | GitHub Environments `dev`, `test` en `prod` bestaan. `prod` heeft protection rules met required reviewer, wait timer en protected branches/tags. Docker Compose heeft aparte overrides voor test en prod. GitHub environment secrets en deployment-workflow ontbreken nog. | `bewijs/repository-access/github-environments.png`; `bewijs/repository-access/production-environment-rules.png`; `docker/docker-compose*.yml` | Gedeeltelijk aanwezig |
| **A.8.28 Veilig coderen** | Onveilige code vroeg detecteren. | CodeQL / code scanning is actief en uploadt scanresultaten. | `bewijs/scanning/codeql-workflow.png`; `bewijs/scanning/code-scanning-alerts.png`; `bewijs/scanning/github-security-settings.png` | Aanwezig |
| **A.8.29 Beveiligingstests** | Software toetsen op beveiligingseisen. | Dependabot, CodeQL, Snyk, SBOM en Maven Tests ondersteunen security testing. De Maven workflow draait API-tests en omod security regressietests op pull requests. PR-checks tonen dat CodeQL, Snyk en code scanning groen draaien op een pull request. | `.github/workflows/maven-tests.yml`; `bewijs/sbom-sca/dependabot-alerts-overview.png`; `bewijs/scanning/code-scanning-alerts.png`; `bewijs/scanning/pr-security-checks-passed.png`; `bewijs/sbom-sca/sbom-workflow-success.png` | Aanwezig; Maven workflow moet na eerste run nog als required check worden geselecteerd |
| **A.8.33 Testdata** | Voorkomen dat gevoelige productiegegevens in testomgevingen komen. | Testdatabeleid is vastgelegd. Docker-configuratie gebruikt gescheiden `.env` bestanden per omgeving. | `03-testdatabeleid.md`; `docker/.env.template` | Aanwezig |

## 4. Belangrijkste bewijsstukken

- **GitHub Environments:** `dev`, `test` en `prod` zijn zichtbaar in `bewijs/repository-access/github-environments.png`.
- **Production protection:** `prod` heeft required reviewers, een wait timer en protected branches/tags in `bewijs/repository-access/production-environment-rules.png`.
- **Dependabot:** Maven dependency alerts zijn zichtbaar in `bewijs/sbom-sca/dependabot-alerts-overview.png`.
- **Code scanning:** CodeQL is succesvol uitgevoerd en findings zijn zichtbaar in `bewijs/scanning/codeql-workflow.png` en `bewijs/scanning/code-scanning-alerts.png`.
- **PR securitychecks:** `bewijs/scanning/pr-security-checks-passed.png` toont dat CodeQL, Snyk Security Scan en code scanning groen zijn afgerond op een pull request.
- **Secret scanning:** secret scanning alerts staan ingeschakeld volgens `bewijs/scanning/github-security-settings.png`.
- **SBOM:** de SBOM-workflow is succesvol uitgevoerd en levert een artifact op volgens `bewijs/sbom-sca/sbom-workflow-success.png` en `bewijs/sbom-sca/sbom-artifact-overview.png`.
- **Testdata:** het beleid staat in `03-testdatabeleid.md`.

## 5. Beperkingen en open punten

| Open punt | Control(s) | Status |
|---|---|---|
| GitHub environment secrets zijn nog niet ingericht, omdat er nog geen echte secrets beschikbaar zijn. | A.8.25 | Nog in te richten |
| Er is nog geen deployment-workflow die expliciet `environment: test` of `environment: prod` gebruikt. | A.8.25 | Nog in te richten |
| Required checks en minimaal 1 review zijn ingericht volgens teamwerkwijze. De screenshots tonen de actieve `protect-main` ruleset en dependency PR's met `Review required`. De nieuwe Maven Tests workflow moet na de eerste run nog als required check worden toegevoegd. | A.8.3, A.8.29 | Gedeeltelijk aanwezig |
| Code scanning en Dependabot tonen open findings. Deze moeten worden beoordeeld in de security backlog en het risk assessment. | A.8.8, A.8.28, A.8.29 | Meenemen in Sprint 2 |

## 6. Conclusie

De repository-inrichting voldoet voor Sprint 1 aan een belangrijk deel van de gevraagde Secure SDLC-maatregelen. De projectorganisatie is ingericht met GitHub Environments, production protection rules, Dependabot, CodeQL/code scanning, secret scanning, SBOM-generatie en een testdatabeleid.

De belangrijkste resterende beperking vanuit de Sprint 1-opdracht is dat echte GitHub environment secrets nog niet aantoonbaar zijn ingericht. Dat is bewust als open punt opgenomen, omdat er op dit moment nog geen echte secrets beschikbaar zijn. Een deployment-workflow is daarnaast nuttig voor verdere onderbouwing van de Secure SDLC, maar het ontbreken daarvan wordt meegenomen als vervolgpunt.
