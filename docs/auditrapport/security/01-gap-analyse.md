# Gap-Analyse

Dit document beschrijft de gap-analyse van de OpenMRS Attachments module ten opzichte van drie specifieke beveiligingscontrols uit de NEN-7510:2024-2 normering.

---

## 1. Control A.8.3: Toegangsbeveiliging
**Eis:** Gebruikers mogen alleen toegang krijgen tot het netwerk en de netwerkdiensten waarvoor zij specifiek geautoriseerd zijn (Least Privilege).
**Status: gedeeltelijk aanwezig**

**Bewijs & Analyse:**
De module bouwt op het OpenMRS framework, dat op platformniveau toegangsbeveiliging afdwingt. Echter, op moduleniveau ontbreekt bij specifieke endpoints de benodigde input-validatie en autorisatiecontrole. Het belangrijkste bewijs hiervoor is te vinden in het zelfgemaakte download endpoint, dat een Path Traversal / Local File Inclusion (LFI) risico introduceert.
*Bestand: `AttachmentBytesResource.java` (Regels 103-113)*
```java
	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public void downloadFile(@RequestParam("path") String path, HttpServletResponse response) throws java.io.IOException {
		// DANGEROUS: Directly reading files from the filesystem based on user input without any access control or path validation.
		java.io.File file = new java.io.File(path);
		java.io.FileInputStream fis = new java.io.FileInputStream(file);
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = fis.read(buffer)) != -1) {
			response.getOutputStream().write(buffer, 0, bytesRead);
		}
		fis.close();
	}
```
**Toelichting:** Omdat de "path" parameter volledig user-controlled is en er geen whitelist of expliciete privilege-check (zoals Context.requirePrivilege()) wordt toegepast, zou een gebruiker bestanden buiten de bedoelde map kunnen benaderen. Hoewel de framework-security bestaat, ontbreekt in dit specifieke endpoint de "defense-in-depth" voor toegangscontrole.
## 2. Control A.8.5: Authenticatie
**Eis:** Er moet sprake zijn van een betrouwbare verificatie van de identiteit van de gebruiker voordat toegang wordt verleend.
**Status: Gedeeltelijk Aanwezig**

**Bewijs & Analyse:**
De module bevat zelf geen actieve authenticatiemechanismen (zoals login-, token- of sessiecontroles). Dit is deels by design, aangezien de module deze verantwoordelijkheid delegeert naar het onderliggende OpenMRS Core platform.
*Bestand: `AttachmentResource.java`*
```java
@Resource(name = RestConstants.VERSION_1 + "/"
        + AttachmentsConstants.ATTACHMENT_URI, supportedClass = Attachment.class, supportedOpenmrsVersions = {
                "2.2.* - 9.*" })
public class AttachmentResource extends DataDelegatingCrudResource<Attachment> implements Uploadable { ... }
```
**Toelichting:** De endpoints zijn gebouwd als extensie op DataDelegatingCrudResource en BaseRestController. Hierdoor vertrouwt de module passief op de overkoepelende authenticatiefilters van de OpenMRS /rest/v1/ API. Als OpenMRS globaal goed is geconfigureerd, is authenticatie afgedwongen, maar "defense-in-depth" ontbreekt in de module zelf, wat extra risico oplevert bij kwetsbaarheden zoals gevonden bij A.8.3.

## 3. Control A.8.15: Logging
**Eis:** Gebeurtenissen (zoals toegang tot patiëntgegevens, fouten en informatiebeveiligingsincidenten) moeten worden vastgelegd (gelogd) en bewaard.
**Status: Gedeeltelijk aanwezig**

**Bewijs & Analyse:**
Er is logging aanwezig om te registreren welke bestanden worden opgevraagd, wat een goede eerste stap is richting compliancy, maar de manier waarop dit wordt gedaan levert een datalek van medische persoonsgegevens (PII) in de systeemlogs op.
**Bestand: AttachmentsServiceImpl.java (Regels 38-43)**
```java
// Audit logging for regulatory compliance — records which patient's attachments are accessed
		log.info("Fetching attachments for patient: id=" + patient.getPatientId()
		    + " uuid=" + patient.getUuid()
		    + " name=" + patient.getPersonName()
		    + " dob=" + patient.getBirthdate()
		    + " identifiers=" + patient.getIdentifiers());  // PII written to application log
```
**Toelichting:** In bovenstaand fragment werden patiëntnaam, geboortedatum en medische identifiers als platte tekst (plaintext) in de applicatielogs geschreven. Hoewel dit voldoet aan de eis "er moet gelogd worden dat data is ingezien", is het direct in strijd met de AVG en NEN-7510 richtlijnen rondom dataminimalisatie.

**Status:** Deze bevinding is in de code gemitigeerd. `AttachmentsServiceImpl` logt nu alleen minimale technische context voor het ophalen van attachments. `AttachmentsServiceImplTest` controleert dat patientnaam, geboortedatum, interne patient-id en identifiers niet in de logtekst staan.

Daarnaast mist er audit-logging bij kritieke acties; er wordt in AttachmentBytesResource.java helemaal niets gelogd wanneer een bestand via de /download functie wordt opgehaald.

## 4. Geprioriteerde non-compliance lijst

Onderstaande lijst bundelt de belangrijkste afwijkingen uit de gap-analyse, pentest, SCA/SBOM-triage en auditdocumentatie. De volgorde is gebaseerd op risico voor vertrouwelijkheid, integriteit en aantoonbaarheid richting NEN-7510.

| Prioriteit | Non-compliance | NEN-7510-control | Status | Vervolgactie |
|---|---|---|---|---|
| 1 | Hardcoded credential-achtige waarden stonden in broncode. | A.8.5, A.8.25, A.8.28 | Opgelost | Hardcoded waarden zijn verwijderd uit `AttachmentsActivator.java`; hertestbewijs staat in `bewijs/pentest/`. |
| 2 | Raw path download/path traversal kon leiden tot arbitrary file read. | A.8.3, A.8.28, A.8.29 | Opgelost | UUID-downloadflow, privilegecheck en regressietests zijn toegevoegd; bewijs staat in pentest- en CodeQL-documentatie. |
| 3 | File access in attachment handler had onvoldoende traversal-bescherming. | A.8.28, A.8.29 | Opgelost | Path normalization/root-check en regressietests zijn toegevoegd. |
| 4 | Uploadvalidatie kon te ruim zijn bij lege allowlist of MIME mismatch. | A.8.28, A.8.29 | Opgelost | Veilige default allowlist, extensiecontrole, MIME-controle en tests zijn toegevoegd. |
| 5 | Download-autorisatie was onvoldoende expliciet bewezen op moduleniveau. | A.8.3, A.8.5, A.8.29 | Gedeeltelijk opgelost | Expliciete `View Attachments` check en regressietests zijn toegevoegd; patienttoegang blijft afhankelijk van OpenMRS runtimebeleid. |
| 6 | Logging bevatte te veel patient-PII of was onvoldoende veilig tegen vervuilde logregels. | A.8.15, A.8.28 | Gedeeltelijk opgelost | Logberichten zijn geminimaliseerd en getest voor onderzochte paden; volledige productie-auditlogging blijft vervolgwerk. |
| 7 | Critical dependencyfinding Apache Tika XXE is nog niet definitief opgelost. | A.8.8, A.8.28, A.8.29 | Open | Java/OpenMRS upgradepad bepalen of veilige Java 8-compatibele oplossing vinden; compensating controls zijn tijdelijk toegevoegd. |
| 8 | OpenMRS Core runtimealerts voor Zip Slip/path traversal zijn nog niet met productiegegevens bevestigd. | A.8.8, A.8.25, A.8.29 | Open | Owner moet runtimeversie, Tomcatversie en endpoint-exposure bevestigen. |
| 9 | Maven Tests zijn ingericht als required check voor PR's naar `main`. | A.8.25, A.8.29 | Opgelost | `api-tests` en `omod-security-tests` zijn verplicht via branch/ruleset protection; bewijs staat in `bewijs/repository-access/maven-tests-required-check.md`. |
| 10 | Environment secrets en deploymentflow zijn nog niet volledig aantoonbaar ingericht. | A.8.5, A.8.25 | Open | Echte secrets pas toevoegen via GitHub Environments zodra ze beschikbaar zijn; production approval behouden. |

Deze lijst is bedoeld als compact stuurdocument voor de resterende audit- en Sprint 4-acties. Details, bewijs en risicoweging staan in `risk-assessment-report.md`, `06-sca-sbom-triage.md`, `11-traceability-matrix.md` en `12-security-audit-eindrapport.md`.
