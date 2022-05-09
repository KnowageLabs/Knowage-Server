/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.boot.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.dao.SbiConfigDao;
import it.eng.knowage.boot.dao.dto.SbiConfig;

@Component
@Order(2)
public class LocaleFilter implements Filter {

	private static final Logger LOGGER = Logger.getLogger(LocaleFilter.class);

	private static String KNOWAGE_LOCALE = "kn.lang";

	private static String KNOWAGE_DEFAULT_BE_LANGUAGE_LABEL = "SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE.default";

	@Autowired
	private BusinessRequestContext businessRequestContext;

	@Autowired
	private SbiConfigDao sbiConfigDao;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;

			Locale locale = null;
			String localeString = null;

			if (isCookiePresent(httpRequest, KNOWAGE_LOCALE)) {
				localeString = getCookieValue(httpRequest, KNOWAGE_LOCALE);
			} else {
				SbiConfig defaultBackendConfig = sbiConfigDao.findByLabel(KNOWAGE_DEFAULT_BE_LANGUAGE_LABEL);
				localeString = defaultBackendConfig.getValueCheck();
			}

			locale = Locale.forLanguageTag(localeString);
			businessRequestContext.setLocale(locale);

			LOGGER.debug("Locale used for this request: " + locale);

			chain.doFilter(request, response);

		} else {
			if (response instanceof HttpServletResponse) {
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.setStatus(400);
			}
		}
	}

	private boolean isCookiePresent(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]);

		return Arrays.asList(cookies)
			.stream()
			.anyMatch(e -> e.getName().equals(cookieName));
	}

	private String getCookieValue(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]);

		return Arrays.asList(cookies)
			.stream()
			.filter(e -> e.getName().equals(cookieName))
			.findFirst()
			.get()
			.getValue();
	}

}
