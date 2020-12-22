package it.eng.spagobi.engines.datamining.common;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;

public class FunctionExecutor {

	static protected Logger logger = Logger.getLogger(FunctionExecutor.class);

	public static String execute(String body, SbiCatalogFunction function, UserProfile userProfile, Map env) {
		logger.debug("IN");
		return "";
	}

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
}
