# False-positivebeleid

## Doel

Dit beleid beschrijft hoe scanner-bevindingen (Snyk, CodeQL, Dependabot) worden getriageerd, wanneer een bevinding als false positive wordt aangemerkt en waar dit wordt vastgelegd. Zo blijft controleerbaar waarom een melding niet is opgevolgd en wie daarvoor heeft getekend.

## Triageproces

1. Elke nieuwe bevinding uit een scan wordt binnen vijf werkdagen beoordeeld door een teamlid.
2. De beoordelaar stelt vast of de bevinding terecht is (echte kwetsbaarheid), niet van toepassing (false positive) of geaccepteerd risico.
3. Terechte bevindingen worden opgelost of als issue op de security backlog gezet.
4. False positives en geaccepteerde risico's worden vastgelegd in het register (zie hieronder) en daarna pas onderdrukt in de tool.
5. Een tweede teamlid controleert de onderbouwing en tekent af. Niemand tekent zijn eigen triage af.

## Wanneer is iets een false positive?

Een bevinding wordt alleen als false positive aangemerkt als aan minstens een van deze voorwaarden is voldaan:

- de gemelde kwetsbare code of dependency wordt aantoonbaar niet uitgevoerd (bijvoorbeeld alleen in testscope);
- de scanner interpreteert de code aantoonbaar verkeerd (bijvoorbeeld een gemelde injectie waar de invoer al gevalideerd wordt);
- de bevinding betreft een omgeving of configuratie die in dit project niet bestaat.

Twijfelgevallen zijn geen false positive: die blijven open of worden als geaccepteerd risico met motivatie vastgelegd. "Te veel werk om op te lossen" is nooit een geldige reden.

## Register

False positives worden gelogd in `docs/auditrapport/false-positive-register.md` met per bevinding de volgende kolommen:

| Kolom | Inhoud |
|---|---|
| Finding-id | Het id van de bevinding in de tool (bijv. Snyk issue-id, CodeQL rule-id, CVE/GHSA-nummer). |
| Tool | De scanner die de melding gaf (Snyk SCA, Snyk Code, CodeQL, Dependabot). |
| Rationale | Concrete onderbouwing waarom de melding onterecht is, met verwijzing naar code of configuratie. |
| Wie tekent af | Naam van de tweede beoordelaar plus datum van aftekening. |

Onderdrukking in de tool zelf (bijv. een `.snyk` policy-bestand of het dismissen van een code scanning alert in GitHub) gebeurt pas na aftekening en verwijst in de toelichting naar het register.

## Herbeoordeling

Het register wordt per sprint doorgelopen. Een false positive vervalt als de onderbouwing niet meer klopt (bijvoorbeeld na een dependency-upgrade of codewijziging); de bevinding wordt dan opnieuw getriageerd.

## Koppeling met NEN-7510

Dit beleid ondersteunt de maatregelen rond:

- A.8.8 Kwetsbaarheidsbeheer;
- A.8.15 Logging;
- A.8.25 Veilige ontwikkelcyclus.
