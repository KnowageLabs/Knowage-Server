/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.utils;

import org.apache.log4j.Logger;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

public class FilterZeroStandardCategoryItemLabelGenerator  extends StandardCategoryItemLabelGenerator {

	/**
	 * This class is used to generate value labels over bars and lines, while filtering 0 values.
	 * 
	 */
	
	private static transient Logger logger=Logger.getLogger(FilterZeroStandardCategoryItemLabelGenerator.class);

	@Override
	public String generateLabel(CategoryDataset dataset, int row, int column) {
		String result=super.generateLabel(dataset, row, column);
		// filter 0 or 0.0 values
		if(result.equalsIgnoreCase("0") || result.equalsIgnoreCase("0.0")){
			return null;
		}
		else return result;
		
	}

	

}
