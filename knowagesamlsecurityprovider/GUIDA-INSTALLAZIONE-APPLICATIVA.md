# Installazione applicativa MyID/PM - guida essenziale

Per il sistemista di un **Knowage gia operativo**: aggiunta dell'integrazione,
non installazione dell'IdP. Servono release 9.2 integrata e collaudata,
Java 17/Tomcat 9 (`javax.servlet`), properties di esempio, script SQL del DB
e materiali concordati nella [guida preparatoria](GUIDA-PREPARAZIONE-IDP.md).
I percorsi Windows sono esempi.

**Variante senza PM:** usare `standard.properties.example` con
`provider=standard` per un IdP standard, oppure mantenere `provider=myid` e
i relativi endpoint per MyID. Impostare `profiling.mode=local`
e `identity.attribute=<attributo
SAML contenente lo user_id Knowage esatto>`, con `identity.prefix=` se non serve
normalizzazione. Creare prima gli utenti Knowage e assegnare i ruoli nel
tenant di ciascun account; il tenant viene ricavato automaticamente dall'utente
locale, anche in ambienti multi-tenant. Non impostare `local.tenant`.
Omettere tutte le proprieta e i prerequisiti PM/VAM della
guida; rete, certificati, registro, distribuzione e logout restano necessari.
Password/MFA sono gestiti dall'IdP. Il blocco locale nega il login;
ruoli, attributi, ruolo predefinito e superadmin provengono dal catalogo Knowage,
non dall'asserzione. Collaudare due utenti locali di tenant diversi e il rifiuto
di utenti inesistenti, bloccati, senza tenant/ruoli o con ruoli di un altro tenant.

## 1. Preparare rete e HTTPS

Pubblicare frontend/backend sulla **stessa origine HTTPS**, mantenendo i contesti
API/engine. ACS/SLO devono coincidere con le URL registrate presso MyID.
Proxy: preservare POST/parametri SAML, nessun login aggiuntivo sugli endpoint
SAML e nessuna cache su login/ACS/exchange/SLO. Tomcat deve riconoscere HTTPS;
preservare cookie Secure/HttpOnly e CSRF.
Consentire HTTPS verso PM/VAM; verificare DNS, CA TLS fidate nel runtime Java
e NTP su tutti i nodi, senza disabilitare i controlli TLS.

## 2. Distribuire gli artefatti

Backup di applicazione/configurazioni/DB; fermare i servizi. Distribuire backend,
frontend di produzione, API ed engine con common/utils e provider aggiornati:
**non soltanto il JAR SAML o il WAR principale**. Non usare Vite o il PM mock locale.
Verificare `META-INF\web-fragment.xml` dei utils e `FederatedSessionFilter` in
ogni WAR; mantenere coerenti le chiavi JWT/HMAC esistenti.

## 3. Predisporre il registro condiviso

Applicare una sola volta `src\main\resources\saml\schema-<db>.sql`
(mysql/postgresql/oracle/hsqldb). Se `SBI_SAML_STATE` esiste, verificarla senza
ricrearla. DB persistente e **stesso schema scrivibile per tutti i consumer**.
Configurare `jdbc/SamlState` in ogni contesto/JVM: SELECT/INSERT/UPDATE/DELETE,
auto-commit `true`, READ_COMMITTED, supporto `SELECT FOR UPDATE`.

Esempio MySQL, da aggiungere nel `<Context>` esistente di
`conf\Catalina\localhost\knowage.xml` e nei contesti degli altri consumer:

```xml
<Resource name="jdbc/SamlState" auth="Container" type="javax.sql.DataSource"
    factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
    driverClassName="com.mysql.cj.jdbc.Driver"
    url="jdbc:mysql://DB_HOST:3306/KNOWAGE_DB"
    username="UTENTE_DB" password="SEGRETO_DB"
    defaultAutoCommit="true" defaultTransactionIsolation="2" />
```

Sostituire valori, proteggere credenziali e installare il driver nel runtime
del pool. Negli altri DB/consumer adattare driver/URL mantenendo lo stesso JNDI.

## 4. Installare i materiali di fiducia

In `C:\knowage\conf`, fuori dai WAR, installare metadata IdP verificati e
PKCS12 SP con chiave privata RSA/certificato dell'ambiente. Lettura solo agli
account autorizzati; niente chiavi di test/laboratorio o condivise fra ambienti.

## 5. Compilare il file di configurazione

Copiare `myid-production.properties.example` (oppure
`myid-collaudo.properties.example`) da `src\main\resources\saml` in
`C:\knowage\conf\saml.properties`; compilare:

| Chiavi | Valori da inserire |
| --- | --- |
| `provider` | `myid` |
| `profiling.mode` | `pm` per questa guida; `local` per la variante senza PM |
| `frontend.url` | URL HTTPS pubblico della pagina `/knowage-vue/saml-login`, **senza fragment** |
| `sp.entity-id`, `sp.acs-url`, `sp.slo-url` | Valori esatti registrati presso MyID |
| `authn.contexts` | Contesti concordati, separati da virgole senza spazi; non assumere il livello dell'esempio |
| `sp.name-id-format` | `urn:oasis:names:tc:SAML:2.0:nameid-format:transient` |
| `myid.logout-url`, `pm.token-url` | Endpoint confermati per l'ambiente |
| `identity.attribute`, `identity.prefix` | Attributo identificativo MyID e prefisso da rimuovere, oppure prefisso vuoto |
| `pm.identity-parameter`, `pm.identity-field` | Parametro di ricerca e campo utente supportati realmente da PM |
| `pm.procedure`, `pm.user-type` | Procedura e tipo utente confermati dal gestore PM |
| `pm.role-map.*`, `pm.tenant-map.*` | Codici PM verso ruoli e tenant Knowage esistenti; organizzazioni distinte verso tenant distinti |
| `pm.function-map.*`, `pm.access-type-map.*` | Codici PM verso funzionalita Knowage, separabili con virgole senza spazi |
| `session.seconds` | Durata da 1 a 36000 secondi; resta limitata dalla scadenza IdP e dalla mezzanotte Europe/Rome |

Valorizzare **ogni `env:` del file nell'ambiente del servizio Tomcat**:
parametri IdP, keystore SP, credenziali VAM/PM e tre endpoint PM.
Nessun `REPLACE_`, mapping fittizio o segreto mancante.
Non sostituire globalmente i supplier di autenticazione.

## 6. Abilitare e riavviare

Opzioni JVM Knowage:

```text
-Dknowage.saml.enabled=true
-Dknowage.saml.config=C:\knowage\conf\saml.properties
-Dknowage.saml.datasource=java:comp/env/jdbc/SamlState
```

Impostare il DataSource anche sulle JVM API/engine. Se OIDC non e utilizzato,
impostare `-Doauth2_flow_type=NONE`. Riavviare tutti i servizi aggiornati.

## 7. Consegnare i metadata e verificare l'attivazione

`GET https://HOST_PUBBLICO/knowage/restful-services/saml/metadata`: HTTP 200,
XML con Entity ID/certificato/ACS/SLO concordati. Inviare al gestore MyID;
ottenere attivazione e registrazione del ritorno post-logout
`/knowage-vue/saml-login#samlLoggedOut=true`.

`GET https://HOST_PUBBLICO/knowage/restful-services/loginconfig` deve restituire
`items[0].samlActive=true` e `authenticationMethod=SAML`.

## 8. Eseguire il collaudo minimo

Aprire `/knowage-vue/saml-login`: redirect MyID automatico, niente pulsante
federato/form locale. Provare due profili PM diversi: account/ruoli/funzionalita
attesi, `currentuser` HTTP 200 e scope dati corretto. Il flag superadmin globale
non viene abilitato dal profilo federato.
Login negato per identita assente/non corrispondente, utente bloccato o PM
indisponibile. Logout Knowage e remoto MyID: nessun rientro automatico;
vecchio token rifiutato alla richiesta successiva su **ogni API/engine aperto**.
Aprire al pubblico solo dopo questi controlli.

## 9. Impostare la gestione operativa

Monitorare DB/PM/VAM/NTP, pianificare rinnovi e aggiornare metadata su tutti i
nodi. Pulire solo record con `EXPIRES_AT` scaduto (epoch millisecondi).
Proteggere registro/backup, contenenti anche token temporanei.
Niente asserzioni/token/codici/verificatori/segreti nei log;
logger `com.onelogin.saml2` disattivato.
