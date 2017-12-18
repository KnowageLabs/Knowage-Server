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
package it.eng.spagobi.tools.scheduler.init;

import it.eng.spago.base.SourceBean;
import it.eng.spago.init.InitializerIFace;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

import java.io.InputStream;
import java.util.Properties;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;


public class QuartzInitializer implements InitializerIFace {
	
	private SourceBean _config = null;

	
	/* (non-Javadoc)
	 * @see it.eng.spago.init.InitializerIFace#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {
		StdSchedulerFactory stdSchedFact = new StdSchedulerFactory();
	    Properties properties = new Properties();
	    try{
	    	Thread currThread = Thread.currentThread();
	    	ClassLoader classLoader = currThread.getContextClassLoader();
	    	InputStream propIs = classLoader.getResourceAsStream("quartz.properties");
	        properties.load(propIs);
	        stdSchedFact.initialize(properties);
	        Scheduler sched = stdSchedFact.getScheduler();
	        sched.start();
	    } catch (Exception e) {
	    	SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
	    			            "init", "Error while initializing scheduler " + e);
	    }
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spago.init.InitializerIFace#getConfig()
	 */
	public SourceBean getConfig() {
		return _config;
	}

	
}
