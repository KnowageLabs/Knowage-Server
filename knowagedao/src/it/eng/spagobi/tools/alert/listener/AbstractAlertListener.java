package it.eng.spagobi.tools.alert.listener;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tenant.Tenant;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public abstract class AbstractAlertListener implements Job, IAlertListener {

	private JobDetail jobDetail;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		this.jobDetail = context.getJobDetail();
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		execute(dataMap.getString(LISTENER_PARAMS));
	}

	public String getTenant() throws JobExecutionException {
		Tenant tenant = null;
		try {
			tenant = DAOFactory.getSchedulerDAO().findTenant(jobDetail);
		} catch (EMFUserError e) {
			throw new JobExecutionException("Unable to retrieve Tenant", e);
		}
		return tenant != null ? tenant.getName() : null;
	}

	public Integer getListenerId() {
		String listenerId = this.jobDetail.getJobDataMap().getString(LISTENER_ID);
		if (listenerId != null && listenerId.matches("\\d+")) {
			return new Integer(listenerId);
		} else {
			return null;
		}
	}

}
