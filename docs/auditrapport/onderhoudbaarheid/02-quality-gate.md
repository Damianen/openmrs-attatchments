# Quality gate die CI laat falen - Sprint 4

## 1. Doel

In Sprint 3 (zie [`security/10-coverage-quality-gate.md`](../security/10-coverage-quality-gate.md)) is besloten om coverage en kwaliteit te **meten en te reviewen**, maar nog niet hard af te dwingen. In de A1-stap is daarna een SonarCloud-analyse toegevoegd via `.github/workflows/sonar.yml`. Die analyse *meet* wel, maar liet de build groen, ook als de SonarCloud Quality Gate faalde.

Dit document beschrijft de A2-stap: de SonarCloud Quality Gate **blokkeert nu de CI-build**. Een overtreding van de gate levert een rode (niet-nul) workflow-run op. Daarmee voldoet de pipeline aan de eis "CI faalt bij non-compliance".

## 2. Hoe CI blokkeert

De SonarCloud-scan draait in `.github/workflows/sonar.yml`. Het scancommando is uitgebreid met één parameter:

```
mvn -B "-Dformatter.skip=true" "-Dsonar.qualitygate.wait=true" \
    org.sonarsource.scanner.maven:sonar-maven-plugin:sonar
```

`sonar.qualitygate.wait=true` zorgt ervoor dat de scanner na het uploaden wacht op het resultaat van de Quality Gate in SonarCloud:

- gate **Passed** -> scanner eindigt met exitcode 0 -> stap groen;
- gate **Failed** -> scanner eindigt met een niet-nul exitcode -> stap "SonarCloud scan" faalt -> de hele workflow-run is rood.

Er is bewust gekozen voor deze ingebouwde flag in plaats van een extra GitHub Action (`sonarqube-quality-gate-action`): het is één regel, vereist geen extra secret of `report-task.txt`-koppeling, en het faalgedrag zit op dezelfde plek als de scan zelf.

## 3. Uitgangspunt: Clean as You Code

De module is legacy/OpenMRS-code met sterke frameworkafhankelijkheden (Spring REST, DataDelegatingCrudResource, obs-integratie). Het in één keer wegwerken van alle bestaande technische schuld is binnen deze sprint niet realistisch, en een harde grens op de *volledige* codebase zou elke PR blokkeren om redenen die niets met de wijziging te maken hebben.

Daarom volgen we het SonarCloud-principe **Clean as You Code**: de Quality Gate beoordeelt vooral **nieuwe en gewijzigde code** (New Code), niet de hele legacy-baseline. Nieuwe code wordt aan een strengere lat gehouden dan de bestaande 60%-startnorm uit Sprint 3. Zo neemt de schuld niet verder toe en verbetert de kwaliteit geleidelijk met elke PR.

## 4. Afgedwongen condities (passende gate)

De volgende condities worden afgedwongen op **New Code**. Faalt één conditie, dan faalt de gate en dus de CI-build.

| Conditie (New Code) | Operator | Drempel | Onderbouwing |
|---|---|---|---|
| Coverage | is minder dan | **70%** | Hoger dan de 60% legacy-startnorm uit Sprint 3 (baseline: api 74%, omod-securitysubset 66%), maar lager dan de Sonar-default van 80%. Nieuwe code in deze module bevat nog onvermijdelijke OpenMRS framework-glue (resource-registratie, delegating CRUD) die via integratietests wordt geraakt en niet altijd zuiver unit-testbaar is. |
| Duplicated Lines (%) | is meer dan | **3%** | Sonar-default; op nieuwe code goed haalbaar en houdt copy-paste tegen. |
| Maintainability Rating | slechter dan | **A** | Nieuwe code mag geen nieuwe code smells / technische schuld toevoegen. |
| Reliability Rating | slechter dan | **A** | Nieuwe code mag geen nieuwe bugs introduceren. |
| Security Rating | slechter dan | **A** | Nieuwe code mag geen nieuwe kwetsbaarheden introduceren (vgl. de path traversal / LFI uit de gap-analyse, [`security/01-gap-analyse.md`](../security/01-gap-analyse.md)). |
| Security Hotspots Reviewed | is minder dan | **100%** | Elke nieuwe security hotspot moet expliciet getrieerd zijn voordat hij wordt gemerged. |

Deze gate beoordeeld bewust géén condities op de volledige (Overall) code, conform het Clean as You Code-principe uit sectie 3.

## 5. Bewijs van een blokkerende gate (RED-run)

Om aan te tonen dat de gate de CI *echt* laat falen, is tijdelijk één extra conditie op **Overall Code** toegevoegd:

> Overall Code -> Coverage is minder dan **80%**

De totale coverage van de legacy-module ligt rond ~70% (zie baseline in Sprint 3), dus deze conditie faalt gegarandeerd. De analyse-run werd hierdoor rood:

- de SonarCloud Quality Gate ging op **Failed**;
- het Maven-scancommando eindigde met een niet-nul exitcode;
- de GitHub Actions-run "SonarCloud" werd rood.

Dat is het bewijs dat een gate-overtreding de pipeline blokkeert. Na het maken van de screenshots is deze tijdelijke Overall-conditie **verwijderd**, zodat de definitieve gate weer de onderbouwde New Code-condities uit sectie 4 hanteert en de run groen wordt.

## 6. SonarCloud-UI: gate instellen (RED daarna GREEN)

### (a) Forceer een rode run voor het bewijs

1. Ga naar `https://sonarcloud.io/organizations/damianen/quality_gates`.
2. Klik **Create** en maak een gate **"OpenMRS Attachments Gate"** (of kopieer "Sonar way" als basis).
3. Voeg met **Add Condition** -> *On New Code* de zes condities uit sectie 4 toe.
4. Voeg met **Add Condition** -> *On Overall Code* toe: metric **Coverage**, operator *is less than*, waarde **80**. Sla op.
5. Open op de gate-pagina het tabblad **Projects** en koppel het project `Damianen_openmrs-attatchments` aan deze gate.
6. Trigger een analyse: GitHub -> **Actions** -> workflow **SonarCloud** -> **Run workflow** op branch `ci/sonarcloud-quality-gate` (of push een commit). De stap "SonarCloud scan" faalt omdat de gate op ERROR staat.
7. Maak screenshots:
   - SonarCloud projecthome met de melding **"Quality Gate Failed"** en de falende Coverage-conditie -> `bewijs/quality-gate/sonar-gate-failed.png`;
   - de gate-configuratie met alle condities -> `bewijs/quality-gate/sonar-gate-conditions.png`;
   - de rode GitHub Actions-run "SonarCloud" -> `bewijs/quality-gate/ci-sonarcloud-run-failed.png`.

### (b) Versoepel naar de onderbouwde, passende gate

1. Open de gate opnieuw en **verwijder** de conditie *Overall Code -> Coverage < 80%*. De zes New Code-condities blijven staan.
2. Re-run de SonarCloud-workflow. De gate slaagt nu (deze PR voegt geen analyseerbare nieuwe code toe) en de run wordt groen.
3. Maak screenshots:
   - SonarCloud projecthome met **"Quality Gate Passed"** -> `bewijs/quality-gate/sonar-gate-passed.png`;
   - de groene GitHub Actions-run "SonarCloud" -> `bewijs/quality-gate/ci-sonarcloud-run-passed.png`.

## 7. Bewijs

Alle screenshots horen in `bewijs/quality-gate/` (zie de checklist in [`bewijs/quality-gate/README.md`](bewijs/quality-gate/README.md)):

| Bestand | Toont |
|---|---|
| `sonar-gate-conditions.png` | De gate-condities in SonarCloud. |
| `sonar-gate-failed.png` | Quality Gate **Failed** met falende conditie (RED-bewijs). |
| `ci-sonarcloud-run-failed.png` | Rode GitHub Actions-run: CI blokkeert op de gate. |
| `sonar-gate-passed.png` | Quality Gate **Passed** na versoepeling. |
| `ci-sonarcloud-run-passed.png` | Groene GitHub Actions-run met de definitieve gate. |

## 8. Conclusie

Met `sonar.qualitygate.wait=true` blokkeert de SonarCloud Quality Gate nu de CI-build: een overtreding levert een rode workflow-run op. De gate hanteert onderbouwde New Code-condities die passen bij een legacy OpenMRS-module (Clean as You Code), met een coverage-drempel van 70% op nieuwe code die voortbouwt op de 60%-startnorm uit Sprint 3. Het rode-run-bewijs toont aan dat de gate daadwerkelijk faalt op non-compliance.
