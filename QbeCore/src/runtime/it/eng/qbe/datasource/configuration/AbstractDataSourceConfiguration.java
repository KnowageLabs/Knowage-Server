/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.configuration;

import it.eng.qbe.datasource.configuration.dao.fileimpl.InLineFunctionsDAOFileImpl.InLineFunction;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.properties.SimpleModelProperties;
import it.eng.qbe.model.structure.IModelRelationshipDescriptor;
import it.eng.qbe.model.structure.IModelViewEntityDescriptor;
import it.eng.qbe.model.structure.ModelCalculatedField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * This is an abstract configuration. All the methods return an empty collection of the valid type
 * without performing any real load activity. This class can be used as a base class to implement
 * more complex configuration objects.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class AbstractDataSourceConfiguration implements IDataSourceConfiguration {

	String modelName;
	
	public AbstractDataSourceConfiguration(String modelName) {
		this.modelName = modelName;
	}
	public String getModelName() {
		return modelName;
	}

	public IModelProperties loadModelProperties() {
		return new SimpleModelProperties(new Properties());
	}

	public IModelProperties loadModelI18NProperties() {
		return new SimpleModelProperties(new Properties());
	}

	public IModelProperties loadModelI18NProperties(Locale locale) {
		return new SimpleModelProperties(new Properties());
	}

	public Map<String, Object> loadDataSourceProperties() {
		return new HashMap<String, Object>();
	}

	public List<IModelRelationshipDescriptor> loadRelationships() {
		return new ArrayList<IModelRelationshipDescriptor>();
	}
	
	public List<IModelViewEntityDescriptor> loadViews() {
		return new ArrayList<IModelViewEntityDescriptor>();
	}

	public Map<String, List<ModelCalculatedField>> loadCalculatedFields() {
		return new HashMap<String, List<ModelCalculatedField>>();
	}

	public void saveCalculatedFields(
			Map<String, List<ModelCalculatedField>> calculatedFields) {
		// do nothing
	}
	
	public HashMap<String, InLineFunction> loadInLineFunctions(String dialect){
		return new HashMap<String, InLineFunction>();
		
	}
}
