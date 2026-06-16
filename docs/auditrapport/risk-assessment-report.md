# Risk assessment report - OpenMRS Attachments Module

## 1. Documentinformatie

| Onderdeel | Invulling |
|---|---|
| Project | OpenMRS Attachments Module |
| Document | Risk assessment report |
| Datum | 16 juni 2026 |
| Scope | Attachments module, REST endpoints, file storage, logging, CI/CD en dependencies |
| Methode | CIA/BIV, STRIDE, kans x impact en NEN-7510:2024-2 koppeling |
| Pentest | Nog niet opgenomen; pentest is nog niet afgerond |

## 2. Gebruikte bronnen

Dit rapport is gebaseerd op de documenten en bewijzen die al in het project aanwezig zijn:

| Bron | Waarvoor gebruikt |
|---|---|
| `docs/auditrapport/01-gap-analyse.md` | Eerste security gaps en codevoorbeelden |
| `docs/auditrapport/02-pipeline-compliance.md` | CI/CD, GitHub securitymaatregelen en pipeline-status |
| `docs/auditrapport/03-assets-risicocriteria.md` | Crown jewels, risicoscore en risicobereidheid |
| `docs/auditrapport/03-testdatabeleid.md` | Testdata-afspraken |
| `docs/auditrapport/04-threat-model.md` | STRIDE threat model en risicomatrix |
| `docs/auditrapport/05-security-backlog.md` | Security backlog en mitigaties |
| `docs/auditrapport/false-positive-beleid.md` | Werkwijze voor false positives |
| `docs/auditrapport/bewijs/` | Screenshots van pipeline- en repositorymaatregelen |
| `docs/architecture/` | C4-diagrammen voor systeem-, container- en componentniveau |

## 3. Managementsamenvatting

De OpenMRS Attachments Module verwerkt medische bijlagen en bijbehorende patientmetadata. Daardoor ligt de belangrijkste security-impact bij vertrouwelijkheid: onbevoegde toegang tot attachment-inhoud, bestandsnamen, patientgegevens of logs kan leiden tot een datalek.

De belangrijkste risico's zitten in file access, uploadvalidatie, download-autorisatie, logging en dependencybeheer. Een eerder CodeQL-risico rond een raw `/download?path=` downloadpad is in de huidige broncode niet meer teruggevonden en is gekoppeld aan commit `e9e4aa0 fix CodeQL security alerts`. De documentatie en validatie rondom dit punt moeten nog worden bijgewerkt.

De repository heeft al meerdere securitymaatregelen ingericht, zoals CodeQL/code scanning, Dependabot, SBOM-generatie, GitHub Environments, branch protection/rulesets, secret scanning en MFA-bewijs. Tegelijk blijven er nog open acties over, vooral rond pentest, validatie, false-positive registratie, dependencytriage en securitytests als quality gate.

## 4. Scope

In scope:

- uploaden van patient attachments;
- zoeken en ophalen van attachmentmetadata;
- downloaden van attachment bytes;
- REST API endpoints van de module;
- OpenMRS services die door de module worden gebruikt;
- attachment file storage;
- applicatielogs;
- CI/CD, SBOM, SCA, CodeQL en Dependabot op hoofdlijnen.

Out of scope:

- volledige OpenMRS Core implementatie;
- productie-infrastructuur buiten de repository;
- echte productiepatientdata;
- pentestresultaten, omdat de pentest nog niet is afgerond.

## 5. Assets en crown jewels

De belangrijkste te beschermen assets zijn:

| Asset | Classificatie | Belangrijkste impact |
|---|---|---|
| Bijlage-inhoud, zoals documenten, scans en foto's | PHI | Vertrouwelijkheid |
| Base64-gecodeerde uploadinhoud | PHI | Vertrouwelijkheid |
| Bestandsnaam | PII/PHI | Vertrouwelijkheid |
| Bijschrift of opmerking bij attachment | PHI | Vertrouwelijkheid |
| Patientnaam, geboortedatum en identifiers | PII | Vertrouwelijkheid |
| Patient UUID of interne patient-ID | PII | Vertrouwelijkheid |
| Encounter- en visit-koppeling | PHI | Vertrouwelijkheid en integriteit |
| Obs-datum/tijd | PHI metadata | Integriteit |
| MIME-type en content-family | PHI metadata | Vertrouwelijkheid |
| Provider-identiteit | PII | Vertrouwelijkheid |
| Audit- en wijzigingsmetadata | PII | Integriteit |
| Serverbestandsinhoud via onveilige file access | PHI/credentials | Vertrouwelijkheid |

Omdat de module medische gegevens verwerkt, is de risicobereidheid laag. Onbevoegde blootstelling van attachment-inhoud, serverbestanden of patient-PII in logs wordt als onacceptabel risico behandeld.

## 6. Risicocriteria

De risicoscore wordt berekend met:

`risicoscore = kans x impact`

Beide assen gebruiken een schaal van 1 tot en met 5.

| Scoreband | Betekenis | Actie |
|---|---|---|
| 1-6 | Laag risico | Accepteren en monitoren |
| 8-12 | Verhoogd risico | Mitigeren binnen afgesproken termijn |
| 15-25 | Hoog/onacceptabel risico | Direct behandelen en escaleren |

Voor patientdata geldt dat rode risico's niet onbehandeld mogen blijven. Oranje risico's kunnen tijdelijk bestaan als er een duidelijke backlogactie en opvolging is.

## 7. Risico-overzicht

| ID | Risico | BIV-impact | Kans | Impact | Score | Status |
|---|---|---|---:|---:|---:|---|
| T01 | Raw `/download?path=` kon willekeurig bestandspad lezen | Vertrouwelijkheid | 4 | 5 | 20 | In broncode gemitigeerd; documentatie/validatie moet nog bijgewerkt worden |
| T02 | Path traversal via `DefaultAttachmentHandler.getAttachmentByPath` | Vertrouwelijkheid, integriteit | 4 | 4 | 16 | Moet nog gedaan worden |
| T03 | Upload allowlist is te ruim als configuratie leeg is | Integriteit, beschikbaarheid | 4 | 4 | 16 | Moet nog gedaan worden |
| T04 | Download-autorisatie moet expliciet bewezen worden | Vertrouwelijkheid | 3 | 5 | 15 | Moet nog gedaan worden |
| T05 | Patientgegevens kunnen in logs terechtkomen | Vertrouwelijkheid, traceerbaarheid | 3 | 4 | 12 | Moet nog gedaan worden |
| T06 | Malformed base64 upload kan onduidelijk foutgedrag geven | Beschikbaarheid, integriteit | 3 | 3 | 9 | Moet nog gedaan worden |
| T07 | Legacy dependencies kunnen bekende CVE's bevatten | Vertrouwelijkheid, integriteit, beschikbaarheid | 3 | 4 | 12 | Moet nog gedaan worden |
| T08 | Deployment secrets en deployment workflow zijn nog niet volledig bewezen | Integriteit | 2 | 4 | 8 | Moet nog gedaan worden |
| T09 | Build/securitytests zijn nog niet volledig betrouwbaar als quality gate | Integriteit, traceerbaarheid | 3 | 3 | 9 | Moet nog gedaan worden |
| T10 | Attachmentmetadata kan verkeerd gekoppeld worden aan patient/context | Integriteit, vertrouwelijkheid | 2 | 4 | 8 | Moet nog gedaan worden |

## 8. Hoogste risico's

De hoogste risico's uit de bestaande analyse zijn:

| Prioriteit | Risico | Reden |
|---|---|---|
| 1 | File access en path traversal | Attachment file storage bevat medische bestanden. Onveilige paden kunnen leiden tot uitlekken of manipulatie. |
| 2 | Uploadvalidatie | Uploads zijn een belangrijk aanvalsvlak. Een lege of te ruime allowlist kan ongewenste bestandstypen toelaten. |
| 3 | Download-autorisatie | Bij patientattachments moet aantoonbaar zijn dat alleen bevoegde gebruikers bytes kunnen downloaden. |
| 4 | PII in logs | Logs zijn vaak breder toegankelijk dan medische data en mogen geen onnodige patientgegevens bevatten. |
| 5 | Dependency- en CI/CD-risico's | Kwetsbare dependencies of zwakke quality gates kunnen onveilige code laten doorstromen. |

## 9. Bestaande beveiligingsmaatregelen

| Maatregel | Bewijs | Status |
|---|---|---|
| GitHub Environments `dev`, `test` en `prod` | `bewijs/environments.png` | Aanwezig |
| Production protection rules | `bewijs/environment-rules.png` | Aanwezig |
| Branch protection / ruleset | `bewijs/ruleset.png` | Aanwezig |
| MFA voor teamleden | `bewijs/mfa-*.png` | Aanwezig |
| Dependabot | `.github/dependabot.yml`, `bewijs/dependabot.png` | Aanwezig |
| CodeQL/code scanning | `bewijs/codeql.png`, `bewijs/code-scanning.png` | Aanwezig |
| Secret scanning | `bewijs/security.png` | Aanwezig |
| SBOM-generatie | `.github/workflows/sbom.yml`, `bewijs/sbom-action-success.png`, `bewijs/sbom-artifact.png` | Aanwezig |
| Testdatabeleid | `docs/auditrapport/03-testdatabeleid.md` | Aanwezig |

## 10. NEN-7510:2024-2 koppeling

| Control | Relevantie in dit project | Gekoppelde risico's |
|---|---|---|
| A.8.3 Toegangsbeveiliging | Alleen bevoegde gebruikers mogen patientattachments lezen of wijzigen. | T01, T02, T04, T10 |
| A.8.5 Authenticatie/autorisatie | Rechten en omgevingsbescherming moeten aantoonbaar zijn. | T04, T08 |
| A.8.8 Kwetsbaarheidsbeheer | CVE's en dependencyrisico's moeten worden gevonden en opgevolgd. | T07 |
| A.8.15 Logging | Logs moeten bruikbaar zijn zonder onnodige patientgegevens te bevatten. | T05 |
| A.8.25 Secure SDLC | Security moet onderdeel zijn van branch, build, review en release. | T08, T09 |
| A.8.28 Secure coding | File access, uploads en parsing moeten veilig zijn ontworpen. | T01, T02, T03, T06 |
| A.8.29 Security testing | Belangrijke security-eisen moeten met tests worden bewezen. | T01, T02, T03, T04, T09 |
| A.8.33 Testdata | Testdata mag geen productiepatientdata bevatten. | Testdata-afspraken en pentestvoorbereiding |

## 11. Mitigatieplan

| Backlog ID | Maatregel | Prioriteit | Status |
|---|---|---|---|
| SB-01 | Verwijder of beveilig raw `/download?path=` en gebruik veilige UUID-download | P0 | In broncode gemitigeerd; documentatie/validatie moet nog bijgewerkt worden |
| SB-02 | Maak file access in handlers veilig met path normalization en root-check | P0 | Moet nog gedaan worden |
| SB-03 | Stel veilige upload allowlist in en valideer extensie en MIME altijd | P1 | Moet nog gedaan worden |
| SB-04 | Bewijs download-autorisatie met tests of expliciete check | P1 | Moet nog gedaan worden |
| SB-05 | Verwijder onnodige PII uit logs en voeg veilige auditlogging toe | P2 | Moet nog gedaan worden |
| SB-06 | Maak base64 upload parsing robuust | P2 | Moet nog gedaan worden |
| SB-07 | Triager dependency- en SBOM-findings | P2 | Moet nog gedaan worden |
| SB-08 | Maak securitytests betrouwbaar in CI | P2 | Moet nog gedaan worden |
| SB-09 | Test en versterk metadata-/patientbinding bij upload | P2 | Moet nog gedaan worden |
| SB-10 | Richt deployment secrets later veilig in via GitHub Environments | P3 | Moet nog gedaan worden |

## 12. Moet nog gedaan worden

De volgende onderdelen zijn nog niet volledig afgerond en worden daarom niet inhoudelijk als bewijs opgevoerd:

| Onderdeel | Actie |
|---|---|
| Pentest | Pentest afronden en daarna pas resultaten verwerken in een apart pentestdocument of als bijlage |
| Pentest-bewijs | Screenshots, stappen en resultaten pas toevoegen zodra de pentest klaar is |
| False-positive register | Registerdeel in `false-positive-beleid.md` aanvullen met beoordeelde scanbevindingen |
| Dependencytriage | SCA/SBOM/Dependabot findings beoordelen op CVSS, runtime-aanwezigheid en impact |
| Securitytests | Tests toevoegen voor path traversal, uploadvalidatie, autorisatie en base64 parsing |
| CI quality gate | Bepalen welke securitytests verplicht moeten slagen voordat een PR mag mergen |
| Documentatie bijwerken | Oude verwijzingen naar raw `/download?path=` controleren en bijwerken naar de huidige situatie |
| Kosteninschatting | Uren/kosten per mitigatie bepalen zodra scope en taakverdeling duidelijk zijn |

## 13. Conclusie

De risk assessment laat zien dat de grootste risico's draaien om vertrouwelijkheid van patientattachments en patientmetadata. De basisdocumenten zijn aanwezig: assets, risicocriteria, threat model, pipeline-compliance, testdatabeleid en security backlog.

De huidige beveiligingsinrichting bevat al belangrijke maatregelen zoals CodeQL, Dependabot, SBOM, secret scanning, MFA, environments en branch protection. De belangrijkste vervolgstap is het afronden van de open backlogpunten en pas daarna het toevoegen van pentestresultaten aan de bewijsvoering.
