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

import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.qbe.template.QbeXMLTemplateParser;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

/**
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class QbeEngineEditDatasetStartAction extends QbeEngineStartAction {	
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(QbeEngineEditDatasetStartAction.class);
        
    private static final String INPUT_DATA_SET_LABEL = "dataset_label";
    
    private IDataSet datasetToEdit = null;
    
	public String getDocumentId() {
		 return null;   	
	}
    
	 
	 public SourceBean getTemplateAsSourceBean() {
		 String modelName = this.getModelName();
		 try {
			 SourceBean qbeSB = new SourceBean(QbeXMLTemplateParser.TAG_ROOT_NORMAL) ;
			 SourceBean datamartSB = new SourceBean(QbeXMLTemplateParser.TAG_DATAMART) ;
			 datamartSB.setAttribute(QbeXMLTemplateParser.PROP_DATAMART_NAME,modelName);
			 qbeSB.setAttribute(datamartSB);
			 return qbeSB;
		 } catch (SourceBeanException e) {
			 SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(getEngineName(), "Impossible to create a new template for the model "+modelName, e);
			 engineException.setDescription("Impossible to parse template's content:  " + e.getMessage());
			 engineException.addHint("Check if the document's template is a well formed xml file");
			 throw engineException;
		 }		
		
	 }
	 
	 private String getModelName() {
		IDataSet toEdit = this.getDataSetToEdit();
		QbeDataSet qbeDataSet = (QbeDataSet) toEdit;
		String datamarts = qbeDataSet.getDatamarts();
		logger.debug("Model name is [" + datamarts + "]");
		return datamarts;
	}


	public IDataSource getDataSource() {
		 IDataSet toEdit = this.getDataSetToEdit();
		 IDataSource dataSource = toEdit.getDataSource();
		 Assert.assertNotNull(dataSource, "Datasource is missing");
		 logger.debug("Datasource's label is [" + dataSource.getLabel() + "]");
		 return dataSource;
	 }


	private IDataSet getDataSetToEdit() {
		if (datasetToEdit == null) {
			String dataSetLabel = this.getAttributeAsString(INPUT_DATA_SET_LABEL);
			logger.debug("Input parameter " + INPUT_DATA_SET_LABEL + " is [" + dataSetLabel + "]");
			Assert.assertTrue(StringUtilities.isNotEmpty(dataSetLabel), "Dataset to edit not specified");
			datasetToEdit = this.getDataSetServiceProxy().getDataSetByLabel(dataSetLabel);
		}
		return datasetToEdit;
	}


	@Override
	public byte[] getAnalysisStateRowData() {
		IDataSet toEdit = this.getDataSetToEdit();
		QbeDataSet qbeDataSet = (QbeDataSet) toEdit;
		String jsonQuery = qbeDataSet.getJsonQuery();
		LogMF.debug(logger, "JSON query is [ {0}]", jsonQuery);
		return jsonQuery.getBytes();
	}

}