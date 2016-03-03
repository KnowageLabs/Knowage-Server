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
package it.eng.spagobi.engines.worksheet.template;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class WorksheetTemplateParser {

	static private Logger logger = Logger.getLogger(WorksheetTemplateParser.class);
	
	Map<String, IWorksheetTemplateParser> parsers;
	
	static WorksheetTemplateParser instance;
	
	public static WorksheetTemplateParser getInstance() {
		if(instance == null) {
			instance = new WorksheetTemplateParser();
		}
		return instance;
	}
	
	private WorksheetTemplateParser(){
		parsers = new HashMap();
		parsers.put(SourceBean.class.getName(), new WorksheetXMLTemplateParser());
	}
	
	
	public WorksheetTemplate parse(Object template,  Map env) {
		
		if(template == null){
			return new WorksheetTemplate();
		}
		
		WorksheetTemplate worksheetTemplate;
		IWorksheetTemplateParser parser;
		
		worksheetTemplate = null;
		
		if(!parsers.containsKey(template.getClass().getName())) {
			throw new SpagoBIEngineRuntimeException("Impossible to parse template of type [" + template.getClass().getName() + "]");
		} else {
			parser = parsers.get(template.getClass().getName());
			worksheetTemplate = parser.parse(template, env);
		}
		return worksheetTemplate;
	}
	
	public boolean hasInnerQbeQuery(Object template) {
		if (template == null) {
			logger.debug("template in input is null, returning false");
			return false;
		}
		if(!parsers.containsKey(template.getClass().getName())) {
			throw new SpagoBIEngineRuntimeException("Impossible to parse template of type [" + template.getClass().getName() + "]");
		} else {
			IWorksheetTemplateParser parser = parsers.get(template.getClass().getName());
			return parser.hasInnerQbeQuery(template);
		}
	}
	
	
}