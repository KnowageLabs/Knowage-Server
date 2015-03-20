/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */


/**
 * ImportExportSDKServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.sdk.importexport.stub;

public class ImportExportSDKServiceServiceLocator extends org.apache.axis.client.Service implements it.eng.spagobi.sdk.importexport.stub.ImportExportSDKServiceService {

    public ImportExportSDKServiceServiceLocator() {
    }


    public ImportExportSDKServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ImportExportSDKServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ImportExportSDKService
    private java.lang.String ImportExportSDKService_address = "http://localhost:8080/SpagoBI/sdk/ImportExportSDKService";

    public java.lang.String getImportExportSDKServiceAddress() {
        return ImportExportSDKService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ImportExportSDKServiceWSDDServiceName = "ImportExportSDKService";

    public java.lang.String getImportExportSDKServiceWSDDServiceName() {
        return ImportExportSDKServiceWSDDServiceName;
    }

    public void setImportExportSDKServiceWSDDServiceName(java.lang.String name) {
        ImportExportSDKServiceWSDDServiceName = name;
    }

    public it.eng.spagobi.sdk.importexport.stub.ImportExportSDKService getImportExportSDKService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ImportExportSDKService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getImportExportSDKService(endpoint);
    }

    public it.eng.spagobi.sdk.importexport.stub.ImportExportSDKService getImportExportSDKService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            it.eng.spagobi.sdk.importexport.stub.ImportExportSDKServiceSoapBindingStub _stub = new it.eng.spagobi.sdk.importexport.stub.ImportExportSDKServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getImportExportSDKServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setImportExportSDKServiceEndpointAddress(java.lang.String address) {
        ImportExportSDKService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (it.eng.spagobi.sdk.importexport.stub.ImportExportSDKService.class.isAssignableFrom(serviceEndpointInterface)) {
                it.eng.spagobi.sdk.importexport.stub.ImportExportSDKServiceSoapBindingStub _stub = new it.eng.spagobi.sdk.importexport.stub.ImportExportSDKServiceSoapBindingStub(new java.net.URL(ImportExportSDKService_address), this);
                _stub.setPortName(getImportExportSDKServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("ImportExportSDKService".equals(inputPortName)) {
            return getImportExportSDKService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:spagobisdkimportexport", "ImportExportSDKServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:spagobisdkimportexport", "ImportExportSDKService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ImportExportSDKService".equals(portName)) {
            setImportExportSDKServiceEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
