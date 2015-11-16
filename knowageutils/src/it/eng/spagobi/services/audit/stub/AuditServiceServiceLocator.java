/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.services.audit.stub;

public class AuditServiceServiceLocator extends org.apache.axis.client.Service implements it.eng.spagobi.services.audit.stub.AuditServiceService {

    public AuditServiceServiceLocator() {
    }


    public AuditServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public AuditServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for AuditService
    private java.lang.String AuditService_address = "http://localhost:8080/SpagoBI/services/AuditService";

    public java.lang.String getAuditServiceAddress() {
        return AuditService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AuditServiceWSDDServiceName = "AuditService";

    public java.lang.String getAuditServiceWSDDServiceName() {
        return AuditServiceWSDDServiceName;
    }

    public void setAuditServiceWSDDServiceName(java.lang.String name) {
        AuditServiceWSDDServiceName = name;
    }

    public it.eng.spagobi.services.audit.stub.AuditService getAuditService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(AuditService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAuditService(endpoint);
    }

    public it.eng.spagobi.services.audit.stub.AuditService getAuditService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            it.eng.spagobi.services.audit.stub.AuditServiceSoapBindingStub _stub = new it.eng.spagobi.services.audit.stub.AuditServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getAuditServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAuditServiceEndpointAddress(java.lang.String address) {
        AuditService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (it.eng.spagobi.services.audit.stub.AuditService.class.isAssignableFrom(serviceEndpointInterface)) {
                it.eng.spagobi.services.audit.stub.AuditServiceSoapBindingStub _stub = new it.eng.spagobi.services.audit.stub.AuditServiceSoapBindingStub(new java.net.URL(AuditService_address), this);
                _stub.setPortName(getAuditServiceWSDDServiceName());
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
        if ("AuditService".equals(inputPortName)) {
            return getAuditService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:spagobiaudit", "AuditServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:spagobiaudit", "AuditService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("AuditService".equals(portName)) {
            setAuditServiceEndpointAddress(address);
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
