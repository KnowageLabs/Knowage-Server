/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.wapp.bo.Homepage;
import it.eng.spagobi.wapp.bo.HomepageTemplate;
import it.eng.spagobi.wapp.bo.HomepageType;
import it.eng.spagobi.wapp.bo.Menu;
import it.eng.spagobi.wapp.bo.MenuPlaceholder;
import it.eng.spagobi.wapp.dao.IHomepageDAO;
import it.eng.spagobi.wapp.util.MenuUtilities;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class HomepageResourceTest {

	@After
	public void cleanUp() {
		UserProfileManager.unset();
	}

	@Test
	public void shouldPreviewDefaultHomepageWhenRoleIdIsDefault() {
		Homepage defaultHomepage = dynamicHomepage(true, 10, 20);
		final int[] loadDefaultCalls = new int[1];

		mockDaos(createProxy(IHomepageDAO.class, (proxy, method, args) -> {
			if ("loadDefaultHomepage".equals(method.getName())) {
				loadDefaultCalls[0]++;
				return defaultHomepage;
			}
			return defaultValue(method.getReturnType());
		}), null);

		Response response = new HomepageResource().previewHomepageByRole("default");

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertSame(defaultHomepage, response.getEntity());
		assertEquals(1, loadDefaultCalls[0]);
		assertEquals(Arrays.asList(10, 20), defaultHomepage.getTemplate().getMenuPlaceholders().get(0).getMenuIds());
	}

	@Test
	public void shouldLoadDefaultHomepageWhenDefaultRoleAliasIsRequested() {
		Homepage defaultHomepage = staticHomepage(true, Collections.emptyList(), "landing.html");
		final int[] loadDefaultCalls = new int[1];

		mockDaos(createProxy(IHomepageDAO.class, (proxy, method, args) -> {
			if ("loadDefaultHomepage".equals(method.getName())) {
				loadDefaultCalls[0]++;
				return defaultHomepage;
			}
			return defaultValue(method.getReturnType());
		}), null);

		Response response = new HomepageResource().getHomepageByRole("default");

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertSame(defaultHomepage, response.getEntity());
		assertEquals(1, loadDefaultCalls[0]);
	}

	@Test
	public void shouldNormalizeDefaultRoleIdInCreateHomepageRequest() throws JSONException {
		Homepage[] savedHomepage = new Homepage[1];
		mockRequestBody(new JSONObject()
				.put("type", HomepageType.STATIC.getValue())
				.put("staticPage", "landing.html")
				.put("roleId", "default"));

		mockDaos(createProxy(IHomepageDAO.class, (proxy, method, args) -> {
			if ("saveHomepage".equals(method.getName())) {
				savedHomepage[0] = (Homepage) args[0];
				return args[0];
			}
			return defaultValue(method.getReturnType());
		}), null);

		Response response = new HomepageResource().createHomepage(null);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertTrue(savedHomepage[0].isDefaultHomepage());
		assertTrue(savedHomepage[0].getRoleIds().isEmpty());
		assertSame(savedHomepage[0], response.getEntity());
	}

	@Test
	public void shouldNormalizeDefaultRoleIdsArrayInCreateHomepageRequest() throws JSONException {
		Homepage[] savedHomepage = new Homepage[1];
		mockRequestBody(new JSONObject()
				.put("type", HomepageType.STATIC.getValue())
				.put("staticPage", "landing.html")
				.put("roleIds", new JSONArray().put("default")));

		mockDaos(createProxy(IHomepageDAO.class, (proxy, method, args) -> {
			if ("saveHomepage".equals(method.getName())) {
				savedHomepage[0] = (Homepage) args[0];
				return args[0];
			}
			return defaultValue(method.getReturnType());
		}), null);

		Response response = new HomepageResource().createHomepage(null);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertTrue(savedHomepage[0].isDefaultHomepage());
		assertTrue(savedHomepage[0].getRoleIds().isEmpty());
	}

	@Test
	public void shouldTreatMissingRoleAsDefaultHomepageWhenNoDefaultFlagIsProvided() throws JSONException {
		Homepage[] savedHomepage = new Homepage[1];
		mockRequestBody(new JSONObject()
				.put("type", HomepageType.STATIC.getValue())
				.put("staticPage", "landing.html"));

		mockDaos(createProxy(IHomepageDAO.class, (proxy, method, args) -> {
			if ("saveHomepage".equals(method.getName())) {
				savedHomepage[0] = (Homepage) args[0];
				return args[0];
			}
			return defaultValue(method.getReturnType());
		}), null);

		Response response = new HomepageResource().createHomepage(null);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertTrue(savedHomepage[0].isDefaultHomepage());
		assertTrue(savedHomepage[0].getRoleIds().isEmpty());
	}

	@Test(expected = SpagoBIRuntimeException.class)
	public void shouldStillRejectMissingRoleWhenDefaultIsExplicitlyFalse() throws JSONException {
		mockRequestBody(new JSONObject()
				.put("type", HomepageType.STATIC.getValue())
				.put("staticPage", "landing.html")
				.put("default", false));

		new HomepageResource().createHomepage(null);
	}

	@Test(expected = SpagoBIRuntimeException.class)
	public void shouldRejectMixedDefaultAndNumericRoleIdsInCreateHomepageRequest() throws JSONException {
		mockRequestBody(new JSONObject()
				.put("type", HomepageType.STATIC.getValue())
				.put("staticPage", "landing.html")
				.put("roleIds", new JSONArray().put("default").put(10)));

		new HomepageResource().createHomepage(null);
	}

	@Test
	public void shouldFilterFallbackHomepageForNumericRolePreview() {
		UserProfile profile = new UserProfile();
		profile.setRoles(new ArrayList<>());
		UserProfileManager.setProfile(profile);

		Homepage fallbackHomepage = dynamicHomepage(true, 10, 20);
		Role previewRole = new Role();
		previewRole.setName("ROLE_PREVIEW");
		final Integer[] requestedRoleId = new Integer[1];

		mockDaos(createProxy(IHomepageDAO.class, (proxy, method, args) -> {
			if ("loadHomepageByRoleId".equals(method.getName())) {
				requestedRoleId[0] = (Integer) args[0];
				return fallbackHomepage;
			}
			return defaultValue(method.getReturnType());
		}), createProxy(IRoleDAO.class, (proxy, method, args) -> {
			if ("loadByID".equals(method.getName())) {
				return previewRole;
			}
			return defaultValue(method.getReturnType());
		}));
		mockMenuItems(Collections.singletonList(clickableMenu(20)));

		Response response = new HomepageResource().previewHomepageByRole("7");
		Homepage filteredHomepage = (Homepage) response.getEntity();

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertEquals(Integer.valueOf(7), requestedRoleId[0]);
		assertEquals(Collections.singletonList(20), filteredHomepage.getTemplate().getMenuPlaceholders().get(0).getMenuIds());
		assertEquals(Arrays.asList(10, 20), fallbackHomepage.getTemplate().getMenuPlaceholders().get(0).getMenuIds());
	}

	private void mockRequestBody(JSONObject body) {
		new MockUp<RestUtilities>() {
			@Mock
			public JSONObject readBodyAsJSONObject(HttpServletRequest request) {
				return body;
			}
		};
	}

	private void mockDaos(IHomepageDAO homepageDAO, IRoleDAO roleDAO) {
		new MockUp<DAOFactory>() {
			@Mock
			public IHomepageDAO getHomepageDAO() {
				return homepageDAO;
			}

			@Mock
			public IRoleDAO getRoleDAO() {
				return roleDAO;
			}
		};
	}

	private void mockMenuItems(List<Menu> menuItems) {
		new MockUp<MenuUtilities>() {
			@Mock
			public List getMenuItemsForRole(UserProfile completeProfile, String roleName) {
				return menuItems;
			}
		};
	}

	@SuppressWarnings("unchecked")
	private <T> T createProxy(Class<T> type, InvocationHandler handler) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler);
	}

	private Object defaultValue(Class<?> returnType) {
		if (!returnType.isPrimitive()) {
			return null;
		}
		if (returnType == boolean.class) {
			return false;
		}
		if (returnType == byte.class) {
			return (byte) 0;
		}
		if (returnType == short.class) {
			return (short) 0;
		}
		if (returnType == int.class) {
			return 0;
		}
		if (returnType == long.class) {
			return 0L;
		}
		if (returnType == float.class) {
			return 0F;
		}
		if (returnType == double.class) {
			return 0D;
		}
		if (returnType == char.class) {
			return '\0';
		}
		return null;
	}

	private Homepage staticHomepage(boolean defaultHomepage, List<Integer> roleIds, String staticPage) {
		Homepage homepage = new Homepage();
		homepage.setDefaultHomepage(defaultHomepage);
		homepage.setRoleIds(new ArrayList<>(roleIds));
		homepage.setType(HomepageType.STATIC.getValue());
		homepage.setStaticPage(staticPage);
		return homepage;
	}

	private Homepage dynamicHomepage(boolean defaultHomepage, Integer... menuIds) {
		MenuPlaceholder placeholder = new MenuPlaceholder();
		placeholder.setIndex(0);
		placeholder.setMenuIds(new ArrayList<>(Arrays.asList(menuIds)));

		HomepageTemplate template = new HomepageTemplate();
		template.setHtml("<div></div>");
		template.setMenuPlaceholders(new ArrayList<>(Collections.singletonList(placeholder)));

		Homepage homepage = new Homepage();
		homepage.setDefaultHomepage(defaultHomepage);
		homepage.setType(HomepageType.DYNAMIC.getValue());
		homepage.setTemplate(template);
		return homepage;
	}

	private Menu clickableMenu(int menuId) {
		Menu menu = new Menu();
		menu.setMenuId(menuId);
		menu.setClickable(true);
		return menu;
	}
}
