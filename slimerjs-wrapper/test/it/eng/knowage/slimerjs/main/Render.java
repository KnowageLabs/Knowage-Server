package it.eng.knowage.slimerjs.main;

import it.eng.knowage.export.pdf.PDFCreator;
import it.eng.knowage.slimerjs.wrapper.RenderException;
import it.eng.knowage.slimerjs.wrapper.SlimerJS;
import it.eng.knowage.slimerjs.wrapper.beans.CustomHeaders;
import it.eng.knowage.slimerjs.wrapper.beans.RenderOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

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
				"http://localhost:8080/knowagecockpitengine/api/1.0/pages/execute?EXPORT_MODE=true&user_id=biadmin&SPAGOBI_AUDIT_ID=28&DOCUMENT_LABEL=TestAssociative4Datasets&DOCUMENT_COMMUNITIES=%5B%5D&knowage_sys_country=US&DOCUMENT_IS_VISIBLE=true&SBI_EXECUTION_ROLE=admin&SBICONTEXT=%2Fknowage&knowage_sys_language=en&DOCUMENT_FUNCTIONALITIES=%5B3%5D&SBI_COUNTRY=US&DOCUMENT_AUTHOR=biadmin&DOCUMENT_DESCRIPTION=&document=3&IS_TECHNICAL_USER=true&SBI_SPAGO_CONTROLLER=%2Fservlet%2FAdapterHTTP&SBI_LANGUAGE=en&DOCUMENT_NAME=TestAssociative4Datasets&NEW_SESSION=TRUE&DOCUMENT_IS_PUBLIC=false&DOCUMENT_VERSION=7&SBI_HOST=http%3A%2F%2Flocalhost%3A8080&SBI_ENVIRONMENT=DOCBROWSER&SBI_EXECUTION_ID=d5e92863395511e7b26e6db3022acd33&EDIT_MODE=null&timereloadurl=1494842648408&export=true");
		URL urlQA = new URL("http://192.168.29.53:888/qa");
		Map<String, String> authenticationHeaders = new HashMap<String, String>(1);
		String userId = "biadmin";
		String encodedUserId = Base64.encodeBase64String(userId.getBytes("UTF-8"));
		authenticationHeaders.put("Authorization", "Direct " + encodedUserId);
		List<InputStream> images = SlimerJS.render(url, 2, RenderOptions.DEFAULT.withCustomHeaders(new CustomHeaders(authenticationHeaders))
				.withJavaScriptExecutionDetails(20000L, 20000L));
		// List<InputStream> images = SlimerJS.render(urlQA, 1, RenderOptions.DEFAULT);
		PDFCreator.createPDF(images, output, front, back);
		// Files.deleteIfExists(output);
	}
}