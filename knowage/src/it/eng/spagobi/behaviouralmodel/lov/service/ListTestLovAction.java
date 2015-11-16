/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.behaviouralmodel.lov.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.dbaccess.Utils;
import it.eng.spago.dbaccess.sql.DataConnection;
import it.eng.spago.dbaccess.sql.DataRow;
import it.eng.spago.dbaccess.sql.SQLCommand;
import it.eng.spago.dbaccess.sql.result.DataResult;
import it.eng.spago.dbaccess.sql.result.ScrollableDataResult;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spago.paginator.basic.PaginatorIFace;
import it.eng.spago.paginator.basic.impl.GenericList;
import it.eng.spago.paginator.basic.impl.GenericPaginator;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.behaviouralmodel.lov.bo.DatasetDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.FixedListDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.IJavaClassLov;
import it.eng.spagobi.behaviouralmodel.lov.bo.JavaClassDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.ScriptDetail;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.services.DelegatedBasicListService;
import it.eng.spagobi.commons.utilities.DataSourceUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.json.JSONUtils;
import it.eng.spagobi.utilities.service.JSONFailure;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */
public class ListTestLovAction extends AbstractSpagoBIAction {

	private static Logger logger = Logger.getLogger(ListTestLovAction.class);
	private static final String PAGINATION_PAGE = "page";
	private static final String PAGINATION_START = "start";
	private static final String PAGINATION_LIMIT = "limit";

	public static final String SERVICE_NAME = "LIST_TEST_LOV_ACTION";

	@Override
	public void doService() {
		try {
			Exception responseFailure = null;
			JSONObject response = new JSONObject();
			GridMetadataContainer lovExecutionResult = new GridMetadataContainer();
			// define the spago paginator and list object
			PaginatorIFace paginator = new GenericPaginator();
			ListIFace list = new GenericList();
			// recover lov object
			RequestContainer requestContainer = getRequestContainer();
			SessionContainer session = requestContainer.getSessionContainer();
			ModalitiesValue modVal = (ModalitiesValue) session.getAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT);
			// get the lov provider
			String looProvider = modVal.getLovProvider();
			// get from the request the type of lov
			String typeLov = LovDetailFactory.getLovTypeCode(looProvider);
			// get the usser profile
			IEngUserProfile profile = null;
			profile = (IEngUserProfile) session.getAttribute(SpagoBIConstants.USER_PROFILE_FOR_TEST);
			if (profile == null) {
				SessionContainer permSess = session.getPermanentContainer();
				profile = (IEngUserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			} else {
				session.delAttribute(SpagoBIConstants.USER_PROFILE_FOR_TEST);
			}
			// based on lov type fill the spago list and paginator object
			SourceBean rowsSourceBean = null;
			List<String> colNames = new ArrayList<String>();
			if (typeLov.equalsIgnoreCase("QUERY")) {
				QueryDetail qd = QueryDetail.fromXML(looProvider);
				// String pool = qd.getConnectionName();
				String datasource = qd.getDataSource();
				String statement = qd.getQueryDefinition();
				// execute query
				try {
					statement = StringUtilities.substituteProfileAttributesInString(statement, profile);
					// rowsSourceBean = (SourceBean) executeSelect(getRequestContainer(), getResponseContainer(), pool, statement, colNames);
					rowsSourceBean = (SourceBean) executeSelect(getRequestContainer(), getResponseContainer(), datasource, statement, colNames);
				} catch (Exception e) {
					logger.error("Exception occurred executing query lov: ", e);
					String stacktrace = e.toString();
					response.put("stacktrace", stacktrace);
					int startIndex = stacktrace.indexOf("java.sql.");
					int endIndex = stacktrace.indexOf("\n\tat ", startIndex);
					if (endIndex == -1)
						endIndex = stacktrace.indexOf(" at ", startIndex);
					if (startIndex != -1 && endIndex != -1)
						response.put("errorMessage", stacktrace.substring(startIndex, endIndex));
					responseFailure = e;
					response.put("testExecuted", "false");
				}
			} else if (typeLov.equalsIgnoreCase("FIXED_LIST")) {
				FixedListDetail fixlistDet = FixedListDetail.fromXML(looProvider);
				try {
					String result = fixlistDet.getLovResult(profile, null, null, null);
					rowsSourceBean = SourceBean.fromXMLString(result);
					colNames = findFirstRowAttributes(rowsSourceBean);
					if (!rowsSourceBean.getName().equalsIgnoreCase("ROWS")) {
						throw new Exception("The fix list is empty");
					} else if (rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG).size() == 0) {
						throw new Exception("The fix list is empty");
					}
				} catch (Exception e) {
					SpagoBITracer
							.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "getList", "Error while converting fix lov into spago list", e);
					String stacktrace = e.toString();
					response.put("stacktrace", stacktrace);
					response.put("errorMessage", "Error while executing fix list lov");
					responseFailure = e;
					response.put("testExecuted", "false");

				}
			} else if (typeLov.equalsIgnoreCase("SCRIPT")) {
				ScriptDetail scriptDetail = ScriptDetail.fromXML(looProvider);
				try {
					String result = scriptDetail.getLovResult(profile, null, null, null);
					rowsSourceBean = SourceBean.fromXMLString(result);
					colNames = findFirstRowAttributes(rowsSourceBean);
				} catch (Exception e) {
					SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "getList", "Error while executing the script lov", e);
					String stacktrace = e.toString();
					response.put("stacktrace", stacktrace);
					response.put("errorMessage", "Error while executing script");
					responseFailure = e;
					response.put("testExecuted", "false");

				}
			} else if (typeLov.equalsIgnoreCase("JAVA_CLASS")) {
				JavaClassDetail javaClassDetail = JavaClassDetail.fromXML(looProvider);
				try {
					String javaClassName = javaClassDetail.getJavaClassName();
					IJavaClassLov javaClassLov = (IJavaClassLov) Class.forName(javaClassName).newInstance();
					String result = javaClassLov.getValues(profile);
					rowsSourceBean = SourceBean.fromXMLString(result);
					colNames = findFirstRowAttributes(rowsSourceBean);
				} catch (Exception e) {
					SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "getList", "Error while executing the java class lov", e);
					String stacktrace = e.toString();
					response.put("stacktrace", stacktrace);
					response.put("errorMessage", "Error while executing java class");
					responseFailure = e;
					response.put("testExecuted", "false");

				}
			} else if (typeLov.equalsIgnoreCase("DATASET")) {
				DatasetDetail datasetClassDetail = DatasetDetail.fromXML(looProvider);
				try {
					String result = datasetClassDetail.getLovResult(profile, null, null, null);
					rowsSourceBean = SourceBean.fromXMLString(result);
					colNames = findFirstRowAttributes(rowsSourceBean);
				} catch (Exception e) {
					SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "getList", "Error while executing the dataset lov", e);
					String stacktrace = e.toString();
					response.put("stacktrace", stacktrace);
					response.put("errorMessage", "Error while executing dataset");
					responseFailure = e;
					response.put("testExecuted", "false");

				}
			}
			if (rowsSourceBean != null) {

				// MANAGE THE PAGINATION
				// int page = getAttributeAsInteger(PAGINATION_PAGE);
				Integer start = getAttributeAsInteger(PAGINATION_START);
				Integer limit = getAttributeAsInteger(PAGINATION_LIMIT);

				// filter the list
				String valuefilter = getAttributeAsString(SpagoBIConstants.VALUE_FILTER);
				if (valuefilter != null) {
					String columnfilter = getAttributeAsString(SpagoBIConstants.COLUMNS_FILTER);
					String typeFilter = getAttributeAsString(SpagoBIConstants.TYPE_FILTER);
					String typeValueFilter = getAttributeAsString(SpagoBIConstants.TYPE_VALUE_FILTER);
					rowsSourceBean = DelegatedBasicListService.filterList(rowsSourceBean, valuefilter, typeValueFilter, columnfilter, typeFilter,
							getResponseContainer().getErrorHandler());
				}

				lovExecutionResult.setValues(toList(rowsSourceBean, start, limit));
				lovExecutionResult.setFields(GridMetadataContainer.buildHeaderMapForGrid(colNames));
				List rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
				lovExecutionResult.setResults(rows.size());

				response.put("testExecuted", "true");
			}

			if (response.getString("testExecuted").equals("false")) {
				try {
					logger.debug("OUT");
					JSONObject errorStackTrace = new JSONObject();
					errorStackTrace.put("stacktrace", responseFailure);
					errorStackTrace.put("error", "error");

					JSONObject error = new JSONObject();
					error.put("success", "false");
					error.put("message", "Error");
					error.put("data", errorStackTrace);

					writeBackToClient(new JSONSuccess(error));

				} catch (IOException e) {
					SpagoBIEngineServiceException serviceError = new SpagoBIEngineServiceException("Execution", "Error executing the cockpit");
					try {
						writeBackToClient(new JSONFailure(serviceError));
					} catch (Exception ex) {
						logger.error("Exception occurred writing back to client", ex);
						throw new SpagoBIServiceException("Exception occurred writing back to client", ex);
					}
					throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
				}
			} else {
				try {
					logger.debug("OUT");
					String toreturn = lovExecutionResult.toJSONString();
					writeBackToClient(new JSONSuccess(JSONUtils.toJSONObject(toreturn)));
				} catch (IOException e) {
					SpagoBIEngineServiceException serviceError = new SpagoBIEngineServiceException("Execution", "Error executing the cockpit");
					try {
						writeBackToClient(new JSONFailure(serviceError));
					} catch (Exception ex) {
						logger.error("Exception occurred writing back to client", ex);
						throw new SpagoBIServiceException("Exception occurred writing back to client", ex);
					}
					throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
				}
			}

		} catch (Exception e) {
			logger.error("Error testing lov", e);
			throw new SpagoBIServiceException("Error testing lov", e);
		}

	}

	/**
	 * Executes a select statement.
	 * 
	 * @param requestContainer
	 *            The request container object
	 * @param responseContainer
	 *            The response container object
	 * @param statement
	 *            The statement definition string
	 * @param datasource
	 *            the datasource
	 * @param columnsNames
	 *            the columns names
	 * 
	 * @return A generic object containing the Execution results
	 * 
	 * @throws EMFInternalError
	 *             the EMF internal error
	 */
	public static Object executeSelect(RequestContainer requestContainer, ResponseContainer responseContainer, String datasource, String statement,
			List columnsNames) throws EMFInternalError {
		// ResponseContainer responseContainer, String pool, String statement, List columnsNames) throws EMFInternalError {
		Object result = null;
		// DataConnectionManager dataConnectionManager = null;
		DataConnection dataConnection = null;
		SQLCommand sqlCommand = null;
		DataResult dataResult = null;
		try {
			/*
			 * dataConnectionManager = DataConnectionManager.getInstance(); dataConnection = dataConnectionManager.getConnection(pool);
			 */
			// gets connection
			DataSourceUtilities dsUtil = new DataSourceUtilities();
			Connection conn = dsUtil.getConnection(requestContainer, datasource);
			dataConnection = dsUtil.getDataConnection(conn);

			sqlCommand = dataConnection.createSelectCommand(statement, false);
			dataResult = sqlCommand.execute();
			ScrollableDataResult scrollableDataResult = (ScrollableDataResult) dataResult.getDataObject();
			List temp = Arrays.asList(scrollableDataResult.getColumnNames());
			columnsNames.addAll(temp);
			result = scrollableDataResult.getSourceBean();
		} catch (Exception e) {
			logger.error("Error in executing LOV query: " + statement);
			throw new SpagoBIServiceException(SERVICE_NAME, "Error inn executing LOV query");
		} finally {
			Utils.releaseResources(dataConnection, sqlCommand, dataResult);
		}
		return result;
	}

	/**
	 * Find the attributes of the first row of the xml passed at input: this xml is assumed to be: &lt;ROWS&gt; &lt;ROW attribute_1="value_of_attribute_1" ...
	 * /&gt; .... &lt;ROWS&gt;
	 * 
	 * @param rowsSourceBean
	 *            The sourcebean to be parsed
	 * @return the list of the attributes of the first row
	 */
	private List<String> findFirstRowAttributes(SourceBean rowsSourceBean) {
		List<String> columnsNames = new ArrayList<String>();
		if (rowsSourceBean != null) {
			List rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
			if (rows != null && rows.size() != 0) {
				SourceBean row = (SourceBean) rows.get(0);
				List rowAttrs = row.getContainedAttributes();
				Iterator rowAttrsIter = rowAttrs.iterator();
				while (rowAttrsIter.hasNext()) {
					SourceBeanAttribute rowAttr = (SourceBeanAttribute) rowAttrsIter.next();
					columnsNames.add(rowAttr.getKey());
				}
			}
		}
		return columnsNames;
	}

	private List<Map<String, String>> toList(SourceBean rowsSourceBean, Integer start, Integer limit) throws JSONException {
		Map<String, String> map;
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		int startIter = 0;
		int endIter;

		if (start != null) {
			startIter = start;
		}

		if (rowsSourceBean != null) {
			List<SourceBean> rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
			if (rows != null && rows.size() > 0) {
				if (limit != null && limit > 0) {
					endIter = startIter + limit;
					if (endIter > rows.size()) {
						endIter = rows.size();
					}
				} else {
					endIter = rows.size();
				}

				for (int i = startIter; i < endIter; i++) {
					JSONObject rowJson = new JSONObject();
					List<SourceBeanAttribute> rowAttrs = (rows.get(i)).getContainedAttributes();
					Iterator<SourceBeanAttribute> rowAttrsIter = rowAttrs.iterator();
					map = new HashMap<String, String>();
					while (rowAttrsIter.hasNext()) {
						SourceBeanAttribute rowAttr = rowAttrsIter.next();
						map.put(rowAttr.getKey(), (rowAttr.getValue()).toString());
					}
					list.add(map);
				}
			}
		}
		return list;
	}

}
