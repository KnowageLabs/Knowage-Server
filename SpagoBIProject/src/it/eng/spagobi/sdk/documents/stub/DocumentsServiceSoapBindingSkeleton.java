/**
 * DocumentsServiceSoapBindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.sdk.documents.stub;

public class DocumentsServiceSoapBindingSkeleton implements it.eng.spagobi.sdk.documents.stub.DocumentsService, org.apache.axis.wsdl.Skeleton {
    private it.eng.spagobi.sdk.documents.stub.DocumentsService impl;
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
        _oper = new org.apache.axis.description.OperationDesc("getDocumentsAsList", _params, new javax.xml.namespace.QName("", "getDocumentsAsListReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "ArrayOf_tns2_SDKDocument"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "getDocumentsAsList"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDocumentsAsList") == null) {
            _myOperations.put("getDocumentsAsList", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDocumentsAsList")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDocumentById", _params, new javax.xml.namespace.QName("", "getDocumentByIdReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKDocument"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "getDocumentById"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDocumentById") == null) {
            _myOperations.put("getDocumentById", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDocumentById")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDocumentByLabel", _params, new javax.xml.namespace.QName("", "getDocumentByLabelReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKDocument"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "getDocumentByLabel"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDocumentByLabel") == null) {
            _myOperations.put("getDocumentByLabel", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDocumentByLabel")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDocumentsAsTree", _params, new javax.xml.namespace.QName("", "getDocumentsAsTreeReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKFunctionality"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "getDocumentsAsTree"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDocumentsAsTree") == null) {
            _myOperations.put("getDocumentsAsTree", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDocumentsAsTree")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getCorrectRolesForExecution", _params, new javax.xml.namespace.QName("", "getCorrectRolesForExecutionReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "ArrayOf_soapenc_string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "getCorrectRolesForExecution"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getCorrectRolesForExecution") == null) {
            _myOperations.put("getCorrectRolesForExecution", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getCorrectRolesForExecution")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NonExecutableDocumentException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NonExecutableDocumentException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDocumentParameters", _params, new javax.xml.namespace.QName("", "getDocumentParametersReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "ArrayOf_tns2_SDKDocumentParameter"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "getDocumentParameters"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDocumentParameters") == null) {
            _myOperations.put("getDocumentParameters", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDocumentParameters")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NonExecutableDocumentException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NonExecutableDocumentException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getAdmissibleValues", _params, new javax.xml.namespace.QName("", "getAdmissibleValuesReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "ArrayOf_tns2_SDKDocumentParameterValue"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "getAdmissibleValues"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getAdmissibleValues") == null) {
            _myOperations.put("getAdmissibleValues", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getAdmissibleValues")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NonExecutableDocumentException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NonExecutableDocumentException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDefaultValues", _params, new javax.xml.namespace.QName("", "getDefaultValuesReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "ArrayOf_tns2_SDKDocumentParameterValue"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "getDefaultValues"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDefaultValues") == null) {
            _myOperations.put("getDefaultValues", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDefaultValues")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NonExecutableDocumentException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NonExecutableDocumentException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("downloadTemplate", _params, new javax.xml.namespace.QName("", "downloadTemplateReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKTemplate"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "downloadTemplate"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("downloadTemplate") == null) {
            _myOperations.put("downloadTemplate", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("downloadTemplate")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKTemplate"), it.eng.spagobi.sdk.documents.bo.SDKTemplate.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("uploadTemplate", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "uploadTemplate"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("uploadTemplate") == null) {
            _myOperations.put("uploadTemplate", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("uploadTemplate")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKDocument"), it.eng.spagobi.sdk.documents.bo.SDKDocument.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKTemplate"), it.eng.spagobi.sdk.documents.bo.SDKTemplate.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("saveNewDocument", _params, new javax.xml.namespace.QName("", "saveNewDocumentReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "saveNewDocument"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("saveNewDocument") == null) {
            _myOperations.put("saveNewDocument", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("saveNewDocument")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKDocument"), it.eng.spagobi.sdk.documents.bo.SDKDocument.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:spagobisdkdocuments", "ArrayOf_tns2_SDKDocumentParameter"), it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("executeDocument", _params, new javax.xml.namespace.QName("", "executeDocumentReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKExecutedDocumentContent"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "executeDocument"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("executeDocument") == null) {
            _myOperations.put("executeDocument", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("executeDocument")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NonExecutableDocumentException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NonExecutableDocumentException"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidParameterValue");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.InvalidParameterValue");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "InvalidParameterValue"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("MissingParameterValue");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.MissingParameterValue");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "MissingParameterValue"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKTemplate"), it.eng.spagobi.sdk.documents.bo.SDKTemplate.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKTemplate"), it.eng.spagobi.sdk.documents.bo.SDKTemplate.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("uploadDatamartTemplate", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "uploadDatamartTemplate"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("uploadDatamartTemplate") == null) {
            _myOperations.put("uploadDatamartTemplate", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("uploadDatamartTemplate")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKTemplate"), it.eng.spagobi.sdk.documents.bo.SDKTemplate.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("uploadDatamartModel", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "uploadDatamartModel"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("uploadDatamartModel") == null) {
            _myOperations.put("uploadDatamartModel", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("uploadDatamartModel")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("downloadDatamartFile", _params, new javax.xml.namespace.QName("", "downloadDatamartFileReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKTemplate"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "downloadDatamartFile"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("downloadDatamartFile") == null) {
            _myOperations.put("downloadDatamartFile", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("downloadDatamartFile")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("downloadDatamartModelFiles", _params, new javax.xml.namespace.QName("", "downloadDatamartModelFilesReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKTemplate"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "downloadDatamartModelFiles"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("downloadDatamartModelFiles") == null) {
            _myOperations.put("downloadDatamartModelFiles", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("downloadDatamartModelFiles")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
        };
        _oper = new org.apache.axis.description.OperationDesc("getAllDatamartModels", _params, new javax.xml.namespace.QName("", "getAllDatamartModelsReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "getAllDatamartModels"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getAllDatamartModels") == null) {
            _myOperations.put("getAllDatamartModels", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getAllDatamartModels")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKSchema"), it.eng.spagobi.sdk.documents.bo.SDKSchema.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("uploadMondrianSchema", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "uploadMondrianSchema"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("uploadMondrianSchema") == null) {
            _myOperations.put("uploadMondrianSchema", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("uploadMondrianSchema")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("SDKException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.SDKException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "SDKException"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
    }

    public DocumentsServiceSoapBindingSkeleton() {
        this.impl = new it.eng.spagobi.sdk.documents.stub.DocumentsServiceSoapBindingImpl();
    }

    public DocumentsServiceSoapBindingSkeleton(it.eng.spagobi.sdk.documents.stub.DocumentsService impl) {
        this.impl = impl;
    }
    public it.eng.spagobi.sdk.documents.bo.SDKDocument[] getDocumentsAsList(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException
    {
        it.eng.spagobi.sdk.documents.bo.SDKDocument[] ret = impl.getDocumentsAsList(in0, in1, in2);
        return ret;
    }

    public it.eng.spagobi.sdk.documents.bo.SDKDocument getDocumentById(java.lang.Integer in0) throws java.rmi.RemoteException
    {
        it.eng.spagobi.sdk.documents.bo.SDKDocument ret = impl.getDocumentById(in0);
        return ret;
    }

    public it.eng.spagobi.sdk.documents.bo.SDKDocument getDocumentByLabel(java.lang.String in0) throws java.rmi.RemoteException
    {
        it.eng.spagobi.sdk.documents.bo.SDKDocument ret = impl.getDocumentByLabel(in0);
        return ret;
    }

    public it.eng.spagobi.sdk.documents.bo.SDKFunctionality getDocumentsAsTree(java.lang.String in0) throws java.rmi.RemoteException
    {
        it.eng.spagobi.sdk.documents.bo.SDKFunctionality ret = impl.getDocumentsAsTree(in0);
        return ret;
    }

    public java.lang.String[] getCorrectRolesForExecution(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException
    {
        java.lang.String[] ret = impl.getCorrectRolesForExecution(in0);
        return ret;
    }

    public it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter[] getDocumentParameters(java.lang.Integer in0, java.lang.String in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException
    {
        it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter[] ret = impl.getDocumentParameters(in0, in1);
        return ret;
    }

    public it.eng.spagobi.sdk.documents.bo.SDKDocumentParameterValue[] getAdmissibleValues(java.lang.Integer in0, java.lang.String in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException
    {
        it.eng.spagobi.sdk.documents.bo.SDKDocumentParameterValue[] ret = impl.getAdmissibleValues(in0, in1);
        return ret;
    }

    public it.eng.spagobi.sdk.documents.bo.SDKDocumentParameterValue[] getDefaultValues(java.lang.Integer in0, java.lang.String in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException
    {
        it.eng.spagobi.sdk.documents.bo.SDKDocumentParameterValue[] ret = impl.getDefaultValues(in0, in1);
        return ret;
    }

    public it.eng.spagobi.sdk.documents.bo.SDKTemplate downloadTemplate(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.documents.bo.SDKTemplate ret = impl.downloadTemplate(in0);
        return ret;
    }

    public void uploadTemplate(java.lang.Integer in0, it.eng.spagobi.sdk.documents.bo.SDKTemplate in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        impl.uploadTemplate(in0, in1);
    }

    public java.lang.Integer saveNewDocument(it.eng.spagobi.sdk.documents.bo.SDKDocument in0, it.eng.spagobi.sdk.documents.bo.SDKTemplate in1, java.lang.Integer in2) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        java.lang.Integer ret = impl.saveNewDocument(in0, in1, in2);
        return ret;
    }

    public it.eng.spagobi.sdk.documents.bo.SDKExecutedDocumentContent executeDocument(it.eng.spagobi.sdk.documents.bo.SDKDocument in0, it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter[] in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException, it.eng.spagobi.sdk.exceptions.InvalidParameterValue, it.eng.spagobi.sdk.exceptions.MissingParameterValue, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.documents.bo.SDKExecutedDocumentContent ret = impl.executeDocument(in0, in1, in2, in3);
        return ret;
    }

    public void uploadDatamartTemplate(it.eng.spagobi.sdk.documents.bo.SDKTemplate in0, it.eng.spagobi.sdk.documents.bo.SDKTemplate in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException
    {
        impl.uploadDatamartTemplate(in0, in1, in2, in3);
    }

    public void uploadDatamartModel(it.eng.spagobi.sdk.documents.bo.SDKTemplate in0) throws java.rmi.RemoteException
    {
        impl.uploadDatamartModel(in0);
    }

    public it.eng.spagobi.sdk.documents.bo.SDKTemplate downloadDatamartFile(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException
    {
        it.eng.spagobi.sdk.documents.bo.SDKTemplate ret = impl.downloadDatamartFile(in0, in1);
        return ret;
    }

    public it.eng.spagobi.sdk.documents.bo.SDKTemplate downloadDatamartModelFiles(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException
    {
        it.eng.spagobi.sdk.documents.bo.SDKTemplate ret = impl.downloadDatamartModelFiles(in0, in1, in2);
        return ret;
    }

    public java.util.HashMap getAllDatamartModels() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        java.util.HashMap ret = impl.getAllDatamartModels();
        return ret;
    }

    public void uploadMondrianSchema(it.eng.spagobi.sdk.documents.bo.SDKSchema in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        impl.uploadMondrianSchema(in0);
    }

}
