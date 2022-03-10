package it.eng.spagobi.tools.alert.job;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.jobs.AbstractSpagoBIJob;

public abstract class AbstractSuspendableJob extends AbstractSpagoBIJob implements Job {

	public enum JOB_STATUS {
		SUSPENDED, ACTIVE, EXPIRED
	}

	private static Logger logger = Logger.getLogger(AbstractSuspendableJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		setTenant(context);
		// Execute internal only if trigger isn't paused
		if (!isTriggerPaused(context)) {
			internalExecute(context);
		}
		unsetTenant();
	}

	public abstract void internalExecute(JobExecutionContext context) throws JobExecutionException;

	private boolean isTriggerPaused(JobExecutionContext jobExecutionContext) {
		Trigger trigger = jobExecutionContext.getTrigger();
		String triggerGroup = trigger.getKey().getGroup();
		String triggerName = trigger.getKey().getName();
		String jobName = trigger.getJobKey().getName();
		String jobGroupOriginal = jobExecutionContext.getJobDetail().getKey().getGroup();
		String[] bits = jobGroupOriginal.split("/");
		String jobGroup = bits[bits.length - 1];

		ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();
		return schedulerDAO.isTriggerPaused(triggerGroup, triggerName, jobGroup, jobName);
	}
}
