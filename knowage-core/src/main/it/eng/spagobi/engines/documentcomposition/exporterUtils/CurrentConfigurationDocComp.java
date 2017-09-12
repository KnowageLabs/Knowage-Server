/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
