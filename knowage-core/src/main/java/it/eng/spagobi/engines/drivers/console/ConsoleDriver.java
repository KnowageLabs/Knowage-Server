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
package it.eng.spagobi.engines.drivers.console;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.engines.drivers.generic.GenericDriver;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * Driver Implementation (IEngineDriver Interface) for Chart External Engine.
 */
public class ConsoleDriver extends GenericDriver {

	static private Logger logger = Logger.getLogger(ConsoleDriver.class);

	/**
	 * Returns a map of parameters which will be send in the request to the engine application.
	 *
	 * @param profile            Profile of the user
	 * @param roleName           the name of the execution role
	 * @param analyticalDocument the biobject
	 *
	 * @return Map The map of the execution call parameters
	 */
	@Override
	public Map getParameterMap(Object analyticalDocument, IEngUserProfile profile, String roleName) {
		Map parameters;
		BIObject biObject;

		logger.debug("IN");

		try {
			parameters = super.getParameterMap(analyticalDocument, profile, roleName);
			parameters = applyService(parameters, null);
		} finally {
			logger.debug("OUT");
		}

		return parameters;
	}

	/**
	 * Returns a map of parameters which will be send in the request to the engine application.
	 *
	 * @param analyticalDocumentSubObject SubObject to execute
	 * @param profile                     Profile of the user
	 * @param roleName                    the name of the execution role
	 * @param analyticalDocument          the object
	 *
	 * @return Map The map of the execution call parameters
	 */
	@Override
	public Map getParameterMap(Object analyticalDocument, Object analyticalDocumentSubObject, IEngUserProfile profile, String roleName) {
		return super.getParameterMap(analyticalDocument, analyticalDocumentSubObject, profile, roleName);
	}

	/**
	 * Function not implemented. Thid method should not be called
	 *
	 * @param biobject The BIOBject to edit
	 * @param profile  the profile
	 *
	 * @return the edits the document template build url
	 *
	 * @throws InvalidOperationRequest the invalid operation request
	 */
	@Override
	public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest {
		logger.warn("Function not implemented");
		throw new InvalidOperationRequest();
	}

	/**
	 * Function not implemented. Thid method should not be called
	 *
	 * @param biobject The BIOBject to edit
	 * @param profile  the profile
	 *
	 * @return the new document template build url
	 *
	 * @throws InvalidOperationRequest the invalid operation request
	 */
	@Override
	public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest {
		logger.warn("Function not implemented");
		throw new InvalidOperationRequest();
	}

	private final static String PARAM_SERVICE_NAME = "ACTION_NAME";
	private final static String PARAM_NEW_SESSION = "NEW_SESSION";
	private final static String PARAM_MODALITY = "MODALITY";

	private Map applyService(Map parameters, BIObject biObject) {
		ObjTemplate template;

		logger.debug("IN");

		try {
			Assert.assertNotNull(parameters, "Input [parameters] cannot be null");
			parameters.put(PARAM_SERVICE_NAME, "CONSOLE_ENGINE_START_ACTION");
			parameters.put(PARAM_MODALITY, "VIEW");
			parameters.put(PARAM_NEW_SESSION, "TRUE");
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to guess from template extension the engine startup service to call");
		} finally {
			logger.debug("OUT");
		}

		return parameters;
	}

	private ObjTemplate getTemplate(BIObject biObject) {
		ObjTemplate template;
		IObjTemplateDAO templateDAO;

		logger.debug("IN");

		try {
			Assert.assertNotNull(biObject, "Input [biObject] cannot be null");

			templateDAO = DAOFactory.getObjTemplateDAO();
			Assert.assertNotNull(templateDAO, "Impossible to instantiate templateDAO");

			template = templateDAO.getBIObjectActiveTemplate(biObject.getId());
			Assert.assertNotNull(template, "Loaded template cannot be null");

			logger.debug("Active template [" + template.getName() + "] of document [" + biObject.getLabel() + "] loaded succesfully");
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to load template for document [" + biObject.getLabel() + "]", t);
		} finally {
			logger.debug("OUT");
		}

		return template;
	}

	private Locale getLocale() {
		logger.debug("IN");
		try {
			Locale locale = null;
			RequestContainer requestContainer = RequestContainer.getRequestContainer();
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

			return locale;
		} catch (Exception e) {
			logger.error("Error while getting locale; using default one", e);
			return GeneralUtilities.getDefaultLocale();
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public ArrayList<String> getDatasetAssociated(byte[] contentTemplate) throws JSONException {
		logger.debug("IN");

		ArrayList<String> datasetsLabel = new ArrayList<String>();
		JSONObject templateContent = getTemplateAsJsonObject(contentTemplate);

		// get datasets from template

		Object ob = templateContent.get("datasets");
		if (ob != null && ob instanceof JSONArray) {
			JSONArray dsArrays = (JSONArray) ob;
			for (int i = 0; i < dsArrays.length(); i++) {
				JSONObject ds = (JSONObject) dsArrays.get(i);
				String dsLabel = ds.getString("label");
				datasetsLabel.add(dsLabel);
				datasetsLabel.add(dsLabel + "Errors");
				datasetsLabel.add(dsLabel + "Alarms");
				logger.debug("added association between object console and " + dsLabel);
			}
		} else {
			logger.warn("No datasets associated to the console ");
		}

		logger.debug("OUT");
		return datasetsLabel;
	}
}
