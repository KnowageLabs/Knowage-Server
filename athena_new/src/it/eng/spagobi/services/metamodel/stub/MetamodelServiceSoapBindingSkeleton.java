/**
 * MetamodelServiceSoapBindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.services.metamodel.stub;

public class MetamodelServiceSoapBindingSkeleton implements it.eng.spagobi.services.metamodel.stub.MetamodelService, org.apache.axis.wsdl.Skeleton {
    private it.eng.spagobi.services.metamodel.stub.MetamodelService impl;
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
        };
        _oper = new org.apache.axis.description.OperationDesc("getMetamodelContentByName", _params, new javax.xml.namespace.QName("", "getMetamodelContentByNameReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "DataHandler"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobimetamodel", "getMetamodelContentByName"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getMetamodelContentByName") == null) {
            _myOperations.put("getMetamodelContentByName", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getMetamodelContentByName")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getMetamodelContentLastModified", _params, new javax.xml.namespace.QName("", "getMetamodelContentLastModifiedReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobimetamodel", "getMetamodelContentLastModified"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getMetamodelContentLastModified") == null) {
            _myOperations.put("getMetamodelContentLastModified", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getMetamodelContentLastModified")).add(_oper);
    }

    public MetamodelServiceSoapBindingSkeleton() {
        this.impl = new it.eng.spagobi.services.metamodel.stub.MetamodelServiceSoapBindingImpl();
    }

    public MetamodelServiceSoapBindingSkeleton(it.eng.spagobi.services.metamodel.stub.MetamodelService impl) {
        this.impl = impl;
    }
    public javax.activation.DataHandler getMetamodelContentByName(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException
    {
        javax.activation.DataHandler ret = impl.getMetamodelContentByName(in0, in1, in2);
        return ret;
    }

    public long getMetamodelContentLastModified(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException
    {
        long ret = impl.getMetamodelContentLastModified(in0, in1, in2);
        return ret;
    }

}
