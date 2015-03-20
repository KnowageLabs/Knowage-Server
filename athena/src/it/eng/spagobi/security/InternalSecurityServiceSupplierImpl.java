/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.security;


import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class InternalSecurityServiceSupplierImpl implements
		ISecurityServiceSupplier {
	
	static private Logger logger = Logger.getLogger(InternalSecurityServiceSupplierImpl.class);

	private SpagoBIUserProfile checkAuthentication(SbiUser user, String userId, String psw) {
        logger.debug("IN - userId: " + userId);
		
		if (userId == null) {
			return null;
		}

		// get user from database
		
		try {
			
			String password = user.getPassword();
			String encrPass = Password.encriptPassword(psw);
			if (password == null || password.length() == 0) {
			    logger.error("UserName/pws not defined into database");
			    return null;
			}else if(!password.equals(encrPass)){
				logger.error("UserName/pws not found into database");
				return null;
			}
			
		    logger.debug("Logged in with SHA pass");
			SpagoBIUserProfile obj = new SpagoBIUserProfile();
			obj.setUniqueIdentifier(user.getUserId());
			obj.setUserId(user.getUserId());
			obj.setUserName(user.getFullName());
			obj.setOrganization(user.getCommonInfo().getOrganization());
			obj.setIsSuperadmin(user.getIsSuperadmin());
			
			logger.debug("OUT");
			return obj;
		} catch (EMFUserError e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error("PASS decrypt error:"+e.getMessage(), e);
		}
		return null;

	
		
	}
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
        logger.debug("IN - userId: " + userId);
		
		if(userId != null){
		  SbiUser user;
		  try{
		    user = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(userId);
		    if(user == null){
		      logger.error("UserName not found into database");
		      return null;
		    }
		  }catch (EMFUserError e) {
		     logger.error(e.getMessage(), e);
		     return null;
		  }
		  return checkAuthentication( user, userId, psw );
		}
		return null;
	}

	public SpagoBIUserProfile checkAuthenticationWithToken(String userId,
			String token) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean checkAuthorization(String userId, String function) {
		// TODO Auto-generated method stub
		return false;
	}

	public SpagoBIUserProfile createUserProfile(String userId) {
		logger.debug("IN - userId: " + userId);
		SpagoBIUserProfile profile = null;
		try {
			SbiUser user = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(userId);

			if (user == null) {
				logger.error("UserName [" + userId + "] not found!!");
			    return null;
			}
	
			profile = new SpagoBIUserProfile();
			profile.setUniqueIdentifier(user.getUserId());
			profile.setUserId(user.getUserId());
			profile.setUserName(user.getFullName());
			profile.setOrganization(user.getCommonInfo().getOrganization());
			profile.setIsSuperadmin(user.getIsSuperadmin());
	
			// get user name
			String userName = userId;
			// get roles of the user
			
			ArrayList<SbiExtRoles> rolesSB = DAOFactory.getSbiUserDAO().loadSbiUserRolesById(user.getId());
			List roles = new ArrayList();
			Iterator iterRolesSB = rolesSB.iterator();
			
			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			while (iterRolesSB.hasNext()) {
				SbiExtRoles roleSB = (SbiExtRoles) iterRolesSB.next();

			    roles.add(roleSB.getName());
			}
			HashMap attributes = new HashMap();
			ArrayList<SbiUserAttributes> attribs = DAOFactory.getSbiUserDAO().loadSbiUserAttributesById(user.getId());
			if(attribs != null){
				Iterator iterAttrs = attribs.iterator();
				while(iterAttrs.hasNext()){
				    // Attribute to lookup
					SbiUserAttributes attribute = (SbiUserAttributes) iterAttrs.next();
					
					String attributeName = attribute.getSbiAttribute().getAttributeName();

				    String attributeValue = attribute.getAttributeValue();
				    if (attributeValue != null) {
				    	logger.debug("Add attribute. " + attributeName + "=" + attributeName + " to the user"
				    			+ userName);
						attributes.put(attributeName, attributeValue);
				    }
				}
			}
	
			logger.debug("Attributes load into SpagoBI profile: " + attributes);
	
			// end load profile attributes
	
			String[] roleStr = new String[roles.size()];
			for (int i = 0; i < roles.size(); i++) {
			    roleStr[i] = (String) roles.get(i);
			}
	
			profile.setRoles(roleStr);
			profile.setAttributes(attributes);
		} catch (EMFUserError e) {
			logger.error(e.getMessage(), e);
			return null;
		}
		logger.debug("OUT");
		return profile;

	}

}
