/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.core;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.engines.qbe.externalservices.ExternalServiceConfiguration;
import it.eng.spagobi.services.exportrecords.proxy.ExportRecordsServiceProxy;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONFailure;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;


/**
 * The Class InvokeExternalServiceAction.
 */
public class InvokeExternalServiceAction extends AbstractQbeEngineAction {	
	
	// INPUT PARAMETERS
	public static final String SERVICE_ID = "id";
	public static final String RECORDS = "records";
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(InvokeExternalServiceAction.class);
    
	
	public void service(SourceBean request, SourceBean response)  {				
		
		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;
		Monitor extServiceMonitor = null;
		
		logger.debug("IN");
		
		try {
		
			super.service(request, response);	
			
			totalTimeMonitor = MonitorFactory.start("QbeEngine.invokeExternalServiceAction.totalTime");
			
			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");
			
			ExternalServiceConfiguration serviceConfig = this.getExternalServiceConfiguration();
			Assert.assertNotNull(serviceConfig, "Required service not found!!!");
			
			JSONArray records = this.getAttributeAsJSONArray(RECORDS);
			Assert.assertNotNull(records, "Parameter [" + RECORDS + "] cannot be null in oder to execute " + this.getActionName() + " service");

			// if no records are selected, send a message to the client
			if (records.length() == 0) {
				try {
					writeBackToClient( new JSONFailure(new SpagoBIEngineServiceException(getActionName(), "No records selected!!")) );
					return;
				} catch (IOException e) {
					String message = "Impossible to write back the responce to the client";
					throw new SpagoBIEngineServiceException(getActionName(), message, e);
				}
			}
			
			// if some columns are missing, send a message to the client
			List<String> missingColumns = getMissingColumns(serviceConfig, records);
			logger.debug("Missing columns : " + join(missingColumns, ","));
			if (missingColumns.size() > 0) {
				logger.debug("The following columns are required in order to invoke " +
						"the required service: " + join(missingColumns, ","));
				try {
					JSONObject obj = new JSONObject();
					obj.put("missingcolumns", join(missingColumns, ","));
					writeBackToClient( new JSONSuccess(obj) );
					return;
				} catch (IOException e) {
					String message = "Impossible to write back the responce to the client";
					throw new SpagoBIEngineServiceException(getActionName(), message, e);
				}
			}
			
			// invoke external service
			String serviceResponse = null;
			try {
				extServiceMonitor = MonitorFactory.start("QbeEngine.externalService");
				serviceResponse = invokeExternalService(serviceConfig, records);
			} finally {
				extServiceMonitor.stop();
			}
			logger.debug("Service has returned this message response:\n" + serviceResponse);
			
			try {
				JSONObject obj = new JSONObject();
				obj.put("serviceresponse", serviceResponse);
				writeBackToClient( new JSONSuccess(obj) );
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}
			
		} catch(Throwable t) {
			errorHitsMonitor = MonitorFactory.start("QbeEngine.errorHits");
			errorHitsMonitor.stop();
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			if(totalTimeMonitor != null) totalTimeMonitor.stop();
			logger.debug("OUT");
		}	
		
		
	}
	
	private String invokeExternalService(
			ExternalServiceConfiguration serviceConfig, JSONArray records) throws RemoteException {
		ExportRecordsServiceProxy proxy = new ExportRecordsServiceProxy();
		proxy.setEndpoint(serviceConfig.getEndpoint());
		SourceBean sb = (SourceBean) ConfigSingleton.getInstance().getAttribute("QBE.EXTERNAL_SERVICES");
		int timeout = Integer.parseInt((String) sb.getAttribute("timeout"));
		proxy.setTimeout(timeout);
		String result = null;
		try {
			result = proxy.processRecords(records.toString(), serviceConfig.getOperation());
		} catch (RemoteException e) {
			if (e.detail != null && e.detail instanceof SocketTimeoutException) {
				throw new SpagoBIEngineServiceException(getActionName(), "Service invocation produced a timeout error", e);
			} else {
				throw e;
			}
		}
		return result;
	}

	private List<String> getMissingColumns(ExternalServiceConfiguration serviceConfig, 
			JSONArray records) throws JSONException {
		List<String> toReturn = new ArrayList<String>();
		JSONObject aRecord = records.getJSONObject(0);
		String[] requiredColumns = serviceConfig.getRequiredColumns();
		if (requiredColumns != null && requiredColumns.length > 0) {
			for (int i = 0; i < requiredColumns.length; i++) {
				String aRequiredColumn = requiredColumns[i];
				if (!aRecord.has(aRequiredColumn)) {
					toReturn.add(aRequiredColumn);
				}
			}
		}
		return toReturn;
	}

	private ExternalServiceConfiguration getExternalServiceConfiguration() {
		try {
			logger.debug("IN");
			ExternalServiceConfiguration toReturn = null;
			String serviceId = this.getAttributeAsString(SERVICE_ID);
			logger.debug("Parameter [" + SERVICE_ID + "] is equals to [" + serviceId + "]");
			Assert.assertNotNull(serviceId, "Parameter [" + SERVICE_ID + "] cannot be null in oder to execute " + this.getActionName() + " service");
			List<ExternalServiceConfiguration> services = getEngineInstance().getTemplate().getExternalServiceConfigurations();
			Iterator<ExternalServiceConfiguration> it = services.iterator();
			while (it.hasNext()) {
				ExternalServiceConfiguration temp = it.next();
				if (temp.getId().equals(serviceId)) {
					logger.debug("Required service found");
					toReturn = temp;
					break;
				}
			}
			return toReturn;
		} finally {
			logger.debug("OUT");
		}
	}
	
    public static String join(Collection s, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }


}
