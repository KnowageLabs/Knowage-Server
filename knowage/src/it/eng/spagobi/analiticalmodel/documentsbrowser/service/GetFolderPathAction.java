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
package it.eng.spagobi.analiticalmodel.documentsbrowser.service;

import it.eng.spagobi.analiticalmodel.documentsbrowser.utils.FolderContentUtil;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.FoldersJSONSerializer;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public class GetFolderPathAction extends AbstractSpagoBIAction {
	
	// REQUEST PARAMETERS
	public static final String FOLDER_ID = "folderId";
	public static final String ROOT_FOLDER_ID = "rootFolderId";
	
	public static final String ROOT_NODE_ID = "rootNode";
	
	// logger component
	private static Logger logger = Logger.getLogger(GetFolderPathAction.class);
	
	public void doService() {
		
		List functionalities = new ArrayList();
		
		logger.debug("IN");
		
		try {
			
			String folderIdStr = getAttributeAsString(FOLDER_ID);
			String rootFolderID = getAttributeAsString(ROOT_FOLDER_ID);	
			
			logger.debug("Parameter [" + FOLDER_ID + "] is equal to [" + folderIdStr + "]");
			logger.debug("Parameter [" + ROOT_FOLDER_ID + "] is equal to [" + rootFolderID + "]");
			
			FolderContentUtil fcUtil = new FolderContentUtil();
			LowFunctionality folder = null;
			if (folderIdStr != null && !folderIdStr.equalsIgnoreCase(FolderContentUtil.ROOT_NODE_ID)) {
				int folderId = new Integer(folderIdStr);
				logger.debug("Folder id is " + folderId);
				folder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(folderId, false);
				logger.debug("Folder is " + folder);
				boolean canSee = fcUtil.checkRequiredFolder(folder, this.getUserProfile());
				if (!canSee) {
					logger.error("Required folder does not exist or you don't have priviledges to see it");
					throw new SpagoBIServiceException(SERVICE_NAME, "Required folder does not exist or you don't have priviledges to see it");
				}
			}

			// Check if there is folder specified as home for the document browser (Property in SBI_CONFIG with label SPAGOBI.DOCUMENTBROWSER.HOME)
			if (folder == null) {
				Config documentBrowserHomeConfig = DAOFactory.getSbiConfigDAO().loadConfigParametersByLabel("SPAGOBI.DOCUMENTBROWSER.HOME");
				if (documentBrowserHomeConfig != null) {
					if (documentBrowserHomeConfig.isActive()) {

						String folderLabel = documentBrowserHomeConfig.getValueCheck();

						if (!StringUtils.isEmpty(folderLabel)) {
							folder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByCode(folderLabel, false);

						}

					}
				}

				
			}
			// ------------------

			if (folder == null || folderIdStr.equalsIgnoreCase(ROOT_NODE_ID)) {
				// getting default folder (root)
				folder = DAOFactory.getLowFunctionalityDAO().loadRootLowFunctionality(false);
				functionalities.add(folder);
			} else {
				functionalities = DAOFactory.getLowFunctionalityDAO().loadParentFunctionalities(folder.getId(),
						(rootFolderID == null ? null : Integer.valueOf(rootFolderID)));
			}
			
			HttpServletRequest httpRequest = getHttpRequest();
			MessageBuilder m = new MessageBuilder();
			Locale locale = m.getLocale(httpRequest);
			JSONArray foldersJSON = (JSONArray)SerializerFactory.getSerializer("application/json").serialize( functionalities,locale );
			
			try {
				writeBackToClient( new JSONSuccess(  createJSONResponse(foldersJSON) ) ) ;
			} catch (IOException e) {
				throw new SpagoBIException("Impossible to write back the responce to the client", e);
			}
			
		} catch (SpagoBIServiceException e) {
			throw e;
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get folder content", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	/**
	 * Creates a json array with parents folder informations
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	private JSONArray createJSONResponse(JSONArray rows) throws JSONException {
		JSONObject node;
		JSONArray nodes;

		nodes = new JSONArray();
		
		for (int i=rows.length()-1; i>=0; i--){
			JSONObject tmpNode = rows.getJSONObject(i);
			node = new JSONObject();
			node.put("id", tmpNode.get(FoldersJSONSerializer.ID));
			node.put("name", tmpNode.get(FoldersJSONSerializer.NAME));
			node.put("path", tmpNode.get(FoldersJSONSerializer.PATH));
			
			nodes.put(node);
		}
		return nodes;
	}
	
}
