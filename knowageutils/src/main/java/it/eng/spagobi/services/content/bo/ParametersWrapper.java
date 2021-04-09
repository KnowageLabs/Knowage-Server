package it.eng.spagobi.services.content.bo;

import java.util.LinkedHashMap;
import java.util.Map;

public class ParametersWrapper {

	private final Map<String, ?> map = new LinkedHashMap<>();

	public Map<String, ?> getMap() {
		return map;
	}

}
