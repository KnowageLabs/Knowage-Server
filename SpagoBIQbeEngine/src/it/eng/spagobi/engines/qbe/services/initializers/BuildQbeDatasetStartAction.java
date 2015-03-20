/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.initializers;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.QbeEngine;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.template.QbeTemplateParseException;
import it.eng.spagobi.engines.qbe.template.QbeXMLTemplateParser;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.AbstractEngineStartAction;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * The Class BuildQbeDatasetStartAction.
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class BuildQbeDatasetStartAction extends AbstractEngineStartAction {	
	
	// INPUT PARAMETERS
	public static final String DATAMART_NAME = "DATAMART_NAME";
	public static final String DATASOURCE_LABEL = "DATASOURCE_LABEL";
	
	
	// OUTPUT PARAMETERS
	public static final String LANGUAGE = "LANGUAGE";
	public static final String COUNTRY = "COUNTRY";
	public static final String DATAMARTS_NAMES = "DATAMARTS_NAMES";
	
	// SESSION PARAMETRES	
	public static final String ENGINE_INSTANCE = EngineConstants.ENGINE_INSTANCE;
	
	
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(BuildQbeDatasetStartAction.class);
    
    public static final String ENGINE_NAME = "SpagoBIQbeEngine";
		
    public void service(SourceBean serviceRequest, SourceBean serviceResponse) {
    	QbeEngineInstance qbeEngineInstance = null;
    	Locale locale;
    	Map env;
    	
    	
    	logger.debug("IN");
       
    	try {
    		setEngineName(ENGINE_NAME);
			super.service(serviceRequest, serviceResponse);
			
			logger.debug("User Id: " + getUserId());
			checkUser();
			
			env = getEnv();

			
			String datamartName = this.getAttributeAsString(DATAMART_NAME);
			logger.debug("Datamart's name: " + datamartName);
			
			//checkIfDatamartExists(datamartName);
			
			SourceBean template = buildTemplate(datamartName);
			
			logger.debug("Creating engine instance ...");
			try {
				qbeEngineInstance = QbeEngine.createInstance( template, env );
			} catch(Throwable t) {
				SpagoBIEngineStartupException serviceException;
				String msg = "Impossible to create engine instance for datamart [" + datamartName + "].";
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
			
			setAttributeInSession( ENGINE_INSTANCE, qbeEngineInstance);		
			setAttribute(ENGINE_INSTANCE, qbeEngineInstance);
			
			locale = (Locale) env.get(EngineConstants.ENV_LOCALE);
			setAttribute(LANGUAGE, locale.getLanguage());
			setAttribute(COUNTRY, locale.getCountry());
			
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

	/**
     * Checks if the user is able to build dataset
     */
    private void checkUser() {
    	logger.debug("IN");
    	try {
    		UserProfile profile = this.getUserProfile();
    		if (!profile.isAbleToExecuteAction("DatasetManagement")) {
    			throw new SecurityException("User [" + 
    					"unique identifier: " + profile.getUserUniqueIdentifier() + 
    					"user id : " + profile.getUserId() + 
    					"name: " + profile.getUserName() + 
    					"] cannot build dataset!!");
    		}
    	} catch (Throwable t) {
    		throw new SpagoBIRuntimeException("Cannot verify if user is able to build dataset", t);
    	} finally {
    		logger.debug("OUT");
    	}
	}

	private void checkIfDatamartExists(String datamartName) {
    	logger.debug("IN: datamartName = " + datamartName);
    	try {
    		boolean datamartExists = false;
    		File datamartsDir = QbeEngineConfig.getInstance().getQbeDataMartDir();
    		File datamartDir = new File(datamartsDir, datamartName);
    		datamartExists = datamartDir.exists() && datamartDir.isDirectory();
    		if (!datamartExists) {
    			
    			throw new SpagoBIRuntimeException("Datamart " + datamartName + " not found!");
    		}
    	} finally {
    		logger.debug("OUT");
    	}
	}

	private SourceBean buildTemplate(String datamartName) throws SourceBeanException {
		SourceBean template = new SourceBean(QbeXMLTemplateParser.TAG_ROOT_NORMAL);
		SourceBean datamart = new SourceBean(QbeXMLTemplateParser.TAG_DATAMART);
		datamart.setAttribute(QbeXMLTemplateParser.PROP_DATAMART_NAME, datamartName);
		template.setAttribute(datamart);
		return template;
    }
	
	@Override
	public String getDocumentId() {
		return null;
	}
	
	@Override
    public IDataSource getDataSource() {
    	String schema = null;
        String attrname = null;
    
        String datasourceLabel = this.getAttributeAsString(DATASOURCE_LABEL);
        
        IDataSource dataSource = getDataSourceServiceProxy().getDataSourceByLabel( datasourceLabel );  	
        if (dataSource.checkIsMultiSchema()){
            logger.debug("Datasource [" + dataSource.getLabel() + "] is defined on multi schema");
            try {            
                logger.debug("Retriving target schema for datasource [" + dataSource.getLabel() + "]");
                attrname = dataSource.getSchemaAttribute();
                logger.debug("Datasource's schema attribute name is equals to [" + attrname + "]");                                 
                Assert.assertNotNull(attrname, "Datasource's schema attribute name cannot be null in order to retrive the target schema");
                schema = (String)getUserProfile().getUserAttribute(attrname);
                Assert.assertNotNull(schema, "Impossible to retrive the value of attribute [" + attrname + "] form user profile");
                dataSource.setJndi( dataSource.getJndi() + schema);
                logger.debug("Target schema for datasource  [" + dataSource.getLabel() + "] is [" + dataSource.getJndi()+ "]");
            } catch (Throwable t) {
                throw new SpagoBIEngineRuntimeException("Impossible to retrive target schema for datasource [" + dataSource.getLabel() + "]", t);
            }
            logger.debug("Target schema for datasource  [" + dataSource.getLabel() + "] retrieved succesfully"); 
        }            

		return dataSource;
    }

}
