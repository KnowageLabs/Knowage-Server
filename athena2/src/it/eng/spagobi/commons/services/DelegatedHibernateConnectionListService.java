/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.services;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dbaccess.sql.DataConnection;
import it.eng.spago.dbaccess.sql.DataRow;
import it.eng.spago.dbaccess.sql.mappers.SQLMapper;
import it.eng.spago.dispatching.service.RequestContextIFace;
import it.eng.spago.dispatching.service.ServiceIFace;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.init.InitializerIFace;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spago.paginator.basic.PaginatorIFace;
import it.eng.spago.paginator.basic.impl.GenericList;
import it.eng.spago.paginator.basic.impl.GenericPaginator;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

import java.sql.Connection;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DelegatedHibernateConnectionListService extends DelegatedBasicListService {
	
	/**
	 * Gets the list.
	 * 
	 * @param service the service
	 * @param request the request
	 * @param response the response
	 * 
	 * @return the list
	 * 
	 * @throws Exception the exception
	 */
	public static ListIFace getList(ServiceIFace service, SourceBean request, SourceBean response) throws Exception {

		Session aSession = null;
		Transaction tx = null;
		PaginatorIFace paginator = new GenericPaginator();
		String currentFieldOrder = (request.getAttribute("FIELD_ORDER") == null || ((String)request.getAttribute("FIELD_ORDER")).equals(""))?"":(String)request.getAttribute("FIELD_ORDER");
		if (currentFieldOrder.equals("")){
			currentFieldOrder = "DESCR";
			request.delAttribute("FIELD_ORDER");
			request.setAttribute("FIELD_ORDER", currentFieldOrder);
		}
		
		String currentTypOrder = (request.getAttribute("TYPE_ORDER") == null || ((String)request.getAttribute("TYPE_ORDER")).equals(""))?"":(String)request.getAttribute("TYPE_ORDER");		
		if (currentTypOrder.equals("")){
			currentTypOrder = " ASC";
			request.delAttribute("TYPE_ORDER");
			request.setAttribute("TYPE_ORDER",currentTypOrder);			
		}

		InitializerIFace serviceInitializer = (InitializerIFace) service;
		RequestContextIFace serviceRequestContext = (RequestContextIFace) service;
		int pagedRows = 10;
		SourceBean rowsSourceBean = null;
		pagedRows = Integer.parseInt((String) serviceInitializer.getConfig().getAttribute("ROWS"));
		paginator.setPageSize(pagedRows);
		SourceBean statement = (SourceBean) serviceInitializer.getConfig().getAttribute("QUERIES.SELECT_QUERY");		
		
		try {			
			
			aSession = HibernateSessionManager.getCurrentSession();
			tx = aSession.beginTransaction();
			//Connection jdbcConnection = aSession.connection();
			Connection jdbcConnection = HibernateSessionManager.getConnection(aSession);
			DataConnection dataConnection = getDataConnection(jdbcConnection);
			
			rowsSourceBean =
				(SourceBean) DelegatedQueryExecutor.executeQuery(
					serviceRequestContext.getRequestContainer(),
					serviceRequestContext.getResponseContainer(),
					dataConnection,
					statement,
					"SELECT");
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
		}
		List rowsVector = null;
		if (rowsSourceBean != null)
			rowsVector = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
		if (rowsSourceBean == null) {
			EMFErrorHandler engErrorHandler = serviceRequestContext.getErrorHandler();
			engErrorHandler.addError(new EMFUserError(EMFErrorSeverity.INFORMATION, 10001));
		}
		else
			for (int i = 0; i < rowsVector.size(); i++)
				paginator.addRow(rowsVector.get(i));		
		
		ListIFace list = new GenericList();
		list.setPaginator(paginator);
		
		// filter the list 
		Object valuefilterObj = (Object)request.getAttribute(SpagoBIConstants.VALUE_FILTER);
		String valuefilter = null;
		if(valuefilterObj!=null){
			valuefilter = valuefilterObj.toString();
		}
		//String valuefilter = (String)request.getAttribute(SpagoBIConstants.VALUE_FILTER);
		if (valuefilter != null) {
			String columnfilter = (String) request
					.getAttribute(SpagoBIConstants.COLUMN_FILTER);
			String typeFilter = (String) request
					.getAttribute(SpagoBIConstants.TYPE_FILTER);
			String typeValueFilter = (String) request
					.getAttribute(SpagoBIConstants.TYPE_VALUE_FILTER);
			list = filterList(list, valuefilter, typeValueFilter, columnfilter, typeFilter, serviceRequestContext.getErrorHandler());
		}
		
		return list;
	}
	
   /**
    * Gets the data connection.
    * 
    * @param con the con
    * 
    * @return the data connection
    * 
    * @throws EMFInternalError the EMF internal error
    */
   public static DataConnection getDataConnection(Connection con) throws EMFInternalError {
       DataConnection dataCon = null;
       try {
           Class mapperClass = Class.forName("it.eng.spago.dbaccess.sql.mappers.OracleSQLMapper");
           SQLMapper sqlMapper = (SQLMapper)mapperClass.newInstance();
           dataCon = new DataConnection(con, "2.1", sqlMapper);
       } catch(Exception e) {
           SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, DelegatedHibernateConnectionListService.class.getName() , "getDataConnection",
                   "Error while getting Spago DataConnection " + e);
           throw new EMFInternalError(EMFErrorSeverity.ERROR, "cannot build DataConnection object");
       }
       return dataCon;
   }
   
	/**
	 * Traces the exception information of a throwable input object.
	 * 
	 * @param t The input throwable object
	 */
	public static void logException(Throwable t){
		SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, 
	            t.getClass().getName(), 
	            "", 
	            t.getMessage());
	}
	
}
