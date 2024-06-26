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
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.crossnavigation.TargetClickable;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.engines.whatif.template.initializer.AbstractInitializer;

/**
 * @author Dragan Pirkovic
 *
 */
public class TargetsClickableInitializerImpl extends AbstractInitializer {

	public static transient Logger logger = Logger.getLogger(TargetsClickableInitializerImpl.class);

	public static final String TAG_MDX_QUERY = "MDXquery";
	public static final String TAG_MDX_CLICKABLE = "clickable";
	public static final String TAG_CNC_TITLE = "title";
	public static final String TAG_CNC_UNIQUENAME = "uniqueName";
	public static final String TAG_CNC_PARAMETERS = "clickParameter";
	public static final String TAG_CNC_NAME = "name";
	public static final String TAG_CNC_VALUE = "value";

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		SourceBean mdxSB = (SourceBean) template.getAttribute(TAG_MDX_QUERY);
		List clickableNodes = mdxSB.getAttributeAsList(TAG_MDX_CLICKABLE);
		List<TargetClickable> targetsClickable = new ArrayList<TargetClickable>();
		logger.debug(TAG_MDX_CLICKABLE + ": " + clickableNodes);
		if (clickableNodes != null && !clickableNodes.isEmpty()) {
			for (int i = 0; i < clickableNodes.size(); i++) {
				TargetClickable targetClickable = new TargetClickable((SourceBean) clickableNodes.get(i));
				if (targetClickable != null) {
					targetsClickable.add(targetClickable);
				}
			}

			toReturn.setTargetsClickable(targetsClickable);
		}
	}

}
