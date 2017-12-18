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

package it.eng.spagobi.sdk.behavioural.stub;

public class BehaviouralServiceSoapBindingSkeleton implements it.eng.spagobi.sdk.behavioural.stub.BehaviouralService, org.apache.axis.wsdl.Skeleton {
    private it.eng.spagobi.sdk.behavioural.stub.BehaviouralService impl;
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
        };
        _oper = new org.apache.axis.description.OperationDesc("getAllAttributes", _params, new javax.xml.namespace.QName("", "getAllAttributesReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkbehavioural", "ArrayOf_tns2_SDKAttribute"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkbehavioural", "getAllAttributes"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getAllAttributes") == null) {
            _myOperations.put("getAllAttributes", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getAllAttributes")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkbehavioural", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
        };
        _oper = new org.apache.axis.description.OperationDesc("getRoles", _params, new javax.xml.namespace.QName("", "getRolesReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkbehavioural", "ArrayOf_tns2_SDKRole"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkbehavioural", "getRoles"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getRoles") == null) {
            _myOperations.put("getRoles", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getRoles")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkbehavioural", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getRolesByUserId", _params, new javax.xml.namespace.QName("", "getRolesByUserIdReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:spagobisdkbehavioural", "ArrayOf_tns2_SDKRole"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkbehavioural", "getRolesByUserId"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getRolesByUserId") == null) {
            _myOperations.put("getRolesByUserId", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getRolesByUserId")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkbehavioural", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
    }

    public BehaviouralServiceSoapBindingSkeleton() {
        this.impl = new it.eng.spagobi.sdk.behavioural.stub.BehaviouralServiceSoapBindingImpl();
    }

    public BehaviouralServiceSoapBindingSkeleton(it.eng.spagobi.sdk.behavioural.stub.BehaviouralService impl) {
        this.impl = impl;
    }
    public it.eng.spagobi.sdk.behavioural.bo.SDKAttribute[] getAllAttributes(java.lang.String in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.behavioural.bo.SDKAttribute[] ret = impl.getAllAttributes(in0);
        return ret;
    }

    public it.eng.spagobi.sdk.behavioural.bo.SDKRole[] getRoles() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.behavioural.bo.SDKRole[] ret = impl.getRoles();
        return ret;
    }

    public it.eng.spagobi.sdk.behavioural.bo.SDKRole[] getRolesByUserId(java.lang.String in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.behavioural.bo.SDKRole[] ret = impl.getRolesByUserId(in0);
        return ret;
    }

}
