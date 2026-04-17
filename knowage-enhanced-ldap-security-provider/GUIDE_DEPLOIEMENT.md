# Guide de déploiement — Authentification LDAP/Active Directory pour Knowage

**Composant** : `knowage-enhanced-ldap-security-provider`
**Version** : 9.1.x
**Compatibilité** : Knowage 9.x, Java 17+, Active Directory / OpenLDAP

---

## 1. Présentation

Ce composant remplace le fournisseur d'authentification LDAP livré en standard avec Knowage par une implémentation corrigée et enrichie, conçue pour les environnements Active Directory multi-OU.

### Fonctionnalités

| Fonctionnalité | Description |
|---|---|
| Recherche subtree | L'utilisateur est recherché dans tout l'annuaire depuis la racine (`BASE_DN`), quelle que soit son OU de rattachement |
| Provisionnement automatique | À la première connexion, le compte est créé automatiquement dans Knowage si `AUTO_CREATE_USER=true` |
| Synchronisation des rôles | À chaque connexion, les groupes AD sont comparés aux rôles Knowage : les nouveaux groupes sont ajoutés, les groupes révoqués dans l'AD sont supprimés de Knowage |
| Support des noms non-ASCII | Les groupes AD dont le nom contient des caractères accentués (retournés en `byte[]` par JNDI) sont correctement décodés en UTF-8 |
| Politique de referral configurable | Le comportement face aux referrals LDAP (Active Directory multi-domaine) est paramétrable via `LDAP_REFERRAL` |
| Timeouts réseau | Timeout de connexion (5 s) et de lecture (10 s) configurés pour éviter le blocage de threads Tomcat en cas de défaillance réseau |
| Mot de passe chiffré | Le mot de passe du compte de service peut être stocké chiffré (`ENC(...)`) |
| Repli interne | Si l'annuaire est indisponible, l'authentification Knowage interne peut prendre le relai (`FALLBACK_TO_INTERNAL=true`) |

---

## 2. Prérequis

### Côté infrastructure

- Knowage 9.x déployé sur Tomcat
- Connectivité réseau entre le serveur Knowage et le contrôleur de domaine (port 389 ou 636 pour LDAPS)
- Un **compte de service** Active Directory (ou LDAP) avec les droits en lecture sur les OUs utilisateurs et groupes

### Informations à recueillir avant déploiement

| Information | Exemple | Obtenu auprès de |
|---|---|---|
| Nom d'hôte du contrôleur de domaine | `ad.mondomaine.fr` | Équipe Active Directory |
| Port LDAP | `389` (ou `636` avec SSL) | Équipe Active Directory |
| DN racine de l'annuaire | `DC=mondomaine,DC=fr` | Équipe Active Directory |
| DN complet du compte de service | `CN=svc-knowage,OU=Service_Accounts,DC=mondomaine,DC=fr` | Équipe Active Directory |
| Mot de passe du compte de service | — | Équipe Active Directory |
| Attribut d'identifiant utilisateur | `sAMAccountName` (AD) ou `uid` (OpenLDAP) | Équipe Active Directory |
| Filtre sur les groupes autorisés | ex. `KNW_.*` (regex) | Métier / Équipe sécurité |
| Liste des groupes AD → rôles Knowage | ex. `KNW_ADMIN` → admin | Métier |

---

## 3. Livrable

Le composant est livré sous forme d'un fichier JAR unique :

```
knowage-enhanced-ldap-security-provider-9.1.0-SNAPSHOT.jar
```

> Ce fichier est compilé depuis les sources disponibles sur
> [github.com/egwada/Knowage-Server](https://github.com/egwada/Knowage-Server),
> branche `enhance-ldap-security-provider`.

---

## 4. Installation du JAR

Copier le JAR dans le répertoire des bibliothèques de l'application Knowage :

```bash
cp knowage-enhanced-ldap-security-provider-9.1.0-SNAPSHOT.jar \
   /chemin/vers/tomcat/webapps/knowage/WEB-INF/lib/
```

> **Environnement Docker** : si Knowage tourne dans un conteneur, ajouter le JAR à l'image
> via un `Dockerfile` :
> ```dockerfile
> FROM knowagelabs/knowage-server-docker:9.1-SNAPSHOT
> COPY knowage-enhanced-ldap-security-provider-9.1.0-SNAPSHOT.jar \
>      /home/knowage/apache-tomcat/webapps/knowage/WEB-INF/lib/
> ```

---

## 5. Configuration LDAP

### 5.1 Créer le fichier de configuration

Créer le fichier `/etc/knowage/ldap.properties` (le chemin est libre) en s'appuyant sur le modèle ci-dessous. **Adapter toutes les valeurs à l'environnement cible.**

```properties
# =============================================================================
# Connexion au serveur LDAP / Active Directory
# =============================================================================
HOST=ad.mondomaine.fr
PORT=389
USE_SSL=false
# Mettre USE_SSL=true et PORT=636 pour une connexion chiffrée (LDAPS)

# DN racine de l'annuaire (point de départ de toutes les recherches)
BASE_DN=DC=mondomaine,DC=fr

# =============================================================================
# Compte de service (utilisé pour lier et rechercher les utilisateurs)
# =============================================================================
# DN complet du compte de service
ADMIN_USER=CN=svc-knowage,OU=Service_Accounts,DC=mondomaine,DC=fr
# Mot de passe en clair (ou chiffré, voir section 5.3)
ADMIN_PSW=motdepasseservice

# =============================================================================
# Recherche des utilisateurs
# =============================================================================
# Laisser vide pour rechercher depuis BASE_DN, ou restreindre à une OU :
USER_SEARCH_PATH=
# Attribut de connexion : sAMAccountName pour AD, uid pour OpenLDAP
USER_ID_ATTRIBUTE=sAMAccountName
USER_OBJECT_CLASS=person
# Attribut utilisé comme nom complet dans Knowage
USER_DISPLAYNAME_ATTR=displayName

# =============================================================================
# Groupes et rôles
# =============================================================================
# LDAP  : les groupes AD déterminent les rôles Knowage
# KNOWAGE : les rôles sont gérés manuellement dans Knowage (ignorer les groupes AD)
ROLES_SOURCE=LDAP
# Attribut de membership sur l'entrée utilisateur
USER_MEMBEROF_ATTRIBUTE=memberOf
# Expression régulière : seuls les groupes dont le nom correspond sont pris en compte.
# Exemples :
#   KNW_.*         → tous les groupes commençant par KNW_
#   (KNW_ADMIN|KNW_DEV)  → uniquement ces deux groupes
#   laisser vide   → tous les groupes sont acceptés
ACCESS_GROUP_FILTER=KNW_.*

# =============================================================================
# Provisionnement automatique
# =============================================================================
# true : créer le compte Knowage à la première connexion si absent
AUTO_CREATE_USER=true
# Rôle assigné lors de la création (laisser vide pour aucun rôle par défaut)
DEFAULT_ROLE=
# Tenant Knowage de rattachement
DEFAULT_TENANT=DEFAULT_TENANT

# =============================================================================
# Comportement en cas d'erreur LDAP
# =============================================================================
# true : si l'annuaire est inaccessible, tenter l'authentification Knowage interne
FALLBACK_TO_INTERNAL=true

# =============================================================================
# Politique de referral LDAP (Active Directory multi-domaine)
# =============================================================================
# ignore : ignorer les referrals (recommandé pour la plupart des environnements AD)
# follow : suivre les referrals vers d'autres contrôleurs de domaine
# throw  : lever une exception à la rencontre d'un referral (comportement JNDI par défaut)
# Valeur par défaut si absente : ignore
LDAP_REFERRAL=ignore
```

### 5.2 Déclarer le fichier de configuration au démarrage de Tomcat

Ajouter la propriété système suivante dans `setenv.sh` (ou `setenv.bat` sous Windows) :

```bash
# Fichier : $CATALINA_HOME/bin/setenv.sh
export CATALINA_OPTS="$CATALINA_OPTS -Denhanced.ldap.config=/etc/knowage/ldap.properties"
```

> **Environnement Docker** : passer la variable via `docker-compose.yml` :
> ```yaml
> environment:
>   - CATALINA_OPTS=-Denhanced.ldap.config=/home/knowage/apache-tomcat/resources/ldap.properties
> ```
> et monter le fichier comme volume :
> ```yaml
> volumes:
>   - ./ldap.properties:/home/knowage/apache-tomcat/resources/ldap.properties:ro
> ```

### 5.3 Chiffrement du mot de passe du compte de service (optionnel)

Pour ne pas stocker le mot de passe en clair :

1. Définir une clé de chiffrement symétrique au démarrage de la JVM :
   ```bash
   export CATALINA_OPTS="$CATALINA_OPTS -Dsymmetric_encryption_key=CleSecrete32Caracteres"
   ```
   > Cette clé doit être identique à celle déjà utilisée par Knowage pour le chiffrement
   > des données sensibles (`SENSIBLE_DATA_ENCRYPTION_SECRET`).

2. Chiffrer le mot de passe via la console d'administration Knowage ou l'utilitaire fourni.

3. Remplacer la valeur dans le fichier de configuration :
   ```properties
   ADMIN_PSW=ENC(valeurBase64ChiffreeIci==)
   ```

---

## 6. Configuration dans Knowage

### 6.1 Créer les rôles correspondant aux groupes Active Directory

Dans la console d'administration Knowage (**Administration → Rôles**), créer un rôle pour chaque groupe AD autorisé. Le **nom du rôle doit correspondre exactement au nom du groupe AD**.

| Groupe Active Directory | Nom du rôle Knowage | Type de rôle |
|---|---|---|
| `KNW_ADMIN` | `KNW_ADMIN` | Administrateur |
| `KNW_DEV` | `KNW_DEV` | Développeur |
| `KNW_USER` | `KNW_USER` | Utilisateur |

> Le type de rôle (Administrateur, Développeur, Utilisateur…) détermine les permissions
> dans Knowage. Le nom est la clé de correspondance avec le groupe AD.

### 6.2 Activer le fournisseur d'authentification

Exécuter la requête SQL suivante sur la base de données Knowage :

```sql
UPDATE SBI_CONFIG
SET VALUE_CHECK = 'it.eng.knowage.security.ldap.provider.EnhancedLdapSecurityServiceSupplier'
WHERE LABEL = 'SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS.className';
```

Vérifier que la mise à jour a bien été appliquée :

```sql
SELECT LABEL, VALUE_CHECK
FROM SBI_CONFIG
WHERE LABEL = 'SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS.className';
```

> **Important** : ne pas modifier la clé `PORTAL-SECURITY-CLASS.className`, qui doit rester
> à `it.eng.spagobi.security.InternalSecurityInfoProviderImpl`.

---

## 7. Redémarrage et vérification

### 7.1 Redémarrer Knowage

```bash
$CATALINA_HOME/bin/shutdown.sh
$CATALINA_HOME/bin/startup.sh
```

### 7.2 Vérifier le chargement de la configuration

Au démarrage, le log Knowage doit contenir une ligne confirmant le chargement de la propriété :

```
Command line argument: -Denhanced.ldap.config=/etc/knowage/ldap.properties
```

### 7.3 Tester la connectivité LDAP avant le premier login

Depuis le serveur Knowage, vérifier que le compte de service peut se connecter et retrouver un utilisateur :

```bash
ldapsearch -H ldap://ad.mondomaine.fr:389 -x \
  -D "CN=svc-knowage,OU=Service_Accounts,DC=mondomaine,DC=fr" \
  -w motdepasseservice \
  -b "DC=mondomaine,DC=fr" \
  "(sAMAccountName=nom.utilisateur)" \
  dn memberOf displayName
```

La commande doit retourner l'entrée de l'utilisateur avec ses attributs `memberOf`.

### 7.4 Premier login utilisateur

Se connecter à Knowage avec un identifiant Active Directory. Le log doit afficher la séquence suivante :

```
DEBUG checkAuthentication called for userId='nom.utilisateur'
DEBUG Service account bind successful
DEBUG User 'nom.utilisateur' found: CN=...,DC=mondomaine,DC=fr
DEBUG Authentication successful for DN: CN=...
DEBUG LDAP group 'KNW_ADMIN' matched to Knowage role
INFO  Synced LDAP group 'KNW_ADMIN' → Knowage role 'KNW_ADMIN' for user 'nom.utilisateur'
```

Après la connexion, vérifier dans **Administration → Utilisateurs** que :
- Le compte a bien été créé (si `AUTO_CREATE_USER=true`)
- Le rôle correspondant au groupe AD est bien associé à l'utilisateur

---

## 8. Flux d'authentification

```
Utilisateur saisit identifiant + mot de passe
        │
        ▼
[1] Lecture de la configuration LDAP (fichier ou SBI_CONFIG)
        │
        ▼
[2] Liaison du compte de service sur l'annuaire
    → Erreur : tentative avec l'auth interne Knowage (si FALLBACK_TO_INTERNAL=true)
        │
        ▼
[3] Recherche subtree de l'utilisateur par sAMAccountName (ou uid)
    → Non trouvé : accès refusé (ou repli interne)
        │
        ▼
[4] Vérification du mot de passe utilisateur (bind LDAP avec le DN trouvé)
    → Échec : accès refusé
        │
        ▼
[5] Lecture des groupes (attribut memberOf) et filtrage par ACCESS_GROUP_FILTER
    → Aucun groupe autorisé : accès refusé
        │
        ▼
[6] Chargement ou création du compte dans Knowage (SBI_USER)
        │
        ▼
[7] Synchronisation des rôles LDAP → SBI_EXT_USER_ROLES
    (ajout des nouveaux groupes, suppression des groupes révoqués)
        │
        ▼
[8] Construction du profil utilisateur (jeton JWT, rôles, attributs)
        │
        ▼
Accès accordé — session Knowage ouverte
```

---

## 9. Dépannage

### Activer les logs détaillés

Ajouter dans `log4j2.xml` (redémarrage requis) :

```xml
<Logger name="it.eng.knowage.security.ldap" level="DEBUG" additivity="false">
    <AppenderRef ref="KNOWAGE_CORE"/>
</Logger>
```

Les logs apparaissent dans `logs/global/knowage.YYYY-MM-DD.log`.

### Erreurs fréquentes

| Message dans les logs | Cause probable | Action corrective |
|---|---|---|
| `Service account bind failed` | DN ou mot de passe du compte de service incorrect | Vérifier `ADMIN_USER` et `ADMIN_PSW` avec `ldapwhoami` |
| `User 'xxx' not found in LDAP directory` | Utilisateur hors du périmètre de recherche ou mauvais attribut d'identifiant | Vérifier `USER_SEARCH_PATH` et `USER_ID_ATTRIBUTE` |
| `Attribute 'memberOf' not present on user entry` | Le serveur LDAP ne retourne pas `memberOf` | Vérifier que l'overlay `memberOf` est activé (OpenLDAP) ou que l'attribut est accessible |
| `User 'xxx' has no groups matching ACCESS_GROUP_FILTER` | L'utilisateur n'appartient à aucun groupe autorisé | Vérifier la valeur de `ACCESS_GROUP_FILTER` et les memberships dans l'AD |
| `PartialResultException: Unprocessed Continuation Reference` | L'AD retourne des referrals lors de la recherche subtree | Vérifier que `LDAP_REFERRAL=ignore` est bien positionné dans la configuration |
| `LdapSearchException` + `userName/pwd uncorrect` malgré des identifiants corrects | Même cause que ci-dessus : la recherche échoue avant la vérification du mot de passe | Idem |
| `Required LDAP configuration parameter missing: KNOWAGE_LDAP.HOST` | Le fichier de configuration n'est pas chargé | Vérifier la propriété JVM `-Denhanced.ldap.config` au démarrage |
| `Failed to decrypt LDAP admin password` | Clé de chiffrement absente ou incorrecte | Vérifier `-Dsymmetric_encryption_key` |
| Connexion refusée, aucun log LDAP visible | Le provider n'est pas activé | Vérifier `USER-PROFILE-FACTORY-CLASS.className` en base |
| `ClassNotFoundException` au démarrage | Le JAR n'est pas dans `WEB-INF/lib` | Vérifier la présence du JAR dans le répertoire lib de l'application |
| Un utilisateur conserve un rôle après avoir été retiré d'un groupe AD | Version du JAR antérieure à la correction de la révocation de rôle | Mettre à jour vers la dernière version du JAR et reconnecter l'utilisateur |

### Vérifier la configuration active en base

```sql
-- Provider actif
SELECT VALUE_CHECK FROM SBI_CONFIG
WHERE LABEL = 'SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS.className';

-- Paramètres LDAP stockés en base (si Option B utilisée)
SELECT LABEL, VALUE_CHECK FROM SBI_CONFIG
WHERE LABEL LIKE 'KNOWAGE_LDAP.%'
ORDER BY LABEL;
```

### Rétablir l'authentification interne en urgence

Si l'authentification LDAP bloque tous les accès (y compris l'administrateur), rétablir l'authentification interne directement en base :

```sql
UPDATE SBI_CONFIG
SET VALUE_CHECK = 'it.eng.spagobi.security.InternalSecurityServiceSupplierImpl'
WHERE LABEL = 'SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS.className';
```

Redémarrer Knowage. Le compte `biadmin` (authentification interne) sera à nouveau utilisable.

---

## 10. Référence des paramètres

| Paramètre | Obligatoire | Défaut | Description |
|---|---|---|---|
| `HOST` | Oui | — | Nom d'hôte ou IP du serveur LDAP |
| `PORT` | Non | `389` | Port LDAP (389 = standard, 636 = LDAPS) |
| `USE_SSL` | Non | `false` | Activer LDAPS |
| `BASE_DN` | Oui | — | DN racine de l'annuaire, ex. `DC=mondomaine,DC=fr` |
| `ADMIN_USER` | Oui | — | DN complet du compte de service |
| `ADMIN_PSW` | Oui | — | Mot de passe (clair ou `ENC(...)`) |
| `USER_SEARCH_PATH` | Non | *(BASE_DN)* | OU de recherche des utilisateurs (vide = toute l'arborescence) |
| `USER_ID_ATTRIBUTE` | Non | `sAMAccountName` | Attribut de login (`uid` pour OpenLDAP) |
| `USER_OBJECT_CLASS` | Non | `person` | Classe d'objet des utilisateurs |
| `USER_SEARCH_FILTER` | Non | *(généré)* | Filtre LDAP complet (remplace les deux paramètres précédents) |
| `USER_DISPLAYNAME_ATTR` | Non | `displayName` | Attribut utilisé comme nom complet dans Knowage |
| `ROLES_SOURCE` | Non | `LDAP` | `LDAP` ou `KNOWAGE` |
| `USER_MEMBEROF_ATTRIBUTE` | Non | `memberOf` | Attribut de membership sur l'entrée utilisateur |
| `ACCESS_GROUP_FILTER` | Non | *(tous)* | Regex sur le nom des groupes autorisés |
| `AUTO_CREATE_USER` | Non | `true` | Créer le compte Knowage à la première connexion |
| `DEFAULT_ROLE` | Non | *(aucun)* | Rôle assigné lors de la création automatique |
| `DEFAULT_TENANT` | Non | `DEFAULT_TENANT` | Tenant de rattachement pour les comptes créés automatiquement |
| `FALLBACK_TO_INTERNAL` | Non | `true` | Repli sur l'authentification interne si LDAP indisponible |
| `LDAP_REFERRAL` | Non | `ignore` | Politique de referral LDAP : `ignore` (recommandé AD), `follow` (multi-domaine), `throw` (défaut JNDI) |
