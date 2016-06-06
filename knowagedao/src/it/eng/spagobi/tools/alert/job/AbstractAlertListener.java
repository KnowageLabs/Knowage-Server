package it.eng.spagobi.tools.alert.job;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.alert.bo.Alert;
import it.eng.spagobi.tools.alert.bo.AlertAction;
import it.eng.spagobi.tools.alert.dao.IAlertDAO;
import it.eng.spagobi.tools.alert.exception.AlertActionException;
import it.eng.spagobi.tools.alert.exception.AlertListenerException;
import it.eng.spagobi.tools.alert.metadata.SbiAlertLog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public abstract class AbstractAlertListener extends AbstractSuspendableJob implements IAlertListener {
	private static Logger logger = Logger.getLogger(AbstractAlertListener.class);

	private static final String LAST_KEY = "LAST_KEY";
	private static final String CONSECUTIVE_ALERTS_TRIGGERED = "CONSECUTIVE_ALERTS_TRIGGERED";
	private JobDetail jobDetail;
	private JobDataMap jobDataMap;
	private int eventBeforeTriggerAction;

	private final Map<Integer, IAlertAction> cachedActionMap = new HashMap<>();

	@Override
	public void internalExecute(JobExecutionContext context) throws JobExecutionException {
		jobDetail = context.getJobDetail();
		jobDataMap = jobDetail.getJobDataMap();
		String alertId = jobDataMap.getString(LISTENER_PARAMS);
		try {
			IAlertDAO alertDao = DAOFactory.getAlertDAO();
			Alert alert = alertDao.loadAlert(Integer.valueOf(alertId));
			eventBeforeTriggerAction = alert.getEventBeforeTriggerAction() != null ? alert.getEventBeforeTriggerAction() : 0;
			boolean singleExecution = alert.isSingleExecution();
			if (!singleExecution || lookForNewExecutions(alert.getJsonOptions())) {
				executeListener(alert.getJsonOptions());
			}
		} catch (NumberFormatException e) {
			logger.error("Alert id is not valid [" + alertId + "]", e);
			throw new JobExecutionException("Alert id is not valid [" + alertId + "]", e);
		} catch (EMFUserError e) {
			logger.error("Alert DAO error", e);
			throw new JobExecutionException("Alert DAO error", e);
		} catch (AlertListenerException e) {
			logger.error("AlertListener [" + getClass().getName() + "] error", e);
			throw new JobExecutionException("AlertListener [" + getClass().getName() + "] error", e);
		}
	}

	protected abstract boolean lookForNewExecutions(String jsonParameters) throws AlertListenerException;

	protected void writeAlertLog(String listenerParams, Integer actionId, String actionParams, String errorMsg) throws EMFUserError {
		SbiAlertLog alertLog = new SbiAlertLog();
		alertLog.setActionId(actionId);
		alertLog.setActionParams(actionParams);
		alertLog.setListenerId(getListenerId());
		alertLog.setDetail(errorMsg);
		alertLog.setListenerParams(listenerParams);
		alertLog.getCommonInfo().setTimeIn(new Date());
		alertLog.getCommonInfo().setSbiVersionIn(SbiCommonInfo.SBI_VERSION);
		Tenant tenant = TenantManager.getTenant();
		if (tenant != null) {
			alertLog.getCommonInfo().setOrganization(tenant.getName());
		}
		DAOFactory.getAlertDAO().insertAlertLog(alertLog);
	}

	private IAlertAction getActionInstance(Integer actionId) throws EMFUserError, AlertListenerException {
		if (!cachedActionMap.containsKey(actionId)) {
			AlertAction alertAction = DAOFactory.getAlertDAO().loadAction(actionId);
			try {
				IAlertAction action = (IAlertAction) Class.forName(alertAction.getClassName()).newInstance();
				cachedActionMap.put(actionId, action);
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				logger.error("Error executing action class[" + alertAction.getClassName() + "]", e);
				throw new AlertListenerException("Error executing action class[" + alertAction.getClassName() + "]", e);
			}
		}
		return cachedActionMap.get(actionId);
	}

	protected void executeAction(Object listenerParamsObj, Integer actionId, Object actionParamsObj, Map<String, String> parameterMapFromListener)
			throws EMFUserError, AlertListenerException {
		String listenerParams = JsonConverter.objectToJson(listenerParamsObj, listenerParamsObj.getClass()).toString();
		String actionParams = JsonConverter.objectToJson(actionParamsObj, actionParamsObj.getClass()).toString();
		try {
			// incrementActionTriggered(actionId);
			IAlertAction action = getActionInstance(actionId);
			action.executeAction(actionParams, parameterMapFromListener);
			writeAlertLog(listenerParams, actionId, actionParams, null);
		} catch (AlertActionException e) {
			logger.error("Error executing action: \"" + actionId + "\"", e);
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			writeAlertLog(listenerParams, actionId, actionParams, "Error executing action. " + sw.toString());
		}
	}

	public Integer getListenerId() {
		try {
			String listenerId = jobDataMap.getString(LISTENER_ID);
			return listenerId != null ? Integer.valueOf(listenerId) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	protected Object loadLastKey(Class clazz) {
		if (jobDataMap.containsKey(LAST_KEY)) {
			String key = jobDataMap.getString(LAST_KEY);
			return JsonConverter.jsonToObject(key, clazz);
		} else {
			return null;
		}
	}

	protected void saveLastKey(Object key) throws AlertListenerException {
		String _key = JsonConverter.objectToJson(key, key.getClass()).toString();
		jobDataMap.put(LAST_KEY, _key);
		try {
			StdSchedulerFactory.getDefaultScheduler().addJob(jobDetail, true);
		} catch (SchedulerException e) {
			throw new AlertListenerException("Error saving lastKey", e);
		}
	}

	// protected long getActionTriggered(Integer actionId) {
	// String key = ACTION_TRIGGERED + "_" + actionId;
	// return jobDataMap.containsKey(key) ? Integer.valueOf(jobDataMap.getString(key)) : 0;
	// }
	//
	// protected long getActionExecuted(Integer actionId) {
	// String key = ACTION_EXECUTED + "_" + actionId;
	// return jobDataMap.containsKey(key) ? Integer.valueOf(jobDataMap.getString(key)) : 0;
	// }
	//
	// protected void incrementActionTriggered(Integer actionId) {
	// long n = getActionTriggered(actionId) + 1;
	// String key = ACTION_TRIGGERED + "_" + actionId;
	// jobDataMap.put(key, "" + n);
	// try {
	// StdSchedulerFactory.getDefaultScheduler().addJob(jobDetail, true);
	// } catch (SchedulerException e) {
	// e.printStackTrace();
	// }
	// }

	protected void incrementAlertTriggered() {
		long n = getConsecutiveAlertsTriggered() + 1;
		String key = CONSECUTIVE_ALERTS_TRIGGERED;
		jobDataMap.put(key, "" + n);
		try {
			StdSchedulerFactory.getDefaultScheduler().addJob(jobDetail, true);
		} catch (SchedulerException e) {
			logger.error("incrementAlertTriggered", e);
		}
	}

	protected void resetConsecutiveAlertsTriggered() {
		jobDataMap.put(CONSECUTIVE_ALERTS_TRIGGERED, "0");
		try {
			StdSchedulerFactory.getDefaultScheduler().addJob(jobDetail, true);
		} catch (SchedulerException e) {
			logger.error("resetConsecutiveAlertsTriggered", e);
		}
	}

	protected int getConsecutiveAlertsTriggered() {
		String key = CONSECUTIVE_ALERTS_TRIGGERED;
		return jobDataMap.containsKey(key) ? Integer.valueOf(jobDataMap.getString(key)) : 0;
	}

	/**
	 * @return the eventBeforeTriggerAction
	 */
	public int getEventBeforeTriggerAction() {
		return eventBeforeTriggerAction;
	}

	/**
	 * @param eventBeforeTriggerAction
	 *            the eventBeforeTriggerAction to set
	 */
	public void setEventBeforeTriggerAction(Integer eventBeforeTriggerAction) {
		this.eventBeforeTriggerAction = eventBeforeTriggerAction;
	}

}
