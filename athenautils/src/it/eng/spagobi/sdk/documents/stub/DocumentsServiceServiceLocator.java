/**
 * DocumentsServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.sdk.documents.stub;

public class DocumentsServiceServiceLocator extends org.apache.axis.client.Service implements it.eng.spagobi.sdk.documents.stub.DocumentsServiceService {

    public DocumentsServiceServiceLocator() {
    }


    public DocumentsServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public DocumentsServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for DocumentsService
    private java.lang.String DocumentsService_address = "http://localhost:8080/SpagoBI/sdk/DocumentsService";

    public java.lang.String getDocumentsServiceAddress() {
        return DocumentsService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String DocumentsServiceWSDDServiceName = "DocumentsService";

    public java.lang.String getDocumentsServiceWSDDServiceName() {
        return DocumentsServiceWSDDServiceName;
    }

    public void setDocumentsServiceWSDDServiceName(java.lang.String name) {
        DocumentsServiceWSDDServiceName = name;
    }

    public it.eng.spagobi.sdk.documents.stub.DocumentsService getDocumentsService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(DocumentsService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getDocumentsService(endpoint);
    }

    public it.eng.spagobi.sdk.documents.stub.DocumentsService getDocumentsService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            it.eng.spagobi.sdk.documents.stub.DocumentsServiceSoapBindingStub _stub = new it.eng.spagobi.sdk.documents.stub.DocumentsServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getDocumentsServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setDocumentsServiceEndpointAddress(java.lang.String address) {
        DocumentsService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (it.eng.spagobi.sdk.documents.stub.DocumentsService.class.isAssignableFrom(serviceEndpointInterface)) {
                it.eng.spagobi.sdk.documents.stub.DocumentsServiceSoapBindingStub _stub = new it.eng.spagobi.sdk.documents.stub.DocumentsServiceSoapBindingStub(new java.net.URL(DocumentsService_address), this);
                _stub.setPortName(getDocumentsServiceWSDDServiceName());
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
        if ("DocumentsService".equals(inputPortName)) {
            return getDocumentsService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:spagobisdkdocuments", "DocumentsServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:spagobisdkdocuments", "DocumentsService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("DocumentsService".equals(portName)) {
            setDocumentsServiceEndpointAddress(address);
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
