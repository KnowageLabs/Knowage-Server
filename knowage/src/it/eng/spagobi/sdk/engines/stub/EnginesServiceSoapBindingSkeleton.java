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

package it.eng.spagobi.sdk.engines.stub;

public class EnginesServiceSoapBindingSkeleton implements it.eng.spagobi.sdk.engines.stub.EnginesService, org.apache.axis.wsdl.Skeleton {
    private it.eng.spagobi.sdk.engines.stub.EnginesService impl;
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
        };
        _oper = new org.apache.axis.description.OperationDesc("getEngines", _params, new javax.xml.namespace.QName("", "getEnginesReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkengines", "ArrayOf_tns2_SDKEngine"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkengines", "getEngines"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getEngines") == null) {
            _myOperations.put("getEngines", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getEngines")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkengines", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"), java.lang.Integer.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getEngine", _params, new javax.xml.namespace.QName("", "getEngineReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.engines.sdk.spagobi.eng.it", "SDKEngine"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkengines", "getEngine"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getEngine") == null) {
            _myOperations.put("getEngine", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getEngine")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkengines", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
    }

    public EnginesServiceSoapBindingSkeleton() {
        this.impl = new it.eng.spagobi.sdk.engines.stub.EnginesServiceSoapBindingImpl();
    }

    public EnginesServiceSoapBindingSkeleton(it.eng.spagobi.sdk.engines.stub.EnginesService impl) {
        this.impl = impl;
    }
    public it.eng.spagobi.sdk.engines.bo.SDKEngine[] getEngines() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.engines.bo.SDKEngine[] ret = impl.getEngines();
        return ret;
    }

    public it.eng.spagobi.sdk.engines.bo.SDKEngine getEngine(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.engines.bo.SDKEngine ret = impl.getEngine(in0);
        return ret;
    }

}
