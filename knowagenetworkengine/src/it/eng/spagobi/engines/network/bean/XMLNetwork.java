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

package it.eng.spagobi.engines.network.bean;

import org.json.JSONObject;

import it.eng.spagobi.engines.network.serializer.SerializationException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class XMLNetwork implements INetwork{

	private static final String TYPE = "XML";
	private String net="";
	private CrossNavigationLink networkCrossNavigation;
	private JSONObject networkOptions;
	
	
	/**
	 * @param net
	 */
	public XMLNetwork(String net, CrossNavigationLink networkCrossNavigation) {
		super();
		this.net = net;
		this.networkOptions = new JSONObject();
		this.networkCrossNavigation = networkCrossNavigation;
	}
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.network.bean.INetwork#getNetworkAsString()
	 */
	public String getNetworkAsString() throws SerializationException {
		return net;
	}
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.network.bean.INetwork#getNetworkType()
	 */
	public String getNetworkType() {
		return TYPE;
	}
	
	
	
	public void setNetworkCrossNavigation(CrossNavigationLink networkCrossNavigation) {
		this.networkCrossNavigation = networkCrossNavigation;
	}
	@JsonIgnore
	public String getNetworkCrossNavigation() throws SerializationException{
		ObjectMapper mapper = new ObjectMapper();
		String s ="";
		try {
			
			s = mapper.writeValueAsString(networkCrossNavigation);

		} catch (Exception e) {
			
			throw new SerializationException("Error serializing the network",e);
		}
		return  s; 
	}

	public String getNetworkOptions(){
		return networkOptions.toString();
	}
	public void setNetworkOptions(JSONObject networkOptions) {
		this.networkOptions = networkOptions;
	}

	public String getNetworkInfo(){
		return null;
	}	
	
}
