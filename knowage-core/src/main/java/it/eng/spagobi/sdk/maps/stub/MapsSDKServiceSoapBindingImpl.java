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

package it.eng.spagobi.sdk.maps.stub;

import it.eng.spagobi.sdk.maps.impl.MapsSDKServiceImpl;

public class MapsSDKServiceSoapBindingImpl implements it.eng.spagobi.sdk.maps.stub.MapsSDKService{
    public it.eng.spagobi.sdk.maps.bo.SDKMap[] getMaps() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	MapsSDKServiceImpl impl = new MapsSDKServiceImpl();
    	return impl.getMaps();
    }

    public it.eng.spagobi.sdk.maps.bo.SDKMap getMapById(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	MapsSDKServiceImpl impl = new MapsSDKServiceImpl();
    	return impl.getMapById(in0);
    }

    public it.eng.spagobi.sdk.maps.bo.SDKFeature[] getMapFeatures(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	MapsSDKServiceImpl impl = new MapsSDKServiceImpl();
    	return impl.getMapFeatures(in0);
    }

    public it.eng.spagobi.sdk.maps.bo.SDKFeature[] getFeatures() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	MapsSDKServiceImpl impl = new MapsSDKServiceImpl();
    	return impl.getFeatures();
    }

    public it.eng.spagobi.sdk.maps.bo.SDKFeature getFeatureById(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	MapsSDKServiceImpl impl = new MapsSDKServiceImpl();
    	return impl.getFeatureById(in0);
    }

}
