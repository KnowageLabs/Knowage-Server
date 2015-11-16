/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.initializers.metadata;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnitGrant;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class UnitGrantInitializer extends SpagoBIInitializer {

	static private Logger logger = Logger.getLogger(UnitGrantInitializer.class);

	public UnitGrantInitializer() {
		targetComponentName = "Unit Grant";
	}
	
	
	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
			//resets grants availability to true if grants are present
			String hql = "from SbiOrgUnitGrant g where g.isAvailable != true ";
			Query hqlQuery = hibernateSession.createQuery(hql);
			List grants = hqlQuery.list();
			if (grants.isEmpty()) {
				logger.debug("Nothing to reset...");
			} else {
				logger.debug("Start resetting availability...");
				resetGrantsAvailable(hibernateSession, grants);
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while resetting organizational units' grants", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	private void resetGrantsAvailable(Session aSession, List grants)
			throws Exception {
		logger.debug("IN");
		for (int i = 0; i < grants.size(); i++) {
			SbiOrgUnitGrant grant = (SbiOrgUnitGrant) grants.get(i);
			grant.setIsAvailable(true);
			aSession.save(grant);
			aSession.flush();
		}
		logger.debug("OUT");
	}

}
