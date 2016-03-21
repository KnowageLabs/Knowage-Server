/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.knowage.meta.generator.jpamapping.wrappers.impl;

import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaColumn;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaTable;
import it.eng.knowage.meta.generator.jpamapping.wrappers.JpaProperties;
import it.eng.knowage.meta.generator.utils.JavaKeywordsUtils;
import it.eng.knowage.meta.generator.utils.StringUtils;
import it.eng.spagobi.meta.initializer.properties.PhysicalModelPropertiesFromFileInitializer;
import it.eng.spagobi.meta.model.ModelProperty;
import it.eng.spagobi.meta.model.business.BusinessColumn;
import it.eng.spagobi.meta.model.business.BusinessRelationship;
import it.eng.spagobi.meta.model.business.BusinessView;
import it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship;
import it.eng.spagobi.meta.model.business.SimpleBusinessColumn;
import it.eng.spagobi.meta.model.physical.PhysicalColumn;
import it.eng.spagobi.meta.model.physical.PhysicalModel;
import it.eng.spagobi.meta.model.util.JDBCTypeMapper;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class JpaColumn implements IJpaColumn {
	SimpleBusinessColumn businessColumn;
	AbstractJpaTable jpaTable;
	String quoteString;

	private static Logger logger = LoggerFactory.getLogger(JpaColumn.class);

	/**
	 * 
	 * @param parentTable
	 *            the jpaTable that contains this column
	 * @param businessColumn
	 *            the wrapped business column
	 */
	protected JpaColumn(AbstractJpaTable parentTable, SimpleBusinessColumn businessColumn) {
		this.jpaTable = parentTable;
		this.businessColumn = businessColumn;
		PhysicalModel physicalModel = businessColumn.getTable().getModel().getPhysicalModel();
		ModelProperty modelProperty = physicalModel.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_DATABASE_QUOTESTRING);
		if (modelProperty != null) {
			quoteString = modelProperty.getValue();
		} else {
			quoteString = "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#isIdentifier()
	 */
	@Override
	public boolean isIdentifier() {
		return businessColumn.isIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#isPKReadOnly()
	 */
	@Override
	public boolean isPKReadOnly() {
		if (jpaTable.hasCompositeKey() && businessColumn.isIdentifier())
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#isColumnInRelationship()
	 */
	@Override
	public boolean isColumnInRelationship() {
		boolean isColumnInRelationship;

		List<BusinessRelationship> relationships;

		isColumnInRelationship = false;
		relationships = jpaTable.getBusinessRelationships();

		logger.trace("The OBJECT " + jpaTable.getClassName() + " has " + relationships.size() + " Relationship");
		for (BusinessRelationship relationship : relationships) {
			logger.info("The RELATIONSHIP IS : " + relationship.getName());
			List<BusinessColumn> columns = null;
			if (relationship.getSourceTable() == null) {
				logger.trace("The relationship " + relationship.getName() + " doesn't have any source table");
				continue;
			}
			if (relationship.getDestinationTable() == null) {
				logger.trace("The relationship " + relationship.getName() + " doesn't have any destination table");
				continue;
			}

			if (jpaTable instanceof JpaViewInnerTable) {
				if (relationship.getSourceTable().equals(((JpaViewInnerTable) jpaTable).getBusinessView())) {
					columns = relationship.getSourceColumns();
				} else {
					columns = relationship.getDestinationColumns();
				}
			} else if (jpaTable instanceof JpaTable) {
				if (relationship.getSourceTable().equals(((JpaTable) jpaTable).getBusinessTable())) {
					columns = relationship.getSourceColumns();
				} else {
					columns = relationship.getDestinationColumns();
				}
			}

			if (columns != null) {
				// scann columns
				for (BusinessColumn column : columns) {
					if (column.equals(businessColumn)) {
						isColumnInRelationship = true;
						logger.debug("Column [{}] belong to a relationship", getSqlName());
					}
				}
			} else {
				logger.error("The Columns are null");
			}
		}
		// check if the column belong to a innerJoin
		if (jpaTable instanceof JpaViewInnerTable) {
			for (BusinessViewInnerJoinRelationship innerJoin : ((JpaViewInnerTable) jpaTable).getBusinessView().getJoinRelationships()) {
				List<PhysicalColumn> columns = null;
				logger.trace("The INNER RELATIONSHIP IS : " + innerJoin.getName());
				if (innerJoin == null || innerJoin.getSourceTable() == null) {
					logger.error("There is a problem , the innerRelationship doesn't have any source Table");
					continue;
				}
				if (innerJoin == null || innerJoin.getDestinationTable() == null) {
					logger.error("There is a problem , the innerRelationship doesn't have any destination Table");
					continue;
				}
				if (innerJoin.getSourceTable().equals(jpaTable.getPhysicalTable())) {
					columns = innerJoin.getSourceColumns();
				} else {
					columns = innerJoin.getDestinationColumns();
				}
				if (columns != null) {
					// scann columns
					for (PhysicalColumn column : columns) {
						if (column.getName().equals(businessColumn.getPhysicalColumn().getName())) {
							isColumnInRelationship = true;
							logger.debug("Column [{}] belong to an inner relationship", getSqlName());
						}
					}
				} else {
					logger.error("The Columns are null");
				}

			}
		}

		if (!isColumnInRelationship)
			logger.debug("Column [{}] doesn't belong to any relationship", getSqlName());

		return isColumnInRelationship;
	}

	@Override
	public boolean isColumnInRelationshipWithView() {
		boolean isColumnInRelationshipWithView = false;
		try {

			List<BusinessRelationship> relationships;
			List<BusinessColumn> columns = null;

			relationships = jpaTable.getBusinessRelationships();

			if (jpaTable instanceof JpaViewInnerTable) {
				return false;
			} else {
				for (BusinessRelationship relationship : relationships) {
					if ((relationship.getDestinationTable() instanceof BusinessView) || (relationship.getSourceTable() instanceof BusinessView)) {
						logger.debug("Found relationship with BusinessView for table: " + jpaTable.getName());
						if (relationship.getSourceTable().equals(((JpaTable) jpaTable).getBusinessTable())) {
							// outbound
							columns = relationship.getSourceColumns();
						} else {
							// inbound
							columns = relationship.getDestinationColumns();
						}
					}
				}
			}

			// scan columns
			if (columns != null) {
				for (BusinessColumn column : columns) {
					if (column.equals(businessColumn)) {
						isColumnInRelationshipWithView = true;
						logger.debug("Column [{}] belong to a business view relationship", getSqlName());
					}
				}
			} else {
				isColumnInRelationshipWithView = false;
				logger.debug("The Columns are null");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return isColumnInRelationshipWithView;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaColumn#getName()
	 */
	@Override
	public String getName() {
		return businessColumn.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaColumn#getDescription()
	 */
	@Override
	public String getDescription() {
		return businessColumn.getDescription() != null ? businessColumn.getDescription() : getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#getPropertyName()
	 */
	@Override
	public String getPropertyName() {
		String name;

		name = JavaKeywordsUtils.transformToJavaPropertyName(businessColumn.getUniqueName());

		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#getSimplePropertyType()
	 */
	@Override
	public String getSimplePropertyType() {
		String result = null;
		result = getPropertyType().substring(getPropertyType().lastIndexOf('.') + 1);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#getPropertyType()
	 */
	@Override
	public String getSqlDataType() {
		ModelProperty property = businessColumn.getProperties().get(JpaProperties.COLUMN_DATATYPE);
		return property.getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#getPropertyType()
	 */
	@Override
	public String getPropertyType() {
		String type;
		type = JDBCTypeMapper.getJavaSimpleTypeName(getSqlDataType());
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#getJpaTable()
	 */
	@Override
	public IJpaTable getJpaTable() {
		return jpaTable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#isDataTypeLOB()
	 */
	@Override
	public boolean isDataTypeLOB() {
		ModelProperty property = businessColumn.getProperties().get(JpaProperties.COLUMN_DATATYPE);
		String modelType = property.getValue();
		if (modelType.equals("BLOB") || modelType.equals("CLOB"))
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#getSqlName()
	 */
	@Override
	public String getSqlName() {
		return businessColumn.getPhysicalColumn().getName();
	}

	@Override
	public String getUnqualifiedUniqueName() {
		String uniqueName = this.getName();

		if (this.getJpaTable().hasFakePrimaryKey() || (this.isIdentifier() && this.getJpaTable().hasCompositeKey())) {
			uniqueName = this.getJpaTable().getCompositeKeyPropertyName() + "." + getPropertyName();
		} else {
			uniqueName = getPropertyName();
		}

		return uniqueName;
	}

	@Override
	public String getUniqueName() {
		String uniqueName = this.getName();

		uniqueName = this.getJpaTable().getQualifiedClassName() + "/" + getUnqualifiedUniqueName();

		return uniqueName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#getColumnNameDoubleQuoted()
	 */
	@Override
	public String getColumnNameDoubleQuoted() {
		return StringUtils.doubleQuote(businessColumn.getPhysicalColumn().getName());
	}

	@Override
	public String getQuotedMappingColumnName() {
		return StringUtils.doubleQuote(quoteString + businessColumn.getPhysicalColumn().getName() + quoteString);
		// return StringUtils.doubleQuote("`"+businessColumn.getPhysicalColumn().getName()+"`");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#needMapTemporalType()
	 */
	@Override
	public boolean needMapTemporalType() {
		if (getPropertyType().equals("java.util.Date") || getPropertyType().equals("java.util.Calendar"))
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#getMapTemporalType()
	 */
	@Override
	public String getMapTemporalType() {
		if (getPropertyType().equals("java.sql.Date"))
			return "DATE";
		if (getPropertyType().equals("java.time.Date"))
			return "TIME";
		if (getPropertyType().equals("java.sql.Timestamp"))
			return "TIMESTAMP";
		else
			return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#getPropertyNameGetter()
	 */
	@Override
	public String getPropertyNameGetter() {
		return "get" + StringUtils.initUpper(getPropertyName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#getPropertyNameSetter()
	 */
	@Override
	public String getPropertyNameSetter() {
		return "set" + StringUtils.initUpper(getPropertyName());
	}

	@Override
	public String getAttribute(String name) {
		ModelProperty property = businessColumn.getProperties().get(name);
		return property != null ? property.getValue() : "";
	}

	@Override
	public boolean isDataTypeGeometry() {
		ModelProperty property = businessColumn.getProperties().get(JpaProperties.COLUMN_DATATYPE);
		String modelType = property.getValue();
		if (modelType.equals("GEOMETRY"))
			return true;
		else
			return false;
	}

}
