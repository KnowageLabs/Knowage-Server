package it.eng.spagobi.commons.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.servlet.http.HttpServletRequest;
import javax.imageio.ImageIO;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONException;
import org.junit.After;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.serializer.v3.MenuListJSONSerializerForREST;
import it.eng.spagobi.commons.serializer.v3.dto.ItemMenu;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.user.UserProfileManager;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class LoginConfigResourceTest {

	@Rule
	public TemporaryFolder resources = new TemporaryFolder();

	@After
	public void cleanUp() {
		UserProfileManager.unset();
	}

	@Test
	public void shouldRejectAnonymousAndNonSuperadminWrites() {
		assertEquals(403, new LoginConfigResource().saveBranding(Collections.emptyMap()).getStatus());
		assertEquals(403, new LoginConfigResource().uploadBrandingImage(null).getStatus());
		UserProfileManager.setProfile(new UserProfile());
		assertEquals(403, new LoginConfigResource().saveBranding(Collections.emptyMap()).getStatus());
		assertEquals(403, new LoginConfigResource().uploadBrandingImage(null).getStatus());
	}

	@Test
	public void shouldShowTheLoginManagementMenuOnlyToSuperadmins() throws Exception {
		UserProfile profile = new UserProfile();
		MenuListJSONSerializerForREST serializer = new MenuListJSONSerializerForREST(profile, null);
		ItemMenu item = new ItemMenu("2007", "menu.loginPageManagement", null, "/login-page-management", null, null, "superadmin", null, null, null);
		Method condition = MenuListJSONSerializerForREST.class.getDeclaredMethod("menuConditionIsSatisfied", ItemMenu.class);
		condition.setAccessible(true);
		assertEquals(false, condition.invoke(serializer, item));
		profile.setIsSuperadmin(true);
		assertEquals(true, condition.invoke(serializer, item));
	}

	@Test
	public void shouldPersistAndResetBrandingUsingTheExistingConfiguration() throws JSONException {
		UserProfile profile = new UserProfile();
		profile.setIsSuperadmin(true);
		UserProfileManager.setProfile(profile);
		Config[] stored = new Config[1];
		IConfigDAO dao = (IConfigDAO) Proxy.newProxyInstance(IConfigDAO.class.getClassLoader(), new Class<?>[] { IConfigDAO.class },
				(proxy, method, args) -> {
					if ("loadConfigParametersByLabelIfExist".equals(method.getName())) {
						assertEquals(LoginBranding.CONFIG_LABEL, args[0]);
						return Optional.ofNullable(stored[0]);
					}
					if ("saveConfig".equals(method.getName())) {
						stored[0] = (Config) args[0];
						stored[0].setId(42);
					}
					return null;
				});
		new MockUp<DAOFactory>() {
			@Mock
			public IConfigDAO getSbiConfigDAO() {
				return dao;
			}
		};
		Map<String, String> body = new HashMap<>();
		body.put("backgroundUrl", " /images/bg.jpg ");
		body.put("logoUrl", "https://example.com/logo.svg");
		body.put("faviconUrl", "/favicon.ico");
		Response response = new LoginConfigResource().saveBranding(body);
		assertEquals(200, response.getStatus());
		assertEquals("/images/bg.jpg", LoginBranding.read(stored[0].getValueCheck()).get("backgroundUrl"));

		assertEquals(200, new LoginConfigResource().saveBranding(Collections.emptyMap()).getStatus());
		assertEquals(Integer.valueOf(42), stored[0].getId());
		assertTrue(LoginBranding.read(stored[0].getValueCheck()).values().stream().allMatch(String::isEmpty));
	}

	@Test
	public void shouldRejectUnsupportedImageUrls() {
		String[] invalid = { "javascript:alert(1)", "data:image/svg+xml,test", "//example.com/logo", "/\\example.com/logo",
				"images/logo.png", "https:example.com/logo", "https://example.com/a'b" };
		for (String url : invalid) {
			try {
				LoginBranding.normalize(Collections.singletonMap("logoUrl", url));
				org.junit.Assert.fail("Accepted unsupported image URL");
			} catch (IllegalArgumentException expected) {
				// Expected for each unsupported URL.
			} catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldEnforceExistingConfigurationColumnLength() throws JSONException {
		LoginBranding.normalize(Collections.singletonMap("logoUrl", "https://example.com/" + String.join("", Collections.nCopies(1000, "x"))));
	}

	@Test
	public void shouldKeepDefaultsForAbsentOrMalformedStoredBranding() throws JSONException {
		assertTrue(LoginBranding.read(null).values().stream().allMatch(String::isEmpty));
		assertTrue(LoginBranding.read("not-json").values().stream().allMatch(String::isEmpty));
		assertTrue(LoginBranding.read("{\"logoUrl\":\"javascript:alert(1)\"}").values().stream().allMatch(String::isEmpty));
	}

	@Test
	public void shouldUploadAnImageAndServeItBeforeAuthentication() throws Exception {
		UserProfile profile = new UserProfile();
		profile.setIsSuperadmin(true);
		UserProfileManager.setProfile(profile);
		byte[] png = pngImage();
		FileItem item = new DiskFileItemFactory().createItem("file", "image/png", false, "../../logo.png");
		item.getOutputStream().write(png);
		new MockUp<ServletFileUpload>() {
			@Mock
			public java.util.List<FileItem> parseRequest(HttpServletRequest request) {
				return Arrays.asList(item);
			}
		};
		File root = resources.getRoot();
		new MockUp<SpagoBIUtilities>() {
			@Mock
			public String getRootResourcePath() {
				return root.getAbsolutePath();
			}
		};
		HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, (proxy, method, args) -> "/knowage");
		Response upload = new LoginConfigResource().uploadBrandingImage(request);
		assertEquals(200, upload.getStatus());
		String url = ((Map<String, String>) upload.getEntity()).get("url");
		assertTrue(url.startsWith("/knowage/restful-services/loginconfig/images/"));
		String name = url.substring(url.lastIndexOf('/') + 1);
		UserProfileManager.unset();
		Response response = new LoginConfigResource().getBrandingImage(name);
		assertEquals(200, response.getStatus());
		assertArrayEquals(png, (byte[]) response.getEntity());
		assertEquals("image/png", response.getMediaType().toString());
		assertEquals("nosniff", response.getHeaderString("X-Content-Type-Options"));
		assertEquals(404, new LoginConfigResource().getBrandingImage("../secret.png").getStatus());
	}

	@Test
	public void shouldRejectOversizedOrDisguisedImagesWithoutWritingFiles() throws Exception {
		LoginBrandingImages images = new LoginBrandingImages(resources.getRoot().toPath());
		try {
			images.save(new byte[LoginBrandingImages.MAX_BYTES + 1], "large.png");
			org.junit.Assert.fail("Accepted an oversized image");
		} catch (IllegalArgumentException expected) {
		}
		try {
			images.save("<html>not an image</html>".getBytes(StandardCharsets.UTF_8), "fake.png");
			org.junit.Assert.fail("Accepted a disguised image");
		} catch (IllegalArgumentException expected) {
		}
		assertEquals(0, resources.getRoot().list().length);
	}

	@Test
	public void shouldAcceptStaticSvgAndRejectActiveSvg() throws Exception {
		LoginBrandingImages images = new LoginBrandingImages(resources.getRoot().toPath());
		String safe = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1\" height=\"1\"><path d=\"M0 0h1v1z\"/></svg>";
		String name = images.save(safe.getBytes(StandardCharsets.UTF_8), "logo.svg");
		assertTrue(Files.exists(images.find(name)));
		String active = "<svg xmlns=\"http://www.w3.org/2000/svg\" onload=\"alert(1)\"/>";
		try {
			images.save(active.getBytes(StandardCharsets.UTF_8), "active.svg");
			org.junit.Assert.fail("Accepted active SVG");
		} catch (IllegalArgumentException expected) {
		}
		assertEquals(1, resources.getRoot().list().length);
	}

	private byte[] pngImage() throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "png", output);
		return output.toByteArray();
	}
}
