/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.commons;

import it.eng.spagobi.commons.resource.DefaultResourceLocator;
import it.eng.spagobi.commons.resource.IResourceLocator;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class SpagoBIPlugin {
	
	String pluginId;
	IResourceLocator resourceLocator;
	
	protected SpagoBIPlugin(String pluginId) {
		this.pluginId = pluginId;
		this.resourceLocator = new DefaultResourceLocator(pluginId);
	}
	
	public IResourceLocator getResourceLocator() {
		return resourceLocator;
	}
	
	public String getPluginId() {
		return pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}
}
