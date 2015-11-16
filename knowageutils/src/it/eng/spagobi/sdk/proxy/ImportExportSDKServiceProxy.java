/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.proxy;

import it.eng.spagobi.sdk.callbacks.ClientCredentialsHolder;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
import it.eng.spagobi.sdk.importexport.bo.SDKFile;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.apache.axis.client.Stub;
import org.apache.ws.security.handler.WSHandlerConstants;

public class ImportExportSDKServiceProxy extends AbstractSDKServiceProxy implements it.eng.spagobi.sdk.importexport.stub.ImportExportSDKService {
  private String _endpoint = null;
  private it.eng.spagobi.sdk.importexport.stub.ImportExportSDKService importExportSDKService = null;
  private ClientCredentialsHolder cch = null;
  
  public ImportExportSDKServiceProxy(String user, String pwd) {
	cch = new ClientCredentialsHolder(user, pwd);
	_initImportExportSDKServiceProxy();
  }
  
  private void _initImportExportSDKServiceProxy() {
    try {
		it.eng.spagobi.sdk.importexport.stub.ImportExportSDKServiceServiceLocator locator = new it.eng.spagobi.sdk.importexport.stub.ImportExportSDKServiceServiceLocator();
		Remote remote = locator.getPort(it.eng.spagobi.sdk.importexport.stub.ImportExportSDKService.class);
        Stub axisPort = (Stub) remote;
        axisPort._setProperty(WSHandlerConstants.USER, cch.getUsername());
        axisPort._setProperty(WSHandlerConstants.PW_CALLBACK_REF, cch);
        //axisPort.setTimeout(30000); //used in SpagoBIStudio

        importExportSDKService = (it.eng.spagobi.sdk.importexport.stub.ImportExportSDKService) axisPort;
      if (importExportSDKService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)importExportSDKService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)importExportSDKService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (importExportSDKService != null)
      ((javax.xml.rpc.Stub)importExportSDKService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }

  public SDKFile importDocuments(SDKFile importExportFile, SDKFile associationsSDKFile, boolean override)
			throws RemoteException, NotAllowedOperationException {
	 if (importExportSDKService == null)
	    	_initImportExportSDKServiceProxy();
	 return importExportSDKService.importDocuments(importExportFile, associationsSDKFile, override);
  }
  

}