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
package it.eng.knowage.engine.cockpit.api.crosstable;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class NodeComparator implements Comparator<Node> {

	private int direction;
	private String parentValue;
	private String measureLabel;

	public NodeComparator() {
		this.direction = 1; // default
		this.parentValue = null;
		this.measureLabel = null;
	}

	public NodeComparator(int direction, String parentValue, String measureLabel) {
		this.direction = direction;
		this.parentValue = parentValue;
		this.measureLabel = measureLabel;
	}

	public NodeComparator(int direction, String parentValue) {
		this.direction = direction;
		this.parentValue = parentValue;
	}

	public NodeComparator(int direction) {
		this.direction = direction;
		this.parentValue = null;
		this.measureLabel = null;
	}

	@Override
	public int compare(Node arg0, Node arg1) {
		String value0 = arg0.getValue();
		String value1 = arg1.getValue();
		try {
			/*
			 * Here we can consider only one node
			 * because the second one has the same
			 * configuration, obviously.
			 */
			JSONObject jsonObject = arg0.getJsonObject();
			String type = jsonObject.getString("type");
			if ("string".equals(type)) {
				return direction * value0.compareTo(value1);
			} else if ("date".equals(type)) {
				String pattern = jsonObject.getString("dateFormatJava");
				SimpleDateFormat sdf = new SimpleDateFormat(pattern);
				Date date0 = sdf.parse(value0);
				Date date1 = sdf.parse(value1);
				return direction * date0.compareTo(date1);
			} else {
				// compares only on values
				Float arg0Value = new Float(value0);
				Float arg1Value = new Float(value1);
				return direction * arg0Value.compareTo(arg1Value);
			}
		} catch (Exception e) {
			// if its not possible to convert the values in float, consider them
			// as strings
			return direction * value0.compareTo(value1);
		}
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int dir) {
		this.direction = dir;
	}

	public String getParentValue() {
		return parentValue;
	}

	public void setParentValue(String value) {
		this.parentValue = value;
	}

	public String getMeasureLabel() {
		return measureLabel;
	}

	public void setMeasureLabel(String label) {
		this.measureLabel = label;
	}

}
