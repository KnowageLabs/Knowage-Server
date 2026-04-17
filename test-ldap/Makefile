# ============================================================
# Makefile - Environnement de test LDAP pour Knowage / ACME
# ============================================================

COMPOSE_FILE = ../docker-compose.test-ldap.yml
COMPOSE      = docker compose -f $(COMPOSE_FILE)

.PHONY: up down wait test-shell test-java test-all logs clean

## Démarrer l'environnement Docker Compose
up:
	$(COMPOSE) up -d
	@echo ""
	@echo "Attente du démarrage d'OpenLDAP (15s)..."
	@sleep 15
	@echo "Environnement prêt."
	@echo "  phpLDAPadmin : http://localhost:8090"
	@echo "  Login admin  : CN=admin,DC=example,DC=com / adminpassword"

## Arrêter et supprimer les volumes
down:
	$(COMPOSE) down -v

## Attendre qu'OpenLDAP soit prêt
wait:
	@echo "Vérification de la disponibilité d'OpenLDAP..."
	@$(COMPOSE) exec ldap-test bash -c \
	  'until ldapwhoami -H ldap://openldap:389 -x > /dev/null 2>&1; do echo "En attente..."; sleep 2; done; echo "OpenLDAP prêt."'

## Lancer les tests shell (ldapsearch)
test-shell:
	@echo "=== Tests shell LDAP ==="
	$(COMPOSE) exec ldap-test /tests/run-tests.sh

## Compiler et lancer le test Java (JNDI)
test-java:
	@echo "=== Test Java JNDI ==="
	@echo "--- Compilation ---"
	$(COMPOSE) exec ldap-test bash -c \
	  "cd /tests/test-java-auth && javac TestKnowageLdapAuth.java"
	@echo "--- Scénario 1 : semery (IT, SO_BO_DEV + SO_BO_USER) ---"
	$(COMPOSE) exec ldap-test bash -c \
	  "cd /tests/test-java-auth && java TestKnowageLdapAuth openldap semery password123"
	@echo ""
	@echo "--- Scénario 2 : pdupont (Direction, SO_BO_ADMIN) ---"
	$(COMPOSE) exec ldap-test bash -c \
	  "cd /tests/test-java-auth && java TestKnowageLdapAuth openldap pdupont password123"
	@echo ""
	@echo "--- Scénario 3 : jmartin (Marketing, SO_BO_USER) ---"
	$(COMPOSE) exec ldap-test bash -c \
	  "cd /tests/test-java-auth && java TestKnowageLdapAuth openldap jmartin password123"
	@echo ""
	@echo "--- Scénario 4 : arousseau (Comptabilité, SO_BO_USER) ---"
	$(COMPOSE) exec ldap-test bash -c \
	  "cd /tests/test-java-auth && java TestKnowageLdapAuth openldap arousseau password123"

## Lancer tous les tests
test-all: test-shell test-java

## Afficher les logs OpenLDAP
logs:
	$(COMPOSE) logs -f openldap

## Supprimer les .class compilés
clean:
	rm -f test-java-auth/*.class
