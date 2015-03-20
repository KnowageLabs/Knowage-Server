/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.template;

import it.eng.spago.base.SourceBean;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QbeTemplateParser implements IQbeTemplateParser{
	
	Map<String, IQbeTemplateParser> parsers;
	
	static QbeTemplateParser instance;
	
	public static QbeTemplateParser getInstance() {
		if(instance == null) {
			instance = new QbeTemplateParser();
		}
		return instance;
	}
	
	private QbeTemplateParser(){
		parsers = new HashMap();
		parsers.put(SourceBean.class.getName(), new QbeXMLTemplateParser());
		parsers.put(JSONObject.class.getName(), new QbeJSONTemplateParser());
	}
	
	
	public QbeTemplate parse(Object template) {
		QbeTemplate qbeTemplate;
		IQbeTemplateParser parser;
		
		qbeTemplate = null;

		if(template != null ){
			if(!parsers.containsKey(template.getClass().getName())) {
				throw new QbeTemplateParseException("Impossible to parse template of type [" + template.getClass().getName() + "]");
			} else {
				parser = parsers.get(template.getClass().getName());
				qbeTemplate = parser.parse(template);
			}
		}
		
		return qbeTemplate;
	}
}
