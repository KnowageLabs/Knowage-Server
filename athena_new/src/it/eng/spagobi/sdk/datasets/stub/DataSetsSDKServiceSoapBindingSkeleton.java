/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.sdk.datasets.stub;

public class DataSetsSDKServiceSoapBindingSkeleton implements it.eng.spagobi.sdk.datasets.stub.DataSetsSDKService, org.apache.axis.wsdl.Skeleton {
    private it.eng.spagobi.sdk.datasets.stub.DataSetsSDKService impl;
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
        };
        _oper = new org.apache.axis.description.OperationDesc("getDataSets", _params, new javax.xml.namespace.QName("", "getDataSetsReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "ArrayOf_tns2_SDKDataSet"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "getDataSets"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDataSets") == null) {
            _myOperations.put("getDataSets", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDataSets")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDataSet", _params, new javax.xml.namespace.QName("", "getDataSetReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataSet"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "getDataSet"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDataSet") == null) {
            _myOperations.put("getDataSet", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDataSet")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataSet"), it.eng.spagobi.sdk.datasets.bo.SDKDataSet.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDataStoreMetadata", _params, new javax.xml.namespace.QName("", "getDataStoreMetadataReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataStoreMetadata"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "getDataStoreMetadata"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDataStoreMetadata") == null) {
            _myOperations.put("getDataStoreMetadata", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDataStoreMetadata")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidParameterValue");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.InvalidParameterValue");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "InvalidParameterValue"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("MissingParameterValue");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.MissingParameterValue");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "MissingParameterValue"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataSet"), it.eng.spagobi.sdk.datasets.bo.SDKDataSet.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("saveDataset", _params, new javax.xml.namespace.QName("", "saveDatasetReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "saveDataset"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("saveDataset") == null) {
            _myOperations.put("saveDataset", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("saveDataset")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:spagobisdkdatasets", "ArrayOf_tns2_SDKDataSetParameter"), it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("executeDataSet", _params, new javax.xml.namespace.QName("", "executeDataSetReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "executeDataSet"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("executeDataSet") == null) {
            _myOperations.put("executeDataSet", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("executeDataSet")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
    }

    public DataSetsSDKServiceSoapBindingSkeleton() {
        this.impl = new it.eng.spagobi.sdk.datasets.stub.DataSetsSDKServiceSoapBindingImpl();
    }

    public DataSetsSDKServiceSoapBindingSkeleton(it.eng.spagobi.sdk.datasets.stub.DataSetsSDKService impl) {
        this.impl = impl;
    }
    public it.eng.spagobi.sdk.datasets.bo.SDKDataSet[] getDataSets() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.datasets.bo.SDKDataSet[] ret = impl.getDataSets();
        return ret;
    }

    public it.eng.spagobi.sdk.datasets.bo.SDKDataSet getDataSet(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.datasets.bo.SDKDataSet ret = impl.getDataSet(in0);
        return ret;
    }

    public it.eng.spagobi.sdk.datasets.bo.SDKDataStoreMetadata getDataStoreMetadata(it.eng.spagobi.sdk.datasets.bo.SDKDataSet in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.InvalidParameterValue, it.eng.spagobi.sdk.exceptions.MissingParameterValue, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.datasets.bo.SDKDataStoreMetadata ret = impl.getDataStoreMetadata(in0);
        return ret;
    }

    public java.lang.Integer saveDataset(it.eng.spagobi.sdk.datasets.bo.SDKDataSet in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        java.lang.Integer ret = impl.saveDataset(in0);
        return ret;
    }

    public java.lang.String executeDataSet(java.lang.String in0, it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter[] in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        java.lang.String ret = impl.executeDataSet(in0, in1);
        return ret;
    }

}
