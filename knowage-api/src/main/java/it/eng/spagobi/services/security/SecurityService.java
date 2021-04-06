/**
 * SecurityService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.services.security;

public interface SecurityService extends javax.xml.rpc.Service {
    public java.lang.String getSecurityServicePortAddress();

    public it.eng.spagobi.services.security.SecurityServiceService getSecurityServicePort() throws javax.xml.rpc.ServiceException;

    public it.eng.spagobi.services.security.SecurityServiceService getSecurityServicePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
