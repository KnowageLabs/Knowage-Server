/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import java.util.Collection;
import java.util.Locale;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 * @authors Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class DocumentsJSONDecorator {

	public static final String DECORATORS = "decorators";
	public static final String IS_SAVABLE = "isSavable";
	
	static private Logger logger = Logger.getLogger(DocumentsJSONDecorator.class);
	
	public static JSONArray decorateDocuments(JSONArray documents, IEngUserProfile profile, LowFunctionality folder) {
		if (documents != null) {
			for (int i = 0; i < documents.length(); i++) {
				try {
					decorateDocument(
							documents.getJSONObject(i), profile, folder);
				} catch (JSONException e) {
					throw new SpagoBIRuntimeException("Error while decorating document with index " + i, e);
				}
			}
		}
		return documents;
	}
	
	public static JSONObject decorateDocument(JSONObject document, IEngUserProfile profile, LowFunctionality folder) {
		return decorateDocument(document, profile, folder, null);
	}
	
	public static JSONObject decorateDocument(JSONObject document, IEngUserProfile profile, LowFunctionality folder, Locale locale) {
		JSONObject decorators = new JSONObject();
		try {
			document.put(DECORATORS, decorators);
			addExecutabilityToFunctionalities(document, profile);
			addDeleteAction(document, profile, folder, locale);
			addCloneAction(document, profile, folder, locale);
			addDetailAction(document, profile, locale);
			addShowMetadataAction(document, profile, locale);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while decorating document", e);
		}
		return document; 
	}
	
	private static void addShowMetadataAction(JSONObject document,
			IEngUserProfile profile, Locale locale) throws JSONException, EMFInternalError {
		Collection userFunctionalities = profile.getFunctionalities();
		if (userFunctionalities.contains("SeeMetadataFunctionality")) {
			JSONObject showmetadataAction = new JSONObject();
			MessageBuilder msgBuild=new MessageBuilder();
			showmetadataAction.put("name", "showmetadata");
			showmetadataAction.put("description", msgBuild.getMessage( "sbiobjects.actions.metadata.description", locale));
			document.getJSONArray(DocumentsJSONSerializer.ACTIONS).put(
					showmetadataAction);
		}
	}

	private static void addDetailAction(JSONObject document,
			IEngUserProfile profile, Locale locale) throws JSONException {
		JSONObject detailAction = new JSONObject();
		MessageBuilder msgBuild=new MessageBuilder();
		detailAction.put("name", "detail");
		detailAction.put("description", msgBuild.getMessage( "sbiobjects.actions.detail.description", locale));
		if (ObjectsAccessVerifier
				.canDevBIObject(document.getInt(DocumentsJSONSerializer.ID), profile)) {
			document.getJSONArray(DocumentsJSONSerializer.ACTIONS).put(detailAction);
		}
	}

	private static void addDeleteAction(JSONObject document,
			IEngUserProfile profile, LowFunctionality folder, Locale locale) throws JSONException {
		boolean canDelete = false;
		if (folder == null) {
			canDelete = ObjectsAccessVerifier.canDeleteBIObject(document.getInt(DocumentsJSONSerializer.ID), profile);
		} else {
			canDelete = ObjectsAccessVerifier.canDeleteBIObject(document.getInt(DocumentsJSONSerializer.ID), profile, folder);
		}
		if (canDelete) {
			JSONObject deleteAction = new JSONObject();
			MessageBuilder msgBuild=new MessageBuilder();
			deleteAction.put("name", "delete");
			deleteAction.put("description", msgBuild.getMessage( "sbiobjects.actions.delete.description", locale));
			document.getJSONArray(DocumentsJSONSerializer.ACTIONS).put(
					deleteAction);
		}
	}
	
	private static void addCloneAction(JSONObject document,
			IEngUserProfile profile, LowFunctionality folder, Locale locale) throws JSONException {
		boolean canClone = false;
		if (folder == null) {
			canClone = ObjectsAccessVerifier.canCloneBIObject(document.getInt(DocumentsJSONSerializer.ID), profile);
		} else {
			canClone = ObjectsAccessVerifier.canCloneBIObject(document.getInt(DocumentsJSONSerializer.ID), profile, folder);
		}
		if (canClone) {
			JSONObject cloneAction = new JSONObject();
			MessageBuilder msgBuild=new MessageBuilder();
			cloneAction.put("name", "clone");
			cloneAction.put("description", msgBuild.getMessage( "sbiobjects.actions.clone.description", locale));
			document.getJSONArray(DocumentsJSONSerializer.ACTIONS).put(
					cloneAction);
		}
	}

	private static void addExecutabilityToFunctionalities(JSONObject document,
			IEngUserProfile profile) throws JSONException {
		if (profile != null) {
			JSONArray functionalities = (JSONArray) document
					.get(DocumentsJSONSerializer.FUNCTIONALITIES);
			try {
				document.getJSONObject(DECORATORS).put(
						IS_SAVABLE,
						ObjectsAccessVerifier.isAbleToSave(functionalities,
								profile));
			} catch (EMFInternalError e) {
				logger.error(
						"Error reading if the object is savable by the user "
								+ profile.getUserUniqueIdentifier(), e);
				throw new SpagoBIRuntimeException(
						"Error reading if the object is savable by the user "
								+ profile.getUserUniqueIdentifier(), e);
			}
		}
	}
	
}
