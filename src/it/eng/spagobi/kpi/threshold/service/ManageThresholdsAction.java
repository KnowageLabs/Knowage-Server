/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.threshold.service;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.kpi.threshold.bo.Threshold;
import it.eng.spagobi.kpi.threshold.bo.ThresholdValue;
import it.eng.spagobi.kpi.threshold.dao.IThresholdDAO;
import it.eng.spagobi.kpi.threshold.dao.IThresholdValueDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

	public class ManageThresholdsAction extends AbstractSpagoBIAction{
		// logger component
	private static Logger logger = Logger.getLogger(ManageThresholdsAction.class);
	private final String MESSAGE_DET = "MESSAGE_DET";
	// type of service
	private final String THRESHOLDS_LIST = "THRESHOLDS_LIST";
	private final String THRESHOLD_INSERT = "THRESHOLD_INSERT";
	private final String THRESHOLD_DELETE = "THRESHOLD_DELETE";
	private final String THR_VAL_DELETE = "THR_VAL_DELETE";
	
	private final String THRESHOLD_DOMAIN_TYPE = "THRESHOLD_TYPE";
	private final String THRESHOLD_SEVERITY_TYPE = "SEVERITY";
	
	// RES detail
	private final String ID = "id";
	private final String NAME = "name";
	private final String CODE = "code";
	private final String DESCRIPTION = "description";
	private final String NODE_TYPE_CODE = "typeCd";	
	private final String THRESHOLD_VALUES = "thrValues";
	private final String THR_VAL_ID_TO_DELETE = "thrValIid";
	
	private static final String THR_VAL_ID = "idThrVal";
	private static final String THR_VAL_LABEL = "label";
	private static final String THR_VAL_POSITION = "position";
	private static final String THR_VAL_MIN = "min";
	private static final String THR_VAL_MIN_INCLUDED = "minIncluded";
	private static final String THR_VAL_MAX = "max";
	private static final String THR_VAL_MAX_INCLUDED = "maxIncluded";
	private static final String THR_VAL_VALUE = "val";
	private static final String THR_VAL_COLOR = "color";
	private static final String THR_VAL_SEVERITY_CD = "severityCd";
	
	public static String START = "start";
	public static String LIMIT = "limit";
	public static Integer START_DEFAULT = 0;
	public static Integer LIMIT_DEFAULT = 14;
	//filters parameters
	public static String FILTERS = "FILTERS";
	@Override
	public void doService() {
		logger.debug("IN");
		IThresholdDAO thrDao;
		IThresholdValueDAO tDao;
		try {
			thrDao = DAOFactory.getThresholdDAO();
			thrDao.setUserProfile(getUserProfile());
			tDao = DAOFactory.getThresholdValueDAO();
			tDao.setUserProfile(getUserProfile());
		} catch (EMFUserError e1) {
			logger.error(e1.getMessage(), e1);
			throw new SpagoBIServiceException(SERVICE_NAME,	"Error occurred");
		}
		Locale locale = getLocale();
	
		String serviceType = this.getAttributeAsString(MESSAGE_DET);
		logger.debug("Service type "+serviceType);
		if (serviceType != null && serviceType.equalsIgnoreCase(THRESHOLDS_LIST)) {
			
			try {		
				JSONObject filtersJSON = null;
				
				Integer start = getAttributeAsInteger( START );
				Integer limit = getAttributeAsInteger( LIMIT );
				
				if(start==null){
					start = START_DEFAULT;
				}
				if(limit==null){
					limit = LIMIT_DEFAULT;
				}
	
				Integer totalItemsNum = thrDao.countThresholds();
				
				
				List thresholds = null;
				if(this.requestContainsAttribute( FILTERS ) ) {
					filtersJSON = getAttributeAsJSONObject( FILTERS );
					String hsql = filterList(filtersJSON);
					thresholds = thrDao.loadThresholdListFiltered(hsql, start, limit);
				}else{//not filtered
					thresholds = thrDao.loadPagedThresholdList(start,limit);
				}
				

				logger.debug("Loaded thresholds list");
				JSONArray resourcesJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(thresholds, locale);
				JSONObject resourcesResponseJSON = createJSONResponseResources(resourcesJSON, totalItemsNum);
	
				writeBackToClient(new JSONSuccess(resourcesResponseJSON));
	
			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving thresholds", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving thresholds", e);
			}
		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(THRESHOLD_INSERT)) {
			String id = getAttributeAsString(ID);
			String code = getAttributeAsString(CODE);
			String name = getAttributeAsString(NAME);
			String description = getAttributeAsString(DESCRIPTION);
			String typeCD = getAttributeAsString(NODE_TYPE_CODE);	
			
			JSONArray thrValuesJSON = null;
			Integer position = null;
			String thrValId = null;
			String label = null;			
			String colourString = null;
			Double value = null;
			String severityCd = null;
			Boolean minClosed = null;
			Double minValue = null;
			Boolean maxClosed = null;
			Double maxValue = null;
			
			if(typeCD!=null && typeCD.equals("RANGE")){
				
				thrValuesJSON = getAttributeAsJSONArray(THRESHOLD_VALUES);
				
			}else if(typeCD!=null && (typeCD.equals("MINIMUM") || typeCD.equals("MAXIMUM")) ){
				
				String pos = getAttributeAsString(THR_VAL_POSITION);	
				if(pos!=null && !pos.equals("")){
					position = new Integer(pos);
				}				
				thrValId = getAttributeAsString(THR_VAL_ID);
				label = getAttributeAsString(THR_VAL_LABEL);
				colourString = getAttributeAsString(THR_VAL_COLOR);
				String valueS = getAttributeAsString(THR_VAL_VALUE);				
				if(valueS!=null && !valueS.equals("")){
					value = new Double(valueS);
				}
				severityCd = getAttributeAsString(THR_VAL_SEVERITY_CD);
				String minC = getAttributeAsString(THR_VAL_MIN_INCLUDED);	
				if(minC!=null && !minC.equals("")){
					minClosed = new Boolean(minC);
				}else{
					minClosed = new Boolean("false");
				}
				String minValueS = getAttributeAsString(THR_VAL_MIN);
				if(minValueS!=null && !minValueS.equals("")){
					minValue = new Double(minValueS);
				}
				
				String maxValueS = getAttributeAsString(THR_VAL_MAX);				
				if(maxValueS!=null && !maxValueS.equals("")){
					maxValue = new Double(maxValueS);
				}
				String maxC = getAttributeAsString(THR_VAL_MAX_INCLUDED);	
				if(maxC!=null && !maxC.equals("")){
					maxClosed = new Boolean(maxC);
				}else{
					maxClosed = new Boolean("false");
				}
			}

			List<Domain> domains = (List<Domain>)getSessionContainer().getAttribute("thrTypesList");
			List<Domain> domainsthrValues = (List<Domain>)getSessionContainer().getAttribute("thrSeverityTypes");
			domains.addAll(domainsthrValues);
			
		    HashMap<String, Integer> domainIds = new HashMap<String, Integer> ();
		    if(domains != null){
			    for(int i=0; i< domains.size(); i++){
			    	domainIds.put(domains.get(i).getValueCd(), domains.get(i).getValueId());
			    }
		    }
		    
		    Integer typeID = domainIds.get(typeCD);
		    if(typeID == null){
		    	logger.error("Threshold type CD does not exist");
		    	throw new SpagoBIServiceException(SERVICE_NAME,	"Threshold Type ID is undefined");
		    }
	
			if (name != null && typeID != null && code != null) {
				Threshold thr = new Threshold();
				thr.setName(name);
				thr.setThresholdTypeCode(typeCD);
				thr.setThresholdTypeId(typeID);
				thr.setCode(code);
	
				if(description != null){
					thr.setDescription(description);
				}	
				
				List thrValuesList = new ArrayList();
				if(typeCD != null){
					if(typeCD.equals("MINIMUM") || typeCD.equals("MAXIMUM")){
						ThresholdValue tVal = new ThresholdValue();
						if(thrValId!= null && !thrValId.equals("") && !thrValId.equals("0")){
							tVal.setId(Integer.valueOf(thrValId));
						}
						tVal.setLabel(label);						
						tVal.setPosition(position);
						tVal.setColourString(colourString);
						tVal.setValue(value);
						tVal.setSeverityCd(severityCd);
						if(severityCd!=null && !severityCd.equals("")){
							Integer severityId = domainIds.get(severityCd);		
							tVal.setSeverityId(severityId);		
						}

						if(typeCD.equals("MINIMUM")){
							tVal.setMinClosed(minClosed);
							tVal.setMinValue(minValue);
						}else if(typeCD.equals("MAXIMUM")){
							tVal.setMaxClosed(maxClosed);
							tVal.setMaxValue(maxValue);
						}	
						thrValuesList.add(tVal);
												
					}else if(typeCD.equals("RANGE")){
						if(thrValuesJSON!=null){
							try {
								thrValuesList = deserializeThresholdValuesJSONArray(thrValuesJSON, domainIds);
							} catch (JSONException e) {
								logger.error("JSON Exception");
								e.printStackTrace();
							}
						}
					}
					thr.setThresholdValues(thrValuesList);
				}
			
				try {

					Integer idToReturnToClient = null;
					
					if(id != null && !id.equals("") && !id.equals("0")){	
						//modify
						thr.setId(Integer.valueOf(id));
						try{
							thrDao.modifyThreshold(thr);
						}catch(EMFUserError e){
							logger.error(e.getMessage(), e);
							throw e;
							
						}
						idToReturnToClient = Integer.valueOf(id);						
					}else{
						//insert new
						idToReturnToClient = thrDao.insertThreshold(thr);		
					}
					
					List thrValueIds = new ArrayList();
					if(thrValuesList!=null && !thrValuesList.isEmpty()){							
						Iterator it = thrValuesList.iterator();
						while(it.hasNext()){
							ThresholdValue tVal = (ThresholdValue)it.next();
							tVal.setThresholdId(Integer.valueOf(idToReturnToClient));							
							//insert or update all threshold values
							Integer thrValueId = null;
							thrValueId = tDao.saveOrUpdateThresholdValue(tVal);								
							tVal.setId(thrValueId);
							thrValueIds.add(thrValueId);
						}				
					}
					
					logger.debug("Threshold inserted or updated");
					JSONObject attributesResponseSuccessJSON = new JSONObject();
					attributesResponseSuccessJSON.put("success", true);
					attributesResponseSuccessJSON.put("responseText", "Operation succeded");
					attributesResponseSuccessJSON.put("id", idToReturnToClient);
					if(thrValueIds!=null && !thrValueIds.isEmpty()){
						if(thrValueIds.size()==1){
							attributesResponseSuccessJSON.put("idThrVal", thrValueIds.get(0));
						}else{
							JSONArray thrValsJSONArray = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(thrValuesList, locale);			
							attributesResponseSuccessJSON.put("thrValues", thrValsJSONArray);
						}
					}
					writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );
	
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
					if(e instanceof EMFUserError && ((EMFUserError)e).getCode() == 10119){
						String descr = e.getLocalizedMessage();
						String[] descrTokens = descr.split("[\\]\\[]");

						throw new SpagoBIServiceException(SERVICE_NAME,
								descrTokens[descrTokens.length-1], e);
					}
					throw new SpagoBIServiceException(SERVICE_NAME,
							"Exception occurred while saving new threshold", e);
				}
								
			}else{
				logger.error("Resource name, code or type are missing");
				throw new SpagoBIServiceException(SERVICE_NAME,	"Please fill threshold name, code and type");
			}
		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(THRESHOLD_DELETE)) {
			Integer id = getAttributeAsInteger(ID);
			try {
				thrDao.deleteThreshold(id);
				logger.debug("Threshold deleted");
				writeBackToClient( new JSONAcknowledge("Operation succeded") );
			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving Threshold to delete", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving Threshold to delete", e);
			}
		}else if (serviceType != null	&& serviceType.equalsIgnoreCase(THR_VAL_DELETE)) {
			Integer id = getAttributeAsInteger(THR_VAL_ID_TO_DELETE);
			try {
				tDao.deleteThresholdValue(id);
				
				logger.debug("Threshold value deleted deleted");
				writeBackToClient( new JSONAcknowledge("Operation succeded") );
			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving Threshold value  to delete", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving Threshold value  to delete", e);
			}
		}else if(serviceType == null){
			try {
				List nodeTypes = DAOFactory.getDomainDAO().loadListDomainsByType(THRESHOLD_DOMAIN_TYPE);
				getSessionContainer().setAttribute("thrTypesList", nodeTypes);
				List thrSeverityTypes = DAOFactory.getDomainDAO().loadListDomainsByType(THRESHOLD_SEVERITY_TYPE);
				getSessionContainer().setAttribute("thrSeverityTypes", thrSeverityTypes);
				
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
		results.put("title", "Thresholds");
		results.put("rows", rows);
		return results;
	}
	
	private List deserializeThresholdValuesJSONArray(JSONArray rows, HashMap<String, Integer> domainIds) throws JSONException{
		List toReturn = new ArrayList();
		
		for(int i=0; i< rows.length(); i++){
			JSONObject obj = (JSONObject)rows.get(i);
					
			String thVId = obj.getString(THR_VAL_ID);			
			Integer position = null;
			String pos = obj.getString(THR_VAL_POSITION);
			if(pos!=null && !pos.equals("")){
				position = new Integer(pos);			
			}
			
			Double value = null;
			String val = obj.getString(THR_VAL_VALUE);
			if(val!=null && !val.equals("")){
				value = new Double(val);			
			}
			
			String label = obj.getString(THR_VAL_LABEL);
			String colourString = obj.getString(THR_VAL_COLOR);
			String severityCd = obj.getString(THR_VAL_SEVERITY_CD);
			
			Integer severityId = null;
			if(severityCd!=null && !severityCd.equals("")){
				severityId = domainIds.get(severityCd);	
			}
			 
			Boolean minClosed = null;
			Double minValue = null;
			Boolean maxClosed = null;
			Double maxValue = null;
			String minC = obj.getString(THR_VAL_MIN_INCLUDED);
			String min = obj.getString(THR_VAL_MIN);
			String maxC = obj.getString(THR_VAL_MAX_INCLUDED);
			String max = obj.getString(THR_VAL_MAX);
			if(minC!=null && (minC.equalsIgnoreCase("true") || minC.equalsIgnoreCase("false"))){
				minClosed = new Boolean(minC);
			}
			if(maxC!=null && (maxC.equalsIgnoreCase("true") || maxC.equalsIgnoreCase("false"))){
				maxClosed = new Boolean(maxC);
			}
			if(min!=null && !min.equals("")){
				minValue = new Double(min);
			}
			if(max!=null && !max.equals("")){
				maxValue = new Double(max);
			}
	
			ThresholdValue tVal = new ThresholdValue();
			if(thVId!= null && !thVId.equals("") && !thVId.equals("0")){
				Integer thrValId = new Integer(thVId);
				tVal.setId(thrValId);
			}
			tVal.setLabel(label);						
			tVal.setPosition(position);
			tVal.setColourString(colourString);
			tVal.setValue(value);
			tVal.setSeverityCd(severityCd);						   
			tVal.setSeverityId(severityId);					
			tVal.setMinClosed(minClosed);
			tVal.setMinValue(minValue);
			tVal.setMaxClosed(maxClosed);
			tVal.setMaxValue(maxValue);

			toReturn.add(tVal);			
		}	
		return toReturn;
	}
	private String filterList(JSONObject filtersJSON) throws JSONException {
		logger.debug("IN");
		String hsql= " from SbiThreshold t ";
		if (filtersJSON != null) {
			String valuefilter = (String) filtersJSON.get(SpagoBIConstants.VALUE_FILTER);
			String typeFilter = (String) filtersJSON.get(SpagoBIConstants.TYPE_FILTER);
			String columnFilter = (String) filtersJSON.get(SpagoBIConstants.COLUMN_FILTER);
			if(typeFilter.equals("=")){
				hsql += " where t."+columnFilter+" = '"+valuefilter+"'";
			}else if(typeFilter.equals("like")){
				hsql += " where t."+columnFilter+" like '%"+valuefilter+"%'";
			}
			
		}
		logger.debug("OUT");
		return hsql;
	}
}
