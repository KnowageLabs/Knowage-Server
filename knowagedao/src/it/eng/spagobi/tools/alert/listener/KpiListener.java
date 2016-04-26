package it.eng.spagobi.tools.alert.listener;

import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.scheduler.jobs.AbstractSpagoBIJob;

import java.util.List;

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
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("KpiListener Executed!");
		String jsonParameters = context.getJobDetail().getJobDataMap().getString("listenerParams");
		InputParameter par = (InputParameter) JsonConverter.jsonToObject(jsonParameters, InputParameter.class);

		for (Action action : par.getActions()) {
			// TODO look for kpi_values by kpiId (and date?)
			// TODO if value is between min and max of a threshold --> call action
			// TODO save result on ALERT_XXXX table
		}

	}
}

class InputParameter {
	private Integer kpiId;
	private List<Action> actions;

	public Integer getKpiId() {
		return kpiId;
	}

	public void setKpiId(Integer kpiId) {
		this.kpiId = kpiId;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

}

class Action {
	private Integer idAction;
	private List<Integer> thresholdValues;
	private String jsonActionParameters;

	public Integer getIdAction() {
		return idAction;
	}

	public void setIdAction(Integer idAction) {
		this.idAction = idAction;
	}

	public List<Integer> getThresholdValues() {
		return thresholdValues;
	}

	public void setThresholdValues(List<Integer> thresholdValues) {
		this.thresholdValues = thresholdValues;
	}

	public String getJsonActionParameters() {
		return jsonActionParameters;
	}

	public void setJsonActionParameters(String jsonActionParameters) {
		this.jsonActionParameters = jsonActionParameters;
	}

}