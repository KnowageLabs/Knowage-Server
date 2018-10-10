/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.filter;

import it.eng.spagobi.meta.model.ModelObject;
import it.eng.spagobi.meta.model.physical.PhysicalTable;

import java.util.List;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class PhysicalTableFilter implements IModelObjectFilter{

	List<PhysicalTable> tablesTrue;
	public PhysicalTableFilter(List<PhysicalTable> tablesToMantain){
		tablesTrue = tablesToMantain;
	}
	@Override
	public boolean filter(ModelObject o) {
		if (tablesTrue.contains((PhysicalTable)o))
			return false;
		else
			return true;
	}		
}
