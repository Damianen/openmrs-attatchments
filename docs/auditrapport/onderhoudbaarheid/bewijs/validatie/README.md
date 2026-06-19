# Bewijs: validatie van de A3-refactor

Screenshots horend bij [`../../validatie.md`](../../validatie.md). De URL's en wat er per
opname op moet staan, staan in sectie 7 van dat document.

Project: organisatie `damianen`, key `Damianen_openmrs-attatchments`.
De cijfers zijn al in `validatie.md` ingevuld op basis van de SonarCloud-oogst
(ná PR #33 op `main`, commit `2931cf42`); deze screenshots zijn het visuele bewijs.

## Checklist

- [x] `sonar-na-main-summary.png` — Summary `main`: Quality Gate **Passed**, Security **A**,
      0 open issues.
- [x] `sonar-na-hotspots-leeg.png` — Security Hotspots `main`: **100% reviewed**, 0 "to review".
- [x] `sonar-na-issues-security-0.png` — Issues `main` gefilterd op Security: **0** open
      security issues.
- [x] `sonar-pr33-summary.png` — PR #33 Summary: Quality Gate **Passed**, 0 New Issues,
      0 New Hotspots.
- [ ] `sonar-voor-activity.png` *(optioneel/historisch)* — Activity-datapunt vóór commit
      `99486699`. **Let op:** momenteel niet beschikbaar (Activity-pagina geeft een fout);
      vermeld als meetlacune.
