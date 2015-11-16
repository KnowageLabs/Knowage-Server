/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.datamining.template;

import it.eng.spago.base.SourceBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Monica Franceschini
 */
public class DataMiningTemplateParser implements IDataMiningTemplateParser {

	Map<String, IDataMiningTemplateParser> parsers;

	static DataMiningTemplateParser instance;

	public static DataMiningTemplateParser getInstance() {
		if (instance == null) {
			instance = new DataMiningTemplateParser();
		}
		return instance;
	}

	private DataMiningTemplateParser() {
		parsers = new HashMap<String, IDataMiningTemplateParser>();
		parsers.put(SourceBean.class.getName(), new DataMiningXMLTemplateParser());
	}

	public DataMiningTemplate parse(Object template) {
		DataMiningTemplate dmTemplate;
		IDataMiningTemplateParser parser;

		dmTemplate = null;

		if (template != null) {
			if (!parsers.containsKey(template.getClass().getName())) {
				throw new DataMiningTemplateParseException("Impossible to parse template of type [" + template.getClass().getName() + "]");
			} else {
				parser = parsers.get(template.getClass().getName());
				dmTemplate = parser.parse(template);
			}
		}

		return dmTemplate;
	}
}
