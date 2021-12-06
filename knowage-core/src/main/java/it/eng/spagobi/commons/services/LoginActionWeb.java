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
package it.eng.spagobi.commons.services;

import java.io.IOException;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spago.base.Constants;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.SessionUserProfileBuilder;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIServiceExceptionHandler;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.services.security.service.SecurityServiceSupplierFactory;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.AbstractBaseHttpAction;
import it.eng.spagobi.utilities.service.JSONSuccess;
import it.eng.spagobi.wapp.services.ChangeTheme;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it) This action is used by SpagoBISDK module.
 */
public class LoginActionWeb extends AbstractBaseHttpAction {

	public static final String SERVICE_NAME = "LOGIN_ACTION_WEB";

	// configuration parameter
	public static String SSO_ACTIVE = "SPAGOBI_SSO.ACTIVE";

	// request parameters
	public static String CALLBACK = "callback";

	public static String USER_ID = "user";
	public static String PASSWORD = "password";

	public static String THEME = ChangeTheme.THEME_NAME;
	public static String BACK_URL = SpagoBIConstants.BACK_URL;

	// logger component
	static Logger logger = Logger.getLogger(LoginActionWeb.class);

	IEngUserProfile profile = null;

	@Override
	public void service(SourceBean request, SourceBean response) throws SpagoBIServiceException {
		String callback = null;
		String theme;
		String backUrl;
		boolean isSSOActive = false;
		String usr = null;
		String pwd;

		RequestContainer requestContainer = this.getRequestContainer();
		SessionContainer session = requestContainer.getSessionContainer();
		SessionContainer permanentSession = session.getPermanentContainer();
		profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		logger.debug("IN");

		try {
			setSpagoBIRequestContainer(request);
			setSpagoBIResponseContainer(response);

			Assert.assertNotNull(ConfigSingleton.getInstance(), "Impossible to load SpagoBI configuration file");

			callback = getAttributeAsString(CALLBACK);
			logger.debug("Parameter [" + CALLBACK + "] is equals to [" + callback + "]");

			usr = getAttributeAsString(USER_ID);
			logger.debug("Parameter [" + USER_ID + "] is equals to [" + usr + "]");

			pwd = getAttributeAsString(PASSWORD);
			logger.debug("Parameter [" + PASSWORD + "] is equals to [" + (StringUtilities.isEmpty(pwd) ? "null" : "*******") + "]"); // do not log pwd !

			theme = getAttributeAsString(THEME);
			logger.debug("Parameter [" + THEME + "] is equals to [" + theme + "]");

			backUrl = getAttributeAsString(BACK_URL);
			logger.debug("Parameter [" + BACK_URL + "] is equals to [" + backUrl + "]");

			String activeStr = SingletonConfig.getInstance().getConfigValue("SPAGOBI_SSO.ACTIVE");

			if (activeStr != null && activeStr.equalsIgnoreCase("true")) {
				isSSOActive = true;
			}
			logger.debug("Configuration parameter [" + SSO_ACTIVE + "] is equals to [" + isSSOActive + "]");
			logger.info("SSO is " + (isSSOActive ? "enabled" : "disabled"));

			String userUniqueIdentifier = null;
			// If SSO is not active, check username and password, i.e. performs the authentication;
			// instead, if SSO is active, the authentication mechanism is provided by the SSO itself, so SpagoBI does not make
			// any authentication, just creates the user profile object and puts it into Spago permanent container
			if (!isSSOActive) {
				ISecurityServiceSupplier supplier = SecurityServiceSupplierFactory.createISecurityServiceSupplier();

				Assert.assertNotNull(usr, "User identifier not spicified");
				Assert.assertNotNull(usr, "Password not spicified");

				SpagoBIUserProfile userProfile = null;
				userProfile = supplier.checkAuthentication(usr, pwd);

				if (userProfile == null) {
					logger.warn("An attempt to authenticate with wrong credential has made [" + usr + "/" + pwd + "]");
					AuditLogUtilities.updateAudit(getHttpRequest(), profile, "LOGIN", null, "KO");
					throw new SpagoBIServiceException(SERVICE_NAME, "An attempt to authenticate with wrong credential has made [" + usr + "/" + pwd + "]");
				}

				// authentication was successful, we get the user unique identifier
				userUniqueIdentifier = userProfile.getUniqueIdentifier();

			} else {
				// in case SSO is enabled, we get user unique identifier from request
				userUniqueIdentifier = UserUtilities.getUserId(this.getHttpRequest());
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "LOGIN", null, "KO");
				Assert.assertNotNull(usr, "User identifier not found. Cannot build user profile object");

			}

			logger.info("User with unique identifier [" + userUniqueIdentifier + "] has been autheticated succesfully");

			profile = UserUtilities.getUserProfile(userUniqueIdentifier);
			AuditLogUtilities.updateAudit(getHttpRequest(), profile, "LOGIN", null, "OK");
			Assert.assertNotNull(profile, "Impossible to load profile for the user [" + userUniqueIdentifier + "]");

			String userId = (String) ((UserProfile) profile).getUserId();
			logger.info("User [" + ((UserProfile) profile).getUserId() + "] profile has been loaded succesfully");

			// in case user has a default role, we get his default user profile object
			profile = SessionUserProfileBuilder.getDefaultUserProfile((UserProfile) profile);
			// put user profile into session
			storeProfileInSession((UserProfile) profile, getSessionContainer().getPermanentContainer(), getHttpRequest().getSession());

			// Propagate THEME if present
			if (theme != null && theme.length() > 0) {
				getSessionContainer().getPermanentContainer().setAttribute(SpagoBIConstants.THEME, theme);
			}

			// Propagate BACK URL if present
			if (!StringUtilities.isEmpty(backUrl)) {
				getHttpSession().setAttribute(SpagoBIConstants.BACK_URL, backUrl);
			}

			// Propagate locale
			Locale locale = MessageBuilder.getBrowserLocaleFromSpago();
			logger.debug("User [" + usr + "] loacale has been set to [" + locale.getLanguage() + "/" + locale.getCountry() + "]");
			if (locale != null) {
				getSessionContainer().getPermanentContainer().setAttribute(Constants.USER_LANGUAGE, locale.getLanguage());
				getSessionContainer().getPermanentContainer().setAttribute(Constants.USER_COUNTRY, locale.getCountry());
			}

			try {
				JSONObject results = new JSONObject();
				results.put("username", userId);
				results.put("userid", profile.getUserUniqueIdentifier());
				logger.debug("Response for [" + userId + "] succesfully built");
				writeBackToClient(new JSONSuccess(results, callback));
				logger.debug("Response for [" + userId + "] succesfully written back to user");
			} catch (IOException e) {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "LOGIN", null, "KO");
				throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
			}
		} catch (Exception t) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "LOGIN", null, "KO");
			} catch (Exception e) {
				e.printStackTrace();
			}
			throw SpagoBIServiceExceptionHandler.getInstance().getWrappedException(SERVICE_NAME, t);
		} finally {
			logger.debug("OUT");
		}
	}

}
