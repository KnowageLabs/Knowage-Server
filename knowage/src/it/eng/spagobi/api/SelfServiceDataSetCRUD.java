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
package it.eng.spagobi.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.execution.service.ExecuteAdHocUtility;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.serializer.DataSetJSONSerializer;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.metamodel.MetaModelWrapper;
import it.eng.spagobi.metamodel.SiblingsFileWrapper;
import it.eng.spagobi.rest.annotations.ToValidate;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.tools.dataset.bo.CkanDataSet;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.dataproxy.CkanDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.FileDataProxy;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.dao.DataSetFactory;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogue;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogueSingleton;
import it.eng.spagobi.tools.dataset.normalization.GeoSpatialDimensionDatasetNormalizer;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.dataset.utils.datamart.SpagoBICoreDatamartRetriever;
import it.eng.spagobi.tools.dataset.validation.ErrorField;
import it.eng.spagobi.tools.dataset.validation.GeoDatasetValidatorFactory;
import it.eng.spagobi.tools.dataset.validation.HierarchyLevel;
import it.eng.spagobi.tools.dataset.validation.IDatasetValidator;
import it.eng.spagobi.tools.dataset.validation.IDatasetValidatorFactory;
import it.eng.spagobi.tools.dataset.validation.ValidationErrors;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.notification.AbstractEvent;
import it.eng.spagobi.tools.notification.DatasetNotificationEvent;
import it.eng.spagobi.tools.notification.DatasetNotificationManager;
import it.eng.spagobi.tools.notification.EventConstants;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.json.JSONUtils;

/**
 * @authors Antonella Giachino (antonella.giachino@eng.it) Monica Franceschini (monica.franceschini@eng.it)
 */
@Path("/selfservicedataset")
public class SelfServiceDataSetCRUD {

	static private Logger logger = Logger.getLogger(SelfServiceDataSetCRUD.class);
	static private String deleteNullIdDataSetError = "error.mesage.description.data.set.cannot.be.null";
	static private String deleteInUseDSError = "error.mesage.description.data.set.deleting.inuse";
	static private String canNotFillResponseError = "error.mesage.description.generic.can.not.responce";
	static private String saveDuplicatedDSError = "error.mesage.description.data.set.saving.duplicated";
	static private String parsingDSError = "error.mesage.description.data.set.parsing.error";

	static private String previewRowsConfigLabel = "SPAGOBI.DATASET.PREVIEW_ROWS";

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getAllDataSet(@Context HttpServletRequest req) {
		IDataSetDAO dataSetDao = null;
		List<IDataSet> dataSets = new ArrayList<IDataSet>();
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		String typeDocWizard = (req.getParameter("typeDoc") != null && !"null".equals(req.getParameter("typeDoc"))) ? req.getParameter("typeDoc") : null;
		JSONObject JSONReturn = new JSONObject();
		JSONArray datasetsJSONArray = new JSONArray();
		try {
			List<IDataSet> unfilteredDataSets;
			List<Integer> categories = getCategories(profile);
			dataSetDao = DAOFactory.getDataSetDAO();
			dataSetDao.setUserProfile(profile);

			boolean isTechDsMngr = UserUtilities.isTechDsManager(profile);
			String showOnlyOwner = req.getParameter("showOnlyOwner");
			String showDerivedDatasetsStr = "true";// req.getParameter("showDerivedDataset");
			boolean showDerivedDatasets = showDerivedDatasetsStr != null && showDerivedDatasetsStr.equalsIgnoreCase("true") ? true : false;

			if (!isTechDsMngr) {
				if (showOnlyOwner != null && !showOnlyOwner.equalsIgnoreCase("true")) {
					if (showDerivedDatasets) {
						unfilteredDataSets = dataSetDao.loadDatasetOwnedAndShared(((UserProfile) profile).getUserId().toString());
					} else {
						unfilteredDataSets = dataSetDao.loadNotDerivedDatasetOwnedAndShared(((UserProfile) profile).getUserId().toString());
					}
				} else {
					if (showDerivedDatasets) {
						unfilteredDataSets = dataSetDao.loadUserDataSets(((UserProfile) profile).getUserId().toString());
					} else {
						unfilteredDataSets = dataSetDao.loadNotDerivedUserDataSets(((UserProfile) profile).getUserId().toString());

					}
				}
				dataSets = getFilteredDatasets(unfilteredDataSets, categories);
			}

			datasetsJSONArray = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(dataSets, null);

			JSONArray datasetsJSONReturn = putActions(profile, datasetsJSONArray, typeDocWizard);

			JSONReturn.put("root", datasetsJSONReturn);

		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while instatiating the dao", t);
		}
		return JSONReturn.toString();

	}

	private JSONArray putActions(IEngUserProfile profile, JSONArray datasetsJSONArray, String typeDocWizard) throws JSONException, EMFInternalError {

		Engine wsEngine = null;
		try {
			wsEngine = ExecuteAdHocUtility.getWorksheetEngine();
		} catch (SpagoBIRuntimeException r) {
			// the ws engine is not found
			logger.info("Engine not found. ", r);
		}

		Engine qbeEngine = null;
		try {
			qbeEngine = ExecuteAdHocUtility.getQbeEngine();
		} catch (SpagoBIRuntimeException r) {
			// the qbe engine is not found
			logger.info("Engine not found. ", r);
		}

		Engine geoEngine = null;
		try {
			geoEngine = ExecuteAdHocUtility.getGeoreportEngine();
		} catch (SpagoBIRuntimeException r) {
			// the geo engine is not found
			logger.info("Engine not found. ", r);
		}

		// sets action to modify dataset
		JSONObject detailAction = new JSONObject();
		detailAction.put("name", "detaildataset");
		detailAction.put("description", "Dataset detail");

		JSONObject deleteAction = new JSONObject();
		deleteAction.put("name", "delete");
		deleteAction.put("description", "Delete dataset");

		JSONObject georeportAction = new JSONObject();
		georeportAction.put("name", "georeport");
		georeportAction.put("description", "Show Map");

		JSONObject worksheetAction = new JSONObject();
		worksheetAction.put("name", "worksheet");
		worksheetAction.put("description", "Show Worksheet");

		JSONObject qbeAction = new JSONObject();
		qbeAction.put("name", "qbe");
		qbeAction.put("description", "Show Qbe");

		JSONArray datasetsJSONReturn = new JSONArray();
		for (int i = 0; i < datasetsJSONArray.length(); i++) {
			JSONArray actions = new JSONArray();
			JSONObject datasetJSON = datasetsJSONArray.getJSONObject(i);
			if (typeDocWizard == null) {
				actions.put(detailAction);
				if (((UserProfile) profile).getUserId().toString().equals(datasetJSON.get("owner"))) {
					// the delete action is able only for private dataset
					actions.put(deleteAction);
				}
			}
			boolean isGeoDataset = false;
			// all execution action are added ONLY if the relative engine
			// (getted throught the driver) exists.
			if (geoEngine != null && (typeDocWizard == null || typeDocWizard.equalsIgnoreCase("GEO"))) {
				try {
					String meta = datasetJSON.getString("meta");
					isGeoDataset = ExecuteAdHocUtility.hasGeoHierarchy(meta);
				} catch (Exception e) {
					logger.error("Error during check of Geo spatial column", e);
				}
				if (isGeoDataset)
					actions.put(georeportAction); // Annotated view map action
													// to release SpagoBI 4
			}
			if (wsEngine != null && (typeDocWizard == null || typeDocWizard.equalsIgnoreCase("REPORT"))) {
				actions.put(worksheetAction);
			}

			String dsType = datasetJSON.optString(DataSetConstants.DS_TYPE_CD);
			if (dsType == null || !dsType.equals(DataSetFactory.FEDERATED_DS_TYPE)) {
				if (qbeEngine != null && (typeDocWizard == null || typeDocWizard.equalsIgnoreCase("REPORT"))) {
					if (profile.getFunctionalities().contains(SpagoBIConstants.BUILD_QBE_QUERIES_FUNCTIONALITY)) {
						actions.put(qbeAction);
					}
				}
			}

			datasetJSON.put("actions", actions);
			if (typeDocWizard != null && typeDocWizard.equalsIgnoreCase("GEO")) {
				// if is caming from myAnalysis - create Geo Document - must
				// shows only ds geospatial --> isGeoDataset == true
				if (geoEngine != null && isGeoDataset)
					datasetsJSONReturn.put(datasetJSON);
			} else
				datasetsJSONReturn.put(datasetJSON);
		}
		return datasetsJSONReturn;
	}

	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String deleteDataSet(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		HashMap<String, String> logParam = new HashMap();

		try {
			String id = req.getParameter("id");
			Assert.assertNotNull(id, deleteNullIdDataSetError);
			IDataSetDAO dsDAO = DAOFactory.getDataSetDAO();
			dsDAO.setUserProfile(profile);
			// dsDAO.setUserID(profile.getUserUniqueIdentifier().toString());
			dsDAO.setUserID(((UserProfile) profile).getUserId().toString());
			IDataSet ds = dsDAO.loadDataSetById(new Integer(id));

			// Create DatasetNotificationEvent but wait to notify
			DatasetNotificationEvent datasetEvent = null;
			try {
				datasetEvent = new DatasetNotificationEvent(EventConstants.DATASET_EVENT_DELETED_DATASET, "The dataset has been deleted", ds);
				datasetEvent.retrieveEmailAddressesOfMapAuthors();
			} catch (Exception e) {
				logger.error("Error during creation of Dataset Events", e);
			}

			try {

				// ATTENTION! This Delete Dataset Also if there are documents
				// using it, this could lead to missing link in documents
				dsDAO.deleteDataSetNoChecks(ds.getId());
				deleteDatasetFile(ds);

				// notify that dataset has been deleted
				try {
					DatasetNotificationManager dsNotificationManager = new DatasetNotificationManager();
					if (datasetEvent != null) {
						dsNotificationManager.handleEvent(datasetEvent);
					}
				} catch (Exception e) {
					logger.error("Error during notification of Dataset Events", e);
				}

			} catch (Exception ex) {
				if (ex.getMessage().startsWith("[deleteInUseDSError]")) {
					updateAudit(req, profile, "DATA_SET.DELETE", logParam, "KO");
					throw new SpagoBIRuntimeException(deleteInUseDSError);
				} else {
					throw ex;
				}
			}
			logParam.put("LABEL", ds.getLabel());
			updateAudit(req, profile, "DATA_SET.DELETE", null, "OK");
			return ("{resp:'ok'}");
		} catch (Exception ex) {
			logger.error("Cannot fill response container", ex);
			updateAudit(req, profile, "DATA_SET.DELETE", null, "ERR");
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(canNotFillResponseError, null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		}
	}

	public void deleteDatasetFile(IDataSet dataset) {
		if (dataset instanceof VersionedDataSet) {
			VersionedDataSet versionedDataset = (VersionedDataSet) dataset;
			IDataSet wrappedDataset = versionedDataset.getWrappedDataset();

			if (wrappedDataset instanceof FileDataSet) {
				FileDataSet fileDataset = (FileDataSet) wrappedDataset;
				String resourcePath = fileDataset.getResourcePath();
				String fileName = fileDataset.getFileName();
				String filePath = resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar;
				File datasetFile = new File(filePath + fileName);

				if (datasetFile.exists()) {
					boolean isDeleted = datasetFile.delete();
					if (isDeleted) {
						logger.debug("Dataset File " + fileName + " has been deleted");
					}
				}
			}
		}

	}

	@POST
	@Path("/save")
	@ToValidate(typeName = "dataset")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String saveDataSet(@Context HttpServletRequest request) {
		IEngUserProfile profile = (IEngUserProfile) request.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		try {
			IDataSetDAO dao = DAOFactory.getDataSetDAO();
			dao.setUserProfile(profile);
			String label = request.getParameter("label");
			String meta = request.getParameter(DataSetConstants.METADATA);
			// attributes for persisting dataset
			String persist = request.getParameter("persist");
			String persistTablePrefix = request.getParameter("tablePrefix");
			String persistTableName = request.getParameter("tableName");

			IDataSet ds = dao.loadDataSetByLabel(label);
			IDataSet dsNew = recoverDataSetDetails(request, ds, true);

			logger.debug("Recalculating dataset's metadata: executing the dataset...");
			String dsMetadata = null;
			dsMetadata = getDatasetTestMetadata(dsNew, profile, meta);
			dsNew.setDsMetadata(dsMetadata);
			LogMF.debug(logger, "Dataset executed, metadata are [{0}]", dsMetadata);

			HashMap<String, String> logParam = new HashMap();
			logParam.put("LABEL", dsNew.getLabel());

			// Perform dataset normalization
			IDataSet normalizedDataset = normalizeDataset(dsNew, meta);
			if (normalizedDataset != null) {
				dsNew = normalizedDataset;
			}

			Integer newId = -1;
			if (dsNew.getId() == -1) {
				// if a ds with the same label not exists on db ok else error
				if (DAOFactory.getDataSetDAO().loadDataSetByLabel(dsNew.getLabel()) != null) {
					updateAudit(request, profile, "DATA_SET.ADD", logParam, "KO");
					throw new SpagoBIRuntimeException(saveDuplicatedDSError);
				}
				newId = dao.insertDataSet(dsNew);
				updateAudit(request, profile, "DATA_SET.ADD", logParam, "OK");
			} else {
				// update ds
				dao.modifyDataSet(dsNew);

				// Notifications Management -----------------------------------
				notificationManagement(request, ds, dsNew);

				updateAudit(request, profile, "DATA_SET.MODIFY", logParam, "OK");
			}

			// Dataset persistence
			// Manage persistence of dataset if required. On modify it will drop
			// and create the destination table!
			if ((persist != null) && (persist.equalsIgnoreCase("true"))) {
				logger.debug("Start persistence...");
				// gets the dataset object informations
				IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetByLabel(dsNew.getLabel());
				dataset.setPersisted(true);
				if ((persistTableName != null) && (persistTableName.length() > 0)) {
					// use specified name
					dataset.setPersistTableName(persistTablePrefix.toUpperCase() + persistTableName.toUpperCase());
				} else {
					// otherwise use dataset name as table name
					String name = request.getParameter("name");
					dataset.setPersistTableName(name);
				}

				// checkQbeDataset(((VersionedDataSet) dataset).getWrappedDataset());
				checkFileDataset(((VersionedDataSet) dataset).getWrappedDataset());

				PersistedTableManager ptm = new PersistedTableManager(profile);
				ptm.persistDataSet(dataset);
				logger.debug("Persistence ended succesfully!");
			}

			return ("{id:" + newId + " }");
		} catch (SpagoBIRuntimeException ex) {
			logger.error("Cannot fill response container", ex);
			updateAudit(request, profile, "DATA_SET.SAVE", null, "ERR");
			logger.debug(ex.getMessage());
			try {
				return (ExceptionUtilities.serializeException(ex.getMessage(), null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		} catch (Exception ex) {
			logger.error("Cannot fill response container", ex);
			updateAudit(request, profile, "DATA_SET.SAVE", null, "ERR");
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(canNotFillResponseError, null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		}
	}

	/*
	 * Change the scope of the dataset. If the dataset is private change it to public (SHARE) If the dataset is public change it to private (UNSHARE)
	 */
	@POST
	@Path("/share")
	@Produces(MediaType.APPLICATION_JSON)
	public String shareDataSet(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		try {
			IDataSetDAO dao = DAOFactory.getDataSetDAO();
			dao.setUserProfile(profile);
			int id = Integer.valueOf(req.getParameter("id"));

			IDataSet ds = dao.loadDataSetById(id);

			HashMap<String, String> logParam = new HashMap();

			logParam.put("LABEL", ds.getLabel());
			if (ds.isPublic()) {
				// If dataset is shared change it to unshared (from public to
				// private)
				ds.setPublic(false);
			} else {
				// Share dataset (from private to public)
				ds.setPublic(true);
			}
			String type = getDatasetTypeName(ds.getDsType());
			ds.setDsType(type);

			dao.modifyDataSet(ds);

			// Notifications Management -----------------------------------
			// notificationManagement(req,ds,ds);

			updateAudit(req, profile, "DATA_SET.MODIFY", logParam, "OK");

			int newId = ds.getId();

			return ("{\"id\":" + newId + ", \"isPublic\":" + ds.isPublic() + " }");
		} catch (SpagoBIRuntimeException ex) {
			logger.error("Cannot fill response container", ex);
			updateAudit(req, profile, "DATA_SET.SHARE", null, "ERR");
			logger.debug(ex.getMessage());
			try {
				return (ExceptionUtilities.serializeException(ex.getMessage(), null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		} catch (Exception ex) {
			logger.error("Cannot fill response container", ex);
			updateAudit(req, profile, "DATA_SET.SHARE", null, "ERR");
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(canNotFillResponseError, null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		}
	}

	// Modifiy the original file associated to the dataset adding a column with
	// correct values to use for geo hierarchy
	// then set this column as the hierarchy level column inside the dataset
	// metadata
	private IDataSet normalizeDataset(IDataSet dataSet, String datasetMetadata) {
		try {

			// Check if the .sibling file is present (required for
			// normalization)
			MeasureCatalogue measureCatalogue = MeasureCatalogueSingleton.getMeasureCatologue();
			if (measureCatalogue.isValid()) {
				MetaModelWrapper metamodelWrapper = measureCatalogue.getMetamodelWrapper();
				SiblingsFileWrapper siblingsFile = metamodelWrapper.getSiblingsFileWrapper();
				if (siblingsFile != null) {
					// Siblings file found, proceed with the validation of the
					// dataset
					dataSet.loadData(0, 10, GeneralUtilities.getDatasetMaxResults());
					IDataStore dataStore = dataSet.getDataStore();

					if (datasetMetadata != null) {
						// validation of columns with specified Hierarchies and
						// with numeric Type
						Map<String, HierarchyLevel> hierarchiesColumnsToCheck = getHierarchiesColumnsToCheck(datasetMetadata);

						GeoSpatialDimensionDatasetNormalizer geoDatasetNormalizer = new GeoSpatialDimensionDatasetNormalizer();
						IDataSet normalizedDataset = geoDatasetNormalizer.normalizeDataset(dataSet, hierarchiesColumnsToCheck);
						if (normalizedDataset != null) {
							dataSet = normalizedDataset;
						}

					}
				}
			}

		} catch (IOException ex) {
			logger.error("IOException in normalizeDataset: " + ex);
			logger.debug(ex.getMessage());
		} catch (JSONException ex) {
			logger.error("JSONException in normalizeDataset: " + ex);
			logger.debug(ex.getMessage());
		}

		return dataSet; // could return the original dataSet or a modified
						// version

	}

	private void notificationManagement(HttpServletRequest req, IDataSet currentDataset, IDataSet updatedDataset) {

		try {
			// DatasetNotificationManager dsNotificationManager = new
			// DatasetNotificationManager();
			IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();

			DatasetNotificationManager dsNotificationManager = new DatasetNotificationManager(msgBuilder);
			List<AbstractEvent> datasetEvents = new ArrayList<AbstractEvent>();

			// File change check
			boolean newFileUploaded = false;
			if (req.getParameter("fileUploaded") != null) {
				newFileUploaded = Boolean.valueOf((req.getParameter("fileUploaded")));
			}
			if (newFileUploaded) {
				DatasetNotificationEvent datasetEvent = new DatasetNotificationEvent(EventConstants.DATASET_EVENT_FILE_CHANGED,
						"The dataset has changed his file", updatedDataset);
				datasetEvents.add(datasetEvent);
			}

			// Metadata change check
			boolean metadataChanged = checkMetadataChange(currentDataset, updatedDataset);
			if (metadataChanged) {
				// notify that metadata is changed
				DatasetNotificationEvent datasetEvent = new DatasetNotificationEvent(EventConstants.DATASET_EVENT_METADATA_CHANGED,
						"The dataset has changed his metadata", updatedDataset);
				datasetEvents.add(datasetEvent);
			}

			// Licence change check
			boolean licenceChanged = checkLicenceChange(currentDataset, updatedDataset);
			if (licenceChanged) {
				// notify that license is changed
				DatasetNotificationEvent datasetEvent = new DatasetNotificationEvent(EventConstants.DATASET_EVENT_LICENCE_CHANGED,
						"The dataset has changed his licence", updatedDataset);
				datasetEvents.add(datasetEvent);

			}

			// Name change
			boolean nameChanged = checkNameChange(currentDataset, updatedDataset);
			if (nameChanged) {
				// notify that name is changed
				DatasetNotificationEvent datasetEvent = new DatasetNotificationEvent(EventConstants.DATASET_EVENT_NAME_CHANGED,
						"The dataset has changed his name", updatedDataset);
				datasetEvents.add(datasetEvent);
			}

			// Description change
			boolean descriptionChanged = checkDescriptionChange(currentDataset, updatedDataset);
			if (descriptionChanged) {
				// notify that Description is changed
				DatasetNotificationEvent datasetEvent = new DatasetNotificationEvent(EventConstants.DATASET_EVENT_DESCRIPTION_CHANGED,
						"The dataset has changed his description", updatedDataset);
				datasetEvents.add(datasetEvent);
			}

			boolean categoryChanged = checkCategoryChange(currentDataset, updatedDataset);
			if (categoryChanged) {
				// notify that Category is changed
				DatasetNotificationEvent datasetEvent = new DatasetNotificationEvent(EventConstants.DATASET_EVENT_CATEGORY_CHANGED,
						"The dataset has changed his category", updatedDataset);
				datasetEvents.add(datasetEvent);
			}

			boolean scopeChanged = checkScopeChange(currentDataset, updatedDataset);
			if (scopeChanged) {
				// notify that Scope (Public/Private)
				DatasetNotificationEvent datasetEvent = new DatasetNotificationEvent(EventConstants.DATASET_EVENT_SCOPE_CHANGED,
						"The dataset has changed his scope", updatedDataset);
				datasetEvents.add(datasetEvent);

			}

			// Sending notifications to Manager

			if (!datasetEvents.isEmpty()) {
				if (datasetEvents.size() == 1) {
					dsNotificationManager.handleEvent(datasetEvents.get(0));
				} else {
					dsNotificationManager.handleMultipleEvents(datasetEvents);

				}
			}

		} catch (Exception ex) {
			logger.error("Error during notification of Dataset Events", ex);

		}

	}

	private boolean checkScopeChange(IDataSet currentDataset, IDataSet updatedDataset) throws Exception {
		if ((currentDataset != null) && (updatedDataset != null)) {
			if (currentDataset instanceof VersionedDataSet) {
				currentDataset = ((VersionedDataSet) currentDataset).getWrappedDataset();
			}
			if (currentDataset.isPublic() == updatedDataset.isPublic()) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	private boolean checkCategoryChange(IDataSet currentDataset, IDataSet updatedDataset) throws Exception {
		if ((currentDataset != null) && (updatedDataset != null)) {
			if (currentDataset instanceof VersionedDataSet) {
				currentDataset = ((VersionedDataSet) currentDataset).getWrappedDataset();
			}
			Integer currentDatasetCategory = currentDataset.getCategoryId();
			Integer updatedDatasetCategory = updatedDataset.getCategoryId();
			if (currentDatasetCategory == null) {
				if (updatedDatasetCategory == null) {
					return false;
				} else {
					return true;
				}
			} else {
				if (currentDatasetCategory.intValue() == updatedDatasetCategory.intValue()) {
					return false;
				} else {
					return true;
				}
			}

		}
		return false;
	}

	private boolean checkDescriptionChange(IDataSet currentDataset, IDataSet updatedDataset) throws Exception {
		if ((currentDataset != null) && (updatedDataset != null)) {
			if (currentDataset instanceof VersionedDataSet) {
				currentDataset = ((VersionedDataSet) currentDataset).getWrappedDataset();
			}
			String currentDatasetDescription = currentDataset.getDescription();
			String updatedDatasetDescription = updatedDataset.getDescription();
			if (currentDatasetDescription == null) {
				if (updatedDatasetDescription == null) {
					return false;
				} else {
					return true;
				}
			} else {
				if (currentDatasetDescription.equals(updatedDatasetDescription)) {
					return false;
				} else {
					return true;
				}
			}

		}
		return false;

	}

	private boolean checkNameChange(IDataSet currentDataset, IDataSet updatedDataset) throws Exception {
		if ((currentDataset != null) && (updatedDataset != null)) {
			if (currentDataset instanceof VersionedDataSet) {
				currentDataset = ((VersionedDataSet) currentDataset).getWrappedDataset();
			}
			String currentDatasetName = currentDataset.getName();
			String updatedDatasetName = updatedDataset.getName();

			if (currentDatasetName == null) {
				if (updatedDatasetName == null) {
					return false;
				} else {
					return true;
				}
			} else {
				if (currentDatasetName.equals(updatedDatasetName)) {
					return false;
				} else {
					return true;
				}
			}

		}
		return false;

	}

	private boolean checkMetadataChange(IDataSet currentDataset, IDataSet updatedDataset) throws Exception {
		if ((currentDataset != null) && (updatedDataset != null)) {
			if (currentDataset instanceof VersionedDataSet) {
				currentDataset = ((VersionedDataSet) currentDataset).getWrappedDataset();
			}

			String currentDatasetMetadata = currentDataset.getDsMetadata();
			String updatedDatasetMetadata = updatedDataset.getDsMetadata();

			if (currentDatasetMetadata == null) {
				if (updatedDatasetMetadata == null) {
					return false;
				} else {
					return true;
				}
			} else {
				if (currentDatasetMetadata.equals(updatedDatasetMetadata)) {
					return false;
				} else {
					return true;
				}
			}

		}
		return false;
	}

	private boolean checkLicenceChange(IDataSet currentDataset, IDataSet updatedDataset) throws Exception {

		if ((currentDataset != null) && (updatedDataset != null)) {
			if (currentDataset instanceof VersionedDataSet) {
				currentDataset = ((VersionedDataSet) currentDataset).getWrappedDataset();
			}

			String currentDatasetMetadata = currentDataset.getDsMetadata();
			String updatedDatasetMetadata = updatedDataset.getDsMetadata();

			DatasetMetadataParser dsp = new DatasetMetadataParser();
			IMetaData currentMetadata = dsp.xmlToMetadata(currentDatasetMetadata);
			IMetaData updatedMetadata = dsp.xmlToMetadata(updatedDatasetMetadata);
			String currentLicence = (String) currentMetadata.getProperty("licence");
			String updatedLicence = (String) updatedMetadata.getProperty("licence");
			if (currentLicence != null) {
				if (updatedLicence != null) {
					// check if the licence is changed
					if (!currentLicence.equals(updatedLicence)) {
						return true;
					} else {
						return false;
					}
				}
			} else {
				// no currentLicence, then check if licence is set for the first
				// time
				if ((updatedLicence != null) && (!updatedLicence.isEmpty())) {
					return true;
				}
			}

		}

		return false;

	}

	@POST
	@Path("/testDataSet")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String testDataSet(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		try {
			IDataSetDAO dao = DAOFactory.getDataSetDAO();
			dao.setUserProfile(profile);
			String label = req.getParameter("label");
			String meta = req.getParameter(DataSetConstants.METADATA);

			IDataSet dsToTest = recoverDataSetDetails(req, null, false);

			logger.debug("Recalculating dataset's metadata: executing the dataset...");
			String dsMetadata = null;
			dsMetadata = getDatasetTestMetadata(dsToTest, profile, meta);
			JSONArray datasetColumns = getDatasetColumns(dsToTest, profile);
			dsToTest.setDsMetadata(dsMetadata);
			LogMF.debug(logger, "Dataset executed, metadata are [{0}]", dsMetadata);

			List<IDataSet> dataSets = new ArrayList();
			dataSets.add(dsToTest);

			JSONObject metaJSONobject = DataSetJSONSerializer.serializeGenericMetadata(dsMetadata);
			JSONObject JSONReturn = new JSONObject();
			JSONReturn.put("meta", metaJSONobject);
			JSONReturn.put("datasetColumns", datasetColumns);
			return JSONReturn.toString();
		} catch (SpagoBIRuntimeException ex) {
			logger.error("Cannot fill response container", ex);
			updateAudit(req, profile, "DATA_SET.TEST", null, "ERR");
			logger.debug(ex.getMessage());
			try {
				return (ExceptionUtilities.serializeException(ex.getMessage(), null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		} catch (RuntimeException ex) {
			logger.error("Cannot fill response container", ex);
			updateAudit(req, profile, "DATA_SET.TEST", null, "ERR");
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(parsingDSError, null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		} catch (Exception ex) {
			logger.error("Cannot fill response container", ex);
			updateAudit(req, profile, "DATA_SET.TEST", null, "ERR");
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(canNotFillResponseError, null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		}
	}

	@POST
	@Path("/getDataStore")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDataStore(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		Integer start = new Integer(0);
		Integer limit = new Integer(10);
		Integer resultNumber = null;
		Integer maxSize = null;

		try {
			IDataSetDAO dao = DAOFactory.getDataSetDAO();
			dao.setUserProfile(profile);
			String datasetMetadata = req.getParameter("datasetMetadata");
			IDataSet dataSet;
			// check if configuration for limit dataset preview is active then
			// use it
			IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
			Config previewRowsConfig = configDao.loadConfigParametersByLabel(previewRowsConfigLabel);
			String limitPreview = req.getParameter("limitPreview");
			boolean limitPreviewCheck = false;
			if (limitPreview.equalsIgnoreCase("true")) {
				limitPreviewCheck = true;
			}
			if (limitPreviewCheck && (previewRowsConfig != null) && (previewRowsConfig.isActive())) {
				// use a preview limit
				String previewRowsConfigValue = previewRowsConfig.getValueCheck();
				dataSet = recoverDataSetDetails(req, null, false, true, Integer.valueOf(previewRowsConfigValue));
			} else {
				// no preview limit
				dataSet = recoverDataSetDetails(req, null, false);
			}

			String dsMetadata = getDatasetTestMetadata(dataSet, profile, datasetMetadata);
			dataSet.setDsMetadata(dsMetadata);

			dataSet.loadData(start, limit, GeneralUtilities.getDatasetMaxResults());
			IDataStore dataStore = dataSet.getDataStore();

			resultNumber = (Integer) dataStore.getMetaData().getProperty("resultNumber");

			logger.debug("Total records: " + resultNumber);

			boolean overflow = maxSize != null && resultNumber >= maxSize;
			if (overflow) {
				logger.warn("Query results number [" + resultNumber + "] exceeds max result limit that is [" + maxSize + "]");
			}

			JSONDataWriter dataSetWriter = new JSONDataWriter();
			dataSetWriter.setSetRenderer(true);
			JSONObject gridDataFeed = (JSONObject) dataSetWriter.write(dataStore);
			// remove the recNo inside fields that is not managed by
			// DynamicGridPanel
			JSONObject metadata = gridDataFeed.getJSONObject("metaData");
			if (metadata != null) {
				JSONArray fieldsArray = metadata.getJSONArray("fields");
				boolean elementFound = false;
				int i = 0;
				for (; i < fieldsArray.length(); i++) {
					String element = fieldsArray.getString(i);
					if (element.equals("recNo")) {
						elementFound = true;
						break;
					}
				}
				if (elementFound) {
					fieldsArray.remove(i);
				}

			}

			// Dataset Validation ---------------------------------------------
			if (datasetMetadata != null) {
				ValidationErrors validationErrors = new ValidationErrors();

				// validation of columns with specified Hierarchies and with
				// numeric Type
				Map<String, HierarchyLevel> hierarchiesColumnsToCheck = getHierarchiesColumnsToCheck(datasetMetadata);

				if (!hierarchiesColumnsToCheck.isEmpty()) {
					// We get the category of the dataset and with this we
					// search the appropriate validator
					Integer categoryId = dataSet.getCategoryId();

					if (categoryId != null) {
						IDomainDAO domainDao = DAOFactory.getDomainDAO();
						Domain domain = domainDao.loadDomainById(categoryId);
						String categoryValueName = domain.getValueName();

						// Validate only if there are the proper metadata set
						IDatasetValidatorFactory geoValidatorFactory = new GeoDatasetValidatorFactory();

						IDatasetValidator geoValidator = geoValidatorFactory.getValidator(categoryValueName);

						if (geoValidator != null) {
							MessageBuilder msgBuild = new MessageBuilder();
							Locale locale = msgBuild.getLocale(req);
							geoValidator.setLocale(locale);

							// Validate the dataset and return the fields not
							// valid
							ValidationErrors hierarchiesColumnsValidationErrors = geoValidator.validateDataset(dataStore, hierarchiesColumnsToCheck);
							if (!hierarchiesColumnsValidationErrors.isEmpty()) {
								validationErrors.addAll(hierarchiesColumnsValidationErrors);
							}

						}

					}

				}
				if (!validationErrors.isEmpty()) {
					// this create an array containing the fields with error for
					// each rows
					JSONArray errorsArray = validationErrorsToJSONObject(validationErrors);
					gridDataFeed.put("validationErrors", errorsArray);

				}

			}
			// -----------------------------------------------------------------

			return gridDataFeed.toString();

		} catch (SpagoBIRuntimeException ex) {
			logger.error("Cannot fill response container", ex);
			updateAudit(req, profile, "DATA_SET.GETDATASTORE", null, "ERR");
			logger.debug(ex.getMessage());
			try {
				return (ExceptionUtilities.serializeException(ex.getMessage(), null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		} catch (RuntimeException ex) {
			logger.error("Cannot fill response container", ex);
			updateAudit(req, profile, "DATA_SET.GETDATASTORE", null, "ERR");
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(parsingDSError, null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		} catch (Exception ex) {
			logger.error("Cannot fill response container", ex);
			updateAudit(req, profile, "DATA_SET.GETDATASTORE", null, "ERR");
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(canNotFillResponseError, null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		}
	}

	private Map<String, HierarchyLevel> getHierarchiesColumnsToCheck(String datasetMetadata)
			throws JsonMappingException, JsonParseException, JSONException, IOException {
		JSONObject metadataObject = null;

		Map<String, HierarchyLevel> hierarchiesColumnsToCheck = new HashMap<String, HierarchyLevel>();

		if ((!datasetMetadata.equals("")) && (!datasetMetadata.equals("[]"))) {
			metadataObject = JSONUtils.toJSONObject(datasetMetadata);
			JSONArray columnsMetadataArray = metadataObject.getJSONArray("columns");
			// JSONArray datasetMetadataArray =
			// metadataObject.getJSONArray("dataset");

			for (int j = 0; j < columnsMetadataArray.length(); j++) {
				JSONObject columnJsonObject = columnsMetadataArray.getJSONObject(j);
				String columnName = columnJsonObject.getString("column");
				String propertyName = columnJsonObject.getString("pname");
				String propertyValue = columnJsonObject.getString("pvalue");

				if (propertyName.equals("hierarchy")) {
					HierarchyLevel hierarchyLevel = hierarchiesColumnsToCheck.get(columnName);

					if (hierarchyLevel == null) {
						hierarchyLevel = new HierarchyLevel();
						hierarchyLevel.setHierarchy_name(propertyValue);
						hierarchiesColumnsToCheck.put(columnName, hierarchyLevel);
					} else {
						hierarchyLevel.setHierarchy_name(propertyValue);
						hierarchiesColumnsToCheck.put(columnName, hierarchyLevel);
					}
				}
				if (propertyName.equals("hierarchy_level")) {
					HierarchyLevel hierarchyLevel = hierarchiesColumnsToCheck.get(columnName);

					if (hierarchyLevel == null) {
						hierarchyLevel = new HierarchyLevel();
						hierarchyLevel.setLevel_name(propertyValue);
						hierarchiesColumnsToCheck.put(columnName, hierarchyLevel);
					} else {
						hierarchyLevel.setLevel_name(propertyValue);
						hierarchiesColumnsToCheck.put(columnName, hierarchyLevel);
					}
				}
				if (propertyName.equalsIgnoreCase("Type")) {
					if ((propertyValue.equalsIgnoreCase("Integer")) || (propertyValue.equalsIgnoreCase("Double"))) {
						HierarchyLevel hierarchyLevel = hierarchiesColumnsToCheck.get(columnName);
						if (hierarchyLevel == null) {
							hierarchyLevel = new HierarchyLevel();
							hierarchyLevel.setColumn_type("numeric");
							hierarchiesColumnsToCheck.put(columnName, hierarchyLevel);
						} else {
							hierarchyLevel.setColumn_type("numeric");
							hierarchiesColumnsToCheck.put(columnName, hierarchyLevel);
						}
					}
				}

			}

		}
		return hierarchiesColumnsToCheck;
	}

	public JSONArray validationErrorsToJSONObject(ValidationErrors validationErrors) throws JSONException {

		JSONArray errorsArray = new JSONArray();
		Map<Integer, List<ErrorField>> allErrors = validationErrors.getAllErrors();

		for (Map.Entry<Integer, List<ErrorField>> entry : allErrors.entrySet()) {
			JSONObject rowJSONObject = new JSONObject();
			rowJSONObject.put("id", String.valueOf(entry.getKey()));

			List<ErrorField> rowErrors = entry.getValue();
			for (ErrorField errorColumn : rowErrors) {
				rowJSONObject.put("column_" + errorColumn.getColumnIndex(), errorColumn.getErrorDescription());
			}

			errorsArray.put(rowJSONObject);
		}
		return errorsArray;

	}

	private static void updateAudit(HttpServletRequest request, IEngUserProfile profile, String action_code, HashMap<String, String> parameters, String esito) {
		try {
			AuditLogUtilities.updateAudit(request, profile, action_code, parameters, esito);
		} catch (Exception e) {
			logger.debug("Error writnig audit", e);
		}
	}

	private JSONObject serializeDatasets(List<IDataSet> dataSets) throws SerializationException, JSONException {
		JSONObject dataSetsJSON = new JSONObject();
		JSONArray dataSetsJSONArray = new JSONArray();
		if (dataSets != null) {
			dataSetsJSONArray = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(dataSets, null);
			dataSetsJSON.put("root", dataSetsJSONArray);
		}
		return dataSetsJSON;
	}

	private IDataSet recoverDataSetDetails(HttpServletRequest request, IDataSet dataSet, boolean savingDataset)
			throws EMFUserError, SourceBeanException, IOException {
		return recoverDataSetDetails(request, dataSet, savingDataset, false, -1);
	}

	private IDataSet recoverDataSetDetails(HttpServletRequest request, IDataSet dataSet, boolean savingDataset, boolean checkMaxResults, int maxResults)
			throws EMFUserError, SourceBeanException, IOException {
		boolean insertion = (dataSet == null);
		Integer id = -1;
		String idStr = request.getParameter("id");
		if (idStr != null && !idStr.equals("")) {
			id = new Integer(idStr);
		}
		String type = request.getParameter("type");
		String label = request.getParameter("label");
		String description = request.getParameter("description");
		String name = request.getParameter("name");
		String catTypeVn = request.getParameter("catTypeVn");
		String meta = request.getParameter(DataSetConstants.METADATA);
		String scopeCd = DataSetConstants.DS_SCOPE_USER;
		Boolean isPublic = Boolean.valueOf((request.getParameter("isPublicDS") == null) ? "false" : request.getParameter("isPublicDS"));

		type = getDatasetTypeName(type);
		IDataSet toReturn = null;
		if (type.equals(DataSetConstants.DS_QBE)) {
			toReturn = this.getQbeDataSet(request);
		} else if (type.equals(DataSetConstants.DS_CKAN)) {
			if (checkMaxResults) {
				toReturn = this.getCkanDataSet(request, savingDataset, maxResults);
			} else {
				toReturn = this.getCkanDataSet(request, savingDataset);
			}
		} else {
			if (checkMaxResults) {
				toReturn = this.getFileDataSet(request, savingDataset, maxResults);
			} else {
				toReturn = this.getFileDataSet(request, savingDataset);
			}
		}

		if (!insertion) {
			toReturn.setId(dataSet.getId());
			toReturn.setName(dataSet.getName());
			toReturn.setLabel(dataSet.getLabel());
			toReturn.setDescription(dataSet.getDescription());

			// set detail dataset ID
			toReturn.setTransformerId((dataSet.getTransformerId() == null) ? null : dataSet.getTransformerId());
			toReturn.setPivotColumnName(dataSet.getPivotColumnName());
			toReturn.setPivotRowName(dataSet.getPivotRowName());
			toReturn.setPivotColumnValue(dataSet.getPivotColumnValue());
			toReturn.setNumRows(dataSet.isNumRows());
			toReturn.setParameters(dataSet.getParameters());
			toReturn.setDsMetadata(dataSet.getDsMetadata());

			// set persist values
			toReturn.setPersisted(dataSet.isPersisted());
		}

		// update general informations
		toReturn.setDsType(type);
		toReturn.setDsMetadata(meta);
		toReturn.setId(id.intValue());
		toReturn.setLabel(label);
		toReturn.setName(name);
		toReturn.setDescription(description);

		// always USER scope
		toReturn.setScopeCd(scopeCd);
		Integer scopeId = null;
		try {
			scopeId = Integer.parseInt(scopeCd);
		} catch (Exception e) {
			logger.debug("Scope must be decodified...");
			scopeId = getScopeId(scopeCd);
			logger.debug("Scope Id is : " + scopeId);
		}
		toReturn.setScopeId(scopeId);

		Integer categoryCode = null;
		try {
			categoryCode = Integer.parseInt(catTypeVn);
		} catch (Exception e) {
			logger.debug("Category must be decodified...");
			categoryCode = getCategoryCode(catTypeVn);
			logger.debug("Category value decodified is : " + categoryCode);
		}
		logger.debug("Category code is :  " + categoryCode);
		toReturn.setCategoryId(categoryCode);
		toReturn.setPublic(isPublic);

		return toReturn;
	}

	private IDataSet getQbeDataSet(HttpServletRequest request) {
		QbeDataSet toReturn = new QbeDataSet();
		this.getQbeDataSetConfig(request, toReturn);
		SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
		Map parameters = toReturn.getParamsMap();
		if (parameters == null) {
			parameters = new HashMap();
			toReturn.setParamsMap(parameters);
		}
		toReturn.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
		return toReturn;
	}

	private void getQbeDataSetConfig(HttpServletRequest request, QbeDataSet toReturn) {
		try {
			JSONObject jsonDsConfig = new JSONObject();
			String qbeJSONQuery = request.getParameter(DataSetConstants.QBE_JSON_QUERY);
			String datamarts = request.getParameter(DataSetConstants.QBE_DATAMARTS);
			String dataSourceLabel = request.getParameter(DataSetConstants.QBE_DATA_SOURCE);

			jsonDsConfig.put(DataSetConstants.QBE_JSON_QUERY, qbeJSONQuery);
			jsonDsConfig.put(DataSetConstants.QBE_DATAMARTS, datamarts);
			jsonDsConfig.put(DataSetConstants.QBE_DATA_SOURCE, dataSourceLabel);

			IDataSource dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(dataSourceLabel);

			toReturn.setConfiguration(jsonDsConfig.toString());
			toReturn.setJsonQuery(qbeJSONQuery);
			toReturn.setDatamarts(datamarts);
			toReturn.setDataSource(dataSource);

		} catch (Exception e) {
			logger.error("Error while getting Qbe query details", e);
			throw new SpagoBIRuntimeException("Error while getting Qbe query details", e);
		}
	}

	private IDataSet getFileDataSet(HttpServletRequest request, boolean savingDataset, int maxResults) {
		IDataSet dataSet = this.getFileDataSet(request, savingDataset);
		if (dataSet instanceof FileDataSet) {
			FileDataSet fileDataSet = ((FileDataSet) dataSet);
			fileDataSet.setMaxResults(maxResults);
			FileDataProxy fileDataProxy = fileDataSet.getDataProxy();
			if (fileDataProxy != null) {
				fileDataProxy.setMaxResultsReader(fileDataSet.getMaxResults());
			}
		}
		return dataSet;
	}

	private IDataSet getFileDataSet(HttpServletRequest request, boolean savingDataset) {
		FileDataSet toReturn = new FileDataSet();
		toReturn.setResourcePath(DAOConfig.getResourcePath());
		JSONObject jsonDsConfig = this.getFileDataSetConfig(request, savingDataset);
		toReturn.setConfiguration(jsonDsConfig.toString());

		Integer id = -1;
		String idStr = request.getParameter("id");
		if (idStr != null && !idStr.equals("")) {
			id = new Integer(idStr);
		}
		String label = request.getParameter("label");
		String fileName = request.getParameter("fileName");
		String fileType = request.getParameter("fileType");
		Boolean newFileUploaded = false;
		if (request.getParameter("fileUploaded") != null) {
			newFileUploaded = Boolean.valueOf((request.getParameter("fileUploaded")));
		}

		if (id == -1) {
			// creating a new dataset, the file uploaded has to be renamed and
			// moved
			toReturn.setUseTempFile(true);

			if (savingDataset) {
				// rename and move the file
				String resourcePath = toReturn.getResourcePath();
				renameAndMoveDatasetFile(fileName, label, resourcePath, fileType);
				toReturn.setUseTempFile(false);
			}
		} else {
			// reading or modifying a existing dataset

			if (newFileUploaded) {
				// modifying an existing dataset with a new file uploaded
				toReturn.setUseTempFile(true);

				// saving the existing dataset with a new file associated
				if (savingDataset) {
					// rename and move the file
					String resourcePath = toReturn.getResourcePath();
					renameAndMoveDatasetFile(fileName, label, resourcePath, fileType);
					toReturn.setUseTempFile(false);
				}

			} else {
				// using existing dataset file, file in correct place
				toReturn.setUseTempFile(false);
			}

		}

		// next steps are necessary to define a valid dataProxy
		if (savingDataset) {
			// the file used will have the name equals to dataset's label
			toReturn.setFileName(label + "." + fileType.toLowerCase());
		} else {
			toReturn.setFileName(fileName);
		}

		return toReturn;
	}

	private JSONObject getFileDataSetConfig(HttpServletRequest req, boolean savingDataset) {
		JSONObject jsonDsConfig = new JSONObject();
		try {
			String label = req.getParameter("label");
			String fileName = req.getParameter("fileName");
			String csvDelimiter = req.getParameter("csvDelimiter");
			String csvQuote = req.getParameter("csvQuote");
			String csvEncoding = req.getParameter("csvEncoding");
			String fileType = req.getParameter("fileType");
			String skipRows = req.getParameter("skipRows");
			String limitRows = req.getParameter("limitRows");
			String xslSheetNumber = req.getParameter("xslSheetNumber");
			String scopeCd = DataSetConstants.DS_SCOPE_USER;

			jsonDsConfig.put(DataSetConstants.FILE_TYPE, fileType);
			if (savingDataset) {
				// when saving the dataset the file associated will get the
				// dataset label name
				jsonDsConfig.put(DataSetConstants.FILE_NAME, label + "." + fileType.toLowerCase());
			} else {
				jsonDsConfig.put(DataSetConstants.FILE_NAME, fileName);
			}
			jsonDsConfig.put(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER, csvDelimiter);
			jsonDsConfig.put(DataSetConstants.CSV_FILE_QUOTE_CHARACTER, csvQuote);
			jsonDsConfig.put(DataSetConstants.CSV_FILE_ENCODING, csvEncoding);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_SKIP_ROWS, skipRows);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_LIMIT_ROWS, limitRows);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_SHEET_NUMBER, xslSheetNumber);
			jsonDsConfig.put(DataSetConstants.DS_SCOPE, scopeCd);

		} catch (Exception e) {
			logger.error("Error while defining dataset configuration. Error: " + e.getMessage());
			throw new SpagoBIRuntimeException("Error while defining dataset configuration", e);
		}
		return jsonDsConfig;
	}

	// This method rename a file and move it from resources\dataset\files\temp
	// to resources\dataset\files
	private void renameAndMoveDatasetFile(String originalFileName, String newFileName, String resourcePath, String fileType) {
		String filePath = resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar + "temp" + File.separatorChar;
		String fileNewPath = resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar;

		File originalDatasetFile = new File(filePath + originalFileName);
		File newDatasetFile = new File(fileNewPath + newFileName + "." + fileType.toLowerCase());
		if (originalDatasetFile.exists()) {
			/*
			 * This method copies the contents of the specified source file to the specified destination file. The directory holding the destination file is
			 * created if it does not exist. If the destination file exists, then this method will overwrite it.
			 */
			try {
				FileUtils.copyFile(originalDatasetFile, newDatasetFile);

				// Then delete temp file
				originalDatasetFile.delete();
			} catch (IOException e) {
				logger.debug("Cannot move dataset File");
				throw new SpagoBIRuntimeException("Cannot move dataset File", e);
			}
		}
	}

	private IDataSet getCkanDataSet(HttpServletRequest request, boolean savingDataset, int maxResults) {
		IDataSet dataSet = this.getCkanDataSet(request, savingDataset);
		if (dataSet instanceof CkanDataSet) {
			CkanDataSet ckanDataSet = ((CkanDataSet) dataSet);
			ckanDataSet.setMaxResults(maxResults);
			CkanDataProxy ckanDataProxy = ckanDataSet.getDataProxy();
			if (ckanDataProxy != null) {
				ckanDataProxy.setMaxResultsReader(ckanDataSet.getMaxResults());
			}
		}
		return dataSet;
	}

	private IDataSet getCkanDataSet(HttpServletRequest request, boolean savingDataset) {
		CkanDataSet toReturn = new CkanDataSet();
		JSONObject jsonDsConfig = this.getCkanDataSetConfig(request, savingDataset);
		toReturn.setConfiguration(jsonDsConfig.toString());

		Integer id = -1;
		String idStr = request.getParameter("id");
		if (idStr != null && !idStr.equals("")) {
			id = new Integer(idStr);
		}
		String ckanUrl = request.getParameter("ckanUrl");
		String ckanId = request.getParameter("ckanId");
		toReturn.setCkanId(ckanId);
		toReturn.setCkanUrl(ckanUrl);
		toReturn.setResourcePath(ckanUrl);
		String label = request.getParameter("label");
		String fileName = request.getParameter("fileName");
		String fileType = request.getParameter("fileType");
		Boolean newFileUploaded = false;
		if (request.getParameter("fileUploaded") != null) {
			newFileUploaded = Boolean.valueOf((request.getParameter("fileUploaded")));
		}

		if (id == -1) {

			if (savingDataset) {
				// rename and move the file
				String resourcePath = DAOConfig.getResourcePath();
				deleteDatasetFile(fileName, resourcePath, fileType);
				toReturn.setFileName(label + "." + fileType.toLowerCase());
			}
		} else {
			// reading or modifying a existing dataset
			if (newFileUploaded) {
				String resourcePath = DAOConfig.getResourcePath();
				deleteDatasetFile(fileName, resourcePath, fileType);
				toReturn.setFileName(label + "." + fileType.toLowerCase());
			}
		}
		return toReturn;
	}

	private JSONObject getCkanDataSetConfig(HttpServletRequest req, boolean savingDataset) {
		JSONObject jsonDsConfig = new JSONObject();
		try {
			String label = req.getParameter("label");
			String fileName = req.getParameter("fileName");
			String csvDelimiter = req.getParameter("csvDelimiter");
			String csvQuote = req.getParameter("csvQuote");
			String csvEncoding = req.getParameter("csvEncoding");
			String fileType = req.getParameter("fileType");
			String skipRows = req.getParameter("skipRows");
			String limitRows = req.getParameter("limitRows");
			String xslSheetNumber = req.getParameter("xslSheetNumber");
			String ckanId = req.getParameter("ckanId");
			String ckanUrl = req.getParameter("ckanUrl");
			String scopeCd = DataSetConstants.DS_SCOPE_USER;

			jsonDsConfig.put(DataSetConstants.FILE_TYPE, fileType);
			if (savingDataset) {
				// when saving the dataset the file associated will get the
				// dataset label name
				jsonDsConfig.put(DataSetConstants.FILE_NAME, label + "." + fileType.toLowerCase());
			} else {
				jsonDsConfig.put(DataSetConstants.FILE_NAME, fileName);
			}
			jsonDsConfig.put(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER, csvDelimiter);
			jsonDsConfig.put(DataSetConstants.CSV_FILE_QUOTE_CHARACTER, csvQuote);
			jsonDsConfig.put(DataSetConstants.CSV_FILE_ENCODING, csvEncoding);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_SKIP_ROWS, skipRows);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_LIMIT_ROWS, limitRows);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_SHEET_NUMBER, xslSheetNumber);
			jsonDsConfig.put(DataSetConstants.CKAN_ID, ckanId);
			jsonDsConfig.put(DataSetConstants.CKAN_URL, ckanUrl);
			jsonDsConfig.put(DataSetConstants.DS_SCOPE, scopeCd);

		} catch (Exception e) {
			logger.error("Error while defining dataset configuration. Error: " + e.getMessage());
			throw new SpagoBIRuntimeException("Error while defining dataset configuration", e);
		}
		return jsonDsConfig;
	}

	private void deleteDatasetFile(String fileName, String resourcePath, String fileType) {
		String filePath = resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar + "temp" + File.separatorChar;

		File datasetFile = new File(filePath + fileName);
		if (datasetFile.exists()) {
			datasetFile.delete();
		}
	}

	private String serializeException(Exception e) throws JSONException {
		return ExceptionUtilities.serializeException(e.getMessage(), null);
	}

	private String getDatasetTypeName(String datasetTypeCode) {
		String datasetTypeName = null;

		try {

			if (datasetTypeCode == null)
				return null;
			List<Domain> datasetTypes = null;

			try {
				datasetTypes = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.DATA_SET_TYPE);
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An unexpected error occured while loading dataset types from database", t);
			}

			if (datasetTypes == null) {
				return null;
			}

			for (Domain datasetType : datasetTypes) {
				if (datasetTypeCode.equalsIgnoreCase(datasetType.getValueCd())) {
					datasetTypeName = datasetType.getValueName();
					break;
				}
			}
		} catch (Throwable t) {
			if (t instanceof SpagoBIRuntimeException)
				throw (SpagoBIRuntimeException) t;
			throw new SpagoBIRuntimeException("An unexpected error occured while resolving dataset type name from dataset type code [" + datasetTypeCode + "]");
		}

		return datasetTypeName;
	}

	private Integer getCategoryCode(String category) {
		Integer categoryCode = null;

		try {

			if (category == null)
				return null;
			List<Domain> categories = null;

			try {
				categories = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.CATEGORY_DOMAIN_TYPE);
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An unexpected error occured while loading categories types from database", t);
			}

			if (categories == null) {
				return null;
			}

			for (Domain dmCategory : categories) {
				if (category.equalsIgnoreCase(dmCategory.getValueCd())) {
					categoryCode = dmCategory.getValueId();
					break;
				}
			}
		} catch (Throwable t) {
			if (t instanceof SpagoBIRuntimeException)
				throw (SpagoBIRuntimeException) t;
			throw new SpagoBIRuntimeException("An unexpected error occured while resolving dataset type name from dataset type code [" + category + "]");
		}

		return categoryCode;
	}

	private Integer getScopeId(String scopeCd) {
		Integer scopeId = null;
		try {

			if (scopeCd == null)
				return null;
			List<Domain> scopes = null;

			try {
				scopes = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.DS_SCOPE);
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An unexpected error occured while loading scopes types from database", t);
			}

			if (scopes == null) {
				return null;
			}

			for (Domain dmScope : scopes) {
				if (scopeCd.equalsIgnoreCase(dmScope.getValueCd())) {
					scopeId = dmScope.getValueId();
					break;
				}
			}
		} catch (Throwable t) {
			if (t instanceof SpagoBIRuntimeException)
				throw (SpagoBIRuntimeException) t;
			throw new SpagoBIRuntimeException("An unexpected error occured while resolving scope id name from dataset scope code [" + scopeCd + "]");
		}
		return scopeId;
	}

	private String getDatasetTestMetadata(IDataSet dataSet, IEngUserProfile profile, String metadata) throws Exception {
		logger.debug("IN");
		String dsMetadata = null;

		Integer start = new Integer(0);
		Integer limit = new Integer(10);

		dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));

		try {
			if (dataSet instanceof FileDataSet) {
				FileDataSet fileDataSet = (FileDataSet) dataSet;
				FileDataProxy fileDataProxy = fileDataSet.getDataProxy();
				fileDataProxy.setUseTempFile(fileDataSet.useTempFile); // inform
																		// the
																		// DataProxy
																		// to
																		// use a
																		// tempFile
																		// or
																		// not
			}
			dataSet.loadData(start, limit, GeneralUtilities.getDatasetMaxResults());
			IDataStore dataStore = dataSet.getDataStore();
			DatasetMetadataParser dsp = new DatasetMetadataParser();

			JSONObject metadataObject = new JSONObject();
			JSONArray columnsMetadataArray = new JSONArray();
			JSONArray datasetMetadataArray = new JSONArray();

			if ((!metadata.equals("")) && (!metadata.equals("[]"))) {
				metadataObject = JSONUtils.toJSONObject(metadata);
				columnsMetadataArray = metadataObject.getJSONArray("columns");
				datasetMetadataArray = metadataObject.getJSONArray("dataset");
			}

			IMetaData metaData = dataStore.getMetaData();
			// Setting general custom properties for entire Dataset
			for (int i = 0; i < datasetMetadataArray.length(); i++) {
				JSONObject datasetJsonObject = datasetMetadataArray.getJSONObject(i);
				String propertyName = datasetJsonObject.getString("pname");
				String propertyValue = datasetJsonObject.getString("pvalue");
				metaData.setProperty(propertyName, propertyValue);
			}
			for (int i = 0; i < metaData.getFieldCount(); i++) {
				IFieldMetaData ifmd = metaData.getFieldMeta(i);

				String gussedType = guessColumnType(dataStore, i);

				// Setting mandatory property to defaults, if specified they
				// will be overridden
				if ("Double".equalsIgnoreCase(gussedType)) {
					ifmd.setFieldType(IFieldMetaData.FieldType.MEASURE);
					Class type = Class.forName("java.lang.Double");
					ifmd.setType(type);
				} else {
					ifmd.setFieldType(IFieldMetaData.FieldType.ATTRIBUTE);
					Class type = Class.forName("java.lang.String");
					ifmd.setType(type);
				}

				for (int j = 0; j < columnsMetadataArray.length(); j++) {
					JSONObject columnJsonObject = columnsMetadataArray.getJSONObject(j);
					String columnName = columnJsonObject.getString("column");
					if (ifmd.getName().equals(columnName)) {

						String propertyName = columnJsonObject.getString("pname");
						String propertyValue = columnJsonObject.getString("pvalue");

						// FieldType is a mandatory property
						if (propertyName.equalsIgnoreCase("fieldType")) {
							if (propertyValue.equalsIgnoreCase("MEASURE")) {
								ifmd.setFieldType(IFieldMetaData.FieldType.MEASURE);
							} else if (propertyValue.equalsIgnoreCase("ATTRIBUTE")) {
								ifmd.setFieldType(IFieldMetaData.FieldType.ATTRIBUTE);
							} else {
								if ("Double".equalsIgnoreCase(gussedType)) {
									ifmd.setFieldType(IFieldMetaData.FieldType.MEASURE);
								} else {
									ifmd.setFieldType(IFieldMetaData.FieldType.ATTRIBUTE);
								}
							}
						}
						// Type is a mandatory property
						else if (propertyName.equalsIgnoreCase("Type")) {
							if (propertyValue.equalsIgnoreCase("Integer")) {
								Class type = Class.forName("java.lang.Integer");
								ifmd.setType(type);
							} else if (propertyValue.equalsIgnoreCase("Double")) {
								Class type = Class.forName("java.lang.Double");
								ifmd.setType(type);
							} else if (propertyValue.equalsIgnoreCase("String")) {
								Class type = Class.forName("java.lang.String");
								ifmd.setType(type);
							} else {
								if ("Double".equalsIgnoreCase(gussedType)) {
									Class type = Class.forName("java.lang.Double");
									ifmd.setType(type);
								} else {
									Class type = Class.forName("java.lang.String");
									ifmd.setType(type);
								}

							}
						} else {
							// Custom Properties
							ifmd.setProperty(propertyName, propertyValue);

						}

					}
				}

			}

			dsMetadata = dsp.metadataToXML(dataStore.getMetaData()); // using
																		// new
																		// parser
		} catch (Exception e) {
			logger.error("Error while executing dataset for test purpose", e);
			throw new RuntimeException("Error while executing dataset for test purpose", e);
		}

		logger.debug("OUT");
		return dsMetadata;
	}

	/**
	 * @param dataStore
	 * @param i
	 * @return
	 */
	private String guessColumnType(IDataStore dataStore, int columnIndex) {
		boolean isNumeric = true;
		for (int i = 0; i < Math.min(10, dataStore.getRecordsCount()); i++) {
			IRecord record = dataStore.getRecordAt(i);
			IField field = record.getFieldAt(columnIndex);
			Object value = field.getValue();
			try {
				Double.parseDouble(value.toString());
			} catch (Throwable t) {
				isNumeric = false;
				break;
			}
		}

		return isNumeric ? "Double" : "String";
	}

	public JSONArray getDatasetColumns(IDataSet dataSet, IEngUserProfile profile) throws Exception {
		logger.debug("IN");

		Integer start = new Integer(0);
		Integer limit = new Integer(10);

		JSONArray columnsJSON = new JSONArray();

		try {
			dataSet.loadData(start, limit, GeneralUtilities.getDatasetMaxResults());
			IDataStore dataStore = dataSet.getDataStore();

			IMetaData metaData = dataStore.getMetaData();
			for (int i = 0; i < metaData.getFieldCount(); i++) {
				IFieldMetaData ifmd = metaData.getFieldMeta(i);
				String name = ifmd.getName();
				JSONObject jsonMeta = new JSONObject();
				jsonMeta.put("columnName", name);
				columnsJSON.put(jsonMeta);

			}

		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			logger.error("Error while getting dataset columns", e);
			return null;
		}

		logger.debug("OUT");
		return columnsJSON;

	}

	// private void checkQbeDataset(IDataSet dataSet) {
	// if (dataSet instanceof QbeDataSet) {
	// SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
	// Map parameters = dataSet.getParamsMap();
	// if (parameters == null) {
	// parameters = new HashMap();
	// dataSet.setParamsMap(parameters);
	// }
	// dataSet.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
	// }
	// }

	private void checkFileDataset(IDataSet dataSet) {
		if (dataSet instanceof FileDataSet) {
			((FileDataSet) dataSet).setResourcePath(DAOConfig.getResourcePath());
		}
	}

	protected List<Integer> getCategories(IEngUserProfile profile) {

		List<Integer> categories = new ArrayList<Integer>();
		try {
			// NO CATEGORY IN THE DOMAINS
			IDomainDAO domaindao = DAOFactory.getDomainDAO();
			List<Domain> dialects = domaindao.loadListDomainsByType("CATEGORY_TYPE");
			if (dialects == null || dialects.size() == 0) {
				return null;
			}

			Collection userRoles = profile.getRoles();
			Iterator userRolesIter = userRoles.iterator();
			IRoleDAO roledao = DAOFactory.getRoleDAO();
			while (userRolesIter.hasNext()) {
				String roleName = (String) userRolesIter.next();
				Role role = roledao.loadByName(roleName);

				List<RoleMetaModelCategory> aRoleCategories = roledao.getMetaModelCategoriesForRole(role.getId());
				List<RoleMetaModelCategory> resp = new ArrayList<>();
				List<SbiDomains> array = DAOFactory.getDomainDAO().loadListDomainsByType("CATEGORY_TYPE");
				for (RoleMetaModelCategory r : aRoleCategories) {
					for (SbiDomains dom : array) {
						if (r.getCategoryId().equals(dom.getValueId())) {
							resp.add(r);
						}
					}

				}
				if (resp != null) {
					for (Iterator iterator = resp.iterator(); iterator.hasNext();) {
						RoleMetaModelCategory roleDataSetCategory = (RoleMetaModelCategory) iterator.next();
						categories.add(roleDataSetCategory.getCategoryId());
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error loading the data set categories visible from the roles of the user");
			throw new SpagoBIRuntimeException("Error loading the data set categories visible from the roles of the user");
		}
		return categories;
	}

	private List<IDataSet> getFilteredDatasets(List<IDataSet> unfilteredDataSets, List<Integer> categories) {
		List<IDataSet> dataSets = new ArrayList<IDataSet>();
		if (categories != null && categories.size() != 0) {
			for (IDataSet ds : unfilteredDataSets) {
				if (ds.getCategoryId() == null || categories.contains(ds.getCategoryId())) {
					dataSets.add(ds);
				}
			}
		}
		return dataSets;
	}

}
