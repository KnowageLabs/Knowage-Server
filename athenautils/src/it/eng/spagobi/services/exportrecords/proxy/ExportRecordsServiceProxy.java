/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.exportrecords.proxy;

import it.eng.spagobi.services.exportrecords.stub.*;

public class ExportRecordsServiceProxy implements ExportRecordsService {
  private String _endpoint = null;
  private ExportRecordsService exportRecordsService = null;
  
  public ExportRecordsServiceProxy() {
    _initExportRecordsServiceProxy();
  }
  
  public ExportRecordsServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initExportRecordsServiceProxy();
  }
  
  private void _initExportRecordsServiceProxy() {
    try {
      exportRecordsService = (new ExportRecordsServiceServiceLocator()).getExportRecordsService();
      if (exportRecordsService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)exportRecordsService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)exportRecordsService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (exportRecordsService != null)
      ((javax.xml.rpc.Stub)exportRecordsService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public ExportRecordsService getExportRecordsService() {
    if (exportRecordsService == null)
      _initExportRecordsServiceProxy();
    return exportRecordsService;
  }
  
  public java.lang.String processRecords(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException{
    if (exportRecordsService == null)
      _initExportRecordsServiceProxy();
    return exportRecordsService.processRecords(in0, in1);
  }
  
  /**
   * Sets the timeout in milliseconds
   * @param timeout the timeout in milliseconds
   */
  public void setTimeout(int timeout) {
	  ExportRecordsServiceSoapBindingStub stub = (ExportRecordsServiceSoapBindingStub) exportRecordsService;
	  stub.setTimeout(timeout);
  }
  
}