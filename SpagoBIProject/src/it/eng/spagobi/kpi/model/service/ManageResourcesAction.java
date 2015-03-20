/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.model.service;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.model.dao.IResourceDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ManageResourcesAction extends AbstractSpagoBIAction {

	// logger component
	private static Logger logger = Logger.getLogger(ManageResourcesAction.class);
	private final String MESSAGE_DET = "MESSAGE_DET";
	// type of service
	private final String RESOURCES_LIST = "RESOURCES_LIST";
	private final String RESOURCE_INSERT = "RESOURCE_INSERT";
	private final String RESOURCE_DELETE = "RESOURCE_DELETE";
	
	private final String RESOURCE_DOMAIN_TYPE = "RESOURCE";

	// RES detail
	private final String ID = "id";
	private final String NAME = "name";
	private final String CODE = "code";
	private final String DESCRIPTION = "description";
	private final String TABLE_NAME = "tablename";
	private final String COLUMN_NAME = "columnname";
	private final String NODE_TYPE_CODE = "typeCd";
	
	public static String START = "start";
	public static String LIMIT = "limit";
	public static Integer START_DEFAULT = 0;
	public static Integer LIMIT_DEFAULT = 15;

	@Override
	public void doService() {
		logger.debug("IN");
		IResourceDAO resDao;
		try {
			resDao = DAOFactory.getResourceDAO();
			resDao.setUserProfile(getUserProfile());
		} catch (EMFUserError e1) {
			logger.error(e1.getMessage(), e1);
			throw new SpagoBIServiceException(SERVICE_NAME,	"Error occurred");
		}
		Locale locale = getLocale();

		String serviceType = this.getAttributeAsString(MESSAGE_DET);
		logger.debug("Service type "+serviceType);
		if (serviceType != null && serviceType.equalsIgnoreCase(RESOURCES_LIST)) {
			
			try {		
				
				Integer start = getAttributeAsInteger( START );
				Integer limit = getAttributeAsInteger( LIMIT );
				
				if(start==null){
					start = START_DEFAULT;
				}
				if(limit==null){
					limit = LIMIT_DEFAULT;
				}

				Integer totalResNum = resDao.countResources();
				List resources = resDao.loadPagedResourcesList(start,limit);
				logger.debug("Loaded resources list");
				JSONArray resourcesJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(resources, locale);
				JSONObject resourcesResponseJSON = createJSONResponseResources(resourcesJSON, totalResNum);

				writeBackToClient(new JSONSuccess(resourcesResponseJSON));

			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving users", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving users", e);
			}
		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(RESOURCE_INSERT)) {
			String id = getAttributeAsString(ID);
			String code = getAttributeAsString(CODE);
			String name = getAttributeAsString(NAME);
			String description = getAttributeAsString(DESCRIPTION);
			String tablename = getAttributeAsString(TABLE_NAME);
			String columnname = getAttributeAsString(COLUMN_NAME);
			String resourceTypeCD = getAttributeAsString(NODE_TYPE_CODE);		
			
			List<Domain> domains = (List<Domain>)getSessionContainer().getAttribute("nodeTypesList");
			
		    HashMap<String, Integer> domainIds = new HashMap<String, Integer> ();
		    if(domains != null){
			    for(int i=0; i< domains.size(); i++){
			    	domainIds.put(domains.get(i).getValueCd(), domains.get(i).getValueId());
			    }
		    }
		    
		    Integer resourceTypeID = domainIds.get(resourceTypeCD);
		    if(resourceTypeID == null){
		    	logger.error("Resource type CD does not exist");
		    	throw new SpagoBIServiceException(SERVICE_NAME,	"Resource Type ID is undefined");
		    }

			if (name != null && resourceTypeID != null && code != null) {
				Resource res = new Resource();
				res.setName(name);
				res.setType(resourceTypeCD);
				res.setTypeId(resourceTypeID);
				res.setCode(code);
				
				if(description != null){
					res.setDescr(description);
				}	
				if(tablename != null){
					res.setTable_name(tablename);
				}
				if(columnname != null){
					res.setColumn_name(columnname);
				}				
				
				try {
					if(id != null && !id.equals("") && !id.equals("0")){							
						res.setId(Integer.valueOf(id));
						resDao.modifyResource(res);
						logger.debug("Resource "+id+" updated");
						JSONObject attributesResponseSuccessJSON = new JSONObject();
						attributesResponseSuccessJSON.put("success", true);
						attributesResponseSuccessJSON.put("responseText", "Operation succeded");
						attributesResponseSuccessJSON.put("id", id);
						writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );
					}else{
						Integer resourceID = resDao.insertResource(res);
						logger.debug("New Resource inserted");
						JSONObject attributesResponseSuccessJSON = new JSONObject();
						attributesResponseSuccessJSON.put("success", true);
						attributesResponseSuccessJSON.put("responseText", "Operation succeded");
						attributesResponseSuccessJSON.put("id", resourceID);
						writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );
					}

				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
					throw new SpagoBIServiceException(SERVICE_NAME,
							"Exception occurred while saving new resource", e);
				}
								
			}else{
				logger.error("Resource name, code or type are missing");
				throw new SpagoBIServiceException(SERVICE_NAME,	"Please fill resource name, code and type");
			}
		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(RESOURCE_DELETE)) {
			Integer id = getAttributeAsInteger(ID);
			try {
				resDao.deleteResource(id);
				logger.debug("Resource deleted");
				writeBackToClient( new JSONAcknowledge("Operation succeded") );
			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving resource to delete", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving resource to delete", e);
			}
		}else if(serviceType == null){
			try {
				List nodeTypes = DAOFactory.getDomainDAO().loadListDomainsByType(RESOURCE_DOMAIN_TYPE);
				getSessionContainer().setAttribute("nodeTypesList", nodeTypes);
				
			} catch (EMFUserError e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception retrieving resources types", e);
			}
		}
		logger.debug("OUT");
	}

	/**
	 * Creates a json array with children users informations
	 * 
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	private JSONObject createJSONResponseResources(JSONArray rows, Integer totalResNumber)
			throws JSONException {
		JSONObject results;

		results = new JSONObject();
		results.put("total", totalResNumber);
		results.put("title", "Resources");
		results.put("rows", rows);
		return results;
	}

}
