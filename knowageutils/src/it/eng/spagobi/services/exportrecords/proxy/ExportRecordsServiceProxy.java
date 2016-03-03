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