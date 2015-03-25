/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.services;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.dbaccess.DataConnectionManager;
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
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.FixedListDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.IJavaClassLov;
import it.eng.spagobi.behaviouralmodel.lov.bo.JavaClassDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.ScriptDetail;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.DelegatedBasicListService;
import it.eng.spagobi.commons.utilities.DataSourceUtilities;
import it.eng.spagobi.commons.utilities.PortletUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * Tests the query and produces the list as output. 
 */

public class LovLookupAjaxModule extends AbstractBasicListModule {
	
    static private Logger logger = Logger.getLogger(LovLookupAjaxModule.class);
	
	/**
	 * Class Constructor.
	 */
	public LovLookupAjaxModule() {
		super();
	} 
	
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.list.basic.IFaceBasicListService#getList(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public ListIFace getList(SourceBean request, SourceBean response) throws Exception {
		
	    logger.debug("IN");
		ListIFace list = null;
		// get role / par id / par field name name
		String roleName = (String)request.getAttribute("roleName");
		String parIdStr = (String)request.getAttribute("parameterId");
		logger.debug("roleName="+roleName);
		logger.debug("parameterId="+parIdStr);
		
		Integer parId = new Integer(parIdStr);
		// check if the parameter use is manual input
		IParameterUseDAO parusedao = DAOFactory.getParameterUseDAO();
		ParameterUse paruse = parusedao.loadByParameterIdandRole(parId, roleName);
		Integer manInp = paruse.getManualInput();
		if(manInp.intValue()==1) {
			String message = "";
			try{
				message = PortletUtilities.getMessage("scheduler.fillparmanually", "component_scheduler_messages");
			}catch (Exception e) {
				IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
				message = msgBuilder.getMessage("scheduler.fillparmanually","component_scheduler_messages");
			}
			response.setAttribute(SpagoBIConstants.MESSAGE_INFO, message);
		} else {
			list = loadSpagoList(request, response, parId, roleName);
		}
		// fill response
		response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "LovLookupAjax");
		logger.debug("OUT");
		return list;
	}

	
	private ListIFace loadSpagoList(SourceBean request, SourceBean response, Integer parId, String roleName) throws Exception {
	        logger.debug("IN");
		RequestContainer requestContainer = getRequestContainer();
		SessionContainer session = requestContainer.getSessionContainer();
		String parameterFieldName = (String)request.getAttribute("parameterFieldName");
		logger.debug("parameterFieldName="+parameterFieldName);
		// define the spago paginator and list object
		PaginatorIFace paginator = new GenericPaginator();
		ListIFace list = new GenericList();
		// define variable for value column name
		String valColName = "";
		// recover lov object	
		IParameterDAO pardao = DAOFactory.getParameterDAO();
		Parameter par = pardao.loadForExecutionByParameterIDandRoleName(parId, roleName);
		ModalitiesValue modVal = par.getModalityValue();
		// get the lov provider
		String looProvider = modVal.getLovProvider();
		// get from the request the type of lov
		String typeLov = LovDetailFactory.getLovTypeCode(looProvider);
		// get the user profile 
		IEngUserProfile profile = null;
		SessionContainer permanentSession = session.getPermanentContainer();
		profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
//		HttpServletRequest httpReq = (HttpServletRequest)requestContainer.getInternalRequest();
//		HttpSession httpSess = httpReq.getSession();
//		profile = (IEngUserProfile)httpSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		// based on lov type fill the spago list / paginator object / valColName
		SourceBean rowsSourceBean = null;
		if(typeLov.equalsIgnoreCase("QUERY")) {
			QueryDetail qd = QueryDetail.fromXML(looProvider);
//			if (qd.requireProfileAttributes()) {
//				String message = PortletUtilities.getMessage("scheduler.noProfileAttributesSupported", "component_scheduler_messages");
//				response.setAttribute(SpagoBIConstants.MESSAGE_INFO, message);
//				return list;
//			}
			valColName = qd.getValueColumnName();			
			//String pool = qd.getConnectionName();
			String datasource = qd.getDataSource();
			String statement = qd.getQueryDefinition();
			// execute query
			try {
				statement = StringUtilities.substituteProfileAttributesInString(statement, profile);
				//rowsSourceBean = (SourceBean) executeSelect(getRequestContainer(), getResponseContainer(), pool, statement);
				rowsSourceBean = (SourceBean) executeSelect(getRequestContainer(), getResponseContainer(), datasource, statement);
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
//			if (fixlistDet.requireProfileAttributes()) {
//				String message = PortletUtilities.getMessage("scheduler.noProfileAttributesSupported", "component_scheduler_messages");
//				response.setAttribute(SpagoBIConstants.MESSAGE_INFO, message);
//				return list;
//			}
			valColName = fixlistDet.getValueColumnName();
			try{
				String result = fixlistDet.getLovResult(profile, null, null, null);
				rowsSourceBean = SourceBean.fromXMLString(result);
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
//			if (scriptDetail.requireProfileAttributes()) {
//				String message = PortletUtilities.getMessage("scheduler.noProfileAttributesSupported", "component_scheduler_messages");
//				response.setAttribute(SpagoBIConstants.MESSAGE_INFO, message);
//				return list;
//			}
			valColName = scriptDetail.getValueColumnName();
			try{
				String result = scriptDetail.getLovResult(profile, null, null, null);
				rowsSourceBean = SourceBean.fromXMLString(result);
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
//			if (javaClassDetail.requireProfileAttributes()) {
//				String message = PortletUtilities.getMessage("scheduler.noProfileAttributesSupported", "component_scheduler_messages");
//				response.setAttribute(SpagoBIConstants.MESSAGE_INFO, message);
//				return list;
//			}
			valColName = javaClassDetail.getValueColumnName();
			try{		
				String javaClassName = javaClassDetail.getJavaClassName();
				IJavaClassLov javaClassLov = (IJavaClassLov) Class.forName(javaClassName).newInstance();
	    		String result = javaClassLov.getValues(profile);
        		rowsSourceBean = SourceBean.fromXMLString(result);
			} catch (Exception e) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
			                        "getList", "Error while executing the java class lov", e);
				String stacktrace = e.toString();
				response.setAttribute("stacktrace", stacktrace);
				response.setAttribute("errorMessage", "Error while executing java class");
				response.setAttribute("testExecuted", "false");
				return list;
			}
		}
		// fill paginator
		int count = 0;
		if(rowsSourceBean != null) {
			List rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
			for (int i = 0; i < rows.size(); i++){
				paginator.addRow(rows.get(i));
				count++;
			}
		}
		paginator.setPageSize(count);
		list.setPaginator(paginator);
		
		// get all the columns name
	    rowsSourceBean = list.getPaginator().getAll();
		List colNames = new ArrayList();
		List rows = null;
		if(rowsSourceBean != null) {
			rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
			if((rows!=null) && (rows.size()!=0)) {
				SourceBean row = (SourceBean)rows.get(0);
				List rowAttrs = row.getContainedAttributes();
				Iterator rowAttrsIter = rowAttrs.iterator();
				while(rowAttrsIter.hasNext()) {
					SourceBeanAttribute rowAttr = (SourceBeanAttribute)rowAttrsIter.next();
					colNames.add(rowAttr.getKey());
				}
			}
		}
		
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
		
		// fill response
		response.setAttribute(SpagoBIConstants.PARAMETER_FIELD_NAME, parameterFieldName);
		response.setAttribute(SpagoBIConstants.VALUE_COLUMN_NAME, valColName);
		logger.debug("OUT");	
		return list;
	}
	

	 /**
 	 * Executes a select statement.
 	 * 
 	 * @param requestContainer The request container object
 	 * @param responseContainer The response container object
 	 * @param statement The statement definition string
 	 * @param datasource the datasource
 	 * 
 	 * @return A generic object containing the Execution results
 	 * 
 	 * @throws EMFInternalError the EMF internal error
 	 * 
 	 * @deprecated
 	 */
		 public static Object executeSelect(RequestContainer requestContainer,
				ResponseContainer responseContainer, String datasource, String statement) throws EMFInternalError {
			Object result = null;
			DataConnectionManager dataConnectionManager = null;
			DataConnection dataConnection = null;
			SQLCommand sqlCommand = null;
			DataResult dataResult = null;
			try {
				//dataConnectionManager = DataConnectionManager.getInstance();
				//dataConnection = dataConnectionManager.getConnection(pool);
				DataSourceUtilities dsUtil = new DataSourceUtilities();
				Connection conn = dsUtil.getConnection(requestContainer,datasource); 
				dataConnection = dsUtil.getDataConnection(conn);

				sqlCommand = dataConnection.createSelectCommand(statement);
				dataResult = sqlCommand.execute();
				ScrollableDataResult scrollableDataResult = (ScrollableDataResult) dataResult
						.getDataObject();
				result = scrollableDataResult.getSourceBean();
			} finally {
				Utils.releaseResources(dataConnection, sqlCommand, dataResult);
			}
			return result;
		}

}
