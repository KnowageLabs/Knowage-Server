package it.eng.spagobi.kpi.alert;

import it.eng.spagobi.tools.scheduler.jobs.AbstractSpagoBIJob;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class KpiListener extends AbstractSpagoBIJob implements Job {
	/*
	 * public String getTemplateUrl{
	 * 
	 * } public void save(){ // create job & trigger DAOFactory.getNewKpiDAO().loadScorecard(id) }
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("KpiListener Executed!");

	}
}
