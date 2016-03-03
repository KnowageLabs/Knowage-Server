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


