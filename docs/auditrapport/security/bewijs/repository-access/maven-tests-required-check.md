# B2 — Maven Tests als verplichte (required) status check op `main`

Dit document beschrijft hoe de `Maven Tests`-workflow als **verplichte status check**
op de `main`-branch wordt afgedwongen, plus de verificatie en het bijbehorende bewijs.
Hiermee wordt het OTAP-onderdeel verdedigd: ongeteste of ongereviewde code kan niet
naar `main` mergen.

## Exacte check-namen

De workflow `.github/workflows/maven-tests.yml` (naam **`Maven Tests`**) heeft twee jobs.
De namen zoals getoond in de PR-checks — en dus de contexts die als required check
geselecteerd worden — zijn:

- **`api-tests`**
- **`omod-security-tests`**

Beide draaien al groen op `main` (app `github-actions`), dus ze zijn selecteerbaar.
We maken **beide** verplicht, zodat de volledige testworkflow de merge gatet.

## Mechanisme

`main` wordt beschermd door de actieve ruleset **`protect-main`** (id `17167317`).
Die dwingt nu al af: geen verwijderen, verplichte PR-review (1) en geen force-push,
maar **nog geen status checks**. De juiste, schone aanpak is daarom de **bestaande
ruleset uitbreiden** met een `required_status_checks`-regel (in plaats van een
losse, parallelle classic branch protection aan te maken).

## Aanbevolen — bestaande ruleset `protect-main` uitbreiden

De repository-owner draait dit commando (na `gh auth login`). Het behoudt de
bestaande regels en voegt de twee verplichte checks toe:

```bash
gh api --method PUT repos/Damianen/openmrs-attatchments/rulesets/17167317 --input - <<'JSON'
{
  "name": "protect-main",
  "target": "branch",
  "enforcement": "active",
  "conditions": { "ref_name": { "include": ["~DEFAULT_BRANCH"], "exclude": [] } },
  "bypass_actors": [],
  "rules": [
    { "type": "deletion" },
    { "type": "non_fast_forward" },
    { "type": "pull_request", "parameters": {
        "required_approving_review_count": 1,
        "dismiss_stale_reviews_on_push": true,
        "required_reviewers": [],
        "require_code_owner_review": false,
        "require_last_push_approval": false,
        "required_review_thread_resolution": false,
        "allowed_merge_methods": ["merge", "squash", "rebase"] } },
    { "type": "required_status_checks", "parameters": {
        "strict_required_status_checks_policy": false,
        "do_not_enforce_on_create": false,
        "required_status_checks": [
          { "context": "api-tests" },
          { "context": "omod-security-tests" } ] } }
  ]
}
JSON
```

Optionele hardening: voeg per check `"integration_id": 15368` toe om de check vast te
pinnen aan de GitHub Actions-app (voorkomt dat een andere app een check met dezelfde
naam kan posten).

Controleer daarna dat de regel actief is:

```bash
gh api repos/Damianen/openmrs-attatchments/rulesets/17167317 \
  --jq '.rules[] | select(.type=="required_status_checks")'
```

## Alternatief — classic branch protection

> Let op: dit maakt een **apart** beschermingsmechanisme náást de ruleset. Alleen
> kiezen als je bewust van ruleset naar classic branch protection wilt overstappen.

```bash
gh api --method PUT repos/Damianen/openmrs-attatchments/branches/main/protection --input - <<'JSON'
{
  "required_status_checks": { "strict": false,
    "checks": [ { "context": "api-tests" }, { "context": "omod-security-tests" } ] },
  "enforce_admins": false,
  "required_pull_request_reviews": { "required_approving_review_count": 1 },
  "restrictions": null
}
JSON
```

## Handmatige fallback (via de UI)

Settings → Rules → Rulesets → **`protect-main`** (of Settings → Branches → Branch
protection rule voor `main`) → **Require status checks to pass** aanvinken → in het
zoekveld **`api-tests`** en **`omod-security-tests`** selecteren → opslaan.

De checks zijn pas selecteerbaar nadat ze minstens één keer (groen) hebben gedraaid;
dat is in deze repository al gebeurd.

## Verificatie — wegwerp-PR die niet kan mergen tot de check groen is

1. Maak een wegwerp-branch met een onschuldige wijziging en open een PR:
   ```bash
   git checkout -b chore/verify-required-check main
   # kleine no-op wijziging, bv. een spatie in een markdownbestand
   git commit -am "chore: verifieer required check (wegwerp)"
   git push -u origin chore/verify-required-check
   gh pr create --fill
   ```
2. Terwijl `api-tests` / `omod-security-tests` nog draaien, toont de merge-box
   **"Required statuses must pass before merging"** en is de merge-knop **uitgeschakeld**.
3. Zodra beide checks groen zijn, wordt de merge-knop actief — bewijs dat de check
   de merge echt gatet.
4. Sluit de PR en verwijder de wegwerp-branch.

## Screenshots (bewijs)

Plaats in deze map (`docs/auditrapport/security/bewijs/repository-access/`):

- `maven-tests-required-check-ruleset.png` — de ruleset/settings-pagina met
  `api-tests` en `omod-security-tests` aangevinkt als required status checks.
- `maven-tests-required-check-blocks-merge.png` — de merge-box van de wegwerp-PR met
  "Required statuses must pass before merging", merge-knop uitgeschakeld, beide checks zichtbaar.
- (optioneel) `maven-tests-required-check-mergeable.png` — dezelfde PR nadat de checks
  groen zijn en de merge-knop actief is.
