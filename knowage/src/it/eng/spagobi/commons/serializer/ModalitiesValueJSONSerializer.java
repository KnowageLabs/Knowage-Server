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

import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;

import java.util.Locale;

import org.json.JSONObject;

public class ModalitiesValueJSONSerializer implements Serializer {

	public static final String ID = "ID";
	public static final String NAME = "NAME";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String LOVPROVIDER = "LOVPROVIDER";
	public static final String ITYPECD = "ITYPECD";
	public static final String ITYPEID = "ITYPEID";
	// public static final String DATASETID = "DATASETID";
	// public static final String DATASET = "DATASET";
	public static final String LABEL = "LABEL";
	public static final String SELECTIONTYPE = "SELECTIONTYPE";

	public Object serialize(Object o, Locale locale) throws SerializationException {

		JSONObject result = null;

		if (!(o instanceof ModalitiesValue)) {

			throw new SerializationException("ModalitiesValueJSONSerializer is unable to serialize object of type: " + o.getClass().getName());

		}

		try {

			ModalitiesValue modalitiesValue = null;
			result = new JSONObject();
			modalitiesValue = (ModalitiesValue) o;

			result.put(ID, modalitiesValue.getId());
			result.put(NAME, modalitiesValue.getName());
			result.put(DESCRIPTION, modalitiesValue.getDescription());
			result.put(LOVPROVIDER, modalitiesValue.getLovProvider());
			result.put(ITYPECD, modalitiesValue.getITypeCd());
			result.put(ITYPEID, modalitiesValue.getITypeId());
			// result.put(DATASETID, modalitiesValue.getDatasetID());
			// result.put(DATASET, modalitiesValue.getDataset());
			result.put(LABEL, modalitiesValue.getLabel());
			result.put(SELECTIONTYPE, modalitiesValue.getSelectionType());

		} catch (Throwable t) {
			// TODO Auto-generated catch block
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

		}

		return result;
	}

}
