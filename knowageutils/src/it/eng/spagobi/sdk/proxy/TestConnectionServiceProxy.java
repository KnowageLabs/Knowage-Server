/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.proxy;

import java.rmi.Remote;

import org.apache.axis.client.Stub;
import org.apache.ws.security.handler.WSHandlerConstants;

import it.eng.spagobi.sdk.callbacks.ClientCredentialsHolder;

public class TestConnectionServiceProxy extends AbstractSDKServiceProxy implements it.eng.spagobi.sdk.test.stub.TestConnectionService {
  private String _endpoint = null;
  private it.eng.spagobi.sdk.test.stub.TestConnectionService testConnectionService = null;
  private ClientCredentialsHolder cch = null;
  
	public TestConnectionServiceProxy(String user, String pwd) {
		cch = new ClientCredentialsHolder(user, pwd);
		_initTestConnectionServiceProxy();
	}
  
  private void _initTestConnectionServiceProxy() {
    try {
		it.eng.spagobi.sdk.test.stub.TestConnectionServiceServiceLocator locator = new it.eng.spagobi.sdk.test.stub.TestConnectionServiceServiceLocator();
		Remote remote = locator.getPort(it.eng.spagobi.sdk.test.stub.TestConnectionService.class);
        Stub axisPort = (Stub) remote;
        axisPort._setProperty(WSHandlerConstants.USER, cch.getUsername());
        axisPort._setProperty(WSHandlerConstants.PW_CALLBACK_REF, cch);
        //axisPort.setTimeout(30000); //used in SpagoBIStudio

        testConnectionService = (it.eng.spagobi.sdk.test.stub.TestConnectionService) axisPort;
    	
      if (testConnectionService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)testConnectionService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)testConnectionService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (testConnectionService != null)
      ((javax.xml.rpc.Stub)testConnectionService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public it.eng.spagobi.sdk.test.stub.TestConnectionService getTestConnectionService() {
    if (testConnectionService == null)
      _initTestConnectionServiceProxy();
    return testConnectionService;
  }
  
  public boolean connect() throws java.rmi.RemoteException{
    if (testConnectionService == null)
      _initTestConnectionServiceProxy();
    return testConnectionService.connect();
  }
  
  
}