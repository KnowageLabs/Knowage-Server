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
package it.eng.knowage.meta.generator.jpamapping.wrappers.impl;

import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaCalculatedColumn;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaColumn;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaRelationship;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaSubEntity;
import it.eng.knowage.meta.generator.utils.JavaKeywordsUtils;
import it.eng.knowage.meta.initializer.properties.PhysicalModelPropertiesFromFileInitializer;
import it.eng.knowage.meta.model.ModelProperty;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.CalculatedBusinessColumn;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;
import it.eng.knowage.meta.model.physical.PhysicalModel;
import it.eng.knowage.meta.model.physical.PhysicalTable;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wrap a business table and provide all the utility methods used by the template engine in order to generate the java class mapping
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JpaTable extends AbstractJpaTable {

	BusinessTable businessTable;
	String quoteString;
	List<IJpaSubEntity> allSubEntities = new ArrayList<IJpaSubEntity>();
	List<BusinessColumnSet> parents;
	List<IJpaCalculatedColumn> jpaCalculatedColumns;

	private static Logger logger = LoggerFactory.getLogger(JpaTable.class);

	protected JpaTable(BusinessTable businessTable) {
		super(businessTable.getPhysicalTable());
		this.businessTable = businessTable;

		PhysicalModel physicalModel = businessTable.getModel().getPhysicalModel();
		ModelProperty modelProperty = physicalModel.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_DATABASE_QUOTESTRING);
		if (modelProperty != null) {
			quoteString = modelProperty.getValue();
		} else {
			quoteString = "";
		}

		initColumnTypesMap();
	}

	@Override
	List<BusinessColumn> getBusinessColumns() {
		List<BusinessColumn> businessColumns = new ArrayList<BusinessColumn>();
		businessColumns.addAll(businessTable.getSimpleBusinessColumns());
		return businessColumns;
	}

	@Override
	public List<BusinessRelationship> getBusinessRelationships() {
		return businessTable.getRelationships();
	}

	@Override
	protected BusinessModel getModel() {
		return businessTable.getModel();
	}

	public BusinessTable getBusinessTable() {
		return businessTable;
	}

	@Override
	public String getCatalog() {
		String useCatalog = getUseCatalog();
		if ((useCatalog != null) && (useCatalog.equalsIgnoreCase("true"))) {
			logger.debug("Catalog is: " + getModel().getPhysicalModel().getCatalog());
			String catalog = getModel().getPhysicalModel().getCatalog();
			if (catalog != null) {
				if (!quoteString.equals(" ")) {
					catalog = "`" + catalog + "`";
				}
			}

			// if(catalog!=null && !catalog.equals("") && getModel().getPhysicalModel().getDatabaseName().contains("PostgreSQL")){
			// catalog = "\\\""+catalog+"\\\"";
			// }
			return catalog;
		} else {
			return null;
		}

	}

	@Override
	public String getSchema() {
		String useSchema = getUseSchema();
		if ((useSchema != null) && (useSchema.equalsIgnoreCase("true"))) {
			logger.debug("Schema is: " + getModel().getPhysicalModel().getSchema());
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
	public PhysicalTable getPhysicalTable() {
		return businessTable.getPhysicalTable();
	}

	/**
	 * Returns the <code>JpaColumn</code> objects to be generated for this table.
	 */
	@Override
	public List<IJpaColumn> getColumns() {
		if (jpaColumns == null) {
			jpaColumns = new ArrayList<IJpaColumn>();
			for (SimpleBusinessColumn businessColumn : businessTable.getSimpleBusinessColumns()) {
				JpaColumn jpaColumn = new JpaColumn(this, businessColumn);
				jpaColumns.add(jpaColumn);
				logger.debug("Business table [{}] contains column [{}]", businessTable.getName(), businessColumn.getName());

			}
		}
		return jpaColumns;
	}

	@Override
	public List<IJpaCalculatedColumn> getCalculatedColumns() {
		if (jpaCalculatedColumns == null) {
			jpaCalculatedColumns = new ArrayList<IJpaCalculatedColumn>();
			for (CalculatedBusinessColumn calculatedBusinessColumn : businessTable.getCalculatedBusinessColumns()) {
				JpaCalculatedColumn jpaCalculatedColumn = new JpaCalculatedColumn(this, calculatedBusinessColumn);
				jpaCalculatedColumns.add(jpaCalculatedColumn);
				logger.debug("Business table [{}] contains calculated column [{}]", businessTable.getName(), calculatedBusinessColumn.getName());

			}
		}
		return jpaCalculatedColumns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaTable#hasFakePrimaryKey()
	 */
	@Override
	public boolean hasFakePrimaryKey() {
		return !(businessTable.getIdentifier() != null ? businessTable.getIdentifier().getColumns().size() > 0 : false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaTable#hasCompositeKey()
	 */
	@Override
	public boolean hasCompositeKey() {
		boolean hasCompositeKey = false;

		if (businessTable.getIdentifier() != null) { // if there's a key...
			if (businessTable.getIdentifier().getColumns().size() > 1) { // ...and it is composed by more then one column
				hasCompositeKey = true;
			}
		} else { // if there isn't a key
			hasCompositeKey = true;
			// we return true because we are going to generate a fake key composed by
			// all columns in the table in order to keep jpa runtime happy (in jpa as in
			// hibernate any persisted object must have a key)
		}

		return hasCompositeKey;
	}

	/**
	 * @return the <code>JpaRelationship</code> that contains this table
	 */
	@Override
	public List<IJpaRelationship> getRelationships() {
		List<IJpaRelationship> jpaRelationships;
		JpaRelationship jpaRelationship;

		logger.trace("IN");

		jpaRelationships = new ArrayList<IJpaRelationship>();
		logger.debug("Business table [{}] have  [{}] relationships", businessTable.getName(), businessTable.getRelationships().size());

		for (BusinessRelationship relationship : businessTable.getRelationships()) {
			logger.debug("Business table [{}] contains relationship  [{}] ", businessTable.getName(), relationship.getName());

			jpaRelationship = new JpaRelationship(this, relationship);
			jpaRelationships.add(jpaRelationship);
		}

		logger.trace("OUT");
		return jpaRelationships;
	}

	/**
	 * @returns the generated java class name (not qualified).
	 */
	@Override
	public String getClassName() {
		String name;
		name = JavaKeywordsUtils.transformToJavaClassName(businessTable.getUniqueName());
		return name;
	}

	@Override
	public String getName() {
		return businessTable.getName();
	}

	@Override
	public String getDescription() {
		return businessTable.getDescription() != null ? businessTable.getDescription() : getName();
	}

	@Override
	public String getSqlName() {

		String name = businessTable.getPhysicalTable().getName();
		return name;
	}

	@Override
	public String getQuotedMappingTableName() {
		String name = businessTable.getPhysicalTable().getName();
		if (!quoteString.equals(" ")) {
			name = quoteString + name + quoteString;
		}
		return name;
	}

	@Override
	public String getAttribute(String name) {
		ModelProperty property = businessTable.getProperties().get(name);
		return property != null ? property.getValue() : "";
	}

	public String getType() {
		String tableType = getAttribute("structural.tabletype");
		if (tableType != null) {
			tableType = tableType.replaceAll(" ", "_");
		}
		return tableType;
	}

	@Override
	public List<IJpaSubEntity> getSubEntities() {
		// List<IJpaSubEntity> subEntities = new ArrayList<IJpaSubEntity>();
		allSubEntities.clear();

		for (BusinessRelationship relationship : businessTable.getRelationships()) {
			if (relationship.getSourceTable() != businessTable)
				continue;

			// List of parents to avoid cyclic exploration
			parents = new ArrayList<BusinessColumnSet>();

			JpaSubEntity subEntity = new JpaSubEntity(businessTable, null, relationship);
			// subEntities.add(subEntity);
			allSubEntities.add(subEntity);
			// System.out.println("Added "+subEntity.getName()+" to AllSubEntities");
			parents.add(businessTable);
			// System.out.println("Added "+businessTable.getName()+" to parents");

			List<IJpaSubEntity> levelEntities = new ArrayList<IJpaSubEntity>();
			levelEntities.addAll(subEntity.getChildren());
			// System.out.println("Added children of "+subEntity.getName()+" to level Entities");

			allSubEntities.addAll(levelEntities);
			// add children to max deep level of 10
			for (int i = 0; i < 8; i++) {
				if (!levelEntities.isEmpty()) {
					List<IJpaSubEntity> nextLevel = getSubLevelEntities(levelEntities);
					allSubEntities.addAll(nextLevel);
					levelEntities = nextLevel;
				}
				logger.debug("getSubEntities iteration level is [{}]", i);
			}

		}

		return allSubEntities;
	}

	public List<IJpaSubEntity> getSubLevelEntities(List<IJpaSubEntity> entities) {
		List<IJpaSubEntity> subEntities = new ArrayList<IJpaSubEntity>();
		for (IJpaSubEntity entity : entities) {
			BusinessColumnSet businessColumnSet = ((JpaSubEntity) entity).getBusinessColumnSet();
			if (!parents.contains(businessColumnSet)) {
				subEntities.addAll(((JpaSubEntity) entity).getChildren());
				parents.add(businessColumnSet);
			}
		}
		return subEntities;
	}

	@Override
	public boolean isInnerTable() {
		return false;
	}

	@Override
	public String getUseSchema() {
		BusinessModel businessModel = businessTable.getModel();
		ModelProperty property = businessModel.getProperties().get("structural.mapping.useSchema");
		return property != null ? property.getValue() : "";
	}

	@Override
	public String getUseCatalog() {
		BusinessModel businessModel = businessTable.getModel();
		ModelProperty property = businessModel.getProperties().get("structural.mapping.useCatalog");
		return property != null ? property.getValue() : "";
	}

}
