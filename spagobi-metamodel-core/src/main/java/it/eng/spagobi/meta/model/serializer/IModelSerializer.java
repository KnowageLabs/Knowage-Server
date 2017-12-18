/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.serializer;

import it.eng.spagobi.meta.model.Model;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface IModelSerializer {
	void serialize(Model model, File file);
	void serialize(Model model, OutputStream outputStream);
	
	Model deserialize(File file);	
	Model deserialize(InputStream inputStream);	
}
