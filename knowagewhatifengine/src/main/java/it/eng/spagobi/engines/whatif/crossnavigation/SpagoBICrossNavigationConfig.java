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

package it.eng.spagobi.engines.whatif.crossnavigation;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.template.WhatIfXMLTemplateParser;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


public class SpagoBICrossNavigationConfig {
	private static transient Logger logger = Logger.getLogger(SpagoBICrossNavigationConfig.class);
	
	private List<TargetParameter> parameters = null;
	
	public static final String ID = "cross_navigation_config"; 
	
	private boolean buttonClicked;
	
	private String modelStatus;
	
	/**
	 * Constructor given the CROSS_NAVIGATION node of the xml document template.
	 * @param config: the CROSS_NAVIGATION node of the xml document template
	 */
	public SpagoBICrossNavigationConfig(SourceBean config) {
		logger.debug("Configuration:\n" + config.toXML());
		init(config);
	}
	
		
	public SpagoBICrossNavigationConfig() {
	}

	private void init(SourceBean node){
		parameters = new ArrayList<TargetParameter>();
		List parametersSB = node.getAttributeAsList(WhatIfXMLTemplateParser.TAG_TN_PARAMETERS);
		List parameterSB = ((SourceBean) parametersSB.get(0)).getAttributeAsList(WhatIfXMLTemplateParser.TAG_TN_PARAMETER);
		
		boolean hasParameters = parameterSB != null && !parameterSB.isEmpty();
		
		if (hasParameters) {
			for (int i = 0; i < parameterSB.size(); i++) {
				SourceBean temp = (SourceBean) parameterSB.get(i);
				TargetParameter aParameter = new TargetParameter(temp);
				if (aParameter != null) {
					parameters.add(aParameter);
				}
			}
		}
		

	}
		

	public boolean isButtonClicked() {
		return buttonClicked;
	}

	public void setButtonClicked(boolean buttonClicked) {
		this.buttonClicked = buttonClicked;
	}

	public String getModelStatus() {
		return modelStatus;
	}

	public void setModelStatus(String modelStatus) {
		this.modelStatus = modelStatus;
	}


	public List<TargetParameter> getParameters() {
		return parameters;
	}


	public void setParameters(List<TargetParameter> parameters) {
		this.parameters = parameters;
	}


	
	

	
}

