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
package it.eng.spagobi.engines.qbe.services.dataset;

import it.eng.qbe.dataset.FederatedDataSet;
import it.eng.qbe.dataset.QbeDataSet;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFAbstractError;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.engines.qbe.services.core.catalogue.SetCatalogueAction;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.tools.dataset.bo.FlatDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 *
 *         This action is intended for final users; it saves a new dataset.
 *
 */
public class SaveDatasetUserAction extends SetCatalogueAction {

	private static final long serialVersionUID = 4801143200134017772L;

	public static final String SERVICE_NAME = "SAVE_DATASET_USER_ACTION";

	public static final String FLAT_TABLE_NAME_PREFIX = "SBI_FLAT_";

	public static final int FLAT_TABLE_NAME_LENGHT = 30; // Oracle supports maximum 30 characters table names

	// INPUT PARAMETERS
	public static final String LABEL = "LABEL";
	public static final String NAME = "NAME";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String FLAT_TABLE_NAME = "flatTableName";
	public static final String SCOPE_CD = "scopeCd";
	public static final String SCOPE_ID = "scopeId";
	public static final String CATEGORY_CD = "categoryCd";
	public static final String CATEGORY_ID = "categoryId";

	// loggers
	private static Logger logger = Logger.getLogger(SaveDatasetUserAction.class);

	@Override
	public void service(SourceBean request, SourceBean response) {
		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;

		logger.debug("IN");

		try {

			totalTimeMonitor = MonitorFactory.start("QbeEngine.saveDatasetUserAction.totalTime");

			super.handleTimeFilter = false;
			super.service(request, response);

			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName()
			+ " service before having properly created an instance of EngineInstance class");

			validateLabel();
			validateInput();

			IDataSet dataset = getEngineInstance().getActiveQueryAsDataSet();
			int datasetId = -1;
			// if (dataset instanceof HQLDataSet || dataset instanceof JPQLDataSet) {
			// dataset defined on a model --> save it as a Qbe dataset
			datasetId = this.saveQbeDataset(dataset);
			// } else {
			// // dataset defined on another dataset --> save it as a flat dataset
			// datasetId = this.saveFlatDataset(dataset);
			// }

			try {
				JSONObject obj = new JSONObject();
				obj.put("success", "true");
				obj.put("id", String.valueOf(datasetId));
				JSONSuccess success = new JSONSuccess(obj);
				writeBackToClient(success);
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

		} catch (Throwable t) {
			errorHitsMonitor = MonitorFactory.start("QbeEngine.errorHits");
			errorHitsMonitor.stop();
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			if (totalTimeMonitor != null)
				totalTimeMonitor.stop();
			logger.debug("OUT");
		}
	}

	private int saveQbeDataset(IDataSet dataset) {

		QbeDataSet newDataset = createNewQbeDataset(dataset);

		IDataSet datasetSaved = this.saveNewDataset(newDataset);

		int datasetId = datasetSaved.getId();
		return datasetId;
	}

	private QbeDataSet createNewQbeDataset(IDataSet dataset) {
		AbstractQbeDataSet qbeDataset = (AbstractQbeDataSet) dataset;

		QbeDataSet newDataset;

		UserProfile profile = (UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE);

		// if its a federated dataset we've to add the dependent datasets
		if (getEnv().get(EngineConstants.ENV_FEDERATION) != null) {

			FederationDefinition federation = (FederationDefinition) getEnv().get(EngineConstants.ENV_FEDERATION);
			// Object relations = (getEnv().get(EngineConstants.ENV_RELATIONS));
			// if (relations != null) {
			// federation.setRelationships(relations.toString());
			// } else {
			// logger.debug("No relation defined " + relations);
			// }
			//
			// federation.setLabel((getEnv().get(EngineConstants.ENV_FEDERATED_ID).toString()));
			// federation.setFederation_id(new Integer((String) (getEnv().get(EngineConstants.ENV_FEDERATED_ID))));

			newDataset = new FederatedDataSet(federation, (String) profile.getUserId());
			// ((FederatedDataSet) newDataset).setDependentDataSets(federation.getSourceDatasets());
			newDataset.setDataSourceForWriting((IDataSource) getEnv().get(EngineConstants.ENV_DATASOURCE));
			newDataset.setDataSourceForReading((IDataSource) getEnv().get(EngineConstants.ENV_DATASOURCE));
		} else {
			newDataset = new QbeDataSet();
		}

		newDataset.setLabel(getAttributeAsString(LABEL));
		newDataset.setName(getAttributeAsString(NAME));
		newDataset.setDescription(getAttributeAsString(DESCRIPTION));

		String scopeCd = null;
		Integer scopeId = null;
		String categoryCd = null;
		Integer categoryId = null;

		if (getAttributeAsInteger(SCOPE_ID) != null) {
			scopeCd = getAttributeAsString(SCOPE_CD);
			scopeId = getAttributeAsInteger(SCOPE_ID);
		} else {
			scopeCd = SpagoBIConstants.DS_SCOPE_USER;
		}

		if (getAttributeAsInteger(CATEGORY_ID) != null) {
			categoryCd = getAttributeAsString(CATEGORY_CD);
			categoryId = getAttributeAsInteger(CATEGORY_ID);
		} else {
			categoryCd = dataset.getCategoryCd();
			categoryId = dataset.getCategoryId();
		}
		if (categoryId == null
				&& (scopeCd.equalsIgnoreCase(SpagoBIConstants.DS_SCOPE_TECHNICAL) || scopeCd.equalsIgnoreCase(SpagoBIConstants.DS_SCOPE_ENTERPRISE))) {
			getErrorHandler().addError(
					new EMFValidationError(EMFErrorSeverity.ERROR, "category", "Dataset Enterprise or Technical must have a category", new ArrayList()));
			validateInput();
		}
		newDataset.setScopeCd(scopeCd);
		newDataset.setScopeId(scopeId);
		newDataset.setCategoryCd(categoryCd);
		newDataset.setCategoryId(categoryId);

		String owner = profile.getUserId().toString();
		// saves owner of the dataset
		newDataset.setOwner(owner);

		String metadata = getMetadataAsString(dataset);
		logger.debug("Dataset's metadata: [" + metadata + "]");
		newDataset.setDsMetadata(metadata);

		newDataset.setDataSource(qbeDataset.getDataSource());

		String datamart = qbeDataset.getStatement().getDataSource().getConfiguration().getModelName();
		String datasource = qbeDataset.getDataSource().getLabel();
		String jsonQuery;
		try {
			jsonQuery = new String(getEngineInstance().getAnalysisState().store());
		} catch (SpagoBIEngineException e) {
			throw new SpagoBIEngineRuntimeException("Error while serializing engine state", e);
		}

		JSONObject jsonConfig = new JSONObject();
		try {
			jsonConfig.put(QbeDataSet.QBE_DATA_SOURCE, datasource);
			jsonConfig.put(QbeDataSet.QBE_DATAMARTS, datamart);
			jsonConfig.put(QbeDataSet.QBE_JSON_QUERY, jsonQuery);
			jsonConfig.put(FederatedDataSet.QBE_DATASET_CACHE_MAP, getEnv().get(EngineConstants.ENV_DATASET_CACHE_MAP));
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Error while creating dataset's JSON config", e);
		}

		newDataset.setConfiguration(jsonConfig.toString());

		// get Persist and scheduling informations
		boolean isPersisted = getAttributeAsBoolean("isPersisted");
		newDataset.setPersisted(isPersisted);
		boolean isScheduled = getAttributeAsBoolean("isScheduled");
		newDataset.setScheduled(isScheduled);
		if (getAttributeAsString("persistTable") != null) {
			String persistTable = getAttributeAsString("persistTable").toString();
			newDataset.setPersistTableName(persistTable);
		}
		if (getAttributeAsString("startDateField") != null) {
			String startDateField = getAttributeAsString("startDateField").toString();
			newDataset.setStartDateField(startDateField);
		}
		if (getAttributeAsString("endDateField") != null) {
			String endDateField = getAttributeAsString("endDateField").toString();
			newDataset.setEndDateField(endDateField);
		}
		if (getAttributeAsString("schedulingCronLine") != null) {
			String schedulingCronLine = getAttributeAsString("schedulingCronLine").toString();
			newDataset.setSchedulingCronLine(schedulingCronLine);
		}

		String meta = getAttributeAsString("meta");

		try {

			JSONArray metadataArray = JSONUtils.toJSONArray(meta);

			IMetaData metaData = dataset.getMetadata();
			for (int i = 0; i < metaData.getFieldCount(); i++) {
				IFieldMetaData ifmd = metaData.getFieldMeta(i);
				for (int j = 0; j < metadataArray.length(); j++) {

					String fieldAlias = ifmd.getAlias() != null ? ifmd.getAlias() : "";
					// remove dataset source
					String fieldName = ifmd.getName().substring(ifmd.getName().indexOf(':') + 1);

					if (fieldAlias.equals((metadataArray.getJSONObject(j)).getString("name"))
							|| fieldName.equals((metadataArray.getJSONObject(j)).getString("name"))) {
						if ("MEASURE".equals((metadataArray.getJSONObject(j)).getString("fieldType"))) {
							ifmd.setFieldType(IFieldMetaData.FieldType.MEASURE);
						} else {
							ifmd.setFieldType(IFieldMetaData.FieldType.ATTRIBUTE);
						}
						break;
					}
				}
			}

			DatasetMetadataParser dsp = new DatasetMetadataParser();
			String dsMetadata = dsp.metadataToXML(metaData);

			newDataset.setDsMetadata(dsMetadata);

		} catch (Exception e) {
			logger.error("Error in calculating metadata");
			throw new SpagoBIRuntimeException("Error in calculating metadata", e);
		}

		return newDataset;
	}

	private int saveFlatDataset(IDataSet dataset) {
		IDataSetTableDescriptor descriptor = persistCurrentDataset(dataset);

		IDataSet newDataset = createNewFlatDataSet(dataset, descriptor);
		IDataSet datasetSaved = saveNewDataset(newDataset);

		int datasetId = datasetSaved.getId();
		return datasetId;
	}
	private void validateLabel() {
		String label = getAttributeAsString(LABEL);
		DataSetServiceProxy proxy = (DataSetServiceProxy) getEnv().get(EngineConstants.ENV_DATASET_PROXY);
		IDataSet dataset = proxy.getDataSetByLabel(label);
		if (dataset != null) {
			getErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, "label", "Label already in use", new ArrayList()));
		}
	}

	public void validateInput() {
		EMFErrorHandler errorHandler = getErrorHandler();
		if (!errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
			Collection errors = errorHandler.getErrors();
			Iterator it = errors.iterator();
			while (it.hasNext()) {
				EMFAbstractError error = (EMFAbstractError) it.next();
				if (error.getSeverity().equals(EMFErrorSeverity.ERROR)) {
					throw new SpagoBIEngineServiceException(getActionName(), error.getMessage(), null);
				}
			}
		}
	}

	private IDataSet createNewFlatDataSet(IDataSet dataset, IDataSetTableDescriptor descriptor) {
		logger.debug("IN");

		UserProfile profile = (UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE);
		String owner = profile.getUserId().toString();

		FlatDataSet flatFataSet = new FlatDataSet();

		flatFataSet.setLabel(getAttributeAsString(LABEL));
		flatFataSet.setName(getAttributeAsString(NAME));
		flatFataSet.setDescription(getAttributeAsString(DESCRIPTION));

		flatFataSet.setCategoryCd(dataset.getCategoryCd());
		flatFataSet.setCategoryId(dataset.getCategoryId());
		// saves owner of the dataset
		flatFataSet.setOwner(owner);
		// saves scope which is always "USER"
		flatFataSet.setScopeCd(SpagoBIConstants.DS_SCOPE_USER);

		JSONObject jsonConfig = new JSONObject();
		try {
			jsonConfig.put(FlatDataSet.FLAT_TABLE_NAME, descriptor.getTableName());
			jsonConfig.put(FlatDataSet.DATA_SOURCE, descriptor.getDataSource().getLabel());
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Error while creating dataset's JSON config", e);
		}

		flatFataSet.setTableName(descriptor.getTableName());
		flatFataSet.setDataSource(descriptor.getDataSource());
		flatFataSet.setConfiguration(jsonConfig.toString());

		String metadata = getMetadataAsString(dataset, descriptor);
		logger.debug("Dataset's metadata: [" + metadata + "]");
		flatFataSet.setDsMetadata(metadata);

		logger.debug("OUT");
		return flatFataSet;
	}

	private String getMetadataAsString(IDataSet dataset, IDataSetTableDescriptor descriptor) {
		IMetaData metadata = getDataSetMetadata(dataset);
		MetaData newMetadata;
		try {
			newMetadata = (MetaData) ((MetaData) metadata).clone();
		} catch (CloneNotSupportedException e) {
			throw new SpagoBIRuntimeException("Error while cloning dataset's metadata", e);
		}

		for (int i = 0; i < metadata.getFieldCount(); i++) {
			IFieldMetaData fieldMetadata = metadata.getFieldMeta(i);
			IFieldMetaData newFieldMetadata = newMetadata.getFieldMeta(i);
			String columnName = descriptor.getColumnName(fieldMetadata.getName());
			newFieldMetadata.setName(columnName);
		}

		DatasetMetadataParser parser = new DatasetMetadataParser();
		String toReturn = parser.metadataToXML(newMetadata);
		return toReturn;
	}
	private String getMetadataAsString(IDataSet dataset) {
		IMetaData metadata = getDataSetMetadata(dataset);
		DatasetMetadataParser parser = new DatasetMetadataParser();
		String toReturn = parser.metadataToXML(metadata);
		return toReturn;
	}

	private IMetaData getDataSetMetadata(IDataSet dataset) {
		IMetaData metaData = null;
		Integer start = new Integer(0);
		Integer limit = new Integer(10);
		Integer maxSize = QbeEngineConfig.getInstance().getResultLimit();
		try {
			dataset.loadData(start, limit, maxSize);
			IDataStore dataStore = dataset.getDataStore();
			metaData = dataStore.getMetaData();
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while executing dataset", e);
		}
		return metaData;
	}

	private IDataSet saveNewDataset(IDataSet newDataset) {
		DataSetServiceProxy proxy = (DataSetServiceProxy) getEnv().get(EngineConstants.ENV_DATASET_PROXY);
		logger.debug("Saving new dataset ...");
		IDataSet saved = proxy.saveDataSet(newDataset);
		logger.debug("Dataset saved without errors");
		return saved;
	}

	private IDataSetTableDescriptor persistCurrentDataset(IDataSet dataset) {
		// gets the name of the table that will contain data
		IDataSetTableDescriptor descriptor = null;
		HttpSession session = this.getHttpSession();
		synchronized (session) { // we synchronize this block in order to avoid concurrent requests
			String flatTableName = getFlatTableName();
			logger.debug("Flat table name : [" + flatTableName + "]");
			IDataSource dataSource = getEngineInstance().getDataSourceForWriting();
			logger.debug("Persisting working dataset ...");
			descriptor = dataset.persist(flatTableName, dataSource);
			logger.debug("Working dataset persisted");
		}
		return descriptor;
	}

	private String getFlatTableName() {
		logger.debug("IN");
		String persistTableName = FLAT_TABLE_NAME_PREFIX + StringUtilities.getRandomString(FLAT_TABLE_NAME_LENGHT - FLAT_TABLE_NAME_PREFIX.length());
		logger.debug("OUT : returning [" + persistTableName + "]");
		return persistTableName;
	}

}
