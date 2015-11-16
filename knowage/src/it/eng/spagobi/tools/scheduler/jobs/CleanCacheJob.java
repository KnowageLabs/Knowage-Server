package it.eng.spagobi.tools.scheduler.jobs;

import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CleanCacheJob extends AbstractSpagoBIJob implements Job {

	static private Logger logger = Logger.getLogger(CleanCacheJob.class);

	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		logger.debug("IN");
		try {
			this.setTenant(jobExecutionContext);
			this.executeInternal(jobExecutionContext);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
	}

	private void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		logger.debug("IN");
		try {
			ICache cache = SpagoBICacheManager.getCache();
			cache.deleteAll();
			logger.debug("Cache cleaning ended succesfully!");
		} catch (Exception e) {
			logger.error("Error while executiong job ", e);
		} finally {
			logger.debug("OUT");
		}
	}
}
