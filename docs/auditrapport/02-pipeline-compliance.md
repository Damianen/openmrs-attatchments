# Mini-complianceverslag pipeline

## 1. Doel en scope

Dit verslag legt vast in welke mate de **CI/CD-pipeline en repository-inrichting** van de **OpenMRS Attachments Module** voldoen aan een selectie van **NEN-7510 / ISO 27002 (Annex A)** controls, volgens de keten **control → maatregel → bewijs → status**.

- **In scope:** de GitHub-repository, branch protection / ruleset, GitHub Actions-pipeline, security-tooling (Dependabot, CodeQL, code scanning), de OTAP-omgevingen via GitHub Environments (`dev`/`test`/`prod`) met Docker Compose, en de teamafspraken rond toegang en samenwerking.
- **Out of scope:** de OpenMRS-productie-infrastructuur, de functionele werking van de module en organisatorische controls buiten de pipeline.
- **Beoordeelde controls:** A.8.3, A.8.5, A.8.8, A.8.15, A.8.25, A.8.28, A.8.29, A.8.33.
- **Bewijs** bestaat uit screenshots in `docs/auditrapport/bewijs/`, configuratiebestanden in de repository en schriftelijke teamverklaringen.

> **Let op:** dit verslag beoordeelt repository- en pipeline-maatregelen. De codegerichte beoordeling van A.8.3, A.8.5 en A.8.15 staat in de aparte gap-analyse (`01-gap-analyse.md`), die op een andere branch is uitgewerkt.

## 2. Compliance-overzicht

| NEN-7510 control | Doel | Pipeline-/repositorymaatregel | Bewijs | Status |
|---|---|---|---|---|
| **A.8.3 Toegangsbeveiliging** | Toegang beperken en ongecontroleerde wijzigingen voorkomen. | Branch protection / ruleset op `main`: geen directe push, wijzigingen verplicht via pull request; iedereen werkt op een eigen branch. | `bewijs/ruleset.png`; teamafspraak werkwijze | Aanwezig |
| **A.8.5 Authenticatie** | Betrouwbare verificatie van de gebruiker. | Elk teamlid gebruikt een persoonlijk GitHub-account met MFA ingeschakeld. | Teamverklaring MFA (tekstueel bewijs) | Aanwezig |
| **A.8.8 Kwetsbaarheidsbeheer** | Kwetsbaarheden tijdig signaleren en verhelpen. | Dependabot voor Maven (wekelijks, update-PR's, alerts aan); SBOM-workflow (CycloneDX) die met succes draait en een artifact oplevert. | `.github/dependabot.yml`; `bewijs/dependabot.png`; `bewijs/security.png`; `.github/workflows/sbom.yml`; `bewijs/sbom-action-success.png`; `bewijs/sbom-artifact.png` | Aanwezig |
| **A.8.15 Logging** | Gebeurtenissen vastleggen en bewaren. | GitHub biedt auditsporen via commits, pull requests, workflow-runs en security events. De gap-analyse toont dat module-logging gedeeltelijk aanwezig is maar risico's bevat (PII in applicatielogs, ontbrekende auditlogging bij het download-endpoint). | GitHub audit-/activiteitssporen; gap-analyse (aparte branch) | Gedeeltelijk aanwezig |
| **A.8.25 Veilige ontwikkelcyclus** | Beveiliging inbedden in het hele ontwikkelproces. | Security checks in de repository (Security-tab bundelt Dependabot + code scanning); OTAP via GitHub Environments `dev`/`test`/`prod` met protection rules op `prod` (required reviewer, wait timer, protected branches/tags) en Docker Compose per omgeving. Een deployment-workflow die deze environments echt gebruikt, is nog niet bewezen. | `bewijs/security.png`; `bewijs/code-scanning.png`; `bewijs/environments.png`; `bewijs/environment-rules.png`; `docker/docker-compose*.yml` | Gedeeltelijk aanwezig |
| **A.8.28 Veilig coderen** | Onveilige code vroegtijdig opsporen. | Statische codeanalyse via CodeQL en GitHub code scanning. | `bewijs/codeql.png`; `bewijs/code-scanning.png` | Aanwezig |
| **A.8.29 Beveiligingstests** | Software toetsen op beveiligingseisen. | Dependency- en code-scanning fungeren als testpoort; een geautomatiseerde Maven-(security)testfase als CI-poort is nog niet aantoonbaar. | CodeQL / code scanning (zie A.8.28) | Gedeeltelijk aanwezig |
| **A.8.33 Testdata** | Testdata veilig kiezen en beschermen; geen gevoelige (productie)data. | Beleid en controle op veilige, niet-gevoelige testdata in de pipeline. | Nog aan te vullen | Nog in te richten |

## 3. Belangrijkste nuances

- **MFA (A.8.5)** is onderbouwd via een **teamverklaring** (tekstueel bewijs), bewust niet via persoonlijke screenshots, om accountgebonden gegevens niet onnodig vast te leggen. *Verklaring:* alle drie de teamleden gebruiken hun persoonlijke GitHub-account met MFA voor toegang tot de repository.
- **Branch protection (A.8.3)** is zowel een teamafspraak ("geen directe push naar `main`, alles via pull request") als technisch afgedwongen via de ruleset op `main`.
- **OTAP (A.8.25)** is **gedeeltelijk aanwezig**: de omgevingen `dev`/`test`/`prod` en de protection rules op `prod` bestaan, maar **environment secrets** en een **deployment-workflow die deze environments daadwerkelijk gebruikt, zijn nog niet bewezen**.
- **SBOM (A.8.8)** is **aanwezig en bewezen**: de CycloneDX-workflow heeft met succes gedraaid (`bewijs/sbom-action-success.png`) en levert een SBOM-artifact op (`bewijs/sbom-artifact.png`).
- **Logging (A.8.15)** verwijst voor de codegerichte beoordeling naar de gap-analyse op een andere branch; in dit pipeline-verslag is daarom geen directe link opgenomen om een kapotte verwijzing te voorkomen.

## 4. Open actiepunten

| Actiepunt | Control(s) | Status |
|---|---|---|
| Geautomatiseerde Maven-tests én securitytests als verplichte CI-poort aantoonbaar maken (workflow-run + groene checks op PR). | A.8.29 | Nog in te richten |
| Environment secrets inrichten en een deployment-workflow koppelen aan de GitHub Environments, zodat OTAP aantoonbaar in een draaiende pipeline wordt gebruikt. | A.8.25 | Nog in te richten |
| Logging-maatregel binnen de pipeline beschrijven en het bewijs (audit-/workflow-logs) vastleggen; PII-logging op moduleniveau adresseren. | A.8.15 | Nog in te richten |
| Testdatabeleid opstellen en aantoonbaar maken dat geen gevoelige of productiedata in tests wordt gebruikt. | A.8.33 | Nog in te richten |
| SBOM-artifact eventueel downloaden en als `docs/sbom.cdx.json` in versiebeheer opnemen. | A.8.8 | Nog aan te vullen |

## 5. Conclusie

De pipeline en repository-inrichting voldoen al aantoonbaar aan een belangrijk deel van de geselecteerde NEN-7510 controls:

- **Aanwezig:** toegangsbeveiliging (A.8.3), authenticatie (A.8.5), kwetsbaarheidsbeheer incl. bewezen SBOM (A.8.8) en veilig coderen (A.8.28).
- **Gedeeltelijk aanwezig:** veilige ontwikkelcyclus (A.8.25) — OTAP staat, maar deployment-workflow/secrets ontbreken nog —, beveiligingstests (A.8.29) en logging (A.8.15) — die verbetering vraagt vanwege PII in logs en ontbrekende auditlogging bij download-acties (zie gap-analyse).
- **Nog in te richten:** testdata (A.8.33).

Dit verslag is een momentopname binnen Sprint 1. Na verdere uitwerking van de openstaande pipeline- en codeverbeteringen worden de actiepunten uit hoofdstuk 4 aangevuld met maatregelen en bewijs en wordt het overzicht geactualiseerd.
