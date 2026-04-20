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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.wapp.bo.Menu;
import it.eng.spagobi.wapp.util.MenuUtilities;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class MenuResourceTest {

	private Path resourceRoot;

	@After
	public void cleanUp() throws Exception {
		UserProfileManager.unset();
		if (resourceRoot != null) {
			FileUtils.deleteDirectory(resourceRoot.toFile());
		}
	}

	@Test
	public void shouldPreviewMenuUsingCurrentProfileWhenRoleIdIsDefault() {
		UserProfile profile = new UserProfile();
		profile.setRoles(Collections.singletonList("ROLE_A"));
		UserProfileManager.setProfile(profile);

		List<Menu> expectedMenuItems = new ArrayList<>();
		expectedMenuItems.add(new Menu());

		final IEngUserProfile[] capturedMenuProfile = new IEngUserProfile[1];
		final boolean[] capturedDaoFlag = new boolean[1];
		final IEngUserProfile[] capturedFilterProfile = new IEngUserProfile[1];
		final boolean[] filterCalled = new boolean[1];

		new MockUp<MenuUtilities>() {
			@Mock
			public List getMenuItems(IEngUserProfile currentProfile, boolean menuRolesDaoRequiresProfile) {
				capturedMenuProfile[0] = currentProfile;
				capturedDaoFlag[0] = menuRolesDaoRequiresProfile;
				return expectedMenuItems;
			}

			@Mock
			public void filterListForUserClickableElements(List menuList, IEngUserProfile userProfile) {
				assertSame(expectedMenuItems, menuList);
				capturedFilterProfile[0] = userProfile;
				filterCalled[0] = true;
			}
		};

		Response response = new MenuResource().previewMenuByRole("default");

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertSame(expectedMenuItems, response.getEntity());
		assertSame(profile, capturedMenuProfile[0]);
		assertTrue(capturedDaoFlag[0]);
		assertSame(profile, capturedFilterProfile[0]);
		assertTrue(filterCalled[0]);
	}

	@Test
	public void shouldPreviewMenuForNumericRole() {
		UserProfile profile = new UserProfile();
		UserProfileManager.setProfile(profile);

		Role previewRole = new Role();
		previewRole.setName("ROLE_PREVIEW");
		List<Menu> expectedMenuItems = new ArrayList<>();
		expectedMenuItems.add(new Menu());
		final Integer[] capturedRoleId = new Integer[1];
		final String[] capturedRoleName = new String[1];

		new MockUp<DAOFactory>() {
			@Mock
			public IRoleDAO getRoleDAO() {
				return createProxy(IRoleDAO.class, (proxy, method, args) -> {
					if ("loadByID".equals(method.getName())) {
						capturedRoleId[0] = (Integer) args[0];
						return previewRole;
					}
					return defaultValue(method.getReturnType());
				});
			}
		};

		new MockUp<MenuUtilities>() {
			@Mock
			public List getMenuItemsForRole(UserProfile completeProfile, String roleName) {
				assertSame(profile, completeProfile);
				capturedRoleName[0] = roleName;
				return expectedMenuItems;
			}
		};

		Response response = new MenuResource().previewMenuByRole("7");

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertSame(expectedMenuItems, response.getEntity());
		assertEquals(Integer.valueOf(7), capturedRoleId[0]);
		assertEquals("ROLE_PREVIEW", capturedRoleName[0]);
	}

	@Test(expected = NotFoundException.class)
	public void shouldRejectInvalidPreviewRoleId() {
		new MenuResource().previewMenuByRole("not-a-role");
	}

	@Test
	public void shouldReturnHtmlFileContent() throws Exception {
		resourceRoot = Files.createTempDirectory("menu-resource-test");
		Path staticMenuDirectory = Files.createDirectories(resourceRoot.resolve("static_menu"));
		Path htmlFile = staticMenuDirectory.resolve("maranza.html");
		Files.writeString(htmlFile, "<html>ciao</html>", StandardCharsets.UTF_8);
		mockResourcePath(resourceRoot);

		Response response = new MenuResource().getHTMLFile("maranza.html");

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertEquals("<html>ciao</html>", response.getEntity());
	}

	@Test
	public void shouldRejectInvalidHtmlFileName() throws Exception {
		resourceRoot = Files.createTempDirectory("menu-resource-test");
		mockResourcePath(resourceRoot);

		Response response = new MenuResource().getHTMLFile("../maranza.html");

		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	@Test
	public void shouldReturnNotFoundWhenHtmlFileDoesNotExist() throws Exception {
		resourceRoot = Files.createTempDirectory("menu-resource-test");
		Files.createDirectories(resourceRoot.resolve("static_menu"));
		mockResourcePath(resourceRoot);

		Response response = new MenuResource().getHTMLFile("missing.html");

		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}

	private void mockResourcePath(Path resourcePath) {
		final String mockedResourcePath = resourcePath.toString();
		new MockUp<SpagoBIUtilities>() {
			@Mock
			public String getResourcePath() {
				return mockedResourcePath;
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
}
