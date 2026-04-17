# Knowage Enhanced LDAP Security Provider — Setup Guide

## 1. Prerequisites

- Knowage 9.x (tested on 9.1.0-SNAPSHOT)
- JDK 17+
- An LDAP/Active Directory server accessible from the Knowage host
- A service account DN with read access to the user and group subtrees

---

## 2. Build

```bash
# From the Knowage-Server root
mvn install -pl knowage-enhanced-ldap-security-provider -am -DskipTests
```

The JAR will be at:
```
knowage-enhanced-ldap-security-provider/target/knowage-enhanced-ldap-security-provider-9.1.0-SNAPSHOT.jar
```

---

## 3. Deployment

Copy the JAR to the Knowage webapp lib directory:

```bash
cp knowage-enhanced-ldap-security-provider/target/knowage-enhanced-ldap-security-provider-9.1.0-SNAPSHOT.jar \
   /path/to/tomcat/webapps/knowage/WEB-INF/lib/
```

---

## 4. Configuration

### Option A — Properties file (recommended for quick setup)

1. Copy the example file:
   ```bash
   cp knowage-enhanced-ldap-security-provider/src/main/resources/it/eng/knowage/security/ldap/enhanced_ldap.properties.example \
      /etc/knowage/enhanced_ldap.properties
   ```

2. Edit `/etc/knowage/enhanced_ldap.properties` with your LDAP parameters.

3. Add the system property to Tomcat's `setenv.sh`:
   ```bash
   JAVA_OPTS="$JAVA_OPTS -Denhanced.ldap.config=/etc/knowage/enhanced_ldap.properties"
   ```

### Option B — SBI_CONFIG table (recommended for production)

Insert each parameter into `SBI_CONFIG` with label prefix `KNOWAGE_LDAP.`:

```sql
-- Minimum required keys:
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.HOST',       'LDAP Host',       'ad.example.com',                     1);
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.PORT',       'LDAP Port',       '389',                              1);
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.USE_SSL',    'LDAP Use SSL',    'false',                            1);
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.BASE_DN',    'LDAP Base DN',    'DC=example,DC=com',                  1);
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.ADMIN_USER', 'LDAP Admin DN',
        'CN=adaccess,OU=Service_Accounts,DC=example,DC=com',                         1);
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.ADMIN_PSW',  'LDAP Admin Password', 'servicepass123',               1);

-- User search:
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.USER_ID_ATTRIBUTE', 'LDAP User ID Attr', 'sAMAccountName',          1);
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.USER_OBJECT_CLASS', 'LDAP User Object Class', 'person',             1);
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.USER_DISPLAYNAME_ATTR', 'Display Name Attr', 'displayName',         1);

-- Groups:
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.ROLES_SOURCE',     'Roles Source',     'LDAP',                      1);
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.USER_MEMBEROF_ATTRIBUTE', 'MemberOf Attr', 'memberOf',              1);
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.ACCESS_GROUP_FILTER', 'Group Filter Regex', 'SO_BO_.*',             1);

-- Provisioning:
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.AUTO_CREATE_USER', 'Auto Create User', 'true',                      1);
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.DEFAULT_TENANT',   'Default Tenant',   'DEFAULT_TENANT',            1);
INSERT INTO SBI_CONFIG (LABEL, NAME, VALUE_CHECK, IS_ACTIVE)
VALUES ('KNOWAGE_LDAP.FALLBACK_TO_INTERNAL', 'Fallback Internal', 'true',                 1);
```

SBI_CONFIG entries take **priority** over the properties file.

### Password encryption (optional)

To store the service account password encrypted:

1. Set the encryption key at JVM startup:
   ```bash
   JAVA_OPTS="$JAVA_OPTS -Dsymmetric_encryption_key=<your-secret-key>"
   ```

2. Encrypt the password using Knowage's built-in tool (or `EncryptionPBEWithMD5AndDES`):
   ```java
   String encrypted = EncryptionPBEWithMD5AndDES.getInstance().encrypt("servicepass123");
   // e.g. "X7h3aB+kQ..."
   ```

3. Store with `ENC(...)` wrapper:
   ```properties
   ADMIN_PSW=ENC(X7h3aB+kQ...)
   ```

---

## 5. Activate the provider

In the Knowage administration console, or directly in SBI_CONFIG:

```sql
-- This is the key used by SecurityServiceSupplierFactory (login flow)
UPDATE SBI_CONFIG
SET VALUE_CHECK = 'it.eng.knowage.security.ldap.provider.EnhancedLdapSecurityServiceSupplier'
WHERE LABEL = 'SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS.className';
```

> **Note**: `PORTAL-SECURITY-CLASS.className` is a different key used by `SecurityInfoProviderFactory`
> (interface `ISecurityInfoProvider`) and must stay set to `it.eng.spagobi.security.InternalSecurityInfoProviderImpl`.

Restart Knowage/Tomcat.

---

## 6. Adding the module to the parent POM (developers only)

Add the following line to `knowage-ce-parent/pom.xml` in the `<modules>` section,
**after** `knowageldapsecurityprovider`:

```xml
<module>../knowage-enhanced-ldap-security-provider</module>
```

---

## 7. Example: Active Directory (ACME)

```properties
HOST=ad.example.com
PORT=389
USE_SSL=false
BASE_DN=DC=example,DC=com
ADMIN_USER=CN=adaccess,OU=Service_Accounts,DC=example,DC=com
ADMIN_PSW=servicepass123
USER_SEARCH_PATH=
USER_ID_ATTRIBUTE=sAMAccountName
USER_OBJECT_CLASS=person
USER_DISPLAYNAME_ATTR=displayName
ROLES_SOURCE=LDAP
USER_MEMBEROF_ATTRIBUTE=memberOf
GROUP_SEARCH_PATH=OU=Groupes_Knowage,DC=example,DC=com
ACCESS_GROUP_FILTER=SO_BO_.*
AUTO_CREATE_USER=true
DEFAULT_ROLE=
DEFAULT_TENANT=DEFAULT_TENANT
FALLBACK_TO_INTERNAL=true
```

Authentication flow:
1. `adaccess` binds to `DC=example,DC=com`
2. Subtree search for `(sAMAccountName=pdupont)`
3. Returns `CN=Pierre Dupont,OU=Direction,OU=Utilisateurs,DC=example,DC=com`
4. Bind with that DN + user password
5. Read `memberOf` → `CN=SO_BO_ADMIN,...` → matches `SO_BO_.*` → role: `SO_BO_ADMIN`
6. User `pdupont` created in `SBI_USER` if not present

---

## 8. Example: OpenLDAP (Docker test)

```properties
HOST=openldap
PORT=389
USE_SSL=false
BASE_DN=DC=example,DC=com
ADMIN_USER=CN=adaccess,OU=Service_Accounts,DC=example,DC=com
ADMIN_PSW=servicepass123
USER_ID_ATTRIBUTE=uid
USER_OBJECT_CLASS=inetOrgPerson
USER_DISPLAYNAME_ATTR=displayName
ROLES_SOURCE=LDAP
USER_MEMBEROF_ATTRIBUTE=memberOf
GROUP_SEARCH_PATH=OU=Groupes_Knowage,DC=example,DC=com
GROUP_OBJECT_CLASS=groupOfNames
GROUP_ID_ATTRIBUTE=cn
ACCESS_GROUP_FILTER=SO_BO_.*
AUTO_CREATE_USER=true
DEFAULT_TENANT=DEFAULT_TENANT
FALLBACK_TO_INTERNAL=true
```

---

## 9. Troubleshooting

### Enable DEBUG logging

Add to `log4j2.xml` (or your logging config):
```xml
<Logger name="it.eng.knowage.security.ldap" level="DEBUG" additivity="false">
    <AppenderRef ref="Console"/>
</Logger>
```

### Common errors

| Error | Cause | Fix |
|-------|-------|-----|
| `LdapConnectionException: Service account bind failed` | Wrong `ADMIN_USER` DN or `ADMIN_PSW` | Verify with `ldapwhoami -D "..." -w "..."` |
| `User 'xxx' not found in LDAP directory` | User not in search scope, wrong `USER_ID_ATTRIBUTE` | Check `USER_SEARCH_PATH` and `USER_ID_ATTRIBUTE` (AD: `sAMAccountName`, OpenLDAP: `uid`) |
| `User 'xxx' has no groups matching ACCESS_GROUP_FILTER` | User not member of any matching group | Check `memberOf` values and regex |
| `Required LDAP configuration parameter missing: KNOWAGE_LDAP.HOST` | Config not loaded | Check system property or SBI_CONFIG |
| `Failed to decrypt LDAP admin password` | Missing `symmetric_encryption_key` | Add `-Dsymmetric_encryption_key=...` to JVM |
| `err=32 No such object` (OpenLDAP) | Service account lacks read ACL | Update OpenLDAP ACL: `by users read` |

### Verify connectivity manually

```bash
# Test service account bind + user search
ldapsearch -H ldap://HOST:389 -x \
  -D "CN=adaccess,OU=Service_Accounts,DC=example,DC=com" \
  -w servicepass123 \
  -b "DC=example,DC=com" "(uid=semery)" dn memberOf
```

### Test with Docker environment

```bash
cd knowage-enhanced-ldap-security-provider/..
docker compose -f docker-compose.test-ldap.yml up -d
cd test-ldap && make test-all
```
