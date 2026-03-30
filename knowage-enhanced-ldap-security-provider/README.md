# knowage-enhanced-ldap-security-provider

Enhanced LDAP/Active Directory security provider for Knowage 9.x.

## Why this module?

The existing `knowageldapsecurityprovider` has three critical bugs in multi-OU Active Directory environments:

| Bug | Symptom | Fix |
|-----|---------|-----|
| `bindWithCredentials` corrupts service account DN | Auth fails on first subtree search | Service account bound with exact DN, no manipulation |
| `DN_POSTFIX` conflates search base with DN suffix | Can't search across multiple OUs | Separate `BASE_DN` (search root) from user DN |
| `FullLdapSecurityServiceSupplier` builds DN by concatenation | Fails for users outside a single OU | Replaced by subtree search (`SUBTREE_SCOPE`) |
| No auto-provisioning | Users valid in AD but missing in `SBI_USER` are rejected | `AUTO_CREATE_USER=true` creates users on first login |

## Quick start

```bash
# 1. Build
mvn install -pl knowage-enhanced-ldap-security-provider -am -DskipTests

# 2. Configure
cp src/main/resources/it/eng/knowage/security/ldap/enhanced_ldap.properties.example \
   /etc/knowage/enhanced_ldap.properties
# Edit /etc/knowage/enhanced_ldap.properties

# 3. Start JVM with:
# -Denhanced.ldap.config=/etc/knowage/enhanced_ldap.properties

# 4. Activate (SQL)
# UPDATE SBI_CONFIG SET VALUE_CHECK = 'it.eng.knowage.security.ldap.provider.EnhancedLdapSecurityServiceSupplier'
# WHERE LABEL = 'SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS.className';
```

See [LDAP_SETUP.md](LDAP_SETUP.md) for the complete setup guide.

## Authentication flow

```
checkAuthentication(userId, psw)
  ├── EnhancedLdapConfig.load()          ← SBI_CONFIG → .properties fallback
  ├── connector.bindAsServiceAccount()    ← exact DN, no manipulation
  ├── connector.searchUser(ctx, userId)   ← SUBTREE_SCOPE from BASE_DN
  │     └── returns full DN + attributes
  ├── connector.authenticateUser(dn, psw) ← bind with found DN
  ├── connector.getUserGroups(attrs)      ← memberOf + ACCESS_GROUP_FILTER regex
  ├── loadOrProvisionUser()               ← create in SBI_USER if missing
  └── buildProfile()                      ← JWT token, roles, attributes
```

## Configuration parameters

| Key | Default | Description |
|-----|---------|-------------|
| `KNOWAGE_LDAP.HOST` | (required) | LDAP server hostname |
| `KNOWAGE_LDAP.PORT` | `389` | LDAP port |
| `KNOWAGE_LDAP.USE_SSL` | `false` | Use LDAPS |
| `KNOWAGE_LDAP.BASE_DN` | (required) | Directory root, e.g. `DC=example,DC=com` |
| `KNOWAGE_LDAP.ADMIN_USER` | (required) | Service account full DN |
| `KNOWAGE_LDAP.ADMIN_PSW` | (required) | Service account password (plain or `ENC(...)`) |
| `KNOWAGE_LDAP.USER_ID_ATTRIBUTE` | `sAMAccountName` | Login attribute (`uid` for OpenLDAP) |
| `KNOWAGE_LDAP.USER_SEARCH_PATH` | `` | Search base (empty = `BASE_DN`) |
| `KNOWAGE_LDAP.USER_OBJECT_CLASS` | `person` | User objectClass |
| `KNOWAGE_LDAP.USER_SEARCH_FILTER` | `` | Custom filter (overrides above two) |
| `KNOWAGE_LDAP.USER_DISPLAYNAME_ATTR` | `displayName` | Attribute for full name |
| `KNOWAGE_LDAP.ROLES_SOURCE` | `LDAP` | `LDAP` or `KNOWAGE` |
| `KNOWAGE_LDAP.USER_MEMBEROF_ATTRIBUTE` | `memberOf` | Group membership attribute |
| `KNOWAGE_LDAP.ACCESS_GROUP_FILTER` | `` | Regex for allowed group names |
| `KNOWAGE_LDAP.AUTO_CREATE_USER` | `true` | Create user in `SBI_USER` on first login |
| `KNOWAGE_LDAP.DEFAULT_ROLE` | `` | Role assigned to auto-created users |
| `KNOWAGE_LDAP.DEFAULT_TENANT` | `DEFAULT_TENANT` | Tenant for auto-created users |
| `KNOWAGE_LDAP.FALLBACK_TO_INTERNAL` | `true` | Try internal auth if LDAP fails |

## License

AGPL-3.0 — same as Knowage.
