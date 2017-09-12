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
