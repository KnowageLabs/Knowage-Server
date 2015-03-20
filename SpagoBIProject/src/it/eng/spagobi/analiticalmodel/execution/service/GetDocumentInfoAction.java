/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Zerbetto Davide
 */
public class GetDocumentInfoAction extends ExecuteDocumentAction {
	
	public static final String SERVICE_NAME = "GET_DOCUMENT_INFO_ACTION";
	
	// logger component
	private static Logger logger = Logger.getLogger(GetDocumentInfoAction.class);
	
	public void doService() {
		logger.debug("IN");
		
		try {
			BIObject obj = getRequiredBIObject();
			boolean documentFound = false;
			boolean canSeeDocument = false;
			UserProfile profile = (UserProfile) getUserProfile();
			documentFound = obj != null;
			if (obj != null) {
				canSeeDocument = ObjectsAccessVerifier.canSee(obj, profile);
			}
			HttpServletRequest httpRequest = getHttpRequest();
			MessageBuilder m = new MessageBuilder();
			Locale locale = m.getLocale(httpRequest);
			JSONObject result = new JSONObject();
			if (!documentFound) {
				result.put("documentFound", false);
			} else {
				result.put("documentFound", true);
				if (!canSeeDocument) {
					result.put("canSeeDocument", false);
				} else {
					JSONObject document = (JSONObject) SerializerFactory.getSerializer("application/json").serialize( obj ,locale);
					result.put("document", document);
					result.put("canSeeDocument", true);						
					SubObject subObject = getRequiredSubObject(obj);
					if (subObject != null) {
						if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN) || 
								(subObject.getIsPublic().booleanValue() || subObject.getOwner().equals(profile.getUserId()))) {
							JSONObject subObjectJSON = (JSONObject) SerializerFactory.getSerializer("application/json").serialize( subObject ,locale);
							result.put("subobject", subObjectJSON);
						} else {
							logger.warn("User cannot see subobject [" + subObject.getName() + "] of document with label [" + obj.getLabel() + "].");
							result.put("canSeeSubobject", false);
						}
					}
				}
			}
			writeBackToClient( new JSONSuccess( result ) );
		} catch (IOException e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
		} catch (SerializationException e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects", e);
		} catch (JSONException e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects into a JSON object", e);
		} catch (EMFInternalError e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Service internal error", e);
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Service internal error", e);
		} finally {
			logger.debug("OUT");
		}
	}
	
}
