/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.initializers;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;

import org.apache.log4j.Logger;


/**
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */
public class QbeEngineStartActionFromBusinessModel extends QbeEngineStartAction {	
	
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(QbeEngineStartActionFromBusinessModel.class);
    public static final String ENGINE_DATASOURCE_LABEL = "ENGINE_DATASOURCE_LABEL";
        
    private static final String DATA_SOURCE_LABEL = "DATA_SOURCE_LABEL";
    
	 public String getDocumentId() {
		 return null;   	
	 }
    
	 
	 public SourceBean getTemplateAsSourceBean() {
		 String modelName = getAttributeAsString(EngineConstants.MODEL_NAME);
		 try {
			 SourceBean qbeSB = new SourceBean("QBE") ;
			 SourceBean datamartSB = new SourceBean("DATAMART") ;
			 datamartSB.setAttribute("name",modelName);
			 qbeSB.setAttribute(datamartSB);
			 return qbeSB;
		 } catch (SourceBeanException e) {
			 SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(getEngineName(), "Impossible to create a new template for the model "+modelName, e);
			 engineException.setDescription("Impossible to parse template's content:  " + e.getMessage());
			 engineException.addHint("Check if the document's template is a well formed xml file");
			 throw engineException;
		 }		

		
	 }
	 
	 public IDataSource getDataSource() {
		 String dataSourceLabel = getAttributeAsString(DATA_SOURCE_LABEL);
		if(dataSourceLabel==null){
			dataSourceLabel = this.getAttributeAsString( ENGINE_DATASOURCE_LABEL );
		}	
		 IDataSource dataSource = getDataSourceServiceProxy().getDataSourceByLabel(dataSourceLabel);
		 return dataSource;
	 }


	 
}


