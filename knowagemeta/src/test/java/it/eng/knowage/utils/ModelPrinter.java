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

import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.physical.PhysicalModel;

/**
 * @author Andrea Gioia (andrea.gioi@eng.it)
 *
 */
public class ModelPrinter {
	public static void print(PhysicalModel model) {

	}

	public static void print(BusinessModel model) {
		for (int i = 0; i < model.getTables().size(); i++) {
			BusinessColumnSet t = model.getTables().get(i);
			System.out.println(i + " " + t.getName());
			for (int j = 0; j < t.getColumns().size(); j++) {
				System.out.println("  -  " + t.getColumns().get(j).getName());
			}
		}

		/*
		 * for(int i = 0; i < businessModel.getRelationships().size(); i++) { BusinessRelationship r = businessModel.getRelationships().get(i);
		 *
		 * System.out.println( "(" + r.getName() + ") " +r.getSourceTable().getName() + " -> " + r.getDestinationTable().getName() ); for(int j = 0; j <
		 * r.getSourceColumns().size(); j++) { System.out.println( "  -  " + r.getSourceColumns().get(j).getName() + " -> "+
		 * r.getDestinationColumns().get(j).getName() ); } }
		 */
	}
}
