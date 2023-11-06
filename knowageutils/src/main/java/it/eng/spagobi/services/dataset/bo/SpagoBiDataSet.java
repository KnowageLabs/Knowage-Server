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

package it.eng.spagobi.services.dataset.bo;

public class SpagoBiDataSet implements java.io.Serializable {
	private boolean _public;

	private boolean active;

	private Integer categoryId;

	private String configuration;

	private it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSource;

	private it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSourceForReading;

	private Object[] dependentDataSets;

	private String description;

	private int dsId;

	private String dsMetadata;

	private int federationId;

	private String federationRelations;

	private String federationlabel;

	private String flatTableName;

	private String label;

	private String name;

	private boolean numRows;

	private String organization;

	private String owner;

	private String parameters;

	private String persistTableName;

	private boolean persisted;

	private boolean persistedHDFS;

	private String pivotColumnName;

	private String pivotColumnValue;

	private String pivotRowName;

	private boolean scheduled;

	private String scopeCd;

	private Integer scopeId;

	private Integer transformerId;

	private String type;

	private int versionNum;

	private String startDateField;

	private String endDateField;

	private String schedulingCronLine;

	private boolean degenerated;

	public SpagoBiDataSet() {
	}

	public SpagoBiDataSet(boolean _public, boolean active, Integer categoryId, String configuration,
			it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSource,
			it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSourceForReading, Object[] dependentDataSets,
			String description, int dsId, String dsMetadata, int federationId, String federationRelations,
			String federationlabel, String flatTableName, String label, String name, boolean numRows,
			String organization, String owner, String parameters, String persistTableName, boolean persisted,
			String pivotColumnName, String pivotColumnValue, String pivotRowName, boolean scheduled, String scopeCd,
			Integer scopeId, Integer transformerId, String type, int versionNum, String startDateField,
			String endDateField, String schedulingCronLine, boolean degenerated) {
		this._public = _public;
		this.active = active;
		this.categoryId = categoryId;
		this.configuration = configuration;
		this.dataSource = dataSource;
		this.dataSourceForReading = dataSourceForReading;
		this.dependentDataSets = dependentDataSets;
		this.description = description;
		this.dsId = dsId;
		this.dsMetadata = dsMetadata;
		this.federationId = federationId;
		this.federationRelations = federationRelations;
		this.federationlabel = federationlabel;
		this.flatTableName = flatTableName;
		this.label = label;
		this.name = name;
		this.numRows = numRows;
		this.organization = organization;
		this.owner = owner;
		this.parameters = parameters;
		this.persistTableName = persistTableName;
		this.persisted = persisted;
		this.pivotColumnName = pivotColumnName;
		this.pivotColumnValue = pivotColumnValue;
		this.pivotRowName = pivotRowName;
		this.scheduled = scheduled;
		this.scopeCd = scopeCd;
		this.scopeId = scopeId;
		this.transformerId = transformerId;
		this.type = type;
		this.versionNum = versionNum;
		this.startDateField = startDateField;
		this.endDateField = endDateField;
		this.schedulingCronLine = schedulingCronLine;
		this.degenerated = degenerated;
	}

	/**
	 * Gets the _public value for this SpagoBiDataSet.
	 *
	 * @return _public
	 */
	public boolean is_public() {
		return _public;
	}

	/**
	 * Sets the _public value for this SpagoBiDataSet.
	 *
	 * @param _public
	 */
	public void set_public(boolean _public) {
		this._public = _public;
	}

	/**
	 * Gets the active value for this SpagoBiDataSet.
	 *
	 * @return active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets the active value for this SpagoBiDataSet.
	 *
	 * @param active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Gets the categoryId value for this SpagoBiDataSet.
	 *
	 * @return categoryId
	 */
	public Integer getCategoryId() {
		return categoryId;
	}

	/**
	 * Sets the categoryId value for this SpagoBiDataSet.
	 *
	 * @param categoryId
	 */
	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * Gets the configuration value for this SpagoBiDataSet.
	 *
	 * @return configuration
	 */
	public String getConfiguration() {
		return configuration;
	}

	/**
	 * Sets the configuration value for this SpagoBiDataSet.
	 *
	 * @param configuration
	 */
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	/**
	 * Gets the dataSource value for this SpagoBiDataSet.
	 *
	 * @return dataSource
	 */
	public it.eng.spagobi.services.datasource.bo.SpagoBiDataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Sets the dataSource value for this SpagoBiDataSet.
	 *
	 * @param dataSource
	 */
	public void setDataSource(it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Gets the dataSourceForReading value for this SpagoBiDataSet.
	 *
	 * @return dataSourceForReading
	 */
	public it.eng.spagobi.services.datasource.bo.SpagoBiDataSource getDataSourceForReading() {
		return dataSourceForReading;
	}

	/**
	 * Sets the dataSourceForReading value for this SpagoBiDataSet.
	 *
	 * @param dataSourceForReading
	 */
	public void setDataSourceForReading(it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSourceForReading) {
		this.dataSourceForReading = dataSourceForReading;
	}

	/**
	 * Gets the dependentDataSets value for this SpagoBiDataSet.
	 *
	 * @return dependentDataSets
	 */
	public Object[] getDependentDataSets() {
		return dependentDataSets;
	}

	/**
	 * Sets the dependentDataSets value for this SpagoBiDataSet.
	 *
	 * @param dependentDataSets
	 */
	public void setDependentDataSets(Object[] dependentDataSets) {
		this.dependentDataSets = dependentDataSets;
	}

	/**
	 * Gets the description value for this SpagoBiDataSet.
	 *
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description value for this SpagoBiDataSet.
	 *
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the dsId value for this SpagoBiDataSet.
	 *
	 * @return dsId
	 */
	public int getDsId() {
		return dsId;
	}

	/**
	 * Sets the dsId value for this SpagoBiDataSet.
	 *
	 * @param dsId
	 */
	public void setDsId(int dsId) {
		this.dsId = dsId;
	}

	/**
	 * Gets the dsMetadata value for this SpagoBiDataSet.
	 *
	 * @return dsMetadata
	 */
	public String getDsMetadata() {
		return dsMetadata;
	}

	/**
	 * Sets the dsMetadata value for this SpagoBiDataSet.
	 *
	 * @param dsMetadata
	 */
	public void setDsMetadata(String dsMetadata) {
		this.dsMetadata = dsMetadata;
	}

	/**
	 * Gets the federationId value for this SpagoBiDataSet.
	 *
	 * @return federationId
	 */
	public int getFederationId() {
		return federationId;
	}

	/**
	 * Sets the federationId value for this SpagoBiDataSet.
	 *
	 * @param federationId
	 */
	public void setFederationId(int federationId) {
		this.federationId = federationId;
	}

	/**
	 * Gets the federationRelations value for this SpagoBiDataSet.
	 *
	 * @return federationRelations
	 */
	public String getFederationRelations() {
		return federationRelations;
	}

	/**
	 * Sets the federationRelations value for this SpagoBiDataSet.
	 *
	 * @param federationRelations
	 */
	public void setFederationRelations(String federationRelations) {
		this.federationRelations = federationRelations;
	}

	/**
	 * Gets the federationlabel value for this SpagoBiDataSet.
	 *
	 * @return federationlabel
	 */
	public String getFederationlabel() {
		return federationlabel;
	}

	/**
	 * Sets the federationlabel value for this SpagoBiDataSet.
	 *
	 * @param federationlabel
	 */
	public void setFederationlabel(String federationlabel) {
		this.federationlabel = federationlabel;
	}

	/**
	 * Gets the flatTableName value for this SpagoBiDataSet.
	 *
	 * @return flatTableName
	 */
	public String getFlatTableName() {
		return flatTableName;
	}

	/**
	 * Sets the flatTableName value for this SpagoBiDataSet.
	 *
	 * @param flatTableName
	 */
	public void setFlatTableName(String flatTableName) {
		this.flatTableName = flatTableName;
	}

	/**
	 * Gets the label value for this SpagoBiDataSet.
	 *
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label value for this SpagoBiDataSet.
	 *
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the name value for this SpagoBiDataSet.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name value for this SpagoBiDataSet.
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the numRows value for this SpagoBiDataSet.
	 *
	 * @return numRows
	 */
	public boolean isNumRows() {
		return numRows;
	}

	/**
	 * Sets the numRows value for this SpagoBiDataSet.
	 *
	 * @param numRows
	 */
	public void setNumRows(boolean numRows) {
		this.numRows = numRows;
	}

	/**
	 * Gets the organization value for this SpagoBiDataSet.
	 *
	 * @return organization
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * Sets the organization value for this SpagoBiDataSet.
	 *
	 * @param organization
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * Gets the owner value for this SpagoBiDataSet.
	 *
	 * @return owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Sets the owner value for this SpagoBiDataSet.
	 *
	 * @param owner
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * Gets the parameters value for this SpagoBiDataSet.
	 *
	 * @return parameters
	 */
	public String getParameters() {
		return parameters;
	}

	/**
	 * Sets the parameters value for this SpagoBiDataSet.
	 *
	 * @param parameters
	 */
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	/**
	 * Gets the persistTableName value for this SpagoBiDataSet.
	 *
	 * @return persistTableName
	 */
	public String getPersistTableName() {
		return persistTableName;
	}

	/**
	 * Sets the persistTableName value for this SpagoBiDataSet.
	 *
	 * @param persistTableName
	 */
	public void setPersistTableName(String persistTableName) {
		this.persistTableName = persistTableName;
	}

	/**
	 * Gets the persisted value for this SpagoBiDataSet.
	 *
	 * @return persisted
	 */
	public boolean isPersisted() {
		return persisted;
	}

	/**
	 * Sets the persisted value for this SpagoBiDataSet.
	 *
	 * @param persisted
	 */
	public void setPersisted(boolean persisted) {
		this.persisted = persisted;
	}

	/**
	 * Gets the pivotColumnName value for this SpagoBiDataSet.
	 *
	 * @return pivotColumnName
	 */
	public String getPivotColumnName() {
		return pivotColumnName;
	}

	/**
	 * Sets the pivotColumnName value for this SpagoBiDataSet.
	 *
	 * @param pivotColumnName
	 */
	public void setPivotColumnName(String pivotColumnName) {
		this.pivotColumnName = pivotColumnName;
	}

	/**
	 * Gets the pivotColumnValue value for this SpagoBiDataSet.
	 *
	 * @return pivotColumnValue
	 */
	public String getPivotColumnValue() {
		return pivotColumnValue;
	}

	/**
	 * Sets the pivotColumnValue value for this SpagoBiDataSet.
	 *
	 * @param pivotColumnValue
	 */
	public void setPivotColumnValue(String pivotColumnValue) {
		this.pivotColumnValue = pivotColumnValue;
	}

	/**
	 * Gets the pivotRowName value for this SpagoBiDataSet.
	 *
	 * @return pivotRowName
	 */
	public String getPivotRowName() {
		return pivotRowName;
	}

	/**
	 * Sets the pivotRowName value for this SpagoBiDataSet.
	 *
	 * @param pivotRowName
	 */
	public void setPivotRowName(String pivotRowName) {
		this.pivotRowName = pivotRowName;
	}

	/**
	 * Gets the scheduled value for this SpagoBiDataSet.
	 *
	 * @return scheduled
	 */
	public boolean isScheduled() {
		return scheduled;
	}

	/**
	 * Sets the scheduled value for this SpagoBiDataSet.
	 *
	 * @param scheduled
	 */
	public void setScheduled(boolean scheduled) {
		this.scheduled = scheduled;
	}

	/**
	 * Gets the scopeCd value for this SpagoBiDataSet.
	 *
	 * @return scopeCd
	 */
	public String getScopeCd() {
		return scopeCd;
	}

	/**
	 * Sets the scopeCd value for this SpagoBiDataSet.
	 *
	 * @param scopeCd
	 */
	public void setScopeCd(String scopeCd) {
		this.scopeCd = scopeCd;
	}

	/**
	 * Gets the scopeId value for this SpagoBiDataSet.
	 *
	 * @return scopeId
	 */
	public Integer getScopeId() {
		return scopeId;
	}

	/**
	 * Sets the scopeId value for this SpagoBiDataSet.
	 *
	 * @param scopeId
	 */
	public void setScopeId(Integer scopeId) {
		this.scopeId = scopeId;
	}

	/**
	 * Gets the transformerId value for this SpagoBiDataSet.
	 *
	 * @return transformerId
	 */
	public Integer getTransformerId() {
		return transformerId;
	}

	/**
	 * Sets the transformerId value for this SpagoBiDataSet.
	 *
	 * @param transformerId
	 */
	public void setTransformerId(Integer transformerId) {
		this.transformerId = transformerId;
	}

	/**
	 * Gets the type value for this SpagoBiDataSet.
	 *
	 * @return type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type value for this SpagoBiDataSet.
	 *
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the versionNum value for this SpagoBiDataSet.
	 *
	 * @return versionNum
	 */
	public int getVersionNum() {
		return versionNum;
	}

	/**
	 * Sets the versionNum value for this SpagoBiDataSet.
	 *
	 * @param versionNum
	 */
	public void setVersionNum(int versionNum) {
		this.versionNum = versionNum;
	}

	/**
	 * Gets the startDateField value for this SpagoBiDataSet.
	 *
	 * @return startDateField
	 */
	public String getStartDateField() {
		return startDateField;
	}

	/**
	 * Sets the startDateField value for this SpagoBiDataSet.
	 *
	 * @param startDateField
	 */
	public void setStartDateField(String startDateField) {
		this.startDateField = startDateField;
	}

	/**
	 * Gets the endDateField value for this SpagoBiDataSet.
	 *
	 * @return endDateField
	 */
	public String getEndDateField() {
		return endDateField;
	}

	/**
	 * Sets the endDateField value for this SpagoBiDataSet.
	 *
	 * @param endDateField
	 */
	public void setEndDateField(String endDateField) {
		this.endDateField = endDateField;
	}

	/**
	 * Gets the schedulingCronLine value for this SpagoBiDataSet.
	 *
	 * @return schedulingCronLine
	 */
	public String getSchedulingCronLine() {
		return schedulingCronLine;
	}

	/**
	 * Sets the schedulingCronLine value for this SpagoBiDataSet.
	 *
	 * @param schedulingCronLine
	 */
	public void setSchedulingCronLine(String schedulingCronLine) {
		this.schedulingCronLine = schedulingCronLine;
	}

	/**
	 * Gets the degenerated value for this SpagoBiDataSet.
	 *
	 * @return degenerated
	 */
	public boolean isDegenerated() {
		return degenerated;
	}

	/**
	 * Sets the degenerated value for this SpagoBiDataSet.
	 *
	 * @param degenerated
	 */
	public void setDegenerated(boolean degenerated) {
		this.degenerated = degenerated;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SpagoBiDataSet))
			return false;
		SpagoBiDataSet other = (SpagoBiDataSet) obj;
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true && this._public == other.is_public() && this.active == other.isActive()
				&& ((this.categoryId == null && other.getCategoryId() == null)
						|| (this.categoryId != null && this.categoryId.equals(other.getCategoryId())))
				&& ((this.configuration == null && other.getConfiguration() == null)
						|| (this.configuration != null && this.configuration.equals(other.getConfiguration())))
				&& ((this.dataSource == null && other.getDataSource() == null)
						|| (this.dataSource != null && this.dataSource.equals(other.getDataSource())))
				&& ((this.dataSourceForReading == null && other.getDataSourceForReading() == null)
						|| (this.dataSourceForReading != null
								&& this.dataSourceForReading.equals(other.getDataSourceForReading())))
				&& ((this.dependentDataSets == null && other.getDependentDataSets() == null)
						|| (this.dependentDataSets != null
								&& java.util.Arrays.equals(this.dependentDataSets, other.getDependentDataSets())))
				&& ((this.description == null && other.getDescription() == null)
						|| (this.description != null && this.description.equals(other.getDescription())))
				&& this.dsId == other.getDsId()
				&& ((this.dsMetadata == null && other.getDsMetadata() == null)
						|| (this.dsMetadata != null && this.dsMetadata.equals(other.getDsMetadata())))
				&& this.federationId == other.getFederationId()
				&& ((this.federationRelations == null && other.getFederationRelations() == null)
						|| (this.federationRelations != null
								&& this.federationRelations.equals(other.getFederationRelations())))
				&& ((this.federationlabel == null && other.getFederationlabel() == null)
						|| (this.federationlabel != null && this.federationlabel.equals(other.getFederationlabel())))
				&& ((this.flatTableName == null && other.getFlatTableName() == null)
						|| (this.flatTableName != null && this.flatTableName.equals(other.getFlatTableName())))
				&& ((this.label == null && other.getLabel() == null)
						|| (this.label != null && this.label.equals(other.getLabel())))
				&& ((this.name == null && other.getName() == null)
						|| (this.name != null && this.name.equals(other.getName())))
				&& this.numRows == other.isNumRows()
				&& ((this.organization == null && other.getOrganization() == null)
						|| (this.organization != null && this.organization.equals(other.getOrganization())))
				&& ((this.owner == null && other.getOwner() == null)
						|| (this.owner != null && this.owner.equals(other.getOwner())))
				&& ((this.parameters == null && other.getParameters() == null)
						|| (this.parameters != null && this.parameters.equals(other.getParameters())))
				&& ((this.persistTableName == null && other.getPersistTableName() == null)
						|| (this.persistTableName != null && this.persistTableName.equals(other.getPersistTableName())))
				&& this.persisted == other.isPersisted()
				&& ((this.pivotColumnName == null && other.getPivotColumnName() == null)
						|| (this.pivotColumnName != null && this.pivotColumnName.equals(other.getPivotColumnName())))
				&& ((this.pivotColumnValue == null && other.getPivotColumnValue() == null)
						|| (this.pivotColumnValue != null && this.pivotColumnValue.equals(other.getPivotColumnValue())))
				&& ((this.pivotRowName == null && other.getPivotRowName() == null)
						|| (this.pivotRowName != null && this.pivotRowName.equals(other.getPivotRowName())))
				&& this.scheduled == other.isScheduled()
				&& ((this.scopeCd == null && other.getScopeCd() == null)
						|| (this.scopeCd != null && this.scopeCd.equals(other.getScopeCd())))
				&& ((this.scopeId == null && other.getScopeId() == null)
						|| (this.scopeId != null && this.scopeId.equals(other.getScopeId())))
				&& ((this.transformerId == null && other.getTransformerId() == null)
						|| (this.transformerId != null && this.transformerId.equals(other.getTransformerId())))
				&& ((this.type == null && other.getType() == null)
						|| (this.type != null && this.type.equals(other.getType())))
				&& this.versionNum == other.getVersionNum()
				&& ((this.startDateField == null && other.getStartDateField() == null)
						|| (this.startDateField != null && this.startDateField.equals(other.getStartDateField())))
				&& ((this.endDateField == null && other.getEndDateField() == null)
						|| (this.endDateField != null && this.endDateField.equals(other.getEndDateField())))
				&& ((this.schedulingCronLine == null && other.getSchedulingCronLine() == null)
						|| (this.schedulingCronLine != null
								&& this.schedulingCronLine.equals(other.getSchedulingCronLine())))
				&& this.degenerated == other.isDegenerated();
		__equalsCalc = null;
		return _equals;
	}

	private boolean __hashCodeCalc = false;

	@Override
	public synchronized int hashCode() {
		if (__hashCodeCalc) {
			return 0;
		}
		__hashCodeCalc = true;
		int _hashCode = 1;
		_hashCode += (is_public() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		_hashCode += (isActive() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		if (getCategoryId() != null) {
			_hashCode += getCategoryId().hashCode();
		}
		if (getConfiguration() != null) {
			_hashCode += getConfiguration().hashCode();
		}
		if (getDataSource() != null) {
			_hashCode += getDataSource().hashCode();
		}
		if (getDataSourceForReading() != null) {
			_hashCode += getDataSourceForReading().hashCode();
		}
		if (getDependentDataSets() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getDependentDataSets()); i++) {
				Object obj = java.lang.reflect.Array.get(getDependentDataSets(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getDescription() != null) {
			_hashCode += getDescription().hashCode();
		}
		_hashCode += getDsId();
		if (getDsMetadata() != null) {
			_hashCode += getDsMetadata().hashCode();
		}
		_hashCode += getFederationId();
		if (getFederationRelations() != null) {
			_hashCode += getFederationRelations().hashCode();
		}
		if (getFederationlabel() != null) {
			_hashCode += getFederationlabel().hashCode();
		}
		if (getFlatTableName() != null) {
			_hashCode += getFlatTableName().hashCode();
		}
		if (getLabel() != null) {
			_hashCode += getLabel().hashCode();
		}
		if (getName() != null) {
			_hashCode += getName().hashCode();
		}
		_hashCode += (isNumRows() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		if (getOrganization() != null) {
			_hashCode += getOrganization().hashCode();
		}
		if (getOwner() != null) {
			_hashCode += getOwner().hashCode();
		}
		if (getParameters() != null) {
			_hashCode += getParameters().hashCode();
		}
		if (getPersistTableName() != null) {
			_hashCode += getPersistTableName().hashCode();
		}
		_hashCode += (isPersisted() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		if (getPivotColumnName() != null) {
			_hashCode += getPivotColumnName().hashCode();
		}
		if (getPivotColumnValue() != null) {
			_hashCode += getPivotColumnValue().hashCode();
		}
		if (getPivotRowName() != null) {
			_hashCode += getPivotRowName().hashCode();
		}
		_hashCode += (isScheduled() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		if (getScopeCd() != null) {
			_hashCode += getScopeCd().hashCode();
		}
		if (getScopeId() != null) {
			_hashCode += getScopeId().hashCode();
		}
		if (getTransformerId() != null) {
			_hashCode += getTransformerId().hashCode();
		}
		if (getType() != null) {
			_hashCode += getType().hashCode();
		}
		_hashCode += getVersionNum();
		if (getStartDateField() != null) {
			_hashCode += getStartDateField().hashCode();
		}
		if (getEndDateField() != null) {
			_hashCode += getEndDateField().hashCode();
		}
		if (getSchedulingCronLine() != null) {
			_hashCode += getSchedulingCronLine().hashCode();
		}
		_hashCode += (isDegenerated() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		__hashCodeCalc = false;
		return _hashCode;
	}

	public boolean isPersistedHDFS() {
		return persistedHDFS;
	}

	public void setPersistedHDFS(boolean persistedHDFS) {
		this.persistedHDFS = persistedHDFS;
	}

}
