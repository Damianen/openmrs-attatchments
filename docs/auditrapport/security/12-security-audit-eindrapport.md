# Security audit eindrapport - Sprint 4

## 1. Executive summary

Dit eindrapport vat de security- en compliancebevindingen samen voor de OpenMRS Attachments Module. De module verwerkt medische bijlagen en patientmetadata. Daardoor ligt de belangrijkste security-impact bij vertrouwelijkheid, integriteit en traceerbaarheid van patientattachments.

De audit laat zien dat de grootste risico's zitten in:

- file access en path traversal;
- uploadvalidatie en MIME-controle;
- download- en search-autorisatie;
- logging van patientgegevens;
- dependency- en supply-chainrisico's.

Tijdens het verbeteronderzoek zijn meerdere maatregelen aantoonbaar ingericht of aangescherpt:

- CodeQL, Snyk, Dependabot en SBOM-generatie;
- Maven Tests workflow met JaCoCo coverage artifacts;
- veilige upload allowlist en MIME-validatie;
- path traversal mitigaties en regressietests;
- veilige logging zonder onnodige patient-PII;
- bijgewerkt pentestdocument met 10 pentestcases;
- traceability naar NEN-7510:2024 controls.

De belangrijkste open punten zijn niet inhoudelijk genegeerd, maar expliciet vastgelegd: Tika-upgradepad, productie-runtimecontrole, OpenMRS Core exposure en het verplicht maken van Maven Tests als required check.

## 2. Scope en context

### 2.1 Systeem

De scope is de OpenMRS Attachments Module. Deze module beheert medische attachments via OpenMRS REST endpoints en complex obs storage.

Belangrijke onderdelen:

- REST upload, download, search en delete/purge;
- opslag en ophalen van attachmentbestanden;
- koppeling aan patient, visit en encounter;
- MIME-detectie via Apache Tika;
- logging rond attachmentacties;
- CI/CD en dependency scanning.

### 2.2 In scope

- Securityanalyse van de modulecode;
- NEN-7510:2024 mapping;
- risicoanalyse en backlog;
- SAST, SCA en SBOM;
- pentestaanpak en pentestresultaten;
- securitytests en CI-bewijs;
- advies en open punten.

### 2.3 Out of scope

- echte productiepatientdata;
- volledige OpenMRS Core audit;
- productie-infrastructuur buiten deze repository;
- owner-only GitHub settings die niet door het team aangepast kunnen worden;
- definitieve bevestiging van productie-runtime/Tomcatversie, omdat die informatie niet aantoonbaar in deze repository staat.

## 3. Auditmethodologie

De audit is uitgevoerd als combinatie van documentreview, code review, threat modeling, SCA/SBOM-triage, CI/CD-review, regressietests en pentestdocumentatie.

Gebruikte methoden:

| Methode | Toepassing |
|---|---|
| Gap-analyse | Eerste beoordeling van security- en compliancegaten. |
| Asset- en risicocriteria | Bepalen welke data en processen het belangrijkst zijn. |
| STRIDE threat model | Dreigingen classificeren op spoofing, tampering, repudiation, information disclosure, denial of service en elevation of privilege. |
| Risk assessment | Kans x impact, gekoppeld aan BIV/CIA en NEN-7510. |
| Security backlog | Bevindingen vertalen naar geprioriteerde maatregelen. |
| SAST/SCA/SBOM | CodeQL, Snyk, Dependabot en CycloneDX gebruiken als scan- en bewijsbronnen. |
| Pentest | Gerichte white-box pentest op 10 kwetsbaarheidscases. |
| CI/CD-review | Controleren of checks, artifacts, branch rules en reviewbeleid aantoonbaar zijn. |
| Traceability matrix | Controls koppelen aan bewijsartefacten. |

Belangrijke brondocumenten:

- `01-gap-analyse.md`
- `02-pipeline-compliance.md`
- `03-assets-risicocriteria.md`
- `04-threat-model.md`
- `05-security-backlog.md`
- `06-sca-sbom-triage.md`
- `07-risicomatrix-bow-tie.md`
- `08-attack-surface-overview.md`
- `09-logging-gap-analyse.md`
- `10-coverage-quality-gate.md`
- `11-traceability-matrix.md`
- `risk-assessment-report.md`
- `docs/auditrapport/08-pentest.pdf`

## 4. Risico-analyse en bevindingen

De risk assessment gebruikt kans x impact en koppelt bevindingen aan vertrouwelijkheid, integriteit en beschikbaarheid. Omdat de module medische attachments verwerkt, is de risicobereidheid laag voor onbevoegde toegang tot patientbestanden of patientmetadata.

### Finding 1 - Path traversal / arbitrary file read

| Onderdeel | Invulling |
|---|---|
| Risico | Een aanvaller kan proberen bestanden buiten de bedoelde attachment storage te lezen. |
| Impact | Hoog voor vertrouwelijkheid; mogelijk uitlekken van patientbestanden of serverconfiguratie. |
| OWASP | A01 Broken Access Control |
| NEN-7510 | A.8.3, A.8.28, A.8.29 |
| Status | Gemitigeerd en getest voor de onderzochte codepaden. |
| Bewijs | `04-threat-model.md`; `05-security-backlog.md`; `risk-assessment-report.md`; `docs/auditrapport/08-pentest.pdf`; `api/src/main/java/org/openmrs/module/attachments/obs/DefaultAttachmentHandler.java` |

### Finding 2 - Upload allowlist bypass en MIME mismatch

| Onderdeel | Invulling |
|---|---|
| Risico | Een lege of te ruime allowlist kan ongewenste bestandstypen toelaten. MIME mismatch kan schadelijke content verhullen. |
| Impact | Hoog voor integriteit en beschikbaarheid; mogelijk opslag van ongewenste of gevaarlijke bestanden. |
| OWASP | A05 Security Misconfiguration, A08 Software and Data Integrity Failures |
| NEN-7510 | A.8.28, A.8.29 |
| Status | Gemitigeerd met veilige default allowlist, extensiecontrole, MIME-controle en regressietests. |
| Bewijs | `05-security-backlog.md`; `06-sca-sbom-triage.md`; `risk-assessment-report.md`; `docs/auditrapport/08-pentest.pdf`; `omod/src/main/java/org/openmrs/module/attachments/rest/AttachmentResource.java`; `omod/src/test/java/org/openmrs/module/attachments/rest/AttachmentResourceTest.java` |

### Finding 3 - Download-autorisatie / IDOR

| Onderdeel | Invulling |
|---|---|
| Risico | Een gebruiker kan proberen attachment bytes op te halen via een bekende obs/attachment UUID. |
| Impact | Hoog voor vertrouwelijkheid, omdat attachments medische gegevens kunnen bevatten. |
| OWASP | A01 Broken Access Control |
| NEN-7510 | A.8.3, A.8.5, A.8.29 |
| Status | Gedeeltelijk gemitigeerd met expliciete privilegecheck en regressietests; patient access blijft afhankelijk van OpenMRS autorisatiemodel. |
| Bewijs | `08-attack-surface-overview.md`; `risk-assessment-report.md`; `docs/auditrapport/08-pentest.pdf`; `omod/src/main/java/org/openmrs/module/attachments/rest/AttachmentBytesResource.java`; `omod/src/test/java/org/openmrs/module/attachments/rest/AttachmentBytesResourceTest.java` |

### Finding 4 - PII en log injection in logging

| Onderdeel | Invulling |
|---|---|
| Risico | Logs kunnen patientnaam, geboortedatum, identifiers of vervalste logregels bevatten. |
| Impact | Middel tot hoog voor vertrouwelijkheid en traceerbaarheid. |
| OWASP | A09 Security Logging and Monitoring Failures |
| NEN-7510 | A.8.15, A.8.28 |
| Status | Gemitigeerd voor onderzochte logpaden met veilige logmessage-methodes en tests. |
| Bewijs | `09-logging-gap-analyse.md`; `risk-assessment-report.md`; `docs/auditrapport/08-pentest.pdf`; `omod/src/test/java/org/openmrs/module/attachments/rest/AttachmentResourceTest.java`; `omod/src/test/java/org/openmrs/module/attachments/rest/AttachmentBytesResourceTest.java` |

### Finding 5 - Dependencyrisico Apache Tika en OpenMRS Core alerts

| Onderdeel | Invulling |
|---|---|
| Risico | Kwetsbare dependencies kunnen via upload/parsing of runtimecomponenten misbruikt worden. |
| Impact | Hoog als kwetsbare code runtime bereikbaar is. |
| OWASP | A06 Vulnerable and Outdated Components |
| NEN-7510 | A.8.8, A.8.25, A.8.28 |
| Status | Getriageerd; Tika oplossen zodra upgradepad mogelijk is; OpenMRS Core alerts open houden tot runtime/exposure bevestigd is. |
| Bewijs | `06-sca-sbom-triage.md`; `bewijs/sbom-sca/sbom.cdx.json`; `bewijs/sbom-sca/dependabot-tika-critical-alert-full.png`; `bewijs/sbom-sca/dependabot-openmrs-module-upload-zip-slip-overview.png`; `bewijs/sbom-sca/dependabot-openmrs-module-resources-path-traversal-overview.png` |

## 5. SBOM en supply chain security

De supply-chaininrichting bestaat uit:

- Dependabot voor Maven dependency alerts en update PR's;
- Snyk voor SCA/SAST workflow en JSON artifacts;
- CycloneDX SBOM-generatie;
- CodeQL/code scanning;
- branch protection en PR-reviewplicht.

Het SBOM-bewijs staat in:

- `docs/auditrapport/security/bewijs/sbom-sca/sbom.cdx.json`
- `docs/auditrapport/security/bewijs/sbom-sca/sbom-workflow-run-artifact.png`
- `docs/auditrapport/security/bewijs/sbom-sca/sbom-artifact-upload-log.png`

De CycloneDX SBOM bevat 130 componenten voor `org.openmrs.module:attachments:3.5.0`. De belangrijkste dependencybevindingen zijn uitgewerkt in `06-sca-sbom-triage.md`.

Besluiten:

| Finding | Besluit | Reden |
|---|---|---|
| Apache Tika XXE | Oplossen | Tika wordt direct gebruikt bij upload MIME-detectie. Directe upgrade naar 3.2.2 blokkeert op Java 8. |
| OpenMRS Module Upload Zip Slip | Open houden | Runtime/exposure moet door owner of deploymentbeheerder worden bevestigd. |
| OpenMRS ModuleResourcesServlet path traversal | Open houden | Tomcatversie en module resource exposure zijn niet aantoonbaar in deze repository. |

## 6. CRA-mapping op hoofdlijnen

De Cyber Resilience Act vraagt dat softwareleveranciers kwetsbaarheden beheersen, security by design toepassen en security-informatie aantoonbaar kunnen leveren. Voor dit project is CRA niet juridisch volledig uitgewerkt, maar de audit artefacten ondersteunen dezelfde doelen. De detailmapping staat in `13-cra-mapping.md`.

| CRA-thema | Projectmaatregel | Bewijs |
|---|---|---|
| Vulnerability handling | Dependabot, Snyk, CodeQL, SBOM en dependencytriage. | `06-sca-sbom-triage.md`; `false-positive-beleid.md`; `sbom.cdx.json` |
| Secure by design | Threat model, secure coding fixes, uploadvalidatie en path traversal mitigaties. | `04-threat-model.md`; `05-security-backlog.md`; `risk-assessment-report.md` |
| Security testing | Pentest, regressietests, Maven Tests workflow en coverage artifacts. | `08-pentest.pdf`; `10-coverage-quality-gate.md`; `maven-tests.yml` |
| Traceerbaarheid | Traceability matrix, PR-reviewbeleid en bewijsmap. | `11-traceability-matrix.md`; `02-pipeline-compliance.md`; `bewijs/` |

## 7. Conclusie en advies

De module heeft een duidelijke securityverbetering doorgemaakt. De belangrijkste technische risico's zijn zichtbaar gemaakt, geprioriteerd en voor meerdere kritieke codepaden gemitigeerd of getest. De auditdocumentatie bevat nu een samenhangende lijn van risico naar maatregel naar bewijs.

Advies:

1. Maak Maven Tests verplicht in de GitHub ruleset zodra een owner dit kan instellen.
2. Rond het Tika-upgradepad af via Java/OpenMRS upgrade of een veilige Java 8-compatibele oplossing.
3. Laat een owner de echte productie-runtime, Tomcatversie en module endpoint exposure bevestigen.
4. Houd Snyk, CodeQL, Dependabot en SBOM periodiek actief en bewaar relevante artifacts bij releases.
5. Herhaal pentestcases bij grote wijzigingen in upload, download, search, logging of dependencies.
6. Houd `false-positive-beleid.md` actueel bij nieuwe scanbevindingen.

## 8. Bijlagen

| Bijlage | Locatie |
|---|---|
| Traceability matrix | `11-traceability-matrix.md` |
| SBOM | `bewijs/sbom-sca/sbom.cdx.json` |
| SAST/SCA bewijs | `bewijs/scanning/`, `bewijs/sbom-sca/` |
| Risicomatrix en bow-tie | `07-risicomatrix-bow-tie.md` |
| Threat model | `04-threat-model.md` |
| Risk assessment | `risk-assessment-report.md` |
| Pentest | `docs/auditrapport/08-pentest.pdf` |
| Pipeline compliance | `02-pipeline-compliance.md` |
| Coverage quality gate | `10-coverage-quality-gate.md` |
| False-positivebeleid en register | `false-positive-beleid.md` |
| CRA-mapping | `13-cra-mapping.md` |

## 9. Niet afgeronde items

| Item | Waarom niet afgerond | Vervolg |
|---|---|---|
| Maven Tests als required check | Vereist repository-owner rechten. | Owner moet de check selecteren in branch/ruleset settings. |
| Tika definitieve upgrade | Tika 3.2.2 vereist Java 11; project draait op Java 8. | Upgradepad onderzoeken of Java 8-compatibele oplossing kiezen. |
| OpenMRS Core runtime/exposure | Productie-runtime staat niet aantoonbaar in deze repository. | Owner/deploymentbeheerder moet runtime en endpointbereikbaarheid bevestigen. |
| Productie-infrastructuur audit | Buiten repositoryscope. | Apart behandelen met toegang tot deploymentomgeving. |
