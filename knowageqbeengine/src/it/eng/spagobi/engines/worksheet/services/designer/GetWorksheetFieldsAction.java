/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.services.designer;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.dataset.DataSetDataSource;
import it.eng.qbe.statement.sql.SQLDataSet;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.worksheet.WorksheetEngineInstance;
import it.eng.spagobi.engines.worksheet.services.AbstractWorksheetEngineAction;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class GetWorksheetFieldsAction  extends AbstractWorksheetEngineAction {	

	private static final long serialVersionUID = -5874137232683097175L;

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(GetWorksheetFieldsAction.class);

	// PROPERTIES TO LOOK FOR INTO THE FIELDS
	public static final String PROPERTY_VISIBLE = "visible";
	public static final String PROPERTY_CALCULATED_EXPERT = "calculatedExpert";
	public static final String PROPERTY_IS_SEGMENT_ATTRIBUTE = "isSegmentAttribute";
	public static final String PROPERTY_IS_MANDATORY_MEASURE = "isMandatoryMeasure";
	public static final String PROPERTY_AGGREGATION_FUNCTION = "aggregationFunction";

	public void service(SourceBean request, SourceBean response)  {

		JSONObject resultsJSON;

		logger.debug("IN");

		try {		
			super.service(request, response);	

			WorksheetEngineInstance engineInstance = this.getEngineInstance();
			Assert.assertNotNull(engineInstance, "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");
			IDataSet dataset = engineInstance.getDataSet();
			Assert.assertNotNull(dataset, "The engine instance is missing the dataset!!");
			IMetaData metadata = dataset.getMetadata();
			Assert.assertNotNull(metadata, "No metadata retrieved by the dataset");

			JSONArray fieldsJSON = writeFields(metadata);
			logger.debug("Metadata read:");
			logger.debug(fieldsJSON);
			resultsJSON = new JSONObject();
			resultsJSON.put("results", fieldsJSON);
			if (dataset instanceof SQLDataSet) {
				insertDsMetadataIntoJson(resultsJSON, dataset);
			}

			try {
				writeBackToClient( new JSONSuccess( resultsJSON ) );
			} catch (IOException e) {
				throw new SpagoBIEngineServiceException(getActionName(), "Impossible to write back the responce to the client [" + resultsJSON.toString(2)+ "]", e);
			}

		} catch(Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {			
			logger.debug("OUT");
		}
	}

	private void insertDsMetadataIntoJson(JSONObject resultsJSON, IDataSet dataSetSQL) {

		IDataSource dataSource = ((SQLDataSet) dataSetSQL).getStatement().getDataSource();
		if (dataSource instanceof DataSetDataSource) {
			try {
				List<IDataSet> datasets = ((DataSetDataSource) dataSource).getDatasets();
				if (datasets.size() == 1) {
					MetaData metadata = cleanAndGetMetadata((MetaData) dataSetSQL.getMetadata(), datasets.get(0).getDsMetadata());
					String metadataJson = JsonConverter.objectToJson(metadata, MetaData.class);
					resultsJSON.put("metadata", metadataJson);
				}
			} catch (Throwable t) {
				throw new SpagoBIEngineServiceException(getActionName(), "Impossible to get metadata from the original dataset", t);

			}
		}
	}

	private MetaData cleanAndGetMetadata(MetaData formMetadata, String xmlMetadata) throws Throwable {

		MetaData metadata = new MetaData();

		DatasetMetadataParser dsMetaParser = new DatasetMetadataParser();
		MetaData dsMetadata = (MetaData) dsMetaParser.xmlToMetadata(xmlMetadata);
		Iterator<Object> formIterator = formMetadata.getFieldsMeta().iterator();
		while (formIterator.hasNext()) {
			IFieldMetaData field = (IFieldMetaData) formIterator.next();
			int index = dsMetadata.getFieldIndex(field.getAlias());
			field.setType(dsMetadata.getFieldMeta(index).getType());
			field.setProperties(new HashMap());
		}
		return formMetadata;
	}

	public JSONArray writeFields(IMetaData metadata) throws Exception {

		// field's meta
		JSONArray fieldsMetaDataJSON = new JSONArray();
		
		List<JSONObject> attributesList = new ArrayList<JSONObject>();
		List<JSONObject> measuresList = new ArrayList<JSONObject>();

		int fieldCount = metadata.getFieldCount();
		logger.debug("Number of fields = " + fieldCount);
		Assert.assertTrue(fieldCount > 0, "Dataset has no fields!!!");

		for (int i = 0; i < fieldCount; i++) {
			IFieldMetaData fieldMetaData = metadata.getFieldMeta(i);
			Assert.assertNotNull(fieldMetaData, "Field metadata for position " + i + " not found.");
			logger.debug("Evaluating field with name [" + fieldMetaData.getName() + "], alias [" + fieldMetaData.getAlias() + "] ...");

			Boolean isCalculatedExpert= (Boolean) fieldMetaData.getProperty(PROPERTY_CALCULATED_EXPERT);
			
			if(isCalculatedExpert!=null && isCalculatedExpert){
				logger.debug("The field is a expert calculated field so we skip it");
				//continue;
			}
			
			Object propertyRawValue = fieldMetaData.getProperty(PROPERTY_VISIBLE);
			logger.debug("Read property " + PROPERTY_VISIBLE + ": its value is [" + propertyRawValue + "]");
			
			if (propertyRawValue != null && !propertyRawValue.toString().equals("")
					&& (Boolean.parseBoolean(propertyRawValue.toString()) == false)) {
				logger.debug("The field is not visible");
				continue;
			} else {
				logger.debug("The field is visible");
			}

			String fieldName = getFieldName(fieldMetaData);
			String fieldHeader = getFieldAlias(fieldMetaData);

			JSONObject fieldMetaDataJSON = new JSONObject();
			fieldMetaDataJSON.put("id", fieldName);						
			fieldMetaDataJSON.put("alias", fieldHeader);

			FieldType type = fieldMetaData.getFieldType();
			logger.debug("The field type is " + type.name());
			switch (type) {
			case ATTRIBUTE:
				Object isSegmentAttributeObj = fieldMetaData.getProperty(PROPERTY_IS_SEGMENT_ATTRIBUTE);
				logger.debug("Read property " + PROPERTY_IS_SEGMENT_ATTRIBUTE + ": its value is [" + propertyRawValue + "]");
				String attributeNature = (isSegmentAttributeObj != null
						&& (Boolean.parseBoolean(isSegmentAttributeObj.toString())==true)) ? "segment_attribute" : "attribute";
				
				logger.debug("The nature of the attribute is recognized as " + attributeNature);
				fieldMetaDataJSON.put("nature", attributeNature);
				fieldMetaDataJSON.put("funct", AggregationFunctions.NONE);
				fieldMetaDataJSON.put("iconCls", attributeNature);
				break;
			case MEASURE:
				Object isMandatoryMeasureObj = fieldMetaData.getProperty(PROPERTY_IS_MANDATORY_MEASURE);
				logger.debug("Read property " + PROPERTY_IS_MANDATORY_MEASURE + ": its value is [" + isMandatoryMeasureObj + "]");
				String measureNature = (isMandatoryMeasureObj != null
						&& (Boolean.parseBoolean(isMandatoryMeasureObj.toString())==true)) ? "mandatory_measure" : "measure";
				logger.debug("The nature of the measure is recognized as " + measureNature);
				fieldMetaDataJSON.put("nature", measureNature);
				String aggregationFunction = (String) fieldMetaData.getProperty(PROPERTY_AGGREGATION_FUNCTION);
				logger.debug("Read property " + PROPERTY_AGGREGATION_FUNCTION + ": its value is [" + aggregationFunction + "]");
				fieldMetaDataJSON.put("funct", AggregationFunctions.get(aggregationFunction).getName());
				fieldMetaDataJSON.put("iconCls", measureNature);
				String decimalPrecision= (String) fieldMetaData.getProperty(IFieldMetaData.DECIMALPRECISION);
				if(decimalPrecision!=null){
					fieldMetaDataJSON.put("precision", decimalPrecision);
				}else{
					fieldMetaDataJSON.put("precision", "2");
				}
				break;
			}
			
			if(type.equals(it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType.MEASURE)){
				measuresList.add(fieldMetaDataJSON);
			}
			else{
				attributesList.add(fieldMetaDataJSON);
			}
		}

		
		//  put first measures and only after attributes
		
		for (Iterator iterator = measuresList.iterator(); iterator.hasNext();) {
			JSONObject jsonObject = (JSONObject) iterator.next();
			fieldsMetaDataJSON.put(jsonObject);
		}	

		for (Iterator iterator = attributesList.iterator(); iterator.hasNext();) {
			JSONObject jsonObject = (JSONObject) iterator.next();
			fieldsMetaDataJSON.put(jsonObject);
		}	

		return fieldsMetaDataJSON;

	}

	protected String getFieldAlias(IFieldMetaData fieldMetaData) {
		String fieldAlias = fieldMetaData.getAlias() != null ? fieldMetaData.getAlias() : fieldMetaData.getName();
		return fieldAlias;
	}

	protected String getFieldName(IFieldMetaData fieldMetaData) {
		String fieldName = fieldMetaData.getName();
		return fieldName;
	}

}
