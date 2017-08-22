package it.eng.spagobi.tools.dataset.common.datawriter;

import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public class CockpitJSONDataWriter extends JSONDataWriter {

	public CockpitJSONDataWriter(Map<String, Object> properties) {
		super(properties);
	}

	@Override
	protected Object getFieldValue(IField field, IFieldMetaData fieldMetaData) {
		Object result = null;

		Object value = field.getValue();
		if (value != null) {
			if (Timestamp.class.isAssignableFrom(fieldMetaData.getType())) {
				result = TIMESTAMP_FORMATTER.format(value);
			} else if (Date.class.isAssignableFrom(fieldMetaData.getType())) {
				result = DATE_FORMATTER.format(value);
			} else if (Number.class.isAssignableFrom(fieldMetaData.getType())) {
				result = Double.valueOf(value.toString());
			} else if (Boolean.class.isAssignableFrom(fieldMetaData.getType())) {
				result = Boolean.valueOf(value.toString());
			} else {
				result = value.toString();
			}
		}

		return result;
	}

}
