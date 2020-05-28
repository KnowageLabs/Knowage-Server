package it.eng.spagobi.tools.scheduler.init;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.init.InitializerIFace;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.jobs.ResourceExportFolderCleaningJob;
import it.eng.spagobi.tools.scheduler.utils.PredefinedCronExpression;

public class ResourceExportFolderSchedulerInitializer implements InitializerIFace {
	public static final String DEFAULT_JOB_NAME = "CleanResourceExportFolderJob";
	public static final String DEFAULT_TRIGGER_NAME = "schedule_resource_export_cleaning";

	public static final String RESOURCE_EXPORT_FOLDER_SCHEDULING_FULL_CLEAN = "SPAGOBI.RESOURCE.EXPORT.FOLDER.SCHEDULING_FULL_CLEAN";

	private String valueCheck = PredefinedCronExpression.DAILY.getLabel();

	private final SourceBean _config = null;
	private transient Logger logger = Logger.getLogger(ResourceExportFolderSchedulerInitializer.class);

	@Override
	public void init(SourceBean config) {
		logger.debug("IN");
		try {
			initCleanForDefaultTenant();
		} catch (Exception e) {
		} finally {
			logger.debug("OUT");
		}

	}

	public void initCleanForDefaultTenant() {

		ISchedulerDAO schedulerDAO = null;
		try {
			logger.debug("IN");
			schedulerDAO = DAOFactory.getSchedulerDAO();
			/* Tenant is mandatory. Set DEFAULT_TENANT but job is for all the tenants */
			schedulerDAO.setTenant("DEFAULT_TENANT");
			Job jobDetail = schedulerDAO.loadJob(DEFAULT_JOB_NAME, DEFAULT_JOB_NAME);
			if (jobDetail == null) {
				// CREATE JOB DETAIL
				jobDetail = new Job();
				jobDetail.setName(DEFAULT_JOB_NAME);
				jobDetail.setGroupName(DEFAULT_JOB_NAME);
				jobDetail.setDescription(DEFAULT_JOB_NAME);
				jobDetail.setDurable(true);
				jobDetail.setVolatile(false);
				jobDetail.setRequestsRecovery(true);
				jobDetail.setJobClass(ResourceExportFolderCleaningJob.class);

				schedulerDAO.insertJob(jobDetail);
				logger.debug("Added job with name " + DEFAULT_JOB_NAME);
			}

			Config configValue = DAOFactory.getSbiConfigDAO().loadConfigParametersByLabel(RESOURCE_EXPORT_FOLDER_SCHEDULING_FULL_CLEAN);

			if (configValue != null && configValue.isActive()) {
				valueCheck = configValue.getValueCheck();
			}

			String cronExpression = getCronExpression(valueCheck);
			schedulerDAO.deleteTrigger(DEFAULT_TRIGGER_NAME, DEFAULT_JOB_NAME);
			if (cronExpression != null) {
				String nameTrig = DEFAULT_TRIGGER_NAME;

				Trigger simpleTrigger = new Trigger();
				simpleTrigger.setName(nameTrig);
				simpleTrigger.setGroupName(DEFAULT_JOB_NAME);
				simpleTrigger.setJob(jobDetail);
				simpleTrigger.getChronExpression().setExpression(cronExpression);
				simpleTrigger.setRunImmediately(false);

				schedulerDAO.insertTrigger(simpleTrigger);
				logger.debug("Added trigger with name " + DEFAULT_TRIGGER_NAME);
			} else {
				logger.debug("The value " + valueCheck
						+ " is not a valid value for schedule RESOURCE EXPORT FOLDER cleaning trigger. Please provide a valid one and restart the Server. PERIODIC RESOURCE EXPORT FOLDER CLEANING DISABLED.");
			}
			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error while initializing scheduler ", e);
		} finally {
			if (schedulerDAO != null) {
				schedulerDAO.setTenant(null);
			}
		}
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

	@Override
	public SourceBean getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

}
