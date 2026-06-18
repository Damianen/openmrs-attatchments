# Bewijs: SonarCloud Quality Gate blokkeert CI

Screenshots horend bij [`../../02-quality-gate.md`](../../02-quality-gate.md). De UI-stappen om deze
te maken staan in sectie 6 van dat document. Sla de afbeeldingen hier op met onderstaande namen.

## Checklist

- [ ] `sonar-gate-conditions.png` — SonarCloud gate-configuratie met alle condities (sectie 4).
- [ ] `sonar-gate-failed.png` — SonarCloud projecthome met **Quality Gate Failed** en de falende
      Overall-Coverage-conditie (RED-bewijs, sectie 5).
- [ ] `ci-sonarcloud-run-failed.png` — GitHub Actions, workflow **SonarCloud**: de **rode** run.
      Bewijst dat CI blokkeert bij een gate-overtreding.
- [ ] `sonar-gate-passed.png` — SonarCloud projecthome met **Quality Gate Passed** nadat de
      tijdelijke Overall-conditie is verwijderd.
- [ ] `ci-sonarcloud-run-passed.png` — GitHub Actions, workflow **SonarCloud**: de **groene** run
      met de definitieve gate.

Maak eerst de RED-screenshots, versoepel daarna de gate en maak de GREEN-screenshots.
