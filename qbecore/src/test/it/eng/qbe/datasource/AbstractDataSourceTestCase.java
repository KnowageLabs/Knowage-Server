/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource;

import it.eng.qbe.AbstractQbeTestCase;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.model.structure.ModelEntity;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractDataSourceTestCase extends AbstractQbeTestCase {
	
	protected String modelName;
	protected String testEntityUniqueName;

	public void doTests() {
		doTestSmoke();
		//doTestLabelLocalization();
		//doTestTooltipLocalization();
		doTestStructure();
		doTestQuery();
	}
	
	public void doTestSmoke() {
		try {
			IModelStructure modelStructure = dataSource.getModelStructure();
			assertNotNull("Impossible to build modelStructure", modelStructure);
		} catch(Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void doTestLabelLocalization() {
		IModelProperties properties;
		String label;
		IModelEntity entity = dataSource.getModelStructure().getEntity(testEntityUniqueName);
		
		properties = dataSource.getModelI18NProperties(Locale.ITALIAN);
		label = properties.getProperty(entity, "label");
		assertTrue("[" + label + "] is not equal to [" + "Customer Italiano" + "]", "Customer Italiano".equals(label));
		
		properties = dataSource.getModelI18NProperties(Locale.ENGLISH);
		label = properties.getProperty(entity, "label");
		assertTrue("[" + label + "] is not equal to [" + "Customer Inglese" + "]", "Customer Inglese".equals(label));
		
		properties = dataSource.getModelI18NProperties(Locale.JAPANESE);
		label = properties.getProperty(entity, "label");
		assertTrue("[" + label + "] is not equal to [" + "Customer Default" + "]", "Customer Default".equals(label));
	}
	
	public void doTestTooltipLocalization() {
		IModelProperties properties;
		String tooltip;
		IModelEntity entity = dataSource.getModelStructure().getEntity(testEntityUniqueName);
		
		properties = dataSource.getModelI18NProperties(Locale.ITALIAN);
		tooltip = properties.getProperty(entity, "tooltip");
		assertTrue("[" + tooltip + "] is not equal to [" + "Customer Italiano" + "]", "Customer Italiano".equals(tooltip));
		
		properties = dataSource.getModelI18NProperties(Locale.ENGLISH);
		tooltip = properties.getProperty(entity, "tooltip");
		assertTrue("[" + tooltip + "] is not equal to [" + "Customer Inglese" + "]", "Customer Inglese".equals(tooltip));
		
		properties = dataSource.getModelI18NProperties(Locale.JAPANESE);
		tooltip = properties.getProperty(entity, "tooltip");
		assertTrue("[" + tooltip + "] is not equal to [" + "Customer Default" + "]", "Customer Default".equals(tooltip));
	}
	
	public void doTestStructure() {
		try {
			IModelStructure modelStructure = dataSource.getModelStructure();
			assertNotNull(modelStructure);
			List entities = modelStructure.getRootEntities(modelName);
			Set<String> names = modelStructure.getModelNames();
			assertNotNull(entities);
			assertTrue(entities.size() > 0);
		} catch(Throwable t) {
			t.printStackTrace();
			fail();
		}
	}
	
	public void doTestQuery() {
		Query query = new Query();
		
		IModelStructure modelStructure = dataSource.getModelStructure();
		List entities = modelStructure.getRootEntities(modelName);
		if(entities.size() > 0) {
			ModelEntity entity = (ModelEntity)entities.get(0);
			List fields = entity.getAllFields();
			for(int i = 0; i < fields.size(); i++) {
				IModelField field = (IModelField)fields.get(i);

				query.addSelectFiled(field.getUniqueName(), null, field.getName(), true, true, false, null, null);			
			}
		}
		
		IStatement statement = dataSource.createStatement(query);
		IDataSet datsSet = QbeDatasetFactory.createDataSet(statement);
		
		try {
			datsSet.loadData();
		} catch (Throwable e) {
			e.printStackTrace();
			fail();
		}
		
		IDataStore dataStore = datsSet.getDataStore();
		
		assertNotNull(dataStore);
		assertTrue(dataStore.getRecordsCount() > 0);
	}
}
