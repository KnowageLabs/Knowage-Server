#!/bin/bash
# ============================================================
# Tests de validation LDAP pour Knowage - ACME
# ============================================================
set -e

LDAP_HOST="${LDAP_HOST:-openldap}"
LDAP_PORT="${LDAP_PORT:-389}"
BASE_DN="DC=example,DC=com"
ADMIN_DN="CN=adaccess,OU=Service_Accounts,DC=example,DC=com"
ADMIN_PSW="servicepass123"

PASS=0
FAIL=0

pass() { echo "  [PASS] $1"; PASS=$((PASS+1)); }
fail() { echo "  [FAIL] $1"; echo "         Attendu : $2"; echo "         Obtenu  : $3"; FAIL=$((FAIL+1)); }

echo "============================================"
echo "  Tests de validation LDAP pour Knowage"
echo "  Serveur : $LDAP_HOST:$LDAP_PORT"
echo "============================================"

# -----------------------------------------------------------
# TEST 1 : Connectivité anonyme
# -----------------------------------------------------------
echo ""
echo "[TEST 1] Connectivité LDAP (ldapwhoami anonyme)..."
if ldapwhoami -H "ldap://$LDAP_HOST:$LDAP_PORT" -x > /dev/null 2>&1; then
    pass "Connexion LDAP établie"
else
    fail "Connexion LDAP" "code retour 0" "erreur de connexion"
    echo "FATAL: Impossible de contacter le serveur LDAP. Arrêt."
    exit 1
fi

# -----------------------------------------------------------
# TEST 2 : Bind du compte de service
# -----------------------------------------------------------
echo ""
echo "[TEST 2] Bind du compte de service (adaccess)..."
result=$(ldapwhoami -H "ldap://$LDAP_HOST:$LDAP_PORT" -x \
    -D "$ADMIN_DN" -w "$ADMIN_PSW" 2>&1)
if echo "$result" | grep -q "dn:"; then
    pass "Bind adaccess réussi : $result"
else
    fail "Bind adaccess" "dn: cn=adaccess,..." "$result"
fi

# -----------------------------------------------------------
# TEST 3 : Recherche subtree - utilisateur dans OU=IT
# -----------------------------------------------------------
echo ""
echo "[TEST 3] Recherche de semery (dans OU=IT)..."
result=$(ldapsearch -H "ldap://$LDAP_HOST:$LDAP_PORT" -x \
    -D "$ADMIN_DN" -w "$ADMIN_PSW" \
    -b "$BASE_DN" "(uid=jdoe)" dn 2>&1)
expected="CN=Jane Doe,OU=IT,OU=Utilisateurs,DC=example,DC=com"
if echo "$result" | grep -qi "CN=Jane Doe"; then
    pass "semery trouvé : $(echo "$result" | grep -i 'dn:' | head -1)"
else
    fail "Recherche semery" "$expected" "$result"
fi

# -----------------------------------------------------------
# TEST 4 : Recherche subtree - utilisateur dans OU=Marketing
# -----------------------------------------------------------
echo ""
echo "[TEST 4] Recherche de jmartin (dans OU=Marketing)..."
result=$(ldapsearch -H "ldap://$LDAP_HOST:$LDAP_PORT" -x \
    -D "$ADMIN_DN" -w "$ADMIN_PSW" \
    -b "$BASE_DN" "(uid=jmartin)" dn 2>&1)
expected="CN=Alice Brown,OU=Marketing,OU=Utilisateurs,DC=example,DC=com"
if echo "$result" | grep -qi "CN=Alice Brown"; then
    pass "jmartin trouvé : $(echo "$result" | grep -i 'dn:' | head -1)"
else
    fail "Recherche jmartin" "$expected" "$result"
fi

# -----------------------------------------------------------
# TEST 5 : Recherche subtree - utilisateur dans OU=Direction
# -----------------------------------------------------------
echo ""
echo "[TEST 5] Recherche de pdupont (dans OU=Direction)..."
result=$(ldapsearch -H "ldap://$LDAP_HOST:$LDAP_PORT" -x \
    -D "$ADMIN_DN" -w "$ADMIN_PSW" \
    -b "$BASE_DN" "(uid=jsmith)" dn 2>&1)
expected="CN=John Smith,OU=Direction,OU=Utilisateurs,DC=example,DC=com"
if echo "$result" | grep -qi "CN=John Smith"; then
    pass "pdupont trouvé : $(echo "$result" | grep -i 'dn:' | head -1)"
else
    fail "Recherche pdupont" "$expected" "$result"
fi

# -----------------------------------------------------------
# TEST 6 : Bind utilisateur avec DN réel (simule auth Knowage)
# -----------------------------------------------------------
echo ""
echo "[TEST 6] Bind utilisateur semery avec son DN réel..."
USER_DN="CN=Jane Doe,OU=IT,OU=Utilisateurs,DC=example,DC=com"
result=$(ldapwhoami -H "ldap://$LDAP_HOST:$LDAP_PORT" -x \
    -D "$USER_DN" -w "password123" 2>&1)
if echo "$result" | grep -q "dn:"; then
    pass "Bind semery réussi"
else
    fail "Bind semery" "dn: cn=sophie emery,..." "$result"
fi

# -----------------------------------------------------------
# TEST 7 : Groupes (memberOf) de semery
# -----------------------------------------------------------
echo ""
echo "[TEST 7] Groupes de semery (memberOf)..."
result=$(ldapsearch -H "ldap://$LDAP_HOST:$LDAP_PORT" -x \
    -D "$ADMIN_DN" -w "$ADMIN_PSW" \
    -b "$BASE_DN" "(uid=jdoe)" memberOf 2>&1)
has_dev=$(echo "$result" | grep -ci "SO_BO_DEV" || true)
has_user=$(echo "$result" | grep -ci "SO_BO_USER" || true)
if [ "$has_dev" -gt 0 ] && [ "$has_user" -gt 0 ]; then
    pass "semery a SO_BO_DEV et SO_BO_USER"
else
    fail "Groupes semery" "SO_BO_DEV + SO_BO_USER" "$result"
fi

# -----------------------------------------------------------
# TEST 8 : Groupes (memberOf) de pdupont
# -----------------------------------------------------------
echo ""
echo "[TEST 8] Groupes de pdupont (memberOf)..."
result=$(ldapsearch -H "ldap://$LDAP_HOST:$LDAP_PORT" -x \
    -D "$ADMIN_DN" -w "$ADMIN_PSW" \
    -b "$BASE_DN" "(uid=jsmith)" memberOf 2>&1)
has_admin=$(echo "$result" | grep -ci "SO_BO_ADMIN" || true)
if [ "$has_admin" -gt 0 ]; then
    pass "pdupont a SO_BO_ADMIN"
else
    fail "Groupes pdupont" "SO_BO_ADMIN" "$result"
fi

# -----------------------------------------------------------
# TEST 9 : Recherche d'un utilisateur inexistant
# -----------------------------------------------------------
echo ""
echo "[TEST 9] Recherche d'un utilisateur inexistant (fantome)..."
result=$(ldapsearch -H "ldap://$LDAP_HOST:$LDAP_PORT" -x \
    -D "$ADMIN_DN" -w "$ADMIN_PSW" \
    -b "$BASE_DN" "(uid=fantome)" dn 2>&1)
count=$(echo "$result" | grep -c "^numEntries:" 2>/dev/null || echo "0")
if echo "$result" | grep -q "numEntries: 0" || ! echo "$result" | grep -q "dn:"; then
    pass "Aucun résultat pour 'fantome' (attendu)"
else
    fail "Utilisateur inexistant" "0 résultat" "$result"
fi

# -----------------------------------------------------------
# TEST 10 : Bind avec mauvais mot de passe
# -----------------------------------------------------------
echo ""
echo "[TEST 10] Bind avec mauvais mot de passe..."
USER_DN="CN=Jane Doe,OU=IT,OU=Utilisateurs,DC=example,DC=com"
result=$(ldapwhoami -H "ldap://$LDAP_HOST:$LDAP_PORT" -x \
    -D "$USER_DN" -w "wrongpassword" 2>&1; echo "EXIT:$?")
exit_code=$(echo "$result" | grep "EXIT:" | cut -d: -f2)
if [ "$exit_code" != "0" ]; then
    pass "Bind avec mauvais mot de passe refusé (code: $exit_code)"
else
    fail "Mauvais mot de passe" "code retour non-zéro (49)" "bind accepté"
fi

# -----------------------------------------------------------
# TEST 11 : Listing des groupes matchant SO_BO_*
# -----------------------------------------------------------
echo ""
echo "[TEST 11] Listing des groupes matchant SO_BO_* dans OU=Groupes_Knowage..."
result=$(ldapsearch -H "ldap://$LDAP_HOST:$LDAP_PORT" -x \
    -D "$ADMIN_DN" -w "$ADMIN_PSW" \
    -b "OU=Groupes_Knowage,$BASE_DN" "(objectClass=groupOfNames)" cn 2>&1)
has_admin=$(echo "$result" | grep -c "cn: SO_BO_ADMIN" || true)
has_dev=$(echo "$result" | grep -c "cn: SO_BO_DEV" || true)
has_user=$(echo "$result" | grep -c "cn: SO_BO_USER" || true)
has_restricted=$(echo "$result" | grep -c "cn: SO_BO_RESTRICTED" || true)
if [ "$has_admin" -gt 0 ] && [ "$has_dev" -gt 0 ] && [ "$has_user" -gt 0 ] && [ "$has_restricted" -gt 0 ]; then
    pass "4 groupes SO_BO_* trouvés (ADMIN, DEV, USER, RESTRICTED)"
else
    fail "Groupes SO_BO_*" "4 groupes (ADMIN, DEV, USER, RESTRICTED)" "$result"
fi

# -----------------------------------------------------------
# TEST 12 : Groupes hors périmètre (Groupes_Autres)
# -----------------------------------------------------------
echo ""
echo "[TEST 12] Groupes hors périmètre (OU=Groupes_Autres)..."
result=$(ldapsearch -H "ldap://$LDAP_HOST:$LDAP_PORT" -x \
    -D "$ADMIN_DN" -w "$ADMIN_PSW" \
    -b "OU=Groupes_Autres,$BASE_DN" "(objectClass=groupOfNames)" cn 2>&1)
has_vpn=$(echo "$result" | grep -c "cn: GRP_VPN_USERS" || true)
if [ "$has_vpn" -gt 0 ]; then
    pass "GRP_VPN_USERS présent dans Groupes_Autres (ne doit PAS être importé par le provider)"
else
    fail "Groupes hors périmètre" "GRP_VPN_USERS dans Groupes_Autres" "$result"
fi

# -----------------------------------------------------------
# TEST 13 : Attributs de profil de semery
# -----------------------------------------------------------
echo ""
echo "[TEST 13] Attributs de profil de semery..."
result=$(ldapsearch -H "ldap://$LDAP_HOST:$LDAP_PORT" -x \
    -D "$ADMIN_DN" -w "$ADMIN_PSW" \
    -b "$BASE_DN" "(uid=jdoe)" cn sn givenName mail displayName 2>&1)
has_cn=$(echo "$result" | grep -c "^cn:" || true)
has_sn=$(echo "$result" | grep -c "^sn:" || true)
has_given=$(echo "$result" | grep -c "^givenName:" || true)
has_mail=$(echo "$result" | grep -c "^mail:" || true)
has_display=$(echo "$result" | grep -c "^displayName:" || true)
if [ "$has_cn" -gt 0 ] && [ "$has_sn" -gt 0 ] && [ "$has_given" -gt 0 ] && [ "$has_mail" -gt 0 ] && [ "$has_display" -gt 0 ]; then
    pass "Tous les attributs de profil présents (cn, sn, givenName, mail, displayName)"
else
    fail "Attributs de profil" "cn + sn + givenName + mail + displayName" "$result"
fi

# -----------------------------------------------------------
# Bilan
# -----------------------------------------------------------
echo ""
echo "============================================"
echo "  BILAN : $PASS tests passés, $FAIL échecs"
echo "============================================"

if [ "$FAIL" -gt 0 ]; then
    exit 1
fi
