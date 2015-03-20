/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spago.dispatching.httpchannel;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.base.Constants;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.ResponseContainerAccess;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.dispatching.coordinator.CoordinatorIFace;
import it.eng.spago.dispatching.coordinator.DispatcherManager;
import it.eng.spago.dispatching.httpchannel.upload.IUploadHandler;
import it.eng.spago.dispatching.httpchannel.upload.UploadFactory;
import it.eng.spago.dispatching.service.DefaultRequestContext;
import it.eng.spago.dispatching.service.RequestContextIFace;
import it.eng.spago.dispatching.service.ServiceIFace;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.SessionExpiredException;
import it.eng.spago.event.EventNotifierFactory;
import it.eng.spago.event.IEventNotifier;
import it.eng.spago.event.ServiceEndEvent;
import it.eng.spago.event.ServiceStartEvent;
import it.eng.spago.exception.EMFExceptionHandler;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spago.navigation.Navigator;
import it.eng.spago.presentation.Publisher;
import it.eng.spago.presentation.PublisherConfiguration;
import it.eng.spago.presentation.rendering.RenderIFace;
import it.eng.spago.presentation.rendering.RenderManager;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spago.util.Serializer;

// TODO: Auto-generated Javadoc
/**
 * The Class AdapterHTTP.
 */
public class AdapterHTTP extends HttpServlet {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant NEW_SESSION. */
    public static final String NEW_SESSION = "NEW_SESSION";

    /** The Constant HTTP_CONTENT_TYPE. */
    public static final String HTTP_CONTENT_TYPE = "text/html";

    /** The Constant WAP_CONTENT_TYPE. */
    public static final String WAP_CONTENT_TYPE = "text/vnd.wap.wml";

    /** The Constant XML_CONTENT_TYPE. */
    public static final String XML_CONTENT_TYPE = "text/xml";

    /** The Constant HTTP_REQUEST_AUTH_TYPE. */
    public static final String HTTP_REQUEST_AUTH_TYPE = "HTTP_REQUEST_AUTH_TYPE";

    /** The Constant HTTP_REQUEST_CHARACTER_ENCODING. */
    public static final String HTTP_REQUEST_CHARACTER_ENCODING = "HTTP_REQUEST_CHARACTER_ENCODING";

    /** The Constant HTTP_REQUEST_CONTENT_LENGTH. */
    public static final String HTTP_REQUEST_CONTENT_LENGTH = "HTTP_REQUEST_CONTENT_LENGTH";

    /** The Constant HTTP_REQUEST_CONTENT_TYPE. */
    public static final String HTTP_REQUEST_CONTENT_TYPE = "HTTP_REQUEST_CONTENT_TYPE";

    /** The Constant HTTP_REQUEST_CONTEXT_PATH. */
    public static final String HTTP_REQUEST_CONTEXT_PATH = "HTTP_REQUEST_CONTEXT_PATH";

    /** The Constant HTTP_REQUEST_METHOD. */
    public static final String HTTP_REQUEST_METHOD = "HTTP_REQUEST_METHOD";

    /** The Constant HTTP_REQUEST_PATH_INFO. */
    public static final String HTTP_REQUEST_PATH_INFO = "HTTP_REQUEST_PATH_INFO";

    /** The Constant HTTP_REQUEST_PATH_TRANSLATED. */
    public static final String HTTP_REQUEST_PATH_TRANSLATED = "HTTP_REQUEST_PATH_TRANSLATED";

    /** The Constant HTTP_REQUEST_PROTOCOL. */
    public static final String HTTP_REQUEST_PROTOCOL = "HTTP_REQUEST_PROTOCOL";

    /** The Constant HTTP_REQUEST_QUERY_STRING. */
    public static final String HTTP_REQUEST_QUERY_STRING = "HTTP_REQUEST_QUERY_STRING";

    /** The Constant HTTP_REQUEST_REMOTE_ADDR. */
    public static final String HTTP_REQUEST_REMOTE_ADDR = "HTTP_REQUEST_REMOTE_ADDR";

    /** The Constant HTTP_REQUEST_REMOTE_HOST. */
    public static final String HTTP_REQUEST_REMOTE_HOST = "HTTP_REQUEST_REMOTE_HOST";

    /** The Constant HTTP_REQUEST_REMOTE_USER. */
    public static final String HTTP_REQUEST_REMOTE_USER = "HTTP_REQUEST_REMOTE_USER";

    /** The Constant HTTP_REQUEST_REQUESTED_SESSION_ID. */
    public static final String HTTP_REQUEST_REQUESTED_SESSION_ID = "HTTP_REQUEST_REQUESTED_SESSION_ID";

    /** The Constant HTTP_REQUEST_REQUEST_URI. */
    public static final String HTTP_REQUEST_REQUEST_URI = "HTTP_REQUEST_REQUEST_URI";

    /** The Constant HTTP_REQUEST_SCHEME. */
    public static final String HTTP_REQUEST_SCHEME = "HTTP_REQUEST_SCHEME";

    /** The Constant HTTP_REQUEST_SERVER_NAME. */
    public static final String HTTP_REQUEST_SERVER_NAME = "HTTP_REQUEST_SERVER_NAME";

    /** The Constant HTTP_REQUEST_SERVER_PORT. */
    public static final String HTTP_REQUEST_SERVER_PORT = "HTTP_REQUEST_SERVER_PORT";

    /** The Constant HTTP_REQUEST_SERVLET_PATH. */
    public static final String HTTP_REQUEST_SERVLET_PATH = "HTTP_REQUEST_SERVLET_PATH";

    /** The Constant HTTP_REQUEST_USER_PRINCIPAL. */
    public static final String HTTP_REQUEST_USER_PRINCIPAL = "HTTP_REQUEST_USER_PRINCIPAL";

    /** The Constant HTTP_REQUEST_REQUESTED_SESSION_ID_FROM_COOKIE. */
    public static final String HTTP_REQUEST_REQUESTED_SESSION_ID_FROM_COOKIE = "HTTP_REQUEST_REQUESTED_SESSION_ID_FROM_COOKIE";

    /** The Constant HTTP_REQUEST_REQUESTED_SESSION_ID_FROM_URL. */
    public static final String HTTP_REQUEST_REQUESTED_SESSION_ID_FROM_URL = "HTTP_REQUEST_REQUESTED_SESSION_ID_FROM_URL";

    /** The Constant HTTP_REQUEST_REQUESTED_SESSION_ID_VALID. */
    public static final String HTTP_REQUEST_REQUESTED_SESSION_ID_VALID = "HTTP_REQUEST_REQUESTED_SESSION_ID_VALID";

    /** The Constant HTTP_REQUEST_SECURE. */
    public static final String HTTP_REQUEST_SECURE = "HTTP_REQUEST_SECURE";

    /** The Constant HTTP_ACCEPT_HEADER. */
    public static final String HTTP_ACCEPT_HEADER = "ACCEPT";

    /** The Constant WAP_MIME_TYPE. */
    public static final String WAP_MIME_TYPE = "vnd.wap";

    /** The Constant HTTP_SESSION_ID. */
    public static final String HTTP_SESSION_ID = "HTTP_SESSION_ID";
    
    /** The Constant HTTP_REQUESTED_WITH. */
    public static final String HTTP_REQUESTED_WITH = "x-requested-with";
        
    /** The Constant XMLHTTPREQUEST. */
    public static final String XMLHTTPREQUEST = "XMLHttpRequest";

    /** The Constant SERIALIZE_SESSION_ATTRIBUTE. */
    private static final String SERIALIZE_SESSION_ATTRIBUTE = "COMMON.SERIALIZE_SESSION";

    // Atributo della configurazione che indica se serializzare il contenuto
    // della sessione
    /** The serialize session. */
    private boolean serializeSession = false;

    
    /**
     * Handle query string field.
     * 
     * @param request the request
     * @param serviceReq the service req
     * @param queryStringFieldName the query string field name
     * 
     * @throws SourceBeanException the source bean exception
     */
    private void handleQueryStringField(HttpServletRequest request,SourceBean serviceReq,String queryStringFieldName) throws SourceBeanException {
    	
    	String queryString = queryStringFieldName.substring(queryStringFieldName.indexOf("{")+1, queryStringFieldName.indexOf("}"));
    	
    	StringTokenizer st = new StringTokenizer(queryString, "&", false);
    	
    	String parameterToken = null;
    	String parameterName = null;
    	String parameterValue = null;
    	while (st.hasMoreTokens()){
    		parameterToken = st.nextToken();
    		parameterName = parameterToken.substring(0, parameterToken.indexOf("="));
    		parameterValue = parameterToken.substring(parameterToken.indexOf("=")+1);
    		if (serviceReq.containsAttribute(parameterName)){
    			serviceReq.updAttribute(parameterName, parameterValue);
    		} else {
    			serviceReq.setAttribute(parameterName, parameterValue);
    		}
    	}
    	
    }
    
    /**
     * Make the service name available in request also if the service was invoked with the
     * .action or .page URL
     * 
     * @param requestContainer Current RequestContainer
     * @param serviceRequest Current request
     */
    private void handleServiceName(SourceBean serviceRequest, final RequestContainer requestContainer) {
    	String path = ((String)requestContainer.getAttribute(HTTP_REQUEST_SERVLET_PATH)).toUpperCase();
    	try {
            if (path.endsWith(Constants.ACTION_URL_PATTERN)) {
            	serviceRequest.setAttribute(Constants.ACTION_NAME, path.substring(1, path.length() - Constants.ACTION_URL_PATTERN.length()));
            } else if (path.endsWith(Constants.PAGE_URL_PATTERN)) {
            	serviceRequest.setAttribute(Constants.PAGE, path.substring(1, path.length() - Constants.PAGE_URL_PATTERN.length()));
            }
    	} catch (SourceBeanException ex) {
            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING,
                    "AdapterHTTP::handleServiceName: ", ex);
    	}
    }
    
    /**
     * Handle suspend resume.
     * 
     * @param serviceRequest the service request
     * @param requestContainer the request container
     * 
     * @throws SourceBeanException the source bean exception
     */
    private void handleSuspendResume(SourceBean serviceRequest, RequestContainer requestContainer) 
    	throws SourceBeanException {
        String deleteSuspendResumeId = (String)serviceRequest.getAttribute("DELETE_SUSPEND_RESUME_ID"); 
        if ((deleteSuspendResumeId != null) && (deleteSuspendResumeId.trim().length() > 0)){
        	TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.INFORMATION,
					"AdapterHTTP::service: DELETE_SUSPEND_RESUME_ID ["+deleteSuspendResumeId+"] FOUND IN SERVICE REQUEST : DELETE SUSPEND RESUME CONTAINERS");
        	SessionContainer aPermanentContainer = requestContainer.getSessionContainer().getPermanentContainer();
        	SourceBean suspendedResumeContainers = (SourceBean)aPermanentContainer.getAttribute(deleteSuspendResumeId);
        	/*
        	TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.INFORMATION,
					"AdapterHTTP::service: DELETE_SUSPEND_RESUME_ID Container to delete" + suspendedResumeContainers);
        	*/
        	if (suspendedResumeContainers != null){
        		aPermanentContainer.delAttribute(deleteSuspendResumeId);
        	}
        	/*
        	suspendedResumeContainers = (SourceBean)aPermanentContainer.getAttribute(deleteSuspendResumeId);
        	TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.INFORMATION,
					"AdapterHTTP::service: DELETE_SUSPEND_RESUME_ID Container to delete" + suspendedResumeContainers);
        	*/
        }	
        // Suspend Service Request
        String suspendResumeId = (String)serviceRequest.getAttribute("SUSPEND_RESUME_ID"); 
        if ((suspendResumeId != null) && (suspendResumeId.trim().length() > 0)){
        	TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.INFORMATION,
					"AdapterHTTP::service: SUSPEND_RESUME_ID ["+suspendResumeId+"] FOUND IN SERVICE REQUEST SUSPEND CURRENT SERVICE REQUEST");
        	SessionContainer aPermanentContainer = requestContainer.getSessionContainer().getPermanentContainer();
        	SourceBean suspendedResumeContainers = (SourceBean)aPermanentContainer.getAttribute(suspendResumeId);
            
            if (suspendedResumeContainers == null) {
                TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING,
                        "AdapterHTTP::service: SUSPENDED-RESUME-CONTAINER NOT FOUND");
            } else if (suspendedResumeContainers.getAttribute(Constants.SERVICE_REQUEST) != null){
        		TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.INFORMATION,
        				"AdapterHTTP::service: SUSPENDED-RESUME-CONTAINER CONTAINS ALREADY A SERVICE REQUEST OVERWRITE IT");
        		suspendedResumeContainers.updAttribute(serviceRequest);
        	} else {
        		TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.INFORMATION,
        				"AdapterHTTP::service: SUSPENDED-RESUME-CONTAINER CONTAINS ALREADY A SERVICE REQUEST WRITE IT");
        		suspendedResumeContainers.setAttribute(serviceRequest);
        	}
        }
    }
    
    /**
     * Handle multipart form.
     * 
     * @param request the request
     * @param requestContext the request context
     * 
     * @throws Exception the exception
     */
    private void handleMultipartForm(HttpServletRequest request, RequestContextIFace requestContext) 
    	throws Exception{
    	SourceBean serviceRequest = requestContext.getServiceRequest();
    	
    	// Create a factory for disk-based file items
    	FileItemFactory factory = new DiskFileItemFactory();
    	
        // Create a new file upload handler
    	ServletFileUpload upload = new ServletFileUpload(factory);

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
     * Handle simple form.
     * 
     * @param request the request
     * @param requestContext the request context
     * 
     * @throws SourceBeanException the source bean exception
     */
    private void handleSimpleForm(HttpServletRequest request, RequestContextIFace requestContext)
    	throws SourceBeanException{
    	SourceBean serviceRequest = requestContext.getServiceRequest();
        Enumeration names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String parameterName = (String) names.nextElement();
            
            if (parameterName.startsWith("QUERY_STRING")){
            	handleQueryStringField(request, serviceRequest, parameterName);
            }else{
            	String[] parameterValues = request.getParameterValues(parameterName);
            	if (parameterValues != null)
            		for (int i = 0; i < parameterValues.length; i++)
            			serviceRequest.setAttribute(parameterName, parameterValues[i]);
            }
        } // while (names.hasMoreElements())
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Monitor monitor = null;
    	IEventNotifier eventNotifier = null;
    	RequestContextIFace requestContext = null;

        try {
        	SourceBean serviceRequest = null;
            EMFErrorHandler emfErrorHandler = null;
            EMFExceptionHandler exceptionHandler = new EMFExceptionHandler();
            
            // Retrieve LOOP responseContainer, if any
            ResponseContainer loopbackResponseContainer = ResponseContainer.getResponseContainer();
            
            RequestContainer requestContainer = new RequestContainer();
            RequestContainer.setRequestContainer(requestContainer);
            
            ResponseContainer responseContainer = new ResponseContainer();
            ResponseContainer.setResponseContainer(responseContainer);
            
            requestContext = new DefaultRequestContext(requestContainer,
                    responseContainer);
            
            // Retrieve HTTP session
            HttpSession session = request.getSession(true);

        	eventNotifier = EventNotifierFactory.getEventNotifier();
        	eventNotifier.notifyEvent(
        				new ServiceStartEvent(session), 
        				requestContext);            
        	// Trace only after calling listener, so the session id can be written on log files
            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
            					"AdapterHTTP::service: invocato");

            boolean loopback = (request.getAttribute(Constants.PUBLISHING_MODE_LOOPBACK) != null);
            if (loopback) {
                TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
                "AdapterHTTP::service: loop-back rilevato");

                // remove from the request the loopback attribute
            	request.removeAttribute(Constants.PUBLISHING_MODE_LOOPBACK);
            
                loopbackResponseContainer = ResponseContainerAccess.getResponseContainer(request);
                serviceRequest = loopbackResponseContainer.getLoopbackServiceRequest();
                if (serviceRequest == null) {
                    serviceRequest = new SourceBean(Constants.SERVICE_REQUEST);
                } else {
                    Object newServiceRequest = serviceRequest
                            .getAttribute(Constants.SERVICE_REQUEST);
                    if ((newServiceRequest != null) && (newServiceRequest instanceof SourceBean))
                        serviceRequest = (SourceBean) newServiceRequest;
                } // if (serviceRequest == null)
                requestContainer.setServiceRequest(serviceRequest);
                
                // The errors are kept in loop mode, so retrieve old error handler
                emfErrorHandler = loopbackResponseContainer.getErrorHandler();
                
                if (emfErrorHandler == null) {
                    emfErrorHandler = new EMFErrorHandler();
                }
            } // if (loopbackResponseContainer != null)
            else {
                monitor = MonitorFactory.start("controller.adapter.http");
                serviceRequest = new SourceBean(Constants.SERVICE_REQUEST);
                requestContainer.setServiceRequest(serviceRequest);

                // Get header parameter before parsing the request
                setHttpRequestData(request, requestContainer);
                
                // Check if the service was invoked with the .action or .page URL
                handleServiceName(serviceRequest, requestContainer);
                
                boolean isMultipart = ServletFileUpload.isMultipartContent(new ServletRequestContext(request));
                if (isMultipart) {
                	handleMultipartForm(request, requestContext);
                } else {
                	handleSimpleForm(request, requestContext);
                }

                emfErrorHandler = new EMFErrorHandler();
            } // if (loopbackResponseContainer != null) else

            
            //***************** NAVIGATION CONTROL *******************************************************
            serviceRequest = LightNavigationManager.controlLightNavigation(request, serviceRequest);
            requestContainer.setServiceRequest(serviceRequest);
            //********************************************************************************************
            
            Exception serviceException = null;
            CoordinatorIFace coordinator = null;
            try {
	            responseContainer.setErrorHandler(emfErrorHandler);
	            
	            String channelType = Constants.HTTP_CHANNEL;
	            String channelTypeParameter = (String) (serviceRequest
	                    .getAttribute(Constants.CHANNEL_TYPE));
	            String channelTypeHeader = (String) (requestContainer.getAttribute(HTTP_ACCEPT_HEADER));
	            if (((channelTypeParameter != null) && channelTypeParameter
	                    .equalsIgnoreCase(Constants.WAP_CHANNEL))
	                    || ((channelTypeHeader != null) && (channelTypeHeader.indexOf(WAP_MIME_TYPE) != -1)))
	                channelType = Constants.WAP_CHANNEL;
	            requestContainer.setChannelType(channelType);
	            requestContainer.setInternalRequest(request);
	            requestContainer.setInternalResponse(response);
	            requestContainer.setAdapterConfig(getServletConfig());
	            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
	                    "AdapterHTTP::service: requestContainer", requestContainer);
	            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
	                    "AdapterHTTP::service: sessionContainer", requestContainer
	                            .getSessionContainer());
	
	            SourceBean serviceResponse = new SourceBean(Constants.SERVICE_RESPONSE);
	            responseContainer.setServiceResponse(serviceResponse);

	            checkSession(session, requestContext);
                Navigator.checkNavigation(requestContainer);
	            
	            // Refresh service request because Navigator services can changed it
	            serviceRequest = requestContainer.getServiceRequest();
	            
	            // Suspend/Resume service
	            handleSuspendResume(serviceRequest, requestContainer);
	            
	            coordinator = DispatcherManager.getCoordinator(requestContext);
	            if (coordinator == null) {
	                TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING,
	                        "AdapterHTTP::service: coordinator nullo !");
	                serviceException = new Exception("Coordinatore non trovato");
	                emfErrorHandler.addError(new EMFInternalError(EMFErrorSeverity.ERROR,
	                        "Coordinatore non trovato !"));
	            } // if (coordinator == null)
	            else {
	                ((RequestContextIFace) coordinator).setRequestContext(requestContext);
	                responseContainer.setBusinessType(coordinator.getBusinessType());
	                responseContainer.setBusinessName(coordinator.getBusinessName());
	                responseContainer.setPublisherName(coordinator.getPublisherName());
	                    coordinator.service(serviceRequest, serviceResponse);
	                    
                    ((RequestContextIFace) coordinator).setRequestContext(null);
//	                	requestContainer.setInternalRequest(null);
	            } // if (coordinator == null) else

            } // try
            catch (Exception ex) {
            	ServiceIFace service = (coordinator != null)? coordinator.getService() : null;
            	exceptionHandler.handleException(ex, service, requestContext);
            } // catch (Exception ex)
            
//            requestContainer.setInternalResponse(null);
//            requestContainer.setAdapterConfig(null);
            // nel caso in cui sia attiva la persistenza della sessione
            // forza la scrittura sul database
            synchronized (session) {
                session.setAttribute(Constants.REQUEST_CONTAINER, session.getAttribute(Constants.REQUEST_CONTAINER));
            } // synchronized (session)
            
            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
                    "AdapterHTTP::service: responseContainer", responseContainer);
            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
                    "AdapterHTTP::service: sessionContainer", requestContainer
                            .getSessionContainer());

            if (serializeSession) {
                TracerSingleton
                        .log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
                                "AdapterHTTP::service: sessionContainer size ["
                                        + Serializer.serialize(requestContainer
                                                .getSessionContainer()).length + "]");
            }

            render(requestContext, serviceException);
        } // try
        catch (Exception ex) {
            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL,
                    "AdapterHTTP::service: ", ex);
        } // catch (Excpetion ex) try
        finally {
            RequestContainer.delRequestContainer();
            ResponseContainer.delResponseContainer();
            if (monitor != null) {
                monitor.stop();
            }
            
            if (eventNotifier != null) {
            	eventNotifier.notifyEvent(
        				new ServiceEndEvent(null), 
        				requestContext);
            }
            
        } // finally
    } // public void service(HttpServletRequest request, HttpServletResponse
    // response) throws IOException, ServletException

    /**
     * Process file field.
     * 
     * @param item the item
     * @param requestContext the request context
     * 
     * @throws Exception the exception
     */
    private void processFileField(final FileItem item, RequestContextIFace requestContext)
            throws Exception {
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
     * Check session.
     * 
     * @param session the session
     * @param requestContext the request context
     * 
     * @throws SessionExpiredException the session expired exception
     */
    private void checkSession(HttpSession session, RequestContextIFace requestContext)
    	throws SessionExpiredException {
    	
    	// start modifications by Zerbetto on 25-02-2008: NEW_SESSION parameter can force a new session
        boolean isRequestedSessionIdValid = true;
        boolean isRequiredNewSession = false;    // Zerbetto on 25-02-2008
        RequestContainer requestContainer = requestContext.getRequestContainer();
                
        if (session.isNew()) {
            isRequestedSessionIdValid = (requestContainer.getAttribute(HTTP_REQUEST_REQUESTED_SESSION_ID) == null);
        	String newSessionRequestAttr = (String) requestContainer.getServiceRequest().getAttribute(NEW_SESSION); // Zerbetto on 25-02-2008
        	isRequiredNewSession = newSessionRequestAttr != null && newSessionRequestAttr.equalsIgnoreCase("TRUE"); // Zerbetto on 25-02-2008
        } // if (session.isNew())
        synchronized (session) {
            RequestContainer parentRequestContainer = (RequestContainer) session
                    .getAttribute(Constants.REQUEST_CONTAINER);
            if (!Navigator.isNavigatorEnabled()) {
                if (parentRequestContainer == null)
                    requestContainer.setSessionContainer(new SessionContainer(true));
                else
                    requestContainer.setSessionContainer(parentRequestContainer
                            .getSessionContainer());
            }
            else {
                if (parentRequestContainer == null)
                    requestContainer.setSessionContainer(new SessionContainer(true));
                else {
                    requestContainer.setSessionContainer(new SessionContainer(false));
                    requestContainer.setParent(parentRequestContainer);
                } // if (parentRequestContainer == null) else
            } // if (!Navigator.isNavigatorEnabled())
            session.setAttribute(Constants.REQUEST_CONTAINER, requestContainer);
        } // synchronized (session)
        if (!isRequestedSessionIdValid) {
        	if (!isRequiredNewSession) { // Zerbetto on 25-02-2008
	            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING,
	                    "AdapterHTTP::service: sessione scaduta !");
	            throw new SessionExpiredException(EMFErrorSeverity.ERROR, "Expired Session");
        	} // Zerbetto on 25-02-2008
        } // if (!isRequestedSessionIdValid)
        // end modifications by Zerbetto on 25-02-2008: NEW_SESSION parameter can force a new session
    }

    /**
     * Render.
     * 
     * @param requestContext the request context
     * @param serviceException the service exception
     * 
     * @throws Exception the exception
     */
    private void render(RequestContextIFace requestContext, Exception serviceException)
            throws Exception {
        ResponseContainer responseContainer = requestContext.getResponseContainer();
       
        Boolean isHttpResponseFreezed = (Boolean) responseContainer
                .getAttribute(Constants.HTTP_RESPONSE_FREEZED);
        if ((isHttpResponseFreezed == null) || (!isHttpResponseFreezed.booleanValue())) {
        	
        	// Retrieve publisher configuration for current service
            PublisherConfiguration publisherConfig = Publisher.getPublisherConfiguration(requestContext,
                    serviceException);
            
            try {
            	// Retrieve renderer according to publisher type and channel type
	        	RenderIFace renderer = RenderManager.getInstance().getRenderer(publisherConfig);

	        	// Store in session last used publisher name
	            Publisher.setLastPublisherName(publisherConfig.getName());

	            // Setup phase
	            renderer.prepareRender(requestContext, publisherConfig, getServletContext());
	            
	            // Render phase
	            renderer.render(requestContext, publisherConfig, getServletContext());
            } finally {
            	// Allow better garbage collection
            	publisherConfig.release();
            }

        } // if ((isHttpResponseFreezed == null) ||
        // (!isHttpResponseFreezed.getBoolean()))
        else
            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
                    "AdapterHTTP::service: http response congelata");
    }

    /**
     * Sets the http request data.
     * 
     * @param request the request
     * @param requestContainer the request container
     */
    private void setHttpRequestData(HttpServletRequest request, RequestContainer requestContainer) {
        requestContainer.setAttribute(HTTP_REQUEST_AUTH_TYPE, request.getAuthType());
        requestContainer.setAttribute(HTTP_REQUEST_CHARACTER_ENCODING, request.getCharacterEncoding());
        requestContainer.setAttribute(HTTP_REQUEST_CONTENT_LENGTH, String.valueOf(request
                .getContentLength()));
        requestContainer.setAttribute(HTTP_REQUEST_CONTENT_TYPE, request.getContentType());
        requestContainer.setAttribute(HTTP_REQUEST_CONTEXT_PATH, request.getContextPath());
        requestContainer.setAttribute(HTTP_REQUEST_METHOD, request.getMethod());
        requestContainer.setAttribute(HTTP_REQUEST_PATH_INFO, request.getPathInfo());
        requestContainer.setAttribute(HTTP_REQUEST_PATH_TRANSLATED, request.getPathTranslated());
        requestContainer.setAttribute(HTTP_REQUEST_PROTOCOL, request.getProtocol());
        requestContainer.setAttribute(HTTP_REQUEST_QUERY_STRING, request.getQueryString());
        requestContainer.setAttribute(HTTP_REQUEST_REMOTE_ADDR, request.getRemoteAddr());
        requestContainer.setAttribute(HTTP_REQUEST_REMOTE_HOST, request.getRemoteHost());
        requestContainer.setAttribute(HTTP_REQUEST_REMOTE_USER, request.getRemoteUser());
        requestContainer.setAttribute(HTTP_REQUEST_REQUESTED_SESSION_ID, request
                .getRequestedSessionId());
        requestContainer.setAttribute(HTTP_REQUEST_REQUEST_URI, request.getRequestURI());
        requestContainer.setAttribute(HTTP_REQUEST_SCHEME, request.getScheme());
        requestContainer.setAttribute(HTTP_REQUEST_SERVER_NAME, request.getServerName());
        requestContainer.setAttribute(HTTP_REQUEST_SERVER_PORT, String.valueOf(request
                .getServerPort()));
        requestContainer.setAttribute(HTTP_REQUEST_SERVLET_PATH, request.getServletPath());
        if (request.getUserPrincipal() != null)
            requestContainer.setAttribute(HTTP_REQUEST_USER_PRINCIPAL, request.getUserPrincipal());
        requestContainer.setAttribute(HTTP_REQUEST_REQUESTED_SESSION_ID_FROM_COOKIE, String
                .valueOf(request.isRequestedSessionIdFromCookie()));
        requestContainer.setAttribute(HTTP_REQUEST_REQUESTED_SESSION_ID_FROM_URL, String
                .valueOf(request.isRequestedSessionIdFromURL()));
        requestContainer.setAttribute(HTTP_REQUEST_REQUESTED_SESSION_ID_VALID, String
                .valueOf(request.isRequestedSessionIdValid()));
        requestContainer.setAttribute(HTTP_REQUEST_SECURE, String.valueOf(request.isSecure()));
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            requestContainer.setAttribute(headerName, headerValue);
        } // while (headerNames.hasMoreElements())
        requestContainer.setAttribute(HTTP_SESSION_ID, request.getSession().getId());
        requestContainer.setAttribute(Constants.HTTP_IS_XML_REQUEST, "FALSE");
    } // private void setHttpRequestData(HttpServletRequest request,

    // RequestContainer requestContainer)

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {
        super.init();

        String serializeSessionStr = (String) ConfigSingleton.getInstance().getAttribute(
                SERIALIZE_SESSION_ATTRIBUTE);
        if ((serializeSessionStr != null) && (serializeSessionStr.equalsIgnoreCase("TRUE"))) {
            serializeSession = true;
        }
    }

} // public class ActionServlet extends HttpServlet
