package it.eng.knowage.slimerjs.main;

import it.eng.knowage.slimerjs.wrapper.SlimerJS;
import it.eng.knowage.slimerjs.wrapper.beans.SlimerJSOptions;

import java.io.IOException;

/**
 * A command line tool for accessing the render method of the phantomJS binary
 */
public class Render {
	/**
	 * Render the first argument to a PDF located at the second argument
	 *
	 * @param args
	 *            command line arguments
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
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

		SlimerJS.exec(SlimerJS.class.getResourceAsStream("screenshot.js"), SlimerJSOptions.DEFAULT);
	}
}