/**
 * DomainsService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.sdk.domains.stub;

public interface DomainsService extends java.rmi.Remote {
    public boolean insertDomain(it.eng.spagobi.sdk.domains.bo.SDKDomain in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException;
    public boolean updateDomain(it.eng.spagobi.sdk.domains.bo.SDKDomain in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException;
    public it.eng.spagobi.sdk.domains.bo.SDKDomain getDomainById(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException;
    public it.eng.spagobi.sdk.domains.bo.SDKDomain getDomainByDomainAndValueCd(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException;
    public it.eng.spagobi.sdk.domains.bo.SDKDomain[] getAllDomains() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException;
    public it.eng.spagobi.sdk.domains.bo.SDKDomain[] getDomainsListByDomainCd(java.lang.String in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException;
}
