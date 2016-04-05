package it.eng.spagobi.kpi.job;

import it.eng.spagobi.tools.scheduler.jobs.AbstractSpagoBIJob;

import java.text.DateFormat;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ProcessKpiJob extends AbstractSpagoBIJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO
		System.out.println(DateFormat.getInstance().format(new Date()) + " Processing Kpi Job...");

	}

}
