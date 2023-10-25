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

package it.eng.spagobi.sdk.engines.bo;

public class SDKEngine implements java.io.Serializable {
	private String className;

	private String description;

	private String documentType;

	private String driverClassName;

	private String driverName;

	private Integer encrypt;

	private String engineType;

	private Integer id;

	private String label;

	private String mainUrl;

	private String name;

	private String secondUrl;

	private String url;

	private java.lang.Boolean useDataSet;

	private java.lang.Boolean useDataSource;

	public SDKEngine() {
	}

	public SDKEngine(String className, String description, String documentType, String driverClassName,
			String driverName, Integer encrypt, String engineType, Integer id, String label, String mainUrl,
			String name, String secondUrl, String url, java.lang.Boolean useDataSet, java.lang.Boolean useDataSource) {
		this.className = className;
		this.description = description;
		this.documentType = documentType;
		this.driverClassName = driverClassName;
		this.driverName = driverName;
		this.encrypt = encrypt;
		this.engineType = engineType;
		this.id = id;
		this.label = label;
		this.mainUrl = mainUrl;
		this.name = name;
		this.secondUrl = secondUrl;
		this.url = url;
		this.useDataSet = useDataSet;
		this.useDataSource = useDataSource;
	}

	/**
	 * Gets the className value for this SDKEngine.
	 * 
	 * @return className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Sets the className value for this SDKEngine.
	 * 
	 * @param className
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Gets the description value for this SDKEngine.
	 * 
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description value for this SDKEngine.
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the documentType value for this SDKEngine.
	 * 
	 * @return documentType
	 */
	public String getDocumentType() {
		return documentType;
	}

	/**
	 * Sets the documentType value for this SDKEngine.
	 * 
	 * @param documentType
	 */
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	/**
	 * Gets the driverClassName value for this SDKEngine.
	 * 
	 * @return driverClassName
	 */
	public String getDriverClassName() {
		return driverClassName;
	}

	/**
	 * Sets the driverClassName value for this SDKEngine.
	 * 
	 * @param driverClassName
	 */
	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	/**
	 * Gets the driverName value for this SDKEngine.
	 * 
	 * @return driverName
	 */
	public String getDriverName() {
		return driverName;
	}

	/**
	 * Sets the driverName value for this SDKEngine.
	 * 
	 * @param driverName
	 */
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	/**
	 * Gets the encrypt value for this SDKEngine.
	 * 
	 * @return encrypt
	 */
	public Integer getEncrypt() {
		return encrypt;
	}

	/**
	 * Sets the encrypt value for this SDKEngine.
	 * 
	 * @param encrypt
	 */
	public void setEncrypt(Integer encrypt) {
		this.encrypt = encrypt;
	}

	/**
	 * Gets the engineType value for this SDKEngine.
	 * 
	 * @return engineType
	 */
	public String getEngineType() {
		return engineType;
	}

	/**
	 * Sets the engineType value for this SDKEngine.
	 * 
	 * @param engineType
	 */
	public void setEngineType(String engineType) {
		this.engineType = engineType;
	}

	/**
	 * Gets the id value for this SDKEngine.
	 * 
	 * @return id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id value for this SDKEngine.
	 * 
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the label value for this SDKEngine.
	 * 
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label value for this SDKEngine.
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the mainUrl value for this SDKEngine.
	 * 
	 * @return mainUrl
	 */
	public String getMainUrl() {
		return mainUrl;
	}

	/**
	 * Sets the mainUrl value for this SDKEngine.
	 * 
	 * @param mainUrl
	 */
	public void setMainUrl(String mainUrl) {
		this.mainUrl = mainUrl;
	}

	/**
	 * Gets the name value for this SDKEngine.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name value for this SDKEngine.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the secondUrl value for this SDKEngine.
	 * 
	 * @return secondUrl
	 */
	public String getSecondUrl() {
		return secondUrl;
	}

	/**
	 * Sets the secondUrl value for this SDKEngine.
	 * 
	 * @param secondUrl
	 */
	public void setSecondUrl(String secondUrl) {
		this.secondUrl = secondUrl;
	}

	/**
	 * Gets the url value for this SDKEngine.
	 * 
	 * @return url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url value for this SDKEngine.
	 * 
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Gets the useDataSet value for this SDKEngine.
	 * 
	 * @return useDataSet
	 */
	public java.lang.Boolean getUseDataSet() {
		return useDataSet;
	}

	/**
	 * Sets the useDataSet value for this SDKEngine.
	 * 
	 * @param useDataSet
	 */
	public void setUseDataSet(java.lang.Boolean useDataSet) {
		this.useDataSet = useDataSet;
	}

	/**
	 * Gets the useDataSource value for this SDKEngine.
	 * 
	 * @return useDataSource
	 */
	public java.lang.Boolean getUseDataSource() {
		return useDataSource;
	}

	/**
	 * Sets the useDataSource value for this SDKEngine.
	 * 
	 * @param useDataSource
	 */
	public void setUseDataSource(java.lang.Boolean useDataSource) {
		this.useDataSource = useDataSource;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKEngine))
			return false;
		SDKEngine other = (SDKEngine) obj;
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
				&& ((this.className == null && other.getClassName() == null)
						|| (this.className != null && this.className.equals(other.getClassName())))
				&& ((this.description == null && other.getDescription() == null)
						|| (this.description != null && this.description.equals(other.getDescription())))
				&& ((this.documentType == null && other.getDocumentType() == null)
						|| (this.documentType != null && this.documentType.equals(other.getDocumentType())))
				&& ((this.driverClassName == null && other.getDriverClassName() == null)
						|| (this.driverClassName != null && this.driverClassName.equals(other.getDriverClassName())))
				&& ((this.driverName == null && other.getDriverName() == null)
						|| (this.driverName != null && this.driverName.equals(other.getDriverName())))
				&& ((this.encrypt == null && other.getEncrypt() == null)
						|| (this.encrypt != null && this.encrypt.equals(other.getEncrypt())))
				&& ((this.engineType == null && other.getEngineType() == null)
						|| (this.engineType != null && this.engineType.equals(other.getEngineType())))
				&& ((this.id == null && other.getId() == null) || (this.id != null && this.id.equals(other.getId())))
				&& ((this.label == null && other.getLabel() == null)
						|| (this.label != null && this.label.equals(other.getLabel())))
				&& ((this.mainUrl == null && other.getMainUrl() == null)
						|| (this.mainUrl != null && this.mainUrl.equals(other.getMainUrl())))
				&& ((this.name == null && other.getName() == null)
						|| (this.name != null && this.name.equals(other.getName())))
				&& ((this.secondUrl == null && other.getSecondUrl() == null)
						|| (this.secondUrl != null && this.secondUrl.equals(other.getSecondUrl())))
				&& ((this.url == null && other.getUrl() == null)
						|| (this.url != null && this.url.equals(other.getUrl())))
				&& ((this.useDataSet == null && other.getUseDataSet() == null)
						|| (this.useDataSet != null && this.useDataSet.equals(other.getUseDataSet())))
				&& ((this.useDataSource == null && other.getUseDataSource() == null)
						|| (this.useDataSource != null && this.useDataSource.equals(other.getUseDataSource())));
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
		if (getClassName() != null) {
			_hashCode += getClassName().hashCode();
		}
		if (getDescription() != null) {
			_hashCode += getDescription().hashCode();
		}
		if (getDocumentType() != null) {
			_hashCode += getDocumentType().hashCode();
		}
		if (getDriverClassName() != null) {
			_hashCode += getDriverClassName().hashCode();
		}
		if (getDriverName() != null) {
			_hashCode += getDriverName().hashCode();
		}
		if (getEncrypt() != null) {
			_hashCode += getEncrypt().hashCode();
		}
		if (getEngineType() != null) {
			_hashCode += getEngineType().hashCode();
		}
		if (getId() != null) {
			_hashCode += getId().hashCode();
		}
		if (getLabel() != null) {
			_hashCode += getLabel().hashCode();
		}
		if (getMainUrl() != null) {
			_hashCode += getMainUrl().hashCode();
		}
		if (getName() != null) {
			_hashCode += getName().hashCode();
		}
		if (getSecondUrl() != null) {
			_hashCode += getSecondUrl().hashCode();
		}
		if (getUrl() != null) {
			_hashCode += getUrl().hashCode();
		}
		if (getUseDataSet() != null) {
			_hashCode += getUseDataSet().hashCode();
		}
		if (getUseDataSource() != null) {
			_hashCode += getUseDataSource().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
