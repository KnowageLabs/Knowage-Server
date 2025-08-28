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
package it.eng.spagobi.behaviouralmodel.lov.bo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;

/**
 * Defines method to manage lov result
 */
public class LovResultHandler {

	private static Logger logger = Logger.getLogger(LovResultHandler.class);

	/**
	 * Sourcebean of the lov result
	 */
	private SourceBean lovResultSB = null;

	/**
	 * constructor.
	 *
	 * @param lovResult
	 *            the xml string representation of the lov result
	 *
	 * @throws SourceBeanException
	 *             the source bean exception
	 */
	public LovResultHandler(String lovResult) throws SourceBeanException {
		lovResultSB = SourceBean.fromXMLString(lovResult);
	}

	/**
	 * Gets the list of result rows.
	 *
	 * @return list of rows
	 */
	public List getRows() {
		return lovResultSB.getAttributeAsList("ROW");
	}

	/**
	 * Gets the SourceBean of the row which value column contains the input value.
	 *
	 * @param value
	 *            input value which identifies the row
	 * @param valueColumnName
	 *            name of the column that holds the values
	 *
	 * @return the SourceBean of the row
	 */
	public SourceBean getRow(String value, String valueColumnName) {
		SourceBean row = null;
		Object o = lovResultSB.getFilteredSourceBeanAttribute("ROW", valueColumnName, value);
		// if there are duplicated row return only the first (this can happen when the checklist lookup
		// modality is used)
		if (o instanceof ArrayList) {
			List list = (ArrayList) o;
			row = (SourceBean) list.get(0);
		} else {
			row = (SourceBean) o;
		}

		return row;
	}

	/**
	 * Gets the list of values contained into the lov result.
	 *
	 * @param valueColumnName
	 *            name of the column that holds the values
	 *
	 * @return list of values
	 */
	public List getValues(String valueColumnName) {
		List values = new ArrayList();
		List rows = getRows();
		for (int i = 0; i < rows.size(); i++) {
			SourceBean row = (SourceBean) rows.get(i);
			values.add(row.getAttribute(valueColumnName));
		}
		return values;
	}

	/**
	 * Checks if a lov result contains a specific result.
	 *
	 * @param value
	 *            the value to search
	 * @param valueColumnName
	 *            name of the column that holds the values
	 *
	 * @return true if the value is contained, false otherwise
	 */
	public boolean containsValue(String value, String valueColumnName) {
		List values = getValues(valueColumnName);
		for (int i = 0; i < values.size(); i++) {
			if (value == null && values.get(i) == null) {
				return true;
			}
			if (values.get(i) != null && values.get(i).toString().trim().equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}

	// TODO: WARNING!!! MUST BE CHANGED, THIS IS ONLY A TEMPORARY SOLUTION
	public boolean containsValueForTree(String value, String valueColumnName) {
		List values = getValues(valueColumnName);
		for (int i = 0; i < values.size(); i++) {
			if (value == null && values.get(i) == null) {
				return true;
			}
			if (values.get(i) != null) {
				String rightValue = values.get(i).toString();
				rightValue = rightValue.split("__")[0]; // TODO: HARD CODED VALUE
				if (rightValue.equalsIgnoreCase(value)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets the description specified on descriptionColumnName relevant to the row that has valueColumnName equal to value.
	 *
	 * @param value
	 *            The value to search
	 * @param valueColumnName
	 *            The value column
	 * @param descriptionColumnName
	 *            The description column
	 *
	 * @return the description specified on descriptionColumnName relevant to the row that has valueColumnName equal to value
	 */
	public String getValueDescription(String value, String valueColumnName, String descriptionColumnName) {
		SourceBean sb = getRow(value, valueColumnName);
		if (sb == null) {
			logger.warn("Value [" + value + "] not found in column [" + valueColumnName + "]");
			return null;
		}
		Object description = sb.getAttribute(descriptionColumnName);
		if (description == null) {
			return null;
		} else {
			return description.toString();
		}
	}

	/**
	 * Gets the sourcebean of the lov result.
	 *
	 * @return the sourcebean of the lov result
	 */
	public SourceBean getLovResultSB() {
		return lovResultSB;
	}

	/**
	 * Checks if the lov result as only one value.
	 *
	 * @return true if the result contains only one value, false otherwise
	 */
	public boolean isSingleValue() {
		return (getRows().size() == 1);
	}

}
