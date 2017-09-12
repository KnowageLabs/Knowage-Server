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
