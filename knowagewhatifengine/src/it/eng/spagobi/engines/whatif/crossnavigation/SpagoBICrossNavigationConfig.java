/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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

