/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */                                                                                                                                            
package it.eng.spagobi.behaviouralmodel.analyticaldriver.presentation;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorCategory;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.presentation.PublisherDispatcherIFace;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

/**
 * Publishes the results of a detail request for a LOV value into the correct 
 * jsp page according to what contained into request. If Any errors occurred during the 
 * execution of the <code>DetailModalitiesValueModule</code> class, the publisher
 * is able to call the error page with the error message caught before and put into 
 * the error handler. If the input information don't fall into any of the cases declared,
 * another error is generated. 
 */
public class DetailModalitiesValuePublisher implements PublisherDispatcherIFace {

	private SourceBean detailMR 			= null;
	private SourceBean listTestLovMR 	= null;
	
	public static final String DETAIL_MODALITIES_VALUE_MODULE = "DetailModalitiesValueModule";
	public static final String LIST_TEST_LOV_MODULE = "ListTestLovModule";

	/**
	 * Gets the module response.
	 * 
	 * @param responseContainer the response container
	 * @param moduleName the module name
	 * 
	 * @return the module response
	 */
	public SourceBean getModuleResponse(ResponseContainer responseContainer, String moduleName) {
		return (SourceBean) responseContainer.getServiceResponse().getAttribute(moduleName);
	}
	
	/**
	 * Gets the module responses.
	 * 
	 * @param responseContainer the response container
	 * 
	 * @return the module responses
	 */
	public void getModuleResponses(ResponseContainer responseContainer) {
		detailMR = getModuleResponse(responseContainer, DETAIL_MODALITIES_VALUE_MODULE);
		listTestLovMR = getModuleResponse(responseContainer, LIST_TEST_LOV_MODULE);
	}
	
	private boolean noModuledResponse() {
		return (detailMR == null && listTestLovMR == null);
	}
	
	private String getErrorPublisherName() {
		return "error";
	}
	
	private String getTestErrorPublisherName() {
		if (listTestLovMR != null && isTestExecuted(listTestLovMR)) 
			return "detailLovTestResult";
		else
			return getErrorPublisherName();
	}
	
	private String getModuleDefaultPublisherName() {
		if (detailMR != null) {
			return new String("detailModalitiesValue");
		} else if (listTestLovMR != null) {
			return "detailLovTestResult";
		}
		return getErrorPublisherName();
	}
	
	private boolean isTestExecuted(SourceBean moduleResponse){
		return ("yes".equalsIgnoreCase((String) moduleResponse.getAttribute("testExecuted")));
	}
	
	private Object getAttributeFromModuleResponse(SourceBean moduleResponse, String attributeName) {
		return ( (moduleResponse == null)? null: moduleResponse.getAttribute(attributeName));
	}
	
	private boolean isAttrbuteDefinedInModuleResponse(SourceBean moduleResponse, String attributeName) {
		return (getAttributeFromModuleResponse(moduleResponse, attributeName) != null);
	}
	
	private boolean isLoop() {
		return isAttrbuteDefinedInModuleResponse(detailMR, "loopback");
	}
	
	private void notifyError(EMFErrorHandler errorHandler, String message) {
		SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, 
	            "DetailModalitiesValuePublisher", 
	            "getPublisherName", 
	            message);
		EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 10 );
		errorHandler.addError(error);
	}
	
	/**
	 * Checks if is test executed succesfully.
	 * 
	 * @param testModuleResponse the test module response
	 * 
	 * @return true, if is test executed succesfully
	 */
	public boolean isTestExecutedSuccesfully(SourceBean testModuleResponse) {		
		return (isAttrbuteDefinedInModuleResponse(testModuleResponse, "testExecuted"));
	}
	
	
	/**
	 * Given the request at input, gets the name of the reference publisher,driving
	 * the execution into the correct jsp page, or jsp error page, if any error occurred.
	 * 
	 * @param requestContainer The object containing all request information
	 * @param responseContainer The object containing all response information
	 * 
	 * @return A string representing the name of the correct publisher, which will
	 * call the correct jsp reference.
	 */
	public String getPublisherName(RequestContainer requestContainer, ResponseContainer responseContainer) {

		// recover error handler 
		EMFErrorHandler errorHandler = responseContainer.getErrorHandler();
        // recover all the module responses
		getModuleResponses(responseContainer);	
		// if there are no module response return error publisher
		if (noModuledResponse()) {
			notifyError(errorHandler, "Module response is null");
			return getErrorPublisherName();
		}
		// if there are some errors into the errorHandler, return the name for the errors publisher 
		if(!errorHandler.isOKByCategory(EMFErrorCategory.USER_ERROR) || !errorHandler.isOKByCategory(EMFErrorCategory.INTERNAL_ERROR)) {
			return  getTestErrorPublisherName();
		}
		// check if the execution flow is after a test request		
        boolean afterTest = false;
        Object testExecuted = getAttributeFromModuleResponse(listTestLovMR, "testExecuted");
        if(testExecuted != null) {
	    		afterTest = true;
        }
        
		// check if the execution flow is after a delete request		
        boolean afterDelete = false;
        Object afterDeleteObj = getAttributeFromModuleResponse(detailMR, "afterDeleteLoop");
        if(afterDeleteObj != null) {
        	afterDelete = true;
        }
        
        // check if the request want to do the test but he must fill profile attributes
        boolean fillProfAttr = false;
        Object profAttToFillList = getAttributeFromModuleResponse(detailMR, SpagoBIConstants.PROFILE_ATTRIBUTES_TO_FILL);
        if(profAttToFillList != null) {
        	fillProfAttr = true;
        }
		// if there are errors and they are only validation errors return the name for the detail publisher
		if(!errorHandler.isOK()) {
			if(GeneralUtilities.isErrorHandlerContainingOnlyValidationError(errorHandler)) {
				if(afterTest) {
					return "detailLovTestResult";
				} else {
					return getModuleDefaultPublisherName();
				}
			}
		}
        
        // switch to correct publisher
		if (isLoop()) {
			return new String("detailModalitiesValueLoop");
		}else if (afterDelete) {
			return new String("deleteModalitiesValueLoop");
		} else if (afterTest) {
			return new String("detailLovTestResult");
		} else if(fillProfAttr) {
			return new String("detailLovFillProfileAttributes");
		} else {
			return new String("detailModalitiesValue");
		}

	}

}