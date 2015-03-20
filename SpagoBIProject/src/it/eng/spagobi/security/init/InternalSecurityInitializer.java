/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.security.init;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.init.InitializerIFace;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiAuthorizations;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.profiling.bean.SbiExtUserRolesId;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bean.SbiUserAttributesId;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.security.Password;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class InternalSecurityInitializer implements InitializerIFace {

	private SourceBean _config = null;
	
	
	static private Logger logger = Logger.getLogger(InternalSecurityInitializer.class);
	
	public SourceBean getConfig() {
		return _config;
	}
	
	public void init(SourceBean config) {
		
		logger.debug("IN");
		
		try {
			if(config == null) {
				logger.warn("Security initialization aborted because the input parameter [config] is null");
				return;
			}
			
			_config = config;
			
			List<SbiAttribute> attributesList = initProfileAttributes(config);
			List<Role> rolesList = initRoles(config);
			Map<String,Integer> usersLookupMap = initUsers(config);
			initDefaultAuthorizations(config);
			
			ISbiUserDAO userDAO= DAOFactory.getSbiUserDAO();
			
			//finally default users associations
			List<SourceBean> defaultsUsers = _config.getAttributeAsList("DEFAULT_USERS.USER");
			for (SourceBean defaultUser : defaultsUsers) {
			    	
		    	String userId = (String) defaultUser.getAttribute("userId");
		    	String organization = (String) defaultUser.getAttribute("organization");
		    	userDAO.setTenant(organization);
			    List<SourceBean> attributes = defaultUser.getAttributeAsList("ATTRIBUTE");
			    if(attributes != null){
				    for(int i= 0; i< attributes.size(); i++){
				    	SourceBean attribute = attributes.get(i);
				    	String name = (String)attribute.getAttribute("name");
				    	String value = (String)attribute.getAttribute("value");
				    	logger.debug("Setting attribute [" + name +"] of user [" + userId + "] to value [" + value + "]");
				    	if(usersLookupMap.get(userId) == null) {
				    		logger.debug("User [" + userId + "] was already stored in the database. The value of attribute [" + name +"] will not be overwritten");
				    		continue;
				    	}
				    	
				    	
				    	SbiUserAttributes sbiUserAttr = new SbiUserAttributes();
				    	sbiUserAttr.setAttributeValue(value);
				    	
				    	Integer attrID = findAttributeId(attributesList, name, organization);
				    	
				    	SbiUserAttributesId sbiUserAttrID = new SbiUserAttributesId();
				    	sbiUserAttrID.setId( usersLookupMap.get(userId) );//user ID
				    	sbiUserAttrID.setAttributeId(attrID.intValue());
				    	sbiUserAttr.setId(sbiUserAttrID);
				    	
				    	userDAO.updateSbiUserAttributes(sbiUserAttr);
				    	
				    	logger.debug("Attribute [" + name +"] of user [" + userId + "] succesfully set to value [" + value + "]");
				    }
			    }
			   
			    List<SourceBean> userroles = defaultUser.getAttributeAsList("ROLE");
			    if(userroles != null){
			    	for(int i= 0; i< userroles.size(); i++){
				    	SourceBean role = userroles.get(i);
				    	String name = (String)role.getAttribute("name");
				    	logger.debug("Creating association beetween user [" + userId +"] and role [" + name + "]");
				    	if(usersLookupMap.get(userId) == null) {
				    		logger.debug("User [" + userId + "] was already stored in the database. The associatino with role [" + name +"] will not be created");
				    		continue;
				    	}
				    	
				    	SbiExtUserRoles sbiExtUserRole = new SbiExtUserRoles();
				    	SbiExtUserRolesId id = new SbiExtUserRolesId();
				    	
				    	Integer extRoleId = findRoleId(rolesList, name, organization);

				    	int userIdInt= usersLookupMap.get(userId).intValue();
				    	id.setExtRoleId(extRoleId);//role Id
				    	id.setId(userIdInt);//user ID
				    
				    	sbiExtUserRole.setId(id);
				    	
				    	userDAO.updateSbiUserRoles(sbiExtUserRole);

				    	logger.debug("Association beetween user [" + userId +"] and role [" + name + "] succesfully created");
			    	}
			    }
			}
		} catch (Throwable t) {
			logger.error("An unexpected error occurred during users' initialization", t);
			throw new SpagoBIRuntimeException("An unexpected error occurred during users' initialization", t);
		}
		logger.debug("OUT");

	}
	
	private Integer findRoleId(List<Role> rolesList, String name,
			String organization) {
		for (Role role : rolesList) {
			if (role.getName().equalsIgnoreCase(name)
					&& role.getOrganization()
							.equals(organization)) {
				return role.getId();
			}
		}
		logger.warn("Role with name [" + name + "] and organization [" + organization + "] not found");
		return null;
	}

	private Integer findAttributeId(List<SbiAttribute> attributesList,
			String name, String organization) {
		for (SbiAttribute attribute : attributesList) {
			if (attribute.getAttributeName().equalsIgnoreCase(name)
					&& attribute.getCommonInfo().getOrganization()
							.equals(organization)) {
				return attribute.getAttributeId();
			}
		}
		logger.warn("Attribute with name [" + name + "] and organization [" + organization + "] not found");
		return null;
	}

	/**
	 * @return The map of role ids (Integer) indexed by role name (String)
	 */
	public HashMap< String, Integer> initUsers(SourceBean config) {
		HashMap< String, Integer> usersLookup;
		
		logger.debug("IN");
		
		usersLookup = new HashMap< String, Integer>();
		try {
			Assert.assertNotNull(config, "Input parameter [config] cannot be null");
			
			ISbiUserDAO userDAO = DAOFactory.getSbiUserDAO();
			//get configuration about the public user
			IConfigDAO configDAO = DAOFactory.getSbiConfigDAO();
			Config usePublicUserConf = configDAO.loadConfigParametersByLabel(SpagoBIConstants.USE_PUBLIC_USER);
			
			List<SbiUser> defaultUsers = readUsers(config);
			for(SbiUser defaultUser: defaultUsers) {
				SbiUser existingUser = userDAO.loadSbiUserByUserId( defaultUser.getUserId() );
				boolean usePublicUser = false;
				boolean insert = false;
				
				if (usePublicUserConf == null ||  (usePublicUserConf.isActive() && 	
						usePublicUserConf.getValueCheck() != null && usePublicUserConf.getValueCheck().equals("true")))
					usePublicUser = true;
			
		    	if (existingUser == null) {
		    		insert = true;
		    		if(defaultUser.getUserId().equalsIgnoreCase(SpagoBIConstants.PUBLIC_USER_ID) && !usePublicUser) insert = false;
		    		if (insert){
			    		String userId = defaultUser.getUserId(); // save this because the dao during save set it to id
			    		logger.debug("Storing user [" + defaultUser.getUserId() + "] into database ");
			    		Integer newId = userDAO.saveSbiUser(defaultUser);
			    		usersLookup.put(defaultUser.getUserId(), newId);
			    		logger.debug("User [" + defaultUser.getUserId() + "] sucesfully stored into database with id [" + newId + "]");
		    		}
			    } 
			}			  
		} catch(Throwable t) {
			logger.error("An unexpected error occurred while initializieng default users", t);
		} finally {
			logger.debug("OUT");
		}
		
		return usersLookup;
	}
	
	public List<SbiUser> readUsers(SourceBean config) {
		List<SbiUser> defaultUsers;
		
		logger.debug("IN");
			
		defaultUsers = new ArrayList<SbiUser>();
		
		try {
			Assert.assertNotNull(config, "Input parameter [config] cannot be null");
			
			List<SourceBean> defaultsUsersSB = _config.getAttributeAsList("DEFAULT_USERS.USER");
			logger.debug("Succesfully read from configuration [" + defaultsUsersSB.size() + "] defualt user(s)");
			
			
			for (SourceBean defaultUserSB : defaultsUsersSB) {
			  
			    SbiUser defaultUser = new SbiUser();
			   
			    String userId = (String) defaultUserSB.getAttribute("userId");
			    defaultUser.setUserId(userId);
			   
			    
			    String password = (String) defaultUserSB.getAttribute("password");
				if (password != null){
				    try {
				    	String pwd = Password.encriptPassword(password);
				    	defaultUser.setPassword(pwd);
					} catch (Exception e) {
						logger.error("Impossible to encript Password", e);
					}
				}
				
			    String fullName = (String) defaultUserSB.getAttribute("fullName");
			    if(fullName != null){
			    	defaultUser.setFullName(fullName);
			    }
			    
			    String organization = (String) defaultUserSB.getAttribute("organization");
			    if (organization == null) {
			    	throw new SpagoBIRuntimeException("Predefined user [" + userId + "] has no organization set.");
			    }
			    //superadmin management for multi-tenency
			    defaultUser.setIsSuperadmin(false);
			    String isSuperadmin = (String) defaultUserSB.getAttribute("isSuperadmin");
			    if (organization != null) {
			    	Boolean isSuperadm= new Boolean(isSuperadmin);
			    	defaultUser.setIsSuperadmin(isSuperadm);
			    }
			    
			    defaultUser.getCommonInfo().setOrganization(organization);
			    
			    defaultUsers.add(defaultUser);
			    
			    logger.debug("Succesfully parsed from configuration user [" + userId  + ";" + fullName + "]");
			}
			
			
		} catch(Throwable t) {
			logger.error("An unexpected error occurred while reading defualt users", t);
		} finally {
			logger.debug("OUT");
		}
		
		return defaultUsers;
	}
	
	/**
	 * @return The list of roles
	 */
	public List<Role> initRoles(SourceBean config) {
		List<Role> rolesList;
		IRoleDAO roleDAO;
		List<Role> defualtRoles;
		
		logger.debug("IN");
		
		rolesList = new ArrayList<Role>();
		try {
			Assert.assertNotNull(config, "Input parameter [config] cannot be null");
			
			defualtRoles = readDefaultRoles(config);
		  
			for (Role defualtRole : defualtRoles) {
				
				roleDAO = DAOFactory.getRoleDAO();
				roleDAO.setUserID("server_init");
				roleDAO.setTenant(defualtRole.getOrganization());
				
				Role existingRole = roleDAO.loadByName(
						defualtRole.getName());
				if (existingRole == null) {
					logger.debug("Storing role [" + defualtRole.getName()
							+ "] for organization ["
							+ defualtRole.getOrganization()
							+ "] into database ");
					roleDAO.insertRole(defualtRole);
					existingRole = roleDAO.loadByName(defualtRole.getName());
					logger.debug("Role [" + defualtRole.getName()
							+ "] for organization ["
							+ defualtRole.getOrganization()
							+ "] succesfully stored into database with id ["
							+ existingRole.getId() + "]");
				} else {
					logger.debug("Role [" + defualtRole.getName()
							+ "] for organization ["
							+ defualtRole.getOrganization()
							+ "] is alerdy stored into database with id ["
							+ existingRole.getId() + "]");
				}

				rolesList.add(existingRole);
			}
		
		} catch(Throwable t) {
			logger.error("An unexpected error occurred while initializieng default roles", t);
		} finally {
			logger.debug("OUT");
		}
		
		return rolesList;
	}
	
	
	
	
	public List<Role> readDefaultRoles(SourceBean config) {
		List<Role> defaultRoles;
		List<SourceBean> defaultRolesSB;
		
		logger.debug("IN");
		
		defaultRoles = new ArrayList<Role>();
		try {
			Assert.assertNotNull(config, "Input parameter [config] cannot be null");
			defaultRolesSB = config.getAttributeAsList("DEFAULT_ROLES.ROLE");
		
			logger.debug("Succesfully read from configuration [" + defaultRolesSB.size() + "] defualt role(s)");
			
			IDomainDAO domainDAO = DAOFactory.getDomainDAO();
		    List<Domain> domains =domainDAO.loadListDomainsByType("ROLE_TYPE");
		    HashMap<String, Integer> domainIds = new HashMap<String, Integer> ();
		    for(int i=0; i< domains.size(); i++){
		    	domainIds.put(domains.get(i).getValueCd(), domains.get(i).getValueId());
		    }
		    
			for (SourceBean defaultRoleSB : defaultRolesSB) {
				Role sbiRole = new Role();
			    
				String roleName = (String) defaultRoleSB.getAttribute("roleName");
				sbiRole.setName(roleName);
				
				String roleDescr = (String) defaultRoleSB.getAttribute("description");
				sbiRole.setDescription(roleDescr);
				    
				String roleTypeCD = (String) defaultRoleSB.getAttribute("roleTypeCD");
				sbiRole.setRoleTypeCD(roleTypeCD);
				    
				Integer valueId = domainIds.get(roleTypeCD);
				if(valueId != null){
					sbiRole.setRoleTypeID(valueId);
				}
				
			    String organization = (String) defaultRoleSB.getAttribute("organization");
			    if (organization == null) {
			    	throw new SpagoBIRuntimeException("Predefined role [" + roleName + "] has no organization set.");
			    }
			    sbiRole.setOrganization(organization);
				
				defaultRoles.add(sbiRole);
				
			    logger.debug("Succesfully parsed from configuration profile attribute [" + roleName  + ";" + roleDescr + ";" + roleTypeCD + "]");
			}
		} catch(Throwable t) {
			logger.error("An unexpected error occurred while reading defualt profile attibutes", t);
		} finally {
			logger.debug("OUT");
		}
		
		return defaultRoles;
	}
	
	/**
	 * @return The list of attributes
	 */
	private List<SbiAttribute> initProfileAttributes(SourceBean config) {
		
		List<SbiAttribute> attributesList;
		ISbiAttributeDAO profileAttributeDAO;
		
		logger.debug("IN");
		
		attributesList = new ArrayList<SbiAttribute>();
		try {
			Assert.assertNotNull(config, "Input parameter [config] cannot be null");
			
			List<SbiAttribute> defaultProfileAttributes = readDefaultProfileAttributes(config);
			
			for (SbiAttribute defaultProfileAttribute : defaultProfileAttributes) {
				
				profileAttributeDAO = DAOFactory.getSbiAttributeDAO();
				profileAttributeDAO.setUserID("server_init");
				profileAttributeDAO.setTenant(defaultProfileAttribute.getCommonInfo()
						.getOrganization());
				
				SbiAttribute existingAttribute = profileAttributeDAO
						.loadSbiAttributeByName(defaultProfileAttribute.getAttributeName());
				if (existingAttribute == null) {
					logger.debug("Storing attribute ["
							+ defaultProfileAttribute.getAttributeName()
							+ "] for organization ["
							+ defaultProfileAttribute.getCommonInfo()
									.getOrganization() + "] into database ");
					try {
						Integer id = profileAttributeDAO
								.saveSbiAttribute(defaultProfileAttribute);
						defaultProfileAttribute.setAttributeId(id);
						attributesList.add(defaultProfileAttribute);
						logger.debug("Attribute ["
								+ defaultProfileAttribute.getAttributeName()
								+ "] for organization ["
								+ defaultProfileAttribute.getCommonInfo()
										.getOrganization()
								+ "] succesfully stored into database with id equals to ["
								+ id + "]");
					} catch (EMFUserError e) {
						logger.error(e.getMessage(), e);
						throw new SpagoBIRuntimeException("Error while storing users' attribute", e);
					}
				} else {
					attributesList.add(existingAttribute);
					logger.debug("Attribute ["
							+ existingAttribute.getAttributeName()
							+ "] is already stored into the database with id equals to ["
							+ existingAttribute.getAttributeId() + "]");
				}
			}
		} catch(Throwable t) {
			logger.error("An unexpected error occurred while initializing profile attibutes", t);
		} finally {
			logger.debug("OUT");
		}
		
		return attributesList;
	}
	
	public List<SbiAttribute> readDefaultProfileAttributes(SourceBean config) {
		List<SbiAttribute> defaultProfileAttributes;
		List<SourceBean> defaultProfileAttributesSB;
		
		logger.debug("IN");
		
		defaultProfileAttributes = new ArrayList<SbiAttribute>();
		try {
			Assert.assertNotNull(config, "Input parameter [config] cannot be null");
			defaultProfileAttributesSB = config.getAttributeAsList("DEFAULT_ATTRIBUTES.ATTRIBUTE");
			
			logger.debug("Succesfully read from configuration [" + defaultProfileAttributesSB.size() + "] defualt profile attribute(s)");
			
			for (SourceBean defaultProfileAttributeSB : defaultProfileAttributesSB) {
			    SbiAttribute sbiAttribute = new SbiAttribute();
			    String attributeName = (String)defaultProfileAttributeSB.getAttribute("name");
			    String attributeDescription = (String) defaultProfileAttributeSB.getAttribute("description");
			    String organization = (String) defaultProfileAttributeSB.getAttribute("organization");
			    if (organization == null) {
			    	throw new SpagoBIRuntimeException("Predefined attribute [" + attributeName + "] has no organization set.");
			    }
			    sbiAttribute.setAttributeName(attributeName);			    
			    sbiAttribute.setDescription(attributeDescription);
			    sbiAttribute.getCommonInfo().setOrganization(organization);
			    defaultProfileAttributes.add(sbiAttribute);
			    
			    logger.debug("Succesfully parsed from configuration profile attribute [" + attributeName  + ";" + attributeDescription + "]");
			}
		} catch(Throwable t) {
			logger.error("An unexpected error occurred while reading defualt profile attibutes", t);
		} finally {
			logger.debug("OUT");
		}
		
		return defaultProfileAttributes;
	}

	
	public void initDefaultAuthorizations(SourceBean config) {
		List<SourceBean> defaultAuthorizationsSB;
		
		logger.debug("IN");
		try {
			Assert.assertNotNull(config, "Input parameter [config] cannot be null");
			defaultAuthorizationsSB = config.getAttributeAsList("DEFAULT_AUTHORIZATIONS.AUTHORIZATION");

			logger.debug("Succesfully read from configuration [" + defaultAuthorizationsSB.size() + "] defualt authorization(s)");

			List<SbiAuthorizations> authorizations = DAOFactory.getRoleDAO().loadAllAuthorizations();


			if(authorizations == null || authorizations.isEmpty()){
				logger.debug("Initializer inserts default authorization");
				for (SourceBean defaultAuthSB : defaultAuthorizationsSB) {
					String authName = (String) defaultAuthSB.getAttribute("authorizationName");	
					logger.debug("insert "+authName);

					String organization = (String) defaultAuthSB.getAttribute("organization");
					if (organization == null) {
						throw new SpagoBIRuntimeException("Predefined authorization [" + authName + "] has no organization set.");
					}

					if(!authorizations.contains(authName)){
						DAOFactory.getRoleDAO().insertAuthorization(authName, organization);
						logger.debug("Succesfully inserted authorization [" + authName  + "]");		
					}
					else{
						logger.debug("Not inserted authorization [" + authName  + "] because already present.");		
						
					}
				}
			}
			
		} catch(Throwable t) {
			logger.error("An unexpected error occurred while reading defualt profile attibutes", t);
		} finally {
			logger.debug("OUT");
		}
		
		return;
	}
	
}
