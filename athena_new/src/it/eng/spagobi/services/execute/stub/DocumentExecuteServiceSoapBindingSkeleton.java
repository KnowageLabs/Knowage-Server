/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.services.execute.stub;

public class DocumentExecuteServiceSoapBindingSkeleton implements it.eng.spagobi.services.execute.stub.DocumentExecuteService, org.apache.axis.wsdl.Skeleton {
    private it.eng.spagobi.services.execute.stub.DocumentExecuteService impl;
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
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"), java.util.HashMap.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("executeChart", _params, new javax.xml.namespace.QName("", "executeChartReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "base64Binary"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobiexecute", "executeChart"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("executeChart") == null) {
            _myOperations.put("executeChart", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("executeChart")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getKpiValueXML", _params, new javax.xml.namespace.QName("", "getKpiValueXMLReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobiexecute", "getKpiValueXML"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getKpiValueXML") == null) {
            _myOperations.put("getKpiValueXML", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getKpiValueXML")).add(_oper);
    }

    public DocumentExecuteServiceSoapBindingSkeleton() {
        this.impl = new it.eng.spagobi.services.execute.stub.DocumentExecuteServiceSoapBindingImpl();
    }

    public DocumentExecuteServiceSoapBindingSkeleton(it.eng.spagobi.services.execute.stub.DocumentExecuteService impl) {
        this.impl = impl;
    }
    public byte[] executeChart(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.util.HashMap in3) throws java.rmi.RemoteException
    {
        byte[] ret = impl.executeChart(in0, in1, in2, in3);
        return ret;
    }

    public java.lang.String getKpiValueXML(java.lang.String in0, java.lang.String in1, java.lang.Integer in2) throws java.rmi.RemoteException
    {
        java.lang.String ret = impl.getKpiValueXML(in0, in1, in2);
        return ret;
    }

}
