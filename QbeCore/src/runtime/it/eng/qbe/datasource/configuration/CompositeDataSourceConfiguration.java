/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.configuration;

import it.eng.qbe.datasource.configuration.dao.fileimpl.InLineFunctionsDAOFileImpl.InLineFunction;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.properties.SimpleModelProperties;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelRelationshipDescriptor;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.model.structure.IModelViewEntityDescriptor;
import it.eng.qbe.model.structure.ModelCalculatedField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class CompositeDataSourceConfiguration implements IDataSourceConfiguration {

	String modelName;
	Map<String,Object> dataSourceProperties;
	
	List<IDataSourceConfiguration> subConfigurations;
	
	public CompositeDataSourceConfiguration(String modelName, Map<String,Object> dataSourceProperties) {
		this.modelName = modelName;
		this.dataSourceProperties = dataSourceProperties;
		
		this.subConfigurations = new ArrayList<IDataSourceConfiguration>();
	}
	
	public CompositeDataSourceConfiguration() {
		this(null);
	}
	
	public CompositeDataSourceConfiguration(String modelName) {
		this.modelName = modelName;
		this.dataSourceProperties = new HashMap<String,Object>();
		
		this.subConfigurations = new ArrayList<IDataSourceConfiguration>();
	}
	
	public String getModelName() {
		return modelName;
	}
	
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.configuration.IDataSourceConfiguration#getDataSourceProperties()
	 */
	public Map<String, Object> loadDataSourceProperties() {
		return dataSourceProperties;
	}
	
	public void addSubConfiguration(IDataSourceConfiguration configuration){
		subConfigurations.add(configuration);
	}
	
	public List<IDataSourceConfiguration> getSubConfigurations(){
		return subConfigurations;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.configuration.IDataSourceConfiguration#getModelProperties()
	 */
	public IModelProperties loadModelProperties() {
		SimpleModelProperties properties = new SimpleModelProperties();
		Iterator<IDataSourceConfiguration> it = subConfigurations.iterator();
		while (it.hasNext()) {
			IDataSourceConfiguration configuration = it.next();
			IModelProperties props = configuration.loadModelProperties();
			properties.putAll(props);
		}
		
		return properties;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.configuration.IDataSourceConfiguration#getModelLabels()
	 */
	public SimpleModelProperties loadModelI18NProperties() {
		return loadModelI18NProperties(null);
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.configuration.IDataSourceConfiguration#getModelLabels(java.util.Locale)
	 */
	public SimpleModelProperties loadModelI18NProperties(Locale locale) {
		SimpleModelProperties properties = new SimpleModelProperties();
		Iterator<IDataSourceConfiguration> it = subConfigurations.iterator();
		while (it.hasNext()) {
			IDataSourceConfiguration subModelConfiguration = it.next();
			IModelProperties subModelProperties = subModelConfiguration.loadModelI18NProperties(locale);
			properties.putAll(subModelProperties);
		}
		
		return properties;
	}
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.configuration.IDataSourceConfiguration#getCalculatedFields()
	 */
	public Map<String, List<ModelCalculatedField>> loadCalculatedFields() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.configuration.IDataSourceConfiguration#setCalculatedFields(java.util.Map)
	 */
	public void saveCalculatedFields(Map<String, List<ModelCalculatedField>> calculatedFields) {
		
		Iterator<List<ModelCalculatedField>> it = calculatedFields.values().iterator();
		if(!it.hasNext()) return; // if NO calculated fields to add return
		IModelStructure structure = it.next().get(0).getStructure();
		
		Iterator<IDataSourceConfiguration> subConfigurationIterator = subConfigurations.iterator();
		while(it.hasNext()) {
			IDataSourceConfiguration subConfiguration = subConfigurationIterator.next();
			Map<String, List<ModelCalculatedField>> datamartCalcultedField = getCalculatedFieldsForDatamart(structure, subConfiguration.getModelName());

			subConfiguration.saveCalculatedFields(datamartCalcultedField);
		}
	

	}
	
	/**
	 * The input map contains all the calculated fields defined into the entire datamart model structure. 
	 * This method returns the calculated field defined for a single datamart (used in case of composite datasource, i.e. more than 1 datamart).
	 * @param structure The datamart model structure
	 * @param calculatedFields All the calculated fields defined into the entire datamart model structure
	 * @param datamartName The datamart for which the calculated fields should be retrieved
	 * @return the calculated field defined for the specified datamart 
	 */
	private Map<String, List<ModelCalculatedField>> getCalculatedFieldsForDatamart(IModelStructure structure, String datamartName) {
		Map<String, List<ModelCalculatedField>> toReturn = new HashMap<String, List<ModelCalculatedField>>();
		Map<String, List<ModelCalculatedField>> calculatedFields = structure.getCalculatedFields();
		Set keys = calculatedFields.keySet();
		Iterator keysIt = keys.iterator();
		while (keysIt.hasNext()) {
			String entityUniqueName = (String) keysIt.next();
			IModelEntity dataMartEntity = structure.getEntity(entityUniqueName);
			IModelEntity dataMartRootEntity = dataMartEntity.getRoot();
			List rootEntities = structure.getRootEntities(datamartName);
			if (rootEntities.contains(dataMartRootEntity)) {
				toReturn.put(entityUniqueName, calculatedFields.get(entityUniqueName));
			}
		}
		return toReturn;
	}

	public List<IModelRelationshipDescriptor> loadRelationships() {
		List<IModelRelationshipDescriptor> relationships = new ArrayList<IModelRelationshipDescriptor>();
		for(IDataSourceConfiguration subConfiguration: subConfigurations) {
			relationships.addAll( subConfiguration.loadRelationships() );
		}
		return relationships;
	}
	
	public List<IModelViewEntityDescriptor> loadViews() {
		List<IModelViewEntityDescriptor> views = new ArrayList<IModelViewEntityDescriptor>();
		for(IDataSourceConfiguration subConfiguration: subConfigurations) {
			views.addAll( subConfiguration.loadViews() );
		}
		return views;
	}

	public HashMap<String, InLineFunction> loadInLineFunctions(String dialect) {
		HashMap<String, InLineFunction> functions = new HashMap<String, InLineFunction>();
		functions =	subConfigurations.get(0).loadInLineFunctions(dialect);
		
		return functions;
	}
}
