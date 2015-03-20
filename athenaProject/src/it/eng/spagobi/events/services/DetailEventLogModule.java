/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.events.services;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.events.bo.EventLog;
import it.eng.spagobi.events.dao.EventLogDAOHibImpl;
import it.eng.spagobi.events.handlers.IEventPresentationHandler;

import java.util.HashMap;
import java.util.Vector;

/**
 * @author Gioia
 *
 */
public class DetailEventLogModule extends AbstractModule {
	
	public static final String MODULE_PAGE = "DetailEventLogPage";
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.AbstractModule#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {	
		
		String message = (String) request.getAttribute("MESSAGEDET");
		SpagoBITracer.debug(SpagoBIConstants.NAME_MODULE, this.getClass().getName(),"service","begin of detail EventLog service with message = " + message);
		
		EMFErrorHandler errorHandler = getErrorHandler();
		try {
			if (message == null) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);
				SpagoBITracer.debug(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "service", "The message parameter is null");
				throw userError;
			}
			String id = (String) request.getAttribute("id");
			if (message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_SELECT)) {
				getDetailEventLog(id, response);
			} else if (message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_DEL)) {
				delDetailEventLog(id, response);
			}
		} catch (EMFUserError eex) {
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			errorHandler.addError(internalError);
			return;
		}
	}
	
	private void getDetailEventLog(String idStr, SourceBean response) throws EMFUserError {
		try {
			Integer id = new Integer(idStr);
			EventLogDAOHibImpl eventLogDAO = new EventLogDAOHibImpl();
			EventLog event = eventLogDAO.loadEventLogById(id);
			if (event == null) {
				HashMap params = new HashMap();
				params.put(AdmintoolsConstants.PAGE, "EVENTS_MONITOR_PAGE");
				params.put("REFRESH", "TRUE");
				params.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
				Vector v = new Vector();
				v.add(idStr);
				EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 1067, v, params);
				throw error;
			}
			String presentationHandler = event.getHandler();
			SpagoBITracer.debug(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "getDetailEventLog", "The presentation hanlder for event with id = " + idStr + " is : " + presentationHandler);
			Class presentationHandlerClass = Class.forName(presentationHandler);
			IEventPresentationHandler eventPresentationHanlder = (IEventPresentationHandler) presentationHandlerClass.newInstance();
			eventPresentationHanlder.loadEventInfo(event, response);
		} catch (EMFUserError error) {
			throw error;
		} catch (Exception ex) {
			SpagoBITracer.major(AdmintoolsConstants.NAME_MODULE, this.getClass().getName(), "getDetailEventLog", "Cannot fill response container", ex);
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, "EVENTS_MONITOR_PAGE");
			params.put("REFRESH", "TRUE");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 1065, new Vector(), params);
		}
	}
	
	private void delDetailEventLog(String idStr, SourceBean response) throws EMFUserError, SourceBeanException  {
		try {
			EventLogDAOHibImpl eventLogDAO = new EventLogDAOHibImpl();
			Integer id = new Integer(idStr);
			EventLog eventLog = new EventLog();
			eventLog.setId(id);
			eventLogDAO.eraseEventLog(eventLog);
		} catch (EMFUserError e){
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, "EVENTS_MONITOR_PAGE");
			params.put("REFRESH", "TRUE");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 1066, new Vector(), params);
		} catch (Exception ex) {
			SpagoBITracer.major(AdmintoolsConstants.NAME_MODULE, this.getClass().getName(), "delDetailEventLog", "Cannot fill response container", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		response.setAttribute("PUBLISHER_NAME", "deletedEventLogLoopPublisher");
	}
	
}
