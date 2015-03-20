/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
