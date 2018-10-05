package it.eng.spagobi.tools.dataset.metadata.mapping;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MetaDataMapping {

	private static Map<String, Class> metaDataTypes;

	static {
		metaDataTypes = new HashMap<>();
		metaDataTypes.put("Double", Double.class);
		metaDataTypes.put("Date", Date.class);
		metaDataTypes.put("String", String.class);
		metaDataTypes.put("Integer", Integer.class);
		metaDataTypes.put("BigDecimal", BigDecimal.class);
		metaDataTypes.put("Timestamp", Timestamp.class);
		metaDataTypes.put("Time", Time.class);
		metaDataTypes.put("Byte", Byte.class);
		metaDataTypes.put("Short", Short.class);
		metaDataTypes.put("Float", Float.class);
		metaDataTypes.put("Long", Long.class);
		metaDataTypes.put("Boolean", Boolean.class);
		metaDataTypes.put("java.lang.Integer", Integer.class);
		metaDataTypes.put("java.util.Date", Date.class);
		metaDataTypes.put("java.lang.Long", Long.class);
		metaDataTypes.put("java.lang.String", String.class);
		metaDataTypes.put("java.math.BigDecimal", BigDecimal.class);
		metaDataTypes.put("java.sql.Date", java.sql.Date.class);
		metaDataTypes.put("java.lang.Double", Double.class);
		metaDataTypes.put("java.sql.Timestamp", Timestamp.class);
		metaDataTypes.put("java.sql.Time", Time.class);
		metaDataTypes.put("java.lang.Byte", Byte.class);
		metaDataTypes.put("java.lang.Short", Short.class);
		metaDataTypes.put("java.lang.Float", Float.class);
		metaDataTypes.put("java.lang.Long", Long.class);
		metaDataTypes.put("java.lang.Boolean", Boolean.class);

	}

	public static Class getMetaDataType(String value) {
		return metaDataTypes.get(value);
	}

}
