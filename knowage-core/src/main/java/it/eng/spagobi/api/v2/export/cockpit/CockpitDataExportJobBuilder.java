/**
 *
 */
package it.eng.spagobi.api.v2.export.cockpit;

import java.util.Locale;
import java.util.UUID;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

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
		if (documentExportConf.getDocumentId() == null) {
			throw new IllegalArgumentException("Attribute documentId cannot be null");
		}
		if (userProfile == null) {
			throw new IllegalArgumentException("Attribute userProfile cannot be null");
		}

		String jobDescription = String.format("Export of dataset %d to %s", documentExportConf.getDocumentId(), documentExportConf.getExportType());

		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(CockpitDataExportConstans.DOC_EXP_CONF, documentExportConf);
		jobDataMap.put(CockpitDataExportConstans.LOCALE, getLocale());
		jobDataMap.put(CockpitDataExportConstans.USER_PROFILE, getUserProfile());
		jobDataMap.put(CockpitDataExportConstans.RESOURCE_PATH, resoursePath);
		jobDataMap.put(CockpitDataExportConstans.JOB_ID, randomUUID);

		JobDetail job = new JobDetail("export_" + randomUUID, "export", CockpitDataExportJob.class);
		job.setDescription(jobDescription);
		job.setJobDataMap(jobDataMap);
		job.setDurability(false);

		return job;
	}

}
