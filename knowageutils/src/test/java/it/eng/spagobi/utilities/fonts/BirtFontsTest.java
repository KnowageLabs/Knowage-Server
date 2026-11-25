package it.eng.spagobi.utilities.fonts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;

public class BirtFontsTest {

	@Rule
	public TemporaryFolder temporary = new TemporaryFolder();

	@Test
	public void savesExactGlobalPathsAndUpdatesMappingAfterDeletion() throws Exception {
		BirtFonts fonts = new BirtFonts(temporary.newFolder("resources & fonts").toPath());
		fonts.save(validFont(), "Regular.ttf");
		fonts.save(validFont(), "Bold.otf");
		Map<String, Object> list = fonts.list();
		assertEquals(2, ((List<?>) list.get("items")).size());
		Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fonts.getConfigurationPath().toFile());
		assertEquals("font", xml.getDocumentElement().getTagName());
		assertEquals(2, xml.getElementsByTagName("path").getLength());
		assertEquals(Paths.get((String) list.get("directory"), "Bold.otf").toString(),
				xml.getElementsByTagName("path").item(0).getAttributes().getNamedItem("path").getNodeValue());
		assertTrue(fonts.delete("Regular.ttf"));
		assertFalse(fonts.delete("Regular.ttf"));
		assertEquals(1, ((List<?>) fonts.list().get("items")).size());
		xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fonts.getConfigurationPath().toFile());
		assertEquals(1, xml.getElementsByTagName("path").getLength());
		assertTrue(fonts.delete("Bold.otf"));
		assertEquals(0, ((List<?>) fonts.list().get("items")).size());
	}

	@Test
	public void rejectsInvalidFontsAndUnsafeNamesWithoutWriting() throws Exception {
		BirtFonts fonts = new BirtFonts(temporary.getRoot().toPath());
		String[] names = { "../font.ttf", "C:\\font.ttf", "font.woff", "CON.ttf", "font.ttf/other.ttf" };
		for (String name : names) {
			try {
				fonts.save(validFont(), name);
				fail("Accepted unsafe name " + name);
			} catch (IllegalArgumentException expected) {
				assertFalse(Files.exists(fonts.getConfigurationPath()));
			}
		}
		byte[][] invalid = { new byte[0], new byte[] { 0, 1, 0, 0 }, new byte[BirtFonts.MAX_BYTES + 1] };
		for (byte[] content : invalid) {
			try {
				fonts.save(content, "font.ttf");
				fail("Accepted invalid font");
			} catch (IllegalArgumentException expected) {
				assertFalse(Files.exists(fonts.getConfigurationPath()));
			}
		}
	}

	@Test
	public void rejectsCaseInsensitiveDuplicatesWithoutOverwriting() throws Exception {
		BirtFonts fonts = new BirtFonts(temporary.getRoot().toPath());
		fonts.save(validFont(), "Regular.ttf");
		byte[] configuration = Files.readAllBytes(fonts.getConfigurationPath());
		try {
			fonts.save(validFont(), "regular.TTF");
			fail("Accepted duplicate");
		} catch (FileAlreadyExistsException expected) {
			org.junit.Assert.assertArrayEquals(configuration, Files.readAllBytes(fonts.getConfigurationPath()));
			assertEquals(1, ((List<?>) fonts.list().get("items")).size());
		}
	}

	@Test
	public void rollsBackFontChangesIfConfigurationCannotBeReplaced() throws Exception {
		BirtFonts fonts = new BirtFonts(temporary.getRoot().toPath());
		fonts.save(validFont(), "Regular.ttf");
		Files.delete(fonts.getConfigurationPath());
		Files.createDirectory(fonts.getConfigurationPath());
		Files.createFile(fonts.getConfigurationPath().resolve("blocker"));
		try {
			fonts.save(validFont(), "Bold.ttf");
			fail("Saved without a valid configuration");
		} catch (java.io.IOException expected) {
			assertFalse(Files.exists(fonts.getConfigurationPath().getParent().resolve("fonts").resolve("Bold.ttf")));
		}
		try {
			fonts.delete("Regular.ttf");
			fail("Deleted without a valid configuration");
		} catch (java.io.IOException expected) {
			assertTrue(Files.exists(fonts.getConfigurationPath().getParent().resolve("fonts").resolve("Regular.ttf")));
		}
	}

	private byte[] validFont() throws Exception {
		Path root = Paths.get("").toAbsolutePath();
		while (root != null) {
			Path fixture = root.resolve("knowage").resolve("src").resolve("main").resolve("resources").resolve("fonts").resolve("DejaVuSans.ttf");
			if (Files.isRegularFile(fixture)) return Files.readAllBytes(fixture);
			root = root.getParent();
		}
		throw new IllegalStateException("Existing DejaVuSans test fixture not found");
	}
}
