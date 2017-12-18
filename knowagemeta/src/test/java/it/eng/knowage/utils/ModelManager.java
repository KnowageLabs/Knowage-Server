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
package it.eng.knowage.utils;

import it.eng.knowage.meta.initializer.BusinessModelInitializer;
import it.eng.knowage.meta.initializer.descriptor.BusinessViewInnerJoinRelationshipDescriptor;
import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.BusinessView;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalModel;
import it.eng.knowage.meta.model.physical.PhysicalTable;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ModelManager {
	Model model;
	PhysicalModel physicalModel;
	BusinessModel businessModel;
	BusinessModelInitializer initializer;

	public ModelManager(Model model) {
		this.model = model;
		physicalModel = model.getPhysicalModels().get(0);
		businessModel = model.getBusinessModels().get(0);
		initializer = new BusinessModelInitializer();
	}

	public BusinessView createView(BusinessTable businessTable, BusinessViewInnerJoinRelationshipDescriptor innerJoinRelationshipDescriptor) {
		return initializer.upgradeBusinessTableToBusinessView(businessTable, innerJoinRelationshipDescriptor);
	}

	public void addBusinessTable(PhysicalTable physicalTable) {
		initializer.addTable(physicalTable, businessModel, false);
	}

	public void addBusinessColumn(PhysicalColumn physicalColumn, BusinessColumnSet businessColumnSet) {
		initializer.addColumn(physicalColumn, businessColumnSet);
	}
}
