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
package it.eng.qbe.datasource;

/**
 * The Class DBConnection.
 * 
 * @author Andrea Gioia
 */
public class ConnectionDescriptor {
	
		// GENERAL
	 	
		/** The name. */
		private String name;
		
		/** The dialect. */
		private String dialect;
		
		// JNDI

	    /** The jndi name. */
		private String jndiName;
	    
	    // STATIC
	    
	    /** The url. */
    	private String url;	  
	    
	    /** The driver class. */
    	private String driverClass;

	    /** The password. */
    	private String password;
	    
	    /** The username. */
    	private String username;

	    
	    
		/**
		 * Gets the name.
		 * 
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Sets the name.
		 * 
		 * @param name the new name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Gets the jndi name.
		 * 
		 * @return the jndi name
		 */
		public String getJndiName() {
			return jndiName;
		}

		/**
		 * Sets the jndi name.
		 * 
		 * @param jndiName the new jndi name
		 */
		public void setJndiName(String jndiName) {
			this.jndiName = jndiName;
		}

		/**
		 * Gets the url.
		 * 
		 * @return the url
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * Sets the url.
		 * 
		 * @param url the new url
		 */
		public void setUrl(String url) {
			this.url = url;
		}

		/**
		 * Gets the password.
		 * 
		 * @return the password
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * Sets the password.
		 * 
		 * @param password the new password
		 */
		public void setPassword(String password) {
			this.password = password;
		}

		/**
		 * Gets the driver class.
		 * 
		 * @return the driver class
		 */
		public String getDriverClass() {
			return driverClass;
		}

		/**
		 * Sets the driver class.
		 * 
		 * @param driverClass the new driver class
		 */
		public void setDriverClass(String driverClass) {
			this.driverClass = driverClass;
		}

		/**
		 * Gets the username.
		 * 
		 * @return the username
		 */
		public String getUsername() {
			return username;
		}

		/**
		 * Sets the username.
		 * 
		 * @param username the new username
		 */
		public void setUsername(String username) {
			this.username = username;
		}

		/**
		 * Gets the dialect.
		 * 
		 * @return the dialect
		 */
		public String getDialect() {
			return dialect;
		}

		/**
		 * Sets the dialect.
		 * 
		 * @param dialect the new dialect
		 */
		public void setDialect(String dialect) {
			this.dialect = dialect;
		}
		
		/**
		 * Checks if is jndi conncetion.
		 * 
		 * @return true, if is jndi conncetion
		 */
		public boolean isJndiConncetion() {
			return (jndiName != null && !jndiName.trim().equals(""));
		}
		
		public it.eng.spagobi.tools.datasource.bo.IDataSource getDataSource(){
			it.eng.spagobi.tools.datasource.bo.IDataSource dataSource = new it.eng.spagobi.tools.datasource.bo.DataSource();			
			dataSource.setLabel("connection");
			dataSource.setHibDialectClass( dialect );			
			dataSource.setDriver( driverClass );			
			dataSource.setPwd( password );
			dataSource.setUrlConnection( url);
			dataSource.setUser( username );
			return dataSource;
		}

	    
}
