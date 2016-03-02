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

import it.eng.spagobi.api.CacheResource;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;

import java.util.Locale;

import org.json.JSONObject;

public class MetaModelJSONSerializer implements Serializer {

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DATA_SOURCE_LABEL = "data_source_label";
	public static final String DESCRIPTION = "description";
	public static final String CATEGORY = "category";
	public static final String LOCKED = "locked";
	public static final String LOCKER = "locker";
	public static final String TYPE = "type";
	public static final String CACHE_DATA_SOURCE = "cache_data_source";

	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject result = null;

		if (!(o instanceof MetaModel)) {
			throw new SerializationException("MetaModelJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}

		try {
			MetaModel model = (MetaModel) o;
			result = new JSONObject();
			result.put(ID, model.getId());
			result.put(NAME, model.getName());
			result.put(DESCRIPTION, model.getDescription());
			result.put(CATEGORY, model.getCategory());
			result.put(DATA_SOURCE_LABEL, model.getDataSourceLabel());
			result.put(LOCKED, model.getModelLocked());
			result.put(LOCKER, model.getModelLocker());
			result.put(TYPE, "MODEL");

			String cacheDataSource = new CacheResource().getCacheDataSource();
			if (cacheDataSource != null) {
				result.put(CACHE_DATA_SOURCE, cacheDataSource);
			}

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

		}

		return result;
	}

}
