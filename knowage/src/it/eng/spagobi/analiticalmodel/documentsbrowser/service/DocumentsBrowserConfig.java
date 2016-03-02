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


import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.container.SpagoBISourceBeanContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Antonella Giachino (antonella.giachino@eng.it), Andrea Gioia (andrea.gioia@eng.it)
 */
public class DocumentsBrowserConfig {
	
	private SourceBean configSB;
	
	private static Map folderFields;
	private static Map documentFields;
	private static Map documentSearchableFields;
	
	public static final String ID = "id";
	public static final String CODE = "code";
	public static final String CODTYPE = "codType";
	public static final String PATH = "path";
	public static final String PROG = "prog";
	public static final String PARENTID = "parentId";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	
	public static final String DEVROLES = "devRoles";
	public static final String TESTROLES = "testRoles";
	public static final String EXECROLES = "execRoles";
	public static final String BIOBJECTS = "biObjects";
	
	static {
		folderFields = new HashMap();
		folderFields.put("id", "id");
		folderFields.put("code", "code");
		folderFields.put("codType", "codType");
		folderFields.put("path", "path");
		folderFields.put("prog", "prog");
		folderFields.put("parentId", "parentId");
		folderFields.put("name", "name");
		folderFields.put("description", "description");		
		folderFields.put("devRoles", "devRoles");
		folderFields.put("testRoles", "testRoles");
		folderFields.put("execRoles", "execRoles");
		folderFields.put("biObjects", "biObjects");
		
		documentFields = new HashMap();
		documentFields.put("id", "id");
		documentFields.put("label", "label");
		documentFields.put("name", "name");
		documentFields.put("description", "description");
		documentFields.put("typeCode", "typeCode");
		documentFields.put("encrypt", "encrypt");
		documentFields.put("profiledVisibility", "profiledVisibility");
		documentFields.put("engine", "engine");
		documentFields.put("datasource", "datasource");
		documentFields.put("dataset", "dataset");		
		documentFields.put("uuid", "uuid");
		documentFields.put("relname", "relname");
		documentFields.put("stateCode", "stateCode");
		documentFields.put("stateId", "stateId");
		documentFields.put("functionalities", "functionalities");
		documentFields.put("creationDate", "creationDate");
		documentFields.put("extendedDescription", "extendedDescription");
		documentFields.put("creationUser", "creationUser");
		documentFields.put("language", "language");
		documentFields.put("objectve", "objectve");
		documentFields.put("keywords", "keywords");
		documentFields.put("refreshSeconds", "refreshSeconds");
		documentFields.put("resourcesPath", "resourcesPath");
		
		documentSearchableFields = new HashMap();
		documentSearchableFields.put("label", "label");
		documentSearchableFields.put("name", "name");
		documentSearchableFields.put("stateCode", "stateCode");
		documentSearchableFields.put("creationDate", "creationDate");
		documentSearchableFields.put("engine", "engine");
	}
	
	private static DocumentsBrowserConfig instance;
	public static DocumentsBrowserConfig getInstance() {
		if(instance == null) {
			instance = new DocumentsBrowserConfig();
		}
		return instance;
	}
	
	// logger component
	private static Logger logger = Logger.getLogger(DocumentsBrowserConfig.class);
	
	private DocumentsBrowserConfig() {
		ConfigSingleton config = ConfigSingleton.getInstance();
		configSB = (SourceBean)config.getAttribute("DOCUMENT_BROWSER");
		logger.debug ("SpagoBI Document Browser configuration retrived");
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject jsonObj = new JSONObject();
		
		jsonObj.put("metaFolder", foldersMetaToJSON());
		jsonObj.put("metaDocument", documentsMetaToJSON());
		
		return jsonObj;
	}
	
	private JSONArray foldersMetaToJSON() throws JSONException {
		JSONArray jsonFolders = new JSONArray();
		
		
		SourceBean sbFolder = (SourceBean)configSB.getAttribute("FOLDER");
		
		//loops on documents
		List fieldList = sbFolder.getAttributeAsList("FIELD");
		for(int i = 0; i < fieldList.size(); i++) {
			SourceBean fieldSB = (SourceBean)fieldList.get(i);
			SpagoBISourceBeanContainer c = new SpagoBISourceBeanContainer(fieldSB);
			if( folderFields.containsKey( c.getString("id") )	) {
				JSONObject attrD = new JSONObject();
				attrD.put("id", c.getString("id"));
				attrD.put("visible", c.getBoolean("visible"));
				attrD.put("showLabel", c.getBoolean("showLabel"));
				if(c.get("maxChars") != null) {
					attrD.put("maxChars", c.getInteger("maxChars"));
				}
					
				jsonFolders.put(attrD);
			} else {
				logger.info("field id [" + c.getString("id") + "] is not valid. The configuration row will be ignored");
			}			
		}
		
		return jsonFolders;
	}
	
	private JSONArray documentsMetaToJSON() throws JSONException {
		JSONArray jsonDocs = new JSONArray();
		
		SourceBean sbDocs = (SourceBean)configSB.getAttribute("DOCUMENT");
						
		//loops on documents
		List fieldList = sbDocs.getAttributeAsList("FIELD");
		for(int i = 0; i < fieldList.size(); i++) {
			SourceBean fieldSB = (SourceBean)fieldList.get(i);
			SpagoBISourceBeanContainer c = new SpagoBISourceBeanContainer(fieldSB);
			if( documentFields.containsKey( c.getString("id") )	) {
				JSONObject attrD = new JSONObject();
				attrD.put("id", c.getString("id"));
				attrD.put("visible", c.getBoolean("visible"));
				attrD.put("sortable", c.getBoolean("sortable"));
				attrD.put("groupable", c.getBoolean("groupable"));
				if(documentSearchableFields.containsKey( c.getString("id") )) {
					attrD.put("searchable", c.getBoolean("searchable"));
				} else {
					logger.info("field id [" + c.getString("id") + "] cannot be sorted. Configuration property will be ignored");
				}
				attrD.put("showLabel", c.getBoolean("showLabel"));
				if(c.get("maxChars") != null) {
					attrD.put("maxChars", c.getInteger("maxChars"));
				}
				SingletonConfig configSingleton = SingletonConfig.getInstance();
				String path  = configSingleton.getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
				String resourcePath= SpagoBIUtilities.readJndiResource(path);
				attrD.put("pathResources", resourcePath);
				jsonDocs.put(attrD);
			} else {
				logger.info("field id [" + c.getString("id") + "] is not valid. The configuration row will be ignored");
			}
			
			
			
		}
		
		return jsonDocs;
	}

}
