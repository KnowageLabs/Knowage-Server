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
