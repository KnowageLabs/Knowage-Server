package it.eng.spagobi.tools.alert.listener;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public abstract class AbstractAlertListener implements Job, IAlertListener {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		execute(dataMap.getString(LISTENER_PARAMS));
	}

}
