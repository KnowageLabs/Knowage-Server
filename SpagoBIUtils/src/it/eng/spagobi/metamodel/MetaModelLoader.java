/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.metamodel;

import it.eng.spagobi.meta.model.Model;
import it.eng.spagobi.meta.model.serializer.EmfXmiSerializer;
import it.eng.spagobi.meta.model.serializer.IModelSerializer;

import java.io.File;

/**
 * 
 * This class loads a Model from file system or other sources.
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class MetaModelLoader {
	
	/**
	 * Loads the Metamodel from the file system
	 * @param modelFile the *.sbimodel file
	 * @return Model
	 */
	public static Model load(File modelFile){
		IModelSerializer serializer = new EmfXmiSerializer();
		Model model = serializer.deserialize(modelFile);
		return model;
	}
	

}
