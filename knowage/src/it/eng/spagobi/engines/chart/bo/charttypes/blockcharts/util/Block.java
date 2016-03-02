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
package it.eng.spagobi.engines.chart.bo.charttypes.blockcharts.util;

import java.util.ArrayList;

public class Block {

	String code;
	ArrayList<Activity> activities;

	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public ArrayList<Activity> getActivities() {
		return activities;
	}
	public void setActivities(ArrayList<Activity> activities) {
		this.activities = activities;
	}

	public void addActivities(Activity activity) {
		this.activities.add(activity);
	}

	public Block(String code) {
		super();
		this.code = code;
		activities=new ArrayList<Activity>();
	}




}
