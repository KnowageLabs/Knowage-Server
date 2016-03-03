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
package it.eng.spagobi.engines.worksheet.serializer.json;

import it.eng.qbe.serializer.ISerializer;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spagobi.engines.worksheet.bo.Attribute;
import it.eng.spagobi.utilities.assertion.Assert;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class AttributeJSONSerializer implements ISerializer {

    public static transient Logger logger = Logger.getLogger(AttributeJSONSerializer.class);

	//@Override
	public Object serialize(Object o) throws SerializationException {
		JSONObject toReturn = null;
		Attribute attribute;
				
		Assert.assertNotNull(o, "Input parameter cannot be null");
		Assert.assertTrue(o instanceof Attribute, "Unable to serialize objects of type [" + o.getClass().getName() + "]");
		
		try {
			toReturn = new JSONObject();
			
			attribute = (Attribute) o;
			
			toReturn.put(FieldsSerializationConstants.ID, attribute.getEntityId());
			toReturn.put(FieldsSerializationConstants.ALIAS, attribute.getAlias());
			toReturn.put(FieldsSerializationConstants.ICON_CLS, attribute.getIconCls());
			toReturn.put(FieldsSerializationConstants.NATURE, attribute.getNature());
			toReturn.put(FieldsSerializationConstants.VALUES, attribute.getValues());

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return toReturn;
	}

    
		
}
