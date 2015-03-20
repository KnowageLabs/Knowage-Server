/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.persist.temporarytable;

import java.util.Map;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public interface INativeDBTypeable {

	public static final String SIZE ="size";
	public static final String PRECISION ="precision";
	public static final String SCALE ="scale";
	public static final String DECIMAL ="decimal";
	
	/**
	 * Translate the java type in input with the corresponding native db type
	 * @param typeJavaName the java type
	 * @param properties the properties (for example scale, mantissa length, scale)
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	String getNativeTypeString(String typeJavaName, Map properties);
	
}
