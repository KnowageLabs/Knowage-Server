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
package it.eng.spagobi.api.v2;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class MenuResourceTest {

	private Path resourceRoot;

	@After
	public void cleanUp() throws Exception {
		if (resourceRoot != null) {
			FileUtils.deleteDirectory(resourceRoot.toFile());
		}
	}

	@Test
	public void shouldReturnHtmlFileContent() throws Exception {
		resourceRoot = Files.createTempDirectory("menu-resource-test");
		Path staticMenuDirectory = Files.createDirectories(resourceRoot.resolve("static_menu"));
		Path htmlFile = staticMenuDirectory.resolve("maranza.html");
		Files.writeString(htmlFile, "<html>ciao</html>", StandardCharsets.UTF_8);
		mockResourcePath(resourceRoot);

		Response response = new MenuResource().getHTMLFile("maranza.html");

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertEquals("<html>ciao</html>", response.getEntity());
	}

	@Test
	public void shouldRejectInvalidHtmlFileName() throws Exception {
		resourceRoot = Files.createTempDirectory("menu-resource-test");
		mockResourcePath(resourceRoot);

		Response response = new MenuResource().getHTMLFile("../maranza.html");

		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	@Test
	public void shouldReturnNotFoundWhenHtmlFileDoesNotExist() throws Exception {
		resourceRoot = Files.createTempDirectory("menu-resource-test");
		Files.createDirectories(resourceRoot.resolve("static_menu"));
		mockResourcePath(resourceRoot);

		Response response = new MenuResource().getHTMLFile("missing.html");

		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}

	private void mockResourcePath(Path resourcePath) {
		final String mockedResourcePath = resourcePath.toString();
		new MockUp<SpagoBIUtilities>() {
			@Mock
			public String getResourcePath() {
				return mockedResourcePath;
			}
		};
	}
}
