package it.eng.spagobi.sdk.proxy;

import java.rmi.Remote;

import org.apache.axis.client.Stub;
import org.apache.ws.security.handler.WSHandlerConstants;

import it.eng.spagobi.sdk.callbacks.ClientCredentialsHolder;
import it.eng.spagobi.sdk.domains.stub.DomainsService;

public class DomainsServiceProxy extends AbstractSDKServiceProxy implements DomainsService {
  private String _endpoint = null;
  private DomainsService domainsService = null;
  private ClientCredentialsHolder cch = null;
  
  
  public DomainsServiceProxy(String user, String pwd) {
		cch = new ClientCredentialsHolder(user, pwd);
	    _initDomainsServiceProxy();
	  }
  
  public DomainsServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initDomainsServiceProxy();
  }
  
  private void _initDomainsServiceProxy() {
    try {
		it.eng.spagobi.sdk.domains.stub.DomainsServiceServiceLocator locator = new it.eng.spagobi.sdk.domains.stub.DomainsServiceServiceLocator();
		Remote remote = locator.getPort(it.eng.spagobi.sdk.domains.stub.DomainsService.class);
        Stub axisPort = (Stub) remote;
        axisPort._setProperty(WSHandlerConstants.USER, cch.getUsername());
        axisPort._setProperty(WSHandlerConstants.PW_CALLBACK_REF, cch);
      //axisPort.setTimeout(30000); //used in SpagoBIStudio
        domainsService = (it.eng.spagobi.sdk.domains.stub.DomainsService) axisPort;
    	
     // domainsService = (new DomainsServiceServiceLocator()).getDomainsService();
      if (domainsService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)domainsService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)domainsService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (domainsService != null)
      ((javax.xml.rpc.Stub)domainsService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public DomainsService getDomainsService() {
    if (domainsService == null)
      _initDomainsServiceProxy();
    return domainsService;
  }
  
  public boolean insertDomain(it.eng.spagobi.sdk.domains.bo.SDKDomain in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException{
    if (domainsService == null)
      _initDomainsServiceProxy();
    return domainsService.insertDomain(in0);
  }
  
  public boolean updateDomain(it.eng.spagobi.sdk.domains.bo.SDKDomain in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException{
    if (domainsService == null)
      _initDomainsServiceProxy();
    return domainsService.updateDomain(in0);
  }
  
  public it.eng.spagobi.sdk.domains.bo.SDKDomain getDomainById(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException{
    if (domainsService == null)
      _initDomainsServiceProxy();
    return domainsService.getDomainById(in0);
  }
  
  public it.eng.spagobi.sdk.domains.bo.SDKDomain getDomainByDomainAndValueCd(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException{
    if (domainsService == null)
      _initDomainsServiceProxy();
    return domainsService.getDomainByDomainAndValueCd(in0, in1);
  }
  
  public it.eng.spagobi.sdk.domains.bo.SDKDomain[] getAllDomains() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException{
    if (domainsService == null)
      _initDomainsServiceProxy();
    return domainsService.getAllDomains();
  }
  
  public it.eng.spagobi.sdk.domains.bo.SDKDomain[] getDomainsListByDomainCd(java.lang.String in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException{
    if (domainsService == null)
      _initDomainsServiceProxy();
    return domainsService.getDomainsListByDomainCd(in0);
  }
  
  
}