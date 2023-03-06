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
package it.eng.spagobi.api.v2.export;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.qbe.dataset.DerivedDataSet;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

abstract class AbstractExportJob implements Job {

	private static final Logger logger = Logger.getLogger(AbstractExportJob.class);

	public static final String MAP_KEY_DATA_SET_ID = "dataSetId";

	public static final String MAP_KEY_DRIVERS = "drivers";

	public static final String MAP_KEY_ID = "id";

	public static final String MAP_KEY_LOCALE = "locale";

	public static final String MAP_KEY_PARAMETERS = "parameters";

	public static final String MAP_KEY_RESOURCE_PATH = "resourcePath";

	public static final String MAP_KEY_USER_PROFILE = "userProfile";

	public static final String MAP_KEY_USER_ID = "userId";

	private Path dataFile = null;
	private OutputStream dataOutputStream = null;
	private IDataSet dataSet = null;
	private Integer dataSetId = null;
	private Map<String, Object> drivers = null;
	private UUID id = null;
	private Locale locale = null;
	private Map<String, String> parameters = null;
	Path resourcePath = null;
	String resourcePathAsStr = null;
	UserProfile userProfile = null;
	String userId = null;

	/**
	 * Internal cleanup in case of error.
	 */
	protected void deleteJobDirectory() {
		try {
			FileUtils.deleteDirectory(resourcePath.toFile());
		} catch (IOException e) {
			// Yes, it's mute!
		}
	}

	@Override
	public final void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap mergedJobDataMap = context.getMergedJobDataMap();

		dataSetId = getDataSetId(mergedJobDataMap);
		drivers = getDriversData(mergedJobDataMap);
		id = getJobId(mergedJobDataMap);
		locale = getLocale(mergedJobDataMap);
		parameters = getParametersData(mergedJobDataMap);
		resourcePathAsStr = getResourcePathString(mergedJobDataMap);
		userProfile = getUserProfile(mergedJobDataMap);
		userId = getUserId(mergedJobDataMap);

		initializeTenant();

		dataSet = getDataSet(dataSetId, drivers, parameters, userProfile);

		resourcePath = ExportPathBuilder.getInstance().getPerJobExportPath(resourcePathAsStr, userProfile, id);

		try {
			Files.createDirectories(resourcePath);
		} catch (IOException e) {
			String msg = String.format("Error creating directory \"%s\"!", resourcePath);
			logger.error(msg, e);
			throw new JobExecutionException(e);
		}

		dataFile = ExportPathBuilder.getInstance().getPerJobIdDataFile(resourcePathAsStr, userProfile, id);

		try {
			dataOutputStream = Files.newOutputStream(dataFile);
		} catch (IOException e) {

			deleteJobDirectory();

			String msg = String.format("Error creating file \"%s\"!", dataFile);
			logger.error(msg, e);
			throw new JobExecutionException(e);
		}

		export(context);
		Path metadataFile = ExportPathBuilder.getInstance().getPerJobIdMetadataFile(resourcePathAsStr, userProfile, id);

		try {
			String dataSetName = dataSet.getName();

			ExportMetadata exportMetadata = new ExportMetadata();
			exportMetadata.setId(id);
			exportMetadata.setDataSetName(dataSetName);
			exportMetadata.setFileName(dataSetName + "." + extension());
			exportMetadata.setMimeType(mime());
			exportMetadata.setStartDate(Calendar.getInstance(getLocale()).getTime());

			ExportMetadata.writeToJsonFile(exportMetadata, metadataFile);

		} catch (Exception e) {

			deleteJobDirectory();

			String msg = String.format("Error creating file \"%s\"!", metadataFile);
			logger.error(msg, e);
			throw new JobExecutionException(e);
		}

	}

	/**
	 * @return The MIME type of generated file.
	 */
	protected abstract String extension();

	public Path getDataFile() {
		return dataFile;
	}

	protected OutputStream getDataOutputStream() {
		return dataOutputStream;
	}

	protected final IDataSet getDataSet() {
		return dataSet;
	}

	private final IDataSet getDataSet(Integer dataSetId, Map<String, Object> drivers, Map<String, String> parameters, UserProfile userProfile)
			throws JobExecutionException {
		IDataSetDAO dsDAO = DAOFactory.getDataSetDAO();
		dsDAO.setUserProfile(userProfile);
		IDataSet dataSet = dsDAO.loadDataSetById(dataSetId);
		if (dataSet instanceof VersionedDataSet) {
			VersionedDataSet vds = (VersionedDataSet) dataSet;
			if (vds.getWrappedDataset() instanceof DerivedDataSet) {
				IDataSet sourcedataSet = this.getDerivedSourceDataset(dataSet);
				String jsonQuery = this.getJSonQueryDataset(dataSet);
				if (sourcedataSet != null) {
					DerivedDataSet dataSetDer = (DerivedDataSet) vds.getWrappedDataset();
					dataSetDer.setSourceDataset(sourcedataSet);
					dataSetDer.setJsonQuery(jsonQuery);
					dataSetDer
							.setDataSource(dataSetDer.getDataSourceForReading() != null ? dataSetDer.getDataSourceForReading() : sourcedataSet.getDataSource());
					dataSetDer.setDataSourceForReading(
							dataSetDer.getDataSourceForReading() != null ? dataSetDer.getDataSourceForReading() : sourcedataSet.getDataSource());

					if (dataSetDer.isPersisted()) { // Current export jobs don't use persistence tables when exporting, we use the same way for derived...
						dataSetDer.setPersisted(false);
						dataSetDer.setDataSource(sourcedataSet.getDataSource());
						dataSetDer.setDataSourceForReading(sourcedataSet.getDataSource());
					}
					dataSet = dataSetDer;
				}
			}
		}

		logger.debug("Dump drivers:");
		for (Entry<String, Object> entry : drivers.entrySet()) {
			String msg = String.format("\t%s: %s", entry.getKey(), entry.getValue());
			logger.debug(msg);
		}

		logger.debug("Dump parameters:");
		for (java.util.Map.Entry<String, String> entry : parameters.entrySet()) {
			String msg = String.format("\t%s: %s", entry.getKey(), entry.getValue());
			logger.debug(msg);
		}

		dataSet.setDrivers(drivers);
		try {
			dataSet.setParametersMap(parameters);
		} catch (JSONException e) {
			throw new JobExecutionException("An error occurred when applying parameters into dataset", e);
		}
		dataSet.resolveParameters();

		dataSet.setUserProfileAttributes(userProfile.getUserAttributes());
		return dataSet;
	}

	protected Integer getDataSetId() {
		return dataSetId;
	}

	protected final Integer getDataSetId(JobDataMap mergedJobDataMap) {
		return (Integer) mergedJobDataMap.get(MAP_KEY_DATA_SET_ID);
	}

	protected Map<String, Object> getDrivers() {
		return drivers;
	}

	protected final Map<String, Object> getDriversData(JobDataMap mergedJobDataMap) {
		return (Map<String, Object>) mergedJobDataMap.get(MAP_KEY_DRIVERS);
	}

	protected UUID getId() {
		return id;
	}

	protected final UUID getJobId(JobDataMap mergedJobDataMap) {
		return (UUID) mergedJobDataMap.get(MAP_KEY_ID);
	}

	protected Locale getLocale() {
		return locale;
	}

	protected final Locale getLocale(JobDataMap mergedJobDataMap) {
		return (Locale) mergedJobDataMap.get(MAP_KEY_LOCALE);
	}

	protected Map<String, String> getParameters() {
		return parameters;
	}

	protected final Map<String, String> getParametersData(JobDataMap mergedJobDataMap) {
		return (Map<String, String>) mergedJobDataMap.get(MAP_KEY_PARAMETERS);
	}

	protected String getResourcePathAsStr() {
		return resourcePathAsStr;
	}

	protected final String getResourcePathString(JobDataMap mergedJobDataMap) {
		return (String) mergedJobDataMap.get(MAP_KEY_RESOURCE_PATH);
	}

	protected final String getUserId(JobDataMap mergedJobDataMap) {
		return (String) mergedJobDataMap.get(MAP_KEY_USER_ID);
	}

	protected UserProfile getUserProfile() {
		return userProfile;
	}

	protected final UserProfile getUserProfile(JobDataMap mergedJobDataMap) {
		return (UserProfile) mergedJobDataMap.get(MAP_KEY_USER_PROFILE);
	}

	/**
	 * Set tenant in the job thread.
	 */
	private void initializeTenant() {
		String organization = userProfile.getOrganization();
		Tenant tenant = new Tenant(organization);
		TenantManager.setTenant(tenant);
	}

	/**
	 * Call the real export.
	 *
	 * @param context
	 * @throws JobExecutionException
	 */
	protected abstract void export(JobExecutionContext context) throws JobExecutionException;

	/**
	 * @return The MIME type of generated file.
	 */
	protected abstract String mime();

	public IDataSet getDerivedSourceDataset(IDataSet dataset) {
		if (dataset instanceof VersionedDataSet) {
			VersionedDataSet vds = (VersionedDataSet) dataset;
			if (vds.getWrappedDataset() instanceof DerivedDataSet) {
				JSONObject sourceJsonConfig;
				try {
					sourceJsonConfig = new JSONObject(vds.getWrappedDataset().getConfiguration());
					String sourceDatasetLabel = sourceJsonConfig.getString("sourceDatasetLabel");
					return DAOFactory.getDataSetDAO().loadDataSetByLabel(sourceDatasetLabel);
				} catch (JSONException e) {
					throw new SpagoBIRuntimeException("sourceJsonConfig no longer exists for " + vds.getWrappedDataset().getLabel() + " Dataset");
				}

			}
		}
		return dataset;
	}

	public String getJSonQueryDataset(IDataSet dataset) {
		String jsonQuery = null;
		if (dataset instanceof VersionedDataSet) {
			VersionedDataSet vds = (VersionedDataSet) dataset;
			if (vds.getWrappedDataset() instanceof DerivedDataSet) {
				JSONObject sourceJsonConfig;
				try {
					sourceJsonConfig = new JSONObject(vds.getWrappedDataset().getConfiguration());
					jsonQuery = sourceJsonConfig.getString("qbeJSONQuery");
				} catch (JSONException e) {
					throw new SpagoBIRuntimeException("sourceJsonConfig no longer exists for " + vds.getWrappedDataset().getLabel() + " Dataset");
				}

			}
		}
		return jsonQuery;
	}
}
