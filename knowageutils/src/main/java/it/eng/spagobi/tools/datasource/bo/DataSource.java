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
package it.eng.spagobi.tools.datasource.bo;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;
import it.eng.spagobi.services.validation.Xss;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDatasetFactory;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.metasql.query.SelectQuery;
import it.eng.spagobi.tools.datasource.DataSourceManager;
import it.eng.spagobi.tools.datasource.bo.serializer.JDBCDataSourcePoolConfigurationJSONSerializer;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Defines an <code>DataSource</code> object
 *
 * The field <code>pwd</code> must be hidden during deserialization because of security concerns: with {@link JsonIgnoreProperties} we enable it during
 * serialization only.
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(value = { "pwd" }, allowGetters = false, allowSetters = true)
public class DataSource implements Serializable, IDataSource {

	private static transient Logger logger = Logger.getLogger(DataSource.class);

	@NotNull
	private int dsId;

	@Xss
	@Size(max = 160)
	private String descr;

	@NotNull
	@Xss
	@Size(max = 50)
	private String label;

	@Xss
	@Size(max = 50)
	private String jndi;

	@Xss
	@Size(max = 500)
	private String urlConnection;

	@Xss
	@Size(max = 50)
	private String user;

	@Xss
	@Size(max = 50)
	private String pwd;

	@Xss
	@Size(max = 160)
	private String driver;

	@NotNull
	@Size(max = 50)
	private String dialectName;

	private String hibDialectClass;
	private Set engines = null;
	private Set objects = null;

	@Size(max = 45)
	private String schemaAttribute = null;

	@NotNull
	private Boolean multiSchema = null;

	private Boolean readOnly;
	private Boolean writeDefault;
	private Boolean useForDataprep;

	// Advanced Options - JDBCPoolConfiguration
	private JDBCDataSourcePoolConfiguration jdbcPoolConfiguration;

	// Owner of DataSource - UserIn column in Database
	private String owner;

	public Boolean getReadOnly() {
		return readOnly;
	}

	public Boolean getWriteDefault() {
		return writeDefault;
	}

	public Boolean getUseForDataprep() {
		return useForDataprep;
	}

	@Override
	public String getSchemaAttribute() {
		return schemaAttribute;
	}

	@Override
	public void setSchemaAttribute(String schemaAttribute) {
		this.schemaAttribute = schemaAttribute;
	}

	@Override
	public Boolean getMultiSchema() {
		return multiSchema;
	}

	@Override
	public void setMultiSchema(Boolean multiSchema) {
		this.multiSchema = multiSchema;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#toSpagoBiDataSource()
	 */
	@Override
	public SpagoBiDataSource toSpagoBiDataSource() {
		SpagoBiDataSource sbd = new SpagoBiDataSource();
		sbd.setId(dsId);
		sbd.setDriver(driver);
		sbd.setHibDialectClass("");
		sbd.setJndiName(jndi);
		sbd.setLabel(label);
		sbd.setPassword(pwd);
		sbd.setUrl(urlConnection);
		sbd.setUser(user);
		sbd.setHibDialectClass(hibDialectClass);
		sbd.setMultiSchema(multiSchema);
		sbd.setSchemaAttribute(schemaAttribute);
		sbd.setReadOnly(readOnly);
		sbd.setWriteDefault(writeDefault);
		sbd.setUseForDataprep(useForDataprep);

		if (jdbcPoolConfiguration != null) {
			sbd.setJdbcPoolConfiguration((String) new JDBCDataSourcePoolConfigurationJSONSerializer().serialize(jdbcPoolConfiguration));
		}
		return sbd;
	}

	@Override
	public boolean checkIsMultiSchema() {
		return getMultiSchema() != null && getMultiSchema().booleanValue();
	}

	@Override
	public boolean checkIsJndi() {
		return getJndi() != null && getJndi().equals("") == false;
	}

	@Override
	@JsonIgnore
	public Connection getConnection() throws NamingException, SQLException, ClassNotFoundException {
		IEngUserProfile profile = UserProfileManager.getProfile();
		return getConnectionByUserProfile(profile);
	}

	@Override
	public Connection getConnectionByUserProfile(IEngUserProfile profile) {
		try {
			if (checkIsJndi()) {
				String jndiName = this.getJNDIRunTime(profile);
				return getJndiConnection(jndiName);
			} else {
				return getDirectConnection();
			}
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("Cannot get connection to datasource", e);
		}
	}

	/**
	 * Get the connection from JNDI.
	 *
	 * @return Connection to database
	 *
	 * @throws NamingException the naming exception
	 * @throws SQLException    the SQL exception
	 */
	private Connection getJndiConnection(String jndiName) throws NamingException, SQLException {
		Connection connection = null;
		Context ctx;
		ctx = new InitialContext();
		javax.sql.DataSource ds = (javax.sql.DataSource) ctx.lookup(jndiName);
		connection = ds.getConnection();
		return connection;
	}

	@Override
	public String getJNDIRunTime(IEngUserProfile profile) {

		if (checkIsMultiSchema()) {
			// We check if User profile is required by datasource (in case of multischema datasource) but user profile object is missing
			Assert.assertNotNull(profile, "Datasource is multischema, but User profile object is not provided");
			String attributeName = this.getSchemaAttribute();
			logger.debug("Datasource multischema attribute name: " + attributeName);
			String jndiName = null;
			String schema = null;
			logger.debug("Looking for attribute [" + attributeName + "] for user [" + profile + "] ...");
			Object attributeValue;
			try {
				attributeValue = profile.getUserAttribute(attributeName);
				if (attributeValue != null) {
					schema = attributeValue.toString();
					Assert.assertNotEmpty(schema.trim(), "Attibute value of current User profile is not provided");
				}
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("Cannot get attribute [" + attributeName + "] from user profile object", e);
			}
			logger.debug("Attribute " + attributeName + " is " + attributeValue);

			jndiName = getJndi() + schema;
			logger.debug("OUT: JNDI name is [" + jndiName + "]");

			return jndiName;

		} else {
			// For regular datasource (one that is NOT multischema) we can consider that provided JNDI string is the final one
			return this.getJndi();
		}

	}

	@Override
	public String getSignature(IEngUserProfile profile) {
		String toReturn;
		if (this.checkIsJndi()) {
			toReturn = this.getJNDIRunTime(profile);
		} else {
			toReturn = this.getUser() + "_" + this.getUrlConnection();
		}
		return toReturn;
	}

	/**
	 * Get the connection using jdbc.
	 *
	 * @return Connection to database
	 *
	 * @throws ClassNotFoundException the class not found exception
	 * @throws SQLException           the SQL exception
	 */
	@JsonIgnore
	private Connection getDirectConnection() throws ClassNotFoundException, SQLException {
		return DataSourceManager.getConnection(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getDsId()
	 */
	@Override
	public int getDsId() {
		return dsId;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setDsId(int)
	 */
	@Override
	public void setDsId(int dsId) {
		this.dsId = dsId;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getDescr()
	 */
	@Override
	public String getDescr() {
		return descr;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setDescr(java.lang.String)
	 */
	@Override
	public void setDescr(String descr) {
		this.descr = descr;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setLabel(java.lang.String)
	 */
	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getJndi()
	 */
	@Override
	public String getJndi() {
		return jndi;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setJndi(java.lang.String)
	 */
	@Override
	public void setJndi(String jndi) {
		this.jndi = jndi;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getUrlConnection()
	 */
	@Override
	public String getUrlConnection() {
		return urlConnection;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setUrlConnection(java. lang.String)
	 */
	@Override
	public void setUrlConnection(String url_connection) {
		this.urlConnection = url_connection;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getUser()
	 */
	@Override
	public String getUser() {
		return user;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setUser(java.lang.String)
	 */
	@Override
	public void setUser(String user) {
		this.user = user;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getPwd()
	 */
	@Override
	public String getPwd() {
		return pwd;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setPwd(java.lang.String)
	 */
	@Override
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getDriver()
	 */
	@Override
	public String getDriver() {
		return driver;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setDriver(java.lang.String )
	 */
	@Override
	public void setDriver(String driver) {
		this.driver = driver;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getDialectName()
	 */
	@Override
	public String getDialectName() {
		return dialectName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setDialectName(java.lang.String)
	 */
	@Override
	public void setDialectName(String dialectName) {
		this.dialectName = dialectName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getEngines()
	 */
	@Override
	@JsonIgnore
	public Set getEngines() {
		return engines;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setEngines(java.util.Set)
	 */
	@Override
	public void setEngines(Set engines) {
		this.engines = engines;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getObjects()
	 */
	@Override
	@JsonIgnore
	public Set getObjects() {
		return objects;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setObjects(java.util.Set)
	 */
	@Override
	public void setObjects(Set objects) {
		this.objects = objects;
	}

	@Override
	public String getHibDialectClass() {
		return hibDialectClass;
	}

	@Override
	public void setHibDialectClass(String hibDialectClass) {
		this.hibDialectClass = hibDialectClass;
	}

	@Override
	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public void setWriteDefault(Boolean writeDefault) {
		this.writeDefault = writeDefault;
	}

	@Override
	public void setUseForDataprep(Boolean useForDataprep) {
		this.useForDataprep = useForDataprep;
	}

	@Override
	public Boolean checkIsReadOnly() {
		if (readOnly == null)
			readOnly = true;
		return readOnly;
	}

	@Override
	public Boolean checkUseForDataprep() {
		if (useForDataprep == null)
			useForDataprep = false;
		return useForDataprep;
	}

	@Override
	public Boolean checkIsWriteDefault() {
		if (writeDefault == null)
			writeDefault = false;
		return writeDefault;
	}

	@Override
	public IDataStore executeStatement(String statement, Integer start, Integer limit) {
		return executeStatement(statement, start, limit, -1, true);
	}

	@Override
	public IDataStore executeStatement(String statement, Integer start, Integer limit, boolean calculateTotalResultsNumber) {
		return executeStatement(statement, start, limit, -1, calculateTotalResultsNumber);
	}

	@Override
	public IDataStore executeStatement(String statement, Integer start, Integer limit, Integer maxRowCount, boolean calculateTotalResultsNumber) {
		IDataSet dataSet = JDBCDatasetFactory.getJDBCDataSet(this);
		return executeStatement(dataSet, statement, start, limit, maxRowCount, calculateTotalResultsNumber);
	}

	@Override
	public IDataStore executeStatement(SelectQuery selectQuery, Integer start, Integer limit, Integer maxRowCount, boolean calculateTotalResultsNumber)
			throws DataBaseException {
		IDataSet dataSet = JDBCDatasetFactory.getJDBCDataSet(this);
		((AbstractJDBCDataset) dataSet).setSelectQuery(selectQuery);
		return executeStatement(dataSet, selectQuery.toSql(this), start, limit, maxRowCount, calculateTotalResultsNumber);
	}

	private IDataStore executeStatement(IDataSet dataSet, String statement, Integer start, Integer limit, Integer maxRowCount,
			boolean calculateTotalResultsNumber) {
		logger.debug("IN: Statement is [" + statement + "], start = [" + start + "], limit = [" + limit + "], maxResults = [" + maxRowCount
				+ "], calculateTotalResultsNumber = [" + calculateTotalResultsNumber + "]");
		dataSet.setDataSource(this);
		((AbstractJDBCDataset) dataSet).setQuery(statement); // all datasets retrieved by the factory extend AbstractJDBCDataset
		((AbstractJDBCDataset) dataSet).setCalculateResultNumberOnLoad(calculateTotalResultsNumber);
		if (start == null && limit == null && maxRowCount == null) {
			dataSet.loadData();
		} else {
			dataSet.loadData(start, limit, maxRowCount);
		}
		IDataStore dataStore = dataSet.getDataStore();
		logger.debug("Data store retrieved successfully");
		logger.debug("OUT");
		return dataStore;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descr == null) ? 0 : descr.hashCode());
		result = prime * result + ((dialectName == null) ? 0 : dialectName.hashCode());
		result = prime * result + ((driver == null) ? 0 : driver.hashCode());
		result = prime * result + dsId;
		result = prime * result + ((engines == null) ? 0 : engines.hashCode());
		result = prime * result + ((hibDialectClass == null) ? 0 : hibDialectClass.hashCode());
		result = prime * result + ((jndi == null) ? 0 : jndi.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((multiSchema == null) ? 0 : multiSchema.hashCode());
		result = prime * result + ((objects == null) ? 0 : objects.hashCode());
		result = prime * result + ((pwd == null) ? 0 : pwd.hashCode());
		result = prime * result + ((readOnly == null) ? 0 : readOnly.hashCode());
		result = prime * result + ((schemaAttribute == null) ? 0 : schemaAttribute.hashCode());
		result = prime * result + ((urlConnection == null) ? 0 : urlConnection.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + ((writeDefault == null) ? 0 : writeDefault.hashCode());
		result = prime * result + ((useForDataprep == null) ? 0 : useForDataprep.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DataSource))
			return false;
		DataSource other = (DataSource) obj;
		if (descr == null) {
			if (other.descr != null)
				return false;
		} else if (!descr.equals(other.descr))
			return false;
		if (dialectName == null) {
			if (other.dialectName != null)
				return false;
		} else if (!dialectName.equals(other.dialectName))
			return false;
		if (driver == null) {
			if (other.driver != null)
				return false;
		} else if (!driver.equals(other.driver))
			return false;
		if (dsId != other.dsId)
			return false;
		if (hibDialectClass == null) {
			if (other.hibDialectClass != null)
				return false;
		} else if (!hibDialectClass.equals(other.hibDialectClass))
			return false;
		if (jndi == null) {
			if (other.jndi != null)
				return false;
		} else if (!jndi.equals(other.jndi))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (multiSchema == null) {
			if (other.multiSchema != null)
				return false;
		} else if (!multiSchema.equals(other.multiSchema))
			return false;
		if (pwd == null) {
			if (other.pwd != null)
				return false;
		} else if (!pwd.equals(other.pwd))
			return false;
		if (schemaAttribute == null) {
			if (other.schemaAttribute != null)
				return false;
		} else if (!schemaAttribute.equals(other.schemaAttribute))
			return false;
		if (urlConnection == null) {
			if (other.urlConnection != null)
				return false;
		} else if (!urlConnection.equals(other.urlConnection))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	@Override
	public JDBCDataSourcePoolConfiguration getJdbcPoolConfiguration() {
		return jdbcPoolConfiguration;
	}

	@Override
	public void setJdbcPoolConfiguration(JDBCDataSourcePoolConfiguration jdbcPoolConfiguration) {
		this.jdbcPoolConfiguration = jdbcPoolConfiguration;
	}

	@Override
	public String getOwner() {
		return owner;
	}

	@Override
	public void setOwner(String owner) {
		this.owner = owner;
	}

}
