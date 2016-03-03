/*
* Knowage, Open Source Business Intelligence suite
* Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
* 
* Knowage is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Knowage is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package it.eng.spagobi.engines.qbe.services.initializers;

import it.eng.qbe.query.Query;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.presentation.DynamicPublisher;
import it.eng.spagobi.engines.qbe.FormState;
import it.eng.spagobi.engines.qbe.QbeEngine;
import it.eng.spagobi.engines.qbe.QbeEngineAnalysisState;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.SmartFilterAnalysisState;
import it.eng.spagobi.engines.qbe.template.QbeTemplateParseException;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.AbstractEngineStartAction;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;

import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;


/**
 * The Class QbeEngineStartAction.
 * 
 * @author Andrea Gioia
 */
public class FormEngineStartAction extends AbstractEngineStartAction {	
	
	// INPUT PARAMETERS
	private final static String PARAM_MODALITY = "MODALITY";
	
	// OUTPUT PARAMETERS
	public static final String LANGUAGE = "LANGUAGE";
	public static final String COUNTRY = "COUNTRY";
	
	// SESSION PARAMETRES	
	public static final String ENGINE_INSTANCE = EngineConstants.ENGINE_INSTANCE;
	
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(FormEngineStartAction.class);
    
    public static final String ENGINE_NAME = "SpagoBIFormEngine";
	
    public void service(SourceBean serviceRequest, SourceBean serviceResponse) {
    	QbeEngineInstance qbeEngineInstance = null;
    	QbeEngineAnalysisState analysisState;
    	SmartFilterAnalysisState analysisFormState = null;
    	Locale locale;
    	
    	
    	logger.debug("IN");
       
    	try {
    		setEngineName(ENGINE_NAME);
			super.service(serviceRequest, serviceResponse);
			
			logger.debug("User Id: " + getUserId());
			logger.debug("Audit Id: " + getAuditId());
			logger.debug("Document Id: " + getDocumentId());
			logger.debug("Template: " + getTemplateAsSourceBean());
						
			if(getAuditServiceProxy() != null) {
				logger.debug("Audit enabled: [TRUE]");
				getAuditServiceProxy().notifyServiceStartEvent();
			} else {
				logger.debug("Audit enabled: [FALSE]");
			}
			
			// Add the dataset (if any)
			Map env = addDatasetsToEnv();
			
			logger.debug("Creating engine instance ...");
			try {
				qbeEngineInstance = QbeEngine.createInstance(getTemplateAsSourceBean(), env );
				
				Query query = qbeEngineInstance.getQueryCatalogue().getFirstQuery();
				qbeEngineInstance.setActiveQuery(query);
				
			} catch(Throwable t) {
				SpagoBIEngineStartupException serviceException;
				String msg = "Impossible to create engine instance for document [" + getDocumentId() + "].";
				Throwable rootException = t;
				while(rootException.getCause() != null) {
					rootException = rootException.getCause();
				}
				String str = rootException.getMessage()!=null? rootException.getMessage(): rootException.getClass().getName();
				msg += "\nThe root cause of the error is: " + str;
				serviceException = new SpagoBIEngineStartupException(ENGINE_NAME, msg, t);
				
				if(rootException instanceof QbeTemplateParseException) {
					QbeTemplateParseException e = (QbeTemplateParseException)rootException;
					serviceException.setDescription( e.getDescription());
					serviceException.setHints( e.getHints() );
				} 
				
				throw serviceException;
			}
			logger.debug("Engine instance succesfully created");
		
			qbeEngineInstance.setAnalysisMetadata( getAnalysisMetadata() );
	
			
			// initializes form state, if not already initialized (starting a new form definition)
			FormState formState = qbeEngineInstance.getFormState();
			if (formState == null) {
				logger.debug("Initializing a new form state object...");
				formState = new FormState();
				formState.setConf(new JSONObject());
				qbeEngineInstance.setFormState(formState);
			}
			//Integer subObjectId = getAttributeAsInteger("subobjectId");
			
			
			//get the form values saved (if the user has loaded a subobject)
			analysisFormState = new SmartFilterAnalysisState();
			analysisFormState.load( getAnalysisStateRowData() );
			formState.setFormStateValues(analysisFormState.getFormValues());
			
			//save the map id-->field name
			formState.setIdNameMap();
			
			qbeEngineInstance.getEnv().put("TEMPLATE", getTemplateAsSourceBean());
			String docId = this.getAttributeAsString("formDocumentId");
			if(docId != null) qbeEngineInstance.getEnv().put("DOCUMENT", docId);
			else {
				qbeEngineInstance.getEnv().put("DOCUMENT", this.getDocumentId());
			}
			
			locale = (Locale)qbeEngineInstance.getEnv().get(EngineConstants.ENV_LOCALE);
			
			setAttributeInSession( ENGINE_INSTANCE, qbeEngineInstance);		
			setAttribute(ENGINE_INSTANCE, qbeEngineInstance);
			
			setAttribute(LANGUAGE, locale.getLanguage());
			setAttribute(COUNTRY, locale.getCountry());
			
			String publisherName = "VIEW_FORM_ENGINE_PUBLISHER";
			
			String modality = this.getAttributeAsString(PARAM_MODALITY);
			logger.debug("Input " + PARAM_MODALITY + " parameter is " + modality);
			if (modality != null && modality.trim().equalsIgnoreCase("EDIT")) {
				// edit template
				publisherName = "EDIT_FORM_ENGINE_PUBLISHER";
			}
			
			serviceResponse.setAttribute(DynamicPublisher.PUBLISHER_NAME, publisherName);
			
		} catch (Throwable e) {
			SpagoBIEngineStartupException serviceException = null;
			
			if(e instanceof SpagoBIEngineStartupException) {
				serviceException = (SpagoBIEngineStartupException)e;
			} else {
				Throwable rootException = e;
				while(rootException.getCause() != null) {
					rootException = rootException.getCause();
				}
				String str = rootException.getMessage()!=null? rootException.getMessage(): rootException.getClass().getName();
				String message = "An unpredicted error occurred while executing " + getEngineName() + " service."
								 + "\nThe root cause of the error is: " + str;
				
				serviceException = new SpagoBIEngineStartupException(getEngineName(), message, e);
			}
			
			throw serviceException;
			
			//throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), qbeEngineInstance, e);
		} finally {
			logger.debug("OUT");
		}		

		
	}
    
	public Map getEnv() {
		Map env = super.getEnv();
		
		IDataSource datasource = this.getDataSource();
		if (datasource == null || datasource.checkIsReadOnly()) {
			logger.debug("Getting datasource for writing, since the datasource is not defined or it is read-only");
			IDataSource datasourceForWriting = this.getDataSourceForWriting();
			env.put(EngineConstants.DATASOURCE_FOR_WRITING, datasourceForWriting);
		} else {
			env.put(EngineConstants.DATASOURCE_FOR_WRITING, datasource);
		}

		return env;
	}
	
    public Map addDatasetsToEnv(){
		Map env = getEnv();
		return env;
    }
    
}
