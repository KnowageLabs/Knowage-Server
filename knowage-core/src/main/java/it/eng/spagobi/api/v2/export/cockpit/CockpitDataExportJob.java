/**
 *
 */
package it.eng.spagobi.api.v2.export.cockpit;

import java.util.Locale;
import java.util.UUID;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.knowage.document.export.cockpit.CockpitDataExporterBuilder;
import it.eng.knowage.document.export.cockpit.ICockpitDataExporter;
import it.eng.spagobi.commons.bo.UserProfile;

/**
 * @author Dragan Pirkovic
 *
 */
public class CockpitDataExportJob implements Job {

	private DocumentExportConf documentExportConf;
	private Locale locale;
	private String resourcePath;
	private UserProfile userProfile;
	private UUID id;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		init(context.getMergedJobDataMap());

		ICockpitDataExporter cockpitDataExporter = new CockpitDataExporterBuilder().setDocumentId(documentExportConf.getDocumentId())
				.setDocumentLabel(documentExportConf.getDocumentLabel()).setDocumentParameters(documentExportConf.getParameters())
				.setType(documentExportConf.getExportType()).setLocale(locale).setResourcePath(resourcePath).setUserProfile(userProfile).build();

		cockpitDataExporter.export();

	}

	/**
	 * @param mergedJobDataMap
	 */
	private void init(JobDataMap mergedJobDataMap) {
		documentExportConf = (DocumentExportConf) mergedJobDataMap.get(CockpitDataExportConstans.DOC_EXP_CONF);
		locale = (Locale) mergedJobDataMap.get(CockpitDataExportConstans.LOCALE);
		resourcePath = (String) mergedJobDataMap.get(CockpitDataExportConstans.RESOURCE_PATH);
		userProfile = (UserProfile) mergedJobDataMap.get(CockpitDataExportConstans.USER_PROFILE);
		id = (UUID) mergedJobDataMap.get(CockpitDataExportConstans.JOB_ID);

	}

}
