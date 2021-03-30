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
package it.eng.spagobi.commons.serializer;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersList;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.constants.CkanDataSetConstants;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.constants.PythonDataSetConstants;
import it.eng.spagobi.tools.dataset.constants.RESTDataSetConstants;
import it.eng.spagobi.tools.dataset.constants.SPARQLDatasetConstants;
import it.eng.spagobi.tools.dataset.constants.SolrDataSetConstants;
import it.eng.spagobi.tools.dataset.service.ManageDatasets;
import it.eng.spagobi.tools.tag.SbiTag;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.ActionNotPermittedException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;
import it.eng.spagobi.utilities.messages.EngineMessageBundle;

public class DataSetJSONSerializer implements Serializer {

	private static transient Logger logger = Logger.getLogger(DataSetJSONSerializer.class);

	public static final String DS_ID = "dsId";
	public static final String VERSION_ID = "versId";
	public static final String VERSION_NUM = "versNum";
	public static final String USER_IN = "userIn";
	public static final String TYPE = "type";
	public static final String DATE_IN = "dateIn";
	public static final String DS_OLD_VERSIONS = "dsVersions";

	public static final String ID = "id";
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String LABEL = "label";
	private static final String USED_BY_N_DOCS = "usedByNDocs";

	private static final String CATEGORY_TYPE_VN = "catTypeVn";
	private static final String CATEGORY_TYPE_CD = "catTypeCd";
	private static final String CATEGORY_TYPE_ID = "catTypeId";

	private static final String PARS = "pars";
	private static final String METADATA = "meta";
	private static final String CUSTOMS = "Custom";

	private static final String DS_TYPE_CD = "dsTypeCd";
	private static final String FILE_NAME = "fileName";
	private static final String QUERY = "query";
	private static final String QUERY_SCRIPT = "queryScript";
	private static final String QUERY_SCRIPT_LANGUAGE = "queryScriptLanguage";
	private static final String DATA_SOURCE = "dataSource";
	private static final String WS_ADDRESS = "wsAddress";
	private static final String WS_OPERATION = "wsOperation";
	private static final String SCRIPT = "script";
	private static final String SCRIPT_LANGUAGE = "scriptLanguage";
	private static final String JCLASS_NAME = "jClassName";

	private static final String QBE_DATA_SOURCE = "qbeDataSource";
	private static final String QBE_DATA_SOURCE_ID = "qbeDataSourceId";
	private static final String QBE_DATAMARTS = "qbeDatamarts";
	private static final String QBE_JSON_QUERY = "qbeJSONQuery";
	private static final String QBE_SQL_QUERY = "qbeSQLQuery";

	private static final String TRASFORMER_TYPE_CD = "trasfTypeCd";
	private static final String PIVOT_COL_NAME = "pivotColName";
	private static final String PIVOT_COL_VALUE = "pivotColValue";
	private static final String PIVOT_ROW_NAME = "pivotRowName";
	private static final String PIVOT_IS_NUM_ROWS = "pivotIsNumRows";

	private static final String IS_PERSISTED = "isPersisted";
	private static final String IS_PERSISTED_HDFS = "isPersistedHDFS";
	private static final String PERSIST_TABLE_NAME = "persistTableName";
	private static final String FLAT_TABLE_NAME = "flatTableName";
	private static final String DATA_SOURCE_FLAT = "dataSourceFlat";

	// (danristo)
	private static final String IS_SCHEDULED = "isScheduled";
	private static final String START_DATE = "startDate";
	private static final String END_DATE = "endDate";
	private static final String SCHEDULING_CRON_LINE = "schedulingCronLine";

	private static final String OWNER = "owner";

	public static final String CSV_FILE_DELIMITER_CHARACTER = "csvDelimiter";
	public static final String CSV_FILE_QUOTE_CHARACTER = "csvQuote";
	public static final String CSV_FILE_ENCODING = "csvEncoding";
	public static final String FILE_DATE_FORMAT = "dateFormat";
	public static final String FILE_TIMESTAMP_FORMAT = "timestampFormat";
	public static final String FILE_TYPE = "fileType";

	public static final String XSL_FILE_SKIP_ROWS = "skipRows";
	public static final String XSL_FILE_LIMIT_ROWS = "limitRows";
	public static final String XSL_FILE_SHEET_NUMBER = "xslSheetNumber";

	private static final String SCOPE_CD = "scopeCd";
	private static final String SCOPE_ID = "scopeId";

	public static final String CKAN_CSV_FILE_DELIMITER_CHARACTER = "ckanCsvDelimiter";
	public static final String CKAN_CSV_FILE_QUOTE_CHARACTER = "ckanCsvQuote";
	public static final String CKAN_CSV_FILE_ENCODING = "ckanCsvEncoding";
	public static final String CKAN_FILE_TYPE = "ckanFileType";
	public static final String CKAN_CSV_DATE_FORMAT = "ckanDateFormat";

	public static final String CKAN_XSL_FILE_SKIP_ROWS = "ckanSkipRows";
	public static final String CKAN_XSL_FILE_LIMIT_ROWS = "ckanLimitRows";
	public static final String CKAN_XSL_FILE_SHEET_NUMBER = "ckanXslSheetNumber";

	public static final String CKAN_URL = "ckanUrl";
	public static final String CKAN_ID = "ckanId";
	public static final String FEDERATION_ID = "federationId";
	public static final String FEDERATION_NAME = "federationName";

	public static final String IS_REALTIME = "isRealtime";
	public static final String IS_ITERABLE = "isIterable";

	public static final String TAGS = "tags";

	private static final String CAN_LOAD_DATA = "canLoadData";

	private static final String CANNOT_LOAD_DATA_MESSAGE = "cannotLoadDataMessage";

	@SuppressWarnings("unchecked")
	@Override
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject result = null;

		if (!(o instanceof IDataSet)) {
			throw new SerializationException("DataSetJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}

		try {
			IDataSet ds = (IDataSet) o;
			result = new JSONObject();
			Integer dsId = ds.getId();
			result.put(ID, dsId);
			result.put(LABEL, ds.getLabel());
			result.put(NAME, ds.getName());
			result.put(DESCRIPTION, ds.getDescription());
			if (ds.getDatasetFederation() != null) {
				result.put(FEDERATION_ID, ds.getDatasetFederation().getFederation_id());
				result.put(FEDERATION_NAME, ds.getDatasetFederation().getName());
			}

			Integer numObjAssociated = DAOFactory.getDataSetDAO().countBIObjAssociated(dsId);

			Integer numFederAssociated = DAOFactory.getFedetatedDatasetDAO().countFederationsUsingDataset(dsId);

			Integer allUsing = numObjAssociated + numFederAssociated;

			if (allUsing != null) {
				result.put(USED_BY_N_DOCS, allUsing);
			}

			// result.put(CATEGORY_TYPE_VN, ds.getCategoryValueName());
			result.put(CATEGORY_TYPE_VN, ds.getCategoryCd());
			result.put(CATEGORY_TYPE_ID, ds.getCategoryId());
			// result.put(CATEGORY_TYPE_CD, ds.getCategoryCd());

			JSONArray parsListJSON = new JSONArray();
			String pars = ds.getParameters();
			if (pars != null && !pars.equals("")) {
				SourceBean source = SourceBean.fromXMLString(pars);
				if (source != null && source.getName().equals("PARAMETERSLIST")) {
					List<SourceBean> rows = source.getAttributeAsList("ROWS.ROW");
					for (int i = 0; i < rows.size(); i++) {
						SourceBean row = rows.get(i);
						String name = (String) row.getAttribute("NAME");
						String type = (String) row.getAttribute("TYPE");
						String defaultValue = (String) row.getAttribute(DataSetParametersList.DEFAULT_VALUE_XML);
						boolean multiValue = "true".equalsIgnoreCase((String) row.getAttribute("MULTIVALUE"));

						JSONObject jsonPar = new JSONObject();
						jsonPar.put("name", name);
						jsonPar.put("type", type);
						jsonPar.put(ManageDatasets.DEFAULT_VALUE_PARAM, defaultValue);
						jsonPar.put("multiValue", multiValue);
						parsListJSON.put(jsonPar);
					}
				}
			}
			result.put(PARS, parsListJSON);

			String meta = ds.getDsMetadata();
			DataSetMetadataJSONSerializer dataSetMetadataJSONSerializer = new DataSetMetadataJSONSerializer();
			Object serializedMetadata = dataSetMetadataJSONSerializer.serialize(meta, locale);

			result.put(METADATA, serializedMetadata);

			JSONArray versionsListJSON = new JSONArray();
			List<IDataSet> nonActiveDetails = ds.getNoActiveVersions();
			if (nonActiveDetails != null && !nonActiveDetails.isEmpty()) {
				Iterator it = nonActiveDetails.iterator();
				while (it.hasNext()) {
					IDataSet tempDetail = (IDataSet) it.next();
					Integer dsVersionNum = null;
					if (tempDetail instanceof VersionedDataSet) {
						dsVersionNum = ((VersionedDataSet) tempDetail).getVersionNum();
					}
					String dsType = tempDetail.getDsType();
					String userIn = tempDetail.getUserIn();
					// Integer dsVersionId = tempDetail.getDsHId();
					Date timeIn = tempDetail.getDateIn();
					JSONObject jsonOldVersion = new JSONObject();
					jsonOldVersion.put(TYPE, dsType);
					jsonOldVersion.put(USER_IN, userIn);
					jsonOldVersion.put(VERSION_NUM, dsVersionNum);
					// jsonOldVersion.put(VERSION_ID, dsVersionId);
					jsonOldVersion.put(DATE_IN, timeIn);
					jsonOldVersion.put(DS_ID, dsId);
					versionsListJSON.put(jsonOldVersion);
				}
			}
			result.put(DS_OLD_VERSIONS, versionsListJSON);

			// TODO fix this!!!! the same method for dsType is used with 2 set
			// of values: Qbe, File, .... and SbiQbeDataSet, SbiFileDataSet,
			// ....!!!!!
			String type = ds.getDsType();
			if (DataSetConstants.code2name.containsKey(type)) {
				type = DataSetConstants.code2name.get(type);
			}

			result.put(DS_TYPE_CD, type);

			result.put(USER_IN, ds.getUserIn());
			result.put(VERSION_NUM, ((VersionedDataSet) ds).getVersionNum());
			// result.put(VERSION_ID, dsDetail.getDsHId());

			String dateIn = null;
			if (ds.getDateIn() != null) {
				DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
				DateTime dateTime = new DateTime(ds.getDateIn());
				dateIn = formatter.print(dateTime);
			}

			result.put(DATE_IN, dateIn);

			String config = JSONUtils.escapeJsonString(ds.getConfiguration());
			JSONObject jsonConf = ObjectUtils.toJSONObject(config);
			try {
				if (type.equalsIgnoreCase(DataSetConstants.FILE) || type.equalsIgnoreCase(DataSetConstants.CKAN)) {
					String fileName = jsonConf.getString(DataSetConstants.FILE_NAME);
					if (fileName != null) {
						result.put(FILE_NAME, fileName);
					}
					String fileType = jsonConf.getString(DataSetConstants.FILE_TYPE);
					if (fileType != null) {
						result.put(FILE_TYPE, fileType);
					}
					String csvDelimiter = jsonConf.getString(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER);
					if (csvDelimiter != null) {
						result.put(CSV_FILE_DELIMITER_CHARACTER, csvDelimiter);
					}
					String csvQuote = jsonConf.getString(DataSetConstants.CSV_FILE_QUOTE_CHARACTER);
					if (csvQuote != null) {
						result.put(CSV_FILE_QUOTE_CHARACTER, csvQuote);
					}
					// added this check for retrocompatibility
					if (jsonConf.has(DataSetConstants.FILE_DATE_FORMAT)) {
						String dateFormat = jsonConf.getString(DataSetConstants.FILE_DATE_FORMAT);
						if (dateFormat != null) {
							result.put(FILE_DATE_FORMAT, dateFormat);
						}
					} else {
						result.put(FILE_DATE_FORMAT, "");
					}
					if (jsonConf.has(DataSetConstants.FILE_TIMESTAMP_FORMAT)) {
						String timestampFormat = jsonConf.getString(DataSetConstants.FILE_TIMESTAMP_FORMAT);
						if (timestampFormat != null) {
							result.put(FILE_TIMESTAMP_FORMAT, timestampFormat);
						}
					} else {
						result.put(FILE_TIMESTAMP_FORMAT, "");
					}

					// added this check for retrocompatibility
					if (jsonConf.has(DataSetConstants.CSV_FILE_ENCODING)) {
						String csvEncoding = jsonConf.getString(DataSetConstants.CSV_FILE_ENCODING);
						if (csvEncoding != null) {
							result.put(CSV_FILE_ENCODING, csvEncoding);
						}
					} else {
						result.put(CSV_FILE_ENCODING, "");
					}

					String skipRows = jsonConf.getString(DataSetConstants.XSL_FILE_SKIP_ROWS);
					if (skipRows != null) {
						result.put(XSL_FILE_SKIP_ROWS, skipRows);
					}
					String limitRows = jsonConf.getString(DataSetConstants.XSL_FILE_LIMIT_ROWS);
					if (limitRows != null) {
						result.put(XSL_FILE_LIMIT_ROWS, limitRows);
					}
					String xslSheetNumber = jsonConf.getString(DataSetConstants.XSL_FILE_SHEET_NUMBER);
					if (xslSheetNumber != null) {
						result.put(XSL_FILE_SHEET_NUMBER, xslSheetNumber);
					}

					if (type.equalsIgnoreCase(DataSetConstants.CKAN)) {

						String ckanFileType = jsonConf.getString(CkanDataSetConstants.CKAN_FILE_TYPE);
						if (ckanFileType != null) {
							result.put(CKAN_FILE_TYPE, ckanFileType);
						}
						String ckanCsvDelimiter = jsonConf.getString(CkanDataSetConstants.CKAN_CSV_FILE_DELIMITER_CHARACTER);
						if (ckanCsvDelimiter != null) {
							result.put(CKAN_CSV_FILE_DELIMITER_CHARACTER, ckanCsvDelimiter);
						}
						String ckanCsvQuote = jsonConf.getString(CkanDataSetConstants.CKAN_CSV_FILE_QUOTE_CHARACTER);
						if (ckanCsvQuote != null) {
							result.put(CKAN_CSV_FILE_QUOTE_CHARACTER, ckanCsvQuote);
						}
						// added this check for retrocompatibility
						if (jsonConf.has(CkanDataSetConstants.CKAN_CSV_FILE_ENCODING)) {
							String ckanCsvEncoding = jsonConf.getString(CkanDataSetConstants.CKAN_CSV_FILE_ENCODING);
							if (ckanCsvEncoding != null) {
								result.put(CKAN_CSV_FILE_ENCODING, ckanCsvEncoding);
							}
						} else {
							result.put(CKAN_CSV_FILE_ENCODING, "");
						}

						if (jsonConf.has(CkanDataSetConstants.CKAN_CSV_DATE_FORMAT)) {
							String dateFormat = jsonConf.getString(CkanDataSetConstants.CKAN_CSV_DATE_FORMAT);
							if (dateFormat != null) {
								result.put(CKAN_CSV_DATE_FORMAT, dateFormat);
							}
						} else {
							result.put(CKAN_CSV_DATE_FORMAT, "");
						}
						String ckanSkipRows = jsonConf.getString(CkanDataSetConstants.CKAN_XSL_FILE_SKIP_ROWS);
						if (ckanSkipRows != null) {
							result.put(CKAN_XSL_FILE_SKIP_ROWS, ckanSkipRows);
						}
						String ckanLimitRows = jsonConf.getString(CkanDataSetConstants.CKAN_XSL_FILE_LIMIT_ROWS);
						if (ckanLimitRows != null) {
							result.put(CKAN_XSL_FILE_LIMIT_ROWS, ckanLimitRows);
						}
						String ckanXslSheetNumber = jsonConf.getString(CkanDataSetConstants.CKAN_XSL_FILE_SHEET_NUMBER);
						if (ckanXslSheetNumber != null) {
							result.put(CKAN_XSL_FILE_SHEET_NUMBER, ckanXslSheetNumber);
						}

						String ckanUrl = jsonConf.getString(CkanDataSetConstants.CKAN_URL);
						if (ckanUrl != null) {
							result.put(CKAN_URL, ckanUrl);
						}

						String ckanId = jsonConf.getString(CkanDataSetConstants.CKAN_ID);
						if (ckanId != null) {
							result.put(CKAN_ID, ckanId);
						}
					}

				} else if (type.equalsIgnoreCase(DataSetConstants.QUERY)) {
					result.put(QUERY, jsonConf.getString(DataSetConstants.QUERY));
					result.put(QUERY_SCRIPT, jsonConf.getString(DataSetConstants.QUERY_SCRIPT));
					result.put(QUERY_SCRIPT_LANGUAGE, jsonConf.getString(DataSetConstants.QUERY_SCRIPT_LANGUAGE));
					result.put(DATA_SOURCE, jsonConf.getString(DataSetConstants.DATA_SOURCE));
				} else if (type.equalsIgnoreCase(DataSetConstants.QBE)) {
					// result.put(QBE_SQL_QUERY,
					// jsonConf.getString(DataSetConstants.QBE_SQL_QUERY));
					result.put(QBE_JSON_QUERY, jsonConf.getString(DataSetConstants.QBE_JSON_QUERY));
					result.put(QBE_DATA_SOURCE, jsonConf.getString(DataSetConstants.QBE_DATA_SOURCE));
					Integer dataSourceId = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(jsonConf.getString(DataSetConstants.QBE_DATA_SOURCE)).getDsId();
					result.put(QBE_DATA_SOURCE_ID, dataSourceId);
					result.put(QBE_DATAMARTS, jsonConf.getString(DataSetConstants.QBE_DATAMARTS));
				} else if (type.equalsIgnoreCase(DataSetConstants.FEDERATED)) {
					result.put(QBE_JSON_QUERY, jsonConf.getString(DataSetConstants.QBE_JSON_QUERY));
					result.put(QBE_DATA_SOURCE, jsonConf.getString(DataSetConstants.QBE_DATA_SOURCE));
					Integer dataSourceId = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(jsonConf.getString(DataSetConstants.QBE_DATA_SOURCE)).getDsId();
					result.put(QBE_DATA_SOURCE_ID, dataSourceId);
					result.put(FEDERATION_ID, ds.getDatasetFederation().getFederation_id());
					result.put(FEDERATION_NAME, ds.getDatasetFederation().getName());
				} else if (type.equalsIgnoreCase(DataSetConstants.WEB_SERVICE)) {
					String ws_address = jsonConf.getString(DataSetConstants.WS_ADDRESS);
					if (ws_address != null) {
						result.put(WS_ADDRESS, ws_address);
					}
					String ws_operation = jsonConf.getString(DataSetConstants.WS_OPERATION);
					if (ws_operation != null) {
						result.put(WS_OPERATION, ws_operation);
					}
				} else if (type.equalsIgnoreCase(DataSetConstants.SCRIPT)) {
					String script = jsonConf.getString(DataSetConstants.SCRIPT);
					if (script != null) {
						result.put(SCRIPT, script);
					}
					String script_language = jsonConf.getString(DataSetConstants.SCRIPT_LANGUAGE);
					if (script_language != null) {
						result.put(SCRIPT_LANGUAGE, script_language);
					}
				} else if (type.equalsIgnoreCase(DataSetConstants.JAVA_CLASS)) {
					String jClass = jsonConf.getString(DataSetConstants.JCLASS_NAME);
					if (jClass != null) {
						result.put(JCLASS_NAME, jClass);
					}
				} else if (type.equalsIgnoreCase(CUSTOMS)) {
					String customData = jsonConf.getString(DataSetConstants.CUSTOM_DATA);
					JSONObject customJSONObject = new JSONObject();
					if (customData != null && !customData.equals("")) {
						customJSONObject = new JSONObject(customData);
					}
					result.put(CUSTOMS, customJSONObject);

					String jClass = jsonConf.getString(DataSetConstants.JCLASS_NAME);
					if (jClass != null) {
						result.put(JCLASS_NAME, jClass);
					}
				} else if (type.equalsIgnoreCase(DataSetConstants.FLAT)) {
					result.put(DATA_SOURCE_FLAT, jsonConf.getString(DataSetConstants.DATA_SOURCE_FLAT));
					result.put(FLAT_TABLE_NAME, jsonConf.getString(DataSetConstants.FLAT_TABLE_NAME));
				} else if (DataSetConstants.DS_REST_NAME.equalsIgnoreCase(type)) {
					manageRESTDataSet(jsonConf, result);
				} else if (DataSetConstants.DS_PYTHON_NAME.equalsIgnoreCase(type)) {
					managePythonDataSet(jsonConf, result);
				} else if (DataSetConstants.DS_SOLR_NAME.equalsIgnoreCase(type)) {
					manageSolrDataSet(jsonConf, result);
				} else if (type.equalsIgnoreCase(DataSetConstants.SPARQL)) {
					manageSPARQLDataSet(jsonConf, result);
				}
			} catch (Exception e) {
				String msg = "Error while defining dataset configuration.";
				logger.error(msg, e);
				throw new SpagoBIRuntimeException(msg, e);
			}

			result.put(TRASFORMER_TYPE_CD, ds.getTransformerCd());
			result.put(PIVOT_COL_NAME, ds.getPivotColumnName());
			result.put(PIVOT_COL_VALUE, ds.getPivotColumnValue());
			result.put(PIVOT_ROW_NAME, ds.getPivotRowName());
			result.put(PIVOT_IS_NUM_ROWS, ds.isNumRows());
			result.put(IS_PERSISTED, ds.isPersisted());
			result.put(IS_PERSISTED_HDFS, ds.isPersistedHDFS());
			result.put(PERSIST_TABLE_NAME, ds.getPersistTableName());
			result.put(IS_SCHEDULED, ds.isScheduled());
			result.put(START_DATE, ds.getStartDateField());
			result.put(END_DATE, ds.getEndDateField());
			result.put(SCHEDULING_CRON_LINE, ds.getSchedulingCronLine());
			result.put(IS_REALTIME, ds.isRealtime());
			result.put(IS_ITERABLE, ds.isIterable());
			result.put(OWNER, ds.getOwner());
			result.put(DATE_IN, dateIn);
			result.put(SCOPE_CD, ds.getScopeCd());
			result.put(SCOPE_ID, ds.getScopeId());

			Set<SbiTag> dsTags = ds.getTags();
			JSONArray tags = new JSONArray();
			Iterator<SbiTag> it = dsTags.iterator();
			while (it.hasNext()) {
				SbiTag tag = it.next();
				JSONObject tagObj = new JSONObject();
				tagObj.put("tagId", tag.getTagId());
				tagObj.put("name", tag.getName());
				tags.put(tagObj);
			}
			result.put(TAGS, tags);

			UserProfile profile = UserProfileManager.getProfile();
			try {
				new DatasetManagementAPI(profile).canLoadData(ds);
				result.put(CAN_LOAD_DATA, true);
			} catch (ActionNotPermittedException e) {
				logger.warn("User " + profile + " cannot preview the dataset with label " + ds.getLabel());
				result.put(CAN_LOAD_DATA, false);
				result.put(CANNOT_LOAD_DATA_MESSAGE, EngineMessageBundle.getMessage(e.getI18NCode(), "MessageFiles.messages", locale));
			}

		} catch (Throwable t) {
			IDataSet ds = (IDataSet) o;
			if (ds instanceof VersionedDataSet) {
				ds = ((VersionedDataSet) ds).getWrappedDataset();
			}
			throw new SerializationException("An error occurred to dataset (" + ds.getLabel() + ") while serializing object: " + o, t);
		} finally {

		}
		return result;
	}

	private void manageSPARQLDataSet(JSONObject conf, JSONObject result) throws JSONException {
		for (String attr : SPARQLDatasetConstants.SPARQL_ATTRIBUTES) {
			if (!conf.has(attr)) {
				continue;
			}
			Object value = conf.get(attr);
			Assert.assertNotNull(value, "json value");
			result.put(attr, value.toString());
		}

	}

	private static void manageRESTDataSet(JSONObject conf, JSONObject result) throws JSONException {
		for (String attr : RESTDataSetConstants.REST_ALL_ATTRIBUTES) {
			if (!conf.has(attr)) {
				// optional attribute
				continue;
			}
			Object value = conf.get(attr);
			Assert.assertNotNull(value, "json value");
			result.put(attr, value.toString());
		}
	}

	private static void managePythonDataSet(JSONObject conf, JSONObject result) throws JSONException {
		for (String attr : PythonDataSetConstants.PYTHON_ALL_ATTRIBUTES) {
			if (!conf.has(attr) || result.has(attr)) {
				continue;
			}
			Object value = conf.get(attr);
			Assert.assertNotNull(value, "json value");
			result.put(attr, value.toString());
		}
	}

	private static void manageSolrDataSet(JSONObject conf, JSONObject result) throws JSONException {
		for (String ja : RESTDataSetConstants.REST_JSON_OBJECT_ATTRIBUTES) {
			Object prop = conf.get(ja);
			if (prop != null) {
				result.put(ja, new JSONObject(prop));
			}
		}

		for (String sa : SolrDataSetConstants.SOLR_STRING_ATTRIBUTES) {
			Object prop = conf.get(sa);
			if (prop != null) {
				result.put(sa, prop);
			}
		}
		for (String ja : SolrDataSetConstants.SOLR_JSON_ARRAY_ATTRIBUTES) {
			Object prop = conf.get(ja);
			if (prop != null) {
				result.put(ja, prop);
			}
		}
	}

}