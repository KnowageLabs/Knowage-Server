/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.datamining.model;

import java.io.Serializable;

/**
 * @author Monica Franceschini
 */
public class DataMiningScript implements Serializable {

	private static final long serialVersionUID = 1L;

	private String mode;
	private String name;
	private String datasets;
	private String code;
	private String label;
	private String libraries;

	public String getLibraries() {
		return libraries;
	}

	public void setLibraries(String libraries) {
		this.libraries = libraries;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDatasets() {
		return datasets;
	}

	public void setDatasets(String datasets) {
		this.datasets = datasets;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
