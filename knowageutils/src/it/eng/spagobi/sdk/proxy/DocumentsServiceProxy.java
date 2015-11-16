/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.proxy;

import it.eng.spagobi.sdk.callbacks.ClientCredentialsHolder;
import it.eng.spagobi.sdk.documents.stub.DocumentsService;

import java.rmi.Remote;

import org.apache.axis.client.Stub;
import org.apache.ws.security.handler.WSHandlerConstants;

public class DocumentsServiceProxy extends AbstractSDKServiceProxy implements DocumentsService {
	private String _endpoint = null;
	private DocumentsService documentsService = null;
	private ClientCredentialsHolder cch = null;

	// public DocumentsServiceProxy() {
	// _initDocumentsServiceProxy();
	// }

	public DocumentsServiceProxy(String user, String pwd) {
		cch = new ClientCredentialsHolder(user, pwd);
		_initDocumentsServiceProxy();
	}

	public DocumentsServiceProxy(String endpoint) {
		_endpoint = endpoint;
		_initDocumentsServiceProxy();
	}

	private void _initDocumentsServiceProxy() {
		// try {
		// documentsService = (new
		// DocumentsServiceServiceLocator()).getDocumentsService();
		// if (documentsService != null) {
		// if (_endpoint != null)
		// ((javax.xml.rpc.Stub)
		// documentsService)._setProperty("javax.xml.rpc.service.endpoint.address",
		// _endpoint);
		// else
		// _endpoint = (String) ((javax.xml.rpc.Stub)
		// documentsService)._getProperty("javax.xml.rpc.service.endpoint.address");
		// }
		//
		// } catch (javax.xml.rpc.ServiceException serviceException) {
		// }
		try {
			it.eng.spagobi.sdk.documents.stub.DocumentsServiceServiceLocator locator = new it.eng.spagobi.sdk.documents.stub.DocumentsServiceServiceLocator();
			Remote remote = locator.getPort(it.eng.spagobi.sdk.documents.stub.DocumentsService.class);
			Stub axisPort = (Stub) remote;
			axisPort._setProperty(WSHandlerConstants.USER, cch.getUsername());
			axisPort._setProperty(WSHandlerConstants.PW_CALLBACK_REF, cch);
			// axisPort.setTimeout(30000); //used in SpagoBIStudio
			documentsService = (it.eng.spagobi.sdk.documents.stub.DocumentsService) axisPort;
			if (documentsService != null) {
				if (_endpoint != null)
					((javax.xml.rpc.Stub) documentsService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
				else
					_endpoint = (String) ((javax.xml.rpc.Stub) documentsService)._getProperty("javax.xml.rpc.service.endpoint.address");
			}

		} catch (javax.xml.rpc.ServiceException serviceException) {
		}

	}

	public String getEndpoint() {
		return _endpoint;
	}

	public void setEndpoint(String endpoint) {
		_endpoint = endpoint;
		if (documentsService != null)
			((javax.xml.rpc.Stub) documentsService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);

	}

	public DocumentsService getDocumentsService() {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService;
	}

	public it.eng.spagobi.sdk.documents.bo.SDKDocument[] getDocumentsAsList(java.lang.String in0, java.lang.String in1, java.lang.String in2)
			throws java.rmi.RemoteException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService.getDocumentsAsList(in0, in1, in2);
	}

	public it.eng.spagobi.sdk.documents.bo.SDKDocument getDocumentById(java.lang.Integer in0) throws java.rmi.RemoteException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService.getDocumentById(in0);
	}

	public it.eng.spagobi.sdk.documents.bo.SDKDocument getDocumentByLabel(java.lang.String in0) throws java.rmi.RemoteException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService.getDocumentByLabel(in0);
	}

	public it.eng.spagobi.sdk.documents.bo.SDKFunctionality getDocumentsAsTree(java.lang.String in0) throws java.rmi.RemoteException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService.getDocumentsAsTree(in0);
	}

	public java.lang.String[] getCorrectRolesForExecution(java.lang.Integer in0) throws java.rmi.RemoteException,
			it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService.getCorrectRolesForExecution(in0);
	}

	public it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter[] getDocumentParameters(java.lang.Integer in0, java.lang.String in1)
			throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService.getDocumentParameters(in0, in1);
	}

	public it.eng.spagobi.sdk.documents.bo.SDKDocumentParameterValue[] getAdmissibleValues(java.lang.Integer in0, java.lang.String in1)
			throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService.getAdmissibleValues(in0, in1);
	}

	public it.eng.spagobi.sdk.documents.bo.SDKDocumentParameterValue[] getDefaultValues(java.lang.Integer in0, java.lang.String in1)
			throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService.getDefaultValues(in0, in1);
	}

	public it.eng.spagobi.sdk.documents.bo.SDKTemplate downloadTemplate(java.lang.Integer in0) throws java.rmi.RemoteException,
			it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService.downloadTemplate(in0);
	}

	public void uploadTemplate(java.lang.Integer in0, it.eng.spagobi.sdk.documents.bo.SDKTemplate in1) throws java.rmi.RemoteException,
			it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		documentsService.uploadTemplate(in0, in1);
	}

	public java.lang.Integer saveNewDocument(it.eng.spagobi.sdk.documents.bo.SDKDocument in0, it.eng.spagobi.sdk.documents.bo.SDKTemplate in1,
			java.lang.Integer in2) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService.saveNewDocument(in0, in1, in2);
	}

	public it.eng.spagobi.sdk.documents.bo.SDKExecutedDocumentContent executeDocument(it.eng.spagobi.sdk.documents.bo.SDKDocument in0,
			it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter[] in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException,
			it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException, it.eng.spagobi.sdk.exceptions.InvalidParameterValue,
			it.eng.spagobi.sdk.exceptions.MissingParameterValue, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService.executeDocument(in0, in1, in2, in3);
	}

	public void uploadDatamartTemplate(it.eng.spagobi.sdk.documents.bo.SDKTemplate in0, it.eng.spagobi.sdk.documents.bo.SDKTemplate in1, java.lang.String in2,
			java.lang.String in3) throws java.rmi.RemoteException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		documentsService.uploadDatamartTemplate(in0, in1, in2, in3);
	}

	public void uploadDatamartModel(it.eng.spagobi.sdk.documents.bo.SDKTemplate in0) throws java.rmi.RemoteException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		documentsService.uploadDatamartModel(in0);
	}

	public it.eng.spagobi.sdk.documents.bo.SDKTemplate downloadDatamartFile(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService.downloadDatamartFile(in0, in1);
	}

	public it.eng.spagobi.sdk.documents.bo.SDKTemplate downloadDatamartModelFiles(java.lang.String in0, java.lang.String in1, java.lang.String in2)
			throws java.rmi.RemoteException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService.downloadDatamartModelFiles(in0, in1, in2);
	}

	public java.util.HashMap getAllDatamartModels() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		return documentsService.getAllDatamartModels();
	}

	public void uploadMondrianSchema(it.eng.spagobi.sdk.documents.bo.SDKSchema in0) throws java.rmi.RemoteException,
			it.eng.spagobi.sdk.exceptions.SDKException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
		if (documentsService == null)
			_initDocumentsServiceProxy();
		documentsService.uploadMondrianSchema(in0);
	}

}