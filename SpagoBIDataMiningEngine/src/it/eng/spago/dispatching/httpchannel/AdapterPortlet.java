/**
 * SpagoBI - The Business Intelligence Free Platform
 *
 * Copyright (C) 2004 - 2008 Engineering Ingegneria Informatica S.p.A.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 **/

/*
 * This is the same AdapterPortlet of Spago-portlet-2.2.0 with a correction by Davide Zerbetto
 * made on September 10th 2007.
 * The problem is that in Spago-portlet-2.2.0 a portlet renderer is missing and the loopback service request 
 * is not set. In previous version of Spago the loopback service request was set by Publisher class.
 */

package it.eng.spago.dispatching.httpchannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.base.Constants;
import it.eng.spago.base.PortletAccess;
import it.eng.spago.base.PortletSessionContainer;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.dispatching.action.NavigationErrorUtility;
import it.eng.spago.dispatching.action.SessionExpiredUtility;
import it.eng.spago.dispatching.coordinator.CoordinatorIFace;
import it.eng.spago.dispatching.coordinator.DispatcherManager;
import it.eng.spago.dispatching.httpchannel.upload.IUploadHandler;
import it.eng.spago.dispatching.httpchannel.upload.UploadFactory;
import it.eng.spago.dispatching.service.DefaultRequestContext;
import it.eng.spago.dispatching.service.RequestContextIFace;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.init.PortletInitializerManager;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spago.navigation.NavigationException;
import it.eng.spago.navigation.Navigator;
import it.eng.spago.presentation.PresentationRendering;
import it.eng.spago.presentation.Publisher;
import it.eng.spago.presentation.PublisherConfiguration;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spago.util.ContextScooping;
import it.eng.spago.util.PortletTracer;
import it.eng.spago.util.Serializer;

// TODO: Auto-generated Javadoc
/**
 * The Class AdapterPortlet.
 */
public class AdapterPortlet extends GenericPortlet {
    
    /** The Constant NEW_SESSION. */
    public static final String NEW_SESSION = "NEW_SESSION";
    
    /** The Constant HTTP_CONTENT_TYPE. */
    public static final String HTTP_CONTENT_TYPE = "text/html";
    
    /** The Constant WAP_CONTENT_TYPE. */
    public static final String WAP_CONTENT_TYPE = "text/vnd.wap.wml";
    
    /** The Constant HTTP_ACCEPT_HEADER. */
    public static final String HTTP_ACCEPT_HEADER = "ACCEPT";
    
    /** The Constant WAP_MIME_TYPE. */
    public static final String WAP_MIME_TYPE = "vnd.wap";
    
    /** The Constant HTTP_SESSION_ID. */
    public static final String HTTP_SESSION_ID = "HTTP_SESSION_ID";
    
    /** The Constant SERVLET_PUBLISHER_TYPE. */
    private static final String SERVLET_PUBLISHER_TYPE = "SERVLET";
    
    /** The Constant JSP_PUBLISHER_TYPE. */
    private static final String JSP_PUBLISHER_TYPE = "JSP";
    
    /** The Constant FORWARD_PUBLISHING_MODE. */
    private static final String FORWARD_PUBLISHING_MODE = "FORWARD";
	
	/** The Constant SERIALIZE_SESSION_ATTRIBUTE. */
	private static final String SERIALIZE_SESSION_ATTRIBUTE = "COMMON.SERIALIZE_SESSION";
	
	/** The Constant SERVICE_EXCEPTION. */
	private static final String SERVICE_EXCEPTION = "SERVICE_EXCEPTION";
	
	/** The Constant PORTLET_MODE. */
	private static final String PORTLET_MODE = "PORTLET_MODE";
	
	/** The Constant PORTLET_EXCEPTION. */
	private static final String PORTLET_EXCEPTION = "PORTLET_EXCEPTION";
	
	/** The Constant INITIAL_JSP. */
	private static final String INITIAL_JSP = "pageStart";
	//**************** START MODFIFICATION ZERBETTO 09-10-2006 ****************
	//public static final String REQUEST_CONTAINER_NAME = "REQUEST_CONTAINER_NAME";
	//public static final String RESPONSE_CONTAINER_NAME = "RESPONSE_CONTAINER_NAME";
	//**************** END MODFIFICATION ZERBETTO 09-10-2006 ****************
	/** The serialize session. */
	private boolean serializeSession = false;
	
	/** The Constant SERVICE_TYPE. */
	private static final String SERVICE_TYPE = "serviceType";
	
	/** The Constant SERVICE_NAME. */
	private static final String SERVICE_NAME = "serviceName";
	
	/** The Constant SERVICE_ACTION. */
	private static final String SERVICE_ACTION = "ACTION";
	
	/** The Constant SERVICE_PAGE. */
	private static final String SERVICE_PAGE = "PAGE";
	
	/** The Constant SERVICE_JSP. */
	private static final String SERVICE_JSP = "JSP";
	
	/* (non-Javadoc)
	 * @see javax.portlet.GenericPortlet#init()
	 */
	public void init() throws PortletException {
		super.init();
		
		PortletTracer.info(Constants.NOME_MODULO, "AdapterPortlet", "init", "Invocato");
		String serializeSessionStr = (String) ConfigSingleton.getInstance().getAttribute(SERIALIZE_SESSION_ATTRIBUTE);
		if ((serializeSessionStr != null) && (serializeSessionStr.equalsIgnoreCase("TRUE"))) {
			serializeSession = true;
		}
		PortletInitializerManager.init();
	}

	
	
    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    public void processAction(
        ActionRequest request,
        ActionResponse response)
        throws PortletException, IOException {
       
    	PortletTracer.info(Constants.NOME_MODULO, "AdapterPortlet", "action", "Invocato");
        // set into threadLocal variables the jsr 168 portlet object 
        PortletAccess.setPortletConfig(getPortletConfig());
        PortletAccess.setPortletRequest(request);
        PortletAccess.setPortletResponse(response);
        PortletSession portletSession = request.getPortletSession();
        portletSession.setAttribute("BrowserLocale", request.getLocale());
        
        
        processService(request, response);
    }   
        
    
    
    
    //**************** START MODFIFICATION ZERBETTO 09-10-2006 ****************
    //public Object processService(PortletRequest request, PortletResponse response) throws PortletException, IOException {
    	//HashMap map = new HashMap();
    /**
     * Process service.
     * 
     * @param request the request
     * @param response the response
     * 
     * @throws PortletException the portlet exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void processService(PortletRequest request, PortletResponse response) throws PortletException, IOException {
    //**************** END MODFIFICATION ZERBETTO 09-10-2006 ****************
    	
    	Monitor monitor = null;
    	PortletTracer.info(Constants.NOME_MODULO, "AdapterPortlet", "processService", "Invocato");
        try {
            SourceBean serviceRequest = null;
            EMFErrorHandler emfErrorHandler = null;
            
            RequestContainer requestContainer = new RequestContainer();
            RequestContainer.setRequestContainer(requestContainer);
            ResponseContainer responseContainer = new ResponseContainer();
            ResponseContainer.setResponseContainer(responseContainer);
            RequestContextIFace requestContext = new DefaultRequestContext(requestContainer,
                    responseContainer);
            
            // try to get from the request the loopback attribute. If present the method has to serve
            // a loopback request (the method has been called from the doRenderService)
            // if not present is a normal request
            boolean isLoop = request.getAttribute(Constants.PUBLISHING_MODE_LOOPBACK) != null;
            if(isLoop) {
            	// remove from the request the loopback attribute
            	request.removeAttribute(Constants.PUBLISHING_MODE_LOOPBACK);
            	//String responseContainerName = (String)request.getAttribute(Constants.RESPONSE_CONTAINER);
            	
            	//**************** START MODFIFICATION ZERBETTO 09-10-2006 ****************
            	// get from the session the previous response container name
            	//String responseContainerName = (String)request.getPortletSession().getAttribute(RESPONSE_CONTAINER_NAME);
            	// get from the session the previous response container
            	//ResponseContainer loopbackResponseContainer = (ResponseContainer)request.getPortletSession().getAttribute(responseContainerName);
            	ResponseContainer loopbackResponseContainer = (ResponseContainer)request.getPortletSession().getAttribute(Constants.RESPONSE_CONTAINER);
            	//**************** END MODFIFICATION ZERBETTO 09-10-2006 ****************
            	
                TracerSingleton.log(
                    Constants.NOME_MODULO,
                    TracerSingleton.DEBUG,
                    "AdapterPortlet::service: loop-back rilevato");
                serviceRequest =
                    loopbackResponseContainer.getLoopbackServiceRequest();
                if (serviceRequest == null)
                    serviceRequest = new SourceBean(Constants.SERVICE_REQUEST);
                else {
                    Object newServiceRequest =
                        serviceRequest.getAttribute(Constants.SERVICE_REQUEST);
                    if ((newServiceRequest != null)
                        && (newServiceRequest instanceof SourceBean))
                        serviceRequest = (SourceBean) newServiceRequest;
                } // if (serviceRequest == null)
                emfErrorHandler = loopbackResponseContainer.getErrorHandler();
                if (emfErrorHandler == null)
                    emfErrorHandler = new EMFErrorHandler();
            } // if (isLoop)
            else {
	            monitor = MonitorFactory.start("controller.adapter.portlet");
	            serviceRequest = new SourceBean(Constants.SERVICE_REQUEST);
	            requestContainer.setServiceRequest(serviceRequest);
	            
                boolean isMultipart = false;
                
                // only an ActionRequest can have a multipart content
                if (request instanceof ActionRequest && PortletFileUpload.isMultipartContent((ActionRequest) request)) {
                	isMultipart = true;
                }
                
                if (isMultipart) {
                	handleMultipartForm((ActionRequest) request, requestContext);
                } else {
                	handleSimpleForm(request, requestContext);
                }
	            
	          	// ***************** START SERVICE ***********************************************
	            String actionName = (String) request.getAttribute("ACTION_NAME");
	            if(actionName!=null) {
	            	request.removeAttribute("ACTION_NAME");
	            	serviceRequest.setAttribute("ACTION_NAME", actionName);
	            	serviceRequest.setAttribute(NEW_SESSION, "TRUE");
	            }
	            String page = (String)request.getAttribute("PAGE");
	            if(page!=null) {
	            	request.removeAttribute("PAGE");
	            	serviceRequest.setAttribute("PAGE", page);
	            	serviceRequest.setAttribute(NEW_SESSION, "TRUE");
	            }  
	        	// *******************************************************************************************
	            emfErrorHandler = new EMFErrorHandler();
            } 
            
            //***************** NAVIGATION CONTROL *******************************************************
            String navigation = getInitParameter("light_navigation");
            if ("enabled".equalsIgnoreCase(navigation)) {
            	serviceRequest = LightNavigationManager.controlLightNavigation(request, serviceRequest);
            }
            //updates service request after LightNavigationManager control
            requestContainer.setServiceRequest(serviceRequest);
            //********************************************************************************************
            
            boolean isRequestedSessionIdValid = true;
            PortletSession session = request.getPortletSession(true);
            /*
            if (session.isNew()) {
                String newSessionString =
                    (String) (serviceRequest.getAttribute(NEW_SESSION));
                isRequestedSessionIdValid =
                    ((newSessionString != null)
                        && (newSessionString.equalsIgnoreCase("TRUE")));
            } // if (session.isNew())
            */
            synchronized (session) {
                
            	// try to get the previous request container. Download from the session the previous 
            	// request container name and if it isn't null (it's null only formthe first invocation) 
            	//use it for download the request container object 
            	RequestContainer parentRequestContainer = null;
            	
            	//**************** START MODFIFICATION ZERBETTO 09-10-2006 ****************
            	//String parentRequestContainerName =
            	//	(String) session.getAttribute(REQUEST_CONTAINER_NAME);
            	//if(parentRequestContainerName != null) {
            	//	parentRequestContainer = (RequestContainer) session.getAttribute(parentRequestContainerName);
            	//}
            	parentRequestContainer = (RequestContainer) session.getAttribute(Constants.REQUEST_CONTAINER);
            	//**************** END MODFIFICATION ZERBETTO 09-10-2006 ****************

                if (!Navigator.isNavigatorEnabled()) {
                    if (parentRequestContainer == null)
                        requestContainer.setSessionContainer(
                            new PortletSessionContainer(true));
                    else
                        requestContainer.setSessionContainer(
                            parentRequestContainer.getSessionContainer());
                }
                else {
                    if (parentRequestContainer == null)
                        requestContainer.setSessionContainer(
                            new PortletSessionContainer(true));
                    else {
                        requestContainer.setSessionContainer(
                            new PortletSessionContainer(false));
                        requestContainer.setParent(parentRequestContainer);
                    } // if (parentRequestContainer == null) else
                } // if (!Navigator.isNavigatorEnabled())
                
                //**************** START MODFIFICATION ZERBETTO 09-10-2006 ****************
                //session.setAttribute(Constants.REQUEST_CONTAINER, requestContainer);
                //**************** END MODFIFICATION ZERBETTO 09-10-2006 ****************
                
            } // synchronized (session)
            if (!isRequestedSessionIdValid) {
                TracerSingleton.log(
                    Constants.NOME_MODULO,
                    TracerSingleton.WARNING,
                    "AdapterPortlet::processAction: sessione scaduta !");
                SessionExpiredUtility.setSessionExpiredAction(serviceRequest);
            } // if (!isRequestedSessionIdValid)
            
            requestContainer.setAttribute(
                    HTTP_SESSION_ID,
                    request.getPortletSession().getId());
            String channelType = "PORTLET";
            String channelTypeParameter =
                (String) (serviceRequest.getAttribute(Constants.CHANNEL_TYPE));
            String channelTypeHeader =
                (String) (requestContainer.getAttribute(HTTP_ACCEPT_HEADER));
            if (((channelTypeParameter != null)
                && channelTypeParameter.equalsIgnoreCase(Constants.WAP_CHANNEL))
                || ((channelTypeHeader != null)
                    && (channelTypeHeader.indexOf(WAP_MIME_TYPE) != -1)))
                channelType = Constants.WAP_CHANNEL;
            requestContainer.setChannelType(channelType);


            TracerSingleton.log(
                Constants.NOME_MODULO,
                TracerSingleton.DEBUG,
                "AdapterPortlet::processAction: requestContainer",
                requestContainer);
            TracerSingleton.log(
                Constants.NOME_MODULO,
                TracerSingleton.DEBUG,
                "AdapterPortlet::processAction: sessionContainer",
                requestContainer.getSessionContainer());

            responseContainer.setErrorHandler(emfErrorHandler);
            SourceBean serviceResponse =
                new SourceBean(Constants.SERVICE_RESPONSE);
            responseContainer.setServiceResponse(serviceResponse);
            try {
                Navigator.checkNavigation(requestContainer);
            } // try
            catch (NavigationException ne) {
                TracerSingleton.log(
                    Constants.NOME_MODULO,
                    TracerSingleton.CRITICAL,
                    "AdapterPortlet::processAction: ",
                    ne);
                requestContainer.setServiceRequest(
                    NavigationErrorUtility.getNavigationErrorServiceRequest());
            } // catch (NavigationException ne)
            serviceRequest = requestContainer.getServiceRequest();

            CoordinatorIFace coordinator =
                DispatcherManager.getCoordinator(requestContext);
            Exception serviceException = null;
            if (coordinator == null) {
                TracerSingleton.log(
                    Constants.NOME_MODULO,
                    TracerSingleton.WARNING,
                    "AdapterPortlet::processAction: coordinator nullo !");
                serviceException = new Exception("Coordinatore non trovato");
                emfErrorHandler.addError(
                    new EMFInternalError(
                        EMFErrorSeverity.ERROR,
                        "Coordinatore non trovato !"));
            } // if (coordinator == null)
            else {
                ((RequestContextIFace) coordinator).setRequestContext(
                    requestContext);
                responseContainer.setBusinessType(
                        coordinator.getBusinessType());
                    responseContainer.setBusinessName(
                        coordinator.getBusinessName());
                try {
                    coordinator.service(serviceRequest, serviceResponse);
                } // try
                catch (Exception ex) {
                    TracerSingleton.log(
                        Constants.NOME_MODULO,
                        TracerSingleton.CRITICAL,
                        "AdapterPortlet::processAction:",
                        ex);
                    serviceException = ex;
                    emfErrorHandler.addError(
                        new EMFInternalError(EMFErrorSeverity.ERROR, ex));
                    responseContainer.setAttribute(PORTLET_EXCEPTION, serviceException);
                } // catch (Exception ex)
                 ((RequestContextIFace) coordinator).setRequestContext(null);
            } // if (coordinator == null) else
            
            //**************** START MODFIFICATION ZERBETTO 09-10-2006 ****************
            //synchronized (session) {
            //    session.setAttribute(
            //        Constants.REQUEST_CONTAINER,
            //        session.getAttribute(Constants.REQUEST_CONTAINER));
            //} // synchronized (session)
            //**************** END MODFIFICATION ZERBETTO 09-10-2006 ****************
            
            TracerSingleton.log(
                Constants.NOME_MODULO,
                TracerSingleton.DEBUG,
                "AdapterPortlet::processAction: responseContainer",
                responseContainer);
            TracerSingleton.log(
                Constants.NOME_MODULO,
                TracerSingleton.DEBUG,
                "AdapterPortlet::processAction: sessionContainer",
                requestContainer.getSessionContainer());
                
			if (serializeSession) {
				TracerSingleton.log(
					Constants.NOME_MODULO,
					TracerSingleton.DEBUG,
					"AdapterPortlet::processAction: sessionContainer size ["
						+ Serializer.serialize(
							requestContainer.getSessionContainer()).length
						+ "]");
			}
			
			//**************** START MODFIFICATION ZERBETTO 09-10-2006 ****************
            //String requestContainerName =
            //    Constants.REQUEST_CONTAINER
            //        + requestContainer.hashCode();
            //String responseContainerName =
            //    Constants.RESPONSE_CONTAINER
            //        + responseContainer.hashCode();                           

            // set into the session new request and response container produced
            //session.setAttribute(requestContainerName, requestContainer);
            //session.setAttribute(responseContainerName, responseContainer);
            // set into the session the name of the new request and response container produced
            //session.setAttribute(REQUEST_CONTAINER_NAME, requestContainerName);
            //session.setAttribute(RESPONSE_CONTAINER_NAME, responseContainerName);
            // if the response is of type ActionResponse (the method has been called from the container)
            // set the name of the container like parameter of the request for the render method
            //if(response instanceof ActionResponse) {
            //	((ActionResponse)response).setRenderParameter(REQUEST_CONTAINER_NAME, requestContainerName);
            //	((ActionResponse)response).setRenderParameter(RESPONSE_CONTAINER_NAME, responseContainerName);
            //} 
            // if the response is of type RenderResponse the method has been called from the doRenderService
            // method and the container names must be returned to the method into an hashmap (it's not possible
            // to set them like parameters because the container will not call automatically the render service)
            //else if(response instanceof RenderResponse){
            //	map.put(REQUEST_CONTAINER_NAME, requestContainerName);
            //	map.put(RESPONSE_CONTAINER_NAME, responseContainerName);
            //}
            session.setAttribute(Constants.REQUEST_CONTAINER, requestContainer);
            session.setAttribute(Constants.RESPONSE_CONTAINER, responseContainer);
            //**************** END MODFIFICATION ZERBETTO 09-10-2006 ****************
            
        } // try
        catch (Exception ex) {
            TracerSingleton.log(
                Constants.NOME_MODULO,
                TracerSingleton.CRITICAL,
                "AdapterPortlet::processAction: ",
                ex);
        } // catch (Excpetion ex) try
        finally {    
        	RequestContainer.delRequestContainer();
            ResponseContainer.delResponseContainer();
            if (monitor != null)
                monitor.stop();
        } // finally
        
        //**************** START MODFIFICATION ZERBETTO 09-10-2006 ****************
        //return map;
        //**************** END MODFIFICATION ZERBETTO 09-10-2006 ****************
        
    } // public void processAction(ActionRequest request, ActionResponse)
    
    
    /**
     * Handle multipart form.
     * 
     * @param request the request
     * @param requestContext the request context
     * 
     * @throws Exception the exception
     */
    private void handleMultipartForm(ActionRequest request, RequestContextIFace requestContext) throws Exception {
    	SourceBean serviceRequest = requestContext.getServiceRequest();
    	
    	// Create a factory for disk-based file items
    	FileItemFactory factory = new DiskFileItemFactory();
    	
        // Create a new file upload handler
    	PortletFileUpload upload = new PortletFileUpload(factory);

        // Parse the request
        List fileItems = upload.parseRequest(request);
        Iterator iter = fileItems.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();

            if (item.isFormField()) {
                String name = item.getFieldName();
                String value = item.getString();
                serviceRequest.setAttribute(name, value);
            } else {
                processFileField(item, requestContext);
            }
        }
    }
    
    
    /**
     * Process file field.
     * 
     * @param item the item
     * @param requestContext the request context
     * 
     * @throws Exception the exception
     */
    private void processFileField(final FileItem item, RequestContextIFace requestContext) throws Exception {
		String uploadManagerName = (String)ConfigSingleton.getInstance().getAttribute("UPLOAD.UPLOAD-MANAGER.NAME");
		if (uploadManagerName == null) {
		    TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL,
		            "AdapterHTTP::processFileField: metodo di upload non selezionato");
		}
		
		IUploadHandler uploadHandler = UploadFactory.getHandler(uploadManagerName);
		if (uploadHandler instanceof RequestContextIFace) {
		    ((RequestContextIFace) uploadHandler).setRequestContext(requestContext);
		}
		uploadHandler.upload(item);
    }
    
    
    /**
     * Handle simple form.
     * 
     * @param request the request
     * @param requestContext the request context
     * 
     * @throws SourceBeanException the source bean exception
     */
    private void handleSimpleForm(PortletRequest request, RequestContextIFace requestContext) throws SourceBeanException {
    	SourceBean serviceRequest = requestContext.getServiceRequest();
    	Enumeration names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String parameterName = (String) names.nextElement();
            String[] parameterValues =
                request.getParameterValues(parameterName);
            if (parameterValues != null)
                for (int i = 0; i < parameterValues.length; i++)
                    serviceRequest.setAttribute(
                        parameterName,
                        parameterValues[i]);
        } // while (names.hasMoreElements())
	}    
   
   
    
    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public void doView(RenderRequest request, RenderResponse response) {   	
    	PortletTracer.info(Constants.NOME_MODULO, "AdapterPortler", "doView", "Invocato");
    	doRenderService(request, response);
    } // public void doView(RenderRequest request, RenderResponse
    
    
    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#doEdit(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public void doEdit(RenderRequest request, RenderResponse response) {
    	PortletTracer.info(Constants.NOME_MODULO, "AdapterPortler", "doView", "Invocato");
    	doRenderService(request, response);
    } //public void doEdit(RenderRequest request, RenderResponse response) 
    
    
    
    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#doHelp(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public void doHelp(RenderRequest request, RenderResponse response) {
    	PortletTracer.info(Constants.NOME_MODULO, "AdapterPortler", "doView", " AdapterPortlet::doHelp Invocato");
    	doRenderService(request, response);
    } //public void doHelp(RenderRequest request, RenderResponse response)   
    
    
    
    
    
    
    
    
    
    
    
   
    
    /**
     * Do render service.
     * 
     * @param request the request
     * @param response the response
     */
    public void doRenderService(RenderRequest request, RenderResponse response) {   	 	   	
        // set into threadLocal variables the jsr 168 portlet object 
    	PortletTracer.info(Constants.NOME_MODULO, "AdapterPortler", "doRenderService", " AdapterPortlet::doRenderService Invocato");
    	PortletSession portletSession = request.getPortletSession();
    	portletSession.setAttribute("PortalLocale", response.getLocale());
        PortletAccess.setPortletConfig(getPortletConfig());
        PortletAccess.setPortletRequest(request);
        PortletAccess.setPortletResponse(response);
        //PortletAccess.setPortalLocale(response.getLocale());
    	// get portlet mode
    	String portletMode = request.getPortletMode().toString();
    	TracerSingleton.log(
                Constants.NOME_MODULO,
                TracerSingleton.DEBUG,
                "AdapterPortlet::doView: invocato per la portlet " + getPortletName() 
				+ " in modalita: " + portletMode);
    	PortletSession session = request.getPortletSession();
    	// get the name of the current request and reponse container which are contained into the session
    	// try to get the names from the attributes of the request (case of the loopback)
    	// if the names aren't defined into the attributes get them from the parameters of the request
    	// (normal case, not loopback)
    	
    	//**************** START MODFIFICATION ZERBETTO 09-10-2006 ****************
    	//String requestContainerName = "";
    	//String responseContainerName = "";
    	//requestContainerName =(String)request.getAttribute(REQUEST_CONTAINER_NAME);
    	//responseContainerName = (String)request.getAttribute(RESPONSE_CONTAINER_NAME);
    	//if( (requestContainerName==null) && (responseContainerName==null) ) {
	    //	requestContainerName =(String)session.getAttribute(REQUEST_CONTAINER_NAME);
	    //	responseContainerName = (String)session.getAttribute(RESPONSE_CONTAINER_NAME);
    	//}
		// download from the session the object response container and request container
        //RequestContainer requestContainer = null;
        //ResponseContainer responseContainer = null;
		//if (requestContainerName != null)
		//	requestContainer = (RequestContainer)session.getAttribute(requestContainerName);
		//else 
		//	TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.INFORMATION, "AdapterPortlet::doView: requestContainerName nullo");
		//if (responseContainerName != null) 
		//	responseContainer = (ResponseContainer)session.getAttribute(responseContainerName);
		//else
		//	TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.INFORMATION, "AdapterPortlet::doView: responseContainerName nullo");
		// if request container is not null set into RequestContainer ThreadLocal variable
    	RequestContainer requestContainer = (RequestContainer)session.getAttribute(Constants.REQUEST_CONTAINER);
    	ResponseContainer responseContainer = (ResponseContainer)session.getAttribute(Constants.RESPONSE_CONTAINER);
    	TracerSingleton.log(
                Constants.NOME_MODULO,
                TracerSingleton.DEBUG,
                "AdapterPortlet::doView: request container retrieved from session: " + requestContainer);
    	TracerSingleton.log(
                Constants.NOME_MODULO,
                TracerSingleton.DEBUG,
                "AdapterPortlet::doView: response container retrieved from session: " + responseContainer);
		//**************** END MODFIFICATION ZERBETTO 09-10-2006 ****************
		
		if(requestContainer != null) 
			RequestContainer.setRequestContainer(requestContainer);
        Boolean isHttpResponseFreezed = null;
        if (responseContainer != null)
        	isHttpResponseFreezed = (Boolean) responseContainer.getAttribute(
                Constants.HTTP_RESPONSE_FREEZED);
        if ((isHttpResponseFreezed == null)
            || (!isHttpResponseFreezed.booleanValue())) {
        	String resource = null;
        	// if requestContainer and responseContainer are null meansthat is the first invocation of
        	// the doView method, so it's necessaty to get from the portlet configuration the type
        	// of service to execute
        	if (requestContainer == null && responseContainer == null) {
        		// ***************** START SERVICE ***********************************************
        		String serviceType = getPortletConfig().getInitParameter(SERVICE_TYPE);
        		String serviceName = getPortletConfig().getInitParameter(SERVICE_NAME);
        		if(serviceType==null) {
        			TracerSingleton.log(
        					Constants.NOME_MODULO,
            	            TracerSingleton.CRITICAL,
            	            "AdapterPortlet::doView: type of start service not find (ACTION/PAGE/JSP)");
        			//System.out.println("AdapterPortlet::doView: type of start service not find (ACTION/PAGE/JSP)");
        		}
        		if(!serviceType.equalsIgnoreCase(SERVICE_JSP) &&
        		   !serviceType.equalsIgnoreCase(SERVICE_ACTION) &&
        		   !serviceType.equalsIgnoreCase(SERVICE_PAGE) ) {
        				TracerSingleton.log(
        					Constants.NOME_MODULO,
            	            TracerSingleton.CRITICAL,
            	            "AdapterPortlet::doView: type of start service unknow (ACTION/PAGE/JSP)");
        				//System.out.println("AdapterPortlet::doView: type of start service unknow (ACTION/PAGE/JSP)");
        				return;
        		}
        		if(serviceName==null) {
        			TracerSingleton.log(
        					Constants.NOME_MODULO,
            	            TracerSingleton.CRITICAL,
            	            "AdapterPortlet::doView: name of start service not find");
        			System.out.println("AdapterPortlet::doView: name of start service not find");
        			return;
        		}		        		
        		if(serviceType.equalsIgnoreCase(SERVICE_JSP)) {
        			RouterPortlet router = new RouterPortlet(serviceName);
                    try {
                        router.route(getPortletContext(), request, response);   
            	    } catch (Exception ex) {
            	        TracerSingleton.log(
            	            Constants.NOME_MODULO,
            	            TracerSingleton.CRITICAL,
            	            "AdapterPortlet::doView: Error during the route to " + serviceName, ex);
            	        System.out.println("AdapterPortlet::doView: Error during the route to " 
            	        		            + serviceName + " (see log file)");
            	    }   
        		} else {
        			if(serviceType.equalsIgnoreCase(SERVICE_ACTION)) {
        				request.setAttribute("ACTION_NAME", serviceName);
        			} else {
        				request.setAttribute("PAGE", serviceName);
        			}
        			try{
        				
        				//**************** START MODFIFICATION ZERBETTO 09-10-2006 ****************
        				//HashMap map = (HashMap)processService(request, response);
                    	//requestContainerName = (String) map.get(REQUEST_CONTAINER_NAME);
                    	//responseContainerName = (String) map.get(RESPONSE_CONTAINER_NAME);
                    	// the new request and response name are set into the session and into the request
                    	// for the recursive call to the doRenderService
                    	//session.setAttribute(REQUEST_CONTAINER_NAME, requestContainerName);
                        //session.setAttribute(RESPONSE_CONTAINER_NAME, responseContainerName);
                        //request.setAttribute(REQUEST_CONTAINER_NAME, requestContainerName);
                        //request.setAttribute(RESPONSE_CONTAINER_NAME, responseContainerName);
        				processService(request, response);
                        //**************** END MODFIFICATION ZERBETTO 09-10-2006 ****************
                        
                    	// recall the method for presentation
                        doRenderService(request, response); 
                        return;	                    	 
        			} catch (Exception e) {
        				TracerSingleton.log(
                	            Constants.NOME_MODULO,
                	            TracerSingleton.CRITICAL,
                	            "AdapterPortlet::doView: Error during the process of the "
								+ serviceType + " service " + serviceName, e);
        				System.out.println("AdapterPortlet::doView: Error during the process of the "
								+ serviceType + " service " + serviceName + " (see log file)");
        			}
        		} 
        		// *******************************************************************
        	} else {
	            Exception serviceException = (Exception)responseContainer.getAttribute(PORTLET_EXCEPTION);
                RequestContextIFace requestContext =
                    new DefaultRequestContext(requestContainer, responseContainer);
                PublisherConfiguration publisher =
	                Publisher.getPublisherConfiguration(requestContext,
	                    serviceException);
	            String publisherType = publisher.getType();
	            List resources = publisher.getResources();
                
                // if publisher is a loop publisher is necessary to recall the method process service 
                // and do view (recursion) in order to exec the new request
                if (publisherType.equalsIgnoreCase(
                        Constants.LOOP_PUBLISHER_TYPE)) {
                    resources = new ArrayList();
                    try { 
                    	// START MODIFICATIONS BY DAVIDE ZERBETTO September 10th 2007
                    	prepareRender(requestContext, publisher);
                    	// refresh ResponseContainer in session
                    	session.setAttribute(Constants.RESPONSE_CONTAINER, responseContainer);
                    	// END MODIFICATIONS BY DAVIDE ZERBETTO September 10th 2007
                    	
                    	// se into the request a parameter loopback, the process service method
                    	// will use it in order to know if is a loopback request or not
                    	request.setAttribute(Constants.PUBLISHING_MODE_LOOPBACK, "TRUE");
                    	// recall the process service, The name of the new request and response container
                    	// are returned into an hashmap. Normally the names are set like parameters of the
                    	// renderRequest from the process service method and when the container invokes
                    	// the doView, doEdit, doHelp method these can get them. In this case calling the 
                    	// method directly, the container cannot recall the doView, doEdit, doHelp so the 
                    	// way is to set into the renderRequest object the names returned and call the 
                    	// doRenderService (recursion)
                    	
                    	//**************** START MODFIFICATION ZERBETTO 09-10-2006 ****************
                    	//HashMap map = (HashMap)processService(request, response);
                    	//requestContainerName = (String) map.get(REQUEST_CONTAINER_NAME);
                    	//responseContainerName = (String) map.get(RESPONSE_CONTAINER_NAME);
                    	// the new request and response name are set into the session, override the 
                    	// previous names 
                    	//session.setAttribute(REQUEST_CONTAINER_NAME, requestContainerName);
                        //session.setAttribute(RESPONSE_CONTAINER_NAME, responseContainerName);
                        //request.setAttribute(REQUEST_CONTAINER_NAME, requestContainerName);
                        //request.setAttribute(RESPONSE_CONTAINER_NAME, responseContainerName);
                    	processService(request, response);
                        //**************** END MODFIFICATION ZERBETTO 09-10-2006 ****************
                        
                    	// recall the method for presentation
                        doRenderService(request, response); 
                        return;	                    	 
                    } catch (Exception e) {
                    	TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.MAJOR, 
        						"AdapterPortlet::doRenderService: error during the execution of the loopback request", e);
                    }
                }
	            
	            
	            
	            if (publisherType
	                .equalsIgnoreCase(Constants.LOOP_PUBLISHER_TYPE)
	                || publisherType.equalsIgnoreCase(SERVLET_PUBLISHER_TYPE)
	                || publisherType.equalsIgnoreCase(JSP_PUBLISHER_TYPE)) {          	
	                Iterator iterator = resources.iterator();
	                SourceBean resourceSourceBean = null;                
	            	while (iterator.hasNext()) {
	            		resourceSourceBean = (SourceBean)iterator.next();
	            		if ( ((String)resourceSourceBean.getAttribute("mode")).equalsIgnoreCase(portletMode) )
	            			break;
	            	}
	            	if (resourceSourceBean == null) {
	            			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.MAJOR, 
	            						"AdapterPortlet::doView: no resources defined for mode " + portletMode);
	            	} else 
	            		resource = (String)resourceSourceBean.getAttribute("resource");
	                        	                                
	                RouterPortlet router = new RouterPortlet(resource);
	                try {
	                	//**************** START MODFIFICATION ZERBETTO 09-10-2006 ****************
	                    //router.setParameter(Constants.REQUEST_CONTAINER, requestContainerName);
	                    //router.setParameter(Constants.RESPONSE_CONTAINER,responseContainerName);
	                    //**************** END MODFIFICATION ZERBETTO 09-10-2006 ****************
	                    
	                    router.route(getPortletContext(), request, response);   
	        	    } // try
	        	    catch (Exception ex) {
	        	        TracerSingleton.log(
	        	            Constants.NOME_MODULO,
	        	            TracerSingleton.CRITICAL,
	        	            "AdapterPortlet::doView: ",
	        	            ex);
	        	    } // catch (Excpetion ex) try    
	        	    finally {
	                	// Allow better garbage collection
	                	publisher.release();
	        	    }
	            } // if (publisherType.equalsIgnoreCase(Constants.LOOP_PUBLISHER_TYPE)
	              // || publisherType.equalsIgnoreCase(SERVLET_PUBLISHER_TYPE) ||
	              // publisherType.equalsIgnoreCase(JSP_PUBLISHER_TYPE))
	            else { 
	                response.setContentType(HTTP_CONTENT_TYPE);
	                Monitor renderingMonitor =
	                        MonitorFactory.start(
	                            "view.portlet."
	                                + publisherType.toLowerCase()
	                                + "."
	                                + publisher.getName().toLowerCase());
	                try {
	                    response.getWriter().print(
	                        PresentationRendering.render(
	                            responseContainer,
	                            resources));
	                    response.getWriter().flush();
	                } // try
	                catch (Exception ex) {
	                	TracerSingleton.log(
	                            Constants.NOME_MODULO,
	                            TracerSingleton.DEBUG,
	                            "AdapterPortlet::doView:eccezzione", ex);
	                } // catch (Exception ex)
	                finally {
	                	// Allow better garbage collection
	                	publisher.release();
	                	// Stop performance measurement
	                    renderingMonitor.stop();
	                } // finally
	            } // if (publisherType.equalsIgnoreCase(AF_PUBLISHER_TYPE)
	            // || publisherType.equalsIgnoreCase(SERVLET_PUBLISHER_TYPE) ||
	            // publisherType.equalsIgnoreCase(JSP_PUBLISHER_TYPE)) else
        	} //if (requestContainer == null && responseContainer == null) else
        } // if ((isHttpResponseFreezed == null) ||
        // (!isHttpResponseFreezed.getBoolean()))
        else
            TracerSingleton.log(
                Constants.NOME_MODULO,
                TracerSingleton.DEBUG,
                "AdapterPortlet::service: http response congelata");   
    } // public void doRenderService(RenderRequest request, RenderResponse

    
    // START MODIFICATIONS BY DAVIDE ZERBETTO September 10th 2007
    // This method is here but it shouldn't be!!!
    // Since a portlet loop render is missing, this method was copied here from HTTPLoopRenderer
	/**
     * Prepare render.
     * 
     * @param requestContext the request context
     * @param publisher the publisher
     * 
     * @throws Exception the exception
     */
    private void prepareRender(RequestContextIFace requestContext, PublisherConfiguration publisher) throws Exception {

		RequestContainer requestContainer = requestContext.getRequestContainer();
		ResponseContainer responseContainer = requestContext.getResponseContainer();

		// Prepare service request for loopback management
        try {
            SourceBean loopbackServiceRequest = new SourceBean(Constants.SERVICE_REQUEST);
            loopbackServiceRequest.setAttribute(Navigator.NAVIGATOR_DISABLED, "TRUE");
            
            SourceBean renderingConfig = publisher.getRenderingConfig();
            List resourcesConfig = renderingConfig.getAttributeAsList("RESOURCES.PARAMETER");
            for (int j = 0; j < resourcesConfig.size(); j++) {
                SourceBean consequence = (SourceBean) resourcesConfig.get(j);
                String parameterName = (String) consequence.getAttribute("NAME");
                String parameterScope = (String) consequence.getAttribute("SCOPE");
                String parameterType = (String) consequence.getAttribute("TYPE");
                String parameterValue = (String) consequence.getAttribute("VALUE");
                Object inParameterValue = null;
                if (parameterType.equalsIgnoreCase("ABSOLUTE"))
                    inParameterValue = parameterValue;
                else {
                	inParameterValue = ContextScooping.getScopedParameter(requestContainer,
                    						responseContainer, 
                    						parameterValue, parameterScope, consequence);
                }
                
                if (inParameterValue == null)
                    continue;
                if (inParameterValue instanceof SourceBean)
                    loopbackServiceRequest.setAttribute((SourceBean) inParameterValue);
                else
                    loopbackServiceRequest.setAttribute(parameterName, inParameterValue);
            } // for (int j = 0; j < consequences.size(); j++)
            responseContainer.setLoopbackServiceRequest(loopbackServiceRequest);
        } // try
        catch (SourceBeanException sbe) {
            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL,
                    "Publisher::getPublisher:", sbe);
        } // catch (SourceBeanException sbe)
	}
	// END MODIFICATIONS BY DAVIDE ZERBETTO September 10th 2007

    
} // public class AdapterPortlet extends GenericPortlet
