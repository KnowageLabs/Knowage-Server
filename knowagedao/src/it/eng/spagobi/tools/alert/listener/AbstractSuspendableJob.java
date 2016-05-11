package it.eng.spagobi.tools.alert.listener;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.jobs.AbstractSpagoBIJob;

public abstract class AbstractSuspendableJob extends AbstractSpagoBIJob implements Job {

	public enum JOB_STATUS {
		SUSPENDED, ACTIVE, EXPIRED
	};

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
		String triggerGroup = trigger.getGroup();
		String triggerName = trigger.getName();
		String jobName = trigger.getJobName();
		String jobGroupOriginal = jobExecutionContext.getJobDetail().getGroup();
		String[] bits = jobGroupOriginal.split("/");
		String jobGroup = bits[bits.length - 1];
		boolean result = false;

		ISchedulerDAO schedulerDAO;
		try {
			schedulerDAO = DAOFactory.getSchedulerDAO();
			result = schedulerDAO.isTriggerPaused(triggerGroup, triggerName, jobGroup, jobName);
		} catch (EMFUserError e) {
			logger.error("Error while checking if the trigger [" + triggerName + "] is paused");
		}

		return result;

	}
}
