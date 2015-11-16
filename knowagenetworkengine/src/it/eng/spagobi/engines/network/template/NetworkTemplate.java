/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.network.template;

import org.json.JSONObject;

import it.eng.spagobi.engines.network.bean.CrossNavigationLink;
import it.eng.spagobi.engines.network.bo.NetworkDefinition;


/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class NetworkTemplate {

	private NetworkDefinition networkDefinition; //XMLGRAPH
	private JSONObject networkJSON; //JSON parsing of the template
	private CrossNavigationLink crossNavigationLink;// cross navigation link
	private JSONObject info;
	
	public static final String OPTIONS = "options";
	public static final String DATA_SET_MAPPING = "datasetMapping";
	public static final String DATA_SET_MAPPING_ELEMENT = "element";
	public static final String DATA_SET_MAPPING_COLUMN = "column";
	public static final String DATA_SET_MAPPING_TYPE = "type";
	public static final String DATA_SET_MAPPING_VALUE = "value";
	public static final String DATA_SET_MAPPING_PROPERTY = "property";
	public static final String DATA_SET_MAPPING_SOURCE = "source";
	public static final String DATA_SET_MAPPING_TARGHET = "target";
	public static final String DATA_SET_MAPPING_EDGE = "edge";
	
	public NetworkTemplate() {
		networkDefinition = new NetworkDefinition() ;
	}

	public NetworkDefinition getNetworkDefinition() {
		return networkDefinition;
	}

	public void setNetworkDefinition(NetworkDefinition networkDefinition) {
		this.networkDefinition = networkDefinition;
	}
	
	public void setNetworkXML(String networkXML){
		networkDefinition.setNetworkXML(networkXML);
	}

	public JSONObject getNetworkJSON() {
		return networkJSON;
	}

	public void setNetworkJSNO(JSONObject networkJSON) {
		this.networkJSON = networkJSON;
	}

	public CrossNavigationLink getCrossNavigationLink() {
		return crossNavigationLink;
	}

	public void setCrossNavigationLink(CrossNavigationLink crossNavigationLink) {
		this.crossNavigationLink = crossNavigationLink;
	}

	public JSONObject getInfo() {
		return info;
	}

	public void setInfo(JSONObject info) {
		this.info = info;
	}	
	
	
	
}
