/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines all SpagoBI's constants.
 */
public class DataSetConstants {

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
	
	public static final String CATEGORY_DOMAIN_TYPE = "CATEGORY_TYPE";
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
	public static final String FLAT = "Flat";
	
	public static final String TRASFORMER_TYPE_CD = "trasfTypeCd";
	public static final String PIVOT_COL_NAME = "pivotColName";
	public static final String PIVOT_COL_VALUE = "pivotColValue";
	public static final String PIVOT_ROW_NAME = "pivotRowName";
	public static final String PIVOT_IS_NUM_ROWS = "pivotIsNumRows";
	
	public static final String RECALCULATE_METADATA = "recalculateMetadata";
	
	public static final String DS_WS = "SbiWSDataSet";
	public static final String DS_FILE = "SbiFileDataSet";
	public static final String DS_CKAN = "SbiCkanDataSet";
	public static final String DS_JCLASS = "SbiJClassDataSet";
	public static final String DS_QUERY = "SbiQueryDataSet";
	public static final String DS_SCRIPT = "SbiScriptDataSet";
	public static final String DS_QBE = "SbiQbeDataSet";
	public static final String DS_CUSTOM = "SbiCustomDataSet";
	public static final String DS_FLAT = "SbiFlatDataSet";
	
	public static final String QBE_DATA_SOURCE = "qbeDataSource";
	public static final String QBE_DATAMARTS = "qbeDatamarts";
	public static final String QBE_JSON_QUERY = "qbeJSONQuery";
	public static final String QBE_SQL_QUERY = "qbeSQLQuery";
	
	public static final String SOURCE_DS_LABEL = "sourceDatasetLabel";
	
	public static final String IS_PERSISTED = "isPersisted";
	public static final String IS_SCHEDULED = "isScheduled";
	public static final String FLAT_TABLE_NAME = "flatTableName";
	public static final String DATA_SOURCE_FLAT = "dataSourceFlat";
	public static final String IS_PUBLIC = "isPublic";
	public static final String PERSIST_TABLE_NAME = "persistTableName";
	
	public static final String START = "start";
	public static final String LIMIT = "limit";
	public static final Integer START_DEFAULT = 0;
	public static final Integer LIMIT_DEFAULT = 15;
	
	//filters parameters
	public static final String FILTERS = "FILTERS";
	
	public static final String FILE_TYPE = "fileType";
	public static final String CSV_FILE_DELIMITER_CHARACTER = "csvDelimiter";
	public static final String CSV_FILE_QUOTE_CHARACTER = "csvQuote";
	public static final String CSV_FILE_ENCODING = "csvEncoding";

	public static final String XSL_FILE_SKIP_ROWS = "skipRows";
	public static final String XSL_FILE_LIMIT_ROWS = "limitRows";
	public static final String XSL_FILE_SHEET_NUMBER = "xslSheetNumber";

	// CKAN dataset constants

	public static final String CKAN_FILE_TYPE = "ckanFileType";
	public static final String CKAN_CSV_FILE_DELIMITER_CHARACTER = "ckanCsvDelimiter";
	public static final String CKAN_CSV_FILE_QUOTE_CHARACTER = "ckanCsvQuote";
	public static final String CKAN_CSV_FILE_ENCODING = "ckanCsvEncoding";

	public static final String CKAN_XSL_FILE_SKIP_ROWS = "ckanSkipRows";
	public static final String CKAN_XSL_FILE_LIMIT_ROWS = "ckanLimitRows";
	public static final String CKAN_XSL_FILE_SHEET_NUMBER = "ckanXslSheetNumber";

	public static final String CKAN_ID = "ckanId";
	public static final String CKAN_URL = "ckanUrl";

	public static final Map<String, String> name2Code;
	
	static {
		name2Code = new HashMap<String, String>();
		name2Code.put("Web Service", DataSetConstants.DS_WS);
		name2Code.put("File", DataSetConstants.DS_FILE);
		name2Code.put("Ckan", DataSetConstants.DS_CKAN);
		name2Code.put("Java Class", DataSetConstants.DS_JCLASS);
		name2Code.put("Query", DataSetConstants.DS_QUERY);
		name2Code.put("Script", DataSetConstants.DS_SCRIPT);
		name2Code.put("Qbe", DataSetConstants.DS_QBE);
		name2Code.put("Custom", DataSetConstants.DS_CUSTOM);
		name2Code.put("Flat", DataSetConstants.DS_FLAT);
	}
	
	public static final Map<String, String> code2name;
	
	static {
		code2name = new HashMap<String, String>();
		code2name.put(DataSetConstants.DS_WS, "Web Service");
		code2name.put(DataSetConstants.DS_FILE, "File");
		code2name.put(DataSetConstants.DS_CKAN, "Ckan");
		code2name.put(DataSetConstants.DS_JCLASS, "Java Class");
		code2name.put(DataSetConstants.DS_QUERY, "Query");
		code2name.put(DataSetConstants.DS_SCRIPT, "Script");
		code2name.put(DataSetConstants.DS_QBE, "Qbe");
		code2name.put(DataSetConstants.DS_CUSTOM, "Custom");
		code2name.put(DataSetConstants.DS_FLAT, "Flat");
	}
	
}
