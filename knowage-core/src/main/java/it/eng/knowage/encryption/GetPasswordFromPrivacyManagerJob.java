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

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import privacymanager.wrapper.IPrivacyManagerAPI;
import privacymanager.wrapper.PrivacyManagerAPIBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Marco Libanori
 */
public class GetPasswordFromPrivacyManagerJob implements Job {

	public static final String PARAM_PM_URL = "PARAM_PM_URL";
	public static final String PARAM_PM_USER = "PARAM_PM_USER";
	public static final String PARAM_PM_PWD = "PARAM_PM_PWD";
	public static final String PARAM_PM_APP = "PARAM_PM_APP";

	private static final Logger LOGGER = LogManager.getLogger(GetPasswordFromPrivacyManagerJob.class);


	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String pmUrl = (String) context.getMergedJobDataMap().get(PARAM_PM_URL);
		String pmUser = (String) context.getMergedJobDataMap().get(PARAM_PM_USER);
		String pmPwd = (String) context.getMergedJobDataMap().get(PARAM_PM_PWD);
		String pmApp = (String) context.getMergedJobDataMap().get(PARAM_PM_APP);

		try {
			LOGGER.info("Starting Privacy Manager password refresh job for application " + pmApp + " on " + pmUrl);
			IPrivacyManagerAPI api = PrivacyManagerAPIBuilder.newBuilder()
				.withUrl(pmUrl)
				.withAppId(pmApp)
				.build();

			LOGGER.info("Requesting authentication token from Privacy Manager for application " + pmApp);
			api.getToken(pmUser, pmPwd);

			String key = api.retrieveKey();

			String cfgKey = DEFAULT_CFG_KEY;
			EncryptionConfiguration cfg = EncryptionPreferencesRegistry.getInstance()
				.getConfiguration(cfgKey);

			cfg.setEncryptionPwd(key);

			DataEncryptionGlobalCfg decfee = DataEncryptionGlobalCfg.getInstance();
			decfee.setKeyTemplateForAlgorithm(cfgKey, cfg.getAlgorithm());
			decfee.setKeyTemplateForPassword(cfgKey, key);

		} catch (Exception e) {

			LOGGER.error("Privacy Manager password retrieval job failed", e);

			JobExecutionException e2 = new JobExecutionException(e);

			// TODO : Do we need some time before refire?
			e2.refireImmediately();

			throw e2;
		}
	}

}
