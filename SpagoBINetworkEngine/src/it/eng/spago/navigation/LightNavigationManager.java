/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spago.navigation;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import it.eng.spago.base.Constants;
import it.eng.spago.base.SourceBean;
import it.eng.spago.tracing.TracerSingleton;

// TODO: Auto-generated Javadoc
/**
 * The Class LightNavigationManager.
 */
public class LightNavigationManager {

    /** The Constant LIGHT_NAVIGATOR_RESET. */
    public static final String LIGHT_NAVIGATOR_RESET = "LIGHT_NAVIGATOR_RESET";
    
    /** The Constant LIGHT_NAVIGATOR_DISABLED. */
    public static final String LIGHT_NAVIGATOR_DISABLED = "LIGHT_NAVIGATOR_DISABLED";
    
    /** The Constant LIGHT_NAVIGATOR_BACK_TO. */
    public static final String LIGHT_NAVIGATOR_BACK_TO = "LIGHT_NAVIGATOR_BACK_TO";
    
    /** The Constant LIGHT_NAVIGATOR_REPLACE_LAST. */
    public static final String LIGHT_NAVIGATOR_REPLACE_LAST = "LIGHT_NAVIGATOR_REPLACE_LAST";
    
    /** The Constant LIGHT_NAVIGATOR_BACK_TO_MARK. */
    public static final String LIGHT_NAVIGATOR_BACK_TO_MARK = "LIGHT_NAVIGATOR_BACK_TO_MARK";
    
    /** The Constant LIGHT_NAVIGATOR_MARK. */
    public static final String LIGHT_NAVIGATOR_MARK = "LIGHT_NAVIGATOR_MARK";

    /**
     * Control light navigation.
     * 
     * @param request the request
     * @param serviceRequest the service request
     * 
     * @return the source bean
     * 
     * @throws Exception the exception
     */
    public static SourceBean controlLightNavigation (PortletRequest request, SourceBean serviceRequest) throws Exception {
    	Object o = (Object)request;    	
    	return controlLightNavigation(o, serviceRequest);
    }
	
    /**
     * This method is responsible for the requests stack (the <code>LightNavigator</code>) modification.
     * It controls if there is one of the following attributes in the service request:
     * <code>LIGHT_NAVIGATOR_RESET</code> (requests stack is resetted);
     * <code>LIGHT_NAVIGATOR_DISABLED</code> (the navigator is disabled: the stack is not modified and the original request is returned);
     * <code>LIGHT_NAVIGATOR_BACK_TO</code> (the request in the stack at position represented by this attribute is returned);
     * <code>LIGHT_NAVIGATOR_BACK_TO_MARK</code> (the request in the stack marked by the string represented by this attribute is returned);
     * <code>LIGHT_NAVIGATOR_MARK</code> (the request at input is marked with the string represented by this attribute and put in the stack).
     * <code>LIGHT_NAVIGATOR_REPLACE_LAST</code> (the more recent request is replaced by the request at input).
     * If any errors occur, the original request is returned.
     * 
     * @param serviceRequest The original service request <code>SourceBean</code>
     * @param request the request
     * 
     * @return the service request <code>SourceBean</code>
     * 
     * @throws Exception the exception
     */
	public static SourceBean controlLightNavigation (Object request, SourceBean serviceRequest) throws Exception {
		TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "Method controlNavigation in LightNavigationManager class invoked with service request:\n" + serviceRequest);
		
		if (request == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "LightNavigationManager: " +
					"controlLightNavigation: request object at input is null.");
			throw new Exception ("request object is null.");
		}
		if (serviceRequest == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "LightNavigationManager: " +
					"controlLightNavigation: the service request SourceBean at input is null.");
			throw new Exception ("Service request SourceBean is null.");
		}
		
		// Makes a clone of the serviceRequest, to be returned in case of errors
		SourceBean savedServiceRequest = (SourceBean) serviceRequest.cloneObject();
		
		// Retrieves the LightNavigator object from session; if it does not exist it creates a new one
		LightNavigator lightNavigator = retrieveLightNavigatorFromSession(request);
		
		try {
			
	        // If LightNavigator is disabled return the serviceRequest unmodified
			String lightNavigatorDisabled = (String) serviceRequest.getAttribute(LIGHT_NAVIGATOR_DISABLED);
			if ("true".equalsIgnoreCase(lightNavigatorDisabled)) {
				TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "LightNavigator disabled; " +
						"return from the method controlNavigation without any modifications to the original request.");
				TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "The requests stack is not modified:\n" + lightNavigator.toString());
				return serviceRequest;
			}

			String lightNavigatorBackTo = (String) serviceRequest.getAttribute(LIGHT_NAVIGATOR_BACK_TO);
			if (lightNavigatorBackTo != null) {
				int position = Integer.parseInt(lightNavigatorBackTo);
				MarkedRequest markedRequest = lightNavigator.goBackToPosition(position);
				TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "LightNavigationManager: controlLightNavigation: " +
						"returning request at position " + position + " of the stack.");
				TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "The requests stack now is:\n" + lightNavigator.toString());
				return markedRequest.getRequest();
			}
			
			String lightNavigatorReplaceLast = (String) serviceRequest.getAttribute(LIGHT_NAVIGATOR_REPLACE_LAST);
			if (lightNavigatorReplaceLast != null) {
				String mark = (String) serviceRequest.getAttribute(LIGHT_NAVIGATOR_MARK);
				MarkedRequest markedRequest = new MarkedRequest(serviceRequest, mark);
				lightNavigator.replaceLast(markedRequest);
				TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "LightNavigationManager: controlLightNavigation: " +
						"substituted the more recent request (at position 0) of the stack with the input one.");
				TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "The requests stack now is:\n" + lightNavigator.toString());
				return markedRequest.getRequest();
			}
			
			String lightNavigatorBackToMark = (String) serviceRequest.getAttribute(LIGHT_NAVIGATOR_BACK_TO_MARK);
			if (lightNavigatorBackToMark != null) {
				MarkedRequest markedRequest = lightNavigator.goBackToMark(lightNavigatorBackToMark);
				TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "LightNavigationManager: controlLightNavigation: " +
						"returning request with mark " + lightNavigatorBackToMark + " of the stack.");
				TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "The requests stack now is:\n" + lightNavigator.toString());
				return markedRequest.getRequest();
			}
			
			String lightNavigatorReset = (String) serviceRequest.getAttribute(LIGHT_NAVIGATOR_RESET);
			if (lightNavigatorReset != null) {
				lightNavigator.reset();
				TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "LightNavigationManager: controlLightNavigation: " +
						"stack reset executed.");
				TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "The requests stack now is empty.");
				return serviceRequest;
			}
			
			String mark = (String) serviceRequest.getAttribute(LIGHT_NAVIGATOR_MARK);
			MarkedRequest markedRequest = new MarkedRequest(serviceRequest, mark);
			lightNavigator.add(markedRequest);
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "LightNavigationManager: controlLightNavigation: " +
					"putting the request in the stack " + 
					(mark == null ? "without mark" : "with mark '" + mark + "'") + ". Returning unmodified request.");
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "The requests stack now is:\n" + lightNavigator.toString());
			return serviceRequest;
			
		} catch (Exception e) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.MAJOR, "LightNavigationManager: " +
					"controlLightNavigation: an exception occurred. " +
					"Returning the original request.", e);
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "The requests stack now is:\n" + 
					lightNavigator.toString());
			return savedServiceRequest;
		}

	}
	
	/**
	 * Retrieves the <code>LightNavigator</code> object from session; if it does not exist it creates a new one.
	 * 
	 * @param request the request
	 * 
	 * @return the light navigator
	 * 
	 * @throws Exception the exception
	 */
	private static LightNavigator retrieveLightNavigatorFromSession (Object request) throws Exception {
		LightNavigator lightNavigator = null;
		if(request instanceof PortletRequest) {
			PortletRequest portletRequest = (PortletRequest)request;
			PortletSession session = portletRequest.getPortletSession();
			if (session == null) {
				TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "LightNavigationManager: " +
					"controlLightNavigation: PortletSession object is null.");
				throw new Exception ("PortletSession object is null.");
			}
			Object lightNavigatorObj = session.getAttribute("LIGHT_NAVIGATOR");
			if (lightNavigatorObj != null) lightNavigator = (LightNavigator) lightNavigatorObj;
			else {
				lightNavigator = new LightNavigator();
				session.setAttribute("LIGHT_NAVIGATOR", lightNavigator);
			}
		} else if(request instanceof HttpServletRequest) {
			HttpServletRequest servRequest = (HttpServletRequest)request;
			HttpSession session = servRequest.getSession();
			if (session == null) {
				TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "LightNavigationManager: " +
					"controlLightNavigation: HttpSession object is null.");
				throw new Exception ("HttpSession object is null.");
			}
			Object lightNavigatorObj = session.getAttribute("LIGHT_NAVIGATOR");
			if (lightNavigatorObj != null) lightNavigator = (LightNavigator) lightNavigatorObj;
			else {
				lightNavigator = new LightNavigator();
				session.setAttribute("LIGHT_NAVIGATOR", lightNavigator);
			}
		} else {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "LightNavigationManager: " +
								"controlLightNavigation: request object is neither a PortletRequest " +
								"nor an HttpServletRequest");
			throw new Exception ("request object is neither a PortletRequest nor an HttpServletRequest");
		}
		return lightNavigator;
	}
	
}
