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
package it.eng.spagobi.tools.dataset.service;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import commonj.work.WorkException;
import it.eng.qbe.dataset.FederatedDataSet;
import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.serializer.DataSetMetadataJSONSerializer;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.scheduler.service.ISchedulerServiceSupplier;
import it.eng.spagobi.services.scheduler.service.SchedulerServiceSupplierFactory;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.CkanDataSet;
import it.eng.spagobi.tools.dataset.bo.ConfigurableDataSet;
import it.eng.spagobi.tools.dataset.bo.CustomDataSet;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersList;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.FlatDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDatasetFactory;
import it.eng.spagobi.tools.dataset.bo.JavaClassDataSet;
import it.eng.spagobi.tools.dataset.bo.MongoDataSet;
import it.eng.spagobi.tools.dataset.bo.RESTDataSet;
import it.eng.spagobi.tools.dataset.bo.SPARQLDataSet;
import it.eng.spagobi.tools.dataset.bo.ScriptDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.transformer.PivotDataSetTransformer;
import it.eng.spagobi.tools.dataset.constants.CkanDataSetConstants;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.constants.RESTDataSetConstants;
import it.eng.spagobi.tools.dataset.constants.SPARQLDatasetConstants;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.exceptions.DatasetInUseException;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.persist.IPersistedManager;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.dataset.utils.DatasetPersistenceUtils;
import it.eng.spagobi.tools.dataset.utils.datamart.SpagoBICoreDatamartRetriever;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.utils.SchedulerUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.json.JSONUtils;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONSuccess;

@SuppressWarnings("serial")
public class ManageDatasets extends AbstractSpagoBIAction {

	private static final String PARAM_VALUE_NAME = "value";

	public static final String DEFAULT_VALUE_PARAM = "defaultValue";

	// logger component
	public static Logger logger = Logger.getLogger(ManageDatasets.class);
	public static Logger auditlogger = Logger.getLogger("dataset.audit");

	public static final String JOB_GROUP = "PersistDatasetExecutions";
	public static final String TRIGGER_GROUP = "DEFAULT";
	public static final String TRIGGER_NAME_PREFIX = "persist_";

	public static final String PUBLIC = "PUBLIC";
	protected IEngUserProfile profile;

	// Boolean isFromSaveNoMetadata =
	// getAttributeAsBoolean(DataSetConstants.IS_FROM_SAVE_NO_METADATA);

	@Override
	public void doService() {
		logger.debug("IN");
		profile = getUserProfile();
		String serviceType = getAttributeAsString(DataSetConstants.MESSAGE_DET);
		try {
			IDataSetDAO dsDao = DAOFactory.getDataSetDAO();
			dsDao.setUserProfile(profile);
			Locale locale = getLocale();
			logger.debug("Service type " + serviceType);

			if (serviceType != null && serviceType.equalsIgnoreCase(DataSetConstants.DATASETS_FOR_KPI_LIST)) {
				returnDatasetForKpiList(dsDao, locale);
			} else if (serviceType != null && serviceType.equalsIgnoreCase(DataSetConstants.DATASETS_LIST)) {
				returnDatasetList(dsDao, locale);
			} else if (serviceType != null && serviceType.equalsIgnoreCase(DataSetConstants.DATASET_INSERT)) {
				datasetInsert(dsDao, locale);
			} else if (serviceType != null && serviceType.equalsIgnoreCase(DataSetConstants.DATASET_TEST)) {
				datasetTest(dsDao, locale);
			} else if (serviceType != null && serviceType.equalsIgnoreCase(DataSetConstants.DATASET_DELETE)) {
				datasetDelete(dsDao, locale);
			} else if (serviceType != null && serviceType.equalsIgnoreCase(DataSetConstants.DATASET_VERSION_DELETE)) {
				datasetVersionDelete(dsDao, locale);
			} else if (serviceType != null && serviceType.equalsIgnoreCase(DataSetConstants.DATASET_ALL_VERSIONS_DELETE)) {
				datasetAllVersionsDelete(dsDao, locale);
			} else if (serviceType != null && serviceType.equalsIgnoreCase(DataSetConstants.DATASET_VERSION_RESTORE)) {
				datasetVersionRestore(dsDao, locale);
			} else if (serviceType == null) {
				setUsefulItemsInSession(dsDao, locale);
			}
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Error occurred while performing " + serviceType, e);
		} finally {
			logger.debug("OUT");
		}
	}

	private void returnDatasetForKpiList(IDataSetDAO dsDao, Locale locale) {
		try {
			Integer totalItemsNum = dsDao.countDatasets();
			List<SbiDataSet> items = getListOfGenericDatasetsForKpi(dsDao);
			logger.debug("Loaded items list");
			JSONArray itemsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(items, locale);
			JSONObject responseJSON = createJSONResponse(itemsJSON, totalItemsNum);
			writeBackToClient(new JSONSuccess(responseJSON));

		} catch (Throwable e) {
			logger.error("Exception occurred while retrieving items", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "sbi.general.retrieveItemsError", e);
		}
	}

	private void returnDatasetList(IDataSetDAO dsDao, Locale locale) {
		logger.debug("IN");
		try {
			Integer totalItemsNum = dsDao.countDatasets();
			List<IDataSet> items = getListOfGenericDatasets(dsDao);
			logger.debug("Loaded items list");
			JSONArray itemsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(items, locale);
			ISchedulerDAO schedulerDAO;
			try {
				schedulerDAO = DAOFactory.getSchedulerDAO();
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("Impossible to load scheduler DAO", t);
			}
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				for (int i = 0; i < items.size(); i++) {
					if (items.get(i).isPersisted()) {
						List<Trigger> triggers = schedulerDAO.loadTriggers(JOB_GROUP, items.get(i).getLabel());
						if (triggers.isEmpty()) {
							itemsJSON.getJSONObject(i).put("isScheduled", false);
						} else {
							// Dataset scheduling is mono-trigger
							Trigger trigger = triggers.get(0);
							if (!trigger.isRunImmediately()) {
								itemsJSON.getJSONObject(i).put("isScheduled", true);
								if (trigger.getStartTime() != null) {
									itemsJSON.getJSONObject(i).put("startDate", sdf.format(trigger.getStartTime()));
								} else {
									itemsJSON.getJSONObject(i).put("startDate", "");
								}
								if (trigger.getEndTime() != null) {
									itemsJSON.getJSONObject(i).put("endDate", sdf.format(trigger.getEndTime()));
								} else {
									itemsJSON.getJSONObject(i).put("endDate", "");
								}
								itemsJSON.getJSONObject(i).put("schedulingCronLine", trigger.getChronExpression().getExpression());
							}
						}
					}
					// Check if dataset is used by objects or by federations
					ArrayList<BIObject> objectsUsing = DAOFactory.getBIObjDataSetDAO().getBIObjectsUsingDataset(items.get(i).getId());
					String documentsNames = "";
					if (objectsUsing != null && objectsUsing.size() > 1) {
						for (int o = 0; o < objectsUsing.size(); o++) {
							BIObject obj = objectsUsing.get(o);
							documentsNames += obj.getName();
							if (o < objectsUsing.size() - 1)
								documentsNames += ", ";
						}
						itemsJSON.getJSONObject(i).put("hasDocumentsAssociated", documentsNames);
					}

					List<FederationDefinition> federationsAssociated = DAOFactory.getFedetatedDatasetDAO().loadFederationsUsingDataset(items.get(i).getId());
					if (federationsAssociated != null && federationsAssociated.size() > 0)
						itemsJSON.getJSONObject(i).put("hasFederationsAssociated", "true");

				}
				JSONObject responseJSON = createJSONResponse(itemsJSON, totalItemsNum);
				writeBackToClient(new JSONSuccess(responseJSON));

			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An unexpected error occured while loading trigger list for datasets", t);
			} finally {
				logger.debug("OUT");
			}

		} catch (Throwable e) {
			logger.error("Exception occurred while retrieving items", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "sbi.general.retrieveItemsError", e);
		}
	}

	protected void saveMetadataAfterSaving() {
		IDataSet ds = getGuiGenericDatasetToInsert();
	}

	protected void datasetInsert(IDataSetDAO dsDao, Locale locale) throws NamingException, WorkException, InterruptedException {
		IDataSet ds = getGuiGenericDatasetToInsert();
		datasetInsert(ds, dsDao, locale);
		calculateDomainValues(ds);
	}

	protected void datasetInsert(IDataSet ds, IDataSetDAO dsDao, Locale locale) {
		JSONObject attributesResponseSuccessJSON = new JSONObject();
		HashMap<String, String> logParam = new HashMap();

		if (ds != null) {
			logParam.put("NAME", ds.getName());
			logParam.put("LABEL", ds.getLabel());
			logParam.put("TYPE", ds.getDsType());
			String id = getAttributeAsString(DataSetConstants.ID);
			try {
				IDataSet existingByName = dsDao.loadDataSetByName(ds.getName());
				if (id != null && !id.equals("") && !id.equals("0")) {
					if (existingByName != null && !Integer.valueOf(id).equals(existingByName.getId())) {
						throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.nameAlreadyExistent");
					}

					ds.setId(Integer.valueOf(id));
					modifyPersistenceAndScheduling(ds, logParam);
					dsDao.modifyDataSet(ds);
					logger.debug("Resource " + id + " updated");
					attributesResponseSuccessJSON.put("success", true);
					attributesResponseSuccessJSON.put("responseText", "Operation succeded");
					attributesResponseSuccessJSON.put("id", id);
					attributesResponseSuccessJSON.put("dateIn", ds.getDateIn());
					attributesResponseSuccessJSON.put("userIn", ds.getUserIn());
					attributesResponseSuccessJSON.put("meta", new DataSetMetadataJSONSerializer().metadataSerializerChooser(ds.getDsMetadata()));
				} else {
					IDataSet existingByLabel = dsDao.loadDataSetByLabel(ds.getLabel());
					if (existingByLabel != null) {
						throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.labelAlreadyExistent");
					}

					if (existingByName != null) {
						throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.nameAlreadyExistent");
					}

					Integer dsID = dsDao.insertDataSet(ds);
					VersionedDataSet dsSaved = (VersionedDataSet) dsDao.loadDataSetById(dsID);
					auditlogger.info("[Saved dataset without metadata with id: " + dsID + "]");
					logger.debug("New Resource inserted");
					attributesResponseSuccessJSON.put("success", true);
					attributesResponseSuccessJSON.put("responseText", "Operation succeded");
					attributesResponseSuccessJSON.put("id", dsID);
					if (dsSaved != null) {
						attributesResponseSuccessJSON.put("dateIn", dsSaved.getDateIn());
						attributesResponseSuccessJSON.put("userIn", dsSaved.getUserIn());
						attributesResponseSuccessJSON.put("versNum", dsSaved.getVersionNum());
						attributesResponseSuccessJSON.put("meta", new DataSetMetadataJSONSerializer().metadataSerializerChooser(dsSaved.getDsMetadata()));
					}
				}
				String operation = (id != null && !id.equals("") && !id.equals("0")) ? "DATA_SET.MODIFY" : "DATA_SET.ADD";
				Boolean isFromSaveNoMetadata = getAttributeAsBoolean(DataSetConstants.IS_FROM_SAVE_NO_METADATA);
				// handle insert of persistence and scheduling
				if (!isFromSaveNoMetadata) {
					auditlogger.info("[Start persisting metadata for dataset with id " + ds.getId() + "]");
					insertPersistenceAndScheduling(ds, logParam);
					auditlogger.info("Metadata saved for dataset with id " + ds.getId() + "]");
					auditlogger.info("[End persisting metadata for dataset with id " + ds.getId() + "]");
				}

				AuditLogUtilities.updateAudit(getHttpRequest(), profile, operation, logParam, "OK");
				writeBackToClient(new JSONSuccess(attributesResponseSuccessJSON));
			} catch (SpagoBIServiceException es) {
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DATA_SET.ADD", logParam, "KO");
				} catch (Exception es1) {
					// TODO Auto-generated catch block
					es1.printStackTrace();
				}
				throw new SpagoBIServiceException(SERVICE_NAME, es.getMessage(), es);
			} catch (Throwable e) {
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DATA_SET.ADD", logParam, "KO");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.saveDsError", e);
			}
		} else {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DATA_SET.ADD/MODIFY", logParam, "ERR");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.error("DataSet name, label or type are missing");
			throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.fillFieldsError");
		}
	}

	public void insertPersistenceAndScheduling(IDataSet ds, HashMap<String, String> logParam) throws Exception {
		logger.debug("IN");
		if (ds.isPersisted()) {
			// Manage persistence of dataset if required. On modify it
			// will drop and create the destination table!
			logger.debug("Start persistence...");
			// gets the dataset object informations
			auditlogger.info("-------------");
			auditlogger.info("Dataset INFO:");
			auditlogger.info("-------------");
			auditlogger.info("ID: " + ds.getId());
			auditlogger.info("LABEL: " + ds.getLabel());
			auditlogger.info("NAME: " + ds.getName());
			auditlogger.info("ORGANIZATION: " + ds.getOrganization());
			auditlogger.info("PERSIST TABLE NAME FOR DATASET WITH ID " + ds.getId() + " : " + ds.getPersistTableName());
			auditlogger.info("-------------");
			IDataSetDAO iDatasetDao = DAOFactory.getDataSetDAO();
			iDatasetDao.setUserProfile(profile);
			IDataSet dataset = iDatasetDao.loadDataSetByLabel(ds.getLabel());
			checkFileDataset(((VersionedDataSet) dataset).getWrappedDataset());

			if (getRequestContainer() != null) {
				JSONArray parsListJSON = getAttributeAsJSONArray(DataSetConstants.PARS);
				if (parsListJSON.length() > 0) {
					logger.error("The dataset cannot be persisted because uses parameters!");
					throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.dsCannotPersist");
				}
			}
			IPersistedManager ptm = new PersistedTableManager(profile);
			ptm.persistDataSet(dataset);

			logger.debug("Persistence ended succesfully!");
			if (ds.isScheduled()) {
				DatasetPersistenceUtils dspu = new DatasetPersistenceUtils(profile, null, SERVICE_NAME, ds);
				String jobName = dspu.saveDatasetJob(ds, logParam);
				if (jobName != null) {
					dspu.saveTriggerForDatasetJob(jobName, getSpagoBIRequestContainer());
				} else {
					logger.error("The job is not saved correctly!");
					throw new SpagoBIServiceException(SERVICE_NAME, "The job is not saved correctly!");
				}
			} else {
				ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
				String servoutStr = schedulerService.deleteJob(ds.getLabel(), JOB_GROUP);
				SourceBean execOutSB = SchedulerUtilities.getSBFromWebServiceResponse(servoutStr);
				if (execOutSB != null) {
					String outcome = (String) execOutSB.getAttribute("outcome");
					if (outcome.equalsIgnoreCase("fault")) {
						try {
							AuditLogUtilities.updateAudit(getHttpRequest(), profile, "SCHED_JOB.DELETE", logParam, "KO");
						} catch (Exception e) {
							e.printStackTrace();
						}
						throw new SpagoBIServiceException(SERVICE_NAME, "Job " + ds.getLabel() + " not deleted by the web service");
					}
				}
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "SCHED_TRIGGER.DELETE", logParam, "OK");
			}
		}
		logger.debug("OUT");
	}

	public void modifyPersistenceAndScheduling(IDataSet ds, HashMap<String, String> logParam) throws Exception {
		logger.debug("IN");
		IDataSetDAO iDatasetDao = DAOFactory.getDataSetDAO();
		iDatasetDao.setUserProfile(profile);
		IDataSet previousDataset = iDatasetDao.loadDataSetById(ds.getId());
		if (previousDataset.isPersisted() && !ds.isPersisted()) {
			logger.error("The dataset [" + previousDataset.getLabel() + "] has to be unpersisted");
			PersistedTableManager ptm = new PersistedTableManager(profile);
			ptm.dropTableIfExists(previousDataset.getDataSourceForWriting(), previousDataset.getTableNameForReading());

			ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();
			List<Trigger> triggers = schedulerDAO.loadTriggers(JOB_GROUP, previousDataset.getLabel());

			if (triggers != null && !triggers.isEmpty() && !ds.isScheduled()) {
				logger.error("The dataset [" + previousDataset.getLabel() + "] has to be unscheduled");

				ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
				String servoutStr = schedulerService.deleteJob(previousDataset.getLabel(), JOB_GROUP);
				SourceBean execOutSB = SchedulerUtilities.getSBFromWebServiceResponse(servoutStr);
				if (execOutSB != null) {
					String outcome = (String) execOutSB.getAttribute("outcome");
					if (outcome.equalsIgnoreCase("fault")) {
						try {
							AuditLogUtilities.updateAudit(getHttpRequest(), profile, "SCHED_JOB.DELETE", logParam, "KO");
						} catch (Exception e) {
							logger.error(e);
						}
						throw new SpagoBIServiceException(SERVICE_NAME, "Job " + previousDataset.getLabel() + " not deleted by the web service");
					}
				}
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "SCHED_TRIGGER.DELETE", logParam, "OK");
			}
		}
		logger.debug("OUT");
	}

	public void deletePersistenceAndScheduling(IDataSet ds, HashMap<String, String> logParam) throws Exception {
		logger.debug("IN");
		if (ds.isPersisted()) {
			logger.error("The dataset [" + ds.getLabel() + "] has to be unpersisted");
			PersistedTableManager ptm = new PersistedTableManager(profile);
			ptm.dropTableIfExists(ds.getDataSourceForWriting(), ds.getTableNameForReading());

			ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();
			List<Trigger> triggers = schedulerDAO.loadTriggers(JOB_GROUP, ds.getLabel());

			if (triggers != null && !triggers.isEmpty()) {
				logger.error("The dataset [" + ds.getLabel() + "] has to be unscheduled");

				ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
				String servoutStr = schedulerService.deleteJob(ds.getLabel(), JOB_GROUP);
				SourceBean execOutSB = SchedulerUtilities.getSBFromWebServiceResponse(servoutStr);
				if (execOutSB != null) {
					String outcome = (String) execOutSB.getAttribute("outcome");
					if (outcome.equalsIgnoreCase("fault")) {
						try {
							AuditLogUtilities.updateAudit(getHttpRequest(), profile, "SCHED_JOB.DELETE", logParam, "KO");
						} catch (Exception e) {
							logger.error(e);
						}
						throw new SpagoBIServiceException(SERVICE_NAME, "Job " + ds.getLabel() + " not deleted by the web service");
					}
				}
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "SCHED_TRIGGER.DELETE", logParam, "OK");
			}
		}
		logger.debug("OUT");
	}

	private void datasetTest(IDataSetDAO dsDao, Locale locale) {
		try {
			JSONObject dataSetJSON = getDataSetResultsAsJSON();
			if (dataSetJSON != null) {
				try {
					writeBackToClient(new JSONSuccess(dataSetJSON));
				} catch (IOException e) {
					throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
				}
			} else {
				throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.testError");
			}
		} catch (Throwable t) {
			if (t instanceof SpagoBIServiceException) {
				throw (SpagoBIServiceException) t;
			}
			throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.testError", t);
		}
	}

	private void datasetDelete(IDataSetDAO dsDao, Locale locale) {
		Integer dsID = getAttributeAsInteger(DataSetConstants.ID);
		IDataSet ds = dsDao.loadDataSetById(dsID);
		HashMap<String, String> logParam = new HashMap();
		logParam.put("NAME", ds.getName());
		logParam.put("LABEL", ds.getLabel());
		logParam.put("TYPE", ds.getDsType());
		try {
			dsDao.deleteDataSet(dsID);
			deleteDatasetFile(ds); // for FileDatase
			deletePersistenceAndScheduling(ds, logParam);
			clearDomainValues(ds);
			logger.debug("Dataset deleted");
			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			String servoutStr = schedulerService.deleteJob(ds.getLabel(), JOB_GROUP);
			SourceBean execOutSB = SchedulerUtilities.getSBFromWebServiceResponse(servoutStr);
			if (execOutSB != null) {
				String outcome = (String) execOutSB.getAttribute("outcome");
				if (outcome.equalsIgnoreCase("fault")) {
					try {
						AuditLogUtilities.updateAudit(getHttpRequest(), profile, "SCHED_JOB.DELETE", logParam, "KO");
					} catch (Exception e) {
						e.printStackTrace();
					}
					throw new SpagoBIServiceException(SERVICE_NAME, "Job " + ds.getLabel() + " not deleted by the web service");
				}
			}
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "SCHED_JOB.DELETE", logParam, "OK");
			} catch (Exception e) {
				e.printStackTrace();
			}
			AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DATA_SET.DELETE", logParam, "OK");
			writeBackToClient(new JSONAcknowledge("Operation succeded"));
		} catch (Throwable e) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DATA_SET.DELETE", logParam, "KO");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (e instanceof DatasetInUseException) {
				DatasetInUseException duie = (DatasetInUseException) e;
				logger.warn("Cannot delete a dataset in use", e);
				MessageBuilder msgBuild = new MessageBuilder();
				String errorDescription = msgBuild.getMessage("sbi.ds.deleteDsInUseError", getLocale());

				errorDescription = errorDescription.replaceAll("%0", duie.getBiObjectMessage()).replaceAll("%1", duie.getFederationsMessage())
						.replaceAll("%2", duie.getKpiMessage()).replaceAll("%3", duie.getLovMessage());

				throw new SpagoBIServiceException(SERVICE_NAME, errorDescription, e);
			} else {
				logger.error("Exception occurred while retrieving dataset to delete", e);
				throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.deleteDsError", e);
			}
		}
	}

	private void datasetVersionDelete(IDataSetDAO dsDao, Locale locale) {
		Integer dsVersionNum = getAttributeAsInteger(DataSetConstants.VERSION_NUM);
		Integer dsId = getAttributeAsInteger(DataSetConstants.DS_ID);
		try {
			boolean deleted = dsDao.deleteInactiveDataSetVersion(dsVersionNum, dsId);
			if (deleted) {
				logger.debug("Dataset Version deleted");
				writeBackToClient(new JSONAcknowledge("Operation succeded"));
			} else {
				throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.deleteVersion");
			}
		} catch (Throwable e) {
			logger.error("Exception occurred while retrieving dataset version to delete", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.deleteVersion", e);
		}
	}

	private void datasetAllVersionsDelete(IDataSetDAO dsDao, Locale locale) {
		Integer dsID = getAttributeAsInteger(DataSetConstants.DS_ID);
		try {
			dsDao.deleteAllInactiveDataSetVersions(dsID);
			logger.debug("All Older Dataset versions deleted");
			writeBackToClient(new JSONAcknowledge("Operation succeded"));
		} catch (Throwable e) {
			logger.error("Exception occurred while retrieving dataset to delete", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.deleteVersion", e);
		}
	}

	private void datasetVersionRestore(IDataSetDAO dsDao, Locale locale) {
		Integer dsID = getAttributeAsInteger(DataSetConstants.DS_ID);
		Integer dsVersionNum = getAttributeAsInteger(DataSetConstants.VERSION_NUM);
		try {
			IDataSet dsNewDetail = dsDao.restoreOlderDataSetVersion(dsID, dsVersionNum);
			logger.debug("Dataset Version correctly Restored");
			calculateDomainValues(dsNewDetail);
			List temp = new ArrayList();
			temp.add(dsNewDetail);
			JSONArray itemJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(temp, locale);
			JSONObject version = itemJSON.getJSONObject(0);
			JSONObject attributesResponseSuccessJSON = new JSONObject();
			attributesResponseSuccessJSON.put("success", true);
			attributesResponseSuccessJSON.put("responseText", "Operation succeded");
			attributesResponseSuccessJSON.put("result", version);
			writeBackToClient(new JSONSuccess(attributesResponseSuccessJSON));
		} catch (Throwable e) {
			logger.error("Exception occurred while retrieving dataset to restore", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.restoreVersionError", e);
		}
	}

	private void setUsefulItemsInSession(IDataSetDAO dsDao, Locale locale) {
		try {
			List dsTypesList = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.DATA_SET_TYPE);
			filterDataSetType(dsTypesList);
			getSessionContainer().setAttribute("dsTypesList", dsTypesList);
			List catTypesList = getCategories();
			getSessionContainer().setAttribute("catTypesList", catTypesList);
			List dataSourceList = DAOFactory.getDataSourceDAO().loadAllDataSources();
			getSessionContainer().setAttribute("dataSourceList", dataSourceList);
			List scriptLanguageList = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.SCRIPT_TYPE);
			getSessionContainer().setAttribute("scriptLanguageList", scriptLanguageList);
			List trasfTypesList = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.TRANSFORMER_TYPE);
			getSessionContainer().setAttribute("trasfTypesList", trasfTypesList);
			List sbiAttrs = DAOFactory.getSbiAttributeDAO().loadSbiAttributes();
			getSessionContainer().setAttribute("sbiAttrsList", sbiAttrs);

			List scopeCdList = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.DS_SCOPE);
			getSessionContainer().setAttribute("scopeCdList", scopeCdList);

			String filePath = SpagoBIUtilities.getResourcePath();
			filePath += File.separator + "dataset" + File.separator + "files";
			File dir = new File(filePath);
			String[] fileNames = dir.list();
			getSessionContainer().setAttribute("fileNames", fileNames);
		} catch (EMFUserError | EMFInternalError e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.dsTypesRetrieve", e);
		}
	}

	private void filterDataSetType(List<Domain> domains) throws EMFInternalError {
		if (!getUserProfile().getFunctionalities().contains(SpagoBIConstants.CKAN_FUNCTIONALITY)) {
			Iterator<Domain> iterator = domains.iterator();
			while (iterator.hasNext()) {
				Domain domain = iterator.next();
				if (domain.getValueCd().toLowerCase().contains("ckan")) {
					iterator.remove();
				}
			}
		}
	}

	public List getCategories() {
		IRoleDAO rolesDao = null;
		Role role = new Role();
		try {
			UserProfile profile = (UserProfile) this.getUserProfile();
			rolesDao = DAOFactory.getRoleDAO();
			rolesDao.setUserProfile(profile);
			IDomainDAO domainDAO = DAOFactory.getDomainDAO();
			ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();
			if (UserUtilities.hasDeveloperRole(profile) && !UserUtilities.hasAdministratorRole(profile)) {
				List<Domain> categoriesDev = new ArrayList<>();
				Collection<String> roles = profile.getRolesForUse();
				Iterator<String> itRoles = roles.iterator();
				while (itRoles.hasNext()) {
					String roleName = itRoles.next();
					role = rolesDao.loadByName(roleName);
					List<RoleMetaModelCategory> ds = rolesDao.getMetaModelCategoriesForRole(role.getId());
					List<Domain> array = categoryDao.getCategoriesForDataset()
						.stream()
						.map(Domain::fromCategory)
						.collect(toList());
					for (RoleMetaModelCategory r : ds) {
						for (Domain dom : array) {
							if (r.getCategoryId().equals(dom.getValueId())) {
								categoriesDev.add(dom);
							}
						}
					}
				}
				return categoriesDev;
			} else {
				return categoryDao.getCategoriesForDataset()
						.stream()
						.map(Domain::fromCategory)
						.collect(toList());
			}
		} catch (Exception e) {
			logger.error("Role with selected id: " + role.getId() + " doesn't exists", e);
			throw new SpagoBIRuntimeException("Item with selected id: " + role.getId() + " doesn't exists", e);
		}
	}

	private List<SbiDataSet> getListOfGenericDatasetsForKpi(IDataSetDAO dsDao) throws JSONException, EMFUserError {
		Integer start = getAttributeAsInteger(DataSetConstants.START);
		Integer limit = getAttributeAsInteger(DataSetConstants.LIMIT);

		if (start == null) {
			start = DataSetConstants.START_DEFAULT;
		}
		if (limit == null) {
			limit = DataSetConstants.LIMIT_DEFAULT;
		}
		List<SbiDataSet> items = dsDao.loadPagedSbiDatasetConfigList(start, limit);
		return items;
	}

	protected List<IDataSet> getListOfGenericDatasets(IDataSetDAO dsDao) throws JSONException, EMFUserError {
		Integer start = getAttributeAsInteger(DataSetConstants.START);
		Integer limit = getAttributeAsInteger(DataSetConstants.LIMIT);

		if (start == null) {
			start = DataSetConstants.START_DEFAULT;
		}
		if (limit == null) {
			// limit = DataSetConstants.LIMIT_DEFAULT;
			limit = DataSetConstants.LIMIT_DEFAULT;
		}
		JSONObject filtersJSON = null;
		List<IDataSet> items = null;
		if (this.requestContainsAttribute(DataSetConstants.FILTERS)) {
			filtersJSON = getAttributeAsJSONObject(DataSetConstants.FILTERS);
			String hsql = filterList(filtersJSON);
			items = dsDao.loadFilteredDatasetList(hsql, start, limit, ((UserProfile) profile).getUserId().toString());
		} else {// not filtered
			items = dsDao.loadPagedDatasetList(start, limit);
			// items =
			// dsDao.loadPagedDatasetList(start,limit,profile.getUserUniqueIdentifier().toString(),
			// true);
		}
		return items;
	}

	protected IDataSet getGuiGenericDatasetToInsert() {

		IDataSet ds = null;

		String label = getAttributeAsString(DataSetConstants.LABEL);
		String name = getAttributeAsString(DataSetConstants.NAME);
		String description = getAttributeAsString(DataSetConstants.DESCRIPTION);
		String datasetTypeCode = getAttributeAsString(DataSetConstants.DS_TYPE_CD);
		String datasetTypeName = getDatasetTypeName(datasetTypeCode);
		Boolean isFromSaveNoMetadata = getAttributeAsBoolean(DataSetConstants.IS_FROM_SAVE_NO_METADATA);

		try {
			if (name != null && label != null && datasetTypeName != null && !datasetTypeName.equals("")) {
				try {
					ds = getDataSet(datasetTypeName, true);
				} catch (Exception e) {
					logger.error("Error in building dataset of type " + datasetTypeName, e);
					throw e;
				}
				if (ds != null) {
					ds.setLabel(label);
					ds.setName(name);

					if (description != null && !description.equals("")) {
						ds.setDescription(description);
					}
					ds.setDsType(datasetTypeName);

					String catTypeCd = getAttributeAsString(DataSetConstants.CATEGORY_TYPE_VN);

					String meta = getAttributeAsString(DataSetConstants.METADATA);
					String trasfTypeCd = getAttributeAsString(DataSetConstants.TRASFORMER_TYPE_CD);

					List<Domain> domainsCat = (List<Domain>) getSessionContainer().getAttribute("catTypesList");
					HashMap<String, Integer> domainIds = new HashMap<>();
					if (domainsCat != null) {
						for (int i = 0; i < domainsCat.size(); i++) {
							domainIds.put(domainsCat.get(i).getValueCd(), domainsCat.get(i).getValueId());
						}
					}
					Integer catTypeID = domainIds.get(catTypeCd);
					if (catTypeID != null) {
						ds.setCategoryCd(catTypeCd);
						ds.setCategoryId(catTypeID);
					}

					List<Domain> domainsScope = (List<Domain>) getSessionContainer().getAttribute("scopeCdList");
					HashMap<String, Integer> domainScopeIds = new HashMap<>();
					if (domainsScope != null) {
						for (int i = 0; i < domainsScope.size(); i++) {
							domainScopeIds.put(domainsScope.get(i).getValueCd(), domainsScope.get(i).getValueId());
						}
					}
					String scopeCode = getAttributeAsString("scopeCd");
					Integer scopeID = domainScopeIds.get(scopeCode.toUpperCase());
					if (scopeID == null) {
						logger.error("Impossible to save Data Set. The scope in not set");
						throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to save Data Set. The scope in not set");
					} else {
						ds.setScopeCd(scopeCode);
						ds.setScopeId(scopeID);
					}

					if (scopeCode.equalsIgnoreCase(SpagoBIConstants.DS_SCOPE_ENTERPRISE) || scopeCode.equalsIgnoreCase(SpagoBIConstants.DS_SCOPE_TECHNICAL)) {
						if (catTypeCd == null || catTypeCd.equals("")) {
							logger.error("Impossible to save DataSet. The category is mandatory for Data Set Enterprise or Technical");
							throw new SpagoBIServiceException(SERVICE_NAME,
									"Impossible to save DataSet. The category is mandatory for Data Set Enterprise or Technical");
						}
					}

					if (meta != null && !meta.equals("")) {
						ds.setDsMetadata(meta);
					}

					String pars = getDataSetParametersAsString();
					if (pars != null) {
						ds.setParameters(pars);
					}

					if (trasfTypeCd != null && !trasfTypeCd.equals("")) {
						ds = setTransformer(ds, trasfTypeCd);
					}

					IDataSet dsRecalc = null;

					if (datasetTypeName != null && !datasetTypeName.equals("")) {
						try {
							dsRecalc = getDataSet(datasetTypeName, true);
						} catch (Exception e) {
							logger.error("Error in building dataset of type " + datasetTypeName, e);
							throw e;
						}

						if (dsRecalc != null) {
							if (trasfTypeCd != null && !trasfTypeCd.equals("")) {
								dsRecalc = setTransformer(dsRecalc, trasfTypeCd);
							}
							String recalculateMetadata = this.getAttributeAsString(DataSetConstants.RECALCULATE_METADATA);
							String dsMetadata = null;
							if ((recalculateMetadata == null || recalculateMetadata.trim().equals("yes") || recalculateMetadata.trim().equals("true"))
									&& (!isFromSaveNoMetadata)) {
								// recalculate metadata
								logger.debug("Recalculating dataset's metadata: executing the dataset...");
								HashMap parametersMap = new HashMap();
								parametersMap = getDataSetParametersAsMap(true);

								IEngUserProfile profile = getUserProfile();
								ds.setPersisted(false);
								ds.setPersistedHDFS(false);

								IMetaData currentMetadata = null;
								try {
									currentMetadata = getDatasetTestMetadata(dsRecalc, parametersMap, profile, meta);
								} catch (Exception e) {
									logger.error("Error while recovering dataset metadata: check dataset definition ", e);
									throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.test.error.metadata", e);
								}

								// check if there are metadata field with same columns or aliases
								List<String> aliases = new ArrayList<>();
								for (int i = 0; i < currentMetadata.getFieldCount(); i++) {
									String alias = currentMetadata.getFieldAlias(i);
									if (aliases.contains(alias)) {
										logger.error(
												"Cannot save dataset cause preview revealed that two columns with name " + alias + " exist; change aliases");
										throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.test.error.duplication");
									}
									aliases.add(alias);
								}

								DatasetMetadataParser dsp = new DatasetMetadataParser();
								dsMetadata = dsp.metadataToXML(currentMetadata);
								LogMF.debug(logger, "Dataset executed, metadata are [{0}]", dsMetadata);

								// compare current metadata with previous
								// metadata if dataset is in use
								String previousId = getAttributeAsString(DataSetConstants.ID);
								if (previousId != null) {
									Integer previousIdInteger = Integer.valueOf(previousId);
									if (previousIdInteger != 0) {

										// Check if dataset is used by objects
										// or by federations

										ArrayList<BIObject> objectsUsing = null;
										try {
											objectsUsing = DAOFactory.getBIObjDataSetDAO().getBIObjectsUsingDataset(previousIdInteger);
										} catch (Exception e) {
											logger.error("Error while getting dataset metadataa", e);
											throw e;
										}
										// check if dataset is used by document
										// by querying SBI_OBJ_DATA_SET table
										List<FederationDefinition> federationsAssociated = DAOFactory.getFedetatedDatasetDAO()
												.loadFederationsUsingDataset(previousIdInteger);

										// if (!objectsUsing.isEmpty() ||
										// !federationsAssociated.isEmpty()) {
										// block save action ONLY for
										// federations (if metadata are changed)
										if (!federationsAssociated.isEmpty()) {
											logger.debug("dataset " + ds.getLabel() + " is used by some " + objectsUsing.size() + "objects or some "
													+ federationsAssociated.size() + " federations");
											// get the previous dataset
											IDataSet dataSet = null;
											try {
												dataSet = DAOFactory.getDataSetDAO().loadDataSetById(previousIdInteger);
											} catch (Exception e) {
												logger.error("Error while getting dataset metadataa", e);
												throw e;
											}

											IMetaData previousMetadata = dataSet.getMetadata();
											boolean isRemoving = isRemovingMetadataFields(previousMetadata, currentMetadata);
											if (isRemoving) {
												// TODO: better would be not to
												// have log tracing of this
												// warning
												throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.deleteOrRenameMetadata");

											}
										}
									}
								}

								LogMF.debug(logger, "Dataset executed, metadata are [{0}]", dsMetadata);
							} else if (!isFromSaveNoMetadata) {
								// load existing metadata
								logger.debug("Loading existing dataset...");
								String id = getAttributeAsString(DataSetConstants.ID);
								if (id != null && !id.equals("") && !id.equals("0")) {
									IDataSet existingDataSet = null;

									try {
										existingDataSet = DAOFactory.getDataSetDAO().loadDataSetById(new Integer(id));
									} catch (Exception e) {
										logger.error("Error while getting dataset metadataa", e);
										throw e;
									}

									dsMetadata = existingDataSet.getDsMetadata();
									LogMF.debug(logger, "Reloaded metadata : [{0}]", dsMetadata);
								} else {
									// when saving a NEW dataset without
									// metadata, and there is no ID
									dsMetadata = "";
								}
							} else {// just isFromSaveNoMetadata
								logger.debug("Saving dataset without metadata. I'll add empty metadata with version = -1");
								DatasetMetadataParser dsp = new DatasetMetadataParser();
								dsMetadata = dsp.buildNoMetadataXML();
							}
							ds.setDsMetadata(dsMetadata);
						}
					} else {
						logger.error("DataSet type is not existent");
						throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.dsTypeError");
					}
				} else {
					logger.error("DataSet type is not existent");
					throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.dsTypeError");
				}

				try {
					getPersistenceInfo(ds);
				} catch (EMFUserError e) {
					logger.error("Erro while updating persistence info ", e);
					throw e;
				}

			}

		} catch (SpagoBIServiceException e) {
			logger.error("Service Error while updating dataset metadata, throw it to make it to user");
			throw e;

		} catch (Exception e) {
			logger.error("Erro while updating dataset metadata, cannot save the dataset", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Error while updating dataset metadata, cannot save the dataset");

		}
		return ds;
	}

	private void getPersistenceInfo(IDataSet ds) throws EMFUserError {
		Boolean isPersisted = getAttributeAsBoolean(DataSetConstants.IS_PERSISTED);
		Boolean isPersistedHDFS = getAttributeAsBoolean(DataSetConstants.IS_PERSISTED_HDFS);
		Boolean isScheduled = getAttributeAsBoolean(DataSetConstants.IS_SCHEDULED);
		if (isPersisted != null) {
			ds.setPersisted(isPersisted.booleanValue());
			if (isPersistedHDFS != null) {
				ds.setPersistedHDFS(isPersistedHDFS.booleanValue());
			}
			if (isScheduled != null) {
				ds.setScheduled(isScheduled.booleanValue());
			}
		}
		if (isPersisted) {
			// String dataSourcePersistLabel =
			// getAttributeAsString(DataSetConstants.DATA_SOURCE_PERSIST);
			// if (dataSourcePersistLabel != null) {
			// IDataSource dataSource = DAOFactory.getDataSourceDAO()
			// .loadDataSourceByLabel(dataSourcePersistLabel);
			// if (dataSource != null) {
			// ds.setDataSourcePersist(dataSource);
			// }
			// }
			String persistTableName = getAttributeAsString(DataSetConstants.PERSIST_TABLE_NAME);
			if (persistTableName != null) {
				ds.setPersistTableName(persistTableName);
			}
		} else {
			ds.setPersistTableName("");
		}
	}

	private JSONObject getDataSetResultsAsJSON() {

		JSONObject dataSetJSON = null;
		JSONArray parsJSON = getAttributeAsJSONArray(DataSetConstants.PARS);
		String transformerTypeCode = getAttributeAsString(DataSetConstants.TRASFORMER_TYPE_CD);

		IDataSet dataSet = getDataSet();
		if (dataSet == null) {
			throw new SpagoBIRuntimeException("Impossible to retrieve dataset from request");
		}

		if (StringUtilities.isNotEmpty(transformerTypeCode)) {
			dataSet = setTransformer(dataSet, transformerTypeCode);
		}
		HashMap<String, String> parametersMap = new HashMap<>();
		if (parsJSON != null) {
			parametersMap = getDataSetParametersAsMap(false);
		}
		IEngUserProfile profile = getUserProfile();

		dataSetJSON = getDatasetTestResultList(dataSet, parametersMap, profile);

		return dataSetJSON;
	}

	private String getDatasetTypeName(String datasetTypeCode) {
		String datasetTypeName = null;

		try {

			if (datasetTypeCode == null) {
				return null;
			}

			List<Domain> datasetTypes = (List<Domain>) getSessionContainer().getAttribute("dsTypesList");
			// if the method is called out of DatasetManagement
			if (datasetTypes == null) {
				try {
					datasetTypes = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.DATA_SET_TYPE);
				} catch (Throwable t) {
					throw new SpagoBIRuntimeException("An unexpected error occured while loading dataset types from database", t);
				}
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
			if (t instanceof SpagoBIRuntimeException) {
				throw (SpagoBIRuntimeException) t;
			}
			throw new SpagoBIRuntimeException("An unexpected error occured while resolving dataset type name from dataset type code [" + datasetTypeCode + "]");
		}

		return datasetTypeName;
	}

	private IDataSet getDataSet() {
		IDataSet dataSet = null;
		try {
			String datasetTypeCode = getAttributeAsString(DataSetConstants.DS_TYPE_CD);

			String datasetTypeName = getDatasetTypeName(datasetTypeCode);
			if (datasetTypeName == null) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to resolve dataset type whose code is equal to [" + datasetTypeCode + "]");
			}
			dataSet = getDataSet(datasetTypeName, false);
		} catch (Throwable t) {
			if (t instanceof SpagoBIServiceException) {
				throw (SpagoBIServiceException) t;
			}
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occured while retriving dataset from request", t);
		}
		return dataSet;
	}

	private IDataSet getDataSet(String datasetTypeName, boolean savingDataset) throws Exception {

		IDataSet dataSet = null;
		JSONObject jsonDsConfig = new JSONObject();

		if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_FILE)) {

			String dsId = getAttributeAsString(DataSetConstants.DS_ID);
			String dsLabel = getAttributeAsString(DataSetConstants.LABEL);
			String fileType = getAttributeAsString(DataSetConstants.FILE_TYPE);

			String csvDelimiter = getAttributeAsString(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER);
			String csvQuote = getAttributeAsString(DataSetConstants.CSV_FILE_QUOTE_CHARACTER);

			String skipRows = getAttributeAsString(DataSetConstants.XSL_FILE_SKIP_ROWS);
			String limitRows = getAttributeAsString(DataSetConstants.XSL_FILE_LIMIT_ROWS);
			String xslSheetNumber = getAttributeAsString(DataSetConstants.XSL_FILE_SHEET_NUMBER);

			Boolean newFileUploaded = false;
			if (getAttributeAsString("fileUploaded") != null) {
				newFileUploaded = Boolean.valueOf(getAttributeAsString("fileUploaded"));
			}

			jsonDsConfig.put(DataSetConstants.FILE_TYPE, fileType);
			jsonDsConfig.put(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER, csvDelimiter);
			jsonDsConfig.put(DataSetConstants.CSV_FILE_QUOTE_CHARACTER, csvQuote);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_SKIP_ROWS, skipRows);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_LIMIT_ROWS, limitRows);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_SHEET_NUMBER, xslSheetNumber);

			dataSet = new FileDataSet();
			((FileDataSet) dataSet).setResourcePath(DAOConfig.getResourcePath());

			String fileName = getAttributeAsString(DataSetConstants.FILE_NAME);
			File pathFile = new File(fileName);
			fileName = pathFile.getName();
			if (savingDataset) {
				// when saving the dataset the file associated will get the
				// dataset label name
				if (dsLabel != null) {
					jsonDsConfig.put(DataSetConstants.FILE_NAME, dsLabel + "." + fileType.toLowerCase());
				}
			} else {
				jsonDsConfig.put(DataSetConstants.FILE_NAME, fileName);
			}

			dataSet.setConfiguration(jsonDsConfig.toString());

			if ((dsId == null) || (dsId.isEmpty())) {
				// creating a new dataset, the file uploaded has to be renamed
				// and moved
				((FileDataSet) dataSet).setUseTempFile(true);

				if (savingDataset) {
					// rename and move the file
					String resourcePath = ((FileDataSet) dataSet).getResourcePath();
					if (dsLabel != null) {
						renameAndMoveDatasetFile(fileName, dsLabel, resourcePath, fileType);
						((FileDataSet) dataSet).setUseTempFile(false);
					}

				}

			} else {
				// reading or modifying a existing dataset

				// if change the label then the name of the file should be
				// changed

				JSONObject configuration;
				Integer id_ds = getAttributeAsInteger(DataSetConstants.DS_ID);
				configuration = new JSONObject(DAOFactory.getDataSetDAO().loadDataSetById(id_ds).getConfiguration());
				String realName = configuration.getString("fileName");
				if (dsLabel != null && !realName.equals(dsLabel)) {

					File dest = new File(SpagoBIUtilities.getResourcePath() + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar
							+ dsLabel + "." + configuration.getString("fileType").toLowerCase());
					File source = new File(
							SpagoBIUtilities.getResourcePath() + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar + realName);

					if (!source.getCanonicalPath().equals(dest.getCanonicalPath())) {
						logger.debug("Source and destination are not the same. Copying from source to dest");
						FileUtils.copyFile(source, dest);
						FileUtils.forceDeleteOnExit(source);
					}
				}

				if (newFileUploaded) {
					// modifying an existing dataset with a new file uploaded
					((FileDataSet) dataSet).setUseTempFile(true);

					if (savingDataset) {
						// rename and move the file
						String resourcePath = ((FileDataSet) dataSet).getResourcePath();
						if (dsLabel != null) {
							renameAndMoveDatasetFile(fileName, dsLabel, resourcePath, fileType);
							((FileDataSet) dataSet).setUseTempFile(false);
						}
					}

				} else {
					// using existing dataset file, file in correct place
					((FileDataSet) dataSet).setUseTempFile(false);
				}
			}

			((FileDataSet) dataSet).setFileType(fileType);

			if (savingDataset) {
				// the file used will have the name equals to dataset's label
				((FileDataSet) dataSet).setFileName(dsLabel + "." + fileType.toLowerCase());
			} else {
				((FileDataSet) dataSet).setFileName(fileName);
			}

		}

		if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_CKAN)) {
			// added
			String dsId = getAttributeAsString(DataSetConstants.DS_ID);
			String dsLabel = getAttributeAsString(DataSetConstants.LABEL);
			String fileType = getAttributeAsString(CkanDataSetConstants.CKAN_FILE_TYPE);

			String csvDelimiter = getAttributeAsString(CkanDataSetConstants.CKAN_CSV_FILE_DELIMITER_CHARACTER);
			String csvQuote = getAttributeAsString(CkanDataSetConstants.CKAN_CSV_FILE_QUOTE_CHARACTER);

			String skipRows = getAttributeAsString(CkanDataSetConstants.CKAN_XSL_FILE_SKIP_ROWS);
			String limitRows = getAttributeAsString(CkanDataSetConstants.CKAN_XSL_FILE_LIMIT_ROWS);
			String xslSheetNumber = getAttributeAsString(CkanDataSetConstants.CKAN_XSL_FILE_SHEET_NUMBER);

			String ckanUrl = getAttributeAsString(CkanDataSetConstants.CKAN_URL);

			String ckanId = getAttributeAsString(CkanDataSetConstants.CKAN_ID);
			String scopeCd = DataSetConstants.DS_SCOPE_USER;

			String ckanEncodig = getAttributeAsString(CkanDataSetConstants.CKAN_CSV_FILE_ENCODING);

			Boolean newFileUploaded = false;
			if (getAttributeAsString("fileUploaded") != null) {
				newFileUploaded = Boolean.valueOf(getAttributeAsString("fileUploaded"));
			}

			jsonDsConfig.put(DataSetConstants.FILE_TYPE, fileType);
			jsonDsConfig.put(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER, csvDelimiter);
			jsonDsConfig.put(DataSetConstants.CSV_FILE_QUOTE_CHARACTER, csvQuote);
			jsonDsConfig.put(DataSetConstants.CSV_FILE_ENCODING, ckanEncodig);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_SKIP_ROWS, skipRows);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_LIMIT_ROWS, limitRows);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_SHEET_NUMBER, xslSheetNumber);
			jsonDsConfig.put(CkanDataSetConstants.CKAN_URL, ckanUrl);
			jsonDsConfig.put(CkanDataSetConstants.CKAN_ID, ckanId);
			jsonDsConfig.put(DataSetConstants.DS_SCOPE, scopeCd);

			dataSet = new CkanDataSet();
			((CkanDataSet) dataSet).setResourcePath(ckanUrl);
			((CkanDataSet) dataSet).setCkanUrl(ckanUrl);

			String fileName = getAttributeAsString(DataSetConstants.FILE_NAME);
			if (savingDataset) {
				// when saving the dataset the file associated will get the
				// dataset label name
				if (dsLabel != null) {
					jsonDsConfig.put(DataSetConstants.FILE_NAME, dsLabel + "." + fileType.toLowerCase());
				}
			} else {
				jsonDsConfig.put(DataSetConstants.FILE_NAME, fileName);
			}

			dataSet.setConfiguration(jsonDsConfig.toString());

			if ((dsId == null) || (dsId.isEmpty())) {
				// creating a new dataset, the file uploaded has to be renamed
				// and moved
				if (savingDataset) {
					// delete the file
					String resourcePath = DAOConfig.getResourcePath();
					deleteDatasetFile(fileName, resourcePath, fileType);
				}
			} else {
				// reading or modifying a existing dataset
				if (newFileUploaded) {
					// modifying an existing dataset with a new file uploaded
					// saving the existing dataset with a new file associated
					if (savingDataset) {
						// rename and move the file
						String resourcePath = DAOConfig.getResourcePath();
						deleteDatasetFile(fileName, resourcePath, fileType);
					}
				}
			}

			((CkanDataSet) dataSet).setFileType(fileType);

			if (savingDataset) {
				// the file used will have the name equals to dataset's label
				((CkanDataSet) dataSet).setFileName(dsLabel + "." + fileType.toLowerCase());
			} else {
				// fileName can be empty if you preview it as administrator
				if (fileName.isEmpty()) {
					((CkanDataSet) dataSet).setFileName(CkanDataSetConstants.CKAN_DUMMY_FILENAME + "." + fileType.toLowerCase());
				} else {
					((CkanDataSet) dataSet).setFileName(fileName);
				}
			}

		}

		if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_REST_TYPE)) {
			dataSet = manageRESTDataSet(savingDataset, jsonDsConfig);
		}

		if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_SPARQL)) {
			dataSet = manageSPARQLDataSet(savingDataset, jsonDsConfig);
		}

		if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_QUERY)) {
			String query = getAttributeAsString(DataSetConstants.QUERY);
			String queryScript = getAttributeAsString(DataSetConstants.QUERY_SCRIPT);
			String queryScriptLanguage = getAttributeAsString(DataSetConstants.QUERY_SCRIPT_LANGUAGE);
			String dataSourceLabel = getAttributeAsString(DataSetConstants.DATA_SOURCE);
			jsonDsConfig.put(DataSetConstants.QUERY, query);
			jsonDsConfig.put(DataSetConstants.QUERY_SCRIPT, queryScript);
			jsonDsConfig.put(DataSetConstants.QUERY_SCRIPT_LANGUAGE, queryScriptLanguage);
			jsonDsConfig.put(DataSetConstants.DATA_SOURCE, dataSourceLabel);

			if (dataSourceLabel != null && !dataSourceLabel.equals("")) {
				IDataSource dataSource;
				try {
					dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(dataSourceLabel);
					if (dataSource != null) {
						if (dataSource.getHibDialectClass().toLowerCase().contains("mongo")) {
							dataSet = new MongoDataSet();
						} else {
							dataSet = JDBCDatasetFactory.getJDBCDataSet(dataSource);
						}

						((ConfigurableDataSet) dataSet).setDataSource(dataSource);
						((ConfigurableDataSet) dataSet).setQuery(query);
						((ConfigurableDataSet) dataSet).setQueryScript(queryScript);
						((ConfigurableDataSet) dataSet).setQueryScriptLanguage(queryScriptLanguage);
					} else {
						logger.error("A datasource with label " + dataSourceLabel + " could not be found");
					}
				} catch (EMFUserError e) {
					logger.error("Error while retrieving Datasource with label=" + dataSourceLabel, e);
					e.printStackTrace();
				}
			}

		}

		if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_SCRIPT)) {
			dataSet = new ScriptDataSet();
			String script = getAttributeAsString(DataSetConstants.SCRIPT);
			String scriptLanguage = getAttributeAsString(DataSetConstants.SCRIPT_LANGUAGE);
			jsonDsConfig.put(DataSetConstants.SCRIPT, script);
			jsonDsConfig.put(DataSetConstants.SCRIPT_LANGUAGE, scriptLanguage);
			((ScriptDataSet) dataSet).setScript(script);
			((ScriptDataSet) dataSet).setScriptLanguage(scriptLanguage);
		}

		if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_JCLASS)) {
			dataSet = new JavaClassDataSet();
			String jclassName = getAttributeAsString(DataSetConstants.JCLASS_NAME);
			jsonDsConfig.put(DataSetConstants.JCLASS_NAME, jclassName);
			((JavaClassDataSet) dataSet).setClassName(jclassName);
		}

		if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_CUSTOM)) {
			CustomDataSet customDs = new CustomDataSet();
			String customData = getAttributeAsString(DataSetConstants.CUSTOM_DATA);
			jsonDsConfig.put(DataSetConstants.CUSTOM_DATA, customData);
			customDs.setCustomData(customData);
			String javaClassName = getAttributeAsString(DataSetConstants.JCLASS_NAME);
			jsonDsConfig.put(DataSetConstants.JCLASS_NAME, javaClassName);
			customDs.setJavaClassName(javaClassName);
			// customDs.init();
			// if custom type call the referred class extending
			// CustomAbstractDataSet
			try {
				dataSet = customDs.instantiate();
			} catch (Exception e) {
				logger.error("Cannot instantiate class " + customDs.getJavaClassName() + ": go on with CustomDatasetClass");
				throw new SpagoBIServiceException("Manage Dataset", "Cannot instantiate class " + javaClassName + ": check it extends AbstractCustomDataSet");
			}
		}

		if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_QBE)) {

			dataSet = new QbeDataSet();
			QbeDataSet qbeDataSet = (QbeDataSet) dataSet;
			String qbeDatamarts = getAttributeAsString(DataSetConstants.QBE_DATAMARTS);
			String dataSourceLabel = getAttributeAsString(DataSetConstants.QBE_DATA_SOURCE);
			String jsonQuery = getAttributeAsString(DataSetConstants.QBE_JSON_QUERY);
			jsonDsConfig.put(DataSetConstants.QBE_DATAMARTS, qbeDatamarts);
			jsonDsConfig.put(DataSetConstants.QBE_DATA_SOURCE, dataSourceLabel);
			jsonDsConfig.put(DataSetConstants.QBE_JSON_QUERY, jsonQuery);

			// START -> This code should work instead of CheckQbeDataSets around
			// the projects
			SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
			Map parameters = qbeDataSet.getParamsMap();
			if (parameters == null) {
				parameters = new HashMap();
				qbeDataSet.setParamsMap(parameters);
			}
			qbeDataSet.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
			logger.debug("Datamart retriever correctly added to Qbe dataset");
			// END

			qbeDataSet.setJsonQuery(jsonQuery);
			qbeDataSet.setDatamarts(qbeDatamarts);
			if (dataSourceLabel != null && !dataSourceLabel.trim().equals("")) {
				IDataSource dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(dataSourceLabel);
				qbeDataSet.setDataSource(dataSource);
			}

			String sourceDatasetLabel = getAttributeAsString(DataSetConstants.SOURCE_DS_LABEL);
			IDataSet sourceDataset = null;
			if (sourceDatasetLabel != null && !sourceDatasetLabel.trim().equals("")) {
				try {
					sourceDataset = DAOFactory.getDataSetDAO().loadDataSetByLabel(sourceDatasetLabel);
					if (sourceDataset == null) {
						throw new SpagoBIRuntimeException("Dataset with label [" + sourceDatasetLabel + "] does not exist");
					}
					qbeDataSet.setSourceDataset(sourceDataset);
					qbeDataSet.setDataSource(sourceDataset.getDataSource());
				} catch (Exception e) {
					throw new SpagoBIServiceException(SERVICE_NAME, "Cannot retrieve source dataset information", e);
				}
			}

		}

		if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_FEDERATED)) {

			Integer id = getAttributeAsInteger(DataSetConstants.DS_ID);
			if (id == null) {
				logger.error("The federated dataset id is null");
			}

			IDataSetDAO dao = DAOFactory.getDataSetDAO();
			dao.setUserProfile(getUserProfile());
			dataSet = dao.loadDataSetById(id);
			// if its a federated dataset the datasource are teh ones on cahce
			SQLDBCache cache = (SQLDBCache) CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
			dataSet.setDataSourceForReading(cache.getDataSource());
			dataSet.setDataSourceForWriting(cache.getDataSource());
			jsonDsConfig = new JSONObject(dataSet.getConfiguration());

			// update the json query getting the one passed from the qbe editor
			String jsonQuery = getAttributeAsString(DataSetConstants.QBE_JSON_QUERY);
			jsonDsConfig.put(DataSetConstants.QBE_JSON_QUERY, jsonQuery);
			((FederatedDataSet) (((VersionedDataSet) dataSet).getWrappedDataset())).setJsonQuery(jsonQuery);

		}

		if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_FLAT)) {
			dataSet = new FlatDataSet();
			FlatDataSet flatDataSet = (FlatDataSet) dataSet;
			String tableName = getAttributeAsString(DataSetConstants.FLAT_TABLE_NAME);
			String dataSourceLabel = getAttributeAsString(DataSetConstants.DATA_SOURCE_FLAT);
			jsonDsConfig.put(DataSetConstants.FLAT_TABLE_NAME, tableName);
			jsonDsConfig.put(DataSetConstants.DATA_SOURCE, dataSourceLabel);
			flatDataSet.setTableName(tableName);
			IDataSource dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(dataSourceLabel);
			flatDataSet.setDataSource(dataSource);
		}

		dataSet.setConfiguration(jsonDsConfig.toString());
		return dataSet;
	}

	private SPARQLDataSet manageSPARQLDataSet(boolean savingDataset, JSONObject config) throws JSONException {
		for (String sa : SPARQLDatasetConstants.SPARQL_ATTRIBUTES) {
			config.put(sa, getAttributeAsString(sa));
		}
		SPARQLDataSet res = new SPARQLDataSet(config);
		return res;
	}

	private RESTDataSet manageRESTDataSet(boolean savingDataset, JSONObject config) throws JSONException {
		for (String sa : RESTDataSetConstants.REST_STRING_ATTRIBUTES) {
			config.put(sa, getAttributeAsString(sa));
		}
		for (String ja : RESTDataSetConstants.REST_JSON_OBJECT_ATTRIBUTES) {
			config.put(ja, getAttributeAsJSONObject(ja));
		}
		for (String ja : RESTDataSetConstants.REST_JSON_ARRAY_ATTRIBUTES) {
			config.put(ja, getAttributeAsJSONArray(ja));
		}
		RESTDataSet res = new RESTDataSet(config);
		return res;
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

	private void deleteDatasetFile(String fileName, String resourcePath, String fileType) {
		String filePath = resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar + "temp" + File.separatorChar;

		File datasetFile = new File(filePath + fileName);
		if (datasetFile.exists()) {
			datasetFile.delete();
		}
	}

	private IDataSet setTransformer(IDataSet ds, String trasfTypeCd) {
		List<Domain> domainsTrasf = (List<Domain>) getSessionContainer().getAttribute("trasfTypesList");
		HashMap<String, Integer> domainTrasfIds = new HashMap<>();
		if (domainsTrasf != null) {
			for (int i = 0; i < domainsTrasf.size(); i++) {
				domainTrasfIds.put(domainsTrasf.get(i).getValueCd(), domainsTrasf.get(i).getValueId());
			}
		}
		Integer transformerId = domainTrasfIds.get(trasfTypeCd);

		String pivotColName = getAttributeAsString(DataSetConstants.PIVOT_COL_NAME);
		if (pivotColName != null) {
			pivotColName = pivotColName.trim();
		}
		String pivotColValue = getAttributeAsString(DataSetConstants.PIVOT_COL_VALUE);
		if (pivotColValue != null) {
			pivotColValue = pivotColValue.trim();
		}
		String pivotRowName = getAttributeAsString(DataSetConstants.PIVOT_ROW_NAME);
		if (pivotRowName != null) {
			pivotRowName = pivotRowName.trim();
		}
		Boolean pivotIsNumRows = getAttributeAsBoolean(DataSetConstants.PIVOT_IS_NUM_ROWS);

		if (pivotColName != null && !pivotColName.equals("")) {
			ds.setPivotColumnName(pivotColName);
		}
		if (pivotColValue != null && !pivotColValue.equals("")) {
			ds.setPivotColumnValue(pivotColValue);
		}
		if (pivotRowName != null && !pivotRowName.equals("")) {
			ds.setPivotRowName(pivotRowName);
		}
		if (pivotIsNumRows != null) {
			ds.setNumRows(pivotIsNumRows);
		}

		ds.setTransformerId(transformerId);

		if (ds.getPivotColumnName() != null && ds.getPivotColumnValue() != null && ds.getPivotRowName() != null) {
			ds.setDataStoreTransformer(new PivotDataSetTransformer(ds.getPivotColumnName(), ds.getPivotColumnValue(), ds.getPivotRowName(), ds.isNumRows()));
		}
		return ds;
	}

	protected JSONObject createJSONResponse(JSONArray rows, Integer totalResNumber) throws JSONException {
		JSONObject results;

		results = new JSONObject();
		results.put("total", totalResNumber);
		results.put("title", "Datasets");
		results.put("rows", rows);
		return results;
	}

	private String getDataSetParametersAsString() {
		String parametersString = null;

		try {
			JSONArray parsListJSON = getAttributeAsJSONArray(DataSetConstants.PARS);
			if (parsListJSON == null) {
				return null;
			}

			SourceBean sb = new SourceBean("PARAMETERSLIST");
			SourceBean sb1 = new SourceBean("ROWS");

			for (int i = 0; i < parsListJSON.length(); i++) {
				JSONObject obj = (JSONObject) parsListJSON.get(i);
				String name = obj.getString("name");
				String type = obj.getString("type");
				String defaultValue = obj.optString(DEFAULT_VALUE_PARAM);
				String multiValue = String.valueOf(obj.optBoolean("MULTIVALUE"));

				SourceBean b = new SourceBean("ROW");
				b.setAttribute("NAME", name);
				b.setAttribute("TYPE", type);
				b.setAttribute(DataSetParametersList.DEFAULT_VALUE_XML, defaultValue);
				b.setAttribute("MULTIVALUE", multiValue);
				sb1.setAttribute(b);
			}
			sb.setAttribute(sb1);
			parametersString = sb.toXML(false);
		} catch (Throwable t) {
			if (t instanceof SpagoBIServiceException) {
				throw (SpagoBIServiceException) t;
			}
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occured while deserializing dataset parameters", t);
		}
		return parametersString;
	}

	private HashMap<String, String> getDataSetParametersAsMap(boolean forSave) {
		HashMap<String, String> parametersMap = null;

		try {
			parametersMap = new HashMap<>();

			JSONArray parsListJSON = getAttributeAsJSONArray(DataSetConstants.PARS);
			if (parsListJSON == null) {
				return parametersMap;
			}

			for (int i = 0; i < parsListJSON.length(); i++) {
				JSONObject obj = (JSONObject) parsListJSON.get(i);
				String name = obj.getString("name");
				String type = null;
				if (obj.has("type")) {
					type = obj.getString("type");
				}

				// check if has value, if has not a valid value then use default
				// value
				boolean hasVal = obj.has(PARAM_VALUE_NAME) && !obj.getString(PARAM_VALUE_NAME).isEmpty();
				String tempVal = "";
				if (hasVal) {
					tempVal = obj.getString(PARAM_VALUE_NAME);
				} else {
					boolean hasDefaultValue = obj.has(DEFAULT_VALUE_PARAM);
					if (hasDefaultValue) {
						tempVal = obj.getString(DEFAULT_VALUE_PARAM);
						logger.debug("Value of param not present, use default value: " + tempVal);
					}
				}

				boolean multivalue = false;
				if (tempVal != null && tempVal.contains(",")) {
					multivalue = true;
				}

				String value = "";
				if (multivalue) {
					value = getMultiValue(tempVal, type);
				} else {
					value = getSingleValue(tempVal, type);
				}

				logger.debug("name: " + name + " / value: " + value);
				parametersMap.put(name, value);
			}
		} catch (Throwable t) {
			if (t instanceof SpagoBIServiceException) {
				throw (SpagoBIServiceException) t;
			}
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occured while deserializing dataset parameters", t);
		}
		return parametersMap;
	}

	/**
	 * Protected for testing purposes
	 *
	 * @param value
	 * @param type
	 * @param forSave
	 * @return
	 */
	static String getSingleValue(String value, String type) {
		String toReturn = "";
		value = value.trim();
		if (type.equalsIgnoreCase(DataSetUtilities.STRING_TYPE)) {
			if (!(value.startsWith("'") && value.endsWith("'"))) {
				toReturn = "'" + value + "'";
			} else {
				toReturn = value;
			}
		} else if (type.equalsIgnoreCase(DataSetUtilities.NUMBER_TYPE)) {

			if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
				toReturn = value.substring(1, value.length() - 1);
			} else {
				toReturn = value;
			}
			if (toReturn == null || toReturn.length() == 0) {
				toReturn = "";
			}
		} else if (type.equalsIgnoreCase(DataSetUtilities.GENERIC_TYPE)) {
			toReturn = value;
		} else if (type.equalsIgnoreCase(DataSetUtilities.RAW_TYPE)) {
			if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
				toReturn = value.substring(1, value.length() - 1);
			} else {
				toReturn = value;
			}
		}

		return toReturn;
	}

	private String getMultiValue(String value, String type) {
		String toReturn = "";

		String[] tempArrayValues = value.split(",");
		for (int j = 0; j < tempArrayValues.length; j++) {
			String tempValue = tempArrayValues[j];
			if (j == 0) {
				toReturn = getSingleValue(tempValue, type);
			} else {
				toReturn = toReturn + "," + getSingleValue(tempValue, type);
			}
		}

		return toReturn;
	}

	public JSONArray serializeJSONArrayParsList(String parsList) throws JSONException, SourceBeanException {
		JSONArray toReturn = new JSONArray();
		DataSetParametersList params = DataSetParametersList.fromXML(parsList);
		toReturn = ObjectUtils.toJSONArray(params.getItems());
		return toReturn;
	}

	// public String getDatasetTestMetadataAsString(IDataSet dataSet, HashMap
	// parametersFilled, IEngUserProfile profile, String metadata) throws
	// Exception {
	// logger.debug("IN");
	// String toReturn = null;
	// try {
	// DatasetMetadataParser dsp = new DatasetMetadataParser();
	// toReturn = dsp.metadataToXML(metaData);
	// IMetaData metaData = getDatasetTestMetadata(dataSet, parametersFilled,
	// profile, metadata);
	//
	// if (metaData == null)
	// return null;
	//
	//
	// } catch (Exception e) {
	// logger.error("Error while executing dataset for test purpose", e);
	// return null;
	// }
	//
	// logger.debug("OUT");
	// return toReturn;
	// }

	public IMetaData getDatasetTestMetadata(IDataSet dataSet, HashMap parametersFilled, IEngUserProfile profile, String metadata) throws Exception {
		logger.debug("IN");

		IDataStore dataStore = null;

		Integer start = new Integer(0);
		Integer limit = new Integer(10);

		dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));
		dataSet.setParamsMap(parametersFilled);
		try {
			// checkQbeDataset(dataSet);
			checkFileDataset(dataSet);
			dataSet.loadData(start, limit, GeneralUtilities.getDatasetMaxResults());
			dataStore = dataSet.getDataStore();
			// DatasetMetadataParser dsp = new DatasetMetadataParser();

			JSONArray metadataArray = JSONUtils.toJSONArray(metadata);

			IMetaData metaData = dataStore.getMetaData();
			for (int i = 0; i < metaData.getFieldCount(); i++) {
				IFieldMetaData ifmd = metaData.getFieldMeta(i);
				for (int j = 0; j < metadataArray.length(); j++) {
					if (ifmd.getName().equals((metadataArray.getJSONObject(j)).getString("name"))) {
						if (IFieldMetaData.FieldType.MEASURE.toString().equals((metadataArray.getJSONObject(j)).getString("fieldType"))) {
							ifmd.setFieldType(IFieldMetaData.FieldType.MEASURE);
						} else if (IFieldMetaData.FieldType.SPATIAL_ATTRIBUTE.toString().equals((metadataArray.getJSONObject(j)).getString("fieldType"))) {
							ifmd.setFieldType(IFieldMetaData.FieldType.SPATIAL_ATTRIBUTE);
						} else {
							ifmd.setFieldType(IFieldMetaData.FieldType.ATTRIBUTE);
						}
						break;
					}
				}
			}

			// dsMetadata = dsp.metadataToXML(dataStore.getMetaData());
		} catch (Exception e) {
			logger.error("Error while executing dataset for test purpose", e);
			throw e;
		}

		logger.debug("OUT");

		return dataStore.getMetaData();
	}

	public JSONObject getDatasetTestResultList(IDataSet dataSet, HashMap<String, String> parametersFilled, IEngUserProfile profile) {

		JSONObject dataSetJSON;

		logger.debug("IN");

		dataSetJSON = null;
		try {
			Integer start = -1;
			try {
				start = getAttributeAsInteger(DataSetConstants.START);
			} catch (NullPointerException e) {
				logger.info("start option undefined");
			}
			Integer limit = -1;
			try {
				limit = getAttributeAsInteger(DataSetConstants.LIMIT);
			} catch (NullPointerException e) {
				logger.info("limit option undefined");
			}

			dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));
			dataSet.setParamsMap(parametersFilled);
			// checkQbeDataset(dataSet);
			checkFileDataset(dataSet);
			IDataStore dataStore = null;
			try {
				if (dataSet.getTransformerId() != null) {
					dataStore = dataSet.test();
				} else {
					dataStore = dataSet.test(start, limit, GeneralUtilities.getDatasetMaxResults());
				}
				if (dataStore == null) {
					throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to read resultset");
				}
			} catch (Throwable t) {
				Throwable rootException = t;
				while (rootException.getCause() != null) {
					rootException = rootException.getCause();
				}
				String rootErrorMsg = rootException.getMessage() != null ? rootException.getMessage() : rootException.getClass().getName();
				if (dataSet instanceof JDBCDataSet) {
					JDBCDataSet jdbcDataSet = (JDBCDataSet) dataSet;
					if (jdbcDataSet.getQueryScript() != null) {
						QuerableBehaviour querableBehaviour = (QuerableBehaviour) jdbcDataSet.getBehaviour(QuerableBehaviour.class.getName());
						String statement = querableBehaviour.getStatement();
						rootErrorMsg += "\nQuery statement: [" + statement + "]";
					}
				}

				throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occured while executing dataset: " + rootErrorMsg, t);
			}

			try {
				JSONDataWriter dataSetWriter = new JSONDataWriter();
				dataSetJSON = (JSONObject) dataSetWriter.write(dataStore);
				if (dataSetJSON == null) {
					throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to read serialized resultset");
				}
			} catch (Exception t) {
				throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occured while serializing resultset", t);
			}
		} catch (Throwable t) {
			if (t instanceof SpagoBIServiceException) {
				throw (SpagoBIServiceException) t;
			}
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occured while getting dataset results", t);
		} finally {
			logger.debug("OUT");
		}

		return dataSetJSON;
	}

	private void checkFileDataset(IDataSet dataSet) {
		if (dataSet instanceof FileDataSet) {
			((FileDataSet) dataSet).setResourcePath(DAOConfig.getResourcePath());
		}
	}

	public JSONObject getJSONDatasetResult(Integer dsId, IEngUserProfile profile) {
		logger.debug("IN");
		JSONObject dataSetJSON = null;
		// Integer id = obj.getDataSetId();
		// gets the dataset object informations
		try {
			IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(dsId);
			if (dataset.getParameters() != null) {
				HashMap<String, String> parametersMap = new HashMap<>();
				parametersMap = getDataSetParametersAsMap(false);
				dataSetJSON = getDatasetTestResultList(dataset, parametersMap, profile);
			}
		} catch (Exception e) {
			logger.error("Error while executing dataset", e);
			return null;
		}
		logger.debug("OUT");
		return dataSetJSON;
	}

	private String filterList(JSONObject filtersJSON) throws JSONException {
		logger.debug("IN");
		boolean isAdmin = false;
		try {
			// Check if user is an admin
			isAdmin = profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN);
		} catch (EMFInternalError e) {
			logger.error("Error while filtering datasets");
		}
		String hsql = " from SbiDataSet h where h.active = true ";
		// Ad Admin can see other users' datasets
		if (!isAdmin) {
			hsql = hsql + " and h.owner = '" + ((UserProfile) profile).getUserId().toString() + "'";
		}
		if (filtersJSON != null) {
			String valuefilter = (String) filtersJSON.get(SpagoBIConstants.VALUE_FILTER);
			String typeFilter = (String) filtersJSON.get(SpagoBIConstants.TYPE_FILTER);
			String columnFilter = (String) filtersJSON.get(SpagoBIConstants.COLUMN_FILTER);
			if (typeFilter.equals("=")) {
				hsql += " and h." + columnFilter + " = '" + valuefilter + "'";
			} else if (typeFilter.equals("like")) {
				hsql += " and h." + columnFilter + " like '%" + valuefilter + "%'";
			}
		}
		logger.debug("OUT");
		return hsql;
	}

	private boolean isRemovingMetadataFields(IMetaData previousMetadata, IMetaData currentMetadata) {
		logger.debug("IN");

		ArrayList<String> previousFieldsName = new ArrayList<>();
		ArrayList<String> currentFieldsName = new ArrayList<>();

		for (int i = 0; i < previousMetadata.getFieldCount(); i++) {
			String field = previousMetadata.getFieldAlias(i);
			previousFieldsName.add(field);
		}
		for (int i = 0; i < currentMetadata.getFieldCount(); i++) {
			String field = currentMetadata.getFieldAlias(i);
			currentFieldsName.add(field);
		}
		// if number of columns is diminished return true
		if (previousFieldsName.size() > currentFieldsName.size()) {
			logger.warn("Cannot remove metadata from a dataset in use");
			return true;
		}
		// else check that all labels previously present are still present
		for (Iterator iterator = previousFieldsName.iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			if (!currentFieldsName.contains(name)) {
				logger.warn("Cannot remove field " + name + " of a dataset in use");
				return true;
			}
		}

		logger.debug("OUT");
		return false;
	}

	private void calculateDomainValues(IDataSet ds) throws NamingException, WorkException, InterruptedException {
		DatasetManagementAPI datasetManagementAPI = new DatasetManagementAPI((UserProfile) profile);
		datasetManagementAPI.calculateDomainValues(ds);
	}

	private void clearDomainValues(IDataSet ds) throws NamingException, WorkException {
		DatasetManagementAPI datasetManagementAPI = new DatasetManagementAPI((UserProfile) profile);
		datasetManagementAPI.clearDomainValues(ds);
	}

	public IEngUserProfile getProfile() {
		return profile;
	}

	public void setProfile(IEngUserProfile profile) {
		this.profile = profile;
	}

}
