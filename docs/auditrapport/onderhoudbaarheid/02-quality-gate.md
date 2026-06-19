# Quality gate: meten en rapporteren (niet-blokkerend) - Sprint 4

## 1. Doel

In Sprint 3 (zie [`security/10-coverage-quality-gate.md`](../security/10-coverage-quality-gate.md)) is besloten om coverage en kwaliteit te **meten en te reviewen**, maar nog niet hard af te dwingen. In de A1-stap is daarna een SonarCloud-analyse toegevoegd via `.github/workflows/sonar.yml`.

Dit document beschrijft hoe de SonarCloud Quality Gate is ingericht als **meet- en rapportagecontrole**: bij elke PR en op het dashboard is zichtbaar of de gate **Passed** of **Failed** is, maar de CI-build wordt hier **bewust niet** op hard afgebroken. De gate-status is daarmee een expliciet reviewsignaal in plaats van een technische blokkade. Dit sluit aan op het Sprint 3-besluit om te "meten, rapporteren en reviewen" in plaats van een harde fail-threshold af te dwingen.

## 2. Hoe de gate wordt geëvalueerd (en waarom niet-blokkerend)

De SonarCloud-scan draait in `.github/workflows/sonar.yml`:

```
mvn -B "-Dformatter.skip=true" \
    org.sonarsource.scanner.maven:sonar-maven-plugin:sonar
```

De scanner uploadt de analyse naar SonarCloud; SonarCloud evalueert vervolgens de Quality Gate en toont de uitkomst:

- op het **projectdashboard** in SonarCloud (Passed/Failed met de falende condities);
- als **PR-decoratie / check** op de pull request, zodat reviewers de status meteen zien.

De scan gebruikt **niet** de flag `sonar.qualitygate.wait=true`. Daardoor wacht de scanner niet op het gate-resultaat en eindigt de stap met exitcode 0 zolang de analyse zelf slaagt. Een **Failed** gate maakt de workflow-run dus **niet** rood; de status is informatief en wordt via PR-review beoordeeld.

### Waarom niet hard blokkeren

- De module is legacy/OpenMRS-code met sterke frameworkafhankelijkheden (Spring REST, DataDelegatingCrudResource, obs-integratie). De baseline stabiliseert nog; een harde gate kan PR's blokkeren om redenen die niets met de wijziging te maken hebben.
- Dit is consistent met het Sprint 3-besluit (`security/10-coverage-quality-gate.md` §7): kwaliteit en coverage worden **gemeten, gerapporteerd en gereviewd**, niet met een technische fail-threshold afgedwongen.
- Het afdwingen kan later alsnog terug door `sonar.qualitygate.wait=true` weer aan het scancommando toe te voegen, zodra de baseline stabiel genoeg is en het team dit wil.

## 3. Uitgangspunt: Clean as You Code

De module is legacy/OpenMRS-code met sterke frameworkafhankelijkheden (Spring REST, DataDelegatingCrudResource, obs-integratie). Het in één keer wegwerken van alle bestaande technische schuld is binnen deze sprint niet realistisch, en condities op de *volledige* codebase zouden elke PR rood kleuren om redenen die niets met de wijziging te maken hebben.

Daarom volgen we het SonarCloud-principe **Clean as You Code**: de Quality Gate beoordeelt vooral **nieuwe en gewijzigde code** (New Code), niet de hele legacy-baseline. Nieuwe code wordt aan een strengere lat gehouden dan de bestaande 60%-startnorm uit Sprint 3. Zo neemt de schuld niet verder toe en verbetert de kwaliteit geleidelijk met elke PR.

## 4. Geconfigureerde condities (passende gate)

De volgende condities zijn op **New Code** geconfigureerd. Faalt één conditie, dan rapporteert SonarCloud de gate als **Failed** — zichtbaar op het dashboard en de PR, maar zonder de CI-build te breken (zie sectie 2).

| Conditie (New Code) | Operator | Drempel | Onderbouwing |
|---|---|---|---|
| Coverage | is minder dan | **70%** | Hoger dan de 60% legacy-startnorm uit Sprint 3 (baseline: api 74%, omod-securitysubset 66%), maar lager dan de Sonar-default van 80%. Nieuwe code in deze module bevat nog onvermijdelijke OpenMRS framework-glue (resource-registratie, delegating CRUD) die via integratietests wordt geraakt en niet altijd zuiver unit-testbaar is. |
| Duplicated Lines (%) | is meer dan | **3%** | Sonar-default; op nieuwe code goed haalbaar en houdt copy-paste tegen. |
| Maintainability Rating | slechter dan | **A** | Nieuwe code mag geen nieuwe code smells / technische schuld toevoegen. |
| Reliability Rating | slechter dan | **A** | Nieuwe code mag geen nieuwe bugs introduceren. |
| Security Rating | slechter dan | **A** | Nieuwe code mag geen nieuwe kwetsbaarheden introduceren (vgl. de path traversal / LFI uit de gap-analyse, [`security/01-gap-analyse.md`](../security/01-gap-analyse.md)). |
| Security Hotspots Reviewed | is minder dan | **100%** | Elke nieuwe security hotspot moet expliciet getrieerd zijn voordat hij wordt gemerged. |

De gate beoordeelt bewust géén condities op de volledige (Overall) code, conform het Clean as You Code-principe uit sectie 3.

## 5. Gedrag bij een falende gate

Omdat de gate niet-blokkerend is, ziet een gate-overtreding er zo uit:

- de SonarCloud Quality Gate gaat op **Failed** met de falende condities;
- die status verschijnt op het projectdashboard en als PR-decoratie/check;
- de GitHub Actions-run "SonarCloud" blijft **groen**, omdat de scanner niet op de gate wacht;
- het team beoordeelt de gemelde overtreding tijdens de **PR-review** en besluit of die wordt opgelost of gemotiveerd geaccepteerd.

De gate is dus een reviewsignaal, geen technische poort. Wil je de gate wél hard laten blokkeren, voeg dan `"-Dsonar.qualitygate.wait=true"` toe aan het scancommando in sectie 2; de run wordt dan rood bij een **Failed** gate.

> Live voorbeeld: op `main` staat de gate op **Failed** wegens New Code Reliability Rating (C); de CI-run bleef groen omdat de gate niet-blokkerend is — zie `bewijs/quality-gate/sonar-gate-status.png`.

## 6. SonarCloud-UI: gate instellen

1. Ga naar `https://sonarcloud.io/organizations/damianen/quality_gates`.
2. Klik **Create** en maak een gate **"OpenMRS Attachments Gate"** (of kopieer "Sonar way" als basis).
3. Voeg met **Add Condition** -> *On New Code* de zes condities uit sectie 4 toe.
4. Open op de gate-pagina het tabblad **Projects** en koppel het project `Damianen_openmrs-attatchments` aan deze gate.
5. Trigger een analyse: GitHub -> **Actions** -> workflow **SonarCloud** -> **Run workflow** (of push een commit). De analyse loopt en SonarCloud toont de gate-status op het dashboard en de PR; de run zelf blijft groen.
6. Maak screenshots:
   - de gate-configuratie met alle condities -> `bewijs/quality-gate/sonar-gate-conditions.png`;
   - het SonarCloud-projecthome met de gate-status (**Passed** of **Failed** met condities) -> `bewijs/quality-gate/sonar-gate-status.png`;
   - de PR-decoratie/check met de gate-status -> `bewijs/quality-gate/sonar-pr-decoration.png`;
   - de groene GitHub Actions-run "SonarCloud" -> `bewijs/quality-gate/ci-sonarcloud-run.png`.

## 7. Bewijs

Alle screenshots horen in `bewijs/quality-gate/` (zie de checklist in [`bewijs/quality-gate/README.md`](bewijs/quality-gate/README.md)):

| Bestand | Toont |
|---|---|
| `sonar-gate-conditions.png` | De gate-condities in SonarCloud (sectie 4). |
| `sonar-gate-status.png` | SonarCloud projecthome met de gate-status (Passed/Failed). |
| `sonar-pr-decoration.png` | De gate-status als PR-decoratie/check, het reviewsignaal. |
| `ci-sonarcloud-run.png` | Groene GitHub Actions-run: de analyse draait, zonder de build te breken. |

## 8. Conclusie

De SonarCloud Quality Gate is ingericht als **meet- en rapportagecontrole**: hij hanteert onderbouwde New Code-condities die passen bij een legacy OpenMRS-module (Clean as You Code), met een coverage-drempel van 70% op nieuwe code die voortbouwt op de 60%-startnorm uit Sprint 3. De gate-status is bij elke PR en op het dashboard zichtbaar en wordt via PR-review beoordeeld, maar breekt de CI-build **bewust niet** — consistent met het Sprint 3-besluit om kwaliteit te meten, rapporteren en reviewen. Hard afdwingen kan later terug door `sonar.qualitygate.wait=true` weer toe te voegen aan het scancommando.
