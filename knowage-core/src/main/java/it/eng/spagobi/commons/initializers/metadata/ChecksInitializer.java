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

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.base.SourceBean;
import it.eng.spago.init.InitializerIFace;
import it.eng.spagobi.behaviouralmodel.check.metadata.SbiChecks;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ChecksInitializer extends SpagoBIInitializer {

	private static Logger logger = Logger.getLogger(ChecksInitializer.class);

	public ChecksInitializer() {
		targetComponentName = "Checks";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/checks.xml";
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
			String hql = "from SbiChecks c where c.commonInfo.organization = :organization";
			Query hqlQuery = hibernateSession.createQuery(hql);
			hqlQuery.setString("organization", tenant.getName());
			List checks = hqlQuery.list();
			if (checks.isEmpty()) {
				logger.info("No checks for tenant " + tenant.getName() + ". Starting populating predefined checks...");
				writeChecks(hibernateSession, tenant);
			} else {
				logger.debug("Checks table is already populated for tenant " + tenant.getName());
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Ab unexpected error occured while initializeng Checks", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	private void writeChecks(Session aSession, SbiTenant tenant) throws Exception {
		logger.debug("IN");
		SourceBean checksSB = getConfiguration();
		if (checksSB == null) {
			logger.info("Configuration file for predefined checks not found");
			return;
		}
		List checksList = checksSB.getAttributeAsList("CHECK");
		if (checksList == null || checksList.isEmpty()) {
			logger.info("No predefined checks available from configuration file");
			return;
		}
		Iterator it = checksList.iterator();
		while (it.hasNext()) {
			SourceBean aChecksSB = (SourceBean) it.next();
			SbiChecks aCheck = new SbiChecks();
			aCheck.setLabel((String) aChecksSB.getAttribute("label"));
			aCheck.setName((String) aChecksSB.getAttribute("name"));
			aCheck.setDescr((String) aChecksSB.getAttribute("descr"));

			String valueTypeCd = (String) aChecksSB.getAttribute("valueTypeCd");
			SbiDomains domainValueType = findDomain(aSession, valueTypeCd, "PRED_CHECK");
			aCheck.setCheckType(domainValueType);
			aCheck.setValueTypeCd(valueTypeCd);

			aCheck.setValue1((String) aChecksSB.getAttribute("value1"));
			aCheck.setValue2((String) aChecksSB.getAttribute("value2"));
			
			// setting tenant/organization info
			aCheck.getCommonInfo().setOrganization(tenant.getName());
			
			logger.debug("Inserting Check with label = [" + aChecksSB.getAttribute("label") + "] ...");

			aSession.save(aCheck);
		}
		logger.debug("OUT");
	}

}
