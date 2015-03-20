/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.sdk.maps.stub;

public class MapsSDKServiceSoapBindingSkeleton implements it.eng.spagobi.sdk.maps.stub.MapsSDKService, org.apache.axis.wsdl.Skeleton {
    private it.eng.spagobi.sdk.maps.stub.MapsSDKService impl;
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
        _oper = new org.apache.axis.description.OperationDesc("getMaps", _params, new javax.xml.namespace.QName("", "getMapsReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkmaps", "ArrayOf_tns2_SDKMap"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkmaps", "getMaps"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getMaps") == null) {
            _myOperations.put("getMaps", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getMaps")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkmaps", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getMapById", _params, new javax.xml.namespace.QName("", "getMapByIdReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.maps.sdk.spagobi.eng.it", "SDKMap"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkmaps", "getMapById"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getMapById") == null) {
            _myOperations.put("getMapById", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getMapById")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkmaps", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getMapFeatures", _params, new javax.xml.namespace.QName("", "getMapFeaturesReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkmaps", "ArrayOf_tns2_SDKFeature"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkmaps", "getMapFeatures"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getMapFeatures") == null) {
            _myOperations.put("getMapFeatures", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getMapFeatures")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkmaps", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
        };
        _oper = new org.apache.axis.description.OperationDesc("getFeatures", _params, new javax.xml.namespace.QName("", "getFeaturesReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkmaps", "ArrayOf_tns2_SDKFeature"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkmaps", "getFeatures"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getFeatures") == null) {
            _myOperations.put("getFeatures", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getFeatures")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkmaps", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getFeatureById", _params, new javax.xml.namespace.QName("", "getFeatureByIdReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.maps.sdk.spagobi.eng.it", "SDKFeature"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkmaps", "getFeatureById"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getFeatureById") == null) {
            _myOperations.put("getFeatureById", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getFeatureById")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkmaps", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
    }

    public MapsSDKServiceSoapBindingSkeleton() {
        this.impl = new it.eng.spagobi.sdk.maps.stub.MapsSDKServiceSoapBindingImpl();
    }

    public MapsSDKServiceSoapBindingSkeleton(it.eng.spagobi.sdk.maps.stub.MapsSDKService impl) {
        this.impl = impl;
    }
    public it.eng.spagobi.sdk.maps.bo.SDKMap[] getMaps() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.maps.bo.SDKMap[] ret = impl.getMaps();
        return ret;
    }

    public it.eng.spagobi.sdk.maps.bo.SDKMap getMapById(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.maps.bo.SDKMap ret = impl.getMapById(in0);
        return ret;
    }

    public it.eng.spagobi.sdk.maps.bo.SDKFeature[] getMapFeatures(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.maps.bo.SDKFeature[] ret = impl.getMapFeatures(in0);
        return ret;
    }

    public it.eng.spagobi.sdk.maps.bo.SDKFeature[] getFeatures() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.maps.bo.SDKFeature[] ret = impl.getFeatures();
        return ret;
    }

    public it.eng.spagobi.sdk.maps.bo.SDKFeature getFeatureById(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.maps.bo.SDKFeature ret = impl.getFeatureById(in0);
        return ret;
    }

}
