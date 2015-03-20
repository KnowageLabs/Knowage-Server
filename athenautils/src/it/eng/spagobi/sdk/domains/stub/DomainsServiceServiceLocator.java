/**
 * DomainsServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.sdk.domains.stub;

public class DomainsServiceServiceLocator extends org.apache.axis.client.Service implements it.eng.spagobi.sdk.domains.stub.DomainsServiceService {

    public DomainsServiceServiceLocator() {
    }


    public DomainsServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public DomainsServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for DomainsService
    private java.lang.String DomainsService_address = "http://localhost:8181/SpagoBI/sdk/DomainsService";

    public java.lang.String getDomainsServiceAddress() {
        return DomainsService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String DomainsServiceWSDDServiceName = "DomainsService";

    public java.lang.String getDomainsServiceWSDDServiceName() {
        return DomainsServiceWSDDServiceName;
    }

    public void setDomainsServiceWSDDServiceName(java.lang.String name) {
        DomainsServiceWSDDServiceName = name;
    }

    public it.eng.spagobi.sdk.domains.stub.DomainsService getDomainsService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(DomainsService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getDomainsService(endpoint);
    }

    public it.eng.spagobi.sdk.domains.stub.DomainsService getDomainsService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            it.eng.spagobi.sdk.domains.stub.DomainsServiceSoapBindingStub _stub = new it.eng.spagobi.sdk.domains.stub.DomainsServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getDomainsServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setDomainsServiceEndpointAddress(java.lang.String address) {
        DomainsService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (it.eng.spagobi.sdk.domains.stub.DomainsService.class.isAssignableFrom(serviceEndpointInterface)) {
                it.eng.spagobi.sdk.domains.stub.DomainsServiceSoapBindingStub _stub = new it.eng.spagobi.sdk.domains.stub.DomainsServiceSoapBindingStub(new java.net.URL(DomainsService_address), this);
                _stub.setPortName(getDomainsServiceWSDDServiceName());
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
        if ("DomainsService".equals(inputPortName)) {
            return getDomainsService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:spagobisdkdomains", "DomainsServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:spagobisdkdomains", "DomainsService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("DomainsService".equals(portName)) {
            setDomainsServiceEndpointAddress(address);
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
