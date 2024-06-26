/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.commons.resource;

import java.io.File;
import java.net.URL;

import org.eclipse.emf.common.util.ResourceLocator;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface IResourceLocator extends ResourceLocator {
	/** 
	   * Returns the URL from which all resources are based.
	   * @return the URL from which all resources are based.
	   */
	  URL getBaseURL();
	  
	  File getFile(String fileRelativePath);

	  /**
	   * Returns the description that can be used to create the image resource associated with the key.
	   * The description will typically be in the form of a URL to the image data.
	   * Creation of an actual image depends on the GUI environment;
	   * 
	   * @param key the key of the image resource.
	   * @return the description on the image resource.
	   */
	  Object getImage(String key);
	  
	  /**
	   * Return the property value associated with the key. The type of the value is typically a String
	   * 
	   * @param key the key of the property resource.
	   * @return the value of the property
	   */
	  Object getProperty(String key);
	  
	  Object getProperty(String key, Object defaultValue);
	  
	  String getPropertyAsString(String key);
	  String getPropertyAsString(String key, String defaultValue);
	  Integer getPropertyAsInteger(String key);
	  Integer getPropertyAsInteger(String key, Integer defaultValue);
	  
	  /**
	   * Returns the string resource associated with the key, translated to the current locale.
	   * 
	   * @param key the key of the string resource.
	   * @return the string resource associated with the key.
	   */
	  String getString(String key);


	  /**
	   * Returns a string resource associated with the key, and performs substitutions.
	   * 
	   * @param key the key of the string.
	   * @param substitutions the message substitutions.
	   * @return a string resource associated with the key.
	  
	   * @see #getString(String)
	   * @see java.text.MessageFormat#format(String, Object[])
	   */
	  String getString(String key, Object [] substitutions);
}
