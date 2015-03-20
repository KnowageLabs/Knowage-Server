/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.dataproxy;

import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.wsconnectors.stub.IWsConnector;
import it.eng.spagobi.tools.dataset.wsconnectors.stub.IWsConnectorServiceLocator;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class WebServiceDataProxy extends AbstractDataProxy {

	String address;
	String operation;

	private static transient Logger logger = Logger.getLogger(WebServiceDataProxy.class);
	public static final String messageBundle = "web_service_dataset";


	public WebServiceDataProxy() {

	}

	public WebServiceDataProxy(String address, String operation, String executorClass) {
		this.setAddress(address);
		this.setOperation(operation);
	}

	public IDataStore load(String statement, IDataReader dataReader){
		throw new UnsupportedOperationException("metothd load not yet implemented");
	}


	public IDataStore load(IDataReader dataReader) {
		IDataStore dataStore = null;
		String resultXML="";
		IWsConnectorServiceLocator locator = new IWsConnectorServiceLocator();   
		IWsConnector connector=null;
		URL addressToCall=null;


		try{
			addressToCall=new URL(address);
		}
		catch (Exception e) {
			throw new SpagoBIRuntimeException("Invalid URL [" + address + "]", e);
		}
		try {
			connector = locator.getWSDataSetService(addressToCall);

		} catch (ServiceException e) {
			throw new SpagoBIRuntimeException("DataSetService not available", e);
		}
		
		// Add the profile Attributes

		try {
			parameters = addProfileAtributes(parameters);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An error occurred while resolving profile attributes", e);
		}
		
		try {
			resultXML=connector.readDataSet(address, parameters, operation);
		} catch (RemoteException e) {
			throw new SpagoBIRuntimeException("DataSetService not available", e);
		}		

		try {
			dataStore = dataReader.read(resultXML);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to parse responde [" + resultXML + "]", e);
		}
		
		return dataStore;

	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public Map addProfileAtributes(Map mapNameValue) throws EMFInternalError{
		if(mapNameValue==null) mapNameValue=new HashMap();
		Set names=(Set)profile.keySet();
		for (Iterator iterator = names.iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			String value=(String)profile.get(name);
			mapNameValue.put(name, value);
		}
		return mapNameValue;
	}




}
