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
package it.eng.spagobi.engines.datamining.model;

import java.util.List;

public class Output {
	/**
	 * outputType: possible values image,text or script
	 */
	private String outputType;
	/**
	 * outputName: name to be used by plot image (internally)
	 */
	private String outputName;
	/**
	 * outputValue: value of the variable to be displayed in the result
	 */
	private String outputValue;
	/**
	 * outputDataType: data type of the result (internally)
	 */
	private String outputDataType;
	/**
	 * ouputLabel: used by layout
	 */
	private String ouputLabel;
	/**
	 * outputMode: manual or auto
	 */
	private String outputMode;
	/**
	 * outputFunction: used by image outputType for simple functions as plot,
	 * biplot ecc
	 */	
	private String outputFunction;
	
	private List<Variable> variables;

	public List<Variable> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}

	public String getOutputFunction() {
		return outputFunction;
	}

	public void setOutputFunction(String outputFunction) {
		this.outputFunction = outputFunction;
	}

	public String getOuputLabel() {
		return ouputLabel;
	}

	public void setOuputLabel(String ouputLabel) {
		this.ouputLabel = ouputLabel;
	}

	public String getOutputMode() {
		return outputMode;
	}

	public void setOutputMode(String outputMode) {
		this.outputMode = outputMode;
	}

	public String getOutputType() {
		return outputType;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}

	public String getOutputValue() {
		return outputValue;
	}

	public void setOutputValue(String outputValue) {
		this.outputValue = outputValue;
	}

	public String getOutputDataType() {
		return outputDataType;
	}

	public void setOutputDataType(String outputDataType) {
		this.outputDataType = outputDataType;
	}

}
