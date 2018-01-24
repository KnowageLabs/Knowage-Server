package it.eng.spagobi.tools.dataset.metadata.mapping;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class MetaDataMapping {
	
	private static Map<String, Class> metaDataTypes;
	
	static	{
		metaDataTypes = new HashMap<>();
		metaDataTypes.put("Double", BigDecimal.class);
		metaDataTypes.put("String", String.class);
		metaDataTypes.put("Integer", Integer.class);
	}
	
	public static Class getMetaDataType(String value) {
		return metaDataTypes.get(value);
	}

}
