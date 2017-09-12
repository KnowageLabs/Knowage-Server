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
package it.eng.qbe.datasource.transaction.jpa;

import it.eng.qbe.datasource.jpa.IJpaDataSource;
import it.eng.qbe.datasource.transaction.ITransaction;
import it.eng.qbe.datasource.transaction.hibernate.HibernateTransaction;

import java.sql.Connection;

import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class JPAHibernateTransaction implements ITransaction{
	
	private IJpaDataSource dataSource;
	private Session session;
	
	public JPAHibernateTransaction(IJpaDataSource dataSource){
		this.dataSource = dataSource;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.transaction.ITransaction#open()
	 */
	public void open(){
		session = ( (HibernateEntityManager) dataSource.getEntityManager()).getSession();
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.transaction.ITransaction#close()
	 */
	public void close(){
		//we use the active session so we should not close it
		//session.close();
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.transaction.ITransaction#getSQLConnection()
	 */
	public java.sql.Connection getSQLConnection(){
		return getConnection(this.session);
	}
	
	public static Connection getConnection(Session session) {
		return HibernateTransaction.getConnection(session);
	}
	
}