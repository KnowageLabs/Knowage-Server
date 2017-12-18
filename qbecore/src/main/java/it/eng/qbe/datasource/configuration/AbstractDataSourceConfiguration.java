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
package it.eng.qbe.datasource.configuration;

import it.eng.qbe.datasource.configuration.dao.fileimpl.InLineFunctionsDAOFileImpl.InLineFunction;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.properties.SimpleModelProperties;
import it.eng.qbe.model.structure.HierarchicalDimensionField;
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
	public Map<String, HierarchicalDimensionField> loadHierarchicalDimension() {
		return new HashMap<String, HierarchicalDimensionField>();
	}
}
