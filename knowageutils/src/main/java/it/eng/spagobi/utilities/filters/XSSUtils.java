package it.eng.spagobi.utilities.filters;

import org.apache.log4j.Logger;

import it.eng.spagobi.utilities.filters.utils.HtmlSanitizer;
import it.eng.spagobi.utilities.whitelist.WhiteList;

public class XSSUtils {

	private static final Logger LOGGER = Logger.getLogger(XSSUtils.class);

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

	public String sanitize(String input) {

		LOGGER.debug("Sanitizing: " + input);

		String output = sanitizer.sanitize(input);

		LOGGER.debug("Sanitized as: " + output);

		return output;

	}

	public boolean isSafe(String input) {

		LOGGER.debug("Checking: " + input);

		return sanitizer.isSafe(input);

	}

}
