package it.eng.spagobi.tools.hierarchiesmanagement.utils;

import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;

import org.json.JSONException;
import org.json.JSONObject;

public class HierarchyUtils {

	public static JSONObject createJSONObjectFromField(Field field, boolean isHierarchyField) throws JSONException {

		JSONObject result = new JSONObject();
		result.put(HierarchyConstants.FIELD_ID, field.getId());
		result.put(HierarchyConstants.FIELD_NAME, field.getName());
		result.put(HierarchyConstants.FIELD_VISIBLE, field.isVisible());
		result.put(HierarchyConstants.FIELD_EDITABLE, field.isEditable());
		result.put(HierarchyConstants.FIELD_TYPE, field.getType());

		if (isHierarchyField) {
			result.put(HierarchyConstants.FIELD_SINGLE_VALUE, field.isSingleValue());
			result.put(HierarchyConstants.FIELD_REQUIRED, field.isRequired());
		}

		return result;

	}

	// public static JSONObject createJSONObjectFromFieldsList(List<Field> fields, String name) {
	//
	// }

}
