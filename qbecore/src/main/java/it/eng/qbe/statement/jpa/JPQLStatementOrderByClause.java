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
package it.eng.qbe.statement.jpa;

import java.util.Map;

import org.apache.log4j.Logger;

import it.eng.qbe.datasource.jpa.JPADataSource;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.AbstractStatementOrderByClause;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JPQLStatementOrderByClause extends AbstractStatementOrderByClause {

	public static transient Logger logger = Logger.getLogger(JPQLStatementOrderByClause.class);

	public static String build(JPQLStatement parentStatement, Query query, Map<String, Map<String, String>> entityAliasesMaps, boolean isSubquery) {
		JPQLStatementOrderByClause clause = new JPQLStatementOrderByClause(parentStatement);

		String orderBy = clause.buildClause(query, entityAliasesMaps);

		if (orderBy.isEmpty()) {
			JPADataSource ds = ((JPADataSource) parentStatement.getDataSource());
			String dialect = ds.getToolsDataSource().getHibDialectClass();
			if (dialect.toLowerCase().contains("oracle") && !isSubquery) {
				StringBuffer buffer;
				buffer = new StringBuffer();
				if (!dialect.equals("org.hibernatespatial.oracle.CustomOracleSpatialDialect")) {
					buffer.append(JPQLStatementConstants.STMT_KEYWORD_ORDER_BY);
					buffer.append(" 1 ");
					buffer.append(JPQLStatementConstants.STMT_KEYWORD_ASCENDING);
					orderBy = buffer.toString();
				}

			}

		}

		return orderBy;
	}

	protected JPQLStatementOrderByClause(JPQLStatement statement) {
		parentStatement = statement;
	}

}
