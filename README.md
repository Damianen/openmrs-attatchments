# OpenMRS Attachments Module (backend)

The **Attachments** module brings a dedicated Java and web API to manage patient file attachments in OpenMRS.

It encompasses files uploaded elsewhere within OpenMRS as long as they are saved as [complex obs](https://wiki.openmrs.org/display/docs/Creating+Complex+Observations+and+Concepts).

In a nutshell the Attachments module is a **complex obs management API module** whose Java API is designed to be extended through new versions of the module to support further content types and concept complex coded obs.

## Content is handled based on its MIME type

The Attachments module is designed to handle content or MIME types on an ad-hoc basis. For example, images with `image/*` content types are provided a custom handler that saves them alongside their thumbnails.

## Not-yet-handled content types

When a content type is not provided a bespoke handling mechanism, it can still be accessed generically as it would be on any drive or storage.

## How to try it out

Build the master branch and install the built OMOD to your instance running OpenMRS with the REST web-services module installed.

```bash
git clone https://github.com/openmrs/openmrs-module-attachments
cd openmrs-module-attachments
mvn clean package
```

### Runtime requirements and compatibility

- [Core 2.3.0 and beyond](https://github.com/openmrs/openmrs-core)
- [OpenMRS REST Web Services module 2.33.0 and beyond](https://github.com/openmrs/openmrs-module-webservices.rest)

## LU2 projectomgeving

Voor het LU2 verbeteronderzoek is deze repository ingericht met gescheiden ontwikkel-, test- en productieomgevingen. De omgevingen worden gebruikt om het ontwikkelproces aantoonbaar veiliger te maken en om testdata, configuratie en productie-instellingen van elkaar te scheiden.

### Omgevingen

De repository gebruikt drie omgevingen:

| Omgeving | Doel | Configuratie |
|---|---|---|
| `dev` | Lokale ontwikkeling en experimenten | `docker/docker-compose.yml` met eigen `.env.dev` |
| `test` | Controleren van wijzigingen voor oplevering | `docker/docker-compose.yml` + `docker/docker-compose.test.yml` met eigen `.env.test` |
| `prod` | Productie-achtige omgeving met strengere regels | `docker/docker-compose.yml` + `docker/docker-compose.prod.yml` met eigen `.env.prod` |

In GitHub zijn de environments `dev`, `test` en `prod` aangemaakt. Voor `prod` zijn protection rules ingericht, waaronder een required reviewer, wait timer en beperking tot protected branches/tags. Bewijs hiervan staat in `docs/auditrapport/bewijs/`.

### Configuratie en secrets

Echte secrets worden niet in Git gezet. De template `docker/.env.template` laat zien welke variabelen per omgeving nodig zijn. Deze template wordt lokaal gekopieerd naar bijvoorbeeld `.env.dev`, `.env.test` of `.env.prod`.

Voorbeelden van gescheiden instellingen zijn:

- database naam;
- database gebruiker;
- database wachtwoord;
- root wachtwoord;
- poorten;
- debug-instellingen.

Omdat echte GitHub environment secrets nog niet zijn ingericht, is dit als beperking opgenomen in het pipeline-complianceverslag. Zodra secrets beschikbaar zijn, moeten ze per environment apart worden toegevoegd.

### Testdata

Test- en ontwikkelomgevingen mogen alleen synthetische of geanonimiseerde testdata gebruiken. Productiedata mag niet naar `dev` of `test` worden gekopieerd. Testdata moet herkenbaar nep zijn en mag geen echte patientgegevens, medische documenten of identificerende gegevens bevatten.

Het volledige testdatabeleid staat in `docs/auditrapport/03-testdatabeleid.md`.

### Nieuwe developer starten

1. Clone de repository.
2. Installeer Java 8, Maven en Docker.
3. Kopieer `docker/.env.template` naar een eigen environmentbestand, bijvoorbeeld `.env.dev`.
4. Vul alleen lokale testwaarden in, geen echte productiegegevens.
5. Bouw de module:

```bash
mvn clean package
```

6. Start een lokale omgeving met Docker Compose vanuit de `docker` map:

```bash
docker compose --env-file .env.dev -f docker-compose.yml up
```

Voor de testomgeving kan de test-override worden gebruikt:

```bash
docker compose --env-file .env.test -f docker-compose.yml -f docker-compose.test.yml up
```

Voor productie-achtige controles kan de productie-override worden gebruikt:

```bash
docker compose --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml up
```

### CI/CD en security checks

De repository gebruikt GitHub security tooling om kwetsbaarheden vroeg te signaleren:

- Dependabot voor Maven dependencies;
- CodeQL / code scanning;
- secret scanning;
- een SBOM-workflow met CycloneDX.

Bewijs van deze inrichting staat in `docs/auditrapport/bewijs/`. De resultaten worden meegenomen in de security backlog en het risk assessment.

## Release notes

### Version 3.0.0

Breaking changes:

- This module no longer supports the 2.x UI Framework functionalities. It has become headless with only REST endpoints for the management of attachments. For support of the removed features, use version 2.6.0 or below.
