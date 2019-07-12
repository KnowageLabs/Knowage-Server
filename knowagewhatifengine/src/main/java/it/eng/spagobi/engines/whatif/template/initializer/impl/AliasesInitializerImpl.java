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
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.engines.whatif.template.initializer.AbstractInitializer;
import it.eng.spagobi.writeback4j.SbiAlias;
import it.eng.spagobi.writeback4j.SbiAliases;

/**
 * @author Dragan Pirkovic
 *
 */
public class AliasesInitializerImpl extends AbstractInitializer {

	public static transient Logger logger = Logger.getLogger(AliasesInitializerImpl.class);

	public static final String DIMENSION_TAG = "DIMENSION";
	public static final String HIERARCHY_TAG = "HIERARCHY";
	public static final String TAG_ALIASES = "ALIASES";
	public static final String PROP_ALIAS = "alias";
	public static final String ALIAS_TAG = "ALIAS";

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		logger.debug("IN. loading the aliases for the what-if model");
		SourceBean aliasesSB = (SourceBean) template.getAttribute(TAG_ALIASES);
		if (aliasesSB != null) {
			logger.debug(TAG_ALIASES + ": " + aliasesSB);
			SbiAliases aliases = new SbiAliases();

			initModelAliases(aliasesSB, aliases);

			logger.debug("Aliases successfully loaded");
			toReturn.setAliases(aliases);

		} else {
			logger.debug(TAG_SCENARIO + ": no write back configuration found in the template");
		}
	}

	private void initModelAliases(SourceBean aliasesSB, SbiAliases aliases) {
		logger.debug("IN. loading the aliases");
		List<SbiAlias> aliasesFound = new ArrayList<SbiAlias>();

		List<SourceBean> aliasesBeans = aliasesSB.getAttributeAsList(DIMENSION_TAG);
		if (aliasesBeans != null && aliasesBeans.size() > 0) {
			for (int i = 0; i < aliasesBeans.size(); i++) {
				String name = (String) aliasesBeans.get(i).getAttribute(PROP_NAME);
				String alias = (String) aliasesBeans.get(i).getAttribute(PROP_ALIAS);
				String type = DIMENSION_TAG;
				aliasesFound.add(new SbiAlias(name, alias, type));
			}
		}

		aliasesBeans = aliasesSB.getAttributeAsList(HIERARCHY_TAG);
		if (aliasesBeans != null && aliasesBeans.size() > 0) {
			for (int i = 0; i < aliasesBeans.size(); i++) {
				String name = (String) aliasesBeans.get(i).getAttribute(PROP_NAME);
				String alias = (String) aliasesBeans.get(i).getAttribute(PROP_ALIAS);
				String type = HIERARCHY_TAG;
				aliasesFound.add(new SbiAlias(name, alias, type));
			}
		}

		// TODO add generic aliases
		aliasesBeans = aliasesSB.getAttributeAsList(ALIAS_TAG);
		if (aliasesBeans != null && aliasesBeans.size() > 0) {
			for (int i = 0; i < aliasesBeans.size(); i++) {
				String name = (String) aliasesBeans.get(i).getAttribute(PROP_NAME);
				String alias = (String) aliasesBeans.get(i).getAttribute(PROP_ALIAS);
				String type = ALIAS_TAG;
				aliasesFound.add(new SbiAlias(name, alias, type));
			}
		}

		aliases.setAliases(aliasesFound);
		logger.debug("OUT. loaded " + aliasesFound.size() + " aliases");
	}

}
