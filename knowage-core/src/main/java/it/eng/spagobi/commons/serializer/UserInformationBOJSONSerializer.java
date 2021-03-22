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
package it.eng.spagobi.commons.serializer;

import java.util.Locale;

import org.json.JSONObject;

import it.eng.spagobi.profiling.bo.UserInformationBO;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */
public class UserInformationBOJSONSerializer implements Serializer {

	// please modify also documentBrowser.xml properly everytime this serializer is modified
	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String NAME = "name";
	public static final String SHORT_NAME = "shortName";
	public static final String DESCRIPTION = "description";
	public static final String TYPECODE = "typeCode";
	public static final String TYPEID = "typeId";
	public static final String ENCRYPT = "encrypt";
	public static final String VISIBLE = "visible";
	public static final String PROFILEDVISIBILITY = "profiledVisibility";
	public static final String ENGINE = "engine";
	public static final String ENGINE_ID = "engineid";
	public static final String DATASOURCE = "datasource";
	public static final String DATASET = "dataset";
	public static final String UUID = "uuid";
	public static final String RELNAME = "relname";
	public static final String STATECODE = "stateCode";
	public static final String STATEID = "stateId";
	public static final String FUNCTIONALITIES = "functionalities";
	public static final String CREATIONDATE = "creationDate";
	public static final String CREATIONUSER = "creationUser";
	public static final String REFRESHSECONDS = "refreshSeconds";
	public static final String PREVIEWFILE = "previewFile";
	public static final String PATH_RESOURCES = "pathResources";
	public static final String ACTIONS = "actions";
	public static final String EXPORTERS = "exporters";
	public static final String IS_PUBLIC = "isPublic";
	public static final String DOC_VERSION = "docVersion";
	public static final String PARAMETERS_REGION = "parametersRegion";
	public static final String LOCKED_BY_USER = "lockedByUser";

	public static final Integer SHORT_NAME_CHARACTERS_LIMIT = 60;

	@Override
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject result = null;

		if (!(o instanceof UserInformationBO)) {
			throw new SerializationException("UserInformationBOJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}

		try {
			UserInformationBO obj = (UserInformationBO) o;
			result = new JSONObject();

			result.put("userId", obj.getUserId());

			result.put("defaultRoleId", obj.getDefaultRoleId());

			result.put("fullName", obj.getFullName());

			result.put("isSuperadmin", obj.getIsSuperadmin());

			result.put("dtLastAccess", obj.getDtLastAccess());

			result.put("attributes", obj.getAttributes());

			result.put("locale", obj.getLocale());

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

		}

		return result;
	}

}
