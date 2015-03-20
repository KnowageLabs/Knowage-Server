/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @class
 * Container of the ContextManager
 * You should use this Object in the abstractengineservices.. For example:
 * 
 * 
 * public class XXXEngineService {
 *	public ExecutionSession es;
 *	
 *	@Context
 *	protected HttpServletRequest servletRequest;
 *    
 *    public ExecutionSession getExecutionSession(){
 *    	if(es==null){
 *    		es = new ExecutionSession(servletRequest, servletRequest.getSession());
 *    	}
 *   	return es;
 *   }
 *
 *	public WhatIfEngineInstance getXXXEngineInstance() {
 *		ExecutionSession es = getExecutionSession();
 *   	return (EngineInstance)es.getAttributeFromSession( EngineConstants.ENGINE_INSTANCE );
 *	}
 * }
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

package it.eng.spagobi.utilities.engines.rest;

import it.eng.spagobi.container.ContextManager;
import it.eng.spagobi.container.IBeanContainer;
import it.eng.spagobi.container.strategy.ExecutionContextRetrieverStrategy;
import it.eng.spagobi.container.strategy.IContextRetrieverStrategy;
import it.eng.spagobi.services.proxy.ContentServiceProxy;
import it.eng.spagobi.utilities.engines.AbstractEngineAction;
import it.eng.spagobi.utilities.engines.EngineAnalysisMetadata;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.IEngineAnalysisState;
import it.eng.spagobi.utilities.engines.IEngineInstance;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

public class ExecutionSession {
	
	/**
	 * Manager of the context: retrieves the attributes in the portion of the session assigned to the current execution instance
	 */
	private ContextManager contextManager;
	/**
	 * Container of the HTTPSession
	 */
	private HttpRequestContainer requestContainer;
	/**
	 * Container of the HTTPRequest
	 */
	private HttpSessionContainer sessionContainer;
	
	
	public ExecutionSession(HttpServletRequest request, HttpSession session) {
		requestContainer = new HttpRequestContainer(request);
		sessionContainer = new HttpSessionContainer(session);
	}
	
	public ContextManager getConetxtManager() {
		if(contextManager == null) {
			IContextRetrieverStrategy contextRetriveStrategy;
			contextRetriveStrategy = new ExecutionContextRetrieverStrategy( requestContainer );
			contextManager = new ContextManager(sessionContainer, contextRetriveStrategy);
		}
		
		List list = contextManager.getKeys();
		
		return contextManager;
	}
	
	public IBeanContainer getSpagoBISessionContainer() {
		return getSpagoBIHttpSessionContainer();
	}
	
	
	public IBeanContainer getSpagoBIHttpSessionContainer() {
		return getConetxtManager();
	}
	
	public IEngineInstance getEngineInstance() {
    	return (IEngineInstance)getAttributeFromSession( EngineConstants.ENGINE_INSTANCE );
    }
	
	public Map getEnv() {
		return getEngineInstance().getEnv();
	}
	
	
	public String saveAnalysisState() throws SpagoBIEngineException {
		IEngineInstance engineInstance = null;
		String documentId = null;
		EngineAnalysisMetadata analysisMetadata = null;
		IEngineAnalysisState analysisState = null;
		ContentServiceProxy  contentServiceProxy = null;
		String serviceResponse= null;
		
		
		
		engineInstance = getEngineInstance();
		analysisMetadata = engineInstance.getAnalysisMetadata();
		analysisState = engineInstance.getAnalysisState();

		if(getEnv() == null) {
			return "KO - Missing environment";
		}
		
		contentServiceProxy = (ContentServiceProxy)getEnv().get( EngineConstants.ENV_CONTENT_SERVICE_PROXY );
		if(contentServiceProxy == null) {
			return "KO - Missing content service proxy";
		}
		
		documentId = (String)getEnv().get( EngineConstants.ENV_DOCUMENT_ID );
		if(documentId == null) {
			return "KO - Missing document id";
		}
		
	    String isPublic = "false";
	    if (AbstractEngineAction.PUBLIC_SCOPE.equalsIgnoreCase(analysisMetadata.getScope())) 
	    	isPublic = "true";
		
		serviceResponse = contentServiceProxy.saveSubObject(documentId, 
				analysisMetadata.getName(),
				analysisMetadata.getDescription(), 
				isPublic, 
				new String(analysisState.store()) );
		
		return serviceResponse;
	}

	public Locale getLocale() {
		return  (Locale)getEnv().get(EngineConstants.ENV_LOCALE);
	}
	
	/**
	 * Sets the qbe engine locale.
	 * 
	 * @param locale the new qbe engine locale
	 */
	public void setLocale(Locale locale) {
		getEnv().put(EngineConstants.ENV_LOCALE, locale);
	}
	
	
	
	
	protected HttpRequestContainer getSpagoBIRequestContainer() {
		return requestContainer;
		
	}

	protected void setSpagoBIRequestContainer(HttpRequestContainer requestContainer) {
		this.requestContainer = requestContainer;
	}
	
	public boolean requestContainsAttribute(String attrName) {		
		return !getSpagoBIRequestContainer().isNull( attrName );
	}
	
	public boolean requestContainsAttribute(String attrName, String attrValue) {
		return ( requestContainsAttribute(attrName) && getAttribute(attrName).toString().equalsIgnoreCase(attrValue) );
	}
		
	public Object getAttribute(String attrName) {
		return getSpagoBIRequestContainer().get(attrName);
	}
	
	public String getAttributeAsString(String attrName) {
		return getSpagoBIRequestContainer().getString( attrName );
	}
	
	public Integer getAttributeAsInteger(String attrName) {
		return getSpagoBIRequestContainer().getInteger( attrName );
	}
	
	public boolean getAttributeAsBoolean(String attrName) {
		return getAttributeAsBoolean(attrName, false);
	}

	public boolean getAttributeAsBoolean(String attrName, boolean defaultValue) {
		if( getAttribute(attrName) == null ) return defaultValue;
		return getSpagoBIRequestContainer().getBoolean( attrName ).booleanValue();
	}
	
	public List getAttributeAsList(String attrName) {
		return getSpagoBIRequestContainer().toList( attrName );
	}
	
	public List getAttributeAsCsvStringList(String attrName, String separator) {
		return getSpagoBIRequestContainer().toCsvList( attrName );
	}
	
	public JSONObject getAttributeAsJSONObject(String attrName) {
		return getSpagoBIRequestContainer().toJSONObject( attrName );
	}	
	
	public JSONArray getAttributeAsJSONArray(String attrName) {
		return getSpagoBIRequestContainer().toJSONArray( attrName );
	}	
	
	public Map<String,Object> getAttributesAsMap() {
		
		List attributeNames;
		String attributeName;
		Object attributeVaue;
		HashMap<String , Object> attributesMap;
		Iterator it;
		
		attributesMap = new HashMap <String , Object> ();
		attributeNames = getSpagoBIRequestContainer().getKeys();
		
		it = attributeNames.iterator();
		while( it.hasNext() ) {
			attributeName = (String)it.next();
			attributeVaue = getAttribute(attributeName);
			attributesMap.put(attributeName, attributeVaue);
		}
		
		return attributesMap;
	}
	
public LinkedHashMap<String,Object> getAttributesAsLinkedMap() {
		
		List attributeNames;
		String attributeName;
		Object attributeVaue;
		LinkedHashMap<String , Object> attributesMap;
		Iterator it;
		
		attributesMap = new LinkedHashMap <String , Object> ();
		attributeNames = getSpagoBIRequestContainer().getKeys();
		
		it = attributeNames.iterator();
		while( it.hasNext() ) {
			attributeName = (String)it.next();
			attributeVaue = getAttribute(attributeName);
			attributesMap.put(attributeName, attributeVaue);
		}
		
		return attributesMap;
	}
	
	
	
	// =================================================================================================
	// SESSION utility methods
	// =================================================================================================
	
	
	public boolean sessionContainsAttribute(String attrName) {
		return !getSpagoBISessionContainer().isNull(attrName);
	}
		
	public Object getAttributeFromSession(String attrName) {
		return getSpagoBISessionContainer().get( attrName );
	}

	public String getAttributeFromSessionAsString(String attrName) {
		return getSpagoBISessionContainer().getString( attrName );
	}

	public boolean getAttributeFromSessionAsBoolean(String attrName) {
		return getAttributeFromSessionAsBoolean(attrName, false);
	}

	public boolean getAttributeFromSessionAsBoolean(String attrName, boolean defaultValue) {
		if( !sessionContainsAttribute(attrName) ) return defaultValue;
		return getSpagoBISessionContainer().getBoolean( attrName ).booleanValue();
	}
	
	public void delAttributeFromSession(String attrName) {
		if( sessionContainsAttribute(attrName) ) {
			getSpagoBISessionContainer().remove(attrName);
		}
	}
		
	
	public void setAttributeInSession(String attrName, Object attrValue) {
		delAttributeFromSession(attrName);
		getSpagoBISessionContainer().set(attrName, attrValue);
	}	
	
	
	// =================================================================================================
	// HTTP-SESSION utility methods
	// =================================================================================================
	
	

	
	public boolean httpSessionContainsAttribute(String attrName) {
		return !getSpagoBIHttpSessionContainer().isNull(attrName);
	}
	
	
	public Object getAttributeFromHttpSession(String attrName) {
		return getSpagoBIHttpSessionContainer().get(attrName);
	}
	
	
	public String getAttributeFromHttpSessionAsString(String attrName) {
		return getSpagoBIHttpSessionContainer().getString(attrName);
	}
	
	
	public boolean getAttributeFromHttpSessionAsBoolean(String attrName) {
		return getAttributeFromHttpSessionAsBoolean(attrName, false);
	}

	
	public boolean getAttributeFromHttpSessionAsBoolean(String attrName, boolean defaultValue) {
		if( !httpSessionContainsAttribute(attrName) ) return defaultValue;
		return getSpagoBIHttpSessionContainer().getBoolean( attrName ).booleanValue();
	}
	
	public void delAttributeFromHttpSession(String attrName) {
		if( httpSessionContainsAttribute(attrName) ) {
			getSpagoBIHttpSessionContainer().remove(attrName);
		}
	}
		
	public void setAttributeInHttpSession(String attrName, Object attrValue) {
		delAttributeFromHttpSession(attrName);
		getSpagoBIHttpSessionContainer().set(attrName, attrValue);
	}	
}
