/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.datasource.bo;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDatasetFactory;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;



/**
 * Defines an <code>DataSource</code> object
 *
 */

public class DataSource implements Serializable, IDataSource {
	
	private static transient Logger logger = Logger.getLogger(DataSource.class);
	
	private int dsId;
	private String descr;
	private String label;
	private String jndi;
	private String urlConnection;
	private String user;
	private String pwd;
	private String driver;
	private Integer dialectId;	
	private String hibDialectClass;
	private String hibDialectName;
	private Set engines = null;
	private Set objects = null;
    private String schemaAttribute=null;
	private Boolean multiSchema=null;
	private Boolean readOnly;
	private Boolean writeDefault;
	
	
    public String getSchemaAttribute() {
		return schemaAttribute;
	}

	public void setSchemaAttribute(String schemaAttribute) {
		this.schemaAttribute = schemaAttribute;
	}

	public Boolean getMultiSchema() {
		return multiSchema;
	}

	public void setMultiSchema(Boolean multiSchema) {
		this.multiSchema = multiSchema;
	}

	
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#toSpagoBiDataSource()
	 */
	public SpagoBiDataSource toSpagoBiDataSource(){
		SpagoBiDataSource sbd = new SpagoBiDataSource();
		sbd.setId(dsId);
		sbd.setDriver(driver);
		sbd.setHibDialectClass("");
		sbd.setHibDialectName("");
		sbd.setJndiName(jndi);
		sbd.setLabel(label);
		sbd.setPassword(pwd);
		sbd.setUrl(urlConnection);
		sbd.setUser(user);
		sbd.setHibDialectClass(hibDialectClass);
		sbd.setHibDialectName(hibDialectName);
		sbd.setMultiSchema(multiSchema);
		sbd.setSchemaAttribute(schemaAttribute);
		sbd.setReadOnly(readOnly);
		sbd.setWriteDefault(writeDefault);
		return sbd;
	}

	
	public boolean checkIsMultiSchema() {
    	return getMultiSchema() != null 
    			&& getMultiSchema().booleanValue();
    }
	
	
	public boolean checkIsJndi() {
    	return getJndi() != null 
    			&& getJndi().equals("") == false;
    }
	
	public Connection getConnection() throws NamingException, SQLException, ClassNotFoundException {
    	return getConnection(null);
    }
	
    public Connection getConnection(String schema) throws NamingException, SQLException, ClassNotFoundException {
    	Connection connection = null;
    	 
    	if( checkIsJndi() ) {
    		connection = getJndiConnection(schema);
    	} else {    		
    		connection = getDirectConnection();
    	}
    	
    	return connection;
    }
    
    
    /**
     * Get the connection from JNDI.
     * 
     * @return Connection to database
     * 
     * @throws NamingException the naming exception
     * @throws SQLException the SQL exception
     */
    private Connection getJndiConnection(String schema) throws NamingException, SQLException {
		Connection connection = null;
		
		Context ctx;		
		String jndiName;
		
		jndiName = (checkIsMultiSchema() && schema != null)? getJndi() + schema: getJndi();
		
		ctx = new InitialContext();
		javax.sql.DataSource ds = (javax.sql.DataSource) ctx.lookup( jndiName );
		connection = ds.getConnection();		
		
		return connection;
    }

    /**
     * Get the connection using jdbc.
     * 
     * @return Connection to database
     * 
     * @throws ClassNotFoundException the class not found exception
     * @throws SQLException the SQL exception
     */
    private Connection getDirectConnection() throws ClassNotFoundException, SQLException {
		Connection connection = null;
		
		try {
		Class.forName( getDriver() );
		connection = DriverManager.getConnection(getUrlConnection(), getUser(), getPwd());
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to create a direct connection to database at host [" + getUrlConnection() + "] with user [" + getUser() + "]. Check if the database is running and if the password is correct.");
		}
		return connection;
    }
	
	
	
	
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getDsId()
	 */
	public int getDsId() {
		return dsId;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setDsId(int)
	 */
	public void setDsId(int dsId) {
		this.dsId = dsId;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getDescr()
	 */
	public String getDescr() {
		return descr;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setDescr(java.lang.String)
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getLabel()
	 */
	public String getLabel() {
		return label;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setLabel(java.lang.String)
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getJndi()
	 */
	public String getJndi() {
			return jndi;		
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setJndi(java.lang.String)
	 */
	public void setJndi(String jndi) {
		this.jndi = jndi;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getUrlConnection()
	 */
	public String getUrlConnection() {
		return urlConnection;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setUrlConnection(java.lang.String)
	 */
	public void setUrlConnection(String url_connection) {
		this.urlConnection = url_connection;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getUser()
	 */
	public String getUser() {
		return user;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setUser(java.lang.String)
	 */
	public void setUser(String user) {
		this.user = user;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getPwd()
	 */
	public String getPwd() {
		return pwd;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setPwd(java.lang.String)
	 */
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getDriver()
	 */
	public String getDriver() {
		return driver;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setDriver(java.lang.String)
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getDialectId()
	 */
	public Integer getDialectId() {
		return dialectId;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setDialectId(java.lang.Integer)
	 */
	public void setDialectId(Integer dialectId) {
		this.dialectId = dialectId;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getEngines()
	 */
	public Set getEngines() {
		return engines;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setEngines(java.util.Set)
	 */
	public void setEngines(Set engines) {
		this.engines = engines;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getObjects()
	 */
	public Set getObjects() {
		return objects;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setObjects(java.util.Set)
	 */
	public void setObjects(Set objects) {
		this.objects = objects;
	}

	public String getHibDialectClass() {
		return hibDialectClass;
	}

	public void setHibDialectClass(String hibDialectClass) {
		this.hibDialectClass = hibDialectClass;
	}

	public String getHibDialectName() {
		return hibDialectName;
	}

	public void setHibDialectName(String hibDialectName) {
		this.hibDialectName = hibDialectName;
	}


	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}



	public void setWriteDefault(Boolean writeDefault) {
		this.writeDefault = writeDefault;
	}

	public Boolean checkIsReadOnly() {
		if(readOnly == null)
			readOnly = true;
		return readOnly;
	}

	public Boolean checkIsWriteDefault() {
		if(writeDefault == null)
			writeDefault = false;
		return writeDefault;
	}

	public IDataStore executeStatement(String statement, Integer start, Integer limit) {
		logger.debug("IN: Statement is [" + statement + "], start = [" + start + "], limit = [" + limit + "]");
		IDataSet dataSet = JDBCDatasetFactory.getJDBCDataSet(this);
		dataSet.setDataSource(this);
		((AbstractJDBCDataset) dataSet).setQuery(statement); // all datasets retrieved by the factory extend AbstractJDBCDataset
		if (start == null && limit == null) {
			dataSet.loadData();
		} else {
			dataSet.loadData(start, limit, -1);
		}
		IDataStore dataStore = dataSet.getDataStore();
		logger.debug("Data store retrieved successfully");
		logger.debug("OUT");
		return dataStore;
	}
	
	
	

}
