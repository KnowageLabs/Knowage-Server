/**
 * SecurityServiceService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package spagobisecurity;

public interface SecurityServiceService extends javax.xml.rpc.Service {
    public java.lang.String getSecurityServiceAddress();

    public spagobisecurity.SecurityService getSecurityService() throws javax.xml.rpc.ServiceException;

    public spagobisecurity.SecurityService getSecurityService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
