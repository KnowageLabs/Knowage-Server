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
package it.eng.spagobi.engines.drivers.smartfilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
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
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IBinContentDAO;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.AbstractEngineDriver;
import it.eng.spagobi.engines.drivers.DefaultOutputParameter;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;

/**
 * Driver Implementation (IEngineDriver Interface) for Qbe External Engine.
 */
public class SmartFilterDriver extends AbstractEngineDriver implements IEngineDriver {

	static private Logger logger = Logger.getLogger(SmartFilterDriver.class);

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
			Assert.assertNotNull(analyticalDocument, "Input parameter [analyticalDocument] cannot be null");
			Assert.assertTrue((analyticalDocument instanceof BIObject),
					"Input parameter [analyticalDocument] cannot be an instance of [" + analyticalDocument.getClass().getName() + "]");

			biObject = (BIObject) analyticalDocument;

			parameters = new Hashtable();
			parameters = getRequestParameters(biObject);
			parameters = applySecurity(parameters, profile);
			parameters = addDocumentParametersInfo(parameters, biObject);
			parameters = applyService(parameters, biObject);
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

		Map parameters;
		BIObject biObject;
		SubObject subObject;

		logger.debug("IN");

		try {
			Assert.assertNotNull(analyticalDocument, "Input parameter [analyticalDocument] cannot be null");
			Assert.assertTrue((analyticalDocument instanceof BIObject),
					"Input parameter [analyticalDocument] cannot be an instance of [" + analyticalDocument.getClass().getName() + "]");
			biObject = (BIObject) analyticalDocument;

			if (analyticalDocumentSubObject == null) {
				logger.warn("Input parameter [subObject] is null");
				return getParameterMap(analyticalDocument, profile, roleName);
			}
			Assert.assertTrue((analyticalDocumentSubObject instanceof SubObject),
					"Input parameter [subObjectDetail] cannot be an instance of [" + analyticalDocumentSubObject.getClass().getName() + "]");
			subObject = (SubObject) analyticalDocumentSubObject;

			parameters = getRequestParameters(biObject);

			parameters.put("nameSubObject", subObject.getName() != null ? subObject.getName() : "");
			parameters.put("descriptionSubObject", subObject.getDescription() != null ? subObject.getDescription() : "");
			parameters.put("visibilitySubObject", subObject.getIsPublic().booleanValue() ? "Public" : "Private");
			parameters.put("subobjectId", subObject.getId());

			parameters = applySecurity(parameters, profile);
			parameters = addDocumentParametersInfo(parameters, biObject);
			parameters = applyService(parameters, biObject);
			parameters.put("isFromCross", "false");

		} finally {
			logger.debug("OUT");
		}
		return parameters;

	}

	/**
	 * Adds a system parameter contaning info about document parameters (url name, label, type)
	 *
	 * @param biobject The BIObject under execution
	 * @param map      The parameters map
	 * @return the modified map with the new parameter
	 */
	private Map addDocumentParametersInfo(Map map, BIObject biobject) {
		logger.debug("IN");
		JSONArray parametersJSON = new JSONArray();
		try {
			Locale locale = getLocale();
			List parameters = biobject.getDrivers();
			if (parameters != null && parameters.size() > 0) {
				Iterator iter = parameters.iterator();
				while (iter.hasNext()) {
					BIObjectParameter biparam = (BIObjectParameter) iter.next();
					JSONObject jsonParam = new JSONObject();
					jsonParam.put("id", biparam.getParameterUrlName());
					IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
					// String interLabel = msgBuilder.getUserMessage(biparam.getLabel(), SpagoBIConstants.DEFAULT_USER_BUNDLE, locale);
					String interLabel = msgBuilder.getI18nMessage(locale, biparam.getLabel());
					jsonParam.put("label", interLabel);
					jsonParam.put("type", biparam.getParameter().getType());
					parametersJSON.put(jsonParam);
				}
			}
		} catch (Exception e) {
			logger.error("Error while adding document parameters info", e);
		}
		map.put("SBI_DOCUMENT_PARAMETERS", parametersJSON.toString());
		logger.debug("OUT");
		return map;
	}

	/**
	 * Starting from a BIObject extracts from it the map of the paramaeters for the execution call
	 *
	 * @param biObject BIObject to execute
	 * @return Map The map of the execution call parameters
	 */
	private Map getRequestParameters(BIObject biObject) {
		logger.debug("IN");

		Map parameters;
		ObjTemplate template;
		IBinContentDAO contentDAO;
		byte[] content;

		logger.debug("IN");

		parameters = null;

		try {
			parameters = new Hashtable();
			template = this.getTemplate(biObject);

			try {
				contentDAO = DAOFactory.getBinContentDAO();
				Assert.assertNotNull(contentDAO, "Impossible to instantiate contentDAO");

				content = contentDAO.getBinContent(template.getBinId());
				Assert.assertNotNull(content, "Template content cannot be null");
			} catch (Throwable t) {
				throw new RuntimeException("Impossible to load template content for document [" + biObject.getLabel() + "]", t);
			}

			appendRequestParameter(parameters, "document", biObject.getId().toString());
			appendAnalyticalDriversToRequestParameters(biObject, parameters);
			addBIParameterDescriptions(biObject, parameters);
		} finally {
			logger.debug("OUT");
		}

		return parameters;
	}

	/**
	 * Add into the parameters map the BIObject's BIParameter names and values
	 *
	 * @param biobj BIOBject to execute
	 * @param pars  Map of the parameters for the execution call
	 * @return Map The map of the execution call parameters
	 */
	private Map appendAnalyticalDriversToRequestParameters(BIObject biobj, Map pars) {
		logger.debug("IN");

		if (biobj == null) {
			logger.warn("BIObject parameter null");
			return pars;
		}

		ParameterValuesEncoder parValuesEncoder = new ParameterValuesEncoder();
		if (biobj.getDrivers() != null) {
			BIObjectParameter biobjPar = null;
			for (Iterator it = biobj.getDrivers().iterator(); it.hasNext();) {
				try {
					biobjPar = (BIObjectParameter) it.next();
					String value = parValuesEncoder.encode(biobjPar);
					pars.put(biobjPar.getParameterUrlName(), value);
					logger.debug("Add parameter:" + biobjPar.getParameterUrlName() + "/" + value);
				} catch (Exception e) {
					logger.error("Error while processing a BIParameter", e);
				}
			}
		}

		logger.debug("OUT");
		return pars;
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

		EngineURL engineURL;
		BIObject obj;
		String documentId;
		Engine engine;
		String url;
		HashMap parameters;

		logger.debug("IN");

		try {
			obj = null;
			try {
				obj = (BIObject) biobject;
			} catch (ClassCastException cce) {
				logger.error("The input object is not a BIObject type", cce);
				return null;
			}

			documentId = obj.getId().toString();
			engine = obj.getEngine();
			url = engine.getUrl();

			parameters = new HashMap();

			// getting the dataset label from template, if smart filter is based on a dataset
			ObjTemplate objectTemplate = obj.getActiveTemplate();
			byte[] content = objectTemplate.getContent();
			SourceBean sbTemplate = getTemplateAsSourceBean(content);
			if (sbTemplate.getName().equals(EngineConstants.SMART_FILTER_TAG) && sbTemplate.containsAttribute("DATASET")) {
				String label = (String) ((SourceBean) sbTemplate.getAttribute("DATASET")).getAttribute("label");
				parameters.put("dataset_label", label);
			}
			parameters.put("document", documentId);
			parameters.put(PARAM_SERVICE_NAME, "FORM_ENGINE_TEMPLATE_BUILD_ACTION");
			parameters.put(PARAM_NEW_SESSION, "TRUE");
			parameters.put(PARAM_MODALITY, "EDIT");
			applySecurity(parameters, profile);

			engineURL = new EngineURL(url, parameters);
		} catch (Throwable t) {
			throw new RuntimeException("Cannot get engine edit URL", t);
		} finally {
			logger.debug("OUT");
		}

		return engineURL;
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

		EngineURL engineURL;
		BIObject obj;
		String documentId;
		Engine engine;
		String url;
		HashMap parameters;

		logger.debug("IN");

		try {
			obj = null;
			try {
				obj = (BIObject) biobject;
			} catch (ClassCastException cce) {
				logger.error("The input object is not a BIObject type", cce);
				return null;
			}

			documentId = obj.getId().toString();
			engine = obj.getEngine();
			url = engine.getUrl();
			// url = url.replaceFirst("/servlet/AdapterHTTP", "");
			// url += "/templateBuilder.jsp";

			parameters = new HashMap();
			parameters.put("document", documentId);
			parameters.put(PARAM_SERVICE_NAME, "FORM_ENGINE_TEMPLATE_BUILD_ACTION");
			parameters.put(PARAM_NEW_SESSION, "TRUE");
			parameters.put(PARAM_MODALITY, "NEW");
			applySecurity(parameters, profile);

			engineURL = new EngineURL(url, parameters);
		} finally {
			logger.debug("OUT");
		}

		return engineURL;
	}

	private final static String PARAM_SERVICE_NAME = "ACTION_NAME";
	private final static String PARAM_NEW_SESSION = "NEW_SESSION";
	private final static String PARAM_MODALITY = "MODALITY";

	private Map applyService(Map parameters, BIObject biObject) {
		logger.debug("IN");

		try {
			Assert.assertNotNull(parameters, "Input [parameters] cannot be null");

			ObjTemplate objectTemplate = biObject.getActiveTemplate();
			byte[] content = objectTemplate.getContent();
			SourceBean sbTemplate = getTemplateAsSourceBean(content);

			if (sbTemplate.getName().equals(EngineConstants.SMART_FILTER_TAG)) {
				parameters.put(PARAM_SERVICE_NAME, "FORM_ENGINE_FROM_DATASET_START_ACTION");
				if (sbTemplate.containsAttribute("DATASET")) {
					String label = (String) ((SourceBean) sbTemplate.getAttribute("DATASET")).getAttribute("label");
					parameters.put("dataset_label", label);
				}
			} else {
				parameters.put(PARAM_SERVICE_NAME, "FORM_ENGINE_START_ACTION");
			}
			parameters.put(PARAM_MODALITY, "VIEW");

			parameters.put(PARAM_NEW_SESSION, "TRUE");
		} catch (Throwable t) {
			throw new RuntimeException("Cannot apply service parameters", t);
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

	private void appendRequestParameter(Map parameters, String pname, String pvalue) {
		parameters.put(pname, pvalue);
		logger.debug("Added parameter [" + pname + "] with value [" + pvalue + "] to request parameters list");
	}

	@Override
	public SourceBean getTemplateAsSourceBean(byte[] content) {
		SourceBean templateSB = null;
		try {
			templateSB = SourceBean.fromXMLString(getTemplateAsString(content));
		} catch (SourceBeanException e) {
			logger.error("Error while getting template source bean", e);
		}

		return templateSB;
	}

	public String getTemplateAsString(byte[] temp) {

		if (temp != null)
			return new String(temp);
		else
			return new String("");
	}

	@Override
	public ArrayList<String> getDatasetAssociated(byte[] contentTemplate) throws JSONException {
		// TODO Auto-generated method stub
		return null;
	}

	public String composeSmartFilterTemplate(String smartFilterDef, String smartFilterQuery, String smartFilterValues) throws SourceBeanException {
		/*
		 * SourceBean templateSB = new SourceBean(TAG_WORKSHEET); templateSB.setAttribute(ATTRIBUTE_VERSION, CURRENT_VERSION); SourceBean confSB =
		 * SourceBean.fromXMLString(originalQbeTempl); // from version 0 to version 1 worksheet change compensation: on version 0 the // worksheet definition
		 * was inside QBE tag; on version 1 the QBE tag is inside // WORKSHEET tag if (confSB.getName().equalsIgnoreCase(TAG_QBE) ||
		 * confSB.getName().equalsIgnoreCase(TAG_QBE_COMPOSITE) || confSB.getName().equalsIgnoreCase(TAG_SMART_FILTER)) {
		 *
		 * if (confSB.containsAttribute(TAG_WORKSHEET_DEFINITION)) { confSB.delAttribute(TAG_WORKSHEET_DEFINITION); } templateSB.setAttribute(confSB);
		 * SourceBean wk_def_sb = new SourceBean(TAG_WORKSHEET_DEFINITION); wk_def_sb.setCharacters(workSheetDef); templateSB.setAttribute(wk_def_sb);
		 *
		 * if (workSheetQuery != null && !workSheetQuery.equals("")) { SourceBean query_sb = new SourceBean(QUERY); query_sb.setCharacters(workSheetQuery);
		 * confSB.updAttribute(query_sb); }
		 *
		 * if (smartFilterValues != null && !smartFilterValues.equals("")) { SourceBean smartFilterValuesSB = new SourceBean(FORM_VALUES);
		 * smartFilterValuesSB.setCharacters(smartFilterValues); confSB.updAttribute(smartFilterValuesSB); }
		 *
		 * } else {
		 *
		 * SourceBean qbeSB = null;
		 *
		 * if (confSB.containsAttribute(TAG_QBE)) { qbeSB = (SourceBean) confSB.getAttribute(TAG_QBE); } else if (confSB.containsAttribute(TAG_QBE_COMPOSITE)) {
		 * qbeSB = (SourceBean) confSB.getAttribute(TAG_QBE_COMPOSITE); } else if (confSB.containsAttribute(TAG_SMART_FILTER)) { qbeSB = (SourceBean)
		 * confSB.getAttribute(TAG_SMART_FILTER); }
		 *
		 * if (qbeSB != null) { templateSB.setAttribute(qbeSB); if (workSheetQuery != null && !workSheetQuery.equals("")) { SourceBean query_sb = new
		 * SourceBean(QUERY); query_sb.setCharacters(workSheetQuery); qbeSB.updAttribute(query_sb); }
		 *
		 * if (smartFilterValues != null && !smartFilterValues.equals("")) { SourceBean smartFilterValuesSB = new SourceBean(FORM_VALUES);
		 * smartFilterValuesSB.setCharacters(smartFilterValues); qbeSB.updAttribute(smartFilterValuesSB); } }
		 *
		 * SourceBean wk_def_sb = new SourceBean(TAG_WORKSHEET_DEFINITION); wk_def_sb.setCharacters(workSheetDef); templateSB.setAttribute(wk_def_sb); }
		 *
		 * String template = templateSB.toXML(false);
		 */
		String template = "";
		return template;
	}

	public String createNewSmartFitleremplate(String smartFilterDefinition, String modelName) throws SourceBeanException {
		// SourceBean templateSB = new SourceBean(TAG_WORKSHEET);
		// templateSB.setAttribute(ATTRIBUTE_VERSION, CURRENT_VERSION);
		// SourceBean worksheetDefinitionSB = new SourceBean(TAG_WORKSHEET_DEFINITION);
		// worksheetDefinitionSB.setCharacters(smartFilterDefinition);
		// templateSB.setAttribute(worksheetDefinitionSB);
		// if (modelName != null && !modelName.equals("")) {
		// // case when starting from a model
		// SourceBean templateQBE = new SourceBean(TAG_QBE);
		// SourceBean templateDatamart = new SourceBean(DATAMART);
		// templateDatamart.setAttribute("name", modelName);
		// templateQBE.setAttribute(templateDatamart);
		// SourceBean templateQuery = new SourceBean(QUERY);
		// templateQuery.setCharacters(query);
		// templateQBE.setAttribute(templateQuery);
		// templateSB.setAttribute(templateQBE);
		// } else if (query != null && !query.trim().equals("")) {
		// // case when starting from a dataset
		// SourceBean qbeSB = new SourceBean(TAG_QBE);
		// SourceBean queryDefinitionSB = new SourceBean(QUERY);
		// queryDefinitionSB.setCharacters(query);
		// qbeSB.setAttribute(queryDefinitionSB);
		// templateSB.setAttribute(qbeSB);
		// }
		// String template = templateSB.toXML(false);
		String template = "";
		return template;
	}

	// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	@Override
	public List<DefaultOutputParameter> getSpecificOutputParameters(List categories) {
		// TODO Auto-generated method stub
		return null;
	}

	// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	@Override
	public List<DefaultOutputParameter> getSpecificOutputParameters(String specificChartType) {
		// TODO Auto-generated method stub
		return null;
	}

}
