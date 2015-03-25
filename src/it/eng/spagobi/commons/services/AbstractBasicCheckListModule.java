/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.services;

import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.dbaccess.DataConnectionManager;
import it.eng.spago.dbaccess.SQLStatements;
import it.eng.spago.dbaccess.sql.DataConnection;
import it.eng.spago.dbaccess.sql.SQLCommand;
import it.eng.spago.dbaccess.sql.result.DataResult;
import it.eng.spago.dbaccess.sql.result.ScrollableDataResult;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Gioia
 *
 */
public class AbstractBasicCheckListModule extends AbstractListLookupModule {

	protected SourceBean config;
	protected Map checkedObjectsMap = null;
	protected List allElements = new ArrayList();
	protected int pageNumber = 0;
	protected boolean returnValues = true;
	
	public static final String MODULE_PAGE = "CheckListPage";
	
	public static final String OBJECT = "OBJECT";
	public static final String CHECKED_OBJECTS = "CHECKEDOBJECTS";
	public static final String CHECKED_OBJECTS_DESC = "CHECKEDOBJECTSDESC";
	
	
	
	
	/**
	 * Clear session.
	 * 
	 * @param session the session
	 * @param moduleName the module name
	 */
	public static void clearSession(SessionContainer session, String moduleName){
		
		// clear all input parameters
		ConfigSingleton spagoBiConfig = ConfigSingleton.getInstance();
		// TODO patch this
		SourceBean moduleConfig = (SourceBean)spagoBiConfig.getFilteredSourceBeanAttribute("MODULES.MODULE", "NAME", moduleName);
		List parametersList = null;
		SourceBean parameter = null;
		String pvalue, str;
		
		parametersList = moduleConfig.getAttributeAsList("QUERIES.SELECT_QUERY.PARAMETER");		
		for(int i = 0; i < parametersList.size(); i++) {			
			parameter = (SourceBean)parametersList.get(i);
			pvalue = (String)parameter.getAttribute("value");
			if(pvalue == null) {
				str = (String)parameter.getAttribute("name");
				session.delAttribute(str);
			}			
		}
		
		parametersList = moduleConfig.getAttributeAsList("QUERIES.CHECKED_QUERY.PARAMETER");		
		for(int i = 0; i < parametersList.size(); i++) {			
			parameter = (SourceBean)parametersList.get(i);
			pvalue = (String)parameter.getAttribute("value");
			if(pvalue == null) {
				str = (String)parameter.getAttribute("name");
				session.delAttribute(str);
			}			
		}
		
		// clear all output parameters
		session.delAttribute("RETURN_FROM_MODULE");
		session.delAttribute("RETURN_STATUS");
	}
	
	/**
	 * Instantiates a new abstract basic check list module.
	 */
	public AbstractBasicCheckListModule(){
		super();
	}
			
	/**
	 * Save.
	 * 
	 * @throws Exception the exception
	 */
	public void save() throws Exception {
		SourceBean chekhedObjects = getCheckedObjects();
	}
	
	/**
	 * Exit from module.
	 * 
	 * @param response the response
	 * @param abort the abort
	 * 
	 * @throws Exception the exception
	 */
	public void exitFromModule(SourceBean response, boolean abort) throws Exception{
		SessionContainer session = this.getRequestContainer().getSessionContainer();
		
		if(!abort && returnValues){
			SourceBean chekhedObjects = getCheckedObjects();
			session.setAttribute("RETURN_VALUES", chekhedObjects);
		}
		
		getRequestContainer().getSessionContainer().delAttribute(CHECKED_OBJECTS);
		
		String moduleName = (String)_request.getAttribute("AF_MODULE_NAME");
				
		session.setAttribute("RETURN_FROM_MODULE", moduleName);
		session.setAttribute("RETURN_STATUS", ((abort)?"ABORT":"OK") );
		response.setAttribute("PUBLISHER_NAME", "ReturnBackPublisher");
	}
	
	/**
	 * Gets the object key.
	 * 
	 * @param object the object
	 * 
	 * @return the object key
	 */
	public String getObjectKey(SourceBean object) {
		String objectIdName = (String)((SourceBean) config.getAttribute("KEYS.OBJECT")).getAttribute("key");				
		String objectIdValue = object.getAttribute(objectIdName).toString();
		objectIdValue = GeneralUtilities.encode(objectIdValue);		
		return objectIdValue;
	}
	
	/**
	 * Gets the object.
	 * 
	 * @param key the key
	 * 
	 * @return the object
	 * 
	 * @throws Exception the exception
	 */
	public SourceBean getObject(String key) throws Exception {
		String objectIdName = (String)((SourceBean) config.getAttribute("KEYS.OBJECT")).getAttribute("key");
		SourceBean object = new SourceBean(OBJECT);
		object.setAttribute(objectIdName, key);
		return object;
	}
	
	
	
	/**
	 * Gets the checked objects.
	 * 
	 * @return the checked objects
	 * 
	 * @throws Exception the exception
	 */
	public SourceBean getCheckedObjects() throws Exception{
		SourceBean chekhedObjects = new SourceBean(CHECKED_OBJECTS);
		Iterator it = checkedObjectsMap.keySet().iterator();
		while(it.hasNext()){
			String key = (String)it.next();
			SourceBean object = getObject(key);
			chekhedObjects.setAttribute(object);
		}	
		return chekhedObjects;
	}
	
	
	
		
	/**
	 * Gets the query parameters.
	 * 
	 * @param queryName the query name
	 * @param request the request
	 * 
	 * @return the query parameters
	 */
	public String[] getQueryParameters(String queryName, SourceBean request) {
		String[] parameters = null;
		
		List parametersList = config.getAttributeAsList("QUERIES." + queryName + ".PARAMETER");
		parameters = new String[parametersList.size()];
		SourceBean parameter = null;
		String pvalue, str; int index;
		for(int i = 0; i < parametersList.size(); i++) {			
			parameter = (SourceBean)parametersList.get(i);
			str = (String)parameter.getAttribute("number");
			index = Integer.parseInt(str);
			pvalue = (String)parameter.getAttribute("value");
			if(pvalue == null) {
				str = (String)parameter.getAttribute("name");
				pvalue = (String)getAttribute(str, request);
			}
			parameters[index] = pvalue;		
		}
		
		return parameters;
	}	
	
	/**
	 * Gets the query statement.
	 * 
	 * @param queryName the query name
	 * @param parameters the parameters
	 * 
	 * @return the query statement
	 */
	public String getQueryStatement(String queryName, String[] parameters) {
		String statementStr = null;
						
		SourceBean statement = (SourceBean) config.getAttribute("QUERIES." + queryName);		
		statementStr = SQLStatements.getStatement((String) statement.getAttribute("STATEMENT"));
		for(int i = 0; i < parameters.length; i++) {			
			statementStr = statementStr.replaceFirst("\\?", parameters[i]);
		}
		return statementStr;
	}

	
	protected List getCheckedObjectKeys(SourceBean request){
		List results = new ArrayList();
		
		List attrs = request.getAttributeAsList("checkbox");

		for(int i = 0; i < attrs.size(); i++){
			results.add((String)attrs.get(i));
		}	
		
		return results;
	}
	
	/**
	 * Creates the checked object map.
	 * 
	 * @param request the request
	 * 
	 * @throws Exception the exception
	 */
	public void createCheckedObjectMap(SourceBean request) throws Exception {
		checkedObjectsMap = new HashMap();

		// get CHECKED_QUERY query parameters

		String[] parameters = getQueryParameters("CHECKED_QUERY", request);

		// get CHECKED_QUERY statment
		String statement = getQueryStatement("CHECKED_QUERY", parameters);

		// exec CHECKED_QUERY
		ScrollableDataResult scrollableDataResult = null;
		SQLCommand sqlCommand = null;
		DataConnection dataConnection = null;
		DataResult dataResult = null;
		String pool = null;
		try {
			pool = (String) config.getAttribute("POOL");
			dataConnection = DataConnectionManager.getInstance().getConnection(
					pool);
			sqlCommand = dataConnection.createSelectCommand(statement);
			dataResult = sqlCommand.execute();
			scrollableDataResult = (ScrollableDataResult) dataResult
					.getDataObject();
			SourceBean chekedObjectsBean = scrollableDataResult.getSourceBean();
			List checkedObjectsList = chekedObjectsBean
					.getAttributeAsList("ROW");

			String tmpElements = (request.getAttribute("checkedElements")==null)?"":(String)request.getAttribute("checkedElements");
		    String[] arrElements = tmpElements.split(",");
			 for (int i = 0; i< arrElements.length; i++)
				 allElements.add(arrElements[i]);
			 
			 checkedObjectsMap = copyLstObjects(allElements);
			 
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass()
					.getName(), "createCheckedObjectMap", e.getMessage(), e);
		} finally {
			if (dataConnection != null)
				dataConnection.close();
		}
	}
	
	/**
	 * Update checked object map.
	 * 
	 * @param request the request
	 * 
	 * @throws Exception the exception
	 */
	public void updateCheckedObjectMap(SourceBean request) throws Exception {
		if (checkedObjectsMap == null)
			checkedObjectsMap = new HashMap();
		
		String tmpElements = (request.getAttribute("checkedElements")==null)?"":(String)request.getAttribute("checkedElements");
		if (!tmpElements.equals("")){
		    String[] arrElements = tmpElements.split(",");
			for (int i = 0; i< arrElements.length; i++)
				 allElements.add(arrElements[i]);
			 
			 checkedObjectsMap = copyLstObjects(allElements);
		}
	}
	
	/**
	 * Preprocess.
	 * 
	 * @param request the request
	 * 
	 * @throws Exception the exception
	 */
	public void preprocess(SourceBean request) throws Exception {		
		
		if(getRequestContainer().getSessionContainer().getAttribute(CHECKED_OBJECTS) != null ) {
			updateCheckedObjectMap(request);
			
			getRequestContainer().getSessionContainer().delAttribute(CHECKED_OBJECTS);
			String pageNumberStr = (String)request.getAttribute("PAGE_NUMBER");
			if(pageNumberStr != null)
				pageNumber = Integer.parseInt(pageNumberStr);
		}
		else {
			createCheckedObjectMap(request);
			if (checkedObjectsMap != null){
				String tmpAllElements = "";
				Collection colAllElements = checkedObjectsMap.values();	
				Object[] arrElements =(Object[])colAllElements.toArray();
				for (int i = 0; i< arrElements.length; i++)
					 allElements.add(arrElements[i]);
			}
			pageNumber = 1;
		}		
		
	}
	
	
	/**
	 * Checks if is checked.
	 * 
	 * @param object the object
	 * 
	 * @return true, if is checked
	 */
	public boolean isChecked(SourceBean object) {
		return (checkedObjectsMap.get(getObjectKey(object)) != null);
	}
		
	/**
	 * Postprocess.
	 * 
	 * @param response the response
	 * 
	 * @throws Exception the exception
	 */
	public void postprocess(SourceBean response) throws Exception {	
		List objectsList = response.getAttributeAsList("PAGED_LIST.ROWS.ROW");
		SourceBean pagedList = (SourceBean)response.getAttribute("PAGED_LIST");
		response.delAttribute("PAGED_LIST");
		pagedList.delAttribute("ROWS");
		
		
		List pendingDelete = new ArrayList();
		
		SourceBean rows = new SourceBean("ROWS");
		for(int i = 0; i < objectsList.size(); i++) {
			SourceBean object = (SourceBean)objectsList.get(i);
			
			if(isChecked(object)) {					
				object.setAttribute("CHECKED", "true");	
				String key = getObjectKey(object);
				pendingDelete.add(key);
				
			}
			else {
				object.setAttribute("CHECKED", "false");				
			}
			object.setAttribute("ROW_ID", getObjectKey(object));			
			rows.setAttribute(object);
		}		
		SourceBean chekhedObjects = getCheckedObjects();
		
		pagedList.setAttribute(rows);
		response.setAttribute(pagedList);
		if (allElements != null && allElements.size()>0 )
			response.setAttribute("checkedElements",getStringFromList(allElements));
		else{
			response.setAttribute("checkedElements",getStringFromList(pendingDelete));
			allElements = pendingDelete;
		}
		response.setAttribute(chekhedObjects);
		
		
		
	}
	
	public SourceBean _request = null;
	public SourceBean _response = null;
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {		
		config = getConfig();
		if(config == null) config = (SourceBean) response.getAttribute("CONFIG");
		
		
		_request = request;
		_response = response;
		
		String message = (String)request.getAttribute("MESSAGE");

		if(message == null || message.equalsIgnoreCase("INIT_CHECKLIST") ) {
			preprocess(request);	
			super.service(request, response); 
			postprocess(response); 
			response.setAttribute("PUBLISHER_NAME", "CheckLinksDefaultPublischer");	
		}
		else if(message.equalsIgnoreCase("HANDLE_CHECKLIST") ||  message.equalsIgnoreCase("LIST_PAGE")) {
									
			// events raised by navigation buttons defined in CheckListTag class (method makeNavigationButton)
			if(request.getAttribute("LIST_PAGE") != null && !request.getAttribute("LIST_PAGE").equals("") ){
				navigationHandler(request, response, Integer.valueOf(((String)request.getAttribute("LIST_PAGE"))));
				return;
			}
			
			//	events raised by action buttons defined in module.xml file (module name="ListLookupReportsModule")
			if(request.getAttribute("saveback") != null){
				preprocess(request);
				save();
				exitFromModule(response, false);
				return;
			}
							
			if(request.getAttribute("save") != null) {				
				preprocess(request);
				save();
				request.updAttribute("MESSAGE", "LIST_PAGE");	
				request.updAttribute("LIST_PAGE", new Integer(pageNumber).toString());	
				super.service(request, response); 
				postprocess(response); 
//				response.delAttribute("optChecked");
//				response.setAttribute("optChecked", (String)request.getAttribute("optChecked"));
				response.setAttribute("PUBLISHER_NAME", "CheckLinksDefaultPublischer");
				return;			
			}
			
			if(request.getAttribute("back") != null) {			
				exitFromModule(response, true);
				return;
			}
			if(request.getAttribute("checkFilter") != null && request.getAttribute("checkFilter").equals("checkFilter")){
			//reinit checklist according to the filter checkbox selection
				preprocess(request);	
				super.service(request, response); 
				postprocess(response);
//				response.delAttribute("optChecked");
//				response.setAttribute("optChecked", (String)request.getAttribute("optChecked"));
				response.setAttribute("PUBLISHER_NAME", "CheckLinksDefaultPublischer");
			}
		}
		else {
			// error
		}			
	}
	
	/**
	 * Navigation handler.
	 * 
	 * @param request the request
	 * @param response the response
	 * @param moveNext the move next
	 * 
	 * @throws Exception the exception
	 */
	public void navigationHandler(SourceBean request, SourceBean response, boolean moveNext) throws Exception{
		preprocess(request);
		int destPageNumber = (moveNext)? pageNumber+1: pageNumber-1;		
		request.updAttribute("MESSAGE", "LIST_PAGE");	
		request.updAttribute("LIST_PAGE", "" + destPageNumber);			
		super.service(request, response); 				
		postprocess(response); 
//		response.delAttribute("optChecked");
//		response.setAttribute("optChecked", (String)request.getAttribute("optChecked"));
		response.setAttribute("PUBLISHER_NAME", "CheckLinksDefaultPublischer");	
	}

	/**
	 * Navigation handler.
	 * 
	 * @param request the request
	 * @param response the response
	 * @param moveNext the move next
	 * 
	 * @throws Exception the exception
	 */
	public void navigationHandler(SourceBean request, SourceBean response, int destPage) throws Exception{
		preprocess(request);
		request.updAttribute("MESSAGE", "LIST_PAGE");	
		request.updAttribute("LIST_PAGE", "" + destPage);			
		super.service(request, response); 				
		postprocess(response); 
//		response.delAttribute("optChecked");
//		response.setAttribute("optChecked", (String)request.getAttribute("optChecked"));
		response.setAttribute("PUBLISHER_NAME", "CheckLinksDefaultPublischer");	
	}
	
	/**
	 * Navigation handler.
	 * 
	 * @param request the request
	 * @param response the response
	 * @param pageNumber the page number
	 * 
	 * @throws Exception the exception
	 */
	public void navigationHandler(SourceBean request, SourceBean response, Integer pageNumber) throws Exception{
		preprocess(request);
		request.updAttribute("MESSAGE", "LIST_PAGE");
		request.delAttribute("LIST_PAGE");
		request.setAttribute("LIST_PAGE", "" + pageNumber);			
		super.service(request, response); 				
		postprocess(response); 
//		response.delAttribute("optChecked");
//		response.setAttribute("optChecked", (String)request.getAttribute("optChecked"));
		response.setAttribute("PUBLISHER_NAME", "CheckLinksDefaultPublischer");	
	}
	
	protected Object getAttribute(String attrName, SourceBean request) {
		Object attrValue = null;
		attrValue = request.getAttribute(attrName);
		if(attrValue == null) {
			SessionContainer session = this.getRequestContainer().getSessionContainer();
			attrValue = session.getAttribute(attrName);
		}
		else {
		}
			
		return attrValue;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.list.basic.IFaceBasicListService#getList(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public ListIFace getList(SourceBean request, SourceBean response) throws Exception {
		return DelegatedBasicListService.getList(this, request, response);
	} 
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule#delete(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public boolean delete(SourceBean request, SourceBean response) {
		return DelegatedBasicListService.delete(this, request, response);
	} 
	
	/*
	 * copies a list object into a map object
	 */
	private Map copyLstObjects(List originMap){
		Map resultMap = new HashMap();
		for (int i=0; i< originMap.size(); i++){
			resultMap.put(originMap.get(i), originMap.get(i));
		}
		return resultMap;
	}
	
	/*
	 * returns a string whit all values of the input list
	 */
	private String getStringFromList(List lst){
		String strReturn = "";
		if (lst != null){
			for (int i=0; i<lst.size(); i++){
				strReturn = strReturn + (String)lst.get(i);
				if (i < lst.size()-1)
					strReturn = strReturn + ",";
			}
		}
		return strReturn;
	}
}
