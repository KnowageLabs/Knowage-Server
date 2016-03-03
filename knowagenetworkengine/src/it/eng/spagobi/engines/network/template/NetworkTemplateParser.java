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