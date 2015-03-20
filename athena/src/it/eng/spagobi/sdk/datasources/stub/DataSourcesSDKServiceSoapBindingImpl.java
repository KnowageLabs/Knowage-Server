/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.sdk.datasources.stub;

import it.eng.spagobi.sdk.datasets.impl.DataSetsSDKServiceImpl;
import it.eng.spagobi.sdk.datasources.impl.DataSourcesSDKServiceImpl;

public class DataSourcesSDKServiceSoapBindingImpl implements it.eng.spagobi.sdk.datasources.stub.DataSourcesSDKService{
    public it.eng.spagobi.sdk.datasources.bo.SDKDataSource getDataSource(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	DataSourcesSDKServiceImpl impl = new DataSourcesSDKServiceImpl();
    	return impl.getDataSource(in0);
    }

    public it.eng.spagobi.sdk.datasources.bo.SDKDataSource[] getDataSources() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	DataSourcesSDKServiceImpl impl = new DataSourcesSDKServiceImpl();
    	return impl.getDataSources();
    }

}
