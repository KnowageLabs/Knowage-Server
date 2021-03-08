package it.eng.spagobi.tools.dataset.common.datawriter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;

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
			Class type = fieldMetaData.getType();
			boolean multiValue = fieldMetaData.isMultiValue();
			String typeName = type.getName();
			if (multiValue) {
				result = value;
			} else if (Timestamp.class.isAssignableFrom(type)) {
				result = TIMESTAMP_FORMATTER.format(value);
			} else if ("oracle.sql.TIMESTAMP".equals(typeName)) {
				String s = value.toString();
				int year = Integer.parseInt(s.substring(0, 4)) - 1900;
				int month = Integer.parseInt(s.substring(5, 7)) - 1;
				int date = Integer.parseInt(s.substring(8, 10));
				int hour = Integer.parseInt(s.substring(11, 13));
				int minute = Integer.parseInt(s.substring(14, 16));
				int second = Integer.parseInt(s.substring(17, 19));
				String nanoString = s.substring(20);
				int nano = Integer.parseInt(nanoString) * (int) Math.pow(10, 9 - nanoString.length());
				Timestamp timestamp = new Timestamp(year, month, date, hour, minute, second, nano);
				result = TIMESTAMP_FORMATTER.format(timestamp);
			} else if ("oracle.sql.DATE".equals(typeName)) {
				String s = value.toString();
				int year = Integer.parseInt(s.substring(0, 4)) - 1900;
				int month = Integer.parseInt(s.substring(5, 7)) - 1;
				int date = Integer.parseInt(s.substring(8, 10));
				Timestamp timestamp = new Timestamp(year, month, date, 0, 0, 0, 0);
				result = TIMESTAMP_FORMATTER.format(timestamp);
			} else if (Time.class.isAssignableFrom(type)) {
				result = CACHE_TIMEONLY_FORMATTER.format(value);
			} else if (Date.class.isAssignableFrom(type)) {
				result = DATE_FORMATTER.format(value);
			} else if (Boolean.class.isAssignableFrom(type)) {
				result = Boolean.valueOf(value.toString());
			} else if (Byte.class.isAssignableFrom(type)) {
				result = Byte.valueOf(value.toString());
			} else if (Short.class.isAssignableFrom(type)) {
				result = Short.valueOf(value.toString());
			} else if (Integer.class.isAssignableFrom(type)) {
				result = Integer.valueOf(value.toString());
			} else if (Long.class.isAssignableFrom(type)) {
				result = Long.valueOf(value.toString());
			} else if (BigInteger.class.isAssignableFrom(type)) {
				result = new BigInteger(value.toString());
			} else if (Float.class.isAssignableFrom(type)) {
				result = Float.valueOf(value.toString());
			} else if (Double.class.isAssignableFrom(type)) {
				result = Double.valueOf(value.toString());
			} else if (BigDecimal.class.isAssignableFrom(type)) {
				result = value;
			} else {
				result = value.toString();
			}
		}

		return result;
	}

	private boolean isIntegerValue(BigDecimal bd) {
		return bd.stripTrailingZeros().scale() <= 0;
	}

}
