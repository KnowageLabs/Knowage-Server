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
