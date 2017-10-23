/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend.services;

import org.apache.log4j.Logger;

import it.eng.spagobi.engines.talend.TalendEngine;
import it.eng.spagobi.engines.talend.exception.ContextNotFoundException;
import it.eng.spagobi.engines.talend.exception.JobExecutionException;
import it.eng.spagobi.engines.talend.exception.JobNotFoundException;
import it.eng.spagobi.engines.talend.runtime.Job;
import it.eng.spagobi.engines.talend.runtime.RuntimeRepository;
import it.eng.spagobi.utilities.engines.AbstractEngineStartServlet;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

public class JobRunService extends AbstractEngineStartServlet {
	
	public static final String JS_FILE_ZIP = "JS_File";
	public static final String JS_EXT_ZIP = ".zip";	
	
	private static final long serialVersionUID = 1L;
	
	private static transient Logger logger = Logger.getLogger(JobRunService.class);
	
	
	public void doService( EngineStartServletIOManager servletIOManager ) throws SpagoBIEngineException {
		
		RuntimeRepository runtimeRepository;
		Job job;

		logger.debug("IN");
		
		try {		
			
			//servletIOManager.auditServiceStartEvent();
				
			super.doService(servletIOManager);
				
			job = new Job( servletIOManager.getTemplateAsSourceBean() );			
			runtimeRepository = TalendEngine.getRuntimeRepository();
			
			try {
				runtimeRepository.runJob(job, servletIOManager.getEnv());
			} catch (JobNotFoundException ex) {
				logger.error(ex.getMessage());

				throw new SpagoBIEngineException("Job not found",
						"job.not.existing");
		
			} catch (ContextNotFoundException ex) {
				logger.error(ex.getMessage(), ex);
				
				throw new SpagoBIEngineException("Context script not found",
						"context.script.not.existing");
			
			} catch(JobExecutionException ex) {
				logger.error(ex.getMessage(), ex);
				
				throw new SpagoBIEngineException("Job execution error",
						"job.exectuion.error");
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				
				throw new SpagoBIEngineException("Job execution error",
						"job.exectuion.error");
			}

			
			servletIOManager.tryToWriteBackToClient("etl.process.started");
			
		} finally {
			logger.debug("OUT");
		}
	}
}
