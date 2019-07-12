/**
 *
 */
package it.eng.spagobi.engines.whatif.template.initializer.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.engines.whatif.template.initializer.AbstractInitializer;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * @author Dragan Pirkovic
 *
 */
public class ParametersInitializerImpl extends AbstractInitializer {

	public static transient Logger logger = Logger.getLogger(ParametersInitializerImpl.class);

	public static final String TAG_PARAMETER = "parameter";
	public static final String PROP_PARAMETER_ALIAS = "as";

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		SourceBean mdxSB = (SourceBean) template.getAttribute(TAG_MDX_QUERY);
		List<WhatIfTemplate.Parameter> parameters = new ArrayList<WhatIfTemplate.Parameter>();
		List parametersSB = mdxSB.getAttributeAsList(TAG_PARAMETER);
		Iterator it = parametersSB.iterator();
		while (it.hasNext()) {
			SourceBean parameterSB = (SourceBean) it.next();
			logger.debug("Found " + TAG_PARAMETER + " definition :" + parameterSB);
			String name = (String) parameterSB.getAttribute(PROP_PARAMETER_NAME);
			String alias = (String) parameterSB.getAttribute(PROP_PARAMETER_ALIAS);
			Assert.assertNotNull(name, "Missing parameter's " + PROP_PARAMETER_NAME + " attribute");
			Assert.assertNotNull(alias, "Missing parameter's " + PROP_PARAMETER_ALIAS + " attribute");
			WhatIfTemplate.Parameter parameter = toReturn.new Parameter();
			parameter.setName(name);
			parameter.setAlias(alias);
			parameters.add(parameter);
		}
		toReturn.setParameters(parameters);
	}

}
