/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.proxy;

import java.rmi.Remote;

import org.apache.axis.client.Stub;
import org.apache.ws.security.handler.WSHandlerConstants;

import it.eng.spagobi.sdk.callbacks.ClientCredentialsHolder;
import it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter;

public class DataSetsSDKServiceProxy extends AbstractSDKServiceProxy implements it.eng.spagobi.sdk.datasets.stub.DataSetsSDKService {
  private String _endpoint = null;
  private it.eng.spagobi.sdk.datasets.stub.DataSetsSDKService dataSetsSDKService = null;
  private ClientCredentialsHolder cch = null;
  
  public DataSetsSDKServiceProxy(String user, String pwd) {
	cch = new ClientCredentialsHolder(user, pwd);
    _initDataSetsSDKServiceProxy();
  }
  
  private void _initDataSetsSDKServiceProxy() {
    try {
		it.eng.spagobi.sdk.datasets.stub.DataSetsSDKServiceServiceLocator locator = new it.eng.spagobi.sdk.datasets.stub.DataSetsSDKServiceServiceLocator();
		Remote remote = locator.getPort(it.eng.spagobi.sdk.datasets.stub.DataSetsSDKService.class);
        Stub axisPort = (Stub) remote;
        axisPort._setProperty(WSHandlerConstants.USER, cch.getUsername());
        axisPort._setProperty(WSHandlerConstants.PW_CALLBACK_REF, cch);
        //axisPort.setTimeout(30000); //used in SpagoBIStudio

        dataSetsSDKService = (it.eng.spagobi.sdk.datasets.stub.DataSetsSDKService) axisPort;
      if (dataSetsSDKService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)dataSetsSDKService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)dataSetsSDKService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (dataSetsSDKService != null)
      ((javax.xml.rpc.Stub)dataSetsSDKService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public it.eng.spagobi.sdk.datasets.stub.DataSetsSDKService getDataSetsSDKService() {
    if (dataSetsSDKService == null)
      _initDataSetsSDKServiceProxy();
    return dataSetsSDKService;
  }
  
  public it.eng.spagobi.sdk.datasets.bo.SDKDataSet[] getDataSets() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException{
    if (dataSetsSDKService == null)
      _initDataSetsSDKServiceProxy();
    return dataSetsSDKService.getDataSets();
  }
  
  public it.eng.spagobi.sdk.datasets.bo.SDKDataSet getDataSet(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException{
    if (dataSetsSDKService == null)
      _initDataSetsSDKServiceProxy();
    return dataSetsSDKService.getDataSet(in0);
  }
  
  public it.eng.spagobi.sdk.datasets.bo.SDKDataStoreMetadata getDataStoreMetadata(it.eng.spagobi.sdk.datasets.bo.SDKDataSet in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.InvalidParameterValue, it.eng.spagobi.sdk.exceptions.MissingParameterValue, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException{
    if (dataSetsSDKService == null)
      _initDataSetsSDKServiceProxy();
    return dataSetsSDKService.getDataStoreMetadata(in0);
  }
  
  public java.lang.Integer saveDataset(it.eng.spagobi.sdk.datasets.bo.SDKDataSet in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException{
	    if (dataSetsSDKService == null)
	      _initDataSetsSDKServiceProxy();
	    return dataSetsSDKService.saveDataset(in0);
  }

  public java.lang.String executeDataSet(java.lang.String in0, SDKDataSetParameter[] params) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException{
	    if (dataSetsSDKService == null)
	      _initDataSetsSDKServiceProxy();
	    return dataSetsSDKService.executeDataSet(in0,params);
}
}