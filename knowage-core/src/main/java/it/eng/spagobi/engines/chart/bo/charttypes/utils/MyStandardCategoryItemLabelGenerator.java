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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

/**
 *
 * @author gavardi This class is used to generate additiona label over bars
 *
 */

public class MyStandardCategoryItemLabelGenerator extends StandardCategoryItemLabelGenerator {

	private static final Logger LOGGER = Logger.getLogger(MyStandardCategoryItemLabelGenerator.class);

	private Map catSerLabel = null;

	public MyStandardCategoryItemLabelGenerator() {
		catSerLabel = new HashMap();
	}

	public MyStandardCategoryItemLabelGenerator(Map catSerMap) {
		catSerLabel = catSerMap;
	}

	public MyStandardCategoryItemLabelGenerator(Map catSerMap, String labelFormat, DateFormat formatter) {
		super(labelFormat, formatter);
		catSerLabel = catSerMap;
	}

	public MyStandardCategoryItemLabelGenerator(Map catSerMap, String labelFormat, NumberFormat formatter,
			NumberFormat percentFormatter) {
		super(labelFormat, formatter, percentFormatter);
		catSerLabel = catSerMap;
	}

	public MyStandardCategoryItemLabelGenerator(Map catSerMap, String labelFormat, NumberFormat formatter) {
		super(labelFormat, formatter);
		catSerLabel = catSerMap;
	}

	@Override
	public String generateLabel(CategoryDataset dataset, int row, int column) {
		LOGGER.debug("IN");
		String category = (String) dataset.getColumnKey(column);
		String serie = (String) dataset.getRowKey(row);

		String index = category + "-" + serie;

		String value = "";
		if (catSerLabel.get(index) != null && !catSerLabel.get(index).equals("")) {
			LOGGER.debug("set label");
			value = (String) catSerLabel.get(index);
		}

		LOGGER.debug("OUT");

		return value;
	}

}
