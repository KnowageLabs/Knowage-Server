/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.engines.commonj.runtime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import commonj.work.WorkEvent;
import commonj.work.WorkException;
import commonj.work.WorkItem;
import commonj.work.WorkListener;
import it.eng.spagobi.engines.commonj.utils.ProcessesStatusContainer;
import it.eng.spagobi.services.proxy.EventServiceProxy;
import it.eng.spagobi.utilities.engines.AuditServiceProxy;


public class CommonjWorkListener implements WorkListener {

	private static final Logger LOGGER = Logger.getLogger(CommonjWorkListener.class);

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

	@Override
	public void workAccepted(WorkEvent event) {
		LOGGER.info("IN.Work "+workName+" accepted");
		LOGGER.debug("Work "+workName+" accepted");
		LOGGER.info("OUT");
	}


	@Override
	public void workRejected(WorkEvent event) {
		LOGGER.info("IN.Work "+workName+" rejected");
		if(auditServiceProxy != null) {
			auditServiceProxy.notifyServiceErrorEvent("An error occurred while work execution");
		} else {
			LOGGER.warn("Impossible to log START-EVENT because the audit proxy has not been instatiated properly");
		}
		if(eventServiceProxy != null) {
			String pars=builParametersString();
			eventServiceProxy.fireEvent("Event of work "+workName+" launching class "+workClass+": Error",pars,COMMONJ_ROLES_HANDLER_CLASS_NAME, COMMONJ_PRESENTAION_HANDLER_CLASS_NAME);
		} else {
			LOGGER.warn("Impossible to log ERROR-EVENT because the event proxy has not been instatiated properly");
		}
		LOGGER.debug("Work "+workName+" rejected");
		LOGGER.info("OUT");
	}


	@Override
	public void workCompleted(WorkEvent event) {
		LOGGER.info("IN.Entering work "+workName+" completed");

		WorkException workException;

		LOGGER.info("IN");

		try {
			workException = event.getException();
			if (workException != null) {
				LOGGER.error(workException);
			}

			if (workException != null) {
				if(auditServiceProxy != null) {
					auditServiceProxy.notifyServiceErrorEvent("An error occurred while work execution");
				} else {
					LOGGER.warn("Impossible to log START-EVENT because the audit proxy has not been instatiated properly");
				}
				if(eventServiceProxy != null) {
					String pars=builParametersString();
					eventServiceProxy.fireEvent("Event of work "+workName+" launching class "+workClass+": Error",pars,COMMONJ_ROLES_HANDLER_CLASS_NAME, COMMONJ_PRESENTAION_HANDLER_CLASS_NAME);
				} else {
					LOGGER.warn("Impossible to log ERROR-EVENT because the event proxy has not been instatiated properly");
				}
			} else {
				if(auditServiceProxy != null) {
					auditServiceProxy.notifyServiceEndEvent();
				} else {
					LOGGER.warn("Impossible to log START-EVENT because the audit proxy has not been instatiated properly");
				}
				if(eventServiceProxy != null) {
					String pars=builParametersString();
					eventServiceProxy.fireEvent("Event of work "+workName+" launching class "+workClass+": End",pars,COMMONJ_ROLES_HANDLER_CLASS_NAME, COMMONJ_PRESENTAION_HANDLER_CLASS_NAME);
				} else {
					LOGGER.warn("Impossible to log END-EVENT because the event proxy has not been instatiated properly");
				}
			}

		} catch (Throwable t) {
			throw new RuntimeException("An error occurred while handling process completed event");
		} finally {
			LOGGER.debug("OUT");
		}

		// clean the singleton!!!
		if(pid != null){
			ProcessesStatusContainer processesStatusContainer = ProcessesStatusContainer.getInstance();
			processesStatusContainer.getPidContainerMap().remove(pid);
			LOGGER.debug("removed from singleton process item with pid "+pid);
		}
		LOGGER.info("OUT");
	}

	@Override
	public void workStarted(WorkEvent event) {
		LOGGER.info("IN");
		LOGGER.debug("Work "+workName+" started");

		if(auditServiceProxy != null) {
			auditServiceProxy.notifyServiceStartEvent();
		} else {
			LOGGER.warn("Impossible to log START-EVENT because the audit proxy has not been instatiated properly");
		}

		WorkItem wi = event.getWorkItem();

		if(eventServiceProxy != null) {
			String pars=builParametersString();
			eventServiceProxy.fireEvent("Event of work "+workName+" launching class "+workClass+": Started",pars,COMMONJ_ROLES_HANDLER_CLASS_NAME, COMMONJ_PRESENTAION_HANDLER_CLASS_NAME);
		} else {
			LOGGER.warn("Impossible to log START-EVENT because the event proxy has not been instatiated properly");
		}

		LOGGER.info("OUT");


	}


	private String builParametersString(){
		LOGGER.debug("IN");
		Map startEventParams = new HashMap();
		if(biObjectID!=null){
			startEventParams.put(BIOBJECT_ID, biObjectID);
		}
		if(biObjectLabel != null){
			startEventParams.put(BIOBJECT_LABEL, biObjectLabel);
		}

		String startEventParamsStr = getParamsStr(startEventParams);
		LOGGER.debug("OUT");
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
		LOGGER.debug("IN");
		StringBuilder buffer = new StringBuilder();
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
		LOGGER.debug("parameters: " + buffer.toString());
		LOGGER.debug("OUT");
		return buffer.toString();
	}

}
