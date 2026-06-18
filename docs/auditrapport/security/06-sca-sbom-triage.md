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
| `bewijs/sbom-sca/sbom-artifact-upload-log.png` | Bewijs dat `sbom.zip` succesvol is geupload |
| `bewijs/sbom-sca/dependabot-tika-critical-alert-full.png` | Detailbewijs van de Apache Tika critical Dependabot alert |
| `bewijs/sbom-sca/dependabot-tika-critical-alert-details.png` | Extra detailbewijs van de Apache Tika alert met CVSS/CVE/GHSA |
| `../openmrs-distro-referenceapplication/.env` | Runtime tag `TAG=3.5.0` voor de lokale reference application image |
| `../openmrs-distro-referenceapplication/distro/pom.xml` | Distro-bronconfiguratie; bevat o.a. `openmrs.version` en moduleversies |
| Docker image `openmrs/openmrs-reference-application-3-backend:3.5.0` | Runtimecontrole voor OpenMRS, Tomcat, Java en meegeleverde modules |
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
| SCA-002 | Dependabot/Snyk | `org.openmrs.web:openmrs-web` 2.2.0 | OpenMRS Module Upload vulnerable to Path Traversal (Zip Slip), CVE-2026-40076 / GHSA-78fc-9688-w8xw | Critical | 9.4 | De module compileert tegen `provided` OpenMRS dependencies. De lokaal gecontroleerde reference image `3.5.0` bevat `war.openmrs=2.7.6`. | Beperkt in de eigen prod-compose: `MODULE_WEB_ADMIN=false` en modulemap read-only. In dev/reference staat module web admin wel aan. | Open houden met runtimebewijs | Niet als false positive sluiten. Voor echte productie moet de gebruikte image/tag nog door de owner worden bevestigd. Screenshot: `bewijs/sbom-sca/dependabot-openmrs-module-upload-zip-slip-overview.png`. |
| SCA-003 | Dependabot/Snyk | `org.openmrs.web:openmrs-web` 2.2.0 | OpenMRS ModuleResourcesServlet path traversal to arbitrary file read, CVE-2026-40075 / GHSA-jjgj-cx3q-pw4w | High | 8.2 | De lokaal gecontroleerde reference image `3.5.0` bevat `war.openmrs=2.7.6`, Java 17.0.16 en Apache Tomcat 9.0.109. | Mogelijk, omdat module resources via `/openmrs/module/...` door de runtime/gateway bereikbaar kunnen zijn. | Open houden met runtimebewijs | Niet als false positive sluiten. Echte productie-image/tag en endpointbereikbaarheid nog bevestigen. Screenshot: `bewijs/sbom-sca/dependabot-openmrs-module-resources-path-traversal-overview.png`. |

## 5. Besluiten per finding

| ID | Besluit | Onderbouwing | Vervolg |
|---|---|---|---|
| SCA-001 | Oplossen | Tika wordt direct gebruikt in de uploadflow voor MIME-detectie. Daardoor is de finding relevant voor de Attachments module zelf. Een directe upgrade naar `tika-core` 3.2.2 is geprobeerd, maar faalt op Java 8 door class file version 55.0. | Java/OpenMRS-upgradepad bepalen of een Java 8-compatibele gepatchte Tika-versie vinden. Tot die tijd beperken veilige upload allowlist en verplichte MIME/extensievalidatie de aanvalsvector. |
| SCA-002 | Open houden met runtimebewijs | De kwetsbaarheid zit in de OpenMRS runtime dependency `openmrs-web`. Door `provided` scope staat de dependency niet als eigen modulecode in het artifact. De gecontroleerde reference image `3.5.0` gebruikt OpenMRS `2.7.6`; de eigen prod-compose zet module web admin uit en mount modules read-only. | Echte productie-image/tag laten bevestigen door repository/deployment owner. Daarna bepalen of accepteren of upgraden nodig is. |
| SCA-003 | Open houden met runtimebewijs | De kwetsbaarheid zit ook in `openmrs-web` en hangt af van runtimegedrag, module resource endpoints en Tomcat/OpenMRS-versie. De gecontroleerde reference image `3.5.0` gebruikt Tomcat `9.0.109`. | Echte productie-image/tag en endpointbereikbaarheid laten bevestigen. Daarna kiezen tussen upgrade, extra hardening of onderbouwde acceptatie. |

## 6. Samenvatting

De open dependencyrisico's zitten vooral in twee gebieden:

- **Upload/file parsing:** Apache Tika wordt direct door de Attachments module gebruikt voor MIME-detectie. Dit maakt SCA-001 relevant voor de module zelf.
- **OpenMRS Core runtime:** de OpenMRS alerts zitten in `openmrs-web`. Deze dependency heeft `provided` scope, maar is wel onderdeel van de runtime waarin de module draait. Daarom moeten runtimeversie, Tomcatversie en blootgestelde endpoints worden gecontroleerd.

Voor SCA-001 is daarnaast een tijdelijke technische beperking toegevoegd in de uploadflow. Als `allowedFileExtensions` leeg is, gebruikt de module nu een veilige default allowlist (`pdf,png,jpg,jpeg`). Extensiecontrole en Tika-MIME-controle worden altijd uitgevoerd. Regressietests bewijzen dat een lege allowlist niet meer alles toestaat, dat `.exe` wordt geweigerd en dat MIME mismatch wordt geblokkeerd.

De SBOM-workflow is opnieuw handmatig uitgevoerd in `Generate SBOM #15` en heeft succesvol een artifact geupload. Daarmee is het SBOM-bewijs aanwezig.

De Snyk-workflow faalde eerder bij de artifact upload, omdat `snyk-sca.json` en `snyk-code.json` niet werden gevonden. De workflow is daarna aangepast zodat de Snyk-stappen altijd een JSON-bestand achterlaten. Als Snyk succesvol scanresultaten oplevert, worden die bestanden geupload. Als Snyk faalt voordat JSON wordt geschreven, wordt een kleine error-JSON aangemaakt zodat de artifact upload alsnog bewijsbaar is.

De aangepaste workflow is opnieuw uitgevoerd in de pull request en is succesvol afgerond. De artifact upload toont nu dat er 2 bestanden worden geupload en dat `snyk-results.zip` succesvol is aangemaakt.

## 7. OpenMRS runtime/exposure checklist

De OpenMRS Core meldingen blijven open totdat de echte productie-runtime door een bevoegde owner is bevestigd. Wel is de lokale reference application runtime gecontroleerd via Docker image `openmrs/openmrs-reference-application-3-backend:3.5.0`.

| Check | Waarom nodig | Status |
|---|---|---|
| OpenMRS runtimeversie controleren | De vulnerable dependency staat als `provided` dependency in de module, maar wordt geleverd door de runtime. | Gedeeltelijk uitgevoerd: lokale image `3.5.0` bevat `war.openmrs=2.7.6`; distro-bronmap noemt `openmrs.version=2.8.6`, maar de lokale `.env` gebruikt tag `3.5.0`. |
| Tomcatversie controleren | De ModuleResourcesServlet advisory hangt mede af van runtime- en servletcontainergedrag. | Uitgevoerd voor lokale image `3.5.0`: Apache Tomcat 9.0.109. |
| Module upload endpoint controleren | Zip Slip is relevant als module upload bereikbaar is voor de gebruikte omgeving. | Gedeeltelijk uitgevoerd: in de eigen prod-compose staat `MODULE_WEB_ADMIN=false`; in dev/reference compose staat module web admin aan. |
| Module resource endpoint controleren | Path traversal is relevant als module resources via de runtime bereikbaar zijn. | Gedeeltelijk uitgevoerd: gateway/runtime routeert `/openmrs` en module resources kunnen onderdeel zijn van de OpenMRS runtime; echte productie-exposure nog bevestigen. |
| Productieconfiguratie controleren | Admin-only module upload hoort in productie beperkt of uitgeschakeld te zijn. | Uitgevoerd voor repository-config: `docker-compose.prod.yml` zet `MODULE_WEB_ADMIN=false` en mount de modulemap read-only (`:ro`). |

Samengevat: de compile-time Dependabot alert blijft zichtbaar omdat de module tegen oudere `provided` OpenMRS dependencies compileert. Voor de gecontroleerde local/reference runtime is het risico lager dan de alert suggereert, omdat de image OpenMRS `2.7.6` en Tomcat `9.0.109` gebruikt en de eigen prod-compose modulebeheer beperkt. Dit is nog geen definitieve false positive, omdat de echte productie-image/tag en endpoint-exposure door de repository/deployment owner bevestigd moeten worden.

## 8. Tooling- en bewijsstatus

| Onderdeel | Bewijs | Status | Vervolgactie |
|---|---|---|---|
| Dependabot alerts | `bewijs/sbom-sca/dependabot-alerts-overview.png`, Tika-detail screenshots en OpenMRS alert-overviews | Aanwezig | Detailpagina's blijven beschikbaar in GitHub Dependabot |
| Dependabot update PR's | `bewijs/repository-access/dependabot-prs-review-required.png` toont dependency-update PR's met `Review required` | Aanwezig | Updates beoordelen via PR-review voordat ze worden gemerged |
| PR securitychecks | `bewijs/scanning/pr-security-checks-passed.png` toont een gemergde PR met groene CodeQL-, Snyk- en code scanning-checks | Aanwezig | Failed checks blokkeren merge volgens de ingestelde PR/ruleset-werkwijze |
| SBOM | `bewijs/sbom-sca/sbom-workflow-run-artifact.png` en `bewijs/sbom-sca/sbom-artifact-upload-log.png` | Aanwezig | Artifact downloaden/bewaren bij auditbewijs indien nodig |
| Snyk SCA/SAST workflow | `bewijs/sbom-sca/snyk-workflow-run-artifact-success.png` | Aanwezig | Periodiek blijven draaien in CI |
| Snyk JSON artifacts | `bewijs/sbom-sca/snyk-artifact-upload-success.png` toont dat `snyk-sca.json` en `snyk-code.json` worden geupload | Aanwezig | Artifact downloaden/bewaren indien nodig |
| Apache Tika Dependabot alert | `bewijs/sbom-sca/dependabot-tika-critical-alert-full.png` en `bewijs/sbom-sca/dependabot-tika-critical-alert-details.png` tonen package, vulnerable range, patched version, CVSS 10.0, CVE en GHSA | Aanwezig | Java/OpenMRS-upgradepad of Java 8-compatibele gepatchte Tika-versie bepalen |
| Upload allowlist/MIME regressietests | `AttachmentResourceTest` en `AttachmentRestControllerTest` bewijzen veilige default allowlist, verboden extensies, MIME mismatch en expliciet toegestane legacy uploads | Aanwezig | Blijven opnemen in PR-testjob |

## 9. Vervolgacties

| Actie | Eigenaar | Status |
|---|---|---|
| Snyk JSON-artifacts downloaden en bewaren als bewijs | Team | Aanwezig in GitHub Actions; downloaden/bewaren indien nodig |
| CycloneDX SBOM-artifact bewaren bij auditbewijs | Team | Aanwezig in GitHub Actions; downloaden/bewaren indien nodig |
| Tika upgrade-impact testen op Java 8 en OpenMRS modulecompatibiliteit | Developer | Uitgevoerd; directe upgrade naar 3.2.2 faalt op Java 8 |
| Upload allowlist/MIME-validatie toevoegen als compensating control | Developer | Uitgevoerd en getest |
| OpenMRS Core upgradepad onderzoeken | Team | Gedeeltelijk: lokale reference image gebruikt OpenMRS 2.7.6 en bronmap noemt 2.8.6; productie-image/tag nog bevestigen |
| Runtime Tomcatversie controleren | Team | Uitgevoerd voor lokale reference image `3.5.0`: Tomcat 9.0.109 |
| Besluiten welke findings opgelost, geaccepteerd of als false positive geregistreerd worden | Team | Afgerond op hoofdlijn: Tika oplossen, OpenMRS findings open houden |

## 10. Advisory links

- Apache Tika XXE: https://github.com/advisories/GHSA-f58c-gq56-vjjf
- OpenMRS Module Upload Zip Slip: https://github.com/openmrs/openmrs-core/security/advisories/GHSA-78fc-9688-w8xw
- OpenMRS ModuleResourcesServlet path traversal: https://github.com/advisories/GHSA-jjgj-cx3q-pw4w
