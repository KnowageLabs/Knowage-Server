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
