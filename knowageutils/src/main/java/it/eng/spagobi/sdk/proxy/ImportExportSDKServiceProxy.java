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