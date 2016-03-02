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
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.kpi.config.metadata.SbiKpiPeriodicity;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class KpiPeriodicityInitializer extends SpagoBIInitializer {

	static private Logger logger = Logger.getLogger(KpiPeriodicityInitializer.class);

	public KpiPeriodicityInitializer() {
		targetComponentName = "Kpi Periodicity";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/kpi.xml";
	}

	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
			String hql = "from SbiTenant";
			Query hqlQuery = hibernateSession.createQuery(hql);
			List<SbiTenant> tenants = hqlQuery.list();
			for (SbiTenant tenant : tenants) {
				init(config, hibernateSession, tenant);
			}
		} finally {
			logger.debug("OUT");
		}
	}
	
	public void init(SourceBean config, Session hibernateSession, SbiTenant tenant) {
		logger.debug("IN");
		try {
			String hql = "from SbiKpiPeriodicity p where p.commonInfo.organization = :organization";
			Query hqlQuery = hibernateSession.createQuery(hql);
			hqlQuery.setString("organization", tenant.getName());
			List periodicities = hqlQuery.list();
			if (periodicities.isEmpty()) {
				logger.info("No predefined periodicities for tenant " + tenant.getName() + ". Starting populating predefined periodicities...");
				writePeriodicities(hibernateSession, tenant);
			} else {
				logger.debug("Predefined periodicities table is already populated for tenant " + tenant.getName());
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Ab unexpected error occured while initializeng Kpi Periodicity", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	private void writePeriodicities(Session aSession, SbiTenant tenant) throws Exception {
		logger.debug("IN");
		SourceBean kpiSB = getConfiguration();
		if (kpiSB == null) {
			throw new Exception("Kpis configuration file not found!!!");
		}
		List periodicitiesList = kpiSB.getAttributeAsList("PERIODICITY");
		if (periodicitiesList == null || periodicitiesList.isEmpty()) {
			throw new Exception("No predefined periodicities found!!!");
		}
		Iterator it = periodicitiesList.iterator();
		while (it.hasNext()) {
			SourceBean aPeriodicitySB = (SourceBean) it.next();
			SbiKpiPeriodicity periodicity = new SbiKpiPeriodicity();
			periodicity.setName((String) aPeriodicitySB.getAttribute("name"));
			periodicity.setMonths(new Integer((String) aPeriodicitySB.getAttribute("months")));
			periodicity.setDays(new Integer((String) aPeriodicitySB.getAttribute("days")));
			periodicity.setHours(new Integer((String) aPeriodicitySB.getAttribute("hours")));
			periodicity.setMinutes(new Integer((String) aPeriodicitySB.getAttribute("minutes")));
			
			// setting tenant/organization info
			periodicity.getCommonInfo().setOrganization(tenant.getName());
			
			logger.debug("Inserting Periodicity with name = [" + aPeriodicitySB.getAttribute("name") + "]");
			aSession.save(periodicity);
		}
		logger.debug("OUT");
	}

}
