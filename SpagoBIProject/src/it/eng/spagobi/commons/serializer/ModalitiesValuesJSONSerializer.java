package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

public class ModalitiesValuesJSONSerializer implements Serializer {

	public static final String ID = "LOV_ID";
	public static final String NAME = "LOV_NAME";
	public static final String DESCRIPTION = "LOV_DESCRIPTION";
	public static final String PROVIDER = "LOV_PROVIDER";
	public static final String ITYPECD = "I_TYPE_CD";
	public static final String ITYPEID = "I_TYPE_ID";
	public static final String LABEL = "LOV_LABEL";
	public static final String SELECTIONTYPE = "SELECTION_TYPE";

	// public static final String DATASET = "DATASET";
	// public static final String DATASETID = "DATASET_ID";
	// public static final String MULTIVALUE = "MULTIVALUE";

	public Object serialize(Object o, Locale locale) throws SerializationException {

		JSONObject result = new JSONObject();

		ModalitiesValue modalitiesValue = (ModalitiesValue) o;

		try {
			result.put(ID, modalitiesValue.getId());
			result.put(NAME, modalitiesValue.getName());
			result.put(DESCRIPTION, modalitiesValue.getDescription());
			result.put(PROVIDER, modalitiesValue.getLovProvider());
			result.put(ITYPECD, modalitiesValue.getITypeCd());
			result.put(ITYPEID, modalitiesValue.getITypeId());
			result.put(LABEL, modalitiesValue.getLabel());
			result.put(SELECTIONTYPE, modalitiesValue.getSelectionType());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
}