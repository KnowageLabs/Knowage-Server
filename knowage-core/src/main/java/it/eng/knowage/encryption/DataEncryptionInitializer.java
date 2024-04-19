/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.encryption;

import static it.eng.knowage.encryption.EncryptionConfigurationType.GENERIC;
import static it.eng.knowage.encryption.EncryptionConfigurationType.PRIVACY_MANAGER;
import static it.eng.knowage.encryption.EncryptionPreferencesRegistry.DEFAULT_CFG_KEY;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static org.apache.commons.lang3.ObjectUtils.anyNull;

import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.init.InitializerIFace;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;

/**
 * @author Marco Libanori
 */
public class DataEncryptionInitializer implements InitializerIFace {

	private static final String TENANT = "DEFAULT_TENANT";

	private static final Logger LOGGER = LogManager.getLogger(DataEncryptionInitializer.class);

	public static final String DEFAULT_JOB_GROUP = "Encryption";
	public static final String DEFAULT_JOB_NAME = "PrivacyManagerQueryJob";
	public static final String DEFAULT_JOB_DESC = "Query the Privacy Manager";

	public static final String DEFAULT_TRIGGER_GROUP = "Encryption";
	public static final String DEFAULT_TRIGGER_NAME = "PrivacyManagerQueryJob";

	// Privacy Manager
	private static final String PROPERTY_PM_URL = "pm.url";
	private static final String PROPERTY_PM_USER = "pm.user";
	private static final String PROPERTY_PM_PWD = "pm.password";
	private static final String PROPERTY_PM_APPLICATION = "pm.application";
	private static final String PROPERTY_PM_ALGO = "pm.algorithm";

	// Generic
	private static final String PROPERTY_GENERIC_ALGO = "encryption.algorithm";
	private static final String PROPERTY_GENERIC_PWD = "encryption.password";

	@Override
	public void init(SourceBean config) {

		try {
			String pmUrl = getValue(PROPERTY_PM_URL);
			String pmUser = getValue(PROPERTY_PM_USER);
			String pmPwd = getValue(PROPERTY_PM_PWD);
			String pmApp = getValue(PROPERTY_PM_APPLICATION);
			String pmAlgo = getValue(PROPERTY_PM_ALGO);

			String genAlgo = getValue(PROPERTY_GENERIC_ALGO);
			String genPwd = getValue(PROPERTY_GENERIC_PWD);

			LOGGER.warn("Reading encryption configuration: the system properties will take precedence over environment variables");

			if (ObjectUtils.anyNotNull(pmUrl, pmUser, pmPwd, pmApp, pmAlgo, genAlgo, genPwd)) {
				LOGGER.warn("Found some encryption configuration");

				String cfgKey = DEFAULT_CFG_KEY;
				EncryptionConfiguration cfg = null;

				if (anyNull(pmUrl, pmUser, pmPwd, pmApp, pmAlgo)) {
					LOGGER.error(
							"Failing to read Privacy Manager configuration from both system properties and system environment: you must provide all the configuration values listed in the documentation.");
					LOGGER.error("Trying with a generic algorithm");

					if (anyNull(genAlgo, genPwd)) {
						LOGGER.error(
								"Failing to read generic encryption algorithm configuration from both system properties and system environment: you must provide all the configuration values listed in the documentation.");
					} else {
						cfg = configureGenericDecryption(genAlgo, genPwd, cfgKey);
					}
				} else {
					cfg = configurePrivacyManager(pmUrl, pmUser, pmPwd, pmApp, pmAlgo);
				}

				if (cfg != null) {
					EncryptionPreferencesRegistry.getInstance().addConfiguration(cfgKey, cfg);

					if (PRIVACY_MANAGER.equals(cfg.getType())) {
						scheduleJobToRetrieveThePassword(cfg);
					}
				} else {
					LOGGER.warn("No decryption configuration set");
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Error initializing data encryption", e);
		}

	}

	@Override
	public SourceBean getConfig() {
		return null;
	}

	private void scheduleJobToRetrieveThePassword(EncryptionConfiguration cfg) {
		ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();

		// The following job is cross tenant but we need to set this anyway
		schedulerDAO.setTenant(TENANT);

		deleteJobForPrivacyManager(schedulerDAO);
		Job jobDetail = createJobForPrivacyManager(schedulerDAO, cfg);

		deleteTriggerForPrivacyManager(schedulerDAO);
		createTriggerForPrivacyManager(schedulerDAO, jobDetail);
	}

	private void createTriggerForPrivacyManager(ISchedulerDAO schedulerDAO, Job jobDetail) {
		LOGGER.debug("Adding trigger with name {} in group {}", DEFAULT_TRIGGER_NAME, DEFAULT_TRIGGER_GROUP);
		Trigger simpleTrigger = new Trigger();
		simpleTrigger.setName(DEFAULT_TRIGGER_NAME);
		simpleTrigger.setGroupName(DEFAULT_TRIGGER_GROUP);
		simpleTrigger.setJob(jobDetail);
		simpleTrigger.setRunImmediately(true);

		schedulerDAO.insertTrigger(simpleTrigger);
		LOGGER.debug("Added trigger with name {} in group {}", DEFAULT_TRIGGER_NAME, DEFAULT_TRIGGER_GROUP);
	}

	private void deleteTriggerForPrivacyManager(ISchedulerDAO schedulerDAO) {
		LOGGER.debug("Deleting trigger with name {} in group {}", DEFAULT_TRIGGER_NAME, DEFAULT_TRIGGER_GROUP);
		schedulerDAO.deleteTrigger(DEFAULT_TRIGGER_NAME, DEFAULT_TRIGGER_GROUP);
		LOGGER.debug("Deleted trigger with name {} in group {}", DEFAULT_TRIGGER_NAME, DEFAULT_TRIGGER_GROUP);
	}

	private void deleteJobForPrivacyManager(ISchedulerDAO schedulerDAO) {
		LOGGER.debug("Deleting job with name {} in group {}", DEFAULT_JOB_NAME, DEFAULT_JOB_GROUP);
		schedulerDAO.deleteJob(DEFAULT_JOB_NAME, DEFAULT_JOB_GROUP);
		LOGGER.debug("Deleted job with name {} in group {}", DEFAULT_JOB_NAME, DEFAULT_JOB_GROUP);
	}

	private Job createJobForPrivacyManager(ISchedulerDAO schedulerDAO, EncryptionConfiguration cfg) {
		LOGGER.debug("Adding job with name {} in group {}", DEFAULT_JOB_NAME, DEFAULT_JOB_GROUP);
		Job jobDetail = null;

		String pmUrl = cfg.getPmUrl();
		String pmUser = cfg.getPmUser();
		String pmApplication = cfg.getPmApplication();
		String pmPwd = cfg.getPmPwd();

		jobDetail = new Job();
		jobDetail.setName(DEFAULT_JOB_NAME);
		jobDetail.setGroupName(DEFAULT_JOB_GROUP);
		jobDetail.setDescription(String.format(DEFAULT_JOB_DESC, pmUrl, pmUser, pmApplication));
		jobDetail.setDurable(true);
		jobDetail.setVolatile(false);
		jobDetail.setRequestsRecovery(true);
		jobDetail.setJobClass(GetPasswordFromPrivacyManagerJob.class);
		jobDetail.addParameter(GetPasswordFromPrivacyManagerJob.PARAM_PM_URL, pmUrl);
		jobDetail.addParameter(GetPasswordFromPrivacyManagerJob.PARAM_PM_USER, pmUser);
		jobDetail.addParameter(GetPasswordFromPrivacyManagerJob.PARAM_PM_PWD, pmPwd);
		jobDetail.addParameter(GetPasswordFromPrivacyManagerJob.PARAM_PM_APP, pmApplication);

		schedulerDAO.insertJob(jobDetail);
		LOGGER.debug("Created job with name {} in group {}", DEFAULT_JOB_NAME, DEFAULT_JOB_GROUP);
		return jobDetail;
	}

	private String getValue(String key) {
		String ret = null;

		ret = Optional.ofNullable(getProperty(key)).orElse(getenv(key));

		return ret;
	}

	private EncryptionConfiguration configureGenericDecryption(String genAlgo, String genPwd, String cfgKey) {
		EncryptionConfiguration cfg;
		cfg = new EncryptionConfiguration(GENERIC);

		cfg.setEncryptionPwd(genPwd);

		cfg.setAlgorithm(genAlgo);

		try {
			DataEncryptionGlobalCfg decfee = DataEncryptionGlobalCfg.getInstance();
			decfee.setKeyTemplateForAlgorithm(cfgKey, genAlgo);
			decfee.setKeyTemplateForPassword(cfgKey, genPwd);
		} catch (Exception e) {
			LOGGER.warn("Error initializing data encryption for external engines", e);
		}

		LOGGER.warn("Generic encryption algorithm configuration created");
		return cfg;
	}

	private EncryptionConfiguration configurePrivacyManager(String pmUrl, String pmUser, String pmPwd, String pmApp, String pmAlgo) {
		EncryptionConfiguration cfg;
		cfg = new EncryptionConfiguration(PRIVACY_MANAGER);

		cfg.setPmUrl(pmUrl);
		cfg.setPmUser(pmUser);
		cfg.setPmPwd(pmPwd);
		cfg.setPmApplication(pmApp);

		cfg.setAlgorithm(pmAlgo);

		LOGGER.warn("Privacy Manager configuration created");
		return cfg;
	}

}
