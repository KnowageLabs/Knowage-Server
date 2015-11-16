/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.datasets;

import it.eng.spagobi.sdk.datasets.bo.SDKDataSet;
import it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter;
import it.eng.spagobi.sdk.datasets.bo.SDKDataStoreMetadata;
import it.eng.spagobi.sdk.exceptions.MissingParameterValue;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
import it.eng.spagobi.sdk.exceptions.InvalidParameterValue;


public interface DataSetsSDKService {
	
	SDKDataSet[] getDataSets() throws NotAllowedOperationException;
	
	SDKDataSet getDataSet(Integer dataSetId) throws NotAllowedOperationException;
	
	SDKDataStoreMetadata getDataStoreMetadata(SDKDataSet sdkDataSet) throws NotAllowedOperationException, MissingParameterValue, InvalidParameterValue;
	
	Integer saveDataset(SDKDataSet sdkDataSet) throws NotAllowedOperationException;
	
	String executeDataSet(String label, SDKDataSetParameter[] params) throws NotAllowedOperationException;
	
}
