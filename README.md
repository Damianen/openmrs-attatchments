# OpenMRS Attachments Module (backend)

De **Attachments**-module voegt een Java- en REST-API aan OpenMRS toe voor het beheren van patiëntbijlagen. Bijlagen worden opgeslagen als [complex obs](https://wiki.openmrs.org/display/docs/Creating+Complex+Observations+and+Concepts) en op basis van hun MIME-type afgehandeld: afbeeldingen krijgen bijvoorbeeld een eigen handler die thumbnails meeslaat, en niet-herkende bestandstypen blijven generiek benaderbaar als gewone bestanden. De module is headless (alleen REST-endpoints) en vereist OpenMRS Core 2.3.0+ met de REST Web Services-module.

## Hoe de OTAP-omgevingen zijn ingericht

De omgevingen draaien via Docker Compose vanuit de map `docker/`. Het basisbestand `docker-compose.yml` beschrijft de dev-omgeving (OpenMRS + MariaDB 10.8); `docker-compose.test.yml` en `docker-compose.prod.yml` zijn override-bestanden die daar bovenop worden gelegd:

| | dev | test | prod |
|---|---|---|---|
| Compose-bestanden | `docker-compose.yml` | `+ docker-compose.test.yml` | `+ docker-compose.prod.yml` |
| Webpoort (standaard) | 8080 | 8081 (db extern op 3307) | 8080 |
| `OMRS_DEV_DEBUG` | aan | uit | uit |
| `MODULE_WEB_ADMIN` | aan | aan | **uit** |
| Module-mount | lees/schrijf | lees/schrijf | **read-only** (`:ro`) |
| Extra | — | — | `restart: always`, healthchecks op OpenMRS en db |

```bash
cd docker

# dev
docker compose --env-file .env.dev up -d

# test
docker compose --env-file .env.test -f docker-compose.yml -f docker-compose.test.yml up -d

# prod
docker compose --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml up -d
```

**Gescheiden configuratie per omgeving.** Elke omgeving heeft een eigen env-bestand (`.env.dev`, `.env.test`, `.env.prod`), aangemaakt vanaf de template `docker/.env.template`. Daarin staan o.a. de databasenaam, -gebruiker en -wachtwoorden én een eigen `COMPOSE_PROJECT_NAME`. Doordat Docker Compose containers en volumes per projectnaam namespacet, krijgt elke omgeving zo zijn eigen containers en eigen volumes (`<project>_db-data`, `<project>_openmrs-data`).

**Secrets staan niet in git.** `.gitignore` sluit `docker/.env` en `docker/.env.*` uit; alleen de lege `docker/.env.template` wordt gecommit. Daarnaast zijn in GitHub de Environments `dev`/`test`/`prod` ingericht, met protection rules op `prod` (required reviewer, wait timer, protected branches/tags); zie [docs/auditrapport/security/02-pipeline-compliance.md](docs/auditrapport/security/02-pipeline-compliance.md).

## Hoe wordt voorkomen dat testdata in productie terechtkomt

Het uitgangspunt staat in het [testdatabeleid](docs/auditrapport/security/03-testdatabeleid.md): in `dev` en `test` wordt uitsluitend synthetische of geanonimiseerde testdata gebruikt, productiedata wordt nooit gekopieerd naar ontwikkel- of testomgevingen, en in `prod` wordt geen testdata geïmporteerd.

Technisch wordt dat als volgt afgedwongen:

- **Aparte databases en volumes per omgeving.** Elke omgeving draait met een eigen `COMPOSE_PROJECT_NAME` en eigen `.env`, en heeft daardoor een eigen MariaDB-container met eigen `db-data`- en `openmrs-data`-volumes. Data uit dev of test kan dus niet "meeliften" naar de productiedatabase: het zijn fysiek gescheiden databases.
- **Read-only module-mount in prod.** In `docker-compose.prod.yml` is de modulemap (`../omod/target/modules`) read-only gemount (`:ro`), zodat er vanuit de draaiende productiecontainer niets in de modulebestanden kan worden gewijzigd.
- **`MODULE_WEB_ADMIN` uit in prod.** In productie staat de webbeheerinterface voor modules uit, zodat er niet via de OpenMRS-admin handmatig modules (en daarmee ongecontroleerde wijzigingen) in productie geladen kunnen worden. In dev en test staat deze aan voor ontwikkelgemak.
- **Debug uit buiten dev.** `OMRS_DEV_DEBUG` staat alleen in dev aan; in test en prod is dit uitgeschakeld.

## Hoe een nieuwe ontwikkelaar de omgeving opzet

Vereisten: git, JDK 8, Maven en Docker met het Compose-plugin.

1. **Clone de repository:**

   ```bash
   git clone git@github.com:Damianen/openmrs-attatchments.git
   cd openmrs-attatchments
   ```

2. **Bouw de module** (met JDK 8, net als de CI):

   ```bash
   JAVA_HOME=/usr/lib/jvm/java-8-openjdk mvn clean package
   ```

   Dit levert `omod/target/attachments-3.5.0.omod` op.

3. **Zet de omod in de mount-map.** Docker Compose mount `omod/target/modules/` als modulemap in de container, maar de Maven-build maakt die map niet zelf aan:

   ```bash
   mkdir -p omod/target/modules
   cp omod/target/*.omod omod/target/modules/
   ```

4. **Maak je dev-configuratie aan** vanaf de template en vul de waarden in:

   ```bash
   cd docker
   cp .env.template .env.dev
   ```

   Bijvoorbeeld: `COMPOSE_PROJECT_NAME=attachments-dev`, `OMRS_PORT=8080`, `OMRS_DEV_DEBUG=true` en zelfgekozen databasenaam/-gebruiker/-wachtwoorden. Commit dit bestand nooit.

5. **Start de omgeving:**

   ```bash
   docker compose --env-file .env.dev up -d
   ```

6. **Log in.** De eerste start duurt enkele minuten (OpenMRS zet de database op). Ga daarna naar [http://localhost:8080/openmrs](http://localhost:8080/openmrs) en log in met de standaardgebruiker van de reference application: `admin` / `Admin123`. Controleer onder *Administration → Manage Modules* dat de Attachments-module geladen is.

Stoppen kan met `docker compose --env-file .env.dev down`.
