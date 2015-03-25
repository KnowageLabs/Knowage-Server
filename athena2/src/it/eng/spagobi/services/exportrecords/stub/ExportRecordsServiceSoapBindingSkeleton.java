/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.services.exportrecords.stub;

public class ExportRecordsServiceSoapBindingSkeleton implements it.eng.spagobi.services.exportrecords.stub.ExportRecordsService, org.apache.axis.wsdl.Skeleton {
    private it.eng.spagobi.services.exportrecords.stub.ExportRecordsService impl;
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
        };
        _oper = new org.apache.axis.description.OperationDesc("processRecords", _params, new javax.xml.namespace.QName("", "processRecordsReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobiexportrecords", "processRecords"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("processRecords") == null) {
            _myOperations.put("processRecords", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("processRecords")).add(_oper);
    }

    public ExportRecordsServiceSoapBindingSkeleton() {
        this.impl = new it.eng.spagobi.services.exportrecords.stub.ExportRecordsServiceSoapBindingImpl();
    }

    public ExportRecordsServiceSoapBindingSkeleton(it.eng.spagobi.services.exportrecords.stub.ExportRecordsService impl) {
        this.impl = impl;
    }
    public java.lang.String processRecords(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException
    {
        java.lang.String ret = impl.processRecords(in0, in1);
        return ret;
    }

}
