package it.eng.spagobi.tools.alert.listener;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

public abstract class AbstractAlertListener implements Job, IAlertListener {

	private static Logger logger = Logger.getLogger(AbstractAlertListener.class);
	private String listenerId = null;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDetail jobDetail = context.getJobDetail();
		listenerId = jobDetail.getJobDataMap().getString(LISTENER_ID);
		JobDataMap dataMap = jobDetail.getJobDataMap();
		try {
			Tenant tenant = DAOFactory.getSchedulerDAO().findTenant(jobDetail);
			TenantManager.setTenant(tenant);
		} catch (EMFUserError e) {
			throw new JobExecutionException("Unable to retrieve Tenant", e);
		}
		// Execute internal only if trigger isn't paused
		if (!isTriggerPaused(context)) {
			execute(dataMap.getString(LISTENER_PARAMS));
		}
		TenantManager.unset();
	}

	public Integer getListenerId() {
		try {
			return listenerId != null ? Integer.decode(listenerId) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

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
