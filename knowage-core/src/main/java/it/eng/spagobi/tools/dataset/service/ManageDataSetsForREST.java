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

/**
 * Authors: Nikola Simović (nikola.simovic@mht.net)
 */

package it.eng.spagobi.tools.dataset.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.commons.security.PathTraversalChecker;
import it.eng.knowage.parameter.ParameterManagerFactory;
import it.eng.qbe.dataset.DerivedDataSet;
import it.eng.qbe.dataset.FederatedDataSet;
import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.cache.dao.ICacheDAO;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.serializer.DataSetMetadataJSONSerializer;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.ConfigurableDataSet;
import it.eng.spagobi.tools.dataset.bo.CustomDataSet;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersList;
import it.eng.spagobi.tools.dataset.bo.FacetSolrDataSet;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.FlatDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDatasetFactory;
import it.eng.spagobi.tools.dataset.bo.JavaClassDataSet;
import it.eng.spagobi.tools.dataset.bo.MongoDataSet;
import it.eng.spagobi.tools.dataset.bo.PreparedDataSet;
import it.eng.spagobi.tools.dataset.bo.PythonDataSet;
import it.eng.spagobi.tools.dataset.bo.RESTDataSet;
import it.eng.spagobi.tools.dataset.bo.SPARQLDataSet;
import it.eng.spagobi.tools.dataset.bo.ScriptDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.constants.PythonDataSetConstants;
import it.eng.spagobi.tools.dataset.constants.RESTDataSetConstants;
import it.eng.spagobi.tools.dataset.constants.SPARQLDatasetConstants;
import it.eng.spagobi.tools.dataset.constants.SolrDataSetConstants;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.metadata.mapping.MetaDataMapping;
import it.eng.spagobi.tools.dataset.persist.IPersistedManager;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.dataset.utils.datamart.SpagoBICoreDatamartRetriever;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.cache.CacheItem;
import it.eng.spagobi.utilities.exceptions.ActionNotPermittedException;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.json.JSONUtils;
import it.eng.spagobi.utilities.sql.SqlUtils;

public class ManageDataSetsForREST {

	// logger component
	private static final Logger LOGGER = Logger.getLogger(ManageDataSetsForREST.class);
	private static final Logger AUDIT_LOGGER = Logger.getLogger("dataset.audit");

	private static final ManageDataSetsForREST INSTANCE = new ManageDataSetsForREST();
	private static final String PARAM_VALUE_NAME = "value";

	public static final String DEFAULT_VALUE_PARAM = "defaultValue";
	public static final String JOB_GROUP = "PersistDatasetExecutions";
	public static final String SERVICE_NAME = "ManageDatasets";
	public static final String DRIVERS = "DRIVERS";

	public static final String PERSONAL = "personal";
	public static final String MASKED = "masked";
	public static final String DECRYPT = "decrypt";
	public static final String SUBJECT_ID = "subjectId";

	public static ManageDataSetsForREST getInstance() {
		return INSTANCE;
	}

	private ManageDataSetsForREST() {
	}

	public String previewDataset(String jsonString, UserProfile userProfile) {
		JSONObject json = null;
		try {
			json = new JSONObject(jsonString);
		} catch (JSONException e) {
			LOGGER.error("Cannot get values from JSON object while previewing dataset", e);
			throw new SpagoBIServiceException(SERVICE_NAME,
					"Impossible to preview Data Set due to bad formated json of data set.");

		}

		return datasetTest(json, userProfile);
	}

	/**
	 * @param jsonString
	 * @param dsDao
	 * @param locale
	 * @param userProfile
	 * @param req
	 * @return
	 * @throws JSONException
	 */
	public String insertDataset(String jsonString, IDataSetDAO dsDao, Locale locale, UserProfile userProfile,
			HttpServletRequest req) throws JSONException {
		LOGGER.debug("IN");
		JSONObject json = new JSONObject(jsonString);
		LOGGER.debug("OUT");
		return datasetInsert(json, dsDao, locale, userProfile, req);
	}

	protected String datasetInsert(JSONObject json, IDataSetDAO dsDao, Locale locale, UserProfile userProfile,
			HttpServletRequest req) throws JSONException {
		IDataSet ds = getGuiGenericDatasetToInsert(json, userProfile);

		try {
			new DatasetManagementAPI(userProfile).canSave(ds);
		} catch (ActionNotPermittedException e) {
			LOGGER.error("User " + userProfile.getUserId() + " cannot save the dataset with label " + ds.getLabel());
			throw new SpagoBIRestServiceException(e.getI18NCode(), locale,
					"User " + userProfile.getUserId() + " cannot save the dataset with label " + ds.getLabel(), e,
					"MessageFiles.messages");
		}

		return datasetInsert(ds, dsDao, locale, userProfile, json, req);
	}

	/**
	 * @param ds
	 * @param dsDao
	 */
	private void datasetUpdateOldVersions(IDataSet ds, IDataSetDAO dsDao) {

		for (IDataSet oldVersionDs : dsDao.loadDataSetOlderVersions(ds.getId())) {
			oldVersionDs.setName(ds.getName());
			oldVersionDs.setLabel(ds.getLabel());

			dsDao.modifyDataSet(oldVersionDs);
		}

	}

	protected IDataSet getGuiGenericDatasetToInsert(JSONObject json, UserProfile userProfile) throws JSONException {

		IDataSet ds = null;

		try {
			String label = json.getString("label");
			String name = json.getString("name");
			String description = json.optString("description");

			String datasetTypeCode = json.getString("dsTypeCd");
			Assert.assertNotNull(datasetTypeCode, "Dataset type code cannot be null");

			String datasetTypeName = getDatasetTypeName(datasetTypeCode, userProfile);
			Boolean isFromSaveNoMetadata = json.optBoolean("isFromSaveNoMetadata");

			Assert.assertNotNull(name, "Dataset name cannot be null");
			Assert.assertNotNull(label, "Dataset label cannot be null");
			Assert.assertNotNull(datasetTypeName, "Dataset type cannot be null");
			Assert.assertTrue(!datasetTypeName.isEmpty(), "Dataset type cannot be empty");

			ds = getDataSet(datasetTypeName, true, json, userProfile);
			Assert.assertNotNull(ds, "Dataset with type " + datasetTypeName + " is null");
			ds.setLabel(label);
			ds.setName(name);

			if (description != null && !description.equals("")) {
				ds.setDescription(description);
			}
			ds.setDsType(datasetTypeName);

			String catTypeCd = json.optString("catTypeVn");

			String meta = json.optString("meta");
			JSONArray metaAsJSONArray = json.optJSONArray("meta");

			List<Domain> domainsCat = getCategories(userProfile);
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

			List<Domain> domainsScope = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.DS_SCOPE);
			HashMap<String, Integer> domainScopeIds = new HashMap<>();
			if (domainsScope != null) {
				for (int i = 0; i < domainsScope.size(); i++) {
					domainScopeIds.put(domainsScope.get(i).getValueCd(), domainsScope.get(i).getValueId());
				}
			}
			String scopeCode = json.getString("scopeCd");
			Integer scopeID = domainScopeIds.get(scopeCode.toUpperCase());
			if (scopeID == null) {
				LOGGER.error("Impossible to save Data Set. The scope in not set");
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to save Data Set. The scope in not set");
			} else {
				ds.setScopeCd(scopeCode);
				ds.setScopeId(scopeID);
			}

			if (scopeCode.equalsIgnoreCase(SpagoBIConstants.DS_SCOPE_ENTERPRISE)
					|| scopeCode.equalsIgnoreCase(SpagoBIConstants.DS_SCOPE_TECHNICAL)) {
				if (catTypeCd == null || catTypeCd.equals("")) {
					LOGGER.error(
							"Impossible to save DataSet. The category is mandatory for Data Set Enterprise or Technical");
					throw new SpagoBIServiceException(SERVICE_NAME,
							"Impossible to save DataSet. The category is mandatory for Data Set Enterprise or Technical");
				}
			}

			if (metaAsJSONArray != null) {
				manageDataSetMetadataV2(metaAsJSONArray, ds);
			}

			String pars = getDataSetParametersAsString(json);
			if (pars != null) {
				ds.setParameters(pars);
			}


			IDataSet dsRecalc = getDataSet(datasetTypeName, true, json, userProfile);
			Assert.assertNotNull(dsRecalc, "Cannot be null");

			// 1* String recalculateMetadata =
			// this.getAttributeAsString(DataSetConstants.RECALCULATE_METADATA);
			String recalculateMetadata = json.optString("recalculateMetadata");
			String dsMetadata = null;
			if ((recalculateMetadata == null || recalculateMetadata.trim().equals("yes")
					|| recalculateMetadata.trim().equals("") || recalculateMetadata.trim().equals("true"))
					&& (!isFromSaveNoMetadata)) {
				// recalculate metadata
				LOGGER.debug("Recalculating dataset's metadata: executing the dataset...");
				Map<String, String> parametersMap = getDataSetParametersAsMap(json);

				IEngUserProfile profile = userProfile;
				ds.setPersisted(false);
				ds.setPersistedHDFS(false);

				IMetaData currentMetadata = null;
				try {
					if (ds.getDsType().equals(DataSetConstants.DS_PREPARED)) {
						currentMetadata = getPreparedDsMeta(meta);
					} else if (ds.getDsType().equals(DataSetConstants.DS_DERIVED)) {
						String sourceDatasetLabel = json.optString(DataSetConstants.SOURCE_DS_LABEL);
						IDataSet sourceDataset = null;
						if (sourceDatasetLabel != null && !sourceDatasetLabel.trim().equals("")) {
							try {
								sourceDataset = DAOFactory.getDataSetDAO().loadDataSetByLabel(sourceDatasetLabel);
								if (sourceDataset == null) {
									throw new SpagoBIRuntimeException(
											"Dataset with label [" + sourceDatasetLabel + "] does not exist");
								}

								// TODO Add file not persisted management. Use another variable instead of persistTableName
//								String persistTableName = null;
//								if (json.has("persistTableName") && StringUtils.isNotEmpty(json.getString("persistTableName"))) {
//									persistTableName = json.getString("persistTableName");
//									sourceDataset.setPersistTableName(persistTableName);
//									sourceDataset.setPersisted(true);
//									((DerivedDataSet) dsRecalc).setSourceDataset(sourceDataset);
//								}
								if (sourceDataset.getDataSourceForReading() == null
										&& sourceDataset.getDataSourceForWriting() != null) {
									sourceDataset.setDataSourceForReading(sourceDataset.getDataSource());
								}
								((DerivedDataSet) ds).setSourceDataset(sourceDataset);

								currentMetadata = getDatasetTestMetadata(dsRecalc, parametersMap, profile, meta);

								JSONObject sourceJsonConfig = new JSONObject(sourceDataset.getConfiguration());
								JSONObject dsJsonConfig = new JSONObject(ds.getConfiguration());
								if (sourceDataset instanceof VersionedDataSet) {
									VersionedDataSet vds = (VersionedDataSet) sourceDataset;
									if (!(vds.getWrappedDataset() instanceof FileDataSet)) {
										dsJsonConfig.put(DataSetConstants.SOURCE_DATA_SOURCE,
												sourceDataset.getDataSource().getLabel());
									} else {
										dsJsonConfig.put(DataSetConstants.SOURCE_DATA_SOURCE,
												sourceDataset.getDataSourceForReading().getLabel());
									}
									if (vds.getWrappedDataset() instanceof AbstractJDBCDataset) {
										String sqlQuery = ((DerivedDataSet) ds).getStatement()
												.getQuerySQLString(sourceJsonConfig.getString("Query"));
										dsJsonConfig.put("sqlQuery", sqlQuery);
									} else {
										dsJsonConfig.put("sqlQuery",
												((DerivedDataSet) ds).getStatement().getQueryString());
									}
								}
								dsJsonConfig.put(DataSetConstants.SOURCE_DS_LABEL, sourceDatasetLabel);
								ds.setConfiguration(dsJsonConfig.toString());
							} catch (Exception e) {
								throw new SpagoBIServiceException(SERVICE_NAME,
										"Cannot retrieve source dataset information", e);
							}
						}
					} else {
						currentMetadata = getDatasetTestMetadata(dsRecalc, parametersMap, profile, meta);
					}
				} catch (Exception e) {
					LOGGER.error("Error while recovering dataset metadata: check dataset definition ", e);
					throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.test.error.metadata", e);
				}

				// check if there are metadata field with same
				// columns or aliases
				List<String> aliases = new ArrayList<>();
				for (int i = 0; i < currentMetadata.getFieldCount(); i++) {
					String alias = currentMetadata.getFieldAlias(i);
					if (aliases.contains(alias)) {
						LOGGER.error("Cannot save dataset cause preview revealed that two columns with name " + alias
								+ " exist; change aliases");
						throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.test.error.duplication");
					}
					aliases.add(alias);
				}

				DatasetMetadataParser dsp = new DatasetMetadataParser();
				dsMetadata = dsp.metadataToXML(currentMetadata);
				LogMF.debug(LOGGER, "Dataset executed, metadata are [{0}]", dsMetadata);

				// compare current metadata with previous
				// metadata if dataset is in use
				// 2* String previousId =
				// getAttributeAsString(DataSetConstants.ID);
				String previousId = json.optString("id");
				if (previousId != null && !previousId.equals("")) {
					Integer previousIdInteger = Integer.valueOf(previousId);
					if (previousIdInteger != 0) {

						// Check if dataset is used by objects
						// or by federations

						List<BIObject> objectsUsing = null;
						try {
							objectsUsing = DAOFactory.getBIObjDataSetDAO().getBIObjectsUsingDataset(previousIdInteger);
						} catch (Exception e) {
							LOGGER.error("Error while getting dataset metadataa", e);
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
							LOGGER.debug("dataset " + ds.getLabel() + " is used by some " + objectsUsing.size()
									+ "objects or some " + federationsAssociated.size() + " federations");
							// get the previous dataset
							IDataSet dataSet = null;
							try {
								dataSet = DAOFactory.getDataSetDAO().loadDataSetById(previousIdInteger);
							} catch (Exception e) {
								LOGGER.error("Error while getting dataset metadataa", e);
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

				LogMF.debug(LOGGER, "Dataset executed, metadata are [{0}]", dsMetadata);
			} else if (Boolean.FALSE.equals(isFromSaveNoMetadata)) {
				// load existing metadata
				LOGGER.debug("Loading existing dataset...");
				// 3* String id =
				// getAttributeAsString(DataSetConstants.ID);
				String id = json.optString("id");
				if (id != null && !id.equals("") && !id.equals("0")) {
					IDataSet existingDataSet = null;

					try {
						existingDataSet = DAOFactory.getDataSetDAO().loadDataSetById(new Integer(id));
					} catch (Exception e) {
						LOGGER.error("Error while getting dataset metadataa", e);
						throw e;
					}

					dsMetadata = existingDataSet.getDsMetadata();
					LogMF.debug(LOGGER, "Reloaded metadata : [{0}]", dsMetadata);
				} else {
					// when saving a NEW dataset without
					// metadata, and there is no ID
					dsMetadata = "";
				}
			} else {// just isFromSaveNoMetadata
				LOGGER.debug("Saving dataset without metadata. I'll add empty metadata with version = -1");
				DatasetMetadataParser dsp = new DatasetMetadataParser();
				dsMetadata = dsp.buildNoMetadataXML();
			}
			ds.setDsMetadata(dsMetadata);

			// MOVED SECTION OF CODE IN THE getDataset Method, otherwise the preview will not use the isPersisted flag
			getPersistenceInfo(ds, json);
		} catch (Exception e) {
			if (e instanceof SpagoBIServiceException) {
				throw (SpagoBIServiceException) e;
			}
			LOGGER.error("Erro while updating dataset metadata, cannot save the dataset", e);
			throw new SpagoBIServiceException(SERVICE_NAME,
					"Error while updating dataset metadata, cannot save the dataset");

		}
		return ds;
	}

	private IMetaData getPreparedDsMeta(String sparkMetadata) throws JSONException {
		IMetaData toReturn = new MetaData();
		JSONArray metaArray = new JSONArray(sparkMetadata);
		for (int i = 0; i < metaArray.length(); i++) {
			JSONObject metaObj = metaArray.getJSONObject(i);
			String alias = metaObj.optString("displayedName");
			String name = metaObj.optString("name");
			FieldType fieldType = getFieldType(metaObj.optString("fieldType"));
			Class type = getType(metaObj.optString("type"));
			FieldMetadata newMeta = new FieldMetadata();
			newMeta.setName(name);
			newMeta.setAlias(alias);
			newMeta.setType(type);
			newMeta.setFieldType(fieldType);
			newMeta.setPersonal(metaObj.optBoolean("personal"));
//			newMeta.setMasked(metaObj.optBoolean("masked"));
			newMeta.setDecrypt(metaObj.optBoolean("decrypt"));
			newMeta.setSubjectId(metaObj.optBoolean("subjectId"));
			newMeta.setDescription(metaObj.optString("description"));
			toReturn.addFiedMeta(newMeta);
		}
		return toReturn;
	}

	private Class getType(String type) {
		Class toReturn = null;
		try {
			toReturn = Class.forName(type);
		} catch (ClassNotFoundException e) {
			LOGGER.error("Cannot instantiate class {" + type + "}, returning String.class by default");
			toReturn = String.class;
		}
		return toReturn;
	}

	private FieldType getFieldType(String fieldType) {
		FieldType toReturn;
		if (fieldType.equalsIgnoreCase("ATTRIBUTE")) {
			toReturn = IFieldMetaData.FieldType.ATTRIBUTE;
		} else if (fieldType.equalsIgnoreCase("SPATIAL_ATTRIBUTE")) {
			toReturn = IFieldMetaData.FieldType.SPATIAL_ATTRIBUTE;
		} else if (fieldType.equalsIgnoreCase("MEASURE")) {
			toReturn = IFieldMetaData.FieldType.MEASURE;
		} else {
			throw new SpagoBIRuntimeException("Cannot map fieldType {" + fieldType + "}");
		}
		return toReturn;
	}

	private String getDatasetTypeName(String datasetTypeCode, UserProfile userProfile) throws SpagoBIException {
		Assert.assertNotNull(datasetTypeCode, "Parameter datasetTypeCode cannot be null");
		Assert.assertTrue(!datasetTypeCode.isEmpty(), "Parameter datasetTypeCode cannot be empty");
		String datasetTypeName = null;
		try {
			List<Domain> datasetTypes = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.DATA_SET_TYPE);

			for (Domain datasetType : datasetTypes) {
				if (datasetTypeCode.equalsIgnoreCase(datasetType.getValueCd())) {
					datasetTypeName = datasetType.getValueName();
					break;
				}
			}
		} catch (Exception e) {
			throw new SpagoBIException(
					"An unexpected error occured while resolving dataset type name from dataset type code ["
							+ datasetTypeCode + "]",
					e);
		}

		return datasetTypeName;
	}

	private IDataSet getDataSet(String datasetTypeName, boolean savingDataset, JSONObject json, UserProfile userProfile)
			throws Exception {

		IDataSet toReturn = null;
		JSONObject jsonDsConfig = new JSONObject();

		if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_FILE)) {
			toReturn = manageFileDataSet(savingDataset, jsonDsConfig, json);
		} else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_REST_TYPE)) {
			toReturn = manageRESTDataSet(savingDataset, jsonDsConfig, json);
		} else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_PYTHON_TYPE)) {
			toReturn = managePythonDataSet(savingDataset, jsonDsConfig, json);
		} else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_SPARQL)) {
			toReturn = manageSPARQLDataSet(savingDataset, jsonDsConfig, json);
		} else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_SOLR_TYPE)) {
			toReturn = manageSolrDataSet(savingDataset, jsonDsConfig, json, userProfile);
		} else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_QUERY)) {
			toReturn = manageQueryDataSet(savingDataset, jsonDsConfig, json);
		} else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_SCRIPT)) {
			toReturn = manageScriptDataSet(savingDataset, jsonDsConfig, json);
		} else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_JCLASS)) {
			toReturn = manageJavaClassDataSet(savingDataset, jsonDsConfig, json);
		} else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_CUSTOM)) {
			toReturn = manageCustomDataSet(savingDataset, jsonDsConfig, json);
		} else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_QBE)) {
			toReturn = manageQbeDataSet(savingDataset, jsonDsConfig, json, userProfile);
		} else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_FEDERATED)) {
			toReturn = manageFederatedDataSet(savingDataset, jsonDsConfig, json, userProfile);
		} else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_FLAT)) {
			toReturn = manageFlatDataSet(savingDataset, jsonDsConfig, json);
		} else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_PREPARED)) {
			toReturn = managePreparedDataSet(savingDataset, jsonDsConfig, json);
		} else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_DERIVED)) {
			toReturn = manageDerivedDataSet(savingDataset, jsonDsConfig, json, userProfile);
		} else {
			throw new SpagoBIRuntimeException("Cannot find a match with dataset type " + datasetTypeName);
		}

		toReturn.setConfiguration(jsonDsConfig.toString());
		return toReturn;
	}

	public List getCategories(UserProfile userProfile) {
		IRoleDAO rolesDao = null;
		Role role = new Role();
		try {
			UserProfile profile = userProfile;
			rolesDao = DAOFactory.getRoleDAO();
			rolesDao.setUserProfile(profile);
			ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();
			if (UserUtilities.hasDeveloperRole(profile) && !UserUtilities.hasAdministratorRole(profile)) {
				List<Domain> categoriesDev = new ArrayList<>();
				Collection<String> roles = profile.getRolesForUse();
				Iterator<String> itRoles = roles.iterator();
				while (itRoles.hasNext()) {
					String roleName = itRoles.next();
					role = rolesDao.loadByName(roleName);
					List<RoleMetaModelCategory> ds = rolesDao.getMetaModelCategoriesForRole(role.getId());
					List<Domain> array = categoryDao.getCategoriesForDataset().stream().map(Domain::fromCategory)
							.collect(Collectors.toList());
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
				return categoryDao.getCategoriesForDataset().stream().map(Domain::fromCategory)
						.collect(Collectors.toList());
			}
		} catch (Exception e) {
			LOGGER.error("Role with selected id: " + role.getId() + " doesn't exists", e);
			throw new SpagoBIRuntimeException("Item with selected id: " + role.getId() + " doesn't exists", e);
		}
	}

	private String getDataSetParametersAsString(JSONObject json) {
		String parametersString = null;

		try {
			JSONArray parsListJSON = json.optJSONArray(DataSetConstants.PARS);
			if (parsListJSON == null) {
				return null;
			}

			if (hasDuplicates(getDataSetParametersAsMap(json), parsListJSON)) {
				LOGGER.error("duplicated parameter names");
				throw new SpagoBIServiceException(SERVICE_NAME, "duplicated parameter names");
			}

			SourceBean sb = new SourceBean("PARAMETERSLIST");
			SourceBean sb1 = new SourceBean("ROWS");

			for (int i = 0; i < parsListJSON.length(); i++) {
				JSONObject obj = (JSONObject) parsListJSON.get(i);
				String name = obj.optString("name");
				String type = obj.optString("type");
				String multiValue = obj.optString("multiValue");
				String defaultValue = obj.optString(DEFAULT_VALUE_PARAM);

				SourceBean b = new SourceBean("ROW");
				b.setAttribute("NAME", name);
				b.setAttribute("TYPE", type);
				b.setAttribute("MULTIVALUE", multiValue);
				b.setAttribute(DataSetParametersList.DEFAULT_VALUE_XML, defaultValue);
				sb1.setAttribute(b);
			}
			sb.setAttribute(sb1);
			parametersString = sb.toXML(false);
		} catch (SpagoBIServiceException e) {
			throw e;
		} catch (Exception t) {
			throw new SpagoBIServiceException(SERVICE_NAME,
					"An unexpected error occured while deserializing dataset parameters", t);
		}
		return parametersString;
	}

	public Map<String, String> getDataSetParametersAsMap(JSONObject json) {
		HashMap<String, String> parametersMap = null;

		try {
			parametersMap = new HashMap<>();
			JSONArray parsListJSON = json.optJSONArray(DataSetConstants.PARS);
			if (parsListJSON == null) {
				return parametersMap;
			}

			for (int i = 0; i < parsListJSON.length(); i++) {
				JSONObject obj = (JSONObject) parsListJSON.get(i);
				String name = obj.optString("name");
				String type = null;
				if (obj.has("type")) {
					type = obj.optString("type");
				}
				boolean multiValue = obj.optBoolean("multiValue", false);
				String value = obj.optString(PARAM_VALUE_NAME);
				String defaultValue = obj.optString(DEFAULT_VALUE_PARAM);
				String retValue = "";

				retValue = ParameterManagerFactory.getInstance().defaultManager().fromFeToBe(type, value, defaultValue,
						multiValue);

				LOGGER.debug("name: " + name + " / value: " + retValue);
				parametersMap.put(name, retValue);

			}

			if (hasDuplicates(parametersMap, parsListJSON)) {
				LOGGER.error("duplicated parameter names");
				throw new SpagoBIServiceException(SERVICE_NAME, "duplicated parameter names");
			}
		} catch (SpagoBIServiceException e) {
			throw e;
		} catch (Exception t) {
			throw new SpagoBIServiceException(SERVICE_NAME,
					"An unexpected error occured while deserializing dataset parameters", t);
		}
		return parametersMap;
	}

	public Map<String, String> getSolrDataSetParametersAsMap(JSONObject json, UserProfile userProfile) {
		Map<String, String> parametersMap = null;

		try {
			parametersMap = new HashMap<>();
			JSONArray parsListJSON = json.optJSONArray(DataSetConstants.PARS);
			if (parsListJSON == null) {
				return parametersMap;
			}

			for (int i = 0; i < parsListJSON.length(); i++) {
				JSONObject obj = (JSONObject) parsListJSON.get(i);
				String name = obj.optString("name");
				String type = null;
				if (obj.has("type")) {
					type = obj.optString("type");
				}
				boolean multivalue = obj.optBoolean("multiValue", false);
				String value = obj.optString(PARAM_VALUE_NAME);
				String defaultValue = obj.optString(DEFAULT_VALUE_PARAM);
				String retValue = "";

				retValue = ParameterManagerFactory.getInstance().solrManager().fromFeToBe(type, value, defaultValue,
						multivalue);

				LOGGER.debug("name: " + name + " / value: " + retValue);
				parametersMap.put(name, retValue);
			}
		} catch (SpagoBIServiceException e) {
			throw e;
		} catch (Exception t) {
			throw new SpagoBIServiceException(SERVICE_NAME,
					"An unexpected error occured while deserializing dataset parameters", t);
		}
		return parametersMap;
	}

	public IMetaData getDatasetTestMetadata(IDataSet dataSet, Map parametersFilled, IEngUserProfile profile,
			String metadata) throws Exception {
		LOGGER.debug("IN");

		IDataStore dataStore = null;

		Integer start = 0;
		Integer limit = 10;

		dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));
		dataSet.setParamsMap(parametersFilled);

		try {
			checkFileDataset(dataSet);
			dataSet.loadData(start, limit, GeneralUtilities.getDatasetMaxResults());
			dataStore = dataSet.getDataStore();
			// DatasetMetadataParser dsp = new DatasetMetadataParser();

			try {

				IMetaData metaData = dataStore.getMetaData();

				JSONArray metadataArray = JSONUtils.toJSONArray(metadata);

				for (int i = 0; i < metaData.getFieldCount(); i++) {
					IFieldMetaData ifmd = metaData.getFieldMeta(i);

					// check if file data set, apply metadata values passed from frontend
					if (dataSet instanceof FileDataSet) {
						for (int j = 0; j < metadataArray.length(); j++) {
							if (ifmd.getName().equals((metadataArray.getJSONObject(j)).getString("name"))) {
								ifmd.setType(MetaDataMapping
										.getMetaDataType(metadataArray.getJSONObject(j).getString("type")));
								break;
							}

						}
					}

					for (int j = 0; j < metadataArray.length(); j++) {

						JSONObject metadataJSONObject = metadataArray.getJSONObject(j);
						if (ifmd.getName().equals(metadataJSONObject.getString("name"))) {
							if ("MEASURE".equals(metadataJSONObject.getString("fieldType"))) {
								ifmd.setFieldType(IFieldMetaData.FieldType.MEASURE);
							} else if (IFieldMetaData.FieldType.SPATIAL_ATTRIBUTE.toString()
									.equals(metadataJSONObject.getString("fieldType"))) {
								ifmd.setFieldType(IFieldMetaData.FieldType.SPATIAL_ATTRIBUTE);
							} else {
								ifmd.setFieldType(IFieldMetaData.FieldType.ATTRIBUTE);
							}

							ifmd.setPersonal(metadataJSONObject.optBoolean("personal"));
//							ifmd.setMasked(metadataJSONObject.optBoolean("masked"));
							ifmd.setDecrypt(metadataJSONObject.optBoolean("decrypt"));
							ifmd.setSubjectId(metadataJSONObject.optBoolean("subjectId"));
							ifmd.setDescription(metadataJSONObject.getString("description"));
							break;
						}
					}

				}

				if (metadataArray.length() == 0) {
					setNumericValuesAsMeasures(dataStore.getMetaData());
				}
			} catch (ClassCastException e) {
				LOGGER.debug("Recieving an object instead of array for metadata", e);
			}

			// dsMetadata = dsp.metadataToXML(dataStore.getMetaData());
		} catch (Exception e) {
			LOGGER.error("Error while executing dataset for test purpose", e);
			throw e;
		}

		return dataStore.getMetaData();
	}

	private void setNumericValuesAsMeasures(IMetaData metaData) {
		for (int i = 0; i < metaData.getFieldCount(); i++) {
			IFieldMetaData ifmd = metaData.getFieldMeta(i);
			if (Number.class.isAssignableFrom(ifmd.getType())) {
				ifmd.setFieldType(IFieldMetaData.FieldType.MEASURE);
			}
		}
	}

	private boolean isRemovingMetadataFields(IMetaData previousMetadata, IMetaData currentMetadata) {
		LOGGER.debug("IN");

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
			LOGGER.warn("Cannot remove metadata from a dataset in use");
			return true;
		}
		// else check that all labels previously present are still present
		for (Iterator iterator = previousFieldsName.iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			if (!currentFieldsName.contains(name)) {
				LOGGER.warn("Cannot remove field " + name + " of a dataset in use");
				return true;
			}
		}

		LOGGER.debug("OUT");
		return false;
	}

	private void getPersistenceInfo(IDataSet ds, JSONObject json) {
		Boolean isPersisted = json.optBoolean(DataSetConstants.IS_PERSISTED);
		Boolean isPersistedHDFS = json.optBoolean(DataSetConstants.IS_PERSISTED_HDFS);
		Boolean isScheduled = json.optBoolean(DataSetConstants.IS_SCHEDULED);

		if (isPersistedHDFS != null) {
			ds.setPersistedHDFS(isPersistedHDFS);
		}

		if (isPersisted != null) {
			ds.setPersisted(isPersisted);
			String persistTableName = "";
			if (isPersisted) {
				persistTableName = json.optString(DataSetConstants.PERSIST_TABLE_NAME);
				Assert.assertNotNull(persistTableName, "Impossible to define persistence if tablename is null");
				Assert.assertTrue(!persistTableName.isEmpty(), "Impossible to define persistence if tablename is null");

			}
			ds.setPersistTableName(persistTableName);

			if (isScheduled != null) {
				ds.setScheduled(isScheduled);
			}
		}
	}

	// This method rename a file and move it from resources\dataset\files\temp
	// to resources\dataset\files
	private void renameAndMoveDatasetFile(String originalFileName, String newFileName, String resourcePath,
			String fileType) {
		File originalDatasetFile = PathTraversalChecker.get(resourcePath, "dataset", "files", "temp", originalFileName);
		File newDatasetFile = PathTraversalChecker.get(resourcePath, "dataset", "files",
				newFileName + "." + fileType.toLowerCase());

		String filePathCloning = resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files"
				+ File.separatorChar;
		File originalDatasetFileCloning = new File(filePathCloning + originalFileName);

		if (originalDatasetFile.exists()) {
			/*
			 * This method copies the contents of the specified source file to the specified destination file. The directory holding the destination file is created if it
			 * does not exist. If the destination file exists, then this method will overwrite it.
			 */
			try {
				FileUtils.copyFile(originalDatasetFile, newDatasetFile);

				// Then delete temp file
				originalDatasetFile.delete();
			} catch (IOException e) {
				LOGGER.debug("Cannot move dataset File");
				throw new SpagoBIRuntimeException("Cannot move dataset File", e);
			}
		} else if (originalDatasetFileCloning.exists()) {
			try {
				boolean isTwoEqual = FileUtils.contentEquals(originalDatasetFileCloning, newDatasetFile);
				if (!isTwoEqual) {
					FileUtils.copyFile(originalDatasetFileCloning, newDatasetFile);
				}

			} catch (IOException e) {
				LOGGER.debug("Cannot move dataset File");
				throw new SpagoBIRuntimeException("Cannot move dataset File", e);
			}
		}

	}

	private void deleteDatasetFile(String fileName, String resourcePath, String fileType) {
		String filePath = resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files"
				+ File.separatorChar + "temp" + File.separatorChar;

		File datasetFile = new File(filePath + fileName);
		if (datasetFile.exists()) {
			datasetFile.delete();
		}
	}

	public void deleteDatasetFile(IDataSet dataset) {
		if (dataset instanceof VersionedDataSet) {
			VersionedDataSet versionedDataset = (VersionedDataSet) dataset;
			dataset = versionedDataset.getWrappedDataset();
		}
		FileDataSet fileDataset = (FileDataSet) dataset;
		String resourcePath = fileDataset.getResourcePath();
		String fileName = fileDataset.getFileName();
		String filePath = resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files"
				+ File.separatorChar;
		File datasetFile = new File(filePath + fileName);

		if (datasetFile.exists()) {
			boolean isDeleted = datasetFile.delete();
			if (isDeleted) {
				LOGGER.debug("Dataset File " + fileName + " has been deleted");
			}
		}
	}

	private FlatDataSet manageFlatDataSet(boolean savingDataset, JSONObject jsonDsConfig, JSONObject json)
			throws JSONException, EMFUserError {
		FlatDataSet dataSet = new FlatDataSet();
		manageDataSetMetadata(json, dataSet);
		String tableName = json.optString(DataSetConstants.FLAT_TABLE_NAME);
		String dataSourceLabel = json.optString(DataSetConstants.DATA_SOURCE_FLAT);
		jsonDsConfig.put(DataSetConstants.FLAT_TABLE_NAME, tableName);
		jsonDsConfig.put(DataSetConstants.DATA_SOURCE_FLAT, dataSourceLabel);
		dataSet.setTableName(tableName);
		IDataSource dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(dataSourceLabel);
		dataSet.setDataSource(dataSource);
		return dataSet;
	}

	private PreparedDataSet managePreparedDataSet(boolean savingDataset, JSONObject jsonDsConfig, JSONObject json)
			throws JSONException, EMFUserError {
		PreparedDataSet dataSet = new PreparedDataSet();
		manageDataSetMetadata(json, dataSet);
		String tableName = json.optString(DataSetConstants.TABLE_NAME);
		String dataSourceLabel = json.optString(DataSetConstants.DATA_SOURCE);
		String dataPrepInstanceId = json.optString(DataSetConstants.DATA_PREPARATION_INSTANCE_ID);
		jsonDsConfig.put(DataSetConstants.TABLE_NAME, tableName);
		jsonDsConfig.put(DataSetConstants.DATA_SOURCE, dataSourceLabel);
		jsonDsConfig.put(DataSetConstants.DATA_PREPARATION_INSTANCE_ID, dataPrepInstanceId);
		dataSet.setTableName(tableName);
		IDataSource dataSource = DAOFactory.getDataSourceDAO().loadDataSourceUseForDataprep();
		dataSet.setDataSource(dataSource);
		dataSet.setDataPreparationInstance(dataPrepInstanceId);
		return dataSet;
	}

	private DerivedDataSet manageDerivedDataSet(boolean savingDataset, JSONObject jsonDsConfig, JSONObject json,
			UserProfile userProfile) throws JSONException, EMFUserError, IOException {
		// KNOWAGE-7575
		DerivedDataSet dataset = new DerivedDataSet();
		createQbeOrDerivedDataset(jsonDsConfig, json, dataset);
		return dataset;
	}

	private QbeDataSet manageQbeDataSet(boolean savingDataset, JSONObject jsonDsConfig, JSONObject json,
			UserProfile userProfile) throws JSONException, EMFUserError, IOException {
		QbeDataSet dataSet = null;
		String federationId = json.optString("federation_id");
		if (StringUtils.isNoneEmpty(federationId)) {
			FederationDefinition federation = DAOFactory.getFedetatedDatasetDAO()
					.loadFederationDefinition(Integer.parseInt(federationId));
			dataSet = new FederatedDataSet(federation, userProfile.getUserId().toString());

			IDataSource defaultCacheDataSource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();

			dataSet.setDataSourceForReading(defaultCacheDataSource);
			dataSet.setDataSourceForWriting(defaultCacheDataSource);

			json.put(DataSetConstants.QBE_DATA_SOURCE, defaultCacheDataSource.getLabel());
		} else {
			dataSet = new QbeDataSet();
		}
		createQbeOrDerivedDataset(jsonDsConfig, json, dataSet);
		return dataSet;
	}

	private void createQbeOrDerivedDataset(JSONObject jsonDsConfig, JSONObject json, QbeDataSet dataSet)
			throws JSONException, EMFUserError {
		manageDataSetMetadata(json, dataSet);
		String qbeDatamarts = json.optString(DataSetConstants.QBE_DATAMARTS);
		String dataSourceLabel = json.optString(DataSetConstants.QBE_DATA_SOURCE);
		String dataSetLabel = json.optString(DataSetConstants.DATASET_LABEL);
		String jsonQuery = json.optString(DataSetConstants.QBE_JSON_QUERY);
		JSONObject driversJSON = json.optJSONObject(DRIVERS);

		jsonDsConfig.put(DataSetConstants.QBE_DATAMARTS, qbeDatamarts);
		jsonDsConfig.put(DataSetConstants.QBE_DATA_SOURCE, dataSourceLabel);
		jsonDsConfig.put(DataSetConstants.QBE_JSON_QUERY, jsonQuery);

		dataSet.setDrivers(DataSetUtilities.getDriversMap(driversJSON));
		// START -> This code should work instead of CheckQbeDataSets around
		// the projects
		SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
		Map parameters = dataSet.getParamsMap();
		if (parameters == null) {
			parameters = new HashMap();
			dataSet.setParamsMap(parameters);
		}
		dataSet.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
		LOGGER.debug("Datamart retriever correctly added to Qbe dataset");
		// END
		dataSet.setJsonQuery(jsonQuery);
		dataSet.setDatamarts(qbeDatamarts);
		if (dataSourceLabel != null && !dataSourceLabel.trim().equals("")) {
			IDataSource dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(dataSourceLabel);
			dataSet.setDataSource(dataSource);
		}

		String sourceDatasetLabel = json.optString(DataSetConstants.SOURCE_DS_LABEL);
		IDataSet sourceDataset = null;
		if (sourceDatasetLabel != null && !sourceDatasetLabel.trim().equals("")) {
			try {
				sourceDataset = DAOFactory.getDataSetDAO().loadDataSetByLabel(sourceDatasetLabel);
				if (sourceDataset == null) {
					throw new SpagoBIRuntimeException("Dataset with label [" + sourceDatasetLabel + "] does not exist");
				}
				dataSet.setSourceDataset(sourceDataset);
				dataSet.setDataSource(sourceDataset.getDataSource());
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot retrieve source dataset information", e);
			}
		}
		if (StringUtils.isNotEmpty(dataSetLabel)) {
			IDataSet ds = DAOFactory.getDataSetDAO().loadDataSetByLabel(dataSetLabel);
			IDataSource dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(ds.getDataSource().getLabel());
			dataSet.setDataSource(dataSource);
			dataSet.setSourceDataset(ds);
		}
	}

	private IDataSet manageCustomDataSet(boolean savingDataset, JSONObject jsonDsConfig, JSONObject json)
			throws JSONException {
		CustomDataSet customDs = new CustomDataSet();
		manageDataSetMetadata(json, customDs);
		String customData = json.getString(DataSetConstants.CUSTOM_DATA);
		jsonDsConfig.put(DataSetConstants.CUSTOM_DATA, customData);
		customDs.setCustomData(customData);
		String javaClassName = json.getString(DataSetConstants.JCLASS_NAME);
		jsonDsConfig.put(DataSetConstants.JCLASS_NAME, javaClassName);
		customDs.setJavaClassName(javaClassName);
		return customDs.instantiate();
	}

	private IDataSet manageFederatedDataSet(boolean savingDataset, JSONObject jsonDsConfig, JSONObject json,
			UserProfile userProfile) throws Exception {
		Integer id = json.getInt(DataSetConstants.ID);
		Assert.assertNotNull(id, "The federated dataset id is null");
		IDataSetDAO dao = DAOFactory.getDataSetDAO();
		dao.setUserProfile(userProfile);
		IDataSet dataSet = dao.loadDataSetById(id);
		manageDataSetMetadata(json, dataSet);
		// if its a federated dataset the datasource are teh ones on cahce
		SQLDBCache cache = (SQLDBCache) CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
		dataSet.setDataSourceForReading(cache.getDataSource());
		dataSet.setDataSourceForWriting(cache.getDataSource());

		// update the json query getting the one passed from the qbe editor
		String jsonDatamarts = dataSet.getDatasetFederation().getName();
		String jsonDataSource = json.optString(DataSetConstants.QBE_DATA_SOURCE);
		String jsonQuery = json.optString(DataSetConstants.QBE_JSON_QUERY);

		jsonDsConfig.put(DataSetConstants.QBE_DATAMARTS, jsonDatamarts);
		jsonDsConfig.put(DataSetConstants.QBE_DATA_SOURCE, jsonDataSource);
		jsonDsConfig.put(DataSetConstants.QBE_JSON_QUERY, jsonQuery);

		((FederatedDataSet) (((VersionedDataSet) dataSet).getWrappedDataset())).setJsonQuery(jsonQuery);
		return dataSet;
	}

	private JavaClassDataSet manageJavaClassDataSet(boolean savingDataset, JSONObject jsonDsConfig, JSONObject json)
			throws JSONException {
		JavaClassDataSet dataSet = new JavaClassDataSet();
		manageDataSetMetadata(json, dataSet);
		String jclassName = json.getString(DataSetConstants.JCLASS_NAME);
		jsonDsConfig.put(DataSetConstants.JCLASS_NAME, jclassName);
		dataSet.setClassName(jclassName);
		return dataSet;
	}

	private ScriptDataSet manageScriptDataSet(boolean savingDataset, JSONObject jsonDsConfig, JSONObject json)
			throws JSONException {
		ScriptDataSet dataSet = new ScriptDataSet();
		manageDataSetMetadata(json, dataSet);
		String script = json.optString("script");
		String scriptLanguage = json.optString(DataSetConstants.SCRIPT_LANGUAGE);
		jsonDsConfig.put(DataSetConstants.SCRIPT, script);
		jsonDsConfig.put(DataSetConstants.SCRIPT_LANGUAGE, scriptLanguage);
		dataSet.setScript(script);
		dataSet.setScriptLanguage(scriptLanguage);
		return dataSet;
	}

	private IDataSet manageQueryDataSet(boolean savingDataset, JSONObject jsonDsConfig, JSONObject json)
			throws JSONException, EMFUserError {
		IDataSet dataSet;
		String query = json.optString((DataSetConstants.QUERY).toLowerCase());
		String queryScript = json.optString(DataSetConstants.QUERY_SCRIPT);
		String queryScriptLanguage = json.optString(DataSetConstants.QUERY_SCRIPT_LANGUAGE);
		String dataSourceLabel = json.optString(DataSetConstants.DATA_SOURCE);
		jsonDsConfig.put(DataSetConstants.QUERY, query);
		jsonDsConfig.put(DataSetConstants.QUERY_SCRIPT, queryScript);
		jsonDsConfig.put(DataSetConstants.QUERY_SCRIPT_LANGUAGE, queryScriptLanguage);
		jsonDsConfig.put(DataSetConstants.DATA_SOURCE, dataSourceLabel);

		Assert.assertNotNull(dataSourceLabel, "Impossible to continue if dataSource label is null");
		Assert.assertTrue(!dataSourceLabel.isEmpty(), "Impossible to continue if dataSource label is empty");
		IDataSource dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(dataSourceLabel);
		Assert.assertNotNull(dataSource, "A datasource with label " + dataSourceLabel + " could not be found");
		if (dataSource.getHibDialectClass().toLowerCase().contains("mongo")) {
			dataSet = new MongoDataSet();
		} else {
			String checkSqlValidation = SingletonConfig.getInstance().getConfigValue("DATA_SET_SQL_VALIDATION");
			if (("true").equals(checkSqlValidation)) {
				boolean isSelect = false;
				try {
					if (SqlUtils.isSelectSOrWithStatement(query)) {
						LOGGER.info("SQL is a SELECT statement.");
						if (query.toLowerCase().contains(" update ") || query.toLowerCase().contains(" delete ")
								|| query.toLowerCase().contains(" insert ")) {
							isSelect = false;
						} else {
							isSelect = true;
						}
					}
				} catch (Exception e) {
					LOGGER.error("SQL is NOT a SELECT statement.");
					isSelect = false;
				}
				if (isSelect) {
					dataSet = JDBCDatasetFactory.getJDBCDataSet(dataSource);
				} else {
					LOGGER.error("SQL is NOT a SELECT statement, or contains keywords like INSERT, UPDATE, DELETE.");
					throw new SpagoBIServiceException("Manage Dataset", "Provided SQL is NOT a SELECT statement");
				}

			} else {
				dataSet = JDBCDatasetFactory.getJDBCDataSet(dataSource);
			}

		}
		manageDataSetMetadata(json, dataSet);

		((ConfigurableDataSet) dataSet).setDataSource(dataSource);
		((ConfigurableDataSet) dataSet).setQuery(query);
		((ConfigurableDataSet) dataSet).setQueryScript(queryScript);
		((ConfigurableDataSet) dataSet).setQueryScriptLanguage(queryScriptLanguage);

		return dataSet;
	}

	private FileDataSet manageFileDataSet(boolean savingDataset, JSONObject jsonDsConfig, JSONObject json)
			throws JSONException, IOException {
		String dsId = json.optString(DataSetConstants.ID);
		String dsLabel = json.getString(DataSetConstants.LABEL);
		String fileType = json.getString(DataSetConstants.FILE_TYPE);
		String versionNum = json.getString(DataSetConstants.VERSION_NUM);

		String csvDelimiter = json.optString(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER);
		String csvQuote = json.optString(DataSetConstants.CSV_FILE_QUOTE_CHARACTER);
		String dateFormat = json.optString(DataSetConstants.FILE_DATE_FORMAT);
		String timestampFormat = json.optString(DataSetConstants.FILE_TIMESTAMP_FORMAT);
		String csvEncoding = json.optString(DataSetConstants.CSV_FILE_ENCODING);

		String skipRows = json.optString(DataSetConstants.XSL_FILE_SKIP_ROWS);
		String limitRows = json.optString(DataSetConstants.XSL_FILE_LIMIT_ROWS);
		String xslSheetNumber = json.optString(DataSetConstants.XSL_FILE_SHEET_NUMBER);

		FileDataSet dataSet = new FileDataSet();
		manageDataSetMetadata(json, dataSet);

		String dsLab = dsLabel;

		Boolean newFileUploaded = false;
		if (json.optString("fileUploaded") != null) {
			newFileUploaded = Boolean.valueOf(json.optString("fileUploaded"));
		}

		if (versionNum != "") {
			dsLab = dsLabel + "_" + versionNum;
		}

		jsonDsConfig.put(DataSetConstants.FILE_TYPE, fileType);
		jsonDsConfig.put(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER, csvDelimiter);
		jsonDsConfig.put(DataSetConstants.CSV_FILE_QUOTE_CHARACTER, csvQuote);
		jsonDsConfig.put(DataSetConstants.CSV_FILE_ENCODING, csvEncoding);
		jsonDsConfig.put(DataSetConstants.XSL_FILE_SKIP_ROWS, skipRows);
		jsonDsConfig.put(DataSetConstants.XSL_FILE_LIMIT_ROWS, limitRows);
		jsonDsConfig.put(DataSetConstants.XSL_FILE_SHEET_NUMBER, xslSheetNumber);
		jsonDsConfig.put(DataSetConstants.FILE_DATE_FORMAT, dateFormat);
		jsonDsConfig.put(DataSetConstants.FILE_TIMESTAMP_FORMAT, timestampFormat);

		dataSet.setResourcePath(DAOConfig.getResourcePath());
		String fileName = json.getString(DataSetConstants.FILE_NAME);
		File pathFile = new File(fileName);
		fileName = pathFile.getName();
		if (savingDataset) {
			LOGGER.debug("When saving the dataset the file associated will get the dataset label name");//
			if (dsLabel != null) {
				jsonDsConfig.put(DataSetConstants.FILE_NAME, dsLab + "." + fileType.toLowerCase());
			}
		} else {
			jsonDsConfig.put(DataSetConstants.FILE_NAME, fileName);
		}

		dataSet.setConfiguration(jsonDsConfig.toString());

		if ((dsId == null) || (dsId.isEmpty())) {
			LOGGER.debug("By creating a new dataset, the file uploaded has to be renamed and moved");
			dataSet.setUseTempFile(true);
			if (savingDataset) {
				LOGGER.debug("Rename and move the file");
				String resourcePath = dataSet.getResourcePath();
				if (dsLabel != null) {
					renameAndMoveDatasetFile(fileName, dsLab, resourcePath, fileType);
					dataSet.setUseTempFile(false);
				}
			}
		} else {
			LOGGER.debug(
					"Reading or modifying a existing dataset. If change the label then the name of the file should be changed");

			JSONObject configuration;
			Integer currDsId = json.getInt(DataSetConstants.ID);
			configuration = new JSONObject(DAOFactory.getDataSetDAO().loadDataSetById(currDsId).getConfiguration());
			String realName = configuration.getString("fileName");
			if (dsLabel != null && !realName.equals(dsLabel)) {

				File dest = new File(SpagoBIUtilities.getResourcePath() + File.separatorChar + "dataset"
						+ File.separatorChar + "files" + File.separatorChar + dsLab + "."
						+ configuration.getString("fileType").toLowerCase());
				File source = new File(SpagoBIUtilities.getResourcePath() + File.separatorChar + "dataset"
						+ File.separatorChar + "files" + File.separatorChar + realName);

				if (!source.getCanonicalPath().equals(dest.getCanonicalPath()) && savingDataset && !newFileUploaded) {
					LOGGER.debug("Source and destination are not the same. Copying from source to dest");
					FileUtils.copyFile(source, dest);
				}
			}

			if (Boolean.TRUE.equals(newFileUploaded)) {
				LOGGER.debug("Modifying an existing dataset with a new file uploaded");
				dataSet.setUseTempFile(true);

				LOGGER.debug("Saving the existing dataset with a new file associated");
				if (savingDataset) {
					LOGGER.debug("Rename and move the file");
					String resourcePath = dataSet.getResourcePath();
					if (dsLabel != null) {
						renameAndMoveDatasetFile(fileName, dsLabel + "_" + versionNum, resourcePath, fileType);
						dataSet.setUseTempFile(false);
					}
				}

			} else {
				LOGGER.debug("Using existing dataset file, file in correct place");
				dataSet.setUseTempFile(false);
			}
		}

		dataSet.setFileType(fileType);

		if (savingDataset) {
			LOGGER.debug("The file used will have the name equals to dataset's label");
			dataSet.setFileName(dsLab + "." + fileType.toLowerCase());
		} else {
			dataSet.setFileName(fileName);
		}
		return dataSet;
	}

	private void manageDataSetMetadata(JSONObject json, IDataSet dataSet) throws JSONException {
		if (json.optJSONObject(DataSetConstants.METADATA) != null
				&& json.optJSONObject(DataSetConstants.METADATA).optJSONArray("columns") != null) {
			JSONArray dsMeta = json.optJSONObject(DataSetConstants.METADATA).getJSONArray("columns");
			manageDataSetMetadata(dsMeta, dataSet);
		} else if (json.optJSONArray(DataSetConstants.METADATA) != null
				&& json.optJSONArray(DataSetConstants.METADATA).length() > 0) {
			JSONArray dsMeta = json.optJSONArray(DataSetConstants.METADATA);
			manageDataSetMetadataV2(dsMeta, dataSet);
		}
	}

	private void manageDataSetMetadata(JSONArray dsMeta, IDataSet dataSet) throws JSONException {
		DatasetMetadataParser dsp = new DatasetMetadataParser();
		IMetaData userMetaData = getUserMetaData(dsMeta);
		String metadataXML = dsp.metadataToXML(userMetaData);
		dataSet.setDsMetadata(metadataXML);
		dataSet.setMetadata(userMetaData);
	}

	private void manageDataSetMetadataV2(JSONArray dsMeta, IDataSet dataSet) throws JSONException {
		DatasetMetadataParser dsp = new DatasetMetadataParser();
		IMetaData userMetaData = getUserMetaDataV2(dsMeta);
		String metadataXML = dsp.metadataToXML(userMetaData);
		dataSet.setDsMetadata(metadataXML);
		dataSet.setMetadata(userMetaData);
	}

	/**
	 * Parse user metadata.
	 *
	 * It manages JSON like:
	 *
	 * <pre>
	 * [
	 * 	{
	 * 		...,
	 * 		"meta": {
	 * 			"dataset": ...,
	 * 			"columns": [
	 * 				{"column": "col1","pname": "Type","pvalue": "java.lang.String"},
	 * 				{"column": "col1","pname": "fieldType","pvalue": "ATTRIBUTE"},
	 * 				{"column": "col1","pname": "fieldAlias","pvalue": "col1"},
	 * 				{"column": "col1","pname": "personal","pvalue": false},
	 * 				{"column": "col1","pname": "decrypt","pvalue": true},
	 * 				{"column": "col1","pname": "subjectId","pvalue": false},
	 * 				{"column": "col2","pname": "Type","pvalue": "java.lang.String"},
	 * 				{"column": "col2","pname": "fieldType","pvalue": "ATTRIBUTE"},
	 * 				{"column": "col2","pname": "fieldAlias","pvalue": "col2"},
	 * 				{"column": "col2","pname": "personal","pvalue": false},
	 * 				{"column": "col2","pname": "decrypt","pvalue": true},
	 * 				{"column": "col2","pname": "subjectId","pvalue": false},
	 * 				{"column": "col3","pname": "Type","pvalue": "java.lang.Integer"},
	 * 				{"column": "col3","pname": "fieldType","pvalue": "MEASURE"},
	 * 				{"column": "col3","pname": "fieldAlias","pvalue": "col3"},
	 * 				{"column": "col3","pname": "personal","pvalue": false},
	 * 				{"column": "col3","pname": "decrypt","pvalue": false},
	 * 				{"column": "col3","pname": "subjectId","pvalue": false}
	 * 			]
	 * 		},
	 * 		...
	 * 	}
	 * ]
	 * </pre>
	 */
	private IMetaData getUserMetaData(JSONArray dsMeta) throws JSONException {
		MetaData toReturn = new MetaData();

		List<IFieldMetaData> fieldsMeta = new ArrayList<>();
		Map<String, IFieldMetaData> m = new LinkedHashMap<>();

		for (int i = 0; i < dsMeta.length(); i++) {
			JSONObject currMetaType = dsMeta.getJSONObject(i);
			String column = currMetaType.getString("column");
			IFieldMetaData columnMap = m.get(column);
			if (columnMap == null) {
				m.put(column, new FieldMetadata());

				m.get(column).setName(currMetaType.getString("column"));
				m.get(column).setProperties(new HashMap<>());
				m.get(column).setMultiValue(false);
			}

			switch (currMetaType.getString("pname")) {
			case "Type":
				m.get(column).setType(getClassTypeFromColumn(currMetaType.getString("pvalue")));
				break;
			case "fieldType":
				m.get(column).setFieldType(getFieldTypeFromColumn(currMetaType.getString("pvalue")));
				break;
			case "fieldAlias":
				m.get(column).setAlias(currMetaType.getString("pvalue"));
				break;
			case "personal":
				m.get(column).setPersonal(currMetaType.getBoolean("pvalue"));
				break;
			case "decrypt":
				m.get(column).setDecrypt(currMetaType.getBoolean("pvalue"));
				break;
			case "subjectId":
				m.get(column).setSubjectId(currMetaType.getBoolean("pvalue"));
				break;
			case "description":
				try {
					m.get(column).setDescription(currMetaType.getString("description"));
				} catch (JSONException e) {
					m.get(column).setDescription("");
				}
				break;

			default:
				break;
			}

		}

		m.keySet().forEach(x -> fieldsMeta.add(m.get(x)));

		toReturn.setFieldsMeta(fieldsMeta);
		return toReturn;
	}

	/**
	 * Parse user metadata.
	 *
	 * It manages JSON like:
	 *
	 * <pre>
	 * {
	 * 	...
	 * 	"meta": [
	 * 		{"displayedName": "col1","name": "col1","fieldType": "ATTRIBUTE","type": "java.lang.String","personal": false,"decrypt": true,"subjectId": false},
	 * 		{"displayedName": "col2","name": "col2","fieldType": "ATTRIBUTE","type": "java.lang.String","personal": false,"decrypt": true,"subjectId": false},
	 * 		{"displayedName": "col3","name": "col3","fieldType": "MEASURE","type": "java.lang.Integer","personal": false,"decrypt": false,"subjectId": false}
	 * 	],
	 * 	...
	 * }
	 * </pre>
	 */
	private IMetaData getUserMetaDataV2(JSONArray dsMeta) throws JSONException {
		MetaData toReturn = new MetaData();

		List<IFieldMetaData> fieldsMeta = new ArrayList<>();

		for (int i = 0; i < dsMeta.length(); i++) {
			JSONObject currMetaType = dsMeta.getJSONObject(i);
			String name = currMetaType.getString("name");
			String displayedName = currMetaType.getString("displayedName");
			String fieldType = currMetaType.getString("fieldType");
			String type = currMetaType.getString("type");
			boolean decrypt = currMetaType.optBoolean("decrypt");
			boolean personal = currMetaType.optBoolean("personal");
			boolean subjectId = currMetaType.optBoolean("subjectId");
			String description = currMetaType.optString("description");

			FieldType fieldTypeFromColumn = getFieldTypeFromColumn(fieldType);
			Class<?> classTypeFromColumn = getClassTypeFromColumn(type);

			FieldMetadata fieldMetaData = new FieldMetadata();

			fieldMetaData.setAlias(displayedName);
			fieldMetaData.setDecrypt(decrypt);
			fieldMetaData.setFieldType(fieldTypeFromColumn);
			fieldMetaData.setMultiValue(false);
			fieldMetaData.setName(name);
			fieldMetaData.setPersonal(personal);
			fieldMetaData.setPrecision(0);
			fieldMetaData.setProperties(new HashMap<>());
			fieldMetaData.setScale(0);
			fieldMetaData.setSubjectId(subjectId);
			fieldMetaData.setType(classTypeFromColumn);
			fieldMetaData.setDescription(description);

			fieldsMeta.add(fieldMetaData);
		}

		toReturn.setFieldsMeta(fieldsMeta);
		return toReturn;
	}

	private Class getClassTypeFromColumn(String columnClass) {
		if (columnClass.equalsIgnoreCase("java.lang.String")) {
			return java.lang.String.class;
		} else if (columnClass.equalsIgnoreCase("java.lang.Long")) {
			return java.lang.Long.class;
		} else if (columnClass.equalsIgnoreCase("java.lang.Integer")) {
			return java.lang.Integer.class;
		} else if (columnClass.equalsIgnoreCase("java.math.BigDecimal")) {
			return java.math.BigDecimal.class;
		} else if (columnClass.equalsIgnoreCase("java.lang.Double")) {
			return java.lang.Double.class;
		} else if (columnClass.equalsIgnoreCase("java.util.Date")) {
			return java.sql.Date.class;
		} else if (columnClass.equalsIgnoreCase("java.util.Timestamp")) {
			return java.sql.Timestamp.class;
		} else if (columnClass.equalsIgnoreCase("java.sql.Timestamp")) {
			return java.sql.Timestamp.class;
		} else if (columnClass.equalsIgnoreCase("oracle.sql.TIMESTAMP")) {
			return java.sql.Timestamp.class;
		} else if (columnClass.equalsIgnoreCase("java.time.LocalDate")) {
			return java.time.LocalDate.class;
		} else if (columnClass.equalsIgnoreCase("java.time.LocalTime")) {
			return java.time.LocalTime.class;
		} else if (columnClass.equalsIgnoreCase("java.time.LocalDateTime")) {
			return java.time.LocalDateTime.class;
		} else if (columnClass.equalsIgnoreCase("java.time.OffsetTime")) {
			return java.time.OffsetTime.class;
		} else if (columnClass.equalsIgnoreCase("java.time.OffsetDateTime")) {
			return java.time.OffsetDateTime.class;
		} else if (columnClass.equalsIgnoreCase("java.time.ZonedDateTime")) {
			return java.time.ZonedDateTime.class;
		} else if (columnClass.equalsIgnoreCase("java.lang.Boolean")) {
			return java.lang.Boolean.class;
		} else {
			try {
				return Class.forName(columnClass);
			} catch (ClassNotFoundException e) {
				throw new SpagoBIRuntimeException("Couldn't map class <" + columnClass + ">", e);
			}
		}
	}

	private FieldType getFieldTypeFromColumn(String fieldType) {
		if (fieldType.equalsIgnoreCase("ATTRIBUTE")) {
			return FieldType.ATTRIBUTE;
		} else if (fieldType.equalsIgnoreCase("MEASURE")) {
			return FieldType.MEASURE;
		} else if (fieldType.equalsIgnoreCase("SPATIAL_ATTRIBUTE")) {
			return FieldType.SPATIAL_ATTRIBUTE;
		} else {
			throw new SpagoBIRuntimeException("Couldn't map field type <" + fieldType + ">");
		}
	}

	private RESTDataSet manageRESTDataSet(boolean savingDataset, JSONObject config, JSONObject json)
			throws JSONException {
		for (String sa : RESTDataSetConstants.REST_STRING_ATTRIBUTES) {
			config.put(sa, json.optString(sa));
		}
		for (String ja : RESTDataSetConstants.REST_JSON_OBJECT_ATTRIBUTES) {
			config.put(ja, new JSONObject(json.getString(ja)));
		}
		for (String ja : RESTDataSetConstants.REST_JSON_ARRAY_ATTRIBUTES) {
			config.put(ja, new JSONArray(json.getString(ja)));
		}
		RESTDataSet res = new RESTDataSet(config);
		manageDataSetMetadata(json, res);
		res.setLabel(json.optString(DataSetConstants.LABEL));
		return res;
	}

	private PythonDataSet managePythonDataSet(boolean savingDataset, JSONObject config, JSONObject json)
			throws JSONException {
		for (String sa : PythonDataSetConstants.PYTHON_STRING_ATTRIBUTES) {
			config.put(sa, json.optString(sa));
		}
		config.put(DataSetConstants.DATA_SET_TYPE, DataSetConstants.DS_PYTHON_TYPE);
		PythonDataSet res = new PythonDataSet(config);
		manageDataSetMetadata(json, res);
		res.setLabel(json.optString(DataSetConstants.LABEL));
		return res;
	}

	private SPARQLDataSet manageSPARQLDataSet(boolean savingDataset, JSONObject config, JSONObject json)
			throws JSONException {
		for (String sa : SPARQLDatasetConstants.SPARQL_ATTRIBUTES) {
			config.put(sa, json.optString(sa));
		}
		SPARQLDataSet res = new SPARQLDataSet(config);
		manageDataSetMetadata(json, res);
		return res;
	}

	private SolrDataSet manageSolrDataSet(boolean savingDataset, JSONObject config, JSONObject json,
			UserProfile userProfile) throws JSONException {
		for (String ja : RESTDataSetConstants.REST_JSON_OBJECT_ATTRIBUTES) {
			String prop = json.optString(ja);
			if (prop != null && !prop.trim().isEmpty()) {
				config.put(ja, new JSONObject(prop));
			}
		}

		for (String sa : SolrDataSetConstants.SOLR_STRING_ATTRIBUTES) {
			config.put(sa, json.optString(sa));
		}

		for (String ja : SolrDataSetConstants.SOLR_JSON_ARRAY_ATTRIBUTES) {
			String prop = json.optString(ja);
			if (prop != null && !prop.trim().isEmpty()) {
				config.put(ja, new JSONArray(prop));
			}
		}

		Map<String, String> parametersMap = getSolrDataSetParametersAsMap(json, userProfile);
		String solrType = config.getString(SolrDataSetConstants.SOLR_TYPE);
		Assert.assertNotNull(solrType, "Solr type cannot be null");
		SolrDataSet res = solrType.equalsIgnoreCase(SolrDataSetConstants.TYPE.DOCUMENTS.name())
				? new SolrDataSet(config, parametersMap, userProfile)
				: new FacetSolrDataSet(config, parametersMap);
		manageDataSetMetadata(json, res);

		// Force schema read from Solr
		res.forceSchemaRead(config);

		return res;
	}

	private void checkFileDataset(IDataSet dataSet) {
		if (dataSet instanceof FileDataSet) {
			((FileDataSet) dataSet).setResourcePath(DAOConfig.getResourcePath());
		}
	}

	protected String datasetInsert(IDataSet ds, IDataSetDAO dsDao, Locale locale, UserProfile userProfile,
			JSONObject json, HttpServletRequest req) throws JSONException {
		JSONObject attributesResponseSuccessJSON = new JSONObject();
		HashMap<String, String> logParam = new HashMap<>();

		if (ds != null) {

			List<String> persistenceTableNames = dsDao.loadPersistenceTableNames(ds.getId());

			if (persistenceTableNames.stream().anyMatch(tableName -> tableName != null && !tableName.isBlank() && tableName.equalsIgnoreCase(ds.getPersistTableName()))) {
				throw new SpagoBIRuntimeException("The persistence table with name <" + ds.getPersistTableName().toUpperCase(Locale.ROOT) + "> already exists");
			}

			String dsLabel = ds.getLabel();
			String dsName = ds.getName();
			logParam.put("NAME", dsName);
			logParam.put("LABEL", dsLabel);
			logParam.put("TYPE", ds.getDsType());
			String id = json.optString(DataSetConstants.ID);
			try {
				IDataSet existingByName = dsDao.loadDataSetByName(ds.getName());
				IDataSet existingByLabel = dsDao.loadDataSetByLabel(ds.getLabel());

				if (id != null && !id.equals("") && !id.equals("0")) {
					validateLabelAndName(id, dsName, dsLabel, existingByName, existingByLabel);
					ds.setId(Integer.valueOf(id));
					IDataSet existingDataset = dsDao.loadDataSetById(Integer.valueOf(id));
					clearCacheForDataset(existingDataset);

					modifyPersistence(ds, logParam, userProfile);
					dsDao.modifyDataSet(ds);
					LOGGER.debug("Resource " + id + " updated");
					attributesResponseSuccessJSON.put("success", true);
					attributesResponseSuccessJSON.put("responseText", "Operation succeded");
					attributesResponseSuccessJSON.put("id", id);
					attributesResponseSuccessJSON.put("dateIn", ds.getDateIn());
					attributesResponseSuccessJSON.put("userIn", ds.getUserIn());
					attributesResponseSuccessJSON.put("meta",
							new DataSetMetadataJSONSerializer().metadataSerializerChooser(ds.getDsMetadata()));
				} else {
					validateLabelAndName(id, dsName, dsLabel, existingByName, existingByLabel);
					Integer dsID = dsDao.insertDataSet(ds);
					VersionedDataSet dsSaved = (VersionedDataSet) dsDao.loadDataSetById(dsID);
					AUDIT_LOGGER.info("[Saved dataset without metadata with id: " + dsID + "]");
					LOGGER.debug("New Resource inserted");
					attributesResponseSuccessJSON.put("success", true);
					attributesResponseSuccessJSON.put("responseText", "Operation succeded");
					attributesResponseSuccessJSON.put("id", dsID);
					if (dsSaved != null) {
						attributesResponseSuccessJSON.put("dateIn", dsSaved.getDateIn());
						attributesResponseSuccessJSON.put("userIn", dsSaved.getUserIn());
						attributesResponseSuccessJSON.put("versNum", dsSaved.getVersionNum());
						attributesResponseSuccessJSON.put("meta",
								new DataSetMetadataJSONSerializer().metadataSerializerChooser(dsSaved.getDsMetadata()));
					}
				}
				String operation = (id != null && !id.equals("") && !id.equals("0")) ? "DATA_SET.MODIFY"
						: "DATA_SET.ADD";
				Boolean isFromSaveNoMetadata = json.optBoolean(DataSetConstants.IS_FROM_SAVE_NO_METADATA);
				validateLabelAndName(id, dsName, dsLabel, existingByName, existingByLabel);
				// handle insert of persistence and scheduling
				if (Boolean.FALSE.equals(isFromSaveNoMetadata)) {
					AUDIT_LOGGER.info("[Start persisting metadata for dataset with id " + ds.getId() + "]");
					insertPersistence(ds, logParam, json, userProfile);
					AUDIT_LOGGER.info("Metadata saved for dataset with id " + ds.getId() + "]");
					AUDIT_LOGGER.info("[End persisting metadata for dataset with id " + ds.getId() + "]");
				}
				AuditLogUtilities.updateAudit(req, userProfile, operation, logParam, "OK");
				dsDao.updateDatasetOlderVersion(ds);
				return attributesResponseSuccessJSON.toString();
			} catch (Exception e) {
				AuditLogUtilities.updateAudit(req, userProfile, "DATA_SET.ADD", logParam, "KO");
				throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.saveDsError", e);
			}
		} else {
			AuditLogUtilities.updateAudit(req, userProfile, "DATA_SET.ADD/MODIFY", logParam, "ERR");
			LOGGER.error("DataSet name, label or type are missing");
			throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.fillFieldsError");
		}
	}

	private static void clearCacheForDataset(IDataSet ds) throws Exception {
		ICacheDAO cacheDAO = DAOFactory.getCacheDao();
		ICache cache = CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
		List<CacheItem> cacheItems = cacheDAO.loadCacheItemsByDatasetName(ds.getName());

		if (cacheItems == null || cacheItems.isEmpty()) {
			return;
		}

		cacheItems.forEach(cacheItem -> {
			cache.delete(cacheItem.getSignature(), true);
		});
	}

	private void validateLabelAndName(String id, String dsName, String dsLabel, IDataSet existingByName,
			IDataSet existingByLabel) {
		String regex = "[!*'\\(\\);:@&=+$,\\/?%#\\[\\]]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(dsLabel);

		while (m.find()) {
			String message = String.format("The dataset label [%s] contains at least one invalid character", dsLabel);
			LOGGER.error(message);
			throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.label.invalid");
		}

		m = p.matcher(dsName);
		while (m.find()) {
			String message = String.format("The dataset name [%s] contains at least one invalid character", dsName);
			LOGGER.error(message);
			throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.name.invalid");
		}

		if (existingByName != null) {

			if (!id.equals("") && !id.equals("0")) {

				if (!Integer.valueOf(id).equals(existingByName.getId())) {
					throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.name.alreadyExistent");
				}
			} else {
				throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.name.alreadyExistent");
			}
		}
		if (existingByLabel != null) {

			if (!id.equals("") && !id.equals("0")) {
				if (!Integer.valueOf(id).equals(existingByLabel.getId())) {
					throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.label.alreadyExistent");
				}
			} else {
				throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.label.alreadyExistent");
			}
		}
	}

	public void modifyPersistence(IDataSet ds, Map<String, String> logParam, UserProfile userProfile) {
		LOGGER.debug("IN");
		IDataSetDAO iDatasetDao = DAOFactory.getDataSetDAO();
		iDatasetDao.setUserProfile(userProfile);
		IDataSet previousDataset = iDatasetDao.loadDataSetById(ds.getId());
		if (previousDataset.isPersisted() && !ds.isPersisted()) {
			LOGGER.error("The dataset [" + previousDataset.getLabel() + "] has to be unpersisted");
			PersistedTableManager ptm = new PersistedTableManager(userProfile);
			ptm.dropTableIfExists(previousDataset.getDataSourceForWriting(), previousDataset.getTableNameForReading());
		}
		LOGGER.debug("OUT");
	}

	public void insertPersistence(IDataSet ds, Map<String, String> logParam, JSONObject json, UserProfile userProfile)
			throws Exception {
		LOGGER.debug("IN");

		if (ds.isPersisted()) {
			// Manage persistence of dataset if required. On modify it
			// will drop and create the destination table!
			LOGGER.debug("Start persistence...");
			// gets the dataset object informations
			AUDIT_LOGGER.info("-------------");
			AUDIT_LOGGER.info("Dataset INFO:");
			AUDIT_LOGGER.info("-------------");
			AUDIT_LOGGER.info("ID: " + ds.getId());
			AUDIT_LOGGER.info("LABEL: " + ds.getLabel());
			AUDIT_LOGGER.info("NAME: " + ds.getName());
			AUDIT_LOGGER.info("ORGANIZATION: " + ds.getOrganization());
			AUDIT_LOGGER
					.info("PERSIST TABLE NAME FOR DATASET WITH ID " + ds.getId() + " : " + ds.getPersistTableName());
			AUDIT_LOGGER.info("-------------");
			IDataSetDAO iDatasetDao = DAOFactory.getDataSetDAO();

			iDatasetDao.setUserProfile(userProfile);

			IDataSet dataset = iDatasetDao.loadDataSetByLabel(ds.getLabel());
			checkFileDataset(((VersionedDataSet) dataset).getWrappedDataset());

			if (ds.getDsType().equals(DataSetConstants.DS_DERIVED)) {
				if (((DerivedDataSet) ds).getSourceDataset() != null) {
					if (((VersionedDataSet) dataset).getWrappedDataset().getDsType()
							.equals(DataSetConstants.DS_DERIVED)) {
						DerivedDataSet dsDerived = (DerivedDataSet) ((VersionedDataSet) dataset).getWrappedDataset();
						dsDerived.setSourceDataset(((DerivedDataSet) ds).getSourceDataset());
						dsDerived.setJsonQuery((((DerivedDataSet) ds).getJsonQuery()));
						((VersionedDataSet) dataset).setWrappedDataset(dsDerived);

					}

				}
			}

			JSONArray parsListJSON = json.optJSONArray(DataSetConstants.PARS);
			if (parsListJSON != null && parsListJSON.length() > 0) {
				LOGGER.error("The dataset cannot be persisted because uses parameters!");
				throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.dsCannotPersist");
			}

			IPersistedManager ptm = new PersistedTableManager(userProfile);

			ptm.persistDataSet(dataset);

			LOGGER.debug("Persistence ended succesfully!");
		}
		LOGGER.debug("OUT");
	}

	private String datasetTest(JSONObject json, UserProfile userProfile) {
		try {
			JSONObject dataSetJSON = getDataSetResultsAsJSON(json, userProfile);
			if (dataSetJSON != null) {
				// writeBackToClient(new JSONSuccess(dataSetJSON));
				return dataSetJSON.toString();
			} else {
				throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.testError");
			}
		} catch (SpagoBIServiceException e) {
			throw e;
		} catch (Exception t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.testError", t);
		}
	}

	private JSONObject getDataSetResultsAsJSON(JSONObject json, UserProfile userProfile)
			throws EMFUserError, SpagoBIException {

		JSONObject dataSetJSON = null;
		JSONArray parsJSON = json.optJSONArray(DataSetConstants.PARS);

		IDataSet dataSet = getDataSet(json, userProfile);
		if (dataSet == null) {
			throw new SpagoBIRuntimeException("Impossible to retrieve dataset from request");
		}

		Map<String, String> parametersMap = new HashMap<>();
		String datasetTypeCode = json.optString(DataSetConstants.DS_TYPE_CD);

		String datasetTypeName = getDatasetTypeName(datasetTypeCode, userProfile);
		if (parsJSON != null) {
			if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_SOLR_TYPE)) {
				parametersMap = getSolrDataSetParametersAsMap(json, userProfile);
			} else {
				parametersMap = getDataSetParametersAsMap(json);
			}
		}
		IEngUserProfile profile = userProfile;

		dataSetJSON = getDatasetTestResultList(dataSet, parametersMap, profile, json);

		return dataSetJSON;
	}

	public JSONObject getDatasetTestResultList(IDataSet dataSet, Map<String, String> parametersFilled,
			IEngUserProfile profile, JSONObject json) {

		JSONObject dataSetJSON;

		LOGGER.debug("IN");

		dataSetJSON = null;

		try {
			Integer start = json.optInt(DataSetConstants.START);
			Integer limit = json.optInt(DataSetConstants.LIMIT);

			if (limit == null || limit == 0) {
				limit = DataSetConstants.LIMIT_DEFAULT;
			}

			dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));
			if (profile instanceof UserProfile) {
				dataSet.setUserProfile((UserProfile) profile);
			}
			dataSet.setParamsMap(parametersFilled);
			checkFileDataset(dataSet);
			IDataStore dataStore = null;
			try {
				if (dataSet.getTransformerId() != null) {
					dataStore = dataSet.test();
				} else {
					dataStore = dataSet.test(start, limit, GeneralUtilities.getDatasetMaxResults());
				}
				if (dataStore == null) {
					throw new SpagoBIServiceException(SERVICE_NAME,
							"Impossible to read resultset - the headers of the file must not be empty in order to be possible to parse the content of the file");
				}
			} catch (Throwable t) {
				Throwable rootException = t;
				while (rootException.getCause() != null) {
					rootException = rootException.getCause();
				}
				String rootErrorMsg = rootException.getMessage() != null ? rootException.getMessage()
						: rootException.getClass().getName();
				if (dataSet instanceof JDBCDataSet) {
					JDBCDataSet jdbcDataSet = (JDBCDataSet) dataSet;
					if (jdbcDataSet.getQueryScript() != null) {
						QuerableBehaviour querableBehaviour = (QuerableBehaviour) jdbcDataSet
								.getBehaviour(QuerableBehaviour.class.getName());
						String statement = querableBehaviour.getStatement();
						rootErrorMsg += "\nQuery statement: [" + statement + "]";
					}
				}

				throw new SpagoBIServiceException(SERVICE_NAME,
						"An unexpected error occured while executing dataset: " + rootErrorMsg, t);
			}

			try {
				JSONDataWriter dataSetWriter = new JSONDataWriter();
				dataSetJSON = (JSONObject) dataSetWriter.write(dataStore);
				if (dataSetJSON == null) {
					throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to read serialized resultset");
				}
			} catch (Exception t) {
				throw new SpagoBIServiceException(SERVICE_NAME,
						"An unexpected error occured while serializing resultset", t);
			}
		} catch (SpagoBIServiceException e) {
			throw e;
		} catch (Exception t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occured while getting dataset results",
					t);
		} finally {
			LOGGER.debug("OUT");
		}

		return dataSetJSON;
	}

	private IDataSet getDataSet(JSONObject json, UserProfile userProfile) {
		IDataSet dataSet = null;
		try {
			String datasetTypeCode = json.optString(DataSetConstants.DS_TYPE_CD);

			String datasetTypeName = getDatasetTypeName(datasetTypeCode, userProfile);
			if (datasetTypeName == null) {
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Impossible to resolve dataset type whose code is equal to [" + datasetTypeCode + "]");
			}
			dataSet = getDataSet(datasetTypeName, false, json, userProfile);
		} catch (SpagoBIServiceException e) {
			throw e;
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME,
					"An unexpected error occured while retriving dataset from request", t);
		}
		return dataSet;
	}

	/**
	 * @param parametersMap
	 * @param parsListJSON
	 * @return
	 */
	private boolean hasDuplicates(Map<String, String> parametersMap, JSONArray parsListJSON) {
		return parsListJSON.length() > parametersMap.keySet().size();
	}

}
