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
