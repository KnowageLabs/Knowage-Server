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
package integration;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.engines.whatif.WhatIfEngine;
import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.utilities.engines.EngineConstants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

public abstract class AbstractWhatIfCalculatedMemberTestCase extends TestCase {
	private String catalog;

	public WhatIfEngineInstance getWhatifengineiEngineInstance(String c) {
		SourceBean template;
		try {
			catalog = c;
			template = SourceBean.fromXMLFile(getTemplate());
			return WhatIfEngine.createInstance(template, getEnv());
		} catch (SourceBeanException e) {
			e.printStackTrace();
			assertFalse(true);
		}
		return null;
	}

	public Map getEnv() {

		Map env = new HashMap();
		env.put(EngineConstants.ENV_OLAP_SCHEMA, catalog);
		env.put(EngineConstants.ENV_LOCALE, Locale.ITALIAN);

		return env;
	}

	public abstract String getTemplate();

	public abstract String getCatalogue();

}
