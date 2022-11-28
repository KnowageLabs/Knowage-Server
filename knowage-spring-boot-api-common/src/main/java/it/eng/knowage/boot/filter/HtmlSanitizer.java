package it.eng.knowage.boot.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.html.CssSchema;
import org.owasp.html.CssSchema.Property;
import org.owasp.html.HtmlChangeListener;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class HtmlSanitizer {

	private static final Logger LOGGER = LogManager.getLogger(HtmlSanitizer.class);

	private static final Pattern IMG_SRC_DATA = Pattern.compile("^data:image/.*$");

	private PolicyFactory policy;

	private IWhiteList whiteList;

	public HtmlSanitizer(IWhiteList whiteList) {
		super();

		Objects.requireNonNull(whiteList);

		Map<String, Property> stylePropertiesInSVGMap = new HashMap<>();

		Property allValsProperty = new Property(258, ImmutableSet.of(), ImmutableMap.<String, String>of());
		Property fillOpacityProperty = new Property(1, ImmutableSet.of(), ImmutableMap.of());
		Property opacityProperty = new Property(1, ImmutableSet.of(), ImmutableMap.of());
		Property paintOrderProperty = new Property(23, ImmutableSet.of("markers", "fill", "stroke"), ImmutableMap.of());
		Property strokeProperty = new Property(1, ImmutableSet.of("none"), ImmutableMap.of());
		Property strokeLineCapProperty = new Property(1, ImmutableSet.of("butt"), ImmutableMap.of());
		Property strokeLineJoinProperty = new Property(1, ImmutableSet.of("miter"), ImmutableMap.of());
		Property strokeOpacityProperty = new Property(1, ImmutableSet.of(), ImmutableMap.of());
		Property strokeWidthProperty = new Property(1, ImmutableSet.of("auto", "inherit"), ImmutableMap.of());

		stylePropertiesInSVGMap.put("fill", allValsProperty);
		stylePropertiesInSVGMap.put("fill-opacity", fillOpacityProperty);
		stylePropertiesInSVGMap.put("opacity", opacityProperty);
		stylePropertiesInSVGMap.put("paint-order", paintOrderProperty);
		stylePropertiesInSVGMap.put("stroke", strokeProperty);
		stylePropertiesInSVGMap.put("stroke-linecap", strokeLineCapProperty);
		stylePropertiesInSVGMap.put("stroke-linejoin", strokeLineJoinProperty);
		stylePropertiesInSVGMap.put("stroke-opacity", strokeOpacityProperty);
		stylePropertiesInSVGMap.put("stroke-width", strokeWidthProperty);

		CssSchema stylePropertiesInSVG = CssSchema.withProperties(stylePropertiesInSVGMap);

		// @formatter:off
		policy = new HtmlPolicyBuilder()
				.allowCommonBlockElements()
				.allowCommonInlineFormattingElements()
				.allowStandardUrlProtocols()
				.allowStyling(CssSchema.union(CssSchema.DEFAULT, stylePropertiesInSVG))
				.allowElements("a", "audio", "article", "figure", "footer", "header", "hr", "iframe", "input", "img", "kn-import", "label", "pre", "span", "tbody", "tfoot", "thead", "table", "td", "th", "tr", "video","canvas","fieldset")
				.allowAttributes("alt").onElements("img")
				.allowAttributes("aria-label", "aria-hidden").globally()
				.allowAttributes("colspan","rowspan").onElements("td","th")
				.allowAttributes("height", "width").globally()
				.allowAttributes("class").globally()
				.allowAttributes("id").onElements("div")
				.allowAttributes("href").matching(this::isHrefAttributeInWhitelist).onElements("a")
				.allowAttributes("src").matching(this::isSrcAttributeInWhitelist).onElements("audio", "iframe", "img", "kn-import", "video")
				.allowAttributes("title").globally()
				.allowAttributes("type", "value", "min", "max").onElements("input")
				.allowAttributes("for").onElements("label")
				.allowAttributes("frameborder", "allow", "allowfullscreen").onElements("iframe")
				.allowAttributes("target").onElements("a")
				.allowWithoutAttributes("figure", "span")
				.allowUrlProtocols("data")
				// Knowage
				.allowAttributes("kn-cross", "kn-if", "kn-import", "kn-repeat", "kn-preview", "kn-selection-column", "kn-selection-value", "limit").globally()
				// SVG
				.allowElements("circle", "defs", "foreignobject", "g", "metadata", "path", "svg", "text", "tspan")
				.allowElements("dc:format", "dc:title", "dc:type")
				.allowElements("cc:Work")
				.allowElements("rdf:RDF")
				.allowAttributes("aria-labelledby", "cx", "cy", "d", "fill", "id", "r", "role", "stroke", "stroke-dasharray", "stroke-dashoffset", "stroke-width", "transform", "viewBox", "version", "x", "y").globally()
				.allowAttributes("rdf:about", "rdf:resource").globally()
				.allowAttributes("sodipodi:docname").globally()
				.allowAttributes("xml:space").globally()
				.allowAttributes("xmlns", "xmlns:dc", "xmlns:cc", "xmlns:inkscape", "xmlns:rdf", "xmlns:sodipodi", "xmlns:svg").globally()
				// Inkscape
				.allowElements("sodipodi:namedview")
				.allowAttributes("pagecolor", "bordercolor", "borderopacity", "showgrid", "fit-margin-top", "fit-margin-left", "fit-margin-right", "fit-margin-bottom").onElements("sodipodi:namedview")
				.allowAttributes("inkscape:connector-curvature", "inkscape:label", "inkscape:groupmode", "inkscape:version").globally()
				.allowAttributes("sodipodi:nodetypes", "sodipodi:role").globally()
				// font (even if it is not supported by HTML 5)
				.allowElements("font")
				.allowAttributes("size", "face", "color").onElements("font")
				//
				.toFactory();
		// @formatter:on

		this.whiteList = whiteList;

	}

	public String sanitize(String input) {

		LOGGER.debug("Sanitizing: {}", input);

		String output = policy.sanitize(input, new HtmlChangeListener<Void>() {

			@Override
			public void discardedTag(Void context, String elementName) {
				LOGGER.debug("Discarded element: {}", elementName);

			}

			@Override
			public void discardedAttributes(Void context, String tagName, String... attributeNames) {
				LOGGER.debug("In tag {}, discarded attributes: {}", tagName, Joiner.on(", ").join(attributeNames));
			}
		}, null);

		LOGGER.debug("End of sanitizing!");

		return output;
	}

	public boolean isSafe(String input) {

		LOGGER.debug("Checking: {}", input);

		AtomicBoolean valid = new AtomicBoolean(true);

		policy.sanitize(input, new HtmlChangeListener<AtomicBoolean>() {

			@Override
			public void discardedTag(AtomicBoolean valid, String elementName) {
				LOGGER.debug("Discarded element: {}", elementName);
				valid.set(false);
			}

			@Override
			public void discardedAttributes(AtomicBoolean valid, String tagName, String... attributeNames) {
				LOGGER.debug("In tag {}, discarded attributes: {}", tagName, Joiner.on(", ").join(attributeNames));
				valid.set(false);
			}

		}, valid);

		LOGGER.debug("End of checking!");

		return valid.get();
	}

	private boolean isSrcAttributeInWhitelist(String url) {

		boolean isSrcData = isADataUrl(url);
		boolean isInWhiteListAsExternalService = isInWhiteListAsExternalService(url);
		boolean isInWhiteListAsRelativePath = isInWhiteListAsRelativePath(url);

		// @formatter:off
		boolean ret = isSrcData
				|| isInWhiteListAsExternalService
				|| isInWhiteListAsRelativePath;
		// @formatter:on

		LOGGER.debug("Checking if {} in src is in whitelist: {} ", url, ret);

		return ret;
	}

	private boolean isHrefAttributeInWhitelist(String url) {

		boolean isInWhiteListAsExternalService = isInWhiteListAsExternalService(url);
		boolean isInWhiteListAsRelativePath = isInWhiteListAsRelativePath(url);

		// @formatter:off
		boolean ret = isInWhiteListAsExternalService
				|| isInWhiteListAsRelativePath;
		// @formatter:on

		LOGGER.debug("Checking if {} in href is in whitelist: {} ", url, ret);

		return ret;
	}

	private boolean isADataUrl(String url) {

		boolean ret = IMG_SRC_DATA.matcher(url).matches();

		LOGGER.debug("Checking if {} is a data URL: {} ", url, ret);

		return ret;
	}

	private boolean isInWhiteListAsExternalService(String url) {
		List<String> validValues = whiteList.getExternalServices();

		boolean ret = validValues.stream().anyMatch(url::startsWith);

		LOGGER.debug("Checking if {} is in whitelist as external service giving the following {}: {} ", url, validValues, ret);

		return ret;
	}

	private boolean isInWhiteListAsRelativePath(String url) {
		List<String> validValues = whiteList.getRelativePaths();

		boolean ret = validValues.stream().anyMatch(url::startsWith);

		LOGGER.debug("Checking if {} is in whitelist as relative path giving the following {}: {} ", url, validValues, ret);

		return ret;
	}
}
