/**
 * DataSourceServiceSoapBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.services.datasource.stub;

import it.eng.spagobi.services.datasource.service.DataSourceServiceImpl;

public class DataSourceServiceSoapBindingImpl implements it.eng.spagobi.services.datasource.stub.DataSourceService{
    public it.eng.spagobi.services.datasource.bo.SpagoBiDataSource getDataSource(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException {
    	DataSourceServiceImpl service=new DataSourceServiceImpl();
    	return service.getDataSource(in0, in1,in2);
    }

    public it.eng.spagobi.services.datasource.bo.SpagoBiDataSource getDataSourceById(java.lang.String in0, java.lang.String in1, java.lang.Integer in2) throws java.rmi.RemoteException {
    	DataSourceServiceImpl service=new DataSourceServiceImpl();
    	return service.getDataSourceById(in0, in1,in2);
    }

    public it.eng.spagobi.services.datasource.bo.SpagoBiDataSource[] getAllDataSource(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException {
    	DataSourceServiceImpl service=new DataSourceServiceImpl();
    	return service.getAllDataSource(in0,in1);
    }

    public it.eng.spagobi.services.datasource.bo.SpagoBiDataSource getDataSourceByLabel(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException {
    	DataSourceServiceImpl service=new DataSourceServiceImpl();
    	return service.getDataSourceByLabel(in0, in1,in2);
    }

}


