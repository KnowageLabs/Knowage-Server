/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
