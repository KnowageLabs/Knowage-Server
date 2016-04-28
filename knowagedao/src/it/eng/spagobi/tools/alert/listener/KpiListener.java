package it.eng.spagobi.tools.alert.listener;

import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.ThresholdValue;
import it.eng.spagobi.kpi.metadata.SbiKpiThresholdValue;
import it.eng.spagobi.kpi.metadata.SbiKpiValue;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.alert.action.IAlertAction;
import it.eng.spagobi.tools.alert.metadata.SbiAlertAction;
import it.eng.spagobi.tools.scheduler.jobs.AbstractSpagoBIJob;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class KpiListener extends AbstractSpagoBIJob implements Job {

	private static Logger logger = Logger.getLogger(KpiListener.class);

	private final Map<ThresholdValue, List<Action>> thresholdMap = new HashMap<ThresholdValue, List<Action>>();
	private final Map<Integer, SbiAlertAction> actionMap = new HashMap<Integer, SbiAlertAction>();

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("KpiListener running...");
		String jsonParameters = context.getJobDetail().getJobDataMap().getString("listenerParams");
		InputParameter par = (InputParameter) JsonConverter.jsonToObject(jsonParameters, InputParameter.class);

		Session session = HibernateSessionManager.getCurrentSession();
		List<SbiKpiValue> resultSet = loadResults(par.getKpi().getId());
		if (resultSet != null) {
			for (SbiKpiValue sbiKpiValue : resultSet) {
				Integer id = selectThreshold(sbiKpiValue);
				if (id != null) {
					// Value of this sbiKpiValue is in a threshold
					// so we have to execute related actions
					executeActions(id, session);
					// TODO save result on SBI_ALERT_LOG table
					// TODO or log error (same table) if something goes wrong
				}
			}
		} else {
			// TODO log null result to SBI_ALERT_LOG table
		}

		logger.info("KpiListener ended");
	}

	private SbiAlertAction loadAction(Integer actionId, Session session) {
		if (!actionMap.containsKey(actionId)) {
			SbiAlertAction sbiAction = (SbiAlertAction) session.load(SbiAlertAction.class, actionId);
			actionMap.put(actionId, sbiAction);
		}
		return actionMap.get(actionId);
	}

	private List<Action> getActionsByThreshold(Integer thresholdId, Session session) {
		ThresholdValue thresholdValue = new ThresholdValue(thresholdId);
		if (!thresholdMap.containsKey(thresholdValue)) {
			SbiKpiThresholdValue sbiThreshold = (SbiKpiThresholdValue) session.load(SbiKpiThresholdValue.class, thresholdId);
			thresholdMap.put(from(sbiThreshold), new ArrayList<Action>());
		}
		return thresholdMap.get(thresholdValue);
	}

	private void executeActions(Integer id, Session session) {
		List<Action> actions = getActionsByThreshold(id, session);
		for (Action action : actions) {
			SbiAlertAction sbiAction = loadAction(action.getIdAction(), session);
			try {
				IAlertAction alertAction = (IAlertAction) Class.forName(sbiAction.getClassName()).newInstance();
				alertAction.execute(action.getJsonActionParameters());
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				// TODO rise exception or return false
				logger.error("Error execution action class[" + sbiAction.getClassName() + "]", e);
			}
		}
	}

	private ThresholdValue from(SbiKpiThresholdValue sbiValue) {
		ThresholdValue ret = new ThresholdValue();
		ret.setId(sbiValue.getId());
		ret.setIncludeMin('T' == sbiValue.getIncludeMin());
		ret.setIncludeMax('T' == sbiValue.getIncludeMax());
		ret.setMinValue(sbiValue.getMinValue());
		ret.setMaxValue(sbiValue.getMaxValue());
		return ret;
	}

	public static List<SbiKpiValue> loadResults(Integer id) {
		Session session = HibernateSessionManager.getCurrentSession();
		Date lastTimeRun = (Date) session.createCriteria(SbiKpiValue.class).add(Restrictions.eq("kpiId", id)).setProjection(Projections.max("timeRun"))
				.uniqueResult();
		List<SbiKpiValue> lst = null;
		if (lastTimeRun != null) {
			lst = session.createCriteria(SbiKpiValue.class).add(Restrictions.eq("kpiId", id)).add(Restrictions.eq("timeRun", lastTimeRun)).list();
		}
		return lst;
	}

	private Integer selectThreshold(SbiKpiValue kpiValue) {
		for (ThresholdValue threshold : thresholdMap.keySet()) {
			double value = kpiValue.getComputedValue();
			if ((threshold.getMinValue() == null || value >= threshold.getMinValue().doubleValue())
					&& (threshold.getMaxValue() == null || value <= threshold.getMaxValue().doubleValue())
					&& (threshold.isIncludeMin() || value != threshold.getMinValue().doubleValue())
					&& (threshold.isIncludeMax() || value != threshold.getMaxValue().doubleValue())) {
				return threshold.getId();
			}
		}
		return null;
	}
}

class InputParameter {
	private Kpi kpi;
	private List<Action> actions;

	public Kpi getKpi() {
		return kpi;
	}

	public void setKpi(Kpi kpi) {
		this.kpi = kpi;
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