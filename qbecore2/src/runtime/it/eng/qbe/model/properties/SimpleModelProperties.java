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
package it.eng.qbe.model.properties;

import it.eng.qbe.model.structure.IModelNode;
import it.eng.qbe.model.structure.IModelObject;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.Properties;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SimpleModelProperties implements IModelProperties {
	Properties properties;
	
	public SimpleModelProperties() {
		this(new Properties());
	}	
	
	public SimpleModelProperties(Properties  properties) {
		this.properties = properties;
	}
	
	
	public void putAll(IModelProperties modelProperties) {
		properties.putAll(modelProperties.getProperties());
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	public String getProperty(IModelObject item, String propertyName) {
		String propertyQualifiedName;
		String propertyValue;
		
		propertyQualifiedName = null;
		if(item instanceof IModelNode) {
			propertyQualifiedName = getPropertyQualifiedName( (IModelNode)item, propertyName);
			propertyValue = (String)properties.get( propertyQualifiedName );
			
			if(propertyName.equalsIgnoreCase("label") && propertyValue == null) { // back compatibility
				propertyQualifiedName = getItemQulifier((IModelNode)item);
				propertyValue = (String)properties.get( propertyQualifiedName );
			}
			
		} else {
			propertyQualifiedName = propertyName;
			propertyValue = (String)properties.get( propertyQualifiedName );
		}
		
		
		propertyValue = StringUtilities.isNull( propertyValue )? null: propertyValue.trim();
		return propertyValue;
	}
	
	public String getPropertyQualifiedName(IModelNode item, String propertyName) {
		return getItemQulifier( item ) + "." + propertyName.trim();
	}
	
	protected String getItemQulifier( IModelNode item ) {
		Assert.assertNotNull(item, "Parameter [item] cannot be null");
		Assert.assertNotNull(item.getUniqueName(), "Item [uniqueName] cannot be null [" + item.getName() + "]");
		return item.getUniqueName().replaceAll(":", "/");
	}
}
