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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.qbe.dataset.FederatedDataSet;
import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.serializer.DataSetJSONSerializer;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
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
import it.eng.spagobi.tools.dataset.bo.ScriptDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.transformer.PivotDataSetTransformer;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.persist.IPersistedManager;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.dataset.utils.datamart.SpagoBICoreDatamartRetriever;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.json.JSONUtils;
import it.eng.spagobi.utilities.sql.SqlUtils;

public class ManageDataSetsForREST {

	private static final String PARAM_VALUE_NAME = "value";
	public static final String DEFAULT_VALUE_PARAM = "defaultValue";
	public static final String JOB_GROUP = "PersistDatasetExecutions";
	public static final String SERVICE_NAME = "ManageDatasets";
	// logger component
	public static Logger logger = Logger.getLogger(ManageDataSetsForREST.class);
	public static Logger auditlogger = Logger.getLogger("dataset.audit");

	protected IEngUserProfile profile;

	public String previewDataset(String jsonString, UserProfile userProfile) {
		JSONObject json = null;
		try {
			json = new JSONObject(jsonString);
		} catch (JSONException e) {
			logger.error("Cannot get values from JSON object while previewing dataset", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to preview Data Set due to bad formated json of data set.");

		}
		return datatsetTest(json, userProfile);
	}

	public String insertDataset(String jsonString, IDataSetDAO dsDao, Locale locale, UserProfile userProfile, HttpServletRequest req) throws JSONException {
		logger.debug("IN");
		JSONObject json = new JSONObject(jsonString);
		logger.debug("OUT");
		return datasetInsert(json, dsDao, locale, userProfile, req);
	}

	protected String datasetInsert(JSONObject json, IDataSetDAO dsDao, Locale locale, UserProfile userProfile, HttpServletRequest req) throws JSONException {
		IDataSet ds = getGuiGenericDatasetToInsert(json, userProfile);
		return datasetInsert(ds, dsDao, locale, userProfile, json, req);
	}

	protected IDataSet getGuiGenericDatasetToInsert(JSONObject json, UserProfile userProfile) throws JSONException {

		IDataSet ds = null;

		String label = json.getString("label");
		String name = json.getString("name");
		String description = json.optString("description");

		String datasetTypeCode = json.getString("dsTypeCd");
		String datasetTypeName = getDatasetTypeName(datasetTypeCode, userProfile);
		Boolean isFromSaveNoMetadata = json.optBoolean("isFromSaveNoMetadata");

		try {
			if (name != null && label != null && datasetTypeName != null && !datasetTypeName.equals("")) {
				try {
					ds = getDataSet(datasetTypeName, true, json, userProfile);
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

					String catTypeCd = json.optString("catTypeVn");

					String meta = json.optString("meta");
					String trasfTypeCd = json.optString("trasfTypeCd");

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

					String pars = getDataSetParametersAsString(json);
					if (pars != null) {
						ds.setParameters(pars);
					}

					if (trasfTypeCd != null && !trasfTypeCd.equals("")) {
						ds = setTransformer(ds, trasfTypeCd, json);
					}

					IDataSet dsRecalc = null;

					if (datasetTypeName != null && !datasetTypeName.equals("")) {
						try {
							dsRecalc = getDataSet(datasetTypeName, true, json, userProfile);
						} catch (Exception e) {
							logger.error("Error in building dataset of type " + datasetTypeName, e);
							throw e;
						}

						if (dsRecalc != null) {
							if (trasfTypeCd != null && !trasfTypeCd.equals("")) {
								dsRecalc = setTransformer(dsRecalc, trasfTypeCd, json);
							}
							// 1* String recalculateMetadata =
							// this.getAttributeAsString(DataSetConstants.RECALCULATE_METADATA);
							String recalculateMetadata = json.optString("recalculateMetadata");
							String dsMetadata = null;
							if ((recalculateMetadata == null || recalculateMetadata.trim().equals("yes") || recalculateMetadata.trim().equals("")
									|| recalculateMetadata.trim().equals("true")) && (!isFromSaveNoMetadata)) {
								// recalculate metadata
								logger.debug("Recalculating dataset's metadata: executing the dataset...");
								HashMap parametersMap = new HashMap();
								parametersMap = getDataSetParametersAsMap(true, json);

								IEngUserProfile profile = userProfile;
								ds.setPersisted(false);
								ds.setPersistedHDFS(false);

								IMetaData currentMetadata = null;
								try {
									currentMetadata = getDatasetTestMetadata(dsRecalc, parametersMap, profile, meta);
								} catch (Exception e) {
									logger.error("Error while recovering dataset metadata: check dataset definition ", e);
									throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.test.error.metadata", e);
								}

								// check if there are metadata field with same
								// columns or aliases
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
								// 2* String previousId =
								// getAttributeAsString(DataSetConstants.ID);
								String previousId = json.optString("id");
								if (previousId != null && !previousId.equals("")) {
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
								// 3* String id =
								// getAttributeAsString(DataSetConstants.ID);
								String id = json.optString("id");
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
					getPersistenceInfo(ds, json);
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

	private String getDatasetTypeName(String datasetTypeCode, UserProfile userProfile) {
		String datasetTypeName = null;

		try {

			if (datasetTypeCode == null) {
				return null;
			}

			List<Domain> datasetTypes = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.DATA_SET_TYPE);
			filterDataSetType(datasetTypes, userProfile);
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

	private IDataSet getDataSet(String datasetTypeName, boolean savingDataset, JSONObject json, UserProfile userProfile) throws Exception {

		IDataSet toReturn = null;
		JSONObject jsonDsConfig = new JSONObject();

		if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_FILE)) {
			FileDataSet dataSet = new FileDataSet();
			String dsId = json.optString(DataSetConstants.ID);
			String dsLabel = json.getString(DataSetConstants.LABEL);
			String fileType = json.getString(DataSetConstants.FILE_TYPE);
			String versionNum = json.getString(DataSetConstants.VERSION_NUM);

			String csvDelimiter = json.optString(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER);
			String csvQuote = json.optString(DataSetConstants.CSV_FILE_QUOTE_CHARACTER);

			String skipRows = json.optString(DataSetConstants.XSL_FILE_SKIP_ROWS);
			String limitRows = json.optString(DataSetConstants.XSL_FILE_LIMIT_ROWS);
			String xslSheetNumber = json.optString(DataSetConstants.XSL_FILE_SHEET_NUMBER);

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
			jsonDsConfig.put(DataSetConstants.XSL_FILE_SKIP_ROWS, skipRows);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_LIMIT_ROWS, limitRows);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_SHEET_NUMBER, xslSheetNumber);

			dataSet.setResourcePath(DAOConfig.getResourcePath());
			String fileName = json.getString(DataSetConstants.FILE_NAME);
			File pathFile = new File(fileName);
			fileName = pathFile.getName();
			if (savingDataset) {
				logger.debug("When saving the dataset the file associated will get the dataset label name");//
				if (dsLabel != null) {
					jsonDsConfig.put(DataSetConstants.FILE_NAME, dsLab + "." + fileType.toLowerCase());
				}
			} else {
				jsonDsConfig.put(DataSetConstants.FILE_NAME, fileName);
			}

			dataSet.setConfiguration(jsonDsConfig.toString());

			if ((dsId == null) || (dsId.isEmpty())) {
				logger.debug("By creating a new dataset, the file uploaded has to be renamed and moved");
				dataSet.setUseTempFile(true);
				if (savingDataset) {
					logger.debug("Rename and move the file");
					String resourcePath = dataSet.getResourcePath();
					if (dsLabel != null) {
						renameAndMoveDatasetFile(fileName, dsLab, resourcePath, fileType);
						dataSet.setUseTempFile(false);
					}
				}
			} else {
				logger.debug("Reading or modifying a existing dataset. If change the label then the name of the file should be changed");

				JSONObject configuration;
				Integer id_ds = json.getInt(DataSetConstants.ID);
				configuration = new JSONObject(DAOFactory.getDataSetDAO().loadDataSetById(id_ds).getConfiguration());
				String realName = configuration.getString("fileName");
				if (dsLabel != null && !realName.equals(dsLabel)) {

					File dest = new File(SpagoBIUtilities.getResourcePath() + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar
							+ dsLab + "." + configuration.getString("fileType").toLowerCase());
					File source = new File(
							SpagoBIUtilities.getResourcePath() + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar + realName);

					if (!source.getCanonicalPath().equals(dest.getCanonicalPath()) && savingDataset && !newFileUploaded) {
						logger.debug("Source and destination are not the same. Copying from source to dest");
						FileUtils.copyFile(source, dest);
					}
				}

				if (newFileUploaded) {
					logger.debug("Modifying an existing dataset with a new file uploaded");
					dataSet.setUseTempFile(true);

					logger.debug("Saving the existing dataset with a new file associated");
					if (savingDataset) {
						logger.debug("Rename and move the file");
						String resourcePath = dataSet.getResourcePath();
						if (dsLabel != null) {
							renameAndMoveDatasetFile(fileName, dsLabel + "_" + versionNum, resourcePath, fileType);
							dataSet.setUseTempFile(false);
						}
					}

				} else {
					logger.debug("Using existing dataset file, file in correct place");
					dataSet.setUseTempFile(false);
				}
			}

			dataSet.setFileType(fileType);

			if (savingDataset) {
				logger.debug("The file used will have the name equals to dataset's label");
				dataSet.setFileName(dsLab + "." + fileType.toLowerCase());
			} else {
				dataSet.setFileName(fileName);
			}
			toReturn = dataSet;
		}

		else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_CKAN)) {
			CkanDataSet dataSet = new CkanDataSet();

			String dsId = json.optString(DataSetConstants.DS_ID);
			String dsLabel = json.getString(DataSetConstants.LABEL);
			String fileType = json.getString(DataSetConstants.CKAN_FILE_TYPE);

			String csvDelimiter = json.optString(DataSetConstants.CKAN_CSV_FILE_DELIMITER_CHARACTER);
			String csvQuote = json.optString(DataSetConstants.CKAN_CSV_FILE_QUOTE_CHARACTER);

			String skipRows = json.optString(DataSetConstants.CKAN_XSL_FILE_SKIP_ROWS);
			String limitRows = json.optString(DataSetConstants.CKAN_XSL_FILE_LIMIT_ROWS);
			String xslSheetNumber = json.optString(DataSetConstants.CKAN_XSL_FILE_SHEET_NUMBER);

			String ckanUrl = json.optString(DataSetConstants.CKAN_URL);

			String ckanId = json.optString(DataSetConstants.CKAN_ID);
			String scopeCd = DataSetConstants.DS_SCOPE_USER;

			String ckanEncodig = json.optString(DataSetConstants.CKAN_CSV_FILE_ENCODING);

			Boolean newFileUploaded = false;
			if (json.optString("fileUploaded") != null) {
				newFileUploaded = Boolean.valueOf(json.optString("fileUploaded"));
			}

			jsonDsConfig.put(DataSetConstants.FILE_TYPE, fileType);
			jsonDsConfig.put(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER, csvDelimiter);
			jsonDsConfig.put(DataSetConstants.CSV_FILE_QUOTE_CHARACTER, csvQuote);
			jsonDsConfig.put(DataSetConstants.CSV_FILE_ENCODING, ckanEncodig);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_SKIP_ROWS, skipRows);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_LIMIT_ROWS, limitRows);
			jsonDsConfig.put(DataSetConstants.XSL_FILE_SHEET_NUMBER, xslSheetNumber);
			jsonDsConfig.put(DataSetConstants.CKAN_URL, ckanUrl);
			jsonDsConfig.put(DataSetConstants.CKAN_ID, ckanId);
			jsonDsConfig.put(DataSetConstants.DS_SCOPE, scopeCd);

			dataSet.setResourcePath(ckanUrl);
			dataSet.setCkanUrl(ckanUrl);

			String fileName = json.optString("fileName");
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

			dataSet.setFileType(fileType);

			if (savingDataset) {
				// the file used will have the name equals to dataset's label
				dataSet.setFileName(dsLabel + "." + fileType.toLowerCase());
			} else {
				// fileName can be empty if you preview it as administrator
				if (fileName.isEmpty()) {
					dataSet.setFileName(DataSetConstants.CKAN_DUMMY_FILENAME + "." + fileType.toLowerCase());
				} else {
					dataSet.setFileName(fileName);
				}
			}
			toReturn = dataSet;
		}

		else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_REST_TYPE)) {
			toReturn = manageRESTDataSet(savingDataset, jsonDsConfig, json);
		}

		else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_QUERY)) {
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
							logger.info("SQL is a SELECT statement.");
							if (query.toLowerCase().contains(" update ") || query.toLowerCase().contains(" delete ")
									|| query.toLowerCase().contains(" insert ")) {
								isSelect = false;
							} else {
								isSelect = true;
							}
						}
					} catch (Exception e) {
						logger.error("SQL is NOT a SELECT statement.");
						isSelect = false;
					}
					if (isSelect) {
						dataSet = JDBCDatasetFactory.getJDBCDataSet(dataSource);
					} else {
						logger.error("SQL is NOT a SELECT statement, or contains keywords like INSERT, UPDATE, DELETE.");
						throw new SpagoBIServiceException("Manage Dataset", "Provided SQL is NOT a SELECT statement");
					}

				} else {
					dataSet = JDBCDatasetFactory.getJDBCDataSet(dataSource);
				}

			}

			((ConfigurableDataSet) dataSet).setDataSource(dataSource);
			((ConfigurableDataSet) dataSet).setQuery(query);
			((ConfigurableDataSet) dataSet).setQueryScript(queryScript);
			((ConfigurableDataSet) dataSet).setQueryScriptLanguage(queryScriptLanguage);

			toReturn = dataSet;
		}

		else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_SCRIPT)) {
			ScriptDataSet dataSet = new ScriptDataSet();
			String script = json.optString("script");
			String scriptLanguage = json.optString(DataSetConstants.SCRIPT_LANGUAGE);
			jsonDsConfig.put(DataSetConstants.SCRIPT, script);
			jsonDsConfig.put(DataSetConstants.SCRIPT_LANGUAGE, scriptLanguage);
			dataSet.setScript(script);
			dataSet.setScriptLanguage(scriptLanguage);
			toReturn = dataSet;
		}

		else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_JCLASS)) {
			JavaClassDataSet dataSet = new JavaClassDataSet();
			String jclassName = json.getString(DataSetConstants.JCLASS_NAME);
			jsonDsConfig.put(DataSetConstants.JCLASS_NAME, jclassName);
			dataSet.setClassName(jclassName);
			toReturn = dataSet;
		}

		else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_CUSTOM)) {
			CustomDataSet customDs = new CustomDataSet();
			String customData = json.getString(DataSetConstants.CUSTOM_DATA);
			jsonDsConfig.put(DataSetConstants.CUSTOM_DATA, customData);
			customDs.setCustomData(customData);
			String javaClassName = json.getString(DataSetConstants.JCLASS_NAME);
			jsonDsConfig.put(DataSetConstants.JCLASS_NAME, javaClassName);
			customDs.setJavaClassName(javaClassName);
			toReturn = customDs.instantiate();
		}

		else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_QBE)) {
			QbeDataSet dataSet = new QbeDataSet();
			String qbeDatamarts = json.optString(DataSetConstants.QBE_DATAMARTS);
			String dataSourceLabel = json.optString(DataSetConstants.QBE_DATA_SOURCE);
			String jsonQuery = json.optString(DataSetConstants.QBE_JSON_QUERY);
			jsonDsConfig.put(DataSetConstants.QBE_DATAMARTS, qbeDatamarts);
			jsonDsConfig.put(DataSetConstants.QBE_DATA_SOURCE, dataSourceLabel);
			jsonDsConfig.put(DataSetConstants.QBE_JSON_QUERY, jsonQuery);

			// START -> This code should work instead of CheckQbeDataSets around
			// the projects
			SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
			Map parameters = dataSet.getParamsMap();
			if (parameters == null) {
				parameters = new HashMap();
				dataSet.setParamsMap(parameters);
			}
			dataSet.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
			logger.debug("Datamart retriever correctly added to Qbe dataset");
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
			toReturn = dataSet;
		}

		else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_FEDERATED)) {

			Integer id = json.getInt(DataSetConstants.ID);
			Assert.assertNotNull(id, "The federated dataset id is null");
			IDataSetDAO dao = DAOFactory.getDataSetDAO();
			dao.setUserProfile(userProfile);
			IDataSet dataSet = dao.loadDataSetById(id);
			// if its a federated dataset the datasource are teh ones on cahce
			SQLDBCache cache = (SQLDBCache) SpagoBICacheManager.getCache();
			dataSet.setDataSourceForReading(cache.getDataSource());
			dataSet.setDataSourceForWriting(cache.getDataSource());
			jsonDsConfig = new JSONObject(dataSet.getConfiguration());

			// update the json query getting the one passed from the qbe editor
			String jsonQuery = json.optString(DataSetConstants.QBE_JSON_QUERY);
			jsonDsConfig.put(DataSetConstants.QBE_JSON_QUERY, jsonQuery);
			((FederatedDataSet) (((VersionedDataSet) dataSet).getWrappedDataset())).setJsonQuery(jsonQuery);
			toReturn = dataSet;
		}

		else if (datasetTypeName.equalsIgnoreCase(DataSetConstants.DS_FLAT)) {
			FlatDataSet dataSet = new FlatDataSet();
			String tableName = json.optString(DataSetConstants.FLAT_TABLE_NAME);
			String dataSourceLabel = json.optString(DataSetConstants.DATA_SOURCE_FLAT);
			jsonDsConfig.put(DataSetConstants.FLAT_TABLE_NAME, tableName);
			jsonDsConfig.put(DataSetConstants.DATA_SOURCE, dataSourceLabel);
			dataSet.setTableName(tableName);
			IDataSource dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(dataSourceLabel);
			dataSet.setDataSource(dataSource);
			toReturn = dataSet;
		}

		toReturn.setConfiguration(jsonDsConfig.toString());
		return toReturn;
	}

	/*
	 * private void setUsefulItemsInSession(IDataSetDAO dsDao, Locale locale) { try { List dsTypesList =
	 * DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants .DATA_SET_TYPE); filterDataSetType(dsTypesList); List catTypesList = getCategories();
	 * getSessionContainer().setAttribute("catTypesList", catTypesList); List dataSourceList = DAOFactory.getDataSourceDAO().loadAllDataSources();
	 * getSessionContainer().setAttribute("dataSourceList", dataSourceList); List scriptLanguageList = DAOFactory.getDomainDAO().loadListDomainsByType(
	 * DataSetConstants.SCRIPT_TYPE); getSessionContainer().setAttribute("scriptLanguageList", scriptLanguageList); List trasfTypesList =
	 * DAOFactory.getDomainDAO().loadListDomainsByType (DataSetConstants.TRANSFORMER_TYPE); getSessionContainer().setAttribute("trasfTypesList",
	 * trasfTypesList); List sbiAttrs = DAOFactory.getSbiAttributeDAO().loadSbiAttributes(); getSessionContainer().setAttribute("sbiAttrsList", sbiAttrs);
	 *
	 * List scopeCdList = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants .DS_SCOPE); getSessionContainer().setAttribute("scopeCdList",
	 * scopeCdList);
	 *
	 * String filePath = SpagoBIUtilities.getResourcePath(); filePath += File.separator + "dataset" + File.separator + "files"; File dir = new File(filePath);
	 * String[] fileNames = dir.list(); getSessionContainer().setAttribute("fileNames", fileNames); } catch (EMFUserError | EMFInternalError e) {
	 * logger.error(e.getMessage(), e); throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.dsTypesRetrieve", e); } }
	 */

	public List getCategories(UserProfile userProfile) {
		IRoleDAO rolesDao = null;
		Role role = new Role();
		try {
			UserProfile profile = userProfile;
			rolesDao = DAOFactory.getRoleDAO();
			rolesDao.setUserProfile(profile);
			if (UserUtilities.hasDeveloperRole(profile) && !UserUtilities.hasAdministratorRole(profile)) {
				List<Domain> categoriesDev = new ArrayList<>();
				Collection<String> roles = profile.getRolesForUse();
				Iterator<String> itRoles = roles.iterator();
				while (itRoles.hasNext()) {
					String roleName = itRoles.next();
					role = rolesDao.loadByName(roleName);
					List<RoleMetaModelCategory> ds = rolesDao.getMetaModelCategoriesForRole(role.getId());
					List<Domain> array = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.CATEGORY_DOMAIN_TYPE);
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
				return DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.CATEGORY_DOMAIN_TYPE);
			}
		} catch (Exception e) {
			logger.error("Role with selected id: " + role.getId() + " doesn't exists", e);
			throw new SpagoBIRuntimeException("Item with selected id: " + role.getId() + " doesn't exists", e);
		}
	}

	private void filterDataSetType(List<Domain> domains, UserProfile userProfile) throws EMFInternalError {
		if (!userProfile.getFunctionalities().contains(SpagoBIConstants.CKAN_FUNCTIONALITY)) {
			Iterator<Domain> iterator = domains.iterator();
			while (iterator.hasNext()) {
				Domain domain = iterator.next();
				if (domain.getValueCd().toLowerCase().contains("ckan")) {
					iterator.remove();
				}
			}
		}
	}

	private String getDataSetParametersAsString(JSONObject json) {
		String parametersString = null;

		try {
			JSONArray parsListJSON = json.optJSONArray(DataSetConstants.PARS);
			if (parsListJSON == null) {
				return null;
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
		} catch (Throwable t) {
			if (t instanceof SpagoBIServiceException) {
				throw (SpagoBIServiceException) t;
			}
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occured while deserializing dataset parameters", t);

		}
		return parametersString;
	}

	private IDataSet setTransformer(IDataSet ds, String trasfTypeCd, JSONObject json) throws JSONException, EMFUserError {
		List<Domain> domainsTrasf = DAOFactory.getDomainDAO().loadListDomainsByType(DataSetConstants.TRANSFORMER_TYPE);
		HashMap<String, Integer> domainTrasfIds = new HashMap<>();
		if (domainsTrasf != null) {
			for (int i = 0; i < domainsTrasf.size(); i++) {
				domainTrasfIds.put(domainsTrasf.get(i).getValueCd(), domainsTrasf.get(i).getValueId());
			}
		}
		Integer transformerId = domainTrasfIds.get(trasfTypeCd);

		String pivotColName = json.optString("pivotColName");
		if (pivotColName != null) {
			pivotColName = pivotColName.trim();
		}
		String pivotColValue = json.optString("pivotColValue");
		if (pivotColValue != null) {
			pivotColValue = pivotColValue.trim();
		}
		String pivotRowName = json.optString("pivotRowName");
		if (pivotRowName != null) {
			pivotRowName = pivotRowName.trim();
		}
		Boolean pivotIsNumRows = json.optBoolean("pivotIsNumRows");

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

	private HashMap<String, String> getDataSetParametersAsMap(boolean forSave, JSONObject json) {
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

	public IMetaData getDatasetTestMetadata(IDataSet dataSet, HashMap parametersFilled, IEngUserProfile profile, String metadata) throws Exception {
		logger.debug("IN");

		IDataStore dataStore = null;

		Integer start = new Integer(0);
		Integer limit = new Integer(10);

		dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));
		dataSet.setParamsMap(parametersFilled);
		try {
			checkFileDataset(dataSet);
			dataSet.loadData(start, limit, GeneralUtilities.getDatasetMaxResults());
			dataStore = dataSet.getDataStore();
			// DatasetMetadataParser dsp = new DatasetMetadataParser();

			JSONArray metadataArray = null;
			IMetaData metaData = dataStore.getMetaData();

			try {
				metadataArray = JSONUtils.toJSONArray(metadata);
				for (int i = 0; i < metaData.getFieldCount(); i++) {
					IFieldMetaData ifmd = metaData.getFieldMeta(i);
					for (int j = 0; j < metadataArray.length(); j++) {
						if (ifmd.getName().equals((metadataArray.getJSONObject(j)).getString("name"))) {
							if ("MEASURE".equals((metadataArray.getJSONObject(j)).getString("fieldType"))) {
								ifmd.setFieldType(IFieldMetaData.FieldType.MEASURE);
							} else {
								ifmd.setFieldType(IFieldMetaData.FieldType.ATTRIBUTE);
							}
							break;
						}
					}
				}
			} catch (ClassCastException e) {
				logger.debug("Recieving an object instead of array for metadata", e);
			}

			// dsMetadata = dsp.metadataToXML(dataStore.getMetaData());
		} catch (Exception e) {
			logger.error("Error while executing dataset for test purpose", e);
			throw e;
		}

		logger.debug("OUT");
		if (dataStore == null)
			return null;

		if (JSONUtils.toJSONArray(metadata).length() == 0) {
			setNumericValuesAsMeasures(dataStore.getMetaData());
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

	private void getPersistenceInfo(IDataSet ds, JSONObject json) throws EMFUserError, JSONException {
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
	private void renameAndMoveDatasetFile(String originalFileName, String newFileName, String resourcePath, String fileType) {
		String filePath = resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar + "temp" + File.separatorChar;
		String fileNewPath = resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar;

		File originalDatasetFile = new File(filePath + originalFileName);
		File newDatasetFile = new File(fileNewPath + newFileName + "." + fileType.toLowerCase());

		String filePathCloning = resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar;
		File originalDatasetFileCloning = new File(filePathCloning + originalFileName);

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
		} else if (originalDatasetFileCloning.exists()) {
			try {
				boolean isTwoEqual = FileUtils.contentEquals(originalDatasetFileCloning, newDatasetFile);
				if (!isTwoEqual) {
					FileUtils.copyFile(originalDatasetFileCloning, newDatasetFile);
				}

			} catch (IOException e) {
				logger.debug("Cannot move dataset File");
				throw new SpagoBIRuntimeException("Cannot move dataset File", e);
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

	public void deleteDatasetFile(IDataSet dataset) {
		if (dataset instanceof VersionedDataSet) {
			VersionedDataSet versionedDataset = (VersionedDataSet) dataset;
			dataset = versionedDataset.getWrappedDataset();
		}
		FileDataSet fileDataset = (FileDataSet) dataset;
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

	private RESTDataSet manageRESTDataSet(boolean savingDataset, JSONObject config, JSONObject json) throws JSONException {
		for (String sa : DataSetConstants.REST_STRING_ATTRIBUTES) {
			config.put(sa, json.optString(sa));
		}
		for (String ja : DataSetConstants.REST_JSON_OBJECT_ATTRIBUTES) {
			config.put(ja, new JSONObject(json.getString(ja)));
		}
		for (String ja : DataSetConstants.REST_JSON_ARRAY_ATTRIBUTES) {
			config.put(ja, new JSONArray(json.getString(ja)));
		}
		RESTDataSet res = new RESTDataSet(config);
		return res;
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

	private void checkFileDataset(IDataSet dataSet) {
		if (dataSet instanceof FileDataSet) {
			((FileDataSet) dataSet).setResourcePath(DAOConfig.getResourcePath());
		}
	}

	protected String datasetInsert(IDataSet ds, IDataSetDAO dsDao, Locale locale, UserProfile userProfile, JSONObject json, HttpServletRequest req)
			throws JSONException {
		JSONObject attributesResponseSuccessJSON = new JSONObject();
		HashMap<String, String> logParam = new HashMap();

		if (ds != null) {
			logParam.put("NAME", ds.getName());
			logParam.put("LABEL", ds.getLabel());
			logParam.put("TYPE", ds.getDsType());
			String id = json.optString(DataSetConstants.ID);
			try {
				if (id != null && !id.equals("") && !id.equals("0")) {
					ds.setId(Integer.valueOf(id));
					modifyPersistence(ds, logParam, req);
					dsDao.modifyDataSet(ds);
					logger.debug("Resource " + id + " updated");
					attributesResponseSuccessJSON.put("success", true);
					attributesResponseSuccessJSON.put("responseText", "Operation succeded");
					attributesResponseSuccessJSON.put("id", id);
					attributesResponseSuccessJSON.put("dateIn", ds.getDateIn());
					attributesResponseSuccessJSON.put("userIn", ds.getUserIn());
					attributesResponseSuccessJSON.put("meta", DataSetJSONSerializer.metadataSerializerChooser(ds.getDsMetadata()));
				} else {
					IDataSet existing = dsDao.loadDataSetByLabel(ds.getLabel());
					if (existing != null) {
						throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.labelAlreadyExistent");
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
						attributesResponseSuccessJSON.put("meta", DataSetJSONSerializer.metadataSerializerChooser(dsSaved.getDsMetadata()));
					}
				}
				String operation = (id != null && !id.equals("") && !id.equals("0")) ? "DATA_SET.MODIFY" : "DATA_SET.ADD";
				Boolean isFromSaveNoMetadata = json.optBoolean(DataSetConstants.IS_FROM_SAVE_NO_METADATA);
				// handle insert of persistence and scheduling
				if (!isFromSaveNoMetadata) {
					auditlogger.info("[Start persisting metadata for dataset with id " + ds.getId() + "]");
					insertPersistence(ds, logParam, json, userProfile, req);
					auditlogger.info("Metadata saved for dataset with id " + ds.getId() + "]");
					auditlogger.info("[End persisting metadata for dataset with id " + ds.getId() + "]");
				}

				AuditLogUtilities.updateAudit(req, profile, operation, logParam, "OK");
				return attributesResponseSuccessJSON.toString();
			} catch (Exception e) {
				AuditLogUtilities.updateAudit(req, profile, "DATA_SET.ADD", logParam, "KO");
				throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.saveDsError", e);
			}
		} else {
			AuditLogUtilities.updateAudit(req, profile, "DATA_SET.ADD/MODIFY", logParam, "ERR");
			logger.error("DataSet name, label or type are missing");
			throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.fillFieldsError");
		}
	}

	public void modifyPersistence(IDataSet ds, HashMap<String, String> logParam, HttpServletRequest req) throws Exception {
		logger.debug("IN");
		IDataSetDAO iDatasetDao = DAOFactory.getDataSetDAO();
		iDatasetDao.setUserProfile(profile);
		IDataSet previousDataset = iDatasetDao.loadDataSetById(ds.getId());
		if (previousDataset.isPersisted() && !ds.isPersisted()) {
			logger.error("The dataset [" + previousDataset.getLabel() + "] has to be unpersisted");
			PersistedTableManager ptm = new PersistedTableManager(profile);
			ptm.dropTableIfExists(previousDataset.getDataSourceForWriting(), previousDataset.getTableNameForReading());
		}
		logger.debug("OUT");
	}

	public void insertPersistence(IDataSet ds, HashMap<String, String> logParam, JSONObject json, UserProfile userProfile, HttpServletRequest req)
			throws Exception {
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

			profile = userProfile;
			iDatasetDao.setUserProfile(profile);

			IDataSet dataset = iDatasetDao.loadDataSetByLabel(ds.getLabel());
			checkFileDataset(((VersionedDataSet) dataset).getWrappedDataset());

			JSONArray parsListJSON = json.getJSONArray(DataSetConstants.PARS);
			if (parsListJSON.length() > 0) {
				logger.error("The dataset cannot be persisted because uses parameters!");
				throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.dsCannotPersist");
			}

			IPersistedManager ptm = new PersistedTableManager(profile);

			ptm.persistDataSet(dataset);

			logger.debug("Persistence ended succesfully!");
		}
		logger.debug("OUT");
	}

	private String datatsetTest(JSONObject json, UserProfile userProfile) {
		try {
			JSONObject dataSetJSON = getDataSetResultsAsJSON(json, userProfile);
			if (dataSetJSON != null) {
				// writeBackToClient(new JSONSuccess(dataSetJSON));
				return dataSetJSON.toString();
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

	private JSONObject getDataSetResultsAsJSON(JSONObject json, UserProfile userProfile) throws EMFUserError, JSONException {

		JSONObject dataSetJSON = null;
		JSONArray parsJSON = json.optJSONArray(DataSetConstants.PARS);
		String transformerTypeCode = json.optString(DataSetConstants.TRASFORMER_TYPE_CD);

		IDataSet dataSet = getDataSet(json, userProfile);
		if (dataSet == null) {
			throw new SpagoBIRuntimeException("Impossible to retrieve dataset from request");
		}

		if (StringUtilities.isNotEmpty(transformerTypeCode)) {
			dataSet = setTransformer(dataSet, transformerTypeCode, json);
		}
		HashMap<String, String> parametersMap = new HashMap<>();
		if (parsJSON != null) {
			parametersMap = getDataSetParametersAsMap(false, json);
		}
		IEngUserProfile profile = userProfile;

		dataSetJSON = getDatasetTestResultList(dataSet, parametersMap, profile, json);

		return dataSetJSON;
	}

	public JSONObject getDatasetTestResultList(IDataSet dataSet, HashMap<String, String> parametersFilled, IEngUserProfile profile, JSONObject json) {

		JSONObject dataSetJSON;

		logger.debug("IN");

		dataSetJSON = null;
		try {
			Integer start = json.optInt(DataSetConstants.START);
			Integer limit = json.optInt(DataSetConstants.LIMIT);

			if (limit == null || limit == 0) {
				limit = DataSetConstants.LIMIT_DEFAULT;
			}

			dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));
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

	private IDataSet getDataSet(JSONObject json, UserProfile userProfile) {
		IDataSet dataSet = null;
		try {
			String datasetTypeCode = json.optString(DataSetConstants.DS_TYPE_CD);

			String datasetTypeName = getDatasetTypeName(datasetTypeCode, userProfile);
			if (datasetTypeName == null) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to resolve dataset type whose code is equal to [" + datasetTypeCode + "]");
			}
			dataSet = getDataSet(datasetTypeName, false, json, userProfile);
		} catch (Throwable t) {
			if (t instanceof SpagoBIServiceException) {
				throw (SpagoBIServiceException) t;
			}
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occured while retriving dataset from request", t);
		}
		return dataSet;
	}

	public String loadDataSetList(String jsonString, UserProfile userProfile) {
		JSONObject json = null;
		try {
			json = new JSONObject(jsonString);
		} catch (JSONException e) {
			logger.error("Cannot get values from JSON object while previewing dataset", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to preview Data Set due to bad formated json of data set.");

		}
		return datatsetTest(json, userProfile);
	}
}
