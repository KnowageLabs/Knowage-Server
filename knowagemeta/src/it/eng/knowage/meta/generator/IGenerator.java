/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.knowage.meta.generator;

import it.eng.knowage.meta.model.ModelObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public interface IGenerator {

	void hideTechnicalResources();

	/**
	 * @param o
	 * @param outputDir
	 * @param isUpdatableMapping
	 */
	void generate(ModelObject o, String outputDir, boolean isUpdatableMapping);

	void generate(ModelObject o, String outputDir);
}
