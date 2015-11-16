/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.security;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dbaccess.Utils;
import it.eng.spago.dbaccess.sql.DataConnection;
import it.eng.spago.dbaccess.sql.SQLCommand;
import it.eng.spago.dbaccess.sql.result.DataResult;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


/**
 * Contains methods to Syncronize the portal list of roles
 */
public class RoleSynchronizer {
	
	static private Logger logger = Logger.getLogger(RoleSynchronizer.class);
	
	/**
	 * Syncronize the portal roles with SpagoBI roles importing roles missing in SpagoBI.
	 * if a role yet exist into SpagoBI table list,
	 * a tracing message is added and the list iteration goes on; if there is a new role,
	 * it is inserted into role database and another tracing message is added.
	 */
	public void synchronize() {
		logger.debug("IN");
        try {
        	SingletonConfig conf = SingletonConfig.getInstance();
        	logger.debug("Config singleton retrived " + conf);
        	String portalSecurityProviderClass = conf.getConfigValue("SPAGOBI.SECURITY.PORTAL-SECURITY-CLASS.className");
        	portalSecurityProviderClass = portalSecurityProviderClass.trim();
        	logger.debug("Security class name retrived " + portalSecurityProviderClass);
        	Class secProvClass = Class.forName(portalSecurityProviderClass);
        	logger.debug("Security class found " + secProvClass);
        	ISecurityInfoProvider portalSecurityProvider = (ISecurityInfoProvider)secProvClass.newInstance();
        	logger.debug("Security class instance created " + portalSecurityProvider);
        	String secFilterSB = conf.getConfigValue("SPAGOBI.SECURITY.ROLE-NAME-PATTERN-FILTER");
        	logger.debug("Source bean filter retrived " + secFilterSB);
            String rolePatternFilter = secFilterSB;
            logger.debug("Filter string retrived " + rolePatternFilter);
            Pattern filterPattern = Pattern.compile(rolePatternFilter);
            logger.debug("Filter pattern regular expression " + filterPattern);
            Matcher matcher = null;
            List roles = portalSecurityProvider.getRoles();
            logger.debug("Complete list retrived " + roles);
        	Role aRole = null;
        	String roleName = null;
        	for (Iterator it = roles.iterator(); it.hasNext(); ){
        		aRole = (Role)it.next();
        		checkTenant(aRole);
        		roleName = aRole.getName();
        		logger.info("Reading role: "+roleName);
        		matcher = filterPattern.matcher(roleName);
        		if(!matcher.matches()){
        		    logger.info("The role: "+roleName+ " doesn't match");
        		    continue;
        		}
        				
        		if (exist(aRole)) {
        			logger.info(" Role [" + aRole.getName()+"] already in Database");
        		} else {
        			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
        			roleDAO.setTenant(aRole.getOrganization());
        			logger.info(" Role [" + aRole.getName()+"] must be inserted in database");
        			setRoleType(aRole);
        			roleDAO.insertRole(aRole);
        			logger.info(" Portal Role [" + aRole.getName()+"] INSERTED OK");
        		}
        	}
        } catch (EMFUserError emfue) {
        	logger.error(" Exception verified ", emfue);
		} catch(Exception ex){
			logger.error(" An exception has occurred ", ex);
		} finally {
			logger.debug("OUT");
		}
	}
	
	
	private void checkTenant(Role aRole) {
		if (aRole.getOrganization() == null) {
			logger.warn("Role [" + aRole.getName()
					+ "] has no organization/tenant set!!!");
			List<SbiTenant> tenants = DAOFactory.getTenantsDAO()
					.loadAllTenants();
			if (tenants == null || tenants.size() == 0) {
				throw new SpagoBIRuntimeException(
						"No tenants found on database");
			}
			if (tenants.size() > 1) {
				throw new SpagoBIRuntimeException(
						"Tenants are more than one, cannot associate input role ["
								+ aRole.getName() + "] to a single tenant!!!");
			}
			SbiTenant tenant = tenants.get(0);
			logger.warn("Associating role [" + aRole.getName() + "] to tenant [" + tenant.getName() +"]");
			aRole.setOrganization(tenant.getName());
		}
	}


	/**
	 * Returns true if a role already exists into the role list, false if none.
	 * If the role name is found into the roles list, the <code>
	 * loadByName</code> method called doesn't throw any exception, so true is
	 * returned.
	 * 
	 * @param pRole
	 *            The input role to control
	 * @return A boolean value telling us if the role exists or not.
	 */
	private boolean exist(Role pRole) {
		try {
			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			roleDAO.setTenant(pRole.getOrganization());
			Role role = roleDAO.loadByName(pRole.getName());
			if (role != null)
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}
    
	/**
	 * Sets the correct role type, according to the role name and the configured patterns
	 * @param aRole, the role to be modified (information about the role type will be added)
	 */
	private void setRoleType(Role aRole) {
		if (aRole == null) {
			logger.warn("Role in input is null. Returning.");
			return;
		}
		if (isRoleType(aRole, "ADMIN")) {
			logger.debug("Role with name [" + aRole.getName() + "] is ADMIN role type.");
			Integer roleTypeId = findSBIDomainValueID("ROLE_TYPE", "ADMIN");
			aRole.setRoleTypeID(roleTypeId);
			aRole.setRoleTypeCD("ADMIN");
			return;
		}
		if (isRoleType(aRole, "DEV_ROLE")) {
			logger.debug("Role with name [" + aRole.getName() + "] is DEV_ROLE role type.");
			Integer roleTypeId = findSBIDomainValueID("ROLE_TYPE", "DEV_ROLE");
			aRole.setRoleTypeID(roleTypeId);
			aRole.setRoleTypeCD("DEV_ROLE");
			return;
		}		
		if (isRoleType(aRole, "TEST_ROLE")) {
			logger.debug("Role with name [" + aRole.getName() + "] is TEST_ROLE role type.");
			Integer roleTypeId = findSBIDomainValueID("ROLE_TYPE", "TEST_ROLE");
			aRole.setRoleTypeID(roleTypeId);
			aRole.setRoleTypeCD("TEST_ROLE");
			return;
		}
		if (isRoleType(aRole, "MODEL_ADMIN")) {
			logger.debug("Role with name [" + aRole.getName() + "] is MODEL_ADMIN role type.");
			Integer roleTypeId = findSBIDomainValueID("ROLE_TYPE", "MODEL_ADMIN");
			aRole.setRoleTypeID(roleTypeId);
			aRole.setRoleTypeCD("MODEL_ADMIN");
			return;
		}
		
		// Role is not ADMIN/DEV_ROLE/TEST_ROLE/MODEL_ADMIN, default is USER
		Integer roleTypeId = findSBIDomainValueID("ROLE_TYPE", "USER");
		aRole.setRoleTypeID(roleTypeId);
		aRole.setRoleTypeCD("USER");
	}
	
	private boolean isRoleType(Role aRole, String roleTypeCd) {
		String roleName = aRole.getName();
		SingletonConfig conf = SingletonConfig.getInstance();
		String adminRolePatternSB = conf.getConfigValue("SPAGOBI.SECURITY.ROLE-TYPE-PATTERNS." + roleTypeCd + "-PATTERN");
		if (adminRolePatternSB != null) {
			String adminPatternStr = adminRolePatternSB;
			Pattern adminPattern = Pattern.compile(adminPatternStr);
			Matcher matcher = adminPattern.matcher(roleName);
    		if (matcher.matches()) {
    			return true;
    		}
		}
		return false;
	}
	
	
	/**
	 * Gets the id for a Domain, given its code and value 
	 * @param domainCode	The Domain code String
	 * @param valueCode	The domain Value Dtring
	 * @return	The Domain ID 
	 */
    private Integer findSBIDomainValueID(String domainCode, String valueCode ){
    	SQLCommand cmd = null;
		DataResult dr = null;
		DataConnection dataConnection = null;
		Integer returnValue = null;
		try {
			IDomainDAO domdao = DAOFactory.getDomainDAO();
			Domain dom = domdao.loadDomainByCodeAndValue(domainCode, valueCode);
			returnValue = dom.getValueId();
		}  catch (Exception ex) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE,this.getClass().toString(), 
			"findSBIDomainValueID", " An exception has occurred ", ex);
		} finally {
			Utils.releaseResources(dataConnection, cmd, dr);
		}
		return returnValue;
    }
}
