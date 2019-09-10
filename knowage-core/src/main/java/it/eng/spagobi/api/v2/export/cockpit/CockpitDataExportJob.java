/**
 *
 */
package it.eng.spagobi.api.v2.export.cockpit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.knowage.document.export.cockpit.CockpitDataExporterBuilder;
import it.eng.knowage.document.export.cockpit.ICockpitDataExporter;
import it.eng.spagobi.api.v2.export.ExportMetadata;
import it.eng.spagobi.api.v2.export.ExportPathBuilder;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;

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
		ExportPathBuilder pathbuilder = ExportPathBuilder.getInstance();
		Path path = pathbuilder.getPerJobExportPath(resourcePath, userProfile, id);

		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			String msg = String.format("Error creating directory \"%s\"!", resourcePath);

			throw new JobExecutionException(e);
		}

		ICockpitDataExporter cockpitDataExporter = new CockpitDataExporterBuilder().setDocumentId(documentExportConf.getDocumentId())
				.setDocumentLabel(documentExportConf.getDocumentLabel()).setDocumentParameters(documentExportConf.getParameters())
				.setType(documentExportConf.getExportType()).setLocale(locale).setResourcePath(path.toString()).setUserProfile(userProfile).build();

		cockpitDataExporter.export();

		java.nio.file.Path metadataFile = ExportPathBuilder.getInstance().getPerJobIdMetadataFile(resourcePath, userProfile, id);

		try {
			String docLabel = documentExportConf.getDocumentLabel();

			ExportMetadata exportMetadata = new ExportMetadata();
			exportMetadata.setId(id);
			exportMetadata.setDataSetName(docLabel);
			exportMetadata.setFileName(docLabel + "." + extension());
			exportMetadata.setMimeType(mime());
			exportMetadata.setStartDate(Calendar.getInstance(locale).getTime());

			ExportMetadata.writeToJsonFile(exportMetadata, metadataFile);
		} catch (Exception e) {

			// deleteJobDirectory();

			String msg = String.format("Error creating file \"%s\"!", metadataFile);

			throw new JobExecutionException(e);
		}

	}

	private String extension() {
		return "zip";
	}

	private String mime() {
		// TODO Auto-generated method stub
		return "application/zip";
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
		initializeTenant();

	}

	private void initializeTenant() {
		String organization = userProfile.getOrganization();
		Tenant tenant = new Tenant(organization);
		TenantManager.setTenant(tenant);
	}

}
