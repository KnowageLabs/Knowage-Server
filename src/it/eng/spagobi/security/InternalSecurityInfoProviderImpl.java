/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
