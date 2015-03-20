/**
 * MetamodelServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.services.metamodel.stub;

public class MetamodelServiceServiceLocator extends org.apache.axis.client.Service implements it.eng.spagobi.services.metamodel.stub.MetamodelServiceService {

    public MetamodelServiceServiceLocator() {
    }


    public MetamodelServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public MetamodelServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for MetamodelService
    private java.lang.String MetamodelService_address = "http://localhost:8181/SpagoBI/services/MetamodelService";

    public java.lang.String getMetamodelServiceAddress() {
        return MetamodelService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String MetamodelServiceWSDDServiceName = "MetamodelService";

    public java.lang.String getMetamodelServiceWSDDServiceName() {
        return MetamodelServiceWSDDServiceName;
    }

    public void setMetamodelServiceWSDDServiceName(java.lang.String name) {
        MetamodelServiceWSDDServiceName = name;
    }

    public it.eng.spagobi.services.metamodel.stub.MetamodelService getMetamodelService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(MetamodelService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getMetamodelService(endpoint);
    }

    public it.eng.spagobi.services.metamodel.stub.MetamodelService getMetamodelService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            it.eng.spagobi.services.metamodel.stub.MetamodelServiceSoapBindingStub _stub = new it.eng.spagobi.services.metamodel.stub.MetamodelServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getMetamodelServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setMetamodelServiceEndpointAddress(java.lang.String address) {
        MetamodelService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (it.eng.spagobi.services.metamodel.stub.MetamodelService.class.isAssignableFrom(serviceEndpointInterface)) {
                it.eng.spagobi.services.metamodel.stub.MetamodelServiceSoapBindingStub _stub = new it.eng.spagobi.services.metamodel.stub.MetamodelServiceSoapBindingStub(new java.net.URL(MetamodelService_address), this);
                _stub.setPortName(getMetamodelServiceWSDDServiceName());
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
        if ("MetamodelService".equals(inputPortName)) {
            return getMetamodelService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:spagobimetamodel", "MetamodelServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:spagobimetamodel", "MetamodelService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("MetamodelService".equals(portName)) {
            setMetamodelServiceEndpointAddress(address);
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
