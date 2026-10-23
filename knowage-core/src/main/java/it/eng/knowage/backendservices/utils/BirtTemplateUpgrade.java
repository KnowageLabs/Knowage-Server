package it.eng.knowage.backendservices.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Updates only the BIRT design schema version, never report properties or scripts. */
public final class BirtTemplateUpgrade {

	public static final String OLD_VERSION = "3.0.0";
	public static final String NEW_VERSION = "3.2.27";
	private static final int MAX_UNCOMPRESSED_SIZE = 64 * 1024 * 1024;

	private BirtTemplateUpgrade() {
	}

	public static boolean needsUpgrade(byte[] content, String name) throws Exception {
		if (!isZip(name)) {
			return isLegacy(parse(content));
		}
		boolean legacy = false;
		int total = 0;
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				byte[] bytes = readEntry(zip, MAX_UNCOMPRESSED_SIZE - total);
				total += bytes.length;
				if (isDesign(entry)) {
					legacy |= isLegacy(parse(bytes));
				}
			}
		}
		return legacy;
	}

	public static byte[] upgrade(byte[] content, String name) throws Exception {
		if (!isZip(name)) {
			return upgradeDesign(content);
		}
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		int total = 0;
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content));
				ZipOutputStream output = new ZipOutputStream(result)) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				byte[] bytes = readEntry(zip, MAX_UNCOMPRESSED_SIZE - total);
				total += bytes.length;
				ZipEntry copy = new ZipEntry(entry.getName());
				if (entry.getTime() >= 0) {
					copy.setTime(entry.getTime());
				}
				copy.setComment(entry.getComment());
				output.putNextEntry(copy);
				output.write(isDesign(entry) ? upgradeDesign(bytes) : bytes);
				output.closeEntry();
			}
		}
		return result.toByteArray();
	}

	private static boolean isZip(String name) {
		return name != null && name.toLowerCase(Locale.ROOT).endsWith(".zip");
	}

	private static boolean isDesign(ZipEntry entry) {
		return !entry.isDirectory() && entry.getName().toLowerCase(Locale.ROOT).endsWith(".rptdesign");
	}

	private static byte[] readEntry(ZipInputStream input, int remaining) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int count;
		while ((count = input.read(buffer)) != -1) {
			if (count > remaining) {
				throw new IOException("BIRT template archive exceeds the uncompressed size limit");
			}
			remaining -= count;
			result.write(buffer, 0, count);
		}
		return result.toByteArray();
	}

	private static Document parse(byte[] content) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);
		return factory.newDocumentBuilder().parse(new ByteArrayInputStream(content));
	}

	private static boolean isLegacy(Document document) {
		Element root = document.getDocumentElement();
		return "report".equals(root.getLocalName()) && OLD_VERSION.equals(root.getAttribute("version"));
	}

	private static byte[] upgradeDesign(byte[] content) throws Exception {
		Document document = parse(content);
		if (!isLegacy(document)) {
			return content;
		}
		document.getDocumentElement().setAttribute("version", NEW_VERSION);
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		transformer.transform(new DOMSource(document), new StreamResult(result));
		return result.toByteArray();
	}
}