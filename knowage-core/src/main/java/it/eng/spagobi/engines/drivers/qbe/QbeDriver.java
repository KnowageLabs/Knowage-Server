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
package it.eng.spagobi.engines.drivers.qbe;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IBinContentDAO;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.engines.drivers.AbstractEngineDriver;
import it.eng.spagobi.engines.drivers.DefaultOutputParameter;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Driver Implementation (IEngineDriver Interface) for Qbe External Engine.
 */
public class QbeDriver extends AbstractEngineDriver implements IEngineDriver {

	static private Logger logger = Logger.getLogger(QbeDriver.class);

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
			// parameters = addDocumentParametersInfo(parameters, biObject);
			parameters = applyService(parameters, biObject);
			parameters = applyDatasourceForWriting(parameters, biObject);
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
			// parameters = addDocumentParametersInfo(parameters, biObject);
			parameters = applyService(parameters, biObject);
			parameters = applyDatasourceForWriting(parameters, biObject);
			parameters.put("isFromCross", "false");

		} finally {
			logger.debug("OUT");
		}
		return parameters;

	}

	// private Map addDocumentParametersInfo(Map map, BIObject biobject) {
	// logger.debug("IN");
	// JSONArray parametersJSON = new JSONArray();
	// try {
	// Locale locale = getLocale();
	// List parameters = biobject.getDrivers();
	// if (parameters != null && parameters.size() > 0) {
	// Iterator iter = parameters.iterator();
	// while (iter.hasNext()) {
	// BIObjectParameter biparam = (BIObjectParameter) iter.next();
	// JSONObject jsonParam = new JSONObject();
	// jsonParam.put("id", biparam.getParameterUrlName());
	// IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
	// jsonParam.put("label", msgBuilder.getUserMessage(biparam.getLabel(), SpagoBIConstants.DEFAULT_USER_BUNDLE, locale));
	// jsonParam.put("type", biparam.getParameter().getType());
	// parametersJSON.put(jsonParam);
	// }
	// }
	// } catch (Exception e) {
	// logger.error("Error while adding document parameters info", e);
	// }
	// map.put("SBI_DOCUMENT_PARAMETERS", parametersJSON.toString());
	// logger.debug("OUT");
	// return map;
	// }

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
					if (value != null)
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

	public final static String PARAM_SERVICE_NAME = "ACTION_NAME";
	public final static String PARAM_NEW_SESSION = "NEW_SESSION";

	private Map applyService(Map parameters, BIObject biObject) {
		ObjTemplate template;

		logger.debug("IN");

		try {
			Assert.assertNotNull(parameters, "Input [parameters] cannot be null");

			template = getTemplate(biObject);
			if (template.getName().trim().toLowerCase().endsWith(".xml")) {
				parameters.put(PARAM_SERVICE_NAME, "QBE_ENGINE_START_ACTION");
			} else if (template.getName().trim().toLowerCase().endsWith(".json")) {
				parameters.put(PARAM_SERVICE_NAME, "FORM_ENGINE_START_ACTION");
			} else {
				Assert.assertUnreachable("Active template [" + template.getName() + "] extension is not valid (valid extensions are: .xml ; .json)");
			}

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

	private void appendRequestParameter(Map parameters, String pname, String pvalue) {
		parameters.put(pname, pvalue);
		logger.debug("Added parameter [" + pname + "] with value [" + pvalue + "] to request parameters list");
	}

	private Map applyDatasourceForWriting(Map parameters, BIObject biObject) {
		IDataSource datasource;
		try {
			datasource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while loading default datasource for writing", e);
		}
		if (datasource != null) {
			parameters.put(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL, datasource.getLabel());
		} else {
			logger.debug("There is no default datasource for writing");
		}
		return parameters;
	}

	@Override
	public ArrayList<String> getDatasetAssociated(byte[] contentTemplate) throws JSONException {
		// TODO Auto-generated method stub
		return null;
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
