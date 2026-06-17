# SCA/SBOM triage - OpenMRS Attachments Module

## 1. Doel

Dit document legt vast hoe dependency- en SBOM-findings worden opgevolgd. De tooling is al ingericht met Dependabot, Snyk SCA en CycloneDX SBOM. Deze triage maakt zichtbaar welke meldingen zijn gevonden, welke impact ze hebben en welke vervolgstap nodig is.

## 2. Bronnen

| Bron | Gebruik |
|---|---|
| `bewijs/sbom-sca/dependabot-alerts-overview.png` | Screenshot met drie open Dependabot alerts |
| `bewijs/sbom-sca/dependabot-openmrs-module-upload-zip-slip-overview.png` | Overzichtsbewijs van de OpenMRS Module Upload Zip Slip alert |
| `bewijs/sbom-sca/dependabot-openmrs-module-resources-path-traversal-overview.png` | Overzichtsbewijs van de OpenMRS ModuleResourcesServlet path traversal alert |
| `.github/dependabot.yml` | Wekelijkse Maven dependencycontrole |
| `.github/workflows/snyk.yml` | Snyk Open Source scan voor SCA |
| `.github/workflows/sbom.yml` | CycloneDX SBOM-generatie |
| `pom.xml` en `omod/pom.xml` | Maven dependencyversies |
| `bewijs/scanning/pr-security-checks-passed.png` | Bewijs dat securitychecks op een pull request groen zijn afgerond |
| `bewijs/sbom-sca/snyk-workflow-run-artifact-success.png` | Bewijs dat de Snyk workflow succesvol draait en 1 artifact heeft |
| `bewijs/sbom-sca/snyk-artifact-upload-success.png` | Bewijs dat `snyk-sca.json` en `snyk-code.json` succesvol als artifact worden geupload |
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
| SCA-001 | Dependabot/Snyk | `org.apache.tika:tika-core` 2.9.2 | Apache Tika XXE vulnerability, CVE-2025-66516 / GHSA-f58c-gq56-vjjf | Critical | 10.0 | Ja. `AttachmentResource.java` gebruikt Tika voor MIME-detectie bij uploads. | Mogelijk. Uploads verwerken user-controlled bestanden, maar misbruik hangt af van bestandstype/parsergedrag. | Oplossen | Upgrade naar `tika-core` 3.2.2 of hoger testen. Niet accepteren en niet als false positive registreren, omdat Tika direct door de module wordt gebruikt. |
| SCA-002 | Dependabot/Snyk | `org.openmrs.web:openmrs-web` 2.2.0 | OpenMRS Module Upload vulnerable to Path Traversal (Zip Slip), CVE-2026-40076 / GHSA-78fc-9688-w8xw | Critical | 9.4 | Ja, via OpenMRS Core runtime dependency met `provided` scope. | Te controleren. Relevant als module upload via REST/web admin bereikbaar is in de gebruikte OpenMRS runtime. | Open houden | Niet als false positive sluiten. Runtimeconfiguratie, module upload exposure en upgradepad controleren. Screenshot: `bewijs/sbom-sca/dependabot-openmrs-module-upload-zip-slip-overview.png`. |
| SCA-003 | Dependabot/Snyk | `org.openmrs.web:openmrs-web` 2.2.0 | OpenMRS ModuleResourcesServlet path traversal to arbitrary file read, CVE-2026-40075 / GHSA-jjgj-cx3q-pw4w | High | 8.2 | Ja, via OpenMRS Core runtime dependency met `provided` scope. | Mogelijk. Advisory noemt extra afhankelijkheid van Tomcat-versie en module resource endpoint. | Open houden | Niet als false positive sluiten. Tomcat/OpenMRS runtimeversie controleren en daarna upgrade of compensating controls bepalen. Screenshot: `bewijs/sbom-sca/dependabot-openmrs-module-resources-path-traversal-overview.png`. |

## 5. Besluiten per finding

| ID | Besluit | Onderbouwing | Vervolg |
|---|---|---|---|
| SCA-001 | Oplossen | Tika wordt direct gebruikt in de uploadflow voor MIME-detectie. Daardoor is de finding relevant voor de Attachments module zelf. | Upgrade naar `tika-core` 3.2.2 of hoger testen op Java 8 en OpenMRS-compatibiliteit. |
| SCA-002 | Open houden | De kwetsbaarheid zit in de OpenMRS runtime dependency `openmrs-web`. Door `provided` scope staat de dependency niet als eigen modulecode in het artifact, maar de runtime kan wel kwetsbaar zijn. | Controleren of module upload endpoints bereikbaar zijn en welk OpenMRS Core upgradepad mogelijk is. |
| SCA-003 | Open houden | De kwetsbaarheid zit ook in `openmrs-web` en hangt af van runtimegedrag, module resource endpoints en Tomcat/OpenMRS-versie. | Runtimeversie controleren en daarna kiezen tussen upgrade, extra hardening of een onderbouwde acceptatie. |

## 6. Samenvatting

De open dependencyrisico's zitten vooral in twee gebieden:

- **Upload/file parsing:** Apache Tika wordt direct door de Attachments module gebruikt voor MIME-detectie. Dit maakt SCA-001 relevant voor de module zelf.
- **OpenMRS Core runtime:** de OpenMRS alerts zitten in `openmrs-web`. Deze dependency heeft `provided` scope, maar is wel onderdeel van de runtime waarin de module draait. Daarom moeten runtimeversie, Tomcatversie en blootgestelde endpoints worden gecontroleerd.

De SBOM-workflow is opnieuw handmatig uitgevoerd in `Generate SBOM #15` en heeft succesvol een artifact geupload. Daarmee is het SBOM-bewijs aanwezig.

De Snyk-workflow faalde eerder bij de artifact upload, omdat `snyk-sca.json` en `snyk-code.json` niet werden gevonden. De workflow is daarna aangepast zodat de Snyk-stappen altijd een JSON-bestand achterlaten. Als Snyk succesvol scanresultaten oplevert, worden die bestanden geupload. Als Snyk faalt voordat JSON wordt geschreven, wordt een kleine error-JSON aangemaakt zodat de artifact upload alsnog bewijsbaar is.

De aangepaste workflow is opnieuw uitgevoerd in de pull request en is succesvol afgerond. De artifact upload toont nu dat er 2 bestanden worden geupload en dat `snyk-results.zip` succesvol is aangemaakt.

## 7. Tooling- en bewijsstatus

| Onderdeel | Bewijs | Status | Vervolgactie |
|---|---|---|---|
| Dependabot alerts | `bewijs/sbom-sca/dependabot-alerts-overview.png`, Tika-detail screenshots en OpenMRS alert-overviews | Aanwezig | Detailpagina's blijven beschikbaar in GitHub Dependabot |
| PR securitychecks | `bewijs/scanning/pr-security-checks-passed.png` toont een gemergde PR met groene CodeQL-, Snyk- en code scanning-checks | Aanwezig | Failed checks blokkeren merge volgens de ingestelde PR/ruleset-werkwijze |
| SBOM | `bewijs/sbom-sca/sbom-workflow-run-artifact.png` en `bewijs/sbom-sca/sbom-artifact-upload-log.png` | Aanwezig | Artifact downloaden/bewaren bij auditbewijs indien nodig |
| Snyk SCA/SAST workflow | `bewijs/sbom-sca/snyk-workflow-run-artifact-success.png` | Aanwezig | Periodiek blijven draaien in CI |
| Snyk JSON artifacts | `bewijs/sbom-sca/snyk-artifact-upload-success.png` toont dat `snyk-sca.json` en `snyk-code.json` worden geupload | Aanwezig | Artifact downloaden/bewaren indien nodig |
| Apache Tika Dependabot alert | `bewijs/sbom-sca/dependabot-tika-critical-alert-full.png` en `bewijs/sbom-sca/dependabot-tika-critical-alert-details.png` tonen package, vulnerable range, patched version, CVSS 10.0, CVE en GHSA | Aanwezig | Upgrade naar 3.2.2 testen |

## 8. Vervolgacties

| Actie | Eigenaar | Status |
|---|---|---|
| Snyk JSON-artifacts downloaden en bewaren als bewijs | Team | Aanwezig in GitHub Actions; downloaden/bewaren indien nodig |
| CycloneDX SBOM-artifact bewaren bij auditbewijs | Team | Aanwezig in GitHub Actions; downloaden/bewaren indien nodig |
| Tika upgrade-impact testen op Java 8 en OpenMRS modulecompatibiliteit | Developer | Besluit: oplossen; uitvoering moet nog gedaan worden |
| OpenMRS Core upgradepad onderzoeken | Team | Besluit: open houden; onderzoek moet nog gedaan worden |
| Runtime Tomcatversie controleren | Team | Besluit: open houden; onderzoek moet nog gedaan worden |
| Besluiten welke findings opgelost, geaccepteerd of als false positive geregistreerd worden | Team | Afgerond op hoofdlijn: Tika oplossen, OpenMRS findings open houden |

## 9. Advisory links

- Apache Tika XXE: https://github.com/advisories/GHSA-f58c-gq56-vjjf
- OpenMRS Module Upload Zip Slip: https://github.com/openmrs/openmrs-core/security/advisories/GHSA-78fc-9688-w8xw
- OpenMRS ModuleResourcesServlet path traversal: https://github.com/advisories/GHSA-jjgj-cx3q-pw4w
