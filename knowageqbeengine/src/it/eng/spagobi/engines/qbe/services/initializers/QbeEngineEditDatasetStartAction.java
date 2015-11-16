/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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