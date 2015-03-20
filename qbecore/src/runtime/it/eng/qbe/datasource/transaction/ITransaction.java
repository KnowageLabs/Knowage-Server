/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.transaction;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 * This class is useful to open a connection with the db
 */
public interface ITransaction {
	
	
	/**
	 * Open the transaction.. 
	 * After this call the user can can a sql connection
	 */
	void open();
	
	/**
	 * Close the transaction.. 
	 */
	void close();
	
	/**
	 * Get a sql connection
	 */
	java.sql.Connection getSQLConnection();

}
