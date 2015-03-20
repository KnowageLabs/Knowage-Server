/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.proxy;

import org.apache.axis.AxisProperties;
import org.apache.axis.client.Stub;

public abstract class AbstractSDKServiceProxy {

	boolean proxyRequirementAuthentication;

	public void setProxyHost(String proxyHost) {
		if(proxyHost!=null){
			AxisProperties.setProperty("http.proxyHost", proxyHost);
		}	
	}
	public void setProxyPort(String proxyPort) {
		if(proxyPort!=null){
			AxisProperties.setProperty("http.proxyPort", proxyPort);
		}
	}
	public void setProxyUserId(String proxyUserId) {
		if(proxyUserId!=null){
			AxisProperties.setProperty("http.proxyUser", proxyUserId);
		}	
	}
	public void setProxyPassword(String proxyPassword) {
		if(proxyPassword!=null){
			AxisProperties.setProperty("http.proxyPassword", proxyPassword); 
		}
	}


//	public void initProxyProperties(){
//	if(proxyHost!=null)
//	AxisProperties.setProperty("http.proxyHost", proxyHost);
//	if(proxyPort!=null)
//	AxisProperties.setProperty("http.proxyPort", proxyPort);
//	if(proxyUserId!=null)
//	AxisProperties.setProperty("http.proxyUser", proxyUserId);
//	if(proxyPassword!=null)
//	AxisProperties.setProperty("http.proxyPassword", proxyPassword); 
//	}

}
