/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.

 **/
package it.eng.knowage.meta.generator.jpamapping.wrappers.impl;

import it.eng.knowage.meta.exception.KnowageMetaException;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaCalculatedColumn;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaColumn;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaSubEntity;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaTable;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaView;
import it.eng.knowage.meta.generator.jpamapping.wrappers.JpaProperties;
import it.eng.knowage.meta.generator.utils.JavaKeywordsUtils;
import it.eng.knowage.meta.model.ModelProperty;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.business.BusinessView;
import it.eng.knowage.meta.model.business.BusinessViewInnerJoinRelationship;
import it.eng.knowage.meta.model.business.CalculatedBusinessColumn;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalTable;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JpaView implements IJpaView {
	private final BusinessView businessView;
	List<BusinessColumnSet> parents;
	private static Logger logger = LoggerFactory.getLogger(JpaViewInnerTable.class);
	List<IJpaSubEntity> allSubEntities = new ArrayList<IJpaSubEntity>();
	List<IJpaCalculatedColumn> jpaCalculatedColumns;

	protected JpaView(BusinessView businessView) {
		super();
		Assert.assertNotNull("Parameter [businessView] cannot be null", businessView);
		this.businessView = businessView;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaView#getPackage()
	 */
	@Override
	public String getPackage() {
		logger.debug("IN");
		String result = null;
		ModelProperty property = getModel().getProperties().get(JpaProperties.MODEL_PACKAGE);
		// check if property is setted, else get default value
		if (property.getValue() != null) {
			result = property.getValue();
		} else {
			result = property.getPropertyType().getDefaultValue();
		}
		logger.debug("OUT: " + result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaView#getClassName()
	 */
	@Override
	public String getClassName() {
		String name;

		name = null;
		try {
			name = JavaKeywordsUtils.transformToJavaClassName(businessView.getUniqueName());
		} catch (Throwable t) {
			logger.error("Impossible to get class name", t);
			name = "unknown";
		}
		return name;
	}

	protected BusinessModel getModel() {
		return businessView.getModel();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaView#getInnerTables()
	 */
	@Override
	public List<IJpaTable> getInnerTables() {
		List<IJpaTable> innerTables;
		List<PhysicalTable> physiscalTables;

		physiscalTables = businessView.getPhysicalTables();

		innerTables = new ArrayList<IJpaTable>();
		for (PhysicalTable physicaltable : physiscalTables) {
			innerTables.add(new JpaViewInnerTable(businessView, physicaltable));
		}
		return innerTables;
	}

	/**
	 * Return list of jpaColumns that belongs to the View used
	 *
	 * for qbe.properties generation and to maintain the columns order in the query view
	 *
	 *
	 * @return
	 */
	public List<IJpaColumn> getColumns() {
		List<IJpaColumn> jpaColumns = new ArrayList<IJpaColumn>();
		List<IJpaTable> innerTables = getInnerTables();

		List<BusinessColumn> businessColumns = businessView.getColumns();
		for (BusinessColumn businessColumn : businessColumns) {
			if (businessColumn instanceof SimpleBusinessColumn) {
				JpaViewInnerTable correspondingInnerTable = getCorrespondingInnerTable(innerTables, (SimpleBusinessColumn) businessColumn);
				JpaColumn jpaColumn = new JpaColumn(correspondingInnerTable, (SimpleBusinessColumn) businessColumn);
				jpaColumns.add(jpaColumn);
			}
		}
		return jpaColumns;
	}

	private JpaViewInnerTable getCorrespondingInnerTable(List<IJpaTable> innerTables, SimpleBusinessColumn businessColumn) {

		PhysicalColumn physicalColumn = businessColumn.getPhysicalColumn();
		for (IJpaTable innerTable : innerTables) {
			if (innerTable instanceof JpaViewInnerTable) {
				JpaViewInnerTable jpaViewInnerTable = (JpaViewInnerTable) innerTable;
				PhysicalTable innerTablePhysicalTable = jpaViewInnerTable.getPhysicalTable();
				if (innerTablePhysicalTable.getColumns().contains(physicalColumn)) {
					return jpaViewInnerTable;
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaView#getColumns(it.eng.knowage.meta.generator.jpamapping.wrappers.impl.JpaViewInnerTable)
	 */
	@Override
	public List<IJpaColumn> getColumns(JpaViewInnerTable table) {
		List<IJpaColumn> jpaColumns = new ArrayList<IJpaColumn>();
		List<SimpleBusinessColumn> businessColumns = businessView.getSimpleBusinessColumns();
		for (SimpleBusinessColumn businessColumn : businessColumns) {
			if (businessColumn.getPhysicalColumn().getTable() == table.getPhysicalTable()) {
				JpaViewInnerTable jpaTable = new JpaViewInnerTable(businessView, businessColumn.getPhysicalColumn().getTable());
				JpaColumn jpaColumn = new JpaColumn(jpaTable, businessColumn);
				jpaColumns.add(jpaColumn);
			}
		}

		return jpaColumns;
	}

	public List<IJpaCalculatedColumn> getCalculatedColumns() {
		try {
			if (jpaCalculatedColumns == null) {
				jpaCalculatedColumns = new ArrayList<IJpaCalculatedColumn>();
				for (CalculatedBusinessColumn calculatedBusinessColumn : businessView.getCalculatedBusinessColumns()) {
					IJpaTable foundTable = null;
					List<SimpleBusinessColumn> referencedColumns = calculatedBusinessColumn.getReferencedColumns();
					List<IJpaTable> innerTables = this.getInnerTables();
					// check if all the referenced columns are contained in an inner table
					for (IJpaTable innerTable : innerTables) {
						System.out.println("Checking table: " + innerTable.getName());
						if (((JpaViewInnerTable) innerTable).getBusinessColumnsOfInnerTable().containsAll(referencedColumns)) {
							foundTable = innerTable;
							System.out.println("Found table: " + foundTable.getName());
							break;
						}
					}
					if (foundTable != null) {
						JpaCalculatedColumn jpaCalculatedColumn = new JpaCalculatedColumn((AbstractJpaTable) foundTable, calculatedBusinessColumn);
						jpaCalculatedColumns.add(jpaCalculatedColumn);
						logger.debug("Business table [{}] contains calculated column [{}]", businessView.getName(), calculatedBusinessColumn.getName());
					}

				}
			}
		} catch (KnowageMetaException e) {
			logger.error("Calculated Column in JPAView error: ");
			logger.error(e.getMessage());
		}

		return jpaCalculatedColumns;
	}

	public String getUniqueNameWithDoubleDots() {
		return getUniqueName().replaceAll("/", ":");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaView#getJoinRelationships()
	 */
	@Override
	public List<JpaViewInnerJoinRelatioship> getJoinRelationships() {
		List<JpaViewInnerJoinRelatioship> jpaViewInnerJoinRelatioships;
		List<BusinessViewInnerJoinRelationship> joinRelationships;

		jpaViewInnerJoinRelatioships = new ArrayList<JpaViewInnerJoinRelatioship>();
		joinRelationships = businessView.getJoinRelationships();
		for (BusinessViewInnerJoinRelationship joinRealtionship : joinRelationships) {
			JpaViewInnerJoinRelatioship jpaViewInnerJoinRelatioship = new JpaViewInnerJoinRelatioship(businessView, joinRealtionship);
			jpaViewInnerJoinRelatioships.add(jpaViewInnerJoinRelatioship);
		}

		return jpaViewInnerJoinRelatioships;
	}

	@Override
	public List<JpaViewOuterRelationship> getRelationships() {

		// logger.trace("IN");
		// logger.debug("Business view [{}] have  [{}] relationships", businessView.getName(), businessView.getRelationships().size());
		//
		// for(BusinessRelationship relationship : businessView.getRelationships()) {
		// logger.debug("Business view [{}] contains relationship  [{}] ", businessView.getName(), relationship.getName());
		//
		// }
		//
		// logger.trace("OUT");
		// return businessView.getRelationships();

		List<JpaViewOuterRelationship> viewRelationships = new ArrayList<JpaViewOuterRelationship>();

		for (BusinessRelationship relationship : businessView.getRelationships()) {
			logger.debug("Business view [{}] contains relationship  [{}] ", businessView.getName(), relationship.getName());
			boolean isOutbound = false;
			// check if this is an outbound relationship from businessView to another businessColumnSet
			if (relationship.getSourceTable().equals(businessView)) {
				isOutbound = true;
			}
			JpaViewOuterRelationship viewRelationship = new JpaViewOuterRelationship(this, relationship, isOutbound);
			viewRelationships.add(viewRelationship);
		}
		return viewRelationships;

	}

	@Override
	public String getQualifiedClassName() {
		return getPackage() + "." + getClassName();
	}

	@Override
	public String getUniqueName() {
		return getQualifiedClassName() + "//" + getClassName();
	}

	@Override
	public String getName() {
		return businessView.getName();
	}

	@Override
	public String getDescription() {
		return businessView.getDescription() != null ? businessView.getDescription() : getName();
	}

	@Override
	public String getAttribute(String name) {
		ModelProperty property = businessView.getProperties().get(name);
		return property != null ? property.getValue() : "";
	}

	@Override
	public List<IJpaSubEntity> getSubEntities() {
		// List<IJpaSubEntity> subEntities = new ArrayList<IJpaSubEntity>();
		allSubEntities.clear();

		for (BusinessRelationship relationship : businessView.getRelationships()) {
			if (relationship.getSourceTable() != businessView)
				continue;

			// List of parents to avoid cyclic exploration
			parents = new ArrayList<BusinessColumnSet>();

			JpaSubEntity subEntity = new JpaSubEntity(businessView, null, relationship);
			// subEntities.add(subEntity);
			allSubEntities.add(subEntity);
			parents.add(businessView);

			List<IJpaSubEntity> levelEntities = new ArrayList<IJpaSubEntity>();
			levelEntities.addAll(subEntity.getChildren());

			allSubEntities.addAll(levelEntities);
			// add children to max deep level of 10
			for (int i = 0; i < 8; i++) {
				List<IJpaSubEntity> nextLevel = getSubLevelEntities(levelEntities);
				allSubEntities.addAll(nextLevel);
				levelEntities = nextLevel;
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

}
