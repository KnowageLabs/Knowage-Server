/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
