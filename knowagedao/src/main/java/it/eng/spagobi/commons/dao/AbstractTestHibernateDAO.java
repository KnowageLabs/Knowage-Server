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
package it.eng.spagobi.commons.dao;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author Gioia
 *
 */
public class AbstractTestHibernateDAO {
	private static final SessionFactory sessionFactory;
	static {
		try {
			String fileCfg = "hibernate.cfg.xml";
			Configuration conf = new Configuration();
			conf = conf.configure(fileCfg);
			sessionFactory = conf.buildSessionFactory();		
		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
	
	/**
	 * Gets tre current session.
	 * 
	 * @return The current session object.
	 */
	public Session getSession(){
		return sessionFactory.openSession();
	}
	
	/**
	 * Traces the exception information of a throwable input object.
	 * 
	 * @param t The input throwable object
	 */
	public void logException(Throwable t){
		SpagoBITracer.debug(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
				            "logException", t.getClass().getName() + ":" + t.getMessage());
	}
	
	
}
