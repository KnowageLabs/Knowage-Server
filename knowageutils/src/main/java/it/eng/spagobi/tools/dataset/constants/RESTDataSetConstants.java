/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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
 *
 */

package it.eng.spagobi.tools.dataset.constants;

public class RESTDataSetConstants {

	public static final String REST_JSON_PATH_ATTRIBUTES = "restJsonPathAttributes";
	public static final String[] REST_JSON_ARRAY_ATTRIBUTES = { REST_JSON_PATH_ATTRIBUTES };
	public static final String REST_REQUEST_HEADERS = "restRequestHeaders";
	public static final String[] REST_JSON_OBJECT_ATTRIBUTES = { REST_REQUEST_HEADERS };
	public static final String REST_REQUEST_BODY = "restRequestBody";
	public static final String REST_JSON_PATH_ITEMS = "restJsonPathItems";
	public static final String REST_HTTP_METHOD = "restHttpMethod";
	public static final String REST_ADDRESS = "restAddress";
	public static final String REST_JSON_DIRECTLY_ATTRIBUTES = "restDirectlyJSONAttributes";
	public static final String REST_NGSI = "restNGSI";
	public static final String REST_OFFSET = "restOffset";
	public static final String REST_FETCH_SIZE = "restFetchSize";
	public static final String REST_MAX_RESULTS = "restMaxResults";
	public static final String[] REST_STRING_ATTRIBUTES = { REST_ADDRESS, REST_REQUEST_BODY, REST_HTTP_METHOD, REST_JSON_PATH_ITEMS,
			REST_JSON_DIRECTLY_ATTRIBUTES, REST_NGSI, REST_OFFSET, REST_FETCH_SIZE, REST_MAX_RESULTS };
	public static final String[] REST_ALL_ATTRIBUTES = new String[REST_STRING_ATTRIBUTES.length + REST_JSON_OBJECT_ATTRIBUTES.length
			+ REST_JSON_ARRAY_ATTRIBUTES.length];
}
