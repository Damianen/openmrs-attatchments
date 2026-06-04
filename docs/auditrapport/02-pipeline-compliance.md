# Mini-complianceverslag pipeline

## 1. Doel

Dit verslag legt vast in welke mate de CI/CD-pipeline en de repository-inrichting van de **OpenMRS Attachments Module** voldoen aan een selectie van relevante **NEN-7510 / ISO 27002 (Annex A)** controls. Per control beschrijven we de keten:

**NEN-7510 control → pipeline-maatregel → bewijs.**

Het doel is drieledig:

1. Aantonen welke beveiligingsmaatregelen al daadwerkelijk in de pipeline en de repository zijn ingericht.
2. Inzichtelijk maken welk bewijs daarvoor beschikbaar is (screenshots, configuratiebestanden, teamafspraken).
3. Een overzicht geven van de nog ontbrekende maatregelen, zodat deze in volgende sprints kunnen worden aangevuld.

## 2. Scope

| Onderwerp | Toelichting |
|-----------|-------------|
| **In scope** | De GitHub-repository van de OpenMRS Attachments Module, de daarop ingerichte branch protection / ruleset, de CI/CD-pipeline (GitHub Actions), de geïntegreerde security-tooling (Dependabot, CodeQL, code scanning), de OTAP-omgevingen via GitHub Environments (`dev`/`test`/`prod`) en bijbehorende Docker Compose-opstellingen, en de teamafspraken rondom toegang en samenwerking. |
| **Out of scope** | De productie-infrastructuur van OpenMRS zelf, de functionele werking van de module en organisatorische controls die buiten de pipeline vallen (bijv. fysieke beveiliging, personeelsbeleid). |
| **Geselecteerde controls** | A.8.3, A.8.5, A.8.8, A.8.15, A.8.25, A.8.28, A.8.29, A.8.33 |

Dit verslag richt zich uitsluitend op de **technische en procesmatige maatregelen in de softwareketen**. Het is een momentopname binnen Sprint 1 en wordt iteratief aangevuld.

> **Let op:** dit verslag beoordeelt de repository- en pipeline-maatregelen. De codegerichte beoordeling van A.8.3, A.8.5 en A.8.15 staat in [`docs/auditrapport/01-gap-analyse.md`](01-gap-analyse.md).

## 3. Werkwijze en bewijs

Het verslag is tot stand gekomen door:

1. **Inventarisatie** van de geselecteerde NEN-7510 controls en hun doelstelling.
2. **Koppeling** van elke control aan een concrete maatregel in de repository of pipeline.
3. **Verzameling van bewijs**, bestaande uit:
   - Screenshots van de GitHub-instellingen en security-tooling;
   - Configuratiebestanden in de repository (`.github/`);
   - Schriftelijke teamverklaringen en teamafspraken.

### Beschikbaar bewijs

| Bewijsstuk | Type | Betreft |
|------------|------|---------|
| `docs/auditrapport/bewijs/ruleset.png` | Screenshot | Branch protection / ruleset op `main` |
| `docs/auditrapport/bewijs/dependabot.png` | Screenshot | Dependabot alerts |
| `docs/auditrapport/bewijs/security.png` | Screenshot | GitHub Security-tab (overzicht) |
| `docs/auditrapport/bewijs/codeql.png` | Screenshot | CodeQL-analyse |
| `docs/auditrapport/bewijs/code-scanning.png` | Screenshot | Code scanning resultaten |
| `.github/dependabot.yml` | Configuratiebestand | Dependabot-configuratie (Maven, wekelijks) |
| `docs/auditrapport/bewijs/environments.png` | Screenshot | GitHub Environments `dev`, `test` en `prod` |
| `docs/auditrapport/bewijs/environment-rules.png` | Screenshot | Protection rules voor environment `prod` |
| `docker/docker-compose.yml` | Configuratiebestand | Basis-/ontwikkelopstelling (OTAP) |
| `docker/docker-compose.test.yml` | Configuratiebestand | Test-opstelling (OTAP) |
| `docker/docker-compose.prod.yml` | Configuratiebestand | Productie-opstelling (OTAP) |
| `.github/workflows/sbom.yml` | Workflowbestand | SBOM-generatie (CycloneDX) |
| `docs/auditrapport/bewijs/sbom-action-success.png` | Screenshot | Succesvolle SBOM-workflowrun |
| `docs/auditrapport/bewijs/sbom-artifact.png` | Screenshot | Gegenereerd SBOM-artifact |
| Teamverklaring MFA | Tekstueel bewijs | Alle drie teamleden gebruiken een persoonlijk GitHub-account met MFA ingeschakeld |
| Teamafspraak werkwijze | Tekstueel bewijs | Geen directe push naar `main`; werken via eigen branch + pull request |

> **Opmerking over MFA-bewijs:** MFA wordt in dit verslag onderbouwd via een **teamverklaring (tekstueel bewijs)** en niet via persoonlijke screenshots. Dit voorkomt dat persoonlijke accountinstellingen onnodig worden vastgelegd, terwijl de maatregel wel aantoonbaar is afgesproken en toegepast.

## 4. Compliance-overzicht

| NEN-7510 control | Doel van de control | Pipeline-maatregel | Bewijs | Status |
|------------------|---------------------|--------------------|--------|--------|
| **A.8.3 Toegangsbeveiliging** | Toegang tot informatie en code beperken tot wat noodzakelijk is; ongecontroleerde wijzigingen voorkomen. | Branch protection / ruleset op `main`: directe push is geblokkeerd, wijzigingen lopen verplicht via pull request. Teamafspraak: iedereen werkt op een eigen branch. | `docs/auditrapport/bewijs/ruleset.png`; teamafspraak "geen directe push naar main, wijzigingen via pull request" | Aanwezig |
| **A.8.5 Authenticatie** | Veilige authenticatie afdwingen voor toegang tot systemen en repositories. | Elk teamlid gebruikt een persoonlijk GitHub-account met multifactorauthenticatie (MFA) ingeschakeld. | Teamverklaring MFA (tekstueel bewijs): alle drie teamleden hebben MFA ingeschakeld | Aanwezig |
| **A.8.8 Kwetsbaarheidsbeheer** | Technische kwetsbaarheden tijdig signaleren en verhelpen. | Dependabot is geactiveerd en geconfigureerd voor het Maven-ecosysteem (wekelijkse controle, automatische update-PR's); Dependabot alerts staan aan. Daarnaast genereert een SBOM-workflow (CycloneDX) inzicht in de afhankelijkheden; deze workflow is succesvol uitgevoerd en levert een SBOM-artifact op. | `.github/dependabot.yml`; `docs/auditrapport/bewijs/dependabot.png`; `docs/auditrapport/bewijs/security.png`; `.github/workflows/sbom.yml`; `docs/auditrapport/bewijs/sbom-action-success.png`; `docs/auditrapport/bewijs/sbom-artifact.png` | Dependabot: Aanwezig · SBOM: Aanwezig |
| **A.8.15 Logging** | Gebeurtenissen registreren zodat activiteiten herleidbaar en controleerbaar zijn. | Op repository-/pipelineniveau biedt GitHub auditsporen via commits, pull requests, workflow-runs en security events. De gap-analyse laat zien dat logging op moduleniveau gedeeltelijk aanwezig is, maar nog risico's bevat: logging van patiëntgegevens (PII) in de applicatielogs en ontbrekende auditlogging bij het download-endpoint. | GitHub audit-/activiteitssporen; `docs/auditrapport/01-gap-analyse.md` | Gedeeltelijk aanwezig |
| **A.8.25 Veilige ontwikkelcyclus** | Beveiliging structureel inbedden in het hele ontwikkelproces. | Security checks zijn geïntegreerd in de repository; de GitHub Security-tab bundelt de beveiligingsstatus (Dependabot, code scanning). Daarnaast is een OTAP-structuur ingericht via GitHub Environments `dev`, `test` en `prod`, met protection rules op `prod` (required reviewer, wait timer en protected branches/tags). De Docker Compose-bestanden vormen de technische OTAP-basis per omgeving. Een deployment-workflow die deze environments daadwerkelijk gebruikt, is nog niet bewezen. | `docs/auditrapport/bewijs/security.png`; `docs/auditrapport/bewijs/dependabot.png`; `docs/auditrapport/bewijs/code-scanning.png`; `docs/auditrapport/bewijs/environments.png`; `docs/auditrapport/bewijs/environment-rules.png`; `docker/docker-compose.yml`, `docker/docker-compose.test.yml`, `docker/docker-compose.prod.yml` | Gedeeltelijk aanwezig |
| **A.8.28 Veilig coderen** | Onveilige code en programmeerfouten vroegtijdig opsporen. | Statische codeanalyse via CodeQL en GitHub code scanning detecteert kwetsbaarheden in de broncode. | `docs/auditrapport/bewijs/codeql.png`; `docs/auditrapport/bewijs/code-scanning.png` | Aanwezig |
| **A.8.29 Beveiligingstests** | Software toetsen op beveiligingseisen vóór en tijdens uitlevering. | Security-tests in de pipeline (bijv. geautomatiseerde securitytests / dependency- en code-scanning als testpoort). De geautomatiseerde Maven-(security)testfase als CI-poort is nog niet aantoonbaar ingericht. | Gedeeltelijk via CodeQL/code scanning; geautomatiseerde testpoort: nog aan te vullen | Gedeeltelijk aanwezig |
| **A.8.33 Testdata** | Testdata zorgvuldig selecteren en beschermen; geen gevoelige (productie)data in tests. | Beleid en controle op het gebruik van veilige, niet-gevoelige testdata in de pipeline. | Nog aan te vullen — beleid en bewijs volgen na verdere uitwerking van de openstaande pipeline- en codeverbeteringen | Nog in te richten |

## 5. Toelichting op MFA, pull request-werkwijze, OTAP en SBOM

### 5.1 Multifactorauthenticatie (MFA) — A.8.5

Alle drie de teamleden werken uitsluitend met hun **persoonlijke GitHub-account** waarop **MFA is ingeschakeld**. Hierdoor is toegang tot de repository niet alleen afhankelijk van een wachtwoord, maar ook van een tweede factor. Dit verkleint de kans dat een gecompromitteerd wachtwoord leidt tot ongeautoriseerde toegang tot de broncode.

Dit bewijs is bewust vastgelegd als **teamverklaring** in plaats van als persoonlijke screenshots:

- Persoonlijke MFA-instellingen bevatten accountgebonden informatie die niet onnodig in een projectdocument hoort.
- De maatregel is aantoonbaar via een gezamenlijke, ondertekende verklaring van het team.

> *Teamverklaring:* "Alle drie de teamleden gebruiken hun persoonlijke GitHub-account met MFA ingeschakeld voor toegang tot de repository van de OpenMRS Attachments Module."

### 5.2 Pull request-werkwijze en branch protection — A.8.3

Het team hanteert de afspraak dat **direct pushen naar `main` niet is toegestaan**. Iedereen werkt op een **eigen branch** en brengt wijzigingen uitsluitend via een **pull request** naar `main`. Deze werkwijze is bovendien technisch afgedwongen via een **ruleset / branch protection** op de `main`-branch (zie `docs/auditrapport/bewijs/ruleset.png`).

Hiermee wordt geborgd dat:

- wijzigingen herleidbaar zijn naar een pull request;
- er een natuurlijk moment is voor review en (geautomatiseerde) controles vóór samenvoeging;
- de `main`-branch een stabiele en gecontroleerde toestand behoudt.

> *Teamafspraak:* "Direct pushen naar `main` is niet toegestaan; iedereen werkt op een eigen branch en wijzigingen gaan via een pull request naar `main`."

### 5.3 OTAP-omgevingen en environment secrets — A.8.25

Voor de veilige ontwikkelcyclus is een **OTAP-structuur** ingericht via **GitHub Environments**: `dev`, `test` en `prod` (zie `docs/auditrapport/bewijs/environments.png`). Op de `prod`-omgeving zijn **protection rules** ingesteld (zie `docs/auditrapport/bewijs/environment-rules.png`):

- **required reviewer** — een deployment naar `prod` moet eerst worden goedgekeurd;
- **wait timer** — er geldt een wachttijd vóór de deployment doorgaat;
- **protected branches/tags** — alleen toegestane branches/tags mogen naar `prod`.

De technische OTAP-basis per omgeving is vastgelegd in de Docker Compose-bestanden: `docker/docker-compose.yml` (ontwikkel), `docker/docker-compose.test.yml` (test) en `docker/docker-compose.prod.yml` (productie).

Belangrijke nuances voor een eerlijke beoordeling:

- **Er zijn nog geen environment secrets ingericht.** Deze worden toegevoegd zodra een deployment-stap daadwerkelijk gevoelige waarden nodig heeft.
- **Er is nog geen deployment-workflow bewezen die de GitHub Environments echt gebruikt.** De omgevingen en regels zijn aanwezig, maar de koppeling met een draaiende pipeline moet nog worden aangetoond. A.8.25 blijft daarom **Gedeeltelijk aanwezig**.

### 5.4 SBOM (Software Bill of Materials) — A.8.8

Voor het inzicht in afhankelijkheden is een **SBOM-workflow** aanwezig in `.github/workflows/sbom.yml`, die een CycloneDX-SBOM genereert.

- **De SBOM-workflow is succesvol uitgevoerd. De succesvolle run is vastgelegd in `docs/auditrapport/bewijs/sbom-action-success.png` en het gegenereerde artifact `sbom` is vastgelegd in `docs/auditrapport/bewijs/sbom-artifact.png`.**
- Optioneel kan het artifact worden gedownload en als `docs/sbom.cdx.json` in de repository worden opgenomen, zodat de SBOM ook als bestand in versiebeheer beschikbaar is.

## 6. Nog ontbrekend bewijs / actiepunten

De onderstaande punten zijn nog niet (volledig) aantoonbaar en worden — na verdere uitwerking van de openstaande pipeline- en codeverbeteringen — ingericht en van bewijs voorzien.

| # | Actiepunt | Gerelateerde control(s) | Status |
|---|-----------|--------------------------|--------|
| 1 | **Geautomatiseerde Maven-tests** als verplichte CI-poort in de pipeline aantoonbaar maken (workflow-run + groene checks op pull request). | A.8.29 | Nog in te richten |
| 2 | **Securitytests** expliciet als geautomatiseerde testfase opnemen en het resultaat als bewijs vastleggen. | A.8.29 | Nog in te richten |
| 3 | **SBOM-artifact eventueel downloaden en als `docs/sbom.cdx.json` opnemen** in de repository, zodat de SBOM ook als bestand in versiebeheer beschikbaar is. | A.8.8 | Nog aan te vullen |
| 4 | **Environment secrets inrichten** voor de GitHub Environments (`dev`/`test`/`prod`) zodra een deployment-stap gevoelige waarden nodig heeft. | A.8.25 | Nog in te richten |
| 5 | **Deployment-workflow koppelen aan de GitHub Environments**, zodat de OTAP-omgevingen en protection rules aantoonbaar in een draaiende pipeline worden gebruikt. | A.8.25 | Nog in te richten |
| 6 | **Logging-maatregel** binnen de pipeline expliciet beschrijven en het bewijs (audit-/workflow-logs) vastleggen. | A.8.15 | Nog in te richten |
| 7 | **Testdatabeleid** opstellen: borgen dat er geen gevoelige of productiedata in tests wordt gebruikt, en dit aantoonbaar maken. | A.8.33 | Nog in te richten |
| 8 | **Veilige ontwikkelcyclus** verder onderbouwen: koppeling van alle security checks aan de pull request-poort aantoonbaar maken. | A.8.25 | Nog aan te vullen |

## 7. Conclusie

De pipeline en repository-inrichting van de OpenMRS Attachments Module voldoen op dit moment **al aantoonbaar** aan een belangrijk deel van de geselecteerde NEN-7510 controls:

- **Toegangsbeveiliging (A.8.3)**, **authenticatie (A.8.5)** en **veilig coderen (A.8.28)** zijn aanwezig en onderbouwd met concreet bewijs.
- **Kwetsbaarheidsbeheer (A.8.8)** is aanwezig voor Dependabot; het SBOM-onderdeel is aanwezig en aangetoond met een succesvolle workflowrun en artifact.
- **Veilige ontwikkelcyclus (A.8.25)** is gedeeltelijk aanwezig: de OTAP-omgevingen (`dev`/`test`/`prod`) met protection rules op `prod` en de Docker Compose-opstellingen zijn ingericht, maar environment secrets en een deployment-workflow die deze omgevingen echt gebruikt, ontbreken nog.
- **Beveiligingstests (A.8.29)** zijn gedeeltelijk aanwezig.
- **Logging (A.8.15)** is gedeeltelijk aanwezig, maar moet worden verbeterd vanwege patiëntgegevens (PII) in de logs en ontbrekende auditlogging bij download-acties (zie [`docs/auditrapport/01-gap-analyse.md`](01-gap-analyse.md)).
- **Testdata (A.8.33)** moet nog worden ingericht.

Dit verslag is een momentopname binnen Sprint 1. Na verdere uitwerking van de openstaande pipeline- en codeverbeteringen worden de openstaande actiepunten uit hoofdstuk 6 aangevuld met de bijbehorende maatregelen en bewijzen, en wordt het compliance-overzicht geactualiseerd. Daarmee groeit dit document uit tot een volledig mini-complianceverslag van de pipeline.
