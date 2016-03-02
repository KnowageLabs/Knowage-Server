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

package it.eng.spagobi.tools.dataset.wsconnectors.stub;

public class WSDataSetServiceSoapBindingSkeleton implements it.eng.spagobi.tools.dataset.wsconnectors.stub.IWsConnector, org.apache.axis.wsdl.Skeleton {
    private it.eng.spagobi.tools.dataset.wsconnectors.stub.IWsConnector impl;
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
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"), java.util.HashMap.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("readDataSet", _params, new javax.xml.namespace.QName("", "readDataSetReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobiwsdataset", "readDataSet"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("readDataSet") == null) {
            _myOperations.put("readDataSet", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("readDataSet")).add(_oper);
    }

    public WSDataSetServiceSoapBindingSkeleton() {
        this.impl = new it.eng.spagobi.tools.dataset.wsconnectors.stub.WSDataSetServiceSoapBindingImpl();
    }

    public WSDataSetServiceSoapBindingSkeleton(it.eng.spagobi.tools.dataset.wsconnectors.stub.IWsConnector impl) {
        this.impl = impl;
    }
    public java.lang.String readDataSet(java.lang.String in0, java.util.Map in1, java.lang.String in2) throws java.rmi.RemoteException
    {
        java.lang.String ret = impl.readDataSet(in0, in1, in2);
        return ret;
    }

}
