/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.commonj.runtime;

import it.eng.spagobi.engines.commonj.utils.ProcessesStatusContainer;
import it.eng.spagobi.services.proxy.EventServiceProxy;
import it.eng.spagobi.utilities.engines.AuditServiceProxy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import commonj.work.WorkEvent;
import commonj.work.WorkException;
import commonj.work.WorkItem;
import commonj.work.WorkListener;


public class CommonjWorkListener implements WorkListener {


	public static final String COMMONJ_ROLES_HANDLER_CLASS_NAME = "it.eng.spagobi.engines.drivers.commonj.CommonjRolesHandler";
	public static final String COMMONJ_PRESENTAION_HANDLER_CLASS_NAME = "it.eng.spagobi.engines.drivers.commonj.CommonjEventPresentationHandler";
	public static final String BIOBJECT_ID = "biobjectId";
	public static final String BIOBJECT_LABEL = "biobjectLabel";
	public static final String USER_NAME = "userName";

	AuditServiceProxy auditServiceProxy;
	EventServiceProxy eventServiceProxy;
	String workName;
	String workClass;
	String executionRole;
	String biObjectID;
	String biObjectLabel;
	String pid;

	private static transient Logger logger = Logger.getLogger(CommonjWorkListener.class);

	public CommonjWorkListener(AuditServiceProxy auditServiceProxy, EventServiceProxy eventServiceProxy) {
		this.auditServiceProxy = auditServiceProxy;
		this.eventServiceProxy = eventServiceProxy;
	}




	public String getWorkClass() {
		return workClass;
	}

	public void setWorkClass(String workClass) {
		this.workClass = workClass;
	}

	public String getWorkName() {
		return workName;
	}

	public void setWorkName(String workName) {
		this.workName = workName;
	}

	public void workAccepted(WorkEvent event) {
		logger.info("IN.Work "+workName+" accepted");
		logger.debug("Work "+workName+" accepted");
		logger.info("OUT");
	}


	public void workRejected(WorkEvent event) {
		logger.info("IN.Work "+workName+" rejected");
		if(auditServiceProxy != null) {
			auditServiceProxy.notifyServiceErrorEvent("An error occurred while work execution");
		} else {
			logger.warn("Impossible to log START-EVENT because the audit proxy has not been instatiated properly");
		}
		if(eventServiceProxy != null) {
			String pars=builParametersString();
			eventServiceProxy.fireEvent("Event of work "+workName+" launching class "+workClass+": Error",pars,COMMONJ_ROLES_HANDLER_CLASS_NAME, COMMONJ_PRESENTAION_HANDLER_CLASS_NAME);	
		} else {
			logger.warn("Impossible to log ERROR-EVENT because the event proxy has not been instatiated properly");
		}				
		logger.debug("Work "+workName+" rejected");
		logger.info("OUT");
	}


	public void workCompleted(WorkEvent event) {
		logger.info("IN.Entering work "+workName+" completed");

		WorkException workException;
		//Work commonjWork;

		logger.info("IN");

		try {
			workException = event.getException();
			if (workException != null) {
				logger.error(workException); 
			}

			//commonjWork = (Work) event.getWorkItem().getResult();
			if (workException != null) {
				if(auditServiceProxy != null) {
					auditServiceProxy.notifyServiceErrorEvent("An error occurred while work execution");
				} else {
					logger.warn("Impossible to log START-EVENT because the audit proxy has not been instatiated properly");
				}
				if(eventServiceProxy != null) {
					String pars=builParametersString();					
					eventServiceProxy.fireEvent("Event of work "+workName+" launching class "+workClass+": Error",pars,COMMONJ_ROLES_HANDLER_CLASS_NAME, COMMONJ_PRESENTAION_HANDLER_CLASS_NAME);	
				} else {
					logger.warn("Impossible to log ERROR-EVENT because the event proxy has not been instatiated properly");
				}				
			} else {
				if(auditServiceProxy != null) {
					auditServiceProxy.notifyServiceEndEvent();
				} else {
					logger.warn("Impossible to log START-EVENT because the audit proxy has not been instatiated properly");
				}
				if(eventServiceProxy != null) {
					String pars=builParametersString();					
					eventServiceProxy.fireEvent("Event of work "+workName+" launching class "+workClass+": End",pars,COMMONJ_ROLES_HANDLER_CLASS_NAME, COMMONJ_PRESENTAION_HANDLER_CLASS_NAME);	
				} else {
					logger.warn("Impossible to log END-EVENT because the event proxy has not been instatiated properly");
				}
			}

		} catch (Throwable t) {
			throw new RuntimeException("An error occurred while handling process completed event");
		} finally {
			logger.debug("OUT");
		}

		// clean the singleton!!! 
		if(pid != null){
			ProcessesStatusContainer processesStatusContainer = ProcessesStatusContainer.getInstance();
			processesStatusContainer.getPidContainerMap().remove(pid);
			logger.debug("removed from singleton process item with pid "+pid);
		}
		logger.info("OUT");
	}

	public void workStarted(WorkEvent event) {
		logger.info("IN");
		logger.debug("Work "+workName+" started");

		if(auditServiceProxy != null) {
			auditServiceProxy.notifyServiceStartEvent();
		} else {
			logger.warn("Impossible to log START-EVENT because the audit proxy has not been instatiated properly");
		}

		WorkItem wi=event.getWorkItem();

		if(eventServiceProxy != null) {
			String pars=builParametersString();			
			eventServiceProxy.fireEvent("Event of work "+workName+" launching class "+workClass+": Started",pars,COMMONJ_ROLES_HANDLER_CLASS_NAME, COMMONJ_PRESENTAION_HANDLER_CLASS_NAME);	
		} else {
			logger.warn("Impossible to log START-EVENT because the event proxy has not been instatiated properly");
		}

		logger.info("OUT");


	}


	private String builParametersString(){
		logger.debug("IN");
		Map startEventParams = new HashMap();
		//startEventParams.put(EVENT_TYPE, DOCUMENT_EXECUTION_START);
		if(biObjectID!=null){
			startEventParams.put(BIOBJECT_ID, biObjectID);
		}
		if(biObjectLabel != null){
			startEventParams.put(BIOBJECT_LABEL, biObjectLabel);			
		}

		String startEventParamsStr = getParamsStr(startEventParams);
		logger.debug("OUT");
		return  startEventParamsStr;
	}

	String getExecutionRole() {
		return executionRole;
	}


	public void setExecutionRole(String executionRole) {
		this.executionRole = executionRole;
	}




	public String getBiObjectID() {
		return biObjectID;
	}




	public void setBiObjectID(String biObjectID) {
		this.biObjectID = biObjectID;
	}




	public String getBiObjectLabel() {
		return biObjectLabel;
	}




	public void setBiObjectLabel(String biObjectLabel) {
		this.biObjectLabel = biObjectLabel;
	}




	private String getParamsStr(Map params) {
		logger.debug("IN");
		StringBuffer buffer = new StringBuffer();
		Iterator it = params.keySet().iterator();
		boolean isFirstParameter = true;
		while (it.hasNext()) {
			String pname = (String) it.next();
			String pvalue = (String) params.get(pname);
			if (!isFirstParameter)
				buffer.append("&");
			else
				isFirstParameter = false;
			buffer.append(pname + "=" + pvalue);
		}
		logger.debug("parameters: " + buffer.toString());
		logger.debug("OUT");
		return buffer.toString();
	}

}
