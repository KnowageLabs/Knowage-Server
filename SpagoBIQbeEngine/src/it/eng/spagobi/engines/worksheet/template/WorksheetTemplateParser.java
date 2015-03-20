/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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