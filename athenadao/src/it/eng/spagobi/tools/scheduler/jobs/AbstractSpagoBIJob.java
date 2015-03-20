/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.jobs;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public abstract class AbstractSpagoBIJob {

	static private Logger logger = Logger.getLogger(AbstractSpagoBIJob.class);	
	
	protected void setTenant(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		logger.debug("IN");
		JobDetail jobDetail = jobExecutionContext.getJobDetail();
		Tenant tenant;
		try {
			tenant = DAOFactory.getSchedulerDAO().findTenant(jobDetail);
		} catch (Throwable t) {
			logger.error("Cannot retrieve tenant for job " + jobDetail.toString(), t);
			throw new SpagoBIRuntimeException("Cannot retrieve tenant for job " + jobDetail.toString(), t);
		}
		logger.debug("Tenant : " + tenant);
		TenantManager.setTenant(tenant);
		logger.debug("OUT");
	}
	
	protected void unsetTenant() {
		logger.debug("IN");
		TenantManager.unset();
		logger.debug("OUT");
	}
	
}
