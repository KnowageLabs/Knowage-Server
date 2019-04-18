/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.commons.utilities.urls;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import it.eng.knowage.wapp.Environment;
import it.eng.knowage.wapp.Version;

public class UrlBuilder {

	private static transient Logger logger = Logger.getLogger(UrlBuilder.class);

	private String KNOWAGE_VERSION = Version.getCompleteVersion();
	private Environment ENVIRONMENT = Version.getEnvironment();

	public UrlBuilder() {

	}

	public String getResourcePath(String contextpath, String url) {
		logger.debug("IN");
		String fullUrl = null;
		try {
			if (!url.startsWith("/"))
				url = "/" + url;

			fullUrl = contextpath + url;
			// In production mode (prod) create src-[version]
			if (ENVIRONMENT == Environment.PRODUCTION)
				fullUrl = concatSrcWithKnowageVersion(fullUrl);
		} catch (Exception e) {
			logger.error("Cannot build a resource url path", e);
		}

		logger.debug("OUT");
		return fullUrl;
	}

	private String concatSrcWithKnowageVersion(String url) {
		logger.debug("IN");
		Pattern srcPattern = Pattern.compile("/js/(src)");
		Matcher srcMatcher = srcPattern.matcher(url);

		if (srcMatcher.find()) {
			String src = srcMatcher.group(1);
			url = url.replace(src, src + "-" + KNOWAGE_VERSION);
		}

		logger.debug("OUT");
		return url;
	}

}
