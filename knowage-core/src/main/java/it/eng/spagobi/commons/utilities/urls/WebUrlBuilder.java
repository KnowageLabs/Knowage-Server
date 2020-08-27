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
package it.eng.spagobi.commons.utilities.urls;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import it.eng.LightNavigationConstants;
import it.eng.knowage.commons.security.KnowageSystemConfiguration;
import it.eng.knowage.wapp.Environment;
import it.eng.knowage.wapp.Version;
import it.eng.spago.configuration.ConfigSingleton;

/**
 * The implementation of IUrlBuilder used when SpagoBI is used as a STANDALONE WEB APPLICATION
 */
public class WebUrlBuilder implements IUrlBuilder {

	private static transient Logger logger = Logger.getLogger(WebUrlBuilder.class);

	private String baseURL = "";
	private String baseResourceURL = "";
	private String KNOWAGE_VERSION = Version.getCompleteVersion();
	private Environment ENVIRONMENT = Version.getEnvironment();

	private String[] regExpResources = { "/js/(src)", "/themes/commons/(css)/" };

	/**
	 * Inits the.
	 *
	 * @param aHttpServletRequest the a http servlet request
	 */
	public void init(HttpServletRequest aHttpServletRequest) {
		logger.debug("IN");
		baseResourceURL = KnowageSystemConfiguration.getKnowageContext() + "/";
		logger.debug("baseResourceURL" + baseResourceURL);
		baseURL = baseResourceURL + "servlet/AdapterHTTP";
		logger.debug("OUT.baseURL=" + baseURL);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.commons.utilities.urls.IUrlBuilder#getUrl(javax.servlet.http.HttpServletRequest, java.util.Map)
	 */
	@Override
	public String getUrl(HttpServletRequest aHttpServletRequest, Map parameters) {
		logger.debug("IN");
		init(aHttpServletRequest);
		// ConfigSingleton.getInstance().getAttribute(dal master fin qua SPAGO_ADAPTERHTTP_URL)
		StringBuffer sb = new StringBuffer();
		sb.append(baseURL);
		if (parameters != null) {
			Iterator keysIt = parameters.keySet().iterator();
			boolean isFirst = true;
			String paramName = null;
			Object paramValue = null;
			while (keysIt.hasNext()) {
				paramName = (String) keysIt.next();
				paramValue = parameters.get(paramName);
				if (paramValue == null) {
					logger.warn("Parameter with name " + paramName + " has null value. This parameter will be not considered.");
					continue;
				}
				if (isFirst) {
					sb.append("?");
					isFirst = false;
				} else {
					sb.append("&");
				}
				sb.append(paramName + "=" + paramValue.toString());
			}
		}
		// propagating light navigator id
		String lightNavigatorId = aHttpServletRequest.getParameter(LightNavigationConstants.LIGHT_NAVIGATOR_ID);
		if (lightNavigatorId != null && !lightNavigatorId.trim().equals("")) {
			if (sb.indexOf("?") != -1) {
				sb.append("&" + LightNavigationConstants.LIGHT_NAVIGATOR_ID + "=" + lightNavigatorId);
			} else {
				sb.append("?" + LightNavigationConstants.LIGHT_NAVIGATOR_ID + "=" + lightNavigatorId);
			}
		}
		String url = sb.toString();

		logger.debug("OUT.url=" + url);
		return url;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.commons.utilities.urls.IUrlBuilder#getResourceLink(javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public String getResourceLink(HttpServletRequest aHttpServletRequest, String originalUrl) {
		logger.debug("IN.originalUrl=" + originalUrl);
		init(aHttpServletRequest);
		originalUrl = originalUrl.trim();
		if (originalUrl.startsWith("/")) {
			originalUrl = originalUrl.substring(1);
		}
		originalUrl = baseResourceURL + originalUrl;
		if (ENVIRONMENT == Environment.PRODUCTION)
			originalUrl = concatSrcWithKnowageVersion(originalUrl);
		logger.debug("OUT.originalUrl=" + originalUrl);
		return originalUrl;
	}

	@Override
	public String getResourceLinkByTheme(HttpServletRequest aHttpServletRequest, String originalUrl, String theme) {
		logger.debug("IN");
		ConfigSingleton config = ConfigSingleton.getInstance();
		String rootPath = config.getRootPath();
		String urlByTheme = originalUrl;
		originalUrl.trim();
		if (originalUrl.startsWith("/"))
			originalUrl = originalUrl.substring(1);

		if (theme != null) {
			urlByTheme = "/themes/" + theme + "/" + originalUrl;
		}

		String urlComplete = rootPath + urlByTheme;
		// check if object exists
		File check = new File(urlComplete);
		// if file
		if (!check.exists()) {
			urlByTheme = "/themes/sbi_default/" + originalUrl;

			// check if the default object exist
			urlComplete = rootPath + urlByTheme;
			File checkDef = new File(urlComplete);
			// if file
			if (!checkDef.exists()) {
				urlByTheme = originalUrl;
			}
		}

		logger.debug("OUT");
		return getResourceLink(aHttpServletRequest, urlByTheme);
	}

	private String concatSrcWithKnowageVersion(String url) {
		logger.debug("IN");
		for (int i = 0; i < regExpResources.length; i++) {
			String pattern = regExpResources[i];
			Pattern srcPattern = Pattern.compile(pattern);
			Matcher srcMatcher = srcPattern.matcher(url);

			if (srcMatcher.find()) {
				String src = srcMatcher.group(1);
				url = url.replaceFirst(src, src + "-" + KNOWAGE_VERSION);
			}
		}

		logger.debug("OUT");
		return url;
	}

}
