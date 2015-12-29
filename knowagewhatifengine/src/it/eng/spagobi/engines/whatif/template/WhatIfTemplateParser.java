/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.whatif.template;

import it.eng.spago.base.SourceBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public class WhatIfTemplateParser implements IWhatIfTemplateParser {

	Map<String, IWhatIfTemplateParser> parsers;

	static WhatIfTemplateParser instance;

	public static WhatIfTemplateParser getInstance() {
		if (instance == null) {
			instance = new WhatIfTemplateParser();
		}
		return instance;
	}

	private WhatIfTemplateParser() {
		parsers = new HashMap<String, IWhatIfTemplateParser>();
		parsers.put(SourceBean.class.getName(), new WhatIfXMLTemplateParser());
	}

	public WhatIfTemplate parse(Object template) {
		WhatIfTemplate qbeTemplate;
		IWhatIfTemplateParser parser;

		qbeTemplate = null;

		if (template != null) {
			if (!parsers.containsKey(template.getClass().getName())) {
				throw new WhatIfTemplateParseException(
						"Impossible to parse template of type ["
								+ template.getClass().getName() + "]");
			} else {
				parser = parsers.get(template.getClass().getName());
				qbeTemplate = parser.parse(template);
			}
		}

		return qbeTemplate;
	}
}
