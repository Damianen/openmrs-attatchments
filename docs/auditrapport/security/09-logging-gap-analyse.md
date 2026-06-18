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
- user-controlled waarden geen CR/LF/tab-karakters in logregels kunnen injecteren;
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
| `ObsByConceptListSearchHandler.java:73` | Patient UUID niet gevonden bij concept-search | `warn` met generiek event/entity/result | Acceptabel; patient UUID wordt niet gelogd |
| `ObsByConceptListSearchHandler.java:139` | Concept niet gevonden | `warn` met generiek event/entity/result | Acceptabel; conceptwaarde wordt niet gelogd |
| `AttachmentBytesResource.java` | Download attachment bytes | Auditlog voor success, denied en failed download zonder patient-PII | Aanwezig; unit test controleert veilige logberichtinhoud |
| `AttachmentResource.upload` | Upload success/failure | Auditlog voor success en denied upload zonder patient-PII | Aanwezig; unit test controleert veilige logberichtinhoud |
| `AttachmentResource.delete` | Attachment verwijderen/voiden | Auditlog voor delete/purge success en failed zonder patient-PII | Aanwezig; unit test controleert veilige logberichtinhoud |

## 4. Eventmatrix

| ID | Attack surface | Event | Gelogd? | Gevoelige data risico | NEN-7510 A.8.15 status | Actie |
|---|---|---|---|---|---|---|
| LG-01 | AS-01 Upload | Succesvolle upload | Ja | Hoog: patient, filename, caption kunnen gevoelig zijn | Aanwezig | Veilige auditlog bevat technische context, geen filename/caption/patient-PII |
| LG-02 | AS-01 Upload | Upload geweigerd door extensie/MIME/max size | Ja | Middel: filename kan gevoelig zijn | Aanwezig | Weigering wordt gelogd zonder volledige filename of patientdata |
| LG-03 | AS-01 Upload | Patient/visit/encounter mismatch | Ja | Hoog: verkeerde koppeling kan datalek betekenen | Aanwezig | Mismatch valt onder denied upload logging zonder patientdetails |
| LG-04 | AS-06 Download bytes | Succesvolle download | Ja | Hoog: toegang tot patientbestand | Aanwezig | Download wordt gelogd met attachment UUID, view, MIME en bytecount |
| LG-05 | AS-06 Download bytes | Download geweigerd door ontbrekend privilege | Ja | Middel | Aanwezig | Geweigerde toegang wordt gelogd zonder attachmentinhoud of patientdata |
| LG-06 | AS-04 Search | Search attachments voor patient | Ja via `AttachmentsServiceImpl` bij service-call | Middel: patient identifiers mogen niet in log | Aanwezig | Geen extra REST-search auditlog toegevoegd om dubbele patientcontext in applicatielogs te voorkomen |
| LG-07 | AS-05 Concept search | Patient/concept lookup niet gevonden | Ja, zonder patient UUID of conceptwaarde | Laag | Aanwezig | Logt alleen event, entity en result; regressietest bewijst dat user input niet in logtekst staat |
| LG-08 | AS-03 Delete | Attachment verwijderen/voiden | Ja | Hoog: integriteitsimpact | Aanwezig | Delete/purge wordt gelogd met attachment UUID en encounter UUID |
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
| Geen multiline logwaarden | User-controlled waarden worden gesanitized zodat CR/LF/tab geen vervalste logregels kunnen maken. |
| Foutpaden zichtbaar | Geweigerde upload/download en contextmismatch moeten traceerbaar zijn. |

## 6. Teststrategie

Sprint 3 vraagt tests voor logging. De minimale set is:

| Test | Doel | Status |
|---|---|---|
| Attachment-fetch log bevat geen patient PII | Bewijst dat patientnaam, geboortedatum, interne id en identifiers niet worden gelogd | Aanwezig in `AttachmentsServiceImplTest` |
| Upload success logging | Bewijst dat upload veilig auditbaar is zonder PII | Aanwezig via veilige logbericht-unit test |
| Upload failure logging | Bewijst dat geweigerde extensie/MIME of contextmismatch zichtbaar is zonder PII | Aanwezig via denied upload logpad en veilige logbericht-unit test |
| Download success logging | Bewijst dat download van bytes auditbaar is zonder PII | Aanwezig via veilige logbericht-unit test |
| Download denied logging | Bewijst dat ontbrekende privileges traceerbaar zijn zonder PII | Aanwezig via denied download logpad en veilige logbericht-unit test |
| Delete/void logging | Bewijst dat verwijderactie auditbaar is zonder PII | Aanwezig via veilige logbericht-unit test |
| Concept-search lookup logging | Bewijst dat patient UUID en conceptwaarde niet in de lookup-logtekst komen | Aanwezig in `ObsByConceptListSearchHandlerTest` |

## 7. Search metadata auditlogging besluit

Voor `AttachmentResource.doSearch` wordt geen extra REST-level auditlog toegevoegd in deze sprint. De reden is dat search-requests patient-, visit- en encountercontext bevatten. Extra requestlogging kan daardoor snel dezelfde patientcontext dubbel vastleggen.

De bestaande service-level logging in `AttachmentsServiceImpl` geeft al technische zichtbaarheid op attachment-fetches en is getest op het vermijden van patientnaam, geboortedatum, interne patient-id en patient identifiers. Daarom is de huidige keuze:

| Onderdeel | Besluit |
|---|---|
| Extra REST-search auditlog | Niet toevoegen in Sprint 3 |
| Reden | Dataminimalisatie; voorkomen dat patientcontext dubbel in applicatielogs komt |
| Huidige maatregel | Service-level fetch logging zonder patient-PII plus regressietest |
| Herbeoordelen wanneer | Als OpenMRS platform-audit onvoldoende blijkt of als er een expliciete audit-eis komt voor search-requests |

## 8. Voorlopige conclusie

De module heeft nu veilige logging voor attachment-fetches, upload, download bytes, delete/purge, concept-search lookup failures en algemene module/config-events. De belangrijkste resterende Sprint 3-aandachtspunten zijn:

1. bepalen of integratietests met echte log capture nodig zijn naast de huidige unit tests op veilige logberichtinhoud;
2. later controleren of OpenMRS platform-audit voldoende zichtbaarheid geeft voor search-requests.

Voor de events die we zelf loggen, bewijzen regressietests dat geen gevoelige patientdata in de logtekst terechtkomt.
