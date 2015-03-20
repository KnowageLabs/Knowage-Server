/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.transaction.jpa;

import it.eng.qbe.datasource.jpa.IJpaDataSource;
import it.eng.qbe.datasource.transaction.ITransaction;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class JPAEclipseLinkTransaction  implements ITransaction{
	
	private IJpaDataSource dataSource;
	
	public JPAEclipseLinkTransaction(IJpaDataSource dataSource){
		this.dataSource = dataSource;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.transaction.ITransaction#getSQLConnection()
	 */
	public java.sql.Connection getSQLConnection(){
		java.sql.Connection connection = dataSource.getEntityManager().unwrap(java.sql.Connection.class);
		return connection;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.transaction.ITransaction#open()
	 */
	public void open(){
		dataSource.getEntityManager().getTransaction().begin();
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.transaction.ITransaction#close()
	 */
	public void close(){
		dataSource.getEntityManager().getTransaction().commit();
	}

}
