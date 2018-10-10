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

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class QbeEngineEditDatasetStartAction extends QbeEngineStartAction {	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7048646075248909379L;

	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(QbeEngineEditDatasetStartAction.class);
        
    private static final String INPUT_DATA_SET_LABEL = "dataset_label";
    
    private IDataSet datasetToEdit = null;
    


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


}