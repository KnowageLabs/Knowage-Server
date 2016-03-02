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
