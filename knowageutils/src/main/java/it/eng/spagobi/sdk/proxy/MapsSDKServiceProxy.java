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
package it.eng.spagobi.sdk.proxy;

import java.rmi.Remote;

import org.apache.axis.client.Stub;
import org.apache.ws.security.handler.WSHandlerConstants;

import it.eng.spagobi.sdk.callbacks.ClientCredentialsHolder;
import it.eng.spagobi.sdk.maps.stub.MapsSDKService;
import it.eng.spagobi.sdk.maps.stub.MapsSDKServiceServiceLocator;

public class MapsSDKServiceProxy extends AbstractSDKServiceProxy implements MapsSDKService {
  private String _endpoint = null;
  private MapsSDKService mapsSDKService = null;
  private ClientCredentialsHolder cch = null;
  
  public MapsSDKServiceProxy() {
    _initMapsSDKServiceProxy();
  }

  public MapsSDKServiceProxy(String user, String pwd) {
		cch = new ClientCredentialsHolder(user, pwd);
	    _initMapsSDKServiceProxy();
	  }
  
  public MapsSDKServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initMapsSDKServiceProxy();
  }
  
  private void _initMapsSDKServiceProxy() {
    try {
		it.eng.spagobi.sdk.maps.stub.MapsSDKServiceServiceLocator locator = new it.eng.spagobi.sdk.maps.stub.MapsSDKServiceServiceLocator();
		Remote remote = locator.getPort(it.eng.spagobi.sdk.maps.stub.MapsSDKService.class);
        Stub axisPort = (Stub) remote;
        axisPort._setProperty(WSHandlerConstants.USER, cch.getUsername());
        axisPort._setProperty(WSHandlerConstants.PW_CALLBACK_REF, cch);
        //axisPort.setTimeout(30000); //used in SpagoBIStudio

        mapsSDKService = (it.eng.spagobi.sdk.maps.stub.MapsSDKService) axisPort;
    	//mapsSDKService = (new MapsSDKServiceServiceLocator()).getMapsSDKService();
      if (mapsSDKService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)mapsSDKService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)mapsSDKService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (mapsSDKService != null)
      ((javax.xml.rpc.Stub)mapsSDKService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public MapsSDKService getMapsSDKService() {
    if (mapsSDKService == null)
      _initMapsSDKServiceProxy();
    return mapsSDKService;
  }
  
  public it.eng.spagobi.sdk.maps.bo.SDKMap[] getMaps() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException{
    if (mapsSDKService == null)
      _initMapsSDKServiceProxy();
    return mapsSDKService.getMaps();
  }
  
  public it.eng.spagobi.sdk.maps.bo.SDKMap getMapById(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException{
    if (mapsSDKService == null)
      _initMapsSDKServiceProxy();
    return mapsSDKService.getMapById(in0);
  }
  
  public it.eng.spagobi.sdk.maps.bo.SDKFeature[] getMapFeatures(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException{
    if (mapsSDKService == null)
      _initMapsSDKServiceProxy();
    return mapsSDKService.getMapFeatures(in0);
  }
  
  public it.eng.spagobi.sdk.maps.bo.SDKFeature[] getFeatures() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException{
    if (mapsSDKService == null)
      _initMapsSDKServiceProxy();
    return mapsSDKService.getFeatures();
  }
  
  public it.eng.spagobi.sdk.maps.bo.SDKFeature getFeatureById(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException{
    if (mapsSDKService == null)
      _initMapsSDKServiceProxy();
    return mapsSDKService.getFeatureById(in0);
  }
  
  
}