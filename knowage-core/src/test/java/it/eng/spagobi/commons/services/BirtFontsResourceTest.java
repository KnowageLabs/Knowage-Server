package it.eng.spagobi.commons.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.user.UserProfileManager;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class BirtFontsResourceTest {

	@Rule
	public TemporaryFolder resources = new TemporaryFolder();

	@After
	public void cleanUp() {
		UserProfileManager.unset();
	}

	@Test
	public void rejectsAnonymousAndTenantAdministratorsForEveryOperation() {
		BirtFontsResource service = new BirtFontsResource();
		assertEquals(403, service.list().getStatus());
		assertEquals(403, service.upload(null).getStatus());
		assertEquals(403, service.delete("font.ttf").getStatus());
		UserProfileManager.setProfile(new UserProfile());
		assertEquals(403, service.list().getStatus());
		assertEquals(403, service.upload(null).getStatus());
		assertEquals(403, service.delete("font.ttf").getStatus());
	}

	@Test
	public void usesGlobalResourcesAndRejectsTraversalForSuperadmins() {
		UserProfile profile = new UserProfile();
		profile.setIsSuperadmin(true);
		UserProfileManager.setProfile(profile);
		Path root = resources.getRoot().toPath();
		new MockUp<SpagoBIUtilities>() {
			@Mock
			public String getRootResourcePath() {
				return root.toString();
			}
			@Mock
			public String getResourcePath() {
				throw new AssertionError("Must not use tenant resources");
			}
		};
		BirtFontsResource service = new BirtFontsResource();
		assertEquals(200, service.list().getStatus());
		Map<?, ?> result = (Map<?, ?>) service.list().getEntity();
		assertEquals(root.resolve("birt").resolve("fonts").toString(), result.get("directory"));
		assertTrue(Paths.get((String) result.get("configurationPath")).isAbsolute());
		assertEquals(400, service.delete("../other.ttf").getStatus());
		assertEquals(404, service.delete("missing.ttf").getStatus());
	}
}
