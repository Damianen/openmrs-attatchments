# Attack surface overzicht - OpenMRS Attachments Module

## 1. Doel

Dit document brengt de attack surface van de OpenMRS Attachments Module in kaart voor Sprint 3. Het overzicht laat zien via welke ingangen data de module binnenkomt, welke onderdelen impliciet worden vertrouwd en welke ingangen als high risk worden gezien.

Dit document vult het threat model aan:

- `04-threat-model.md`
- `05-security-backlog.md`
- `09-logging-gap-analyse.md`

## 2. Scope

In scope:

- REST endpoints van de Attachments module;
- upload, download, zoekfunctionaliteit en verwijderen van attachments;
- global properties/configuratie die securitygedrag beinvloedt;
- file storage en complex obs handlers;
- logging rond toegang tot patientattachments;
- CI/CD en dependency-ingangen op hoofdlijnen.

Out of scope:

- volledige OpenMRS Core authenticatie-implementatie;
- productie-infrastructuur buiten de repository;
- handmatige pentest-uitvoering. Die wordt apart vastgelegd in het pentestdocument.

## 3. Samenvatting high-risk ingangen

| Ingang | Waarom high risk | Status |
|---|---|---|
| Upload attachment | User-controlled bestand, filename, MIME/content en patient/visit/encounter parameters komen binnen via REST. | Gedeeltelijk gemitigeerd met allowlist, MIME/extensievalidatie en regressietests. |
| Download attachment bytes | Geeft patientbestanden terug. Onjuiste autorisatie leidt direct tot datalek. | Gemitigeerd met privilegecheck, regressietest en veilige auditlogging. |
| Search attachments | Geeft patientmetadata en attachmentmetadata terug op basis van requestparameters. | Inputvalidatie aanwezig; extra REST-search auditlog niet toegevoegd vanwege dataminimalisatie. |
| File storage handler | Leest bestanden uit storage op basis van bestandsnaam/pad. | Path traversal is gemitigeerd/getest voor handlerlogica; blijft belangrijk attack surface. |
| Global properties | Configuratie bepaalt uploadlimieten, allowed extensions en denied filenames. | Relevante waarden moeten in OTAP/prod gecontroleerd worden. |
| Dependencies/SCA | Kwetsbare libraries kunnen runtime-risico geven. | SCA/SBOM triage bestaat; Tika blijft open door Java 8-upgradepad; OpenMRS runtime moet door owner/deploymentbeheerder worden bevestigd. |

## 4. Attack surface tabel

| ID | Ingang | Type | Code / bewijs | User-controlled input | Vertrouwde componenten | Risico | High risk? | Huidige maatregel |
|---|---|---|---|---|---|---|---|---|
| AS-01 | Upload attachment | REST upload | `AttachmentResource.upload`, `AttachmentResource.java:110` | Bestand, filename, content type, patient, visit, encounter, provider, fileCaption, instructions | OpenMRS REST auth, `AttachmentsContext`, Tika, OpenMRS patient/visit/encounter services | Malicious upload, MIME bypass, verkeerde patientkoppeling, DoS door grote bestanden | Ja | Max size, default allowlist, extensie/MIME-validatie, denied filenames, patient/visit/encounter checks, regressietests en veilige auditlogging |
| AS-02 | Attachment metadata ophalen | REST read | `AttachmentResource.getByUniqueId`, `AttachmentResource.java:84` | UUID | OpenMRS REST resource security, ObsService | Onbevoegde metadata-inzage | Ja | Framework security; expliciete autorisatie blijft afhankelijk van OpenMRS REST laag |
| AS-03 | Attachment verwijderen | REST delete | `AttachmentResource.delete`, `AttachmentResource.java:96` | UUID en delete reason | OpenMRS REST auth, ObsService | Onterecht voiden/verwijderen van patientattachment | Ja | OpenMRS REST auth en veilige auditlogging voor delete/purge |
| AS-04 | Attachment zoeken | REST search | `AttachmentResource.doSearch`, `AttachmentResource.java:390` | patient, visit, encounter, includeVoided, includeEncounterless | OpenMRS services, `AttachmentsService` | Metadata disclosure, brede search, verkeerde contextfiltering | Ja | Contextvalidatie bij upload; service-level fetch logging zonder patient-PII; geen extra REST-search log vanwege dataminimalisatie |
| AS-05 | Search by concept list | REST search handler | `ObsByConceptListSearchHandler`, `ObsByConceptListSearchHandler.java:40` | patient UUID, concept queryparameters | OpenMRS patient/concept services | ReDoS/query abuse, metadata disclosure, logging van identifiers | Middel | CodeQL regex finding eerder beoordeeld; lookup logging aangepast zodat patient UUID en conceptwaarde niet in logs komen |
| AS-06 | Download attachment bytes | REST download | `AttachmentBytesResource.getBytes`, `AttachmentBytesResource.java:42` | Attachment UUID | OpenMRS auth, `Context.hasPrivilege`, ObsService, complex obs handler | IDOR, datalek van attachment bytes | Ja | Expliciete privilegecheck, regressietest en veilige auditlogging aanwezig |
| AS-07 | File storage lezen | File system | `DefaultAttachmentHandler.getAttachmentByPath`, `DefaultAttachmentHandler.java:73` | Bestandsnaam/pad uit complex obs metadata | Attachment directory, Java file/path APIs | Path traversal, arbitrary file read | Ja | Path traversal tests/fix aanwezig in Sprint 2 |
| AS-08 | File storage schrijven | File system | `ComplexObsSaver`, attachment handlers | Uploadbestand en afgeleide bestandsnaam | Attachment directory, OpenMRS complex obs | Overschrijven, ongewenste content, storage DoS | Ja | Uploadvalidatie, max file size en handlers; verdere logging nog beoordelen |
| AS-09 | Global properties | Config | `AttachmentsConstants` GP_* en `config.xml` | Admin-configuratie | OpenMRS admin/configbeheer | Te ruime allowlist, te grote uploads, onveilige defaults | Middel | Veilige default allowlist in code; prod-config nog controleren |
| AS-10 | CI/CD workflow | Build pipeline | `.github/workflows/*.yml` | Pull requests, dependencies, workflow config | GitHub Actions, Maven, Snyk, CodeQL | Supply chain, ongeteste wijzigingen, onveilige dependencies | Middel | CodeQL, Snyk, SBOM, Maven Tests workflow; required-check selectie blijft bewijsactie |

## 5. Impliciet vertrouwen

| Vertrouwd onderdeel | Waarop wordt vertrouwd | Risico als aanname niet klopt | Actie |
|---|---|---|---|
| OpenMRS REST authenticatie | Requests naar `/rest/v1` zijn geauthenticeerd. | Onbevoegde gebruikers kunnen endpoints bereiken. | In documentatie vastleggen en waar mogelijk privilegechecks/testbewijs toevoegen. |
| OpenMRS privileges | Gebruikerrechten bepalen toegang tot patientdata. | Download of metadata kan breder toegankelijk zijn dan bedoeld. | Kritieke download heeft expliciete privilegecheck; search/delete nog beoordelen. |
| UUID parameters | UUIDs verwijzen naar de juiste patient, visit, encounter of attachment. | IDOR of verkeerde patientcontext. | Patient/visit/encounter mismatchtests behouden en uitbreiden waar nodig. |
| Tika MIME-detectie | Gedetecteerde MIME past bij werkelijke bestandsinhoud. | MIME spoofing of parserkwetsbaarheid. | Compensating controls behouden; Tika upgradepad open houden. |
| OpenMRS runtime image | De module draait in de OpenMRS/Tomcat runtime die door Docker of productie wordt geleverd. | Compile-time dependency alerts kunnen afwijken van de echte runtime; kwetsbare endpoints kunnen per omgeving verschillen. | Alleen repository-prodconfig is bewezen: `MODULE_WEB_ADMIN=false` en modules read-only; productie-runtime en Tomcatversie nog door owner bevestigen. |
| Global properties | Admin-config is correct en niet te ruim. | Lege/te ruime allowlist of uploadlimieten. | Veilige defaults, configcontrole en bewijs in CI/prod opnemen. |
| Attachment file storage | Complex obs metadata verwijst naar bestanden binnen attachment-root. | Path traversal of uitlezen van serverbestanden. | Path normalization/root-checks en regressietests behouden. |
| GitHub Actions | CI draait op elke PR en checks blokkeren merge. | Kwetsbare code kan zonder tests binnenkomen. | Maven Tests als required check selecteren na eerste workflow-run. |

## 6. Koppeling naar logging

Voor Sprint 3 moet per relevante ingang worden bekeken of het event gelogd moet worden. De logging-gap staat apart in:

- `09-logging-gap-analyse.md`

Belangrijk uitgangspunt: logs moeten genoeg informatie geven voor audit en incidentonderzoek, maar mogen geen onnodige patientnaam, geboortedatum, interne patient-id of patient identifiers bevatten.

## 7. Conclusie

De grootste attack surface zit bij upload, download, search en file storage. Dit zijn de plekken waar patientattachments of patientmetadata direct geraakt kunnen worden. Voor Sprint 3 zijn daarom de belangrijkste vervolgstappen:

1. threat model bijwerken met deze attack surface;
2. logging gap analyse afronden per event;
3. loggingtests behouden voor succesvolle en mislukte kritieke acties;
4. coverage zichtbaar houden in CI en JaCoCo artifacts blijven beoordelen.
