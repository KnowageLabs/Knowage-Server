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

package it.eng.spagobi.sdk.importexport.stub;

public class ImportExportSDKServiceSoapBindingSkeleton implements it.eng.spagobi.sdk.importexport.stub.ImportExportSDKService, org.apache.axis.wsdl.Skeleton {
    private it.eng.spagobi.sdk.importexport.stub.ImportExportSDKService impl;
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
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.importexport.sdk.spagobi.eng.it", "SDKFile"), it.eng.spagobi.sdk.importexport.bo.SDKFile.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://bo.importexport.sdk.spagobi.eng.it", "SDKFile"), it.eng.spagobi.sdk.importexport.bo.SDKFile.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("importDocuments", _params, new javax.xml.namespace.QName("", "importDocumentsReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://bo.importexport.sdk.spagobi.eng.it", "SDKFile"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:spagobisdkimportexport", "importDocuments"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("importDocuments") == null) {
            _myOperations.put("importDocuments", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("importDocuments")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NotAllowedOperationException");
        _fault.setQName(new javax.xml.namespace.QName("urn:spagobisdkimportexport", "fault"));
        _fault.setClassName("it.eng.spagobi.sdk.exceptions.NotAllowedOperationException");
        _fault.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "NotAllowedOperationException"));
        _oper.addFault(_fault);
    }

    public ImportExportSDKServiceSoapBindingSkeleton() {
        this.impl = new it.eng.spagobi.sdk.importexport.stub.ImportExportSDKServiceSoapBindingImpl();
    }

    public ImportExportSDKServiceSoapBindingSkeleton(it.eng.spagobi.sdk.importexport.stub.ImportExportSDKService impl) {
        this.impl = impl;
    }
    public it.eng.spagobi.sdk.importexport.bo.SDKFile importDocuments(it.eng.spagobi.sdk.importexport.bo.SDKFile in0, it.eng.spagobi.sdk.importexport.bo.SDKFile in1, boolean in2) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException
    {
        it.eng.spagobi.sdk.importexport.bo.SDKFile ret = impl.importDocuments(in0, in1, in2);
        return ret;
    }

}
