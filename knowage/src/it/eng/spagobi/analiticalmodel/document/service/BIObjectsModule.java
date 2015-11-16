/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.functionalitytree.service.TreeObjectsModule;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.container.ContextManager;
import it.eng.spagobi.container.SpagoBISessionContainer;
import it.eng.spagobi.container.strategy.LightNavigatorContextRetrieverStrategy;

import org.apache.log4j.Logger;

/**
 * Presentation page for the BIObjects.
 */

public class BIObjectsModule extends AbstractModule {
	static private Logger logger = Logger.getLogger(BIObjectsModule.class);
	public static final String MODULE_PAGE = "BIObjectsPage";
    public static final String MODALITY = "MODALITY";
    public static final String SINGLE_OBJECT = "SINGLE_OBJECT";
    public static final String FILTER_TREE = "FILTER_TREE";
    public static final String ENTIRE_TREE = "ENTIRE_TREE";
    public static final String LABEL_SINGLE_OBJECT = "LABEL_SINGLE_OBJECT";
    // MPENNINGROTH 25-Jan-2008 add sub object label support
    public static final String LABEL_SUB_OBJECT = "LABEL_SUB_OBJECT";
    public static final String PARAMETERS_SINGLE_OBJECT = "PARAMETERS_SINGLE_OBJECT";
    public static final String TOOLBAR_VISIBLE = "TOOLBAR_VISIBLE";
    public static final String SLIDERS_VISIBLE = "SLIDERS_VISIBLE";
    public static final String PATH_SUBTREE = "PATH_SUBTREE";
    public static final String HEIGHT_AREA = "HEIGHT_AREA";
    public static final String SNAPSHOT_NAME = "SNAPSHOT_NAME";
    public static final String SNAPSHOT_NUMBER = "SNAPSHOT_NUMBER";
	
    ContextManager contextManager = null;
    EMFErrorHandler errorHandler = null;
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		
		try {
			logger.debug("IN");
			errorHandler = getErrorHandler();
			logger.debug("error handler retrived");
			RequestContainer requestContainer = getRequestContainer();
			SessionContainer sessionContainer = requestContainer.getSessionContainer();
			contextManager = new ContextManager(new SpagoBISessionContainer(sessionContainer), 
					new LightNavigatorContextRetrieverStrategy(request));
			logger.debug("sessionContainer and permanentContainer retrived");
            logger.debug("user profile retrived");
            String modality = ChannelUtilities.getPreferenceValue(requestContainer, MODALITY, "");
            logger.debug("using "+modality+" modality");
            if (modality != null) {
                if (modality.equalsIgnoreCase(SINGLE_OBJECT)) {
                	singleObjectModalityHandler(request, response);
                } else if (modality.equalsIgnoreCase(FILTER_TREE)) {
                	String initialPath = ChannelUtilities.getPreferenceValue(requestContainer, TreeObjectsModule.PATH_SUBTREE, "");
                	treeModalityHandler(request, response, initialPath);
                } else {               	
                	treeModalityHandler(request, response, null);
                }
            } else {
            	treeModalityHandler(request, response, null);
            }
		} catch (Exception e) {
			logger.error("Error while processing request of service",e);
			EMFUserError emfu = new EMFUserError(EMFErrorSeverity.ERROR, 101);
			errorHandler.addError(emfu); 
		}
		
	}
	
		
		/**
	 * Set information into response for entering a 
	 * 
	 * @param request
	 *            The Spago Request SourceBean
	 * @param response
	 *            The Spago Response SourceBean
	 * @param initialPath
	 *           initial path
	 */
	private void treeModalityHandler(SourceBean request,SourceBean response,  
			                         String initialPath) throws SourceBeanException {
		String objectsView = null;
		String operation = (String) request.getAttribute(SpagoBIConstants.OPERATION);
		if (operation != null && operation.equals(SpagoBIConstants.FUNCTIONALITIES_OPERATION)) {
			objectsView = SpagoBIConstants.VIEW_OBJECTS_AS_TREE;
		} else {
			objectsView = (String) request.getAttribute(SpagoBIConstants.OBJECTS_VIEW);
			if (objectsView == null) {
				// finds objects view modality from portlet preferences
				objectsView = ChannelUtilities.getPreferenceValue(this.getRequestContainer(), SpagoBIConstants.OBJECTS_VIEW, SpagoBIConstants.VIEW_OBJECTS_AS_TREE);
			}
			// default value in case it is not specified or in case the value is not valid
			if (objectsView == null
					|| (!objectsView
							.equalsIgnoreCase(SpagoBIConstants.VIEW_OBJECTS_AS_LIST) && !objectsView
							.equalsIgnoreCase(SpagoBIConstants.VIEW_OBJECTS_AS_TREE)))
				objectsView = SpagoBIConstants.VIEW_OBJECTS_AS_TREE;
		}

		if (initialPath != null && !initialPath.trim().equals("")) 
			response.setAttribute(TreeObjectsModule.PATH_SUBTREE, initialPath);
		
		response.setAttribute(SpagoBIConstants.OBJECTS_VIEW, objectsView);

	}
		

	/**
	 * Set information into response for the execution of a single object
	 * 
	 * @param request
	 *            The request SourceBean
	 * @param response
	 *            The response SourceBean
	 * @param prefs
	 *            Portlet Preferences
	 */
	private void singleObjectModalityHandler(SourceBean request, SourceBean response) throws Exception {
		logger.debug("enter singleObjectModalityHandler");
		RequestContainer requestContainer = this.getRequestContainer();
		// get from preferences the label of the object
		String label = ChannelUtilities.getPreferenceValue(requestContainer, LABEL_SINGLE_OBJECT, "");
		logger.debug("using object label " + label);
		// if label is not set then throw an exception
		if (label == null || label.trim().equals("")) {
			logger.error("Object's label not set");
        	throw new Exception("Label not set");
        }
		// get from preferences the parameters used by the object during execution
		String parameters = ChannelUtilities.getPreferenceValue(requestContainer, PARAMETERS_SINGLE_OBJECT, "");
		logger.debug("using parameters " + parameters);
		// get from preferences the snapshot name
		String snapName = ChannelUtilities.getPreferenceValue(requestContainer, SNAPSHOT_NAME, "");
		logger.debug("using snapshot name " + snapName);
		
		// get from preferences the snapshot history
		String snapHistStr = ChannelUtilities.getPreferenceValue(requestContainer, SNAPSHOT_NUMBER, "0");
		if (snapHistStr.equals("")) snapHistStr = "0";
		logger.debug("using snapshot history " + snapHistStr);
		
		// MPENNINGROTH 25-Jan-2008 add sub object label support
		String labelSubObject = ChannelUtilities.getPreferenceValue(requestContainer, LABEL_SUB_OBJECT, "");
		logger.debug("using subobject " + labelSubObject);
        
		String displayToolbarStr = ChannelUtilities.getPreferenceValue(requestContainer, TOOLBAR_VISIBLE, "TRUE");
		logger.debug("Display toolbar preference: " + displayToolbarStr);
		
		String displaySlidersStr = ChannelUtilities.getPreferenceValue(requestContainer, SLIDERS_VISIBLE, "TRUE");
		logger.debug("Display sliders preference: " + displaySlidersStr);
		
        // set into request all information for invoking ExecuteBIObjectModule.pageCreationHandler on loop call
		response.setAttribute(ObjectsTreeConstants.OBJECT_LABEL, label);
        if (!parameters.trim().equalsIgnoreCase("")) {
        	response.setAttribute(ObjectsTreeConstants.PARAMETERS, parameters);
        }
        if (!labelSubObject.trim().equalsIgnoreCase("")) {
        	response.setAttribute(SpagoBIConstants.SUBOBJECT_NAME, labelSubObject);
        }
        if (!snapName.trim().equalsIgnoreCase("")) {
        	response.setAttribute(SpagoBIConstants.SNAPSHOT_NAME, snapName);
        }
        if (!snapHistStr.trim().equalsIgnoreCase("")) {
        	response.setAttribute(SpagoBIConstants.SNAPSHOT_HISTORY_NUMBER, snapHistStr);
        }
        if (!displayToolbarStr.trim().equalsIgnoreCase("")) {
        	response.setAttribute(SpagoBIConstants.TOOLBAR_VISIBLE, displayToolbarStr);
        }
        if (!displaySlidersStr.trim().equalsIgnoreCase("")) {
        	response.setAttribute(SpagoBIConstants.SLIDERS_VISIBLE, displaySlidersStr);
        }
        response.setAttribute(ObjectsTreeConstants.MODALITY, SpagoBIConstants.SINGLE_OBJECT_EXECUTION_MODALITY);
        // loop publisher to call ExecuteBIObjectModule.pageCreationHandler
        response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "BIOBJECTSMODULE_LOOP_PUBLISHER");
        logger.debug("OUT");
	}
	
}
