/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.documentsbrowser.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.execution.service.ExecuteAdHocUtility;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.PortletLoginAction;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.commons.utilities.PortletUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.axis.utils.StringUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.json.JSONObject;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */
public class UserDocumentsBrowserPortletStartAction extends PortletLoginAction {

	// logger component
	private static Logger logger = Logger.getLogger(UserDocumentsBrowserPortletStartAction.class);

	public static final String LABEL_SUBTREE_NODE = "PATH_SUBTREE";
	public static final String HEIGHT = "HEIGHT";
	public static final String PORTLET = "PORTLET";
	public static final String OUTPUT_PARAMETER_GEOREPORT_EDIT_SERVICE_URL = "georeportServiceUrl";
	public static final String OUTPUT_PARAMETER_COCKPIT_EDIT_SERVICE_URL = "cockpitServiceUrl";
	public static final String OUTPUT_PARAMETER_WORKSHEET_EDIT_SERVICE_URL = "worksheetServiceUrl";

	private Locale locale;
	IEngUserProfile profile;

	@Override
	public void service(SourceBean request, SourceBean response) throws Exception {

		String labelSubTreeNode = null;
		String height = null;
		String channelType;

		logger.debug("IN");
		// Start writing log in the DB
		Session aSession = null;
		try {
			aSession = HibernateSessionManager.getCurrentSession();
			// Connection jdbcConnection = aSession.connection();
			// Connection jdbcConnection = HibernateUtil.getConnection(aSession);
			// TODO

			profile = UserUtilities.getUserProfile();
			AuditLogUtilities.updateAudit(getHttpRequest(), profile, "ACTIVITY.DOCUMENTSBROWSERMENU", null, "OK");
		} catch (HibernateException he) {
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		// End writing log in the DB

		try {
			super.service(request, response);

			HttpServletRequest httpRequest = getHttpRequest();
			MessageBuilder m = new MessageBuilder();
			locale = m.getLocale(httpRequest);

			channelType = getRequestContainer().getChannelType();

			logger.info("[DAJS]:: channelType: " + channelType);

			if (PORTLET.equalsIgnoreCase(channelType)) {
				logger.info("[DAJS]:: mode: " + PORTLET);
				PortletRequest portReq = PortletUtilities.getPortletRequest();
				logger.info("[DAJS]:: portReq: " + portReq);
				PortletPreferences prefs = portReq.getPreferences();
				logger.info("[DAJS]:: prefs: " + prefs);
				labelSubTreeNode = prefs.getValue(LABEL_SUBTREE_NODE, "");
				logger.info("[DAJS]:: labelSubTreeNode: " + labelSubTreeNode);
				height = prefs.getValue(HEIGHT, "600");
				logger.info("[DAJS]:: height: " + height);
				if (labelSubTreeNode != null && !labelSubTreeNode.trim().equals("")) {
					response.setAttribute("labelSubTreeNode", labelSubTreeNode);
					logger.info("[DAJS]:: attribute [labelSubTreeNode] set equals to " + labelSubTreeNode);
				}
				if (height != null && !height.trim().equals("")) {
					response.setAttribute("height", height);
					logger.info("[DAJS]:: attribute [height] set equals to " + height);
				} else {
					response.setAttribute("height", "600");
					logger.info("[DAJS]:: attribute [height] set equals to 600");
				}
			} else {
				logger.info("[DAJS]:: mode: " + channelType);
				DocumentsBrowserConfig config = DocumentsBrowserConfig.getInstance();

				// If this is a "custom" Document Browser we have a subtree path as parameter
				String functID = null;

				String subTree = (String) request.getAttribute(LABEL_SUBTREE_NODE);
				if (subTree != null) {

					if (!StringUtils.isEmpty(subTree)) {
						LowFunctionality funct = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByPath(subTree, false);
						if (funct != null) {
							functID = String.valueOf(funct.getId());
						}
					}

				}
				// ----------------------------------------------
				// Defining action urls
				String executionId = ExecuteAdHocUtility.createNewExecutionId();
				String geoereportEditActionUrl = null;
				String worksheetEditActionUrl = null;
				String cockpitEditActionUrl = null;

				try {
					geoereportEditActionUrl = buildGeoreportEditServiceUrl(executionId);
				} catch (SpagoBIRuntimeException r) {
					// the geo engine is not found
					logger.info("[DAJS]:: error", r);
				}

				try {
					worksheetEditActionUrl = buildWorksheetEditServiceUrl(executionId);
				} catch (SpagoBIRuntimeException r) {
					// the geo engine is not found
					logger.info("[DAJS]:: error", r);
				}

				try {
					cockpitEditActionUrl = buildCockpitEditServiceUrl(executionId);
				} catch (SpagoBIRuntimeException r) {
					// the geo engine is not found
					logger.info("[DAJS]:: error", r);
				}

				JSONObject jsonUrlObj = config.toJSON();
				if (geoereportEditActionUrl != null) {
					jsonUrlObj.put(OUTPUT_PARAMETER_GEOREPORT_EDIT_SERVICE_URL, geoereportEditActionUrl);
				}
				if (worksheetEditActionUrl != null) {
					jsonUrlObj.put(OUTPUT_PARAMETER_WORKSHEET_EDIT_SERVICE_URL, worksheetEditActionUrl);
				}
				if (cockpitEditActionUrl != null) {
					jsonUrlObj.put(OUTPUT_PARAMETER_COCKPIT_EDIT_SERVICE_URL, cockpitEditActionUrl);
				}

				// ----------------------------------------------

				JSONObject jsonObj = config.toJSON();

				if (functID != null) {
					jsonObj.put("defaultFolderId", functID);
				}

				// read value from db
				// labelSubTreeNode = ...;
				// jsonObj.put("labelSubTreeNode", labelSubTreeNode);
				response.setAttribute("metaConfiguration", jsonObj);
				response.setAttribute("engineUrls", jsonUrlObj);
			}

		} catch (Throwable t) {
			logger.error("[DAJS]:: error", t);
			throw new SpagoBIException("An unexpected error occured while executing UserDocumentsBrowserPortletStartAction", t);
		} finally {
			logger.debug("OUT");
		}
	}

	protected String buildGeoreportEditServiceUrl(String executionId) {
		// Map<String, String> parametersMap = buildGeoreportEditServiceBaseParametersMap();
		Map<String, String> parametersMap = buildServiceBaseParametersMap();
		parametersMap.put("SBI_EXECUTION_ID", executionId);

		Engine georeportEngine = ExecuteAdHocUtility.getGeoreportEngine();
		// GeoReportEngineStartEditAction

		String baseEditUrl = georeportEngine.getUrl().replace("GeoReportEngineStartAction", "GeoReportEngineStartEditAction");
		String georeportEditActionUrl = GeneralUtilities.getUrl(baseEditUrl, parametersMap);
		LogMF.debug(logger, "Georeport edit service invocation url is equal to [{}]", georeportEditActionUrl);

		return georeportEditActionUrl;
	}

	// protected Map<String, String> buildGeoreportEditServiceBaseParametersMap() {
	// Map<String, String> parametersMap = buildServiceBaseParametersMap();
	//
	// return parametersMap;
	// }

	// WORKSHEET
	protected String buildWorksheetEditServiceUrl(String executionId) {
		Engine worksheetEngine = null;
		String worksheetEditActionUrl = null;

		Map<String, String> parametersMap = buildEditServiceBaseParametersMap();
		parametersMap.put("SBI_EXECUTION_ID", executionId);

		IDataSource datasource;
		try {
			datasource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while loading default datasource for writing", e);
		}
		if (datasource != null) {
			parametersMap.put(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL, datasource.getLabel());
		} else {
			logger.debug("There is no default datasource for writing");
		}

		try {
			worksheetEngine = ExecuteAdHocUtility.getWorksheetEngine();
		} catch (SpagoBIRuntimeException r) {
			// the ws engine is not found
			logger.info("Engine not found. Error: ", r);
		}
		if (worksheetEngine != null) {
			LogMF.debug(logger, "Engine label is equal to [{0}]", worksheetEngine.getLabel());
			// create the WorkSheet Edit Service's URL
			worksheetEditActionUrl = GeneralUtilities.getUrl(worksheetEngine.getUrl(), parametersMap);
			LogMF.debug(logger, "Worksheet edit service invocation url is equal to [{}]", worksheetEditActionUrl);
		}

		return worksheetEditActionUrl;
	}

	// COCKPIT
	protected String buildCockpitEditServiceUrl(String executionId) {
		Engine cockpitEngine = null;
		String cockpitEditActionUrl = null;

		Map<String, String> parametersMap = buildEditServiceBaseParametersMap();
		parametersMap.put("SBI_EXECUTION_ID", executionId);

		try {
			cockpitEngine = ExecuteAdHocUtility.getCockpitEngine();
		} catch (SpagoBIRuntimeException r) {
			// the cockpit engine is not found
			logger.info("Engine not found. Error: ", r);
		}

		if (cockpitEngine != null) {
			String baseEditUrl = cockpitEngine.getUrl().replace("pages/execute", "pages/edit");
			cockpitEditActionUrl = GeneralUtilities.getUrl(baseEditUrl, parametersMap);
			LogMF.debug(logger, "Cockpit edit service invocation url is equal to [{}]", cockpitEditActionUrl);
		}

		return cockpitEditActionUrl;
	}

	protected Map<String, String> buildEditServiceBaseParametersMap() {
		Map<String, String> parametersMap = buildServiceBaseParametersMap();

		return parametersMap;
	}

	protected Map<String, String> buildServiceBaseParametersMap() {
		HashMap<String, String> parametersMap = new HashMap<String, String>();

		parametersMap.put("NEW_SESSION", "TRUE");

		parametersMap.put(SpagoBIConstants.SBI_CONTEXT, GeneralUtilities.getSpagoBiContext());
		parametersMap.put(SpagoBIConstants.SBI_HOST, GeneralUtilities.getSpagoBiHost());

		parametersMap.put(SpagoBIConstants.SBI_LANGUAGE, locale.getLanguage());
		parametersMap.put(SpagoBIConstants.SBI_COUNTRY, locale.getCountry());

		// if (!GeneralUtilities.isSSOEnabled()) {
		UserProfile userProfile = (UserProfile) profile;
		parametersMap.put(SsoServiceInterface.USER_ID, (String) userProfile.getUserUniqueIdentifier());
		// }

		return parametersMap;
	}
}
