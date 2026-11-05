# Preparazione dell'integrazione Knowage con MyID/PM

Destinatari: referente applicativo e gestore MyID. L'amministrazione dell'IdP
rimane al suo gestore; noi configuriamo Knowage come Service Provider (SP).
Preparare dati, certificati e registrazioni separati per collaudo e produzione.

## 1. Cosa fornire al gestore dell'Identity Provider

Sostituire `HOST_PUBBLICO` con il DNS HTTPS definitivo. I contesti seguenti
presuppongono `/knowage` per il backend e `/knowage-vue` per il frontend;
se diversi, concordare quelli effettivi prima della registrazione.

| Dato da fornire | Valore o contenuto |
| --- | --- |
| Identificazione del servizio | Nome Knowage, ente/progetto, ambiente e referente tecnico |
| Entity ID dello SP | Proposta: `https://HOST_PUBBLICO/knowage/restful-services/saml/metadata`; deve coincidere con `sp.entity-id` |
| Metadata SP | File XML generato da Knowage e URL `https://HOST_PUBBLICO/knowage/restful-services/saml/metadata` |
| ACS: destinazione delle risposte di login | `https://HOST_PUBBLICO/knowage/restful-services/saml/acs`, binding **HTTP-POST** |
| SLO: destinazione dei messaggi di logout | `https://HOST_PUBBLICO/knowage/restful-services/saml/slo`, binding **HTTP-POST** |
| URL di ingresso al servizio | `https://HOST_PUBBLICO/knowage-vue/saml-login` |
| URL finale dopo il logout MyID | `https://HOST_PUBBLICO/knowage-vue/saml-login#samlLoggedOut=true`, da registrare presso MyID |
| Certificato di firma dello SP | Certificato pubblico X.509 con chiave RSA, presente anche nel metadata; formato e rinnovo concordati |
| Livello di autenticazione | Livello richiesto dal servizio e corrispondente `AuthnContextClassRef` concordato |
| Attributo identificativo da rilasciare | Nome SAML esatto, formato e regole descritte al punto 2 |

**Non fornire chiavi private, file PKCS12, password del keystore, chiavi JWT
Knowage o credenziali PM/VAM.** Il certificato SAML pubblico non va confuso
con il certificato TLS del sito.

Se l'applicazione non e ancora disponibile, inviare prima scheda del servizio,
URL proposte e certificato pubblico. Ottenuti i metadata IdP, il sistemista puo
predisporre Knowage ed esportare l'XML SP seguendo la guida di installazione.
Consegnare l'XML finale prima dell'attivazione; non costruire metadata a mano.
Concordare eventuali requisiti di firma del metadata previsti dall'attivazione.

## 2. Identificativo: cosa concordare, senza imporre il codice fiscale

Richiedere **un attributo SAML con un solo valore, stabile e univoco**, rilasciato
al nostro SP. Puo essere il codice fiscale oppure un altro identificativo,
purche il servizio PM supporti la ricerca e il confronto con quel valore.
Concordare il nome `Name` dell'attributo SAML, non soltanto un'etichetta UI.

| Informazione da fissare | Configurazione applicativa |
| --- | --- |
| Attributo SAML che contiene l'identificativo | `identity.attribute` |
| Eventuale prefisso da rimuovere; vuoto se non necessario | `identity.prefix` |
| Parametro accettato da PM nelle ricerche di profilo e autorizzazione | `pm.identity-parameter` |
| Campo dell'utente PM che deve riportare lo stesso identificativo | `pm.identity-field` |

Le ultime due informazioni vanno confermate dal **gestore PM**, non dedotte
dal nome dell'attributo MyID. Nome dei campi, formato del valore e
maiuscole/minuscole devono essere coerenti fra i due sistemi.
Un PM che accetta soltanto codici fiscali non diventa compatibile con altri
identificativi rinominando il parametro lato Knowage.

Il `NameID` SAML transient serve al protocollo e al logout, non a scegliere
l'account Knowage. Non richiedere ruoli Knowage nell'asserzione: account,
ruoli e autorizzazioni applicative provengono da PM.

## 3. Cosa ottenere e confermare con il gestore MyID

| Risposta attesa dal gestore | Uso |
| --- | --- |
| Metadata XML IdP da canale fidato, Entity ID e certificati di firma | Configurazione della fiducia in MyID |
| Endpoint SSO con binding **HTTP-Redirect** | Invio delle richieste di autenticazione firmate |
| Endpoint SLO di richiesta e risposta con binding **HTTP-POST** | `idp.slo-url` e `idp.slo-response-url`; non presumere che siano nei metadata pubblici |
| URL del servizio MyID `/logout/target` per l'ambiente | `myid.logout-url`; Knowage invia il parametro `spid` con l'Entity ID SP |
| Conferma della registrazione dell'URL finale post-logout | Evita di tornare automaticamente al login MyID dopo il logout |
| Conferma del rilascio dell'attributo concordato | Necessario per risolvere l'identita in PM |
| Contesti di autenticazione autorizzati | `authn.contexts`, confronto esatto; non copiare il livello dell'esempio senza verifica |
| Registrazione/qualificazione e Circle of Trust applicabili | Abilitazione dello SP secondo il processo MyID previsto |
| Utenze di collaudo e procedura di rinnovo certificati | Verifica login/logout e gestione operativa |

Confermare la compatibilita con l'implementazione: SAML 2.0, login avviato
da Knowage, restituzione di `RelayState` e `InResponseTo`, Response e Assertion
firmate, `NameID` transient e contesto di sessione per il logout.
Il profilo `provider=myid` usa RSA-SHA1/SHA1: concordarne l'accettazione con il
gestore; non modificare globalmente le policy crittografiche della JVM.
Non sono previsti binding SOAP/Artifact o login IdP-initiated non correlati.

## 4. Prerequisito separato: disponibilita di PM

Ottenere dal gestore PM/VAM gli endpoint HTTPS reali e lo Swagger sottoscritto,
le credenziali OAuth2 e di servizio tramite canale sicuro, il codice procedura,
il tipo utente e i codici di ruoli, organizzazioni, funzioni e tipi di accesso.
Le utenze di collaudo devono avere profili PM attivi con identificativi
corrispondenti a quelli rilasciati da MyID.

La preparazione e completa quando il contratto dell'identificativo e confermato
da **entrambi** i gestori, lo SP e registrato, gli endpoint di logout sono
concordati e le utenze di prova sono abilitate anche in PM.
