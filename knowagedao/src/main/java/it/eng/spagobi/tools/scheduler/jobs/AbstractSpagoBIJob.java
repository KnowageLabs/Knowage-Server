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
