/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.utils;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

/**
 * 
 * @author gavardi
 * This class is used to generate additiona label over bars
 *
 */

public class MyStandardCategoryItemLabelGenerator extends StandardCategoryItemLabelGenerator {

	HashMap catSerLabel=null;
	private static transient Logger logger=Logger.getLogger(MyStandardCategoryItemLabelGenerator.class);


	public MyStandardCategoryItemLabelGenerator() {
		super();
		catSerLabel=new HashMap();
	}

	public MyStandardCategoryItemLabelGenerator(HashMap catSerMap) {
		super();

		catSerLabel=catSerMap;
	}




	public MyStandardCategoryItemLabelGenerator(HashMap catSerMap,String labelFormat,
			DateFormat formatter) {
		super(labelFormat, formatter);
		catSerLabel=catSerMap;
	}

	public MyStandardCategoryItemLabelGenerator(HashMap catSerMap,String labelFormat,
			NumberFormat formatter, NumberFormat percentFormatter) {
		super(labelFormat, formatter, percentFormatter);
		catSerLabel=catSerMap;	}

	public MyStandardCategoryItemLabelGenerator(HashMap catSerMap,String labelFormat,
			NumberFormat formatter) {
		super(labelFormat, formatter);
		catSerLabel=catSerMap;	}

	public String generateLabel(CategoryDataset dataset, int row, int column) {
		logger.debug("IN");
		String category=(String)dataset.getColumnKey(column);
		String serie=(String)dataset.getRowKey(row);

		String index=category+"-"+serie;

		String value="";
		if(catSerLabel.get(index)!=null && !catSerLabel.get(index).equals("")) 
		{
			logger.debug("set label");
			value=(String)catSerLabel.get(index);
		}

		logger.debug("OUT");

		return value;
	}

}
