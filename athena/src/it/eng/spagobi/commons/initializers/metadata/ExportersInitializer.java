/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.initializers.metadata;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.base.SourceBean;
import it.eng.spago.init.InitializerIFace;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.engines.config.metadata.SbiExporters;
import it.eng.spagobi.engines.config.metadata.SbiExportersId;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ExportersInitializer extends SpagoBIInitializer {

	static private Logger logger = Logger.getLogger(ExportersInitializer.class);

	public ExportersInitializer() {
		targetComponentName = "Exporters";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/exporters.xml";
	}
	
/*	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
			init(config, hibernateSession);
		} finally {
			logger.debug("OUT");
		}
	}
	*/
	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
			String hql = "from SbiExporters";
			Query hqlQuery = hibernateSession.createQuery(hql);
			List exporters = hqlQuery.list();
			if (exporters.isEmpty()) {
				logger.info("No exporters . Starting populating predefined exporters...");
				writeExporters(hibernateSession);
			} else {
				logger.debug("Exporters table is already populated");
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Ab unexpected error occured while initializeng LOVs", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	private void writeExporters(Session aSession) throws Exception {
		logger.debug("IN");
		SourceBean exportersSB = getConfiguration();
		if (exportersSB == null) {
			logger.info("Configuration file for predefined exporters not found");
			return;
		}
		List exportersList = exportersSB.getAttributeAsList("EXPORTER");
		if (exportersList == null || exportersList.isEmpty()) {
			logger.info("No predefined exporters available from configuration file");
			return;
		}
		Iterator it = exportersList.iterator();
		while (it.hasNext()) {
			SourceBean anExporterSB = (SourceBean) it.next();

			String domainLabel = ((String) anExporterSB.getAttribute("domain"));
			SbiDomains hibDomain = findDomain(aSession, domainLabel, "EXPORT_TYPE");
			if (hibDomain == null) {
				logger.error("Could not find domain for exporter");
				return;
			}

			String engineLabel = ((String) anExporterSB.getAttribute("engine"));
			SbiEngines hibEngine = findEngine(aSession, engineLabel);
			if (hibEngine == null) {
				logger.error("Could not find engine with label [" + engineLabel + "] for exporter");
			}else{

				String defaultValue=((String) anExporterSB.getAttribute("defaultValue"));
	
				SbiExporters anExporter=new SbiExporters();
				SbiExportersId exporterId=new SbiExportersId(hibEngine.getEngineId(), hibDomain.getValueId());
				anExporter.setId(exporterId);
				anExporter.setSbiDomains(hibDomain);
				anExporter.setSbiEngines(hibEngine);
	
				Boolean value=defaultValue!=null ? Boolean.valueOf(defaultValue) : Boolean.FALSE;
				anExporter.setDefaultValue(value.booleanValue());
	
				logger.debug("Inserting Exporter for engine "+hibEngine.getLabel());
	
				aSession.save(anExporter);
			}
		}
		logger.debug("OUT");
	}
}
