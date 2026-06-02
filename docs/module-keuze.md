# Module-keuze: OpenMRS Attachments

Dit document legt vast welke module we onderzoeken in het kader van LU2 *Verbeter-onderzoek softwarekwaliteit*, en waarom. We hebben gekozen voor de **OpenMRS Attachments Module**.

## 1. Modulegegevens

| Onderdeel | Gegevens |
|---|---|
| Module | OpenMRS Attachments Module |
| Versie | 3.5.0 |
| Broncode | <https://github.com/openmrs/openmrs-module-attachments> |
| Type project | OpenMRS-module |
| Technologie | Java, Maven, OpenMRS REST Web Services |
| Doel | Patiëntbijlagen beheren via REST (uploaden, bekijken, downloaden) |

## 2. Korte beschrijving van de module

De Attachments Module is een uitbreiding op het OpenMRS-platform, een open source systeem voor elektronische patiëntdossiers dat wereldwijd in de zorg wordt gebruikt. De module maakt het mogelijk om bestanden aan een patiëntdossier te koppelen, zoals scans, foto's en documenten.

De functionaliteit wordt aangeboden via REST-endpoints, zodat front-end-applicaties en andere systemen bijlagen kunnen uploaden, opvragen en downloaden. Daarnaast zorgt de module voor de opslag van de bestanden en de bijbehorende metadata, en voor de koppeling tussen een bijlage en de juiste patiënt.

## 3. Motivatie voor de keuze

We hebben deze module gekozen omdat hij een goede balans biedt tussen omvang en onderzoekbaarheid. De module is niet te klein: hij bestaat uit meerdere lagen (REST-laag, service-laag en opslag), heeft externe dependencies en bevat logica die verder gaat dan eenvoudig data ophalen. Tegelijk is hij afgebakend genoeg om binnen de tijd van dit project grondig te bestuderen.

Verder is de module bij uitstek geschikt voor onderzoek naar **onderhoudbaarheid, security en compliance**:

- **Onderhoudbaarheid:** de module kent een duidelijke laagopbouw, gebruikt Maven voor afhankelijkheidsbeheer en bevat tests. Dit geeft genoeg materiaal om de structuur, leesbaarheid en testdekking kritisch te beoordelen.
- **Security:** het uploaden en downloaden van bestanden is van nature gevoelig. Denk aan bestandstypevalidatie, omvangslimieten en autorisatie. Dit zijn realistische risico's die we kunnen onderzoeken.
- **Compliance:** het gaat om medische patiëntgegevens. Daardoor zijn aspecten als toegangscontrole en zorgvuldige omgang met persoonsgegevens (denk aan AVG/GDPR) direct relevant.

Omdat het om echte, in productie gebruikte zorgsoftware gaat, zijn de bevindingen niet hypothetisch maar praktisch toepasbaar.

## 4. Scope van het onderzoek

Het onderzoek beperkt zich tot **de Attachments Module zelf**. We onderzoeken nadrukkelijk niet heel OpenMRS Core. OpenMRS Core wordt alleen meegenomen voor zover dat nodig is om de werking van de module te begrijpen (bijvoorbeeld de aangeroepen API's en het autorisatiemodel).

Binnen de module richten we ons op:

- de REST-endpoints;
- de upload- en download-functionaliteit;
- de bestandstypevalidatie;
- de autorisatie en toegangscontrole;
- de dependencies (via Maven);
- de aanwezige tests;
- de algemene onderhoudbaarheid van de code.

## 5. Kritieke functionaliteit

De volgende onderdelen beschouwen we als kritiek, omdat een fout of zwakte hier direct gevolgen heeft voor de betrouwbaarheid of veiligheid:

- **REST-endpoints:** het correct en veilig afhandelen van inkomende verzoeken.
- **Upload-functionaliteit:** het ontvangen, valideren en opslaan van bestanden.
- **Download-functionaliteit:** het veilig en gecontroleerd teruggeven van bestanden.
- **Bestandstypevalidatie:** het tegenhouden van ongewenste of gevaarlijke bestanden.
- **Autorisatie:** het garanderen dat alleen bevoegde gebruikers bij bijlagen kunnen.

## 6. Eerste aandachtspunten voor onderhoudbaarheid

- **Codestructuur:** is de scheiding tussen REST-laag, service-laag en opslag duidelijk en consequent?
- **Leesbaarheid:** zijn klassen en methoden begrijpelijk benoemd en niet te lang?
- **Testdekking:** welke onderdelen zijn getest en welke niet, en hoe betrouwbaar zijn de tests?
- **Dependencies:** zijn de gebruikte libraries actueel en goed beheerd via Maven?
- **Documentatie:** is er voldoende uitleg om de module te begrijpen en aan te passen?

## 7. Eerste aandachtspunten voor security en compliance

- **Bestandstypevalidatie:** worden alleen toegestane bestandstypen geaccepteerd, en gebeurt dat op basis van inhoud en niet alleen de extensie?
- **Omvangslimieten:** zijn er limieten op de bestandsgrootte om misbruik te voorkomen?
- **Autorisatie:** wordt bij elk endpoint gecontroleerd of de gebruiker de juiste rechten heeft?
- **Toegang tot patiëntgegevens:** kan een gebruiker alleen bijlagen zien van patiënten waarvoor hij bevoegd is?
- **Privacy en compliance:** wordt er zorgvuldig omgegaan met persoonsgegevens, in lijn met wetgeving zoals de AVG/GDPR?

## 8. Conclusie

De OpenMRS Attachments Module is een geschikte keuze voor dit verbeter-onderzoek. De module is groot en realistisch genoeg om diepgaand te onderzoeken, maar tegelijk afgebakend genoeg om binnen de projectplanning te passen. Doordat het gaat om het beheer van medische bestanden via REST, raken de thema's onderhoudbaarheid, security en compliance allemaal direct aan de praktijk. Daarmee biedt deze module voldoende inhoud om concrete en bruikbare verbeterpunten te formuleren.
