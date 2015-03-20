/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.jobs;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * TODO: create an initializer that look up for all job whose job class is equal to XExecuteBIDocumentJob
 * and replace it with ExecuteBIDocumentJob. The remove class ExecuteBIDocumentJob and rename 
 * XExecuteBIDocumentJob to ExecuteBIDocumentJob. NOTE: the old implementation of ExecuteBIDocumentJob has
 * been saved in CopyOfExecuteBIDocumentJob
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ExecuteBIDocumentJob implements Job {

	private static Logger logger = Logger.getLogger(ExecuteBIDocumentJob.class);
	
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		logger.debug("IN");
		Job job = new XExecuteBIDocumentJob();
		job.execute(jobExecutionContext);
		logger.debug("OUT");
	}

}
