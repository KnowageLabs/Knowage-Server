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

package it.eng.spagobi.sdk.datasets.stub;

import it.eng.spagobi.sdk.datasets.impl.DataSetsSDKServiceImpl;

public class DataSetsSDKServiceSoapBindingImpl implements it.eng.spagobi.sdk.datasets.stub.DataSetsSDKService{
    public it.eng.spagobi.sdk.datasets.bo.SDKDataSet[] getDataSets() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	DataSetsSDKServiceImpl impl = new DataSetsSDKServiceImpl();
    	return impl.getDataSets();
    }

    public it.eng.spagobi.sdk.datasets.bo.SDKDataSet getDataSet(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	DataSetsSDKServiceImpl impl = new DataSetsSDKServiceImpl();
    	return impl.getDataSet(in0);
    }

    public it.eng.spagobi.sdk.datasets.bo.SDKDataStoreMetadata getDataStoreMetadata(it.eng.spagobi.sdk.datasets.bo.SDKDataSet in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.InvalidParameterValue, it.eng.spagobi.sdk.exceptions.MissingParameterValue, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	DataSetsSDKServiceImpl impl = new DataSetsSDKServiceImpl();
    	return impl.getDataStoreMetadata(in0);
    }

    public java.lang.Integer saveDataset(it.eng.spagobi.sdk.datasets.bo.SDKDataSet in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	DataSetsSDKServiceImpl impl = new DataSetsSDKServiceImpl();
    	return impl.saveDataset(in0);
    }

    public java.lang.String executeDataSet(java.lang.String in0, it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter[] in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	DataSetsSDKServiceImpl impl = new DataSetsSDKServiceImpl();
    	return impl.executeDataSet(in0,in1);
    }

}
