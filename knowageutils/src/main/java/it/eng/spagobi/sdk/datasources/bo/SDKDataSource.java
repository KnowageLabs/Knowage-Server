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

package it.eng.spagobi.sdk.datasources.bo;

import java.io.ObjectOutputStream;

public class SDKDataSource implements java.io.Serializable {
	private String attrSchema;

	private String descr;

	private Integer dialectId;

	private String driver;

	private Integer id;

	private String jndi;

	private String label;

	private Integer multiSchema;

	private String name;

	private String pwd;

	private String urlConnection;

	public SDKDataSource() {
	}

	public SDKDataSource(String attrSchema, String descr, Integer dialectId, String driver, Integer id, String jndi,
			String label, Integer multiSchema, String name, String pwd, String urlConnection) {
		this.attrSchema = attrSchema;
		this.descr = descr;
		this.dialectId = dialectId;
		this.driver = driver;
		this.id = id;
		this.jndi = jndi;
		this.label = label;
		this.multiSchema = multiSchema;
		this.name = name;
		this.pwd = pwd;
		this.urlConnection = urlConnection;
	}

	/**
	 * Gets the attrSchema value for this SDKDataSource.
	 *
	 * @return attrSchema
	 */
	public String getAttrSchema() {
		return attrSchema;
	}

	/**
	 * Sets the attrSchema value for this SDKDataSource.
	 *
	 * @param attrSchema
	 */
	public void setAttrSchema(String attrSchema) {
		this.attrSchema = attrSchema;
	}

	/**
	 * Gets the descr value for this SDKDataSource.
	 *
	 * @return descr
	 */
	public String getDescr() {
		return descr;
	}

	/**
	 * Sets the descr value for this SDKDataSource.
	 *
	 * @param descr
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}

	/**
	 * Gets the dialectId value for this SDKDataSource.
	 *
	 * @return dialectId
	 */
	public Integer getDialectId() {
		return dialectId;
	}

	/**
	 * Sets the dialectId value for this SDKDataSource.
	 *
	 * @param dialectId
	 */
	public void setDialectId(Integer dialectId) {
		this.dialectId = dialectId;
	}

	/**
	 * Gets the driver value for this SDKDataSource.
	 *
	 * @return driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * Sets the driver value for this SDKDataSource.
	 *
	 * @param driver
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}

	/**
	 * Gets the id value for this SDKDataSource.
	 *
	 * @return id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id value for this SDKDataSource.
	 *
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the jndi value for this SDKDataSource.
	 *
	 * @return jndi
	 */
	public String getJndi() {
		return jndi;
	}

	/**
	 * Sets the jndi value for this SDKDataSource.
	 *
	 * @param jndi
	 */
	public void setJndi(String jndi) {
		this.jndi = jndi;
	}

	/**
	 * Gets the label value for this SDKDataSource.
	 *
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label value for this SDKDataSource.
	 *
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the multiSchema value for this SDKDataSource.
	 *
	 * @return multiSchema
	 */
	public Integer getMultiSchema() {
		return multiSchema;
	}

	/**
	 * Sets the multiSchema value for this SDKDataSource.
	 *
	 * @param multiSchema
	 */
	public void setMultiSchema(Integer multiSchema) {
		this.multiSchema = multiSchema;
	}

	/**
	 * Gets the name value for this SDKDataSource.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name value for this SDKDataSource.
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the pwd value for this SDKDataSource.
	 *
	 * @return pwd
	 */
	public String getPwd() {
		return pwd;
	}

	/**
	 * Sets the pwd value for this SDKDataSource.
	 *
	 * @param pwd
	 */
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	/**
	 * Gets the urlConnection value for this SDKDataSource.
	 *
	 * @return urlConnection
	 */
	public String getUrlConnection() {
		return urlConnection;
	}

	/**
	 * Sets the urlConnection value for this SDKDataSource.
	 *
	 * @param urlConnection
	 */
	public void setUrlConnection(String urlConnection) {
		this.urlConnection = urlConnection;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKDataSource))
			return false;
		SDKDataSource other = (SDKDataSource) obj;
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
				&& ((this.attrSchema == null && other.getAttrSchema() == null)
						|| (this.attrSchema != null && this.attrSchema.equals(other.getAttrSchema())))
				&& ((this.descr == null && other.getDescr() == null)
						|| (this.descr != null && this.descr.equals(other.getDescr())))
				&& ((this.dialectId == null && other.getDialectId() == null)
						|| (this.dialectId != null && this.dialectId.equals(other.getDialectId())))
				&& ((this.driver == null && other.getDriver() == null)
						|| (this.driver != null && this.driver.equals(other.getDriver())))
				&& ((this.id == null && other.getId() == null) || (this.id != null && this.id.equals(other.getId())))
				&& ((this.jndi == null && other.getJndi() == null)
						|| (this.jndi != null && this.jndi.equals(other.getJndi())))
				&& ((this.label == null && other.getLabel() == null)
						|| (this.label != null && this.label.equals(other.getLabel())))
				&& ((this.multiSchema == null && other.getMultiSchema() == null)
						|| (this.multiSchema != null && this.multiSchema.equals(other.getMultiSchema())))
				&& ((this.name == null && other.getName() == null)
						|| (this.name != null && this.name.equals(other.getName())))
				&& ((this.pwd == null && other.getPwd() == null)
						|| (this.pwd != null && this.pwd.equals(other.getPwd())))
				&& ((this.urlConnection == null && other.getUrlConnection() == null)
						|| (this.urlConnection != null && this.urlConnection.equals(other.getUrlConnection())));
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
		if (getAttrSchema() != null) {
			_hashCode += getAttrSchema().hashCode();
		}
		if (getDescr() != null) {
			_hashCode += getDescr().hashCode();
		}
		if (getDialectId() != null) {
			_hashCode += getDialectId().hashCode();
		}
		if (getDriver() != null) {
			_hashCode += getDriver().hashCode();
		}
		if (getId() != null) {
			_hashCode += getId().hashCode();
		}
		if (getJndi() != null) {
			_hashCode += getJndi().hashCode();
		}
		if (getLabel() != null) {
			_hashCode += getLabel().hashCode();
		}
		if (getMultiSchema() != null) {
			_hashCode += getMultiSchema().hashCode();
		}
		if (getName() != null) {
			_hashCode += getName().hashCode();
		}
		if (getPwd() != null) {
			_hashCode += getPwd().hashCode();
		}
		if (getUrlConnection() != null) {
			_hashCode += getUrlConnection().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}
	public void writeObject(ObjectOutputStream aOutputStream) {
		  throw new UnsupportedOperationException("Security violation : cannot serialize object to a stream");
	}

}
