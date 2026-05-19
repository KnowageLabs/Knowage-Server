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
package it.eng.spagobi.wapp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.SessionUserProfile;
import it.eng.spagobi.commons.bo.SessionUserProfileBuilder;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.wapp.bo.Menu;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class MenuUtilitiesTest {

	@Test
	public void shouldBuildMenuItemsForRoleUsingPreviewProfile() throws EMFUserError {
		UserProfile completeProfile = new UserProfile();
		SessionUserProfile previewProfile = new SessionUserProfile();
		List<Menu> expectedMenuItems = new ArrayList<>();
		expectedMenuItems.add(new Menu());

		final String[] capturedRoleName = new String[1];
		final IEngUserProfile[] capturedMenuProfile = new IEngUserProfile[1];
		final boolean[] capturedDaoFlag = new boolean[1];
		final IEngUserProfile[] capturedFilterProfile = new IEngUserProfile[1];
		final boolean[] filterCalled = new boolean[1];

		new MockUp<SessionUserProfileBuilder>() {
			@Mock
			public SessionUserProfile getDefaultUserProfile(UserProfile profile, String roleName) {
				assertSame(completeProfile, profile);
				capturedRoleName[0] = roleName;
				return previewProfile;
			}
		};

		new MockUp<MenuUtilities>() {
			@Mock
			public List getMenuItems(IEngUserProfile profile, boolean menuRolesDaoRequiresProfile) {
				capturedMenuProfile[0] = profile;
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

		List actualMenuItems = MenuUtilities.getMenuItemsForRole(completeProfile, "ROLE_PREVIEW");

		assertSame(expectedMenuItems, actualMenuItems);
		assertEquals("ROLE_PREVIEW", capturedRoleName[0]);
		assertSame(previewProfile, capturedMenuProfile[0]);
		assertTrue(capturedDaoFlag[0]);
		assertSame(previewProfile, capturedFilterProfile[0]);
		assertTrue(filterCalled[0]);
	}
}
