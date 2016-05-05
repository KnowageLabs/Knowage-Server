package it.eng.spagobi.tools.alert.listener;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.alert.bo.Alert;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public abstract class AbstractAlertListener extends AbstractSuspendableJob implements IAlertListener {

	private static Logger logger = Logger.getLogger(AbstractAlertListener.class);
	private String listenerId = null;

	@Override
	public void internalExecute(JobExecutionContext context) throws JobExecutionException {
		JobDetail jobDetail = context.getJobDetail();
		listenerId = jobDetail.getJobDataMap().getString(LISTENER_ID);
		JobDataMap dataMap = jobDetail.getJobDataMap();
		String alertId = dataMap.getString(LISTENER_PARAMS);
		try {
			Alert alert = DAOFactory.getAlertDAO().loadAlert(Integer.valueOf(alertId));
			execute(alert.getJsonOptions());
		} catch (NumberFormatException e) {
			logger.error("Alert id not valid [" + alertId + "]", e);
			throw new JobExecutionException("Alert id not valid [" + alertId + "]", e);
		} catch (EMFUserError e) {
			logger.error("Alert DAO error", e);
			throw new JobExecutionException("Alert DAO error", e);
		}

	}

	public Integer getListenerId() {
		try {
			return listenerId != null ? Integer.valueOf(listenerId) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
