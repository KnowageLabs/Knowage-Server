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
package it.eng.spagobi.engine.cockpit.api;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import org.json.JSONArray;
import org.json.JSONObject;

public class ExceptionUtilities {

	public static String serializeException(String message, String localizedMessage) {

		try {
			JSONArray ja = new JSONArray();
			JSONObject jo = new JSONObject();
			JSONObject je = new JSONObject();
			if (message != null) {
				jo.put("message", message);
			}
			if (localizedMessage != null) {
				jo.put("localizedMessage", localizedMessage);
			}
			ja.put(jo);
			je.put("errors", ja);
			return je.toString();
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot fill response container", e);
		}
	}
}
