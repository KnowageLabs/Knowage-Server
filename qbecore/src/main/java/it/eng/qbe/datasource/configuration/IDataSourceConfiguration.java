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
import it.eng.qbe.model.structure.HierarchicalDimensionField;
import it.eng.qbe.model.structure.IModelRelationshipDescriptor;
import it.eng.qbe.model.structure.IModelViewEntityDescriptor;
import it.eng.qbe.model.structure.ModelCalculatedField;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Classes that implement this interface act as a proxy toward all the resources needed in order to create
 * a new IDataSource. All the methods in these class do not cache managed resources so calling them can involve
 * an heavy overhead due to IO. It's up to the caller to implement the proper caching system in order to minimize
 * the use of this interface.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface IDataSourceConfiguration {
	/**
	 *
	 * @return the name of the model. Must be unique. It is used as model identifier
	 * (ex. while caching model)
	 */
	String getModelName();


	/**
	 * Load the properties associated to the model. These properties are usually
	 * injected into the model by a proper implementation of interface IModelPropertiesInitializer.
	 *
	 * NOTE: model properties can be only read. It is not possible to change them and persist the
	 * modification
	 *
	 * @return the properties associated to the model.
	 */
	IModelProperties loadModelProperties();

	/**
	 * Facility method. It is equivalent to loadModelI18NProperties(null)
	 *
	 * @return the i18n properties associated to the model for the given locale
	 */
	IModelProperties loadModelI18NProperties();

	/**
	 * Load the properties associated to the model that are dependant to the locale (i.e. labels, tooltips).
	 * These properties are not injected into the model because the same datasource can be used at the same
	 * time by different objects  with different locales. Beacuse a datasource can consume a big amount of resources
	 * (i.e. heap space) is not praticable to create different instances of the same datasource for
	 * any available locale.
	 *
	 * @param locale The desired locale. If null load the default properties (i.e. label.properties)
	 *
	 * @return the i18n properties associated to the model for the given locale
	 */
	IModelProperties loadModelI18NProperties(Locale locale);

	/**
	 *
	 * @return the properties associated to the specific data source type. The numeber and type of
	 * these properties depend on the DataSource implementation
	 */
	Map<String,Object> loadDataSourceProperties();


	List<IModelViewEntityDescriptor> loadViews();
	List<IModelRelationshipDescriptor> loadRelationships();
	Map<String, List<ModelCalculatedField>> loadCalculatedFields();
	Map<String, HierarchicalDimensionField> loadHierarchicalDimension();
	void saveCalculatedFields(Map<String, List<ModelCalculatedField>> calculatedFields);

	/**
	 * Loads the inline functions
	 * @return the functions
	 */
	HashMap<String, InLineFunction> loadInLineFunctions(String dialect);
}
