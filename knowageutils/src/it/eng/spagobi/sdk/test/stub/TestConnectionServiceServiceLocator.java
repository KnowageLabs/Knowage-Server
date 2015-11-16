/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.sdk.test.stub;

public class TestConnectionServiceServiceLocator extends org.apache.axis.client.Service implements it.eng.spagobi.sdk.test.stub.TestConnectionServiceService {

    public TestConnectionServiceServiceLocator() {
    }


    public TestConnectionServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public TestConnectionServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for TestConnectionService
    private java.lang.String TestConnectionService_address = "http://localhost:8080/SpagoBI/sdk/TestConnectionService";

    public java.lang.String getTestConnectionServiceAddress() {
        return TestConnectionService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String TestConnectionServiceWSDDServiceName = "TestConnectionService";

    public java.lang.String getTestConnectionServiceWSDDServiceName() {
        return TestConnectionServiceWSDDServiceName;
    }

    public void setTestConnectionServiceWSDDServiceName(java.lang.String name) {
        TestConnectionServiceWSDDServiceName = name;
    }

    public it.eng.spagobi.sdk.test.stub.TestConnectionService getTestConnectionService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(TestConnectionService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getTestConnectionService(endpoint);
    }

    public it.eng.spagobi.sdk.test.stub.TestConnectionService getTestConnectionService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            it.eng.spagobi.sdk.test.stub.TestConnectionServiceSoapBindingStub _stub = new it.eng.spagobi.sdk.test.stub.TestConnectionServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getTestConnectionServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setTestConnectionServiceEndpointAddress(java.lang.String address) {
        TestConnectionService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (it.eng.spagobi.sdk.test.stub.TestConnectionService.class.isAssignableFrom(serviceEndpointInterface)) {
                it.eng.spagobi.sdk.test.stub.TestConnectionServiceSoapBindingStub _stub = new it.eng.spagobi.sdk.test.stub.TestConnectionServiceSoapBindingStub(new java.net.URL(TestConnectionService_address), this);
                _stub.setPortName(getTestConnectionServiceWSDDServiceName());
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
        if ("TestConnectionService".equals(inputPortName)) {
            return getTestConnectionService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:spagobisdktestconnection", "TestConnectionServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:spagobisdktestconnection", "TestConnectionService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("TestConnectionService".equals(portName)) {
            setTestConnectionServiceEndpointAddress(address);
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
