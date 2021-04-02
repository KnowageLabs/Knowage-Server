package spagobisecurity;

public class SecurityServiceProxy implements spagobisecurity.SecurityService {
  private String _endpoint = null;
  private spagobisecurity.SecurityService securityService = null;
  
  public SecurityServiceProxy() {
    _initSecurityServiceProxy();
  }
  
  public SecurityServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initSecurityServiceProxy();
  }
  
  private void _initSecurityServiceProxy() {
    try {
      securityService = (new spagobisecurity.SecurityServiceServiceLocator()).getSecurityService();
      if (securityService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)securityService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)securityService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (securityService != null)
      ((javax.xml.rpc.Stub)securityService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public spagobisecurity.SecurityService getSecurityService() {
    if (securityService == null)
      _initSecurityServiceProxy();
    return securityService;
  }
  
  public boolean isAuthorized(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException{
    if (securityService == null)
      _initSecurityServiceProxy();
    return securityService.isAuthorized(in0, in1, in2, in3);
  }
  
  public boolean checkAuthorization(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException{
    if (securityService == null)
      _initSecurityServiceProxy();
    return securityService.checkAuthorization(in0, in1, in2);
  }
  
  public it.eng.spagobi.services.security.bo.SpagoBIUserProfile getUserProfile(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException{
    if (securityService == null)
      _initSecurityServiceProxy();
    return securityService.getUserProfile(in0, in1);
  }
  
  
}