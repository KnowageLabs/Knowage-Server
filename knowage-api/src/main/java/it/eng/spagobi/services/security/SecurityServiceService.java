/**
 * SecurityServiceService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.services.security;

public interface SecurityServiceService extends java.rmi.Remote {
    public boolean isAuthorized(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2, java.lang.String arg3) throws java.rmi.RemoteException;
    public it.eng.spagobi.services.security.SpagoBIUserProfile getUserProfile(java.lang.String arg0, java.lang.String arg1) throws java.rmi.RemoteException;
    public boolean checkAuthorization(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2) throws java.rmi.RemoteException;
}
