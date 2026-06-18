# Aangepast ontwerp — storage-credentials uit de omgeving

## 1. Aanleiding

De gap-analyse en pentest (zie [`security/01-gap-analyse.md`](../../security/01-gap-analyse.md))
constateerden dat de module cloud-storage-credentials **hardcoded** in de broncode had staan.
In `AttachmentsActivator.java` stonden een access key, secret key en een bucket/endpoint als
`private static final` velden. Een secret in version control is een lek: iedereen met
repo-toegang (of een gelekte kloon) heeft de sleutel, en roteren vereist een code-wijziging.

Deze A3-stap (PoC voor "Realisatie /10") verwijdert de hardcoded secrets en leest ze voortaan
uit de **runtime-omgeving**. De wijziging is tevens de mitigatie waarop de hertest van de pentest
steunt.

## 2. Aangepast ontwerp

De credentials worden nu uit **environment variables** gelezen via een kleine,
single-responsibility provider. Bewust **niet** via OpenMRS global properties: die worden in de
database opgeslagen, en dat is geen geschikte plek voor secrets. De omgeving houdt de secrets
zowel buiten de broncode als buiten de applicatiedatabase.

Twee nieuwe, kleine typen in `org.openmrs.module.attachments`:

- **`StorageConfig`** — enige bron van waarheid voor de storage-credentials/endpoint. Biedt
  `getStorageAccessKey()`, `getStorageSecretKey()` en `getStorageBucket()`. Een private
  `require(...)` faalt *fail-fast* met een `IllegalStateException` als een variabele ontbreekt,
  zodat er nooit stilzwijgend op een hardcoded waarde wordt teruggevallen.
- **`EnvironmentSource`** — kleine interface (`String get(String name)`) die de bron van
  configuratiewaarden abstraheert. De productie-constructor van `StorageConfig` leest hiermee uit
  `System.getenv`; tests injecteren een stub.

De variabelenamen staan als constanten in `AttachmentsConstants`
(`ENV_STORAGE_ACCESS_KEY` = `ATTACHMENTS_STORAGE_ACCESS_KEY`, enz.).

### SOLID-onderbouwing

- **Single Responsibility:** `StorageConfig` doet één ding — storage-config leveren. De activator
  hoeft niets meer van credentials te weten.
- **Dependency Inversion:** `StorageConfig` hangt af van de abstractie `EnvironmentSource`, niet
  rechtstreeks van `System.getenv`. Daardoor is de credential-resolutie testbaar zonder de echte
  proces-omgeving te muteren (wat in JUnit 4 niet zonder extra libraries kan).

## 3. Before / after (codeniveau)

**Before** — `AttachmentsActivator.java`:

```java
// Storage backend credentials for the attachments archive service
private static final String STORAGE_ACCESS_KEY = "AKIA-ATTACHMENTS-SVC-7K3M";
private static final String STORAGE_SECRET_KEY = "wJalrXUtnFEMI/AttachSvc/bPxRfiCYEXAMPLEKEY";
private static final String STORAGE_BUCKET = "openmrs-attachments-prod-eu-west";
```

**After** — de velden zijn weg uit de activator; credentials komen uit `StorageConfig`:

```java
public String getStorageAccessKey() {
    return require(AttachmentsConstants.ENV_STORAGE_ACCESS_KEY); // System.getenv, geen hardcoded waarde
}
```

## 4. Docker-wiring

De `openmrs`-service in `docker/docker-compose.yml` geeft de drie variabelen door vanuit het
`.env`-bestand (zelfde patroon als de bestaande `DB_*`-credentials; de `test`/`prod`-overlays
erven dit env-blok). In `docker/.env.dev`, `.env.test`, `.env.prod` en `.env.template` staan
**alleen placeholders** — nooit echte secrets. In productie worden de echte waarden door de
deploy-pipeline geïnjecteerd.

## 5. Testbewijs

`StorageConfigTest` (plain JUnit 4) bewijst dat:

1. de credentials uit de omgeving komen (en niet de oude hardcoded `AKIA-...`-waarde zijn);
2. een ontbrekende variabele fail-fast een `IllegalStateException` oplevert.

Dit voegt coverage toe op nieuwe code (helpt de quality gate uit
[`02-quality-gate.md`](../02-quality-gate.md)).

## 6. Referentie

Wijziging op branch `security/storage-credentials-env`. Gewijzigde/nieuwe bestanden:
`StorageConfig.java`, `EnvironmentSource.java`, `AttachmentsConstants.java`,
`AttachmentsActivator.java`, `StorageConfigTest.java`, `docker/docker-compose.yml` en de
`docker/.env*`-bestanden. Het exacte commit-hash staat in de PR.
