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
package it.eng.spagobi.wapp.services;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import it.eng.knowage.commons.utilities.KnLanguageCookie;
import it.eng.knowage.commons.utilities.LocalePropertiesUtilities;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;

public class ChangeLanguage extends AbstractSpagoBIAction {

	private static Logger logger = Logger.getLogger(ChangeLanguage.class);

	@Override
	public void doService() {
		logger.debug("IN");
		RequestContainer reqCont = RequestContainer.getRequestContainer();
		SessionContainer sessCont = reqCont.getSessionContainer();
		SessionContainer permSess = sessCont.getPermanentContainer();

		String language = getAttributeAsString("language_id");
		Assert.assertNotBlank(language, "language_id not specified");
		String country = getAttributeAsString("country_id");
		Assert.assertNotBlank(country, "country_id not specified");
		String script = getAttributeAsString("script_id");
		logger.debug("Language selected: " + language);

		Locale locale = findLocale(language, country, script);
		Assert.assertNotNull(locale, String.format("Locale [%s,%s,%s] not found", language, country, script));

		String scriptForSession = StringUtils.isNotBlank(locale.getScript()) ? locale.getScript() : "";
		permSess.setAttribute("AF_LANGUAGE", locale.getLanguage());
		permSess.setAttribute("AF_COUNTRY", locale.getCountry());
		permSess.setAttribute("AF_SCRIPT", scriptForSession);

		UserProfile userProfile = (UserProfile) this.getUserProfile();
		userProfile.setAttributeValue(SpagoBIConstants.LANGUAGE, locale.getLanguage());
		userProfile.setAttributeValue(SpagoBIConstants.COUNTRY, locale.getCountry());
		userProfile.setAttributeValue(SpagoBIConstants.SCRIPT, scriptForSession);

		String knLanguage = LocalePropertiesUtilities.getAngularPropertiesFileName(locale, "-");
		HttpServletResponse resp = getHttpResponse();
		KnLanguageCookie.setCookie(resp, knLanguage);

		try {
			writeBackToClient(new JSONAcknowledge());
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Impossible to write back the responce to the client", e);
		}

		logger.debug("OUT");
	}

	protected Locale findLocale(String language, String country, String script) {
		List<Locale> supportedLocales = GeneralUtilities.getSupportedLocales();

		//@formatter:off
		return supportedLocales
				.stream()
				.filter(l ->
					l.getLanguage().equals(language) && l.getCountry().equals(country) && (StringUtils.isBlank(script) || l.getScript().equals(script))
				)
				.findFirst()
				.orElse(null);
		//@formatter:on

	}

}
