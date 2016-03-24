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
package it.eng.knowage.meta.model.validator;

import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.business.BusinessIdentifier;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.olap.OlapModel;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalModel;
import it.eng.knowage.meta.model.physical.PhysicalTable;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class ModelExtractor {
	public static Model getModel(ModelObject o) {
		Model model;

		model = null;

		if (o == null)
			return null;

		if (o instanceof Model) {
			model = (Model) o;
		} else if (o instanceof BusinessModel) {
			model = ((BusinessModel) o).getParentModel();
		} else if (o instanceof BusinessColumnSet) {
			model = getModel(((BusinessColumnSet) o).getModel());
		} else if (o instanceof BusinessColumn) {
			model = getModel(((BusinessColumn) o).getTable());
		} else if (o instanceof BusinessIdentifier) {
			model = getModel(((BusinessIdentifier) o).getModel());
		} else if (o instanceof BusinessRelationship) {
			model = getModel(((BusinessRelationship) o).getModel());
		} else if (o instanceof OlapModel) {
			model = ((OlapModel) o).getParentModel();
		} else if (o instanceof PhysicalModel) {
			model = ((PhysicalModel) o).getParentModel();
		} else if (o instanceof PhysicalTable) {
			model = getModel(((PhysicalTable) o).getModel());
		} else if (o instanceof PhysicalColumn) {
			model = getModel(((PhysicalColumn) o).getTable());
		}

		return model;
	}
}
