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

package it.eng.spagobi.services.dataset.stub;

public class DataSetServiceSoapBindingSkeleton implements it.eng.spagobi.services.dataset.stub.DataSetService, org.apache.axis.wsdl.Skeleton {
    private it.eng.spagobi.services.dataset.stub.DataSetService impl;
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
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDataSetByLabel", _params, new javax.xml.namespace.QName("", "getDataSetByLabelReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.dataset.services.spagobi.eng.it", "SpagoBiDataSet"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobidataset", "getDataSetByLabel"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDataSetByLabel") == null) {
            _myOperations.put("getDataSetByLabel", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDataSetByLabel")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.dataset.services.spagobi.eng.it", "SpagoBiDataSet"), it.eng.spagobi.services.dataset.bo.SpagoBiDataSet.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("saveDataSet", _params, new javax.xml.namespace.QName("", "saveDataSetReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.dataset.services.spagobi.eng.it", "SpagoBiDataSet"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobidataset", "saveDataSet"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("saveDataSet") == null) {
            _myOperations.put("saveDataSet", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("saveDataSet")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getAllDataSet", _params, new javax.xml.namespace.QName("", "getAllDataSetReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobidataset", "ArrayOf_tns2_SpagoBiDataSet"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobidataset", "getAllDataSet"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getAllDataSet") == null) {
            _myOperations.put("getAllDataSet", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getAllDataSet")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getDataSet", _params, new javax.xml.namespace.QName("", "getDataSetReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.dataset.services.spagobi.eng.it", "SpagoBiDataSet"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobidataset", "getDataSet"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDataSet") == null) {
            _myOperations.put("getDataSet", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getDataSet")).add(_oper);
    }

    public DataSetServiceSoapBindingSkeleton() {
        this.impl = new it.eng.spagobi.services.dataset.stub.DataSetServiceSoapBindingImpl();
    }

    public DataSetServiceSoapBindingSkeleton(it.eng.spagobi.services.dataset.stub.DataSetService impl) {
        this.impl = impl;
    }
    public it.eng.spagobi.services.dataset.bo.SpagoBiDataSet getDataSetByLabel(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException
    {
        it.eng.spagobi.services.dataset.bo.SpagoBiDataSet ret = impl.getDataSetByLabel(in0, in1, in2);
        return ret;
    }

    public it.eng.spagobi.services.dataset.bo.SpagoBiDataSet saveDataSet(java.lang.String in0, java.lang.String in1, it.eng.spagobi.services.dataset.bo.SpagoBiDataSet in2) throws java.rmi.RemoteException
    {
        it.eng.spagobi.services.dataset.bo.SpagoBiDataSet ret = impl.saveDataSet(in0, in1, in2);
        return ret;
    }

    public it.eng.spagobi.services.dataset.bo.SpagoBiDataSet[] getAllDataSet(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException
    {
        it.eng.spagobi.services.dataset.bo.SpagoBiDataSet[] ret = impl.getAllDataSet(in0, in1);
        return ret;
    }

    public it.eng.spagobi.services.dataset.bo.SpagoBiDataSet getDataSet(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException
    {
        it.eng.spagobi.services.dataset.bo.SpagoBiDataSet ret = impl.getDataSet(in0, in1, in2);
        return ret;
    }

}
