package it.eng.knowage.boot.filter;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.html.HtmlChangeListener;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import com.google.common.base.Joiner;

public class HtmlSanitizer {

	private static final Logger LOGGER = LogManager.getLogger(HtmlSanitizer.class);

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
				.allowAttributes("class").onElements("div", "figure", "img", "p", "span")
				.allowAttributes("height").matching(Pattern.compile(".*")).onElements("img")
				.allowAttributes("href").matching(this::isHrefAttributeInWhitelist).onElements("a")
				.allowAttributes("id").onElements("div")
				.allowAttributes("src").matching(this::isSrcAttributeInWhitelist).onElements("audio", "iframe", "img", "video")
				.allowElements("a", "audio", "article", "figure", "footer", "header", "iframe", "img", "pre", "span", "tbody", "tfoot", "thead", "table", "td", "th", "tr", "video")
				.allowUrlProtocols("data")
				.allowWithoutAttributes("figure", "span")
				.toFactory();

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

		boolean ret = isSrcData
				|| isInWhiteListAsExternalService
				|| isInWhiteListAsRelativePath;

		LOGGER.debug("Checking if {} in src is in whitelist: {} ", url, ret);

		return ret;
	}

	private boolean isHrefAttributeInWhitelist(String url) {

		boolean isInWhiteListAsExternalService = isInWhiteListAsExternalService(url);
		boolean isInWhiteListAsRelativePath = isInWhiteListAsRelativePath(url);

		boolean ret = isInWhiteListAsExternalService
				|| isInWhiteListAsRelativePath;

		LOGGER.debug("Checking if {} in href is in whitelist: {} ", url, ret);

		return ret;
	}

	private boolean isADataUrl(String url) {

		boolean ret = IMG_SRC_DATA.matcher(url)
				.matches();

		LOGGER.debug("Checking if {} is a data URL: {} ", url, ret);

		return ret;
	}

	private boolean isInWhiteListAsExternalService(String url) {
		List<String> validValues = whiteList.getExternalServices();

		boolean ret = validValues.stream()
				.anyMatch(url::startsWith);

		LOGGER.debug("Checking if {} is in whitelist as external service giving the following {}: {} ", url, validValues, ret);

		return ret;
	}

	private boolean isInWhiteListAsRelativePath(String url) {
		List<String> validValues = whiteList.getRelativePaths();

		boolean ret = validValues.stream()
				.anyMatch(url::startsWith);

		LOGGER.debug("Checking if {} is in whitelist as relative path giving the following {}: {} ", url, validValues, ret);

		return ret;
	}
}
