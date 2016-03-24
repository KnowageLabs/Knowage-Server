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
package it.eng.knowage.meta.initializer.properties;

import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelFactory;
import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.ModelProperty;
import it.eng.knowage.meta.model.ModelPropertyCategory;
import it.eng.knowage.meta.model.ModelPropertyType;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessIdentifier;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;

/**
 * @deprecated
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class BusinessModelDefaultPropertiesInitializer implements IPropertiesInitializer {
	
	// Model property names
	public static final String MODEL_PACKAGE = "structural.package";
	
	// Table property names
	
	//...
	
	// Column property names
	
	public static final String COLUMN_AGGTYPE = "structural.aggtype";
	public static final String COLUMN_DATATYPE = "structural.datatype";
	
	public static final String COLUMN_ALIGNMENT = "style.alignment";
	
	public static final String COLUMN_PHYSICAL_TABLE = "physical.physicaltable";
	
	// Relationship property names
	
	
	static public ModelFactory FACTORY = ModelFactory.eINSTANCE;
	
	public void addProperties(ModelObject o) {
		
		if(o instanceof BusinessModel) {
			initModelProperties((BusinessModel)o);
		} else if(o instanceof BusinessTable) {
			initTableProperties((BusinessTable)o);
		} else if(o instanceof BusinessColumn) {
			initColumnProperties((SimpleBusinessColumn)o);
		} else if(o instanceof BusinessIdentifier) {
			initIdentifierProperties((BusinessIdentifier)o);
		} else if(o instanceof BusinessRelationship) {
			initRelationshipProperties((BusinessRelationship)o);
		} else {
			
		}
	}
	
	private void initModelProperties(BusinessModel o) {
        ModelPropertyCategory structuralCategory;
        ModelPropertyType propertyType;
        ModelProperty property;
       
        // if doesn't exist create 'structural' category
        structuralCategory =  o.getParentModel().getPropertyCategory("Structural");
        if(structuralCategory == null) {
            structuralCategory = FACTORY.createModelPropertyCategory();
            structuralCategory.setName("Structural");
            structuralCategory.setDescription("Structural properties");
            o.getParentModel().getPropertyCategories().add(structuralCategory);	
        }    
        
        // Create the new property type and add it to the structural category
        propertyType = null;
       
        if(o.getParentModel() != null) {
        	propertyType = o.getParentModel().getPropertyType(MODEL_PACKAGE);
        }
        if(propertyType == null) {
            propertyType = FACTORY.createModelPropertyType();
            propertyType.setId( MODEL_PACKAGE );
            propertyType.setName("Package Name");
            propertyType.setDescription("Package name used in JPA mapping");
            propertyType.setCategory(structuralCategory);
            propertyType.setDefaultValue("it.eng.knowage.meta");
            
            if(o.getParentModel() != null) {
            	o.getParentModel().getPropertyTypes().add(propertyType);
            }
        }
       
        // add a property of type MODEL_PACKAGE to the model object
        property = FACTORY.createModelProperty();
        property.setPropertyType(propertyType);
        o.getProperties().put(property.getPropertyType().getId(), property);
            
	}

	private void initTableProperties(BusinessTable o) {
		
	}
	
	private void initColumnProperties(SimpleBusinessColumn o) {
		Model rootModel;
		ModelPropertyType propertyType;
		ModelProperty property;
		ModelPropertyCategory structuralCategory, styleCategory, otherCategory;
		
		rootModel = null;
		
		if(o.getTable() != null && o.getTable().getModel() != null) {
			rootModel = o.getTable().getModel().getParentModel();
		}
		
		//**** Structural Category ****
		structuralCategory =  o.getTable().getModel().getParentModel().getPropertyCategory("Structural");
		if(structuralCategory == null) {
			structuralCategory = FACTORY.createModelPropertyCategory();
			structuralCategory.setName("Structural");
			structuralCategory.setDescription("Structural properties");
			o.getTable().getModel().getParentModel().getPropertyCategories().add(structuralCategory);
		}		
		
		//**** Style Category ****
		styleCategory =  o.getTable().getModel().getParentModel().getPropertyCategory("Style");
		if(styleCategory == null) {
			styleCategory = FACTORY.createModelPropertyCategory();
			styleCategory.setName("Style");
			styleCategory.setDescription("Style properties");
			o.getTable().getModel().getParentModel().getPropertyCategories().add(styleCategory);
		}
		
		//**** Other Category ****
		otherCategory =  o.getTable().getModel().getParentModel().getPropertyCategory("Other");
		if(otherCategory == null) {
			otherCategory = FACTORY.createModelPropertyCategory();
			otherCategory.setName("Physical Reference");
			otherCategory.setDescription("The reference to the original physical object");
			o.getTable().getModel().getParentModel().getPropertyCategories().add(otherCategory);
		}
		
		
		// AGGREGATION TYPE
		propertyType = null;
		
		if(rootModel != null) propertyType = rootModel.getPropertyType(COLUMN_AGGTYPE);
		if(propertyType == null) {
			propertyType = FACTORY.createModelPropertyType();
			propertyType.setId( COLUMN_AGGTYPE );
			propertyType.setName("Agregation Type");
			propertyType.setDescription("The preferred agregation type for the give column (COUNT, SUM, AVG, MAX, MIN)");
			propertyType.setCategory(structuralCategory);
			propertyType.getAdmissibleValues().add("COUNT");
			propertyType.getAdmissibleValues().add("SUM");
			propertyType.getAdmissibleValues().add("AVG");
			propertyType.getAdmissibleValues().add("MAX");
			propertyType.getAdmissibleValues().add("MIN");
			propertyType.setDefaultValue("COUNT");
			
			if(rootModel != null) rootModel.getPropertyTypes().add(propertyType);
		}
		
		property = FACTORY.createModelProperty();
		property.setPropertyType(propertyType);
		o.getProperties().put(property.getPropertyType().getId(), property);
		
		// DATA TYPE
		propertyType = null;
		
		if(rootModel != null) propertyType = rootModel.getPropertyType(COLUMN_DATATYPE);
		if(propertyType == null) {
			propertyType = FACTORY.createModelPropertyType();
			propertyType.setId( COLUMN_DATATYPE );
			propertyType.setName("Data Type");
			propertyType.setDescription("The data type of the given column (VARCHAR, INTEGER, DOUBLE, ...)");
			propertyType.setCategory(structuralCategory);
			propertyType.getAdmissibleValues().add("VARCHAR");
			propertyType.getAdmissibleValues().add("INTEGER");
			propertyType.getAdmissibleValues().add("DOUBLE");
			propertyType.getAdmissibleValues().add("DATE");
			propertyType.getAdmissibleValues().add("TIMESTAMP");
			// ...
			propertyType.setDefaultValue("VARCHAR");
			
			if(rootModel != null) rootModel.getPropertyTypes().add(propertyType);
		}
		
		property = FACTORY.createModelProperty();
		property.setPropertyType(propertyType);
		o.getProperties().put(property.getPropertyType().getId(), property);
		
		
		// ALIGNMENT TYPE
		if(rootModel != null) propertyType = rootModel.getPropertyType(COLUMN_ALIGNMENT);
		if(propertyType == null) {
			propertyType = FACTORY.createModelPropertyType();
			propertyType.setId( COLUMN_ALIGNMENT );
			propertyType.setName("Alignment Type");
			propertyType.setDescription("The preferred alignment type for the give column (LEFT, CENTER, RIGHT, JUSTIFIED)");
			propertyType.setCategory(structuralCategory);
			propertyType.getAdmissibleValues().add("LEFT");
			propertyType.getAdmissibleValues().add("CENTER");
			propertyType.getAdmissibleValues().add("RIGHT");
			propertyType.getAdmissibleValues().add("JUSTIFIED");
			propertyType.setDefaultValue("LEFT");
			
			if(rootModel != null) rootModel.getPropertyTypes().add(propertyType);
		}
		
		property = FACTORY.createModelProperty();
		property.setPropertyType(propertyType);
		o.getProperties().put(property.getPropertyType().getId(), property);
		
		// Column Physical Table TYPE
		propertyType = null;
		
		if(rootModel != null) propertyType = rootModel.getPropertyType(COLUMN_PHYSICAL_TABLE);
		if(propertyType == null) {
			propertyType = FACTORY.createModelPropertyType();
			propertyType.setId(COLUMN_PHYSICAL_TABLE);
			propertyType.setName("Physical Table");
			propertyType.setDescription("The original physical table of this column");
			propertyType.setCategory(otherCategory);
			propertyType.setDefaultValue(o.getPhysicalColumn().getTable().getName());
			
			if(rootModel != null) rootModel.getPropertyTypes().add(propertyType);
		}
		
		property = FACTORY.createModelProperty();
		property.setPropertyType(propertyType);
		o.getProperties().put(property.getPropertyType().getId(), property);
		
	}
	
	private void initIdentifierProperties(BusinessIdentifier o) {
		
	}
	
	private void initRelationshipProperties(BusinessRelationship o) {
		
	}

}
