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
