# SCA/SBOM triage - OpenMRS Attachments Module

## 1. Doel

Dit document legt vast hoe dependency- en SBOM-findings worden opgevolgd. De tooling is al ingericht met Dependabot, Snyk SCA en CycloneDX SBOM. Deze triage maakt zichtbaar welke meldingen zijn gevonden, welke impact ze hebben en welke vervolgstap nodig is.

## 2. Bronnen

| Bron | Gebruik |
|---|---|
| `bewijs/sbom-sca/dependabot-alerts-overview.png` | Screenshot met drie open Dependabot alerts |
| `.github/dependabot.yml` | Wekelijkse Maven dependencycontrole |
| `.github/workflows/snyk.yml` | Snyk Open Source scan voor SCA |
| `.github/workflows/sbom.yml` | CycloneDX SBOM-generatie |
| `pom.xml` en `omod/pom.xml` | Maven dependencyversies |
| `bewijs/sbom-sca/snyk-workflow-run-no-artifacts.png` | Bewijs dat de Snyk workflow draait, maar geen artifacts heeft |
| `bewijs/sbom-sca/snyk-artifact-upload-missing-files.png` | Bewijs dat `snyk-sca.json` en `snyk-code.json` ontbreken bij artifact upload |
| `bewijs/sbom-sca/sbom-workflow-run-artifact.png` | Bewijs dat de SBOM workflow succesvol draait en 1 artifact heeft |
| `bewijs/sbom-sca/sbom-artifact-upload-log.png` | Bewijs dat `sbom.zip` succesvol is geupload |
| `bewijs/sbom-sca/dependabot-tika-critical-alert-full.png` | Detailbewijs van de Apache Tika critical Dependabot alert |
| `bewijs/sbom-sca/dependabot-tika-critical-alert-details.png` | Extra detailbewijs van de Apache Tika alert met CVSS/CVE/GHSA |
| GitHub Advisory Database | CVE/GHSA, severity, CVSS en patched versions |

## 3. Triagecriteria

Elke dependency-finding wordt beoordeeld op:

- package en versie in het project;
- severity en CVSS-score;
- runtime-gebruik;
- reachability vanuit de module of runtimeomgeving;
- impact op patientdata, file access of beschikbaarheid;
- besluit: oplossen, accepteren met reden, false positive of open houden.

## 4. Bevindingen

| ID | Tool | Package | Finding | Severity | CVSS | Runtime gebruikt? | Reachable? | Besluit | Actie |
|---|---|---|---|---|---:|---|---|---|---|
| SCA-001 | Dependabot/Snyk | `org.apache.tika:tika-core` 2.9.2 | Apache Tika XXE vulnerability, CVE-2025-66516 / GHSA-f58c-gq56-vjjf | Critical | 10.0 | Ja. `AttachmentResource.java` gebruikt Tika voor MIME-detectie bij uploads. | Mogelijk. Uploads verwerken user-controlled bestanden, maar misbruik hangt af van bestandstype/parsergedrag. | Niet geaccepteerd | Upgrade naar `tika-core` 3.2.2 of hoger, of leg vast waarom dit binnen OpenMRS niet haalbaar is en welke compensating controls gelden. |
| SCA-002 | Dependabot/Snyk | `org.openmrs.web:openmrs-web` 2.2.0 | OpenMRS Module Upload vulnerable to Path Traversal (Zip Slip), CVE-2026-40076 / GHSA-78fc-9688-w8xw | Critical in Dependabot screenshot; advisory noemt High | 8.7 | Ja, via OpenMRS Core runtime dependency met `provided` scope. | Te controleren. Relevant als module upload via REST/web admin bereikbaar is in de gebruikte OpenMRS runtime. | Open | Controleer runtimeconfiguratie, module upload exposure en upgradepad naar gepatchte OpenMRS Core-versie. |
| SCA-003 | Dependabot/Snyk | `org.openmrs.web:openmrs-web` 2.2.0 | OpenMRS ModuleResourcesServlet path traversal to arbitrary file read, CVE-2026-40075 / GHSA-jjgj-cx3q-pw4w | High | 8.2 | Ja, via OpenMRS Core runtime dependency met `provided` scope. | Mogelijk. Advisory noemt extra afhankelijkheid van Tomcat-versie en module resource endpoint. | Open | Controleer Tomcat/OpenMRS runtimeversie en plan upgrade naar gepatchte OpenMRS Core-versie of documenteer compensating controls. |

## 5. Samenvatting

De open dependencyrisico's zitten vooral in twee gebieden:

- **Upload/file parsing:** Apache Tika wordt direct door de Attachments module gebruikt voor MIME-detectie. Dit maakt SCA-001 relevant voor de module zelf.
- **OpenMRS Core runtime:** de OpenMRS alerts zitten in `openmrs-web`. Deze dependency heeft `provided` scope, maar is wel onderdeel van de runtime waarin de module draait. Daarom moeten runtimeversie, Tomcatversie en blootgestelde endpoints worden gecontroleerd.

De SBOM-workflow is opnieuw handmatig uitgevoerd in `Generate SBOM #15` en heeft succesvol een artifact geupload. Daarmee is het SBOM-bewijs aanwezig.

De Snyk-workflow is opnieuw handmatig uitgevoerd in `Snyk Security Scan #9`. De workflow eindigde als `Success`, maar de annotations tonen dat de Snyk scanstappen met exit code 2 eindigden en dat `snyk-sca.json` en `snyk-code.json` niet zijn gevonden bij het uploaden van artifacts. Daardoor is de workflow-run wel bewijs dat de workflow draait, maar nog geen volledig bewijs van bewaarde Snyk scanresultaten.

## 6. Tooling- en bewijsstatus

| Onderdeel | Bewijs | Status | Vervolgactie |
|---|---|---|---|
| Dependabot alerts | `bewijs/sbom-sca/dependabot-alerts-overview.png` en Tika-detail screenshots | Aanwezig | Detailpagina's van de twee OpenMRS alerts ook vastleggen |
| SBOM | `bewijs/sbom-sca/sbom-workflow-run-artifact.png` en `bewijs/sbom-sca/sbom-artifact-upload-log.png` | Aanwezig | Artifact downloaden/bewaren bij auditbewijs indien nodig |
| Snyk SCA/SAST workflow | `bewijs/sbom-sca/snyk-workflow-run-no-artifacts.png` | Gedeeltelijk aanwezig | Workflow corrigeren zodat `snyk-sca.json` en `snyk-code.json` daadwerkelijk worden aangemaakt en geupload |
| Snyk JSON artifacts | `bewijs/sbom-sca/snyk-artifact-upload-missing-files.png` toont dat de JSON-bestanden ontbreken | Ontbreekt | Snyk outputpad of commando aanpassen en workflow opnieuw draaien |
| Apache Tika Dependabot alert | `bewijs/sbom-sca/dependabot-tika-critical-alert-full.png` en `bewijs/sbom-sca/dependabot-tika-critical-alert-details.png` tonen package, vulnerable range, patched version, CVSS 10.0, CVE en GHSA | Aanwezig | Upgrade naar 3.2.2 testen |

## 7. Vervolgacties

| Actie | Eigenaar | Status |
|---|---|---|
| Snyk JSON-artifacts downloaden en bewaren als bewijs | Team | Geblokkeerd totdat workflow de JSON-bestanden correct uploadt |
| CycloneDX SBOM-artifact bewaren bij auditbewijs | Team | Aanwezig in GitHub Actions; downloaden/bewaren indien nodig |
| Tika upgrade-impact testen op Java 8 en OpenMRS modulecompatibiliteit | Developer | Moet nog gedaan worden |
| OpenMRS Core upgradepad onderzoeken | Team | Moet nog gedaan worden |
| Runtime Tomcatversie controleren | Team | Moet nog gedaan worden |
| Besluiten welke findings opgelost, geaccepteerd of als false positive geregistreerd worden | Team | Moet nog gedaan worden |

## 8. Advisory links

- Apache Tika XXE: https://github.com/advisories/GHSA-f58c-gq56-vjjf
- OpenMRS Module Upload Zip Slip: https://github.com/openmrs/openmrs-core/security/advisories/GHSA-78fc-9688-w8xw
- OpenMRS ModuleResourcesServlet path traversal: https://github.com/advisories/GHSA-jjgj-cx3q-pw4w
