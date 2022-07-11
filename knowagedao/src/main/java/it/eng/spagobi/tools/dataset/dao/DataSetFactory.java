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
package it.eng.spagobi.tools.dataset.dao;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.qbe.dataset.FederatedDataSet;
import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.federateddataset.dao.ISbiFederationDefinitionDAO;
import it.eng.spagobi.federateddataset.dao.SbiFederationUtils;
import it.eng.spagobi.federateddataset.metadata.SbiFederationDefinition;
import it.eng.spagobi.security.hmacfilter.HMACSecurityException;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.bo.CkanDataSet;
import it.eng.spagobi.tools.dataset.bo.ConfigurableDataSet;
import it.eng.spagobi.tools.dataset.bo.CustomDataSet;
import it.eng.spagobi.tools.dataset.bo.DataSetParameterItem;
import it.eng.spagobi.tools.dataset.bo.FacetSolrDataSet;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.FlatDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCBigQueryDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDatasetFactory;
import it.eng.spagobi.tools.dataset.bo.JDBCHiveDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCOrientDbDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCRedShiftDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCSpannerDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCSynapseDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCVerticaDataSet;
import it.eng.spagobi.tools.dataset.bo.JavaClassDataSet;
import it.eng.spagobi.tools.dataset.bo.MongoDataSet;
import it.eng.spagobi.tools.dataset.bo.PreparedDataSet;
import it.eng.spagobi.tools.dataset.bo.PythonDataSet;
import it.eng.spagobi.tools.dataset.bo.RESTDataSet;
import it.eng.spagobi.tools.dataset.bo.SPARQLDataSet;
import it.eng.spagobi.tools.dataset.bo.ScriptDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.transformer.PivotDataSetTransformer;
import it.eng.spagobi.tools.dataset.constants.CkanDataSetConstants;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.constants.SolrDataSetConstants;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect;
import it.eng.spagobi.tools.dataset.utils.datamart.SpagoBICoreDatamartRetriever;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.DataSourceDAOHibImpl;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;
import it.eng.spagobi.utilities.sql.SqlUtils;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class DataSetFactory {

	public static final String JDBC_DS_TYPE = "Query";
	public static final String FILE_DS_TYPE = "File";
	public static final String CKAN_DS_TYPE = "Ckan";
	public static final String SCRIPT_DS_TYPE = "Script";
	public static final String JCLASS_DS_TYPE = "Java Class";
	public static final String WS_DS_TYPE = "Web Service";
	public static final String QBE_DS_TYPE = "Qbe";
	public static final String CUSTOM_DS_TYPE = "Custom";
	public static final String FEDERATED_DS_TYPE = "Federated";
	public static final String FLAT_DS_TYPE = "Flat";
	public static final String PREPARED_DS_TYPE = "Prepared";
	public static final String SPARQL_DS_TYPE = "SPARQL";

	static private Logger logger = Logger.getLogger(DataSetFactory.class);

	public static IDataSet toGuiDataSet(SbiDataSet sbiDataSet) {
		IDataSet guiDataSet;

		guiDataSet = new VersionedDataSet();

		if (sbiDataSet != null) {
			guiDataSet.setId(sbiDataSet.getId().getDsId());
			guiDataSet.setName(sbiDataSet.getName());
			guiDataSet.setLabel(sbiDataSet.getLabel());
			guiDataSet.setDescription(sbiDataSet.getDescription());

			guiDataSet.setDsMetadata(sbiDataSet.getDsMetadata());
			guiDataSet.setUserIn(sbiDataSet.getUserIn());
			guiDataSet.setDateIn(new Date());

			guiDataSet.setId(sbiDataSet.getId().getDsId());
			guiDataSet.setPersistedHDFS(sbiDataSet.isPersistedHDFS());
		}

		return guiDataSet;
	}

	public static IDataSet toGuiDataSet(IDataSet dataSet) {
		IDataSet toReturn = dataSet;

		if (dataSet instanceof FileDataSet) {
			toReturn.setDsType(FILE_DS_TYPE);
		}

		if (dataSet instanceof CkanDataSet) {
			toReturn.setDsType(CKAN_DS_TYPE);
		}

		if (dataSet instanceof RESTDataSet) {
			toReturn.setDsType(DataSetConstants.DS_REST_NAME);
		}

		if (dataSet instanceof JDBCDataSet) {
			toReturn.setDsType(JDBC_DS_TYPE);
		}

		if (dataSet instanceof SPARQLDataSet) {
			toReturn.setDsType(SPARQL_DS_TYPE);
		}

		if (dataSet instanceof QbeDataSet) {

			QbeDataSet aQbeDataSet = (QbeDataSet) dataSet;
			aQbeDataSet.setJsonQuery(aQbeDataSet.getJsonQuery());
			aQbeDataSet.setDatamarts(aQbeDataSet.getDatamarts());
			IDataSource iDataSource = aQbeDataSet.getDataSource();
			if (iDataSource != null) {
				aQbeDataSet.setDataSource(iDataSource);
			}

			toReturn.setDsType(QBE_DS_TYPE);
		}

		if (dataSet instanceof ScriptDataSet) {
			toReturn.setDsType(SCRIPT_DS_TYPE);
		}

		if (dataSet instanceof JavaClassDataSet) {
			toReturn.setDsType(JCLASS_DS_TYPE);
		}

		if (dataSet instanceof CustomDataSet) {
			toReturn.setDsType(CUSTOM_DS_TYPE);
		}

		if (dataSet instanceof FlatDataSet) {
			toReturn.setDsType(FLAT_DS_TYPE);
		}

		if (dataSet instanceof RESTDataSet) {
			toReturn.setDsType(DataSetConstants.DS_REST_NAME);
		}

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
		toReturn.setPersistedHDFS(dataSet.isPersistedHDFS());
		toReturn.setPersistTableName(dataSet.getPersistTableName());
		toReturn.setScopeCd(dataSet.getScopeCd());
		toReturn.setScopeId(dataSet.getScopeId());

		return toReturn;
	}

	public static IDataSet toDataSet(SbiDataSet sbiDataSet) {
		return toDataSet(sbiDataSet, null);
	}

	public static Set<IDataSet> toDataSet(Set<SbiDataSet> sbiDataSet, IEngUserProfile userProfile) {

		Set<IDataSet> toReturn = new HashSet<>();

		for (Iterator iterator = sbiDataSet.iterator(); iterator.hasNext();) {
			SbiDataSet sbiDataSet3 = (SbiDataSet) iterator.next();
			toReturn.add(toDataSet(sbiDataSet3, userProfile));
		}

		return toReturn;
	}

	public static Set<IDataSet> toDataSet(List<SbiDataSet> sbiDataSet, IEngUserProfile userProfile) {

		Set<IDataSet> toReturn = new HashSet<>();

		for (Iterator iterator = sbiDataSet.iterator(); iterator.hasNext();) {
			SbiDataSet sbiDataSet3 = (SbiDataSet) iterator.next();
			toReturn.add(toDataSet(sbiDataSet3, userProfile));
		}

		return toReturn;
	}

	public static IDataSet toDataSet(SbiDataSet sbiDataSet, IEngUserProfile userProfile) {
		IDataSet ds = null;
		VersionedDataSet versionDS = null;
		logger.debug("IN");
		String config = JSONUtils.escapeJsonString(sbiDataSet.getConfiguration());
		JSONObject jsonConf = ObjectUtils.toJSONObject(config);
		try {
			String type = sbiDataSet.getType();
			if (type.equalsIgnoreCase(DataSetConstants.DS_FILE)) {
				ds = new FileDataSet();
				FileDataSet fds = (FileDataSet) ds;
				String resourcePath = DAOConfig.getResourcePath();
				fds.setResourcePath(resourcePath);

				fds.setConfiguration(jsonConf.toString());

				if (jsonConf.getString(DataSetConstants.FILE_TYPE) != null) {
					fds.setFileType(jsonConf.getString(DataSetConstants.FILE_TYPE));
				}
				fds.setFileName(jsonConf.getString(DataSetConstants.FILE_NAME));
				fds.setDsType(FILE_DS_TYPE);
			} else if (DataSetConstants.DS_REST_TYPE.equalsIgnoreCase(type)) {
				ds = manageRESTDataSet(jsonConf);
			} else if (DataSetConstants.DS_PYTHON_TYPE.equalsIgnoreCase(type)) {
				ds = managePythonDataSet(jsonConf);
			} else if (DataSetConstants.DS_SPARQL.equalsIgnoreCase(type)) {
				ds = manageSPARQLDataSet(jsonConf);
			} else if (DataSetConstants.DS_SOLR_TYPE.equalsIgnoreCase(type)) {
				ds = manageSolrDataSet(jsonConf, sbiDataSet.getParametersList());
			} else if (type.equalsIgnoreCase(DataSetConstants.DS_CKAN)) {
				ds = new CkanDataSet();
				CkanDataSet cds = (CkanDataSet) ds;

				String resourcePath = jsonConf.optString("ckanUrl");
				// String ckanResourceId = jsonConf.optString("ckanResourceId");
				cds.setResourcePath(resourcePath);
				cds.setCkanUrl(resourcePath);

				if (!jsonConf.isNull(DataSetConstants.FILE_TYPE)) {
					jsonConf.put(CkanDataSetConstants.CKAN_FILE_TYPE, jsonConf.getString(DataSetConstants.FILE_TYPE));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER)) {
					jsonConf.put(CkanDataSetConstants.CKAN_CSV_FILE_DELIMITER_CHARACTER, jsonConf.getString(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_QUOTE_CHARACTER)) {
					jsonConf.put(CkanDataSetConstants.CKAN_CSV_FILE_QUOTE_CHARACTER, jsonConf.getString(DataSetConstants.CSV_FILE_QUOTE_CHARACTER));
				}
				if (!jsonConf.isNull(DataSetConstants.FILE_DATE_FORMAT)) {
					jsonConf.put(CkanDataSetConstants.CKAN_CSV_DATE_FORMAT, jsonConf.getString(DataSetConstants.FILE_DATE_FORMAT));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_ENCODING)) {
					jsonConf.put(CkanDataSetConstants.CKAN_CSV_FILE_ENCODING, jsonConf.getString(DataSetConstants.CSV_FILE_ENCODING));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_SKIP_ROWS)) {
					jsonConf.put(CkanDataSetConstants.CKAN_XSL_FILE_SKIP_ROWS, jsonConf.getString(DataSetConstants.XSL_FILE_SKIP_ROWS));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_LIMIT_ROWS)) {
					jsonConf.put(CkanDataSetConstants.CKAN_XSL_FILE_LIMIT_ROWS, jsonConf.getString(DataSetConstants.XSL_FILE_LIMIT_ROWS));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_SHEET_NUMBER)) {
					jsonConf.put(CkanDataSetConstants.CKAN_XSL_FILE_SHEET_NUMBER, jsonConf.getString(DataSetConstants.XSL_FILE_SHEET_NUMBER));
				}
				if (!jsonConf.isNull(CkanDataSetConstants.CKAN_ID)) {
					jsonConf.put(CkanDataSetConstants.CKAN_ID, jsonConf.getString(CkanDataSetConstants.CKAN_ID));
				}

				cds.setConfiguration(jsonConf.toString());

				if (jsonConf.getString(DataSetConstants.FILE_TYPE) != null) {
					cds.setFileType(jsonConf.getString(DataSetConstants.FILE_TYPE));
				}
				cds.setFileName(jsonConf.getString(DataSetConstants.FILE_NAME));
				cds.setDsType(CKAN_DS_TYPE);

			} else if (type.equalsIgnoreCase(DataSetConstants.DS_QUERY)) {

				DataSourceDAOHibImpl dataSourceDao = new DataSourceDAOHibImpl();
				if (userProfile != null)
					dataSourceDao.setUserProfile(userProfile);

				String dataSourceLabel = jsonConf.getString(DataSetConstants.DATA_SOURCE);
				IDataSource dataSource = dataSourceDao.loadDataSourceByLabel(dataSourceLabel);

				if (dataSource == null) {
					logger.error("Datasource " + dataSourceLabel + " is null for " + sbiDataSet.getLabel());
					throw new SpagoBIRuntimeException("Datasource " + dataSourceLabel + " no longer exists for " + sbiDataSet.getLabel() + " Dataset");
				}

				DatabaseDialect dialect = DataBaseFactory.getDataBase(dataSource).getDatabaseDialect();
				if (dialect.equals(DatabaseDialect.MONGO)) {
					ds = new MongoDataSet();
				} else {
					ds = JDBCDatasetFactory.getJDBCDataSet(dataSource);
				}

				ds.setConfiguration(sbiDataSet.getConfiguration());
				ds.setDsType(JDBC_DS_TYPE);
				((ConfigurableDataSet) ds).setQuery(jsonConf.getString(DataSetConstants.QUERY));
				((ConfigurableDataSet) ds).setQueryScript(jsonConf.getString(DataSetConstants.QUERY_SCRIPT));
				((ConfigurableDataSet) ds).setQueryScriptLanguage(jsonConf.getString(DataSetConstants.QUERY_SCRIPT_LANGUAGE));

				if (dataSource != null) {
					((ConfigurableDataSet) ds).setDataSource(dataSource);
					// if data source associated is not read only set is as
					// write one.
					if (!dataSource.checkIsReadOnly()) {
						ds.setDataSourceForWriting(dataSource);
					}
				}
			} else if (type.equalsIgnoreCase(DataSetConstants.DS_SCRIPT)) {
				ds = new ScriptDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((ScriptDataSet) ds).setScript(jsonConf.getString(DataSetConstants.SCRIPT));
				((ScriptDataSet) ds).setScriptLanguage(jsonConf.getString(DataSetConstants.SCRIPT_LANGUAGE));
				ds.setDsType(SCRIPT_DS_TYPE);
			} else if (type.equalsIgnoreCase(DataSetConstants.DS_JCLASS)) {
				ds = new JavaClassDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((JavaClassDataSet) ds).setClassName(jsonConf.getString(DataSetConstants.JCLASS_NAME));
				ds.setDsType(JCLASS_DS_TYPE);
			} else if (type.equalsIgnoreCase(DataSetConstants.DS_CUSTOM)) {
				ds = new CustomDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((CustomDataSet) ds).setCustomData(jsonConf.getString(DataSetConstants.CUSTOM_DATA));
				((CustomDataSet) ds).setJavaClassName(jsonConf.getString(DataSetConstants.JCLASS_NAME));
				ds.setDsType(CUSTOM_DS_TYPE);
			} else if (type.equalsIgnoreCase(DataSetConstants.DS_FEDERATED)) {

				SbiFederationDefinition sbiFederation = sbiDataSet.getFederation();

				ISbiFederationDefinitionDAO dao = DAOFactory.getFedetatedDatasetDAO();
				Set<IDataSet> sourcesDatasets = dao.loadAllFederatedDataSets(sbiFederation.getFederation_id());

				UserProfile profile = (UserProfile) userProfile;
				String userId = null;
				if (profile != null) {
					userId = (String) profile.getUserId();
					logger.debug("Federated dataset but can't fid the user id");
				}

				ds = new FederatedDataSet(SbiFederationUtils.toDatasetFederationWithDataset(sbiFederation, sourcesDatasets), userId);
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((FederatedDataSet) ds).setJsonQuery(jsonConf.getString(DataSetConstants.QBE_JSON_QUERY));

				SbiFederationDefinition sbiFedDef = sbiDataSet.getFederation();
				FederationDefinition fd = DAOFactory.getFedetatedDatasetDAO().loadFederationDefinition(sbiFedDef.getFederation_id());
				((FederatedDataSet) ds).setDatasetFederation(fd);

				// START -> This code should work instead of CheckQbeDataSets
				// around the projects

				Map parameters = ds.getParamsMap();
				if (parameters == null) {
					parameters = new HashMap();
					ds.setParamsMap(parameters);
				}

				// END

				IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
				if (userProfile != null)
					dataSourceDAO.setUserProfile(userProfile);

				IDataSource dataSource = dataSourceDAO.loadDataSourceWriteDefault();
				ds.setDataSourceForWriting(dataSource);
				ds.setDataSourceForReading(dataSource);
				ds.setDataSource(dataSource);
				ds.setDsType(FEDERATED_DS_TYPE);

			} else if (type.equalsIgnoreCase(DataSetConstants.DS_QBE)) {
				ds = new QbeDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((QbeDataSet) ds).setJsonQuery(jsonConf.getString(DataSetConstants.QBE_JSON_QUERY));
				((QbeDataSet) ds).setDatamarts(jsonConf.getString(DataSetConstants.QBE_DATAMARTS));

				// START -> This code should work instead of CheckQbeDataSets
				// around the projects
				SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
				Map parameters = ds.getParamsMap();
				if (parameters == null) {
					parameters = new HashMap();
					ds.setParamsMap(parameters);
				}
				ds.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
				logger.debug("Datamart retriever correctly added to Qbe dataset");
				// END

				DataSourceDAOHibImpl dataSourceDao = new DataSourceDAOHibImpl();
				if (userProfile != null)
					dataSourceDao.setUserProfile(userProfile);
				IDataSource dataSource = dataSourceDao.loadDataSourceByLabel(jsonConf.getString(DataSetConstants.QBE_DATA_SOURCE));
				if (dataSource != null) {
					((QbeDataSet) ds).setDataSource(dataSource);
					if (!dataSource.checkIsReadOnly()) {
						ds.setDataSourceForWriting(dataSource);
					}
				}
				ds.setDsType(QBE_DS_TYPE);

			} else if (type.equalsIgnoreCase(DataSetConstants.DS_FLAT)) {
				ds = new FlatDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				if (ds.isPersisted()) {
					throw new Exception("A flat data set can't be persisted");
				}
				DataSourceDAOHibImpl dataSourceDao = new DataSourceDAOHibImpl();
				if (userProfile != null)
					dataSourceDao.setUserProfile(userProfile);

				/*
				 * WORKAROUND : in the past the datasource attribute was dataSource and not dataSourceFlat.
				 */
				String dataSourceName = null;
				if (jsonConf.has(DataSetConstants.DATA_SOURCE)) {
					dataSourceName = jsonConf.getString(DataSetConstants.DATA_SOURCE);
				} else {
					dataSourceName = jsonConf.getString(DataSetConstants.DATA_SOURCE_FLAT);
				}

				IDataSource dataSource = dataSourceDao.loadDataSourceByLabel(dataSourceName);
				((FlatDataSet) ds).setDataSource(dataSource);
				((FlatDataSet) ds).setTableName(jsonConf.getString(DataSetConstants.FLAT_TABLE_NAME));
				ds.setDsType(FLAT_DS_TYPE);
			} else if (type.equalsIgnoreCase(DataSetConstants.DS_PREPARED)) {
				ds = new PreparedDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				DataSourceDAOHibImpl dataSourceDao = new DataSourceDAOHibImpl();
				if (userProfile != null)
					dataSourceDao.setUserProfile(userProfile);

				IDataSource dataSource = DAOFactory.getDataSourceDAO().loadDataSourceUseForDataprep();
				((PreparedDataSet) ds).setDataSource(dataSource);
				((PreparedDataSet) ds).setTableName(jsonConf.getString(DataSetConstants.TABLE_NAME));
				((PreparedDataSet) ds).setDataPreparationInstance(jsonConf.getString(DataSetConstants.DATA_PREPARATION_INSTANCE_ID));
				ds.setDsType(PREPARED_DS_TYPE);
			}

		} catch (SpagoBIRuntimeException ex) {
			throw ex;
		} catch (Exception e) {
			logger.error("Error while defining dataset configuration.", e);
			throw new SpagoBIRuntimeException("Error while defining dataset configuration.", e);
		}

		if (ds != null) {
			try {

				if (sbiDataSet.getCategory() != null) {
					ds.setCategoryCd(sbiDataSet.getCategory().getCode());
					ds.setCategoryId(sbiDataSet.getCategory().getId());
				}
				// ds.setConfiguration(sbiDataSet.getConfiguration());
				if (sbiDataSet.getId().getDsId() != null)
					ds.setId(sbiDataSet.getId().getDsId());
				ds.setName(sbiDataSet.getName());
				ds.setLabel(sbiDataSet.getLabel());
				ds.setDescription(sbiDataSet.getDescription());

				ds.setTransformerId((sbiDataSet.getTransformer() == null) ? null : sbiDataSet.getTransformer().getValueId());
				ds.setTransformerCd((sbiDataSet.getTransformer() == null) ? null : sbiDataSet.getTransformer().getValueCd());
				ds.setPivotColumnName(sbiDataSet.getPivotColumnName());
				ds.setPivotRowName(sbiDataSet.getPivotRowName());
				ds.setPivotColumnValue(sbiDataSet.getPivotColumnValue());
				ds.setNumRows(sbiDataSet.isNumRows());

				ds.setParameters(sbiDataSet.getParameters());
				ds.setDsMetadata(sbiDataSet.getDsMetadata());
				ds.setMetadata(sbiDataSet.getMetadata());
				ds.setOrganization(sbiDataSet.getId().getOrganization());

				if (ds.getPivotColumnName() != null && ds.getPivotColumnValue() != null && ds.getPivotRowName() != null) {
					ds.setDataStoreTransformer(
							new PivotDataSetTransformer(ds.getPivotColumnName(), ds.getPivotColumnValue(), ds.getPivotRowName(), ds.isNumRows()));
				}
				ds.setPersisted(sbiDataSet.isPersisted());
				ds.setPersistedHDFS(sbiDataSet.isPersistedHDFS());
				ds.setPersistTableName(sbiDataSet.getPersistTableName());
				ds.setOwner(sbiDataSet.getOwner());
				ds.setUserIn(sbiDataSet.getCommonInfo().getUserIn());
				ds.setDateIn(sbiDataSet.getCommonInfo().getTimeIn());

				// set tags
				ds.setTags(sbiDataSet.getTags());

				versionDS = new VersionedDataSet(ds, Integer.valueOf(sbiDataSet.getId().getVersionNum()), sbiDataSet.isActive());

				ds.setScopeId((sbiDataSet.getScope() == null) ? null : sbiDataSet.getScope().getValueId());
				ds.setScopeCd((sbiDataSet.getScope() == null) ? null : sbiDataSet.getScope().getValueCd());
				// if not yet assigned set data source for writing as default
				// one
				if (ds.getDataSourceForWriting() == null) {
					logger.debug("take write default data source as data source for writing");
					DataSourceDAOHibImpl dataSourceDao = new DataSourceDAOHibImpl();
					if (userProfile != null)
						dataSourceDao.setUserProfile(userProfile);
					IDataSource dataSourceWriteDef = dataSourceDao.loadDataSourceWriteDefault();
					if (dataSourceWriteDef != null) {
						logger.debug("data source write default is " + dataSourceWriteDef.getLabel());
						ds.setDataSourceForWriting(dataSourceWriteDef);
					} else {
						logger.warn("No data source for write default was found");
					}
				}

				if (userProfile != null) {
					Map<String, Object> profileAttrs = UserProfileUtils.getProfileAttributes(userProfile);
					ds.setUserProfileAttributes(profileAttrs);
				}

				if (sbiDataSet.isPersisted()) {
					// we read using the same datasource used for writing
					// TODO manage case when datasource for writing has changed
					// in the meanwhile
					IDataSource dataSourceForReading = ds.getDataSourceForWriting();
					if (dataSourceForReading == null) {
						throw new SpagoBIRuntimeException("Dataset is persisted but there is no datasource for writing!!");
					}
					ds.setDataSourceForReading(dataSourceForReading);
				}
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("Error while defining dataset configuration.", e);
			}

			managePersistedDataset(ds);
		}
		logger.debug("OUT");
		return versionDS;
	}

	public static IDataSet toDataSet(SpagoBiDataSet sbiDataSet, IEngUserProfile userProfile) {
		IDataSet ds = null;
		logger.debug("IN");
		String config = JSONUtils.escapeJsonString(sbiDataSet.getConfiguration());
		JSONObject jsonConf = ObjectUtils.toJSONObject(config);
		try {
			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_FILE)) {
				ds = new FileDataSet();
				FileDataSet fds = (FileDataSet) ds;

				String resourcePath = DAOConfig.getResourcePath();
				// String resourcePath = jsonConf.optString("resourcePath");
				// if (StringUtilities.isEmpty(resourcePath)) {
				// resourcePath = DAOConfig.getResourcePath();
				// jsonConf.put("resourcePath", resourcePath);
				// }
				fds.setResourcePath(resourcePath);

				fds.setConfiguration(jsonConf.toString());

				if (jsonConf.getString(DataSetConstants.FILE_TYPE) != null) {
					fds.setFileType(jsonConf.getString(DataSetConstants.FILE_TYPE));
				}
				fds.setFileName(jsonConf.getString(DataSetConstants.FILE_NAME));
				fds.setDsType(FILE_DS_TYPE);
			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_CKAN)) {
				ds = new CkanDataSet();
				CkanDataSet cds = (CkanDataSet) ds;

				String resourcePath = jsonConf.optString("ckanUrl");
				// String ckanResourceId = jsonConf.optString("ckanResourceId");
				cds.setResourcePath(resourcePath);
				cds.setCkanUrl(resourcePath);

				if (!jsonConf.isNull(DataSetConstants.FILE_TYPE)) {
					jsonConf.put(CkanDataSetConstants.CKAN_FILE_TYPE, jsonConf.getString(DataSetConstants.FILE_TYPE));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER)) {
					jsonConf.put(CkanDataSetConstants.CKAN_CSV_FILE_DELIMITER_CHARACTER, jsonConf.getString(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_QUOTE_CHARACTER)) {
					jsonConf.put(CkanDataSetConstants.CKAN_CSV_FILE_QUOTE_CHARACTER, jsonConf.getString(DataSetConstants.CSV_FILE_QUOTE_CHARACTER));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_ENCODING)) {
					jsonConf.put(CkanDataSetConstants.CKAN_CSV_FILE_ENCODING, jsonConf.getString(DataSetConstants.CSV_FILE_ENCODING));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_SKIP_ROWS)) {
					jsonConf.put(CkanDataSetConstants.CKAN_XSL_FILE_SKIP_ROWS, jsonConf.getString(DataSetConstants.XSL_FILE_SKIP_ROWS));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_LIMIT_ROWS)) {
					jsonConf.put(CkanDataSetConstants.CKAN_XSL_FILE_LIMIT_ROWS, jsonConf.getString(DataSetConstants.XSL_FILE_LIMIT_ROWS));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_SHEET_NUMBER)) {
					jsonConf.put(CkanDataSetConstants.CKAN_XSL_FILE_SHEET_NUMBER, jsonConf.getString(DataSetConstants.XSL_FILE_SHEET_NUMBER));
				}
				if (!jsonConf.isNull(CkanDataSetConstants.CKAN_ID)) {
					jsonConf.put(CkanDataSetConstants.CKAN_ID, jsonConf.getString(CkanDataSetConstants.CKAN_ID));
				}

				cds.setConfiguration(jsonConf.toString());

				if (jsonConf.getString(DataSetConstants.FILE_TYPE) != null) {
					cds.setFileType(jsonConf.getString(DataSetConstants.FILE_TYPE));
				}
				cds.setFileName(jsonConf.getString(DataSetConstants.FILE_NAME));
				cds.setDsType(CKAN_DS_TYPE);

			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_QUERY)) {

				DataSourceDAOHibImpl dataSourceDao = new DataSourceDAOHibImpl();
				if (userProfile != null)
					dataSourceDao.setUserProfile(userProfile);

				IDataSource dataSource = dataSourceDao.loadDataSourceByLabel(jsonConf.getString(DataSetConstants.DATA_SOURCE));
				ds = getDataset(dataSource);

				ds.setConfiguration(sbiDataSet.getConfiguration());
				ds.setDsType(JDBC_DS_TYPE);
				((ConfigurableDataSet) ds).setQuery(jsonConf.getString(DataSetConstants.QUERY));
				((ConfigurableDataSet) ds).setQueryScript(jsonConf.getString(DataSetConstants.QUERY_SCRIPT));
				((ConfigurableDataSet) ds).setQueryScriptLanguage(jsonConf.getString(DataSetConstants.QUERY_SCRIPT_LANGUAGE));

				if (dataSource != null) {
					((ConfigurableDataSet) ds).setDataSource(dataSource);
					// if data source associated is not read only set is as
					// write one.
					if (!dataSource.checkIsReadOnly()) {
						ds.setDataSourceForWriting(dataSource);
					}
				} else {
					logger.error("Could not retrieve datasource with label " + jsonConf.getString(DataSetConstants.DATA_SOURCE) + " for dataset "
							+ sbiDataSet.getLabel());
				}

			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_SCRIPT)) {
				ds = new ScriptDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((ScriptDataSet) ds).setScript(jsonConf.getString(DataSetConstants.SCRIPT));
				((ScriptDataSet) ds).setScriptLanguage(jsonConf.getString(DataSetConstants.SCRIPT_LANGUAGE));
				ds.setDsType(SCRIPT_DS_TYPE);
			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_JCLASS)) {
				ds = new JavaClassDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((JavaClassDataSet) ds).setClassName(jsonConf.getString(DataSetConstants.JCLASS_NAME));
				ds.setDsType(JCLASS_DS_TYPE);
			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_CUSTOM)) {
				ds = new CustomDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((CustomDataSet) ds).setCustomData(jsonConf.getString(DataSetConstants.CUSTOM_DATA));
				((CustomDataSet) ds).setJavaClassName(jsonConf.getString(DataSetConstants.JCLASS_NAME));
				ds.setDsType(CUSTOM_DS_TYPE);
			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_QBE)) {
				ds = new QbeDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((QbeDataSet) ds).setJsonQuery(jsonConf.getString(DataSetConstants.QBE_JSON_QUERY));
				((QbeDataSet) ds).setDatamarts(jsonConf.getString(DataSetConstants.QBE_DATAMARTS));

				// START -> This code should work instead of CheckQbeDataSets
				// around the projects
				SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
				Map parameters = ds.getParamsMap();
				if (parameters == null) {
					parameters = new HashMap();
					ds.setParamsMap(parameters);
				}
				ds.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
				logger.debug("Datamart retriever correctly added to Qbe dataset");
				// END

				DataSourceDAOHibImpl dataSourceDao = new DataSourceDAOHibImpl();
				if (userProfile != null)
					dataSourceDao.setUserProfile(userProfile);
				IDataSource dataSource = dataSourceDao.loadDataSourceByLabel(jsonConf.getString(DataSetConstants.QBE_DATA_SOURCE));
				if (dataSource != null) {
					((QbeDataSet) ds).setDataSource(dataSource);
					if (!dataSource.checkIsReadOnly()) {
						ds.setDataSourceForWriting(dataSource);
					}
				}
				ds.setDsType(QBE_DS_TYPE);

			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_FLAT)) {
				ds = new FlatDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				DataSourceDAOHibImpl dataSourceDao = new DataSourceDAOHibImpl();
				if (userProfile != null)
					dataSourceDao.setUserProfile(userProfile);
				IDataSource dataSource = dataSourceDao.loadDataSourceByLabel(jsonConf.getString(DataSetConstants.DATA_SOURCE));
				((FlatDataSet) ds).setDataSource(dataSource);
				((FlatDataSet) ds).setTableName(jsonConf.getString(DataSetConstants.FLAT_TABLE_NAME));
				ds.setDsType(FLAT_DS_TYPE);
			}

		} catch (Exception e) {
			logger.error("Error while defining dataset configuration.  Error: " + e.getMessage());
		}

		if (ds != null) {
			try {

				if (sbiDataSet.getCategoryId() != null) {
					ds.setCategoryId(sbiDataSet.getCategoryId());
					Domain domain = DAOFactory.getDomainDAO().loadDomainById(sbiDataSet.getCategoryId());
					if (domain != null) {
						ds.setCategoryCd(domain.getValueCd());
					}
				}
				// ds.setConfiguration(sbiDataSet.getConfiguration());
				ds.setName(sbiDataSet.getName());
				ds.setLabel(sbiDataSet.getLabel());
				ds.setDescription(sbiDataSet.getDescription());

				ds.setPivotColumnName(sbiDataSet.getPivotColumnName());
				ds.setPivotRowName(sbiDataSet.getPivotRowName());
				ds.setPivotColumnValue(sbiDataSet.getPivotColumnValue());
				ds.setNumRows(sbiDataSet.isNumRows());

				ds.setParameters(sbiDataSet.getParameters());
				ds.setDsMetadata(sbiDataSet.getDsMetadata());
				ds.setOrganization(sbiDataSet.getOrganization());

				if (ds.getPivotColumnName() != null && ds.getPivotColumnValue() != null && ds.getPivotRowName() != null) {
					ds.setDataStoreTransformer(
							new PivotDataSetTransformer(ds.getPivotColumnName(), ds.getPivotColumnValue(), ds.getPivotRowName(), ds.isNumRows()));
				}
				ds.setPersisted(sbiDataSet.isPersisted());
				ds.setPersistedHDFS(sbiDataSet.isPersistedHDFS());
				ds.setPersistTableName(sbiDataSet.getPersistTableName());
				ds.setOwner(sbiDataSet.getOwner());
				ds.setUserIn(sbiDataSet.getOwner());
				ds.setDateIn(new Date());

				ds.setScopeId(sbiDataSet.getScopeId());
				ds.setScopeCd(sbiDataSet.getScopeCd());

				// if not yet assigned set data source for writing as default
				// one
				if (ds.getDataSourceForWriting() == null) {
					logger.debug("take write default data source as data source for writing");
					DataSourceDAOHibImpl dataSourceDao = new DataSourceDAOHibImpl();
					if (userProfile != null)
						dataSourceDao.setUserProfile(userProfile);
					IDataSource dataSourceWriteDef = dataSourceDao.loadDataSourceWriteDefault();
					if (dataSourceWriteDef != null) {
						logger.debug("data source write default is " + dataSourceWriteDef.getLabel());
						ds.setDataSourceForWriting(dataSourceWriteDef);
					} else {
						logger.warn("No data source for write default was found");
					}
				}

				if (userProfile != null) {
					Map<String, Object> profileAttrs = UserProfileUtils.getProfileAttributes(userProfile);
					ds.setUserProfileAttributes(profileAttrs);
				}

				if (sbiDataSet.isPersisted()) {
					// we read using the same datasource used for writing
					// TODO manage case when datasource for writing has changed
					// in the meanwhile
					IDataSource dataSourceForReading = ds.getDataSourceForWriting();
					if (dataSourceForReading == null) {
						throw new SpagoBIRuntimeException("Dataset is persisted but there is no datasource for writing!!");
					}
					ds.setDataSourceForReading(dataSourceForReading);
				}
			} catch (Exception e) {
				logger.error("Error in copying dataset definition ", e);
			}

		}
		logger.debug("OUT");
		return ds;
	}

	private static IDataSet getDataset(IDataSource dataSource) {
		IDataSet ds = null;
		if (dataSource != null) {
			String dialectToLowerCase = dataSource.getHibDialectClass().toLowerCase();
			if (dialectToLowerCase.contains("mongo")) {
				ds = new MongoDataSet();
			} else if (SqlUtils.isHiveLikeDialect(dialectToLowerCase)) {
				ds = new JDBCHiveDataSet();
			} else if (dialectToLowerCase.contains("orient")) {
				ds = new JDBCOrientDbDataSet();
			} else if (dialectToLowerCase.contains("vertica")) {
				ds = new JDBCVerticaDataSet();
			} else if (dialectToLowerCase.contains("RedShift")) {
				ds = new JDBCRedShiftDataSet();
			} else if (dialectToLowerCase.contains("BigQuery")) {
				ds = new JDBCBigQueryDataSet();
			} else if (dialectToLowerCase.contains("Synapse")) {
				ds = new JDBCSynapseDataSet();
			} else if (dialectToLowerCase.contains("Spanner")) {
				ds = new JDBCSpannerDataSet();
			}
		}
		return (ds != null) ? ds : new JDBCDataSet();
	}

	public static IDataSet toDataSetForImport(SbiDataSet sbiDataSet, IEngUserProfile userProfile) {
		IDataSet ds = null;
		VersionedDataSet versionDS = null;
		logger.debug("IN");
		String config = JSONUtils.escapeJsonString(sbiDataSet.getConfiguration());
		JSONObject jsonConf = ObjectUtils.toJSONObject(config);
		try {
			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_FILE)) {
				ds = new FileDataSet();
				FileDataSet fds = (FileDataSet) ds;

				String resourcePath = DAOConfig.getResourcePath();
				// String resourcePath = jsonConf.optString("resourcePath");
				// if (StringUtilities.isEmpty(resourcePath)) {
				// resourcePath = DAOConfig.getResourcePath();
				// jsonConf.put("resourcePath", resourcePath);
				// }
				fds.setResourcePath(resourcePath);

				fds.setConfiguration(jsonConf.toString());

				if (jsonConf.getString(DataSetConstants.FILE_TYPE) != null) {
					fds.setFileType(jsonConf.getString(DataSetConstants.FILE_TYPE));
				}
				fds.setFileName(jsonConf.getString(DataSetConstants.FILE_NAME));
				fds.setDsType(DataSetConstants.DS_FILE);
			}

			if (DataSetConstants.DS_REST_TYPE.equalsIgnoreCase(sbiDataSet.getType())) {
				ds = manageRESTDataSet(jsonConf);
			}

			if (DataSetConstants.DS_PYTHON_TYPE.equalsIgnoreCase(sbiDataSet.getType())) {
				ds = managePythonDataSet(jsonConf);
			}

			if (DataSetConstants.DS_SPARQL.equalsIgnoreCase(sbiDataSet.getType())) {
				ds = manageSPARQLDataSet(jsonConf);
			}

			if (DataSetConstants.DS_SOLR_TYPE.equalsIgnoreCase(sbiDataSet.getType())) {
				ds = manageSolrDataSet(jsonConf, sbiDataSet.getParametersList());
			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_CKAN)) {
				ds = new CkanDataSet();
				CkanDataSet cds = (CkanDataSet) ds;

				String resourcePath = jsonConf.optString("ckanUrl");
				// String ckanResourceId = jsonConf.optString("ckanResourceId");
				cds.setResourcePath(resourcePath);
				cds.setCkanUrl(resourcePath);

				if (!jsonConf.isNull(DataSetConstants.FILE_TYPE)) {
					jsonConf.put(CkanDataSetConstants.CKAN_FILE_TYPE, jsonConf.getString(DataSetConstants.FILE_TYPE));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER)) {
					jsonConf.put(CkanDataSetConstants.CKAN_CSV_FILE_DELIMITER_CHARACTER, jsonConf.getString(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_QUOTE_CHARACTER)) {
					jsonConf.put(CkanDataSetConstants.CKAN_CSV_FILE_QUOTE_CHARACTER, jsonConf.getString(DataSetConstants.CSV_FILE_QUOTE_CHARACTER));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_ENCODING)) {
					jsonConf.put(CkanDataSetConstants.CKAN_CSV_FILE_ENCODING, jsonConf.getString(DataSetConstants.CSV_FILE_ENCODING));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_SKIP_ROWS)) {
					jsonConf.put(CkanDataSetConstants.CKAN_XSL_FILE_SKIP_ROWS, jsonConf.getString(DataSetConstants.XSL_FILE_SKIP_ROWS));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_LIMIT_ROWS)) {
					jsonConf.put(CkanDataSetConstants.CKAN_XSL_FILE_LIMIT_ROWS, jsonConf.getString(DataSetConstants.XSL_FILE_LIMIT_ROWS));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_SHEET_NUMBER)) {
					jsonConf.put(CkanDataSetConstants.CKAN_XSL_FILE_SHEET_NUMBER, jsonConf.getString(DataSetConstants.XSL_FILE_SHEET_NUMBER));
				}
				if (!jsonConf.isNull(CkanDataSetConstants.CKAN_ID)) {
					jsonConf.put(CkanDataSetConstants.CKAN_ID, jsonConf.getString(CkanDataSetConstants.CKAN_ID));
				}

				cds.setConfiguration(jsonConf.toString());

				if (jsonConf.getString(DataSetConstants.FILE_TYPE) != null) {
					cds.setFileType(jsonConf.getString(DataSetConstants.FILE_TYPE));
				}
				cds.setFileName(jsonConf.getString(DataSetConstants.FILE_NAME));
				cds.setDsType(DataSetConstants.DS_CKAN);

			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_QUERY)) {

				DataSourceDAOHibImpl dataSourceDao = new DataSourceDAOHibImpl();
				if (userProfile != null)
					dataSourceDao.setUserProfile(userProfile);
				IDataSource dataSource = dataSourceDao.loadDataSourceByLabel(jsonConf.getString(DataSetConstants.DATA_SOURCE));

				ds = getDataset(dataSource);

				ds.setConfiguration(sbiDataSet.getConfiguration());
				ds.setDsType(DataSetConstants.DS_QUERY);
				((ConfigurableDataSet) ds).setQuery(jsonConf.getString(DataSetConstants.QUERY));
				((ConfigurableDataSet) ds).setQueryScript(jsonConf.getString(DataSetConstants.QUERY_SCRIPT));
				((ConfigurableDataSet) ds).setQueryScriptLanguage(jsonConf.getString(DataSetConstants.QUERY_SCRIPT_LANGUAGE));

				if (dataSource != null) {
					((ConfigurableDataSet) ds).setDataSource(dataSource);
					// if data source associated is not read only set is as
					// write one.
					if (!dataSource.checkIsReadOnly()) {
						ds.setDataSourceForWriting(dataSource);
					}
				} else {
					logger.error("Could not retrieve datasource with label " + jsonConf.getString(DataSetConstants.DATA_SOURCE) + " for dataset "
							+ sbiDataSet.getLabel());
				}

			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_SCRIPT)) {
				ds = new ScriptDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((ScriptDataSet) ds).setScript(jsonConf.getString(DataSetConstants.SCRIPT));
				((ScriptDataSet) ds).setScriptLanguage(jsonConf.getString(DataSetConstants.SCRIPT_LANGUAGE));
				ds.setDsType(DataSetConstants.DS_SCRIPT);
			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_JCLASS)) {
				ds = new JavaClassDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((JavaClassDataSet) ds).setClassName(jsonConf.getString(DataSetConstants.JCLASS_NAME));
				ds.setDsType(DataSetConstants.DS_JCLASS);
			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_CUSTOM)) {
				ds = new CustomDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((CustomDataSet) ds).setCustomData(jsonConf.getString(DataSetConstants.CUSTOM_DATA));
				((CustomDataSet) ds).setJavaClassName(jsonConf.getString(DataSetConstants.JCLASS_NAME));
				ds.setDsType(DataSetConstants.DS_CUSTOM);
			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_FEDERATED)) {

				SbiFederationDefinition sbiFederation = sbiDataSet.getFederation();

				Set<SbiDataSet> sources = sbiFederation.getSourceDatasets();
				Set<IDataSet> sourcesDatasets = new HashSet<>();
				for (SbiDataSet s : sources) {
					IDataSet IDs = toDataSetForImport(s, userProfile);
					sourcesDatasets.add(IDs);

				}

				UserProfile profile = (UserProfile) userProfile;
				ds = new FederatedDataSet(SbiFederationUtils.toDatasetFederationWithDataset(sbiFederation, sourcesDatasets), (String) profile.getUserId());
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((FederatedDataSet) ds).setJsonQuery(jsonConf.getString(DataSetConstants.QBE_JSON_QUERY));

				SbiFederationDefinition sbiFedDef = sbiDataSet.getFederation();
				// FederationDefinition fd =
				// DAOFactory.getFedetatedDatasetDAO().loadFederationDefinition(sbiFedDef.getFederation_id());
				FederationDefinition fd = new FederationDefinition();
				fd.setDegenerated(sbiFedDef.isDegenerated());
				fd.setDescription(sbiFedDef.getDescription());
				fd.setLabel(sbiFedDef.getLabel());
				fd.setName(sbiFedDef.getName());
				fd.setRelationships(sbiFedDef.getRelationships());
				fd.setSourceDatasets(sourcesDatasets);

				((FederatedDataSet) ds).setDatasetFederation(fd);

				// START -> This code should work instead of CheckQbeDataSets
				// around the projects

				Map parameters = ds.getParamsMap();
				if (parameters == null) {
					parameters = new HashMap();
					ds.setParamsMap(parameters);
				}

				// END

				DataSourceDAOHibImpl dataSourceDao = new DataSourceDAOHibImpl();
				if (userProfile != null)
					dataSourceDao.setUserProfile(userProfile);
				IDataSource dataSource = dataSourceDao.loadDataSourceByLabel(jsonConf.getString(DataSetConstants.QBE_DATA_SOURCE));
				if (dataSource != null) {
					((QbeDataSet) ds).setDataSource(dataSource);
					if (!dataSource.checkIsReadOnly()) {
						ds.setDataSourceForWriting(dataSource);
						ds.setDataSourceForReading(dataSource);
					}
				}
				ds.setDsType(DataSetConstants.DS_FEDERATED);

			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_QBE)) {
				ds = new QbeDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((QbeDataSet) ds).setJsonQuery(jsonConf.getString(DataSetConstants.QBE_JSON_QUERY));
				((QbeDataSet) ds).setDatamarts(jsonConf.getString(DataSetConstants.QBE_DATAMARTS));

				// START -> This code should work instead of CheckQbeDataSets
				// around the projects
				SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
				Map parameters = ds.getParamsMap();
				if (parameters == null) {
					parameters = new HashMap();
					ds.setParamsMap(parameters);
				}
				ds.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
				logger.debug("Datamart retriever correctly added to Qbe dataset");
				// END

				DataSourceDAOHibImpl dataSourceDao = new DataSourceDAOHibImpl();
				if (userProfile != null)
					dataSourceDao.setUserProfile(userProfile);
				IDataSource dataSource = dataSourceDao.loadDataSourceByLabel(jsonConf.getString(DataSetConstants.QBE_DATA_SOURCE));
				if (dataSource != null) {
					((QbeDataSet) ds).setDataSource(dataSource);
					if (!dataSource.checkIsReadOnly()) {
						ds.setDataSourceForWriting(dataSource);
					}
				}
				ds.setDsType(DataSetConstants.DS_QBE);

			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_FLAT)) {
				ds = new FlatDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				DataSourceDAOHibImpl dataSourceDao = new DataSourceDAOHibImpl();
				if (userProfile != null)
					dataSourceDao.setUserProfile(userProfile);
				IDataSource dataSource = dataSourceDao.loadDataSourceByLabel(jsonConf.getString(DataSetConstants.DATA_SOURCE));
				((FlatDataSet) ds).setDataSource(dataSource);
				((FlatDataSet) ds).setTableName(jsonConf.getString(DataSetConstants.FLAT_TABLE_NAME));
				ds.setDsType(DataSetConstants.DS_FLAT);
			}

		} catch (Exception e) {
			logger.error("Error while defining dataset configuration.  Error: " + e.getMessage(), e);
		}

		if (ds != null) {
			try {

				if (sbiDataSet.getCategory() != null) {
					ds.setCategoryCd(sbiDataSet.getCategory().getCode());
					ds.setCategoryId(sbiDataSet.getCategory().getId());
				}
				// ds.setConfiguration(sbiDataSet.getConfiguration());
				if (sbiDataSet.getId().getDsId() != null)
					ds.setId(sbiDataSet.getId().getDsId());
				ds.setName(sbiDataSet.getName());
				ds.setLabel(sbiDataSet.getLabel());
				ds.setDescription(sbiDataSet.getDescription());

				ds.setTransformerId((sbiDataSet.getTransformer() == null) ? null : sbiDataSet.getTransformer().getValueId());
				ds.setTransformerCd((sbiDataSet.getTransformer() == null) ? null : sbiDataSet.getTransformer().getValueCd());
				ds.setPivotColumnName(sbiDataSet.getPivotColumnName());
				ds.setPivotRowName(sbiDataSet.getPivotRowName());
				ds.setPivotColumnValue(sbiDataSet.getPivotColumnValue());
				ds.setNumRows(sbiDataSet.isNumRows());

				ds.setParameters(sbiDataSet.getParameters());
				ds.setDsMetadata(sbiDataSet.getDsMetadata());
				ds.setOrganization(sbiDataSet.getId().getOrganization());

				if (ds.getPivotColumnName() != null && ds.getPivotColumnValue() != null && ds.getPivotRowName() != null) {
					ds.setDataStoreTransformer(
							new PivotDataSetTransformer(ds.getPivotColumnName(), ds.getPivotColumnValue(), ds.getPivotRowName(), ds.isNumRows()));
				}
				ds.setPersisted(sbiDataSet.isPersisted());
				ds.setPersistedHDFS(sbiDataSet.isPersistedHDFS());
				ds.setPersistTableName(sbiDataSet.getPersistTableName());
				ds.setOwner(sbiDataSet.getOwner());
				ds.setUserIn(sbiDataSet.getCommonInfo().getUserIn());
				ds.setDateIn(sbiDataSet.getCommonInfo().getTimeIn());
				versionDS = new VersionedDataSet(ds, Integer.valueOf(sbiDataSet.getId().getVersionNum()), sbiDataSet.isActive());

				ds.setScopeId((sbiDataSet.getScope() == null) ? null : sbiDataSet.getScope().getValueId());
				ds.setScopeCd((sbiDataSet.getScope() == null) ? null : sbiDataSet.getScope().getValueCd());
				// if not yet assigned set data source for writing as default
				// one
				if (ds.getDataSourceForWriting() == null) {
					logger.debug("take write default data source as data source for writing");
					DataSourceDAOHibImpl dataSourceDao = new DataSourceDAOHibImpl();
					if (userProfile != null)
						dataSourceDao.setUserProfile(userProfile);
					IDataSource dataSourceWriteDef = dataSourceDao.loadDataSourceWriteDefault();
					if (dataSourceWriteDef != null) {
						logger.debug("data source write default is " + dataSourceWriteDef.getLabel());
						ds.setDataSourceForWriting(dataSourceWriteDef);
					} else {
						logger.warn("No data source for write default was found");
					}
				}

				if (sbiDataSet.isPersisted()) {
					// we read using the same datasource used for writing
					// TODO manage case when datasource for writing has changed
					// in the meanwhile
					IDataSource dataSourceForReading = ds.getDataSourceForWriting();
					if (dataSourceForReading == null) {
						throw new SpagoBIRuntimeException("Dataset is persisted but there is no datasource for writing!!");
					}
					ds.setDataSourceForReading(dataSourceForReading);
				}
			} catch (Exception e) {
				logger.error("Error in copying dataset definition ", e);
			}

		}
		logger.debug("OUT");
		return versionDS;
	}

	private static RESTDataSet manageRESTDataSet(JSONObject jsonConf) {
		RESTDataSet res = new RESTDataSet(jsonConf);
		res.setDsType(DataSetConstants.DS_REST_NAME);
		return res;
	}

	private static PythonDataSet managePythonDataSet(JSONObject jsonConf) {
		PythonDataSet res = new PythonDataSet(jsonConf);
		res.setDsType(DataSetConstants.DS_PYTHON_NAME);
		return res;
	}

	private static SPARQLDataSet manageSPARQLDataSet(JSONObject jsonConf) {
		SPARQLDataSet res = new SPARQLDataSet(jsonConf);
		res.setDsType(DataSetConstants.SPARQL);
		return res;
	}

	private static RESTDataSet manageSolrDataSet(JSONObject jsonConf, List<DataSetParameterItem> parameters)
			throws JSONException, HttpException, HMACSecurityException, IOException {
		SolrDataSet res = null;

		HashMap<String, String> parametersMap = new HashMap<String, String>();
		if (parameters != null) {
			for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
				DataSetParameterItem dataSetParameterItem = (DataSetParameterItem) iterator.next();
				parametersMap.put(dataSetParameterItem.getDefaultValue(), dataSetParameterItem.getName());
			}
		}

		String solrType = jsonConf.getString(SolrDataSetConstants.SOLR_TYPE);
		Assert.assertNotNull(solrType, "Solr type cannot be null");
		res = solrType.equalsIgnoreCase(SolrDataSetConstants.TYPE.DOCUMENTS.name()) ? new SolrDataSet(jsonConf, parametersMap)
				: new FacetSolrDataSet(jsonConf, parametersMap);
		res.setDsType(DataSetConstants.DS_SOLR_NAME);

		if (!jsonConf.has(SolrDataSetConstants.SOLR_FIELDS)) {
			JSONArray solrFields = res.getSolrFields();
			jsonConf.put(SolrDataSetConstants.SOLR_FIELDS, solrFields);
			res.setConfiguration(jsonConf.toString());
		}
		res.setSolrQueryParameters(res.getSolrQuery(), res.getParamsMap());
		return res;
	}

	private static void managePersistedDataset(IDataSet ds) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		ISchedulerDAO schedulerDAO;

		try {
			schedulerDAO = DAOFactory.getSchedulerDAO();
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to load scheduler DAO", t);
		}

		if (ds.isPersisted()) {

			List<Trigger> triggers = schedulerDAO.loadTriggers("PersistDatasetExecutions", ds.getLabel());

			if (triggers.isEmpty()) {
				// itemJSON.put("isScheduled", false);
				ds.setScheduled(false);
			} else {

				// Dataset scheduling is mono-trigger
				Trigger trigger = triggers.get(0);

				if (!trigger.isRunImmediately()) {

					// itemJSON.put("isScheduled", true);
					ds.setScheduled(true);

					if (trigger.getStartTime() != null) {
						ds.setStartDateField(sdf.format(trigger.getStartTime()));
					} else {
						// itemJSON.put("startDate", "");
						ds.setStartDateField("");
					}

					if (trigger.getEndTime() != null) {
						// itemJSON.put("endDate", sdf.format(trigger.getEndTime()));
						ds.setEndDateField(sdf.format(trigger.getEndTime()));
					} else {
						// itemJSON.put("endDate", "");
						ds.setEndDateField("");
					}

					// itemJSON.put("schedulingCronLine", trigger.getChronExpression().getExpression());
					ds.setSchedulingCronLine(trigger.getChronExpression().getExpression());
				}
			}
		}
	}

}
