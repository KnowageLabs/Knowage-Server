package it.eng.knowage.backendservices.utils;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;

public class BirtTemplateUpgradeTest {

	private byte[] design(String version) {
		return ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<report xmlns=\"http://www.eclipse.org/birt/2005/design\" version=\"" + version + "\">"
				+ "<property name=\"description\">Città &amp; attività 3.0.0</property>"
				+ "<method name=\"initialize\"><![CDATA[if (1 < 2) { var version = '3.0.0'; }]]></method></report>")
				.getBytes(StandardCharsets.UTF_8);
	}

	@Test
	public void detectsOnlyLegacyRootVersion() throws Exception {
		assertTrue(BirtTemplateUpgrade.needsUpgrade(design("3.0.0"), "report.rptdesign"));
		assertFalse(BirtTemplateUpgrade.needsUpgrade(design("3.2.27"), "report.rptdesign"));
		assertFalse(BirtTemplateUpgrade.needsUpgrade(design("3.2.26"), "report.rptdesign"));
		assertFalse(BirtTemplateUpgrade.needsUpgrade("<other version='3.0.0'/>".getBytes(StandardCharsets.UTF_8), "report.rptdesign"));
	}

	@Test
	public void upgradesVersionWithoutChangingScriptsOrText() throws Exception {
		byte[] upgraded = BirtTemplateUpgrade.upgrade(design("3.0.0"), "report.rptdesign");
		String xml = new String(upgraded, StandardCharsets.UTF_8);
		assertTrue(xml.contains("version=\"3.2.27\""));
		assertTrue(xml.contains("Città &amp; attività 3.0.0"));
		assertTrue(xml.contains("if (1 < 2) { var version = '3.0.0'; }"));
		assertFalse(BirtTemplateUpgrade.needsUpgrade(upgraded, "report.rptdesign"));
	}

	@Test
	public void leavesOtherVersionsByteIdentical() throws Exception {
		byte[] content = design("3.2.27");
		assertArrayEquals(content, BirtTemplateUpgrade.upgrade(content, "report.rptdesign"));
	}

	@Test
	public void supportsNoNamespaceAndSingleQuotedAttributes() throws Exception {
		byte[] content = "<report id='1' version = '3.0.0'/>".getBytes(StandardCharsets.UTF_8);
		assertTrue(BirtTemplateUpgrade.needsUpgrade(content, "report.rptdesign"));
		assertFalse(BirtTemplateUpgrade.needsUpgrade(BirtTemplateUpgrade.upgrade(content, "report.rptdesign"), "report.rptdesign"));
	}

	@Test
	public void respectsDeclaredXmlEncoding() throws Exception {
		byte[] content = "<?xml version='1.0' encoding='ISO-8859-1'?><report version='3.0.0'><property>Città</property></report>"
				.getBytes("ISO-8859-1");
		byte[] upgraded = BirtTemplateUpgrade.upgrade(content, "report.rptdesign");
		assertEquals("Città", DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new ByteArrayInputStream(upgraded)).getDocumentElement().getTextContent());
	}

	@Test(expected = Exception.class)
	public void rejectsExternalEntities() throws Exception {
		String xml = "<!DOCTYPE report [<!ENTITY xxe SYSTEM 'file:///unavailable'>]><report version='3.0.0'>&xxe;</report>";
		BirtTemplateUpgrade.needsUpgrade(xml.getBytes(StandardCharsets.UTF_8), "report.rptdesign");
	}

	@Test(expected = Exception.class)
	public void rejectsMalformedXml() throws Exception {
		BirtTemplateUpgrade.upgrade("<report".getBytes(StandardCharsets.UTF_8), "report.rptdesign");
	}

	@Test
	public void upgradesZipDesignsAndPreservesOtherEntries() throws Exception {
		Map<String, byte[]> entries = new LinkedHashMap<>();
		entries.put("report.rptdesign", design("3.0.0"));
		entries.put("subreports/current.rptdesign", design("3.2.27"));
		entries.put("lib/custom.jar", new byte[] { 0, 1, 2, -1 });
		entries.put("report.properties", "title=Città".getBytes(StandardCharsets.UTF_8));
		byte[] archive = zip(entries);
		assertTrue(BirtTemplateUpgrade.needsUpgrade(archive, "report.zip"));
		byte[] upgraded = BirtTemplateUpgrade.upgrade(archive, "report.zip");
		assertFalse(BirtTemplateUpgrade.needsUpgrade(upgraded, "report.zip"));
		Map<String, byte[]> actual = unzip(upgraded);
		assertEquals(entries.keySet(), actual.keySet());
		assertArrayEquals(entries.get("lib/custom.jar"), actual.get("lib/custom.jar"));
		assertArrayEquals(entries.get("report.properties"), actual.get("report.properties"));
		assertArrayEquals(entries.get("subreports/current.rptdesign"), actual.get("subreports/current.rptdesign"));
	}

	@Test
	public void archiveWithoutLegacyDesignNeedsNoUpgrade() throws Exception {
		Map<String, byte[]> entries = new LinkedHashMap<>();
		entries.put("current.rptdesign", design("3.2.27"));
		assertFalse(BirtTemplateUpgrade.needsUpgrade(zip(entries), "report.zip"));
	}

	private byte[] zip(Map<String, byte[]> entries) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
			for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
				zip.putNextEntry(new ZipEntry(entry.getKey()));
				zip.write(entry.getValue());
				zip.closeEntry();
			}
		}
		return bytes.toByteArray();
	}

	private Map<String, byte[]> unzip(byte[] archive) throws Exception {
		Map<String, byte[]> entries = new LinkedHashMap<>();
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int count;
				while ((count = zip.read(buffer)) != -1) {
					bytes.write(buffer, 0, count);
				}
				entries.put(entry.getName(), bytes.toByteArray());
			}
		}
		return entries;
	}
}