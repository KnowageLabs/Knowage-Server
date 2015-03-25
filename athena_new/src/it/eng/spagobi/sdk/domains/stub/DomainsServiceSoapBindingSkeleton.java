/**
 * DomainsServiceSoapBindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.sdk.domains.stub;

public class DomainsServiceSoapBindingSkeleton implements it.eng.spagobi.sdk.domains.stub.DomainsService, org.apache.axis.wsdl.Skeleton {
    private it.eng.spagobi.sdk.domains.stub.DomainsService impl;
    private static java.util.Map _myOperations = new java.util.Hashtable();
    private static java.util.Collection _myOperationsList = new java.util.ArrayList();

    /**
    * Returns List of OperationDesc objects with this name
    */
    public static java.util.List getOperationDescByName(java.lang.String methodName) {
        return (java.util.List)_myOperations.get(methodName);
    }

    /**
    * Returns Collection of OperationDescs
    */
    public static java.util.Collection getOperationDescs() {
        return _myOperationsList;
    }

    static {
        org.apache.axis.description.OperationDesc _oper;
        org.apache.axis.description.FaultDesc _fault;
        org.apache.axis.description.ParameterDesc [] _params;
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.domains.sdk.spagobi.eng.it", "SDKDomain"), it.eng.spagobi.sdk.domains.bo.SDKDomain.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("insertDomain", _params, new javax.xml.namespace.QName("", "insertDomainReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdomains", "insertDomain"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("insertDomain") == null) {
            _myOperations.put("insertDomain", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("insertDomain")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("SDKException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdomains", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.SDKException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "SDKException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.domains.sdk.spagobi.eng.it", "SDKDomain"), it.eng.spagobi.sdk.domains.bo.SDKDomain.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("updateDomain", _params, new javax.xml.namespace.QName("", "updateDomainReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdomains", "updateDomain"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("updateDomain") == null) {
            _myOperations.put("updateDomain", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("updateDomain")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("SDKException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdomains", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.SDKException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "SDKException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDomainById", _params, new javax.xml.namespace.QName("", "getDomainByIdReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.domains.sdk.spagobi.eng.it", "SDKDomain"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdomains", "getDomainById"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDomainById") == null) {
            _myOperations.put("getDomainById", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDomainById")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("SDKException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdomains", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.SDKException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "SDKException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDomainByDomainAndValueCd", _params, new javax.xml.namespace.QName("", "getDomainByDomainAndValueCdReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.domains.sdk.spagobi.eng.it", "SDKDomain"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdomains", "getDomainByDomainAndValueCd"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDomainByDomainAndValueCd") == null) {
            _myOperations.put("getDomainByDomainAndValueCd", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDomainByDomainAndValueCd")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("SDKException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdomains", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.SDKException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "SDKException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
        };
        _oper = new org.apache.axis.description.OperationDesc("getAllDomains", _params, new javax.xml.namespace.QName("", "getAllDomainsReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkdomains", "ArrayOf_tns2_SDKDomain"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdomains", "getAllDomains"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getAllDomains") == null) {
            _myOperations.put("getAllDomains", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getAllDomains")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("SDKException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdomains", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.SDKException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "SDKException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDomainsListByDomainCd", _params, new javax.xml.namespace.QName("", "getDomainsListByDomainCdReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkdomains", "ArrayOf_tns2_SDKDomain"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdomains", "getDomainsListByDomainCd"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDomainsListByDomainCd") == null) {
            _myOperations.put("getDomainsListByDomainCd", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDomainsListByDomainCd")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("SDKException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdomains", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.SDKException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "SDKException"));
        _oper.addFault(_fault);
    }

    public DomainsServiceSoapBindingSkeleton() {
        this.impl = new it.eng.spagobi.sdk.domains.stub.DomainsServiceSoapBindingImpl();
    }

    public DomainsServiceSoapBindingSkeleton(it.eng.spagobi.sdk.domains.stub.DomainsService impl) {
        this.impl = impl;
    }
    public boolean insertDomain(it.eng.spagobi.sdk.domains.bo.SDKDomain in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException
    {
        boolean ret = impl.insertDomain(in0);
        return ret;
    }

    public boolean updateDomain(it.eng.spagobi.sdk.domains.bo.SDKDomain in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException
    {
        boolean ret = impl.updateDomain(in0);
        return ret;
    }

    public it.eng.spagobi.sdk.domains.bo.SDKDomain getDomainById(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException
    {
        it.eng.spagobi.sdk.domains.bo.SDKDomain ret = impl.getDomainById(in0);
        return ret;
    }

    public it.eng.spagobi.sdk.domains.bo.SDKDomain getDomainByDomainAndValueCd(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException
    {
        it.eng.spagobi.sdk.domains.bo.SDKDomain ret = impl.getDomainByDomainAndValueCd(in0, in1);
        return ret;
    }

    public it.eng.spagobi.sdk.domains.bo.SDKDomain[] getAllDomains() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException
    {
        it.eng.spagobi.sdk.domains.bo.SDKDomain[] ret = impl.getAllDomains();
        return ret;
    }

    public it.eng.spagobi.sdk.domains.bo.SDKDomain[] getDomainsListByDomainCd(java.lang.String in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException
    {
        it.eng.spagobi.sdk.domains.bo.SDKDomain[] ret = impl.getDomainsListByDomainCd(in0);
        return ret;
    }

}
