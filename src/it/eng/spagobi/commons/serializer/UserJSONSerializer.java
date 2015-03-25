/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bo.UserBO;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author franceschini
 *
 */
public class UserJSONSerializer implements Serializer {
	private static Logger logger = Logger.getLogger(UserJSONSerializer.class);
	
	public static final String USER_ID = "userId";
	public static final String FULL_NAME = "fullName";
	public static final String ID = "id";
	public static final String PWD = "pwd";

	public Object serialize(Object o, Locale locale)
			throws SerializationException {
		logger.debug("IN");
		JSONObject result = new JSONObject();

		if ( !(o instanceof UserBO) ) {
			throw new SerializationException("UserJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {

			UserBO userBO = (UserBO)o;
			result.put(ID, userBO.getId());
			result.put(USER_ID, userBO.getUserId());
			result.put(FULL_NAME, userBO.getFullName());
			result.put(PWD, userBO.getPassword());
			
			//roles
			List allRoles = DAOFactory.getRoleDAO().loadAllRoles();
			Iterator itAllRoles = allRoles.iterator();
			
			List<Integer> userRoles = userBO.getSbiExtUserRoleses();
			//Iterator itRoles = userRoles.iterator();
			
			JSONArray rolesJSON = new JSONArray();
			//rolesJSON.put("roles");
			
			while (itAllRoles.hasNext()) {
				JSONObject jsonRole = new JSONObject();
				Role role = (Role) itAllRoles.next();
				if (role != null) {
					Integer roleId = role.getId();
					if (userRoles.contains(roleId)) {
						jsonRole.put("name", role.getName());
						jsonRole.put("id", role.getId());
						jsonRole.put("description", role.getDescription());
						jsonRole.put("checked", true);
						rolesJSON.put(jsonRole);
					}
				}
			}

			/*while(itRoles.hasNext()){
				JSONObject jsonRole = new JSONObject();
				Integer roleId = (Integer)itRoles.next();

				Role role = DAOFactory.getRoleDAO().loadByID(roleId);
				jsonRole.put("name", role.getName());
				jsonRole.put("id", role.getId());
				jsonRole.put("description", role.getDescription());
				jsonRole.put("checked", true);
				rolesJSON.put(jsonRole);
			}	*/
			result.put("userRoles", rolesJSON);
			
			List allAttributes = DAOFactory.getSbiAttributeDAO().loadSbiAttributes();
			Iterator itAttrs = allAttributes.iterator();
			
			//attributes
			HashMap<Integer, HashMap<String, String>> userAttributes = userBO.getSbiUserAttributeses();
			//Iterator itAttrs = userAttributes.keySet().iterator();
			JSONArray attrsJSON = new JSONArray();
			//attrsJSON.put("attributes");

			while(itAttrs.hasNext()){
				JSONObject jsonAttr = new JSONObject();
				SbiAttribute attr =(SbiAttribute)itAttrs.next();
				Integer userAttrID = attr.getAttributeId();
				jsonAttr.put("name", attr.getAttributeName());
				jsonAttr.put("id", userAttrID);
				if(userAttributes.containsKey(userAttrID)){
					HashMap<String, String> nameAndValueAttr = userAttributes.get(userAttrID);				
					String attrName= nameAndValueAttr.keySet().iterator().next();//unique value

					jsonAttr.put("value", nameAndValueAttr.get(attrName));
									
				}else{
					jsonAttr.put("value","");
				}
				attrsJSON.put(jsonAttr);
				//Integer userAttrID = (Integer)itAttrs.next();
				
				
			}	
			result.put("userAttributes", attrsJSON);
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			logger.debug("OUT");
		}
		return result;
	}

}
