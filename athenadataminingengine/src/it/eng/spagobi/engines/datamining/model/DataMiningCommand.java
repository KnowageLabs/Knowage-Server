/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.datamining.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author Monica Franceschini
 */
public class DataMiningCommand implements Serializable {

	private static final long serialVersionUID = 1L;

	private String scriptName;
	private String name;
	private String mode;
	private String label;
	private List<Output> outputs;
	private List<Variable> variables;
	private String action;
	private Boolean executed;

	public List<Variable> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}

	public Boolean getExecuted() {
		return executed;
	}

	public void setExecuted(Boolean executed) {
		this.executed = executed;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Output> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<Output> outputs) {
		this.outputs = outputs;
	}

}
