/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
