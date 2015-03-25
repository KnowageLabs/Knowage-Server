/**
 * SecurityServiceSoapBindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.services.security.stub;

public class SecurityServiceSoapBindingSkeleton implements it.eng.spagobi.services.security.stub.SecurityService, org.apache.axis.wsdl.Skeleton {
    private it.eng.spagobi.services.security.stub.SecurityService impl;
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
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("isAuthorized", _params, new javax.xml.namespace.QName("", "isAuthorizedReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisecurity", "isAuthorized"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("isAuthorized") == null) {
            _myOperations.put("isAuthorized", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("isAuthorized")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("checkAuthorization", _params, new javax.xml.namespace.QName("", "checkAuthorizationReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisecurity", "checkAuthorization"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("checkAuthorization") == null) {
            _myOperations.put("checkAuthorization", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("checkAuthorization")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getUserProfile", _params, new javax.xml.namespace.QName("", "getUserProfileReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.security.services.spagobi.eng.it", "SpagoBIUserProfile"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisecurity", "getUserProfile"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getUserProfile") == null) {
            _myOperations.put("getUserProfile", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getUserProfile")).add(_oper);
    }

    public SecurityServiceSoapBindingSkeleton() {
        this.impl = new it.eng.spagobi.services.security.stub.SecurityServiceSoapBindingImpl();
    }

    public SecurityServiceSoapBindingSkeleton(it.eng.spagobi.services.security.stub.SecurityService impl) {
        this.impl = impl;
    }
    public boolean isAuthorized(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException
    {
        boolean ret = impl.isAuthorized(in0, in1, in2, in3);
        return ret;
    }

    public boolean checkAuthorization(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException
    {
        boolean ret = impl.checkAuthorization(in0, in1, in2);
        return ret;
    }

    public it.eng.spagobi.services.security.bo.SpagoBIUserProfile getUserProfile(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException
    {
        it.eng.spagobi.services.security.bo.SpagoBIUserProfile ret = impl.getUserProfile(in0, in1);
        return ret;
    }

}
