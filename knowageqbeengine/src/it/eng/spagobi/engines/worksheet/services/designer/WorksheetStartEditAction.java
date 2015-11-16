/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.services.designer;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.presentation.DynamicPublisher;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.engines.worksheet.WorksheetEngineInstance;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;

import org.apache.log4j.Logger;

/**
 * @authors Davide Zerbetto (davide.zerbetto@eng.it), Alberto Ghedin (alberto.ghedin@eng.it)
 */
public class WorksheetStartEditAction extends AbstractQbeEngineAction {	

	private static final long serialVersionUID = 6272194014941617286L;

	public static final String ENGINE_INSTANCE = EngineConstants.ENGINE_INSTANCE;
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(WorksheetStartEditAction.class);
    
    public void service(SourceBean serviceRequest, SourceBean serviceResponse) {
    	QbeEngineInstance qbeEngineInstance = null;
    	
    	logger.debug("IN");
       
    	try {
			super.service(serviceRequest, serviceResponse);
			
			WorksheetEngineInstance worksheetEngineInstance = (WorksheetEngineInstance) getAttributeFromSession(WorksheetEngineInstance.class.getName());
			setAttribute(WorksheetEngineInstance.class.getName(), worksheetEngineInstance);
					
			qbeEngineInstance = getEngineInstance();
					
			//publisher for the qbe edit
			String publisherName = "WORKSHEET_START_EDIT_ACTION_DATASET_PUBLISHER";
			
			if (qbeEngineInstance!= null){
				publisherName = "WORKSHEET_START_EDIT_ACTION_QBE_PUBLISHER";
				setAttribute(ENGINE_INSTANCE, qbeEngineInstance);
				if (qbeEngineInstance.getFormState()!=null) {
					//publisher for the smart filter edit
					publisherName = "WORKSHEET_START_EDIT_ACTION_FORM_PUBLISHER";
					serviceRequest.setAttribute("MODALITY", "WORKSHEET_EDIT");
				}	
			}
			
			serviceResponse.setAttribute(DynamicPublisher.PUBLISHER_NAME, publisherName);
			
		} catch(Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}	
		
	}
    
}