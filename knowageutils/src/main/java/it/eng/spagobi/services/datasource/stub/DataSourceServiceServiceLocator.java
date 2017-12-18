/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.services.datasource.stub;

public class DataSourceServiceServiceLocator extends org.apache.axis.client.Service implements it.eng.spagobi.services.datasource.stub.DataSourceServiceService {

    public DataSourceServiceServiceLocator() {
    }


    public DataSourceServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public DataSourceServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for DataSourceService
    private java.lang.String DataSourceService_address = "http://localhost:8181/SpagoBI/services/DataSourceService";

    public java.lang.String getDataSourceServiceAddress() {
        return DataSourceService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String DataSourceServiceWSDDServiceName = "DataSourceService";

    public java.lang.String getDataSourceServiceWSDDServiceName() {
        return DataSourceServiceWSDDServiceName;
    }

    public void setDataSourceServiceWSDDServiceName(java.lang.String name) {
        DataSourceServiceWSDDServiceName = name;
    }

    public it.eng.spagobi.services.datasource.stub.DataSourceService getDataSourceService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(DataSourceService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getDataSourceService(endpoint);
    }

    public it.eng.spagobi.services.datasource.stub.DataSourceService getDataSourceService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            it.eng.spagobi.services.datasource.stub.DataSourceServiceSoapBindingStub _stub = new it.eng.spagobi.services.datasource.stub.DataSourceServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getDataSourceServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setDataSourceServiceEndpointAddress(java.lang.String address) {
        DataSourceService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (it.eng.spagobi.services.datasource.stub.DataSourceService.class.isAssignableFrom(serviceEndpointInterface)) {
                it.eng.spagobi.services.datasource.stub.DataSourceServiceSoapBindingStub _stub = new it.eng.spagobi.services.datasource.stub.DataSourceServiceSoapBindingStub(new java.net.URL(DataSourceService_address), this);
                _stub.setPortName(getDataSourceServiceWSDDServiceName());
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
        if ("DataSourceService".equals(inputPortName)) {
            return getDataSourceService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:spagobidatasource", "DataSourceServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:spagobidatasource", "DataSourceService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("DataSourceService".equals(portName)) {
            setDataSourceServiceEndpointAddress(address);
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
