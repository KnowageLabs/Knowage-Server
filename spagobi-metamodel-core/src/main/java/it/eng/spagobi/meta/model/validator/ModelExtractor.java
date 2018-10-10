/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.spagobi.meta.model.validator;

import it.eng.spagobi.meta.model.Model;
import it.eng.spagobi.meta.model.ModelObject;
import it.eng.spagobi.meta.model.business.BusinessColumn;
import it.eng.spagobi.meta.model.business.BusinessColumnSet;
import it.eng.spagobi.meta.model.business.BusinessIdentifier;
import it.eng.spagobi.meta.model.business.BusinessModel;
import it.eng.spagobi.meta.model.business.BusinessRelationship;
import it.eng.spagobi.meta.model.olap.OlapModel;
import it.eng.spagobi.meta.model.physical.PhysicalColumn;
import it.eng.spagobi.meta.model.physical.PhysicalModel;
import it.eng.spagobi.meta.model.physical.PhysicalTable;

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
