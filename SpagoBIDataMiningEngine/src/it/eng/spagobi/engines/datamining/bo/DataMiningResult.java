/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.datamining.bo;

public class DataMiningResult {
	private String result;
	private String outputType;
	private String variablename;
	private String plotName;

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getOutputType() {
		return outputType;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	public String getVariablename() {
		return variablename;
	}

	public void setVariablename(String variablename) {
		this.variablename = variablename;
	}

}
