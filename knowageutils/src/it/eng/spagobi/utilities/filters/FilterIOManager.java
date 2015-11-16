/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.filters;

import it.eng.spagobi.container.ContextManager;
import it.eng.spagobi.container.SpagoBIHttpSessionContainer;
import it.eng.spagobi.container.strategy.ExecutionContextRetrieverStrategy;
import it.eng.spagobi.container.strategy.IContextRetrieverStrategy;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class FilterIOManager {
	ServletRequest request;
	ServletResponse response;
	ContextManager contextManager;
	
	private static final String EXECUTION_ID = "SBI_EXECUTION_ID";
	
	//----------------------------------------------------------------------------------------------------
    // Constructor
    //----------------------------------------------------------------------------------------------------
	
	public FilterIOManager(ServletRequest request, ServletResponse response) {
		setRequest( request );
		setResponse( response );
	}
	
	//----------------------------------------------------------------------------------------------------
    // Accessor methods
    //----------------------------------------------------------------------------------------------------
    
    
	public ServletRequest getRequest() {
		return request;
	}

	public void setRequest(ServletRequest request) {
		this.request = request;
	}
	
	public ServletResponse getResponse() {
		return response;
	}

	public void setResponse(ServletResponse response) {
		this.response = response;
	}
	
	/**
	 * @deprecated 
	 */
	public HttpSession getSession() {
    	return ((HttpServletRequest)getRequest()).getSession();
    }
	
	/**
	 * @deprecated 
	 */
	public Object getFromSession(String key) {
		return getSession().getAttribute(key);
	}
	
	/**
	 * @deprecated 
	 */
	public void setInSession(String key, Object value) {
		getSession().setAttribute(key, value);
	}
	
	public void initConetxtManager() {
		SpagoBIHttpSessionContainer sessionContainer;
		IContextRetrieverStrategy contextRetriveStrategy;
		String executionId;		
		
		sessionContainer = new SpagoBIHttpSessionContainer( getSession() );	
		
		executionId = (String)getRequest().getParameter(EXECUTION_ID);
		contextRetriveStrategy = new ExecutionContextRetrieverStrategy( executionId );
		
		setContextManager( new ContextManager(sessionContainer, contextRetriveStrategy) );
	}

	public ContextManager getContextManager() {
		return contextManager;
	}

	public void setContextManager(ContextManager contextManager) {
		this.contextManager = contextManager;
	}
}
