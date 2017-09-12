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

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;

import java.util.Locale;

import org.json.JSONObject;

public class ParametersUseJSONSerialize implements Serializer {

	public static final String ID = "ID";
	public static final String USEID = "USEID";
	public static final String LOVID = "LOVID";
	public static final String DEFAULTLOVID = "DEFAULTLOVID";
	public static final String LABEL = "LABEL";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String NAME = "NAME";
	public static final String MANUALINPUT = "MANUALINPUT";
	public static final String SELECTIONTYPE = "SELECTIONTYPE";
	public static final String VALUESELECTION = "VALUESELECTION";
	public static final String SELECTEDLAYER = "SELECTEDLAYER";
	public static final String SELECTEDLAYERPROP = "SELECTEDLAYERPROP";

	public Object serialize(Object o, Locale locale) throws SerializationException {

		JSONObject result = null;

		if (!(o instanceof ParameterUse)) {

			throw new SerializationException("ParametersUseJSONSerializer is unable to serialize object of type: " + o.getClass().getName());

		}

		try {

			ParameterUse parameterUse = null;
			result = new JSONObject();
			parameterUse = (ParameterUse) o;

			result.put(ID, parameterUse.getId());
			result.put(USEID, parameterUse.getUseID());
			result.put(LOVID, parameterUse.getIdLov());
			result.put(DEFAULTLOVID, parameterUse.getIdLovForDefault());
			result.put(LABEL, parameterUse.getLabel());
			result.put(NAME, parameterUse.getName());
			result.put(DESCRIPTION, parameterUse.getDescription());
			result.put(MANUALINPUT, parameterUse.getManualInput());
			result.put(SELECTIONTYPE, parameterUse.getSelectionType());
			result.put(VALUESELECTION, parameterUse.getValueSelection());
			result.put(SELECTEDLAYER, parameterUse.getSelectedLayer());
			result.put(SELECTEDLAYERPROP, parameterUse.getSelectedLayerProp());

		} catch (Throwable t) {

			throw new SerializationException("An error occurred while serializing object: " + o, t);

		} finally {

		}

		return result;
	}
}
