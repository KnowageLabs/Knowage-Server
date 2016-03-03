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
package it.eng.spagobi.behaviouralmodel.lov.bo;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dbaccess.Utils;
import it.eng.spago.dbaccess.sql.DataConnection;
import it.eng.spago.dbaccess.sql.DataRow;
import it.eng.spago.dbaccess.sql.SQLCommand;
import it.eng.spago.dbaccess.sql.mappers.SQLMapper;
import it.eng.spago.dbaccess.sql.result.DataResult;
import it.eng.spago.dbaccess.sql.result.ScrollableDataResult;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.naming.NamingException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

//import it.eng.spagobi.commons.utilities.DataSourceUtilities;

/**
 * Defines the <code>QueryDetail</code> objects. This object is used to store
 * Query Wizard detail information.
 */
public class QueryDetail implements ILovDetail {
	private static transient Logger logger = Logger.getLogger(QueryDetail.class);

	private String dataSource = "";
	private String queryDefinition = "";

	private List visibleColumnNames = null;
	private String valueColumnName = "";
	private String descriptionColumnName = "";
	private List invisibleColumnNames = null;
	private String databaseDialect = null;
	private List treeLevelsColumns = null;
	private String lovType = "simple";

	private static String ALIAS_DELIMITER = null;
	private static String VALUE_ALIAS = "VALUE";
	private static String DESCRIPTION_ALIAS = "DESCRIPTION";

	public static final String DIALECT_MYSQL = "org.hibernate.dialect.MySQLInnoDBDialect";
	public static final String DIALECT_POSTGRES = "org.hibernate.dialect.PostgreSQLDialect";
	public static final String DIALECT_ORACLE = "org.hibernate.dialect.OracleDialect";
	public static final String DIALECT_HSQL = "org.hibernate.dialect.HSQLDialect";
	public static final String DIALECT_ORACLE9i10g = "org.hibernate.dialect.Oracle9Dialect";
	public static final String DIALECT_SQLSERVER = "org.hibernate.dialect.SQLServerDialect";
	public static final String DIALECT_INGRES = "org.hibernate.dialect.IngresDialect";
	public static final String DIALECT_TERADATA = "org.hibernate.dialect.TeradataDialect";

	/**
	 * constructor.
	 */
	public QueryDetail() {
	}

	/**
	 * constructor.
	 * 
	 * @param dataDefinition
	 *            the xml representation of the lov
	 * 
	 * @throws SourceBeanException
	 *             the source bean exception
	 */
	public QueryDetail(String dataDefinition) throws SourceBeanException {
		loadFromXML(dataDefinition);
	}

	/**
	 * loads the lov from an xml string.
	 * 
	 * @param dataDefinition
	 *            the xml definition of the lov
	 * 
	 * @throws SourceBeanException
	 *             the source bean exception
	 */
	@Override
	public void loadFromXML(String dataDefinition) throws SourceBeanException {
		logger.debug("IN");
		dataDefinition.trim();
		if (dataDefinition.indexOf("<STMT>") != -1) {
			int startInd = dataDefinition.indexOf("<STMT>");
			int endId = dataDefinition.indexOf("</STMT>");
			String query = dataDefinition.substring(startInd + 6, endId);
			query = query.trim();
			if (!query.startsWith("<![CDATA[")) {
				query = "<![CDATA[" + query + "]]>";
				dataDefinition = dataDefinition.substring(0, startInd + 6) + query + dataDefinition.substring(endId);
			}
		}

		SourceBean source = SourceBean.fromXMLString(dataDefinition);
		SourceBean connection = (SourceBean) source.getAttribute("CONNECTION");
		String dataSource = connection.getCharacters();
		SourceBean statement = (SourceBean) source.getAttribute("STMT");
		String queryDefinition = statement.getCharacters();
		SourceBean valCol = (SourceBean) source.getAttribute("VALUE-COLUMN");
		String valueColumn = valCol.getCharacters();
		SourceBean visCol = (SourceBean) source.getAttribute("VISIBLE-COLUMNS");
		String visibleColumns = visCol.getCharacters();
		SourceBean invisCol = (SourceBean) source.getAttribute("INVISIBLE-COLUMNS");
		String invisibleColumns = "";
		// compatibility control (versions till 1.9RC does not have invisible
		// columns definition)
		if (invisCol != null) {
			invisibleColumns = invisCol.getCharacters();
			if (invisibleColumns == null) {
				invisibleColumns = "";
			}
		}
		SourceBean descCol = (SourceBean) source.getAttribute("DESCRIPTION-COLUMN");
		String descriptionColumn = null;
		// compatibility control (versions till 1.9.1 does not have description
		// columns definition)
		if (descCol != null) {
			descriptionColumn = descCol.getCharacters();
			if (descriptionColumn == null) {
				descriptionColumn = valueColumn;
			}
		} else {
			descriptionColumn = valueColumn;
		}
		setDataSource(dataSource);
		setQueryDefinition(queryDefinition);
		setValueColumnName(valueColumn);
		setDescriptionColumnName(descriptionColumn);
		List visColNames = new ArrayList();
		if ((visibleColumns != null) && !visibleColumns.trim().equalsIgnoreCase("")) {
			String[] visColArr = visibleColumns.split(",");
			visColNames = Arrays.asList(visColArr);
		}
		setVisibleColumnNames(visColNames);
		List invisColNames = new ArrayList();
		if ((invisibleColumns != null) && !invisibleColumns.trim().equalsIgnoreCase("")) {
			String[] invisColArr = invisibleColumns.split(",");
			invisColNames = Arrays.asList(invisColArr);
		}
		setInvisibleColumnNames(invisColNames);
		// compatibility control (versions till 3.6 does not have
		// TREE-LEVELS-COLUMN definition)
		SourceBean treeLevelsColumnsBean = (SourceBean) source.getAttribute("TREE-LEVELS-COLUMNS");
		String treeLevelsColumnsString = null;
		if (treeLevelsColumnsBean != null) {
			treeLevelsColumnsString = treeLevelsColumnsBean.getCharacters();
		}
		if ((treeLevelsColumnsString != null) && !treeLevelsColumnsString.trim().equalsIgnoreCase("")) {
			String[] treeLevelsColumnArr = treeLevelsColumnsString.split(",");
			this.treeLevelsColumns = Arrays.asList(treeLevelsColumnArr);
		}
		SourceBean lovTypeBean = (SourceBean) source.getAttribute("LOVTYPE");
		String lovType;
		if (lovTypeBean != null) {
			lovType = lovTypeBean.getCharacters();
			this.lovType = lovType;
		}
		logger.debug("OUT");
	}

	/**
	 * serialize the lov to an xml string.
	 * 
	 * @return the serialized xml string
	 */
	@Override
	public String toXML() {
		String XML = "<QUERY>" + "<CONNECTION>" + this.getDataSource() + "</CONNECTION>" + "<STMT>" + this.getQueryDefinition() + "</STMT>" + "<VALUE-COLUMN>"
				+ this.getValueColumnName() + "</VALUE-COLUMN>" + "<DESCRIPTION-COLUMN>" + this.getDescriptionColumnName() + "</DESCRIPTION-COLUMN>"
				+ "<VISIBLE-COLUMNS>" + GeneralUtilities.fromListToString(this.getVisibleColumnNames(), ",") + "</VISIBLE-COLUMNS>" + "<INVISIBLE-COLUMNS>"
				+ GeneralUtilities.fromListToString(this.getInvisibleColumnNames(), ",") + "</INVISIBLE-COLUMNS>" + "<LOVTYPE>" + this.getLovType()
				+ "</LOVTYPE>" + "<TREE-LEVELS-COLUMNS>" + GeneralUtilities.fromListToString(this.getTreeLevelsColumns(), ",") + "</TREE-LEVELS-COLUMNS>"
				+ "</QUERY>";
		return XML;
	}

	/**
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getLovResult(
	 *      IEngUserProfile profile, List<ObjParuse> dependencies,
	 *      ExecutionInstance executionInstance) throws Exception;
	 */
	@Override
	public String getLovResult(IEngUserProfile profile, List<ObjParuse> dependencies, List<BIObjectParameter> BIObjectParameters, Locale locale)
			throws Exception {
		logger.debug("IN");
		String statement = getWrappedStatement(dependencies, BIObjectParameters);
		statement = StringUtilities.substituteProfileAttributesInString(statement, profile);
		logger.info("User [" + ((UserProfile) profile).getUserId() + "] is executing sql: " + statement);
		String result = getLovResult(profile, statement);
		logger.debug("OUT.result=" + result);
		return result;
	}

	/**
	 * This methods builds the in-line view that filters the original lov using
	 * the dependencies. For example, suppose the lov definition is SELECT
	 * country, state_province, city FROM REGION and there is a dependency that
	 * set country to be "USA", this method returns SELECT * FROM (SELECT
	 * country, state_province, city FROM REGION) T WHERE ( country = 'USA' )
	 * 
	 * @param dependencies
	 *            The dependencies' configuration to be considered into the
	 *            query
	 * @param executionInstance
	 *            The execution instance (useful to retrieve dependencies
	 *            values)
	 * @return the in-line view that filters the original lov using the
	 *         dependencies.
	 */
	public String getWrappedStatement(List<ObjParuse> dependencies, List<BIObjectParameter> BIObjectParameters) {
		logger.debug("IN");
		String result = getQueryDefinition();
		if (dependencies != null && dependencies.size() > 0 && BIObjectParameters != null) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("SELECT * FROM (" + getQueryDefinition() + ") LovTableForCache ");
			buildWhereClause(buffer, dependencies, BIObjectParameters);
			result = buffer.toString();
		}
		logger.debug("OUT.result=" + result);
		return result;
	}

	private String getRandomAlias() {
		return StringUtilities.getRandomString(8);
	}

	/**
	 * This method builds the WHERE clause for the wrapped statement (the
	 * statement that adds filters for correlations/dependencies) See
	 * getWrappedStatement method.
	 * 
	 * @param buffer
	 *            The String buffer that contains query definition
	 * @param dependencies
	 *            The dependencies configuration
	 * @param executionInstance
	 *            The execution instance
	 */
	private void buildWhereClause(StringBuffer buffer, List<ObjParuse> dependencies, List<BIObjectParameter> BIObjectParameters) {
		buffer.append(" WHERE ");
		if (dependencies.size() == 1) {
			ObjParuse dependency = dependencies.get(0);
			addFilter(buffer, dependency, BIObjectParameters);
		} else if (dependencies.size() == 2) {
			ObjParuse leftPart = dependencies.get(0);
			ObjParuse rightPart = dependencies.get(1);
			String lo = leftPart.getLogicOperator();
			addFilter(buffer, leftPart, BIObjectParameters);
			buffer.append(" " + lo + " ");
			addFilter(buffer, rightPart, BIObjectParameters);
		} else {
			// build the expression
			Iterator iterOps = dependencies.iterator();
			while (iterOps.hasNext()) {
				ObjParuse op = (ObjParuse) iterOps.next();
				if (op.getPreCondition() != null) {
					buffer.append(" " + op.getPreCondition() + " ");
				}
				addFilter(buffer, op, BIObjectParameters);
				if (op.getPostCondition() != null) {
					buffer.append(" " + op.getPostCondition() + " ");
				}
				if (op.getLogicOperator() != null) {
					buffer.append(" " + op.getLogicOperator() + " ");
				}
			}
		}
	}

	/**
	 * This methods adds a single filter based on the input dependency's
	 * configuration. See buildWhereClause and getWrappedStatement methods.
	 * 
	 * @param buffer
	 *            The String buffer that contains query definition
	 * @param dependency
	 *            The dependency's configuration
	 * @param executionInstance
	 *            The execution instance
	 */
	private void addFilter(StringBuffer buffer, ObjParuse dependency, List<BIObjectParameter> BIObjectParameters) {
		String operator = findOperator(dependency, BIObjectParameters);
		String value = findValue(dependency, BIObjectParameters);
		if (value != null) {
			buffer.append(" ( ");
			buffer.append(getColumnSQLName(dependency.getFilterColumn()));
			buffer.append(" " + operator + " ");
			buffer.append(" " + value + " ");
			buffer.append(" ) ");
		} else {
			buffer.append(" ( 1 = 1 ) "); // in case a filter has no value, add
											// a TRUE condition
		}
	}

	private String getColumnSQLName(String columnName) {
		if (columnName.contains(" ")) {
			return ALIAS_DELIMITER + columnName + ALIAS_DELIMITER;
		} else {
			return columnName;
		}
	}

	/**
	 * Finds the value to be used into the dependency's filter.
	 * 
	 * @param dependency
	 *            The dependency's configuration
	 * @param executionInstance
	 *            The execution instance
	 * @return the value to be used in the wrapped statement
	 */
	private String findValue(ObjParuse dependency, List<BIObjectParameter> BIObjectParameters) {
		String typeFilter = dependency.getFilterOperation();
		BIObjectParameter fatherPar = getFatherParameter(dependency, BIObjectParameters);
		List values = fatherPar.getParameterValues();
		if (values == null || values.isEmpty() || (values.size() == 1 && values.get(0).equals(""))) {
			return null;
		}
		String firstValue = (String) values.get(0);
		if (typeFilter.equalsIgnoreCase(SpagoBIConstants.START_FILTER)) {
			return getSQLValue(fatherPar, firstValue + "%");
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.END_FILTER)) {
			return getSQLValue(fatherPar, "%" + firstValue);
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.CONTAIN_FILTER)) {
			return getSQLValue(fatherPar, "%" + firstValue + "%");
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.EQUAL_FILTER)) {
			if (values.size() > 1) {
				return "(" + concatenateValues(fatherPar, values) + ")";
			} else {
				return getSQLValue(fatherPar, firstValue);
			}
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.LESS_FILTER)) {
			return getSQLValue(fatherPar, firstValue);
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.LESS_OR_EQUAL_FILTER)) {
			return getSQLValue(fatherPar, firstValue);
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.GREATER_FILTER)) {
			return getSQLValue(fatherPar, firstValue);
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.GREATER_OR_EQUAL_FILTER)) {
			return getSQLValue(fatherPar, firstValue);
		} else {
			logger.error("Filter operator not supported: [" + typeFilter + "]");
			throw new SpagoBIRuntimeException("Filter operator not supported: [" + typeFilter + "]");
		}
	}

	/**
	 * Concatenates values by ','
	 * 
	 * @param biparam
	 *            The BIObjectParameter in the dependency
	 * @param values
	 *            The values to be concatenated
	 * @return the values concatenated by ','
	 */
	private String concatenateValues(BIObjectParameter biparam, List values) {
		StringBuffer buffer = new StringBuffer();
		Iterator it = values.iterator();
		while (it.hasNext()) {
			String aValue = (String) it.next();
			buffer.append(getSQLValue(biparam, aValue));
			if (it.hasNext()) {
				buffer.append(",");
			}
		}
		return buffer.toString();
	}

	/**
	 * Finds the suitable SQL value for the input value. A number is not
	 * changed. A String is surrounded by single-quotes. A date is put inside a
	 * database-dependent function. The date must respect the format returned by
	 * GeneralUtilities.getServerDateFormat() Input values are validated.
	 * 
	 * @param biparam
	 *            The BIObjectParameter in the dependency
	 * @param value
	 *            The value of the parameter
	 * @return the SQL value suitable for the input value
	 */
	private String getSQLValue(BIObjectParameter biparam, String value) {
		String parameterType = biparam.getParameter().getType();
		if (parameterType.equals(SpagoBIConstants.NUMBER_TYPE_FILTER)) {
			validateNumber(value);
			return value;
		} else if (parameterType.equals(SpagoBIConstants.STRING_TYPE_FILTER)) {
			return "'" + escapeString(value) + "'";
		} else if (parameterType.equals(SpagoBIConstants.DATE_TYPE_FILTER)) {
			validateDate(value);
			String dialect = getDataSourceDialect();
			String toReturn = composeStringToDt(dialect, value);
			return toReturn;
		} else {
			logger.error("Parameter type not supported: [" + parameterType + "]");
			throw new SpagoBIRuntimeException("Parameter type not supported: [" + parameterType + "]");
		}
	}

	private void validateNumber(String value) {
		if (!(GenericValidator.isInt(value) || GenericValidator.isFloat(value) || GenericValidator.isDouble(value) || GenericValidator.isShort(value) || GenericValidator
				.isLong(value))) {
			throw new SecurityException("Input value " + value + " is not a valid number");
		}
	}

	private void validateDate(String value) {
		String dateFormat = GeneralUtilities.getServerDateFormat();
		String timestampFormat = GeneralUtilities.getServerTimeStampFormat();
		if (!GenericValidator.isDate(value, dateFormat, true) && !GenericValidator.isDate(value, timestampFormat, true)) {
			throw new SecurityException("Input value " + value + " is not a valid date according to the date format " + dateFormat + " or timestamp format "
					+ timestampFormat);
		}
	}

	private String getDataSourceDialect() {
		return databaseDialect;
	}

	private void setDataSourceDialect() {
		SpagoBiDataSource ds = getDataSourceByLabel(dataSource);
		if (ds != null) {
			databaseDialect = ds.getHibDialectClass();
			if (databaseDialect.equalsIgnoreCase(DIALECT_MYSQL)) {
				ALIAS_DELIMITER = "`";
			} else if (databaseDialect.equalsIgnoreCase(DIALECT_HSQL)) {
				ALIAS_DELIMITER = "\"";
			} else if (databaseDialect.equalsIgnoreCase(DIALECT_INGRES)) {
				ALIAS_DELIMITER = "\""; // TODO check it!!!!
			} else if (databaseDialect.equalsIgnoreCase(DIALECT_ORACLE)) {
				ALIAS_DELIMITER = "\"";
			} else if (databaseDialect.equalsIgnoreCase(DIALECT_ORACLE9i10g)) {
				ALIAS_DELIMITER = "\"";
			} else if (databaseDialect.equalsIgnoreCase(DIALECT_POSTGRES)) {
				ALIAS_DELIMITER = "\"";
			} else if (databaseDialect.equalsIgnoreCase(DIALECT_SQLSERVER)) {
				ALIAS_DELIMITER = ""; // TODO check it!!!!
			} else if (databaseDialect.equalsIgnoreCase(DIALECT_TERADATA)) {
				ALIAS_DELIMITER = "\"";
			} else {
				logger.error("Cannot determine alias delimiter since the database dialect is not set or not recognized!! Using empty string as alias delimiter");
				ALIAS_DELIMITER = "";
			}
		}
	}

	private String escapeString(String value) {
		if (value == null) {
			return null;
		}
		return StringEscapeUtils.escapeSql(value);
	}

	private String composeStringToDt(String dialect, String date) {
		String toReturn = "";
		date = escapeString(date); // for security reasons
		if (dialect != null) {
			if (dialect.equalsIgnoreCase(DIALECT_MYSQL)) {
				if (date.startsWith("'") && date.endsWith("'")) {
					toReturn = " STR_TO_DATE(" + date + ",'%d/%m/%Y %h:%i:%s') ";
				} else {
					toReturn = " STR_TO_DATE('" + date + "','%d/%m/%Y %h:%i:%s') ";
				}
			} else if (dialect.equalsIgnoreCase(DIALECT_HSQL)) {
				try {
					DateFormat df;
					if (date.startsWith("'") && date.endsWith("'")) {
						df = new SimpleDateFormat("'dd/MM/yyyy HH:mm:SS'");
					} else {
						df = new SimpleDateFormat("dd/MM/yyyy HH:mm:SS");
					}

					Date myDate = df.parse(date);
					df = new SimpleDateFormat("yyyy-MM-dd");
					toReturn = "'" + df.format(myDate) + "'";

				} catch (Exception e) {
					toReturn = "'" + date + "'";
				}

			} else if (dialect.equalsIgnoreCase(DIALECT_INGRES)) {
				if (date.startsWith("'") && date.endsWith("'")) {
					toReturn = " STR_TO_DATE(" + date + ",'%d/%m/%Y') ";
				} else {
					toReturn = " STR_TO_DATE('" + date + "','%d/%m/%Y') ";
				}
			} else if (dialect.equalsIgnoreCase(DIALECT_ORACLE)) {
				if (date.startsWith("'") && date.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + date + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + date + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equalsIgnoreCase(DIALECT_ORACLE9i10g)) {
				if (date.startsWith("'") && date.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + date + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + date + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equalsIgnoreCase(DIALECT_POSTGRES)) {
				if (date.startsWith("'") && date.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + date + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + date + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equalsIgnoreCase(DIALECT_SQLSERVER)) {
				if (date.startsWith("'") && date.endsWith("'")) {
					toReturn = date;
				} else {
					toReturn = "'" + date + "'";
				}
			} else if (dialect.equalsIgnoreCase(DIALECT_TERADATA)) {
				if (date.startsWith("'") && date.endsWith("'")) {
					toReturn = " CAST(" + date + " AS DATE FORMAT 'dd/mm/yyyy') ";
				} else {
					toReturn = " CAST('" + date + "' AS DATE FORMAT 'dd/mm/yyyy') ";
				}
			}
		}

		return toReturn;
	}

	/**
	 * Finds the suitable operator for the input dependency.
	 * 
	 * @param dependency
	 *            The dependency's configuration
	 * @param executionInstance
	 *            The Execution instance
	 * @return the suitable operator for the input dependency
	 */
	private String findOperator(ObjParuse dependency, List<BIObjectParameter> BIObjectParameters) {
		String typeFilter = dependency.getFilterOperation();
		if (typeFilter.equalsIgnoreCase(SpagoBIConstants.START_FILTER)) {
			return "LIKE";
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.END_FILTER)) {
			return "LIKE";
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.CONTAIN_FILTER)) {
			return "LIKE";
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.EQUAL_FILTER)) {
			BIObjectParameter fatherPar = getFatherParameter(dependency, BIObjectParameters);
			List values = fatherPar.getParameterValues();
			if (values != null && values.size() > 1) {
				return "IN";
			} else {
				return "=";
			}
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.LESS_FILTER)) {
			return "<";
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.LESS_OR_EQUAL_FILTER)) {
			return "<=";
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.GREATER_FILTER)) {
			return ">";
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.GREATER_OR_EQUAL_FILTER)) {
			return ">=";
		} else {
			logger.error("Filter operator not supported: [" + typeFilter + "]");
			throw new SpagoBIRuntimeException("Filter operator not supported: [" + typeFilter + "]");
		}
	}

	private BIObjectParameter getFatherParameter(ObjParuse dependency, List<BIObjectParameter> BIObjectParameters) {
		Integer fatherId = dependency.getObjParFatherId();
		Iterator it = BIObjectParameters.iterator();
		while (it.hasNext()) {
			BIObjectParameter temp = (BIObjectParameter) it.next();
			if (temp.getId().equals(fatherId)) {
				return temp;
			}
		}
		return null;
	}

	/**
	 * Gets the values and return them as an xml structure
	 * 
	 * @param statement
	 *            the query statement to execute
	 * @return the xml string containing values
	 * @throws Exception
	 */

	private String getLovResult(IEngUserProfile profile, String statement) throws Exception {
		String resStr = null;
		DataConnection dataConnection = null;
		SQLCommand sqlCommand = null;
		DataResult dataResult = null;
		try {
			// gets connection
			Connection conn = getConnection(profile, dataSource);
			dataConnection = getDataConnection(conn);
			sqlCommand = dataConnection.createSelectCommand(statement, false);
			dataResult = sqlCommand.execute();
			ScrollableDataResult scrollableDataResult = (ScrollableDataResult) dataResult.getDataObject();
			SourceBean result = scrollableDataResult.getSourceBean();
			resStr = result.toXML(false);
			resStr = resStr.trim();
			if (resStr.startsWith("<?")) {
				resStr = resStr.substring(2);
				int indFirstTag = resStr.indexOf("<");
				resStr = resStr.substring(indFirstTag);
			}
		} finally {
			Utils.releaseResources(dataConnection, sqlCommand, dataResult);
		}
		return resStr;
	}

	/**
	 * This methods find out if the input parameters' values are admissible for
	 * this QueryDetail instance, i.e. if the values are contained in the query
	 * result.
	 * 
	 * @param profile
	 *            The user profile
	 * @param biparam
	 *            The BIObjectParameter with the values that must be validated
	 * @return a list of errors: it is empty if all values are admissible,
	 *         otherwise it will contain a EMFUserError for each wrong value
	 * @throws Exception
	 */
	public List validateValues(IEngUserProfile profile, BIObjectParameter biparam) throws Exception {
		List toReturn = new ArrayList();
		List<String> values = biparam.getParameterValues();
		List parameterValuesDescription = new ArrayList();
		DataConnection dataConnection = null;
		SQLCommand sqlCommand = null;
		DataResult dataResult = null;
		String statement = null;
		SourceBean result = null;
		try {
			statement = getValidationQuery(profile, biparam, values);
			logger.debug("Executing validation statement [" + statement + "] ...");
			// gets connection
			Connection conn = getConnection(profile, dataSource);
			dataConnection = getDataConnection(conn);
			sqlCommand = dataConnection.createSelectCommand(statement, false);
			dataResult = sqlCommand.execute();
			ScrollableDataResult scrollableDataResult = (ScrollableDataResult) dataResult.getDataObject();
			result = scrollableDataResult.getSourceBean();
		} finally {
			Utils.releaseResources(dataConnection, sqlCommand, dataResult);
		}

		// START converting the SourceBean into a string and then into
		// SourceBean again:
		// this a necessary work-around (workaround, work around) because the
		// getFilteredSourceBeanAttribute is not able to filter on numbers!!!
		// By making this conversion, the information on data type is lost and
		// every attribute becomes a String
		String xml = result.toXML(false);
		result = SourceBean.fromXMLString(xml);
		// END converting the SourceBean into a string and then into SourceBean
		// again:

		Iterator<String> it = values.iterator();
		while (it.hasNext()) {
			String description = null;
			String aValue = it.next();
			Object obj = result.getFilteredSourceBeanAttribute(DataRow.ROW_TAG, VALUE_ALIAS, aValue);
			if (obj == null) {
				// value was not found!!
				logger.error("Parameter '" + biparam.getLabel() + "' cannot assume value '" + aValue + "'" + " for user '"
						+ ((UserProfile) profile).getUserId().toString() + "'.");
				List l = new ArrayList();
				l.add(biparam.getLabel());
				l.add(aValue);
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 1077, l);
				toReturn.add(userError);
				description = "NOT ADMISSIBLE";
			} else {
				// value was found, retrieve description
				if (obj instanceof SourceBean) {
					SourceBean sb = (SourceBean) obj;
					Object descriptionObj = sb.getAttribute(DESCRIPTION_ALIAS);
					description = descriptionObj != null ? descriptionObj.toString() : null;
				} else {
					List l = (List) obj;
					Object descriptionObj = ((SourceBean) l.get(0)).getAttribute(DESCRIPTION_ALIAS);
					description = descriptionObj != null ? descriptionObj.toString() : null;
				}
			}
			parameterValuesDescription.add(description);
		}
		biparam.setParameterValuesDescription(parameterValuesDescription);
		return toReturn;
	}

	/**
	 * This methods builds the validation query, see validateValues method.
	 */
	private String getValidationQuery(IEngUserProfile profile, BIObjectParameter biparam, List<String> values) throws Exception {
		String statement = getQueryDefinition();
		statement = StringUtilities.substituteProfileAttributesInString(statement, profile);
		StringBuffer buffer = new StringBuffer();

		if (this.lovType.equals("treeinner")) {
			for (int i = 0; i < this.treeLevelsColumns.size(); i++) {
				String levelColumn = (String) this.treeLevelsColumns.get(i);

				buffer.append("SELECT ");
				buffer.append(getColumnSQLName(levelColumn) + " AS \"" + VALUE_ALIAS + "\" ");
				buffer.append("FROM (");
				buffer.append(statement);
				buffer.append(") " + getRandomAlias() + " WHERE ");

				if (values.size() == 1) {
					buffer.append(getColumnSQLName(levelColumn) + " = ");
					buffer.append(getSQLValue(biparam, values.get(0)));
				} else {
					buffer.append(getColumnSQLName(levelColumn) + " IN (");
					buffer.append(concatenateValues(biparam, values));
					buffer.append(")");
				}

				if (i + 1 < this.treeLevelsColumns.size()) {
					buffer.append(" UNION ");
				}
			}
		} else {
			buffer.append("SELECT ");
			buffer.append(getColumnSQLName(this.valueColumnName) + " AS \"" + VALUE_ALIAS + "\", ");
			buffer.append(getColumnSQLName(this.descriptionColumnName) + " AS \"" + DESCRIPTION_ALIAS + "\" ");
			buffer.append("FROM (");
			buffer.append(statement);
			buffer.append(") " + getRandomAlias() + " WHERE ");

			if (values.size() == 1) {
				buffer.append(getColumnSQLName(this.valueColumnName) + " = ");
				buffer.append(getSQLValue(biparam, values.get(0)));
			} else {
				buffer.append(getColumnSQLName(this.valueColumnName) + " IN (");
				buffer.append(concatenateValues(biparam, values));
				buffer.append(")");
			}
		}

		return buffer.toString();
	}

	/**
	 * Gets the list of names of the profile attributes required.
	 * 
	 * @return list of profile attribute names
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public List getProfileAttributeNames() throws Exception {
		List names = new ArrayList();
		String query = getQueryDefinition();
		while (query.indexOf("${") != -1) {
			int startind = query.indexOf("${");
			int endind = query.indexOf("}", startind);
			String attributeDef = query.substring(startind + 2, endind);
			if (attributeDef.indexOf("(") != -1) {
				int indroundBrack = query.indexOf("(", startind);
				String nameAttr = query.substring(startind + 2, indroundBrack);
				names.add(nameAttr);
			} else {
				names.add(attributeDef);
			}
			query = query.substring(endind);
		}
		return names;
	}

	/**
	 * Checks if the lov requires one or more profile attributes.
	 * 
	 * @return true if the lov require one or more profile attributes, false
	 *         otherwise
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public boolean requireProfileAttributes() throws Exception {
		boolean contains = false;
		String query = getQueryDefinition();
		if (query.indexOf("${") != -1) {
			contains = true;
		}
		return contains;
	}

	/**
	 * Builds a simple sourcebean
	 * 
	 * @param name
	 *            name of the sourcebean
	 * @param value
	 *            value of the sourcebean
	 * @return the sourcebean built
	 * @throws SourceBeanException
	 */
	private SourceBean buildSourceBean(String name, String value) throws SourceBeanException {
		SourceBean sb = null;
		sb = SourceBean.fromXMLString("<" + name + ">" + (value != null ? value : "") + "</" + name + ">");
		return sb;
	}

	/**
	 * Splits an XML string by using some <code>SourceBean</code> object methods
	 * in order to obtain the source <code>QueryDetail</code> objects whom XML
	 * has been built.
	 * 
	 * @param dataDefinition
	 *            The XML input String
	 * 
	 * @return The corrispondent <code>QueryDetail</code> object
	 * 
	 * @throws SourceBeanException
	 *             If a SourceBean Exception occurred
	 */
	public static QueryDetail fromXML(String dataDefinition) throws SourceBeanException {
		return new QueryDetail(dataDefinition);
	}

	/**
	 * Gets the data source.
	 * 
	 * @return the data source
	 */
	public String getDataSource() {
		return dataSource;
	}

	/**
	 * Sets the data source.
	 * 
	 * @param dataSource
	 *            the new data source
	 */
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
		setDataSourceDialect();
	}

	/**
	 * Gets the query definition.
	 * 
	 * @return the query definition
	 */
	public String getQueryDefinition() {
		return queryDefinition;
	}

	/**
	 * Sets the query definition.
	 * 
	 * @param queryDefinition
	 *            the new query definition
	 */
	public void setQueryDefinition(String queryDefinition) {
		this.queryDefinition = queryDefinition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getDescriptionColumnName
	 * ()
	 */
	@Override
	public String getDescriptionColumnName() {
		return descriptionColumnName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#setDescriptionColumnName
	 * (java.lang.String)
	 */
	@Override
	public void setDescriptionColumnName(String descriptionColumnName) {
		this.descriptionColumnName = descriptionColumnName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getInvisibleColumnNames
	 * ()
	 */
	@Override
	public List getInvisibleColumnNames() {
		return invisibleColumnNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#setInvisibleColumnNames
	 * (java.util.List)
	 */
	@Override
	public void setInvisibleColumnNames(List invisibleColumnNames) {
		this.invisibleColumnNames = invisibleColumnNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getValueColumnName()
	 */
	@Override
	public String getValueColumnName() {
		return valueColumnName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#setValueColumnName(
	 * java.lang.String)
	 */
	@Override
	public void setValueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getVisibleColumnNames()
	 */
	@Override
	public List getVisibleColumnNames() {
		return visibleColumnNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#setVisibleColumnNames
	 * (java.util.List)
	 */
	@Override
	public void setVisibleColumnNames(List visibleColumnNames) {
		this.visibleColumnNames = visibleColumnNames;
	}

	@Override
	public QueryDetail clone() {
		QueryDetail toReturn = new QueryDetail();
		toReturn.setDataSource(this.getDataSource());
		toReturn.setDescriptionColumnName(this.getDescriptionColumnName());
		List invisibleColumnNames = new ArrayList();
		invisibleColumnNames.addAll(this.getInvisibleColumnNames());
		toReturn.setInvisibleColumnNames(invisibleColumnNames);
		toReturn.setQueryDefinition(this.getQueryDefinition());
		toReturn.setValueColumnName(this.getValueColumnName());
		List visibleColumnNames = new ArrayList();
		visibleColumnNames.addAll(this.getVisibleColumnNames());
		toReturn.setVisibleColumnNames(visibleColumnNames);
		return toReturn;
	}

	@Override
	public String getLovType() {
		return lovType;
	}

	@Override
	public void setLovType(String lovType) {
		this.lovType = lovType;
	}

	@Override
	public List getTreeLevelsColumns() {
		return treeLevelsColumns;
	}

	@Override
	public void setTreeLevelsColumns(List treeLevelsColumns) {
		this.treeLevelsColumns = treeLevelsColumns;
	}

	/*
	 * Methods copied from DataSourceSupplier for DAO refactoring
	 */

	/**
	 * Gets the data source by label.
	 * 
	 * @param dsLabel
	 *            the ds label
	 * 
	 * @return the data source by label
	 */
	public SpagoBiDataSource getDataSourceByLabel(String dsLabel) {
		logger.debug("IN");
		SpagoBiDataSource sbds = new SpagoBiDataSource();

		// gets data source data from database
		try {
			IDataSource ds = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(dsLabel);
			if (ds == null) {
				logger.warn("The data source with label " + dsLabel + " is not found on the database.");
				return null;
			}
			sbds = toSpagoBiDataSource(ds);

		} catch (Exception e) {
			logger.error("The data source is not correctly returned", e);
		}
		logger.debug("OUT");
		return sbds;
	}

	private SpagoBiDataSource toSpagoBiDataSource(IDataSource ds) throws Exception {
		SpagoBiDataSource sbds = new SpagoBiDataSource();
		sbds.setLabel(ds.getLabel());
		sbds.setJndiName(ds.getJndi());
		sbds.setUrl(ds.getUrlConnection());
		sbds.setUser(ds.getUser());
		sbds.setPassword(ds.getPwd());
		sbds.setDriver(ds.getDriver());
		sbds.setMultiSchema(ds.getMultiSchema());
		sbds.setSchemaAttribute(ds.getSchemaAttribute());
		// gets dialect informations
		IDomainDAO domaindao = DAOFactory.getDomainDAO();
		Domain doDialect = domaindao.loadDomainById(ds.getDialectId());
		sbds.setHibDialectClass(doDialect.getValueCd());
		sbds.setHibDialectName(doDialect.getValueName());
		sbds.setReadOnly(ds.checkIsReadOnly());
		sbds.setWriteDefault(ds.checkIsWriteDefault());
		return sbds;
	}

	/*
	 * Methods copied from DataSourceUtilities for DAO refactoring
	 */

	/**
	 * use this method in service implementation. If RequestContainer isn't
	 * correct.
	 * 
	 * @param profile
	 * @param dsLabel
	 * @return
	 */
	public Connection getConnection(IEngUserProfile profile, String dsLabel) {
		Connection connection = null;
		// calls implementation for gets data source object

		SpagoBiDataSource ds = getDataSourceByLabel(dsLabel);
		logger.debug("Schema Attribute:" + ds.getSchemaAttribute());
		String schema = null;
		if (profile != null) {
			schema = UserUtilities.getSchema(ds.getSchemaAttribute(), profile);
			logger.debug("Schema:" + schema);
		}
		try {
			connection = ds.readConnection(schema);
		} catch (NamingException e) {
			logger.error("JNDI error", e);
		} catch (SQLException e) {
			logger.error("Cannot retrive connection", e);
		} catch (ClassNotFoundException e) {
			logger.error("Driver not found", e);
		}

		return connection;
	}

	/**
	 * Creates a ago DataConnection object starting from a sql connection.
	 * 
	 * @param con
	 *            Connection to the export database
	 * 
	 * @return The Spago DataConnection Object
	 * 
	 * @throws EMFInternalError
	 *             the EMF internal error
	 */
	public DataConnection getDataConnection(Connection con) throws EMFInternalError {
		DataConnection dataCon = null;
		try {
			Class mapperClass = Class.forName("it.eng.spago.dbaccess.sql.mappers.OracleSQLMapper");
			SQLMapper sqlMapper = (SQLMapper) mapperClass.newInstance();
			dataCon = new DataConnection(con, "2.1", sqlMapper);
		} catch (Exception e) {
			logger.error("Error while getting Data Source " + e);
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "cannot build spago DataConnection object");
		}
		return dataCon;
	}

}
