/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.drivers;

import java.util.Map;

public class EngineURL {
	
	private String mainURL = null;
	
	private Map parameters = null;

	/**
	 * Instantiates a new engine url.
	 * 
	 * @param mainURL the main url
	 * @param parameters the parameters
	 */
	public EngineURL(String mainURL, Map parameters) {
		this.mainURL = mainURL;
		this.parameters = parameters;
	}
	
	/**
	 * Gets the main url.
	 * 
	 * @return the main url
	 */
	public String getMainURL() {
		return mainURL;
	}

	/**
	 * Sets the main url.
	 * 
	 * @param mainURL the new main url
	 */
	public void setMainURL(String mainURL) {
		this.mainURL = mainURL;
	}

	/**
	 * Gets the parameters.
	 * 
	 * @return the parameters
	 */
	public Map getParameters() {
		return parameters;
	}

	/**
	 * Sets the parameters.
	 * 
	 * @param parameters the new parameters
	 */
	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}

	/**
	 * Adds the parameter.
	 * 
	 * @param key the key
	 * @param value the value
	 */
	public void addParameter(String key, Object value) {
		parameters.put(key, value);
	}
	
	/**
	 * Adds the parameters.
	 * 
	 * @param parameters the parameters
	 */
	public void addParameters(Map parameters) {
		this.parameters.putAll(parameters);
	}
}
