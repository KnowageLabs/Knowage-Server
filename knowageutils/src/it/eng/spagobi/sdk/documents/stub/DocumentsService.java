/**
 * DocumentsService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.sdk.documents.stub;

public interface DocumentsService extends java.rmi.Remote {
    public it.eng.spagobi.sdk.documents.bo.SDKDocument[] getDocumentsAsList(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException;
    public it.eng.spagobi.sdk.documents.bo.SDKDocument getDocumentById(java.lang.Integer in0) throws java.rmi.RemoteException;
    public it.eng.spagobi.sdk.documents.bo.SDKDocument getDocumentByLabel(java.lang.String in0) throws java.rmi.RemoteException;
    public it.eng.spagobi.sdk.documents.bo.SDKFunctionality getDocumentsAsTree(java.lang.String in0) throws java.rmi.RemoteException;
    public java.lang.String[] getCorrectRolesForExecution(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException;
    public it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter[] getDocumentParameters(java.lang.Integer in0, java.lang.String in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException;
    public it.eng.spagobi.sdk.documents.bo.SDKDocumentParameterValue[] getAdmissibleValues(java.lang.Integer in0, java.lang.String in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException;
    public it.eng.spagobi.sdk.documents.bo.SDKDocumentParameterValue[] getDefaultValues(java.lang.Integer in0, java.lang.String in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException;
    public it.eng.spagobi.sdk.documents.bo.SDKTemplate downloadTemplate(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
    public void uploadTemplate(java.lang.Integer in0, it.eng.spagobi.sdk.documents.bo.SDKTemplate in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
    public java.lang.Integer saveNewDocument(it.eng.spagobi.sdk.documents.bo.SDKDocument in0, it.eng.spagobi.sdk.documents.bo.SDKTemplate in1, java.lang.Integer in2) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
    public it.eng.spagobi.sdk.documents.bo.SDKExecutedDocumentContent executeDocument(it.eng.spagobi.sdk.documents.bo.SDKDocument in0, it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter[] in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException, it.eng.spagobi.sdk.exceptions.InvalidParameterValue, it.eng.spagobi.sdk.exceptions.MissingParameterValue, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
    public void uploadDatamartTemplate(it.eng.spagobi.sdk.documents.bo.SDKTemplate in0, it.eng.spagobi.sdk.documents.bo.SDKTemplate in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException;
    public void uploadDatamartModel(it.eng.spagobi.sdk.documents.bo.SDKTemplate in0) throws java.rmi.RemoteException;
    public it.eng.spagobi.sdk.documents.bo.SDKTemplate downloadDatamartFile(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException;
    public it.eng.spagobi.sdk.documents.bo.SDKTemplate downloadDatamartModelFiles(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException;
    public java.util.HashMap getAllDatamartModels() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
    public void uploadMondrianSchema(it.eng.spagobi.sdk.documents.bo.SDKSchema in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
}
