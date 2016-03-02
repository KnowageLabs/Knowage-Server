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
package it.eng.spagobi.sdk.behavioural.impl;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.sdk.AbstractSDKService;
import it.eng.spagobi.sdk.behavioural.BehaviouralService;
import it.eng.spagobi.sdk.behavioural.bo.SDKAttribute;
import it.eng.spagobi.sdk.behavioural.bo.SDKRole;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class BehaviouralServiceImpl extends AbstractSDKService implements BehaviouralService {

	static private Logger logger = Logger.getLogger(BehaviouralServiceImpl.class);

	public SDKAttribute[] getAllAttributes(String roleName) throws NotAllowedOperationException {

		logger.debug("IN: roleName = [" + roleName + "]");
		SDKAttribute[] toReturn = null;

		this.setTenant();

		try {
			IEngUserProfile profile = getUserProfile();

			List<SbiAttribute> attributes = DAOFactory.getSbiAttributeDAO().loadSbiAttributes();

			List attsList = new ArrayList();

			if (attributes != null) {
				Iterator it = attributes.iterator();
				while (it.hasNext()) {
					SbiAttribute attribute = (SbiAttribute) it.next();
					SDKAttribute sdkAttribute = new SDKAttribute();
					sdkAttribute.setId(attribute.getAttributeId());
					sdkAttribute.setName(attribute.getAttributeName());
					sdkAttribute.setDescription(attribute.getDescription());
					attsList.add(sdkAttribute);
				}
			}
			toReturn = new SDKAttribute[attsList.size()];
			toReturn = (SDKAttribute[]) attsList.toArray(toReturn);
		} catch (NotAllowedOperationException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}

	public SDKRole[] getRoles() throws NotAllowedOperationException {
		logger.debug("IN");
		SDKRole[] toReturn = null;

		this.setTenant();

		try {
			IEngUserProfile profile = getUserProfile();

			List roles = DAOFactory.getRoleDAO().loadAllRoles();

			List sdkRoles = new ArrayList();

			if (roles != null) {
				Iterator it = roles.iterator();
				while (it.hasNext()) {
					Role role = (Role) it.next();
					SDKRole sdkRole = new SDKRole();
					sdkRole.setCode(role.getCode());
					sdkRole.setDescr(role.getDescription());
					sdkRole.setExtRoleId(role.getId());
					sdkRole.setName(role.getName());
					sdkRole.setOrganization(role.getOrganization());
					sdkRole.setRoleTypeCd(role.getRoleTypeCD());
					sdkRole.setRoleTypeId(role.getRoleTypeID());
					sdkRoles.add(sdkRole);
				}
			}
			toReturn = new SDKRole[sdkRoles.size()];
			toReturn = (SDKRole[]) sdkRoles.toArray(toReturn);
		} catch (NotAllowedOperationException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}

	public SDKRole[] getRolesByUserId(String userId) throws NotAllowedOperationException {
		logger.debug("IN");
		SDKRole[] toReturn = null;

		this.setTenant();

		try {
			IEngUserProfile profile = getUserProfile();

			SbiUser user = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(userId);
			List roles = DAOFactory.getSbiUserDAO().loadSbiUserRolesById(user.getId());

			List sdkRoles = new ArrayList();

			if (roles != null) {
				Iterator it = roles.iterator();
				while (it.hasNext()) {
					Role role = (Role) it.next();
					SDKRole sdkRole = new SDKRole();
					sdkRole.setCode(role.getCode());
					sdkRole.setDescr(role.getDescription());
					sdkRole.setExtRoleId(role.getId());
					sdkRole.setName(role.getName());
					sdkRole.setOrganization(role.getOrganization());
					sdkRole.setRoleTypeCd(role.getRoleTypeCD());
					sdkRole.setRoleTypeId(role.getRoleTypeID());
					sdkRoles.add(sdkRole);
				}
			}
			toReturn = new SDKRole[sdkRoles.size()];
			toReturn = (SDKRole[]) sdkRoles.toArray(toReturn);
		} catch (NotAllowedOperationException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}
}
