/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.qbe.model.structure.builder.dataset;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.dataset.DataSetDataSource;
import it.eng.qbe.model.properties.initializer.IModelStructurePropertiesInitializer;
import it.eng.qbe.model.properties.initializer.SimpleModelStructurePropertiesInitializer;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.model.structure.ModelCalculatedField;
import it.eng.qbe.model.structure.ModelStructure;
import it.eng.qbe.model.structure.builder.IModelStructureBuilder;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */
public class DataSetModelStructureBuilder implements IModelStructureBuilder {
	private static final String FIELD_TYPE_PROPERTY = "type";
	private static transient Logger logger = Logger.getLogger(DataSetModelStructureBuilder.class);

	private IDataSource dataSource;	
	IModelStructurePropertiesInitializer propertiesInitializer;

	public DataSetModelStructureBuilder(DataSetDataSource dataSource) {
		if(dataSource == null) {
			throw new IllegalArgumentException("DataSource parameter cannot be null");
		}
		setDataSource( dataSource );
		propertiesInitializer = new SimpleModelStructurePropertiesInitializer(dataSource);
	}
	

	public IModelStructure build() {
		ModelStructure modelStructure;
		String modelName;
		
		logger.debug("IN");
		
		try {
			modelStructure = new ModelStructure();
	
			modelName = getDataSource().getConfiguration().getModelName();
			Assert.assertNotNull(getDataSource(), "datasource cannot be null");	
			
			propertiesInitializer.addProperties(modelStructure);	
			Map calculatedFields = getDataSource().getConfiguration().loadCalculatedFields();
			if(calculatedFields!=null){
				modelStructure.setCalculatedFields(calculatedFields);
			}
					
			List<IDataSet> rootentities = getDataSource().getRootEntities();
			logger.debug("The root entity names are ["+ rootentities + "] ");
			
			for(int i=0;i< rootentities.size(); i++) {
				IDataSet entity =  rootentities.get(i);
				logger.debug("Adding entity type [" +entity.getName() + "] to model structure");
				addEntity(modelStructure, modelName, entity);	
				logger.info("Entity type [" +entity.getName() + "] succesfully added to model structure");
			}		
			
			
			logger.info("Model structure for model [" + modelName + "] succesfully built");
			
			return modelStructure;
		} 
		catch (Exception e){
			logger.debug("Impossible to build model structure", e);
			throw new RuntimeException("Impossible to build model structure", e);
		}
		catch(Throwable t) {
			throw new RuntimeException("Impossible to build model structure", t);
		
		} finally {
			logger.debug("OUT");
		}
	}
	


	
	private void addEntity (IModelStructure modelStructure, String modelName, IDataSet entity){

		String entityLabel = entity.getName();
		String entityName = entity.getTableNameForReading();
		
		IModelEntity dataMartEntity = modelStructure.addRootEntity(modelName, entityName, null, null, entityName);
		dataMartEntity.getProperties().put("label", entityLabel);
		
		// the query name is used when building the SQL statement, because we need to know the name of the actual table that contains the data of the dataset
		// i.e. we need to know the persistence table name
		dataMartEntity.getProperties().put("queryName", entity.getTableNameForReading());
		
		propertiesInitializer.addProperties(dataMartEntity);
		dataMartEntity.getProperties().put(FIELD_TYPE_PROPERTY,"cube");
		
		addNormalFields(dataMartEntity, entity);
		addCalculatedFields(dataMartEntity);		
	}


	/**
	 * This method adds the normal fields to the datamart entry structure
	 * @param dataMartEntity:  the datamart structure to complete
	 */
	public void addNormalFields(IModelEntity dataMartEntity, IDataSet entity) {		
		logger.debug("Adding the field "+dataMartEntity.getName());

		IMetaData datasetMetadata = entity.getMetadata();
		
		
		for(int i=0; i<datasetMetadata.getFieldCount(); i++) {
			IFieldMetaData fieldMetadata = datasetMetadata.getFieldMeta(i);
			addField(fieldMetadata, dataMartEntity, "");	
		}	
		
		
		logger.debug("Field "+dataMartEntity.getName()+" added");
	}
	

	private void addField(IFieldMetaData fieldMetadata, IModelEntity dataMartEntity, String keyPrefix){
		
//		String fieldName = fieldMetadata.getAlias();
//		if(fieldName==null || fieldName.equals("")){
//			fieldName = fieldMetadata.getName();
//		}
		String fieldUniqueName = fieldMetadata.getName();
		String alias = fieldMetadata.getAlias();
		if (alias == null || alias.equals("")) {
			alias = fieldMetadata.getName();
		}
		
		// TODO: SCALE
		int scale = 0;
		Object precision = (fieldMetadata.getProperty(IFieldMetaData.DECIMALPRECISION));
		
		int precisionInt =0;
		if(precision!=null){
			if(precision instanceof String){
				precisionInt = (new Integer((String)precision)).intValue();
			}else{
				precisionInt = (Integer)precision;
			}
		}

		IModelField datamartField = dataMartEntity.addNormalField(keyPrefix + fieldUniqueName);
		datamartField.setType(fieldMetadata.getType().getName());
		datamartField.setName(fieldUniqueName);
		datamartField.getProperties().put("label", alias);
		datamartField.setPrecision(precisionInt);
		datamartField.setLength(scale);
		propertiesInitializer.addProperties(datamartField);
		if(fieldMetadata.getFieldType()!=null){
			datamartField.getProperties().put(FIELD_TYPE_PROPERTY, (fieldMetadata.getFieldType().name()).toLowerCase());
		}
		
	}
	

	private void addCalculatedFields(IModelEntity dataMartEntity) {
		logger.debug("Adding the calculated field "+dataMartEntity.getName());
		List calculatedFileds;
		ModelCalculatedField calculatedField;
		
		calculatedFileds = dataMartEntity.getStructure().getCalculatedFieldsByEntity(dataMartEntity.getUniqueName()); 
		if(calculatedFileds != null) {
			for(int i = 0; i < calculatedFileds.size(); i++) {
				calculatedField = (ModelCalculatedField)calculatedFileds.get(i);
				dataMartEntity.addCalculatedField(calculatedField);
				propertiesInitializer.addProperties(calculatedField);
			}
		}
		logger.debug("Added the calculated field "+dataMartEntity.getName());
	}
	
	
	
	/**
	 * @return the DataSetDataSource
	 */
	public DataSetDataSource getDataSource() {
		return (DataSetDataSource)dataSource;
	}

	/**
	 * @param DataSetDataSource the datasource to set
	 */
	public void setDataSource(DataSetDataSource dataSource) {
		this.dataSource = dataSource;
	}


}