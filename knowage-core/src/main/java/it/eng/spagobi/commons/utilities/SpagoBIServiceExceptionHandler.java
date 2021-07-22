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
package it.eng.spagobi.commons.utilities;

import java.util.Iterator;
import java.util.Locale;
import java.util.Locale.Builder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class SpagoBIServiceExceptionHandler {
	private static SpagoBIServiceExceptionHandler instance;

	private static transient Logger logger = Logger.getLogger(SpagoBIServiceExceptionHandler.class);

	public static SpagoBIServiceExceptionHandler getInstance() {
		if (instance == null) {
			instance = new SpagoBIServiceExceptionHandler();
		}

		return instance;
	}

	private SpagoBIServiceExceptionHandler() {

	}

	/**
	 *
	 * @param serviceName <code>
	 * public void service(request, response) {
	 *
	 * 		logger.debug("IN");
	 *
	 * 		try {
	 * 			...
	 * 		} catch (Throwable t) {
	 * 			throw SpagoBIServiceExceptionHandler.getInstance().getWrappedException(serviceName, t);
	 * 		} finally {
	 * 			// relese resurces if needed
	 * 		}
	 *
	 * 		logger.debug("OUT");
	 * }
	 * </code>
	 *
	 *
	 * @param e
	 * @return
	 */
	public SpagoBIServiceException getWrappedException(String serviceName, Throwable e) {
		SpagoBIServiceException serviceException = null;
		MessageBuilder msgBuild = new MessageBuilder();
		Locale locale = null;
		RequestContainer requestContainer = RequestContainer.getRequestContainer();
		if (requestContainer != null) {
			SessionContainer permanentSession = requestContainer.getSessionContainer().getPermanentContainer();
			String currLanguage = (String) permanentSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
			String currCountry = (String) permanentSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
			String currScript = (String) permanentSession.getAttribute(SpagoBIConstants.AF_SCRIPT);
			if (currLanguage != null && currCountry != null) {
				Builder tmpLocale = new Locale.Builder().setLanguage(currLanguage).setRegion(currCountry);

				if (StringUtils.isNotBlank(currScript)) {
					tmpLocale.setScript(currScript);
				}

				locale = tmpLocale.build();
			} else
				locale = GeneralUtilities.getDefaultLocale();

		} else {
			locale = GeneralUtilities.getDefaultLocale();
		}

		if (e instanceof SpagoBIServiceException) {
			// this mean that the service have catched the exception nicely
			serviceException = (SpagoBIServiceException) e;
			String sms = serviceException.getMessage();
			sms = msgBuild.getMessage(sms, locale);
			serviceException = new SpagoBIServiceException(serviceName, sms, e);
		} else {
			// otherwise an unpredicted exception has been raised.

			// This is the last line of defense against exceptions. Bytheway all exceptions that are catched
			// only here for the first time can be considered as bugs in the exception handling mechanism. When
			// such an exception is raised the code in the service should be fixed in order to catch it before and add some meaningfull
			// informations on what have caused it.
			Throwable rootException = e;
			while (rootException.getCause() != null) {
				rootException = rootException.getCause();
			}
			String str = rootException.getMessage() != null ? rootException.getMessage() : rootException.getClass().getName();
			str = msgBuild.getMessage(str, locale);
			String message = "An unexpecetd error occurred while executing service." + "\nThe root cause of the error is: " + str;

			serviceException = new SpagoBIServiceException(serviceName, message, e);

		}

		logError(serviceException);

		return serviceException;
	}

	public static void logError(SpagoBIServiceException serviceError) {
		logger.error(serviceError.getMessage());
		logger.error("The error root cause is: " + serviceError.getRootCause());
		if (serviceError.getHints().size() > 0) {
			Iterator hints = serviceError.getHints().iterator();
			while (hints.hasNext()) {
				String hint = (String) hints.next();
				logger.info("hint: " + hint);
			}

		}
		logger.error("The error root cause stack trace is:", serviceError.getCause());
		logger.error("The error full stack trace is:", serviceError);
	}
}
