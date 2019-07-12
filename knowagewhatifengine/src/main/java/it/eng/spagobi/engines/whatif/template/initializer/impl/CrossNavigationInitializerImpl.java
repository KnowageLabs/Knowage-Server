/**
 *
 */
package it.eng.spagobi.engines.whatif.template.initializer.impl;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.crossnavigation.SpagoBICrossNavigationConfig;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.engines.whatif.template.initializer.AbstractInitializer;

/**
 * @author Dragan Pirkovic
 *
 */
public class CrossNavigationInitializerImpl extends AbstractInitializer {

	public static transient Logger logger = Logger.getLogger(CrossNavigationInitializerImpl.class);

	public static final String TAG_CROSS_NAVIGATION = "CROSS_NAVIGATION";

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		SourceBean crossNavigation = (SourceBean) template.getAttribute(TAG_CROSS_NAVIGATION);
		if (crossNavigation != null) {
			SpagoBICrossNavigationConfig cninfo = new SpagoBICrossNavigationConfig(crossNavigation);
			toReturn.setCrossNavigation(cninfo);
			if (toReturn.getToolbarVisibleButtons() == null) {
				toReturn.setToolbarVisibleButtons(new ArrayList<String>());
			}
			toReturn.getToolbarVisibleButtons().add(new String("BUTTON_CROSS_NAVIGATION"));

		}
	}

}
