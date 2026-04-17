import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import java.util.*;

/**
 * Simule le flux complet du KnowageLdapSecurityServiceSupplier :
 * 1. Bind avec le compte de service (DN tel quel)
 * 2. Recherche subtree de l'utilisateur par uid
 * 3. Bind avec le DN trouvé + mot de passe utilisateur
 * 4. Lecture des attributs et groupes
 *
 * Utilise uniquement javax.naming (JNDI) - aucune dépendance externe.
 *
 * Usage : java TestKnowageLdapAuth <ldap_host> <uid> <password>
 * Exemple : java TestKnowageLdapAuth openldap semery password123
 */
public class TestKnowageLdapAuth {

    // Configuration matching the enhanced LDAP provider
    private static final String BASE_DN        = "DC=example,DC=com";
    private static final String SERVICE_DN     = "CN=adaccess,OU=Service_Accounts,DC=example,DC=com";
    private static final String SERVICE_PASS   = "servicepass123";
    private static final String GROUP_FILTER   = "SO_BO_";  // préfixe des groupes autorisés
    private static final String GROUPS_BASE_DN = "OU=Groupes_Knowage,DC=example,DC=com";

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: java TestKnowageLdapAuth <ldap_host> <uid> <password>");
            System.err.println("Ex:    java TestKnowageLdapAuth openldap semery password123");
            System.exit(1);
        }

        String ldapHost = args[0];
        String uid      = args[1];
        String password = args[2];
        String ldapUrl  = "ldap://" + ldapHost + ":389";

        System.out.println("============================================================");
        System.out.println("  TestKnowageLdapAuth - Validation flux JNDI");
        System.out.println("  Serveur : " + ldapUrl);
        System.out.println("  Uid     : " + uid);
        System.out.println("============================================================");

        // --- Étape 1 : Bind avec le compte de service ---
        System.out.println("\n[ETAPE 1] Bind avec le compte de service...");
        System.out.println("  Service DN : " + SERVICE_DN);
        DirContext serviceCtx = bindServiceAccount(ldapUrl, SERVICE_DN, SERVICE_PASS);
        if (serviceCtx == null) {
            System.out.println("  FATAL: Impossible de binder le compte de service. Arrêt.");
            System.exit(1);
        }
        pass("Bind compte de service réussi");

        // --- Étape 2 : Recherche subtree de l'utilisateur ---
        System.out.println("\n[ETAPE 2] Recherche subtree de l'utilisateur uid=" + uid + "...");
        System.out.println("  Base DN : " + BASE_DN);
        System.out.println("  Filtre  : (uid=" + uid + ")");
        String userDN = findUserDistinguishName(serviceCtx, BASE_DN, uid);
        if (userDN == null) {
            fail("Recherche utilisateur uid=" + uid, "DN trouvé", "aucun résultat");
            printSummary();
            System.exit(1);
        }
        pass("Utilisateur trouvé : " + userDN);

        // --- Étape 3 : Bind avec le DN trouvé + mot de passe utilisateur ---
        System.out.println("\n[ETAPE 3] Bind avec le DN trouvé + mot de passe...");
        System.out.println("  User DN  : " + userDN);
        boolean authOk = bindWithCredentials(ldapUrl, userDN, password);
        if (authOk) {
            pass("Authentification réussie pour " + uid);
        } else {
            fail("Authentification " + uid, "bind accepté", "bind refusé (mauvais mot de passe?)");
        }

        // --- Étape 4 : Lecture des attributs de profil ---
        System.out.println("\n[ETAPE 4] Lecture des attributs de profil...");
        readProfileAttributes(serviceCtx, BASE_DN, uid);

        // --- Étape 5 : Lecture des groupes (memberOf) ---
        System.out.println("\n[ETAPE 5] Lecture des groupes (memberOf)...");
        List<String> groups = readMemberOf(serviceCtx, BASE_DN, uid);
        if (!groups.isEmpty()) {
            pass("Groupes trouvés (" + groups.size() + ") : " + groups);
            System.out.println("  Groupes filtrés (préfixe '" + GROUP_FILTER + "') :");
            for (String g : groups) {
                if (g.contains(GROUP_FILTER)) {
                    System.out.println("    [INCLUS]  " + g);
                } else {
                    System.out.println("    [EXCLU]   " + g);
                }
            }
        } else {
            // Fallback : recherche via groupes (member attribute)
            System.out.println("  Pas de memberOf direct, recherche via groupes...");
            List<String> groupsByMember = findGroupsByMember(serviceCtx, GROUPS_BASE_DN, userDN);
            if (!groupsByMember.isEmpty()) {
                pass("Groupes trouvés via member (" + groupsByMember.size() + ") : " + groupsByMember);
            } else {
                fail("Groupes de " + uid, "au moins 1 groupe", "aucun groupe trouvé");
            }
        }

        // --- Étape 8 : Reproduction bug byte[] sur memberOf non-ASCII ---
        System.out.println("\n[ETAPE 8] Lecture memberOf avec valeur non-ASCII (reproduction bug byte[])...");
        testNonAsciiMemberOf(serviceCtx, BASE_DN, uid);

        // --- Étape 9 : extractCnFromDn sur DN non-ASCII ---
        System.out.println("\n[ETAPE 9] Extraction CN depuis un DN contenant des accents...");
        testExtractCnFromNonAsciiDn();

        // --- Test négatif : mauvais mot de passe ---
        System.out.println("\n[ETAPE 6] Test bind avec mauvais mot de passe...");
        boolean badAuth = bindWithCredentials(ldapUrl, userDN, "wrongpassword_XYZ");
        if (!badAuth) {
            pass("Bind refusé avec mauvais mot de passe (attendu)");
        } else {
            fail("Sécurité bind", "bind refusé", "bind accepté avec mauvais mot de passe!");
        }

        // --- Test négatif : utilisateur inexistant ---
        System.out.println("\n[ETAPE 7] Test recherche utilisateur inexistant (fantome)...");
        String ghostDN = findUserDistinguishName(serviceCtx, BASE_DN, "fantome");
        if (ghostDN == null) {
            pass("Utilisateur 'fantome' non trouvé (attendu)");
        } else {
            fail("Utilisateur inexistant", "null", ghostDN);
        }

        serviceCtx.close();
        printSummary();

        if (testsFailed > 0) System.exit(1);
    }

    /**
     * Étape 1 : Bind avec le DN du compte de service (identique à bindServiceAccount du provider).
     * Retourne null si le bind échoue.
     */
    static DirContext bindServiceAccount(String ldapUrl, String serviceDN, String servicePass) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, serviceDN);
        env.put(Context.SECURITY_CREDENTIALS, servicePass);
        env.put("com.sun.jndi.ldap.connect.timeout", "5000");
        try {
            return new InitialDirContext(env);
        } catch (AuthenticationException e) {
            System.out.println("  ERREUR AUTH: " + e.getMessage());
            return null;
        } catch (NamingException e) {
            System.out.println("  ERREUR CONNEXION: " + e.getMessage());
            return null;
        }
    }

    /**
     * Étape 2 : Recherche subtree du DN utilisateur par uid (identique à findUserDistinguishName).
     * Retourne le DN complet ou null si non trouvé.
     */
    static String findUserDistinguishName(DirContext ctx, String baseDN, String uid) {
        try {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(new String[]{"dn"});
            controls.setCountLimit(1);

            String filter = "(uid=" + escapeLdapFilter(uid) + ")";
            NamingEnumeration<SearchResult> results = ctx.search(baseDN, filter, controls);

            if (results.hasMore()) {
                SearchResult result = results.next();
                String dn = result.getNameInNamespace();
                results.close();
                return dn;
            }
            return null;
        } catch (NamingException e) {
            System.out.println("  ERREUR RECHERCHE: " + e.getMessage());
            return null;
        }
    }

    /**
     * Étape 3 : Bind avec le DN trouvé + mot de passe (identique à bindWithCredentials).
     * Retourne true si le bind réussit.
     */
    static boolean bindWithCredentials(String ldapUrl, String userDN, String password) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userDN);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put("com.sun.jndi.ldap.connect.timeout", "5000");
        try {
            DirContext ctx = new InitialDirContext(env);
            ctx.close();
            return true;
        } catch (AuthenticationException e) {
            System.out.println("  Auth refusée : " + e.getMessage());
            return false;
        } catch (NamingException e) {
            System.out.println("  Erreur JNDI : " + e.getMessage());
            return false;
        }
    }

    /**
     * Étape 4 : Lecture des attributs de profil utilisateur.
     */
    static void readProfileAttributes(DirContext ctx, String baseDN, String uid) {
        try {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(new String[]{"cn", "sn", "givenName", "mail", "displayName", "uid"});

            NamingEnumeration<SearchResult> results = ctx.search(baseDN, "(uid=" + escapeLdapFilter(uid) + ")", controls);
            if (results.hasMore()) {
                SearchResult result = results.next();
                Attributes attrs = result.getAttributes();
                String[] expectedAttrs = {"cn", "sn", "givenName", "mail", "displayName"};
                boolean allPresent = true;
                for (String attrName : expectedAttrs) {
                    Attribute attr = attrs.get(attrName);
                    if (attr != null) {
                        System.out.println("  " + attrName + ": " + attr.get());
                    } else {
                        System.out.println("  " + attrName + ": [ABSENT]");
                        allPresent = false;
                    }
                }
                if (allPresent) {
                    pass("Tous les attributs de profil présents");
                } else {
                    fail("Attributs de profil", "cn+sn+givenName+mail+displayName", "certains attributs manquants");
                }
                results.close();
            } else {
                fail("Attributs profil", "utilisateur trouvé", "aucun résultat");
            }
        } catch (NamingException e) {
            fail("Lecture attributs", "succès", e.getMessage());
        }
    }

    /**
     * Étape 5a : Lecture des groupes via l'attribut memberOf (simulé statiquement dans le LDIF).
     */
    static List<String> readMemberOf(DirContext ctx, String baseDN, String uid) {
        List<String> groups = new ArrayList<>();
        try {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(new String[]{"memberOf"});

            NamingEnumeration<SearchResult> results = ctx.search(baseDN, "(uid=" + escapeLdapFilter(uid) + ")", controls);
            if (results.hasMore()) {
                SearchResult result = results.next();
                Attribute memberOf = result.getAttributes().get("memberOf");
                if (memberOf != null) {
                    NamingEnumeration<?> vals = memberOf.getAll();
                    while (vals.hasMore()) {
                        groups.add((String) vals.next());
                    }
                }
                results.close();
            }
        } catch (NamingException e) {
            System.out.println("  Erreur lecture memberOf: " + e.getMessage());
        }
        return groups;
    }

    /**
     * Étape 5b : Fallback - recherche des groupes via l'attribut member des groupes.
     */
    static List<String> findGroupsByMember(DirContext ctx, String groupsBaseDN, String userDN) {
        List<String> groups = new ArrayList<>();
        try {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(new String[]{"cn"});

            String filter = "(&(objectClass=groupOfNames)(member=" + escapeLdapFilter(userDN) + "))";
            NamingEnumeration<SearchResult> results = ctx.search(groupsBaseDN, filter, controls);
            while (results.hasMore()) {
                SearchResult result = results.next();
                Attribute cn = result.getAttributes().get("cn");
                if (cn != null) groups.add((String) cn.get());
            }
            results.close();
        } catch (NamingException e) {
            System.out.println("  Erreur recherche groupes: " + e.getMessage());
        }
        return groups;
    }

    /**
     * Étape 8 : Reproduit le bug du cast (String) sur des valeurs memberOf non-ASCII.
     * JNDI peut retourner byte[] pour un DN contenant des accents.
     * Avant le fix : ClassCastException sur le groupe "Accès Knowage".
     * Après le fix : le groupe est correctement lu.
     */
    static void testNonAsciiMemberOf(DirContext ctx, String baseDN, String uid) {
        try {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(new String[]{"memberOf", "uid"});

            NamingEnumeration<SearchResult> results =
                ctx.search(baseDN, "(uid=" + escapeLdapFilter(uid) + ")", controls);

            if (!results.hasMore()) {
                fail("memberOf non-ASCII", "utilisateur trouvé", "aucun résultat");
                return;
            }

            Attribute memberOf = results.next().getAttributes().get("memberOf");
            results.close();

            if (memberOf == null) {
                fail("memberOf non-ASCII", "attribut memberOf présent", "attribut absent");
                return;
            }

            boolean foundNonAsciiGroup = false;
            boolean hadCastException  = false;

            NamingEnumeration<?> vals = memberOf.getAll();
            while (vals.hasMore()) {
                Object raw = vals.next();
                String groupDn;

                System.out.println("  Valeur memberOf — type Java : " + raw.getClass().getSimpleName());

                if (raw instanceof byte[]) {
                    // Bug reproduced: le provider actuel ferait (String) raw → ClassCastException
                    System.out.println("  [BUG DÉTECTÉ] Valeur retournée en byte[] — cast (String) échouerait!");
                    hadCastException = true;
                    // Décodage correct (ce que le fix devra faire)
                    groupDn = new String((byte[]) raw, java.nio.charset.StandardCharsets.UTF_8);
                } else {
                    groupDn = raw.toString();
                }

                System.out.println("  DN du groupe : " + groupDn);

                // Extraire le CN (premier RDN)
                String cn = groupDn.split(",")[0];
                int eq = cn.indexOf('=');
                String groupName = eq >= 0 ? cn.substring(eq + 1).trim() : cn;
                System.out.println("  CN extrait   : " + groupName);

                if (groupName.contains("Acc")) {
                    foundNonAsciiGroup = true;
                }
            }

            if (foundNonAsciiGroup && hadCastException) {
                pass("Groupe non-ASCII détecté en byte[] — bug reproduit, décodage UTF-8 correct");
            } else if (foundNonAsciiGroup && !hadCastException) {
                pass("Groupe non-ASCII retourné directement en String par JNDI (pas de byte[])");
            } else {
                fail("memberOf non-ASCII", "groupe 'Accès Knowage' trouvé", "groupe absent des memberOf");
            }

        } catch (ClassCastException e) {
            // Ce catch ne sera jamais atteint ici car on utilise Object,
            // mais illustre ce qui se passe dans le provider actuel
            fail("memberOf non-ASCII", "lecture sans exception", "ClassCastException: " + e.getMessage());
        } catch (NamingException e) {
            fail("memberOf non-ASCII", "lecture réussie", e.getMessage());
        }
    }

    /**
     * Étape 9 : Teste l'extraction du CN depuis des DN contenant des accents (RFC 4514).
     * Vérifie les cas nominaux et les cas limites.
     */
    static void testExtractCnFromNonAsciiDn() {
        String[][] cases = {
            // { dn, expectedCn }
            { "CN=SO_BO_ADMIN,OU=Groupes_Knowage,DC=example,DC=com", "SO_BO_ADMIN" },
            { "CN=Accès Knowage,OU=Groupes_Knowage,DC=example,DC=com", "Accès Knowage" },
            { "CN=Répertoire Partagé,OU=Ressources,DC=example,DC=com", "Répertoire Partagé" },
            { "cn=lowercase,dc=example,dc=com", "lowercase" },
        };

        for (String[] c : cases) {
            String dn = c[0];
            String expected = c[1];
            String actual = extractCnFromDn(dn);
            if (expected.equals(actual)) {
                pass("extractCnFromDn '" + dn.split(",")[0] + "' → '" + actual + "'");
            } else {
                fail("extractCnFromDn", expected, actual != null ? actual : "null");
            }
        }
    }

    /** Extrait le CN du premier RDN d'un DN LDAP. Même logique que le provider. */
    static String extractCnFromDn(String dn) {
        if (dn == null || dn.isEmpty()) return null;
        String firstRdn = dn.split(",")[0];
        int eq = firstRdn.indexOf('=');
        if (eq < 0) return null;
        return firstRdn.substring(eq + 1).trim();
    }

    /** Échappe les caractères spéciaux dans un filtre LDAP (RFC 4515). */
    static String escapeLdapFilter(String value) {
        return value
            .replace("\\", "\\5c")
            .replace("*",  "\\2a")
            .replace("(",  "\\28")
            .replace(")",  "\\29")
            .replace("\0", "\\00");
    }

    static void pass(String msg) {
        System.out.println("  [PASS] " + msg);
        testsPassed++;
    }

    static void fail(String test, String expected, String actual) {
        System.out.println("  [FAIL] " + test);
        System.out.println("         Attendu : " + expected);
        System.out.println("         Obtenu  : " + actual);
        testsFailed++;
    }

    static void printSummary() {
        System.out.println("\n============================================================");
        System.out.println("  BILAN : " + testsPassed + " tests passés, " + testsFailed + " échecs");
        System.out.println("============================================================");
    }
}
