# Bewijs: SonarCloud Quality Gate (meten en rapporteren)

Screenshots horend bij [`../../02-quality-gate.md`](../../02-quality-gate.md). De UI-stappen om deze
te maken staan in sectie 6 van dat document. Sla de afbeeldingen hier op met onderstaande namen.

De gate is **niet-blokkerend**: de status (Passed/Failed) is zichtbaar op het dashboard en de PR,
maar breekt de CI-build niet (zie sectie 2 van het document).

## Checklist

- [ ] `sonar-gate-conditions.png` — SonarCloud gate-configuratie met alle condities (sectie 4).
- [ ] `sonar-gate-status.png` — SonarCloud projecthome met de gate-status (**Passed** of
      **Failed** met de condities).
- [ ] `sonar-pr-decoration.png` — de gate-status als PR-decoratie/check (het reviewsignaal).
- [ ] `ci-sonarcloud-run.png` — GitHub Actions, workflow **SonarCloud**: de **groene** run
      (de analyse draait zonder de build te breken).
