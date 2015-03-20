/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.dataset.wsconnectors.stub;

public class IWsConnectorServiceLocator extends org.apache.axis.client.Service implements it.eng.spagobi.tools.dataset.wsconnectors.stub.IWsConnectorService {

    public IWsConnectorServiceLocator() {
    }


    public IWsConnectorServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public IWsConnectorServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for WSDataSetService
    private java.lang.String WSDataSetService_address = "http://localhost:8080/SpagoBI/services/WSDataSetService";

    public java.lang.String getWSDataSetServiceAddress() {
        return WSDataSetService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WSDataSetServiceWSDDServiceName = "WSDataSetService";

    public java.lang.String getWSDataSetServiceWSDDServiceName() {
        return WSDataSetServiceWSDDServiceName;
    }

    public void setWSDataSetServiceWSDDServiceName(java.lang.String name) {
        WSDataSetServiceWSDDServiceName = name;
    }

    public it.eng.spagobi.tools.dataset.wsconnectors.stub.IWsConnector getWSDataSetService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WSDataSetService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getWSDataSetService(endpoint);
    }

    public it.eng.spagobi.tools.dataset.wsconnectors.stub.IWsConnector getWSDataSetService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            it.eng.spagobi.tools.dataset.wsconnectors.stub.WSDataSetServiceSoapBindingStub _stub = new it.eng.spagobi.tools.dataset.wsconnectors.stub.WSDataSetServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getWSDataSetServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setWSDataSetServiceEndpointAddress(java.lang.String address) {
        WSDataSetService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (it.eng.spagobi.tools.dataset.wsconnectors.stub.IWsConnector.class.isAssignableFrom(serviceEndpointInterface)) {
                it.eng.spagobi.tools.dataset.wsconnectors.stub.WSDataSetServiceSoapBindingStub _stub = new it.eng.spagobi.tools.dataset.wsconnectors.stub.WSDataSetServiceSoapBindingStub(new java.net.URL(WSDataSetService_address), this);
                _stub.setPortName(getWSDataSetServiceWSDDServiceName());
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
        if ("WSDataSetService".equals(inputPortName)) {
            return getWSDataSetService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:spagobiwsdataset", "IWsConnectorService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:spagobiwsdataset", "WSDataSetService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("WSDataSetService".equals(portName)) {
            setWSDataSetServiceEndpointAddress(address);
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
