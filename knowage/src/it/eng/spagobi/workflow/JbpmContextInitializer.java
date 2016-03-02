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
