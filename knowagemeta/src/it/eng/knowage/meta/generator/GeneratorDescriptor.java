/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.generator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Used for lazy initialization of the related plugin
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class GeneratorDescriptor {
	IConfigurationElement configElement;
	
	String id;
	String name;
	String description;
	String clazz;
	String icon;
	
	private static final String ATT_ID = "id";
	private static final String ATT_NAME = "name";
	private static final String ATT_DESCRIPTION = "description";
	private static final String ATT_CLASS = "class";
	private static final String ATT_ICON = "icon";
	
	public GeneratorDescriptor(IConfigurationElement configElement) {
		this.configElement = configElement;
		
		this.id = getAttribute(configElement, ATT_ID, null);
		if(this.id == null) {
			throw new IllegalArgumentException("Missing " + ATT_ID + " attribute");
		}
		
		this.name = getAttribute(configElement, ATT_NAME, null);
		if(this.id == null) {
			throw new IllegalArgumentException("Missing " + ATT_ID + " attribute");
		}

		this.clazz = getAttribute(configElement, ATT_CLASS, null);
		if(this.id == null) {
			throw new IllegalArgumentException("Missing " + ATT_ID + " attribute");
		}
		
		this.description = getAttribute(configElement, ATT_DESCRIPTION, null);
		this.icon = getAttribute(configElement, ATT_ICON, null);
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getClazz() {
		return clazz;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public IGenerator getGenerator() throws CoreException {
		return (IGenerator)configElement.createExecutableExtension("class");
		
	}
	
	private String getAttribute(
			IConfigurationElement configElem,
			String name,
			String defaultValue
	) {
		String value = configElem.getAttribute(name);
		if (value != null) return value;
		return defaultValue;
	}
}
