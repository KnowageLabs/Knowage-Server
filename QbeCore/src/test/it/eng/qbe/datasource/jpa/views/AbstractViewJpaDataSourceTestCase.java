/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.jpa.views;

import it.eng.qbe.datasource.AbstractDataSourceTestCase;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.model.structure.IModelViewEntityDescriptor;
import it.eng.qbe.model.structure.ModelViewEntity;

import java.util.List;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractViewJpaDataSourceTestCase extends AbstractDataSourceTestCase {
		
	public void doTests() {
		super.doTests();
		// add custom tests here
		doTestX();
	}
	
	public void doTestX() {
		try {
			List views = dataSource.getConfiguration().loadViews();
			assertNotNull(views);
			assertEquals(1, views.size());
			assertNotNull(views.get(0));
			assertTrue("Views conf cannot be an insatnce of [" + views.get(0).getClass().getName()  +"]", views.get(0) instanceof IModelViewEntityDescriptor);
			
			IModelStructure modelStructure = dataSource.getModelStructure();
			IModelEntity entity = modelStructure.getRootEntity(modelName, "it.eng.spagobi.meta.EmployeeClosure::EmployeeClosure");
			if(entity == null) dumpRootEntities(modelStructure);
			assertNotNull(entity);
			assertTrue(entity instanceof ModelViewEntity);
			List<IModelField> fields = entity.getAllFields();
			List<IModelField> keyFields = entity.getKeyFields();
			List<IModelField> normalFields = entity.getNormalFields();
			assertEquals(fields.size(), keyFields.size() + normalFields.size());
		} catch(Throwable t) {
			t.printStackTrace();
			fail();
		}
	}
	
	// disable localization tests
	public void doTestLabelLocalization() {}
	public void doTestTooltipLocalization() {}
}
