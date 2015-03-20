/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.engines.InternalEngineIFace;
import it.eng.spagobi.engines.chart.bo.ChartImpl;
import it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart;
import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.BarCharts;
import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.LinkableBar;
import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.StackedBarGroup;
import it.eng.spagobi.engines.chart.bo.charttypes.clusterchart.ClusterCharts;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.DrillParameter;
import it.eng.spagobi.engines.chart.utils.AttributesContainer;
import it.eng.spagobi.engines.chart.utils.DatasetMap;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.service.ManageDatasets;
import it.eng.spagobi.utilities.json.JSONTemplateUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Internal Engine * @author Giulio Gavardi giulio.gavardi@eng.it
 */

public class SpagoBIChartInternalEngine implements InternalEngineIFace {

	private static transient Logger logger = Logger.getLogger(SpagoBIChartInternalEngine.class);

	// public static final String messageBundle =
	// "component_spagobichartKPI_messages";
	public static final String messageBundle = "MessageFiles.component_spagobidashboardIE_messages";
	public static final String HIGHCHART_TEMPLATE = "HIGHCHART";

	/**
	 * This method is used to execute a chart code way and returning the image
	 * chart execution Pay attention that must get the parameters from BiObject
	 * in order to filter categories and series
	 * 
	 * All the parameters must be taken not from request but from BiObject
	 * 
	 * @param requestContainer
	 * @param obj
	 * @param response
	 * @throws EMFUserError
	 */
	public File executeChartCode(RequestContainer requestContainer, BIObject obj, SourceBean response, IEngUserProfile userProfile) throws EMFUserError {
		logger.debug("IN");

		File toReturn = null;
		Locale locale = GeneralUtilities.getDefaultLocale();
		DatasetMap datasets = null;
		ChartImpl sbi = null;

		String documentId = obj.getId().toString();

		// **************get the template*****************
		logger.debug("getting template");
		SourceBean serviceRequest = requestContainer.getServiceRequest();

		try {
			// SourceBean content = getTemplate(documentId);
			SourceBean content = null;
			byte[] contentBytes = null;
			try {
				ObjTemplate template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(Integer.valueOf(documentId));
				if (template == null)
					throw new Exception("Active Template null");
				contentBytes = template.getContent();
				if (contentBytes == null) {
					logger.error("TEMPLATE DOESN'T EXIST !!!!!!!!!!!!!!!!!!!!!!!!!!!");
					EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 2007);
					userError.setBundle("messages");
					throw userError;
				}

				// get bytes of template and transform them into a SourceBean

				String contentStr = new String(contentBytes);
				content = SourceBean.fromXMLString(contentStr);
			} catch (Exception e) {
				logger.error("Error while converting the Template bytes into a SourceBean object");
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 2003);
				userError.setBundle("messages");
				throw userError;
			}
			// **************take informations on the chart
			// type*****************
			String type = content.getName();
			String subtype = (String) content.getAttribute("type");
			String data = "";
			try {
				logger.debug("Getting Data Set ID");
				data = obj.getDataSetId().toString();
			} catch (Exception e) {
				logger.error("Error while getting the dataset ", e);
				return null;
			}

			// Map parametersMap=getParametersCode(obj);
			Map parametersMap = getParameters(obj);

			try {
				logger.debug("create the chart");
				// set the right chart type
				sbi = ChartImpl.createChart(type, subtype);
				sbi.setProfile(userProfile);
				sbi.setType(type);
				sbi.setSubtype(subtype);
				sbi.setData(data);
				sbi.setLocale(locale);
				sbi.setParametersObject(parametersMap);
				// configure the chart with template parameters
				sbi.configureChart(content);
				sbi.setLocalizedTitle();

				// Don't care for linkable charts configuration because we are
				// building for static exporting

			} catch (Exception e) {
				logger.error("Error while creating the chart", e);
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 2004);
				userError.setBundle("messages");
				throw userError;
			}

			datasets = retrieveDatasetValue(sbi);

			String series = null;
			String categories = null;
			String catGroups = null;
			String n_visualization = null;

			// check if serie or categories or cat_group have been set, here you
			// have to check among parameters
			if (parametersMap.get("serie") != null) {// check what happens in
														// case of multivalue!
				series = parametersMap.get("serie").toString();
			}
			if (parametersMap.get("cat_group") != null) {// check what happens
															// in case of
															// multivalue!
				catGroups = parametersMap.get("cat_group").toString();
			}

			if (parametersMap.get("categoryAll") != null) {
				categories = "0";
			} else if (parametersMap.get("category") != null) {
				categories = parametersMap.get("category").toString();
				if (categories.equals("0"))
					categories = "1";
			}

			if (parametersMap.get("n_visualization") != null) {// check what
																// happens in
																// case of
																// multivalue!
				n_visualization = parametersMap.get("n_visualization").toString();
			}

			// Now I have to filter the dataset and draw the image!
			AttributesContainer attCont = new AttributesContainer(parametersMap);
			DatasetMap copyDatasets = null;

			// use the same code used in chart.jsp to filter various type of
			// chart
			datasets.setSelectedSeries(new Vector());
			if (sbi.getType().equalsIgnoreCase("BARCHART") || sbi.getType().equalsIgnoreCase("CLUSTERCHART")) {
				// if(sbi.getSubtype().equalsIgnoreCase("simplebar") ||
				// sbi.getSubtype().equalsIgnoreCase("linkableBar") ||
				// sbi.getSubtype().equalsIgnoreCase("stacked_bar") ||
				// sbi.getSubtype().equalsIgnoreCase("stacked_bar_group")){
				if (sbi.getSubtype().equalsIgnoreCase("simplebar") || sbi.getSubtype().equalsIgnoreCase("linkableBar")
						|| sbi.getSubtype().equalsIgnoreCase("stacked_bar")) {
					// returns a new datasets map filtered
					copyDatasets = datasets.filteringSimpleBarChartUtil(attCont, attCont, (BarCharts) sbi, "WEB", true);
				} else if (sbi.getSubtype().equalsIgnoreCase("overlaid_barline") || sbi.getSubtype().equalsIgnoreCase("overlaid_stackedbarline")
						|| sbi.getSubtype().equalsIgnoreCase("combined_category_bar")) {
					copyDatasets = datasets.filteringMultiDatasetBarChartUtil(attCont, attCont, (BarCharts) sbi, "WEB", true);
				} else if (sbi.getSubtype().equalsIgnoreCase("simplecluster")) {
					copyDatasets = datasets.filteringClusterChartUtil(attCont, (ClusterCharts) sbi, "WEB", true);
				} else if (sbi.getSubtype().equalsIgnoreCase("stacked_bar_group")) {
					copyDatasets = datasets.filteringGroupedBarChartUtil(attCont, attCont, (StackedBarGroup) sbi, "WEB", true);
				}
			} else {
				copyDatasets = datasets;
			}

			// TODO MultiCHart export
			if (sbi.getMultichart()) {
				logger.debug("no treated yet multichart export");
			} else {

				JFreeChart chart = null;
				chart = sbi.createChart(copyDatasets);
				String dir = System.getProperty("java.io.tmpdir");
				Random generator = new Random();
				int randomInt = generator.nextInt();
				String path = dir + "/" + Integer.valueOf(randomInt).toString() + ".png";
				// String path=dir+"/"+executionId+".png";
				toReturn = new java.io.File(path);

				ChartUtilities.saveChartAsPNG(toReturn, chart, sbi.getWidth(), sbi.getHeight(), null);

			}

		} catch (Exception e) {
			logger.error("Error in executing th chart");
		}

		logger.debug("OUT");
		return toReturn;

	}

	public void execute(RequestContainer requestContainer, BIObject obj, SourceBean response) throws EMFUserError {

		SessionContainer session = requestContainer.getSessionContainer();
		IEngUserProfile userProfile = (IEngUserProfile) session.getPermanentContainer().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		String userId = (String) ((UserProfile) userProfile).getUserId();

		Locale locale = GeneralUtilities.getDefaultLocale();
		String lang = (String) session.getPermanentContainer().getAttribute(SpagoBIConstants.AF_LANGUAGE);
		String country = (String) session.getPermanentContainer().getAttribute(SpagoBIConstants.AF_COUNTRY);
		if (lang != null && country != null) {
			locale = new Locale(lang, country, "");
		}
		// defines the chart type for the correct execution
		ResponseContainer responseContainer = ResponseContainer.getResponseContainer();
		EMFErrorHandler errorHandler = responseContainer.getErrorHandler();
		String chartType = getChartType(obj, errorHandler);
		// Template templateUtil = new Template();
		JSONTemplateUtils templateUtil = new JSONTemplateUtils();
		try {
			if (chartType != null && chartType.startsWith(HIGHCHART_TEMPLATE)) {
				// gets the dataset object informations
				Integer id = obj.getDataSetId();
				IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(id);

				ManageDatasets mDs = new ManageDatasets();
				JSONArray parsJSON = getParametersAsJSON(obj);
				// converts the template from xml to json format
				JSONObject template = templateUtil.getJSONTemplateFromXml(getTemplate(obj.getId().toString()), parsJSON);
				// sets the response
				response.setAttribute("template", template);
				response.setAttribute("divWidth", (templateUtil.getDivWidth() == null) ? "100%" : templateUtil.getDivWidth());
				response.setAttribute("divHeight", (templateUtil.getDivHeight() == null) ? "100%" : templateUtil.getDivHeight());
				response.setAttribute("themeHighchart", (templateUtil.getTheme() == null) ? "" : templateUtil.getTheme());
				response.setAttribute("numCharts", (templateUtil.getNumCharts() == null) ? 1 : templateUtil.getNumCharts());
				response.setAttribute("subType", (templateUtil.getSubType() == null) ? 1 : templateUtil.getSubType());
				response.setAttribute(DataSetConstants.ID, dataset.getId());
				response.setAttribute(DataSetConstants.LABEL, dataset.getLabel());
				response.setAttribute(DataSetConstants.DS_TYPE_CD, (dataset.getDsType() == null) ? "" : dataset.getDsType());
				response.setAttribute(DataSetConstants.PARS, parsJSON);
				response.setAttribute(DataSetConstants.TRASFORMER_TYPE_CD, (dataset.getTransformerCd() == null) ? "" : dataset.getTransformerCd());
				response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "HIGHCHART");
				response.setAttribute("documentLabel", obj.getLabel());
			} else {
				executeChart(requestContainer, obj, response, userProfile, locale);
			}
		} catch (Exception e) {
			logger.error("Error in execution chart. " + e.getLocalizedMessage());
			throw new EMFUserError(EMFErrorSeverity.ERROR, "100", messageBundle);
		}
	}

	/**
	 * Executes the document and populates the response.
	 * 
	 * @param requestContainer
	 *            The <code>RequestContainer</code>chartImp object (the session
	 *            can be retrieved from this object)
	 * @param obj
	 *            The <code>BIObject</code> representing the document to be
	 *            executed
	 * @param response
	 *            The response <code>SourceBean</code> to be populated
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */

	public void executeChart(RequestContainer requestContainer, BIObject obj, SourceBean response, IEngUserProfile userProfile, Locale locale)
			throws EMFUserError {
		DatasetMap datasets = null;
		ChartImpl sbi = null;

		// RequestContainer
		// requestContainer=RequestContainer.getRequestContainer();
		SourceBean serviceRequest = requestContainer.getServiceRequest();
		ResponseContainer responseContainer = ResponseContainer.getResponseContainer();
		EMFErrorHandler errorHandler = responseContainer.getErrorHandler();
		SourceBean content = getTemplate(obj.getId().toString());

		// **************take informations on the chart type*****************
		try {
			String type = content.getName();
			String subtype = (String) content.getAttribute("type");
			String data = getDataset(obj);

			Map parametersMap = getParameters(obj);

			try {
				logger.debug("create the chart");
				// set the right chart type
				sbi = ChartImpl.createChart(type, subtype);
				sbi.setProfile(userProfile);
				sbi.setType(type);
				sbi.setSubtype(subtype);
				sbi.setData(data);
				sbi.setLocale(locale);
				sbi.setParametersObject(parametersMap);
				// configure the chart with template parameters
				sbi.configureChart(content);
				sbi.setLocalizedTitle();

				boolean linkable = sbi.isLinkable();
				if (linkable) {
					logger.debug("Linkable chart, search in request for serieurlname or categoryurlname");
					String serieurlname = "";
					String categoryurlname = "";

					// checjk if is a linkable bar or pie
					boolean linkableBar = false;
					if (sbi instanceof LinkableBar)
						linkableBar = true;
					else
						linkableBar = false;

					// check is these parameters are in request, if not take
					// them from template, if not use series and category by
					// default

					if (linkableBar) {
						if (serviceRequest != null && serviceRequest.getAttribute("serieurlname") != null) {
							serieurlname = (String) serviceRequest.getAttribute("serieurlname");
							((LinkableBar) sbi).setSerieUrlname(serieurlname);
						}
					}

					// category is defined both for pie and bar linkable charts
					if (serviceRequest != null && serviceRequest.getAttribute("categoryurlname") != null) {
						categoryurlname = (String) serviceRequest.getAttribute("categoryurlname");
						((ILinkableChart) sbi).setCategoryUrlName(categoryurlname);
					}

					// check if there are other parameters from the drill
					// parameters whose value is in the request; elsewhere take
					// them from template
					logger.debug("Linkable chart: search in the request for other parameters");
					HashMap<String, DrillParameter> drillParametersMap = new HashMap<String, DrillParameter>();

					if (((ILinkableChart) sbi).getDrillParametersMap() != null) {

						drillParametersMap = (HashMap) ((ILinkableChart) sbi).getDrillParametersMap().clone();

						// if finds that a parameter is in the request
						// substitute the value; but only if in RELATIVE MODE
						for (Iterator iterator = drillParametersMap.keySet().iterator(); iterator.hasNext();) {
							String name = (String) iterator.next();
							DrillParameter drillPar = drillParametersMap.get(name);
							String typePar = drillPar.getType();
							// if relative put new value!
							if (typePar.equalsIgnoreCase("relative")) {
								if (serviceRequest != null && serviceRequest.getAttribute(name) != null) {
									String value = (String) serviceRequest.getAttribute(name);
									((ILinkableChart) sbi).getDrillParametersMap().remove(name);
									drillPar.setValue(value);
									((ILinkableChart) sbi).getDrillParametersMap().put(name, drillPar);
								}
							}

						}
					}

				}

			} catch (Exception e) {
				logger.error("Error while creating the chart", e);
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 2004);
				userError.setBundle("messages");
				throw userError;
			}

			datasets = retrieveDatasetValue(sbi);
			// create the chart

			// in the re-drawing case in document-composition check if serie or
			// categories or cat_group have been set
			String serie = null;
			String category = null;
			String catGroup = null;
			if (serviceRequest != null && serviceRequest.getAttribute("serie") != null) {
				List series = serviceRequest.getAttributeAsList("serie");
				for (Iterator it = series.iterator(); it.hasNext();) {
					serie = (String) it.next();
					response.setAttribute("serie", serie);
				}
			}

			if (serviceRequest != null && serviceRequest.getAttribute("cat_group") != null) {
				List catGroups = serviceRequest.getAttributeAsList("cat_group");
				for (Iterator it = catGroups.iterator(); it.hasNext();) {
					catGroup = (String) it.next();
					response.setAttribute("cat_group", catGroup);
				}
			}

			// If categoryAll check is checked it overwrites previous
			// informations about slider
			if (serviceRequest != null && serviceRequest.getAttribute("categoryAll") != null) {
				response.setAttribute("category", "0");
			} else if (serviceRequest != null && serviceRequest.getAttribute("category") != null) {
				Object catO = serviceRequest.getAttribute("category");
				category = "";
				try {

					category = (String) catO;
				} catch (Exception e) {
					Integer catI = (Integer) catO;
					category = catI.toString();
				}
				// if category is 0 but categoryAll is not defined means that
				// categoryAll has just been de-selected, so put category to 1
				if (category.equals("0")) {
					category = "1";
				}
				response.setAttribute("category", category);
			}

			// if dinamically changed the number categories visualization
			if (serviceRequest != null && serviceRequest.getAttribute("n_visualization") != null) {
				Object nVis = serviceRequest.getAttribute("n_visualization");
				response.setAttribute("n_visualization", nVis);
			}

			try {
				// chart = sbi.createChart(title,dataset);
				logger.debug("successfull chart creation");
				if (serviceRequest != null && response != null) {
					String executionId = (String) serviceRequest.getAttribute("SBI_EXECUTION_ID");
					if (executionId != null)
						response.setAttribute("SBI_EXECUTION_ID", executionId);
					response.setAttribute("datasets", datasets);
					response.setAttribute(ObjectsTreeConstants.SESSION_OBJ_ATTR, obj);
					response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "CHARTKPI");
					response.setAttribute("sbi", sbi);
				}
			} catch (Exception eex) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 2004);
				userError.setBundle("messages");
				throw userError;
			}

			logger.debug("OUT");

		} catch (EMFUserError e) {

			errorHandler.addError(e);

		} catch (Exception e) {
			EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);
			logger.error("Generic Error");
			errorHandler.addError(userError);

		}

	}

	/**
	 * The <code>SpagoBIDashboardInternalEngine</code> cannot manage subobjects
	 * so this method must not be invoked.
	 * 
	 * @param requestContainer
	 *            The <code>RequestContainer</code> object (the session can be
	 *            retrieved from this object)
	 * @param obj
	 *            The <code>BIObject</code> representing the document
	 * @param response
	 *            The response <code>SourceBean</code> to be populated
	 * @param subObjectInfo
	 *            An object describing the subobject to be executed
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void executeSubObject(RequestContainer requestContainer, BIObject obj, SourceBean response, Object subObjectInfo) throws EMFUserError {
		// it cannot be invoked
		logger.error("SpagoBIDashboardInternalEngine cannot exec subobjects.");
		throw new EMFUserError(EMFErrorSeverity.ERROR, "101", messageBundle);
	}

	/**
	 * Function not implemented. Thid method should not be called
	 * 
	 * @param requestContainer
	 *            The <code>RequestContainer</code> object (the session can be
	 *            retrieved from this object)
	 * @param response
	 *            The response <code>SourceBean</code> to be populated
	 * @param obj
	 *            the obj
	 * 
	 * @throws InvalidOperationRequest
	 *             the invalid operation request
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void handleNewDocumentTemplateCreation(RequestContainer requestContainer, BIObject obj, SourceBean response) throws EMFUserError,
			InvalidOperationRequest {
		logger.error("SpagoBIDashboardInternalEngine cannot build document template.");
		throw new InvalidOperationRequest();

	}

	/**
	 * Function not implemented. Thid method should not be called
	 * 
	 * @param requestContainer
	 *            The <code>RequestContainer</code> object (the session can be
	 *            retrieved from this object)
	 * @param response
	 *            The response <code>SourceBean</code> to be populated
	 * @param obj
	 *            the obj
	 * 
	 * @throws InvalidOperationRequest
	 *             the invalid operation request
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void handleDocumentTemplateEdit(RequestContainer requestContainer, BIObject obj, SourceBean response) throws EMFUserError, InvalidOperationRequest {
		logger.error("SpagoBIDashboardInternalEngine cannot build document template.");
		throw new InvalidOperationRequest();
	}

	public SourceBean getTemplate(String documentId) throws EMFUserError {
		SourceBean content = null;
		byte[] contentBytes = null;
		try {
			ObjTemplate template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(Integer.valueOf(documentId));
			if (template == null)
				throw new Exception("Active Template null");
			contentBytes = template.getContent();
			if (contentBytes == null) {
				logger.error("TEMPLATE DOESN'T EXIST !!!!!!!!!!!!!!!!!!!!!!!!!!!");
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 2007);
				userError.setBundle("messages");
				throw userError;
			}

			// get bytes of template and transform them into a SourceBean

			String contentStr = new String(contentBytes);
			content = SourceBean.fromXMLString(contentStr);
		} catch (Exception e) {
			logger.error("Error while converting the Template bytes into a SourceBean object");
			EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 2003);
			userError.setBundle("messages");
			throw userError;
		}
		return content;

	}

	/**
	 * COnverts from BIObject Parameters to a map, in presence of multi value
	 * merge with ,
	 * 
	 * @param obj
	 * @return
	 */

	public Map getParameters(BIObject obj) {
		HashMap parametersMap = null;

		// Search if the chart has parameters
		List parametersList = obj.getBiObjectParameters();
		logger.debug("Check for BIparameters and relative values");
		if (parametersList != null) {
			parametersMap = new HashMap();
			for (Iterator iterator = parametersList.iterator(); iterator.hasNext();) {
				BIObjectParameter par = (BIObjectParameter) iterator.next();
				String url = par.getParameterUrlName();
				List values = par.getParameterValues();
				if (values != null) {
					if (values.size() == 1) {
						String value = (String) values.get(0);
						Parameter parameter = par.getParameter();
						if (parameter != null) {
							String parType = parameter.getType();
							if (parType.equalsIgnoreCase(SpagoBIConstants.STRING_TYPE_FILTER) || parType.equalsIgnoreCase(SpagoBIConstants.DATE_TYPE_FILTER)) {
								value = value;
							}
						}
						parametersMap.put(url, value);
					} else if (values.size() >= 1) {
						String type = (par.getParameter() != null) ? par.getParameter().getType() : SpagoBIConstants.STRING_TYPE_FILTER;
						// if par is a string or a date close with '', else not
						String value = "";
						if (type.equalsIgnoreCase(SpagoBIConstants.STRING_TYPE_FILTER) || type.equalsIgnoreCase(SpagoBIConstants.DATE_TYPE_FILTER)) {
							value = "'" + (String) values.get(0) + "'";
							for (int k = 1; k < values.size(); k++) {
								value = value + ",'" + (String) values.get(k) + "'";
							}
						} else {
							value = (String) values.get(0);
							for (int k = 1; k < values.size(); k++) {
								value = value + "," + (String) values.get(k) + "";
							}
						}

						parametersMap.put(url, value);
					}
				}
			}

		} // end looking for parameters
		return parametersMap;
	}

	/**
	 * COnverts from BIObject Parameters to a map, in presence of multi value
	 * merge with ,
	 * 
	 * @param obj
	 * @return
	 */

	public JSONArray getParametersAsJSON(BIObject obj) {
		JSONArray JSONPars = new JSONArray();

		// Search if the chart has parameters
		List parametersList = obj.getBiObjectParameters();
		logger.debug("Check for BIparameters and relative values");
		if (parametersList != null) {
			for (Iterator iterator = parametersList.iterator(); iterator.hasNext();) {
				BIObjectParameter par = (BIObjectParameter) iterator.next();
				String name = par.getParameterUrlName();
				String value = "";
				List values = par.getParameterValues();
				if (values != null) {
					if (values.size() == 1) {
						value = (String) values.get(0);
						Parameter parameter = par.getParameter();
						if (parameter != null) {
							String parType = parameter.getType();
							if (parType.equalsIgnoreCase(SpagoBIConstants.STRING_TYPE_FILTER) || parType.equalsIgnoreCase(SpagoBIConstants.DATE_TYPE_FILTER)) {
								value = value;
							}
						}
					} else if (values.size() >= 1) {
						String type = (par.getParameter() != null) ? par.getParameter().getType() : SpagoBIConstants.STRING_TYPE_FILTER;
						// if par is a string or a date close with '', else not
						if (type.equalsIgnoreCase(SpagoBIConstants.STRING_TYPE_FILTER) || type.equalsIgnoreCase(SpagoBIConstants.DATE_TYPE_FILTER)) {
							value = (String) values.get(0);
							for (int k = 1; k < values.size(); k++) {
								value = value + "," + (String) values.get(k);
							}
						} else {
							value = (String) values.get(0);
							for (int k = 1; k < values.size(); k++) {
								value = value + "," + (String) values.get(k) + "";
							}
						}
					}
					try {
						JSONObject JSONObj = new JSONObject();
						JSONObj.put("name", name);
						JSONObj.put("value", value);
						JSONPars.put(JSONObj);
					} catch (Exception e) {
						logger.warn("Impossible to load parameter object " + name + " whose value is " + value + " to JSONObject", e);
					}
				}
			}

		} // end looking for parameters
		return JSONPars;
	}

	public DatasetMap retrieveDatasetValue(ChartImpl sbi) throws EMFUserError {
		DatasetMap datasets = null;
		// calculate values for the chart
		try {
			logger.debug("Retrieve value by executing the dataset");
			datasets = sbi.calculateValue();
		} catch (Exception e) {
			logger.error("Error in retrieving the value", e);
			EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 2006);
			userError.setBundle("messages");
			throw userError;
		}

		return datasets;
	}

	private String getChartType(BIObject obj, EMFErrorHandler errorHandler) throws EMFUserError {
		SourceBean template = getTemplate(obj.getId().toString());
		if (template == null) {
			logger.error("The template object is null.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, "100", messageBundle);
		}
		return template.getName();
	}

	private String getDataset(BIObject obj) throws EMFUserError {
		String toReturn = "";
		try {
			logger.debug("Getting Data Set ID");
			if (obj.getDataSetId() != null) {
				toReturn = obj.getDataSetId().toString();
			} else {
				logger.error("Data Set not defined");
				throw new Exception("Data Set not defined");
			}
		} catch (Exception e) {
			logger.error("Error while getting the dataset");
			EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 9207);
			userError.setBundle("messages");
			throw userError;
		}
		return toReturn;
	}

}
