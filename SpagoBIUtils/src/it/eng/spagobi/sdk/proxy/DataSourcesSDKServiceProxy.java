/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.proxy;

import java.rmi.Remote;

import org.apache.axis.client.Stub;
import org.apache.ws.security.handler.WSHandlerConstants;

import it.eng.spagobi.sdk.callbacks.ClientCredentialsHolder;
import it.eng.spagobi.sdk.datasources.stub.DataSourcesSDKService;
import it.eng.spagobi.sdk.datasources.stub.DataSourcesSDKServiceServiceLocator;


public class DataSourcesSDKServiceProxy extends AbstractSDKServiceProxy implements DataSourcesSDKService {
	private String _endpoint = null;
	private DataSourcesSDKService dataSourcesSDKService = null;
	private ClientCredentialsHolder cch = null;


	public DataSourcesSDKServiceProxy() {
		_initDataSourcesSDKServiceProxy();
	}

//	public DataSourcesSDKServiceProxy(String endpoint) {
//	_endpoint = endpoint;
//	_initDataSourcesSDKServiceProxy();
//	}


	public DataSourcesSDKServiceProxy(String user, String pwd) {
		cch = new ClientCredentialsHolder(user, pwd);
		_initDataSourcesSDKServiceProxy();
	}


	private void _initDataSourcesSDKServiceProxy() {
		try {
			it.eng.spagobi.sdk.datasources.stub.DataSourcesSDKServiceServiceLocator locator = new it.eng.spagobi.sdk.datasources.stub.DataSourcesSDKServiceServiceLocator();
			Remote remote = locator.getPort(it.eng.spagobi.sdk.datasources.stub.DataSourcesSDKService.class);
			Stub axisPort = (Stub) remote;
			axisPort._setProperty(WSHandlerConstants.USER, cch.getUsername());
			axisPort._setProperty(WSHandlerConstants.PW_CALLBACK_REF, cch);
			//axisPort.setTimeout(30000); //used in SpagoBIStudio
			dataSourcesSDKService = (it.eng.spagobi.sdk.datasources.stub.DataSourcesSDKService) axisPort;        
			//dataSourcesSDKService = (new DataSourcesSDKServiceServiceLocator()).getDataSourcesSDKService();
			if (dataSourcesSDKService != null) {
				if (_endpoint != null)
					((javax.xml.rpc.Stub)dataSourcesSDKService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
				else
					_endpoint = (String)((javax.xml.rpc.Stub)dataSourcesSDKService)._getProperty("javax.xml.rpc.service.endpoint.address");
			}

		}
		catch (javax.xml.rpc.ServiceException serviceException) {}
	}

	public String getEndpoint() {
		return _endpoint;
	}

	public void setEndpoint(String endpoint) {
		_endpoint = endpoint;
		if (dataSourcesSDKService != null)
			((javax.xml.rpc.Stub)dataSourcesSDKService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);

	}

	public DataSourcesSDKService getDataSourcesSDKService() {
		if (dataSourcesSDKService == null)
			_initDataSourcesSDKServiceProxy();
		return dataSourcesSDKService;
	}

	public it.eng.spagobi.sdk.datasources.bo.SDKDataSource getDataSource(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException{
		if (dataSourcesSDKService == null)
			_initDataSourcesSDKServiceProxy();
		return dataSourcesSDKService.getDataSource(in0);
	}

	public it.eng.spagobi.sdk.datasources.bo.SDKDataSource[] getDataSources() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException{
		if (dataSourcesSDKService == null)
			_initDataSourcesSDKServiceProxy();
		return dataSourcesSDKService.getDataSources();
	}


}