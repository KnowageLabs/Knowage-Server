/**
 *
 */
package it.eng.spagobi.engines.whatif.template.initializer.impl;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.engines.whatif.template.initializer.AbstractInitializer;

/**
 * @author Dragan Pirkovic
 *
 */
public class MondrianMdxQueryInitializerImpl extends AbstractInitializer {

	public static transient Logger logger = Logger.getLogger(MondrianMdxQueryInitializerImpl.class);

	public static final String TAG_MDX_MONDRIAN_QUERY = "MDXMondrianQuery";

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		SourceBean mdxMondrianSB = (SourceBean) template.getAttribute(TAG_MDX_MONDRIAN_QUERY);
		logger.debug(TAG_MDX_MONDRIAN_QUERY + ": " + mdxMondrianSB);
		String mdxMondrianQuery = mdxMondrianSB.getCharacters();
		toReturn.setMondrianMdxQuery(mdxMondrianQuery);
	}

}
