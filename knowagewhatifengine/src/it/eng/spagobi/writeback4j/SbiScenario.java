/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.writeback4j;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 *         The scenario. It contains the editable measures, and the
 *         configuration for the writeback
 * 
 */
public class SbiScenario implements Serializable {

	private static final long serialVersionUID = -8208743421109211078L;
	private String name;
	private WriteBackEditConfig writebackEditConfig;
	private List<SbiScenarioVariable> variables;

	public SbiScenario(String name) {
		super();
		this.name = name;
	}

	public WriteBackEditConfig getWritebackEditConfig() {
		return writebackEditConfig;
	}

	public void setWritebackEditConfig(WriteBackEditConfig writebackEditConfig) {
		this.writebackEditConfig = writebackEditConfig;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SbiScenarioVariable> getVariables() {
		return variables;
	}

	public void setVariables(List<SbiScenarioVariable> variables) {
		this.variables = variables;
	}

	/**
	 * Get the value of the variable with name variableName. Return null if it
	 * can't find a variable with that name
	 * 
	 * @param variableName
	 * @return the value of the variable or null if it can't find a variable
	 *         with that name
	 */
	public SbiScenarioVariable getVariable(String variableName) {
		if (this.variables != null) {
			for (Iterator<SbiScenarioVariable> iterator = variables.iterator(); iterator.hasNext();) {
				SbiScenarioVariable aVariable = iterator.next();
				if (aVariable.getName().equals(variableName)) {
					return aVariable;
				}
			}
		}
		return null;
	}

	/**
	 * Get the value of the variable with name variableName. Return null if it
	 * can't find a variable with that name
	 * 
	 * @param variableName
	 * @return the value of the variable or null if it can't find a variable
	 *         with that name
	 */
	public Integer getIntVariableValue(String variableName) {
		if (this.variables != null) {
			for (Iterator<SbiScenarioVariable> iterator = variables.iterator(); iterator.hasNext();) {
				SbiScenarioVariable aVariable = iterator.next();
				if (aVariable.getName().equals(variableName)) {
					String o = aVariable.getValue();

				}
			}
		}
		return null;
	}

	/**
	 * Get the value of the variable with name variableName. Return null if it
	 * can't find a variable with that name
	 * 
	 * @param variableName
	 * @return the value of the variable or null if it can't find a variable
	 *         with that name
	 */
	public Double getDoubleVariableValue(String variableName) {
		if (this.variables != null) {
			for (Iterator<SbiScenarioVariable> iterator = variables.iterator(); iterator.hasNext();) {
				SbiScenarioVariable aVariable = iterator.next();
				if (aVariable.getName().equals(variableName)) {
					String o = aVariable.getValue();

				}
			}
		}
		return null;
	}
}
