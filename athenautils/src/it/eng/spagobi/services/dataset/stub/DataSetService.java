/**
 * DataSetService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.services.dataset.stub;

public interface DataSetService extends java.rmi.Remote {
    public it.eng.spagobi.services.dataset.bo.SpagoBiDataSet getDataSet(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException;
    public it.eng.spagobi.services.dataset.bo.SpagoBiDataSet getDataSetByLabel(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException;
    public it.eng.spagobi.services.dataset.bo.SpagoBiDataSet[] getAllDataSet(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException;
    public it.eng.spagobi.services.dataset.bo.SpagoBiDataSet saveDataSet(java.lang.String in0, java.lang.String in1, it.eng.spagobi.services.dataset.bo.SpagoBiDataSet in2) throws java.rmi.RemoteException;
}
