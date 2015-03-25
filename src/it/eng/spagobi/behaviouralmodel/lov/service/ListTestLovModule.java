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
import it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule;
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
import it.eng.spagobi.commons.services.DelegatedBasicListService;
import it.eng.spagobi.commons.utilities.DataSourceUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.commons.utilities.StringUtilities;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tests the query and produces the list as output. 
 */

public class ListTestLovModule extends AbstractBasicListModule {
	
	/**
	 * Class Constructor.
	 */
	public ListTestLovModule() {
		super();
	} 
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.list.basic.IFaceBasicListService#getList(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public ListIFace getList(SourceBean request, SourceBean response) throws Exception {
			
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
		profile = (IEngUserProfile)session.getAttribute(SpagoBIConstants.USER_PROFILE_FOR_TEST);
		if(profile==null) {
			SessionContainer permSess = session.getPermanentContainer();
			profile = (IEngUserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		} else {
			session.delAttribute(SpagoBIConstants.USER_PROFILE_FOR_TEST);
		}
		// based on lov type fill the spago list and paginator object
		SourceBean rowsSourceBean = null;
		List colNames = new ArrayList();
		if(typeLov.equalsIgnoreCase("QUERY")) {
			QueryDetail qd = QueryDetail.fromXML(looProvider);
			//String pool = qd.getConnectionName();
			String datasource = qd.getDataSource();
			String statement = qd.getQueryDefinition();
			// execute query
			try {
				statement = StringUtilities.substituteProfileAttributesInString(statement, profile);
				//rowsSourceBean = (SourceBean) executeSelect(getRequestContainer(), getResponseContainer(), pool, statement, colNames);
				rowsSourceBean = (SourceBean) executeSelect(getRequestContainer(), getResponseContainer(), datasource, statement, colNames);
			} catch (Exception e) {
				String stacktrace = e.toString();
				response.setAttribute("stacktrace", stacktrace);
				int startIndex = stacktrace.indexOf("java.sql.");
				int endIndex = stacktrace.indexOf("\n\tat ", startIndex);
				if (endIndex == -1) endIndex = stacktrace.indexOf(" at ", startIndex);
				if (startIndex != -1 && endIndex != -1) 
					response.setAttribute("errorMessage", stacktrace.substring(startIndex, endIndex));
				response.setAttribute("testExecuted", "false");
			}
		} else if(typeLov.equalsIgnoreCase("FIXED_LIST")) {
			FixedListDetail fixlistDet = FixedListDetail.fromXML(looProvider);
			try{
				String result = fixlistDet.getLovResult(profile, null, null, null);
				rowsSourceBean = SourceBean.fromXMLString(result);
				colNames = findFirstRowAttributes(rowsSourceBean);
				if(!rowsSourceBean.getName().equalsIgnoreCase("ROWS")) {
					throw new Exception("The fix list is empty");
				} else if (rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG).size()==0) {
					throw new Exception("The fix list is empty");
				}
			} catch (Exception e) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
			            			"getList", "Error while converting fix lov into spago list", e);
				String stacktrace = e.toString();
				response.setAttribute("stacktrace", stacktrace);
				response.setAttribute("errorMessage", "Error while executing fix list lov");
				response.setAttribute("testExecuted", "false");
				return list;
			}
		} else if(typeLov.equalsIgnoreCase("SCRIPT")) {
			ScriptDetail scriptDetail = ScriptDetail.fromXML(looProvider);
			try{
				String result = scriptDetail.getLovResult(profile, null, null, null);
				rowsSourceBean = SourceBean.fromXMLString(result);
				colNames = findFirstRowAttributes(rowsSourceBean);
			} catch (Exception e) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
						            "getList", "Error while executing the script lov", e);
				String stacktrace = e.toString();
				response.setAttribute("stacktrace", stacktrace);
				response.setAttribute("errorMessage", "Error while executing script");
				response.setAttribute("testExecuted", "false");
				return list;
			}
		} else if(typeLov.equalsIgnoreCase("JAVA_CLASS")) {
			JavaClassDetail javaClassDetail = JavaClassDetail.fromXML(looProvider);
			try{		
				String javaClassName = javaClassDetail.getJavaClassName();
				IJavaClassLov javaClassLov = (IJavaClassLov) Class.forName(javaClassName).newInstance();
	    		String result = javaClassLov.getValues(profile);
        		rowsSourceBean = SourceBean.fromXMLString(result);
        		colNames = findFirstRowAttributes(rowsSourceBean);
			} catch (Exception e) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
			                        "getList", "Error while executing the java class lov", e);
				String stacktrace = e.toString();
				response.setAttribute("stacktrace", stacktrace);
				response.setAttribute("errorMessage", "Error while executing java class");
				response.setAttribute("testExecuted", "false");
				return list;
			}
		} else if(typeLov.equalsIgnoreCase("DATASET")) {
			DatasetDetail datasetClassDetail = DatasetDetail.fromXML(looProvider);
			try{		
				String result = datasetClassDetail.getLovResult(profile, null, null, null);
				rowsSourceBean = SourceBean.fromXMLString(result);
				colNames = findFirstRowAttributes(rowsSourceBean);
			} catch (Exception e) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
			                        "getList", "Error while executing the dataset lov", e);
				String stacktrace = e.toString();
				response.setAttribute("stacktrace", stacktrace);
				response.setAttribute("errorMessage", "Error while executing dataset");
				response.setAttribute("testExecuted", "false");
				return list;
			}
		}
		
		// fill paginator
		if(rowsSourceBean != null) {
			List rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
			for (int i = 0; i < rows.size(); i++)
				paginator.addRow(rows.get(i));
		}
		list.setPaginator(paginator);
		
		// build module configuration for the list
		String moduleConfigStr = "";
		moduleConfigStr += "<CONFIG>";
		moduleConfigStr += "	<QUERIES/>";
		moduleConfigStr += "	<COLUMNS>";
	    // if there's no colum name add a fake column to show that there's no data
		if(colNames.size()==0) {
			moduleConfigStr += "	<COLUMN name=\"No Result Found\" />";
		} else {
			Iterator iterColNames = colNames.iterator();
			while(iterColNames.hasNext()) {
				String colName = (String)iterColNames.next();
				moduleConfigStr += "	<COLUMN name=\"" + colName + "\" />";
			}
		}
		moduleConfigStr += "	</COLUMNS>";
		moduleConfigStr += "	<CAPTIONS/>";
		moduleConfigStr += "	<BUTTONS/>";
		moduleConfigStr += "</CONFIG>";
		SourceBean moduleConfig = SourceBean.fromXMLString(moduleConfigStr);
		response.setAttribute(moduleConfig);

		
		// filter the list 
		String valuefilter = (String) request.getAttribute(SpagoBIConstants.VALUE_FILTER);
		if (valuefilter != null) {
			String columnfilter = (String) request.getAttribute(SpagoBIConstants.COLUMN_FILTER);
			String typeFilter = (String) request.getAttribute(SpagoBIConstants.TYPE_FILTER);
			String typeValueFilter = (String) request.getAttribute(SpagoBIConstants.TYPE_VALUE_FILTER);
			list = DelegatedBasicListService.filterList(list, valuefilter, typeValueFilter, 
					columnfilter, typeFilter, getResponseContainer().getErrorHandler());
		}
		
		response.setAttribute("testExecuted", "true");
	
		return list;
	}

	
	
	/**
	 * Find the attributes of the first row of the xml passed at input: this xml is assumed to be:
	 * &lt;ROWS&gt;
	 * 	&lt;ROW attribute_1="value_of_attribute_1" ... /&gt;
	 * 	....
	 * &lt;ROWS&gt; 
	 * 
	 * @param rowsSourceBean The sourcebean to be parsed
	 * @return the list of the attributes of the first row
	 */
	private List findFirstRowAttributes(SourceBean rowsSourceBean) {
		List columnsNames = new ArrayList();
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

	/**
	 * Executes a select statement.
	 * 
	 * @param requestContainer The request container object
	 * @param responseContainer The response container object
	 * @param statement The statement definition string
	 * @param datasource the datasource
	 * @param columnsNames the columns names
	 * 
	 * @return A generic object containing the Execution results
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	 public static Object executeSelect(RequestContainer requestContainer,
			 ResponseContainer responseContainer, String datasource, String statement, List columnsNames) throws EMFInternalError {
			//ResponseContainer responseContainer, String pool, String statement, List columnsNames) throws EMFInternalError {
		Object result = null;
		//DataConnectionManager dataConnectionManager = null;
		DataConnection dataConnection = null;
		SQLCommand sqlCommand = null;
		DataResult dataResult = null;
		try {
			/*dataConnectionManager = DataConnectionManager.getInstance();
			dataConnection = dataConnectionManager.getConnection(pool);
			*/
			//gets connection
			DataSourceUtilities dsUtil = new DataSourceUtilities();
			Connection conn = dsUtil.getConnection(requestContainer,datasource); 
			dataConnection = dsUtil.getDataConnection(conn);

			sqlCommand = dataConnection.createSelectCommand(statement, false);
			dataResult = sqlCommand.execute();
			ScrollableDataResult scrollableDataResult = (ScrollableDataResult) dataResult
					.getDataObject();
			List temp = Arrays.asList(scrollableDataResult.getColumnNames());
			columnsNames.addAll(temp);
			result = scrollableDataResult.getSourceBean();
		} finally {
			Utils.releaseResources(dataConnection, sqlCommand, dataResult);
		}
		return result;
	}

}
