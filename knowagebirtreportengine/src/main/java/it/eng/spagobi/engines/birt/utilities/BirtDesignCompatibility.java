package it.eng.spagobi.engines.birt.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Adapts the execution copy of a design to the BIRT 4.4.2 runtime.
 * This is a targeted schema-version workaround, not a general format converter:
 * newer report features may still be unsupported by the runtime.
 */
public final class BirtDesignCompatibility {

	private static final String DESIGN_VERSION = "3.2.27";
	private static final String RUNTIME_VERSION = "3.2.23";
	private static final String DESIGN_NAMESPACE = "http://www.eclipse.org/birt/2005/design";

	private BirtDesignCompatibility() {
	}

	/** Consumes and closes the original stream; never writes back to the template. */
	public static InputStream normalize(InputStream input) throws IOException {
		if (input == null) {
			throw new IOException("Missing BIRT report design");
		}
		try (InputStream source = input; ByteArrayOutputStream content = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[8192];
			int count;
			while ((count = source.read(buffer)) != -1) {
				content.write(buffer, 0, count);
			}
			return new ByteArrayInputStream(normalize(content.toByteArray()));
		}
	}

	/** Returns the original bytes unchanged unless the report version is exactly 3.2.27. */
	public static byte[] normalize(byte[] content) throws IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);
			Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(content));
			Element root = document.getDocumentElement();
			String namespace = root.getNamespaceURI();
			if (!"report".equals(root.getLocalName())
					|| (namespace != null && !DESIGN_NAMESPACE.equals(namespace))
					|| !DESIGN_VERSION.equals(root.getAttribute("version"))) {
				return content;
			}

			root.setAttribute("version", RUNTIME_VERSION);
			TransformerFactory transformers = TransformerFactory.newInstance();
			// Identity serialization only: no stylesheet or external source is loaded.
			// Legacy Xalan bundled with BIRT does not implement setFeature().
			// External entities and DTDs have already been disabled during parsing.
			Transformer transformer = transformers.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			transformer.transform(new DOMSource(document), new StreamResult(result));
			return result.toByteArray();
		} catch (Exception e) {
			throw new IOException("Cannot prepare BIRT report design for runtime compatibility", e);
		}
	}
}