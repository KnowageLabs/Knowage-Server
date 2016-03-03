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
package it.eng.spagobi.tools.scheduler.bo;

import junit.framework.Assert;

import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class CronExpression {
	String expression;

	public CronExpression() {
		// setExpression("single{}");
		setExpression("{'type': 'single'}");
	}

	public CronExpression(String expression) {
		setExpression(expression);
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		Assert.assertNotNull("Input parameter [expression] cannot be null");
		this.expression = expression;
	}

	public boolean isSimpleExpression() {
		if (expression.indexOf("'type':'single'") != -1 || "single{}".equalsIgnoreCase(expression)) {
			return true;
		}
		return false;
	}

	public String getChronoType() {
		try {
			JSONObject jo = new JSONObject(expression);
			String type = jo.getString("type");
			if (type.equals("single")) {
				return "Single";
			} else if (type.equals("event")) {
				return ("Event-" + jo.getJSONObject("parameter").getString("type"));
			} else {
				// scheduler
				return ("Scheduler-" + type);
			}

		} catch (Exception e) {
			return "";
		}

	}

	@Override
	public String toString() {
		return "CronExpression [expression=" + expression + "]";
	}

}
