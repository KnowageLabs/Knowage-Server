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
public class PaginationInitializerImpl extends AbstractInitializer {

	public static transient Logger logger = Logger.getLogger(PaginationInitializerImpl.class);

	public static final String TAG_PAGINATION = "pagination";

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		logger.debug("IN. loading the configuration for a stand alone execution");
		boolean paginationSB = false;
		SourceBean attribute = (SourceBean) template.getAttribute(TAG_PAGINATION);
		if (attribute != null) {
			paginationSB = Boolean.parseBoolean(attribute.getCharacters());
		}

		toReturn.setPagination(paginationSB);
	}

}
