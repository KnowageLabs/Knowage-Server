/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.documentsbrowser.service;

import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.FoldersJSONSerializer;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.service.AbstractBaseHttpAction;
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

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GetFTreeFoldersAction extends AbstractBaseHttpAction {
	
	// request parameters
	public static final String NODE_ID = "node";
	
	public static final String ROOT_NODE_ID = "rootNode";
	
	public static final String INCLUDE_PERSONAL_FOLDERS = "INCLUDE_PERSONAL_FOLDERS";
	public static final String PERMISSION_ON_FOLDER = "PERMISSION_ON_FOLDER";
	public static final String PERMISSION_CREATION = "CREATION";
	public static final String CHECKBOX = "CHECKBOX";
	// logger component
	private static Logger logger = Logger.getLogger(GetFTreeFoldersAction.class);
	
	public void service(SourceBean request, SourceBean response) throws Exception {
		
		String nodeId;
		List<LowFunctionality> folders;
		//boolean includePersonalFolders;
		
		logger.debug("IN");
		
		
		try {
			setSpagoBIRequestContainer( request );
			setSpagoBIResponseContainer( response );
			
			nodeId = getAttributeAsString( NODE_ID );
			String permission_on_folder = getAttributeAsString( PERMISSION_ON_FOLDER );
			logger.debug("Parameter [" + NODE_ID + "] is equal to [" + nodeId + "]");
			
			
//			String includePersonalFoldersAttribute = getAttributeAsString( INCLUDE_PERSONAL_FOLDERS );
//			if(includePersonalFoldersAttribute == null) {
//				includePersonalFolders = true;
//			} else {
//				includePersonalFolders = Boolean.parseBoolean(includePersonalFoldersAttribute);
//			}
			
			SessionContainer sessCont = getSessionContainer();
			SessionContainer permCont = sessCont.getPermanentContainer();
			IEngUserProfile profile = (IEngUserProfile)permCont.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			
			ILowFunctionalityDAO lfDao = DAOFactory.getLowFunctionalityDAO();
			if (nodeId.equalsIgnoreCase(ROOT_NODE_ID)) {
				//getting all I° level folders
				if(permission_on_folder!=null && permission_on_folder.equals(PERMISSION_CREATION)){
					folders = lfDao.loadUserFunctionalitiesFiltered(null, false, profile, PERMISSION_CREATION); 
//					List<LowFunctionality> tmpFolders = lfDao.loadUserFunctionalitiesFiltered(null, false, profile, PERMISSION_CREATION);
//					if(includePersonalFolders) {
//						folders = tmpFolders;
//					} else {
//						folders = new ArrayList<LowFunctionality>();
//						for(LowFunctionality folder : tmpFolders) {
//							if(folder.getCodType().equalsIgnoreCase("LOW_FUNCT")){
//								folders.add(folder);
//							}
//						}
//					}
					
					
					
					String userId = (String) ((UserProfile) profile).getUserId();
					// if user functionality does not exist, add it to the list but without creating it (it will be created if necessary)
//					if (!DAOFactory.getLowFunctionalityDAO().checkUserRootExists(userId) && includePersonalFolders) {
					if (!DAOFactory.getLowFunctionalityDAO().checkUserRootExists(userId)) {
						LowFunctionality userFunct = getPersonalFolder((UserProfile) profile);
						folders.add(userFunct);
					}
				}else{
					folders = lfDao.loadUserFunctionalities(null, false, profile);	
				}
			} else {
				//getting children folders
				if(permission_on_folder!=null && permission_on_folder.equals(PERMISSION_CREATION)){
					folders = lfDao.loadUserFunctionalitiesFiltered(new Integer(nodeId), false, profile, PERMISSION_CREATION);
				}else{
					folders = lfDao.loadUserFunctionalities(new Integer(nodeId), false, profile);	
				}
			}
			HttpServletRequest httpRequest = getHttpRequest();
			MessageBuilder m = new MessageBuilder();
			Locale locale = m.getLocale(httpRequest);
			JSONArray jsonFTree = new JSONArray();
			jsonFTree = (JSONArray)SerializerFactory.getSerializer("application/json").serialize( folders,locale );

			try {
				writeBackToClient( new JSONSuccess(  createNode(jsonFTree) ) ) ;
			} catch (IOException e) {
				throw new SpagoBIException("Impossible to write back the responce to the client", e);
			}
		} catch (Throwable t) {
			throw new SpagoBIException("An unexpected error occured while executing GET_FTREE_FOLDERS_ACTION", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	/**
	 * Create a LowFunctionality object with the data of a user functionality, according to the input user profile
	 * (but it does not actually create it into the metadata database) and with id = -1. 
	 * See also it.eng.spagobi.analiticalmodel.execution.service.SaveDocumentAction
	 * @param profile The user profile
	 * @return A LowFunctionality object with the data of a user functionality and id = -1
	 */
	private LowFunctionality getPersonalFolder(UserProfile profile) {
		String userId = (String) profile.getUserId();
		LowFunctionality userFunct = new LowFunctionality();
	    userFunct.setCode("ufr_" + userId);
	    userFunct.setDescription("User Functionality Root");
	    userFunct.setName(userId);
	    userFunct.setPath("/" + userId);
	    userFunct.setId(-1);
	    return userFunct;
	}
	
	
	private JSONObject createNode(String id, String text, String type, JSONArray children) {
		JSONObject node = new JSONObject();
		try {
			node.put("id", id);
			node.put("text", text );
			node.put("iconCls", type);
			
			JSONObject nodeAttributes = new JSONObject();
			nodeAttributes.put("iconCls", type);
			nodeAttributes.put("type", type);
			node.put("attributes", nodeAttributes);
			
			if(children != null) {
				//node.put("children", children);
			} else {
				node.put("leaf", true);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return node;		
	}

	/**
	 * Creates a json array with folders informations
	 * @param jsonFTree the object serialized
	 * @return the node (folder)
	 * @throws JSONException
	 */
	private JSONArray createNode(JSONArray jsonFTree) throws Exception {
		JSONObject node;
		JSONArray nodes;
		String checks = getAttributeAsString( CHECKBOX );

		
		nodes = new JSONArray();
		
		for (int i=0; i<jsonFTree.length(); i++){
			JSONObject tmpNode = jsonFTree.getJSONObject(i);
			node = new JSONObject();
			node.put("id", tmpNode.get(FoldersJSONSerializer.ID));
			node.put("text", tmpNode.get(FoldersJSONSerializer.NAME));
			node.put("iconCls", "icon-ftree-folder");
			JSONObject nodeAttributes = new JSONObject();
			nodeAttributes.put("iconCls", "icon-ftree-folder");
			node.put("attributes", nodeAttributes);
			if(checks!=null && checks.equals("true")){
				node.put("checked", false);
			}
			
			try{
				List childrenFolders = DAOFactory.getLowFunctionalityDAO().loadChildFunctionalities((Integer)tmpNode.get(FoldersJSONSerializer.ID), false);
				if (childrenFolders == null || childrenFolders.size() == 0)
					node.put("leaf", true);
			} catch (Throwable t) {
				throw new Exception("An unexpected error occured while executing GET_FTREE_FOLDERS_ACTION", t);
			}
			nodes.put(node);
		}
	

		return nodes;
	}
}
