# False-positivebeleid en register

## Doel

Dit document beschrijft hoe scanner-bevindingen (Snyk, CodeQL, Dependabot, SBOM/SCA) worden getriageerd, wanneer een bevinding als false positive wordt aangemerkt en waar dit wordt vastgelegd. Zo blijft controleerbaar waarom een melding niet is opgevolgd en wie daarvoor heeft getekend.

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

## Statuswaarden

| Status | Betekenis |
|---|---|
| Open | Finding is gezien, maar nog niet volledig onderzocht. |
| False positive | Finding is onderzocht en niet relevant of niet exploitable bevonden. |
| Geaccepteerd restrisico | Finding is echt, maar tijdelijk of bewust geaccepteerd met reden. |
| Opgelost | Finding is opgelost met codewijziging, configuratie of dependency-update. |
| Niet geaccepteerd | Finding is echt en moet nog worden opgelost. |

## Register

False positives en geaccepteerde risico's worden hieronder gelogd met per bevinding de tool, locatie, reden, bewijs en status.

| ID | Datum | Tool | Finding | Locatie | Severity | Beoordeling | Reden | Bewijs | Status | Afgetekend door |
|---|---|---|---|---|---|---|---|---|---|---|
| CS-001 | 2026-06-16 | CodeQL | Uncontrolled data used in path expression | `AttachmentBytesResource.java` | High | Echte finding | Raw `/download?path=` endpoint gebruikte user-controlled padinput. Endpoint is verwijderd in commit `e9e4aa0`. | PR #21 / commit `e9e4aa0` | Opgelost | - |
| CS-002 | 2026-06-16 | CodeQL | Polynomial regular expression used on uncontrolled data | `ObsByConceptListSearchHandler.java` | High | Echte finding | Regexverwerking op user-controlled input is aangepast en er is testdekking toegevoegd in commit `e9e4aa0`. | PR #21 / commit `e9e4aa0` | Opgelost | - |

Onderdrukking in de tool zelf (bijv. een `.snyk` policy-bestand of het dismissen van een code scanning alert in GitHub) gebeurt pas na aftekening en verwijst in de toelichting naar het register.

## Huidige status

Op dit moment zijn er nog geen scanmeldingen definitief als false positive beoordeeld. Er zijn wel CodeQL-findings geregistreerd die als echte bevinding zijn beoordeeld en opgelost.

Open scanmeldingen uit CodeQL, Dependabot, Snyk of SBOM/SCA moeten eerst worden onderzocht. Daarna kunnen ze in het register hierboven worden opgenomen als false positive, geaccepteerd restrisico, opgelost of nog open.

## Herbeoordeling

Het register wordt per sprint doorgelopen. Een false positive vervalt als de onderbouwing niet meer klopt (bijvoorbeeld na een dependency-upgrade of codewijziging); de bevinding wordt dan opnieuw getriageerd.

## Koppeling met NEN-7510

Dit beleid ondersteunt de maatregelen rond:

- A.8.8 Kwetsbaarheidsbeheer;
- A.8.15 Logging;
- A.8.25 Veilige ontwikkelcyclus.
