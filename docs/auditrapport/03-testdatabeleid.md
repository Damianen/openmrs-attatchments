# Testdatabeleid

## Doel

Dit testdatabeleid beschrijft hoe binnen het LU2 verbeteronderzoek wordt voorkomen dat gevoelige of echte productiegegevens in ontwikkel-, test- of CI/CD-omgevingen terechtkomen.

## Uitgangspunten

- Er wordt geen echte patientdata gebruikt in `dev`, `test`, screenshots, commits of CI/CD-runs.
- Testdata is synthetisch, geanonimiseerd of afkomstig uit veilige voorbeeldbestanden.
- Productiedata mag niet worden gekopieerd naar ontwikkel- of testomgevingen.
- Secrets, wachtwoorden en tokens worden niet opgeslagen in Git.
- Screenshots voor bewijsvoering mogen geen echte persoonsgegevens tonen.

## Toegestane testdata

Toegestaan:

- fictieve patientnamen;
- fictieve geboortedata;
- fictieve identifiers;
- voorbeeldafbeeldingen zonder medische of persoonlijke inhoud;
- testbestanden uit de repository, zoals OpenMRS logo's en synthetische XML-testdata.

Niet toegestaan:

- echte patientnamen;
- echte BSN's, medische nummers of andere identificerende gegevens;
- echte medische documenten;
- productie-databasedumps;
- echte API keys, wachtwoorden of tokens.

## Scheiding per omgeving

| Omgeving | Testdata-afspraak |
|---|---|
| `dev` | Alleen lokale synthetische testdata voor ontwikkeling. |
| `test` | Alleen controleerbare testdata voor regressietests en acceptatie. |
| `prod` | Geen testdata importeren; productie-achtige configuratie wordt gescheiden gehouden. |

De Docker-configuratie gebruikt per omgeving een eigen `.env` bestand. De template staat in `docker/.env.template`. Echte `.env` bestanden worden niet gecommit.

## CI/CD

In CI/CD worden alleen repositorybestanden, dependency scans, CodeQL-resultaten en SBOM-output verwerkt. Als testbestanden worden toegevoegd, moeten deze vooraf worden gecontroleerd op gevoelige inhoud.

## Controle

Voor nieuwe testdata geldt:

1. Controleer of de data synthetisch of geanonimiseerd is.
2. Controleer of er geen echte persoonsgegevens, secrets of medische documenten in staan.
3. Commit alleen bestanden die nodig zijn voor reproduceerbare tests of bewijsvoering.
4. Leg afwijkingen vast in het auditrapport of de security backlog.

## Koppeling met NEN-7510

Dit beleid ondersteunt de maatregelen rond:

- A.8.3 Toegangsbeveiliging;
- A.8.5 Authenticatie;
- A.8.8 Kwetsbaarheidsbeheer;
- A.8.15 Logging;
- A.8.25 Veilige ontwikkelcyclus;
- A.8.33 Testdata.
