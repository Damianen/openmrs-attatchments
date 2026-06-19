# B2 - Maven Tests als verplichte status check op `main`

Dit document beschrijft hoe de `Maven Tests`-workflow als verplichte status check
op de `main`-branch wordt afgedwongen, plus de verificatie en het bijbehorende bewijs.
Hiermee wordt het OTAP-onderdeel verdedigd: ongeteste of ongereviewde code kan niet
naar `main` mergen.

## Exacte check-namen

De workflow `.github/workflows/maven-tests.yml` heet **Maven Tests** en heeft twee jobs.
De namen zoals getoond in de PR-checks, en dus de contexts die als required check
geselecteerd zijn, zijn:

- `api-tests`
- `omod-security-tests`

Beide draaien groen op `main` via GitHub Actions en zijn als required checks ingesteld.
Daardoor gatet de volledige Maven testworkflow de merge naar `main`.

## Mechanisme

`main` wordt beschermd door de actieve ruleset **protect-main**. Die dwingt af:

- geen directe ongecontroleerde wijziging naar `main`;
- verplichte pull request review;
- geen force-push;
- required status checks voor `api-tests` en `omod-security-tests`.

Een PR kan daardoor niet gemerged worden zolang een van deze Maven checks ontbreekt,
nog draait of faalt.

## Verificatie

De controle is als volgt:

1. Open een PR naar `main`.
2. Terwijl `api-tests` of `omod-security-tests` nog draaien, blokkeert GitHub de merge.
3. Als een required check faalt, blijft de merge geblokkeerd.
4. Pas wanneer beide checks groen zijn en de review-eis is gehaald, kan de PR worden gemerged.

Dit sluit aan op de ingestelde branch/ruleset-protection en maakt security regressietests
een echte quality gate in plaats van alleen een informatieve workflow.

## Bewijs

Bewijs hoort in deze map:

`docs/auditrapport/security/bewijs/repository-access/`

Aanbevolen bewijsbestanden:

| Bestand | Toont |
|---|---|
| `maven-tests-required-check-ruleset.png` | Ruleset/settings-pagina waarin `api-tests` en `omod-security-tests` required zijn. |
| `maven-tests-required-check-blocks-merge.png` | PR-mergebox waarin merge geblokkeerd wordt totdat required checks groen zijn. |
| `maven-tests-required-check-mergeable.png` | Optioneel bewijs dat merge pas mogelijk is nadat de checks groen zijn. |
| `maven-tests-required-checks-green.png` | PR-checkoverzicht waarin `api-tests` en `omod-security-tests` groen zijn en allebei als `Required` gemarkeerd staan. |

## Beheercommando voor later

Als de ruleset later opnieuw moet worden ingesteld, kan een repository-owner de bestaande
ruleset uitbreiden met een `required_status_checks`-regel voor:

```text
api-tests
omod-security-tests
```

De exacte instellingen kunnen via GitHub UI worden gecontroleerd onder:

`Settings -> Rules -> Rulesets -> protect-main -> Require status checks to pass`
