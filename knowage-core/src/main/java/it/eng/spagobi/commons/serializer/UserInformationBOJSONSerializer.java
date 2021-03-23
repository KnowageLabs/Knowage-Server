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

			result.put("organization", obj.getOrganization());

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

		}

		return result;
	}

}
