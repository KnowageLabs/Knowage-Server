/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.commonj.services;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.engines.commonj.CommonjEngine;
import it.eng.spagobi.engines.commonj.runtime.CommonjWork;
import it.eng.spagobi.engines.commonj.runtime.CommonjWorkContainer;
import it.eng.spagobi.engines.commonj.runtime.CommonjWorkListener;
import it.eng.spagobi.engines.commonj.runtime.WorkConfiguration;
import it.eng.spagobi.engines.commonj.runtime.WorksRepository;
import it.eng.spagobi.engines.commonj.utils.GeneralUtils;
import it.eng.spagobi.engines.commonj.utils.ProcessesStatusContainer;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.proxy.ContentServiceProxy;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.engines.AbstractEngineAction;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;
import it.eng.spagobi.utilities.service.JSONFailure;
import it.eng.spagobi.utilities.service.JSONSuccess;
import it.eng.spagobi.utilities.threadmanager.WorkManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import sun.misc.BASE64Decoder;

import commonj.work.Work;
import commonj.work.WorkEvent;
import commonj.work.WorkItem;

import de.myfoo.commonj.work.FooRemoteWorkItem;


public class StartWorkAction extends AbstractEngineAction {

	private static transient Logger logger = Logger.getLogger(StartWorkAction.class);

	private Content template;
	private ContentServiceProxy contentProxy;
	String documentId;
	String documentLabel;
	private static final BASE64Decoder DECODER = new BASE64Decoder();
	String userId=null;
	HttpSession session = null;
	HttpServletRequest httpRequest = null;
	/** Reads document Id and user Id, get the template, configure the work, create process Id, start work
	 */




	public void service(SourceBean request, SourceBean response) {
		logger.debug("IN");
		super.service(request, response);
		HttpSession session=getHttpSession();
		HttpServletRequest httpRequest = getHttpRequest();


		//		USER_ID
		Object userIdO=request.getAttribute("USER_ID");
		if(userIdO!=null)userId=userIdO.toString();
		else{
			// userId
			userIdO=request.getAttribute("userId");
			if(userIdO!=null){
				userId=userIdO.toString();
			}
			else{
				// userId from session
				userIdO=session.getAttribute("userId");

				if(userIdO!=null){
					userId=userIdO.toString();
				}
				else{


					logger.error("could not retrieve user id");
					return;
				}
			}
		}

		// get DOcument ID
		Object document_idO=null;
		document_idO=request.getAttribute("DOCUMENT_ID");
		documentId = null;
		documentLabel = null;
		if(document_idO!=null){
			documentId=document_idO.toString();
		}
		else{
			logger.warn("could not retrieve document id, check for label");

			Object document_labelO=request.getAttribute("DOCUMENT_LABEL");
			documentLabel=null;
			if(document_labelO!=null){
				documentLabel=document_labelO.toString();
			}
			else{
				logger.error("could not retrieve neither document id nor document label, exception!");
				return;
			}

		}			


		// get Parameters
		Map parameters=new HashMap();

		List attributes=request.getContainedAttributes();
		for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
			SourceBeanAttribute object = (SourceBeanAttribute) iterator.next();
			String key=object.getKey();
			Object value=object.getValue();
			parameters.put(key, value);
		}


		serviceStart(userId, documentId, parameters, session, httpRequest, true);

		logger.debug("OUT");

	}	




	public void serviceStart(String userId, String documentId, Map parameters, HttpSession _session, HttpServletRequest _httpRequest, boolean actionMode) {
		logger.debug("IN");

		this.session = _session;
		this.httpRequest = _httpRequest;
		this.documentId = documentId;
		this.userId = userId;


		try{
			JSONObject info=null;
			// work to build
			WorksRepository worksRepository=null;
			CommonjWork work=null;

			// can take the document id or the document label


			boolean isLabel = false;
			String documentUnique = null;
			if(documentLabel != null){
				isLabel = true;
				documentUnique = documentLabel;
			}
			else if(documentId != null){
				isLabel = false;
				documentUnique = documentId;
			}


			// Build work from template
			try {
				work = new CommonjWork( getTemplateAsSourceBean());
			} catch (SpagoBIEngineException e) {
				logger.error("Error in reading work template",e);
				return;				
			} 

			// calculate process Id
			String pId =  work.calculatePId();					
			logger.debug("process Id is "+pId);
			work.setSbiParametersMap(parameters);

			CommonjEngine cm=new CommonjEngine();
			try {
				worksRepository = CommonjEngine.getWorksRepository();
			} catch (SpagoBIEngineException e) {
				logger.error("Error in reatriving works repository",e);
				return;				

			}

			// call Work configurqations's configure method
			try{
				WorkConfiguration workConfiguration=new WorkConfiguration(worksRepository);
				if(workConfiguration != null) {

					workConfiguration.configure(session,work,parameters,documentUnique, isLabel);

				}
			}
			catch (Exception e) {
				logger.error("Error in configuring work",e);
				return;				
			}



			// Get the container object from session: it MUST be present if start button is enabled
			//Object o=session.getAttribute("SBI_PROCESS_"+document_id);
			ProcessesStatusContainer processesStatusContainer = ProcessesStatusContainer.getInstance();
			Object o=processesStatusContainer.getPidContainerMap().get(pId);
			CommonjWorkContainer container=(CommonjWorkContainer)o;

			WorkManager wm=container.getWm();
			Work workToDo=container.getWork();
			CommonjWorkListener listener=container.getListener();
			FooRemoteWorkItem fooRemoteWorkItem=wm.buildFooRemoteWorkItem(workToDo, listener);

			int statusWI;

			// Check if work was accepted
			if(fooRemoteWorkItem.getStatus()==WorkEvent.WORK_ACCEPTED){
				container.setFooRemoteWorkItem(fooRemoteWorkItem);
				// run work!
				WorkItem workItem=(WorkItem)wm.runWithReturnWI(workToDo, listener);
				container.setWorkItem(workItem);
				statusWI=workItem.getStatus();
				// put new Object in singleton!!!

				processesStatusContainer.getPidContainerMap().put(pId, container);
				//session.setAttribute("SBI_PROCESS_"+document_id, container);

				// if not in action mode don't send the response
				if(actionMode){
					try {
						info=GeneralUtils.buildJSONObject(pId,statusWI);
						writeBackToClient( new JSONSuccess(info));

					} catch (IOException e) {
						String message = "Impossible to write back the responce to the client";
						throw new SpagoBIEngineServiceException(getActionName(), message, e);
					}
				}
			}
			else{ // WORK is rejected
				if(actionMode){
					try {
						statusWI=fooRemoteWorkItem.getStatus();
						info=GeneralUtils.buildJSONObject(pId,statusWI);
						writeBackToClient( new JSONSuccess(info));
					} catch (IOException e) {
						String message = "Impossible to write back the responce to the client";
						throw new SpagoBIEngineServiceException(getActionName(), message, e);
					}
				}
			}
		}
		catch (Exception e) {

			logger.error("Error in starting the work");
			if(actionMode){
				try {
					writeBackToClient( new JSONFailure( e) );
				} catch (IOException e1) {
					logger.error("Error in starting the work and in writing back to client",e);
					throw new SpagoBIEngineServiceException(getActionName(), "Error in starting the work and in writing back to client", e1);
				} catch (JSONException e1) {
					logger.error("Error in starting the work and in writing back to client",e);
					throw new SpagoBIEngineServiceException(getActionName(), "Error in starting the work and in writing back to client", e1);
				}
			}
		}

		logger.debug("OUT");
	}










	/*
FUNCTIONS FROM ACTION ENGINE

	 */

	public SourceBean getTemplateAsSourceBean() {
		SourceBean templateSB = null;
		try {
			templateSB = SourceBean.fromXMLString(getTemplateAsString());
		} catch (SourceBeanException e) {
			SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException("CommonJ", "Impossible to parse template's content", e);
			engineException.setDescription("Impossible to parse template's content:  " + e.getMessage());
			engineException.addHint("Check if the document's template is a well formed xml file");
			throw engineException;
		}		

		return templateSB;
	}

	public String getTemplateAsString() {
		return new String(getTemplate());
	}

	private byte[] getTemplate() {
		byte[] templateContent = null;
		HashMap requestParameters;

		if(template == null) {
			contentProxy = getContentServiceProxy();
			if(contentProxy == null) {
				throw new SpagoBIEngineStartupException("SpagoBIQbeEngine", 
						"Impossible to instatiate proxy class [" + ContentServiceProxy.class.getName() + "] " +
						"in order to retrive the template of document [" + documentId + "]");
			}

			requestParameters = ParametersDecoder.getDecodedRequestParameters(httpRequest);
			if(documentId != null){
				template = contentProxy.readTemplate(documentId, requestParameters);
			}
			else if(documentLabel != null){
				template = contentProxy.readTemplateByLabel(documentLabel, requestParameters);
			}	
			try {
				if(template == null)throw new SpagoBIEngineRuntimeException("There are no template associated to document [" + documentId + "]");
				templateContent = DECODER.decodeBuffer(template.getContent());
			} catch (Throwable e) {
				SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException("COmmonj", "Impossible to get template's content", e);
				engineException.setDescription("Impossible to get template's content:  " + e.getMessage());
				engineException.addHint("Check the document's template");
				throw engineException;
			}
		}
		return templateContent;
	}


	private ContentServiceProxy getContentServiceProxy() {
		if(contentProxy == null) {
			contentProxy = new ContentServiceProxy(userId, session);
		}	   

		return contentProxy;
	}

	public ContentServiceProxy getContentProxy() {
		return contentProxy;
	}

	public void setContentProxy(ContentServiceProxy contentProxy) {
		this.contentProxy = contentProxy;
	}



	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}



}
