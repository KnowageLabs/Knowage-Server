/**
 *
 */
package it.eng.spagobi.engines.whatif.template.initializer.impl;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.engines.whatif.template.initializer.AbstractInitializer;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * @author Dragan Pirkovic
 *
 */
public class MondrianSchemaInitializerImpl extends AbstractInitializer {

	public static transient Logger logger = Logger.getLogger(MondrianSchemaInitializerImpl.class);

	public static final String TAG_CUBE = "CUBE";
	public static final String PROP_SCHEMA_REFERENCE = "reference";

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		SourceBean cubeSB = (SourceBean) template.getAttribute(TAG_CUBE);
		logger.debug(TAG_CUBE + ": " + cubeSB);
		Assert.assertNotNull(cubeSB, "Template is missing " + TAG_CUBE + " tag");
		String reference = (String) cubeSB.getAttribute(PROP_SCHEMA_REFERENCE);
		logger.debug(PROP_SCHEMA_REFERENCE + ": " + reference);
		toReturn.setMondrianSchema(reference);
	}

}
