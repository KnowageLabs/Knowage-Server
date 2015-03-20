/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.services.content.stub;

public class ContentServiceServiceLocator extends org.apache.axis.client.Service implements it.eng.spagobi.services.content.stub.ContentServiceService {

    public ContentServiceServiceLocator() {
    }


    public ContentServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ContentServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ContentService
    private java.lang.String ContentService_address = "http://localhost:8080/SpagoBI/services/ContentService";

    public java.lang.String getContentServiceAddress() {
        return ContentService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ContentServiceWSDDServiceName = "ContentService";

    public java.lang.String getContentServiceWSDDServiceName() {
        return ContentServiceWSDDServiceName;
    }

    public void setContentServiceWSDDServiceName(java.lang.String name) {
        ContentServiceWSDDServiceName = name;
    }

    public it.eng.spagobi.services.content.stub.ContentService getContentService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ContentService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getContentService(endpoint);
    }

    public it.eng.spagobi.services.content.stub.ContentService getContentService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            it.eng.spagobi.services.content.stub.ContentServiceSoapBindingStub _stub = new it.eng.spagobi.services.content.stub.ContentServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getContentServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setContentServiceEndpointAddress(java.lang.String address) {
        ContentService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (it.eng.spagobi.services.content.stub.ContentService.class.isAssignableFrom(serviceEndpointInterface)) {
                it.eng.spagobi.services.content.stub.ContentServiceSoapBindingStub _stub = new it.eng.spagobi.services.content.stub.ContentServiceSoapBindingStub(new java.net.URL(ContentService_address), this);
                _stub.setPortName(getContentServiceWSDDServiceName());
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
        if ("ContentService".equals(inputPortName)) {
            return getContentService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:spagobicontent", "ContentServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:spagobicontent", "ContentService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ContentService".equals(portName)) {
            setContentServiceEndpointAddress(address);
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
