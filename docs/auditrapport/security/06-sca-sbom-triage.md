# SCA/SBOM triage - OpenMRS Attachments Module

## 1. Doel

Dit document legt vast hoe dependency- en SBOM-findings worden opgevolgd. De tooling is al ingericht met Dependabot, Snyk SCA en CycloneDX SBOM. Deze triage maakt zichtbaar welke meldingen zijn gevonden, welke impact ze hebben en welke vervolgstap nodig is.

## 2. Bronnen

| Bron | Gebruik |
|---|---|
| `bewijs/sbom-sca/dependabot-alerts-overview.png` | Screenshot met drie open Dependabot alerts |
| `bewijs/repository-access/dependabot-prs-review-required.png` | Bewijs dat Dependabot dependency-update PR's aanmaakt en dat review vereist is |
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
| `bewijs/sbom-sca/sbom-artifact-upload-log.png` | Bewijs dat het SBOM-artifact succesvol is geupload |
| `bewijs/sbom-sca/sbom.cdx.json` | Gedownloade CycloneDX SBOM uit GitHub Actions |
| `bewijs/sbom-sca/dependabot-tika-critical-alert-full.png` | Detailbewijs van de Apache Tika critical Dependabot alert |
| `bewijs/sbom-sca/dependabot-tika-critical-alert-details.png` | Extra detailbewijs van de Apache Tika alert met CVSS/CVE/GHSA |
| `docker/docker-compose.prod.yml` | Productieconfiguratie voor module web admin en read-only module mount |
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
| SCA-001 | Dependabot/Snyk | `org.apache.tika:tika-core` 2.9.2 | Apache Tika XXE vulnerability, CVE-2025-66516 / GHSA-f58c-gq56-vjjf | Critical | 10.0 | Ja. `AttachmentResource.java` gebruikt Tika voor MIME-detectie bij uploads. | Mogelijk. Uploads verwerken user-controlled bestanden, maar misbruik hangt af van bestandstype/parsergedrag. | Oplossen | Upgrade naar `tika-core` 3.2.2 is getest, maar blokkeert op Java 8: Tika 3.2.2 gebruikt class file version 55.0 en het project compileert op Java 8/class file 52.0. Als tijdelijke compensating control is de uploadflow aangescherpt met een veilige default allowlist en verplichte MIME/extensievalidatie. |
| SCA-002 | Dependabot/Snyk | `org.openmrs.web:openmrs-web` 2.2.0 | OpenMRS Module Upload vulnerable to Path Traversal (Zip Slip), CVE-2026-40076 / GHSA-78fc-9688-w8xw | Critical | 9.4 | Ja, via OpenMRS Core runtime dependency met `provided` scope. | Beperkt in de repository-prodconfig: `MODULE_WEB_ADMIN=false` en modulemap read-only. Echte runtimeversie en productie-exposure zijn nog niet bevestigd. | Open houden | Niet als false positive sluiten. Productie-runtime, module upload exposure en upgradepad moeten door een owner/deploymentbeheerder worden bevestigd. Screenshot: `bewijs/sbom-sca/dependabot-openmrs-module-upload-zip-slip-overview.png`. |
| SCA-003 | Dependabot/Snyk | `org.openmrs.web:openmrs-web` 2.2.0 | OpenMRS ModuleResourcesServlet path traversal to arbitrary file read, CVE-2026-40075 / GHSA-jjgj-cx3q-pw4w | High | 8.2 | Ja, via OpenMRS Core runtime dependency met `provided` scope. | Mogelijk. Advisory noemt extra afhankelijkheid van Tomcatversie en module resource endpoint; deze productiegegevens staan niet in de repository. | Open houden | Niet als false positive sluiten. Productie-runtime, Tomcatversie en endpointbereikbaarheid moeten door een owner/deploymentbeheerder worden bevestigd. Screenshot: `bewijs/sbom-sca/dependabot-openmrs-module-resources-path-traversal-overview.png`. |

## 5. Besluiten per finding

| ID | Besluit | Onderbouwing | Vervolg |
|---|---|---|---|
| SCA-001 | Oplossen | Tika wordt direct gebruikt in de uploadflow voor MIME-detectie. Daardoor is de finding relevant voor de Attachments module zelf. Een directe upgrade naar `tika-core` 3.2.2 is geprobeerd, maar faalt op Java 8 door class file version 55.0. | Java/OpenMRS-upgradepad bepalen of een Java 8-compatibele gepatchte Tika-versie vinden. Tot die tijd beperken veilige upload allowlist en verplichte MIME/extensievalidatie de aanvalsvector. |
| SCA-002 | Open houden | De kwetsbaarheid zit in de OpenMRS runtime dependency `openmrs-web`. Door `provided` scope staat de dependency niet als eigen modulecode in het artifact. De repository-prodconfig beperkt modulebeheer met `MODULE_WEB_ADMIN=false` en read-only modulemount, maar de echte productie-runtime is niet vanuit deze repo bewezen. | Productie-runtime en module upload exposure laten bevestigen door repository/deployment owner. Daarna bepalen of accepteren of upgraden nodig is. |
| SCA-003 | Open houden | De kwetsbaarheid zit ook in `openmrs-web` en hangt af van runtimegedrag, module resource endpoints en Tomcat/OpenMRS-versie. Die runtimegegevens staan niet aantoonbaar in deze repository. | Productie-runtime, Tomcatversie en endpointbereikbaarheid laten bevestigen. Daarna kiezen tussen upgrade, extra hardening of onderbouwde acceptatie. |

## 6. Samenvatting

De open dependencyrisico's zitten vooral in twee gebieden:

- **Upload/file parsing:** Apache Tika wordt direct door de Attachments module gebruikt voor MIME-detectie. Dit maakt SCA-001 relevant voor de module zelf.
- **OpenMRS Core runtime:** de OpenMRS alerts zitten in `openmrs-web`. Deze dependency heeft `provided` scope, maar is wel onderdeel van de runtime waarin de module draait. Daarom moeten runtimeversie, Tomcatversie en blootgestelde endpoints worden gecontroleerd.

Voor SCA-001 is daarnaast een tijdelijke technische beperking toegevoegd in de uploadflow. Als `allowedFileExtensions` leeg is, gebruikt de module nu een veilige default allowlist (`pdf,png,jpg,jpeg`). Extensiecontrole en Tika-MIME-controle worden altijd uitgevoerd. Regressietests bewijzen dat een lege allowlist niet meer alles toestaat, dat `.exe` wordt geweigerd en dat MIME mismatch wordt geblokkeerd.

De SBOM-workflow is opnieuw handmatig uitgevoerd in `Generate SBOM #15` en heeft succesvol een artifact geupload. Het artifact is gedownload en de uitgepakte CycloneDX SBOM is in de repository bewaard als `bewijs/sbom-sca/sbom.cdx.json`.

De SBOM bevat:

| Eigenschap | Waarde |
|---|---|
| Formaat | CycloneDX |
| Specificatie | 1.4 |
| Generator | CycloneDX Maven plugin 2.7.10 |
| Timestamp | 2026-06-17T16:21:21Z |
| Root component | `org.openmrs.module:attachments:3.5.0` |
| Aantal componenten | 130 |
| SHA256 `sbom.cdx.json` | `DDBF43A5545A74309E1436BD974F5FC0985063F8F96EC9823040F47887DD4CE9` |

De belangrijkste dependency-findings uit de triage zijn ook in de SBOM terug te vinden, waaronder `org.apache.tika:tika-core:2.9.2` en `org.openmrs.web:openmrs-web:2.2.0`.

De Snyk-workflow faalde eerder bij de artifact upload, omdat `snyk-sca.json` en `snyk-code.json` niet werden gevonden. De workflow is daarna aangepast zodat de Snyk-stappen altijd een JSON-bestand achterlaten. Als Snyk succesvol scanresultaten oplevert, worden die bestanden geupload. Als Snyk faalt voordat JSON wordt geschreven, wordt een kleine error-JSON aangemaakt zodat de artifact upload alsnog bewijsbaar is.

De aangepaste workflow is opnieuw uitgevoerd in de pull request en is succesvol afgerond. De artifact upload toont nu dat er 2 bestanden worden geupload en dat `snyk-results.zip` succesvol is aangemaakt.

## 7. OpenMRS runtime/exposure checklist

De OpenMRS Core meldingen blijven open totdat de echte productie-runtime door een bevoegde owner is bevestigd. Alleen informatie die in deze repository staat, wordt als bewijs opgevoerd.

| Check | Waarom nodig | Status |
|---|---|---|
| OpenMRS runtimeversie controleren | De vulnerable dependency staat als `provided` dependency in de module, maar wordt geleverd door de runtime. | Niet bewezen in deze repository; owner/deploymentbeheerder moet productieversie bevestigen. |
| Tomcatversie controleren | De ModuleResourcesServlet advisory hangt mede af van runtime- en servletcontainergedrag. | Niet bewezen in deze repository; owner/deploymentbeheerder moet Tomcatversie bevestigen. |
| Module upload endpoint controleren | Zip Slip is relevant als module upload bereikbaar is voor de gebruikte omgeving. | Gedeeltelijk vanuit repository-config: `docker-compose.prod.yml` zet `MODULE_WEB_ADMIN=false`; echte productie-exposure nog bevestigen. |
| Module resource endpoint controleren | Path traversal is relevant als module resources via de runtime bereikbaar zijn. | Niet bewezen in deze repository; echte productie-exposure nog bevestigen. |
| Productieconfiguratie controleren | Admin-only module upload hoort in productie beperkt of uitgeschakeld te zijn. | Uitgevoerd voor repository-config: `docker-compose.prod.yml` zet `MODULE_WEB_ADMIN=false` en mount de modulemap read-only (`:ro`). |

Samengevat: de compile-time Dependabot alert blijft zichtbaar omdat de module tegen oudere `provided` OpenMRS dependencies compileert. De repository-prodconfig beperkt modulebeheer met `MODULE_WEB_ADMIN=false` en een read-only modulemount, maar dit is nog geen definitieve false positive. De echte productie-runtime, Tomcatversie en endpoint-exposure moeten door de repository/deployment owner bevestigd worden.

## 8. Tooling- en bewijsstatus

| Onderdeel | Bewijs | Status | Vervolgactie |
|---|---|---|---|
| Dependabot alerts | `bewijs/sbom-sca/dependabot-alerts-overview.png`, Tika-detail screenshots en OpenMRS alert-overviews | Aanwezig | Detailpagina's blijven beschikbaar in GitHub Dependabot |
| Dependabot update PR's | `bewijs/repository-access/dependabot-prs-review-required.png` toont dependency-update PR's met `Review required` | Aanwezig | Updates beoordelen via PR-review voordat ze worden gemerged |
| PR securitychecks | `bewijs/scanning/pr-security-checks-passed.png` toont een gemergde PR met groene CodeQL-, Snyk- en code scanning-checks | Aanwezig | Failed checks blokkeren merge volgens de ingestelde PR/ruleset-werkwijze |
| SBOM | `bewijs/sbom-sca/sbom-workflow-run-artifact.png`, `bewijs/sbom-sca/sbom-artifact-upload-log.png` en `bewijs/sbom-sca/sbom.cdx.json` | Aanwezig | SBOM opnieuw genereren na dependencywijzigingen |
| Snyk SCA/SAST workflow | `bewijs/sbom-sca/snyk-workflow-run-artifact-success.png` | Aanwezig | Periodiek blijven draaien in CI |
| Snyk JSON artifacts | `bewijs/sbom-sca/snyk-artifact-upload-success.png` toont dat `snyk-sca.json` en `snyk-code.json` worden geupload | Aanwezig | Artifact downloaden/bewaren indien nodig |
| Apache Tika Dependabot alert | `bewijs/sbom-sca/dependabot-tika-critical-alert-full.png` en `bewijs/sbom-sca/dependabot-tika-critical-alert-details.png` tonen package, vulnerable range, patched version, CVSS 10.0, CVE en GHSA | Aanwezig | Java/OpenMRS-upgradepad of Java 8-compatibele gepatchte Tika-versie bepalen |
| Upload allowlist/MIME regressietests | `AttachmentResourceTest` en `AttachmentRestControllerTest` bewijzen veilige default allowlist, verboden extensies, MIME mismatch en expliciet toegestane legacy uploads | Aanwezig | Blijven opnemen in PR-testjob |

## 9. Vervolgacties

| Actie | Eigenaar | Status |
|---|---|---|
| Snyk JSON-artifacts downloaden en bewaren als bewijs | Team | Aanwezig in GitHub Actions; downloaden/bewaren indien nodig |
| CycloneDX SBOM-artifact bewaren bij auditbewijs | Team | Uitgevoerd; `sbom.cdx.json` staat in de bewijsmap |
| Tika upgrade-impact testen op Java 8 en OpenMRS modulecompatibiliteit | Developer | Uitgevoerd; directe upgrade naar 3.2.2 faalt op Java 8 |
| Upload allowlist/MIME-validatie toevoegen als compensating control | Developer | Uitgevoerd en getest |
| OpenMRS Core upgradepad onderzoeken | Team | Open; productie-runtime moet door owner/deploymentbeheerder worden bevestigd |
| Runtime Tomcatversie controleren | Team | Open; Tomcatversie staat niet aantoonbaar in deze repository |
| Besluiten welke findings opgelost, geaccepteerd of als false positive geregistreerd worden | Team | Afgerond op hoofdlijn: Tika oplossen, OpenMRS findings open houden |

## 10. Advisory links

- Apache Tika XXE: https://github.com/advisories/GHSA-f58c-gq56-vjjf
- OpenMRS Module Upload Zip Slip: https://github.com/openmrs/openmrs-core/security/advisories/GHSA-78fc-9688-w8xw
- OpenMRS ModuleResourcesServlet path traversal: https://github.com/advisories/GHSA-jjgj-cx3q-pw4w
