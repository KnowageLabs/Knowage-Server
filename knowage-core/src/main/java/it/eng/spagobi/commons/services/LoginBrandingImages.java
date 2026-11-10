package it.eng.spagobi.commons.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

final class LoginBrandingImages {

	static final int MAX_BYTES = 5 * 1024 * 1024;
	private static final long MAX_PIXELS = 16000000;
	private final Path directory;

	LoginBrandingImages(Path directory) {
		this.directory = directory;
	}

	String save(byte[] content, String originalName) throws IOException {
		if (content == null || content.length == 0 || content.length > MAX_BYTES) {
			throw new IllegalArgumentException("Images must be non-empty and no larger than 5 MB");
		}
		String extension = originalName == null ? "" : originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
		validate(content, extension);
		String name = UUID.randomUUID().toString() + "." + extension;
		Files.createDirectories(directory);
		Files.write(directory.resolve(name), content, StandardOpenOption.CREATE_NEW);
		return name;
	}

	Path find(String name) {
		if (name == null || !name.matches("[a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12}\\.(?:png|jpe?g|gif|svg|ico)")) {
			return null;
		}
		Path file = directory.resolve(name);
		return Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) ? file : null;
	}

	static String mediaType(String name) {
		String extension = name.substring(name.lastIndexOf('.') + 1);
		if ("svg".equals(extension)) return "image/svg+xml";
		if ("ico".equals(extension)) return "image/x-icon";
		if ("jpg".equals(extension) || "jpeg".equals(extension)) return "image/jpeg";
		return "image/" + extension;
	}

	private void validate(byte[] content, String extension) {
		try {
			if ("svg".equals(extension)) {
				validateSvg(content);
			} else if ("ico".equals(extension)) {
				validateIcon(content);
			} else if (Arrays.asList("png", "jpg", "jpeg", "gif").contains(extension)) {
				validateRaster(content, extension);
			} else {
				throw new IllegalArgumentException("Supported image formats are PNG, JPG, GIF, SVG and ICO");
			}
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid image content", e);
		}
	}

	private void validateRaster(byte[] content, String extension) throws IOException {
		try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
			if (!readers.hasNext()) throw new IllegalArgumentException("Invalid image content");
			ImageReader reader = readers.next();
			try {
				reader.setInput(stream);
				String format = reader.getFormatName().toLowerCase(Locale.ROOT);
				String expected = "jpg".equals(extension) ? "jpeg" : extension;
				if (!format.equals(expected) || (long) reader.getWidth(0) * reader.getHeight(0) > MAX_PIXELS) {
					throw new IllegalArgumentException("Invalid image format or dimensions (maximum 16 megapixels)");
				}
				reader.read(0);
			} finally {
				reader.dispose();
			}
		}
	}

	private void validateIcon(byte[] content) {
		ByteBuffer buffer = ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN);
		if (content.length < 6 || buffer.getShort() != 0 || buffer.getShort() != 1) {
			throw new IllegalArgumentException("Invalid ICO image");
		}
		int count = Short.toUnsignedInt(buffer.getShort());
		if (count < 1 || count > 256 || content.length < 6 + 16 * count) {
			throw new IllegalArgumentException("Invalid ICO image");
		}
		for (int i = 0; i < count; i++) {
			long size = Integer.toUnsignedLong(buffer.getInt(6 + 16 * i + 8));
			long offset = Integer.toUnsignedLong(buffer.getInt(6 + 16 * i + 12));
			if (size == 0 || offset < 6 + 16 * count || offset + size > content.length) {
				throw new IllegalArgumentException("Invalid ICO image");
			}
		}
	}

	private void validateSvg(byte[] content) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);
		Element root = factory.newDocumentBuilder().parse(new ByteArrayInputStream(content)).getDocumentElement();
		if (!"svg".equals(root.getLocalName()) || !"http://www.w3.org/2000/svg".equals(root.getNamespaceURI())) {
			throw new IllegalArgumentException("Invalid SVG image");
		}
		NodeList elements = root.getOwnerDocument().getElementsByTagName("*");
		for (int i = 0; i < elements.getLength(); i++) {
			Element element = (Element) elements.item(i);
			if (Arrays.asList("script", "foreignobject", "iframe", "object", "embed", "set", "animate", "animatemotion", "animatetransform")
					.contains(element.getLocalName().toLowerCase(Locale.ROOT))) {
				throw new IllegalArgumentException("SVG images must not contain active content");
			}
			NamedNodeMap attributes = element.getAttributes();
			for (int j = 0; j < attributes.getLength(); j++) {
				String name = attributes.item(j).getLocalName().toLowerCase(Locale.ROOT);
				String value = attributes.item(j).getNodeValue().trim();
				if (name.startsWith("on") || ("href".equals(name) && !value.isEmpty() && !value.startsWith("#"))) {
					throw new IllegalArgumentException("SVG images must not contain active or external content");
				}
			}
		}
	}
}
