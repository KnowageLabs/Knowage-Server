package it.eng.spagobi.tools.dataset.common.datawriter;

import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public class CockpitJSONDataWriter extends JSONDataWriter {

	public CockpitJSONDataWriter() {
	}

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
			} else if (Boolean.class.isAssignableFrom(fieldMetaData.getType())) {
				result = Boolean.valueOf(value.toString());
			} else if (Byte.class.isAssignableFrom(fieldMetaData.getType())) {
				result = Byte.valueOf(value.toString());
			} else if (Short.class.isAssignableFrom(fieldMetaData.getType())) {
				result = Short.valueOf(value.toString());
			} else if (Integer.class.isAssignableFrom(fieldMetaData.getType())) {
				result = Integer.valueOf(value.toString());
			} else if (Long.class.isAssignableFrom(fieldMetaData.getType())) {
				result = Long.valueOf(value.toString());
			} else if (BigInteger.class.isAssignableFrom(fieldMetaData.getType())) {
				result = new BigInteger(value.toString());
			} else if (Float.class.isAssignableFrom(fieldMetaData.getType())) {
				result = Float.valueOf(value.toString());
			} else if (Double.class.isAssignableFrom(fieldMetaData.getType())) {
				result = Double.valueOf(value.toString());
			} else if (BigDecimal.class.isAssignableFrom(fieldMetaData.getType())) {
				result = new BigDecimal(value.toString());
			} else {
				result = value.toString();
			}
		}

		return result;
	}

}
