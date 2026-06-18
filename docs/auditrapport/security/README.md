# Security auditdocumentatie

Deze map bevat de security-documentatie en het bijbehorende bewijs voor de audit.

## Documenten

| Bestand | Inhoud |
|---|---|
| `01-gap-analyse.md` | Eerste security gaps en codevoorbeelden |
| `02-pipeline-compliance.md` | CI/CD, GitHub securitymaatregelen en pipeline-status |
| `03-assets-risicocriteria.md` | Crown jewels, risicoscore en risicobereidheid |
| `03-testdatabeleid.md` | Testdata-afspraken |
| `04-threat-model.md` | STRIDE threat model en risicomatrix |
| `05-security-backlog.md` | Security backlog en mitigaties |
| `06-sca-sbom-triage.md` | Dependency- en SBOM/SCA-triage |
| `07-risicomatrix-bow-tie.md` | Risicomatrix en bow-tie uit Sprint 2 |
| `08-attack-surface-overview.md` | Attack surface mapping voor Sprint 3 |
| `09-logging-gap-analyse.md` | Logging gap analyse voor NEN-7510 A.8.15 |
| `10-coverage-quality-gate.md` | Coverage, JaCoCo en CI artifact voor Sprint 3 |
| `false-positive-beleid.md` | False-positivebeleid en register |
| `risk-assessment-report.md` | Overkoepelend risk assessment report |

## Bewijs

| Map | Inhoud |
|---|---|
| `bewijs/repository-access/` | GitHub Environments, production rules, branch ruleset, PR-reviewbewijs en MFA |
| `bewijs/scanning/` | CodeQL, code scanning, PR securitychecks, GitHub security settings en JaCoCo coverage-overzichten |
| `bewijs/sbom-sca/` | Dependabot, SBOM, Snyk en SCA-screenshots |
