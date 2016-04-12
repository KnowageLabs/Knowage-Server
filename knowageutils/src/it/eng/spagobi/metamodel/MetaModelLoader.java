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

package it.eng.spagobi.metamodel;

import it.eng.spagobi.meta.model.Model;
import it.eng.spagobi.meta.model.serializer.EmfXmiSerializer;
import it.eng.spagobi.meta.model.serializer.IModelSerializer;

import java.io.File;
import java.io.InputStream;

/**
 *
 * This class loads a Model from file system or other sources.
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class MetaModelLoader {

	/**
	 * Loads the Metamodel from the file system
	 * 
	 * @param modelFile
	 *            the *.sbimodel file
	 * @return Model
	 */
	public static Model load(File modelFile) {
		IModelSerializer serializer = new EmfXmiSerializer();
		Model model = serializer.deserialize(modelFile);
		return model;
	}

	/**
	 * Loads the Metamodel from the Input Stream
	 * 
	 * @param modelInputStream
	 *            the metamodel input stream
	 * @return Model
	 */
	public static Model load(InputStream modelInputStream) {
		IModelSerializer serializer = new EmfXmiSerializer();
		Model model = serializer.deserialize(modelInputStream);
		return model;
	}

}
