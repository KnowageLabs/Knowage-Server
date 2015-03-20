/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.transaction.dataset;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.transaction.ITransaction;

import java.sql.Connection;

import org.hibernate.Session;


/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class DataSetTransaction implements ITransaction{

	private IDataSource dataSource;

	
	public DataSetTransaction(IDataSource dataSource){
		this.dataSource = dataSource;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.transaction.ITransaction#open()
	 */
	public void open(){
		
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.transaction.ITransaction#close()
	 */
	public void close(){

	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.transaction.ITransaction#getSQLConnection()
	 */
	public java.sql.Connection getSQLConnection(){
		return null;
	}
	
	public static Connection getConnection(Session session) {
		return null;

	}
	
}

