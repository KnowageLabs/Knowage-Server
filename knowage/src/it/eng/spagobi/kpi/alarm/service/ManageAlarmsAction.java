/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.alarm.service;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.kpi.alarm.dao.ISbiAlarmDAO;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarm;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmContact;
import it.eng.spagobi.kpi.config.bo.KpiAlarmInstance;
import it.eng.spagobi.kpi.config.bo.KpiInstance;
import it.eng.spagobi.kpi.config.dao.IKpiInstanceDAO;
import it.eng.spagobi.kpi.config.metadata.SbiKpiInstance;
import it.eng.spagobi.kpi.threshold.bo.ThresholdValue;
import it.eng.spagobi.kpi.threshold.dao.IThresholdValueDAO;
import it.eng.spagobi.kpi.threshold.metadata.SbiThresholdValue;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ManageAlarmsAction extends AbstractSpagoBIAction{

	private static final long serialVersionUID = -755516381785184797L;
	// logger component
	private static Logger logger = Logger.getLogger(ManageAlarmsAction.class);
	
	private final String MESSAGE_DET = "MESSAGE_DET";
	// type of service
	private final String ALARMS_LIST = "ALARMS_LIST";
	private final String ALARM_INSERT = "ALARM_INSERT";
	private final String ALARM_DELETE = "ALARM_DELETE";
	private final String TRESHOLDS_LIST = "TRESHOLDS_LIST";
	
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String LABEL = "label";
	public static final String MODALITY = "modality";
	public static final String SINGLE_EVENT = "singleEvent";
	public static final String AUTO_DISABLED = "autoDisabled";
	public static final String TEXT = "text";
	public static final String URL = "url";
	public static final String CONTACTS = "contacts";
	public static final String DOMAIN_CD = "ALARM_MODALITY";
	public static final String KPI = "kpi";
	public static final String THRESHOLD = "threshold";
	
	public static final String KPI_LIST = "KPI_LIST";
	public static final String TRESHOLD_LIST = "TRESHOLD_LIST";
	
	public static String START = "start";
	public static String LIMIT = "limit";
	public static Integer START_DEFAULT = 0;
	public static Integer LIMIT_DEFAULT = 15;
	
	@Override
	public void doService() {
		logger.debug("IN");
		
		ISbiAlarmDAO alarmDao;
		try {
			alarmDao = DAOFactory.getAlarmDAO();
			alarmDao.setUserProfile(getUserProfile());
		} catch (EMFUserError e1) {
			logger.error(e1.getMessage(), e1);
			throw new SpagoBIServiceException(SERVICE_NAME,	"Error occurred");
		}
		HttpServletRequest httpRequest = getHttpRequest();
		Locale locale = getLocale();

		String serviceType = this.getAttributeAsString(MESSAGE_DET);
		logger.debug("Service type "+serviceType);

		if (serviceType != null && serviceType.equalsIgnoreCase(ALARMS_LIST)) {
			//loads kpi 
			try {			
				Integer start = getAttributeAsInteger( START );
				Integer limit = getAttributeAsInteger( LIMIT );
				
				if(start==null){
					start = START_DEFAULT;
				}
				if(limit==null){
					limit = LIMIT_DEFAULT;
				}

				Integer totalResNum = alarmDao.countAlarms();
				List<SbiAlarm> alarms = alarmDao.loadPagedAlarmsList(start, limit);
				
				logger.debug("Loaded users list");
				JSONArray alarmsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(alarms,locale);
				JSONObject usersResponseJSON = createJSONResponseAlarms(alarmsJSON, totalResNum);

				writeBackToClient(new JSONSuccess(usersResponseJSON));
			
			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving alarms", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving alarms", e);
			}
			
		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(ALARM_INSERT)) {
			
			String id = getAttributeAsString(ID);
			String name = getAttributeAsString(NAME);
			String descr = getAttributeAsString(DESCRIPTION);
			String label = getAttributeAsString(LABEL);
			String modality = getAttributeAsString(MODALITY);
			Boolean singleEvent = getAttributeAsBoolean(SINGLE_EVENT);
			Boolean autoDisabled = getAttributeAsBoolean(AUTO_DISABLED);
			String text = getAttributeAsString(TEXT);
			String url = getAttributeAsString(URL);
			Integer kpiInstId = getAttributeAsInteger(KPI);
			Integer thresholdId = getAttributeAsInteger(THRESHOLD);
			JSONArray contactsJSON = getAttributeAsJSONArray(CONTACTS);

			SbiAlarm alarm = new SbiAlarm();
			alarm.setAutoDisabled(autoDisabled);
			alarm.setDescr(descr);			
			alarm.setLabel(label);
			alarm.setName(name);
			alarm.setSingleEvent(singleEvent);
			alarm.setText(text);
			alarm.setUrl(url);	

			try {
			if(modality!=null){
				SbiDomains dModality = DAOFactory.getDomainDAO().loadSbiDomainByCodeAndValue(DOMAIN_CD, modality);
				
				alarm.setModality(dModality);
			}
			if(kpiInstId != null){
				SbiKpiInstance sbiKpiInstance = DAOFactory.getKpiInstanceDAO().loadSbiKpiInstanceById(kpiInstId);
				alarm.setSbiKpiInstance(sbiKpiInstance);
			}
			if(thresholdId != null){
				SbiThresholdValue sbiThresholdValue = DAOFactory.getThresholdValueDAO().loadSbiThresholdValueById(thresholdId);
				alarm.setSbiThresholdValue(sbiThresholdValue);
			}
			
			if(id != null && !id.equals("") && !id.equals("0")){	
				alarm.setId(Integer.valueOf(id));
			}
			
				Set<SbiAlarmContact> contactsList = null;
				if(contactsJSON != null){
					contactsList = deserializeContactsJSONArray(contactsJSON);
					alarm.setSbiAlarmContacts(contactsList);
				}
				Integer idToReturn = alarmDao.update(alarm);
				logger.debug("Alarm updated or Inserted");
				
				JSONObject attributesResponseSuccessJSON = new JSONObject();
				attributesResponseSuccessJSON.put("success", true);
				attributesResponseSuccessJSON.put("responseText", "Operation succeded");
				attributesResponseSuccessJSON.put("id", idToReturn);
				writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );

			} catch (EMFUserError e) {
				logger.error("Exception occurred while saving alarm", e);
				writeErrorsBackToClient();
				throw new SpagoBIServiceException(SERVICE_NAME,	"Exception occurred while saving alarm",	e);
			} catch (IOException e) {
				logger.error("Exception occurred while writing response to client", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while writing response to client", e);
			} catch (JSONException e) {
				logger.error("JSON Exception", e);
				e.printStackTrace();
			}
			
		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(ALARM_DELETE)) {
			Integer id = getAttributeAsInteger(ID);
			try {
				alarmDao.delete(id);
				logger.debug("Alarm deleted");
				writeBackToClient( new JSONAcknowledge("Operation succeded") );

			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving user to delete", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving user to delete",e);
			}
		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(TRESHOLDS_LIST)) {
			Integer id =  getAttributeAsInteger(ID);
			try {
				if(id != null){
					IThresholdValueDAO tresholdDao = DAOFactory.getThresholdValueDAO();
					IKpiInstanceDAO kpiDao = DAOFactory.getKpiInstanceDAO();
					KpiInstance k = kpiDao.loadKpiInstanceById(id);
					if(k!=null){
						List<ThresholdValue> tresholds = tresholdDao.loadThresholdValuesByThresholdId(k.getThresholdId());
						logger.debug("Threshold values loaded");
						JSONArray trshJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(tresholds,locale);
						JSONObject trashResponseJSON = createJSONResponseThresholds(trshJSON);
		
						writeBackToClient(new JSONSuccess(trashResponseJSON));
					}else{
						writeBackToClient("Error");
					}
				}

			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving tresholds", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving tresholds",e);
			}
		}else if(serviceType == null){
			try {
				IKpiInstanceDAO kpiDao = DAOFactory.getKpiInstanceDAO();
				List<String> kpis = (List<String>)getSessionContainer().getAttribute(KPI_LIST);
				if(kpis != null){
					getSessionContainer().delAttribute(KPI_LIST);				
				}
				List<KpiAlarmInstance> kpisAlarm = kpiDao.loadKpiAlarmInstances();
				if(kpisAlarm != null){
					getSessionContainer().setAttribute(KPI_LIST, kpisAlarm);
				}
				
				List<SbiAlarmContact> contactsList = DAOFactory.getAlarmContactDAO().findAll();
				getSessionContainer().setAttribute("contactsList", contactsList);
				
			} catch (EMFUserError e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception retrieving role types",
						e);
			}
		}
		logger.debug("OUT");	
	}
	
	private JSONObject createJSONResponseAlarms(JSONArray rows, Integer totalResNumber) throws JSONException {
		JSONObject results;		
		results = new JSONObject();
		results.put("total", totalResNumber);
		results.put("title", "Alarms");
		results.put("rows", rows);
		return results;
	}
	
	private JSONObject createJSONResponseThresholds(JSONArray rows)throws JSONException {
		JSONObject results;	
		results = new JSONObject();
		results.put("title", "Alarms");
		results.put("samples", rows);
		return results;
	}
	
	private Set<SbiAlarmContact> deserializeContactsJSONArray(JSONArray rows) throws JSONException{
		Set<SbiAlarmContact> toReturn = new HashSet<SbiAlarmContact>();
		for(int i=0; i< rows.length(); i++){
			JSONObject obj = (JSONObject)rows.get(i);
			SbiAlarmContact c = new SbiAlarmContact();
			Integer id = obj.getInt("id");	
			String email = obj.getString("email");	
			String mobile = obj.getString("mobile");	
			String resources = obj.getString("resources");	
			String name = obj.getString("name");	
			c.setEmail(email);
			c.setId(id);
			c.setMobile(mobile);
			c.setName(name);
			c.setResources(resources);
			toReturn.add(c);
		}	
		return toReturn;
	}

}
