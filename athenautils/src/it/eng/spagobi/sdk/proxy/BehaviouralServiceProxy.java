/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.proxy;

import it.eng.spagobi.sdk.behavioural.stub.BehaviouralService;
import it.eng.spagobi.sdk.callbacks.ClientCredentialsHolder;

import java.rmi.Remote;

import org.apache.axis.client.Stub;
import org.apache.ws.security.handler.WSHandlerConstants;

public class BehaviouralServiceProxy extends AbstractSDKServiceProxy implements BehaviouralService {
	private String _endpoint = null;
	private BehaviouralService behaviouralService = null;
	private ClientCredentialsHolder cch = null;

	// public BehaviouralServiceProxy() {
	// _initBehaviouralServiceProxy();
	// }

	public BehaviouralServiceProxy(String user, String pwd) {
		cch = new ClientCredentialsHolder(user, pwd);
		_initBehaviouralServiceProxy();
	}

	public BehaviouralServiceProxy(String endpoint) {
		_endpoint = endpoint;
		_initBehaviouralServiceProxy();
	}

	private void _initBehaviouralServiceProxy() {
		// try {
		// behaviouralService = (new
		// spagobisdkbehavioural.BehaviouralServiceServiceLocator()).getBehaviouralService();
		// if (behaviouralService != null) {
		// if (_endpoint != null)
		// ((javax.xml.rpc.Stub)
		// behaviouralService)._setProperty("javax.xml.rpc.service.endpoint.address",
		// _endpoint);
		// else
		// _endpoint = (String) ((javax.xml.rpc.Stub)
		// behaviouralService)._getProperty("javax.xml.rpc.service.endpoint.address");
		// }
		//
		// } catch (javax.xml.rpc.ServiceException serviceException) {
		// }

		try {
			it.eng.spagobi.sdk.behavioural.stub.BehaviouralServiceServiceLocator locator = new it.eng.spagobi.sdk.behavioural.stub.BehaviouralServiceServiceLocator();
			Remote remote = locator.getPort(it.eng.spagobi.sdk.behavioural.stub.BehaviouralService.class);
			Stub axisPort = (Stub) remote;
			axisPort._setProperty(WSHandlerConstants.USER, cch.getUsername());
			axisPort._setProperty(WSHandlerConstants.PW_CALLBACK_REF, cch);
			// axisPort.setTimeout(30000); //used in SpagoBIStudio
			behaviouralService = (it.eng.spagobi.sdk.behavioural.stub.BehaviouralService) axisPort;
			if (behaviouralService != null) {
				if (_endpoint != null)
					((javax.xml.rpc.Stub) behaviouralService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
				else
					_endpoint = (String) ((javax.xml.rpc.Stub) behaviouralService)._getProperty("javax.xml.rpc.service.endpoint.address");
			}

		} catch (javax.xml.rpc.ServiceException serviceException) {
		}

	}

	public String getEndpoint() {
		return _endpoint;
	}

	public void setEndpoint(String endpoint) {
		_endpoint = endpoint;
		if (behaviouralService != null)
			((javax.xml.rpc.Stub) behaviouralService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);

	}

	public BehaviouralService getBehaviouralService() {
		if (behaviouralService == null)
			_initBehaviouralServiceProxy();
		return behaviouralService;
	}

	public it.eng.spagobi.sdk.behavioural.bo.SDKAttribute[] getAllAttributes(java.lang.String in0) throws java.rmi.RemoteException,
			it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
		if (behaviouralService == null)
			_initBehaviouralServiceProxy();
		return behaviouralService.getAllAttributes(in0);
	}

}