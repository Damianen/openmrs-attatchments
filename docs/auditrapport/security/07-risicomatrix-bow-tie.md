# Risicomatrix en bow-tie - OpenMRS Attachments Module

## 1. Doel

Dit document legt de risicomatrix en bow-tie analyse vast voor Sprint 2. De originele PDF wordt apart ingeleverd, maar de inhoud wordt hier ook in Markdown bewaard zodat het auditrapport navolgbaar blijft binnen de repository.

Bronbestand:

- `docs/auditrapport/07-risicomatrix_bow-tie.pdf`

Pentest-informatie is bewust niet opgenomen in dit document. De pentest wordt apart uitgewerkt en is nog niet het uitgangspunt voor deze Markdown-versie.

## 2. Koppeling met Sprint 2

Sprint 2 vraagt om:

- analyse van het project op CIA/BIV;
- risicocriteria en risicobereidheid;
- threat model en gevonden threats;
- risicomatrix met gevonden risico's;
- bow-tie analyse voor de hoogste risico's;
- risico-evaluatie van het CI/CD proces met risicomatrix;
- bow-tie voor het meest kritieke CI/CD-risico;
- koppeling naar maatregelen, SAST, SCA, SBOM en security backlog.

Dit document hoort daarom bij:

- `03-assets-risicocriteria.md`
- `04-threat-model.md`
- `05-security-backlog.md`
- `06-sca-sbom-triage.md`
- `risk-assessment-report.md`

## 3. Risicocriteria

De risicomatrix gebruikt kans en impact als assen. De beoordeling sluit aan op de eerder vastgelegde risicocriteria:

| Kans | Betekenis |
|---|---|
| Zeer onwaarschijnlijk | Alleen mogelijk onder uitzonderlijke omstandigheden |
| Onwaarschijnlijk | Niet waarschijnlijk, maar technisch mogelijk |
| Mogelijk | Realistisch scenario bij misconfiguratie of kwetsbare code |
| Waarschijnlijk | Kan naar verwachting voorkomen bij normaal misbruik |
| Zeer waarschijnlijk | Eenvoudig of vaak te misbruiken |

| Impact | Betekenis |
|---|---|
| Onbelangrijk | Nauwelijks security- of compliance-impact |
| Minder ernstig | Beperkte verstoring of beperkte datablootstelling |
| Serieus | Duidelijke impact op vertrouwelijkheid, integriteit of beschikbaarheid |
| Zeer serieus | Grote impact op patientdata, systeemintegriteit of compliance |
| Catastrofaal | Kritiek datalek, server compromise of grote zorgcontinuiteitsimpact |

Voor patientdata is de risicobereidheid laag. Risico's met directe impact op vertrouwelijkheid van medische bestanden worden niet zonder mitigatie geaccepteerd.

## 4. Applicatie-risicomatrix

De PDF bevat de volgende applicatierisico's:

| Risico | Kans | Impact | Niveau | Waarom belangrijk |
|---|---|---|---|---|
| Hardcoded AWS credentials lek | Zeer waarschijnlijk | Catastrofaal | Hoog | Uitgelekte cloud credentials kunnen leiden tot cloud takeover of toegang tot gevoelige configuratie. |
| Path traversal | Waarschijnlijk | Catastrofaal | Hoog | Een aanvaller kan mogelijk bestanden buiten de bedoelde attachment-map lezen. |
| Datalek door fout logging | Mogelijk | Serieus | Verhoogd | Attachment-fetch logging is aangescherpt; verdere logreview blijft nuttig. |
| Blootstelling van originele bestandsnamen | Waarschijnlijk | Serieus | Verhoogd | Bestandsnamen kunnen medische of persoonlijke informatie bevatten. |
| Geen audit-logging | Zeer waarschijnlijk | Minder ernstig | Verhoogd | Zonder auditspoor is achteraf niet goed vast te stellen wie welke actie heeft uitgevoerd. |

De hoogste risico's zijn `Hardcoded AWS credentials lek` en `Path traversal`, omdat beide een catastrofale impact kunnen hebben. Voor de bow-tie is gekozen voor het centrale event `Datalek / ongeautoriseerde toegang`, omdat dit de gezamenlijke uitkomst is van meerdere hoge risico's.

## 5. Applicatie bow-tie

Centraal event:

| Onderdeel | Invulling |
|---|---|
| Centraal event | Datalek / ongeautoriseerde toegang |
| Asset | Patientattachments, patientmetadata, configuratie en secrets |
| Belangrijkste BIV-impact | Vertrouwelijkheid, daarna integriteit en traceerbaarheid |

### Bedreigingen

| Bedreiging | Uitleg |
|---|---|
| Hardcoded AWS credentials lek | Secrets in de repository of configuratie kunnen misbruikt worden. |
| Path traversal | Een onveilig pad kan leiden tot uitlezen buiten de bedoelde opslagmap. |
| Datalek door fout logging | Logs kunnen meer patientinformatie bevatten dan noodzakelijk. |
| Blootstelling van originele bestandsnamen | Bestandsnamen kunnen gevoelige informatie lekken. |
| Geen audit-logging | Misbruik of datatoegang is achteraf minder goed te reconstrueren. |

### Preventieve maatregelen

| Maatregel | Koppeling |
|---|---|
| Secrets apart opslaan in `.env` of GitHub Environments | Voorkomt secrets in git en ondersteunt least privilege. |
| Inputvalidatie en paden afdwingen | Mitigeert path traversal en ongecontroleerde file access. |
| Gevoelige informatie niet loggen | Beperkt PII/PHI in applicatielogs. |
| Bestandsnamen hernoemen | Voorkomt dat originele namen medische context prijsgeven. |
| Auditlogging afdwingen | Maakt kritieke acties traceerbaar. |

### Correctieve maatregelen

| Maatregel | Doel |
|---|---|
| Key revoken | Uitgelekte credentials ongeldig maken. |
| WAF blokkade | Verdachte requests tijdelijk blokkeren. |
| Incident response plan | Reageren op datalek of misbruik volgens vaste stappen. |
| Transport encryptie | Netwerkinterceptie beperken. |
| Alternatieve logging wanneer primaire logging uitvalt | Traceerbaarheid behouden bij storing. |

### Consequenties

| Consequentie | Impact |
|---|---|
| Cloud takeover | Aanvaller krijgt controle over cloudresources. |
| Server compromise | Integriteit en beschikbaarheid van het systeem komen in gevaar. |
| Patientgegevens gestolen | Directe AVG/NEN-7510-impact door datalek. |
| Netwerkintercepties | Gevoelige data kan onderweg worden onderschept. |
| Geen traceerbaarheid | Onderzoek na incident wordt moeilijker. |

## 6. CI/CD-risicomatrix

Sprint 2 vraagt ook om een risico-evaluatie van het ontwikkelproces. De PDF bevat hiervoor een aparte CI/CD-risicomatrix.

| CI/CD-risico | Kans | Impact | Niveau | Waarom belangrijk |
|---|---|---|---|---|
| Supply chain attack | Mogelijk | Catastrofaal | Hoog | Een kwetsbare of malafide dependency kan tijdens build of runtime schade veroorzaken. |
| Secrets lek in CI-logs | Mogelijk | Zeer serieus | Verhoogd | Secrets kunnen zichtbaar worden in workflow-output of artifacts. |
| Ongeautoriseerde code in main | Onwaarschijnlijk | Catastrofaal | Verhoogd | Zonder branch protection kan kwaadaardige of ongeteste code in de hoofdbranch komen. |
| Compromise van externe GitHub Action | Onwaarschijnlijk | Zeer serieus | Verhoogd | Een externe action kan buildstappen manipuleren of secrets proberen te lezen. |

Het belangrijkste CI/CD-risico is `Supply chain attack`, omdat de module Maven dependencies gebruikt en kwetsbare dependencies direct effect kunnen hebben op de betrouwbaarheid van de release.

## 7. CI/CD bow-tie

Centraal event:

| Onderdeel | Invulling |
|---|---|
| Centraal event | Kwetsbare of malafide dependency bereikt succesvol de build of runtime |
| Asset | Build pipeline, artifact, dependency tree en patientdata in runtime |
| Belangrijkste BIV-impact | Integriteit en beschikbaarheid, met mogelijke vertrouwelijkheidsimpact |

### Bedreigingen

| Bedreiging | Uitleg |
|---|---|
| Account van open-source ontwikkelaar wordt overgenomen | Een bestaande Maven dependency kan kwaadaardige code bevatten na account takeover. |
| Typosquatting | Een dependency met bijna dezelfde naam wordt per ongeluk toegevoegd. |
| Bekende CVE niet gepatcht | Een kwetsbare versie blijft in gebruik terwijl er een patch beschikbaar is. |

### Preventieve maatregelen

| Maatregel | Koppeling |
|---|---|
| Harde versies in `pom.xml` en geen dynamische ranges | Builds blijven reproduceerbaar en voorspelbaar. |
| Code reviews | Dependency-wijzigingen worden door een tweede persoon bekeken. |
| Dependabot alerts | Bekende kwetsbaarheden worden automatisch zichtbaar. |
| CodeQL/SAST | Kwetsbare codepatronen worden vroeg gevonden. |
| SCA/SBOM | Dependencies en kwetsbaarheden worden aantoonbaar bijgehouden. |

### Correctieve maatregelen

| Maatregel | Doel |
|---|---|
| SBOM gebruiken | Snel bepalen welke kwetsbare componenten in de software zitten. |
| OTAP-isolatie | Schade beperken als een kwetsbaarheid in een lagere omgeving geraakt wordt. |
| Automated rollback | Terug naar een vorige veilige versie bij incident of foutieve release. |

### Consequenties

| Consequentie | Impact |
|---|---|
| Datalek | Kwetsbare dependency kan toegang geven tot patientdata. |
| Remote code execution | Aanvaller kan code uitvoeren via kwetsbare dependency. |
| Ransomware/downtime | Beschikbaarheid van OpenMRS kan geraakt worden. |

## 8. Koppeling met NEN-7510:2024-2

| Control | Relevantie voor deze analyse |
|---|---|
| A.8.3 Toegangsbeveiliging | Nodig voor download-autorisatie, branch protection en gecontroleerde toegang. |
| A.8.5 Authenticatie/autorisatie | Nodig voor GitHub MFA, production approvals en gebruikersrechten. |
| A.8.8 Kwetsbaarheidsbeheer | Nodig voor Dependabot, SCA, CVE-triage en opvolging van kwetsbare dependencies. |
| A.8.15 Logging | Nodig voor auditlogging zonder onnodige patientdata. |
| A.8.25 Secure SDLC | Nodig om security structureel in CI/CD en releases op te nemen. |
| A.8.28 Secure coding | Nodig voor path traversal, logging, uploadvalidatie en dependencygebruik. |
| A.8.29 Security testing | Nodig voor SAST, SCA, SBOM en security regressietests. |

## 9. Aandachtspunten voor de PDF

De PDF is bruikbaar als visueel bewijs, maar bij een volgende versie kunnen deze punten worden verbeterd:

| Punt | Verbetering |
|---|---|
| Tekst centraal event applicatie | Gebruik `Datalek / ongeautoriseerde toegang` als nette formulering. |
| Transportmaatregel | Gebruik `Transport encryptie` in plaats van een onduidelijke formulering. |
| CI/CD centraal event | Maak de zin volledig: `Kwetsbare of malafide dependency bereikt succesvol de build of runtime`. |
| Preventieve maatregel CI/CD | Zorg dat de tekst over harde versies en geen dynamische ranges niet wordt afgekapt. |

## 10. Conclusie

De risicomatrix laat zien dat de hoogste applicatierisico's zitten in secrets, path traversal, logging en ontbrekende auditbaarheid. De bow-tie maakt duidelijk welke preventieve en correctieve maatregelen nodig zijn om datalekken en ongeautoriseerde toegang te voorkomen of te beperken.

Voor CI/CD ligt het grootste risico bij supply chain attacks. Daarom zijn Dependabot, SCA, SBOM, CodeQL, branch protection en review-afspraken belangrijke maatregelen. Deze analyse vormt de brug tussen het threat model, de security backlog en het risk assessment report.
