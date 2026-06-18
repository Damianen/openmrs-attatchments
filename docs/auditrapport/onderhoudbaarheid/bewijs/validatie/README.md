# Bewijs: validatie van de A3-refactor

Screenshots horend bij [`../../validatie.md`](../../validatie.md). De instructies om ze te
maken (URL's, wat erop moet staan) staan in sectie 7 van dat document. Maak telkens een
**vóór-A3**- en een **na-A3**-opname.

Project: organisatie `damianen`, key `Damianen_openmrs-attatchments`.

## Checklist

- [ ] `sonar-hotspot-before.png` — Security Hotspots vóór A3: de hardcoded-credentials-hotspot
      op `AttachmentsActivator.java` (de `AKIA-…`-regel), status open/To Review.
- [ ] `sonar-hotspot-after.png` — Security Hotspots ná A3: hotspot weg (0 open) of Fixed.
- [ ] `sonar-overview-after.png` — Projecthome ná A3 met **Quality Gate Passed** en de
      measure-tegels (ratings, Hotspots Reviewed 100%, Coverage, Duplications).
- [ ] `sonar-overview-before.png` *(optioneel)* — dezelfde tegels op de pre-A3-analyse.
- [ ] `sonar-activity-hotspots.png` *(optioneel)* — activiteitengrafiek met dalend
      aantal Security Hotspots/Vulnerabilities rond de A3-analyse.

Noteer bij elke opname ook de getallen en vul daarmee de tabellen in sectie 4 van
`validatie.md` in.
