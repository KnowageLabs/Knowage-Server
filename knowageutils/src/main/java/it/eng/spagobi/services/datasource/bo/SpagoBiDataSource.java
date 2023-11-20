/**
 * SpagoBiDataSource.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.services.datasource.bo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class SpagoBiDataSource implements java.io.Serializable {
	private String driver;

	private String hibDialectClass;

	private int id;

	private String jdbcPoolConfiguration;

	private String jndiName;

	private String label;

	private java.lang.Boolean multiSchema;

	private String password;

	private java.lang.Boolean readOnly;

	private String schemaAttribute;

	private String url;

	private String user;

	private java.lang.Boolean writeDefault;

	private java.lang.Boolean useForDataprep;

	public SpagoBiDataSource() {
	}

	public SpagoBiDataSource(String driver, String hibDialectClass, int id, String jdbcPoolConfiguration,
			String jndiName, String label, java.lang.Boolean multiSchema, String password, java.lang.Boolean readOnly,
			String schemaAttribute, String url, String user, java.lang.Boolean writeDefault,
			java.lang.Boolean useForDataprep) {
		this.driver = driver;
		this.hibDialectClass = hibDialectClass;
		this.id = id;
		this.jdbcPoolConfiguration = jdbcPoolConfiguration;
		this.jndiName = jndiName;
		this.label = label;
		this.multiSchema = multiSchema;
		this.password = password;
		this.readOnly = readOnly;
		this.schemaAttribute = schemaAttribute;
		this.url = url;
		this.user = user;
		this.writeDefault = writeDefault;
		this.useForDataprep = useForDataprep;
	}

	/**
	 * Gets the driver value for this SpagoBiDataSource.
	 *
	 * @return driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * Sets the driver value for this SpagoBiDataSource.
	 *
	 * @param driver
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}

	/**
	 * Gets the hibDialectClass value for this SpagoBiDataSource.
	 *
	 * @return hibDialectClass
	 */
	public String getHibDialectClass() {
		return hibDialectClass;
	}

	/**
	 * Sets the hibDialectClass value for this SpagoBiDataSource.
	 *
	 * @param hibDialectClass
	 */
	public void setHibDialectClass(String hibDialectClass) {
		this.hibDialectClass = hibDialectClass;
	}

	/**
	 * Gets the id value for this SpagoBiDataSource.
	 *
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id value for this SpagoBiDataSource.
	 *
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the jdbcPoolConfiguration value for this SpagoBiDataSource.
	 *
	 * @return jdbcPoolConfiguration
	 */
	public String getJdbcPoolConfiguration() {
		return jdbcPoolConfiguration;
	}

	/**
	 * Sets the jdbcPoolConfiguration value for this SpagoBiDataSource.
	 *
	 * @param jdbcPoolConfiguration
	 */
	public void setJdbcPoolConfiguration(String jdbcPoolConfiguration) {
		this.jdbcPoolConfiguration = jdbcPoolConfiguration;
	}

	/**
	 * Gets the jndiName value for this SpagoBiDataSource.
	 *
	 * @return jndiName
	 */
	public String getJndiName() {
		return jndiName;
	}

	/**
	 * Sets the jndiName value for this SpagoBiDataSource.
	 *
	 * @param jndiName
	 */
	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	/**
	 * Gets the label value for this SpagoBiDataSource.
	 *
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label value for this SpagoBiDataSource.
	 *
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the multiSchema value for this SpagoBiDataSource.
	 *
	 * @return multiSchema
	 */
	public java.lang.Boolean getMultiSchema() {
		return multiSchema;
	}

	/**
	 * Sets the multiSchema value for this SpagoBiDataSource.
	 *
	 * @param multiSchema
	 */
	public void setMultiSchema(java.lang.Boolean multiSchema) {
		this.multiSchema = multiSchema;
	}

	/**
	 * Gets the password value for this SpagoBiDataSource.
	 *
	 * @return password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password value for this SpagoBiDataSource.
	 *
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the readOnly value for this SpagoBiDataSource.
	 *
	 * @return readOnly
	 */
	public java.lang.Boolean getReadOnly() {
		return readOnly;
	}

	/**
	 * Sets the readOnly value for this SpagoBiDataSource.
	 *
	 * @param readOnly
	 */
	public void setReadOnly(java.lang.Boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * Gets the schemaAttribute value for this SpagoBiDataSource.
	 *
	 * @return schemaAttribute
	 */
	public String getSchemaAttribute() {
		return schemaAttribute;
	}

	/**
	 * Sets the schemaAttribute value for this SpagoBiDataSource.
	 *
	 * @param schemaAttribute
	 */
	public void setSchemaAttribute(String schemaAttribute) {
		this.schemaAttribute = schemaAttribute;
	}

	/**
	 * Gets the url value for this SpagoBiDataSource.
	 *
	 * @return url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url value for this SpagoBiDataSource.
	 *
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Gets the user value for this SpagoBiDataSource.
	 *
	 * @return user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the user value for this SpagoBiDataSource.
	 *
	 * @param user
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Sets the useForDataprep value for this SpagoBiDataSource.
	 *
	 * @param useForDataprep
	 */
	public void setUseForDataprep(java.lang.Boolean useForDataprep) {
		this.useForDataprep = useForDataprep;
	}

	/**
	 * Gets the useForDataprep value for this SpagoBiDataSource.
	 *
	 * @return useForDataprep
	 */
	public java.lang.Boolean getUseForDataprep() {
		return useForDataprep;
	}

	/**
	 * Gets the writeDefault value for this SpagoBiDataSource.
	 *
	 * @return writeDefault
	 */
	public java.lang.Boolean getWriteDefault() {
		return writeDefault;
	}

	/**
	 * Sets the writeDefault value for this SpagoBiDataSource.
	 *
	 * @param writeDefault
	 */
	public void setWriteDefault(java.lang.Boolean writeDefault) {
		this.writeDefault = writeDefault;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SpagoBiDataSource))
			return false;
		SpagoBiDataSource other = (SpagoBiDataSource) obj;
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
				&& ((this.driver == null && other.getDriver() == null)
						|| (this.driver != null && this.driver.equals(other.getDriver())))
				&& ((this.hibDialectClass == null && other.getHibDialectClass() == null)
						|| (this.hibDialectClass != null && this.hibDialectClass.equals(other.getHibDialectClass())))
				&& this.id == other.getId()
				&& ((this.jdbcPoolConfiguration == null && other.getJdbcPoolConfiguration() == null)
						|| (this.jdbcPoolConfiguration != null
								&& this.jdbcPoolConfiguration.equals(other.getJdbcPoolConfiguration())))
				&& ((this.jndiName == null && other.getJndiName() == null)
						|| (this.jndiName != null && this.jndiName.equals(other.getJndiName())))
				&& ((this.label == null && other.getLabel() == null)
						|| (this.label != null && this.label.equals(other.getLabel())))
				&& ((this.multiSchema == null && other.getMultiSchema() == null)
						|| (this.multiSchema != null && this.multiSchema.equals(other.getMultiSchema())))
				&& ((this.password == null && other.getPassword() == null)
						|| (this.password != null && this.password.equals(other.getPassword())))
				&& ((this.readOnly == null && other.getReadOnly() == null)
						|| (this.readOnly != null && this.readOnly.equals(other.getReadOnly())))
				&& ((this.schemaAttribute == null && other.getSchemaAttribute() == null)
						|| (this.schemaAttribute != null && this.schemaAttribute.equals(other.getSchemaAttribute())))
				&& ((this.url == null && other.getUrl() == null)
						|| (this.url != null && this.url.equals(other.getUrl())))
				&& ((this.user == null && other.getUser() == null)
						|| (this.user != null && this.user.equals(other.getUser())))
				&& ((this.writeDefault == null && other.getWriteDefault() == null)
						|| (this.writeDefault != null && this.writeDefault.equals(other.getWriteDefault())))
				&& ((this.useForDataprep == null && other.getUseForDataprep() == null)
						|| (this.useForDataprep != null && this.useForDataprep.equals(other.getUseForDataprep())));
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
		if (getDriver() != null) {
			_hashCode += getDriver().hashCode();
		}
		if (getHibDialectClass() != null) {
			_hashCode += getHibDialectClass().hashCode();
		}
		_hashCode += getId();
		if (getJdbcPoolConfiguration() != null) {
			_hashCode += getJdbcPoolConfiguration().hashCode();
		}
		if (getJndiName() != null) {
			_hashCode += getJndiName().hashCode();
		}
		if (getLabel() != null) {
			_hashCode += getLabel().hashCode();
		}
		if (getMultiSchema() != null) {
			_hashCode += getMultiSchema().hashCode();
		}
		if (getPassword() != null) {
			_hashCode += getPassword().hashCode();
		}
		if (getReadOnly() != null) {
			_hashCode += getReadOnly().hashCode();
		}
		if (getSchemaAttribute() != null) {
			_hashCode += getSchemaAttribute().hashCode();
		}
		if (getUrl() != null) {
			_hashCode += getUrl().hashCode();
		}
		if (getUser() != null) {
			_hashCode += getUser().hashCode();
		}
		if (getWriteDefault() != null) {
			_hashCode += getWriteDefault().hashCode();
		}
		if (getUseForDataprep() != null) {
			_hashCode += getUseForDataprep().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

	/**
	 * Read connection.
	 *
	 * @return the connection
	 *
	 * @throws NamingException        the naming exception
	 * @throws SQLException           the SQL exception
	 * @throws ClassNotFoundException the class not found exception
	 */
	public Connection readConnection(String schema) throws NamingException, SQLException, ClassNotFoundException {
		Connection connection = null;

		if (checkIsJndi()) {
			connection = readJndiConnection(schema);
		} else {
			connection = readDirectConnection();
		}

		return connection;
	}

	/**
	 * Check is jndi.
	 *
	 * @return true, if successful
	 */
	public boolean checkIsJndi() {
		return getJndiName() != null && !getJndiName().equals("");
	}

	private boolean checkIsMultiSchema() {
		return multiSchema != null && multiSchema.booleanValue();
	}

	/**
	 * Get the connection from JNDI
	 *
	 * @param connectionConfig SourceBean describing data connection
	 * @return Connection to database
	 * @throws NamingException
	 * @throws SQLException
	 */
	private Connection readJndiConnection(String schema) throws NamingException, SQLException {
		Connection connection = null;

		Context ctx;
		ctx = new InitialContext();
		DataSource ds = null;
		if (checkIsMultiSchema()) {
			ds = (DataSource) ctx.lookup(getJndiName() + schema);
		} else {
			ds = (DataSource) ctx.lookup(getJndiName());
		}
		connection = ds.getConnection();

		return connection;
	}

	/**
	 * Get the connection using jdbc
	 *
	 * @param connectionConfig SpagoBiDataSource describing data connection
	 * @return Connection to database
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private Connection readDirectConnection() throws ClassNotFoundException, SQLException {
		Connection connection = null;

		Class.forName(getDriver());
		connection = DriverManager.getConnection(getUrl(), getUser(), getPassword());

		return connection;
	}

}
