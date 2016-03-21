/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
  
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
  
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package it.eng.spagobi.utilities.sql;

import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia
 *
 */
public class SqlUtils {

	public static final String DIALECT_MYSQL = "org.hibernate.dialect.MySQLInnoDBDialect";
	public static final String DIALECT_TERADATA = "org.hibernate.dialect.TeradataDialect";
	public static final String DIALECT_POSTGRES = "org.hibernate.dialect.PostgreSQLDialect";
	public static final String DIALECT_ORACLE = "org.hibernate.dialect.OracleDialect";
	public static final String DIALECT_HSQL = "org.hibernate.dialect.HSQLDialect";
	public static final String DIALECT_ORACLE9i10g = "org.hibernate.dialect.Oracle9Dialect";
	public static final String DIALECT_SQLSERVER = "org.hibernate.dialect.SQLServerDialect";
	public static final String DIALECT_INGRES = "org.hibernate.dialect.IngresDialect";

	static protected Logger logger = Logger.getLogger(SqlUtils.class);

	public static boolean isSelectStatement(String query) {
		if (query == null)
			return false;
		return query.toUpperCase().trim().startsWith("SELECT");
	}

	public static String getSelectClause(String query) {
		String selectClause;

		selectClause = null;

		Assert.assertNotNull(query, "...");
		Assert.assertTrue(isSelectStatement(query), "...");

		int indexOFSelect = query.toUpperCase().indexOf("SELECT");
		int indexOFFrom = query.toUpperCase().indexOf("FROM");

		selectClause = query.substring(indexOFSelect + "SELECT".length(), indexOFFrom).trim();

		return selectClause;
	}

	public static List getSelectFields(String query) {
		return getSelectFields(query, false);
	}

	/**
	 * Get the select fields of a query
	 *
	 * @param query
	 * @param withAliasSeparator
	 *            if true remove the quotes at the beginning and end of the alias
	 * @return a list of String[2] arrays. Where array[0] is the name of the field, array[1] is the alias
	 */
	public static List getSelectFields(String query, boolean withAliasSeparator) {
		List selectFields;
		String selectClause;

		Assert.assertNotNull(query, "...");
		Assert.assertTrue(isSelectStatement(query), "...");

		selectFields = new ArrayList();
		selectClause = getSelectClause(query);
		String[] fields = selectClause.split(",");
		for (int i = 0; i < fields.length; i++) {
			String f = fields[i];
			String[] field = new String[2];
			String[] tokens = fields[i].trim().split("\\s");
			field[0] = tokens[0]; // the column name
			if (tokens.length > 1) {
				String alias = null;
				if (fields[i].endsWith("'")) {
					Pattern p = Pattern.compile("'[^']*'");
					Matcher m = p.matcher(fields[i]);
					while (m.find()) {
						alias = m.group();
						if (withAliasSeparator) {
							alias = alias.trim();
						} else {
							alias.trim().substring(1, alias.length() - 1);
						}
					}
				} else if (fields[i].endsWith("\"")) {
					Pattern p = Pattern.compile("\"[^\"]*\"");
					Matcher m = p.matcher(fields[i]);
					while (m.find()) {
						alias = m.group();
						if (withAliasSeparator) {
							alias = alias.trim();
						} else {
							alias.trim().substring(1, alias.length() - 1);
						}
					}
				} else {
					alias = tokens[tokens.length - 1];
				}
				field[1] = alias;
			}
			selectFields.add(field);
		}
		return selectFields;
	}

	public static final void main(String args[]) {
		List<String[]> results;

		String query = "   select colonna1, " + "colonna2 as Colonna2, " + "colonna3 as 'Colonna 3', " + "colonna4 as \"Colonna 4\", "
				+ "\"colonna5\" as \"Colonna 4\", " + "'colonna6' as 'Colonna 4', " + "'colonna7', " + "\"colonna8\", " + "colonna9 Colonna9, "
				+ "colonna10 'Colonna 10', " + "colonna11 \"Colonna 11\", " + "\"colonna12\" \"Colonna 12\", " + "'colonna13' 'Colonna 13' "
				+ "from table1 where colonna9 = 'pippo'";

		results = getSelectFields(query);
		for (int i = 0; i < results.size(); i++) {
			logger.debug(results.get(i)[0] + " - " + results.get(i)[1]);
		}

	}

	public static String fromObjectToString(Object obj, String dialect) {

		String toReturn = obj.toString();

		if (dialect != null) {

			if (dialect.equalsIgnoreCase(DIALECT_MYSQL)) {
				toReturn = " concat(" + toReturn + ",'') ";
			} else if (dialect.equalsIgnoreCase(DIALECT_HSQL)) {
				toReturn = " concat(" + toReturn + ",'') ";
			} else if (dialect.equalsIgnoreCase(DIALECT_INGRES)) {
				toReturn = toReturn + "||''";
			} else if (dialect.equalsIgnoreCase(DIALECT_ORACLE)) {
				toReturn = " concat(" + toReturn + ",'') ";
			} else if (dialect.equalsIgnoreCase(DIALECT_ORACLE9i10g)) {
				toReturn = " concat(" + toReturn + ",'') ";
			} else if (dialect.equalsIgnoreCase(DIALECT_POSTGRES)) {
				toReturn = toReturn + "||''";
			} else if (dialect.equalsIgnoreCase(DIALECT_SQLSERVER)) {
				toReturn = toReturn + "+''";
			} else if (dialect.equalsIgnoreCase(DIALECT_TERADATA)) {
				toReturn = toReturn + "||''";
			}
		}

		return toReturn;

	}

	public static String unQuote(String string) {
		char[] quoteSimbols = { '\"', '\'', '`' };
		String toReturn = string;

		if (toReturn != null && toReturn.length() > 1) {
			for (int i = 0; i < quoteSimbols.length; i++) {
				if (toReturn.charAt(0) == quoteSimbols[i] && toReturn.charAt(toReturn.length() - 1) == quoteSimbols[i]) {
					return toReturn.substring(1, toReturn.length() - 1);
				}
			}

		}

		return toReturn;

	}

	public static boolean isHiveLikeDialect(String dialect) {
		return (dialect.toLowerCase().contains("cassandra") || dialect.toLowerCase().contains("hive") || dialect.toLowerCase().contains("neo4j")  || 
				dialect.toLowerCase().contains("drill") || dialect.toLowerCase().contains("spark") || dialect.toLowerCase().contains("phoenix") || dialect.toLowerCase().contains("impala"));
	}


}
