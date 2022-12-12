package it.eng.spagobi.utilities.filters;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import it.eng.spagobi.utilities.filters.utils.HtmlSanitizer;
import it.eng.spagobi.utilities.whitelist.WhiteListTest;

public class XSSRequestWrapperTest {

	private static final String VALID_AND_SANITIZED = Joiner.on('\n').join(
			"<h1>header</h1>",
			"<h2>header</h2>",
			"<h3>header</h3>",
			"<h4>header</h4>",
			"<h5>header</h5>",
			"<h6>header</h6>",
			"<div>div</div>", "<div></div>",
			"<span>span</span>", "<span></span>",
			"<p>p</p>", "<p></p>",
			"<b>b</b>", "<b></b>",
			"<i>i</i>", "<i></i>",
			"<pre>pre</pre>", "<pre></pre>",
			"<strong>strong</strong>", "<strong></strong>",
			"<article>article</article>", "<article></article>",
			"<footer>footer</footer>", "<footer></footer>",
			"<header>header</header>", "<header></header>",
			// java-html-sanitizer seems to remove the line breaks in this part
			("<table>"
				+ "<thead>"
				+ "<tr>"
				+ "<th>Month</th>"
				+ "<th>Savings</th>"
				+ "</tr>"
				+ "</thead>"
				+ "<tbody>"
				+ "<tr>"
				+ "<td>January</td>"
				+ "<td>$100</td>"
				+ "</tr>"
				+ "<tr>"
				+ "<td>February</td>"
				+ "<td>$80</td>"
				+ "</tr>"
				+ "</tbody>"
				+ "<tfoot>"
				+ "<tr>"
				+ "<td>Sum</td>"
				+ "<td>$180</td>"
				+ "</tr>"
				+ "</tfoot>"
				+ "</table>"),
			"<img src=\"data:image/png;base64,cyvgyhbj\" />",
			"<img src=\"https://www.youtube.com/image.png\" />",
			"<iframe src=\"https://www.youtube.com/image.png\"></iframe>",
			"<audio src=\"https://www.youtube.com/image.png\"></audio>",
			"<video src=\"https://www.youtube.com/image.png\"></video>",
			"<a href=\"https://www.youtube.com/mylink\">anchor</a>","<a href=\"/knowage/icons/test.ico\">anchor</a>",
			"<div kn-repeat=\"true\" limit=\"1\"></div>",
			"<img height=\"10px\" width=\"10px\" alt=\"text\" />",
			"<img src=\"data:image/png;base64,iVBORw0KGg\" />",
			"<kn-import src=\"https://www.youtube.com/image.png\"></kn-import>",
			"<div style=\"flex:1\"></div>",
			"<div style=\"cursor:pointer;flex:1\"></div>",
			"<div style=\"cursor:move\"></div>",
			"");

	private static final String VALID_BUT_NOT_SANITIZED = Joiner.on('\n').join(
			"<h1>header</h1>",
			"<h2>header</h2>",
			"<h3>header</h3>",
			"<h4>header</h4>",
			"<h5>header</h5>",
			"<h6>header</h6>",
			"<div>div</div>", "<div></div>",
			"<span>span</span>", "<span></span>",
			"<p>p</p>", "<p></p>",
			"<b>b</b>", "<b></b>",
			"<i>i</i>", "<i></i>",
			"<pre>pre</pre>", "<pre></pre>",
			"<strong>strong</strong>", "<strong></strong>",
			"<article>article</article>", "<article></article>",
			"<footer>footer</footer>", "<footer></footer>",
			"<header>header</header>", "<header></header>",
			// java-html-sanitizer seems to remove the line breaks in this part
			("<table>"
				+ "<thead>"
				+ "<tr>"
				+ "<th>Month</th>"
				+ "<th>Savings</th>"
				+ "</tr>"
				+ "</thead>"
				+ "<tbody>"
				+ "<tr>"
				+ "<td>January</td>"
				+ "<td>$100</td>"
				+ "</tr>"
				+ "<tr>"
				+ "<td>February</td>"
				+ "<td>$80</td>"
				+ "</tr>"
				+ "</tbody>"
				+ "<tfoot>"
				+ "<tr>"
				+ "<td>Sum</td>"
				+ "<td>$180</td>"
				+ "</tr>"
				+ "</tfoot>"
				+ "</table>"),
			"<img src=\"data:image/png;base64,cyvgyhbj\" />",
			"<img src=\"https://www.youtube.com/image.png\" />",
			"<iframe src=\"https://www.youtube.com/image.png\"></iframe>",
			"<audio src=\"https://www.youtube.com/image.png\"></audio>",
			"<video src=\"https://www.youtube.com/image.png\"></video>",
			"<a href=\"https://www.youtube.com/mylink\">anchor</a>","<a href=\"/knowage/icons/test.ico\">anchor</a>",
			"<p>\"'=</p>",
			"<img src=\"https://www.youtube.com/image.png\" alt=\"pippo & pluto = paperino ' topolino\" />",
			"");

	private static final String NOT_OK_1 = Joiner.on('\n').join(
			"<img src=\"https://not-in-whitelist.com/image.png\" />",
			"<img src=\"/very-bad-relative-path-image.png\" />",
			"");

	private HtmlSanitizer sanitizer;

	@Before
	public void init() {
		sanitizer = new HtmlSanitizer(new WhiteListTest());
	}

	@Test
	public void test01() {

		String input = VALID_AND_SANITIZED;

		String output = sanitizer.sanitize(input);

		assertEquals(input, output);

	}

	@Test
	public void testTemplatesOnFileSystem() throws IOException {

		Path resourceDirectory = Paths.get("src","test","resources", "html-test-snippets");

		List<Path> paths = Files.list(resourceDirectory)
			.collect(toList());

		for (Path path : paths) {

			String input = new String(Files.readAllBytes(path));

			String output = sanitizer.sanitize(input);

			boolean safe = sanitizer.isSafe(input);

			System.out.println(output);

			assertEquals("File " + path + " doesn't pass the sanitization", input, output);
			assertEquals("File " + path + " doesn't pass the sanitization", true, safe);
		}

	}

	@Test
	public void testWidgets() throws IOException {

		ObjectMapper bm = new ObjectMapper();

		Path resourceDirectory = Paths.get("src","test","resources", "widgets");

		List<Path> paths = Files.list(resourceDirectory)
			.collect(toList());

		for (Path path : paths) {

			Map<String, Object> readedValue = bm.readValue(Files.newInputStream(path), Map.class);

			Map<String, String> code = (Map<String, String>) readedValue.get("code");

			String input = code.get("html");

			String output = sanitizer.sanitize(input);

			boolean safe = sanitizer.isSafe(input);

			System.out.println(output);

//			assertEquals("File " + path + " doesn't pass the sanitization", input, output);
			assertEquals("File " + path + " doesn't pass the sanitization", true, safe);
		}

	}

	@Test
	public void test01_1() {

		String input = VALID_BUT_NOT_SANITIZED;

		boolean output = sanitizer.isSafe(input);

		assertEquals(output, true);

	}

	@Test
	public void test02() {

		String input = NOT_OK_1;

		String output = sanitizer.sanitize(input);

		assertNotEquals(input, output);

	}

}
