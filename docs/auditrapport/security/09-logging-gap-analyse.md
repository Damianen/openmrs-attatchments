# Logging gap analyse - NEN-7510 A.8.15

## 1. Doel

Dit document beoordeelt de logging van de OpenMRS Attachments Module voor Sprint 3. De focus ligt op NEN-7510:2024-2 A.8.15: gebeurtenissen moeten bruikbaar worden vastgelegd, maar logs mogen geen onnodige gevoelige patientdata bevatten.

Dit document sluit aan op:

- `01-gap-analyse.md`
- `08-attack-surface-overview.md`
- `04-threat-model.md`
- `05-security-backlog.md`

## 2. Beoordelingscriteria

Een logging-event is compliant genoeg wanneer:

- duidelijk is welke technische actie is uitgevoerd;
- succes en foutpaden voor kritieke acties te reconstrueren zijn;
- er geen patientnaam, geboortedatum, interne patient-id of patient identifiers in plaintext worden gelogd;
- de logregel genoeg context bevat voor beheer of incidentonderzoek;
- securityrelevante fouten niet stil verdwijnen.

Niet elk event hoeft in applicatielogs terecht te komen. Voor privacygevoelige medische data geldt dat dataminimalisatie belangrijker is dan zoveel mogelijk detail loggen.

## 3. Huidige loggingpunten

| Codeplek | Event | Huidige logging | Beoordeling |
|---|---|---|---|
| `AttachmentsActivator.java` | Module start/stop/refresh | `info` logs zonder patientdata | Acceptabel |
| `AttachmentsServiceImpl.java:40` | Attachments ophalen voor patient | `info` met minimale technische context | Gedeeltelijk acceptabel; PII-test aanwezig |
| `AttachmentsServiceImpl.java:135` | Concept UUID niet gevonden | `error` met concept UUID | Acceptabel, geen patientdata |
| `AttachmentsServiceImpl.java:141` | Geen complex concepts geconfigureerd | `warn` zonder patientdata | Acceptabel |
| `AttachmentsContext.java:286` | Fout bij parsen map global property | `error` met propertynaam en exception | Acceptabel |
| `AttachmentsContext.java:316` | Fout bij parsen list global property | `error` met propertynaam en exception | Acceptabel |
| `ObsByConceptListSearchHandler.java:73` | Patient UUID niet gevonden bij concept-search | `warn` met patient UUID | Twijfelachtig; UUID kan gevoelige context zijn |
| `ObsByConceptListSearchHandler.java:139` | Concept niet gevonden | `warn` met concept query | Acceptabel mits geen vrije patientdata |
| `AttachmentBytesResource.java` | Download attachment bytes | Geen duidelijke auditlog | Gap |
| `AttachmentResource.upload` | Upload success/failure | Geen duidelijke auditlog | Gap |
| `AttachmentResource.delete` | Attachment verwijderen/voiden | Geen duidelijke auditlog | Gap |

## 4. Eventmatrix

| ID | Attack surface | Event | Gelogd? | Gevoelige data risico | NEN-7510 A.8.15 status | Actie |
|---|---|---|---|---|---|---|
| LG-01 | AS-01 Upload | Succesvolle upload | Nee/niet expliciet | Hoog: patient, filename, caption kunnen gevoelig zijn | Gap | Voeg veilige auditlog toe met technische context, geen patientnaam/geboortedatum/identifier |
| LG-02 | AS-01 Upload | Upload geweigerd door extensie/MIME/max size | Nee/niet expliciet | Middel: filename kan gevoelig zijn | Gap | Log securityrelevante weigering zonder volledige filename of patientdata |
| LG-03 | AS-01 Upload | Patient/visit/encounter mismatch | Nee/niet expliciet | Hoog: verkeerde koppeling kan datalek betekenen | Gap | Log mismatch als securityrelevant event zonder patientdetails |
| LG-04 | AS-06 Download bytes | Succesvolle download | Nee | Hoog: toegang tot patientbestand | Gap | Voeg auditlog toe of onderbouw waarom OpenMRS audit voldoende is |
| LG-05 | AS-06 Download bytes | Download geweigerd door ontbrekend privilege | Nee/niet expliciet | Middel | Gap | Log geweigerde toegang zonder attachmentinhoud of patientdata |
| LG-06 | AS-04 Search | Search attachments voor patient | Ja via `AttachmentsServiceImpl` bij service-call | Middel: patient identifiers mogen niet in log | Gedeeltelijk aanwezig | Bestaande PII-test behouden; uitbreiden naar logniveau als mogelijk |
| LG-07 | AS-05 Concept search | Patient UUID niet gevonden | Ja, met patient UUID | Middel | Herbeoordelen | Overweeg minder specifieke logtekst of hash/technische request context |
| LG-08 | AS-03 Delete | Attachment verwijderen/voiden | Nee/niet expliciet | Hoog: integriteitsimpact | Gap | Voeg auditlog toe voor delete/void met veilige technische context |
| LG-09 | AS-09 Config | Fout bij global property parsing | Ja | Laag | Aanwezig | Geen actie behalve test/controle waar nodig |
| LG-10 | Module lifecycle | Start/stop/refresh | Ja | Laag | Aanwezig | Geen actie nodig |

## 5. Gewenste loggingregels

Voor nieuwe of aangepaste logging gelden deze regels:

| Regel | Uitleg |
|---|---|
| Geen directe patient PII | Geen patientnaam, geboortedatum, intern patient-id of patient identifiers in plaintext. |
| Geen bestandsinhoud | Nooit attachment bytes, base64-content of documentinhoud loggen. |
| Voorzichtig met filename/caption | Bestandsnamen en captions kunnen medische informatie bevatten; niet volledig loggen tenzij noodzakelijk en onderbouwd. |
| Wel technische context | Eventtype, resultaat, resource type, eventueel attachment UUID of obs UUID als dat nodig en toegestaan is. |
| Foutpaden zichtbaar | Geweigerde upload/download en contextmismatch moeten traceerbaar zijn. |

## 6. Teststrategie

Sprint 3 vraagt tests voor logging. De minimale set is:

| Test | Doel | Status |
|---|---|---|
| Attachment-fetch log bevat geen patient PII | Bewijst dat patientnaam, geboortedatum, interne id en identifiers niet worden gelogd | Aanwezig in `AttachmentsServiceImplTest` |
| Upload success logging | Bewijst dat upload veilig auditbaar is zonder PII | Nog te maken |
| Upload failure logging | Bewijst dat geweigerde extensie/MIME of contextmismatch zichtbaar is zonder PII | Nog te maken |
| Download success logging | Bewijst dat download van bytes auditbaar is zonder PII | Nog te maken of onderbouwen via OpenMRS audit |
| Download denied logging | Bewijst dat ontbrekende privileges traceerbaar zijn zonder PII | Nog te maken |
| Delete/void logging | Bewijst dat verwijderactie auditbaar is zonder PII | Nog te maken |

## 7. Voorlopige conclusie

De module heeft al veilige logging voor attachment-fetches en algemene module/config-events. De grootste Sprint 3-gaps zitten bij kritieke acties die patientattachments raken:

1. upload success/failure;
2. download success/denied;
3. delete/void;
4. patient/visit/encounter mismatch;
5. concept-search logging met patient UUID.

De volgende stap is per gap te kiezen of we logging in code toevoegen of expliciet onderbouwen dat OpenMRS platform-audit dit event al afdekt. Voor de events die we zelf loggen, moeten regressietests bewijzen dat geen gevoelige patientdata in de logtekst terechtkomt.
