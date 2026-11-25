package it.eng.spagobi.tools.datasource.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasource;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;

public class DataSourceTenantAuthorizationTest {

	@Test
	public void owningAndSharingTenantsCanReadButUnrelatedTenantCannot() throws Exception {
		Fixture owner = new Fixture("TENANT_A", false, "TENANT_A", "TENANT_B");
		assertEquals(2, owner.dao.loadDataSourceByID(2).getDsId());
		Fixture sharing = new Fixture("TENANT_B", false, "TENANT_A", "TENANT_B");
		assertEquals(2, sharing.dao.loadDataSourceByID(2).getDsId());

		Fixture unrelated = new Fixture("TENANT_C", false, "TENANT_A", "TENANT_B");
		expectNotFound(() -> unrelated.dao.loadDataSourceByID(2));
		assertTrue(unrelated.rolledBack);
		assertTrue(unrelated.closed);
	}

	@Test
	public void missingIdAndMissingTenantFailClosed() throws Exception {
		Fixture fixture = new Fixture("TENANT_A", false, "TENANT_A");
		expectNotFound(() -> fixture.dao.loadDataSourceByID(99));
		Fixture noTenant = new Fixture(null, false, "TENANT_A");
		expectNotFound(() -> noTenant.dao.loadDataSourceByID(2));
	}

	@Test
	public void sharingDoesNotGrantManagementOrCredentialReuse() throws Exception {
		Fixture sharing = new Fixture("TENANT_B", false, "TENANT_A", "TENANT_B");
		expectNotFound(() -> sharing.dao.loadDataSourceForModification(2));
		expectNotFound(() -> sharing.dao.modifyDataSource(payload()));
		expectNotFound(() -> sharing.dao.eraseDataSource(payload()));
		expectNotFound(() -> sharing.dao.setCurrentPassword(payload()));
		expectNotFound(() -> sharing.dao.returnEntitiesAssociated(2));
		assertEquals(0, sharing.updated);
		assertEquals(0, sharing.deleted.size());
		assertFalse(sharing.filterDisabled);
	}

	@Test
	public void unrelatedTenantCannotWriteEvenWithForgedOwner() throws Exception {
		Fixture unrelated = new Fixture("TENANT_B", false, "TENANT_A");
		IDataSource forged = payload();
		forged.setOwner("admin");
		expectNotFound(() -> unrelated.dao.modifyDataSource(forged));
		expectNotFound(() -> unrelated.dao.eraseDataSource(forged));
		assertEquals(0, unrelated.updated);
		assertEquals(0, unrelated.deleted.size());
	}

	@Test
	public void owningTenantCanUpdateSharedGlobalConfiguration() throws Exception {
		Fixture owner = new Fixture("TENANT_A", false, "TENANT_A", "TENANT_B");
		assertEquals(2, owner.dao.loadDataSourceForModification(2).getDsId());
		owner.dao.modifyDataSource(payload());
		assertEquals("new description", owner.stored.getDescr());
		assertEquals("TENANT_A", owner.stored.getCommonInfo().getOrganization());
		assertEquals(1, owner.updated);
		assertTrue(owner.committed);
	}

	@Test
	public void authorizedDeleteRemovesAssociationsAcrossAllSharingTenants() throws Exception {
		Fixture owner = new Fixture("TENANT_A", false, "TENANT_A", "TENANT_B");
		owner.dao.eraseDataSource(payload());
		assertEquals(3, owner.deleted.size());
		assertTrue(owner.deleted.contains(owner.stored));
		assertTrue(owner.filterDisabled);
		assertTrue(owner.committed);
	}

	@Test
	public void deletionChecksAlsoFindDocumentsInSharingTenants() throws Exception {
		Fixture owner = new Fixture("TENANT_A", false, "TENANT_A", "TENANT_B");
		owner.foreignDocument = true;
		assertEquals(Collections.emptyList(), owner.dao.returnEntitiesAssociated(2).get("sbi.datasource.usedby.biobject"));
		assertTrue(owner.filterDisabled);
		assertEquals(0, owner.deleted.size());
	}

	@Test
	public void superadminCanManageDatasourceWithoutTenantAssociation() throws Exception {
		Fixture admin = new Fixture("TENANT_C", true, "TENANT_A", "TENANT_B");
		assertEquals(2, admin.dao.loadDataSourceByID(2).getDsId());
		assertEquals(2, admin.dao.loadDataSourceForModification(2).getDsId());
		admin.dao.modifyDataSource(payload());
		assertEquals(1, admin.updated);
		admin.dao.eraseDataSource(payload());
		assertTrue(admin.deleted.contains(admin.stored));
	}

	@Test
	public void cacheManagementUsesPersistedFlagNotPayload() throws Exception {
		Fixture owner = new Fixture("TENANT_A", false, "TENANT_A");
		owner.stored.setWriteDefault(true);
		expectNotFound(() -> owner.dao.modifyDataSource(payload()));
		expectNotFound(() -> owner.dao.eraseDataSource(payload()));
		Fixture admin = new Fixture("TENANT_A", true, "TENANT_A");
		admin.stored.setWriteDefault(true);
		assertEquals(2, admin.dao.loadDataSourceForModification(2).getDsId());
	}

	private static IDataSource payload() {
		IDataSource source = DataSourceFactory.getDataSource();
		source.setDsId(2);
		source.setLabel("shared");
		source.setDescr("new description");
		source.setDialectName("dialect");
		source.setJndi("java:comp/env/jdbc/shared");
		return source;
	}

	private static void expectNotFound(CheckedAction action) throws Exception {
		try {
			action.run();
			fail("Expected inaccessible and nonexistent datasources to be indistinguishable");
		} catch (DataSourceNotFoundException expected) {
			assertEquals("Data source not found", expected.getMessage());
		}
	}

	private interface CheckedAction {
		void run() throws Exception;
	}

	private static class Fixture {
		private final SbiDataSource stored = new SbiDataSource(2);
		private final SbiDomains dialect = new SbiDomains();
		private final Set<String> tenants;
		private final List<Object> deleted = new ArrayList<>();
		private final DataSourceDAOHibImpl dao;
		private boolean committed;
		private boolean rolledBack;
		private boolean closed;
		private boolean filterDisabled;
		private boolean foreignDocument;
		private int updated;

		private Fixture(String tenant, boolean superadmin, String... associatedTenants) {
			tenants = new HashSet<>(Arrays.asList(associatedTenants));
			stored.getCommonInfo().setOrganization("TENANT_A");
			stored.getCommonInfo().setUserIn("creator");
			stored.setLabel("shared");
			stored.setJndi("java:comp/env/jdbc/shared");
			stored.setWriteDefault(false);
			dialect.setValueCd("dialect");
			stored.setDialect(dialect);
			Transaction transaction = (Transaction) Proxy.newProxyInstance(Transaction.class.getClassLoader(), new Class<?>[] { Transaction.class },
					(proxy, method, args) -> {
						switch (method.getName()) {
						case "commit":
							committed = true;
							return null;
						case "rollback":
							rolledBack = true;
							return null;
						case "isActive":
							return true;
						default:
							throw new AssertionError("Unexpected transaction call: " + method.getName());
						}
					});
			Session session = (Session) Proxy.newProxyInstance(Session.class.getClassLoader(), new Class<?>[] { Session.class },
					(proxy, method, args) -> {
						switch (method.getName()) {
						case "beginTransaction":
							return transaction;
						case "createCriteria":
							return criteria((Class<?>) args[0]);
						case "disableFilter":
							filterDisabled = true;
							return null;
						case "createQuery":
							assertTrue(filterDisabled);
							return query((String) args[0]);
						case "update":
							updated++;
							return null;
						case "delete":
							deleted.add(args[0]);
							return null;
						case "flush":
							return null;
						case "isOpen":
							return true;
						case "close":
							closed = true;
							return null;
						default:
							throw new AssertionError("Unexpected session call: " + method.getName());
						}
					});
			dao = new DataSourceDAOHibImpl() {
				@Override
				public Session getSession() {
					return session;
				}
			};
			UserProfile profile = new UserProfile("admin", tenant);
			profile.setIsSuperadmin(superadmin);
			dao.setUserProfile(profile);
		}

		private Criteria criteria(Class<?> entity) {
			List<String> restrictions = new ArrayList<>();
			return (Criteria) Proxy.newProxyInstance(Criteria.class.getClassLoader(), new Class<?>[] { Criteria.class },
					(proxy, method, args) -> {
						switch (method.getName()) {
						case "add":
							restrictions.add(args[0].toString());
							return proxy;
						case "createAlias":
							return proxy;
						case "uniqueResult":
							if (entity == SbiDomains.class) {
								return dialect;
							}
							assertEquals(SbiDataSource.class, entity);
							for (String restriction : restrictions) {
								String[] parts = restriction.split("=", 2);
								switch (parts[0]) {
								case "dsId":
									if (!"2".equals(parts[1])) {
										return null;
									}
									break;
								case "commonInfo.organization":
									if (!"TENANT_A".equals(parts[1])) {
										return null;
									}
									break;
								case "organization.name":
									if (!tenants.contains(parts[1]) || (!filterDisabled && !"TENANT_A".equals(parts[1]))) {
										return null;
									}
									break;
								default:
									throw new AssertionError("Unexpected restriction: " + restriction);
								}
							}
							return stored;
						default:
							throw new AssertionError("Unexpected criteria call: " + method.getName());
						}
					});
		}

		private Query query(String hql) {
			return (Query) Proxy.newProxyInstance(Query.class.getClassLoader(), new Class<?>[] { Query.class },
					(proxy, method, args) -> {
						if (method.getName().startsWith("set")) {
							return proxy;
						}
						if ("list".equals(method.getName())) {
							if (hql.contains("SbiOrganizationDatasource")) {
								ArrayList<SbiOrganizationDatasource> associations = new ArrayList<>();
								for (String tenant : tenants) {
									associations.add(new SbiOrganizationDatasource());
								}
								return associations;
							}
							if (hql.contains("SbiObjects") && foreignDocument) {
								SbiObjects document = new SbiObjects();
								document.setName("document in tenant B");
								document.getCommonInfo().setOrganization("TENANT_B");
								return Arrays.asList(document);
							}
							return new ArrayList<>();
						}
						throw new AssertionError("Unexpected query call: " + method.getName());
					});
		}
	}
}
