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
package it.eng.spagobi.security.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.initializers.metadata.SpagoBIInitializer;
import it.eng.spagobi.commons.metadata.SbiAuthorizations;
import it.eng.spagobi.commons.metadata.SbiAuthorizationsRoles;
import it.eng.spagobi.commons.metadata.SbiAuthorizationsRolesId;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiProductType;
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

public class InternalSecurityInitializer extends SpagoBIInitializer {

	private static String INTERNAL_SECURITY_CONFIG_TAG_NAME = "INTERNAL_PROFILING_INITIALIZER";

	static private Logger logger = Logger.getLogger(InternalSecurityInitializer.class);

	public InternalSecurityInitializer() {
		targetComponentName = "InternalSecurity";
	}

	@Override
	public void init(SourceBean config, Session hibernateSession) {

		logger.debug("IN");

		try {
			config = getConfiguration();

			if (config == null) {
				logger.error("Security initialization aborted because the input parameter [config] is null");
				return;
			}

			ISbiUserDAO userDAO = DAOFactory.getSbiUserDAO();

			if (!userDAO.thereIsAnyUsers()) {
				List<SbiAttribute> attributesList = initProfileAttributes(config);
				List<Role> rolesList = initRoles(config);
				initExtRolesCategory(config);
				Map<String, Integer> usersLookupMap = initUsers(config);
				initDefaultAuthorizations(config);
				initDefaultAuthorizationsRoles(config);

				// finally default users associations
				List<SourceBean> defaultsUsers = config.getAttributeAsList("DEFAULT_USERS.USER");
				for (SourceBean defaultUser : defaultsUsers) {

					String userId = (String) defaultUser.getAttribute("userId");
					String organization = (String) defaultUser.getAttribute("organization");
					userDAO.setTenant(organization);
					List<SourceBean> attributes = defaultUser.getAttributeAsList("ATTRIBUTE");
					if (attributes != null) {
						for (int i = 0; i < attributes.size(); i++) {
							SourceBean attribute = attributes.get(i);
							String name = (String) attribute.getAttribute("name");
							String value = (String) attribute.getAttribute("value");
							logger.debug("Setting attribute [" + name + "] of user [" + userId + "] to value [" + value + "]");
							if (usersLookupMap.get(userId) == null) {
								logger.debug(
										"User [" + userId + "] was already stored in the database. The value of attribute [" + name + "] will not be overwritten");
								continue;
							}

							SbiUserAttributes sbiUserAttr = new SbiUserAttributes();
							sbiUserAttr.setAttributeValue(value);

							Integer attrID = findAttributeId(attributesList, name, organization);

							SbiUserAttributesId sbiUserAttrID = new SbiUserAttributesId();
							sbiUserAttrID.setId(usersLookupMap.get(userId));// user ID
							sbiUserAttrID.setAttributeId(attrID.intValue());
							sbiUserAttr.setId(sbiUserAttrID);

							userDAO.updateSbiUserAttributes(sbiUserAttr);

							logger.debug("Attribute [" + name + "] of user [" + userId + "] succesfully set to value [" + value + "]");
						}
					}

					List<SourceBean> userroles = defaultUser.getAttributeAsList("ROLE");
					if (userroles != null) {
						for (int i = 0; i < userroles.size(); i++) {
							SourceBean role = userroles.get(i);
							String name = (String) role.getAttribute("name");
							logger.debug("Creating association beetween user [" + userId + "] and role [" + name + "]");
							if (usersLookupMap.get(userId) == null) {
								logger.debug(
										"User [" + userId + "] was already stored in the database. The associatino with role [" + name + "] will not be created");
								continue;
							}

							SbiExtUserRoles sbiExtUserRole = new SbiExtUserRoles();
							SbiExtUserRolesId id = new SbiExtUserRolesId();

							Integer extRoleId = findRoleId(rolesList, name, organization);

							int userIdInt = usersLookupMap.get(userId).intValue();
							id.setExtRoleId(extRoleId);// role Id
							id.setId(userIdInt);// user ID
							sbiExtUserRole.getCommonInfo().setOrganization(organization);
							;
							sbiExtUserRole.setId(id);

							userDAO.updateSbiUserRoles(sbiExtUserRole);

							logger.debug("Association beetween user [" + userId + "] and role [" + name + "] succesfully created");
						}
					}
				}
			}
		} catch (Throwable t) {
			logger.error("An unexpected error occurred during users' initialization", t);
			throw new SpagoBIRuntimeException("An unexpected error occurred during users' initialization", t);
		}
		logger.debug("OUT");
	}

	@Override
	protected SourceBean getConfiguration() throws Exception {
		SourceBean config = (SourceBean) ConfigSingleton.getInstance().getAttribute(INTERNAL_SECURITY_CONFIG_TAG_NAME);
		if (config == null) {
			throw new Exception("Internal security configuration not found!!!");
		}
		return config;
	}

	private Integer findRoleId(List<Role> rolesList, String name, String organization) {
		for (Role role : rolesList) {
			if (role.getName().equalsIgnoreCase(name) && role.getOrganization().equals(organization)) {
				return role.getId();
			}
		}
		logger.warn("Role with name [" + name + "] and organization [" + organization + "] not found");
		return null;
	}

	private Integer findAttributeId(List<SbiAttribute> attributesList, String name, String organization) {
		for (SbiAttribute attribute : attributesList) {
			if (attribute.getAttributeName().equalsIgnoreCase(name) && attribute.getCommonInfo().getOrganization().equals(organization)) {
				return attribute.getAttributeId();
			}
		}
		logger.warn("Attribute with name [" + name + "] and organization [" + organization + "] not found");
		return null;
	}

	/**
	 * @return The map of role ids (Integer) indexed by role name (String)
	 */
	public HashMap<String, Integer> initUsers(SourceBean config) {
		HashMap<String, Integer> usersLookup;

		logger.debug("IN");

		usersLookup = new HashMap<String, Integer>();
		try {
			Assert.assertNotNull(config, "Input parameter [config] cannot be null");

			ISbiUserDAO userDAO = DAOFactory.getSbiUserDAO();
			// get configuration about the public user
			IConfigDAO configDAO = DAOFactory.getSbiConfigDAO();
			Config usePublicUserConf = configDAO.loadConfigParametersByLabel(SpagoBIConstants.USE_PUBLIC_USER);

			List<SbiUser> defaultUsers = readUsers(config);
			for (SbiUser defaultUser : defaultUsers) {
				SbiUser existingUser = userDAO.loadSbiUserByUserId(defaultUser.getUserId());
				boolean usePublicUser = false;
				boolean insert = false;

				if (usePublicUserConf == null
						|| (usePublicUserConf.isActive() && usePublicUserConf.getValueCheck() != null && usePublicUserConf.getValueCheck().equals("true")))
					usePublicUser = true;

				if (existingUser == null) {
					insert = true;
					if (defaultUser.getUserId().equalsIgnoreCase(SpagoBIConstants.PUBLIC_USER_ID) && !usePublicUser)
						insert = false;
					if (insert) {
						String userId = defaultUser.getUserId(); // save this because the dao during save set it to id
						logger.debug("Storing user [" + defaultUser.getUserId() + "] into database ");
						Integer newId = userDAO.saveSbiUser(defaultUser);
						usersLookup.put(defaultUser.getUserId(), newId);
						logger.debug("User [" + defaultUser.getUserId() + "] sucesfully stored into database with id [" + newId + "]");
					}
				}
			}
		} catch (Throwable t) {
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

			List<SourceBean> defaultsUsersSB = config.getAttributeAsList("DEFAULT_USERS.USER");
			logger.debug("Succesfully read from configuration [" + defaultsUsersSB.size() + "] defualt user(s)");

			for (SourceBean defaultUserSB : defaultsUsersSB) {

				SbiUser defaultUser = new SbiUser();

				String userId = (String) defaultUserSB.getAttribute("userId");
				defaultUser.setUserId(userId);

				String password = (String) defaultUserSB.getAttribute("password");
				if (password != null) {
					try {
						String pwd = Password.encriptPassword(password);
						defaultUser.setPassword(pwd);
					} catch (Exception e) {
						logger.error("Impossible to encript Password", e);
					}
				}

				String fullName = (String) defaultUserSB.getAttribute("fullName");
				if (fullName != null) {
					defaultUser.setFullName(fullName);
				}

				String organization = (String) defaultUserSB.getAttribute("organization");
				if (organization == null) {
					throw new SpagoBIRuntimeException("Predefined user [" + userId + "] has no organization set.");
				}
				// superadmin management for multi-tenency
				defaultUser.setIsSuperadmin(false);
				String isSuperadmin = (String) defaultUserSB.getAttribute("isSuperadmin");
				if (organization != null) {
					Boolean isSuperadm = new Boolean(isSuperadmin);
					defaultUser.setIsSuperadmin(isSuperadm);
				}

				defaultUser.getCommonInfo().setOrganization(organization);

				defaultUsers.add(defaultUser);

				logger.debug("Succesfully parsed from configuration user [" + userId + ";" + fullName + "]");
			}

		} catch (Throwable t) {
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

				Role existingRole = roleDAO.loadByName(defualtRole.getName());
				if (existingRole == null) {
					logger.debug("Storing role [" + defualtRole.getName() + "] for organization [" + defualtRole.getOrganization() + "] into database ");
					roleDAO.insertRole(defualtRole);
					existingRole = roleDAO.loadByName(defualtRole.getName());
					logger.debug("Role [" + defualtRole.getName() + "] for organization [" + defualtRole.getOrganization()
							+ "] succesfully stored into database with id [" + existingRole.getId() + "]");
				} else {
					logger.debug("Role [" + defualtRole.getName() + "] for organization [" + defualtRole.getOrganization()
							+ "] is alerdy stored into database with id [" + existingRole.getId() + "]");
				}

				rolesList.add(existingRole);
			}

		} catch (Throwable t) {
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
			List<Domain> domains = domainDAO.loadListDomainsByType("ROLE_TYPE");
			HashMap<String, Integer> domainIds = new HashMap<String, Integer>();
			for (int i = 0; i < domains.size(); i++) {
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
				if (valueId != null) {
					sbiRole.setRoleTypeID(valueId);
				}

				String organization = (String) defaultRoleSB.getAttribute("organization");
				if (organization == null) {
					throw new SpagoBIRuntimeException("Predefined role [" + roleName + "] has no organization set.");
				}
				sbiRole.setOrganization(organization);

				defaultRoles.add(sbiRole);

				logger.debug("Succesfully parsed from configuration profile attribute [" + roleName + ";" + roleDescr + ";" + roleTypeCD + "]");
			}
		} catch (Throwable t) {
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
				profileAttributeDAO.setTenant(defaultProfileAttribute.getCommonInfo().getOrganization());

				SbiAttribute existingAttribute = profileAttributeDAO.loadSbiAttributeByName(defaultProfileAttribute.getAttributeName());
				if (existingAttribute == null) {
					logger.debug("Storing attribute [" + defaultProfileAttribute.getAttributeName() + "] for organization ["
							+ defaultProfileAttribute.getCommonInfo().getOrganization() + "] into database ");
					try {
						Integer id = profileAttributeDAO.saveSbiAttribute(defaultProfileAttribute);
						defaultProfileAttribute.setAttributeId(id);
						attributesList.add(defaultProfileAttribute);
						logger.debug("Attribute [" + defaultProfileAttribute.getAttributeName() + "] for organization ["
								+ defaultProfileAttribute.getCommonInfo().getOrganization() + "] succesfully stored into database with id equals to [" + id
								+ "]");
					} catch (EMFUserError e) {
						logger.error(e.getMessage(), e);
						throw new SpagoBIRuntimeException("Error while storing users' attribute", e);
					}
				} else {
					attributesList.add(existingAttribute);
					logger.debug("Attribute [" + existingAttribute.getAttributeName() + "] is already stored into the database with id equals to ["
							+ existingAttribute.getAttributeId() + "]");
				}
			}
		} catch (Throwable t) {
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
				String attributeName = (String) defaultProfileAttributeSB.getAttribute("name");
				String attributeDescription = (String) defaultProfileAttributeSB.getAttribute("description");
				String organization = (String) defaultProfileAttributeSB.getAttribute("organization");
				if (organization == null) {
					throw new SpagoBIRuntimeException("Predefined attribute [" + attributeName + "] has no organization set.");
				}
				sbiAttribute.setAttributeName(attributeName);
				sbiAttribute.setDescription(attributeDescription);
				sbiAttribute.getCommonInfo().setOrganization(organization);
				defaultProfileAttributes.add(sbiAttribute);

				logger.debug("Succesfully parsed from configuration profile attribute [" + attributeName + ";" + attributeDescription + "]");
			}
		} catch (Throwable t) {
			logger.error("An unexpected error occurred while reading defualt profile attibutes", t);
		} finally {
			logger.debug("OUT");
		}

		return defaultProfileAttributes;
	}

	public void initDefaultAuthorizations(SourceBean config) {
		List<SourceBean> defaultAuthorizationsSB;
		Session aSession = null;
		logger.debug("IN");
		try {
			Assert.assertNotNull(config, "Input parameter [config] cannot be null");
			defaultAuthorizationsSB = config.getAttributeAsList("DEFAULT_AUTHORIZATIONS.AUTHORIZATION");
			logger.debug("Succesfully read from configuration [" + defaultAuthorizationsSB.size() + "] defualt authorization(s)");

			List<SbiAuthorizations> authorizations = DAOFactory.getRoleDAO().loadAllAuthorizations();

			aSession = this.getSession();
			// remove from DB the SbiAuthorizations deleted in configuration file
			for (SbiAuthorizations auth : authorizations) {
				boolean isInConfigFile = false;
				String nameInDB = auth.getName();
				String productTypeInDB = auth.getProductType().getLabel();
				Iterator it = defaultAuthorizationsSB.iterator();
				while (it.hasNext()) {
					SourceBean authSB = (SourceBean) it.next();
					String nameInFile = (String) authSB.getAttribute("authorizationName");
					String productTypeInFile = (String) authSB.getAttribute("productType");
					if (nameInFile.equals(nameInDB) && productTypeInFile.equals(productTypeInDB)) {
						isInConfigFile = true;
						break;
					}
				}
				if (!isInConfigFile) {
					deleteAuthorization(aSession, auth);
				}

			}

			// create a Set of names with AuthorizationName-ProductTypeLabel
			Set<String> authorizationsFound = new HashSet<String>();
			for (SbiAuthorizations authorization : authorizations) {
				SbiProductType sbiProductType = authorization.getProductType();
				authorizationsFound.add(authorization.getName() + "-" + sbiProductType.getLabel());
			}

			logger.debug("Initializer inserts default authorization");

			Set<String> productTypes = loadProductTypes();
			for (SourceBean defaultAuthSB : defaultAuthorizationsSB) {
				String authName = (String) defaultAuthSB.getAttribute("authorizationName");
				logger.debug("Insert " + authName);

				/*
				 * String organization = (String) defaultAuthSB.getAttribute("organization"); if (organization == null) { throw new
				 * SpagoBIRuntimeException("Predefined authorization [" + authName + "] has no organization set."); }
				 */
				String productType = (String) defaultAuthSB.getAttribute("productType");
				if (productType == null) {
					throw new SpagoBIRuntimeException("Predefined authorization [" + authName + "] has no product type set.");
				}

				if (productTypes.contains(productType)) {
					if (!authorizationsFound.contains(authName + "-" + productType)) {
						DAOFactory.getRoleDAO().insertAuthorization(authName, productType);
						logger.debug("Succesfully inserted authorization [" + authName + "] for product Type [" + productType + "]");
					} else {
						logger.debug("Not inserted authorization [" + authName + "] for product Type [" + productType + "] because already present.");

					}
				} else {
					logger.debug("Not inserted authorization [" + authName + "]. Product Type [" + productType + "] is not registered.");
				}
			}

		} catch (Throwable t) {
			logger.error("An unexpected error occurred while reading defualt profile attibutes", t);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
			logger.debug("OUT");
		}

		return;
	}

	public void initDefaultAuthorizationsRoles(SourceBean config) {
		List<SourceBean> defaultAuthorizationsRolesSB;
		Session aSession = null;
		logger.debug("IN");
		try {
			Assert.assertNotNull(config, "Input parameter [config] cannot be null");
			defaultAuthorizationsRolesSB = config.getAttributeAsList("DEFAULT_AUTHORIZATIONS_ROLES.AUTHORIZATION_ROLES");
			logger.debug("Succesfully read from configuration [" + defaultAuthorizationsRolesSB.size() + "] defualt authorization(s) roles");

			List<SbiAuthorizations> authorizations = null;
			List<SbiProductType> productTypes = null;

			aSession = this.getSession();

			Map<String, String> roleNames = new HashMap<String, String>();
			for (SourceBean defaultAuthorizationSB : defaultAuthorizationsRolesSB) {
				roleNames.put((String) defaultAuthorizationSB.getAttribute("roleName"), (String) defaultAuthorizationSB.getAttribute("organization"));
			}

			for (String roleName : roleNames.keySet()) {

				IRoleDAO roleDAO = DAOFactory.getRoleDAO();
				String organization = roleNames.get(roleName);
				roleDAO.setTenant(organization);

				SbiCommonInfo sbiCommonInfo = new SbiCommonInfo();
				sbiCommonInfo.setOrganization(organization);

				SbiExtRoles role = (SbiExtRoles) aSession.createCriteria(SbiExtRoles.class).add(Restrictions.eq("name", roleName))
						.add(Restrictions.eq("commonInfo.organization", organization)).uniqueResult();
				List<SbiAuthorizations> authorizationsAlreadyInserted = DAOFactory.getRoleDAO().LoadAuthorizationsAssociatedToRole(role.getExtRoleId());

				if (authorizationsAlreadyInserted.size() == 0) {
					List listOfAuthToInsertForRole = config.getFilteredSourceBeanAttributeAsList("DEFAULT_AUTHORIZATIONS_ROLES.AUTHORIZATION_ROLES", "roleName",
							roleName);

					for (Object defaultAuthorization : listOfAuthToInsertForRole) {
						SourceBean defaultAuthorizationSB = (SourceBean) defaultAuthorization;

						String authorizationName = (String) defaultAuthorizationSB.getAttribute("authorizationName");

						if (productTypes == null)
							productTypes = DAOFactory.getProductTypeDAO().loadAllProductType();

						for (SbiProductType productType : productTypes) {
							if (authorizations == null)
								authorizations = DAOFactory.getRoleDAO().loadAllAuthorizations();

							SbiAuthorizations sbiAuthorizations = getSbiAuthorizationToInsert(authorizations, authorizationName,
									productType.getProductTypeId());

							if (sbiAuthorizations != null) {
								SbiAuthorizationsRoles sbiAuthorizationsRoles = new SbiAuthorizationsRoles();
								sbiAuthorizationsRoles.setId(new SbiAuthorizationsRolesId(sbiAuthorizations.getId(), role.getExtRoleId()));
								sbiAuthorizationsRoles.setSbiExtRoles(role);
								sbiAuthorizationsRoles.setSbiAuthorizations(sbiAuthorizations);

								sbiAuthorizationsRoles.setCommonInfo(sbiCommonInfo);
								aSession.save(sbiAuthorizationsRoles);
								aSession.flush();
							}
						}
					}
				}
			}
		} catch (Throwable t) {
			logger.error("An unexpected error occurred while reading defualt profile attibutes", t);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
			logger.debug("OUT");
		}

	}

	private SbiAuthorizations getSbiAuthorizationToInsert(List<SbiAuthorizations> l, String name, Integer productTypeId) {
		SbiAuthorizations toReturn = null;

		for (SbiAuthorizations object : l) {
			if (object.getName().equals(name) && object.getProductType().getProductTypeId().equals(productTypeId)) {
				toReturn = object;
				break;
			}
		}

		return toReturn;
	}

	public void initExtRolesCategory(SourceBean config) {
		List<SourceBean> extRolesCategoriesSB;
		Session aSession = null;
		logger.debug("IN");
		try {
			Assert.assertNotNull(config, "Input parameter [config] cannot be null");
			extRolesCategoriesSB = config.getAttributeAsList("EXT_ROLES_CATEGORIES.EXT_ROLES_CATEGORY");
			logger.debug("Succesfully read from configuration [" + extRolesCategoriesSB.size() + "] defualt ext roles category(s) roles");
			aSession = this.getSession();

			Map<String, String> roleNames = new HashMap<String, String>();
			for (SourceBean defaultAuthorizationSB : extRolesCategoriesSB) {
				roleNames.put((String) defaultAuthorizationSB.getAttribute("roleName"), (String) defaultAuthorizationSB.getAttribute("organization"));
			}

			for (String roleName : roleNames.keySet()) {

				String organization = roleNames.get(roleName);
				IRoleDAO roleDAO = DAOFactory.getRoleDAO();
				roleDAO.setTenant(organization);
				SbiExtRoles role = (SbiExtRoles) aSession.createCriteria(SbiExtRoles.class).add(Restrictions.eq("name", roleName))
						.add(Restrictions.eq("commonInfo.organization", organization)).uniqueResult();

				if (DAOFactory.getRoleDAO().getMetaModelCategoriesForRole(role.getExtRoleId()).size() == 0) {
					for (SourceBean extRolesCategorySB : extRolesCategoriesSB) {
						SbiDomains category = (SbiDomains) aSession.createCriteria(SbiDomains.class)
								.add(Restrictions.eq("domainCd", extRolesCategorySB.getAttribute("domainCd")))
								.add(Restrictions.isNull("commonInfo.organization")).uniqueResult();

						roleDAO.insertRoleMetaModelCategory(role.getExtRoleId(), category.getValueId());
					}
				}

			}
		} catch (Throwable t) {
			logger.error("An unexpected error occurred while reading defualt profile attibutes", t);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
			logger.debug("OUT");
		}

	}

	private Set<String> loadProductTypes() {
		List<SbiProductType> sbiProductTypes = DAOFactory.getProductTypeDAO().loadAllProductType();
		if (sbiProductTypes != null && !sbiProductTypes.isEmpty()) {
			Set<String> productTypes = new HashSet<String>(sbiProductTypes.size());
			for (SbiProductType sbiProductType : sbiProductTypes) {
				productTypes.add(sbiProductType.getLabel());
			}
			return productTypes;
		} else {
			return new HashSet<String>(0);
		}
	}
}
