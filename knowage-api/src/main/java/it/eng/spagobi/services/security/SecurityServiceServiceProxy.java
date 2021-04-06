package it.eng.spagobi.services.security;

public class SecurityServiceServiceProxy implements it.eng.spagobi.services.security.SecurityServiceService {
  private String _endpoint = null;
  private it.eng.spagobi.services.security.SecurityServiceService securityServiceService = null;
  
  public SecurityServiceServiceProxy() {
    _initSecurityServiceServiceProxy();
  }
  
  public SecurityServiceServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initSecurityServiceServiceProxy();
  }
  
  private void _initSecurityServiceServiceProxy() {
    try {
      securityServiceService = (new it.eng.spagobi.services.security.SecurityServiceLocator()).getSecurityServicePort();
      if (securityServiceService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)securityServiceService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)securityServiceService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (securityServiceService != null)
      ((javax.xml.rpc.Stub)securityServiceService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public it.eng.spagobi.services.security.SecurityServiceService getSecurityServiceService() {
    if (securityServiceService == null)
      _initSecurityServiceServiceProxy();
    return securityServiceService;
  }
  
  public boolean isAuthorized(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2, java.lang.String arg3) throws java.rmi.RemoteException{
    if (securityServiceService == null)
      _initSecurityServiceServiceProxy();
    return securityServiceService.isAuthorized(arg0, arg1, arg2, arg3);
  }
  
  public it.eng.spagobi.services.security.SpagoBIUserProfile getUserProfile(java.lang.String arg0, java.lang.String arg1) throws java.rmi.RemoteException{
    if (securityServiceService == null)
      _initSecurityServiceServiceProxy();
    return securityServiceService.getUserProfile(arg0, arg1);
  }
  
  public boolean checkAuthorization(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2) throws java.rmi.RemoteException{
    if (securityServiceService == null)
      _initSecurityServiceServiceProxy();
    return securityServiceService.checkAuthorization(arg0, arg1, arg2);
  }
  
  
}