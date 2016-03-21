/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.initializer.properties;

import it.eng.knowage.meta.model.ModelFactory;
import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.ModelProperty;
import it.eng.knowage.meta.model.ModelPropertyCategory;
import it.eng.knowage.meta.model.ModelPropertyType;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalForeignKey;
import it.eng.knowage.meta.model.physical.PhysicalModel;
import it.eng.knowage.meta.model.physical.PhysicalPrimaryKey;
import it.eng.knowage.meta.model.physical.PhysicalTable;

/**
 * @deprecated
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class PhysicalModelDefaultPropertiesInitializer implements IPropertiesInitializer {
	
	// Model property names
	public static final String MODEL_CONNECTION_NAME = "connection.name";
	public static final String MODEL_CONNECTION_URL = "connection.url";
	public static final String MODEL_CONNECTION_USERNAME = "connection.username";
	public static final String MODEL_CONNECTION_PASSWORD = "connection.password";
	public static final String MODEL_CONNECTION_DATABASENAME = "connection.databasename";
	public static final String MODEL_CONNECTION_DRIVER = "connection.driver";
	
	public static final String MODEL_CONNECTION_DATABASEQUOTESTRING = "connection.databasequotestring";




	
	static public ModelFactory FACTORY = ModelFactory.eINSTANCE;
	
	public void addProperties(ModelObject o) {
		
		if(o instanceof PhysicalColumn) {
			initColumnProperties((PhysicalColumn)o);
		} else if(o instanceof PhysicalModel) {
			initModelProperties((PhysicalModel)o);
		} else if(o instanceof PhysicalForeignKey) {
			initForeignKeyProperty((PhysicalForeignKey)o);
		} else if(o instanceof PhysicalPrimaryKey) {
			initPrimaryKeyProperties((PhysicalPrimaryKey)o);
		} else if(o instanceof PhysicalTable) {
			initTableProperties((PhysicalTable)o);
		} else {
			
		}
	}
	
	
	private ModelPropertyCategory createPropertyCategoryIfNotExist(PhysicalModel o, String id, String name, String description) {
		ModelPropertyCategory connectionCategory = null;
		
		if (o.getParentModel() != null){
			connectionCategory =  o.getParentModel().getPropertyCategory( id );
		}
		if(connectionCategory == null) {
			connectionCategory = FACTORY.createModelPropertyCategory();
			connectionCategory.setName( name );
			connectionCategory.setDescription( description );
			if(o.getParentModel() != null) {
				o.getParentModel().getPropertyCategories().add(connectionCategory);	
			}
		}   
		
		return connectionCategory;
	}
	
	private ModelPropertyType createPropertyTypeIfNotExist(PhysicalModel o, ModelPropertyCategory category, String id, String name, String description, String defaultValue) {
		ModelPropertyType propertyType;
		
		propertyType = null;
		
		if(o.getParentModel() != null) {
			propertyType = o.getParentModel().getPropertyType( id );
		}
		if(propertyType == null) {
			propertyType = FACTORY.createModelPropertyType();
			propertyType.setId( id );
			propertyType.setName( name );
			propertyType.setDescription( description );
			propertyType.setCategory( category );
			propertyType.setDefaultValue( defaultValue );

			if(o.getParentModel() != null) {
				o.getParentModel().getPropertyTypes().add(propertyType);
			}
		}
		
		return propertyType;
	}
	
	private ModelProperty createProperty(PhysicalModel o, ModelPropertyType propertyType) {
		ModelProperty property;
		
		property = FACTORY.createModelProperty();
		property.setPropertyType(propertyType);
		o.getProperties().put(property.getPropertyType().getId(), property);
		
		return property;
	}
	
	private void initModelProperties(PhysicalModel o) {
		ModelPropertyCategory connectionCategory;
		ModelPropertyType propertyType;
		ModelProperty property;
		
		// create connection category
		connectionCategory =  createPropertyCategoryIfNotExist(o, "Connection", "Connection", "Connection properties");
		
		// create connection name property
		propertyType = this.createPropertyTypeIfNotExist(o, connectionCategory, MODEL_CONNECTION_NAME
				, "Connection Name", "Data Source Connection Name", "connection name");
		createProperty(o, propertyType);
	
		// create connection url property
		propertyType = this.createPropertyTypeIfNotExist(o, connectionCategory, MODEL_CONNECTION_URL
				, "Connection Url", "Data Source Connection Url", "url");
		createProperty(o, propertyType);
		
		// create connection username property
		propertyType = this.createPropertyTypeIfNotExist(o, connectionCategory, MODEL_CONNECTION_USERNAME
				, "Connection Username", "Data Source Connection Username", "username");
		createProperty(o, propertyType);
		
		// create connection password property
		propertyType = this.createPropertyTypeIfNotExist(o, connectionCategory, MODEL_CONNECTION_PASSWORD
				, "Connection Password", "Data Source Connection Password", "password");
		createProperty(o, propertyType);
		
		// create connection database name property
		propertyType = this.createPropertyTypeIfNotExist(o, connectionCategory, MODEL_CONNECTION_DATABASENAME
				, "Connection DatabaseName", "Data Source Connection Database Name", "databasename");
		createProperty(o, propertyType);
		
		// create connection driver property
		propertyType = this.createPropertyTypeIfNotExist(o, connectionCategory, MODEL_CONNECTION_DRIVER
				, "Connection driver", "Data Source Connection driver", "driver");
		createProperty(o, propertyType);

		// create connection password property
		propertyType = this.createPropertyTypeIfNotExist(o, connectionCategory, MODEL_CONNECTION_DATABASEQUOTESTRING
				, "Connection DatabaseQuoteString", "Data Source Connection Database Quote String", " ");
		createProperty(o, propertyType);
	}

	private void initTableProperties(PhysicalTable o) {
		
	}
	
	private void initColumnProperties(PhysicalColumn o) {
		
	}
	
	private void initPrimaryKeyProperties(PhysicalPrimaryKey o) {
		
	}

	private void initForeignKeyProperty(PhysicalForeignKey o) {
	
	}
}
