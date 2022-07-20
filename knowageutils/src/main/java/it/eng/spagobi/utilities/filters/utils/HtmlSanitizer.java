package it.eng.spagobi.utilities.filters.utils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.owasp.html.HtmlChangeListener;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import com.google.common.base.Joiner;

import it.eng.spagobi.utilities.whitelist.IWhiteList;

public class HtmlSanitizer {

	private static final Logger LOGGER = Logger.getLogger(HtmlSanitizer.class);

	private static final Pattern IMG_SRC_DATA = Pattern.compile("^data:image/.*$");

	private PolicyFactory policy;

	private IWhiteList whiteList;

	public HtmlSanitizer(IWhiteList whiteList) {
		super();

		Objects.requireNonNull(whiteList);

		policy = new HtmlPolicyBuilder()
				.allowCommonBlockElements()
				.allowCommonInlineFormattingElements()
				.allowStandardUrlProtocols()
				.allowStyling()
				.allowAttributes("alt").onElements("img")
				.allowAttributes("height", "width").matching(Pattern.compile(".*")).onElements("img")
				.allowAttributes("class").globally()
				.allowAttributes("href").matching(this::isHrefAttributeInWhitelist).onElements("a")
				.allowAttributes("id").onElements("div")
				.allowAttributes("src").matching(this::isSrcAttributeInWhitelist).onElements("audio", "iframe", "img", "video")
				.allowAttributes("kn-cross", "kn-if", "kn-import", "kn-repeat", "kn-preview", "kn-selection-column", "kn-selection-value", "limit").globally()
				.allowElements("a", "audio", "article", "figure", "footer", "header", "iframe", "img", "pre", "span", "tbody", "tfoot", "thead", "table", "td", "th", "tr", "video")
				.allowUrlProtocols("data")
				.allowWithoutAttributes("figure", "span")
				.toFactory();

		this.whiteList = whiteList;

	}

	public String sanitize(String input) {

		LOGGER.debug("Sanitizing: " + input);

		String output = policy.sanitize(input, new HtmlChangeListener<Void>() {

			@Override
			public void discardedTag(Void context, String elementName) {
				LOGGER.debug("Discarded element: " + elementName);

			}

			@Override
			public void discardedAttributes(Void context, String tagName, String... attributeNames) {
				LOGGER.debug("In tag " + tagName + ", discarded attributes: " + Joiner.on(", ").join(attributeNames));
			}
		}, null);

		LOGGER.debug("End of sanitizing!");

		return output;
	}

	public boolean isSafe(String input) {

		LOGGER.debug("Checking: " + input);

		AtomicBoolean valid = new AtomicBoolean(true);

		policy.sanitize(input, new HtmlChangeListener<AtomicBoolean>() {

			@Override
			public void discardedTag(AtomicBoolean valid, String elementName) {
				LOGGER.debug("Discarded element: " + elementName);
				valid.set(false);
			}

			@Override
			public void discardedAttributes(AtomicBoolean valid, String tagName, String... attributeNames) {
				LOGGER.debug("In tag " + tagName + ", discarded attributes: " + Joiner.on(", ").join(attributeNames));
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

		boolean ret = isSrcData
				|| isInWhiteListAsExternalService
				|| isInWhiteListAsRelativePath;

		LOGGER.debug("Checking if " + url + " in src is in whitelist: " + ret);

		return ret;
	}

	private boolean isHrefAttributeInWhitelist(String url) {

		boolean isInWhiteListAsExternalService = isInWhiteListAsExternalService(url);
		boolean isInWhiteListAsRelativePath = isInWhiteListAsRelativePath(url);

		boolean ret = isInWhiteListAsExternalService
				|| isInWhiteListAsRelativePath;

		LOGGER.debug("Checking if " + url + " in href is in whitelist: " + ret);

		return ret;
	}

	private boolean isADataUrl(String url) {

		boolean ret = IMG_SRC_DATA.matcher(url)
				.matches();

		LOGGER.debug("Checking if " + url + " is a data URL: " + ret);

		return ret;
	}

	private boolean isInWhiteListAsExternalService(String url) {
		List<String> validValues = whiteList.getExternalServices();

		boolean ret = validValues.stream()
				.anyMatch(url::startsWith);

		LOGGER.debug("Checking if " + url + " is in whitelist as external service giving the following " + validValues + ": " + ret);

		return ret;
	}

	private boolean isInWhiteListAsRelativePath(String url) {
		List<String> validValues = whiteList.getRelativePaths();

		boolean ret = validValues.stream()
				.anyMatch(url::startsWith);

		LOGGER.debug("Checking if " + url + " is in whitelist as relative path giving the following " + validValues + ": " + ret);

		return ret;
	}
}
