/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.knowage.meta.generator.jpamapping.wrappers.impl;

import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaCalculatedColumn;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaColumn;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaRelationship;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaSubEntity;
import it.eng.knowage.meta.generator.utils.JavaKeywordsUtils;
import it.eng.knowage.meta.generator.utils.StringUtils;
import it.eng.spagobi.meta.initializer.properties.PhysicalModelPropertiesFromFileInitializer;
import it.eng.spagobi.meta.model.ModelProperty;
import it.eng.spagobi.meta.model.business.BusinessColumn;
import it.eng.spagobi.meta.model.business.BusinessModel;
import it.eng.spagobi.meta.model.business.BusinessRelationship;
import it.eng.spagobi.meta.model.business.BusinessView;
import it.eng.spagobi.meta.model.business.CalculatedBusinessColumn;
import it.eng.spagobi.meta.model.business.SimpleBusinessColumn;
import it.eng.spagobi.meta.model.physical.PhysicalColumn;
import it.eng.spagobi.meta.model.physical.PhysicalModel;
import it.eng.spagobi.meta.model.physical.PhysicalTable;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class wrap a physical table used within a business view and provide all the utility methods used by the template engine in order to generate the java
 * class mapping
 * 
 * @authors Angelo Bernabei( angelo.bernabei@eng.it) Andrea Gioia (andrea.gioia@eng.it)
 */
public class JpaViewInnerTable extends AbstractJpaTable {

	private BusinessView businessView;
	private List<IJpaCalculatedColumn> jpaCalculatedColumns;
	private List<BusinessColumn> businessColumnOfInnerTable;
	String quoteString;

	private static Logger logger = LoggerFactory.getLogger(JpaViewInnerTable.class);

	/**
	 * @param businessView
	 *            The business view that contains the physical table
	 * @param physicalTable
	 *            The physical table used to write this java class
	 */
	protected JpaViewInnerTable(BusinessView businessView, PhysicalTable physicalTable) {
		super(physicalTable);

		Assert.assertNotNull("Parameter [businessView] cannot be null", businessView);
		Assert.assertTrue("Parameter [physicalTable] is not contained in parameter [businessView]", businessView.getPhysicalTables().contains(physicalTable));

		logger.debug("Inner table [" + physicalTable.getName() + "] of view [" + businessView.getName() + "]");

		this.businessView = businessView;
		this.physicalTable = physicalTable;

		PhysicalModel physicalModel = physicalTable.getModel();
		ModelProperty modelProperty = physicalModel.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_DATABASE_QUOTESTRING);
		if (modelProperty != null) {
			quoteString = modelProperty.getValue();
		} else {
			quoteString = "";
		}

		initColumnTypesMap();
	}

	@Override
	public List<BusinessColumn> getBusinessColumns() {
		List<BusinessColumn> businessColumns = new ArrayList<BusinessColumn>();
		businessColumns.addAll(businessView.getSimpleBusinessColumns());
		return businessColumns;
	}

	@Override
	public List<BusinessRelationship> getBusinessRelationships() {
		return businessView.getRelationships();
	}

	@Override
	protected BusinessModel getModel() {
		return businessView.getModel();
	}

	public BusinessView getBusinessView() {
		return businessView;
	}

	public void setBusinessView(BusinessView businessView) {
		this.businessView = businessView;
	}

	@Override
	public List<IJpaColumn> getColumns() {

		logger.trace("IN");

		if (jpaColumns == null) {
			jpaColumns = new ArrayList<IJpaColumn>();

			for (PhysicalColumn physicalColumn : physicalTable.getColumns()) {
				BusinessColumn businessColumn = findColumnInBusinessView(physicalColumn);
				// if the columns belong to the BusinessView
				if (businessColumn != null) {
					if (businessColumn instanceof SimpleBusinessColumn) {
						JpaColumn jpaColumn = new JpaColumn(this, (SimpleBusinessColumn) businessColumn);
						jpaColumns.add(jpaColumn);
						logger.info("Add " + jpaColumn.getSqlName() + " real column to the BV " + businessView.getName());
					}
				} else {
					JpaFakeColumn fakeColumn = new JpaFakeColumn(this, physicalColumn);
					jpaColumns.add(fakeColumn);
					logger.info("Add " + fakeColumn.getSqlName() + " fake column to the BV " + businessView.getName());
				}
			}
		}

		logger.trace("OUT");

		return jpaColumns;
	}

	public List<BusinessColumn> getBusinessColumnsOfInnerTable() {

		logger.trace("IN");

		if (businessColumnOfInnerTable == null) {
			businessColumnOfInnerTable = new ArrayList<BusinessColumn>();

			for (PhysicalColumn physicalColumn : physicalTable.getColumns()) {
				BusinessColumn businessColumn = findColumnInBusinessView(physicalColumn);
				// if the colums belong to the BusinessView
				if (businessColumn != null) {
					if (businessColumn instanceof SimpleBusinessColumn) {
						businessColumnOfInnerTable.add(businessColumn);
						logger.info("Found " + businessColumn.getName() + " of this Inner Table" + this.getName());
					}
				}
			}
		}

		logger.trace("OUT");

		return businessColumnOfInnerTable;
	}

	@Override
	public List<IJpaCalculatedColumn> getCalculatedColumns() {
		if (jpaCalculatedColumns == null) {
			jpaCalculatedColumns = new ArrayList<IJpaCalculatedColumn>();
			for (CalculatedBusinessColumn calculatedBusinessColumn : businessView.getCalculatedBusinessColumns()) {
				JpaCalculatedColumn jpaCalculatedColumn = new JpaCalculatedColumn(this, calculatedBusinessColumn);
				jpaCalculatedColumns.add(jpaCalculatedColumn);
				logger.debug("Business table [{}] contains calculated column [{}]", businessView.getName(), calculatedBusinessColumn.getName());

			}
		}
		return jpaCalculatedColumns;
	}

	/**
	 * Check if the physical column belong to the view
	 * 
	 * @param physicalColumn
	 * 
	 * @return the business column that wrap the physical column if it belong to the view. null otherwise
	 */
	protected BusinessColumn findColumnInBusinessView(PhysicalColumn physicalColumn) {

		for (SimpleBusinessColumn businessColumn : businessView.getSimpleBusinessColumns()) {
			if (physicalColumn.equals(businessColumn.getPhysicalColumn())) {
				return businessColumn;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaTable#hasFakePrimaryKey()
	 */
	@Override
	public boolean hasFakePrimaryKey() {
		return true;
		// return !(physicalTable.getPrimaryKey() != null? physicalTable.getPrimaryKey().getColumns().size() > 0 : false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaTable#hasCompositeKey()
	 */
	@Override
	public boolean hasCompositeKey() {
		return true;
		/*
		 * boolean hasCompositeKey = false;
		 * 
		 * if(physicalTable.getPrimaryKey() != null) { // if there's a key... if(physicalTable.getPrimaryKey().getColumns().size() > 1) { // ...and it is
		 * composed by more then one column hasCompositeKey = true; } } else { // if there isn't a key hasCompositeKey = true; // we return true because we are
		 * going to generate a fake key composed by // all columns in the table in order to keep jpa runtime happy (in jpa as in // hibernate any persisted
		 * object must have a key) }
		 * 
		 * return hasCompositeKey;
		 */
	}

	@Override
	public String getClassName() {
		String name;

		name = null;
		try {
			name = JavaKeywordsUtils.transformToJavaClassName(businessView.getUniqueName() + "_" + physicalTable.getName());
		} catch (Throwable t) {
			logger.error("Impossible to get class name", t);
		}
		return name;
	}

	/**
	 * Return the <code>JpaRelationship</code> that contains this table We have to ADD only the relationship belong to this Physical Table.
	 * 
	 * @return
	 */
	@Override
	public List<IJpaRelationship> getRelationships() {
		List<IJpaRelationship> jpaRelationships;
		JpaRelationship jpaRelationship;

		logger.trace("IN");

		jpaRelationships = new ArrayList<IJpaRelationship>();

		for (BusinessRelationship relationship : getBusinessRelationships()) {
			PhysicalTable sourceTable;

			sourceTable = relationship.getSourceSimpleBusinessColumns().get(0).getPhysicalColumn().getTable();

			if (sourceTable.equals(physicalTable)) {
				jpaRelationship = new JpaRelationship(this, relationship);
				jpaRelationships.add(jpaRelationship);
			}
		}
		logger.trace("OUT");
		return jpaRelationships;
	}

	/**
	 * 
	 * @param t
	 * @param r
	 * @return
	 */
	private boolean isBelongToRelationship(PhysicalTable t, BusinessRelationship r) {
		if (r != null && r.getSourceTable() != null && t.getName() != null) {
			List<SimpleBusinessColumn> source = r.getSourceSimpleBusinessColumns();

			for (SimpleBusinessColumn bc : source) {
				PhysicalTable fTable = bc.getPhysicalColumn().getTable();
				if (t.getName().equals(fTable.getName()))
					return true;
			}

		}
		if (r != null && r.getDestinationTable() != null && t.getName() != null) {
			List<SimpleBusinessColumn> source = r.getDestinationSimpleBusinessColumns();

			for (SimpleBusinessColumn bc : source) {
				PhysicalTable fTable = bc.getPhysicalColumn().getTable();
				if (t.getName().equals(fTable.getName()))
					return true;
			}
		}

		return false;
	}

	@Override
	public String getCatalog() {
		String useCatalog = getUseCatalog();
		if ((useCatalog != null) && (useCatalog.equalsIgnoreCase("true"))) {
			String catalog = getModel().getPhysicalModel().getCatalog();
			if (catalog != null && !catalog.equals("")) {
				catalog = "`" + catalog + "`";
			}
			return catalog;
		} else {
			return null;
		}

	}

	@Override
	public String getSchema() {
		String useSchema = getUseSchema();
		if ((useSchema != null) && (useSchema.equalsIgnoreCase("true"))) {
			String schema = getModel().getPhysicalModel().getSchema();
			if (schema != null && !schema.equals("")) {
				if (!quoteString.equals(" ")) {
					schema = quoteString + schema + quoteString;
				}
			}
			return schema;
		} else {
			return null;
		}

	}

	@Override
	public String getName() {
		return businessView.getName() + " > " + StringUtils.initUpper(physicalTable.getName().replace("_", " "));
	}

	@Override
	public String getDescription() {
		return physicalTable.getDescription() != null ? physicalTable.getDescription() : "";
	}

	@Override
	public String getSqlName() {
		return physicalTable.getName();
	}

	@Override
	public String getQuotedMappingTableName() {
		String name = physicalTable.getName();
		return quoteString + name + quoteString;
	}

	@Override
	public String getAttribute(String name) {
		ModelProperty property = businessView.getProperties().get(name);
		return property != null ? property.getValue() : "";
	}

	@Override
	public List<IJpaSubEntity> getSubEntities() {
		List<IJpaSubEntity> subEntities = new ArrayList<IJpaSubEntity>();

		for (BusinessRelationship relationship : businessView.getRelationships()) {
			if (relationship.getSourceTable() != businessView)
				continue;

			JpaSubEntity subEntity = new JpaSubEntity(this, null, relationship);
			subEntities.add(subEntity);
		}

		return subEntities;
	}

	@Override
	public boolean isInnerTable() {
		return true;
	}

	@Override
	public String getUseSchema() {
		BusinessModel businessModel = businessView.getModel();
		ModelProperty property = businessModel.getProperties().get("structural.mapping.useSchema");
		return property != null ? property.getValue() : "";
	}

	@Override
	public String getUseCatalog() {
		BusinessModel businessModel = businessView.getModel();
		ModelProperty property = businessModel.getProperties().get("structural.mapping.useCatalog");
		return property != null ? property.getValue() : "";
	}

}
