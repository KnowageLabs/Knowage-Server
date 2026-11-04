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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;

public class ExternalLibraryTemplateResolverTest {

	private static final String BASE = "https://reports.example/custom-api/";
	private static final String ENDPOINT = "https://reports.example/custom-api/2.0/resources/external-libraries";
	private static final String PLACEHOLDER = "${KNOWAGE_EXTERNAL_LIBRARY:logo.png}";

	@Test
	public void leavesReportsWithoutPlaceholdersUntouched() throws Exception {
		byte[] template = bytes("<report><property>unchanged &amp; original</property></report>");
		assertSame(template, ExternalLibraryTemplateResolver.resolve(template, null, null, () -> {
			throw new AssertionError("A plain report must not require a JWT");
		}));
	}

	@Test
	public void ignoresPlaceholdersInComments() throws Exception {
		byte[] template = bytes("<report><!-- " + PLACEHOLDER + " --></report>");
		assertSame(template, ExternalLibraryTemplateResolver.resolve(template, null, null, () -> {
			throw new AssertionError("Comments must not acquire credentials");
		}));
	}

	@Test
	public void resolvesXmlTextAttributesAndCdataWithoutDoubleEscaping() throws Exception {
		byte[] template = bytes("<report xmlns=\"http://www.eclipse.org/birt/2005/design\" resource=\"" + PLACEHOLDER
				+ "\"><property>" + PLACEHOLDER + "</property><expression><![CDATA[\"" + PLACEHOLDER
				+ "\"]]></expression></report>");
		byte[] original = template.clone();
		AtomicInteger tokenReads = new AtomicInteger();
		byte[] resolved = ExternalLibraryTemplateResolver.resolve(template, BASE, "X-Custom-Authorization", () -> {
			tokenReads.incrementAndGet();
			return "test.jwt.value";
		});
		String url = ENDPOINT + "?libraryName=logo.png&X-Custom-Authorization=test.jwt.value";
		Document document = parse(resolved);
		assertEquals(url, document.getDocumentElement().getAttribute("resource"));
		assertEquals(url, document.getElementsByTagName("property").item(0).getTextContent());
		assertEquals("\"" + url + "\"", document.getElementsByTagName("expression").item(0).getTextContent());
		assertEquals("http://www.eclipse.org/birt/2005/design", document.getDocumentElement().getNamespaceURI());
		assertEquals(1, tokenReads.get());
		assertArrayEquals(original, template);
	}

	@Test
	public void encodesFileNamesAndQueryParameters() throws Exception {
		byte[] template = bytes("<report><property>${KNOWAGE_EXTERNAL_LIBRARY:logo &amp; +#$'.png}</property></report>");
		byte[] resolved = ExternalLibraryTemplateResolver.resolve(template, BASE, "X Auth", () -> "a+b/c=d");
		assertEquals(ENDPOINT + "?libraryName=logo+%26+%2B%23%24%27.png&X+Auth=a%2Bb%2Fc%3Dd",
				parse(resolved).getElementsByTagName("property").item(0).getTextContent());
	}

	@Test
	public void preservesNonUtf8ReportCharacters() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-16\"?><report><property>" + PLACEHOLDER
				+ "</property><label>\u00e8</label></report>";
		byte[] resolved = ExternalLibraryTemplateResolver.resolve(xml.getBytes(StandardCharsets.UTF_16),
				BASE, "X-Kn-Authorization", () -> "test.jwt.value");
		assertEquals("\u00e8", parse(resolved).getElementsByTagName("label").item(0).getTextContent());
		assertFalse(new String(resolved, StandardCharsets.UTF_8).contains(PLACEHOLDER));
	}

	@Test
	public void isolatesEachExecutionAndResolvesMultipleFiles() throws Exception {
		byte[] template = bytes("<report><property>" + PLACEHOLDER
				+ " ${KNOWAGE_EXTERNAL_LIBRARY:banner.png}</property></report>");
		String first = new String(ExternalLibraryTemplateResolver.resolve(template, BASE, "X-Kn-Authorization",
				() -> "first.user.jwt"), StandardCharsets.UTF_8);
		String second = new String(ExternalLibraryTemplateResolver.resolve(template, BASE, "X-Kn-Authorization",
				() -> "second.user.jwt"), StandardCharsets.UTF_8);
		assertTrue(first.contains("libraryName=logo.png"));
		assertTrue(first.contains("libraryName=banner.png"));
		assertFalse(first.contains("second.user.jwt"));
		assertFalse(second.contains("first.user.jwt"));
		assertTrue(second.contains("second.user.jwt"));
		assertTrue(new String(template, StandardCharsets.UTF_8).contains(PLACEHOLDER));
	}

	@Test
	public void rejectsUnsafeOrMissingApiConfigurationBeforeReadingCredentials() {
		for (String base : new String[] { null, "", "/knowage-api", "http://reports.example/api",
				"https://user:password@reports.example/api", "https://reports.example/api?other=1",
				"https://reports.example/api#fragment" }) {
			assertThrows(IOException.class, () -> ExternalLibraryTemplateResolver.resolve(
					bytes("<report>" + PLACEHOLDER + "</report>"), base, "X-Kn-Authorization", () -> {
						throw new AssertionError("Invalid configuration must not acquire credentials");
					}));
		}
	}

	@Test
	public void rejectsMalformedPlaceholdersAndInvalidFileNames() {
		for (String name : new String[] { "", "../logo.png", "folder/logo.png", "folder\\logo.png", ".." }) {
			assertThrows(IOException.class, () -> ExternalLibraryTemplateResolver.resolve(
					bytes("<report>${KNOWAGE_EXTERNAL_LIBRARY:" + name + "}</report>"),
					BASE, "X-Kn-Authorization", () -> "test.jwt.value"));
		}
		assertThrows(IOException.class, () -> ExternalLibraryTemplateResolver.resolve(
				bytes("<report>${KNOWAGE_EXTERNAL_LIBRARY:logo.png</report>"),
				BASE, "X-Kn-Authorization", () -> "test.jwt.value"));
	}

	@Test
	public void rejectsMissingAuthorizationAndTokens() {
		byte[] template = bytes("<report>" + PLACEHOLDER + "</report>");
		assertThrows(IOException.class, () -> ExternalLibraryTemplateResolver.resolve(template, BASE, "",
				() -> "test.jwt.value"));
		assertThrows(IOException.class, () -> ExternalLibraryTemplateResolver.resolve(template, BASE,
				"X-Kn-Authorization", () -> null));
	}

	@Test
	public void rejectsExternalEntities() {
		byte[] template = bytes("<!DOCTYPE report [<!ENTITY file SYSTEM \"file:///not-to-be-read\">]>"
				+ "<report>&file;" + PLACEHOLDER + "</report>");
		assertThrows(IOException.class, () -> ExternalLibraryTemplateResolver.resolve(template, BASE,
				"X-Kn-Authorization", () -> {
					throw new AssertionError("Unsafe XML must not acquire credentials");
				}));
	}

	private static byte[] bytes(String value) {
		return value.getBytes(StandardCharsets.UTF_8);
	}

	private static Document parse(byte[] value) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		return factory.newDocumentBuilder().parse(new ByteArrayInputStream(value));
	}
}
