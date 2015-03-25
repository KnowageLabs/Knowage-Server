/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.profiling.services;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.IServiceResponse;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.persistence.metamodel.Attribute;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ManageAttributesAction extends AbstractSpagoBIAction{
	
	private final String MESSAGE_DET = "MESSAGE_DET";
	// type of service
	private final String ATTR_LIST = "ATTR_LIST";
	private final String ATTR_DELETE = "ATTR_DELETE";
	private final String IS_NEW_ATTR = "IS_NEW_ATTR";
	
	private final String SAMPLES = "samples";
	private final String ID = "id";
	private final String NAME = "name";
	private final String DESCRIPTION = "description";
	private final int nameMaxLenght = 250;
	private final int descriptionMaxLenght = 500;
	//private static final String ALPHANUMERIC_STRING_REGEXP_NOSPACE="^([a-zA-Z0-9\\-\\_])*$";
	private static final String ALPHANUMERIC_STRING_REGEXP_NOSPACE="^[a-zA-Z1-9_\\x2F\\x5F\\x2D ]*$";
	/**
	 * 
	 */
	private static final long serialVersionUID = -3524157303709604995L;
	// logger component
	private static Logger logger = Logger.getLogger(ManageAttributesAction.class);
	
	@Override
	public void doService() {
		logger.debug("IN");
		ISbiAttributeDAO attrDao;
		UserProfile profile = (UserProfile) this.getUserProfile();
		try {
			attrDao = DAOFactory.getSbiAttributeDAO();
			attrDao.setUserProfile(getUserProfile());
		} catch (EMFUserError e1) {
			logger.error(e1.getMessage(), e1);
			throw new SpagoBIServiceException(SERVICE_NAME,	"Error occurred");
		}
		HttpServletRequest httpRequest = getHttpRequest();

		Locale locale = getLocale();

		String serviceType = this.getAttributeAsString(MESSAGE_DET);
		logger.debug("Service type "+serviceType);
		
		
		if (serviceType != null && serviceType.contains(ATTR_LIST)) {
			String name = null;
			String description =null;
			String idStr = null;
			
			try {
				BufferedReader b =httpRequest.getReader();
				if(b!=null){
					String respJsonObject = b.readLine();
					if(respJsonObject!=null){
						JSONObject responseJSON = deserialize(respJsonObject);
						JSONObject samples = responseJSON.getJSONObject(SAMPLES);
						if(!samples.isNull(ID)){
							idStr = samples.getString(ID);
						}
						
						//checks if it is empty attribute to start insert
						boolean isNewAttr = this.getAttributeAsBoolean(IS_NEW_ATTR);

						if(!isNewAttr){
							if(!samples.isNull(NAME)){
								
								name = samples.getString(NAME);
								HashMap<String, String> logParam = new HashMap();
								logParam.put("NAME", name);
								if (GenericValidator.isBlankOrNull(name) || 
										!GenericValidator.matchRegexp(name, ALPHANUMERIC_STRING_REGEXP_NOSPACE)||
											!GenericValidator.maxLength(name, nameMaxLenght)){
									logger.error("Either the field name is blank or it exceeds maxlength or it is not alfanumeric");
									EMFValidationError e = new EMFValidationError(EMFErrorSeverity.ERROR, description, "9000","");
									getHttpResponse().setStatus(404);	
									JSONObject attributesResponseSuccessJSON = new JSONObject();
									attributesResponseSuccessJSON.put("success", false);
									attributesResponseSuccessJSON.put("message", "Either the field name is blank or it exceeds maxlength or it is not alfanumeric");
									attributesResponseSuccessJSON.put("data", "[]");
									
									writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );
									
									AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "PROF_ATTRIBUTES.ADD",logParam , "OK");
									return;
	
								}
							}
							if(!samples.isNull(DESCRIPTION)){
								description = samples.getString(DESCRIPTION);
	
								if (GenericValidator.isBlankOrNull(description) ||
										!GenericValidator.matchRegexp(description, ALPHANUMERIC_STRING_REGEXP_NOSPACE) ||
												!GenericValidator.maxLength(description, descriptionMaxLenght)){
									logger.error("Either the field description is blank or it exceeds maxlength or it is not alfanumeric");
	
									EMFValidationError e = new EMFValidationError(EMFErrorSeverity.ERROR, description, "9000","");
									getHttpResponse().setStatus(404);	
									JSONObject attributesResponseSuccessJSON = new JSONObject();
									attributesResponseSuccessJSON.put("success", false);
									attributesResponseSuccessJSON.put("message", "Either the field description is blank or it exceeds maxlength or it is not alfanumeric");
									attributesResponseSuccessJSON.put("data", "[]");
									writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );
									HashMap<String, String> logParam = new HashMap();
									logParam.put("NAME", name);
									AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "PROF_ATTRIBUTES.ADD",logParam , "OK");
									return;
								}
							}
						
							SbiAttribute attribute = new SbiAttribute();
							if(description!=null){
								attribute.setDescription(description);
							}
							if(name!=null){
								attribute.setAttributeName(name);
							}
							boolean isNewAttrForRes = true;
							if(idStr!=null && !idStr.equals("")){
								Integer attributeId = new Integer(idStr);
								attribute.setAttributeId(attributeId.intValue());
								isNewAttrForRes = false;
							}
							Integer attrID = attrDao.saveOrUpdateSbiAttribute(attribute);
							logger.debug("Attribute updated");

							ArrayList<SbiAttribute> attributes = new ArrayList<SbiAttribute> ();
							attribute.setAttributeId(attrID);
							attributes.add(attribute);
							
							getAttributesListAdded(locale,attributes,isNewAttrForRes, attrID);
							HashMap<String, String> logParam = new HashMap();
							logParam.put("NAME", attribute.getAttributeName());
							AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "PROF_ATTRIBUTES." +((isNewAttrForRes)?"ADD":"MODIFY"),logParam , "OK");
						}
						
						//else the List of attributes will be sent to the client
					}else{
						getAttributesList(locale,attrDao);
					}
				}else{
					getAttributesList(locale,attrDao);
				}

			} catch (Throwable e) {
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "PROF_ATTRIBUTES.ADD",null , "OK");
				} catch (Exception e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
				logger.error(e.getMessage(), e);
				getHttpResponse().setStatus(404);								
				try {
					JSONObject attributesResponseSuccessJSON = new JSONObject();
					attributesResponseSuccessJSON.put("success", true);
					attributesResponseSuccessJSON.put("message", "Exception occurred while saving attribute");
					attributesResponseSuccessJSON.put("data", "[]");
					writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );	
				} catch (IOException e1) {
					logger.error(e1.getMessage(), e1);
				} catch (JSONException e2) {
					logger.error(e2.getMessage(), e2);
				}
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving attributes", e);
			}
		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(ATTR_DELETE)) {
			
			String idStr = null;
			try {
				BufferedReader b =httpRequest.getReader();
				if(b!=null){
					String respJsonObject = b.readLine();
					if(respJsonObject!=null){
						JSONObject responseJSON = deserialize(respJsonObject);
						idStr = responseJSON.getString(SAMPLES);
					}
				}
				
			} catch (IOException e1) {
				logger.error("IO Exception",e1);
				e1.printStackTrace();
			} catch (SerializationException e) {
				logger.error("Deserialization Exception",e);
				e.printStackTrace();
			} catch (JSONException e) {
				logger.error("JSONException",e);
				e.printStackTrace();
			}
			if(idStr!=null && !idStr.equals("")){
				HashMap<String, String> logParam = new HashMap();
				logParam.put("ATTRIBUTE ID", idStr);
				Integer id = new Integer(idStr);
				try {
					attrDao.deleteSbiAttributeById(id);
					logger.debug("Attribute deleted");
					JSONObject attributesResponseSuccessJSON = new JSONObject();
					attributesResponseSuccessJSON.put("success", true);
					attributesResponseSuccessJSON.put("message", "");
					attributesResponseSuccessJSON.put("data", "[]");
					writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "PROF_ATTRIBUTES.DELETE",logParam , "OK");
				} catch (Throwable e) {
					logger.error("Exception occurred while deleting attribute", e);
					getHttpResponse().setStatus(404);								
					try {
						JSONObject attributesResponseSuccessJSON = new JSONObject();
						attributesResponseSuccessJSON.put("success", false);
						attributesResponseSuccessJSON.put("message", "Exception occurred while deleting attribute");
						attributesResponseSuccessJSON.put("data", "[]");
						writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );	
						try {
							AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "PROF_ATTRIBUTES.DELETE",logParam , "KO");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} catch (IOException e1) {
						logger.error(e1.getMessage(), e1);
					} catch (JSONException e2) {
						// TODO Auto-generated catch block
						logger.error(e2.getMessage(), e2);
					}
/*					throw new SpagoBIServiceException(SERVICE_NAME,
							"Exception occurred while deleting attribute",
							e);*/
				}
			}
		}
		logger.debug("OUT");
		
	}
	
	private void getAttributesList(Locale locale,ISbiAttributeDAO attrDao) throws SerializationException, IOException, EMFUserError, JSONException{
		ArrayList<SbiAttribute> attributes = (ArrayList<SbiAttribute>)attrDao.loadSbiAttributes();
		logger.debug("Loaded attributes list");
		JSONArray attributesJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(attributes,	locale);
		JSONObject attributesResponseJSON = createJSONResponseAttributes(attributesJSON);

		writeBackToClient(new JSONSuccess(attributesResponseJSON));

	}
	
	private void getAttributesListAdded(Locale locale, ArrayList<SbiAttribute> attributes, boolean isAdded, Integer attrID) throws SerializationException, IOException, EMFUserError, JSONException{

		JSONArray attributesJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(attributes,locale);
		JSONObject attributesResponseJSON = createJSONResponseAttributes(attributesJSON);
		attributesResponseJSON.put("success", true);
		attributesResponseJSON.put("id", attrID);
		attributesResponseJSON.put("newAttr", isAdded);

		writeBackToClient(new JSONSuccess(attributesResponseJSON));

	}	
	
	/**
	 * Creates a json array with children attributes informations
	 * 
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	private JSONObject createJSONResponseAttributes(JSONArray rows)
			throws JSONException {
		JSONObject results;

		results = new JSONObject();
		results.put("title", "Attributes");
		results.put(SAMPLES, rows);
		return results;
	}
	
	private JSONObject deserialize(Object o) throws SerializationException {
		JSONObject responseJSON = null;
		logger.debug("IN");
		
		try {
			Assert.assertNotNull(o, "Object to be deserialized cannot be null");
			
			if(o instanceof String) {
				logger.debug("Deserializing string [" + (String)o + "]");
				try {
					responseJSON = new JSONObject( (String)o );
				} catch(Throwable t) {
					logger.error("Object to be deserialized must be string encoding a JSON object",t);
					throw new SerializationException("An error occurred while deserializing query: " + (String)o, t);
				}
			} else {
				Assert.assertUnreachable("Object to be deserialized must be of type string or of type JSONObject, not of type [" + o.getClass().getName() + "]");
			}
		
		} finally {
			logger.debug("OUT");
		}
		return responseJSON;
	}
	
}
