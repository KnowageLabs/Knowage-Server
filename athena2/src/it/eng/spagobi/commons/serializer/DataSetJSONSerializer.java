/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.json.JSONUtils;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	private static final String JCLASS_NAME = "jclassName";
	
	private static final String QBE_DATA_SOURCE = "qbeDataSource";
	private static final String QBE_DATAMARTS = "qbeDatamarts";
	private static final String QBE_JSON_QUERY = "qbeJSONQuery";
	private static final String QBE_SQL_QUERY = "qbeSQLQuery";
	
	private static final String TRASFORMER_TYPE_CD = "trasfTypeCd";
	private static final String PIVOT_COL_NAME = "pivotColName";
	private static final String PIVOT_COL_VALUE = "pivotColValue";
	private static final String PIVOT_ROW_NAME = "pivotRowName";
	private static final String PIVOT_IS_NUM_ROWS = "pivotIsNumRows";
	
	private static final String IS_PERSISTED = "isPersisted";
	private static final String PERSIST_TABLE_NAME = "persistTableName";
	private static final String FLAT_TABLE_NAME = "flatTableName";
	private static final String DATA_SOURCE_FLAT = "dataSourceFlat";
	
	private static final String IS_PUBLIC = "isPublic";
	private static final String OWNER = "owner";
	
	public static final String CSV_FILE_DELIMITER_CHARACTER = "csvDelimiter";
	public static final String CSV_FILE_QUOTE_CHARACTER = "csvQuote";
	public static final String CSV_FILE_ENCODING = "csvEncoding";
	public static final String FILE_TYPE = "fileType";
	
	public static final String XSL_FILE_SKIP_ROWS = "skipRows";
	public static final String XSL_FILE_LIMIT_ROWS = "limitRows";
	public static final String XSL_FILE_SHEET_NUMBER = "xslSheetNumber";
	
	private static final String SCOPE_CD = "scopeCd";
	private static final String SCOPE_ID = "scopeId";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof IDataSet)) {
			throw new SerializationException("DataSetJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			IDataSet ds = (IDataSet)o;
			result = new JSONObject();
			Integer dsId = ds.getId();
			result.put(ID, dsId);
			result.put(LABEL, ds.getLabel() );	
			result.put(NAME, ds.getName() );
			result.put(DESCRIPTION, ds.getDescription() );
			Integer numObjAssociated = DAOFactory.getDataSetDAO().countBIObjAssociated(new Integer(ds.getId()));
			if(numObjAssociated!=null){
				result.put(USED_BY_N_DOCS, numObjAssociated );
			}
			
			//result.put(CATEGORY_TYPE_VN, ds.getCategoryValueName());
			result.put(CATEGORY_TYPE_VN, ds.getCategoryCd()); 
			result.put(CATEGORY_TYPE_ID, ds.getCategoryId()); 
			//result.put(CATEGORY_TYPE_CD, ds.getCategoryCd()); 

			JSONArray parsListJSON = new JSONArray();
			String pars = ds.getParameters();
			if(pars!=null && !pars.equals("")){
				SourceBean source = SourceBean.fromXMLString(pars);
				if(source!=null && source.getName().equals("PARAMETERSLIST")){
					List<SourceBean> rows = source.getAttributeAsList("ROWS.ROW");
					for(int i=0; i< rows.size(); i++){
						SourceBean row = rows.get(i);
						String name = (String)row.getAttribute("NAME");
						String type = (String)row.getAttribute("TYPE");
						JSONObject jsonPar = new JSONObject();
						jsonPar.put("name", name);
						jsonPar.put("type", type);
						parsListJSON.put(jsonPar);
					}				
				}
			}
			result.put(PARS, parsListJSON);	
			
			String meta = ds.getDsMetadata();
			Object serializedMetadata = metadataSerializerChooser(meta);
			
			result.put(METADATA, serializedMetadata);
			
			JSONArray versionsListJSON = new JSONArray();
			List<IDataSet> nonActiveDetails = ds.getNoActiveVersions();			
			if(nonActiveDetails!=null && !nonActiveDetails.isEmpty()){
				Iterator it = nonActiveDetails.iterator();
				while(it.hasNext()){
					IDataSet tempDetail = (IDataSet)it.next();
					Integer dsVersionNum = null;
					if (tempDetail instanceof VersionedDataSet){
						dsVersionNum = ((VersionedDataSet) tempDetail).getVersionNum();
					}
					String dsType = tempDetail.getDsType();
					String userIn = tempDetail.getUserIn();				
			//		Integer dsVersionId = tempDetail.getDsHId();
					Date timeIn = tempDetail.getDateIn();
					JSONObject jsonOldVersion = new JSONObject();
					jsonOldVersion.put(TYPE, dsType);
					jsonOldVersion.put(USER_IN, userIn);
					jsonOldVersion.put(VERSION_NUM, dsVersionNum);
					//jsonOldVersion.put(VERSION_ID, dsVersionId);
					jsonOldVersion.put(DATE_IN, timeIn);
					jsonOldVersion.put(DS_ID, dsId);
					versionsListJSON.put(jsonOldVersion);
				}
			}
			result.put(DS_OLD_VERSIONS, versionsListJSON);	
			
			// TODO fix this!!!! the same method for dsType is used with 2 set of values: Qbe, File, .... and SbiQbeDataSet, SbiFileDataSet, ....!!!!!
			String type = ds.getDsType();
			if (DataSetConstants.code2name.containsKey(type)) {
				type = DataSetConstants.code2name.get(type);
			}
			
			result.put(DS_TYPE_CD, type);	
			
			result.put(USER_IN, ds.getUserIn());
			result.put(VERSION_NUM, ((VersionedDataSet) ds).getVersionNum());
			//result.put(VERSION_ID, dsDetail.getDsHId());
			result.put(DATE_IN, ds.getDateIn());

			String config = JSONUtils.escapeJsonString(ds.getConfiguration());		
			JSONObject jsonConf  = ObjectUtils.toJSONObject(config);
			try{
				if(type.equalsIgnoreCase(DataSetConstants.FILE)){
					String fileName = jsonConf.getString(DataSetConstants.FILE_NAME);
					if(fileName!=null){
						result.put(FILE_NAME, fileName);				
					}		
					String fileType = jsonConf.getString(DataSetConstants.FILE_TYPE);
					if(fileType!=null){
						result.put(FILE_TYPE, fileType);				
					}	
					String csvDelimiter = jsonConf.getString(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER);
					if(csvDelimiter!=null){
						result.put(CSV_FILE_DELIMITER_CHARACTER, csvDelimiter);				
					}	
					String csvQuote = jsonConf.getString(DataSetConstants.CSV_FILE_QUOTE_CHARACTER); 
					if(csvQuote!=null){
						result.put(CSV_FILE_QUOTE_CHARACTER, csvQuote);				
					}
					//added this check for retrocompatibility
					if (jsonConf.has(DataSetConstants.CSV_FILE_ENCODING)){
						String csvEncoding = jsonConf.getString(DataSetConstants.CSV_FILE_ENCODING); 
						if(csvEncoding!=null){
							result.put(CSV_FILE_ENCODING, csvEncoding);				
						}
					} else {
						result.put(CSV_FILE_ENCODING, "");	
					}

					String skipRows = jsonConf.getString(DataSetConstants.XSL_FILE_SKIP_ROWS); 
					if(skipRows!=null){
						result.put(XSL_FILE_SKIP_ROWS, skipRows);				
					}
					String limitRows = jsonConf.getString(DataSetConstants.XSL_FILE_LIMIT_ROWS); 
					if(limitRows!=null){
						result.put(XSL_FILE_LIMIT_ROWS, limitRows);				
					}
					String xslSheetNumber = jsonConf.getString(DataSetConstants.XSL_FILE_SHEET_NUMBER); 
					if(xslSheetNumber!=null){
						result.put(XSL_FILE_SHEET_NUMBER, xslSheetNumber);				
					}
					
				}else if(type.equalsIgnoreCase(DataSetConstants.QUERY)){
					result.put(QUERY, jsonConf.getString(DataSetConstants.QUERY));
					result.put(QUERY_SCRIPT, jsonConf.getString(DataSetConstants.QUERY_SCRIPT));
					result.put(QUERY_SCRIPT_LANGUAGE, jsonConf.getString(DataSetConstants.QUERY_SCRIPT_LANGUAGE));
					result.put(DATA_SOURCE, jsonConf.getString(DataSetConstants.DATA_SOURCE));
				}else if(type.equalsIgnoreCase(DataSetConstants.QBE)) {					
				//	result.put(QBE_SQL_QUERY, jsonConf.getString(DataSetConstants.QBE_SQL_QUERY));
					result.put(QBE_JSON_QUERY,jsonConf.getString(DataSetConstants.QBE_JSON_QUERY));
					result.put(QBE_DATA_SOURCE, jsonConf.getString(DataSetConstants.QBE_DATA_SOURCE));
					result.put(QBE_DATAMARTS, jsonConf.getString(DataSetConstants.QBE_DATAMARTS));			
				}else if(type.equalsIgnoreCase(DataSetConstants.WEB_SERVICE)){
					String ws_address = jsonConf.getString(DataSetConstants.WS_ADDRESS);
					if(ws_address!=null){
						result.put(WS_ADDRESS, ws_address);
					}
					String ws_operation = jsonConf.getString(DataSetConstants.WS_OPERATION);
					if(ws_operation!=null){
						result.put(WS_OPERATION, ws_operation);
					}	
				}else if(type.equalsIgnoreCase(DataSetConstants.SCRIPT)){
					String script = jsonConf.getString(DataSetConstants.SCRIPT);
					if(script!=null){					
						result.put(SCRIPT, script);
					}
					String script_language = jsonConf.getString(DataSetConstants.SCRIPT_LANGUAGE);
					if(script_language!=null){
						result.put(SCRIPT_LANGUAGE, script_language);
					}
				}else if(type.equalsIgnoreCase(DataSetConstants.JAVA_CLASS)){
					String jClass =  jsonConf.getString(DataSetConstants.JCLASS_NAME);
					if(jClass!=null){
						result.put(JCLASS_NAME, jClass);
					}
				}else if(type.equalsIgnoreCase(CUSTOMS)){
					String customData =  jsonConf.getString(DataSetConstants.CUSTOM_DATA);
					JSONObject customJSONObject = new JSONObject();
					if(customData!=null && !customData.equals("")){
						customJSONObject =new JSONObject(customData);
					}
					result.put(CUSTOMS, customJSONObject);	
					
					String jClass =  jsonConf.getString(DataSetConstants.JCLASS_NAME);
					if(jClass!=null){
						result.put(JCLASS_NAME, jClass);
					}		
				} else if(type.equalsIgnoreCase(DataSetConstants.FLAT)){
					result.put(DATA_SOURCE_FLAT, jsonConf.getString(DataSetConstants.DATA_SOURCE));
					result.put(FLAT_TABLE_NAME, jsonConf.getString(DataSetConstants.FLAT_TABLE_NAME));
				}
			}catch (Exception e){
				logger.error("Error while defining dataset configuration.  Error: " + e.getMessage());
			}
			
			result.put(TRASFORMER_TYPE_CD, ds.getTransformerCd());
			result.put(PIVOT_COL_NAME, ds.getPivotColumnName());	
			result.put(PIVOT_COL_VALUE, ds.getPivotColumnValue());	
			result.put(PIVOT_ROW_NAME,ds.getPivotRowName());	
			result.put(PIVOT_IS_NUM_ROWS,ds.isNumRows());
			result.put(IS_PERSISTED,ds.isPersisted());	
			result.put(PERSIST_TABLE_NAME,ds.getPersistTableName());		
			result.put(IS_PUBLIC, ds.isPublic());
			result.put(OWNER, ds.getOwner());
			result.put(DATE_IN, ds.getDateIn());
			result.put(SCOPE_CD, ds.getScopeCd()); 
			result.put(SCOPE_ID, ds.getScopeId()); 
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}		
	  return result;
	}
	
	public static Object metadataSerializerChooser(String meta) throws SourceBeanException,JSONException{
		if(meta!=null && !meta.equals("")){
			SourceBean source = SourceBean.fromXMLString(meta);
			if(source!=null){
				if(source.getName().equals("COLUMNLIST")){
					return serializeMetada(meta);
				} else if(source.getName().equals("META")){
					return serializeGenericMetadata(meta);
				}
			}
		}
		
		return null;
		

		
	}
	
	public static JSONArray serializeMetada(String meta) throws JSONException, SourceBeanException{
		JSONArray metaListJSON = new JSONArray();

		if(meta!=null && !meta.equals("")){
			SourceBean source = SourceBean.fromXMLString(meta);
			if(source!=null){
				if(source.getName().equals("COLUMNLIST")){
					List<SourceBean> rows = source.getAttributeAsList("COLUMN");
					for(int i=0; i< rows.size(); i++){
						SourceBean row = rows.get(i);
						String name = (String)row.getAttribute("name");
						String type = (String)row.getAttribute("TYPE");
						String fieldType = (String)row.getAttribute("fieldType");
						JSONObject jsonMeta = new JSONObject();
						jsonMeta.put("name", name);
						jsonMeta.put("type", type);
						jsonMeta.put("fieldType", fieldType);
						metaListJSON.put(jsonMeta);
					}				
				}else if(source.getName().equals("METADATALIST")){
					List<SourceBean> rows = source.getAttributeAsList("ROWS.ROW");
					for(int i=0; i< rows.size(); i++){
						SourceBean row = rows.get(i);
						String name = (String)row.getAttribute("NAME");
						String type = (String)row.getAttribute("TYPE");
						JSONObject jsonMeta = new JSONObject();
						jsonMeta.put("name", name);
						jsonMeta.put("type", type);
						metaListJSON.put(jsonMeta);
					}				
				}
			}
		}
		return metaListJSON;
	}
	
	//Serialize the new generalized version of Metadata
	public static JSONObject serializeGenericMetadata(String meta) throws JSONException, SourceBeanException{
		JSONObject metadataJSONObject = new JSONObject();

		if(meta!=null && !meta.equals("")){
			SourceBean source = SourceBean.fromXMLString(meta);
			
			if(source != null){
				if(source.getName().equals("META")){
					// Dataset Metadata --------------
					
					SourceBean dataset = (SourceBean)source.getAttribute("DATASET");
					JSONArray datasetJSONArray = new JSONArray();
					if (dataset != null){
						List<SourceBean> propertiesDataset = dataset.getAttributeAsList("PROPERTY");
						for(int j=0; j< propertiesDataset.size(); j++){
							SourceBean property = propertiesDataset.get(j);
							String propertyName = (String)property.getAttribute("name");
							String propertyValue = (String)property.getAttribute("value");
							JSONObject propertiesJSONObject = new JSONObject();
							propertiesJSONObject.put("pname", propertyName);
							propertiesJSONObject.put("pvalue", propertyValue);
							datasetJSONArray.put(propertiesJSONObject);
						}
					}

					metadataJSONObject.put("dataset", datasetJSONArray);
				
					// Columns Metadata -------------
					SourceBean columns = (SourceBean)source.getAttribute("COLUMNLIST");
					JSONArray columnsJSONArray = new JSONArray();
					
					List<SourceBean> rows = columns.getAttributeAsList("COLUMN");
					for(int i=0; i< rows.size(); i++){
						SourceBean row = rows.get(i);
						String columnName = (String)row.getAttribute("name");
						String type = (String)row.getAttribute("TYPE");

						
						
						JSONObject typeJSONObject = new JSONObject();
						typeJSONObject.put("column", columnName);
						typeJSONObject.put("pname", "Type");
						typeJSONObject.put("pvalue", type);
						columnsJSONArray.put(typeJSONObject);

						String fieldType = (String)row.getAttribute("fieldType");
						JSONObject fieldTypeJSONObject = new JSONObject();
						fieldTypeJSONObject.put("column", columnName);
						fieldTypeJSONObject.put("pname", "fieldType");
						fieldTypeJSONObject.put("pvalue", fieldType);
						columnsJSONArray.put(fieldTypeJSONObject);
						
						List<SourceBean> properties = row.getAttributeAsList("PROPERTY");
						for(int j=0; j< properties.size(); j++){
							SourceBean property = properties.get(j);
							String propertyName = (String)property.getAttribute("name");
							String propertyValue = (String)property.getAttribute("value");
							JSONObject propertiesJSONObject = new JSONObject();
							propertiesJSONObject.put("column", columnName);
							propertiesJSONObject.put("pname", propertyName);
							propertiesJSONObject.put("pvalue", propertyValue);

							columnsJSONArray.put(propertiesJSONObject);
						}
						
					}
					metadataJSONObject.put("columns", columnsJSONArray);
					
				}
			}
			
			
			

		}
		return metadataJSONObject;
	}
	

	
}