/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.services.sbidocument.stub;

public class SbiDocumentServiceServiceLocator extends org.apache.axis.client.Service implements it.eng.spagobi.services.sbidocument.stub.SbiDocumentServiceService {

    public SbiDocumentServiceServiceLocator() {
    }


    public SbiDocumentServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SbiDocumentServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for SbiDocumentService
    private java.lang.String SbiDocumentService_address = "http://localhost:8080/SpagoBI/services/SbiDocumentService";

    public java.lang.String getSbiDocumentServiceAddress() {
        return SbiDocumentService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SbiDocumentServiceWSDDServiceName = "SbiDocumentService";

    public java.lang.String getSbiDocumentServiceWSDDServiceName() {
        return SbiDocumentServiceWSDDServiceName;
    }

    public void setSbiDocumentServiceWSDDServiceName(java.lang.String name) {
        SbiDocumentServiceWSDDServiceName = name;
    }

    public it.eng.spagobi.services.sbidocument.stub.SbiDocumentService getSbiDocumentService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SbiDocumentService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSbiDocumentService(endpoint);
    }

    public it.eng.spagobi.services.sbidocument.stub.SbiDocumentService getSbiDocumentService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            it.eng.spagobi.services.sbidocument.stub.SbiDocumentServiceSoapBindingStub _stub = new it.eng.spagobi.services.sbidocument.stub.SbiDocumentServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getSbiDocumentServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSbiDocumentServiceEndpointAddress(java.lang.String address) {
        SbiDocumentService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (it.eng.spagobi.services.sbidocument.stub.SbiDocumentService.class.isAssignableFrom(serviceEndpointInterface)) {
                it.eng.spagobi.services.sbidocument.stub.SbiDocumentServiceSoapBindingStub _stub = new it.eng.spagobi.services.sbidocument.stub.SbiDocumentServiceSoapBindingStub(new java.net.URL(SbiDocumentService_address), this);
                _stub.setPortName(getSbiDocumentServiceWSDDServiceName());
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
        if ("SbiDocumentService".equals(inputPortName)) {
            return getSbiDocumentService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:spagobisbidocument", "SbiDocumentServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:spagobisbidocument", "SbiDocumentService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("SbiDocumentService".equals(portName)) {
            setSbiDocumentServiceEndpointAddress(address);
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
