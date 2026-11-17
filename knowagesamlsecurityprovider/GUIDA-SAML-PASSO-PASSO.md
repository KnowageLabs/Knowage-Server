# Configurare SAML su Knowage, partendo da zero

Questa guida parte da un ambiente Knowage gia installato con backend,
frontend, API ed engine che includono l'integrazione SAML dei commit
`57e36b21` e `1a028736`. Non spiega come compilare o aggiornare Knowage.
La sola presenza del JAR del provider non basta.

Lo scopo e arrivare a questo risultato: l'utente apre Knowage, si autentica
sul sistema di identita dell'ente e torna in Knowage con i permessi corretti.
Devono funzionare anche il logout e la revoca della sessione sugli engine.

**La presenza della versione corretta non certifica il funzionamento
dell'installazione: eseguire prima il percorso completo in collaudo.**
Gli esempi sono istruzioni da adattare, non operazioni gia eseguite o
collaudate sul proprio ambiente.

## Come leggere la guida

Eseguire le fasi nell'ordine indicato. Dove e scritto di attendere una
conferma, non sostituirla con un valore inventato. Non eseguire alla cieca
i comandi su produzione.

Gli esempi usano Windows e PowerShell. Le directory, il dominio e i nomi
dei servizi sono esempi: non descrivono necessariamente il proprio server.
Le URL contengono `/`; i percorsi Windows contengono `\`.

Le guide di riferimento sono la
[preparazione IdP](GUIDA-PREPARAZIONE-IDP.md), la guida di
[installazione essenziale](GUIDA-INSTALLAZIONE-APPLICATIVA.md) e il
[README tecnico](README.md).

## 0. Capire quali persone e sistemi servono

| Termine o referente | Significato pratico |
| --- | --- |
| IdP, Identity Provider | Sistema che controlla l'identita dell'utente, per esempio MyID. Gestisce password ed eventuale secondo fattore |
| SP, Service Provider | In questa integrazione e Knowage: riceve e verifica il risultato dell'autenticazione |
| SAML | Protocollo con cui IdP e Knowage si scambiano messaggi firmati sull'autenticazione |
| Gestore IdP | Persona o gruppo a cui chiedere la registrazione di Knowage, i parametri e gli utenti di prova |
| Sistemista Knowage | Persona che configura server, file, servizio applicativo e riavvii |
| Amministratore Knowage | Persona che gestisce utenti, tenant, ruoli e permessi applicativi |
| DBA | Persona autorizzata a modificare il database |
| Referente rete | Persona che gestisce DNS, VPN, proxy, firewall e HTTPS |
| Gestore PM/VAM | Serve soltanto se si sceglie la profilazione esterna PM |
| Tenant | Organizzazione/ambito applicativo a cui appartengono un account e i suoi ruoli |
| Ruolo | Insieme di permessi assegnato a un utente, per esempio per consultare determinati documenti |
| Frontend | Parte dell'applicazione mostrata nel browser, qui nel contesto `/knowage-vue` |
| Backend e API | Servizi sul server chiamati dal browser e dagli altri componenti |
| Engine | Componente Knowage che esegue o visualizza un certo tipo di documento |
| XML e JSON | Formati di testo strutturato: i metadata SAML sono XML, `loginconfig` restituisce JSON |
| Token JWT | Credenziale temporanea rilasciata da Knowage dopo il login; chi la possiede puo usarla fino a scadenza o revoca |

Una stessa persona puo coprire piu ruoli, ma servono comunque le relative
autorizzazioni. Non e necessario amministrare personalmente l'IdP.

## 1. Scegliere ambiente e modalita, prima di configurare

**Chi:** responsabile applicativo, amministratore Knowage e sistemista.

1. Scegliere un ambiente di collaudo, non la produzione.
2. Individuare il gestore dell'IdP e aprire una richiesta di integrazione.
3. Decidere da dove Knowage deve ricavare i permessi:

| Scelta | Configurazione | Cosa bisogna preparare |
| --- | --- | --- |
| Utenti e ruoli gia gestiti in Knowage | `profiling.mode=local` | Account Knowage esistenti, con tenant e ruoli assegnati |
| Profili e autorizzazioni gestiti da PM | `profiling.mode=pm` | Accesso a PM/VAM, utenti abilitati e mapping dei permessi |

`local` non significa autenticazione con password locale: l'autenticazione
resta sull'IdP. Significa soltanto che i permessi sono letti da Knowage.
Non vengono creati automaticamente account locali dopo il login.

4. Stabilire se l'IdP e MyID oppure un IdP SAML standard:
   usare rispettivamente `provider=myid` o `provider=standard`.

Le due decisioni sono indipendenti: si puo usare MyID con utenti Knowage
locali, senza PM. Non cambiare provider per eliminare PM.
Se `profiling.mode` manca, il codice assume `pm`: impostarlo esplicitamente.

**Risultato da ottenere:** ambiente, provider e modalita di profilazione
decisi e annotati nella richiesta di attivazione.

## 2. Mettere al sicuro l'accesso amministrativo

**Chi:** sistemista e amministratore Knowage.

1. Concordare una finestra di manutenzione: l'abilitazione SAML cambia la
   pagina di ingresso e puo impedire il normale accesso se qualcosa e errato.
2. Verificare di avere accesso al server anche senza entrare nel sito Knowage.
3. Identificare i servizi/nodi da riavviare e chi puo farlo.
4. Salvare una copia della configurazione applicativa, delle opzioni di
   avvio, dei contesti Tomcat e della configurazione proxy.
5. Far predisporre un backup del database secondo le procedure dell'ambiente.
6. Documentare come ripristinare il metodo di accesso precedente. Non
   presumere che una sessione amministrativa aperta rimanga utilizzabile.

Non cambiare globalmente i supplier di autenticazione, le chiavi JWT/HMAC
o la configurazione di tutti gli utenti per aggiungere SAML.

**Risultato da ottenere:** possibilita concreta di tornare indietro dalla
console del server, senza dipendere da un login SAML funzionante.

## 3. Fissare gli indirizzi reali, anche se sono soltanto in VPN

**Chi:** sistemista e referente rete.

Nell'esempio il nome e `knowage-collaudo.ente.example`: e fittizio.
Sostituirlo con il nome DNS effettivamente utilizzato dai browser.
Verificare anche i contesti: qui si assumono `/knowage` e `/knowage-vue`.

| Uso | Esempio |
| --- | --- |
| Pagina da cui entra l'utente | `https://knowage-collaudo.ente.example/knowage-vue/saml-login` |
| Metadata, cioe descrizione XML di Knowage | `https://knowage-collaudo.ente.example/knowage/restful-services/saml/metadata` |
| ACS, destinazione del risultato del login | `https://knowage-collaudo.ente.example/knowage/restful-services/saml/acs` |
| SLO, destinazione dei messaggi di logout | `https://knowage-collaudo.ente.example/knowage/restful-services/saml/slo` |
| Ritorno finale dopo il logout MyID | `https://knowage-collaudo.ente.example/knowage-vue/saml-login#samlLoggedOut=true` |

Scegliere inoltre l'**Entity ID dello SP**: e l'identificatore di questa
installazione Knowage presso l'IdP. Per semplicita si puo usare l'URL dei
metadata. Lo stesso identico valore andra in `sp.entity-id`, nel metadata
generato e nella registrazione presso l'IdP. Non e una password e non e
necessariamente un indirizzo da visitare. Usare identificatori distinti
per collaudo e produzione.

1. Collegarsi alla VPN, se richiesta.
2. Aprire Knowage usando il nome HTTPS definitivo, non l'IP o `localhost`.
3. Verificare che il browser non segnali errori sul certificato HTTPS.
4. Aprire anche la pagina di accesso dell'IdP dalla stessa postazione.
5. Far verificare che frontend e backend abbiano la stessa origine:
   stesso schema `https`, stesso host e stessa porta.
6. Far verificare al referente rete che il proxy conservi POST e parametri
   SAML, che Tomcat riconosca HTTPS e che gli endpoint SAML non richiedano
   un ulteriore login a un portale/proxy.
7. Escludere la cache su login, ACS, exchange e SLO, senza disabilitare
   globalmente CSRF o le protezioni dei cookie.
8. Far verificare sincronizzazione dell'orologio su tutti i nodi.
9. Solo per PM: far verificare dal server Knowage DNS, accesso HTTPS e
   fiducia nelle CA dei servizi PM/VAM nel runtime applicativo.

**Non e obbligatorio pubblicare Knowage su Internet.** Nel flusso implementato
il browser trasporta i messaggi di login e logout: deve raggiungere sia
Knowage sia l'IdP. Se Knowage e in VPN, la connessione deve rimanere attiva.
Concordare separatamente se il gestore IdP deve accedere ai metadata via URL
oppure se accetta il file XML consegnato tramite il canale previsto.
La procedura di attivazione dell'IdP puo imporre ulteriori requisiti di rete.

`@PublicService` nel codice significa senza sessione Knowage gia autenticata,
non accessibile da Internet. Non modificare firewall o VPN sulla sola base
di questa annotazione.

**Risultato da ottenere:** elenco definitivo degli indirizzi e conferma del
percorso di rete; non e ancora necessario che i metadata SAML rispondano.

## 4. Inviare la prima richiesta al gestore IdP

**Chi:** referente applicativo, con il sistemista.

Si puo inviare questo testo, sostituendo le parti tra parentesi:

> Dobbiamo registrare Knowage come SP SAML nell'ambiente [collaudo/produzione].
> L'applicazione e raggiungibile [in VPN/sulla rete indicata].
> Proponiamo Entity ID [valore], ACS [URL] HTTP-POST e SLO [URL] HTTP-POST.
> L'ingresso utente e [URL]. Per MyID il ritorno post-logout e [URL completo].
> Il metadata SP finale sara generato da Knowage dopo la configurazione.
> Chiediamo procedura di attivazione, metadata IdP da canale fidato, Entity ID
> IdP, endpoint SSO HTTP-Redirect, endpoint SLO di richiesta e risposta
> HTTP-POST e, per MyID, URL /logout/target.
> Chiediamo inoltre contesto di autenticazione autorizzato, attributo
> identificativo da rilasciare, requisiti del certificato SP e della firma
> del metadata, modalita di consegna/aggiornamento dei metadata e utenti
> di collaudo. Confermare la compatibilita con i vincoli sotto indicati.

I vincoli di questa implementazione da comunicare sono:

- SAML 2.0, login avviato da Knowage; non login spontaneo dall'IdP.
- Richiesta di login firmata, SSO HTTP-Redirect, risposta ad ACS HTTP-POST.
- Response e Assertion firmate; restituzione di `RelayState` e `InResponseTo`.
- `NameID` transient e contesto di sessione per il logout.
- Logout SAML HTTP-POST; non SOAP o Artifact.
- `provider=myid` usa RSA-SHA1/SHA1, `provider=standard` usa SHA-256.

Non indebolire le policy crittografiche globali del server per aggirare un
rifiuto. Un'incompatibilita va risolta con i responsabili dell'integrazione.

### 4.1 Decidere il livello di autenticazione

MFA significa autenticazione a piu fattori, per esempio password piu
un codice temporaneo. **Per questa guida, salvo ulteriori indicazioni,
si assume che il servizio non richieda MFA.** Non aggiungere quindi
un obbligo di secondo fattore alla richiesta di attivazione.

Comunicare al gestore IdP: "Non richiediamo MFA come requisito applicativo,
salvo vostre policy o successive indicazioni del responsabile del servizio.
Indicate il contesto SAML da richiedere per questa configurazione".

L'assenza di un requisito applicativo non disabilita un eventuale secondo
fattore imposto dall'IdP: Knowage non decide come mostrare la sua pagina
di autenticazione. Se emergono requisiti diversi, farli confermare dal
responsabile del servizio prima dell'attivazione.

Anche senza MFA, il gestore IdP deve comunicare la stringa SAML esatta del
contesto di autenticazione, chiamata `AuthnContextClassRef`.

La stringa andra in `authn.contexts`. Piu valori si separano con virgole,
senza spazi. Knowage effettua un confronto esatto, non una graduatoria
automatica fra livelli. Non copiare il livello presente nell'esempio MyID
senza averne confermato l'idoneita. Non lasciare `authn.contexts` vuoto e
non eliminare il controllo: questa implementazione lo richiede anche
quando non viene richiesta MFA.

### 4.2 Decidere come riconoscere l'utente

Chiedere un attributo SAML stabile, univoco, con un solo valore e non
liberamente modificabile dal titolare. Farsi dare il suo `Name` esatto,
non soltanto il nome visualizzato in una schermata dell'IdP.

Esempio illustrativo: attributo `identificativoUtente`, valore `utente123`.
Il nome andra in `identity.attribute`; il valore serve a individuare l'utente.

Con `local`, `utente123` deve essere esattamente lo `user_id` Knowage,
anche nelle maiuscole/minuscole. Il nome visualizzato, l'email e il
`NameID` transient non sono alternative automatiche.

Con `pm`, il valore deve essere accettato nelle ricerche PM e deve poter
essere confrontato con il campo identificativo restituito da PM.
Serve quindi anche la conferma del gestore PM.

Non imporre il codice fiscale se non e richiesto dai sistemi coinvolti.
Se il valore ha un prefisso da eliminare, concordarlo e configurarlo in
`identity.prefix`; altrimenti lasciare `identity.prefix=` vuoto.
Non sono previste altre trasformazioni automatiche del valore.

**Risultato da ottenere:** risposte scritte ai punti precedenti. Attendere
i parametri reali prima di completare il file di configurazione.

## 5. Preparare gli utenti e i permessi

**Chi:** amministratore Knowage; per PM anche il relativo gestore.

### Percorso A: `profiling.mode=local`

1. Accedere come amministratore prima di abilitare SAML.
2. Individuare o creare tramite le funzioni amministrative Knowage un
   account di prova ordinario, senza privilegi superadmin.
3. Verificare lo `user_id` effettivo, non soltanto il nome completo.
4. Verificare che l'utente abbia un tenant e almeno un ruolo di quel tenant.
5. Se ha un ruolo predefinito, verificare che sia fra quelli assegnati.
6. Verificare che l'account non sia bloccato.
7. Chiedere al gestore IdP che l'utente di prova rilasci esattamente
   quell'identificativo nell'attributo concordato.
8. In un ambiente multi-tenant, predisporre un secondo utente di un altro
   tenant per verificare la separazione dei dati.

Non configurare `local.tenant`: il tenant viene ricavato dall'account.
I ruoli presenti nella risposta SAML non sostituiscono quelli locali.
Password e secondo fattore vengono verificati dall'IdP, non da Knowage.

### Percorso B: `profiling.mode=pm`

1. Ottenere dal gestore PM lo Swagger sottoscritto, cioe il contratto delle
   API effettivamente abilitate, con le URL GET reali per profilo, utente
   e autorizzazione. Non dedurle dal dominio MyID.
2. Ottenere URL token VAM, client ID, client secret, utente di servizio e
   password attraverso un canale sicuro.
3. Farsi confermare codice procedura e tipo utente.
4. Concordare `pm.identity-parameter`, nome del parametro di ricerca, e
   `pm.identity-field`, nome del campo identificativo nella risposta utente.
5. Far abilitare un utente di prova con identificativo uguale a quello IdP,
   un solo profilo/account/procedura compatibile e autorizzazioni attive.
6. Preparare con l'amministratore Knowage la corrispondenza fra codici PM
   e ruoli, organizzazioni/tenant, funzioni e tipi di accesso Knowage.
7. Verificare che ruoli e tenant di destinazione esistano realmente.
8. Verificare l'isolamento dei dati: la sola associazione di un'organizzazione
   a un tenant non aggiunge automaticamente filtri a query e dataset.

Le proprieta sono `pm.role-map.*`, `pm.tenant-map.*`,
`pm.function-map.*` e `pm.access-type-map.*`: la parte dopo il prefisso
e il codice PM reale; il valore e il nome interno Knowage concordato.
Non inserire un ruolo amministrativo come ripiego e non inventare mapping
`*`. Tutti i grant attivi devono essere compatibili con le regole del
[README](README.md#profilazione-esterna-pm-api).

**Risultato da ottenere:** almeno un utente con identita e autorizzazioni
coerenti fra i sistemi, senza dipendere da permessi assegnati a caso.

## 6. Creare le directory e predisporre i certificati

**Chi:** sistemista e, se presente, team certificati.

L'esempio usa `C:\knowage\conf\saml`, fuori dai WAR, cioe fuori dagli archivi
dell'applicazione che possono essere sostituiti durante gli aggiornamenti.
Prima verificare che sia un percorso appropriato sul server.

```powershell
New-Item -ItemType Directory -Path 'C:\knowage\conf\saml' -Force
```

In Esplora file abilitare la visualizzazione delle estensioni, per non
creare involontariamente file come `saml.properties.txt`.
Far impostare i permessi NTFS dalla finestra Proprieta > Sicurezza:
lettura all'account del servizio e accesso ai soli amministratori autorizzati.
Non dare accesso generico a tutti gli utenti del server.

Servono tre materiali diversi:

| Materiale | Provenienza | Destinazione |
| --- | --- | --- |
| Metadata IdP verificati | Gestore IdP, tramite canale fidato | File locale `idp-metadata.xml` |
| Chiave privata RSA e certificato SP | Sistemista/team certificati | Keystore `sp-signing.p12`, da proteggere |
| Certificato pubblico SP | Estratto dal materiale precedente | Gestore IdP e metadata SP |

Il certificato SP serve alle firme SAML, non sostituisce il certificato
HTTPS del sito. Knowage non genera autonomamente la chiave o il certificato.
Non usare i file di `src\test\resources`: sono materiale pubblico di test.

### 6.1 Generare chiave e richiesta di certificato, se non esistono gia

Usare una postazione amministrativa autorizzata e OpenSSL fornito dal team
sistemistico. Se non e disponibile, farlo predisporre secondo le procedure
dell'ente; non scaricare eseguibili da fonti casuali.

I comandi seguenti sono esempi per un ambiente nuovo. **Verificare che i file
di destinazione non esistano: non sovrascrivere una chiave gia in uso.**
3072 bit e 365 giorni, dove indicati, sono esempi da confermare con il gestore.

Generare una chiave privata RSA cifrata:

```powershell
openssl genpkey -algorithm RSA -aes-256-cbc -pkeyopt rsa_keygen_bits:3072 -out 'C:\knowage\conf\saml\sp-private-key.pem'
```

Quando richiesto, scegliere una password robusta e conservarla nel gestore
segreti autorizzato. Non scriverla nel comando, in email o in Git.

Generare una CSR, cioe una richiesta di emissione del certificato:

```powershell
openssl req -new -sha256 -key 'C:\knowage\conf\saml\sp-private-key.pem' -out 'C:\knowage\conf\saml\sp-signing.csr'
```

Il comando chiede la password della chiave e i dati del soggetto. Compilarli
secondo le indicazioni del team certificati e i requisiti dell'IdP.

Scegliere **una sola** delle due strade:

1. Se serve un certificato emesso da CA, inviare la CSR al team/CA
   autorizzata e ottenere il certificato X.509 in PEM, salvandolo come
   `C:\knowage\conf\saml\sp-signing-cert.pem`. La chiave privata non si invia.
2. Soltanto se il gestore IdP accetta esplicitamente l'autofirmato, produrlo
   dalla CSR con il comando seguente:

```powershell
openssl x509 -req -in 'C:\knowage\conf\saml\sp-signing.csr' -signkey 'C:\knowage\conf\saml\sp-private-key.pem' -sha256 -days 365 -out 'C:\knowage\conf\saml\sp-signing-cert.pem'
```

Se la CA consegna un formato diverso o richiede una catena di certificati,
farne gestire conversione e confezionamento al team certificati.

### 6.2 Creare il PKCS12 da caricare in Knowage

Il PKCS12 e un contenitore con chiave privata e certificato. Crearlo con
alias `knowage-saml`, da riportare poi nella configurazione:

```powershell
openssl pkcs12 -export -inkey 'C:\knowage\conf\saml\sp-private-key.pem' -in 'C:\knowage\conf\saml\sp-signing-cert.pem' -name 'knowage-saml' -out 'C:\knowage\conf\saml\sp-signing.p12'
```

Il comando chiede la password della chiave e una password di esportazione
per il PKCS12. Quest'ultima sara usata come password del keystore e della
chiave nella configurazione dell'esempio. Non confonderla con la password
del PEM, che puo essere diversa.

Controllare il certificato pubblico:

```powershell
openssl x509 -in 'C:\knowage\conf\saml\sp-signing-cert.pem' -noout -subject -issuer -dates -fingerprint -sha256
```

Controllare il contenitore senza esportare la chiave:

```powershell
openssl pkcs12 -in 'C:\knowage\conf\saml\sp-signing.p12' -info -noout
```

Verificare con il gestore IdP soggetto, validita, formato e impronta del
certificato tramite il canale concordato. L'impronta e un riepilogo del
certificato utile al confronto, non una chiave privata.

Consegnare soltanto `sp-signing-cert.pem` o il formato pubblico richiesto.
Non consegnare `.p12`, `sp-private-key.pem` o password. Non incollarne il
contenuto in ticket, chat o repository. Conservare gli originali soltanto
nel deposito autorizzato e proteggere anche le copie di backup.

### 6.3 Salvare i metadata ricevuti dall'IdP

Salvare il documento verificato come
`C:\knowage\conf\saml\idp-metadata.xml`. Farsi confermare Entity ID e
certificati di firma dal gestore. Non usare un XML casuale trovato online,
una pagina HTML salvata come XML o i metadata di un ambiente diverso.

**Risultato da ottenere:** PKCS12 SP, certificato pubblico SP e metadata
IdP verificati, con password/alias disponibili agli amministratori autorizzati.

## 7. Preparare il registro condiviso nel database

**Chi:** DBA e sistemista. Serve anche con utenti locali e con un solo nodo.

Knowage conserva nel registro richieste pendenti, sessioni e revoche.
Non e un archivio degli utenti e non va sostituito da file locali.

1. Individuare database e schema persistenti destinati al registro.
2. Far controllare se esiste gia `SBI_SAML_STATE`.
3. Se non esiste, far applicare al DBA una sola volta lo script adatto:

| Database | File nel modulo `knowagesamlsecurityprovider` |
| --- | --- |
| MySQL | `src\main\resources\saml\schema-mysql.sql` |
| PostgreSQL | `src\main\resources\saml\schema-postgresql.sql` |
| Oracle | `src\main\resources\saml\schema-oracle.sql` |
| HSQLDB persistente | `src\main\resources\saml\schema-hsqldb.sql` |

4. Se la tabella esiste, farne verificare struttura e indici, senza
   cancellarla o rilanciare alla cieca il `CREATE TABLE`.
5. Assegnare all'utenza applicativa SELECT, INSERT, UPDATE e DELETE sul
   registro, secondo le policy del database.
6. Far configurare una connessione JNDI denominata `jdbc/SamlState`.
   JNDI e il meccanismo con cui l'applicazione trova una connessione
   configurata dal server, invece di contenere le credenziali nel codice.

Su Tomcat, il sistemista puo aggiungere al `<Context>` esistente, per
esempio in `conf\Catalina\localhost\knowage.xml`, una risorsa come questa
**solo se il database e MySQL e il pool indicato e quello disponibile**:

```xml
<Resource name="jdbc/SamlState" auth="Container" type="javax.sql.DataSource"
    factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
    driverClassName="com.mysql.cj.jdbc.Driver"
    url="jdbc:mysql://DB_HOST:3306/KNOWAGE_DB"
    username="UTENTE_DB" password="SEGRETO_DB"
    defaultAutoCommit="true" defaultTransactionIsolation="2" />
```

Sostituire host, database e credenziali con quelli reali, gestendo i segreti
secondo lo standard del deployment e l'escaping XML se necessario.
Non creare un secondo `<Context>` e non sovrascrivere le risorse esistenti.
Per database diversi adattare driver e URL; il driver JDBC deve essere
disponibile al pool.

Configurare l'accesso allo **stesso database e schema scrivibile** in ogni
contesto/nodo/engine/API che consuma i token federati, non solo in `knowage`.
Anche i consumer Spring devono poter risolvere la risorsa JNDI.
Servono auto-commit predefinito `true`, isolamento READ_COMMITTED e supporto
`SELECT FOR UPDATE`; non usare una replica di sola lettura.

**Risultato da ottenere:** tabella e connessioni predisposte su tutti i
consumer. La semplice presenza della tabella non prova il funzionamento JNDI.

## 8. Preparare il file `saml.properties`

**Chi:** sistemista, con i valori confermati dai gestori.

1. Recuperare dalla distribuzione/sorgenti dell'integrazione un modello
   in `knowagesamlsecurityprovider\src\main\resources\saml`.
2. Per IdP standard copiare `standard.properties.example`; per MyID
   copiare `myid-collaudo.properties.example` o
   `myid-production.properties.example` dell'ambiente corretto.
3. Salvare la copia come `C:\knowage\conf\saml\saml.properties`.
4. Aprire il file con un editor di testo, non un elaboratore di documenti.
5. Non modificare soltanto il modello nella repository: Knowage leggera
   il file esterno indicato all'avvio.

### 8.1 Proprieta comuni

La tabella indica cosa inserire, non valori pronti per tutti gli ambienti.

| Proprieta | Cosa inserire |
| --- | --- |
| `provider` | `myid` per MyID, altrimenti `standard` per un IdP compatibile |
| `profiling.mode` | `local` oppure `pm`, come deciso al punto 1 |
| `frontend.url` | URL HTTPS completo della pagina `saml-login`, senza `#...` |
| `sp.entity-id` | Identificatore SP concordato al punto 3 |
| `sp.acs-url` | URL ACS completo concordato |
| `sp.slo-url` | URL SLO completo concordato |
| `idp.entity-id` | Identificatore IdP esatto confermato dal suo gestore |
| `idp.metadata-file` | Percorso del file XML IdP verificato |
| `idp.slo-url` | URL IdP per le richieste di logout, anche se assente nei metadata |
| `idp.slo-response-url` | URL IdP per le risposte di logout, confermato separatamente |
| `authn.contexts` | Stringhe esatte concordate, separate da virgole senza spazi |
| `sp.name-id-format` | `urn:oasis:names:tc:SAML:2.0:nameid-format:transient` |
| `sp.keystore-type` | `PKCS12` |
| `sp.keystore-file` | Percorso del PKCS12 creato al punto 6 |
| `sp.keystore-password` | Riferimento al segreto della password PKCS12 |
| `sp.key-alias` | `knowage-saml`, se si e seguito l'esempio |
| `sp.key-password` | Riferimento alla password della chiave nel PKCS12 |
| `identity.attribute` | `Name` esatto dell'attributo identificativo SAML |
| `identity.prefix` | Prefisso concordato da rimuovere; vuoto se non serve |
| `session.seconds` | Durata massima da 1 a 36000 secondi, secondo la policy |

Con MyID compilare anche `myid.logout-url` con l'URL `/logout/target`
confermato per l'ambiente. Non confonderlo con gli endpoint SLO SAML.
Con `provider=standard` questa proprieta non serve.

Una durata di 3600 secondi equivale a un'ora. Il codice applica anche
l'eventuale scadenza di sessione IdP; in modalita PM applica inoltre il
limite della prossima mezzanotte Europe/Rome.

### 8.2 Come funzionano i valori `env:`

Una riga come:

```properties
sp.keystore-file=env:KNOWAGE_SAML_KEYSTORE_FILE
```

non indica il nome del file: chiede a Knowage di leggere una **variabile
di ambiente del processo applicativo** chiamata `KNOWAGE_SAML_KEYSTORE_FILE`.
Il suo valore deve essere il percorso reale:

```text
C:\knowage\conf\saml\sp-signing.p12
```

Non inserire `env:` dentro il valore della variabile e non aggiungere
virgolette come parte del valore. Impostare nel servizio applicativo le
variabili corrispondenti alle righe `env:` mantenute nel file:

| Variabile dei modelli | Valore da predisporre |
| --- | --- |
| `KNOWAGE_SAML_IDP_ENTITY_ID` | Entity ID IdP, se il modello lo legge da ambiente |
| `KNOWAGE_SAML_IDP_METADATA_FILE` | `C:\knowage\conf\saml\idp-metadata.xml` |
| `KNOWAGE_SAML_IDP_SLO_URL` | Endpoint IdP di richiesta logout |
| `KNOWAGE_SAML_IDP_SLO_RESPONSE_URL` | Endpoint IdP di risposta logout |
| `KNOWAGE_SAML_KEYSTORE_FILE` | `C:\knowage\conf\saml\sp-signing.p12` |
| `KNOWAGE_SAML_KEYSTORE_PASSWORD` | Password PKCS12 |
| `KNOWAGE_SAML_KEY_ALIAS` | `knowage-saml`, se usato nella generazione |
| `KNOWAGE_SAML_KEY_PASSWORD` | Password della chiave nel PKCS12 |

Il modello MyID di collaudo contiene alcuni valori diretti: verificare
anche quelli, non solo le righe `env:`.

Un'assegnazione `$env:...` fatta nella propria PowerShell non configura
automaticamente un servizio Windows gia avviato. Usare il meccanismo del
servizio/wrapper o il gestore segreti del deployment e poi riavviare.
Far verificare al sistemista che il processo erediti i valori senza
stamparne i segreti nei log. Evitare variabili globali di macchina per
password se esiste un meccanismo con ambito e accesso piu limitati.

Se si scrive un percorso direttamente nel file `.properties`, raddoppiare
le barre inverse per l'escaping del formato:

```properties
idp.metadata-file=C:\\knowage\\conf\\saml\\idp-metadata.xml
```

Questo raddoppio serve nel `.properties`, non nel valore di una variabile
di ambiente ne nelle opzioni di avvio mostrate piu avanti.

### 8.3 Completare il ramo scelto

Con `local`, impostare `profiling.mode=local`. Se si e copiato un modello
MyID, eliminare dalla copia tutte le righe `pm.*`: non servono ne PM ne
le relative credenziali. Conservare provider e parametri MyID corretti.

Con `pm`, impostare `profiling.mode=pm` e compilare tutte queste proprieta
con i dati del punto 5:

| Proprieta | Contenuto |
| --- | --- |
| `pm.identity-parameter`, `pm.identity-field` | Contratto identificativo confermato dal gestore PM |
| `pm.token-url` | Endpoint token VAM dell'ambiente |
| `pm.client-id`, `pm.client-secret` | Credenziali OAuth2 autorizzate |
| `pm.service-user`, `pm.service-password` | Credenziali di servizio PM |
| `pm.profile-url`, `pm.user-url`, `pm.authorization-url` | Endpoint GET reali dallo Swagger sottoscritto |
| `pm.procedure`, `pm.user-type` | Procedura e tipo utente confermati |
| `pm.role-map.*`, `pm.tenant-map.*` | Mapping reali verso ruoli e tenant esistenti |
| `pm.function-map.*`, `pm.access-type-map.*` | Mapping reali verso funzionalita Knowage |

Se si mantengono i riferimenti dei modelli, predisporre nel servizio anche
`KNOWAGE_PM_CLIENT_ID`, `KNOWAGE_PM_CLIENT_SECRET`, `KNOWAGE_PM_SERVICE_USER`,
`KNOWAGE_PM_SERVICE_PASSWORD`, `KNOWAGE_PM_PROFILE_URL`,
`KNOWAGE_PM_USER_URL` e `KNOWAGE_PM_AUTHORIZATION_URL`.
I segreti non devono essere salvati nella repository.

**Controllo prima di continuare:** cercare `REPLACE_` nel file e sostituire
ogni occorrenza; controllare anche dominio fittizio, valori vuoti non ammessi,
livello dell'esempio e tutte le variabili `env:`. Non eliminare controlli
o proprieta obbligatorie per far sparire un errore.

## 9. Configurare l'avvio e riavviare

**Chi:** sistemista, nella finestra concordata.

Le opzioni seguenti sono parametri di avvio dell'applicazione, **non comandi
da eseguire in PowerShell e non righe del file `saml.properties`**.

Nel servizio che ospita il backend Knowage aggiungere:

```text
-Dknowage.saml.enabled=true
-Dknowage.saml.config=C:\knowage\conf\saml\saml.properties
-Dknowage.saml.datasource=java:comp/env/jdbc/SamlState
```

Su un Tomcat Windows standard gestito dal relativo monitor, aprire la
configurazione dell'istanza corretta e individuare Java > Java Options:
aggiungere una riga per opzione senza cancellare quelle esistenti.
Il nome del servizio e il percorso del monitor dipendono dall'installazione:
farli identificare al sistemista, senza agire su altre istanze.
Con un altro launcher/container usare il suo meccanismo di configurazione
persistente dei parametri del processo.

In alternativa il codice legge `KNOWAGE_SAML_ENABLED=true`,
`KNOWAGE_SAML_CONFIG` e `KNOWAGE_SAML_DATASOURCE` dall'ambiente.
Le proprieta `-D` hanno precedenza: non lasciare configurazioni contrastanti.

Impostare il DataSource anche su ogni JVM API/engine/consumer interessata
e su tutti i nodi. I consumer di token richiedono il registro condiviso;
non richiedono tutti necessariamente il file IdP/keystore del backend.
Conservare coerenti le chiavi JWT/HMAC esistenti.

Se OIDC non viene usato, impostare anche `-Doauth2_flow_type=NONE` nel
backend per evitare che la configurazione login tenti di caricare OIDC.
Se e usato, far valutare la configurazione al responsabile: non disabilitarlo
alla cieca e non assumere un ripiego automatico tra protocolli.

1. Salvare configurazione di avvio e variabili del servizio.
2. Controllare che l'account del servizio legga XML, PKCS12 e properties.
3. Riavviare i servizi interessati con la procedura ordinaria approvata.
4. Controllare lo stato dei servizi e i log applicativi.
5. Se compaiono errori JNDI, file mancanti o configurazione, correggerli
   prima di invitare utenti al login.

**Attenzione:** da questo momento la pagina di ingresso puo gia avviare SAML,
anche se il gestore IdP non ha ancora completato la registrazione. Limitare
l'accesso al collaudo e mantenere disponibile il ripristino.

## 10. Generare i metadata SP con Knowage e salvarli in un file

**Chi:** sistemista, da una postazione che raggiunge Knowage.

I metadata SP sono il documento XML che presenta Knowage all'IdP:
contengono identificatore dello SP, certificato pubblico e indirizzi di
login/logout. **Li genera Knowage nel momento in cui si richiama
`GET /knowage/restful-services/saml/metadata`.** Non esiste un comando
separato di generazione da lanciare, non serve un pulsante nella console
e non bisogna scrivere l'XML a mano.

Il passaggio e questo:

```text
Configurazione SAML + certificato SP nel PKCS12
    -> richiesta GET all'endpoint metadata
    -> Knowage costruisce l'XML e lo restituisce
    -> il sistemista salva la risposta in sp-metadata.xml
    -> il file viene consegnato al gestore IdP
```

Knowage non crea automaticamente un file `sp-metadata.xml` sul server:
restituisce il documento nella risposta HTTP. Il file nasce quando si
salva quella risposta. Il certificato pubblico viene incluso a partire
dal keystore configurato; la chiave privata non deve comparire nell'XML.

### 10.1 Verificare i prerequisiti della generazione

Prima di richiamare l'endpoint devono essere completati i punti 6-9:
certificato/PKCS12 SP, metadata IdP verificati gia salvati localmente,
registro e risorsa JNDI, properties compilate, variabili disponibili al
servizio, SAML abilitato e applicazione riavviata.

**Si ricevono prima i metadata IdP e poi si generano quelli SP.** Sono
due documenti distinti. Nell'implementazione attuale la generazione carica
la configurazione SAML completa e risolve la risorsa JNDI. Il gestore
IdP non deve pero avere gia attivato il nostro SP per consentire questa
operazione: l'XML generato serve proprio a completarne la registrazione.
Non serve effettuare un login SAML riuscito per generarlo.

1. Collegarsi alla VPN, se necessaria.
2. Aprire nel browser l'URL reale:
   `https://HOST/knowage/restful-services/loginconfig`.
3. Verificare nella risposta JSON, dentro `items[0]`, i valori
   `samlActive: true` e `authenticationMethod: "SAML"`.

Questo controllo prova che il flag SAML e attivo: **non verifica da solo
file, certificati, database o registrazione IdP**. Procedere alla generazione
effettiva descritta sotto.

### 10.2 Generare e salvare l'XML

Scegliere uno dei due metodi: entrambi richiamano lo stesso endpoint e
quindi fanno generare il documento a Knowage.

**Metodo A: browser.**

1. Sostituire `HOST` con il DNS HTTPS reale di Knowage.
2. Aprire `https://HOST/knowage/restful-services/saml/metadata`.
3. Attendersi un documento XML, non una pagina di login o HTML.
4. Se il browser scarica direttamente un file, conservarlo con nome
   `sp-metadata.xml`. Se mostra l'XML, usare Salva con nome e scegliere
   il formato XML/originale, non pagina web completa o PDF.
5. Salvare nella cartella amministrativa scelta e verificare che
   l'estensione sia `.xml`, non `.xml.html` o `.xml.txt`.

**Metodo B: PowerShell e `curl.exe`, con esito HTTP visibile.**

Su una postazione Windows amministrativa con `curl.exe`, sostituire
l'host fittizio nel comando. Usare una cartella esistente e un nome di file
non gia utilizzato per una precedente consegna. L'esempio assume che
`C:\knowage\conf\saml` esista sulla postazione da cui si esegue il comando:

```powershell
curl.exe --fail --show-error --write-out 'HTTP %{http_code}' --output 'C:\knowage\conf\saml\sp-metadata.xml' 'https://knowage-collaudo.ente.example/knowage/restful-services/saml/metadata'
$LASTEXITCODE
```

Attendersi `HTTP 200` e poi `0`: il secondo valore e il codice di uscita
del comando, dove zero indica successo. Se il comando fallisce o restituisce
un altro stato, non consegnare un file eventualmente rimasto da una prova
precedente: risolvere l'errore e ripetere la generazione.

L'esempio scrive sul computer che esegue il comando, non automaticamente
sul server. Non aggiungere `-k` per ignorare errori TLS e non accettare
alla cieca un file HTML restituito da un proxy.

Il successo di questo endpoint non prova da solo lettura/scrittura SQL
o un login federato completo: quelli si verificano nel collaudo.

### 10.3 Controllare il documento generato prima di consegnarlo

Aprire `sp-metadata.xml` con un editor di testo. Se si e usato il percorso
dell'esempio, da PowerShell:

```powershell
notepad.exe 'C:\knowage\conf\saml\sp-metadata.xml'
```

Attendersi un documento con elemento `EntityDescriptor`, eventualmente
preceduto da un prefisso XML come `md:`. Verificare:

- `entityID`: deve essere lo `sp.entity-id` concordato.
- `AssertionConsumerService`: URL ACS corretto e binding HTTP-POST.
- `SingleLogoutService`: URL SLO corretto e binding HTTP-POST.
- Certificato pubblico: deve corrispondere al certificato SP predisposto;
  farne verificare l'impronta al sistemista con gli strumenti XML/certificati.

Non modificare manualmente il documento per correggere URL o certificati:
correggere la configurazione, riavviare se necessario e richiamare nuovamente
l'endpoint per generare e salvare il documento aggiornato. Se cambiano
Entity ID, URL o certificato SP dopo la consegna, coordinare anche
l'aggiornamento della registrazione presso l'IdP.
Non considerare il metadata automaticamente firmato: se il processo IdP
richiede una firma del documento, concordarne modalita e strumenti con il
gestore prima della consegna. Firma dei messaggi SAML e firma del metadata
sono requisiti diversi.

**Risultato da ottenere:** file XML SP generato da Knowage e controllato,
pronto per la registrazione definitiva.

## 11. Far completare l'attivazione al gestore IdP

**Chi:** referente applicativo e gestore IdP.

1. Consegnare metadata SP e certificato pubblico tramite il canale concordato.
2. Confermare gli indirizzi definitivi, senza differenze di schema, host,
   porta, contesto o slash rispetto alla configurazione.
3. Chiedere l'attivazione dello SP e dell'attributo identificativo concordato.
4. Per MyID, far registrare anche il ritorno finale completo:
   `https://HOST/knowage-vue/saml-login#samlLoggedOut=true`.
5. Farsi confermare che contesti di autenticazione e utenti di prova siano
   abilitati per quello specifico SP e ambiente.
6. Ottenere conferma esplicita di attivazione prima del collaudo.

Se l'IdP non raggiunge l'URL dei metadata perche e in VPN, usare la procedura
di consegna file concordata al punto 4; non aprire Internet come soluzione
automatica. Se l'IdP richiede obbligatoriamente il download remoto, risolvere
prima il requisito con i referenti rete.

## 12. Eseguire il primo login completo

**Chi:** collaudatore, con gestori disponibili per la diagnosi.

1. Aprire una nuova finestra privata del browser, senza altre sessioni Knowage.
2. Collegarsi alla VPN se necessaria e non scollegarla durante la prova.
3. Aprire `https://HOST/knowage-vue/saml-login`.
4. Verificare il passaggio automatico alla pagina IdP: non e previsto un
   pulsante SAML da attivare manualmente.
5. Autenticarsi con l'utenza di collaudo, completando l'eventuale secondo fattore.
6. Attendersi il ritorno in Knowage senza errori.
7. Verificare nome/account effettivo, tenant, ruoli, menu e funzionalita.
8. Aprire un documento/dataset previsto per il ruolo e verificare i dati
   realmente visibili. Provare anche gli engine utilizzati nell'ambiente.
9. Se disponibile al collaudatore tecnico, controllare nella scheda Rete
   del browser che le richieste autenticate, inclusa `currentuser`, abbiano
   esito atteso; la sola pagina iniziale caricata non basta.
10. Chiudere la sessione con il logout prima di provare un altro utente.

Non provare ACS aprendo il suo URL a mano: riceve un POST SAML firmato,
non una normale navigazione GET. Non aprire direttamente `/saml/login`
senza la pagina frontend: il codice richiede il challenge generato dal browser.

Se il frontend termina su `#samlError=authentication`, non basta sapere che
la password IdP era corretta: possono essere fallite anche firma, attributo
identificativo, account locale, PM o registro condiviso.

## 13. Provare errori, logout e separazione dei permessi

**Chi:** collaudatore e sistemista, soltanto su utenze/risorse di test.

Registrare per ogni prova esito atteso, esito ottenuto, ambiente e orario,
senza inserire password, token o asserzioni nei verbali.

| Prova | Risultato atteso |
| --- | --- |
| Secondo utente con permessi diversi | Account e permessi propri, senza ereditare quelli del primo |
| Utenti di tenant diversi, se presenti | Nessuna visibilita sui dati dell'altro tenant |
| Identificativo assente/non corrispondente | Accesso negato, nessun account creato automaticamente |
| Utente locale bloccato, senza ruoli o inesistente, con `local` | Accesso negato |
| Utente PM bloccato/non autorizzato o PM indisponibile, con `pm` | Accesso negato, nessun ripiego sui permessi locali |
| Logout da Knowage | Sessione chiusa, nessun nuovo login avviato automaticamente dal ritorno di logout |
| Logout remoto IdP notificato a Knowage | Sessione federata revocata |
| Richiesta con vecchio token dopo il logout su ogni API/engine/nodo | Rifiutata anche se il token non e ancora scaduto |

Le prove di indisponibilita e di riuso del vecchio token devono essere
eseguite dal collaudatore tecnico senza interrompere servizi condivisi o
esporre credenziali. Non incollare token in servizi online per leggerli.
Il tasto Indietro e la sola sparizione della pagina non provano la revoca:
serve una richiesta autenticata effettiva al backend/engine.

Modificare ruoli o bloccare un account non garantisce l'aggiornamento
istantaneo dei token gia emessi: i profili vengono acquisiti al login.
Per rendere effettiva subito una variazione occorre la revoca della sessione
secondo la procedura amministrativa approvata, oppure attendere la scadenza.

## 14. Se qualcosa non funziona

| Sintomo | Primo controllo |
| --- | --- |
| Host irraggiungibile | VPN, DNS, firewall, proxy e contesto effettivo |
| Errore HTTPS | Nome del certificato, catena CA e orologio; non disabilitare TLS |
| Metadata 404 | Flag SAML sul backend giusto; anche routing, versione e contesto possono causare 404 |
| Metadata 401 e log con `PersistenceUnitResource.getTypes` | Conflitto di routing con EclipseLink: distribuire il backend con la risorsa dedicata `SamlMetadataResource`; non abilitare SAML o allentare l'autenticazione per aggirarlo |
| Metadata 500 | Properties, variabili del servizio, file leggibili, password/alias PKCS12, metadata IdP, JNDI |
| `loginconfig` 500 | Log backend e configurazione OIDC eventualmente caricata; non e una prova diretta di errore SAML |
| `loginconfig` attivo ma nessun redirect | Frontend coordinato distribuito, URL della pagina e cache del deployment |
| IdP rifiuta la richiesta | Registrazione SP, Entity ID, certificato, contesto richiesto e compatibilita algoritmi |
| Ritorno con errore di autenticazione | Registrare orario e utenza di test; verificare correlazione, firme, attributo, profilo e registro con i gestori |
| Funziona su un nodo ma non su un altro | File/opzioni coerenti, registro e JNDI condivisi, chiavi JWT/HMAC e orologi |
| Logout seguito subito da nuovo login | URL finale/callback corretti e registrati, frontend coordinato |
| Vecchio token ancora accettato da un engine | Versione common/utils, filtro federato e registro condiviso su quel consumer |

I log possono mostrare soltanto un rifiuto generico di autenticazione:
e intenzionale, per non esporre risposte SAML o segreti. Coordinare la
diagnosi con orario, ambiente e risultato, senza abilitare il dump dei payload.

Se si perde l'accesso e non si riesce a correggere durante la finestra:

1. Dalla console del server disabilitare SAML nel parametro effettivamente
   usato (`-Dknowage.saml.enabled=false` se si usa la proprieta di avvio).
2. Ripristinare la configurazione di autenticazione precedente salvata,
   inclusi gli eventuali parametri OIDC modificati.
3. Riavviare i servizi interessati e verificare il login precedente.
4. Considerare che i token federati gia emessi richiedono comunque una
   gestione esplicita della revoca: disabilitare la pagina SAML non e una
   procedura generale di revoca di tutte le sessioni.

Non cancellare la tabella del registro per tentativi e non ripristinare
l'intero database senza una decisione del DBA e del responsabile applicativo.

## 15. Passare in produzione e organizzare la manutenzione

**Chi:** tutti i referenti, dopo l'accettazione del collaudo.

1. Ripetere preparazione e registrazione per la produzione: non riusare
   chiavi private, password, credenziali o metadata di collaudo.
2. Usare Entity ID, URL, utenti e parametri IdP/PM della produzione.
3. Pianificare backup, attivazione e ripristino con una finestra approvata.
4. Ripetere i controlli di login, permessi, dati, engine e logout prima
   dell'apertura agli utenti previsti.
5. Annotare responsabile e scadenza del certificato SP, chi gestisce il
   rinnovo e con quanto anticipo avvisare il gestore IdP.
6. Concordare come ricevere e verificare la rotazione dei certificati IdP.
   Knowage legge un file locale: non si aggiorna automaticamente seguendo
   un URL. Aggiornare in modo coordinato tutti i nodi interessati.
7. Monitorare disponibilita del registro, PM/VAM se usati, orologi e scadenze.
8. Far pianificare al DBA la pulizia dei soli record con `EXPIRES_AT`
   scaduto: il valore e espresso in millisecondi dall'epoch, non in secondi.
9. Proteggere registro e backup: possono contenere token temporanei.
10. Conservare configurazione e verbale di collaudo in un deposito
    amministrativo autorizzato, senza segreti in Git o nei ticket.

**Il lavoro e concluso soltanto quando accesso, permessi reali, separazione
dei dati e revoca delle sessioni sono stati verificati sui componenti usati.
Un metadata scaricabile o un login riuscito da soli non bastano.**
