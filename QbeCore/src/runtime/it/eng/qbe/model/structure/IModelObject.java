/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure;

import java.util.Map;

/**
 * All objects in the model structure, even the <code>IModelStructure</code>, 
 * extend this interface
 * 
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface IModelObject {
	
	
	long getId();
	String getName();
	void setName(String name);
	
	void setProperties(Map<String,Object> properties);
	
	Map<String,Object> getProperties();
	Object getProperty(String name);
	String getPropertyAsString(String name);
	boolean getPropertyAsBoolean(String name);
	int getPropertyAsInt(String name);

}
