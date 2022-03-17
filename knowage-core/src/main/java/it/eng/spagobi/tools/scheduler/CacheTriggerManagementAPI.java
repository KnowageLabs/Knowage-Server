package it.eng.spagobi.tools.scheduler;

import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.init.CleanCacheQuartzInitializer;
import it.eng.spagobi.tools.scheduler.utils.PredefinedCronExpression;

public class CacheTriggerManagementAPI {

	private static transient Logger logger = Logger.getLogger(CacheTriggerManagementAPI.class);

	public boolean updateCronExpression(String confValue) {
		ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();

		Trigger simpleTrigger = new Trigger();
		simpleTrigger.setName(CleanCacheQuartzInitializer.DEFAULT_TRIGGER_NAME);
		simpleTrigger.setJob(schedulerDAO.loadJob(CleanCacheQuartzInitializer.DEFAULT_JOB_NAME, CleanCacheQuartzInitializer.DEFAULT_JOB_NAME));
		simpleTrigger.setGroupName(Scheduler.DEFAULT_GROUP);
		simpleTrigger.getChronExpression().setExpression(getCronExpression(confValue));
		simpleTrigger.setStartTime(new Date());
		simpleTrigger.setRunImmediately(false);

		if (schedulerDAO.triggerExists(simpleTrigger)) {
			if (confValue.equalsIgnoreCase("none")) {
				schedulerDAO.deleteTrigger(CleanCacheQuartzInitializer.DEFAULT_TRIGGER_NAME, Scheduler.DEFAULT_GROUP);
			} else {
				schedulerDAO.updateTrigger(simpleTrigger);
			}
		} else if (!confValue.equalsIgnoreCase("none")) {
			schedulerDAO.insertTrigger(simpleTrigger);
		}

		return true;
	}

	private String getCronExpression(String valueCheck) {
		if (valueCheck == null) {
			logger.debug("This value is [" + valueCheck + "]");
			return null;
		}

		for (PredefinedCronExpression value : PredefinedCronExpression.values()) {
			if (valueCheck.equalsIgnoreCase(value.getLabel())) {
				logger.debug("Found a predefined cron expression with label equals to [" + valueCheck + "]");
				logger.debug("The cron expression is equals to [" + value.getExpression() + "]");
				return value.getExpression();
			}
		}

		logger.debug("No predefined cron expression found with label equals to [" + valueCheck + "]. Returning null.");
		return null;
	}
}
