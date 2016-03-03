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

	private java.lang.Integer categoryId;

	private java.lang.String configuration;

	private it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSource;

	private it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSourceForReading;

	private java.lang.Object[] dependentDataSets;

	private java.lang.String description;

	private int dsId;

	private java.lang.String dsMetadata;

	private int federationId;

	private java.lang.String federationRelations;

	private java.lang.String federationlabel;

	private java.lang.String flatTableName;

	private java.lang.String label;

	private java.lang.String name;

	private boolean numRows;

	private java.lang.String organization;

	private java.lang.String owner;

	private java.lang.String parameters;

	private java.lang.String persistTableName;

	private boolean persisted;

	private java.lang.String pivotColumnName;

	private java.lang.String pivotColumnValue;

	private java.lang.String pivotRowName;

	private boolean scheduled;

	private java.lang.String scopeCd;

	private java.lang.Integer scopeId;

	private java.lang.Integer transformerId;

	private java.lang.String type;

	private int versionNum;

	private java.lang.String startDateField;

	private java.lang.String endDateField;

	private java.lang.String schedulingCronLine;

	public SpagoBiDataSet() {
	}

	public SpagoBiDataSet(boolean _public, boolean active, java.lang.Integer categoryId, java.lang.String configuration,
			it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSource, it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSourceForReading,
			java.lang.Object[] dependentDataSets, java.lang.String description, int dsId, java.lang.String dsMetadata, int federationId,
			java.lang.String federationRelations, java.lang.String federationlabel, java.lang.String flatTableName, java.lang.String label,
			java.lang.String name, boolean numRows, java.lang.String organization, java.lang.String owner, java.lang.String parameters,
			java.lang.String persistTableName, boolean persisted, java.lang.String pivotColumnName, java.lang.String pivotColumnValue,
			java.lang.String pivotRowName, boolean scheduled, java.lang.String scopeCd, java.lang.Integer scopeId, java.lang.Integer transformerId,
			java.lang.String type, int versionNum, java.lang.String startDateField, java.lang.String endDateField, java.lang.String schedulingCronLine) {
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
	public java.lang.Integer getCategoryId() {
		return categoryId;
	}

	/**
	 * Sets the categoryId value for this SpagoBiDataSet.
	 * 
	 * @param categoryId
	 */
	public void setCategoryId(java.lang.Integer categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * Gets the configuration value for this SpagoBiDataSet.
	 * 
	 * @return configuration
	 */
	public java.lang.String getConfiguration() {
		return configuration;
	}

	/**
	 * Sets the configuration value for this SpagoBiDataSet.
	 * 
	 * @param configuration
	 */
	public void setConfiguration(java.lang.String configuration) {
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
	public java.lang.Object[] getDependentDataSets() {
		return dependentDataSets;
	}

	/**
	 * Sets the dependentDataSets value for this SpagoBiDataSet.
	 * 
	 * @param dependentDataSets
	 */
	public void setDependentDataSets(java.lang.Object[] dependentDataSets) {
		this.dependentDataSets = dependentDataSets;
	}

	/**
	 * Gets the description value for this SpagoBiDataSet.
	 * 
	 * @return description
	 */
	public java.lang.String getDescription() {
		return description;
	}

	/**
	 * Sets the description value for this SpagoBiDataSet.
	 * 
	 * @param description
	 */
	public void setDescription(java.lang.String description) {
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
	public java.lang.String getDsMetadata() {
		return dsMetadata;
	}

	/**
	 * Sets the dsMetadata value for this SpagoBiDataSet.
	 * 
	 * @param dsMetadata
	 */
	public void setDsMetadata(java.lang.String dsMetadata) {
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
	public java.lang.String getFederationRelations() {
		return federationRelations;
	}

	/**
	 * Sets the federationRelations value for this SpagoBiDataSet.
	 * 
	 * @param federationRelations
	 */
	public void setFederationRelations(java.lang.String federationRelations) {
		this.federationRelations = federationRelations;
	}

	/**
	 * Gets the federationlabel value for this SpagoBiDataSet.
	 * 
	 * @return federationlabel
	 */
	public java.lang.String getFederationlabel() {
		return federationlabel;
	}

	/**
	 * Sets the federationlabel value for this SpagoBiDataSet.
	 * 
	 * @param federationlabel
	 */
	public void setFederationlabel(java.lang.String federationlabel) {
		this.federationlabel = federationlabel;
	}

	/**
	 * Gets the flatTableName value for this SpagoBiDataSet.
	 * 
	 * @return flatTableName
	 */
	public java.lang.String getFlatTableName() {
		return flatTableName;
	}

	/**
	 * Sets the flatTableName value for this SpagoBiDataSet.
	 * 
	 * @param flatTableName
	 */
	public void setFlatTableName(java.lang.String flatTableName) {
		this.flatTableName = flatTableName;
	}

	/**
	 * Gets the label value for this SpagoBiDataSet.
	 * 
	 * @return label
	 */
	public java.lang.String getLabel() {
		return label;
	}

	/**
	 * Sets the label value for this SpagoBiDataSet.
	 * 
	 * @param label
	 */
	public void setLabel(java.lang.String label) {
		this.label = label;
	}

	/**
	 * Gets the name value for this SpagoBiDataSet.
	 * 
	 * @return name
	 */
	public java.lang.String getName() {
		return name;
	}

	/**
	 * Sets the name value for this SpagoBiDataSet.
	 * 
	 * @param name
	 */
	public void setName(java.lang.String name) {
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
	public java.lang.String getOrganization() {
		return organization;
	}

	/**
	 * Sets the organization value for this SpagoBiDataSet.
	 * 
	 * @param organization
	 */
	public void setOrganization(java.lang.String organization) {
		this.organization = organization;
	}

	/**
	 * Gets the owner value for this SpagoBiDataSet.
	 * 
	 * @return owner
	 */
	public java.lang.String getOwner() {
		return owner;
	}

	/**
	 * Sets the owner value for this SpagoBiDataSet.
	 * 
	 * @param owner
	 */
	public void setOwner(java.lang.String owner) {
		this.owner = owner;
	}

	/**
	 * Gets the parameters value for this SpagoBiDataSet.
	 * 
	 * @return parameters
	 */
	public java.lang.String getParameters() {
		return parameters;
	}

	/**
	 * Sets the parameters value for this SpagoBiDataSet.
	 * 
	 * @param parameters
	 */
	public void setParameters(java.lang.String parameters) {
		this.parameters = parameters;
	}

	/**
	 * Gets the persistTableName value for this SpagoBiDataSet.
	 * 
	 * @return persistTableName
	 */
	public java.lang.String getPersistTableName() {
		return persistTableName;
	}

	/**
	 * Sets the persistTableName value for this SpagoBiDataSet.
	 * 
	 * @param persistTableName
	 */
	public void setPersistTableName(java.lang.String persistTableName) {
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
	public java.lang.String getPivotColumnName() {
		return pivotColumnName;
	}

	/**
	 * Sets the pivotColumnName value for this SpagoBiDataSet.
	 * 
	 * @param pivotColumnName
	 */
	public void setPivotColumnName(java.lang.String pivotColumnName) {
		this.pivotColumnName = pivotColumnName;
	}

	/**
	 * Gets the pivotColumnValue value for this SpagoBiDataSet.
	 * 
	 * @return pivotColumnValue
	 */
	public java.lang.String getPivotColumnValue() {
		return pivotColumnValue;
	}

	/**
	 * Sets the pivotColumnValue value for this SpagoBiDataSet.
	 * 
	 * @param pivotColumnValue
	 */
	public void setPivotColumnValue(java.lang.String pivotColumnValue) {
		this.pivotColumnValue = pivotColumnValue;
	}

	/**
	 * Gets the pivotRowName value for this SpagoBiDataSet.
	 * 
	 * @return pivotRowName
	 */
	public java.lang.String getPivotRowName() {
		return pivotRowName;
	}

	/**
	 * Sets the pivotRowName value for this SpagoBiDataSet.
	 * 
	 * @param pivotRowName
	 */
	public void setPivotRowName(java.lang.String pivotRowName) {
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
	public java.lang.String getScopeCd() {
		return scopeCd;
	}

	/**
	 * Sets the scopeCd value for this SpagoBiDataSet.
	 * 
	 * @param scopeCd
	 */
	public void setScopeCd(java.lang.String scopeCd) {
		this.scopeCd = scopeCd;
	}

	/**
	 * Gets the scopeId value for this SpagoBiDataSet.
	 * 
	 * @return scopeId
	 */
	public java.lang.Integer getScopeId() {
		return scopeId;
	}

	/**
	 * Sets the scopeId value for this SpagoBiDataSet.
	 * 
	 * @param scopeId
	 */
	public void setScopeId(java.lang.Integer scopeId) {
		this.scopeId = scopeId;
	}

	/**
	 * Gets the transformerId value for this SpagoBiDataSet.
	 * 
	 * @return transformerId
	 */
	public java.lang.Integer getTransformerId() {
		return transformerId;
	}

	/**
	 * Sets the transformerId value for this SpagoBiDataSet.
	 * 
	 * @param transformerId
	 */
	public void setTransformerId(java.lang.Integer transformerId) {
		this.transformerId = transformerId;
	}

	/**
	 * Gets the type value for this SpagoBiDataSet.
	 * 
	 * @return type
	 */
	public java.lang.String getType() {
		return type;
	}

	/**
	 * Sets the type value for this SpagoBiDataSet.
	 * 
	 * @param type
	 */
	public void setType(java.lang.String type) {
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

	public java.lang.String getStartDateField() {
		return startDateField;
	}

	public void setStartDateField(java.lang.String startDateField) {
		this.startDateField = startDateField;
	}

	public java.lang.String getEndDateField() {
		return endDateField;
	}

	public void setEndDateField(java.lang.String endDateField) {
		this.endDateField = endDateField;
	}

	public java.lang.String getSchedulingCronLine() {
		return schedulingCronLine;
	}

	public void setSchedulingCronLine(java.lang.String schedulingCronLine) {
		this.schedulingCronLine = schedulingCronLine;
	}

	private java.lang.Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(java.lang.Object obj) {
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
		_equals = true
				&& this._public == other.is_public()
				&& this.active == other.isActive()
				&& ((this.categoryId == null && other.getCategoryId() == null) || (this.categoryId != null && this.categoryId.equals(other.getCategoryId())))
				&& ((this.configuration == null && other.getConfiguration() == null) || (this.configuration != null && this.configuration.equals(other
						.getConfiguration())))
				&& ((this.dataSource == null && other.getDataSource() == null) || (this.dataSource != null && this.dataSource.equals(other.getDataSource())))
				&& ((this.dataSourceForReading == null && other.getDataSourceForReading() == null) || (this.dataSourceForReading != null && this.dataSourceForReading
						.equals(other.getDataSourceForReading())))
				&& ((this.dependentDataSets == null && other.getDependentDataSets() == null) || (this.dependentDataSets != null && java.util.Arrays.equals(
						this.dependentDataSets, other.getDependentDataSets())))
				&& ((this.description == null && other.getDescription() == null) || (this.description != null && this.description
						.equals(other.getDescription())))
				&& this.dsId == other.getDsId()
				&& ((this.dsMetadata == null && other.getDsMetadata() == null) || (this.dsMetadata != null && this.dsMetadata.equals(other.getDsMetadata())))
				&& this.federationId == other.getFederationId()
				&& ((this.federationRelations == null && other.getFederationRelations() == null) || (this.federationRelations != null && this.federationRelations
						.equals(other.getFederationRelations())))
				&& ((this.federationlabel == null && other.getFederationlabel() == null) || (this.federationlabel != null && this.federationlabel.equals(other
						.getFederationlabel())))
				&& ((this.flatTableName == null && other.getFlatTableName() == null) || (this.flatTableName != null && this.flatTableName.equals(other
						.getFlatTableName())))
				&& ((this.label == null && other.getLabel() == null) || (this.label != null && this.label.equals(other.getLabel())))
				&& ((this.name == null && other.getName() == null) || (this.name != null && this.name.equals(other.getName())))
				&& this.numRows == other.isNumRows()
				&& ((this.organization == null && other.getOrganization() == null) || (this.organization != null && this.organization.equals(other
						.getOrganization())))
				&& ((this.owner == null && other.getOwner() == null) || (this.owner != null && this.owner.equals(other.getOwner())))
				&& ((this.parameters == null && other.getParameters() == null) || (this.parameters != null && this.parameters.equals(other.getParameters())))
				&& ((this.persistTableName == null && other.getPersistTableName() == null) || (this.persistTableName != null && this.persistTableName
						.equals(other.getPersistTableName())))
				&& this.persisted == other.isPersisted()
				&& ((this.pivotColumnName == null && other.getPivotColumnName() == null) || (this.pivotColumnName != null && this.pivotColumnName.equals(other
						.getPivotColumnName())))
				&& ((this.pivotColumnValue == null && other.getPivotColumnValue() == null) || (this.pivotColumnValue != null && this.pivotColumnValue
						.equals(other.getPivotColumnValue())))
				&& ((this.pivotRowName == null && other.getPivotRowName() == null) || (this.pivotRowName != null && this.pivotRowName.equals(other
						.getPivotRowName())))
				&& this.scheduled == other.isScheduled()
				&& ((this.scopeCd == null && other.getScopeCd() == null) || (this.scopeCd != null && this.scopeCd.equals(other.getScopeCd())))
				&& ((this.scopeId == null && other.getScopeId() == null) || (this.scopeId != null && this.scopeId.equals(other.getScopeId())))
				&& ((this.transformerId == null && other.getTransformerId() == null) || (this.transformerId != null && this.transformerId.equals(other
						.getTransformerId())))
				&& ((this.type == null && other.getType() == null) || (this.type != null && this.type.equals(other.getStartDateField())))
				&& ((this.startDateField == null && other.getStartDateField() == null) || (this.startDateField != null && this.startDateField.equals(other
						.getStartDateField())))
				&& ((this.endDateField == null && other.getEndDateField() == null) || (this.endDateField != null && this.endDateField.equals(other
						.getSchedulingCronLine())))
				&& ((this.schedulingCronLine == null && other.getSchedulingCronLine() == null) || (this.schedulingCronLine != null && this.schedulingCronLine
						.equals(other.getSchedulingCronLine()))) && this.versionNum == other.getVersionNum();
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
				java.lang.Object obj = java.lang.reflect.Array.get(getDependentDataSets(), i);
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
		if (getStartDateField() != null) {
			_hashCode += getStartDateField().hashCode();
		}
		if (getEndDateField() != null) {
			_hashCode += getEndDateField().hashCode();
		}
		if (getSchedulingCronLine() != null) {
			_hashCode += getSchedulingCronLine().hashCode();
		}
		_hashCode += getVersionNum();
		__hashCodeCalc = false;
		return _hashCode;
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(SpagoBiDataSet.class, true);

	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName("http://bo.dataset.services.spagobi.eng.it", "SpagoBiDataSet"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("_public");
		elemField.setXmlName(new javax.xml.namespace.QName("", "_public"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("active");
		elemField.setXmlName(new javax.xml.namespace.QName("", "active"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("categoryId");
		elemField.setXmlName(new javax.xml.namespace.QName("", "categoryId"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("configuration");
		elemField.setXmlName(new javax.xml.namespace.QName("", "configuration"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("dataSource");
		elemField.setXmlName(new javax.xml.namespace.QName("", "dataSource"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://bo.datasource.services.spagobi.eng.it", "SpagoBiDataSource"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("dataSourceForReading");
		elemField.setXmlName(new javax.xml.namespace.QName("", "dataSourceForReading"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://bo.datasource.services.spagobi.eng.it", "SpagoBiDataSource"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("dependentDataSets");
		elemField.setXmlName(new javax.xml.namespace.QName("", "dependentDataSets"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("description");
		elemField.setXmlName(new javax.xml.namespace.QName("", "description"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("dsId");
		elemField.setXmlName(new javax.xml.namespace.QName("", "dsId"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("dsMetadata");
		elemField.setXmlName(new javax.xml.namespace.QName("", "dsMetadata"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("federationId");
		elemField.setXmlName(new javax.xml.namespace.QName("", "federationId"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("federationRelations");
		elemField.setXmlName(new javax.xml.namespace.QName("", "federationRelations"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("federationlabel");
		elemField.setXmlName(new javax.xml.namespace.QName("", "federationlabel"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("flatTableName");
		elemField.setXmlName(new javax.xml.namespace.QName("", "flatTableName"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("label");
		elemField.setXmlName(new javax.xml.namespace.QName("", "label"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("name");
		elemField.setXmlName(new javax.xml.namespace.QName("", "name"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("numRows");
		elemField.setXmlName(new javax.xml.namespace.QName("", "numRows"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("organization");
		elemField.setXmlName(new javax.xml.namespace.QName("", "organization"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("owner");
		elemField.setXmlName(new javax.xml.namespace.QName("", "owner"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("parameters");
		elemField.setXmlName(new javax.xml.namespace.QName("", "parameters"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("persistTableName");
		elemField.setXmlName(new javax.xml.namespace.QName("", "persistTableName"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("persisted");
		elemField.setXmlName(new javax.xml.namespace.QName("", "persisted"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("pivotColumnName");
		elemField.setXmlName(new javax.xml.namespace.QName("", "pivotColumnName"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("pivotColumnValue");
		elemField.setXmlName(new javax.xml.namespace.QName("", "pivotColumnValue"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("pivotRowName");
		elemField.setXmlName(new javax.xml.namespace.QName("", "pivotRowName"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("scheduled");
		elemField.setXmlName(new javax.xml.namespace.QName("", "scheduled"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("scopeCd");
		elemField.setXmlName(new javax.xml.namespace.QName("", "scopeCd"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("scopeId");
		elemField.setXmlName(new javax.xml.namespace.QName("", "scopeId"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("transformerId");
		elemField.setXmlName(new javax.xml.namespace.QName("", "transformerId"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("type");
		elemField.setXmlName(new javax.xml.namespace.QName("", "type"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("versionNum");
		elemField.setXmlName(new javax.xml.namespace.QName("", "versionNum"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("startDateField");
		elemField.setXmlName(new javax.xml.namespace.QName("", "startDateField"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("endDateField");
		elemField.setXmlName(new javax.xml.namespace.QName("", "endDateField"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("schedulingCronLine");
		elemField.setXmlName(new javax.xml.namespace.QName("", "schedulingCronLine"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);

	}

	/**
	 * Return type metadata object
	 */
	public static org.apache.axis.description.TypeDesc getTypeDesc() {
		return typeDesc;
	}

	/**
	 * Get Custom Serializer
	 */
	public static org.apache.axis.encoding.Serializer getSerializer(java.lang.String mechType, java.lang.Class _javaType, javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanSerializer(_javaType, _xmlType, typeDesc);
	}

	/**
	 * Get Custom Deserializer
	 */
	public static org.apache.axis.encoding.Deserializer getDeserializer(java.lang.String mechType, java.lang.Class _javaType, javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType, _xmlType, typeDesc);
	}

}
