/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.configuration;

import it.eng.qbe.datasource.configuration.dao.fileimpl.InLineFunctionsDAOFileImpl.InLineFunction;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.structure.IModelRelationshipDescriptor;
import it.eng.qbe.model.structure.IModelViewEntityDescriptor;
import it.eng.qbe.model.structure.ModelCalculatedField;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class allow to manage the configuration in memory. Nothing is loaded. It is the caller to injects 
 * values at runtime into the configuration. 
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class InMemoryDataSourceConfiguration extends AbstractDataSourceConfiguration {

	IModelProperties modelProperties;
	
	IModelProperties modelI18NProperties;
	Map<Locale, IModelProperties> i18nMap;
	
	Map<String, Object> dataSourceProperties;
	List<IModelRelationshipDescriptor> realtionships;
	List<IModelViewEntityDescriptor> views;
	Map<String, List<ModelCalculatedField>> calculatedFields;
	
	List inLineFunctions;
	
	public InMemoryDataSourceConfiguration(String modelName) {
		super(modelName);
	}

	public IModelProperties loadModelProperties() {
		if(modelProperties == null) modelProperties = super.loadModelProperties();
		return modelProperties;
	}

	public IModelProperties loadModelI18NProperties() {
		if(modelI18NProperties == null) modelI18NProperties = super.loadModelI18NProperties();
		return modelI18NProperties;
	}

	public IModelProperties loadModelI18NProperties(Locale locale) {
		if(i18nMap == null) {
			i18nMap = new HashMap<Locale, IModelProperties>();
		}
		
		IModelProperties p = i18nMap.get(locale);
		if(p == null) {
			i18nMap.put(locale, super.loadModelI18NProperties(locale));
		}
		return i18nMap.get(locale);
	}

	public Map<String, Object> loadDataSourceProperties() {
		if(dataSourceProperties == null) dataSourceProperties = super.loadDataSourceProperties();
		return dataSourceProperties;
	}

	public List<IModelViewEntityDescriptor> loadViews() {
		if(views == null) views = super.loadViews();
		return views;
	}
	
	public List<IModelRelationshipDescriptor> loadRelationships() {
		if(realtionships == null) realtionships = super.loadRelationships();
		return realtionships;
	}

	public Map<String, List<ModelCalculatedField>> loadCalculatedFields() {
		if(calculatedFields == null) calculatedFields = super.loadCalculatedFields();
		return calculatedFields;
	}

	public void saveCalculatedFields(
			Map<String, List<ModelCalculatedField>> calculatedFields) {
		// do nothing	
		super.saveCalculatedFields(calculatedFields);
	}
	
	public HashMap<String, InLineFunction> loadInLineFunctions(String dialect) {
		// do nothing	
		return super.loadInLineFunctions(dialect);
	}

}
