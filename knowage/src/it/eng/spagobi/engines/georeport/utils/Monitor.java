/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.engines.georeport.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class Monitor {
	static Map<String, Long> activities;
	
	static {
		activities = new HashMap<String, Long>();
	}
	
	public static void start(String activityName) {
		Long startTime = new Long(System.currentTimeMillis());
		activities.put(activityName, startTime);
	}
	
	public static long elapsed(String activityName) {
		long now = System.currentTimeMillis();
		Long startTime = activities.get(activityName);
		return now - startTime.longValue();
	}
	
	public static long stop(String activityName) {
		long elapsed = elapsed(activityName);
		activities.remove(activityName);
		return elapsed;
	}
}
