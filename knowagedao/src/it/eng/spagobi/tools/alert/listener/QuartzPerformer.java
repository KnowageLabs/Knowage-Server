package it.eng.spagobi.tools.alert.listener;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class QuartzPerformer implements Job {

	IAlertListener listener;

	public QuartzPerformer(IAlertListener listener) {
		this.listener = listener;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String jsonParameters = context.getJobDetail().getJobDataMap().getString("listenerParams");
		listener.execute(jsonParameters);
	}

}
