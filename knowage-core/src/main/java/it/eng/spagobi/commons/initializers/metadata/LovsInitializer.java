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
import it.eng.spagobi.behaviouralmodel.lov.metadata.SbiLov;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class LovsInitializer extends SpagoBIInitializer {

	private static Logger logger = Logger.getLogger(LovsInitializer.class);

	public LovsInitializer() {
		targetComponentName = "Lovs";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/lovs.xml";
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
			String hql = "from SbiLov l where l.commonInfo.organization = :organization";
			Query hqlQuery = hibernateSession.createQuery(hql);
			hqlQuery.setString("organization", tenant.getName());
			List lovs = hqlQuery.list();
			if (lovs.isEmpty()) {
				logger.info("No LOVs for tenant " + tenant.getName() + ". Starting populating predefined LOVs...");
				writeLovs(hibernateSession, tenant);
			} else {
				logger.debug("Lovs table is already populated for tenant " + tenant.getName());
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Ab unexpected error occured while initializeng LOVs", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	private void writeLovs(Session aSession, SbiTenant tenant) throws Exception {
		logger.debug("IN");
		SourceBean lovsSB = getConfiguration();
		if (lovsSB == null) {
			logger.info("Configuration file for predefined lovs not found");
			return;
		}
		List lovsList = lovsSB.getAttributeAsList("LOV");
		if (lovsList == null || lovsList.isEmpty()) {
			logger.info("No predefined lovs available from configuration file");
			return;
		}
		Iterator it = lovsList.iterator();
		while (it.hasNext()) {
			SourceBean aLovsSB = (SourceBean) it.next();
			SbiLov aLov = new SbiLov();
			aLov.setLabel((String) aLovsSB.getAttribute("label"));
			aLov.setName((String) aLovsSB.getAttribute("name"));
			aLov.setDescr((String) aLovsSB.getAttribute("descr"));
			aLov.setDefaultVal((String) aLovsSB.getAttribute("defaultVal"));
			aLov.setProfileAttr((String) aLovsSB.getAttribute("profileAttr"));

			SourceBean lovProviderSB = (SourceBean) aLovsSB.getAttribute("LOV_PROVIDER");
			aLov.setLovProvider(lovProviderSB.getCharacters());

			String inputTypeCd = (String) aLovsSB.getAttribute("inputTypeCd");
			SbiDomains domainInputType = findDomain(aSession, inputTypeCd, "INPUT_TYPE");
			aLov.setInputType(domainInputType);
			aLov.setInputTypeCd(inputTypeCd);

			// setting tenant/organization info
			aLov.getCommonInfo().setOrganization(tenant.getName());
			
			logger.debug("Inserting Lov with label = [" + aLovsSB.getAttribute("label") + "] ...");

			aSession.save(aLov);
		}
		logger.debug("OUT");
	}

}
