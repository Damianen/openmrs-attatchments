# Traceability matrix - Sprint 4

## 1. Doel

Deze traceability matrix koppelt de belangrijkste NEN-7510:2024 controls aan concrete maatregelen en bewijsartefacten in de repository. Hiermee is per control zichtbaar welk risico wordt afgedekt, waar de maatregel staat beschreven en welk bewijs gebruikt kan worden bij de audit.

Sprint 4 vraagt dat elk bewijsstuk traceerbaar is. Daarom verwijst deze matrix naar bestanden in `docs/auditrapport/security/`, codebestanden, workflows en bewijsafbeeldingen.

## 2. Scope

In scope:

- OpenMRS Attachments module;
- upload, download, search, delete/purge en file storage;
- CI/CD, branch protection, code scanning, SBOM/SCA en securitytests;
- logging en auditbaarheid rond patientattachments.

Out of scope:

- productie-infrastructuur buiten deze repository;
- echte productiepatientdata;
- productie-infrastructuur en runtime-instellingen buiten deze repository.

## 3. Matrix

| NEN-7510:2024 control | Securitydoel | Risico / finding | Maatregel | Bewijsartefacten | Status |
|---|---|---|---|---|---|
| A.8.3 Toegangsbeveiliging | Alleen bevoegde gebruikers mogen patientattachments lezen of wijzigen. | Path traversal, IDOR, verkeerde patientcontext en ongeautoriseerde metadata-inzage. | Raw path download is verwijderd/gevalideerd; download gebruikt attachment UUID; uploadcontext wordt gevalideerd; download heeft expliciete privilegecheck. | `04-threat-model.md`; `05-security-backlog.md`; `08-attack-surface-overview.md`; `risk-assessment-report.md`; `docs/auditrapport/08-pentest.pdf`; `omod/src/main/java/org/openmrs/module/attachments/rest/AttachmentBytesResource.java`; `omod/src/main/java/org/openmrs/module/attachments/rest/AttachmentResource.java` | Gedeeltelijk afgerond; productie- en patienttoegang blijven afhankelijk van OpenMRS runtimebeleid. |
| A.8.5 Authenticatie en autorisatie | Accounts, rechten, approvals en secrets moeten aantoonbaar veilig worden beheerd. | Ongeautoriseerde repositorywijzigingen, te brede toegang tot patientdata of hardcoded credentials. | MFA-bewijs, branch ruleset, production environment rules en PR-reviewplicht zijn vastgelegd. Hardcoded AWS-achtige credentials zijn verwijderd en via pentestbewijs hertest. | `02-pipeline-compliance.md`; `bewijs/repository-access/mfa-aliriza-sari.png`; `bewijs/repository-access/mfa-damian-buskens.png`; `bewijs/repository-access/mfa-mohammed-ouaali.png`; `bewijs/repository-access/main-branch-ruleset.png`; `bewijs/repository-access/production-environment-rules.png`; `bewijs/repository-access/dependabot-prs-review-required.png`; `bewijs/pentest/2-1-secrets-before-hardcoded.png`; `bewijs/pentest/2-1-secrets-after-code-removed.png`; `bewijs/pentest/README.md` | Aanwezig. |
| A.8.8 Kwetsbaarheidsbeheer | Bekende kwetsbaarheden moeten worden gevonden, beoordeeld en opgevolgd. | Kwetsbare dependencies zoals Apache Tika en OpenMRS Core alerts. | Dependabot, Snyk en SBOM zijn ingericht; high/critical findings zijn getriageerd met besluit: oplossen, open houden of opvolgen. | `06-sca-sbom-triage.md`; `false-positive-beleid.md`; `bewijs/sbom-sca/dependabot-alerts-overview.png`; `bewijs/sbom-sca/dependabot-tika-critical-alert-full.png`; `bewijs/sbom-sca/dependabot-tika-critical-alert-details.png`; `bewijs/sbom-sca/snyk-workflow-run-artifact-success.png`; `bewijs/sbom-sca/sbom.cdx.json` | Aanwezig; Tika upgradepad en runtimecontrole blijven open. |
| A.8.15 Logging | Security events moeten bruikbaar zijn zonder onnodige patientdata te loggen. | Patientnaam, geboortedatum of identifiers kunnen in logs terechtkomen; log injection kan auditsporen vervalsen. | Logberichten zijn beperkt tot technische context; upload, download, delete/purge en concept-search logging zijn getest op dataminimalisatie. | `09-logging-gap-analyse.md`; `risk-assessment-report.md`; `docs/auditrapport/08-pentest.pdf`; `omod/src/test/java/org/openmrs/module/attachments/rest/AttachmentResourceTest.java`; `omod/src/test/java/org/openmrs/module/attachments/rest/AttachmentBytesResourceTest.java`; `omod/src/test/java/org/openmrs/module/attachments/rest/ObsByConceptListSearchHandlerTest.java` | Aanwezig voor de onderzochte logpaden. |
| A.8.25 Secure SDLC | Security moet onderdeel zijn van ontwerp, build, review en release. | Kwetsbare code kan zonder review of securitytests naar main gaan. | Branch protection/ruleset, PR review, CodeQL, Snyk, SBOM en Maven Tests workflow zijn ingericht. `api-tests` en `omod-security-tests` zijn required checks voor PR's naar `main`. | `02-pipeline-compliance.md`; `10-coverage-quality-gate.md`; `.github/workflows/codeql.yml`; `.github/workflows/snyk.yml`; `.github/workflows/sbom.yml`; `.github/workflows/maven-tests.yml`; `bewijs/repository-access/maven-tests-required-check.md`; `bewijs/scanning/pr-security-checks-passed.png`; `bewijs/scanning/maven-tests-coverage-artifacts-success.png` | Aanwezig. |
| A.8.28 Secure coding | Onveilige file access, uploadvalidatie en parsing moeten worden voorkomen. | Path traversal, upload allowlist bypass, MIME mismatch, malformed base64 en hardcoded secrets. | Path normalization/root-checks, veilige default allowlist, extensie/MIME-validatie en robuuste base64validatie zijn toegevoegd en getest. Hardcoded credentials zijn verwijderd. | `04-threat-model.md`; `05-security-backlog.md`; `risk-assessment-report.md`; `docs/auditrapport/08-pentest.pdf`; `api/src/main/java/org/openmrs/module/attachments/obs/DefaultAttachmentHandler.java`; `api/src/main/java/org/openmrs/module/attachments/AttachmentsActivator.java`; `omod/src/main/java/org/openmrs/module/attachments/rest/AttachmentResource.java`; `omod/src/test/java/org/openmrs/module/attachments/rest/AttachmentResourceTest.java`; `omod/src/test/java/org/openmrs/module/attachments/rest/AttachmentRestControllerTest.java`; `bewijs/pentest/README.md` | Aanwezig voor de belangrijkste onderzochte codepaden. |
| A.8.29 Security testing | Belangrijke security-eisen moeten aantoonbaar getest worden. | Security regressies kunnen terugkomen zonder automatische testdekking of zonder pentest-hertestbewijs. | Security regressietests draaien in Maven workflow; JaCoCo artifacts worden opgeslagen; Maven Tests is required voor PR's naar `main`; pentestdocument is bijgewerkt met 10 cases; P1-hertestbewijs staat in de bewijsmap. | `10-coverage-quality-gate.md`; `14-pentest-review-punchlist.md`; `docs/auditrapport/08-pentest.pdf`; `bewijs/pentest/README.md`; `bewijs/repository-access/maven-tests-required-check.md`; `bewijs/scanning/maven-tests-coverage-artifacts-success.png`; `bewijs/scanning/jacoco-api-coverage-overview.png`; `bewijs/scanning/jacoco-omod-security-coverage-overview.png`; `.github/workflows/maven-tests.yml` | Aanwezig. |
| A.8.33 Testdata | Tests mogen geen productiepatientdata gebruiken. | Testen met echte patientdata kan privacyrisico veroorzaken. | Testdatabeleid beschrijft gebruik van synthetische of bestaande OpenMRS testdata. | `03-testdatabeleid.md`; testklassen onder `omod/src/test/java/` en `api/src/test/java/` | Aanwezig op documentatieniveau. |

## 4. Open punten

| Open punt | Reden | Vervolgactie |
|---|---|---|
| Productie-runtime en Tomcatversie bevestigen | Deze informatie staat niet aantoonbaar in deze repository. | Deploymentbeheerder/owner moet runtimeversies en endpoint-exposure bevestigen. |
| Apache Tika definitief upgraden | Tika 3.2.2 vereist Java 11, terwijl het project op Java 8 draait. | Java/OpenMRS upgradepad bepalen of veilige Java 8-compatibele oplossing vinden. |
| OpenMRS Core Dependabot alerts sluiten | Runtime-impact is nog niet volledig bewezen. | Open houden totdat runtime/exposure door owner is bevestigd. |

## 5. Conclusie

De belangrijkste NEN-controls zijn traceerbaar naar concrete repositorybestanden, bewijsafbeeldingen, workflows, tests en het pentestdocument. De auditbasis is daarmee aanwezig. De resterende punten zijn vooral afhankelijk van productie-runtimeinformatie buiten deze repository.
