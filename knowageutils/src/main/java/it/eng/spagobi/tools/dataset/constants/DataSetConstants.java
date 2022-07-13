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
package it.eng.spagobi.tools.dataset.constants;

import java.util.HashMap;
import java.util.Map;

import it.eng.spagobi.tools.dataset.bo.PreparedDataSet;
import it.eng.spagobi.tools.dataset.bo.PythonDataSet;
import it.eng.spagobi.tools.dataset.bo.RESTDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;

/**
 * Defines all SpagoBI's constants.
 */
public class DataSetConstants {

	public static final String RESOURCE_RELATIVE_FOLDER = "dataset";
	public static final String DOMAIN_VALUES_FOLDER = "domains";
	public static final String FILES_DATASET_FOLDER = "files";
	public static final String DOMAIN_VALUES_EXTENSION = ".bin";

	public static final String MESSAGE_DET = "MESSAGE_DET";
	// type of service
	public static final String DATASETS_LIST = "DATASETS_LIST";
	public static final String DATASET_INSERT = "DATASET_INSERT";
	public static final String DATASET_DELETE = "DATASET_DELETE";
	public static final String DATASET_TEST = "DATASET_TEST";
	public static final String DATASET_EXEC = "DATASET_EXEC";

	public static final String DATASET_VERSION_RESTORE = "DATASET_VERSION_RESTORE";
	public static final String DATASET_VERSION_DELETE = "DATASET_VERSION_DELETE";
	public static final String DATASET_ALL_VERSIONS_DELETE = "DATASET_ALL_VERSIONS_DELETE";

	public static final String DATASETS_FOR_KPI_LIST = "DATASETS_FOR_KPI_LIST";

	public static final String SCRIPT_TYPE = "SCRIPT_TYPE";
	public static final String DATA_SET_TYPE = "DATA_SET_TYPE";
	public static final String TRANSFORMER_TYPE = "TRANSFORMER_TYPE";
	public static final String DS_SCOPE = "DS_SCOPE";
	public static final String DS_SCOPE_USER = "USER";

	public static final String DS_ID = "dsId";
	public static final String VERSION_ID = "versId";
	public static final String VERSION_NUM = "versNum";

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String LABEL = "label";
	public static final String DS_METADATA = "metadata";

	public static final String CATEGORY_TYPE_VN = "catTypeVn";

	public static final String PARS = "pars";
	public static final String METADATA = "meta";

	public static final String DS_TYPE_CD = "dsTypeCd";
	public static final String CONFIGURATION = "configuration";
	public static final String FILE_NAME = "fileName";
	public static final String QUERY = "Query";
	public static final String QUERY_SCRIPT = "queryScript";
	public static final String QUERY_SCRIPT_LANGUAGE = "queryScriptLanguage";
	public static final String DATA_SOURCE = "dataSource";
	public static final String WS_ADDRESS = "wsAddress";
	public static final String WS_OPERATION = "wsOperation";
	public static final String SCRIPT = "Script";
	public static final String SCRIPT_LANGUAGE = "scriptLanguage";
	public static final String JCLASS_NAME = "jClassName";
	public static final String CUSTOM_DATA = "customData";
	public static final String FILE = "File";
	public static final String CKAN = "Ckan";
	public static final String JAVA_CLASS = "Java Class";
	public static final String WEB_SERVICE = "Web Service";
	public static final String QBE = "Qbe";
	public static final String FEDERATED = "Federated";
	public static final String FLAT = "Flat";
	public static final String DS_REST_NAME = "REST";
	public static final String DS_PYTHON_NAME = "Python/R";
	public static final String DS_SOLR_NAME = "Solr";
	public static final String SPARQL = "SPARQL";

	public static final String TRASFORMER_TYPE_CD = "trasfTypeCd";
	public static final String PIVOT_COL_NAME = "pivotColName";
	public static final String PIVOT_COL_VALUE = "pivotColValue";
	public static final String PIVOT_ROW_NAME = "pivotRowName";
	public static final String PIVOT_IS_NUM_ROWS = "pivotIsNumRows";

	public static final String RECALCULATE_METADATA = "recalculateMetadata";

	public static final String DS_FILE = "SbiFileDataSet";
	public static final String DS_CKAN = "SbiCkanDataSet";
	public static final String DS_JCLASS = "SbiJClassDataSet";
	public static final String DS_QUERY = "SbiQueryDataSet";
	public static final String DS_SCRIPT = "SbiScriptDataSet";
	public static final String DS_QBE = "SbiQbeDataSet";
	public static final String DS_FEDERATED = "SbiFederatedDataSet";
	public static final String DS_CUSTOM = "SbiCustomDataSet";
	public static final String DS_FLAT = "SbiFlatDataSet";
	public static final String DS_PREPARED = "SbiPreparedDataSet";
	public static final String DS_REST_TYPE = RESTDataSet.DATASET_TYPE;
	public static final String DS_PYTHON_TYPE = PythonDataSet.DATASET_TYPE;
	public static final String DS_SOLR_TYPE = SolrDataSet.DATASET_TYPE;
	public static final String DS_SPARQL = "SbiSPARQLDataSet";
	public static final String PREPARED_DATASET = PreparedDataSet.DS_TYPE;

	public static final String QBE_DATA_SOURCE = "qbeDataSource";
	public static final String QBE_DATAMARTS = "qbeDatamarts";
	public static final String QBE_JSON_QUERY = "qbeJSONQuery";
	public static final String QBE_SQL_QUERY = "qbeSQLQuery";

	public static final String SOURCE_DS_LABEL = "sourceDatasetLabel";

	public static final String IS_FROM_SAVE_NO_METADATA = "isFromSaveNoMetadata";
	public static final String IS_PERSISTED = "isPersisted";
	public static final String IS_PERSISTED_HDFS = "isPersistedHDFS";
	public static final String IS_SCHEDULED = "isScheduled";
	public static final String TABLE_NAME = "tableName";
	public static final String DATA_PREPARATION_INSTANCE_ID = "dataPrepInstanceId";
	public static final String FLAT_TABLE_NAME = "flatTableName";
	public static final String DATA_SOURCE_FLAT = "dataSourceFlat";
	public static final String IS_PUBLIC = "isPublic";
	public static final String PERSIST_TABLE_NAME = "persistTableName";
	public static final String FEDERATED_DATASET_NAMES = "federatedDatasets";

	public static final String START = "start";
	public static final String LIMIT = "limit";
	public static final Integer START_DEFAULT = 0;
	public static final Integer LIMIT_DEFAULT = 15;

	// filters parameters
	public static final String FILTERS = "FILTERS";

	public static final String FILE_TYPE = "fileType";
	public static final String CSV_FILE_DELIMITER_CHARACTER = "csvDelimiter";
	public static final String CSV_FILE_QUOTE_CHARACTER = "csvQuote";
	public static final String CSV_FILE_ENCODING = "csvEncoding";

	public static final String XSL_FILE_SKIP_ROWS = "skipRows";
	public static final String XSL_FILE_LIMIT_ROWS = "limitRows";
	public static final String XSL_FILE_SHEET_NUMBER = "xslSheetNumber";

	public static final String FILE_DATE_FORMAT = "dateFormat";
	public static final String FILE_TIMESTAMP_FORMAT = "timestampFormat";

	public static final Map<String, String> name2Code;

	static {
		name2Code = new HashMap<String, String>();
		name2Code.put("File", DataSetConstants.DS_FILE);
		name2Code.put("Ckan", DataSetConstants.DS_CKAN);
		name2Code.put("Java Class", DataSetConstants.DS_JCLASS);
		name2Code.put("Query", DataSetConstants.DS_QUERY);
		name2Code.put("Script", DataSetConstants.DS_SCRIPT);
		name2Code.put("Qbe", DataSetConstants.DS_QBE);
		name2Code.put("Custom", DataSetConstants.DS_CUSTOM);
		name2Code.put("Flat", DataSetConstants.DS_FLAT);
		name2Code.put("Federated", DataSetConstants.DS_FEDERATED);
		name2Code.put("Solr", DataSetConstants.DS_SOLR_TYPE);
		name2Code.put(DS_REST_NAME, DS_REST_TYPE);
		name2Code.put(DS_PYTHON_NAME, DS_PYTHON_TYPE);
		name2Code.put("SPARQL", DataSetConstants.DS_SPARQL);
		name2Code.put("Prepared", DataSetConstants.DS_PREPARED);

		// add all REST attributes
		int curr = 0;
		for (String[] a : new String[][] { RESTDataSetConstants.REST_STRING_ATTRIBUTES, RESTDataSetConstants.REST_JSON_OBJECT_ATTRIBUTES,
				RESTDataSetConstants.REST_JSON_ARRAY_ATTRIBUTES }) {
			System.arraycopy(a, 0, RESTDataSetConstants.REST_ALL_ATTRIBUTES, curr, a.length);
			curr += a.length;
		}

		curr = 0;
		for (String[] a : new String[][] { PythonDataSetConstants.PYTHON_STRING_ATTRIBUTES, PythonDataSetConstants.REST_JSON_ARRAY_ATTRIBUTES }) {
			System.arraycopy(a, 0, PythonDataSetConstants.PYTHON_ALL_ATTRIBUTES, curr, a.length);
			curr += a.length;
		}

		curr = 0;
		for (String[] a : new String[][] { SolrDataSetConstants.SOLR_STRING_ATTRIBUTES, SolrDataSetConstants.SOLR_JSON_ARRAY_ATTRIBUTES }) {
			System.arraycopy(a, 0, SolrDataSetConstants.SOLR_ALL_ATTRIBUTES, curr, a.length);
			curr += a.length;
		}

	}

	public static final Map<String, String> code2name;

	static {
		code2name = new HashMap<String, String>();
		code2name.put(DataSetConstants.DS_FILE, "File");
		code2name.put(DataSetConstants.DS_CKAN, "Ckan");
		code2name.put(DataSetConstants.DS_JCLASS, "Java Class");
		code2name.put(DataSetConstants.DS_QUERY, "Query");
		code2name.put(DataSetConstants.DS_SCRIPT, "Script");
		code2name.put(DataSetConstants.DS_QBE, "Qbe");
		code2name.put(DataSetConstants.DS_CUSTOM, "Custom");
		code2name.put(DataSetConstants.DS_FLAT, "Flat");
		code2name.put(DataSetConstants.DS_FEDERATED, "Federated");
		code2name.put(DS_REST_TYPE, DS_REST_NAME);
		code2name.put(DataSetConstants.DS_SOLR_TYPE, "Solr");
		code2name.put(DS_PYTHON_TYPE, DS_PYTHON_NAME);
		code2name.put(DataSetConstants.DS_SPARQL, "SPARQL");
		code2name.put(DataSetConstants.DS_PREPARED, "Prepared");
	}

}
