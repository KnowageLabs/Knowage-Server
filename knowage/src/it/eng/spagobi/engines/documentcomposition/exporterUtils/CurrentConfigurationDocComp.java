/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.documentcomposition.exporterUtils;

import it.eng.spago.util.GeneralUtilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 
 * @author gavardi
 * A map that for a document label keeps track of all parameters and their current value
 *
 */

public class CurrentConfigurationDocComp {

	String label=null;
	Map<String, Object> parameters=null;
	
	public CurrentConfigurationDocComp(String label) {
		super();
		this.label = label;
		this.parameters = new HashMap<String, Object>();
	}

	
	public void fillParsFromUrl(String urlString){
		parameters=it.eng.spagobi.commons.utilities.GeneralUtilities.getParametersFromURL(urlString);
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(java.util.Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
}
