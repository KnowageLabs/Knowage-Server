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

package it.eng.spagobi.engines.drivers.weka.events.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.BIObjectDAOHibImpl;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.SubreportDAOHibImpl;
import it.eng.spagobi.commons.bo.Subreport;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.events.EventsManager;
import it.eng.spagobi.events.bo.EventLog;
import it.eng.spagobi.events.handlers.IEventPresentationHandler;
import it.eng.spagobi.utilities.assertion.Assert;

public class WekaEventPresentationHandler implements IEventPresentationHandler {

	private static String EVENT_PNAME = "firedEvent";
	private static String START_EVENT_ID_PNAME = "startEventId";
	private static String OPERATION_TYPE_PNAME = "operation-type";
	private static String OPERATION_OUTPUT_PNAME = "operation-output";
	private static String OPERATION_RESULT_PNAME = "operation-result";
	private static String DOCUMENT_ID_PNAME = "document";
	private static String DOCUMENT_PNAME = "biobject";
	private static String LINKED_DOCUMENTS_PNAME = "linkedBIObjects";
	private static String ENGINE_BASE_URL_PNAME = "engineBaseUrl";
	
	private static transient Logger logger = Logger.getLogger(WekaEventPresentationHandler.class);
	
	
	public void loadEventInfo(EventLog event, SourceBean response) throws SourceBeanException, EMFUserError {
		Map eventParams;
		String startEventId;
		String documentId;
		BIObject document;
		List<BIObject> linkedDocuments;
		String operationType;
		String operationOutput;
		String operationResult;
		String engineBaseUrl;
		
		logger.debug("IN");
		
		try {
			eventParams = EventsManager.parseParamsStr(event.getParams());
			
			startEventId = (String) eventParams.get(START_EVENT_ID_PNAME);
			operationType = (String) eventParams.get(OPERATION_TYPE_PNAME);
			operationOutput = (String) eventParams.get(OPERATION_OUTPUT_PNAME);
			operationResult = (String) eventParams.get(OPERATION_RESULT_PNAME);
			
			
			documentId = (String) eventParams.get(DOCUMENT_ID_PNAME);
			document = getDocument(documentId);
			linkedDocuments = getLinkedObject(document);
			engineBaseUrl = getEngineBaseUrl(document);
			
			
			response.setAttribute(EVENT_PNAME, event);
			response.setAttribute(DOCUMENT_PNAME, document);
			response.setAttribute(LINKED_DOCUMENTS_PNAME, linkedDocuments);
			if (startEventId != null) {
				response.setAttribute(START_EVENT_ID_PNAME, startEventId);
			} 
			if(operationType != null) {
				response.setAttribute(OPERATION_TYPE_PNAME, operationType);
			}
			if(operationOutput != null) {
				response.setAttribute(OPERATION_OUTPUT_PNAME, operationOutput);
			}
			if(operationResult != null) {
				response.setAttribute(OPERATION_RESULT_PNAME, operationResult);
			}
			response.setAttribute(ENGINE_BASE_URL_PNAME, engineBaseUrl);
			
			response.setAttribute("PUBLISHER_NAME", "WekaExecutionEventLogDetailPublisher");
		} catch(Throwable t) {
			logger.error("Impossible to process event", t);
			if(t instanceof EMFUserError) throw (EMFUserError)t;		
			if(t instanceof SourceBeanException) throw (SourceBeanException)t;	
		} finally {
			logger.debug("OUT");
		}
		
	}
	
	protected BIObject getDocument(String documentId) throws EMFUserError {
		IBIObjectDAO biObjectDAO;
		Integer biObjectId;
		BIObject biObject;
		
		biObject = null;
		try {
			Assert.assertNotNull(documentId, "Parameter [documentId] cannot be null");
			
			
			biObjectDAO = DAOFactory.getBIObjectDAO();
			biObjectId = new Integer(documentId);
			biObject = biObjectDAO.loadBIObjectById(biObjectId);
		} catch(Throwable t) {
			logger.error("Impossible to load document with id equals to [" + documentId + "]", t);
			if(t instanceof EMFUserError) throw (EMFUserError)t;					
		}
		
		return biObject;
	}
	
	protected List<BIObject> getLinkedObject(BIObject document) throws EMFUserError {
		IBIObjectDAO biObjectDAO;
		SubreportDAOHibImpl subreportDAOHibImpl;
		List list;
		List biObjectList;
		
		biObjectList = new ArrayList();
		
		try {
			Assert.assertNotNull(document, "Parameter [document] cannot be null");
			
			biObjectDAO = DAOFactory.getBIObjectDAO();
			
			subreportDAOHibImpl = new SubreportDAOHibImpl();
			list = subreportDAOHibImpl.loadSubreportsByMasterRptId(document.getId());
			
			for(int i = 0; i < list.size(); i++) {
				Subreport subreport = (Subreport)list.get(i);
				BIObject biobj = biObjectDAO.loadBIObjectForDetail(subreport.getSub_rpt_id());
				biObjectList.add(biobj);
			}
		} catch(Throwable t) {
			logger.error("Impossible to load linked documents", t);
			if(t instanceof EMFUserError) throw (EMFUserError)t;			
		}
		
		return biObjectList;
	}
	
	public String getEngineBaseUrl(BIObject document) {
		String url;
		Engine engine = document.getEngine();
		url = engine.getUrl();
		if(url.trim().endsWith("/") || url.trim().endsWith("\\")) url = url.substring(0, url.length()-1);
		
		if(url.lastIndexOf('\\') >0 )url = url.substring(0, url.lastIndexOf('\\'));
		else if(url.lastIndexOf('/') > 0 ) url = url.substring(0, url.lastIndexOf('/'));
		
		return url;
	}
	
}
