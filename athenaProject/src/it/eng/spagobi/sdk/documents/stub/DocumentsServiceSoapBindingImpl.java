/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */


package it.eng.spagobi.sdk.documents.stub;

import it.eng.spagobi.sdk.documents.impl.DocumentsServiceImpl;

public class DocumentsServiceSoapBindingImpl implements it.eng.spagobi.sdk.documents.stub.DocumentsService{
    public it.eng.spagobi.sdk.documents.bo.SDKDocument[] getDocumentsAsList(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException {
      DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	return impl.getDocumentsAsList(in0, in1, in2);
    }

    public it.eng.spagobi.sdk.documents.bo.SDKDocument getDocumentById(java.lang.Integer in0) throws java.rmi.RemoteException {
        DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	return impl.getDocumentById(in0);
    }

    public it.eng.spagobi.sdk.documents.bo.SDKDocument getDocumentByLabel(java.lang.String in0) throws java.rmi.RemoteException {
      DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	return impl.getDocumentByLabel(in0);
    }

    public it.eng.spagobi.sdk.documents.bo.SDKFunctionality getDocumentsAsTree(java.lang.String in0) throws java.rmi.RemoteException {
      DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	return impl.getDocumentsAsTree(in0);
    }

    public java.lang.String[] getCorrectRolesForExecution(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException {
      DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	return impl.getCorrectRolesForExecution(in0);
    }

    public it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter[] getDocumentParameters(java.lang.Integer in0, java.lang.String in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException {
    	DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	return impl.getDocumentParameters(in0, in1);
    }

    public it.eng.spagobi.sdk.documents.bo.SDKDocumentParameterValue[] getAdmissibleValues(java.lang.Integer in0, java.lang.String in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException {
    	DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	return impl.getAdmissibleValues(in0, in1);
    }

    public it.eng.spagobi.sdk.documents.bo.SDKDocumentParameterValue[] getDefaultValues(java.lang.Integer in0, java.lang.String in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException {
    	DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	return impl.getDefaultValues(in0, in1);
    }
    

    public it.eng.spagobi.sdk.documents.bo.SDKTemplate downloadTemplate(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	return impl.downloadTemplate(in0);
    }

    public void uploadTemplate(java.lang.Integer in0, it.eng.spagobi.sdk.documents.bo.SDKTemplate in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
      DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	impl.uploadTemplate(in0,in1);
    }

    public java.lang.Integer saveNewDocument(it.eng.spagobi.sdk.documents.bo.SDKDocument in0, it.eng.spagobi.sdk.documents.bo.SDKTemplate in1, java.lang.Integer in2) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	return impl.saveNewDocument(in0, in1, in2);
    }

    public it.eng.spagobi.sdk.documents.bo.SDKExecutedDocumentContent executeDocument(it.eng.spagobi.sdk.documents.bo.SDKDocument in0, it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter[] in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException, it.eng.spagobi.sdk.exceptions.InvalidParameterValue, it.eng.spagobi.sdk.exceptions.MissingParameterValue, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	return impl.executeDocument(in0, in1, in2, in3);
    }

    public void uploadDatamartTemplate(it.eng.spagobi.sdk.documents.bo.SDKTemplate in0, it.eng.spagobi.sdk.documents.bo.SDKTemplate in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException {
      DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	impl.uploadDatamartTemplate(in0, in1, in2, in3);
    }

    public void uploadDatamartModel(it.eng.spagobi.sdk.documents.bo.SDKTemplate in0) throws java.rmi.RemoteException {
    	DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	impl.uploadDatamartModel(in0);    
    }

    public it.eng.spagobi.sdk.documents.bo.SDKTemplate downloadDatamartFile(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException {
    	DocumentsServiceImpl impl = new DocumentsServiceImpl();
      return impl.downloadDatamartFile(in0, in1);
    }

    public it.eng.spagobi.sdk.documents.bo.SDKTemplate downloadDatamartModelFiles(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException {
    	DocumentsServiceImpl impl = new DocumentsServiceImpl();
      return impl.downloadDatamartModelFiles(in0, in1, in2);
    }

    public java.util.HashMap getAllDatamartModels() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
    	DocumentsServiceImpl impl = new DocumentsServiceImpl();
    	return impl.getAllDatamartModels();
    }

    public void uploadMondrianSchema(it.eng.spagobi.sdk.documents.bo.SDKSchema in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
          DocumentsServiceImpl impl = new DocumentsServiceImpl();
        	impl.uploadMondrianSchema(in0);
    }

}
