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
public class ProfilingUserAttributesInitializerImpl extends AbstractInitializer {

	public static transient Logger logger = Logger.getLogger(ProfilingUserAttributesInitializerImpl.class);

	public static final String TAG_DATA_ACCESS = "DATA-ACCESS";
	public static final String TAG_USER_ATTRIBUTE = "ATTRIBUTE";
	public static final String PROP_USER_ATTRIBUTE_NAME = "name";

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		SourceBean dataAccessSB = (SourceBean) template.getAttribute(TAG_DATA_ACCESS);
		logger.debug(TAG_DATA_ACCESS + ": " + dataAccessSB);
		List<String> attributes = new ArrayList<String>();
		if (dataAccessSB != null) {
			List attributesSB = dataAccessSB.getAttributeAsList(TAG_USER_ATTRIBUTE);
			Iterator it = attributesSB.iterator();
			while (it.hasNext()) {
				SourceBean attributeSB = (SourceBean) it.next();
				logger.debug("Found " + TAG_USER_ATTRIBUTE + " definition :" + attributeSB);
				String name = (String) attributeSB.getAttribute(PROP_USER_ATTRIBUTE_NAME);
				Assert.assertNotNull(name, "Missing [" + PROP_PARAMETER_NAME + "] attribute in user profile attribute");
				attributes.add(name);
			}
		}
		toReturn.setProfilingUserAttributes(attributes);
	}

}
