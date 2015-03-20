/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.transaction.hibernate;

import it.eng.qbe.datasource.hibernate.IHibernateDataSource;
import it.eng.qbe.datasource.transaction.ITransaction;

import java.sql.Connection;

import org.hibernate.Session;


/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class HibernateTransaction implements ITransaction{

	private IHibernateDataSource dataSource;
	private Session session;
	
	public HibernateTransaction(IHibernateDataSource dataSource){
		this.dataSource = dataSource;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.transaction.ITransaction#open()
	 */
	public void open(){
		session = dataSource.getHibernateSessionFactory().openSession();	
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.transaction.ITransaction#close()
	 */
	public void close(){
		//the session in new so we should close it
		session.close();
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.transaction.ITransaction#getSQLConnection()
	 */
	public java.sql.Connection getSQLConnection(){
		return getConnection(this.session);
	}
	
	public static Connection getConnection(Session session) {
		// Hibernate 3
		return session.connection();
		
		// following code for Hibernate 4
//		try {
//			// reflective lookup to bridge between Hibernate 3.x and 4.x
//			// see http://stackoverflow.com/questions/3526556/session-connection-deprecated-on-hibernate
//			Method connectionMethod = session.getClass().getMethod(
//						"connection");
//			return (Connection) connectionMethod.invoke(session);
//		} catch (NoSuchMethodException e) {
//			throw new IllegalStateException(
//					"Cannot find connection() method on Hibernate session", e);
//		} catch (IllegalArgumentException e) {
//			throw new IllegalStateException(
//					"IllegalArgumentException invoking connection() method on Hibernate session", e);
//		} catch (IllegalAccessException e) {
//			throw new IllegalStateException(
//					"IllegalAccessException invoking connection() method on Hibernate session", e);
//		} catch (InvocationTargetException e) {
//			throw new IllegalStateException(
//					"InvocationTargetException invoking connection() method on Hibernate session", e);
//		}
	}
	
}
