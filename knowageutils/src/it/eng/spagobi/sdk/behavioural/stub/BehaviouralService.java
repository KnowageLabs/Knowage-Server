/**
 * BehaviouralService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.sdk.behavioural.stub;

public interface BehaviouralService extends java.rmi.Remote {
    public it.eng.spagobi.sdk.behavioural.bo.SDKAttribute[] getAllAttributes(java.lang.String in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
    public it.eng.spagobi.sdk.behavioural.bo.SDKRole[] getRoles() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
    public it.eng.spagobi.sdk.behavioural.bo.SDKRole[] getRolesByUserId(java.lang.String in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
}
