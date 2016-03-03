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

import it.eng.qbe.datasource.configuration.dao.ICalculatedFieldsDAO;
import it.eng.qbe.datasource.configuration.dao.IHierarchiesDAO;
import it.eng.qbe.datasource.configuration.dao.IInLineFunctionsDAO;
import it.eng.qbe.datasource.configuration.dao.IModelI18NPropertiesDAO;
import it.eng.qbe.datasource.configuration.dao.IModelPropertiesDAO;
import it.eng.qbe.datasource.configuration.dao.IRelationshipsDAO;
import it.eng.qbe.datasource.configuration.dao.IViewsDAO;
import it.eng.qbe.datasource.configuration.dao.fileimpl.InLineFunctionsDAOFileImpl.InLineFunction;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.properties.SimpleModelProperties;
import it.eng.qbe.model.structure.HierarchicalDimensionField;
import it.eng.qbe.model.structure.IModelRelationshipDescriptor;
import it.eng.qbe.model.structure.IModelViewEntityDescriptor;
import it.eng.qbe.model.structure.ModelCalculatedField;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DelegatingDataSourceConfiguration extends InMemoryDataSourceConfiguration {

	IModelI18NPropertiesDAO modelLabelsDAOFileImpl;
	IModelPropertiesDAO modelPropertiesDAO;
	ICalculatedFieldsDAO calculatedFieldsDAO;
	IInLineFunctionsDAO functionsDAO;
	IHierarchiesDAO hierarchiesDAO;

	IRelationshipsDAO relationshipsDAO;
	IViewsDAO viewsDAO;




	public DelegatingDataSourceConfiguration(String modelName) {
		super(modelName);
	}

	// ====================================================================
	// overrides
	// ====================================================================

	// datasource properties are managed in memory -> no delegation here
	// public Map<String, Object> loadDataSourceProperties() { ...

	@Override
	public IModelProperties loadModelProperties() {
		return modelPropertiesDAO.loadModelProperties();
	}

	@Override
	public IModelProperties loadModelI18NProperties() {
		return loadModelI18NProperties(null);
	}

	@Override
	public IModelProperties loadModelI18NProperties(Locale locale) {
		SimpleModelProperties properties = modelLabelsDAOFileImpl.loadProperties(locale);
		return properties;
	}

	@Override
	public Map loadCalculatedFields() {
		return calculatedFieldsDAO.loadCalculatedFields();
	}

	@Override
	public Map<String, HierarchicalDimensionField> loadHierarchicalDimension() {
		return hierarchiesDAO.loadHierarchicalDimensions();
	}

	@Override
	public void saveCalculatedFields(Map<String, List<ModelCalculatedField>> calculatedFields) {
		calculatedFieldsDAO.saveCalculatedFields( calculatedFields );
	}


	@Override
	public List<IModelRelationshipDescriptor> loadRelationships() {
		return relationshipsDAO.loadModelRelationships();
	}

	@Override
	public List<IModelViewEntityDescriptor> loadViews() {
		return viewsDAO.loadModelViews();
	}

	@Override
	public HashMap<String, InLineFunction> loadInLineFunctions(String dialect) {
		return functionsDAO.loadInLineFunctions(dialect);
	}

	// ====================================================================
	// Accessor methods
	// ====================================================================

	public IModelPropertiesDAO getModelPropertiesDAO() {
		return modelPropertiesDAO;
	}

	public void setModelPropertiesDAO(IModelPropertiesDAO modelPropertiesDAO) {
		this.modelPropertiesDAO = modelPropertiesDAO;
	}

	public ICalculatedFieldsDAO getCalculatedFieldsDAO() {
		return calculatedFieldsDAO;
	}

	public void setCalculatedFieldsDAO(ICalculatedFieldsDAO calculatedFieldsDAO) {
		this.calculatedFieldsDAO = calculatedFieldsDAO;
	}


	public void setHierarchiesDAO(IHierarchiesDAO hierarchiesDAO) {
		this.hierarchiesDAO = hierarchiesDAO;
	}

	public IHierarchiesDAO getHierarchiesDAO() {
		return hierarchiesDAO;
	}

	public IModelI18NPropertiesDAO getModelLabelsDAOFileImpl() {
		return modelLabelsDAOFileImpl;
	}

	public void setModelLabelsDAOFileImpl(
			IModelI18NPropertiesDAO modelLabelsDAOFileImpl) {
		this.modelLabelsDAOFileImpl = modelLabelsDAOFileImpl;
	}

	public IInLineFunctionsDAO getFunctionsDAO() {
		return functionsDAO;
	}

	public void setFunctionsDAO(IInLineFunctionsDAO functionsDAO) {
		this.functionsDAO = functionsDAO;
	}

	public IRelationshipsDAO getRelationshipsDAO() {
		return relationshipsDAO;
	}

	public void setRelationshipsDAO(IRelationshipsDAO relationshipsDAO) {
		this.relationshipsDAO = relationshipsDAO;
	}

	public IViewsDAO getViewsDAO() {
		return viewsDAO;
	}

	public void setViewsDAO(IViewsDAO viewsDAO) {
		this.viewsDAO = viewsDAO;
	}
}
