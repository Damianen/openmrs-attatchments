# Assets & Risicocriteria

Dit document legt de basis vast voor de risicobeoordeling van de OpenMRS Attachments module. Het bevat (1) de geïdentificeerde *crown jewels* — de gevoelige en patiëntgegevens die de module verwerkt, opslaat of blootstelt — (2) de risicoscoringsschaal (kans × impact) en (3) de risicobereidheid en grenswaarden waarmee scores worden vertaald naar groen/oranje/rood.

De crown-jewels-tabel is opgesteld op basis van een broncode-analyse van de module onder `api/` en `omod/`, waarbij de werkelijke datastromen (upload → opslag als complex obs/bestand → download) zijn getraceerd. Er zijn uitsluitend gegevensstromen opgenomen die aanwijsbaar zijn in de broncode (bestand + regelnummer).

---

## 1. Crown jewels — gevoelige en patiëntgegevens

| Gegevenstype | Classificatie | Waar verwerkt (bestand + regel) | Meest bedreigde CIA-dimensie |
|---|---|---|---|
| **Bijlage-inhoud** (geüploade bestandsbytes: medische scans, foto's, documenten) | PHI | Ingelezen `ComplexObsSaver.java:73,76,79` (image-stream) en `:91` (`getBytes()`); naar schijf weggeschreven via `ImageAttachmentHandler.java:77-94` en `DefaultAttachmentHandler.java:49-57`; teruggeleverd in `AttachmentBytesResource.java:74-79` | Vertrouwelijkheid |
| **Base64-gecodeerde bestandsinhoud** (alternatief uploadpad) | PHI | `AttachmentResource.java:113,118-119` en `:365-377` (`Base64MultipartFile`) | Vertrouwelijkheid |
| **Bestandsnaam** (originele filename, vaak met patiëntnaam/datum) | PII/PHI | Gelezen `AttachmentResource.java:127`; opgeslagen in `ValueComplex.java:25,57-64,101-103`; blootgesteld als `File-Name`-header `AttachmentBytesResource.java:70` | Vertrouwelijkheid |
| **Bijschrift / opmerking** (`fileCaption` → obs-comment, vrije klinische tekst) | PHI | Gezet `ComplexObsSaver.java:64`; gemapt `Attachment.java:55,127`; getoond in representatie `AttachmentResource.java:209,238` | Vertrouwelijkheid |
| **Patiëntidentiteit: naam, geboortedatum, identifiers** | PII | Wordt niet meer naar de attachment-fetch logregel geschreven; regressietest in `AttachmentsServiceImplTest` controleert dit | Vertrouwelijkheid |
| **Patiënt-UUID / interne patiënt-ID** | PII | `AttachmentResource.java:107,318`; `ObsByConceptListSearchHandler.java:62,70`; `AttachmentsServiceImpl.java:39-40` | Vertrouwelijkheid |
| **Klinische context: encounter- & visit-koppeling** (wanneer/waar patiënt gezien is) | PHI | `AttachmentResource.java:108-109,179-184`; `AttachmentsContext.java:160-190`; `AttachmentsServiceImpl.java:90-123` | Vertrouwelijkheid |
| **Obs-datum/tijd** van de bijlage | PHI (metadata) | `ComplexObsSaver.java:60-61`; `Attachment.java:54,115-121`; blootgesteld `AttachmentResource.java:210,237` | Integriteit |
| **MIME-type / content-family** (onthult type medisch document) | PHI (metadata) | `ValueComplex.java:23,51-55`; `AttachmentComplexDataImpl.java:13,46-52`; `Content-Family`-header `AttachmentBytesResource.java:69` | Vertrouwelijkheid |
| **Provider-identiteit** (zorgverlener gekoppeld aan encounter) | PII | `AttachmentResource.java:110`; `AttachmentsContext.java:178-180` | Vertrouwelijkheid |
| **Audit-/wijzigingsmetadata** (creator, changedBy, voidedBy, voidReason, dateCreated) | PII | `Attachment.java:44-52,91-97` | Integriteit |
| **Arbitraire serverbestandsinhoud** via ongesaneerde padparameter — kan PHI-bestanden en credential/config-bestanden van de server blootstellen | PHI / credential (blootstellingsvector) | Historische raw `/download?path=` finding is niet meer aanwezig; resterende file-access validatie zit bij `DefaultAttachmentHandler.getAttachmentByPath` | Vertrouwelijkheid |

**Toelichting bij enkele rijen:**

- **Kern-PHI is de bijlage-inhoud zelf.** Die wordt als complex obs / bestand op schijf opgeslagen en zonder her-versleuteling teruggeleverd via `AttachmentBytesResource.getFile()`. Vertrouwelijkheid is hier de dominante dimensie.
- **Drie stromen lekken extra of breder dan bedoeld** en springen eruit voor de risicobeoordeling:
  - `AttachmentsServiceImpl.java` logde eerder **naam, geboortedatum en identifiers** als platte tekst naar het applicatielog. Dit is gemitigeerd door de logregel te beperken tot minimale technische context en met `AttachmentsServiceImplTest` te controleren dat naam, geboortedatum en identifiers niet in de logtekst staan.
  - `AttachmentBytesResource.downloadFile()` (`:103-113`) leest een willekeurig bestand op basis van de `path`-requestparameter, zonder sanitisatie.
  - `DefaultAttachmentHandler.getAttachmentByPath()` (`:68-76`) bouwt een pad uit een client-geleverde `fileName` zonder padsanitisatie.
- De classificatie **credential** is uitsluitend van toepassing op de laatste rij: die endpoints geven geen bijlage-data terug maar arbitraire serverbestandsinhoud (mogelijk config-/credential-bestanden). Het is dus een blootstellingsvector, geen door de module opgeslagen gegevenstype.

---

## 2. Risicoscoringsschaal (kans × impact)

Elk risico krijgt een **risicoscore = kans × impact**, met beide assen op een schaal van 1 t/m 5. De score loopt daardoor van 1 (laagst) tot 25 (hoogst).

### 2.1 Kans (waarschijnlijkheid) — 1 t/m 5

| Waarde | Niveau | Omschrijving |
|---|---|---|
| 1 | Zeer onwaarschijnlijk | Treedt naar verwachting (vrijwel) nooit op; vereist een uitzonderlijke samenloop van omstandigheden. |
| 2 | Onwaarschijnlijk | Zou kunnen optreden, maar wordt niet verwacht binnen de beoordelingsperiode. |
| 3 | Mogelijk | Kan redelijkerwijs optreden; bekende kwetsbaarheid zonder volledige mitigatie. |
| 4 | Waarschijnlijk | Treedt naar verwachting op tenzij er actie wordt ondernomen; eenvoudig te misbruiken. |
| 5 | Zeer waarschijnlijk | Treedt vrijwel zeker (regelmatig) op; triviaal te misbruiken of al waargenomen. |

### 2.2 Impact (gevolg) — 1 t/m 5

| Waarde | Niveau | Omschrijving (PHI/PII-context, NEN-7510 / AVG) |
|---|---|---|
| 1 | Verwaarloosbaar | Geen of minimale gevolgen; geen persoonsgegevens betrokken, geen patiëntveiligheidsrisico. |
| 2 | Beperkt | Kleine, herstelbare gevolgen; beperkte interne impact, geen meldplicht. |
| 3 | Matig | Datalek van beperkte omvang of integriteitsverlies; mogelijke meldplicht, reputatieschade. |
| 4 | Ernstig | Lek/verlies van PHI/PII van meerdere patiënten; meldplicht AVG, boete- en reputatierisico. |
| 5 | Catastrofaal | Grootschalig lek van medische gegevens of patiëntveiligheidsrisico; ernstige juridische, financiële en reputatiegevolgen. |

### 2.3 Risicomatrix (5 × 5)

De cel toont de risicoscore (kans × impact) en is ingekleurd conform de grenswaarden uit sectie 3.2 (🟢 groen 1–6, 🟠 oranje 8–12, 🔴 rood 15–25).

| Impact ↓ \ Kans → | 1 | 2 | 3 | 4 | 5 |
|---|---|---|---|---|---|
| **5** | 🟢 5 | 🟠 10 | 🔴 15 | 🔴 20 | 🔴 25 |
| **4** | 🟢 4 | 🟠 8 | 🟠 12 | 🔴 16 | 🔴 20 |
| **3** | 🟢 3 | 🟢 6 | 🟠 9 | 🟠 12 | 🔴 15 |
| **2** | 🟢 2 | 🟢 4 | 🟢 6 | 🟠 8 | 🟠 10 |
| **1** | 🟢 1 | 🟢 2 | 🟢 3 | 🟢 4 | 🟢 5 |

---

## 3. Risicobereidheid & grenswaarden

### 3.1 Risicobereidheid (risk appetite)

Gezien de aard van de assets in sectie 1 — overwegend medische persoonsgegevens (PHI) en persoonsgegevens (PII) — hanteert de module een **lage risicobereidheid**. De leidende kaders zijn **NEN-7510:2024-2** en de **AVG**; waar deze normen strenger zijn dan de hieronder vastgelegde grenswaarden, prevaleren de normen.

Concreet:

- **Geaccepteerd restrisico zonder aanvullende maatregelen:** uitsluitend risico's in de **groene band** (score 1–6). Deze worden geaccepteerd en gemonitord.
- **Zwaarst wegende CIA-dimensie:** **Vertrouwelijkheid.** De meerderheid van de crown jewels (sectie 1) is het kwetsbaarst voor ongeautoriseerde inzage; bij gelijke score weegt een vertrouwelijkheidsrisico daarom zwaarder dan een integriteits- of beschikbaarheidsrisico.
- **Nul-tolerantie (ongeacht de berekende score):** ongeautoriseerde blootstelling van **bijlage-inhoud** en blootstelling van **arbitraire serverbestanden/credentials** via onveilige download- of file-access paden, evenals het wegschrijven van **patiënt-PII naar applicatielogs**. Deze risico's worden altijd als rood behandeld en moeten direct worden gemitigeerd, ongeacht de kans-inschatting.
- **Termijn voor oranje:** verhoogde risico's (score 8–12) worden geaccepteerd als tijdelijke situatie, mits er een mitigatieplan met einddatum is. Richttermijn: mitigeren binnen het lopende sprint-/auditcyclus.

### 3.2 Grenswaarden (groen / oranje / rood)

De risicoscore (1 t/m 25, sectie 2) wordt als volgt ingedeeld in banden. De bereiken dekken alle in een 5×5-matrix mogelijke productwaarden (1, 2, 3, 4, 5, 6, 8, 9, 10, 12, 15, 16, 20, 25).

| Band | Score-bereik | Betekenis | Vereiste actie |
|---|---|---|---|
| 🟢 Groen | 1 – 6 | Laag, aanvaardbaar restrisico | Accepteren en periodiek monitoren; geen aanvullende maatregelen vereist. |
| 🟠 Oranje | 8 – 12 | Verhoogd risico | Mitigeren binnen een afgesproken termijn; mitigatieplan met einddatum en eigenaar vastleggen. |
| 🔴 Rood | 15 – 25 | Onaanvaardbaar risico | Direct handelen en escaleren; behandeling vóór livegang/uitlevering. Geldt ook voor nul-tolerantie-risico's uit sectie 3.1. |

> De risicomatrix in sectie 2.3 is conform deze grenswaarden ingekleurd.
