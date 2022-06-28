package it.eng.knowage.boot.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.knowage.boot.utils.WhiteList;

public class XSSUtils {

	private static final Logger LOGGER = LogManager.getLogger(XSSUtils.class);

	private WhiteList whitelist = WhiteList.getInstance();

	private HtmlSanitizer sanitizer = new HtmlSanitizer(whitelist);

	/**
	 * @return the whitelist
	 */
	public WhiteList getWhitelist() {
		return whitelist;
	}

	/**
	 * @param whitelist the whitelist to set
	 */
	public void setWhitelist(WhiteList whitelist) {
		this.whitelist = whitelist;
	}

	public String stripXSS(String input) {

		LOGGER.debug("Sanitizing: {}", input);

		String output = sanitizer.sanitize(input);

		LOGGER.debug("Sanitized as: {}", input);

		return output;

	}

}
