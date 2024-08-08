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

package it.eng.spagobi.sdk.datasets.bo;

import java.io.ObjectOutputStream;

public class SDKDataSet implements java.io.Serializable {
	private String description;

	// private String fileName;

	private Integer id;

	private Integer versionNum;

	private java.lang.Boolean active;

	private java.lang.Boolean _public;

	// private String javaClassName;

	// private Integer jdbcDataSourceId;

	// private String jdbcQuery;

	// private String jdbcQueryScript;

//    private String jdbcQueryScriptLanguage;

	private String label;

	private String name;

	private java.lang.Boolean numberingRows;

	private it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter[] parameters;

	private String pivotColumnName;

	private String pivotColumnValue;

	private String pivotRowName;

	// private String scriptLanguage;

	// private String scriptText;

	private String type;

	private String configuration;

	private String transformer;

	private String category;

	private String organization;

	// private String jsonQuery;

	// private String datamarts;

	// private String webServiceAddress;

	// private String webServiceOperation;

	// private String customData;

	public SDKDataSet() {
	}

	public SDKDataSet(String description, String fileName, Integer id, Integer versionNum, java.lang.Boolean active,
			java.lang.Boolean _public, String javaClassName, Integer jdbcDataSourceId, String jdbcQuery,
			String jdbcQueryScript, String jdbcQueryScriptLanguage, String label, String name,
			java.lang.Boolean numberingRows, it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter[] parameters,
			String pivotColumnName, String pivotColumnValue, String pivotRowName, String scriptLanguage,
			String scriptText, String type, String configuration, String userIn, String userUp, String userDe,
			String sbiVersionIn, String sbiVersionUp, String sbiVersionDe, String metaVersion, String organization,
			String transformer, String category, String jsonQuery, String datamarts, String webServiceAddress,
			String webServiceOperation, String customData) {
		this.description = description;
		// this.fileName = fileName;
		this.id = id;
		this.versionNum = versionNum;
		this.active = active;
		this._public = _public;
		// this.javaClassName = javaClassName;
		// this.jdbcDataSourceId = jdbcDataSourceId;
		// this.jdbcQuery = jdbcQuery;
		// this.jdbcQueryScript = jdbcQueryScript;
		// this.jdbcQueryScriptLanguage = jdbcQueryScriptLanguage;
		this.label = label;
		this.name = name;
		this.numberingRows = numberingRows;
		this.parameters = parameters;
		this.pivotColumnName = pivotColumnName;
		this.pivotColumnValue = pivotColumnValue;
		this.pivotRowName = pivotRowName;
		// this.scriptLanguage = scriptLanguage;
		// this.scriptText = scriptText;
		this.type = type;
		this.configuration = configuration;
		this.transformer = transformer;
		this.category = category;
		this.organization = organization;

		// this.jsonQuery = jsonQuery;
		// this.datamarts = datamarts;
		// this.webServiceAddress = webServiceAddress;
		// this.webServiceOperation = webServiceOperation;
		// this.customData = customData;
	}

	/**
	 * Gets the description value for this SDKDataSet.
	 *
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description value for this SDKDataSet.
	 *
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the fileName value for this SDKDataSet.
	 *
	 * @return fileName
	 *
	 *         public String getFileName() { return fileName; }
	 */

	/**
	 * Sets the fileName value for this SDKDataSet.
	 *
	 * @param fileName
	 *
	 *                 public void setFileName(String fileName) { this.fileName = fileName; }
	 */

	/**
	 * Gets the id value for this SDKDataSet.
	 *
	 * @return id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @return the versionNum
	 */
	public Integer getVersionNum() {
		return versionNum;
	}

	/**
	 * @param versionNum the versionNum to set
	 */
	public void setVersionNum(Integer versionNum) {
		this.versionNum = versionNum;
	}

	/**
	 * @return the active
	 */
	public java.lang.Boolean getActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(java.lang.Boolean active) {
		this.active = active;
	}

	public java.lang.Boolean get_public() {
		return _public;
	}

	public void set_public(java.lang.Boolean _public) {
		this._public = _public;
	}

	/**
	 * Sets the id value for this SDKDataSet.
	 *
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the javaClassName value for this SDKDataSet.
	 *
	 * @return javaClassName
	 *
	 *         public String getJavaClassName() { return javaClassName; }
	 */

	/**
	 * Sets the javaClassName value for this SDKDataSet.
	 *
	 * @param javaClassName
	 *
	 *                      public void setJavaClassName(String javaClassName) { this.javaClassName = javaClassName; }
	 */

	/**
	 * Gets the jdbcDataSourceId value for this SDKDataSet.
	 *
	 * @return jdbcDataSourceId
	 *
	 *         public Integer getJdbcDataSourceId() { return jdbcDataSourceId; }
	 */

	/**
	 * Sets the jdbcDataSourceId value for this SDKDataSet.
	 *
	 * @param jdbcDataSourceId
	 *
	 *                         public void setJdbcDataSourceId(Integer jdbcDataSourceId) { this.jdbcDataSourceId = jdbcDataSourceId; }
	 */

	/**
	 * Gets the jdbcQuery value for this SDKDataSet.
	 *
	 * @return jdbcQuery
	 *
	 *         public String getJdbcQuery() { return jdbcQuery; }
	 */

	/**
	 * Sets the jdbcQuery value for this SDKDataSet.
	 *
	 * @param jdbcQuery
	 *
	 *                  public void setJdbcQuery(String jdbcQuery) { this.jdbcQuery = jdbcQuery; }
	 *
	 *                  public String getJdbcQueryScript() { return jdbcQueryScript; }
	 *
	 *                  public void setJdbcQueryScript(String jdbcQueryScript) { this.jdbcQueryScript = jdbcQueryScript; }
	 *
	 *                  public String getJdbcQueryScriptLanguage() { return jdbcQueryScriptLanguage; }
	 *
	 *                  public void setJdbcQueryScriptLanguage(String jdbcQueryScriptLanguage) { this.jdbcQueryScriptLanguage = jdbcQueryScriptLanguage; }
	 */
	/**
	 * Gets the label value for this SDKDataSet.
	 *
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label value for this SDKDataSet.
	 *
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the name value for this SDKDataSet.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name value for this SDKDataSet.
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the numberingRows value for this SDKDataSet.
	 *
	 * @return numberingRows
	 */
	public java.lang.Boolean getNumberingRows() {
		return numberingRows;
	}

	/**
	 * Sets the numberingRows value for this SDKDataSet.
	 *
	 * @param numberingRows
	 */
	public void setNumberingRows(java.lang.Boolean numberingRows) {
		this.numberingRows = numberingRows;
	}

	/**
	 * Gets the parameters value for this SDKDataSet.
	 *
	 * @return parameters
	 */
	public it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter[] getParameters() {
		return parameters;
	}

	/**
	 * Sets the parameters value for this SDKDataSet.
	 *
	 * @param parameters
	 */
	public void setParameters(it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter[] parameters) {
		this.parameters = parameters;
	}

	/**
	 * Gets the pivotColumnName value for this SDKDataSet.
	 *
	 * @return pivotColumnName
	 */
	public String getPivotColumnName() {
		return pivotColumnName;
	}

	/**
	 * Sets the pivotColumnName value for this SDKDataSet.
	 *
	 * @param pivotColumnName
	 */
	public void setPivotColumnName(String pivotColumnName) {
		this.pivotColumnName = pivotColumnName;
	}

	/**
	 * Gets the pivotColumnValue value for this SDKDataSet.
	 *
	 * @return pivotColumnValue
	 */
	public String getPivotColumnValue() {
		return pivotColumnValue;
	}

	/**
	 * Sets the pivotColumnValue value for this SDKDataSet.
	 *
	 * @param pivotColumnValue
	 */
	public void setPivotColumnValue(String pivotColumnValue) {
		this.pivotColumnValue = pivotColumnValue;
	}

	/**
	 * Gets the pivotRowName value for this SDKDataSet.
	 *
	 * @return pivotRowName
	 */
	public String getPivotRowName() {
		return pivotRowName;
	}

	/**
	 * Sets the pivotRowName value for this SDKDataSet.
	 *
	 * @param pivotRowName
	 */
	public void setPivotRowName(String pivotRowName) {
		this.pivotRowName = pivotRowName;
	}

	/**
	 * Gets the scriptLanguage value for this SDKDataSet.
	 *
	 * @return scriptLanguage
	 *
	 *         public String getScriptLanguage() { return scriptLanguage; }
	 */

	/**
	 * Sets the scriptLanguage value for this SDKDataSet.
	 *
	 * @param scriptLanguage
	 *
	 *                       public void setScriptLanguage(String scriptLanguage) { this.scriptLanguage = scriptLanguage; }
	 *
	 *
	 *                       /** Gets the scriptText value for this SDKDataSet.
	 *
	 * @return scriptText
	 *
	 *         public String getScriptText() { return scriptText; }
	 *
	 *
	 *         /** Sets the scriptText value for this SDKDataSet.
	 *
	 * @param scriptText
	 *
	 *                   public void setScriptText(String scriptText) { this.scriptText = scriptText; }
	 */

	/**
	 * Gets the type value for this SDKDataSet.
	 *
	 * @return type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type value for this SDKDataSet.
	 *
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the transformer value for this SDKDataSet.
	 *
	 * @return transformer
	 */
	public String getTransformer() {
		return transformer;
	}

	/**
	 * Sets the transformer value for this SDKDataSet.
	 *
	 * @param transformer
	 */
	public void setTransformer(String transformer) {
		this.transformer = transformer;
	}

	/**
	 * Gets the category value for this SDKDataSet.
	 *
	 * @return category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Sets the category value for this SDKDataSet.
	 *
	 * @param category
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * Gets the jsonQuery value for this SDKDataSet.
	 *
	 * @return jsonQuery
	 *
	 *         public String getJsonQuery() { return jsonQuery; }
	 *
	 *
	 *         /** Sets the jsonQuery value for this SDKDataSet.
	 *
	 * @param jsonQuery
	 *
	 *                  public void setJsonQuery(String jsonQuery) { this.jsonQuery = jsonQuery; }
	 *
	 *
	 *                  /** Gets the datamarts value for this SDKDataSet.
	 *
	 * @return datamarts
	 *
	 *         public String getDatamarts() { return datamarts; }
	 *
	 *
	 *         /** Sets the datamarts value for this SDKDataSet.
	 *
	 * @param datamarts
	 *
	 *                  public void setDatamarts(String datamarts) { this.datamarts = datamarts; }
	 *
	 *
	 *                  /** Gets the webServiceAddress value for this SDKDataSet.
	 *
	 * @return webServiceAddress
	 *
	 *         public String getWebServiceAddress() { return webServiceAddress; }
	 *
	 *
	 *         /** Sets the webServiceAddress value for this SDKDataSet.
	 *
	 * @param webServiceAddress
	 *
	 *                          public void setWebServiceAddress(String webServiceAddress) { this.webServiceAddress = webServiceAddress; }
	 *
	 *
	 *                          /** Gets the webServiceOperation value for this SDKDataSet.
	 *
	 * @return webServiceOperation
	 *
	 *         public String getWebServiceOperation() { return webServiceOperation; }
	 *
	 *
	 *         /** Sets the webServiceOperation value for this SDKDataSet.
	 *
	 * @param webServiceOperation
	 *
	 *                            public void setWebServiceOperation(String webServiceOperation) { this.webServiceOperation = webServiceOperation; }
	 *
	 *
	 *                            public String getCustomData() { return customData; }
	 *
	 *                            public void setCustomData(String customData) { this.customData = customData; }
	 */
	/**
	 * @return the configuration
	 */
	public String getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKDataSet))
			return false;
		SDKDataSet other = (SDKDataSet) obj;
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true && ((this.description == null && other.getDescription() == null)
				|| (this.description != null && this.description.equals(other.getDescription()))) &&
		/*
		 * ((this.fileName==null && other.getFileName()==null) || (this.fileName!=null && this.fileName.equals(other.getFileName()))) &&
		 */
				((this.id == null && other.getId() == null) || (this.id != null && this.id.equals(other.getId())))
				&& ((this.versionNum == null && other.getVersionNum() == null)
						|| (this.versionNum != null && this.versionNum.equals(other.getVersionNum())))
				&& ((this.active == null && other.getActive() == null)
						|| (this.active != null && this.active.equals(other.getActive())))
				&& ((this._public == null && other.get_public() == null)
						|| (this._public != null && this._public.equals(other.get_public())))
				&&
				/*
				 * ((this.javaClassName==null && other.getJavaClassName()==null) || (this.javaClassName!=null && this.javaClassName.equals(other.getJavaClassName()))) &&
				 * ((this.jdbcDataSourceId==null && other.getJdbcDataSourceId()==null) || (this.jdbcDataSourceId!=null &&
				 * this.jdbcDataSourceId.equals(other.getJdbcDataSourceId()))) && ((this.jdbcQuery==null && other.getJdbcQuery()==null) || (this.jdbcQuery!=null &&
				 * this.jdbcQuery.equals(other.getJdbcQuery()))) && ((this.jdbcQueryScript==null && other.getJdbcQueryScript()==null) || (this.jdbcQueryScript!=null &&
				 * this.jdbcQueryScript.equals(other.getJdbcQueryScript()))) && ((this.jdbcQueryScriptLanguage==null && other.getJdbcQueryScriptLanguage()==null) ||
				 * (this.jdbcQueryScriptLanguage!=null && this.jdbcQueryScriptLanguage.equals(other.getJdbcQueryScriptLanguage()))) &&
				 */
				((this.label == null && other.getLabel() == null)
						|| (this.label != null && this.label.equals(other.getLabel())))
				&& ((this.name == null && other.getName() == null)
						|| (this.name != null && this.name.equals(other.getName())))
				&& ((this.numberingRows == null && other.getNumberingRows() == null)
						|| (this.numberingRows != null && this.numberingRows.equals(other.getNumberingRows())))
				&& ((this.parameters == null && other.getParameters() == null)
						|| (this.parameters != null && java.util.Arrays.equals(this.parameters, other.getParameters())))
				&& ((this.pivotColumnName == null && other.getPivotColumnName() == null)
						|| (this.pivotColumnName != null && this.pivotColumnName.equals(other.getPivotColumnName())))
				&& ((this.pivotColumnValue == null && other.getPivotColumnValue() == null)
						|| (this.pivotColumnValue != null && this.pivotColumnValue.equals(other.getPivotColumnValue())))
				&& ((this.pivotRowName == null && other.getPivotRowName() == null)
						|| (this.pivotRowName != null && this.pivotRowName.equals(other.getPivotRowName())))
				&&
				/*
				 * ((this.scriptLanguage==null && other.getScriptLanguage()==null) || (this.scriptLanguage!=null && this.scriptLanguage.equals(other.getScriptLanguage()))) &&
				 * ((this.scriptText==null && other.getScriptText()==null) || (this.scriptText!=null && this.scriptText.equals(other.getScriptText()))) &&
				 */
				((this.type == null && other.getType() == null)
						|| (this.type != null && this.type.equals(other.getType())))
				&& ((this.configuration == null && other.getConfiguration() == null)
						|| (this.configuration != null && this.configuration.equals(other.getConfiguration())))
				&& ((this.transformer == null && other.getTransformer() == null)
						|| (this.transformer != null && this.transformer.equals(other.getTransformer())))
				&& ((this.organization == null && other.getOrganization() == null)
						|| (this.organization != null && this.organization.equals(other.getOrganization())))
				&& ((this.category == null && other.getCategory() == null)
						|| (this.category != null && this.category.equals(other.getCategory()))) /*
																									 * && ((this.jsonQuery==null && other.getJsonQuery()==null) || (this.jsonQuery!=null && this.jsonQuery.equals(other.getJsonQuery()))) && ((this.datamarts==null
																									 * && other.getDatamarts()==null) || (this.datamarts!=null && this.datamarts.equals(other.getDatamarts()))) && ((this.webServiceAddress==null &&
																									 * other.getWebServiceAddress()==null) || (this.webServiceAddress!=null && this.webServiceAddress.equals(other.getWebServiceAddress()))) &&
																									 * ((this.customData==null && other.getCustomData()==null) || (this.customData!=null && this.customData.equals(other.getCustomData()))) &&
																									 * ((this.webServiceOperation==null && other.getWebServiceOperation()==null) || (this.webServiceOperation!=null &&
																									 * this.webServiceOperation.equals(other.getWebServiceOperation())))
																									 */
		;
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
		if (getDescription() != null) {
			_hashCode += getDescription().hashCode();
		}
		/*
		 * if (getFileName() != null) { _hashCode += getFileName().hashCode(); } if (getId() != null) { _hashCode += getId().hashCode(); } if (getJavaClassName() !=
		 * null) { _hashCode += getJavaClassName().hashCode(); } if (getJdbcDataSourceId() != null) { _hashCode += getJdbcDataSourceId().hashCode(); } if
		 * (getJdbcQuery() != null) { _hashCode += getJdbcQuery().hashCode(); } if (getJdbcQueryScript() != null) { _hashCode += getJdbcQueryScript().hashCode(); } if
		 * (getJdbcQueryScriptLanguage() != null) { _hashCode += getJdbcQueryScriptLanguage().hashCode(); }
		 */
		if (getLabel() != null) {
			_hashCode += getLabel().hashCode();
		}
		if (getName() != null) {
			_hashCode += getName().hashCode();
		}
		if (getNumberingRows() != null) {
			_hashCode += getNumberingRows().hashCode();
		}
		if (get_public() != null) {
			_hashCode += get_public().hashCode();
		}
		if (getParameters() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getParameters()); i++) {
				Object obj = java.lang.reflect.Array.get(getParameters(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getPivotColumnName() != null) {
			_hashCode += getPivotColumnName().hashCode();
		}
		if (getPivotColumnValue() != null) {
			_hashCode += getPivotColumnValue().hashCode();
		}
		if (getPivotRowName() != null) {
			_hashCode += getPivotRowName().hashCode();
		}
		/*
		 * if (getScriptLanguage() != null) { _hashCode += getScriptLanguage().hashCode(); } if (getScriptText() != null) { _hashCode += getScriptText().hashCode(); }
		 */
		if (getType() != null) {
			_hashCode += getType().hashCode();
		}
		if (getConfiguration() != null) {
			_hashCode += getConfiguration().hashCode();
		}
		if (getTransformer() != null) {
			_hashCode += getTransformer().hashCode();
		}
		if (getCategory() != null) {
			_hashCode += getCategory().hashCode();
		}
		if (getOrganization() != null) {
			_hashCode += getOrganization().hashCode();
		}
		/*
		 * if (getJsonQuery() != null) { _hashCode += getJsonQuery().hashCode(); } if (getDatamarts() != null) { _hashCode += getDatamarts().hashCode(); } if
		 * (getWebServiceAddress() != null) { _hashCode += getWebServiceAddress().hashCode(); } if (getWebServiceOperation() != null) { _hashCode +=
		 * getWebServiceOperation().hashCode(); } if (getCustomData() != null) { _hashCode += getCustomData().hashCode(); }
		 */
		__hashCodeCalc = false;
		return _hashCode;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}
	
	private final void writeObject(ObjectOutputStream aOutputStream) {
		  throw new UnsupportedOperationException("Security violation : cannot serialize object to a stream");
	}

}
