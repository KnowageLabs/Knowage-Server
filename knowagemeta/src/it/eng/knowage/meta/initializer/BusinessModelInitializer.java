/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.knowage.meta.initializer;

import it.eng.knowage.meta.initializer.descriptor.BusinessRelationshipDescriptor;
import it.eng.knowage.meta.initializer.descriptor.BusinessViewInnerJoinRelationshipDescriptor;
import it.eng.knowage.meta.initializer.descriptor.CalculatedFieldDescriptor;
import it.eng.knowage.meta.initializer.name.BusinessModelNamesInitializer;
import it.eng.knowage.meta.initializer.properties.BusinessModelPropertiesFromFileInitializer;
import it.eng.knowage.meta.initializer.properties.IPropertiesInitializer;
import it.eng.knowage.meta.initializer.utils.Pair;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.business.BusinessIdentifier;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessModelFactory;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.BusinessView;
import it.eng.knowage.meta.model.business.BusinessViewInnerJoinRelationship;
import it.eng.knowage.meta.model.business.CalculatedBusinessColumn;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;
import it.eng.knowage.meta.model.filter.IModelObjectFilter;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalForeignKey;
import it.eng.knowage.meta.model.physical.PhysicalModel;
import it.eng.knowage.meta.model.physical.PhysicalPrimaryKey;
import it.eng.knowage.meta.model.physical.PhysicalTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class BusinessModelInitializer {

	IPropertiesInitializer propertiesInitializer;
	BusinessModelNamesInitializer namesInitializer;

	static public String INITIALIZER_NAME = "StandardSpagoBIBusinessModelInitializer";
	static public String INITIALIZER_VERSION = "5.2.0";

	static public BusinessModelFactory FACTORY = BusinessModelFactory.eINSTANCE;

	public BusinessModelInitializer() {
		setPropertiesInitializer(new BusinessModelPropertiesFromFileInitializer());
		setNamesInitializer(new BusinessModelNamesInitializer());
	}

	public BusinessModel initialize(String modelName, PhysicalModel physicalModel) {
		return initialize(modelName, null, physicalModel);
	}

	public BusinessModel initialize(String modelName, IModelObjectFilter tableFilter, PhysicalModel physicalModel) {
		BusinessModel businessModel;

		try {
			businessModel = FACTORY.createBusinessModel();
			businessModel.setName(modelName);

			if (physicalModel.getParentModel() != null) {
				businessModel.setParentModel(physicalModel.getParentModel());
			}

			businessModel.setPhysicalModel(physicalModel);

			// for each physical model object create a related business object...

			// tables
			addTables(physicalModel, tableFilter, businessModel);

			// identifiers - primary keys
			addIdentifiers(physicalModel, businessModel);

			// relationships-foreign keys
			addRelationships(physicalModel, businessModel);

			getPropertiesInitializer().addProperties(businessModel);
			businessModel.setProperty(BusinessModelPropertiesFromFileInitializer.MODEL_INITIALIZER_NAME, INITIALIZER_NAME);
			businessModel.setProperty(BusinessModelPropertiesFromFileInitializer.MODEL_INITIALIZER_VERSION, INITIALIZER_VERSION);

			// Only for Cassandra don't set property by default for not using catalog and schema in mapping-query
			String databaseName = businessModel.getPhysicalModel().getDatabaseName();
			if (databaseName.toLowerCase().contains("cassandra")) {
				businessModel.setProperty(BusinessModelPropertiesFromFileInitializer.MODEL_USE_CATALOG_IN_MAPPING, "false");
				businessModel.setProperty(BusinessModelPropertiesFromFileInitializer.MODEL_USE_SCHEMA_IN_MAPPING, "false");

			}

		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize business model", t);
		}

		return businessModel;
	}

	/*
	 * Create an empty Business Model with only the Physical Model reference
	 */
	public BusinessModel initializeEmptyBusinessModel(String modelName, PhysicalModel physicalModel) {
		BusinessModel businessModel;

		try {
			businessModel = FACTORY.createBusinessModel();
			businessModel.setName(modelName);

			if (physicalModel.getParentModel() != null) {
				businessModel.setParentModel(physicalModel.getParentModel());
			}

			businessModel.setPhysicalModel(physicalModel);

			getPropertiesInitializer().addProperties(businessModel);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize business model", t);
		}

		return businessModel;
	}

	public void addTables(PhysicalModel physicalModel, BusinessModel businessModel) {
		addTables(physicalModel, businessModel);
	}

	public void addTables(PhysicalModel physicalModel, IModelObjectFilter tableFilter, BusinessModel businessModel) {
		PhysicalTable physicalTable;

		try {
			for (int i = 0; i < physicalModel.getTables().size(); i++) {
				physicalTable = physicalModel.getTables().get(i);
				if (tableFilter == null || !tableFilter.filter(physicalTable)) {
					addTable(physicalTable, businessModel, false);
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize business tables", t);
		}
	}

	public BusinessTable addTable(PhysicalTable physicalTable, BusinessModel businessModel, boolean addIdentifier) {
		return addTable(physicalTable, null, businessModel, addIdentifier);
	}

	public BusinessTable addTable(PhysicalTable physicalTable, IModelObjectFilter columnFilter, BusinessModel businessModel, boolean addIdentifier) {
		BusinessTable businessTable;

		try {
			businessTable = FACTORY.createBusinessTable();
			businessTable.setModel(businessModel);
			businessTable.setPhysicalTable(physicalTable);

			getNamesInitializer().setTableUniqueName(businessTable);
			getNamesInitializer().setTableName(businessTable);
			businessTable.setDescription(physicalTable.getDescription());

			addColumns(physicalTable, columnFilter, businessTable);

			businessModel.getTables().add(businessTable);

			// adding table identifier if requested
			if (addIdentifier) {
				if (physicalTable.getPrimaryKey() != null) {
					// addIdentifier(physicalTable.getPrimaryKey(),businessModel);
					addIdentifier(businessTable, businessModel);
				}
			}

			getPropertiesInitializer().addProperties(businessTable);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize business table from physical table [" + physicalTable.getName() + "]", t);
		}
		return businessTable;
	}

	// create a BusinessTable with the name and description passed as parameter
	public BusinessTable addTable(PhysicalTable physicalTable, IModelObjectFilter columnFilter, String businessTableName, String businessTableDescription,
			BusinessModel businessModel, boolean addIdentifier) {
		BusinessTable businessTable;

		try {
			businessTable = FACTORY.createBusinessTable();

			businessTable.setModel(businessModel);
			businessTable.setPhysicalTable(physicalTable);

			getNamesInitializer().setTableName(businessTable, businessTableName);
			getNamesInitializer().setTableUniqueName(businessTable, businessTableName);
			businessTable.setDescription(businessTableDescription);

			addColumns(physicalTable, columnFilter, businessTable);

			businessModel.getTables().add(businessTable);

			// adding table identifier if requested
			if (addIdentifier) {
				if (physicalTable.getPrimaryKey() != null) {
					// addIdentifier(physicalTable.getPrimaryKey(),businessModel);
					addIdentifier(businessTable, businessModel);
				}
			}

			getPropertiesInitializer().addProperties(businessTable);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize business table from physical table [" + physicalTable.getName() + "]", t);
		}
		return businessTable;
	}

	// Add an empty Business Table without Physical Table reference
	public BusinessTable addEmptyTable(BusinessModel businessModel, String tableName) {
		BusinessTable businessTable;
		try {
			businessTable = FACTORY.createBusinessTable();
			businessTable.setModel(businessModel);

			getNamesInitializer().setTableUniqueName(businessTable, tableName);
			getNamesInitializer().setTableName(businessTable, tableName);
			businessTable.setDescription(tableName);

			businessModel.getTables().add(businessTable);

			getPropertiesInitializer().addProperties(businessTable);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to add empty business table ");
		}
		return businessTable;

	}

	public void addColumns(PhysicalTable physicalTable, BusinessTable businessTable) {
		addColumns(physicalTable, null, businessTable);
	}

	public void addColumns(PhysicalTable physicalTable, IModelObjectFilter columnFilter, BusinessTable businessTable) {
		PhysicalColumn physicalColumn;

		try {
			for (int i = 0; i < physicalTable.getColumns().size(); i++) {
				physicalColumn = physicalTable.getColumns().get(i);
				if (columnFilter == null || !columnFilter.filter(physicalColumn)) {
					addColumn(physicalColumn, businessTable);
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize columns meta", t);
		}
	}

	public void addColumn(PhysicalColumn physicalColumn, BusinessColumnSet businessColumnSet) {
		SimpleBusinessColumn businessColumn;

		try {
			businessColumn = FACTORY.createSimpleBusinessColumn();

			businessColumn.setPhysicalColumn(physicalColumn);
			businessColumn.setTable(businessColumnSet);

			getNamesInitializer().setColumnUniqueName(businessColumn);
			getNamesInitializer().setColumnName(businessColumn);
			businessColumn.setDescription(physicalColumn.getDescription());

			businessColumnSet.getColumns().add(businessColumn);

			getPropertiesInitializer().addProperties(businessColumn);
			businessColumn.setProperty(BusinessModelPropertiesFromFileInitializer.COLUMN_DATATYPE, physicalColumn.getDataType());
			businessColumn.setProperty(BusinessModelPropertiesFromFileInitializer.COLUMN_PHYSICAL_TABLE, businessColumn.getPhysicalColumn().getTable()
					.getName());
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize business column from physical column [" + physicalColumn.getName() + "]", t);
		}
	}

	public void addCalculatedColumn(CalculatedFieldDescriptor calculatedColumnDescriptor) {
		CalculatedBusinessColumn calculatedBusinessColumn;
		BusinessColumnSet businessColumnSet = calculatedColumnDescriptor.getBusinessColumnSet();

		try {
			calculatedBusinessColumn = FACTORY.createCalculatedBusinessColumn();

			calculatedBusinessColumn.setTable(businessColumnSet);

			// setting original name as Unique Name
			calculatedBusinessColumn.setUniqueName(calculatedColumnDescriptor.getName());
			calculatedBusinessColumn.setName(calculatedColumnDescriptor.getName());
			calculatedBusinessColumn.setDescription("Calculated Column " + calculatedColumnDescriptor.getName());

			businessColumnSet.getColumns().add(calculatedBusinessColumn);

			getPropertiesInitializer().addProperties(calculatedBusinessColumn);
			// set calculated column expression text
			calculatedBusinessColumn.setProperty(BusinessModelPropertiesFromFileInitializer.CALCULATED_COLUMN_EXPRESSION,
					calculatedColumnDescriptor.getExpression());
			// set calculated column dataType
			calculatedBusinessColumn.setProperty(BusinessModelPropertiesFromFileInitializer.CALCULATED_COLUMN_DATATYPE,
					calculatedColumnDescriptor.getDataType());
			// set column type
			calculatedBusinessColumn.setProperty("structural.columntype", "attribute");

		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize calculted business column ", t);
		}
	}

	public void editCalculatedColumn(CalculatedBusinessColumn calculatedBusinessColumn, CalculatedFieldDescriptor calculatedColumnDescriptor) {
		try {
			calculatedBusinessColumn.setName(calculatedColumnDescriptor.getName());

			// set calculated column expression text
			calculatedBusinessColumn.setProperty(BusinessModelPropertiesFromFileInitializer.CALCULATED_COLUMN_EXPRESSION,
					calculatedColumnDescriptor.getExpression());

			// set calculated column dataType
			calculatedBusinessColumn.setProperty(BusinessModelPropertiesFromFileInitializer.CALCULATED_COLUMN_DATATYPE,
					calculatedColumnDescriptor.getDataType());

		} catch (Throwable t) {
			throw new RuntimeException("Impossible to modify calculted business column ", t);
		}
	}

	public void addIdentifiers(PhysicalModel physicalModel, BusinessModel businessModel) {
		PhysicalPrimaryKey physicalPrimaryKey;

		try {
			for (int i = 0; i < physicalModel.getPrimaryKeys().size(); i++) {
				physicalPrimaryKey = physicalModel.getPrimaryKeys().get(i);
				addIdentifier(physicalPrimaryKey, businessModel);
			}
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize identifier meta", t);
		}
	}

	public BusinessIdentifier addIdentifier(PhysicalPrimaryKey physicalPrimaryKey, BusinessModel businessModel) {
		BusinessIdentifier businessIdentifier;
		PhysicalTable physicalTable;
		List<BusinessTable> businessTables;

		businessIdentifier = null;

		try {
			physicalTable = physicalPrimaryKey.getTable();
			businessTables = businessModel.getBusinessTableByPhysicalTable(physicalTable);

			// create identifier iff businessTable is present in businessModel
			for (BusinessTable businessTable : businessTables) {
				addIdentifier(businessTable, businessModel);
			}

		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize identifier meta", t);
		}

		return businessIdentifier;
	}

	// add Identifier with PhysicalPrimaryKey discovered through passed BusinessTable
	public BusinessIdentifier addIdentifier(BusinessTable businessTable, BusinessModel businessModel) {
		BusinessIdentifier businessIdentifier = null;
		BusinessColumn businessColumn;
		PhysicalPrimaryKey physicalPrimaryKey;

		physicalPrimaryKey = businessTable.getPhysicalTable().getPrimaryKey();
		if (physicalPrimaryKey != null) {
			try {
				if (areAllPKColumnsContainedInBusinessTable(physicalPrimaryKey, businessTable)) {
					List<BusinessColumn> businessColumns = getContainedBusinessColumn(physicalPrimaryKey.getColumns(), businessTable);
					businessIdentifier = addIdentifier(physicalPrimaryKey.getName(), businessTable, businessColumns);
					businessIdentifier.setPhysicalPrimaryKey(physicalPrimaryKey);
				}
			} catch (Throwable t) {
				throw new RuntimeException("Impossible to initialize identifier meta", t);
			}

		}
		return businessIdentifier;
	}

	public boolean areAllPKColumnsContainedInBusinessTable(PhysicalPrimaryKey physicalPrimaryKey, BusinessColumnSet businessTable) {
		List<BusinessColumn> containedBusinessColumns;

		containedBusinessColumns = getContainedBusinessColumn(physicalPrimaryKey.getColumns(), businessTable);

		return containedBusinessColumns.size() == physicalPrimaryKey.getColumns().size();
	}

	public List<BusinessColumn> getContainedBusinessColumn(List<PhysicalColumn> physicalColumns, BusinessColumnSet businessTable) {
		List<BusinessColumn> businessColumns;

		businessColumns = new ArrayList<BusinessColumn>();
		for (int j = 0; j < physicalColumns.size(); j++) {
			BusinessColumn businessColumn = businessTable.getSimpleBusinessColumn(physicalColumns.get(j));
			if (businessColumn != null) {
				businessColumns.add(businessColumn);
			}
		}

		return businessColumns;
	}

	// add Identifier without PhysicalPrimaryKey specified
	public BusinessIdentifier addIdentifier(String businessIdentifierName, BusinessColumnSet businessColumnSet, Collection<BusinessColumn> businessColumns) {
		BusinessIdentifier businessIdentifier;
		BusinessModel businessModel = businessColumnSet.getModel();

		try {
			businessIdentifier = FACTORY.createBusinessIdentifier();
			businessIdentifier.setName(businessIdentifierName);
			businessIdentifier.setTable(businessColumnSet);
			// set original name as Unique Name
			businessIdentifier.setUniqueName(businessIdentifierName);

			businessIdentifier.setModel(businessModel);
			businessModel.getIdentifiers().add(businessIdentifier);

			for (BusinessColumn businessColumn : businessColumns) {
				businessIdentifier.getColumns().add(businessColumn);
			}

			getPropertiesInitializer().addProperties(businessIdentifier);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize identifier meta", t);
		}
		return businessIdentifier;
	}

	public void addRelationships(PhysicalModel physicalModel, BusinessModel businessModel) {
		PhysicalForeignKey physicalForeignKey;

		try {
			for (int i = 0; i < physicalModel.getForeignKeys().size(); i++) {
				physicalForeignKey = physicalModel.getForeignKeys().get(i);
				addRelationship(physicalForeignKey, businessModel);
			}
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize relationship meta", t);
		}
	}

	public BusinessRelationship addRelationship(PhysicalForeignKey physicalForeignKey, BusinessModel businessModel) {
		BusinessRelationship businessRelationship;
		PhysicalTable physicalTable;
		List<BusinessTable> sourceBusinessTables;
		List<BusinessTable> destinationBusinessTables;

		businessRelationship = null;
		try {

			physicalTable = physicalForeignKey.getSourceTable();
			sourceBusinessTables = businessModel.getBusinessTableByPhysicalTable(physicalTable);

			physicalTable = physicalForeignKey.getDestinationTable();
			destinationBusinessTables = businessModel.getBusinessTableByPhysicalTable(physicalTable);

			for (BusinessTable sourceTable : sourceBusinessTables) {
				for (BusinessTable destinationTable : destinationBusinessTables) {
					businessRelationship = addRelationship(sourceTable, destinationTable, physicalForeignKey);
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize business relationship from physical foreign key [" + physicalForeignKey.getSourceName() + "]",
					t);
		}

		return businessRelationship;
	}

	public BusinessRelationship addRelationship(BusinessTable sourceBusinessTable, BusinessTable destinationBusinessTable, PhysicalForeignKey physicalForeignKey) {
		BusinessRelationship businessRelationship;
		BusinessColumn businessColumn;

		if (destinationBusinessTable.getModel() != sourceBusinessTable.getModel())
			return null;
		if (destinationBusinessTable.getPhysicalTable() != physicalForeignKey.getDestinationTable())
			return null;
		if (sourceBusinessTable.getPhysicalTable() != physicalForeignKey.getSourceTable())
			return null;

		businessRelationship = null;
		try {
			BusinessModel businessModel = destinationBusinessTable.getModel();

			if (sourceBusinessTable != null && destinationBusinessTable != null) {

				// create an empty relationship
				businessRelationship = FACTORY.createBusinessRelationship();
				businessRelationship.setName(physicalForeignKey.getSourceName());
				// set Foreign Key Source as Unique Name
				businessRelationship.setUniqueName(physicalForeignKey.getSourceName());
				businessRelationship.setPhysicalForeignKey(physicalForeignKey);

				// add source columns
				businessRelationship.setSourceTable(sourceBusinessTable);
				for (int j = 0; j < physicalForeignKey.getSourceColumns().size(); j++) {
					businessColumn = sourceBusinessTable.getSimpleBusinessColumn(physicalForeignKey.getSourceColumns().get(j));
					businessRelationship.getSourceColumns().add(businessColumn);
				}

				// add destination columns
				businessRelationship.setDestinationTable(destinationBusinessTable);
				for (int j = 0; j < physicalForeignKey.getDestinationColumns().size(); j++) {
					businessColumn = destinationBusinessTable.getSimpleBusinessColumn(physicalForeignKey.getDestinationColumns().get(j));
					businessRelationship.getDestinationColumns().add(businessColumn);
				}

				if (businessRelationship.getDestinationColumns().size() == physicalForeignKey.getDestinationColumns().size()
						&& businessRelationship.getSourceColumns().size() == physicalForeignKey.getSourceColumns().size()) {
					businessModel.getRelationships().add(businessRelationship);
					getPropertiesInitializer().addProperties(businessRelationship);
					// set the destinationRole property
					businessRelationship.setProperty(BusinessModelPropertiesFromFileInitializer.ROLE_DESTINATION, businessRelationship.getDestinationTable()
							.getName());
				} else {
					businessRelationship = null;
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize business relationship from physical foreign key [" + physicalForeignKey.getSourceName() + "]",
					t);
		}

		return businessRelationship;
	}

	// add Relationship without PhysicalForeignKey specified
	public BusinessRelationship addRelationship(BusinessRelationshipDescriptor descriptor) {

		BusinessRelationship businessRelationship;
		BusinessModel businessModel = descriptor.getSourceTable().getModel();

		try {
			businessRelationship = FACTORY.createBusinessRelationship();

			if (descriptor.getRelationshipName() == null) {
				businessRelationship.setName("Business Relationship " + descriptor.getSourceTable().getName() + "_"
						+ descriptor.getDestinationTable().getName());
				businessRelationship.setUniqueName("Business Relationship " + descriptor.getSourceTable().getName() + "_"
						+ descriptor.getDestinationTable().getName());
			} else {
				businessRelationship.setName(descriptor.getRelationshipName());
				businessRelationship.setUniqueName(descriptor.getRelationshipName());
			}

			businessRelationship.setSourceTable(descriptor.getSourceTable());
			for (BusinessColumn businessColumn : descriptor.getSourceColumns()) {
				businessRelationship.getSourceColumns().add(businessColumn);
			}
			businessRelationship.setDestinationTable(descriptor.getDestinationTable());
			for (BusinessColumn businessColumn : descriptor.getDestinationColumns()) {
				businessRelationship.getDestinationColumns().add(businessColumn);
			}
			businessModel.getRelationships().add(businessRelationship);
			getPropertiesInitializer().addProperties(businessRelationship);
			// set the destinationRole property
			businessRelationship.setProperty(BusinessModelPropertiesFromFileInitializer.ROLE_DESTINATION, businessRelationship.getDestinationTable().getName());
			// set the cardinality property
			businessRelationship.setProperty(BusinessModelPropertiesFromFileInitializer.RELATIONSHIP_CARDINALITY, descriptor.getRelationCardinality());

		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize business relationship", t);
		}
		return businessRelationship;
	}

	/**
	 * Create a BusinessView using the data from a BusinessTable and the added PhysicalTable with a specified join path
	 * 
	 * @return BusinessView created
	 */
	public BusinessView upgradeBusinessTableToBusinessView(BusinessTable businessTable,
			BusinessViewInnerJoinRelationshipDescriptor innerJoinRelationshipDescriptor) {
		BusinessView businessView;
		BusinessModel businessModel = businessTable.getModel();
		Collection<BusinessColumn> businessColumns = businessTable.getColumns();

		Collection<BusinessRelationship> businessRelationships = businessTable.getRelationships();
		BusinessIdentifier businessIdentifier;

		try {
			// create BusinessViewInnerJoinRelationship object
			BusinessViewInnerJoinRelationship innerJoinRelationship = addBusinessViewInnerJoinRelationship(businessModel, innerJoinRelationshipDescriptor);

			businessView = FACTORY.createBusinessView();
			businessView.setModel(businessModel);
			businessView.setName(businessTable.getName());
			businessView.setUniqueName(businessTable.getName());
			businessView.setDescription((businessTable.getDescription()));

			// add all the columns of Business Table to the Business View
			businessView.getColumns().addAll(businessColumns);
			// add the inner join relationship between two physical table
			businessView.getJoinRelationships().add(innerJoinRelationship);

			// check Business Table relationships
			for (BusinessRelationship relationship : businessRelationships) {
				if (relationship.getDestinationTable() == businessTable) {
					// replace business table with business view
					relationship.setDestinationTable(businessView);
				} else if (relationship.getSourceTable() == businessTable) {
					// replace business table with business view
					relationship.setSourceTable(businessView);
				}
			}

			// check Identifier to inherit
			businessIdentifier = businessTable.getIdentifier();
			if (businessIdentifier != null) {
				businessIdentifier.setTable(businessView);
			}

			// add BusinessView to BusinessModel
			businessModel.getTables().add(businessView);

			// add BusinessView properties(?)
			getPropertiesInitializer().addProperties(businessView);

			// destroy Business Table
			businessModel.getTables().remove(businessTable);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize business view", t);
		}
		return businessView;
	}

	/**
	 * Upgrade BusinessTable to BusinessView without using Join Paths
	 */
	public BusinessView upgradeBusinessTableToBusinessView(BusinessTable businessTable) {
		BusinessView businessView;
		BusinessModel businessModel = businessTable.getModel();
		Collection<BusinessColumn> businessColumns = businessTable.getColumns();

		Collection<BusinessRelationship> businessRelationships = businessTable.getRelationships();
		BusinessIdentifier businessIdentifier;

		try {
			businessView = FACTORY.createBusinessView();
			businessView.setModel(businessModel);
			businessView.setName(businessTable.getName());
			businessView.setUniqueName(businessTable.getName());
			businessView.setDescription((businessTable.getDescription()));

			// add all the columns of Business Table to the Business View
			businessView.getColumns().addAll(businessColumns);

			// check Business Table relationships
			for (BusinessRelationship relationship : businessRelationships) {
				if (relationship.getDestinationTable() == businessTable) {
					// replace business table with business view
					relationship.setDestinationTable(businessView);
				} else if (relationship.getSourceTable() == businessTable) {
					// replace business table with business view
					relationship.setSourceTable(businessView);
				}
			}

			// check Identifier to inherit
			businessIdentifier = businessTable.getIdentifier();
			if (businessIdentifier != null) {
				businessIdentifier.setTable(businessView);
			}

			// add BusinessView to BusinessModel
			businessModel.getTables().add(businessView);

			// add BusinessView properties
			getPropertiesInitializer().addProperties(businessView);

			// destroy Business Table
			businessModel.getTables().remove(businessTable);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize business view", t);
		}
		return businessView;
	}

	/**
	 * Transform a BusinessView with only one PhysicalTable in the corresponding BusinessTable
	 * 
	 * @param businessView
	 * @return businessTable
	 */
	public BusinessTable downgradeBusinessViewToBusinessTable(BusinessView businessView) {
		BusinessTable businessTable;
		PhysicalTable physicalTable;
		BusinessModel businessModel = businessView.getModel();
		Collection<BusinessColumn> businessColumns = new ArrayList<BusinessColumn>();

		Collection<BusinessRelationship> businessRelationships = businessView.getRelationships();
		BusinessIdentifier businessIdentifier;

		try {
			// get the only PhysicalTable to mantain
			// physicalTable = businessView.getJoinRelationships().get(0).getSourceTable();
			physicalTable = businessView.getPhysicalTables().get(0);

			for (SimpleBusinessColumn bc : businessView.getSimpleBusinessColumns()) {
				if (bc.getPhysicalColumn().getTable().equals(physicalTable)) {
					businessColumns.add(bc);
				}
			}

			// remove BusinessViewInnerJoinRelationship object
			businessView.getJoinRelationships().clear();

			businessTable = FACTORY.createBusinessTable();
			businessTable.setModel(businessModel);
			businessTable.setName(businessView.getName());
			businessTable.setUniqueName(businessView.getName());
			businessTable.setPhysicalTable(physicalTable);
			businessTable.setDescription(businessView.getDescription());

			// add all the columns of Business View to the Business Table
			businessTable.getColumns().addAll(businessColumns);

			// check Business View relationships
			for (BusinessRelationship relationship : businessRelationships) {
				if (relationship.getDestinationTable() == businessView) {
					// replace business view with business table
					relationship.setDestinationTable(businessTable);
				} else if (relationship.getSourceTable() == businessView) {
					// replace business view with business table
					relationship.setSourceTable(businessTable);
				}
			}

			// check Identifier to inherit
			businessIdentifier = businessView.getIdentifier();
			if (businessIdentifier != null) {
				businessIdentifier.setTable(businessTable);
			}

			// add BusinessTable to BusinessView
			businessModel.getTables().add(businessTable);

			// add BusinessView properties(?)
			getPropertiesInitializer().addProperties(businessTable);

			// destroy Business View
			businessModel.getTables().remove(businessView);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize business view", t);
		}
		return businessTable;
	}

	/**
	 * Add a Physical Tables reference to the passed BusinessView
	 */
	public BusinessView addPhysicalTableToBusinessView(BusinessView businessView, BusinessViewInnerJoinRelationshipDescriptor joinRelationshipDescriptor) {
		BusinessModel businessModel = businessView.getModel();

		try {
			// create BusinessViewInnerJoinRelationship object
			BusinessViewInnerJoinRelationship innerJoinRelationship = addBusinessViewInnerJoinRelationship(businessModel, joinRelationshipDescriptor);
			// add the inner join relationship between two physical table
			businessView.getJoinRelationships().add(innerJoinRelationship);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to add physical table to business view", t);
		}
		return businessView;
	}

	/**
	 * Remove a Physical Tables reference to the passed BusinessView
	 */

	public BusinessView removePhysicalTableToBusinessView(BusinessView businessView, BusinessViewInnerJoinRelationshipDescriptor joinRelationshipDescriptor) {
		BusinessModel businessModel = businessView.getModel();

		try {
			// create BusinessViewInnerJoinRelationship object
			BusinessViewInnerJoinRelationship innerJoinRelationship = removeBusinessViewInnerJoinRelationship(businessModel, joinRelationshipDescriptor);

			// check if the removed physicalTable was in join with another PhysicalTable in this BusinessView
			EList<BusinessViewInnerJoinRelationship> joinRelationships = businessView.getJoinRelationships();
			for (BusinessViewInnerJoinRelationship joinRelationship : joinRelationships) {
				// if the physicalTable to remove is used as a SourceTable in a joinRelationship
				// then DO NOT remove the physical table
				if (joinRelationship.getSourceTable() == innerJoinRelationship.getDestinationTable()) {
					return null;
				}
			}

			// remove the inner join relationship between two physical table
			businessView.getJoinRelationships().remove(innerJoinRelationship);

			// remove physical table's columns
			List<SimpleBusinessColumn> businessColumns = businessView.getSimpleBusinessColumns();
			for (SimpleBusinessColumn businessColumn : businessColumns) {
				if (businessColumn.getPhysicalColumn().getTable() == innerJoinRelationship.getDestinationTable()) {
					businessView.getColumns().remove(businessColumn);
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to remove physical table to business view", t);
		}
		return businessView;
	}

	/**
	 * Create BusinessViewInnerJoinRelationship from a BusinessViewInnerJoinRelationshipDescriptor
	 * 
	 * @param businessModel
	 * @return
	 */
	public BusinessViewInnerJoinRelationship addBusinessViewInnerJoinRelationship(BusinessModel businessModel,
			BusinessViewInnerJoinRelationshipDescriptor innerJoinRelationshipDescriptor) {
		BusinessViewInnerJoinRelationship innerJoinRelationship;
		try {
			innerJoinRelationship = FACTORY.createBusinessViewInnerJoinRelationship();
			innerJoinRelationship.setSourceTable(innerJoinRelationshipDescriptor.getSourceTable());
			innerJoinRelationship.getSourceColumns().addAll(innerJoinRelationshipDescriptor.getSourceColumns());
			innerJoinRelationship.setDestinationTable(innerJoinRelationshipDescriptor.getDestinationTable());
			innerJoinRelationship.getDestinationColumns().addAll(innerJoinRelationshipDescriptor.getDestinationColumns());

			// get max identifier for innerJoinRelationship
			long maxId = getMaxNumberInnerJoinRelationship(businessModel);
			// increase value to set new identifier
			maxId = maxId + 1;
			innerJoinRelationship.setId(new Long(maxId).toString());

			// add BusinessViewInnerJoinRelationship properties
			getPropertiesInitializer().addProperties(innerJoinRelationship);

			// add BusinessViewInnerJoinRelationship to BusinessModel
			businessModel.getJoinRelationships().add(innerJoinRelationship);

		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize business view inner join relationship", t);
		}
		return innerJoinRelationship;
	}

	/**
	 * Remove BusinessViewInnerJoinRelationship from a BusinessViewInnerJoinRelationshipDescriptor
	 * 
	 * @param businessModel
	 * @return
	 */
	public BusinessViewInnerJoinRelationship removeBusinessViewInnerJoinRelationship(BusinessModel businessModel,
			BusinessViewInnerJoinRelationshipDescriptor innerJoinRelationshipDescriptor) {
		EList<BusinessViewInnerJoinRelationship> joinRelationships;
		BusinessViewInnerJoinRelationship innerJoinRelationship = null;
		try {

			joinRelationships = businessModel.getJoinRelationships();
			// search the corresponding BusinessViewInnerJoinRelationship
			for (BusinessViewInnerJoinRelationship joinRelationship : joinRelationships) {
				if ((joinRelationship.getSourceTable() == innerJoinRelationshipDescriptor.getSourceTable())
						&& (joinRelationship.getDestinationTable() == innerJoinRelationshipDescriptor.getDestinationTable())) {
					innerJoinRelationship = joinRelationship;
					break;
				}
			}
			// remove BusinessViewInnerJoinRelationship to BusinessModel
			businessModel.getJoinRelationships().remove(innerJoinRelationship);

		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize business view inner join relationship", t);
		}
		return innerJoinRelationship;
	}

	/**
	 * Check if the relationships defined in the model respect the constraints required for Hibernate
	 * 
	 * @return the collection of incorrect relationships
	 */
	public List<Pair<BusinessRelationship, Integer>> checkRelationshipsConstraints(BusinessModel businessModel) {
		List<BusinessRelationship> businessRelationships = businessModel.getRelationships();
		List<Pair<BusinessRelationship, Integer>> incorrectBusinessRelationships = new ArrayList<Pair<BusinessRelationship, Integer>>();
		for (BusinessRelationship businessRelationship : businessRelationships) {
			// Check that the relationship has (at least) the same number of source columns
			// as the destination table's identifier if there isn't an identifier all the columns are used automatically in a default identifier)
			int requiredNumberOfColumns = 0;
			BusinessColumnSet destinationTable = businessRelationship.getDestinationTable();

			BusinessIdentifier destinationTableIdentifier = destinationTable.getIdentifier();

			if (destinationTableIdentifier == null) {
				// identifier not defined, use all the columns in automatic identifier
				requiredNumberOfColumns = destinationTable.getColumns().size();
			} else {
				requiredNumberOfColumns = destinationTableIdentifier.getColumns().size();
			}

			int numberOfSourceColumns = businessRelationship.getSourceColumns().size();

			if ((requiredNumberOfColumns > 0) && (numberOfSourceColumns < requiredNumberOfColumns)) {
				// the relationship doesn't satisfy the constraint for the hibernate mapping
				incorrectBusinessRelationships.add(new Pair(businessRelationship, requiredNumberOfColumns));
			}

		}
		return incorrectBusinessRelationships;
	}

	// --------------------------------------------------------
	// Accessor methods
	// --------------------------------------------------------

	public IPropertiesInitializer getPropertiesInitializer() {
		return propertiesInitializer;
	}

	public void setPropertiesInitializer(IPropertiesInitializer propertyInitializer) {
		this.propertiesInitializer = propertyInitializer;
	}

	/*
	 * Check if business table name is already in use in the Business Model
	 */
	public boolean checkNameAlreadyUsed(BusinessModel businessModel, String businessTableName) {
		if (businessModel.getTable(businessTableName) != null) {
			return true;
		} else {
			return false;
		}
	}

	public BusinessModelNamesInitializer getNamesInitializer() {
		return namesInitializer;
	}

	public void setNamesInitializer(BusinessModelNamesInitializer namesInitializer) {
		this.namesInitializer = namesInitializer;
	}

	/*
	 * Return the current maximum number used as identifier for InnerJoinRelationship objects
	 */
	public long getMaxNumberInnerJoinRelationship(BusinessModel model) {
		EList<BusinessViewInnerJoinRelationship> joinRelationships = model.getJoinRelationships();
		long maxId = 0;
		for (BusinessViewInnerJoinRelationship joinRelation : joinRelationships) {
			// id is a number that I convert to long
			String stringId = joinRelation.getId();
			long longId = Long.parseLong(stringId.trim());
			if (longId > maxId) {
				maxId = longId;
			}
		}
		return maxId;
	}

	// --------------------------------------------------------
	// Static methods
	// --------------------------------------------------------

	public static String beutfyName(String name) {
		return StringUtils.capitalize(name.replace("_", " "));
	}

	private static void log(String msg) {
		// System.out.println(msg);
	}
}
