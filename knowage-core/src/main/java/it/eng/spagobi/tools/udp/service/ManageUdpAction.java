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
package it.eng.spagobi.tools.udp.service;


import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.tools.udp.dao.IUdpDAO;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ManageUdpAction extends AbstractSpagoBIAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8394056124945156086L;

	// logger component
	private static Logger logger = Logger.getLogger(ManageUdpAction.class);
	
	private final String MESSAGE_DET = "MESSAGE_DET";
	// type of service
	private final String UDP_DETAIL = "UDP_DETAIL";
	private final String UDP_LIST = "UDP_LIST";
	private final String UDP_DELETE = "UDP_DELETE";
	
	private final String ID = "id";
	private final String LABEL = "label";
	private final String NAME = "name";	
	private final String DESCRIPTION = "description";
	private final String TYPE = "type";
	private final String FAMILY = "family";
	private final String IS_MULTIVALUE = "multivalue";

	private final String UDP_TYPES = "UDP_TYPE";
	private final String UDP_FAMILIES = "UDP_FAMILY";
	
	public static String START = "start";
	public static String LIMIT = "limit";
	public static Integer START_DEFAULT = 0;
	public static Integer LIMIT_DEFAULT = 16;
	
	@Override
	public void doService() {
		logger.debug("IN");
		IUdpDAO udpDao=null;
		IDomainDAO daoDomain=null;
		try {
			udpDao = DAOFactory.getUdpDAO();
			udpDao.setUserProfile(getUserProfile());
			daoDomain=DAOFactory.getDomainDAO();
			udpDao.setUserProfile(getUserProfile());
		} catch (EMFUserError e1) {
			logger.error(e1.getMessage(), e1);
			throw new SpagoBIServiceException(SERVICE_NAME,	"Error occurred");
		}
		HttpServletRequest httpRequest = getHttpRequest();
		MessageBuilder m = new MessageBuilder();
		Locale locale = m.getLocale(httpRequest);
		
		String serviceType = this.getAttributeAsString(MESSAGE_DET);	
		logger.debug("Service type "+serviceType);
		
		if(serviceType != null && serviceType.equals(UDP_LIST)){
			try {
				Integer start = getAttributeAsInteger( START );
				Integer limit = getAttributeAsInteger( LIMIT );
				
				if(start==null){
					start = START_DEFAULT;
				}
				if(limit==null){
					limit = LIMIT_DEFAULT;
				}

				Integer totalNum = udpDao.countUdp();
				
				List<SbiUdp> udpList = udpDao.loadPagedUdpList(start,limit);
				logger.debug("Loaded udp list");
				JSONArray udpJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(udpList,locale);
				JSONObject udpResponseJSON = createJSONResponseUdp(udpJSON, totalNum);

				writeBackToClient(new JSONSuccess(udpResponseJSON));

			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				try {
					writeBackToClient("Exception occurred while retrieving user defined properties");
				} catch (IOException e1) {
					logger.error(e1.getMessage(), e1);
				}
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving user defined properties", e);
			}
			
		}else if(serviceType != null && serviceType.equals(UDP_DETAIL)){
			try {
				String name = getAttributeAsString(NAME);
				String label = getAttributeAsString(LABEL);
				String description = getAttributeAsString(DESCRIPTION);
				String typeStr = getAttributeAsString(TYPE);			
				Domain tmpDomain = daoDomain.loadDomainByCodeAndValue(UDP_TYPES, typeStr);
				Integer type = tmpDomain.getValueId();
				String familyStr = getAttributeAsString(FAMILY);
				tmpDomain = daoDomain.loadDomainByCodeAndValue(UDP_FAMILIES, familyStr);
				Integer family = tmpDomain.getValueId();
				Boolean isMultivalue = Boolean.valueOf(getAttributeAsBoolean(IS_MULTIVALUE));
				
				String id = getAttributeAsString(ID);
				
				SbiUdp udp = new SbiUdp();
				udp.setName(name);
				udp.setLabel(label);
				udp.setDescription(description);
				udp.setTypeId(type);
				udp.setFamilyId(family);
				udp.setIsMultivalue(isMultivalue);
			
				if(id != null && !id.equals("") && !id.equals("0")){							
					udp.setUdpId(Integer.valueOf(id));
					udpDao.update(udp);
					logger.debug("User attribute "+id+" updated");
					JSONObject attributesResponseSuccessJSON = new JSONObject();	
					attributesResponseSuccessJSON.put("success", true);	
					attributesResponseSuccessJSON.put("responseText", "Operation succeded");
					writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );
				}else{
					Integer udpID = udpDao.insert(udp);
					logger.debug("New User Attribute inserted");
					JSONObject attributesResponseSuccessJSON = new JSONObject();
					attributesResponseSuccessJSON.put("success", true);
					attributesResponseSuccessJSON.put("responseText", "Operation succeded");
					attributesResponseSuccessJSON.put("id", udpID);
					writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );
				}
			} catch (JSONException e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while saving new user attribute",
						e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while saving new user attribute",
						e);
			} catch (Exception e){
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while saving new user attribute",
						e);
			}
			
		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(UDP_DELETE)) {
			Integer id = getAttributeAsInteger(ID);
			try {
				udpDao.delete(id);
				logger.debug("User Attribute deleted");
				writeBackToClient( new JSONAcknowledge("Operation succeded") );

			} catch (Throwable e) {
				logger.error("Exception occurred while deleting role", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while deleting user attribute",
						e);
			}
		}else if(serviceType == null){
			try {
				List types = daoDomain.loadListDomainsByType(UDP_TYPES);
				getSessionContainer().setAttribute("TYPE_LIST", types);
				List families = daoDomain.loadListDomainsByType(UDP_FAMILIES);
				getSessionContainer().setAttribute("FAMILY_LIST", families);
				
			} catch (EMFUserError e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception retrieving types and/or families", e);
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
	private JSONObject createJSONResponseUdp(JSONArray rows, Integer totalResNumber)
			throws JSONException {
		JSONObject results;

		results = new JSONObject();
		results.put("total", totalResNumber);
		results.put("title", "Udp");
		results.put("rows", rows);
		return results;
	}
}
