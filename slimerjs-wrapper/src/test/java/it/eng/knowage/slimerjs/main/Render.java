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

package it.eng.knowage.slimerjs.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

import it.eng.knowage.export.pdf.ExportDetails;
import it.eng.knowage.export.pdf.FrontpageDetails;
import it.eng.knowage.export.pdf.PDFCreator;
import it.eng.knowage.export.pdf.PageNumbering;
import it.eng.knowage.slimerjs.wrapper.RenderException;
import it.eng.knowage.slimerjs.wrapper.SlimerJS;
import it.eng.knowage.slimerjs.wrapper.beans.CustomHeaders;
import it.eng.knowage.slimerjs.wrapper.beans.RenderOptions;

/**
 * A command line tool for accessing the render method of the SlimerJS binary
 */
public class Render {
	/**
	 * Render the first argument to a PDF located at the second argument
	 *
	 * @param args
	 *            command line arguments
	 * @throws IOException
	 * @throws RenderException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws IOException, RenderException, URISyntaxException {
		Path front = Paths.get(Render.class.getClassLoader().getResource("it/eng/knowage/slimerjs/wrapper/Export_Front.pdf").toURI());
		Path back = Paths.get(Render.class.getClassLoader().getResource("it/eng/knowage/slimerjs/wrapper/Export_Back.pdf").toURI());
		Path output = Paths.get("C:\\temp\\" + UUID.randomUUID().toString() + ".pdf");
		URL url = new URL(
				"http://localhost:8080/knowagecockpitengine/api/1.0/pages/execute?user_id=biadmin&SPAGOBI_AUDIT_ID=28&DOCUMENT_LABEL=TestAssociative4Datasets&DOCUMENT_COMMUNITIES=%5B%5D&knowage_sys_country=US&DOCUMENT_IS_VISIBLE=true&SBI_EXECUTION_ROLE=admin&SBICONTEXT=%2Fknowage&knowage_sys_language=en&DOCUMENT_FUNCTIONALITIES=%5B3%5D&SBI_COUNTRY=US&DOCUMENT_AUTHOR=biadmin&DOCUMENT_DESCRIPTION=&document=3&IS_TECHNICAL_USER=true&SBI_SPAGO_CONTROLLER=%2Fservlet%2FAdapterHTTP&SBI_LANGUAGE=en&DOCUMENT_NAME=TestAssociative4Datasets&NEW_SESSION=TRUE&DOCUMENT_IS_PUBLIC=false&DOCUMENT_VERSION=7&SBI_HOST=http%3A%2F%2Flocalhost%3A8080&SBI_ENVIRONMENT=DOCBROWSER&SBI_EXECUTION_ID=d5e92863395511e7b26e6db3022acd33&EDIT_MODE=null&timereloadurl=1494842648408&export=true");
		Map<String, String> authenticationHeaders = new HashMap<>(1);
		String userId = "biadmin";
		String encodedUserId = Base64.encodeBase64String(userId.getBytes("UTF-8"));
		authenticationHeaders.put("Authorization", "Direct " + encodedUserId);
		List<InputStream> images = SlimerJS.render(url, 2,
				RenderOptions.DEFAULT.withCustomHeaders(new CustomHeaders(authenticationHeaders)).withJavaScriptExecutionDetails(5000L, 15000L));
		// List<InputStream> images = SlimerJS.render(urlQA, 1, RenderOptions.DEFAULT);
		PDFCreator.createPDF(images, output, true, true);
		ExportDetails details = new ExportDetails(new FrontpageDetails("Cool dashboard", "The most cool dashboard on earth", new Date()),
				PageNumbering.EXCLUDE_FIRST_AND_LAST);
		PDFCreator.addInformation(output, details);
		// Files.deleteIfExists(output);
	}
}