/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.container;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface IPropertiesContainer {
	Object getProperty(String propertyName);
	void setProperty(String propertyName, Object propertyValue);
	boolean containsProperty(String propertyName);
	
	String getPropertyAsString(String propertyName);
	
	Boolean getPropertyAsBoolean(String attrName);
	
	Boolean getPropertyAsBoolean(String propertyName, boolean defaultValue) ;
	
	Integer getPropertyAsInteger(String propertyName);
	
	List getPropertyAsStringList(String propertyName) ;
	
	List getPropertyAsCsvStringList(String propertyName, String separator) ;
	
	JSONObject getPropertyAsJSONObject(String propertyName) ;
}
