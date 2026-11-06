package it.eng.spagobi.security.saml;

import static org.junit.Assert.*;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;

public class LocalUserProfileLoaderTest {
	@Test
	public void loadsOnlyExistingLocalRolesAttributesAndPrivileges() {
		SbiUser user = user("reader", "tenant");
		SpagoBIUserProfile profile = LocalUserProfileLoader.load("reader", dao(user,
				new ArrayList<>(Arrays.asList(role("user", "tenant"), role("analyst", "tenant"))),
				attribute("department", "sales"), attribute("email", "reader@example.org"),
				attribute("kn_roles", "admin"), attribute("kn_is_super_admin", "true"),
				attribute("kn_saml_profile", "pm"), attribute("user_id", "biadmin"), attribute("exp", "9999999999")));
		assertEquals("reader", profile.getUserId());
		assertEquals("Local reader", profile.getUserName());
		assertEquals("tenant", profile.getOrganization());
		assertArrayEquals(new String[] { "user", "analyst" }, profile.getRoles());
		assertFalse(profile.getIsSuperadmin());
		assertEquals(2, profile.getAttributes().size());
		assertEquals("sales", profile.getAttributes().get("department"));
		assertEquals("reader@example.org", profile.getAttributes().get("email"));

		user.setIsSuperadmin(true);
		assertTrue(LocalUserProfileLoader.load("reader",
				dao(user, new ArrayList<>(Arrays.asList(role("admin", "tenant"))))).getIsSuperadmin());
	}

	@Test
	public void resolvesEachAccountsOwnTenantBeforeLoadingItsRolesAndAttributes() {
		for (String tenant : new String[] { "tenant", "another-tenant" }) {
			SbiUser user = user("reader", tenant);
			SpagoBIUserProfile profile = LocalUserProfileLoader.load("reader",
					dao(user, new ArrayList<>(Arrays.asList(role("user", tenant))), attribute("department", tenant)));
			assertEquals(tenant, profile.getOrganization());
			assertArrayEquals(new String[] { "user" }, profile.getRoles());
			assertEquals(tenant, profile.getAttributes().get("department"));
		}
	}

	@Test
	public void rejectsMissingMismatchedBlockedAndTenantlessAccounts() {
		ArrayList<SbiExtRoles> roles = new ArrayList<>(Arrays.asList(role("user", "tenant")));
		assertThrows(IllegalArgumentException.class, () -> LocalUserProfileLoader.load("reader", dao(null, roles)));
		assertThrows(IllegalArgumentException.class, () -> LocalUserProfileLoader.load("reader", dao(user("other", "tenant"), roles)));
		assertThrows(IllegalArgumentException.class, () -> LocalUserProfileLoader.load("reader", dao(user("Reader", "tenant"), roles)));
		for (String tenant : new String[] { null, "", " " }) {
			assertThrows(IllegalArgumentException.class, () -> LocalUserProfileLoader.load("reader", dao(user("reader", tenant), roles)));
		}
		SbiUser blocked = user("reader", "tenant");
		blocked.setFlgPwdBlocked(true);
		assertThrows(IllegalArgumentException.class, () -> LocalUserProfileLoader.load("reader", dao(blocked, roles)));
		assertThrows(IllegalArgumentException.class, () -> LocalUserProfileLoader.load("", dao(blocked, roles)));
	}

	@Test
	public void rejectsRolelessAccountsAndCrossTenantAssignmentsWithoutFallback() {
		SbiUser user = user("reader", "tenant");
		assertThrows(IllegalArgumentException.class, () -> LocalUserProfileLoader.load("reader", dao(user, new ArrayList<>())));
		assertThrows(IllegalArgumentException.class, () -> LocalUserProfileLoader.load("reader",
				dao(user, new ArrayList<>(Arrays.asList(role("admin", "other"))))));
	}

	private SbiUser user(String userId, String tenant) {
		SbiUser user = new SbiUser(7, userId);
		user.setFullName("Local " + userId);
		user.getCommonInfo().setOrganization(tenant);
		user.setIsSuperadmin(false);
		return user;
	}

	private SbiExtRoles role(String name, String tenant) {
		SbiExtRoles role = new SbiExtRoles();
		role.setName(name);
		role.getCommonInfo().setOrganization(tenant);
		return role;
	}

	private SbiUserAttributes attribute(String name, String value) {
		SbiAttribute definition = new SbiAttribute();
		definition.setAttributeName(name);
		SbiUserAttributes attribute = new SbiUserAttributes();
		attribute.setSbiAttribute(definition);
		attribute.setAttributeValue(value);
		return attribute;
	}

	private ISbiUserDAO dao(SbiUser user, ArrayList<SbiExtRoles> roles, SbiUserAttributes... attributes) {
		boolean[] tenantSelected = { false };
		return (ISbiUserDAO) Proxy.newProxyInstance(ISbiUserDAO.class.getClassLoader(), new Class<?>[] { ISbiUserDAO.class },
				(proxy, method, args) -> {
					switch (method.getName()) {
					case "setTenant":
						assertEquals(user.getCommonInfo().getOrganization(), args[0]);
						tenantSelected[0] = true;
						return null;
					case "loadSbiUserByUserId":
						assertFalse(tenantSelected[0]);
						assertEquals("reader", args[0]);
						return user;
					case "loadSbiUserRolesById":
						assertTrue(tenantSelected[0]);
						assertEquals(7, args[0]);
						return roles;
					case "loadSbiUserAttributesById":
						assertTrue(tenantSelected[0]);
						assertEquals(7, args[0]);
						return new ArrayList<>(Arrays.asList(attributes));
					default:
						throw new AssertionError("Unexpected DAO operation: " + method.getName());
					}
				});
	}
}
