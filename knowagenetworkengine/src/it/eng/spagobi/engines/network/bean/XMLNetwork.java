/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
