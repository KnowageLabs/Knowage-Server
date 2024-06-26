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
package it.eng.qbe.model.properties.initializer;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.properties.ModelPropertiesMeta;
import it.eng.qbe.model.properties.ModelPropertyMeta;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelObject;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.model.structure.ModelCalculatedField;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Iterator;
import java.util.List;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SimpleModelStructurePropertiesInitializer implements IModelStructurePropertiesInitializer {
		
	IDataSource dataSource;
	IModelProperties modelProperties;
	
	
	public SimpleModelStructurePropertiesInitializer(IDataSource dataSource) {
		this.dataSource =  dataSource;
		modelProperties = dataSource.getConfiguration().loadModelProperties();
	}
	

	public void addProperties(IModelObject item) {
		if(item instanceof IModelEntity) {
			addModelEntityProperties( (IModelEntity)item );
		} else if (item instanceof IModelField) {
			addModelFieldProperties( (IModelField)item );
		} else if (item instanceof IModelStructure) {
			addModelProperties( (IModelStructure)item );
		}
	}
	
	private void addModelProperties(IModelStructure item) {
		ModelPropertyMeta property;
		String propertyValue;
		
		for (int i = 0; i < ModelPropertiesMeta.globalProperties.length; i++) {
			property = ModelPropertiesMeta.globalProperties[i];
			propertyValue = modelProperties.getProperty(item, property.getName());
			
			// property not set
			if(propertyValue == null) {
				if(property.isOptional() == false) {
					throw new SpagoBIRuntimeException("Impossible to initialize property [" + property.getName() + "] of structure [" + item.getName() + "]");
				}
				propertyValue = property.getDefaultValue();
			} 
			
			item.getProperties().put(property.getName(), propertyValue);
		}
	}

	protected void addModelEntityProperties(IModelEntity item) {
		ModelPropertyMeta property;
		String propertyValue;
		
		for (int i = 0; i < ModelPropertiesMeta.entityProperties.length; i++) {
			property = ModelPropertiesMeta.entityProperties[i];
			propertyValue = modelProperties.getProperty(item, property.getName());
			
			// property not set
			if(propertyValue == null) {
				if(property.isOptional() == false) {
					throw new SpagoBIRuntimeException("Impossible to initialize property [" + property.getName() + "] of entity [" + item.getUniqueName() + "]");
				}
				propertyValue = property.getDefaultValue();
			} 
			
			// property not set + property default value not set
			if(propertyValue == null && property.isInherited()) {
				propertyValue = getInheritedProperty(item, property.getName());
			}
			
			item.getProperties().put(property.getName(), propertyValue);
		}
	}
	
	protected void addModelFieldProperties(IModelField item) {
		ModelPropertyMeta property;
		String propertyValue;
		
		for (int i = 0; i < ModelPropertiesMeta.fieldProperties.length; i++) {
			property = ModelPropertiesMeta.fieldProperties[i];
			propertyValue = modelProperties.getProperty(item, property.getName());
			
			// property not set
			if(propertyValue == null) {
				if(property.isOptional() == false) {
					throw new SpagoBIRuntimeException("Impossible to initialize property [" + property.getName() + "] of field [" + item.getUniqueName() + "]");
				}
				propertyValue = property.getDefaultValue();
			}
			
			// property not set + property default value not set
			if(propertyValue == null && property.isInherited()) {
				propertyValue = getInheritedProperty(item, property.getName());
			}
			
			item.getProperties().put(property.getName(), propertyValue);
		}
	}
	
	// TODO create method getRootItem in IDataMartItem interface and move some code there
	protected String getInheritedProperty(IModelEntity item, String propertyName) {
		Assert.assertUnreachable("Property [" + propertyName + "] of entity [" + item.getName()+ "] cannot be inehritated");
		String propertyValue;
		IModelEntity rootEntity = item.getStructure().getRootEntity(item);
		Assert.assertNotNull(rootEntity, "Impossible to find root entity of entity [" + item.getName() + "]");
		propertyValue = modelProperties.getProperty(rootEntity, propertyName);
		
		return propertyValue;
	}
	
	// TODO create method getRootItem in IDataMartItem interface and move some code there
	protected String getInheritedProperty(IModelField item, String propertyName) {
		String propertyValue;
		IModelField rootField = null;
		IModelEntity rootEntity = item.getStructure().getRootEntity(item.getParent());
		if(rootEntity == null) {
			rootEntity = item.getStructure().getRootEntity(item.getParent());
			Assert.assertUnreachable("rootEntity for field [" + item.getName() + "] cannot be null");
		}
		
		List fields = null;
		if(item instanceof ModelCalculatedField) {
			fields = rootEntity.getCalculatedFields();
		} else {
			fields = rootEntity.getAllFields();
		}
		Iterator<IModelField> it = fields.iterator();
		while (it.hasNext()) {
			IModelField field = it.next();
			if (field.getName().equals(item.getName())) {
				rootField = field;
				break;
			}
		}
		Assert.assertNotNull(rootField, "Impossible to find root field of field [" + item.getName() + "]");
		propertyValue = modelProperties.getProperty(rootField, propertyName);
		
		return propertyValue;
	}
	

	
	
	
	

}
