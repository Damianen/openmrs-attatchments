# Security backlog - OpenMRS Attachments Module

## 1. Doel

Dit document werkt de security backlog uit voor de OpenMRS Attachments Module. De backlog is gebaseerd op:

- de C4-diagrammen in `docs/architecture/`;
- het threat model in `docs/auditrapport/security/04-threat-model.md`;
- de gap-analyse in `docs/auditrapport/security/01-gap-analyse.md`;
- het pipeline-complianceverslag in `docs/auditrapport/security/02-pipeline-compliance.md`;
- het verdiepende onderzoek naar de Attachments module.

De opdracht vraagt om geprioriteerde security requirements op basis van gevonden risico's. Daarom staan de belangrijkste technische en procesmatige risico's hieronder als backlog-items met prioriteit, maatregel, acceptatiecriteria, testbewijs en NEN-7510-koppeling.

## 2. Prioritering

De prioriteit komt uit het threat model:

| Prioriteit | Betekenis |
|---|---|
| P0 | Kritiek risico. Eerst oppakken, omdat patientdata of file access direct geraakt wordt. |
| P1 | Hoog risico. Snel oppakken, maar iets minder direct kritiek dan P0. |
| P2 | Belangrijk verbeterpunt. Nodig voor security en compliance, maar niet altijd direct exploitable. |
| P3 | Later oppakken of afhankelijk van ontbrekende informatie, zoals echte secrets. |

Voor patientdata is de risicobereidheid laag. Risico's die direct kunnen leiden tot uitlekken of ongeautoriseerd lezen van patientbestanden horen daarom bovenaan.

## 3. Backlog-overzicht

| ID | Prioriteit | Gekoppelde threat | Security requirement | NEN-7510-koppeling |
|---|---|---|---|---|
| SB-01 | P0 | T01 | Downloaden mag alleen via een veilige attachment/obs UUID-flow. | A.8.3, A.8.28, A.8.29 |
| SB-02 | P0 | T02 | File access moet altijd binnen de bedoelde attachment storage blijven. | A.8.3, A.8.28, A.8.29 |
| SB-03 | P1 | T03 | Uploads moeten standaard beperkt zijn tot toegestane bestandstypen. | A.8.28, A.8.29 |
| SB-04 | P1 | T04 | Download-autorisatie moet aantoonbaar getest zijn. | A.8.3, A.8.5, A.8.29 |
| SB-05 | P2 | T05 | Logs mogen geen onnodige patientgegevens bevatten. | A.8.15, A.8.28 |
| SB-06 | P2 | T06 | Base64 uploads moeten veilig en voorspelbaar worden gevalideerd. | A.8.28, A.8.29 |
| SB-07 | P2 | T07 | Dependency-risico's moeten via SBOM/SCA worden opgevolgd. | A.8.8, A.8.25 |
| SB-08 | P2 | T09 | Security tests en coverage moeten betrouwbaar in CI kunnen draaien. | A.8.25, A.8.29 |
| SB-09 | P2 | T10 | Uploadmetadata moet voorkomen dat attachments verkeerd gekoppeld worden. | A.8.3, A.8.28, A.8.29 |
| SB-10 | P3 | T08 | Deployment secrets moeten later via GitHub Environments worden ingericht. | A.8.5, A.8.25 |

## 4. Uitgewerkte backlog-items

### SB-01 - Beveilig of verwijder `/download?path=`

| Onderdeel | Invulling |
|---|---|
| Prioriteit | P0 |
| Risico | `AttachmentBytesResource.downloadFile()` las eerder een user-controlled `path` direct met `new File(path)` en `FileInputStream`. |
| Security requirement | De module mag nooit een willekeurig bestandspad uit een request gebruiken om bestanden te lezen. |
| Maatregel | Verwijder het `/download?path=` endpoint of vervang het door downloaden via bestaande attachment/obs UUID. |
| Acceptatiecriteria | Raw path download is niet meer aanwezig; downloaden werkt alleen voor bestaande attachment UUID's; ongeautoriseerde gebruikers krijgen geen bytes terug. |
| Testbewijs | Codezoekactie toont geen `@RequestParam("path")` downloadpad meer; `AttachmentBytesResourceTest` bewijst dat unauthenticated en unauthorized gebruikers geen bytes krijgen. |
| Status | Gemitigeerd en gevalideerd. |
| NEN-7510 | A.8.3 Toegangsbeveiliging, A.8.28 Secure coding, A.8.29 Security testing. |

### SB-02 - Maak file access in handlers veilig

| Onderdeel | Invulling |
|---|---|
| Prioriteit | P0 |
| Risico | `DefaultAttachmentHandler.getAttachmentByPath()` combineert `attachmentDir` en `fileName` zonder path-sanitization. |
| Security requirement | Bestanden mogen alleen uit de ingestelde attachment directory worden gelezen. |
| Maatregel | Gebruik `Path base = Paths.get(attachmentDir).toAbsolutePath().normalize()`, resolve de bestandsnaam, normalize het resultaat en weiger alles dat niet met `base` begint. |
| Acceptatiecriteria | `../secret.txt`, absolute paden en encoded traversal worden geweigerd; normale bestandsnamen binnen de storage blijven werken. |
| Testbewijs | Unit tests voor veilige bestandsnaam, `../`, absolute Windows-paden, absolute Unix-paden en encoded traversal. |
| NEN-7510 | A.8.3 Toegangsbeveiliging, A.8.28 Secure coding, A.8.29 Security testing. |

### SB-03 - Stel veilige upload allowlist in

| Onderdeel | Invulling |
|---|---|
| Prioriteit | P1 |
| Risico | Zonder veilige fallback kan een lege of verkeerd ingestelde allowlist ongewenste bestandstypen toelaten. |
| Security requirement | Uploads moeten standaard worden beperkt tot bestandstypen die voor medische attachments logisch en toegestaan zijn. |
| Maatregel | Stel een veilige default allowlist in (`pdf,png,jpg,jpeg`) en valideer extensie en MIME altijd. |
| Acceptatiecriteria | Toegestane bestanden worden geaccepteerd; `.exe`, scripts en MIME mismatch worden geweigerd; lege allowlist betekent niet automatisch "alles mag". |
| Testbewijs | `AttachmentResourceTest` en `AttachmentRestControllerTest` controleren veilige default allowlist, verboden extensie, MIME mismatch en expliciet toegestane legacy uploads. |
| NEN-7510 | A.8.28 Secure coding, A.8.29 Security testing. |

### SB-04 - Bewijs download-autorisatie

| Onderdeel | Invulling |
|---|---|
| Prioriteit | P1 |
| Risico | Download via obs UUID vertrouwt op OpenMRS framework-security, maar in `AttachmentBytesResource` is geen duidelijke extra privilege-check zichtbaar. |
| Security requirement | Alleen bevoegde gebruikers mogen attachment bytes downloaden. |
| Maatregel | Voeg expliciete privilege- of patient access check toe, of leg met integratietests vast dat OpenMRS dit betrouwbaar afdwingt. |
| Acceptatiecriteria | Gebruiker zonder `View Attachments` of passende patienttoegang kan geen attachment bytes downloaden; bevoegde gebruiker kan dat wel. |
| Testbewijs | Integratietests met gebruiker zonder privilege en gebruiker met privilege. |
| NEN-7510 | A.8.3 Toegangsbeveiliging, A.8.5 Authenticatie/autorisatie, A.8.29 Security testing. |

### SB-05 - Verwijder PII uit logs en voeg veilige auditlogging toe

| Onderdeel | Invulling |
|---|---|
| Prioriteit | P2 |
| Risico | `AttachmentsServiceImpl` logde patientnaam, geboortedatum en identifiers. Dit was te veel PII voor normale applicatielogs. |
| Security requirement | Logs moeten genoeg informatie geven voor audit, maar geen onnodige patientgegevens bevatten. |
| Maatregel | Log alleen minimale technische identificatie, zoals event type, resultaat, attachment/obs UUID, content type en bytecount. Vermijd naam, geboortedatum, identifiers, captions, filenames en vrije tekst in plaintext logs. |
| Acceptatiecriteria | Logs bevatten geen patientnaam, geboortedatum of identifiers; upload/download/delete acties zijn wel auditbaar. |
| Testbewijs | `AttachmentsServiceImplTest`, `AttachmentResourceTest` en `AttachmentBytesResourceTest` controleren dat logberichten geen patientnaam, geboortedatum, interne patient-id of identifiers bevatten. |
| NEN-7510 | A.8.15 Logging, A.8.28 Secure coding. |

### SB-06 - Maak base64 upload parsing robuust

| Onderdeel | Invulling |
|---|---|
| Prioriteit | P2 |
| Risico | `Base64MultipartFile` verwacht een specifieke data-URI structuur en kan onduidelijk falen bij malformed input. |
| Security requirement | Base64 upload input moet voorspelbaar worden gevalideerd en duidelijke foutmeldingen geven. |
| Maatregel | Maak een aparte `Base64AttachmentParser` die content-type, comma, base64 payload en maximale grootte valideert. |
| Acceptatiecriteria | Lege input, ontbrekende comma, ontbrekend content-type, niet-base64 data en te grote payload geven gecontroleerde foutmeldingen. |
| Testbewijs | Unit tests voor geldige base64 upload en meerdere malformed inputs. |
| NEN-7510 | A.8.28 Secure coding, A.8.29 Security testing. |

### SB-07 - Triager dependency- en SBOM-findings

| Onderdeel | Invulling |
|---|---|
| Prioriteit | P2 |
| Risico | Het project gebruikt legacy dependencies zoals Spring 4.1.4, Jackson 2.9, Log4j 1.2.15, Commons FileUpload 1.3.3 en XStream 1.4.3. |
| Security requirement | Bekende kwetsbaarheden in dependencies moeten zichtbaar, beoordeeld en opgevolgd worden. |
| Maatregel | Gebruik SBOM en SCA-resultaten om findings te beoordelen op runtime-aanwezigheid, reachability, CVSS, patientdata-impact en compensating controls. |
| Acceptatiecriteria | Elke high/critical finding heeft status: fixen, mitigeren, accepteren met reden of false positive. |
| Testbewijs | SBOM-artifact, SCA-output, Dependabot alerts/PR's en triage-overzicht. |
| NEN-7510 | A.8.8 Kwetsbaarheidsbeheer, A.8.25 Secure SDLC. |

### SB-08 - Maak security tests en coverage betrouwbaar in CI

| Onderdeel | Invulling |
|---|---|
| Prioriteit | P2 |
| Risico | Zonder aparte Maven testjob en coverage rapport zijn security regressietests niet zichtbaar genoeg als PR quality gate. |
| Security requirement | Security tests en coverage moeten reproduceerbaar draaien in CI voordat wijzigingen worden gemerged. |
| Maatregel | Splits CI in duidelijke jobs voor `api` en `omod` security regressietests en upload JaCoCo coverage rapporten als artifact. |
| Acceptatiecriteria | CI draait groen voor relevante testjobs; falende securitytest blokkeert merge; testresultaat en coverage artifact zijn zichtbaar in GitHub Actions. |
| Testbewijs | `.github/workflows/maven-tests.yml` draait `mvn -pl api test jacoco:report` en omod security regressietests met `jacoco:report`; required-check selectie en coveragebaseline volgen na eerste workflow-run. |
| NEN-7510 | A.8.25 Secure SDLC, A.8.29 Security testing. |

### SB-09 - Versterk metadata- en patientbinding bij upload

| Onderdeel | Invulling |
|---|---|
| Prioriteit | P2 |
| Risico | Upload gebruikt patient, visit en encounter parameters. Fouten hierin kunnen attachments verkeerd koppelen. |
| Security requirement | Een attachment mag alleen worden gekoppeld aan een geldige en consistente patient/visit/encounter combinatie. |
| Maatregel | Behoud bestaande checks en voeg expliciete validatie toe voor mismatch tussen patient, visit en encounter. Controleer ook edge cases met encounterless attachments. |
| Acceptatiecriteria | Encounter hoort bij de opgegeven visit; visit en encounter horen bij de opgegeven patient; mismatch wordt geweigerd met duidelijke fout. |
| Testbewijs | `AttachmentRestControllerTest` bevat regressietests voor geldige combinatie, visit/encounter mismatch, patient/visit mismatch, patient/encounter mismatch en ontbrekende verplichte patientparameter. |
| Status | Gemitigeerd en getest. |
| NEN-7510 | A.8.3 Toegangsbeveiliging, A.8.28 Secure coding, A.8.29 Security testing. |

### SB-10 - Richt deployment secrets later veilig in

| Onderdeel | Invulling |
|---|---|
| Prioriteit | P3 |
| Risico | Deployment secrets zijn nog niet beschikbaar en een echte deployment workflow is nog niet bewezen. |
| Security requirement | Secrets mogen niet in de repository staan en productie-deployment moet beschermd zijn. |
| Maatregel | Voeg secrets later toe via GitHub Environments. Gebruik production protection rules, required reviewers en protected branches. |
| Acceptatiecriteria | Geen secrets in git; secrets staan alleen in GitHub Environments; production deployment vraagt approval. |
| Testbewijs | Screenshot of bewijs van environment secrets/protection rules en workflow-run zodra secrets beschikbaar zijn. |
| NEN-7510 | A.8.5 Authenticatie/autorisatie, A.8.25 Secure SDLC. |

## 5. Aanbevolen volgorde

1. SB-01: `/download?path=` verwijderd/gevalideerd houden.
2. SB-02: file access in handlers veilig maken.
3. SB-03: upload allowlist en MIME-validatie verbeteren.
4. SB-04: download-autorisatie aantoonbaar testen.
5. SB-08: Maven test workflow na eerste run als required check opnemen en coverage artifacts beoordelen.
6. SB-05, SB-06 en SB-09 zijn inmiddels gemitigeerd en getest.
7. SB-07 blijven opvolgen via SBOM/SCA.
8. SB-10 pas afronden wanneer echte secrets beschikbaar zijn.

## 6. Conclusie

De backlog richt zich eerst op de risico's die direct patientbestanden kunnen raken. Daarom staan arbitrary file read, path traversal, uploadvalidatie en download-autorisatie bovenaan. Daarna volgen logging, base64 parsing, dependencies, CI-tests en deployment secrets.

Deze volgorde past bij de risk assessment: eerst vertrouwelijkheid van patientdata beschermen, daarna integriteit en beschikbaarheid verder versterken.
