/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.sdk.datasets.stub;

public class DataSetsSDKServiceSoapBindingStub extends org.apache.axis.client.Stub implements it.eng.spagobi.sdk.datasets.stub.DataSetsSDKService {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[5];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getDataSets");
        oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "ArrayOf_tns2_SDKDataSet"));
        oper.setReturnClass(it.eng.spagobi.sdk.datasets.bo.SDKDataSet[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getDataSetsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:spagobisdkdatasets", "fault"),
                      "it.eng.spagobi.sdk.exceptions.NotAllowedOperationException",
                      new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getDataSet");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataSet"));
        oper.setReturnClass(it.eng.spagobi.sdk.datasets.bo.SDKDataSet.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getDataSetReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:spagobisdkdatasets", "fault"),
                      "it.eng.spagobi.sdk.exceptions.NotAllowedOperationException",
                      new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"), 
                      true
                     ));
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getDataStoreMetadata");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataSet"), it.eng.spagobi.sdk.datasets.bo.SDKDataSet.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataStoreMetadata"));
        oper.setReturnClass(it.eng.spagobi.sdk.datasets.bo.SDKDataStoreMetadata.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getDataStoreMetadataReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:spagobisdkdatasets", "fault"),
                      "it.eng.spagobi.sdk.exceptions.InvalidParameterValue",
                      new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "InvalidParameterValue"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:spagobisdkdatasets", "fault"),
                      "it.eng.spagobi.sdk.exceptions.MissingParameterValue",
                      new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "MissingParameterValue"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:spagobisdkdatasets", "fault"),
                      "it.eng.spagobi.sdk.exceptions.NotAllowedOperationException",
                      new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"), 
                      true
                     ));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("saveDataset");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataSet"), it.eng.spagobi.sdk.datasets.bo.SDKDataSet.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        oper.setReturnClass(java.lang.Integer.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "saveDatasetReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:spagobisdkdatasets", "fault"),
                      "it.eng.spagobi.sdk.exceptions.NotAllowedOperationException",
                      new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"), 
                      true
                     ));
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("executeDataSet");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:spagobisdkdatasets", "ArrayOf_tns2_SDKDataSetParameter"), it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "executeDataSetReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("urn:spagobisdkdatasets", "fault"),
                      "it.eng.spagobi.sdk.exceptions.NotAllowedOperationException",
                      new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"), 
                      true
                     ));
        _operations[4] = oper;

    }

    public DataSetsSDKServiceSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public DataSetsSDKServiceSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public DataSetsSDKServiceSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataSet");
            cachedSerQNames.add(qName);
            cls = it.eng.spagobi.sdk.datasets.bo.SDKDataSet.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataSetParameter");
            cachedSerQNames.add(qName);
            cls = it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataStoreFieldMetadata");
            cachedSerQNames.add(qName);
            cls = it.eng.spagobi.sdk.datasets.bo.SDKDataStoreFieldMetadata.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataStoreMetadata");
            cachedSerQNames.add(qName);
            cls = it.eng.spagobi.sdk.datasets.bo.SDKDataStoreMetadata.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "InvalidParameterValue");
            cachedSerQNames.add(qName);
            cls = it.eng.spagobi.sdk.exceptions.InvalidParameterValue.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "MissingParameterValue");
            cachedSerQNames.add(qName);
            cls = it.eng.spagobi.sdk.exceptions.MissingParameterValue.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException");
            cachedSerQNames.add(qName);
            cls = it.eng.spagobi.sdk.exceptions.NotAllowedOperationException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:spagobisdkdatasets", "ArrayOf_soapenc_string");
            cachedSerQNames.add(qName);
            cls = java.lang.String[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:spagobisdkdatasets", "ArrayOf_tns2_SDKDataSet");
            cachedSerQNames.add(qName);
            cls = it.eng.spagobi.sdk.datasets.bo.SDKDataSet[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataSet");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:spagobisdkdatasets", "ArrayOf_tns2_SDKDataSetParameter");
            cachedSerQNames.add(qName);
            cls = it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataSetParameter");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:spagobisdkdatasets", "ArrayOf_tns2_SDKDataStoreFieldMetadata");
            cachedSerQNames.add(qName);
            cls = it.eng.spagobi.sdk.datasets.bo.SDKDataStoreFieldMetadata[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataStoreFieldMetadata");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
                    _call.setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public it.eng.spagobi.sdk.datasets.bo.SDKDataSet[] getDataSets() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "getDataSets"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (it.eng.spagobi.sdk.datasets.bo.SDKDataSet[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (it.eng.spagobi.sdk.datasets.bo.SDKDataSet[]) org.apache.axis.utils.JavaUtils.convert(_resp, it.eng.spagobi.sdk.datasets.bo.SDKDataSet[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof it.eng.spagobi.sdk.exceptions.NotAllowedOperationException) {
              throw (it.eng.spagobi.sdk.exceptions.NotAllowedOperationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public it.eng.spagobi.sdk.datasets.bo.SDKDataSet getDataSet(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "getDataSet"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (it.eng.spagobi.sdk.datasets.bo.SDKDataSet) _resp;
            } catch (java.lang.Exception _exception) {
                return (it.eng.spagobi.sdk.datasets.bo.SDKDataSet) org.apache.axis.utils.JavaUtils.convert(_resp, it.eng.spagobi.sdk.datasets.bo.SDKDataSet.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof it.eng.spagobi.sdk.exceptions.NotAllowedOperationException) {
              throw (it.eng.spagobi.sdk.exceptions.NotAllowedOperationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public it.eng.spagobi.sdk.datasets.bo.SDKDataStoreMetadata getDataStoreMetadata(it.eng.spagobi.sdk.datasets.bo.SDKDataSet in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.InvalidParameterValue, it.eng.spagobi.sdk.exceptions.MissingParameterValue, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "getDataStoreMetadata"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (it.eng.spagobi.sdk.datasets.bo.SDKDataStoreMetadata) _resp;
            } catch (java.lang.Exception _exception) {
                return (it.eng.spagobi.sdk.datasets.bo.SDKDataStoreMetadata) org.apache.axis.utils.JavaUtils.convert(_resp, it.eng.spagobi.sdk.datasets.bo.SDKDataStoreMetadata.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof it.eng.spagobi.sdk.exceptions.InvalidParameterValue) {
              throw (it.eng.spagobi.sdk.exceptions.InvalidParameterValue) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof it.eng.spagobi.sdk.exceptions.MissingParameterValue) {
              throw (it.eng.spagobi.sdk.exceptions.MissingParameterValue) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof it.eng.spagobi.sdk.exceptions.NotAllowedOperationException) {
              throw (it.eng.spagobi.sdk.exceptions.NotAllowedOperationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public java.lang.Integer saveDataset(it.eng.spagobi.sdk.datasets.bo.SDKDataSet in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "saveDataset"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.Integer) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.Integer) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.Integer.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof it.eng.spagobi.sdk.exceptions.NotAllowedOperationException) {
              throw (it.eng.spagobi.sdk.exceptions.NotAllowedOperationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public java.lang.String executeDataSet(java.lang.String in0, it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter[] in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:spagobisdkdatasets", "executeDataSet"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof it.eng.spagobi.sdk.exceptions.NotAllowedOperationException) {
              throw (it.eng.spagobi.sdk.exceptions.NotAllowedOperationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

}
