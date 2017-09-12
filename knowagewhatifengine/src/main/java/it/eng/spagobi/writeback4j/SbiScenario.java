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
