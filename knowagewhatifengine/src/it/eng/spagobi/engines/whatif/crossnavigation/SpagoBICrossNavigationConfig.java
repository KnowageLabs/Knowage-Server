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
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;


public class SpagoBICrossNavigationConfig {
	private static transient Logger logger = Logger.getLogger(SpagoBICrossNavigationConfig.class);
	
	private List<Target> targets = null;
	
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
		targets = new ArrayList<Target>();
		List targetNodes = node.getAttributeAsList("TARGET");
		if (targetNodes != null && !targetNodes.isEmpty()) {
			for (int i = 0; i < targetNodes.size(); i++) {
				Target target = new Target((SourceBean) targetNodes.get(i));
				if (target != null) {
					targets.add(target);
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

	public List<Target> getTargets() {
		return targets;
	}

	public void setTargets(List<Target> targets) {
		this.targets = targets;
	}
	
	
	

	
}

