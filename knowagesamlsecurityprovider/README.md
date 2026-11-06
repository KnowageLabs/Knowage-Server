# SAML 2.0: utenti Knowage locali o profilazione PM-API

Guide operative: [preparazione per il gestore IdP](GUIDA-PREPARAZIONE-IDP.md)
e [installazione applicativa essenziale](GUIDA-INSTALLAZIONE-APPLICATIVA.md).

## Stato della consegna

Implementazione per il backend 9.2.0-SNAPSHOT, Java 17 e `javax.servlet`, sul commit
`f2737e2fa68b1b0e7801a8f21cc9a6e00c5ada43`. Il frontend coordinato si trova nella
repository separata
`C:\Users\emacerilli\Documents\knowage\knowage-fe\Knowage-Server-Frontend`.
Le modifiche iniziali al frontend annidato `knowage-vue\src\vue` sono state
annullate; quel worktree e pulito.

**Non considerare l'integrazione collaudata o pronta per la produzione:** sono
stati eseguiti test frontend di correlazione/logout/login e controlli sintattici;
build Java, JUnit, SQL sui database di destinazione, regressioni degli engine e
collaudo federato reale restano da eseguire. Nessun comando Maven/Java e nessun
container Java sono stati avviati sulla macchina di sviluppo.

La funzionalita e disabilitata per impostazione predefinita. Non sono stati
modificati account, dati di produzione o configurazioni esterne. Le modifiche
preesistenti al plugin JAX-WS in `knowage-core\pom.xml` sono state conservate.

## Fonti e scelte

La specifica SAML e il *Manuale di integrazione MyID*, intestazioni/storico v1.6,
28/05/2026, §§9.1-9.6 e 9.8-9.11. La copertina conserva l'indicazione 1.5.
Fonti pubbliche:

- [Manuale MyID](https://myextranet.regione.veneto.it/myportal/MYEXT/api/download/myid_manuale_integrazione)
- [Metadata collaudo](https://myid.collaudo.regione.veneto.it/gw/metadata)
- [Attivazione MyID3](https://myextranet.regione.veneto.it/myportal/MYEXT/api/download/processo_attivazione_myid3)
- [Attivazione servizi regionali](https://myextranet.regione.veneto.it/myportal/MYEXT/api/download/attivazione_myid_servizi_rve)
- [Prodotto](https://myextranet.regione.veneto.it/prodotti-info/myid) e [FAQ](https://myid.regione.veneto.it/wayf/faq)

Il manuale di profilazione ricevuto e *012-01 PM-API - Manuale Programmatore API
REST per 012 - Profile Manager*, v1.2, 06/02/2023:
§§10.1/10.3 per le credenziali di servizio, §10.3.1 per identita/blocco account,
§10.3.9 per il profilo e le restrizioni delle funzioni, §10.3.16 per ruoli,
organizzazioni e validita, §13 per OAuth2/VAM.
Il documento riservato e i suoi payload non sono inclusi nella repository.

Rispetto all'opzione pac4j-SAML, e stato scelto l'equivalente
`com.onelogin:java-saml-core:2.9.0`: il core non dipende da un'integrazione servlet
o da un nuovo framework di autenticazione. Parsing, schema, firme e richieste
restano delegati al toolkit. Le API impiegate sono state confrontate con i
sorgenti del tag 2.9.0; non e stata verificata qui la risoluzione/compilazione
delle dipendenze sul runtime Java 17.

MyID usa RSA-SHA1/SHA1 come richiesto dal §9.2; `provider=standard` usa SHA-256
e rifiuta gli algoritmi deprecati. Non viene indebolita globalmente la sicurezza
XML della JVM. L'adattamento MyID aggiunge Scoping/RequesterID e consente le
asserzioni di mapping non consumate dentro Advice, mantenendo una sola
asserzione principale firmata. Il logout HTTP-POST usa schema e validazione
XML della firma del toolkit, poiche il suo validatore logout ordinario verifica
la firma del binding Redirect. Sono pubblicati soltanto i binding implementati.
L'uso professionale non e stato richiesto e non viene aggiunto.

## Mapping e politica di autorizzazione

La profilazione e indipendente da `provider=standard` / `provider=myid`.
Selezionare `profiling.mode=local` per utenti Knowage esistenti, oppure
`profiling.mode=pm` per PM-API. Se la proprieta manca, resta `pm` per
compatibilita; valori diversi o vuoti sono rifiutati. Un errore non provoca
mai un ripiego automatico da una modalita all'altra.

### Utenti Knowage e ruoli locali

L'esempio `standard.properties.example` non richiede PM:

```properties
provider=standard
profiling.mode=local
identity.attribute=username
identity.prefix=
```

Il singolo valore dell'attributo SAML, dopo l'eventuale rimozione del prefisso,
deve coincidere **esattamente, anche nelle maiuscole/minuscole**, con lo
`user_id` di un utente gia presente in Knowage. Il tenant viene ricavato
dall'account locale: lo stesso ambiente SAML puo autenticare utenti di tenant
diversi, ciascuno con i propri ruoli. Non si configura `local.tenant` e non
si accetta un tenant dall'IdP o dal browser. Non vengono usati
NameID, email o altri attributi come ripiego; non vengono creati utenti.
L'IdP deve garantire che quel valore identifichi soltanto il titolare
dell'account: non deve essere un attributo liberamente modificabile dall'utente.

Knowage legge dal catalogo locale nome, attributi, ruoli e flag superadmin
gia assegnati. I ruoli nell'asserzione SAML sono ignorati. Utenti inesistenti,
bloccati (`flgPwdBlocked=true`), senza tenant, senza ruoli assegnati o con
ruoli di un altro tenant rispetto all'account vengono rifiutati. Viene rispettato il ruolo
predefinito locale, che deve appartenere ai ruoli assegnati.
Password e MFA sono responsabilita dell'IdP; non si verifica la password
Knowage ne si applica la sua scadenza.

Si applicano i normali controlli su ruoli/funzionalita/licenza, senza
`pm_functions` e senza chiamate PM/VAM. Gli attributi locali con prefisso
riservato `kn_` o con nomi di claim JWT (`user_id`, `iss`, `sub`, `aud`,
`exp`, `nbf`, `iat`, `jti`) non vengono esportati nel token.
I profili locali sono acquisiti al login: modifiche alle assegnazioni di
ruoli/attributi o al blocco dell'account richiedono un nuovo login o la revoca
della sessione esistente. La durata resta limitata da `session.seconds` e
dall'IdP, senza il limite di mezzanotte specifico di PM.
Registro condiviso, scambio monouso, revoca e logout SAML rimangono obbligatori.

### Profilazione esterna PM-API

Le regole seguenti riguardano soltanto `profiling.mode=pm`. Gli esempi MyID
mantengono esplicitamente questa modalita e i suoi controlli.

| Origine | Campo | Destinazione Knowage | Obbligatorio | Assenza/errore |
| --- | --- | --- | --- | --- |
| SAML verificato | identificativo stabile configurato con `identity.attribute` | ricerca PM e attributo `pm_identity` | si | login negato se assente/ambiguo |
| SAML verificato | issuer, NameID e qualificatori, SessionIndex, AuthnInstant | registro SAML e SLO, non ID applicativo | contesto di protocollo | validazione SAML; nessun provisioning da NameID |
| PM profilo §10.3.9 | `profili[].account` | `user_id`, `pm_account` | si | zero/piu profili: login negato |
| PM utente §10.3.1 | account, identificativo configurato con `pm.identity-field`, `flgBloccoUtente` | verifica identita applicativa | si | blocco o incongruenza: login negato |
| PM utente | nome/cognome | nome visualizzato | no | ripiego sul solo account PM |
| PM profilo | email | attributo `email` | no | omesso |
| PM autorizzazione §10.3.16 | `autorizzato`, account, procedura | abilitazione alla procedura | si | login negato |
| PM autorizzazione | `codiceRuolo` e date | ruoli interni tramite `pm.role-map.*` | si | grant scaduti/futuri ignorati; mapping assente negato |
| PM autorizzazione | `idOrganizzazione` | tenant dedicato e `pm_organization` | mapping si | ambiguita o tenant inesistente: negato |
| PM profilo | funzioni, `tipoAccesso`, organizzazioni | `pm_functions`, intersezione con permessi ruolo/licenza | si | mapping assente/incompatibile: negato |

L'identificativo non e necessariamente un codice fiscale. Configurare insieme:

```properties
identity.attribute=SubjectId
identity.prefix=
pm.identity-parameter=subjectId
pm.identity-field=externalId
```

In questo esempio il valore dell'attributo SAML `SubjectId` viene inviato come
`subjectId` nelle richieste di profilo e autorizzazione PM. Il campo `externalId`
dell'unico elemento `utenti[]` deve contenere lo stesso valore; l'account
applicativo continua a provenire da `profili[].account`. La ricerca utente usa
ancora `account` e `tipoUtente`. Gli altri campi e mapping PM restano invariati.
Il nome del parametro e quello del campo JSON sono indipendenti dal nome SAML.
`identity.prefix`, se presente all'inizio del valore, viene rimosso prima della
ricerca e del confronto; un identificativo vuoto dopo la normalizzazione e negato.

Questi nomi sono un esempio, **non nuovi parametri garantiti dal PM-API v1.2**:
il servizio PM deve realmente supportare la chiave concordata e restituirla nel
profilo utente. Non basta rinominare un parametro per convertire un'API che
accetta soltanto codici fiscali. Non vengono indovinati attributi alternativi,
usati NameID transient come account, o disabilitati i controlli di identita.

Per compatibilita, in assenza delle nuove chiavi vengono ancora letti
`identity.fiscal-number-attribute` e `identity.fiscal-number-prefix`;
`pm.identity-parameter` e `pm.identity-field` hanno entrambi default
`codiceFiscale`. Le nuove chiavi prevalgono sulle rispettive chiavi legacy;
impostare esplicitamente `identity.prefix=` per annullare un prefisso legacy.
Il profilo contiene sempre `pm_identity`; mantiene anche `codiceFiscale`
soltanto quando il campo di identita PM configurato e `codiceFiscale`.

Sono **scelte implementative conservative**, non prescrizioni universali del
manuale: un solo account/profilo/procedura per login; un'unica organizzazione
attiva; organizzazioni diverse devono avere tenant distinti; tutti i grant attivi
devono avere mapping espliciti; nessun ruolo di ripiego o importazione account.
Le date sono inclusive, nel calendario Europe/Rome. `idOrganizzazione=null`
richiede il mapping esplicito `pm.tenant-map.*`, non un tenant implicito.
L'assenza della data finale non viene indovinata: il contratto ricevuto usa null.
Un array organizzazioni vuoto per una funzione e ammesso soltanto per un grant
senza restrizione `*`; strutture mancanti o non mappate vengono rifiutate.

`pm.function-map.<codice>` e `pm.access-type-map.<tipoAccesso>` contengono nomi
di funzionalita Knowage, separabili con virgole senza spazi. La selezione e
l'intersezione tra funzione, tipo di accesso, organizzazione e permessi interni;
cambiare ruolo di sessione non elimina questa restrizione. Utilizzare ruoli
dedicati con privilegio minimo, non un ruolo amministrativo di ripiego.

**Vincoli sui dati:** il mapping organizzazione/tenant deve corrispondere a uno
scope dati realmente isolato. Il connettore non riscrive query, dataset o
modelli QBE. Se una sorgente contiene piu organizzazioni, i modelli/dataset devono
filtrare tramite `pm_organization` o un isolamento equivalente verificato.
Non abilitare l'integrazione finche questo vincolo non e stato collaudato:
la sola presenza dell'attributo non implementa automaticamente un filtro di riga.

PM/VAM viene consultato a ogni login: nessuna cache positiva di autorizzazioni
esterne e nessuna assegnazione privilegiata in caso di indisponibilita.
La sessione e limitata dalla durata configurata, dalla scadenza SAML di sessione
quando presente e dalla prossima mezzanotte Europe/Rome. Una modifica PM
infragiornaliera non viene interrogata a ogni operazione: richiede nuovo login o
revoca amministrativa della sessione.

Le richieste PM richiedono Bearer VAM e `X-PM-User`/`X-PM-PW`.
Timeout di connessione 5 secondi, richiesta 10 secondi, nessun redirect HTTP,
risposte limitate a 1 MiB. HTTP non-200, JSON incoerente, errori di autenticazione
e servizi indisponibili negano il login; non vengono registrati i payload.
Le URL GET reali **devono provenire dallo Swagger sottoscritto**: il manuale non
le fornisce. Non sono state inventate.

## Preparazione e deploy

1. Copiare fuori dai WAR uno degli esempi in `src\main\resources\saml`:
   `myid-collaudo.properties.example`, `myid-production.properties.example` o
   `standard.properties.example` (utenti locali, senza PM). Sostituire ogni
   `REPLACE_` e valorizzare tutte le variabili `env:` richieste dalla modalita
   scelta; i placeholder/segreti mancanti causano un rifiuto.
   `authn.contexts` del profilo MyID e un esempio di livello 1: scegliere il
   livello richiesto dal servizio secondo §9.1, non assumere che sia SPID livello 2.
2. Installare certificato e chiave RSA nel keystore PKCS12 di deployment,
   separando collaudo/produzione. Configurare file, alias e password tramite
   ambiente o secret manager; limitare permessi e backup. I PEM in
   `src\test\resources` sono materiale sintetico pubblico, **mai utilizzabile
   come chiavi di deployment**.
3. Importare il metadata IdP tramite il canale fidato e verificarne entity ID
   e certificati con il gestore regionale. Si carica un file locale, non un
   metadata scelto dal browser. Sono ammessi solo certificati signing dello
   `IDPSSODescriptor`, non quelli dello SP gateway. Pianificare rotazione:
   mantenere temporaneamente le chiavi signing vecchia/nuova nel metadata
   verificato e aggiornare il file atomicamente su tutti i nodi.
4. Applicare **una sola volta**, con il processo di migrazione del deployment,
   lo schema adatto al database. Sono forniti PostgreSQL, MySQL/InnoDB, Oracle
   e HSQLDB; non sono state eseguite qui le migrazioni. La tabella condivisa e
   `SBI_SAML_STATE`, con payload JSON, scadenze epoch-millisecondi, hash del token,
   stato attivo e indici di ricerca/pulizia. Usare un database persistente anche
   per HSQLDB: il database in memoria esiste soltanto nei test.
5. Esporre un DataSource JNDI verso **lo stesso database/schema scrivibile** su
   ogni nodo, WAR/engine e consumer Spring Boot. Esempio nome
   `java:comp/env/jdbc/SamlState`; impostare `KNOWAGE_SAML_DATASOURCE` con quel
   nome, oppure `-Dknowage.saml.datasource`. Pool con auto-commit predefinito
   true, transazioni READ_COMMITTED, supporto `SELECT FOR UPDATE`; non usare
   read replica o un database diverso su un engine. I consumer Spring esistenti
   usano gia JNDI: rendere disponibile anche questa risorsa nello stesso contesto.
6. Impostare `KNOWAGE_SAML_ENABLED=true` e `KNOWAGE_SAML_CONFIG` sul file esterno,
   oppure le proprieta `knowage.saml.enabled` e `knowage.saml.config`.
   Conservare la normale configurazione JWT/HMAC comune ai consumer Knowage.
   Non e necessario sostituire globalmente tutti i supplier di autenticazione.
7. Ricostruire gli artefatti CE condivisi e tutti i WAR/consumer interessati,
   quindi gli artefatti EE che li consumano. Non distribuire solamente il nuovo
   provider o solamente il WAR principale: servono anche common/utils aggiornati.
   Nessuna modifica diretta EE e stata necessaria nei sorgenti disponibili.
   Verificare la presenza di `META-INF\web-fragment.xml` nel JAR utils e del
   relativo filtro in **ogni** WAR a runtime. Gli attuali descrittori sono Servlet
   4.0 e non escludono il fragment; eventuali overlay/absolute-ordering o filtri
   di scansione del container devono preservarlo.
8. Distribuire `Knowage-Server-Frontend` e pubblicare l'ingresso
   `/knowage-vue/login`, adattando il contesto ai valori Vite effettivi.
   `/knowage-vue/saml-login` e un alias della stessa pagina `Login.vue`,
   compatibile con gli esempi delle proprieta. Quando SAML e abilitato,
   la pagina avvia automaticamente il flusso federato senza pulsante e senza
   mostrare il form locale; quando e disabilitato, i flussi locali/OIDC rimangono
   invariati. Le callback di login/logout/errore non avviano un nuovo flusso
   automatico; per riprovare si usa un ingresso pulito alla pagina di login.

Tutte le URL pubbliche e di servizio devono essere HTTPS. Dietro proxy mantenere
ACS, SLO e URL frontend esattamente uguali ai valori pubblici registrati; non
ricostruirli da header arbitrari. Frontend e backend devono essere same-origin.
La correlazione non dipende da cookie cross-site: lo stato condiviso lega
RelayState/InResponseTo a un challenge del browser, e il codice nel fragment
viene scambiato con il verificatore in sessionStorage entro 60 secondi.
La sessione HTTP viene creata/completata solo allo scambio e il suo ID ruotato.

Preservare i cookie Secure/HttpOnly e collaudare SameSite nel deployment;
non disabilitare globalmente CSRF. Sono esentati soltanto gli esatti POST ACS,
SLO e scambio codice, protetti rispettivamente da firme/correlazione e prova
monouso del browser. I token applicativi rimangono in sessionStorage come nel
frontend esistente; le asserzioni SAML non vengono propagate agli engine.

## Attivazione regionale e logout

Pubblicare metadata a `/knowage/restful-services/saml/metadata`, ACS HTTP-POST
a `/knowage/restful-services/saml/acs` e SLO HTTP-POST a
`/knowage/restful-services/saml/slo`. Non dichiarare SOAP/Artifact non implementati.

Il metadata pubblico di collaudo non contiene l'endpoint SLO: ottenere dal
gestore `idp.slo-url` e `idp.slo-response-url` e concordare il binding POST.
Registrare entity ID, metadata, URL servizio, certificato, contesti, Circle of
Trust e **URL finale post-logout** secondo il processo MyID3 applicabile
(procedura specifica per servizi regionali, se pertinente).
Il logout MyID avviato da Knowage usa `/logout/target?spid=<entityID codificato>`;
`spurl` non viene inviato. Il redirect finale va registrato presso MyID verso
la pagina SAML, preferibilmente con `#samlLoggedOut=true`.

Il logout locale revoca prima il token e invalida la sessione principale.
Il POST remoto firmato funziona senza cookie/frontend, identifica issuer,
NameID normalizzato e indici di sessione e risponde con un messaggio firmato.
Notifiche identiche duplicate entro la finestra di validita sono idempotenti; ID riutilizzati con messaggi
diversi sono respinti. Anche un'autenticazione in corso precedente al logout
viene bloccata mediante tombstone condivise.

Le credenziali sono vincolate al proprio hash nel registro; ogni controllo
federato verifica stato/scadenza senza cache positiva locale. Un engine gia
aperto viene respinto e la sua sessione HTTP invalidata **alla prossima
richiesta** dal filtro condiviso, non fisicamente distrutto a distanza all'istante.
Operazioni gia partite non vengono annullate retroattivamente.

Monitorare database, sincronizzazione NTP, errori generici e dimensione dello
stato. `cleanup()` viene eseguita all'avvio del login; programmare inoltre la
pulizia dei soli record con `EXPIRES_AT <=` ora epoch-millisecondi se il traffico
e discontinuo. Non rimuovere record attivi/tombstone non scaduti.
Proteggere il registro: contiene NameID e token temporanei dello scambio,
non asserzioni; limitare accesso, backup e retention. Un guasto del registro
nega l'accesso federato. Disabilitare il flag non rende validi token revocati.
Per una revoca di emergenza modificare lo stato delle sole SESSION individuate,
con la procedura amministrativa autorizzata, mantenendo il registro disponibile.

Il logger `com.onelogin.saml2` e disattivato nel log4j2 principale. Mantenere
questa impostazione negli overlay, non loggare body SAML, Authorization,
verificatori, codici o credenziali PM nei proxy. I percorsi JWT/profilo comuni
redigono il token federato nei messaggi di debug.

## Moduli e modifiche coordinate

| Modulo/file | Motivo |
| --- | --- |
| provider e reactor CE/core POM | toolkit, protocollo e adattatore PM/VAM |
| `SamlResource`, `LoginConfigResource` | endpoint pubblici e contratto frontend |
| `LoginCompletionService`, `LoginResource` | riuso di storage profilo/privacy, audit e rotazione sessione federata |
| `LogoutResource` | revoca prima dell'invalidazione e avvio MyID/POST standard |
| utils: registro, guard, fragment/filter, JWT service e variante unique ID | stato cross-node, blocco richieste e verifica ticket/credenziali |
| `GenericJWTSsoService` | non riemettere un token federato perdendo il vincolo |
| DAO `UserUtilities`, supplier interno, interceptor REST | tenant/ruoli espliciti, restrizioni PM anche nel cambio ruolo, revoca prima della cache |
| common verifier, Spring `ContextPropertiesConfig`, API verifica JWT e BIRT | controllo del registro dopo le verifiche JWT nei consumer diretti |
| CSRF e log4j2 | eccezioni puntuali e nessuna asserzione nei log |
| frontend separato: `Login.vue`, `useLoginConfig.ts`, helper/test, authState, route, `App.vue`, `App.store.js`, `authHelper.ts` | riuso del login esistente, alias pubblico, scambio browser-bound, logout/revoca e attesa della pulizia cache al cambio identita |

## Verifiche eseguite e prove ancora necessarie

I test del frontend corretto usano Vitest e le dipendenze gia presenti in
`Knowage-Server-Frontend`. Il tooling isolato nella directory `files\tools`
della sessione viene usato per parser, Prettier e lint equivalente, senza
modificare manifest o lockfile del progetto.

```powershell
Set-Location "C:\Users\emacerilli\Documents\knowage\knowage-fe\Knowage-Server-Frontend"
node .\node_modules\vitest\vitest.mjs run src\helpers\commons\samlAuth.spec.ts src\helpers\commons\authHelper.spec.ts src\views\login\Login.saml.spec.ts src\composables\__tests__\useLoginConfig.spec.ts "--testNamePattern=SAML|coordinated logout" --maxWorkers=1
node .\node_modules\vite\bin\vite.js build --base=/knowage-vue "--outDir=<output-temporaneo-sessione>"
node "<tools>\validate-saml.mjs"
node "<tools>\node_modules\prettier\bin\prettier.cjs" --check <file-frontend-mirate>
node "<tools>\node_modules\prettier\bin\prettier.cjs" --write <file-frontend-mirate>
node "<tools>\node_modules\eslint\bin\eslint.js" --config "<tools>\eslint-saml.config.mjs" <file-frontend-mirate> --quiet
git --no-pager diff --check
```

Vitest mirato: **25 test superati** nel frontend corretto, su normalizzazione
della configurazione, challenge SHA-256 senza esposizione del verifier,
login/scambio same-origin, codice monouso, fallimenti chiusi, callback sui due
ingressi e riuso di `completeLogin(token, false)`. I test della pagina verificano
eliminazione di token/ruolo precedenti, pulizia cache prima del nuovo login e
assenza di redirect automatici dopo errori/logout. I test di logout verificano
POST standard, destinazione MyID anche dopo ripristino del token in una nuova
tab, soppressione dei 401 concorrenti, revoca locale e preservazione di
invalidazione engine, OIDC `id_token_hint` e logout legacy.

La suite di autenticazione piu ampia segnala cinque errori preesistenti,
riprodotti anche sui sorgenti/test originali esportati da HEAD
(45 passati, gli stessi cinque falliti), senza sostituire il worktree.
Non sono corretti nell'ambito di questa integrazione: quattro test `Image` usano mock
arrow non costruibili con Vitest 4; un test di `useAuthFlows` si aspetta
`query.logout` come destinazione, mentre il codice esistente usa `query.redirect`
o `/`. Il relativo codice di autenticazione e i test interessati non sono
stati modificati. Gli altri 90 test della prima esecuzione sono passati.
Il config ESLint originale non si carica perche manca
`@vue/typescript/recommended`. La baseline non viene modificata per nascondere
questi limiti. Prettier e applicato agli helper e ai nuovi test.
Il lint isolato equivalente passa per helper, configurazione, nuovi test e
store; sui contenitori `App.vue`, `App.routes.js` e `Login.vue` rileva sei
variabili/import inutilizzati gia presenti, fuori dalle modifiche.
Il controllo TypeScript mirato di `samlAuth.ts` e `authState.ts` passa.
Build Vite di produzione completata con exit code 0, inclusa generazione PWA
(`generateSW`, 547 voci precache). Il primo tentativo reale era bloccato dalla
dipendenza `fflate` dichiarata nel manifest ma assente in node_modules: e stata
ripristinata soltanto `fflate@0.8.3`, senza modificare manifest o lockfile.
Output prodotto in una sottodirectory temporanea della sessione, poi rimosso;
nessun artefatto di deploy esistente e stato sovrascritto.
Rimangono warning preesistenti su `beforeUnmount` duplicato, uso di eval e
dimensione dei chunk, non corretti nell'ambito del supporto SAML.

Il precedente controllo Java/XML sui sorgenti backend non aveva rilevato
errori sintattici; non costituisce compilazione Java o esecuzione JUnit.

Sono stati aggiunti test JUnit su:

- autorizzazioni temporali, identita/procedura, mapping/restrizioni PM e limite
  del body HTTP prima del buffering;
- risoluzione PM completa con identificativo alternativo senza codice fiscale,
  compatibilita delle chiavi legacy, encoding e rifiuto di identita mancanti,
  ambigue, vuote dopo la normalizzazione o non corrispondenti;
- selezione locale/PM con default compatibile, identita locale senza PM,
  caricamento di utenti/ruoli/attributi/privilegi locali, rifiuto di utenti
  mancanti/bloccati o senza tenant, risoluzione di account in tenant diversi,
  rifiuto di ruoli di un altro tenant e protezione dei claim JWT riservati;
- distinzione dei token locali dai token PM preesistenti e rifiuto di modalita
  di profilazione sconosciute;
- stato JDBC monouso concorrente tra nodi, revoca e tombstone per SessionIndex;
- richieste RSA-SHA1/MyID, Advice, firme/issuer/audience/recipient, scadenza,
  correlazione/replay, logout senza cookie e percorso standard SHA-256;
- token gia verificato dopo revoca, hash differente sullo stesso SID,
  guasto del registro e preservazione delle credenziali legacy.

**Questi test JUnit non sono stati eseguiti.** Su una macchina di build
autorizzata con Java 17/Maven, eseguire i test mirati del provider, DAO e utils
includendo le dipendenze reactor e disabilitando lo skip dei test del parent, quindi build
CE/EE e regressioni core/engine. Verificare separatamente tutti gli script SQL
sul database reale e il filter mapping effettivo del container.

Prima del rilascio servono tenant e isolamento dati, keystore e metadata fidati,
attivazione SSO/SLO presso l'IdP. In modalita locale predisporre utenti/ruoli
e il contratto dell'attributo identificativo. Solo per PM servono anche URL
Swagger sottoscritte, chiavi VAM, credenziali PM, procedura/tipo utente/mapping
reali; con MyID confermare Circle of Trust e redirect regionale.
Collaudare login ammesso/negato, errori della profilazione scelta, fixation/cookie/proxy, cambio ruolo,
logout locale/remoto senza cookie, replay, guasto DB, token gia in cache e
sessione aperta su **ogni** engine/consumer, oltre alle autenticazioni legacy.
Provare separatamente un IdP standard diverso da MyID.

In collaudo MyID e disponibile il test dell'IdP MyID, **non quello degli IdP
SPID**. Nessun esito MyID, SPID, CIE o IdP standard reale viene dichiarato qui.
