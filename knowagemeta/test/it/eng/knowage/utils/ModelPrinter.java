/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
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
