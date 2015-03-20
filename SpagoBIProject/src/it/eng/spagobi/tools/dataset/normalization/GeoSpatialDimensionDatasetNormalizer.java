/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.tools.dataset.normalization;

import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.meta.model.olap.Level;
import it.eng.spagobi.metamodel.HierarchyWrapper;
import it.eng.spagobi.metamodel.MetaModelWrapper;
import it.eng.spagobi.metamodel.SiblingsFileWrapper;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.FileDataProxy;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogue;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogueSingleton;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.dataset.validation.HierarchyLevel;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class GeoSpatialDimensionDatasetNormalizer implements IDatasetNormalizer {
	public final String GEO_HIERARCHY_NAME = "geo"; //this normalizer check only hierarchies with this name
	public final String GEO_DIMENSION_NAME = "geo";
	
	public static final String XSL_FILE_SKIP_ROWS = "skipRows";
	public static final String XSL_FILE_LIMIT_ROWS = "limitRows";
	public static final String XSL_FILE_SHEET_NUMBER = "xslSheetNumber";
	public static final String CSV_FILE_DELIMITER_CHARACTER = "csvDelimiter";
	public static final String CSV_FILE_QUOTE_CHARACTER = "csvQuote";
	public static final String CSV_FILE_ENCODING = "csvEncoding";

	
	public static transient Logger logger = Logger.getLogger(GeoSpatialDimensionDatasetNormalizer.class);


	//Modify the original file associated to the dataset adding a column with correct values to use for geo hierarchy
	//then set this column as the hierarchy level column inside the dataset metadata
	public IDataSet normalizeDataset(IDataSet dataset,
			Map<String, HierarchyLevel> hierarchiesColumnsToCheck) {
		
		IDataStore dataStore = dataset.getDataStore(); 

		
		MeasureCatalogue measureCatalogue = MeasureCatalogueSingleton.getMeasureCatologue();
		
		MetaModelWrapper metamodelWrapper = measureCatalogue.getMetamodelWrapper();
		
		for (Map.Entry<String, HierarchyLevel> entry : hierarchiesColumnsToCheck.entrySet()){
			 String columnName = entry.getKey();
			 HierarchyLevel hierarchyLevel = entry.getValue();
			 if (hierarchyLevel.isValidEntry()){
				String hierarchyName = hierarchyLevel.getHierarchy_name();
			    String hierarchyLevelName = hierarchyLevel.getLevel_name();
			    if (hierarchyName.equalsIgnoreCase(GEO_HIERARCHY_NAME)){
		    		HierarchyWrapper hierarchy = metamodelWrapper.getHierarchy(GEO_HIERARCHY_NAME);
		    		if (hierarchy != null){
		    			if (hierarchy.getName().equalsIgnoreCase(hierarchyName)){
		    				Level level = hierarchy.getLevel(hierarchyLevelName);
		    				if (level != null){
		    					String levelName = level.getName();
		    					
		    					//Check current values of the column set has hierarchy level in the file
		    					checkCurrentHierarchyLevel(metamodelWrapper,hierarchy, dataset, dataStore,levelName,columnName);
		    				}
		    			}
		    		}
			    }	
			 }
		}
		
		return dataset;
		
	}
	
	/*
	 * This method check if the current Hierarchy level has the values compatible with the Hierarchy Level values specified on the validation model
	 */
	public void checkCurrentHierarchyLevel(MetaModelWrapper metaModelWrapper, HierarchyWrapper hierarchy, IDataSet dataset, IDataStore datastoreToValidate, String levelName, String columnNameOnDataset ){
		Object fieldValue = null;

		//Get the first value of the datastore to validate
		Iterator it = datastoreToValidate.iterator();
		int columnIndex = datastoreToValidate.getMetaData().getFieldIndex(columnNameOnDataset); 
   		IRecord record = (IRecord)it.next();
    	IField field = record.getFieldAt(columnIndex);
    	fieldValue = field.getValue();
    	
		//then check if the value is ammissible for the Level members (default values used as identifiers values)
		IDataStore dataStoreLevel = hierarchy.getMembers(levelName); //return a dataStore with one column only
		Set<String> admissibleValues = dataStoreLevel.getFieldDistinctValuesAsString(0);    	
		if(fieldValue != null)  {
			if (admissibleValues.contains(fieldValue)){
				 //current column has identifier values, no changes needed
			} else {
				//otherwise check the values on the siblings columns (if any)
				SiblingsFileWrapper siblingsFile = metaModelWrapper.getSiblingsFileWrapper();
				if (siblingsFile != null){
					List<String> siblingsColumnsNames = siblingsFile.getLevelSiblings(GEO_DIMENSION_NAME, GEO_HIERARCHY_NAME, levelName);
					if (!siblingsColumnsNames.isEmpty()){
						for (String siblingColumnName : siblingsColumnsNames){
							IDataStore dataStoreSibling = hierarchy.getSiblingValues(siblingColumnName);
							Set<String> admissibleValuesSibling  = dataStoreSibling.getFieldDistinctValuesAsString(0);
							if (admissibleValuesSibling.contains(fieldValue)){
								//found the current sibling column, we need to add corresponding values for this sibling inside the file

								//modify file and set dataset metadata
								modifyFileDataset(dataset, datastoreToValidate,hierarchy, levelName, siblingColumnName, columnNameOnDataset );
								break;
							}
						}
					}
				}
			}
		}
	}
	
	//add values from the levelName column to the corresponding sibling column values inside the file
	public void modifyFileDataset(IDataSet dataset, IDataStore datastore, HierarchyWrapper hierarchy, String levelName, String siblingColumnName, String columnNameOnDataset){
		if (dataset instanceof FileDataSet){
			FileDataSet fileDataSet = (FileDataSet)dataset;
			FileDataProxy fileDataProxy = fileDataSet.getDataProxy();
			String filePath = fileDataProxy.getCompleteFilePath();

			File datasetFile = new File(filePath);		
			
			if (datasetFile.exists()){
				String fileExtension = FilenameUtils.getExtension(filePath);
				if (fileExtension.equalsIgnoreCase("XLS")){
					//Modifying an Excel file
					logger.debug("Normalizing dataset file [XLS]: "+filePath);
					modifyXLSFile( dataset, datasetFile, hierarchy, levelName, siblingColumnName,columnNameOnDataset );
					
				} else if (fileExtension.equalsIgnoreCase("CSV")){
					//Modifying a CSV file
					logger.debug("Normalizing dataset file [CSV]: "+filePath);
					modifyCSVFile( dataset, datasetFile, hierarchy, levelName, siblingColumnName,columnNameOnDataset );
				}
			}

		}
	}
	
	public void modifyXLSFile(IDataSet dataset, File datasetFile,HierarchyWrapper hierarchy, String levelName, String siblingColumnName, String columnNameOnDataset ){
		Map<Object, Object> levelSiblingsValue = hierarchy.getMembersAndSibling(levelName, siblingColumnName);
		
		XLSFileNormalizer xlsFileNormalizer = new XLSFileNormalizer(datasetFile, levelSiblingsValue, columnNameOnDataset, levelName);
		
		try{
			//Get configuration options (for the parsers)
			String configuration = dataset.getConfiguration();
			if (configuration != null){
				configuration = JSONUtils.escapeJsonString(configuration);
				JSONObject jsonConf  = ObjectUtils.toJSONObject(configuration);	
				
				if (jsonConf.has(XSL_FILE_SKIP_ROWS)){
					String skipRows = jsonConf.getString(XSL_FILE_SKIP_ROWS);
					xlsFileNormalizer.setSkipRows(skipRows);
				}
				if (jsonConf.has(XSL_FILE_LIMIT_ROWS)){
					String limitRows = jsonConf.getString(XSL_FILE_LIMIT_ROWS);
					xlsFileNormalizer.setLimitRows(limitRows);
				}
				if (jsonConf.has(XSL_FILE_SHEET_NUMBER)){
					String sheetNumber = jsonConf.getString(XSL_FILE_SHEET_NUMBER);
					xlsFileNormalizer.setXslSheetNumber(sheetNumber);
				}
			}
		} catch (JSONException e){
			logger.debug("Error reading JSON of dataset configuration");
			throw new SpagoBIRuntimeException("Error reading JSON of dataset configuration", e);
		}
		
		//call normalization on file ***********************************
		xlsFileNormalizer.normalizeFile();
		
		//Get new column name and type for adding new metadata to the file
		String newColumnName = xlsFileNormalizer.getNewColumnName();
		String newColumnType = xlsFileNormalizer.getNewColumnType();
		
		//Modify Dataset Metadata **************************************
		
		modifyDatasetMetadata(dataset, columnNameOnDataset, levelName, newColumnName, newColumnType);
		
	}
	
	public void modifyCSVFile(IDataSet dataset, File datasetFile,HierarchyWrapper hierarchy, String levelName, String siblingColumnName, String columnNameOnDataset ){
		Map<Object, Object> levelSiblingsValue = hierarchy.getMembersAndSibling(levelName, siblingColumnName);
		
		CSVFileNormalizer csvFileNormalizer = new CSVFileNormalizer(datasetFile, levelSiblingsValue, columnNameOnDataset, levelName);

		try{
			//Get configuration options (for the parsers)
			String configuration = dataset.getConfiguration();
			if (configuration != null){
				configuration = JSONUtils.escapeJsonString(configuration);
				JSONObject jsonConf  = ObjectUtils.toJSONObject(configuration);	
				
				if (jsonConf.has(CSV_FILE_DELIMITER_CHARACTER)){
					String csvDelimiter = jsonConf.getString(CSV_FILE_DELIMITER_CHARACTER);
					csvFileNormalizer.setCsvDelimiter(csvDelimiter);
				}
				if (jsonConf.has(CSV_FILE_QUOTE_CHARACTER)){
					String csvQuote = jsonConf.getString(CSV_FILE_QUOTE_CHARACTER);
					csvFileNormalizer.setCsvQuote(csvQuote);
				}
				if(jsonConf.has(CSV_FILE_ENCODING)){
					String csvEncoding = jsonConf.getString(CSV_FILE_ENCODING);
					csvFileNormalizer.setCsvEncoding(csvEncoding);
				}
				
			}
		} catch (JSONException e){
			logger.debug("Error reading JSON of dataset configuration");
			throw new SpagoBIRuntimeException("Error reading JSON of dataset configuration", e);
		}
		
		//call normalization on file ***********************************
		csvFileNormalizer.normalizeFile();		
		
		//Get new column name and type for adding new metadata to the file
		String newColumnName = csvFileNormalizer.getNewColumnName();
		String newColumnType = csvFileNormalizer.getNewColumnType();
		
		//Modify Dataset Metadata **************************************
		modifyDatasetMetadata(dataset, columnNameOnDataset, levelName, newColumnName, newColumnType);
	}
	
	public void modifyDatasetMetadata(IDataSet dataset, String columnNameOnDataset, String levelName, String newColumnName, String newColumnType){
		//Search and remove hierarchy properties already set on a column for Geo Hierarchy
		IMetaData dataStoreMetaData = dataset.getMetadata();
		for (int i = 0; i < dataStoreMetaData.getFieldCount(); i++) {
			IFieldMetaData fieldMetaData=dataStoreMetaData.getFieldMeta(i);
			String name = fieldMetaData.getName();
			if (name.equals(columnNameOnDataset)){		
				fieldMetaData.deleteProperty("hierarchy");
				fieldMetaData.deleteProperty("hierarchy_level");		
				break;
			}
		}
		//add metadata for new column
		IFieldMetaData newFieldMetaData = new FieldMetadata();
		newFieldMetaData.setName(newColumnName);
		Class type = null;
		try {
			type = Class.forName(newColumnType);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		newFieldMetaData.setType(type);
		newFieldMetaData.setProperty("hierarchy", GEO_HIERARCHY_NAME);
		newFieldMetaData.setProperty("hierarchy_level", levelName);
		dataStoreMetaData.addFiedMeta(newFieldMetaData);
		
		//Set the new metadata to the dataset
		DatasetMetadataParser dsp = new DatasetMetadataParser();
		String dsMetadata = dsp.metadataToXML(dataStoreMetaData);
		dataset.setDsMetadata(dsMetadata);
	}
	
	
	

}
