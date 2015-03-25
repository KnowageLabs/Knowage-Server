/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.initializers.metadata;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiOrganizationEngine;
import it.eng.spagobi.commons.metadata.SbiOrganizationEngineId;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class EnginesInitializer extends SpagoBIInitializer {

	static private Logger logger = Logger.getLogger(EnginesInitializer.class);
	
	public EnginesInitializer() {
		targetComponentName = "Engines";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/engines.xml";
	}
	
/*	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
			init(config, hibernateSession);

		} catch(Throwable t){
			logger.error("Impossible to init EnginesInitializer", t);
		}finally {
			logger.debug("OUT");
		}
	}*/
	
	public void init(SourceBean config, Session hibernateSession) { 
		logger.debug("IN");
		try {
			String hql = "from SbiEngines";
			Query hqlQuery = hibernateSession.createQuery(hql);
			List engines = hqlQuery.list();
			if (engines.isEmpty()) {
				logger.info("No engines. Starting populating predefined engines...");
				writeEngines(hibernateSession);
			} else {
				logger.debug("Engines table is already populated");
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Ab unexpected error occured while initializeng Engines", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	private void writeEngines(Session aSession) throws Exception {
		logger.debug("IN");
		SourceBean enginesSB = getConfiguration();
		if (enginesSB == null) {
			logger.info("Configuration file for predefined engines not found");
			return;
		}
		List enginesList = enginesSB.getAttributeAsList("ENGINE");
		if (enginesList == null || enginesList.isEmpty()) {
			logger.info("No predefined engines available from configuration file");
			return;
		}
		Iterator it = enginesList.iterator();
		while (it.hasNext()) {
			SourceBean anEngineSB = (SourceBean) it.next();
			SbiEngines anEngine = new SbiEngines();
			anEngine.setName((String) anEngineSB.getAttribute("name"));
			anEngine.setDescr((String) anEngineSB.getAttribute("descr"));
			anEngine.setMainUrl((String) anEngineSB.getAttribute("mainUrl"));
			anEngine.setDriverNm((String) anEngineSB.getAttribute("driverNm"));
			anEngine.setLabel((String) anEngineSB.getAttribute("label"));
			anEngine.setClassNm((String) anEngineSB.getAttribute("classNm"));
			anEngine.setUseDataSet(new Boolean((String) anEngineSB.getAttribute("useDataSet")));
			anEngine.setUseDataSource(new Boolean((String) anEngineSB.getAttribute("useDataSource")));
			anEngine.setEncrypt(new Short((String) anEngineSB.getAttribute("encrypt")));
			anEngine.setObjUplDir((String) anEngineSB.getAttribute("objUplDir"));
			anEngine.setObjUseDir((String) anEngineSB.getAttribute("objUseDir"));
			anEngine.setSecnUrl((String) anEngineSB.getAttribute("secnUrl"));

			String engineTypeCd = (String) anEngineSB.getAttribute("engineTypeCd");
			SbiDomains domainEngineType = findDomain(aSession, engineTypeCd, "ENGINE_TYPE");
			anEngine.setEngineType(domainEngineType);

			String biobjTypeCd = (String) anEngineSB.getAttribute("biobjTypeCd");
			SbiDomains domainBiobjectType = findDomain(aSession, biobjTypeCd, "BIOBJ_TYPE");
			anEngine.setBiobjType(domainBiobjectType);
			
			Integer engineId = (Integer)aSession.save(anEngine);
			
			logger.debug("Inserting Engine with label = [" + anEngineSB.getAttribute("label") + "] ...");

			///associate to default tenant
			SbiTenant aTenant = getDefaultTenant(aSession);

			SbiOrganizationEngine association = new SbiOrganizationEngine();
			association.setSbiEngines(anEngine);
			association.setSbiOrganizations(aTenant);
			SbiCommonInfo commonInfo = new SbiCommonInfo();
			commonInfo.setUserIn("server");
			commonInfo.setTimeIn(new Date());
			
			association.setCommonInfo(commonInfo);
			
			SbiOrganizationEngineId id = new SbiOrganizationEngineId();
			id.setEngineId(engineId);
			id.setOrganizationId(aTenant.getId());
			association.setId(id);
			
			aSession.save(association);
			
			
		}
		logger.debug("OUT");
	}
	private SbiTenant getDefaultTenant(Session hibernateSession) throws Exception {
		logger.debug("IN");
		SbiTenant aTenant = new SbiTenant();
		String hql = "from SbiTenant t where t.name =?";
		Query hqlQuery = hibernateSession.createQuery(hql);
		hqlQuery.setString(0, "SPAGOBI");
		SbiTenant defaultenant = (SbiTenant)hqlQuery.uniqueResult();
		logger.debug("OUT");
		return defaultenant;
		
	}
}
