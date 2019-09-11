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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
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

	private static Logger logger = Logger.getLogger(CockpitDataExportJob.class);
	private DocumentExportConf documentExportConf;
	private Locale locale;
	private String resourcePath;
	private UserProfile userProfile;
	private UUID id;
	private Path path;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("IN");

		logger.debug("Job initialization");
		init(context.getMergedJobDataMap());

		logger.debug("Creating job directories");
		createJobDirectories();

		logger.debug("Creating exporter");
		ICockpitDataExporter cockpitDataExporter = new CockpitDataExporterBuilder().setDocumentId(documentExportConf.getDocumentId())
				.setDocumentLabel(documentExportConf.getDocumentLabel()).setDocumentParameters(documentExportConf.getParameters())
				.setType(documentExportConf.getExportType()).setLocale(locale).setResourcePath(path.toString()).setUserProfile(userProfile)
				.setZipFileName("data").build();
		logger.debug("Exporting");
		cockpitDataExporter.export();
		logger.debug("Exported");

		createMetaFile();
		logger.debug("Meta File created");

		logger.debug("OUT");
	}

	/**
	 * @throws JobExecutionException
	 */
	private void createJobDirectories() throws JobExecutionException {
		path = ExportPathBuilder.getInstance().getPerJobExportPath(resourcePath, userProfile, id);

		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			String msg = String.format("Error creating directory \"%s\"!", resourcePath);

			throw new JobExecutionException(msg, e);
		}
	}

	/**
	 * @throws JobExecutionException
	 */
	private void createMetaFile() throws JobExecutionException {
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

			deleteJobDirectory();

			String msg = String.format("Error creating file \"%s\"!", metadataFile);

			throw new JobExecutionException(msg, e);
		}
	}

	private void deleteJobDirectory() {
		try {
			FileUtils.deleteDirectory(path.toFile());
		} catch (IOException e) {
			// Yes, it's mute!
		}
	}

	private String extension() {
		return "zip";
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

	private String mime() {
		return "application/zip";
	}

}
