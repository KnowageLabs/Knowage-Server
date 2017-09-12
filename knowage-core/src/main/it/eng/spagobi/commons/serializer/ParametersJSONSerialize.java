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

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;

import java.util.Locale;

import org.json.JSONObject;

public class ParametersJSONSerialize implements Serializer {

	public static final String ID = "ID";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String LENGTH = "LENGTH";
	public static final String LABEL = "LABEL";
	public static final String NAME = "NAME";
	public static final String MASK = "MASK";
	public static final String MODALITY = "MODALITY";
	public static final String FUNCTIONALFLAG = "FUNCTIONALFLAG";
	public static final String TEMPORALFLAG = "TEMPORALFLAG";
	public static final String INPUTTYPECD = "INPUTTYPECD";

	public Object serialize(Object o, Locale locale) throws SerializationException {

		JSONObject result = null;

		if (!(o instanceof Parameter)) {

			throw new SerializationException("ParameterJSONSerializer is unable to serialize object of type: " + o.getClass().getName());

		}

		try {

			Parameter parameter = null;
			result = new JSONObject();

			parameter = (Parameter) o;

			result.put(ID, parameter.getId());
			result.put(DESCRIPTION, parameter.getDescription());
			result.put(LENGTH, parameter.getLength());
			result.put(LABEL, parameter.getLabel());
			result.put(NAME, parameter.getName());
			result.put(FUNCTIONALFLAG, parameter.isFunctional());
			result.put(TEMPORALFLAG, parameter.isTemporal());
			result.put(MASK, parameter.getMask());
			result.put(MODALITY, parameter.getType() + "," + parameter.getTypeId());
			result.put(INPUTTYPECD, parameter.getType());

		} catch (Throwable t) {

			throw new SerializationException("An error occurred while serializing object: " + o, t);

		} finally {

		}

		return result;
	}
}
