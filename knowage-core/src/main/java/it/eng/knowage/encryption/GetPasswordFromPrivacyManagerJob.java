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

import static it.eng.knowage.encryption.EncryptionPreferencesRegistry.DEFAULT_CFG_KEY;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import privacymanager.wrapper.IPrivacyManagerAPI;
import privacymanager.wrapper.PrivacyManagerAPIBuilder;

/**
 * @author Marco Libanori
 */
public class GetPasswordFromPrivacyManagerJob implements Job {

	private static final Logger LOGGER = LogManager.getLogger(GetPasswordFromPrivacyManagerJob.class);

	public static final String PARAM_PM_URL = "PARAM_PM_URL";
	public static final String PARAM_PM_USER = "PARAM_PM_USER";
	public static final String PARAM_PM_PWD = "PARAM_PM_PWD";
	public static final String PARAM_PM_APP = "PARAM_PM_APP";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String pmUrl = (String) context.getMergedJobDataMap().get(PARAM_PM_URL);
		String pmUser = (String) context.getMergedJobDataMap().get(PARAM_PM_USER);
		String pmPwd = (String) context.getMergedJobDataMap().get(PARAM_PM_PWD);
		String pmApp = (String) context.getMergedJobDataMap().get(PARAM_PM_APP);

		try {
			LOGGER.info("Starting Privacy Manager password refresh job for application {} on {}", pmApp, pmUrl);
			IPrivacyManagerAPI api = PrivacyManagerAPIBuilder.newBuilder()
				.withUrl(pmUrl)
				.withAppId(pmApp)
				.build();

			LOGGER.debug("Requesting authentication token from Privacy Manager for application {}", pmApp);
			api.getToken(pmUser, pmPwd);
			LOGGER.debug("Authentication token retrieved from Privacy Manager for application {}", pmApp);

			LOGGER.debug("Requesting encryption key from Privacy Manager for application {}", pmApp);
			String key = api.retrieveKey();
			LOGGER.info("Encryption key retrieved from Privacy Manager for application {}. Key length: {}", pmApp,
					key != null ? key.length() : null);

			String cfgKey = DEFAULT_CFG_KEY;
			EncryptionConfiguration cfg = EncryptionPreferencesRegistry.getInstance()
				.getConfiguration(cfgKey);
			LOGGER.debug("Updating encryption configuration {} for application {}", cfgKey, pmApp);

			cfg.setEncryptionPwd(key);

			DataEncryptionGlobalCfg decfee = DataEncryptionGlobalCfg.getInstance();
			decfee.setKeyTemplateForAlgorithm(cfgKey, cfg.getAlgorithm());
			decfee.setKeyTemplateForPassword(cfgKey, key);
			LOGGER.info("Privacy Manager password refresh job completed for application {}", pmApp);

		} catch (Exception e) {
			LOGGER.error("Privacy Manager password refresh job failed for application {} on {}", pmApp, pmUrl, e);

			JobExecutionException e2 = new JobExecutionException(e);

			// TODO : Do we need some time before refire?
			e2.refireImmediately();

			throw e2;
		}
	}

}
