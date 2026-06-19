# Geprioriteerde Verbeteragenda en Kwaliteitsmatrix

Op basis van de gecombineerde resultaten uit de statische code-analyse (de SonarCloud baseline van scan 1), de dynamische testdekking van JaCoCo en de security-audit is een concrete, geprioriteerde verbeteragenda opgesteld. Dit overzicht stelt het ontwikkelteam in staat om de technische schuld van 868 minuten en kritieke security-risico's doelgericht te reduceren.

## Impact / Effort Matrix

Hieronder worden de geïdentificeerde verbeterpunten geclassificeerd op basis van hun verwachte impact op de softwarekwaliteit (ISO 25010: Beveiliging en Onderhoudbaarheid) versus de benodigde ontwikkel- en testinspanning (Effort).

| Verbeterpunt | Verwachte Impact | Benodigde Inspanning (Effort) | Categorie |
|-------------|-----------------|-------------------------------|-----------|
| Cloud Storage Secrets Refactor (StorageConfig) | Extreem Hoog (CWE-798 security-fix) | Laag | Quick Win / Hoge Prioriteit |
| Opsplitsen van de AttachmentsContext Facade Hub | Hoog (ISO 25010: Analyseerbaarheid en Aanpasbaarheid) | Hoog | Major Project |
| Ontkoppelen REST-laag van Domeinentiteiten (AttachmentResource) | Hoog (Verlagen van tight coupling tussen presentatie- en datalaag) | Medium | Strategische Verbetering |
| Sanering van de 116 SonarCloud Code Smells | Medium (Verlagen van de 14 uur en 28 minuten technische schuld) | Medium | Fill-in Jobs |
| Uitbreiden OMOD Takdekking (Branch Coverage naar boven de 65 procent) | Medium (Verhogen van regressiepreventie en testbaarheid) | Medium | Continu Onderhoud |

## Gedetailleerde Verbeteragenda (Geprioriteerde Lijst met Rationale)

### Prioriteit 1: Cloud Storage Secrets Refactor (StorageConfig Implementatie)

**Rationale:**  
Dit item staat met absolute prioriteit op nummer één omdat het rondslingeren van plaintext infrastructurele tokens in de activator (CWE-798) een kritiek beveiligingsrisico vormde voor de AWS cloud-productieomgeving. Dit was de zwaarste bevinding uit de security-audit.

**Herleidbaarheid:**  
Direct aangetoond via de handmatige code-audit. De oplossing (verplaatsing naar StorageConfig en EnvironmentSource) herstelt het Single Responsibility Principle en is dekkings-technisch afgedekt door de nieuwe JUnit 4 testklasse StorageConfigTest.

### Prioriteit 2: Architectonische opsplitsing van de AttachmentsContext Facade Hub

**Rationale:**  
Uit de SonarCloud complexiteits-triage blijkt dat AttachmentsContext met 1.093 regels code veruit de grootste klasse van het project is. De klasse vertoont een extreem hoge afferente koppeling (wordt door 10 andere klassen in beide modules gebruikt). Dit maakt het een single point of failure en een architectonische bottleneck voor wijzigingen.

**Herleidbaarheid:**  
Traceerbaar naar paragraaf 4 van de onderhoudbaarheid-analyse (Hotspots in Complexiteit). Door deze klasse op te splitsen in kleinere, domeinspecifieke services (bijvoorbeeld een losse configuratie-service en een losse CRUD-service) stijgt de Analyseerbaarheid (NFR-1) aanzienlijk.

### Prioriteit 3: Ontkoppelen van AttachmentResource en OpenMRS Domeinentiteiten

**Rationale:**  
De REST-laag in de omod-module vertoont tight coupling. AttachmentResource (609 regels code) importeert en manipuleert rechtstreeks database-gekoppelde domeinentiteiten zoals Obs, Patient en Encounter, in plaats van uitsluitend via de service-facade te communiceren. Een wijziging in het OpenMRS datamodel dwingt hierdoor direct een wijziging af in de REST-presentatielaag.

**Herleidbaarheid:**  
Direct herleidbaar naar de SonarCloud-triage omtrent Architectonische Koppeling. De introductie van Data Transfer Objects (DTO's) of een extra abstractielaag zal deze koppeling verbreken en de Aanpasbaarheid (NFR-2) optimaliseren.

### Prioriteit 4: Gericht saneren van de 116 SonarCloud Code Smells

**Rationale:**  
Hoewel het project een Maintainability Rating A bezit, vertegenwoordigen de 116 geregistreerde issues een technische schuld van bijna twee volledige werkdagen (868 minuten). Een concreet voorbeeld van deze schuld is de openstaande self-admitted technical debt (Sonar-regel java:S1135) in AttachmentsContext.java op regel 383 met de tekst:

> TODO: Figure out if this is good enough.

**Herleidbaarheid:**  
Herleidbaar naar paragraaf 3.2 en 4 van de onderhoudbaarheid-analyse. Het systematisch oplossen van deze smells (zoals het wegpoetsen van dode code, ongebruikte imports en het oplossen van de openstaande TODO-comments) houdt de codebase gezond en voorkomt bit rot.

### Prioriteit 5: Verhogen van de Takdekking (Branch Coverage) in de OMOD Module

**Rationale:**  
Uit de aanvullende dekkingsmetingen blijkt dat de totale branch coverage van het project achterblijft op 49,4 procent, en dat de testdekking van de web-module (66 procent) lager scoort dan de api-core (74 procent). Complexe conditionele logica (zoals de streaming-afhandeling en invoervalidatie in AttachmentBytesResource) moet intensiever worden getest onder verschillende randvoorwaarden om runtime-crashes te voorkomen.

**Herleidbaarheid:**  
Direct herleidbaar naar de JaCoCo-cijfers uit paragraaf 3.3 van de analyse en de testopzet-documentatie. Het toevoegen van gerichte integratietests met variërende HTTP-payloads zal de Testbaarheid (NFR-3) naar de gewenste Quality Gate-norm trekken.