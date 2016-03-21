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
import it.eng.knowage.meta.generator.utils.JavaKeywordsUtils;
import it.eng.knowage.meta.generator.utils.StringUtils;
import it.eng.spagobi.meta.model.physical.PhysicalColumn;
import it.eng.spagobi.meta.model.util.JDBCTypeMapper;

/**
 * Used to generate mapping also for columns that does not exist as business column in the real model. Useful to add behind the scenes some columns that are
 * required in the mapping for technical reasons. For example columns used in JpaViewInnerRelationship but not added by the user to the BusinessView itself.
 * 
 * @see
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class JpaFakeColumn implements IJpaColumn {

	IJpaTable jpaTable;
	PhysicalColumn physicalColumn;
	String quoteString;

	public JpaFakeColumn(IJpaTable jpaTable, PhysicalColumn physicalColumn) {
		this.jpaTable = jpaTable;
		this.physicalColumn = physicalColumn;
		this.quoteString = physicalColumn.getTable().getModel().getPropertyType("connection.databasequotestring").getDefaultValue();
	}

	@Override
	public IJpaTable getJpaTable() {
		return jpaTable;
	}

	@Override
	public String getName() {
		return physicalColumn.getName().replace(" ", "_");
	}

	@Override
	public String getDescription() {
		return getName();
	}

	@Override
	public String getSqlName() {
		return physicalColumn.getName();
	}

	@Override
	public String getUniqueName() {
		String uniqueName = getJpaTable().getQualifiedClassName();
		uniqueName += "/";
		uniqueName += getUnqualifiedUniqueName();
		return uniqueName;

	}

	@Override
	public String getColumnNameDoubleQuoted() {
		return StringUtils.doubleQuote(physicalColumn.getName());
	}

	@Override
	public boolean isIdentifier() {
		return true;
	}

	@Override
	public boolean isPKReadOnly() {
		if (jpaTable.hasCompositeKey() && isIdentifier())
			return true;
		else
			return false;
	}

	@Override
	public boolean isColumnInRelationship() {
		return false;
	}

	@Override
	public String getPropertyName() {
		String name;

		name = JavaKeywordsUtils.transformToJavaPropertyName(getName());

		return name;
	}

	@Override
	public String getSimplePropertyType() {
		String result = null;
		result = getPropertyType().substring(getPropertyType().lastIndexOf('.') + 1);
		return result;
	}

	@Override
	public String getPropertyType() {
		String type;
		type = JDBCTypeMapper.getJavaSimpleTypeName(getSqlDataType());
		return type;
	}

	@Override
	public String getSqlDataType() {
		return physicalColumn.getDataType();
	}

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
		if ("structural.visible".equals(name)) {
			return "false";
		} else if ("structural.columntype".equals(name)) {
			return "attribute";
		} else {
			return "";
		}
	}

	@Override
	public boolean isDataTypeLOB() {
		String modelType = physicalColumn.getDataType();
		if (modelType.equals("BLOB") || modelType.equals("CLOB"))
			return true;
		else
			return false;
	}

	@Override
	public boolean needMapTemporalType() {
		if (getPropertyType().equals("java.util.Date") || getPropertyType().equals("java.util.Calendar"))
			return true;
		else
			return false;
	}

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
	public String getQuotedMappingColumnName() {
		return StringUtils.doubleQuote(quoteString + physicalColumn.getName() + quoteString);
		// return StringUtils.doubleQuote("`"+businessColumn.getPhysicalColumn().getName()+"`");
	}

	@Override
	public boolean isColumnInRelationshipWithView() {
		return false;
	}

	@Override
	public boolean isDataTypeGeometry() {
		String modelType = physicalColumn.getDataType();
		if (modelType.equals("BLOB") || modelType.equals("CLOB"))
			return true;
		else
			return false;
	}

}
