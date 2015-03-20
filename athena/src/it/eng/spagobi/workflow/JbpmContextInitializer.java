/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.workflow;

import it.eng.spago.base.SourceBean;
import it.eng.spago.init.InitializerIFace;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.persistence.db.DbPersistenceServiceFactory;
import org.jbpm.svc.Services;


public class JbpmContextInitializer implements InitializerIFace {
	
	/** 
	 * SourceBean that contains the configuration parameters
	 */
	private SourceBean _config = null;

	
	/* (non-Javadoc)
	 * @see it.eng.spago.init.InitializerIFace#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {
		JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
		DbPersistenceServiceFactory dbpsf = (DbPersistenceServiceFactory)jbpmConfiguration.getServiceFactory(Services.SERVICENAME_PERSISTENCE);
		try{
			SessionFactory sessionFactHib = dbpsf.getSessionFactory();
			Session sessionHib = sessionFactHib.openSession();
			Query hibQuery = sessionHib.createQuery(" from ProcessDefinition");
			List hibList = hibQuery.list();			
		} catch (HibernateException he) {
			jbpmConfiguration.createSchema();
		} 
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spago.init.InitializerIFace#getConfig()
	 */
	public SourceBean getConfig() {
		return _config;
	}

	
}
