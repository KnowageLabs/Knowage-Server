/**
 * MetamodelServiceSoapBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.services.metamodel.stub;

import it.eng.spagobi.services.metamodel.service.MetamodelServiceImpl;

public class MetamodelServiceSoapBindingImpl implements it.eng.spagobi.services.metamodel.stub.MetamodelService{
    public javax.activation.DataHandler getMetamodelContentByName(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException {
    	MetamodelServiceImpl service = new MetamodelServiceImpl();
    	return service.getMetamodelContentByName(in0, in1 , in2);
    }

    public long getMetamodelContentLastModified(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException {
    	MetamodelServiceImpl service = new MetamodelServiceImpl();
    	return service.getMetamodelContentLastModified(in0, in1 , in2);
    }

}
