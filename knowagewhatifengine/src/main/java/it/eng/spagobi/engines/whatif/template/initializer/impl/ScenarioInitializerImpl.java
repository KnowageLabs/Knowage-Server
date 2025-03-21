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
import it.eng.spagobi.writeback4j.SbiScenario;
import it.eng.spagobi.writeback4j.SbiScenarioVariable;

/**
 * @author Dragan Pirkovic
 *
 */
public class ScenarioInitializerImpl extends AbstractInitializer {

	public static transient Logger logger = Logger.getLogger(ScenarioInitializerImpl.class);

	public static final String WRITEBACK_TAG = "WRITEBACK";
	public static final String EDIT_CUBE_ATTRIBUTE = "editCube";
	public static final String VARIABLE_TAG = "VARIABLE";
	public static final String MEASURE_TAG = "MEASURE";

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		logger.debug("IN. loading the scenario");
		SourceBean scenarioSB = (SourceBean) template.getAttribute(TAG_SCENARIO);
		if (scenarioSB != null) {
			logger.debug(TAG_SCENARIO + ": " + scenarioSB);
			String scenarioName = (String) scenarioSB.getAttribute(PROP_NAME);
			SbiScenario scenario = new SbiScenario(scenarioName);

			initScenarioVariables(scenarioSB, scenario);

			logger.debug("Scenario with name " + scenarioName + " successfully loaded");
			toReturn.setScenario(scenario);

		} else {
			logger.debug(TAG_SCENARIO + ": no write back configuration found in the template");
		}
	}


	private static void initScenarioVariables(SourceBean scenarioSB, SbiScenario scenario) {
		logger.debug("IN. loading the scenario variables");
		List<SbiScenarioVariable> variables = new ArrayList<>();

		List<SourceBean> variablesBeans = scenarioSB.getAttributeAsList(VARIABLE_TAG);
		if (variablesBeans != null && variablesBeans.size() > 0) {
			for (int i = 0; i < variablesBeans.size(); i++) {
				String name = (String) variablesBeans.get(i).getAttribute(PROP_NAME);
				String value = (String) variablesBeans.get(i).getAttribute(PROP_VALUE);
				String type = (String) variablesBeans.get(i).getAttribute(PROP_TYPE);
				variables.add(new SbiScenarioVariable(name, value, type));
			}
		}
		scenario.setVariables(variables);
		logger.debug("OUT. loaded " + variables.size() + " scenario variables");
	}

}
