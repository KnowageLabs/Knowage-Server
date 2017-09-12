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
package it.eng.spagobi.security;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.bean.SbiAttribute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class InternalSecurityInfoProviderImpl implements ISecurityInfoProvider{

	static private Logger logger = Logger.getLogger(InternalSecurityInfoProviderImpl.class);
	
	public List getAllProfileAttributesNames() {
    	logger.debug("IN");
		List attributes = new ArrayList();
		//gets attributes from database
		try {
			List<SbiAttribute> sbiAttributes = DAOFactory.getSbiAttributeDAO().loadSbiAttributes();
			Iterator it = sbiAttributes.iterator();
			while(it.hasNext()) {
				SbiAttribute attribute = (SbiAttribute)it.next();

				attributes.add(attribute.getAttributeName());
			}
		} catch (EMFUserError e) {
			logger.error(e.getMessage(), e);
		}

		logger.debug("OUT");
		return attributes;
	}

	public List getRoles() {
    	logger.debug("IN");
    	//get roles from database
		List roles = new ArrayList();

		//gets roles from database
		try {
			roles = DAOFactory.getRoleDAO().loadAllRoles();

		} catch (EMFUserError e) {
			logger.error(e.getMessage(), e);
		}
		logger.debug("OUT");
		return roles;
	}

}
