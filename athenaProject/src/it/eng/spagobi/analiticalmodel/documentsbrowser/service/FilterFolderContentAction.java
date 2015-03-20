/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.documentsbrowser.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.service.AbstractBaseHttpAction;
import it.eng.spagobi.utilities.service.JSONSuccess;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class FilterFolderContentAction  extends AbstractBaseHttpAction{
	
	// REQUEST PARAMETERS
	public static String CALLBACK = "callback";
	public static final String FOLDER_ID = "folderId";
	public static final String ROOT_FOLDER_ID = "rootFolderId";
	
	
	public static final String ROOT_NODE_ID = "rootNode";
		
	
	// logger component
	private static Logger logger = Logger.getLogger(SearchContentAction.class);
	
	public void service(SourceBean request, SourceBean response) throws Exception {
		
		List objects;
		
		logger.debug("IN");
		
		try {
			setSpagoBIRequestContainer( request );
			setSpagoBIResponseContainer( response );
			
			String folderID = getAttributeAsString(FOLDER_ID);	
			String rootFolderID = getAttributeAsString(ROOT_FOLDER_ID);	
			String typeFilter = getAttributeAsString(SpagoBIConstants.TYPE_FILTER);
			String valueFilter = getAttributeAsString(SpagoBIConstants.VALUE_FILTER);
			String columnFilter = getAttributeAsString(SpagoBIConstants.COLUMN_FILTER );
			String scope = getAttributeAsString(SpagoBIConstants.SCOPE );
			
			String callback = getAttributeAsString( CALLBACK );
			logger.debug("Parameter [" + CALLBACK + "] is equals to [" + callback + "]");
			
			
			logger.debug("Parameter [" + FOLDER_ID + "] is equal to [" + folderID + "]");
			logger.debug("Parameter [" + ROOT_FOLDER_ID + "] is equal to [" + rootFolderID + "]");
			logger.debug("Parameter [" + SpagoBIConstants.TYPE_FILTER + "] is equal to [" + typeFilter + "]");
			logger.debug("Parameter [" + SpagoBIConstants.VALUE_FILTER + "] is equal to [" + valueFilter + "]");
			logger.debug("Parameter [" + SpagoBIConstants.COLUMN_FILTER + "] is equal to [" + columnFilter + "]");
			logger.debug("Parameter [" + SpagoBIConstants.SCOPE + "] is equal to [" + scope + "]"); //'node' or 'tree'
			
			folderID = folderID!=null? folderID: rootFolderID;
			
			//getting default folder (root)
			LowFunctionality rootFunct = DAOFactory.getLowFunctionalityDAO().loadRootLowFunctionality(false);
			if (folderID == null || folderID.equalsIgnoreCase(ROOT_NODE_ID))
				folderID = String.valueOf(rootFunct.getId());
						
			SessionContainer sessCont = getSessionContainer();
			SessionContainer permCont = sessCont.getPermanentContainer();
			IEngUserProfile profile = (IEngUserProfile)permCont.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			
			
			//getting  documents
			List tmpObjects = DAOFactory.getBIObjectDAO().searchBIObjects(valueFilter, typeFilter, columnFilter, scope, Integer.valueOf(folderID), profile);
			objects = new ArrayList();
			if(tmpObjects != null) {
                for(Iterator it = tmpObjects.iterator(); it.hasNext();) {
                    BIObject obj = (BIObject)it.next();
                    if(ObjectsAccessVerifier.checkProfileVisibility(obj, profile))
                    	objects.add(obj);
                }
			}
		
			JSONArray documentsJSON = (JSONArray)SerializerFactory.getSerializer("application/json").serialize( objects,null );
			JSONObject documentsResponseJSON =  createJSONResponseDocuments(documentsJSON);
		
			try {
				if(callback == null) {
					writeBackToClient( new JSONSuccess( createJSONResponse(documentsResponseJSON) ) );
				} else {
					writeBackToClient( new JSONSuccess( createJSONResponse(documentsResponseJSON), callback ) );
				}
			} catch (IOException e) {
				throw new SpagoBIException("Impossible to write back the responce to the client", e);
			}
			
		} catch (Throwable t) {
			throw new SpagoBIException("An unexpected error occured while executing " + getActionName(), t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	/**
	 * Creates a json array with children document informations
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	private JSONObject createJSONResponseDocuments(JSONArray rows) throws JSONException {
		JSONObject results;
		
		results = new JSONObject();
		results.put("title", "Documents");
		results.put("icon", "document.png");
		results.put("samples", rows);
		return results;
	}


	/**
	 * Creates a json array with children document informations
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	private JSONObject createJSONResponse(JSONObject documents) throws JSONException {
		JSONObject results = new JSONObject();
		JSONArray folderContent = new JSONArray();

		folderContent.put(documents);
		results.put("folderContent", folderContent);
		
		return results;
	}
}
