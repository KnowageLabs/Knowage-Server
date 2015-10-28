/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.dao;

import it.eng.qbe.dataset.FederatedDataSet;
import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.federateddataset.dao.ISbiFederationDefinitionDAO;
import it.eng.spagobi.federateddataset.dao.SbiFederationUtils;
import it.eng.spagobi.federateddataset.metadata.SbiFederationDefinition;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.bo.CkanDataSet;
import it.eng.spagobi.tools.dataset.bo.ConfigurableDataSet;
import it.eng.spagobi.tools.dataset.bo.CustomDataSet;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.FlatDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCHBaseDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCHiveDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCOrientDbDataSet;
import it.eng.spagobi.tools.dataset.bo.JavaClassDataSet;
import it.eng.spagobi.tools.dataset.bo.MongoDataSet;
import it.eng.spagobi.tools.dataset.bo.ScriptDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.bo.WebServiceDataSet;
import it.eng.spagobi.tools.dataset.common.transformer.PivotDataSetTransformer;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.utils.datamart.SpagoBICoreDatamartRetriever;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.DataSourceDAOHibImpl;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;
import it.eng.spagobi.utilities.sql.SqlUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
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

		if (dataSet instanceof JDBCDataSet) {
			toReturn.setDsType(JDBC_DS_TYPE);
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

		if (dataSet instanceof WebServiceDataSet) {
			toReturn.setDsType(WS_DS_TYPE);
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
		toReturn.setPersistTableName(dataSet.getPersistTableName());
		toReturn.setScopeCd(dataSet.getScopeCd());
		toReturn.setScopeId(dataSet.getScopeId());

		return toReturn;
	}

	public static IDataSet toDataSet(SbiDataSet sbiDataSet) {
		return toDataSet(sbiDataSet, null);
	}

	// public static Set<IDataSet> toDataSet(Set<SbiDataSet> sbiDataSets, IEngUserProfile userProfile) {
	// Set<IDataSet> ds = new java.util.HashSet<IDataSet>();
	// for (SbiDataSet dataset : sbiDataSets) {
	// ds.add(toDataSet(dataset));
	// }
	// return ds;
	// }

	public static Set<IDataSet> toDataSet(Set<SbiDataSet> sbiDataSet, IEngUserProfile userProfile) {

		Set<IDataSet> toReturn = new HashSet<IDataSet>();

		for (Iterator iterator = sbiDataSet.iterator(); iterator.hasNext();) {
			SbiDataSet sbiDataSet3 = (SbiDataSet) iterator.next();
			toReturn.add(toDataSet(sbiDataSet3, userProfile));
		}

		return toReturn;
	}

	
	public static Set<IDataSet> toDataSet(List<SbiDataSet> sbiDataSet, IEngUserProfile userProfile) {

		Set<IDataSet> toReturn = new HashSet<IDataSet>();

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
			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_FILE)) {
				ds = new FileDataSet();
				FileDataSet fds = (FileDataSet) ds;

				String resourcePath = jsonConf.optString("resourcePath");
				if (StringUtilities.isEmpty(resourcePath)) {
					resourcePath = DAOConfig.getResourcePath();
					jsonConf.put("resourcePath", resourcePath);
				}
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
					jsonConf.put(DataSetConstants.CKAN_FILE_TYPE, jsonConf.getString(DataSetConstants.FILE_TYPE));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER)) {
					jsonConf.put(DataSetConstants.CKAN_CSV_FILE_DELIMITER_CHARACTER, jsonConf.getString(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_QUOTE_CHARACTER)) {
					jsonConf.put(DataSetConstants.CKAN_CSV_FILE_QUOTE_CHARACTER, jsonConf.getString(DataSetConstants.CSV_FILE_QUOTE_CHARACTER));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_ENCODING)) {
					jsonConf.put(DataSetConstants.CKAN_CSV_FILE_ENCODING, jsonConf.getString(DataSetConstants.CSV_FILE_ENCODING));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_SKIP_ROWS)) {
					jsonConf.put(DataSetConstants.CKAN_XSL_FILE_SKIP_ROWS, jsonConf.getString(DataSetConstants.XSL_FILE_SKIP_ROWS));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_LIMIT_ROWS)) {
					jsonConf.put(DataSetConstants.CKAN_XSL_FILE_LIMIT_ROWS, jsonConf.getString(DataSetConstants.XSL_FILE_LIMIT_ROWS));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_SHEET_NUMBER)) {
					jsonConf.put(DataSetConstants.CKAN_XSL_FILE_SHEET_NUMBER, jsonConf.getString(DataSetConstants.XSL_FILE_SHEET_NUMBER));
				}
				if (!jsonConf.isNull(DataSetConstants.CKAN_ID)) {
					jsonConf.put(DataSetConstants.CKAN_ID, jsonConf.getString(DataSetConstants.CKAN_ID));
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

				if (dataSource != null && dataSource.getHibDialectClass().toLowerCase().contains("mongo")) {
					ds = new MongoDataSet();
				} else if (dataSource != null && dataSource.getHibDialectClass().toLowerCase().contains("hbase")) {
					ds = new JDBCHBaseDataSet();
				} else if (dataSource != null && SqlUtils.isHiveLikeDialect(dataSource.getHibDialectClass().toLowerCase())) {
					ds = new JDBCHiveDataSet();
				} else if (dataSource != null && dataSource.getHibDialectClass().toLowerCase().contains("orient")) {
					ds = new JDBCOrientDbDataSet();
				} else {
					ds = new JDBCDataSet();
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
				} else {
					logger.error("Could not retrieve datasource with label " + jsonConf.getString(DataSetConstants.DATA_SOURCE) + " for dataset "
							+ sbiDataSet.getLabel());
				}

			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_WS)) {
				ds = new WebServiceDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((WebServiceDataSet) ds).setAddress(jsonConf.getString(DataSetConstants.WS_ADDRESS));
				((WebServiceDataSet) ds).setOperation(jsonConf.getString(DataSetConstants.WS_OPERATION));
				ds.setDsType(WS_DS_TYPE);
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

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_FEDERATED)) {

				SbiFederationDefinition sbiFederation = sbiDataSet.getFederation();
				
				ISbiFederationDefinitionDAO dao = DAOFactory.getFedetatedDatasetDAO();
				Set<IDataSet> sourcesDatasets =  dao.loadAllFederatedDataSets(sbiFederation.getFederation_id());

				ds = new FederatedDataSet(SbiFederationUtils.toDatasetFederationWithDataset(sbiFederation, userProfile,sourcesDatasets));
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((FederatedDataSet) ds).setJsonQuery(jsonConf.getString(DataSetConstants.QBE_JSON_QUERY));

				// START -> This code should work instead of CheckQbeDataSets around the projects

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
					}
				}
				ds.setDsType(FEDERATED_DS_TYPE);

			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_QBE)) {
				ds = new QbeDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((QbeDataSet) ds).setJsonQuery(jsonConf.getString(DataSetConstants.QBE_JSON_QUERY));
				((QbeDataSet) ds).setDatamarts(jsonConf.getString(DataSetConstants.QBE_DATAMARTS));

				// START -> This code should work instead of CheckQbeDataSets around the projects
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

				if (sbiDataSet.getCategory() != null) {
					ds.setCategoryCd(sbiDataSet.getCategory().getValueCd());
					ds.setCategoryId(sbiDataSet.getCategory().getValueId());
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
					ds.setDataStoreTransformer(new PivotDataSetTransformer(ds.getPivotColumnName(), ds.getPivotColumnValue(), ds.getPivotRowName(), ds
							.isNumRows()));
				}
				ds.setPersisted(sbiDataSet.isPersisted());
				ds.setPersistTableName(sbiDataSet.getPersistTableName());
				ds.setOwner(sbiDataSet.getOwner());
				ds.setPublic(sbiDataSet.isPublicDS());
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

	public static IDataSet toDataSet(SpagoBiDataSet sbiDataSet, IEngUserProfile userProfile) {
		IDataSet ds = null;
		logger.debug("IN");
		String config = JSONUtils.escapeJsonString(sbiDataSet.getConfiguration());
		JSONObject jsonConf = ObjectUtils.toJSONObject(config);
		try {
			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_FILE)) {
				ds = new FileDataSet();
				FileDataSet fds = (FileDataSet) ds;

				String resourcePath = jsonConf.optString("resourcePath");
				if (StringUtilities.isEmpty(resourcePath)) {
					resourcePath = DAOConfig.getResourcePath();
					jsonConf.put("resourcePath", resourcePath);
				}
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
					jsonConf.put(DataSetConstants.CKAN_FILE_TYPE, jsonConf.getString(DataSetConstants.FILE_TYPE));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER)) {
					jsonConf.put(DataSetConstants.CKAN_CSV_FILE_DELIMITER_CHARACTER, jsonConf.getString(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_QUOTE_CHARACTER)) {
					jsonConf.put(DataSetConstants.CKAN_CSV_FILE_QUOTE_CHARACTER, jsonConf.getString(DataSetConstants.CSV_FILE_QUOTE_CHARACTER));
				}
				if (!jsonConf.isNull(DataSetConstants.CSV_FILE_ENCODING)) {
					jsonConf.put(DataSetConstants.CKAN_CSV_FILE_ENCODING, jsonConf.getString(DataSetConstants.CSV_FILE_ENCODING));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_SKIP_ROWS)) {
					jsonConf.put(DataSetConstants.CKAN_XSL_FILE_SKIP_ROWS, jsonConf.getString(DataSetConstants.XSL_FILE_SKIP_ROWS));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_LIMIT_ROWS)) {
					jsonConf.put(DataSetConstants.CKAN_XSL_FILE_LIMIT_ROWS, jsonConf.getString(DataSetConstants.XSL_FILE_LIMIT_ROWS));
				}
				if (!jsonConf.isNull(DataSetConstants.XSL_FILE_SHEET_NUMBER)) {
					jsonConf.put(DataSetConstants.CKAN_XSL_FILE_SHEET_NUMBER, jsonConf.getString(DataSetConstants.XSL_FILE_SHEET_NUMBER));
				}
				if (!jsonConf.isNull(DataSetConstants.CKAN_ID)) {
					jsonConf.put(DataSetConstants.CKAN_ID, jsonConf.getString(DataSetConstants.CKAN_ID));
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

				if (dataSource != null && dataSource.getHibDialectClass().toLowerCase().contains("mongo")) {
					ds = new MongoDataSet();
				} else if (dataSource != null && dataSource.getHibDialectClass().toLowerCase().contains("hbase")) {
					ds = new JDBCHBaseDataSet();
				} else if (dataSource != null && SqlUtils.isHiveLikeDialect(dataSource.getHibDialectClass().toLowerCase())) {
					ds = new JDBCHiveDataSet();
				} else if (dataSource != null && dataSource.getHibDialectClass().toLowerCase().contains("orient")) {
					ds = new JDBCOrientDbDataSet();
				} else {
					ds = new JDBCDataSet();
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
				} else {
					logger.error("Could not retrieve datasource with label " + jsonConf.getString(DataSetConstants.DATA_SOURCE) + " for dataset "
							+ sbiDataSet.getLabel());
				}

			}

			if (sbiDataSet.getType().equalsIgnoreCase(DataSetConstants.DS_WS)) {
				ds = new WebServiceDataSet();
				ds.setConfiguration(sbiDataSet.getConfiguration());
				((WebServiceDataSet) ds).setAddress(jsonConf.getString(DataSetConstants.WS_ADDRESS));
				((WebServiceDataSet) ds).setOperation(jsonConf.getString(DataSetConstants.WS_OPERATION));
				ds.setDsType(WS_DS_TYPE);
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

				// START -> This code should work instead of CheckQbeDataSets around the projects
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
					ds.setDataStoreTransformer(new PivotDataSetTransformer(ds.getPivotColumnName(), ds.getPivotColumnValue(), ds.getPivotRowName(), ds
							.isNumRows()));
				}
				ds.setPersisted(sbiDataSet.isPersisted());
				ds.setPersistTableName(sbiDataSet.getPersistTableName());
				ds.setOwner(sbiDataSet.getOwner());
				ds.setPublic(sbiDataSet.is_public());
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

}
