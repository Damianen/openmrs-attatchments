# Threat model - OpenMRS Attachments Module

## 1. Doel

Dit document is het threat model voor de OpenMRS Attachments Module. Het is opgesteld als onderdeel van de security- en risk-assessment documentatie. De opdracht vraagt om:

- analyse op CIA/BIV;
- C4-diagrammen;
- threat modelling;
- risicocriteria;
- een risicomatrix;
- een security backlog op basis van de gevonden risico's.

De Healthcare Risk Assessment workshop noemt STRIDE, risk assessment en bow-tie analyse als passende methodes. Daarom gebruiken we hier STRIDE om threats te vinden en een simpele kans x impact score om te prioriteren.

Dit threat model hoort inhoudelijk bij de C4-diagrammen:

- `docs/architecture/c4-system-context.drawio`
- `docs/architecture/c4-container-diagram.drawio`
- `docs/architecture/c4-component-diagram.drawio`
- `docs/auditrapport/security/08-attack-surface-overview.md`

## 2. Scope

In scope:

- uploaden van patient attachments;
- zoeken en ophalen van attachment metadata;
- downloaden van attachment bytes;
- REST API endpoints;
- OpenMRS Core services die door de module worden gebruikt;
- database metadata via obs/patient/encounter records;
- attachment file storage;
- CI/CD en dependencies op hoofdlijnen, omdat deze onderdeel zijn van het security- en risk-assessment.

Out of scope:

- volledige OpenMRS Core implementatie;
- productie-infrastructuur buiten de repository;
- echte secrets, omdat die nog niet beschikbaar zijn;
- uitgebreide pentest-uitvoering. Die hoort later apart als bewijs.

## 3. Assets / crown jewels

| Asset | Waarom belangrijk | BIV-impact |
|---|---|---|
| Patient attachments | Bestanden kunnen medische documenten, scans of foto's bevatten. | Vooral vertrouwelijkheid, ook integriteit |
| Patient metadata | Metadata koppelt attachments aan patient, encounter en visit. | Vertrouwelijkheid en integriteit |
| Attachment file storage | Hier staat de echte file content. | Vertrouwelijkheid, integriteit en beschikbaarheid |
| OpenMRS obs metadata | Attachments worden als complex obs gekoppeld. | Integriteit en traceerbaarheid |
| Autorisatie en privileges | Bepaalt wie attachments mag zien of aanmaken. | Vertrouwelijkheid en integriteit |
| Applicatielogs | Kunnen nuttig zijn voor audit, maar mogen geen onnodige PII bevatten. | Vertrouwelijkheid en traceerbaarheid |
| CI/CD pipeline en dependencies | Onveilige dependency of buildstap kan kwetsbare software opleveren. | Integriteit en beschikbaarheid |

## 4. Trust boundaries

| Boundary | Uitleg | Belangrijkste risico |
|---|---|---|
| API client naar OpenMRS REST API | Externe input komt binnen via REST requests. | Onveilige input, upload abuse, ongeautoriseerde toegang |
| Attachments OMOD naar API/service layer | REST-laag roept service- en opslaglogica aan. | Te veel logica in resourceklasse, ontbrekende checks |
| Module naar OpenMRS Core services | Module vertrouwt op OpenMRS services voor patient, obs en auth. | Te veel vertrouwen op framework zonder defense-in-depth |
| Module/handlers naar file storage | Handlers lezen en schrijven bestanden. | Path traversal of uitlezen buiten attachment-map |
| OpenMRS Core naar database | Metadata wordt opgeslagen in de database. | Verkeerde of gemanipuleerde metadata |
| Repository/CI naar dependencies | Maven dependencies worden tijdens build gebruikt. | Kwetsbare of verouderde libraries |

## 4.1 Sprint 3 attack surface update

Voor Sprint 3 is de attack surface opnieuw bekeken in `08-attack-surface-overview.md`. De belangrijkste high-risk ingangen zijn:

| Attack surface | Belangrijkste vertrouwen | Waarom high risk |
|---|---|---|
| Upload attachment | OpenMRS auth, Tika MIME-detectie, global properties en patient/visit/encounter UUIDs | User-controlled bestand en metadata kunnen patientdata, opslag en systeemgedrag raken. |
| Download attachment bytes | OpenMRS auth, module privilegecheck en ObsService | Geeft attachment bytes terug; fout in autorisatie kan direct datalek veroorzaken. |
| Search attachments | OpenMRS services en requestparameters | Kan patient- en attachmentmetadata blootstellen. |
| File storage handler | Attachment-root directory en complex obs metadata | Verkeerde padvalidatie kan leiden tot path traversal of arbitrary file read. |
| Global properties | Correcte adminconfiguratie | Te ruime allowlist of uploadlimieten kunnen securityregels verzwakken. |

Deze update bevestigt dat upload, download, search en file storage de belangrijkste technische ingangen blijven. Voor logging is de detailanalyse uitgewerkt in `09-logging-gap-analyse.md`.

## 5. Risicocriteria

We gebruiken een simpele score van 1 tot 5 voor kans en impact.

| Score | Kans | Impact |
|---|---|---|
| 1 | Onwaarschijnlijk | Nauwelijks effect |
| 2 | Laag | Beperkte fout of kleine verstoring |
| 3 | Middel | Duidelijke security impact, maar beperkt bereik |
| 4 | Hoog | Ernstige impact op patientdata of systeemgedrag |
| 5 | Zeer hoog | Kritiek datalek, volledige bypass of grote compliance-impact |

Risicoscore = kans x impact.

| Score | Prioriteit | Betekenis |
|---|---|---|
| 15-25 | P0/P1 | Hoog risico, moet als eerste op de backlog |
| 8-14 | P2 | Belangrijk, maar minder direct kritisch |
| 1-7 | P3 | Lager risico of vooral verbetering |

Risicobereidheid: voor patientdata accepteren we geen onbehandelde P0-risico's. Alles met directe impact op vertrouwelijkheid van patientbestanden moet minimaal gemitigeerd of aantoonbaar gecompenseerd worden.

## 6. Threats per onderdeel

| ID | Onderdeel | Threat | STRIDE | BIV-impact | Kans | Impact | Score | Bewijs | Maatregel |
|---|---|---|---|---|---:|---:|---:|---|---|
| T01 | Download endpoint | Een gebruiker kon via `/download?path=` een willekeurig bestandspad laten lezen. | Information Disclosure, Elevation of Privilege | Vertrouwelijkheid | 4 | 5 | 20 | Raw path endpoint is niet meer aanwezig in de huidige `AttachmentBytesResource`; downloaden loopt via attachment UUID. | Gemitigeerd en gevalideerd; UUID-only download en privilegecheck blijven behouden. |
| T02 | File storage handler | Path traversal via bestandsnaam in `getAttachmentByPath`. | Information Disclosure, Tampering | Vertrouwelijkheid, integriteit | 4 | 4 | 16 | `DefaultAttachmentHandler.java` regels 68-71 combineert directory en bestandsnaam zonder sanitization. | Gebruik veilige `Path.resolve().normalize()` en weiger paden buiten attachment-root. |
| T03 | Uploadvalidatie | Ongewenste bestandstypen kunnen worden geupload als allowlist leeg is. | Tampering, Denial of Service | Integriteit, beschikbaarheid | 4 | 4 | 16 | `allowedFileExtensions` heeft nu een veilige default en `AttachmentResource.java` valideert extensie/MIME altijd. | Gedeeltelijk gemitigeerd; regressietests bewijzen lege allowlist, verboden extensie en MIME mismatch. |
| T04 | Download-autorisatie | Download via obs UUID vertrouwt vooral op OpenMRS framework en heeft geen duidelijke extra module-check. | Elevation of Privilege, Information Disclosure | Vertrouwelijkheid | 3 | 5 | 15 | `AttachmentBytesResource.java` regels 41-58 haalt complex obs op en schrijft bytes terug; expliciete `View Attachments` check is niet zichtbaar in methode. | Expliciete privilege/patient access check of integratietest die framework-check bewijst. |
| T05 | Logging | Patientgegevens kunnen in logs terechtkomen. | Information Disclosure, Repudiation | Vertrouwelijkheid, traceerbaarheid | 3 | 4 | 12 | `AttachmentsServiceImpl.java`, `AttachmentResource.java` en `AttachmentBytesResource.java` loggen minimale technische context. | Gedeeltelijk gemitigeerd; tests bewijzen dat logberichten voor attachment-fetch, upload/delete en bytes-download geen patientnaam, geboortedatum, interne id of identifiers bevatten. |
| T06 | Base64 upload | Malformed base64 input kan exceptions of onduidelijk foutgedrag veroorzaken. | Denial of Service, Tampering | Beschikbaarheid, integriteit | 3 | 3 | 9 | `AttachmentResource.java` regels 113-119 en 365-370 parsen base64 met vaste aannames over data URI structuur. | Aparte parser maken met duidelijke validatie en foutmeldingen; negatieve tests toevoegen. |
| T07 | Dependencies | Verouderde dependencies kunnen bekende CVE's bevatten. | Elevation of Privilege, Tampering, DoS | Integriteit, beschikbaarheid, vertrouwelijkheid | 3 | 4 | 12 | Verdiepend onderzoek noemt o.a. Spring 4.1.4, Jackson 2.9, Log4j 1.2.15, Commons FileUpload 1.3.3 en XStream 1.4.3. | SBOM + SCA blijven draaien; findings triageren op runtime/reachability; upgrades plannen. |
| T08 | CI/CD secrets en deployment | Secrets en deployment naar echte environments zijn nog niet volledig bewezen. | Spoofing, Tampering | Integriteit | 2 | 4 | 8 | Het pipelineverslag noemt dat environment secrets en deployment workflow nog open staan. | Secrets pas toevoegen via GitHub Environments; prod approvals behouden; geen secrets in repo. |
| T09 | Build/testbaarheid | Security tests en coverage moeten betrouwbaar in CI draaien. | Tampering, Repudiation | Integriteit, traceerbaarheid | 3 | 3 | 9 | `.github/workflows/maven-tests.yml` draait API-tests, omod security regressietests en JaCoCo coverage rapporten op pull requests. | Na eerste workflow-run de Maven testjob als required check opnemen en coverage artifacts beoordelen. |
| T10 | Database metadata | Verkeerde of gemanipuleerde metadata kan attachment aan verkeerde patient/context koppelen. | Tampering | Integriteit, vertrouwelijkheid | 2 | 4 | 8 | Uploadflow valideert patient, visit en encounter samenhang in `AttachmentResource.java`. | Gemitigeerd met checks en regressietests voor patient/visit/encounter mismatch. |

## 7. STRIDE samenvatting

| STRIDE categorie | Relevant in deze module? | Voorbeeld |
|---|---|---|
| Spoofing | Ja, vooral via CI/CD en authenticatiecontext. | Onbewezen deployment/secrets kunnen later risico geven. |
| Tampering | Ja. | Upload van ongewenste bestanden of manipulatie van attachment metadata. |
| Repudiation | Ja. | Kritieke upload-, download- en delete-acties hebben nu veilige auditlogging; search en platform-audit blijven aandachtspunten. |
| Information Disclosure | Ja, dit is de grootste categorie. | Historische `/download?path=` finding, path traversal en PII in logs. |
| Denial of Service | Ja. | Grote/malformed uploads, kwetsbare dependencies of instabiele build/tests. |
| Elevation of Privilege | Ja. | Download zonder expliciete module-check of misbruik van file access. |

## 8. Hoogste risico's

De hoogste risico's zijn:

1. **T01 - Arbitrary file read via `/download?path=`**
   - Score: 20
   - Reden: directe impact op vertrouwelijkheid van patientdata en mogelijk ook secrets/configuratiebestanden. Deze finding is in de huidige code gemitigeerd doordat het raw path endpoint niet meer aanwezig is.

2. **T02 - Path traversal in `DefaultAttachmentHandler`**
   - Score: 16
   - Reden: file storage is een crown jewel en de helper gebruikt client input zonder path-sanitization.

3. **T03 - Upload allowlist staat standaard te ruim**
   - Score: 16
   - Reden: uploadfunctionaliteit is een van de belangrijkste aanvalsvlakken van deze module.

4. **T04 - Download-autorisatie moet expliciet bewezen worden**
   - Score: 15
   - Reden: bij patientattachments moet toegang aantoonbaar beperkt zijn, niet alleen aangenomen.

Voor een bow-tie analyse is T01 de beste kandidaat, omdat er een duidelijke oorzaak, event en gevolg is:

- Oorzaken: user-controlled `path`, geen normalisatie, geen root-check, geen expliciete autorisatie.
- Event: onbevoegd bestand wordt gelezen via download endpoint.
- Gevolgen: datalek, uitlekken configuratie/secrets, NEN/AVG-impact.
- Preventief: endpoint verwijderen, UUID-only download, path resolver, autorisatietest.
- Correctief: auditlogging, incident response, log review, secrets roteren als gevoelige files zijn geraakt.

## 9. Koppeling naar NEN-7510:2024-2

| Control | Waarom relevant | Threats |
|---|---|---|
| A.8.3 Toegangsbeveiliging | Gebruikers mogen alleen bij data waarvoor ze bevoegd zijn. | T01, T02, T04 |
| A.8.5 Authenticatie/autorisatie | Identiteit en rechten moeten betrouwbaar worden afgedwongen. | T04, T08 |
| A.8.8 Kwetsbaarheidsbeheer | Kwetsbare dependencies en code moeten worden gevonden en opgevolgd. | T07 |
| A.8.15 Logging | Security events moeten bruikbaar worden gelogd zonder onnodige PII. | T05 |
| A.8.25 Secure SDLC | Security moet in ontwerp, build en releaseproces zitten. | T08, T09 |
| A.8.28 Secure coding | Onveilige file access, uploadvalidatie en parsing moeten worden voorkomen. | T01, T02, T03, T06 |
| A.8.29 Security testing | Belangrijke security checks moeten testbaar zijn. | T01, T02, T03, T04, T09 |
| A.8.33 Testdata | Tests mogen geen productiepatientdata bevatten. | Indirect bij pentest en regressietests |

## 10. Eerste security backlog uit dit threat model

| Prioriteit | Backlog item | Komt uit threat |
|---|---|---|
| P0 | `/download?path=` is verwijderd/gevalideerd; behoud alleen UUID-gebaseerde download. | T01 |
| P0 | Maak file access veilig met path normalization en root-directory controle. | T02 |
| P1 | Stel veilige upload allowlist in en valideer extensie en MIME altijd. | T03 |
| P1 | Voeg autorisatietests toe voor downloaden zonder `View Attachments` privilege. | T04 |
| P2 | PII uit attachment-fetch logs verwijderd; veilige auditlogging toegevoegd voor upload, bytes-download en delete/purge. | T05 |
| P2 | Maak een veilige parser voor base64 uploads. | T06 |
| P2 | Triager SCA/SBOM findings op runtime en exploitability. | T07 |
| P2 | Maven test workflow draait API- en omod securitytests op PR's en uploadt JaCoCo coverage artifacts; required-check selectie volgt na eerste run. | T09 |
| P3 | Werk deployment secrets later uit zodra echte secrets beschikbaar zijn. | T08 |

## 11. Conclusie

De grootste risico's zitten niet in het normale gebruik van OpenMRS zelf, maar in de plekken waar deze module met bestanden werkt. Vooral download en file storage zijn gevoelig, omdat daar patientbestanden direct geraakt worden. Uploadvalidatie is de tweede grote groep, omdat onveilige bestanden of foutieve MIME-controle later voor misbruik kunnen zorgen.

De logische volgende stap is om van de hoogste risico's een risicomatrix en bow-tie te maken. Daarna kan de security backlog definitief worden uitgewerkt met acceptatiecriteria en testbewijs.
