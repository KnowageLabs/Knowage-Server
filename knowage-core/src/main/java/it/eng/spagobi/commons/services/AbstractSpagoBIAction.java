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

import java.util.Collection;
import java.util.Locale;
import java.util.Locale.Builder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spago.base.Constants;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFAbstractError;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIServiceExceptionHandler;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.container.CoreContextManager;
import it.eng.spagobi.container.IBeanContainer;
import it.eng.spagobi.container.strategy.ExecutionContextRetrieverStrategy;
import it.eng.spagobi.container.strategy.IContextRetrieverStrategy;
import it.eng.spagobi.utilities.exceptions.CannotWriteErrorsToClientException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.AbstractBaseHttpAction;
import it.eng.spagobi.utilities.service.JSONFailure;
import it.eng.spagobi.utilities.themes.ThemesManager;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public abstract class AbstractSpagoBIAction extends AbstractBaseHttpAction {

	public static final String SERVICE_NAME = "SPAGOBI_SERVICE";

	private static transient Logger logger = Logger.getLogger(AbstractSpagoBIAction.class);

	private CoreContextManager contextManager;

	@Override
	public void init(SourceBean config) {
		super.init(config);
	}

	@Override
	public void service(SourceBean request, SourceBean response) throws SpagoBIServiceException {
		EMFErrorHandler errorHandler = this.getErrorHandler();
		if (!errorHandler.isOK()) {
			writeErrorsBackToClient();
			return;
		}
		setSpagoBIRequestContainer(request);
		setSpagoBIResponseContainer(response);
		// setting language and country info for Spago framework
		String language = this.getAttributeAsString("SBI_LANGUAGE");
		String country = this.getAttributeAsString("SBI_COUNTRY");
		String script = this.getAttributeAsString("SBI_SCRIPT");
		if (language != null && !language.trim().equals("")) {
			this.getSessionContainer().getPermanentContainer().setAttribute(Constants.USER_LANGUAGE, language);
			if (country != null && !country.trim().equals("")) {
				this.getSessionContainer().getPermanentContainer().setAttribute(Constants.USER_COUNTRY, country);
			}
			if (StringUtils.isNotBlank(script)) {
				this.getSessionContainer().getPermanentContainer().setAttribute("AF_SCRIPT", script);
			}
		}
		try {
			this.doService();
		} catch (Throwable t) {
			handleException(t);
		}
		;
	}

	protected void writeErrorsBackToClient() {
		logger.debug("IN");
		Collection<EMFAbstractError> errors = getErrorHandler().getErrors();
		try {
			writeBackToClient(new JSONFailure(errors));
		} catch (Throwable t) {
			logger.error(t);
			throw new CannotWriteErrorsToClientException(SERVICE_NAME, "Cannot write errors to client", t);
		}
		logger.debug("OUT");
	}

	public abstract void doService();

	public IEngUserProfile getUserProfile() {
		return (IEngUserProfile) getSessionContainer().getPermanentContainer().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	}

	public CoreContextManager getContext() {
		if (contextManager == null) {
			IBeanContainer contextsContainer = getSpagoBISessionContainer();
			IContextRetrieverStrategy contextRetriverStartegy = new ExecutionContextRetrieverStrategy(getSpagoBIRequestContainer());
			contextManager = new CoreContextManager(contextsContainer, contextRetriverStartegy);
		}

		return contextManager;
	}

	public CoreContextManager createContext(String contextId) {
		IBeanContainer contextsContainer;
		IContextRetrieverStrategy contextRetriverStartegy;

		contextsContainer = getSpagoBISessionContainer();
		contextRetriverStartegy = new ExecutionContextRetrieverStrategy(contextId);
		contextManager = new CoreContextManager(contextsContainer, contextRetriverStartegy);

		return contextManager;
	}

	public Locale getLocale() {
		Locale locale = null;

		RequestContainer requestContainer;

		locale = GeneralUtilities.getDefaultLocale();

		requestContainer = RequestContainer.getRequestContainer();
		if (requestContainer != null) {

			SessionContainer permanentSession = getSessionContainer().getPermanentContainer();

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

		}

		return locale;
	}

	public String localize(String str) {
		String lnStr;
		IMessageBuilder msgBuilder;

		msgBuilder = MessageBuilderFactory.getMessageBuilder();
		// lnStr = msgBuilder.getUserMessage(str , SpagoBIConstants.DEFAULT_USER_BUNDLE, getLocale());
		// lnStr =msgBuilder.getI18nMessage(getLocale(), str);
		lnStr = str;
		return lnStr;
	}

	public String getTheme() {
		return ThemesManager.getCurrentTheme(getRequestContainer());
	}

	public void handleException(Throwable t) {
		// wrap excption here
		// dump some context
		// release resources
		// rethrows the wrapped exception (it will be trapped)

		throw SpagoBIServiceExceptionHandler.getInstance().getWrappedException(SERVICE_NAME, t);
	}

	protected void checkError() {

	}
}
