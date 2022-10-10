/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.parameter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface IParameterManager {

	String fromFeToBe(String type, String value, String defaultValue, boolean multiValue) throws JSONException;

	/**
	 *
	 * @param type
	 * @param value
	 * @param defaultValue
	 * @param multiValue
	 * @return either {@link JSONObject} or {@link JSONArray}
	 * @throws JSONException
	 */
	Object fromBeToFe(String type, String defaultValue, boolean multiValue) throws JSONException;

}