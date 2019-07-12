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
public class MdxQueryInitializerImpl extends AbstractInitializer {

	public static transient Logger logger = Logger.getLogger(MdxQueryInitializerImpl.class);

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		SourceBean mdxSB = (SourceBean) template.getAttribute(TAG_MDX_QUERY);
		logger.debug(TAG_MDX_QUERY + ": " + mdxSB);
		Assert.assertNotNull(mdxSB, "Template is missing " + TAG_MDX_QUERY + " tag");
		toReturn.setMdxQuery(mdxSB.getCharacters());

	}

}
