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

package it.eng.spagobi.sdk.datasources.stub;

public class DataSourcesSDKServiceSoapBindingSkeleton implements it.eng.spagobi.sdk.datasources.stub.DataSourcesSDKService, org.apache.axis.wsdl.Skeleton {
    private it.eng.spagobi.sdk.datasources.stub.DataSourcesSDKService impl;
    private static java.util.Map _myOperations = new java.util.Hashtable();
    private static java.util.Collection _myOperationsList = new java.util.ArrayList();

    /**
    * Returns List of OperationDesc objects with this name
    */
    public static java.util.List getOperationDescByName(java.lang.String methodName) {
        return (java.util.List)_myOperations.get(methodName);
    }

    /**
    * Returns Collection of OperationDescs
    */
    public static java.util.Collection getOperationDescs() {
        return _myOperationsList;
    }

    static {
        org.apache.axis.description.OperationDesc _oper;
        org.apache.axis.description.FaultDesc _fault;
        org.apache.axis.description.ParameterDesc [] _params;
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDataSource", _params, new javax.xml.namespace.QName("", "getDataSourceReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.datasources.sdk.spagobi.eng.it", "SDKDataSource"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdatasources", "getDataSource"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDataSource") == null) {
            _myOperations.put("getDataSource", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDataSource")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdatasources", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
        };
        _oper = new org.apache.axis.description.OperationDesc("getDataSources", _params, new javax.xml.namespace.QName("", "getDataSourcesReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkdatasources", "ArrayOf_tns2_SDKDataSource"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkdatasources", "getDataSources"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDataSources") == null) {
            _myOperations.put("getDataSources", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDataSources")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkdatasources", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
    }

    public DataSourcesSDKServiceSoapBindingSkeleton() {
        this.impl = new it.eng.spagobi.sdk.datasources.stub.DataSourcesSDKServiceSoapBindingImpl();
    }

    public DataSourcesSDKServiceSoapBindingSkeleton(it.eng.spagobi.sdk.datasources.stub.DataSourcesSDKService impl) {
        this.impl = impl;
    }
    public it.eng.spagobi.sdk.datasources.bo.SDKDataSource getDataSource(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.datasources.bo.SDKDataSource ret = impl.getDataSource(in0);
        return ret;
    }

    public it.eng.spagobi.sdk.datasources.bo.SDKDataSource[] getDataSources() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.datasources.bo.SDKDataSource[] ret = impl.getDataSources();
        return ret;
    }

}
