package it.eng.knowage.slimerjs.main;

import it.eng.knowage.slimerjs.wrapper.RenderException;
import it.eng.knowage.slimerjs.wrapper.SlimerJS;
import it.eng.knowage.slimerjs.wrapper.beans.CustomHeaders;
import it.eng.knowage.slimerjs.wrapper.beans.RenderOptions;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
	 */
	public static void main(String[] args) throws IOException, RenderException {
		// try (final InputStream html = Files.newInputStream(Paths.get(args[0]));
		// final InputStream pdf = SlimerJS.render(null, html, PaperSize.Letter, ViewportDimensions.VIEW_1280_1024, Margin.ZERO, BannerInfo.EMPTY,
		// BannerInfo.EMPTY, RenderFormat.PDF, 10000L, 100L);) {
		//
		// Path dest = Paths.get(args[1]);
		// Files.deleteIfExists(dest);
		// Files.copy(pdf, dest);
		//
		// } catch (IOException | RenderException e) {
		// e.printStackTrace();
		// }

		URL url = new URL(
				"http://localhost:8080/knowagecockpitengine/api/1.0/pages/execute?EXPORT_MODE=true&user_id=biadmin&SPAGOBI_AUDIT_ID=28&DOCUMENT_LABEL=TestAssociative4Datasets&DOCUMENT_COMMUNITIES=%5B%5D&knowage_sys_country=US&DOCUMENT_IS_VISIBLE=true&SBI_EXECUTION_ROLE=admin&SBICONTEXT=%2Fknowage&knowage_sys_language=en&DOCUMENT_FUNCTIONALITIES=%5B3%5D&SBI_COUNTRY=US&DOCUMENT_AUTHOR=biadmin&DOCUMENT_DESCRIPTION=&document=3&IS_TECHNICAL_USER=true&SBI_SPAGO_CONTROLLER=%2Fservlet%2FAdapterHTTP&SBI_LANGUAGE=en&DOCUMENT_NAME=TestAssociative4Datasets&NEW_SESSION=TRUE&DOCUMENT_IS_PUBLIC=false&DOCUMENT_VERSION=7&SBI_HOST=http%3A%2F%2Flocalhost%3A8080&SBI_ENVIRONMENT=DOCBROWSER&SBI_EXECUTION_ID=d5e92863395511e7b26e6db3022acd33&EDIT_MODE=null&timereloadurl=1494842648408");

		Map<String, String> authenticationHeaders = new HashMap<String, String>(1);
		String userId = "biadmin";
		String encodedUserId = Base64.getEncoder().encodeToString(userId.getBytes("UTF-8"));
		authenticationHeaders.put("Authorization", "Direct " + encodedUserId);

		SlimerJS.render(url, 2, RenderOptions.DEFAULT.withCustomHeaders(new CustomHeaders(authenticationHeaders)));
		// SlimerJS.exec(SlimerJS.class.getResourceAsStream("screenshot.js"), SlimerJSOptions.DEFAULT);
	}
}