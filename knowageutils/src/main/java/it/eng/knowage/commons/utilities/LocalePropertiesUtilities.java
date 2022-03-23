/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

/**
 *
 */
package it.eng.knowage.commons.utilities;

import java.util.Locale;
import java.util.Locale.Builder;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spago.base.SessionContainer;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author albnale
 *
 */
public class LocalePropertiesUtilities {
	private static transient Logger logger = Logger.getLogger(LocalePropertiesUtilities.class);

	public static String getScriptFromLocale(SessionContainer permSess) {
		String toReturn = "";
		String script = (String) permSess.getAttribute(SpagoBIConstants.AF_SCRIPT);

		if (StringUtils.isNotBlank(script)) {
			toReturn = script + "-";
		}

		return toReturn;

	}

	public static String getAngularPropertiesFileName(Locale locale, String separator) {
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String script = locale.getScript();

		StringBuilder sb = new StringBuilder();
		sb.append(language);
		sb.append(separator);
		if (StringUtils.isNotBlank(script)) {
			sb.append(script);
			sb.append(separator);
		}
		sb.append(country);

		return sb.toString();
	}

	public static String getAngularPropertiesFileName(String currLanguage, String currScript, String currCountry, String separator) {
		Locale locale = null;
		if (StringUtils.isAnyBlank(currLanguage, currCountry)) {
			locale = getDefaultLocale();
		} else {
			Builder builder = new Builder().setLanguage(currLanguage).setRegion(currCountry);
			if (StringUtils.isNotBlank(separator)) {
				builder = builder.setScript(currScript);
			}
			locale = builder.build();
		}
		return "/js/lib/angular-localization/" + getAngularPropertiesFileName(locale, separator) + ".js";
	}

	public static Locale getDefaultLocale() {
		logger.trace("IN");
		Locale locale = null;
		try {
			String defaultLanguageTag = SingletonConfig.getInstance().getConfigValue("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE.default");
			String languageTag = StringUtils.isNotBlank(defaultLanguageTag) ? defaultLanguageTag : "en-US";
			logger.trace("Default locale found: " + languageTag);
			locale = Locale.forLanguageTag(languageTag);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Error while getting default locale", t);
		}
		logger.debug("OUT:" + locale.toString());
		return locale;
	}

}
