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
package it.eng.spagobi.engines.commonj.services;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import commonj.work.Work;
import commonj.work.WorkEvent;
import commonj.work.WorkItem;

import de.myfoo.commonj.work.FooRemoteWorkItem;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.commonj.runtime.CommonjWorkContainer;
import it.eng.spagobi.engines.commonj.runtime.CommonjWorkListener;
import it.eng.spagobi.engines.commonj.utils.GeneralUtils;
import it.eng.spagobi.engines.commonj.utils.ProcessesStatusContainer;
import it.eng.spagobi.utilities.engines.AbstractEngineAction;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONFailure;
import it.eng.spagobi.utilities.service.JSONSuccess;
import it.eng.spagobi.utilities.threadmanager.WorkManager;


public class StatusWorkAction extends AbstractEngineAction {

	private static transient Logger logger = Logger.getLogger(StatusWorkAction.class);


	@Override
	public void init(SourceBean config) {
		// TODO Auto-generated method stub
		super.init(config);
	}

	@Override
	public void service(SourceBean request, SourceBean response) {
		logger.debug("IN");


		JSONObject info=null;
		Object pidO=request.getAttribute("PROCESS_ID");
		String pid="";
		if(pidO!=null){
			pid=pidO.toString();

		}
		else{   // if pidO not found just return an empty xml Object
			try {

				info=GeneralUtils.buildJSONObject(pid,0);
				writeBackToClient( new JSONSuccess(info));
			} catch (Exception e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

			return;


		}

		super.service(request, response);
		HttpSession session=getHttpSession();

		// Get document id, must find
//		String document_id=null;
//		Object document_idO=null;
//		document_idO=request.getAttribute("DOCUMENT_ID");
//		if(document_idO!=null){
//			document_id=document_idO.toString();
//		}
//		else{
//			document_id="";
//			logger.error("could not retrieve document id");
//			throw new SpagoBIEngineServiceException(getActionName(), "could not find document id");
//		}

		CommonjWorkContainer container=null;
		ProcessesStatusContainer processesStatusContainer = ProcessesStatusContainer.getInstance();
		Object o=processesStatusContainer.getPidContainerMap().get(pid);
		//recover from session, if does not find means work is completed
		//Object o=session.getAttribute("SBI_PROCESS_"+document_id);
		try{
			int statusWI;

			if(o!=null){			// object found in session, work could be not started, running or completed

				container=(CommonjWorkContainer)o;
				FooRemoteWorkItem fooRwi=container.getFooRemoteWorkItem();
				WorkItem wi=container.getWorkItem();

				// if WorkItem is not set means work has never been started
				if(fooRwi!=null && wi!=null){
					statusWI=wi.getStatus();
					// if finds that work is finished delete the attribute from session
					if(statusWI==WorkEvent.WORK_COMPLETED){
						logger.debug("Work is finished - remove from session");
						//session.removeAttribute("SBI_PROCESS_"+document_id);
						processesStatusContainer.getPidContainerMap().remove(pid);
					}
				}
				else{
					// if not workitem is set means that is not started yet or has been cancelled by listener!?!
					statusWI=0;
				}
			}
			else{
				// No more present in session, so it has been deleted
				statusWI=WorkEvent.WORK_COMPLETED;
			}

			info=GeneralUtils.buildJSONObject(pid,statusWI);
			logger.debug(GeneralUtils.getEventMessage(statusWI));
			try {
				writeBackToClient( new JSONSuccess(info));
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

		}
		catch (Exception e) {
			logger.error("Error in reading work status");
			try {
				writeBackToClient( new JSONFailure( e) );
			} catch (IOException e1) {
				logger.error("Error in reading work status and in writing back to client",e);
				throw new SpagoBIEngineServiceException(getActionName(), "Error in reading work status and in writing back to client", e1);
			} catch (JSONException e1) {
				logger.error("Error in reading work status and in writing back to client",e);
				throw new SpagoBIEngineServiceException(getActionName(), "Error in reading work status and in writing back to client", e1);
			}
		}	
		logger.debug("OUT");

	}

}
