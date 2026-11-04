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
package it.eng.spagobi.engines.birt.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class ExternalLibraryTemplateResolver {

	private static final String PREFIX = "${KNOWAGE_EXTERNAL_LIBRARY:";
	private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{KNOWAGE_EXTERNAL_LIBRARY:([^{}]*)\\}");
	private static final String RESOURCE_PATH = "/2.0/resources/external-libraries";

	private ExternalLibraryTemplateResolver() {
	}

	public static byte[] resolve(byte[] template, String apiBaseUrl, String authorizationParameter,
			Supplier<String> userToken) throws IOException {
		if (!containsPlaceholder(template)) {
			return template;
		}

		Document document;
		List<Node> nodes = new ArrayList<>();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);
			document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(template));
			collectNodes(document.getDocumentElement(), nodes);
		} catch (Exception e) {
			throw new IOException("Cannot parse the BIRT external-library placeholders");
		}
		if (nodes.isEmpty()) {
			return template;
		}

		String endpoint = getEndpoint(apiBaseUrl);
		if (authorizationParameter == null || authorizationParameter.trim().isEmpty()) {
			throw new IOException("Missing external-library authorization parameter name");
		}
		String token = userToken.get();
		if (token == null || token.trim().isEmpty()) {
			throw new IOException("Missing user JWT for BIRT external libraries");
		}
		String authorization = "&" + encode(authorizationParameter) + "=" + encode(token);
		for (Node node : nodes) {
			Matcher matcher = PLACEHOLDER.matcher(node.getNodeValue());
			StringBuffer value = new StringBuffer();
			while (matcher.find()) {
				String libraryName = matcher.group(1).trim();
				if (libraryName.isEmpty() || libraryName.contains("/") || libraryName.contains("\\")
						|| ".".equals(libraryName) || "..".equals(libraryName)) {
					throw new IOException("Invalid file name in a BIRT external-library placeholder");
				}
				String url = endpoint + "?libraryName=" + encode(libraryName) + authorization;
				matcher.appendReplacement(value, Matcher.quoteReplacement(url));
			}
			matcher.appendTail(value);
			if (value.indexOf(PREFIX) >= 0) {
				throw new IOException("Malformed BIRT external-library placeholder");
			}
			node.setNodeValue(value.toString());
		}

		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			transformer.transform(new DOMSource(document), new StreamResult(output));
			return output.toByteArray();
		} catch (Exception e) {
			// Transformer errors must not expose the personalized document or its JWT.
			throw new IOException("Cannot serialize the BIRT execution template");
		}
	}

	private static boolean containsPlaceholder(byte[] template) {
		return new String(template, StandardCharsets.UTF_8).contains(PREFIX)
				|| new String(template, StandardCharsets.UTF_16LE).contains(PREFIX)
				|| new String(template, StandardCharsets.UTF_16BE).contains(PREFIX);
	}

	private static void collectNodes(Node node, List<Node> nodes) {
		if ((node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE
				|| node.getNodeType() == Node.ATTRIBUTE_NODE) && node.getNodeValue().contains(PREFIX)) {
			nodes.add(node);
		}
		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null) {
			for (int i = 0; i < attributes.getLength(); i++) {
				collectNodes(attributes.item(i), nodes);
			}
		}
		// Attribute values are already handled above; their child text nodes must not be processed twice.
		if (node.getNodeType() != Node.ATTRIBUTE_NODE) {
			for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
				collectNodes(child, nodes);
			}
		}
	}

	private static String getEndpoint(String apiBaseUrl) throws IOException {
		try {
			URI base = new URI(apiBaseUrl);
			if (!"https".equalsIgnoreCase(base.getScheme()) || base.getHost() == null
					|| base.getRawUserInfo() != null || base.getRawQuery() != null || base.getRawFragment() != null) {
				throw new IllegalArgumentException();
			}
			return base.toASCIIString().replaceAll("/+$", "") + RESOURCE_PATH;
		} catch (Exception e) {
			throw new IOException("Configure BIRT EXTERNAL_LIBRARIES_API_BASE_URL with an absolute HTTPS API base URL");
		}
	}

	private static String encode(String value) throws IOException {
		return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
	}
}
