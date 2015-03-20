/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.alarm.service;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.kpi.alarm.dao.ISbiAlarmContactDAO;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmContact;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.model.dao.IResourceDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ManageContactsAction extends AbstractSpagoBIAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8394056124945156086L;

	// logger component
	private static Logger logger = Logger.getLogger(ManageContactsAction.class);
	
	private final String MESSAGE_DET = "MESSAGE_DET";
	// type of service
	private final String CONTACT_INSERT = "CONTACT_INSERT";
	private final String CONTACTS_LIST = "CONTACTS_LIST";
	private final String CONTACT_DELETE = "CONTACT_DELETE";
	
	private final String RESOURCES_LIST = "RESOURCES_LIST";
	
	private final String ID = "id";
	private final String NAME = "name";
	private final String MOBILE = "mobile";
	private final String EMAIL = "email";
	private final String RESOURCES = "resources";
	private final String NO_RESOURCES_STR = "-";
	
	public static String START = "start";
	public static String LIMIT = "limit";
	public static Integer START_DEFAULT = 0;
	public static Integer LIMIT_DEFAULT = 16;
	
	@Override
	public void doService() {
		logger.debug("IN");
		ISbiAlarmContactDAO contactDao;
		try {
			contactDao = DAOFactory.getAlarmContactDAO();
			contactDao.setUserProfile(getUserProfile());
		} catch (EMFUserError e1) {
			logger.error(e1.getMessage(), e1);
			throw new SpagoBIServiceException(SERVICE_NAME,	"Error occurred");
		}
		HttpServletRequest httpRequest = getHttpRequest();
		Locale locale = getLocale();
	
		String serviceType = this.getAttributeAsString(MESSAGE_DET);
		logger.debug("Service type "+serviceType);
		if(serviceType != null && serviceType.equals(CONTACTS_LIST)){
			try {
				Integer start = getAttributeAsInteger( START );
				Integer limit = getAttributeAsInteger( LIMIT );
				
				if(start==null){
					start = START_DEFAULT;
				}
				if(limit==null){
					limit = LIMIT_DEFAULT;
				}

				Integer totalResNum = contactDao.countContacts();
				List<SbiAlarmContact> contacts = contactDao.loadPagedContactsList(start, limit);
				
				logger.debug("Loaded contacts list");
				JSONArray contactsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(contacts,locale);
				JSONObject contactsResponseJSON = createJSONResponseContacts(contactsJSON, totalResNum);

				writeBackToClient(new JSONSuccess(contactsResponseJSON));

			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				try {
					writeBackToClient("Exception occurred while retrieving contacts");
				} catch (IOException e1) {
					logger.error(e1.getMessage(), e1);
				}
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving contacts", e);
			}
			
		}else if(serviceType != null && serviceType.equals(CONTACT_INSERT)){
			String name = getAttributeAsString(NAME);
			String email = getAttributeAsString(EMAIL);
			String mobile = getAttributeAsString(MOBILE);
			String resources = getAttributeAsString(RESOURCES);
			
			String id = getAttributeAsString(ID);
			
			SbiAlarmContact contact = new SbiAlarmContact();
			contact.setEmail(email);
			contact.setMobile(mobile);
			contact.setName(name);
			if(resources != null && !resources.equals(NO_RESOURCES_STR)){
				contact.setResources(resources);
			}else{
				contact.setResources(null);
			}
			try {
				if(id != null && !id.equals("") && !id.equals("0")){							
					contact.setId(Integer.valueOf(id));
					contactDao.update(contact);
					logger.debug("Contact "+id+" updated");
					JSONObject attributesResponseSuccessJSON = new JSONObject();	
					attributesResponseSuccessJSON.put("success", true);	
					attributesResponseSuccessJSON.put("responseText", "Operation succeded");
					writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );
				}else{
					Integer contactID = contactDao.insert(contact);
					logger.debug("New Contact inserted");
					JSONObject attributesResponseSuccessJSON = new JSONObject();
					attributesResponseSuccessJSON.put("success", true);
					attributesResponseSuccessJSON.put("responseText", "Operation succeded");
					attributesResponseSuccessJSON.put("id", contactID);
					writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );
				}
			} catch (JSONException e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while saving new contact",
						e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while saving new contact",
						e);
			}
		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(CONTACT_DELETE)) {
			Integer id = getAttributeAsInteger(ID);
			try {
				contactDao.delete(id);
				logger.debug("Contact deleted");
				writeBackToClient( new JSONAcknowledge("Operation succeded") );

			} catch (Throwable e) {
				logger.error("Exception occurred while deleting role", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while deleting contact",
						e);
			}
		}else if(serviceType == null){
			try {
				IResourceDAO resourceDao = DAOFactory.getResourceDAO();
				List<String> resources = (List<String>)getSessionContainer().getAttribute(RESOURCES_LIST);
				if(resources == null){
					List<Resource> resourcesOBJ = resourceDao.loadResourcesList(null, null);
					resources = new ArrayList<String>();
					for(int i =0; i< resourcesOBJ.size(); i++){
						Resource res = resourcesOBJ.get(i);
						resources.add(res.getName());
					}
					resources.add(NO_RESOURCES_STR);
					getSessionContainer().setAttribute(RESOURCES_LIST, resources);
				}
				
			} catch (EMFUserError e) {
				logger.error(e.getMessage(), e);
				try {
					writeBackToClient("Exception occurred while retrieving resources");
				} catch (IOException e1) {
					logger.error(e1.getMessage(), e1);
				}
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving resources", e);
			}
		}

		logger.debug("OUT");
		
	}
	/**
	 * Creates a json array with children roles informations
	 * 
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	private JSONObject createJSONResponseContacts(JSONArray rows, Integer totalResNumber)
			throws JSONException {
		JSONObject results;

		results = new JSONObject();
		results.put("total", totalResNumber);
		results.put("title", "Contacts");
		results.put("rows", rows);
		return results;
	}
}
