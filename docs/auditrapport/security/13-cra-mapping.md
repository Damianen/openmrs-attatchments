# CRA-mapping - Sprint 4

## 1. Doel

Dit document koppelt de securitymaatregelen uit het verbeteronderzoek aan hoofdlijnen uit de Cyber Resilience Act (CRA). De CRA-mapping is bedoeld als aanvullende bijlage bij het security audit eindrapport.

Dit is geen volledige juridische CRA-conformiteitsverklaring. De mapping laat zien hoe de gekozen maatregelen bijdragen aan kwetsbaarheidsbeheer, secure development, security testing en traceerbare bewijsvoering.

## 2. Scope

In scope:

- OpenMRS Attachments Module;
- repositorymaatregelen voor security en compliance;
- SAST, SCA, SBOM, pentest en regressietests;
- auditbewijs in `docs/auditrapport/security/`.

Out of scope:

- formele productcertificering;
- volledige leverancier- of marktverantwoordelijkheid onder de CRA;
- productie-infrastructuur buiten deze repository.

## 3. Mapping

| CRA-thema | Verwachting op hoofdlijn | Projectmaatregel | Bewijsartefacten | Status |
|---|---|---|---|---|
| Security by design | Securityrisico's moeten vroeg in ontwerp en ontwikkeling worden meegenomen. | Threat model, attack surface overview, risk assessment en security backlog zijn uitgewerkt. | `04-threat-model.md`; `05-security-backlog.md`; `08-attack-surface-overview.md`; `risk-assessment-report.md` | Aanwezig |
| Vulnerability handling | Bekende kwetsbaarheden moeten worden gevonden, beoordeeld en opgevolgd. | Dependabot, Snyk, CodeQL en SCA/SBOM-triage zijn ingericht. | `06-sca-sbom-triage.md`; `false-positive-beleid.md`; `bewijs/sbom-sca/dependabot-alerts-overview.png`; `bewijs/sbom-sca/snyk-workflow-run-artifact-success.png` | Aanwezig; enkele findings blijven open met onderbouwing |
| SBOM / component transparency | Componenten en dependencies moeten inzichtelijk zijn. | CycloneDX SBOM wordt gegenereerd en bewaard als JSON. | `bewijs/sbom-sca/sbom.cdx.json`; `bewijs/sbom-sca/sbom-workflow-run-artifact.png`; `.github/workflows/sbom.yml` | Aanwezig |
| Secure updates | Dependency-updates moeten beoordeeld en veilig gemerged worden. | Dependabot PR's zijn zichtbaar en vereisen review; branch/ruleset beschermt `main`. | `bewijs/repository-access/dependabot-prs-review-required.png`; `bewijs/repository-access/main-branch-ruleset.png`; `.github/dependabot.yml` | Aanwezig |
| Secure coding | Bekende kwetsbaarheidspatronen moeten in code worden voorkomen. | Path traversal, upload allowlist/MIME, malformed base64, download privilegecheck en veilige logging zijn gemitigeerd of getest. | `05-security-backlog.md`; `docs/auditrapport/08-pentest.pdf`; `omod/src/test/java/`; `api/src/test/java/` | Aanwezig voor onderzochte codepaden |
| Security testing | Security-eisen moeten getest en reproduceerbaar zijn. | Maven Tests workflow draait security regressietests, uploadt JaCoCo artifacts en is required voor PR's naar `main`; pentestdocument bevat 10 cases. | `.github/workflows/maven-tests.yml`; `10-coverage-quality-gate.md`; `bewijs/repository-access/maven-tests-required-check.md`; `bewijs/scanning/maven-tests-coverage-artifacts-success.png`; `docs/auditrapport/08-pentest.pdf` | Aanwezig |
| Logging en incidentonderzoek | Security events moeten traceerbaar zijn zonder onnodige gevoelige data. | Logging gap analyse en tests voor veilige logberichten zijn uitgewerkt. | `09-logging-gap-analyse.md`; `AttachmentResourceTest.java`; `AttachmentBytesResourceTest.java`; `ObsByConceptListSearchHandlerTest.java` | Aanwezig |
| Access control | Onbevoegde toegang tot data moet worden voorkomen. | Branch protection, MFA, production environment rules en codegerichte toegangstests zijn vastgelegd. | `02-pipeline-compliance.md`; `bewijs/repository-access/mfa-*.png`; `bewijs/repository-access/production-environment-rules.png`; `docs/auditrapport/08-pentest.pdf` | Aanwezig |
| Evidence and traceability | Securitymaatregelen moeten aantoonbaar en traceerbaar zijn. | Sprint 4 traceability matrix koppelt controls aan bewijs. | `11-traceability-matrix.md`; `12-security-audit-eindrapport.md`; `bewijs/` | Aanwezig |

## 4. Belangrijkste open punten

| Open punt | CRA-relevantie | Vervolgactie |
|---|---|---|
| Apache Tika upgradepad | Kwetsbaarheidsbeheer en veilige componenten. | Java/OpenMRS upgradepad bepalen of een Java 8-compatibele veilige oplossing kiezen. |
| OpenMRS runtime/exposure | Runtimekwetsbaarheden kunnen alleen goed worden beoordeeld met productiecontext. | Owner/deploymentbeheerder moet runtimeversie, Tomcatversie en endpoint-exposure bevestigen. |
| False-positive register vullen | Scanbevindingen moeten aantoonbaar beoordeeld worden. | Bij nieuwe of bestaande scanbevindingen status, reden en bewijs toevoegen in `false-positive-beleid.md`. |

## 5. Conclusie

De repository bevat de belangrijkste bouwstenen voor CRA-gerelateerde security evidence: SBOM, SCA/SAST, securitytests, pentest, risk assessment, traceability en reviewbeleid. De module kan nog niet als volledig CRA-afgerond worden beschouwd, omdat enkele punten afhankelijk zijn van upgradebesluiten en productie-runtimeinformatie. Wel is duidelijk vastgelegd welke maatregelen aanwezig zijn en welke opvolging nog nodig is.
