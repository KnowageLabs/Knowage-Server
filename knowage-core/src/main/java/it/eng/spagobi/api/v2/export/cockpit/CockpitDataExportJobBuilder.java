/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v2.export.cockpit;

import static org.quartz.JobBuilder.newJob;

import java.util.Locale;
import java.util.UUID;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * @author Dragan Pirkovic
 *
 */
public class CockpitDataExportJobBuilder {

	private DocumentExportConf documentExportConf;
	private Locale locale;
	private UserProfile userProfile;
	private final String resoursePath = SpagoBIUtilities.getResourcePath();
	private final UUID randomUUID = UUID.randomUUID();

	/**
	 * @param documentExportConf
	 * @return
	 */

	public DocumentExportConf getDocumentExportConf() {
		return documentExportConf;
	}

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @return the userProfile
	 */
	public UserProfile getUserProfile() {
		return userProfile;
	}

	public CockpitDataExportJobBuilder setDocumentExportConf(DocumentExportConf documentExportConf) {
		this.documentExportConf = documentExportConf;
		return this;
	}

	/**
	 * @param locale
	 * @return
	 */
	public CockpitDataExportJobBuilder setLocale(Locale locale) {
		this.locale = locale;
		return this;
	}

	/**
	 * @param profile
	 * @return
	 */
	public CockpitDataExportJobBuilder setUserProfile(UserProfile profile) {
		this.userProfile = profile;
		return this;
	}

	/**
	 * @return
	 */
	public JobDetail build() {
		Assert.assertNotNull(documentExportConf.getDocumentId(), "Attribute documentId cannot be null");
		Assert.assertNotNull(documentExportConf.getDocumentLabel(), "Attribute document label cannot be null");
		Assert.assertNotNull(documentExportConf.getExportType(), "Attribute export type cannot be null");

		Assert.assertNotNull(userProfile, "Attribute userProfile cannot be null");

		String jobDescription = String.format("Export of dataset %d to %s", documentExportConf.getDocumentId(), documentExportConf.getExportType());

		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(CockpitDataExportConstans.DOC_EXP_CONF, documentExportConf);
		jobDataMap.put(CockpitDataExportConstans.LOCALE, getLocale());
		jobDataMap.put(CockpitDataExportConstans.USER_PROFILE, getUserProfile());
		jobDataMap.put(CockpitDataExportConstans.RESOURCE_PATH, resoursePath);
		jobDataMap.put(CockpitDataExportConstans.JOB_ID, randomUUID);

		JobDetail job = newJob().withIdentity("export_" + randomUUID, "export")
			.withDescription(jobDescription)
			.usingJobData(jobDataMap)
			.storeDurably(false)
			.build();

		return job;
	}

}
