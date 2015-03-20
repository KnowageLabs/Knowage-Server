/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure.builder.hibernate;

import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.qbe.datasource.hibernate.IHibernateDataSource;
import it.eng.qbe.model.properties.initializer.IModelStructurePropertiesInitializer;
import it.eng.qbe.model.properties.initializer.ModelStructurePropertiesInitializerFactory;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.model.structure.ModelCalculatedField;
import it.eng.qbe.model.structure.ModelEntity;
import it.eng.qbe.model.structure.ModelStructure;
import it.eng.qbe.model.structure.builder.IModelStructureBuilder;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.hibernate.SessionFactory;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.Type;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class HibernateModelStructureBuilder implements IModelStructureBuilder {
	
	private IHibernateDataSource dataSource;	
	IModelStructurePropertiesInitializer propertiesInitializer;
	
	public HibernateModelStructureBuilder(IHibernateDataSource dataSource) {
		Assert.assertNotNull(dataSource, "Parameter [dataSource] cannot be null");
		setDataSource( dataSource );
		propertiesInitializer = ModelStructurePropertiesInitializerFactory.getDataMartStructurePropertiesInitializer(dataSource);		
	}
	
	public IModelStructure build() {
		
		IModelStructure dataMartStructure;
		List<IDataSourceConfiguration> subConfigurations;
		String datamartName;
		Map classMetadata;
			
		dataMartStructure = new ModelStructure();	
		dataMartStructure.setName( getDataSource().getName() );
		propertiesInitializer.addProperties(dataMartStructure);
		
		subConfigurations = getDataSource().getSubConfigurations();
		for(int i = 0; i < subConfigurations.size(); i++) {
			datamartName = subConfigurations.get(i).getModelName();
			Assert.assertNotNull(getDataSource(), "datasource cannot be null");	
			SessionFactory sf = getDataSource().getHibernateSessionFactory(datamartName);
			if(sf == null) {
				throw new MissingResourceException("Impossible to find the jar file associated to datamart named: [" + datamartName + "]"
						, SessionFactory.class.getName()
						, datamartName );
			}
			
			
			Map calculatedFields = subConfigurations.get(i).loadCalculatedFields();
			dataMartStructure.setCalculatedFields(calculatedFields);
			
			classMetadata = sf.getAllClassMetadata();
			for(Iterator it = classMetadata.keySet().iterator(); it.hasNext(); ) {
				String entityType = (String)it.next();			
				addEntity(dataMartStructure, datamartName, entityType);		
			}
		}
		
		
		return dataMartStructure;
	}

	private void addEntity (IModelStructure dataMartStructure, String datamartName, String entityType){

		String entityName = getEntityNameFromEntityType(entityType);		
		IModelEntity dataMartEntity = dataMartStructure.addRootEntity(datamartName, entityName, null, null, entityType);
		propertiesInitializer.addProperties(dataMartEntity);
		
		addKeyFields(dataMartEntity);		
		List subEntities = addNormalFields(dataMartEntity);	
		addCalculatedFields(dataMartEntity);
		addSubEntities(dataMartEntity, subEntities, 0);
		
		
	}
	
	private void addCalculatedFields(IModelEntity dataMartEntity) {
		List calculatedFileds;
		ModelCalculatedField calculatedField;
		
		calculatedFileds = dataMartEntity.getStructure().getCalculatedFieldsByEntity(dataMartEntity.getUniqueName());
		if(calculatedFileds != null) {
			for(int i = 0; i < calculatedFileds.size(); i++) {
				calculatedField = (ModelCalculatedField)calculatedFileds.get(i);
				dataMartEntity.addCalculatedField(calculatedField);
				propertiesInitializer.addProperties(calculatedField);
			}
		}
	}

	private void addSubEntities(IModelEntity dataMartEntity, List subEntities, int recursionLevel) {
		
		Iterator it = subEntities.iterator();
		while (it.hasNext()) {
			IModelEntity subentity = (IModelEntity)it.next();
			if (subentity.getType().equalsIgnoreCase(dataMartEntity.getType())){
				// ciclo di periodo 0!
			} else if(recursionLevel > 10) {
				// prune recursion tree 
			} else {
				addSubEntity(dataMartEntity, 
						subentity,
						recursionLevel+1);
			}
		}
	}
	
	private void addSubEntity (IModelEntity parentEntity,
			IModelEntity subEntity, 			
			int recursionLevel){

		IModelEntity dataMartEntity;				

		
		//String entityName = getEntityNameFromEntityType(entityType);		
		dataMartEntity = parentEntity.addSubEntity(subEntity.getName(), subEntity.getRole(), subEntity.getType());
		propertiesInitializer.addProperties(dataMartEntity);
		
		addKeyFields(dataMartEntity);			
		List subEntities = addNormalFields(dataMartEntity);		
		addCalculatedFields(dataMartEntity);
		addSubEntities(dataMartEntity, subEntities, recursionLevel);
		
		
	}
	
	private void addKeyFields(IModelEntity dataMartEntity) {
		
		PersistentClass classMapping;
		ClassMetadata classMetadata;
		Type identifierType;
		
		getDataSource().getHibernateConfiguration().buildMappings();//must be called 
		classMapping = getDataSource().getHibernateConfiguration().getClassMapping(dataMartEntity.getType());
		if(classMapping == null ){
			Iterator<PersistentClass> it =  getDataSource().getHibernateConfiguration().getClassMappings();
			while(it.hasNext()) {
				PersistentClass pc = it.next();
			}
		}
		
		
		classMetadata = getDataSource().getHibernateSessionFactory().getClassMetadata(dataMartEntity.getType());
		identifierType = classMetadata.getIdentifierType();
		
		
		
		List identifierPropertyNames = new ArrayList();
		String[] propertyClass = null;
		String[] type  = null;
		int[] scale  = null;
		int[] precision = null;
		
		String identifierPropertyName = classMetadata.getIdentifierPropertyName();
		
		if (identifierType.isComponentType()) {
			
				ComponentType componentIdentifierType = (ComponentType)identifierType;	
				String[] subPropertyNames = componentIdentifierType.getPropertyNames();
				
				
				Type[] subPropertyTypes = componentIdentifierType.getSubtypes();
				
				propertyClass = new String[subPropertyNames.length];
				type  = new String[subPropertyNames.length];
				scale  = new int[subPropertyNames.length];
				precision = new int[subPropertyNames.length];
				Class subPropertyClass = null;				
				
				for (int j=0; j < subPropertyNames.length; j++){
					subPropertyClass = subPropertyTypes[j].getClass();
					
					if( subPropertyTypes[j].isComponentType() ) {
						ComponentType cType = (ComponentType)subPropertyTypes[j];	
						String[] sPropertyNames = cType.getPropertyNames();
						Type[] sTypes = cType.getSubtypes();
						for(int z = 0; z < sPropertyNames.length; z++) {
							identifierPropertyNames.add(identifierPropertyName + "." + subPropertyNames[j] + "."+ sPropertyNames[z]);
							propertyClass[j] = subPropertyClass.getName();
							type[j] = subPropertyTypes[j].getName();
						}
					} else {
						identifierPropertyNames.add(identifierPropertyName + "." + subPropertyNames[j]);
						propertyClass[j] = subPropertyClass.getName();
						type[j] = subPropertyTypes[j].getName();
					}
				}		
		
		} else {
				propertyClass = new String[1];
				type = new String[1];
				scale = new int[1];
				precision = new int[1];
				
				identifierPropertyNames.add(identifierPropertyName);
				propertyClass[0] = identifierType.getClass().getName();
				type[0] = identifierType.getName();						
		}		
		    	
		
		
		Iterator it = classMapping.getIdentifierProperty().getColumnIterator();
		for (int k = 0; k < scale.length; k++){
			if(!it.hasNext()) continue;
			Column column = (Column)it.next();
			scale[k] = column.getScale();
			precision[k] = column.getPrecision();
		}
		
		
		
		for (int j = 0; j < identifierPropertyNames.size(); j++) {
			String fieldName = (String)identifierPropertyNames.get(j);					
			IModelField dataMartField = dataMartEntity.addKeyField(fieldName);
			dataMartField.setType(type[j]);
			dataMartField.setPrecision(precision[j]);
			dataMartField.setLength(scale[j]);
			propertiesInitializer.addProperties(dataMartField);
		}
	}
	
	public List addNormalFields(IModelEntity dataMartEntity) {
		
		ClassMetadata classMetadata;
		PersistentClass classMapping;
		String[] propertyNames;
		Property property;
		Type propertyType;	
		
		classMetadata = getDataSource().getHibernateSessionFactory().getClassMetadata(dataMartEntity.getType());
		classMapping = getDataSource().getHibernateConfiguration().getClassMapping(dataMartEntity.getType());		
		propertyNames = classMetadata.getPropertyNames();		
		
			
		List subEntities = new ArrayList();		
		String propertyName = null;
		
		for(int i=0; i < propertyNames.length; i++) { 
			
			property = classMapping.getProperty(propertyNames[i]);
			
			// TEST if they are the same: if so use the first invocation
		 	propertyType = property.getType();
		 	
		 	Iterator columnIterator = property.getColumnIterator();
		 	Column column;
		 	
		 	if (propertyType instanceof ManyToOneType){ // chiave esterna
		 		
		 		ManyToOneType manyToOnePropertyType = (ManyToOneType)propertyType; 
		 		String entityType = manyToOnePropertyType.getAssociatedEntityName();
		 		
			 	String columnName = null;
			 	if (columnIterator.hasNext()){
			 		column = (Column)columnIterator.next();
			 		columnName = column.getName(); // ????
			 	}
			 	
		 		propertyName = propertyNames[i];	
		 		
		 		
		 		//String entityName = getEntityNameFromEntityType(entityType);
		 		String entityName = propertyName;
		 		IModelEntity subentity = new ModelEntity(entityName, null, columnName, entityType, dataMartEntity.getStructure());		
		 		subEntities.add(subentity);	
		 		
		 	} else if (propertyType instanceof CollectionType) { // chiave interna
				
		 		
			} else { // normal field
				propertyName = propertyNames[i];
				
				String type = propertyType.getName();
				int scale = 0;
				int precision = 0;
				
								
			 	if (columnIterator.hasNext()){
			 		column = (Column)columnIterator.next();
			 		scale = column.getScale();
			 		precision = column.getPrecision();
			 	}
		 		
			 
					
				IModelField datamartField = dataMartEntity.addNormalField(propertyName);
				datamartField.setType(type);
				datamartField.setPrecision(precision);
				datamartField.setLength(scale);
				propertiesInitializer.addProperties(datamartField);
			}
		 }
		
		return subEntities;
	}
	
	private String getEntityNameFromEntityType(String entityType) {
		String entityName = entityType;
		entityName = (entityName.lastIndexOf('.') > 0 ?
				  entityName.substring(entityName.lastIndexOf('.') + 1 , entityName.length()) :
				  entityName);
				  
		return entityName;
	}

	protected IHibernateDataSource getDataSource() {
		return dataSource;
	}

	protected void setDataSource(IHibernateDataSource dataSource) {
		this.dataSource = dataSource;
	}
}
