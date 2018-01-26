package it.eng.spagobi.tools.dataset.metadata.mapping;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MetaDataMapping {
	
	private static Map<String, Class> metaDataTypes;
	
	static	{
		metaDataTypes = new HashMap<>();
		metaDataTypes.put("Double", BigDecimal.class);
		metaDataTypes.put("Date", Date.class);
		metaDataTypes.put("String", String.class);
		metaDataTypes.put("Integer", Integer.class);
		metaDataTypes.put("java.lang.Integer", Integer.class);
		metaDataTypes.put("java.util.Date", Date.class);
		metaDataTypes.put("java.lang.Long", Long.class);
		metaDataTypes.put("java.lang.String", String.class);
		metaDataTypes.put("java.sql.Date", java.sql.Date.class);
		metaDataTypes.put("java.lang.Double", Double.class);
		
	}
	
	public static Class getMetaDataType(String value) {
		return metaDataTypes.get(value);
	}

}
