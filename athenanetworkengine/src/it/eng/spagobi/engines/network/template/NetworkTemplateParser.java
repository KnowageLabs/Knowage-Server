/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.network.template;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.HashMap;
import java.util.Map;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class NetworkTemplateParser {

	Map<String, INetworkTemplateParser> parsers;
	
	static NetworkTemplateParser instance;
	
	public static NetworkTemplateParser getInstance() {
		if(instance == null) {
			instance = new NetworkTemplateParser();
		}
		return instance;
	}
	
	private NetworkTemplateParser(){
		parsers = new HashMap();
		parsers.put(String.class.getName(), new NetworkXMLTemplateParser());
	}
	
	
	public NetworkTemplate parse(Object template,  Map env) {
		
		if(template == null){
			return new NetworkTemplate();
		}
		
		NetworkTemplate networkTemplate;
		INetworkTemplateParser parser;
		
		networkTemplate = null;
		
		if(!parsers.containsKey(template.getClass().getName())) {
			throw new SpagoBIEngineRuntimeException("Impossible to parse template of type [" + template.getClass().getName() + "]");
		} else {
			parser = parsers.get(template.getClass().getName());
			networkTemplate = parser.parse(template, env);
		}
		return networkTemplate;
	}
}