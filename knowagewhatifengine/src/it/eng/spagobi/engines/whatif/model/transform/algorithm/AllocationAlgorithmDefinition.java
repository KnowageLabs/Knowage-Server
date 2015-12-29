/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it) 
 * 
 * @class Definition of the allocation algorithm. It contains the useful properties to create an allocation algorithm
 */
package it.eng.spagobi.engines.whatif.model.transform.algorithm;

public class AllocationAlgorithmDefinition {
	private final String name;
	private final String className;
	private final boolean defaultAlgorithm;

	public AllocationAlgorithmDefinition(String name, String className, boolean defaultAlgorithm) {
		super();
		this.name = name;
		this.className = className;
		this.defaultAlgorithm = defaultAlgorithm;
	}

	public String getName() {
		return name;
	}

	public String getClassName() {
		return className;
	}

	public boolean isDefaultAlgorithm() {
		return defaultAlgorithm;
	}

}
