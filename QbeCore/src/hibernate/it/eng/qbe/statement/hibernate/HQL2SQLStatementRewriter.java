/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement.hibernate;


import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.hql.QueryTranslator;
import org.hibernate.hql.ast.ASTQueryTranslatorFactory;

/**
 * The Class HqlToSqlQueryRewriter.
 * 
 * @author Gioia
 */
public class HQL2SQLStatementRewriter {
	
	/** The session. */
	private Session session;
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(HQL2SQLStatementRewriter.class);
	
	
	/**
	 * Instantiates a new hql to sql query rewriter.
	 * 
	 * @param session the session
	 */
	public HQL2SQLStatementRewriter(Session session) {
		this.session = session;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.export.IQueryRewriter#rewrite(java.lang.String)
	 */
	public String rewrite(String query) {
		String sqlQuery = null;		
		logger.debug("rewrite: HQL query to convert: " + query);		
		
		
		Query hibQuery = session.createQuery(query);
		SessionFactory sessionFactory = session.getSessionFactory();
		SessionFactoryImplementor sessionFactoryImplementor = (SessionFactoryImplementor) sessionFactory;
		ASTQueryTranslatorFactory astQueryTranslatorFactory = new ASTQueryTranslatorFactory();
		QueryTranslator queryTranslator = null; 
		
		Class[] parsTypes = null;

		Method createQueryTranslatorMethod = null;
		try {
			// Hibernate 3.0
			parsTypes = new Class[3];
			parsTypes[0] = String.class;
			parsTypes[1] = Map.class;
			parsTypes[2] = SessionFactoryImplementor.class;
			
			createQueryTranslatorMethod = astQueryTranslatorFactory.getClass().getMethod("createQueryTranslator", parsTypes);
			try{
				queryTranslator = (QueryTranslator)createQueryTranslatorMethod.invoke(astQueryTranslatorFactory, new Object[]{
						hibQuery.getQueryString()
						, Collections.EMPTY_MAP
						, sessionFactoryImplementor
				});
			}catch (Throwable e) {
				e.printStackTrace();
			}
		} catch (NoSuchMethodException e) {
			
			parsTypes = new Class[4];
			
			parsTypes[0] = String.class;
			parsTypes[1] = String.class;
			parsTypes[2] = Map.class;
			parsTypes[3] = SessionFactoryImplementor.class;
			
			try{
				createQueryTranslatorMethod = astQueryTranslatorFactory.getClass().getMethod("createQueryTranslator", parsTypes); 
			
				if (createQueryTranslatorMethod != null){
					try{
						queryTranslator = (QueryTranslator)createQueryTranslatorMethod.invoke(
								astQueryTranslatorFactory, 
								new Object[]{String.valueOf(
										System.currentTimeMillis())
										, hibQuery.getQueryString()
										,Collections.EMPTY_MAP
										, sessionFactoryImplementor
								}
						);
					}catch (Throwable t) {
						t.printStackTrace();
					}
				}
			} catch (NoSuchMethodException ex) {
				e.printStackTrace();
			}
		}
		
		queryTranslator.compile(new HashMap(), false);
		sqlQuery = queryTranslator.getSQLString();
		
		logger.debug("rewrite: generated SQL query: " + sqlQuery);		
		
		return sqlQuery;
	}
	
	

}
