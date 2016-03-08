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
