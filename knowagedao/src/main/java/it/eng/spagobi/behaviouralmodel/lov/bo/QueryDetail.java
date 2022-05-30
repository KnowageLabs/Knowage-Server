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

import static it.eng.spagobi.commons.constants.SpagoBIConstants.DATE_RANGE_TYPE;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

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
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;
import it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.DateRangeUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.database.IDataBase;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.objects.Couple;

//import it.eng.spagobi.commons.utilities.DataSourceUtilities;

/**
 * Defines the <code>QueryDetail</code> objects. This object is used to store Query Wizard detail information.
 */
public class QueryDetail extends AbstractLOV implements ILovDetail {
	private static transient Logger logger = Logger.getLogger(QueryDetail.class);

	public static final String TRUE_CONDITION = " ( 1 = 1 ) ";
	private String dataSource = "";
	private String queryDefinition = "";

	private List visibleColumnNames = null;
	private String valueColumnName = "";
	private String descriptionColumnName = "";
	private List invisibleColumnNames = null;
	private DatabaseDialect databaseDialect = null;
	// private List treeLevelsColumns = null;
	// each entry of the list contains the name of the column to be considered
	// as value column as first item, and the name of the column to be
	// considered as
	// description column as second item
	private List<Couple<String, String>> treeLevelsColumns = null;

	private String lovType = "simple";

	private static String ALIAS_DELIMITER = null;
	private static String VALUE_ALIAS = "VALUE";
	private static String DESCRIPTION_ALIAS = "DESCRIPTION";

	/**
	 * constructor.
	 */
	public QueryDetail() {
	}

	/**
	 * constructor.
	 *
	 * @param dataDefinition the xml representation of the lov
	 * @throws SourceBeanException the source bean exception
	 */
	public QueryDetail(String dataDefinition) throws SourceBeanException {
		loadFromXML(dataDefinition);
	}

	/**
	 * loads the lov from an xml string.
	 *
	 * @param dataDefinition the xml definition of the lov
	 * @throws SourceBeanException the source bean exception
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
			query = convertSpecialChars(query);
			if (!query.startsWith("<![CDATA[")) {
				query = "<![CDATA[" + query + "]]>";
				dataDefinition = dataDefinition.substring(0, startInd + 6) + query + dataDefinition.substring(endId);
			}
		}
		if (dataDefinition.indexOf("<decoded_STMT>") != -1) {
			int startInd = dataDefinition.indexOf("<decoded_STMT>");
			int endId = dataDefinition.indexOf("</decoded_STMT>");
			String query = dataDefinition.substring(startInd + 14, endId);
			query = query.trim();
			query = convertSpecialChars(query);
			if (!query.startsWith("<![CDATA[")) {
				query = "<![CDATA[" + query + "]]>";
				dataDefinition = dataDefinition.substring(0, startInd + 14) + query + dataDefinition.substring(endId);
			}
		}

		SourceBean source = SourceBean.fromXMLString(dataDefinition);
		SourceBean connection = (SourceBean) source.getAttribute("CONNECTION");
		String dataSource = connection.getCharacters();
		SourceBean statement = (SourceBean) source.getAttribute("STMT");
		String queryDefinition = statement.getCharacters();
		queryDefinition = StringEscapeUtils.unescapeXml(queryDefinition);
		SourceBean valCol = (SourceBean) source.getAttribute("VALUE-COLUMN");
		String valueColumn = valCol != null ? valCol.getCharacters() : null;
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

		try {
			SourceBean treeLevelsColumnsBean = (SourceBean) source.getAttribute("TREE-LEVELS-COLUMNS");
			if (treeLevelsColumnsBean != null) {

				// COMPATIBILITY OLD TREE LOV
				String treeLevelsColumnsString = treeLevelsColumnsBean.getCharacters();
				if (treeLevelsColumnsString != null && !treeLevelsColumnsString.trim().equalsIgnoreCase("")) {
					List<Couple<String, String>> levelsMap = new ArrayList<Couple<String, String>>();
					String[] valuesColumns = treeLevelsColumnsString.split(",");
					List<String> valuesColumnsList = Arrays.asList(valuesColumns);
					for (int i = 0; i < valuesColumnsList.size(); i++) {
						String aValueColumn = valuesColumnsList.get(i);
						levelsMap.add(new Couple<String, String>(aValueColumn, aValueColumn));
						// TREE LEAF
						if (i == valuesColumnsList.size() - 1) {
							this.setValueColumnName(aValueColumn);
							SourceBean descriptionSourceBean = (SourceBean) source.getAttribute("DESCRIPTION-COLUMN");
							String description = (descriptionSourceBean != null && descriptionSourceBean.getCharacters() != null)
									? descriptionSourceBean.getCharacters()
									: aValueColumn;
							this.setDescriptionColumnName(description);
						}

					}
					this.treeLevelsColumns = levelsMap;

				}

			} else {
				SourceBean valuesColumnsBean = (SourceBean) source.getAttribute("VALUE-COLUMNS");
				SourceBean descriptionColumnsBean = (SourceBean) source.getAttribute("DESCRIPTION-COLUMNS");
				if (valuesColumnsBean != null) {

					Assert.assertTrue(descriptionColumnsBean != null, "DESCRIPTION-COLUMNS tag not defined");

					List<Couple<String, String>> levelsMap = new ArrayList<Couple<String, String>>();
					String valuesColumnsStr = valuesColumnsBean.getCharacters();
					logger.debug("VALUE-COLUMNS is [" + valuesColumnsStr + "]");
					String descriptionColumnsStr = descriptionColumnsBean.getCharacters();
					logger.debug("DESCRIPTION-COLUMNS is [" + descriptionColumnsStr + "]");
					String[] valuesColumns = valuesColumnsStr.split(",");
					String[] descriptionColumns = descriptionColumnsStr.split(",");
					List<String> valuesColumnsList = Arrays.asList(valuesColumns);
					List<String> descriptionColumnsList = Arrays.asList(descriptionColumns);

					Assert.assertTrue(valuesColumnsList.size() == descriptionColumnsList.size(),
							"Value columns list and description columns list must have the same length");

					for (int i = 0; i < valuesColumnsList.size(); i++) {
						String aValueColumn = valuesColumnsList.get(i);
						String aDescriptionColumn = descriptionColumnsList.get(i);
						levelsMap.add(new Couple<String, String>(aValueColumn, aDescriptionColumn));
						// TREE LEAF
						if (i == valuesColumnsList.size() - 1) {
							this.setValueColumnName(aValueColumn);
							this.setDescriptionColumnName(aDescriptionColumn);
						}

					}
					this.treeLevelsColumns = levelsMap;
				}
			}
		} catch (Exception e) {
			logger.error("Error while reading LOV definition from XML", e);
			throw new SpagoBIRuntimeException("Error while reading LOV definition from XML", e);
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

		String XML = "<QUERY>" + "<CONNECTION>" + this.getDataSource() + "</CONNECTION>" + "<STMT>" + this.getQueryDefinition() + "</STMT>"
				+ "<VISIBLE-COLUMNS>" + GeneralUtilities.fromListToString(this.getVisibleColumnNames(), ",") + "</VISIBLE-COLUMNS>" + "<INVISIBLE-COLUMNS>"
				+ GeneralUtilities.fromListToString(this.getInvisibleColumnNames(), ",") + "</INVISIBLE-COLUMNS>" + "<LOVTYPE>" + this.getLovType()
				+ "</LOVTYPE>";
		if (this.isSimpleLovType()) {
			XML += "<VALUE-COLUMN>" + valueColumnName + "</VALUE-COLUMN>" + "<DESCRIPTION-COLUMN>" + descriptionColumnName + "</DESCRIPTION-COLUMN>";
		} else {
			XML += "<VALUE-COLUMNS>" + GeneralUtilities.fromListToString(this.getTreeValueColumns(), ",") + "</VALUE-COLUMNS>" + "<DESCRIPTION-COLUMNS>"
					+ GeneralUtilities.fromListToString(this.getTreeDescriptionColumns(), ",") + "</DESCRIPTION-COLUMNS>";
		}
		XML += "</QUERY>";
		return XML;
	}

	/**
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getLovResult(IEngUserProfile profile, List<ObjParuse> dependencies, ExecutionInstance
	 *      executionInstance) throws Exception;
	 */
	@Override
	public String getLovResult(IEngUserProfile profile, List<? extends AbstractParuse> dependencies, List<? extends AbstractDriver> drivers, Locale locale)
			throws Exception {
		return getLovResult(profile, dependencies, drivers, locale, false);
	}

	public String getLovResult(IEngUserProfile profile, List<? extends AbstractParuse> dependencies, List<? extends AbstractDriver> drivers, Locale locale,
			boolean getAllColumns) throws Exception {
		logger.debug("IN");
		Map<String, String> parameters = getParametersNameToValueMap(drivers);
		String statement = getWrappedStatement(dependencies, drivers);
		statement = StringUtilities.substituteProfileAttributesInString(statement, profile);
		if (parameters != null && !parameters.isEmpty()) {
			Map<String, String> types = getParametersNameToTypeMap(drivers);
			statement = StringUtilities.substituteParametersInString(statement, parameters, types, false);
		}
		logger.info("User [" + ((UserProfile) profile).getUserId() + "] is executing sql: " + statement);
		String result = getLovResult(profile, statement, getAllColumns);
		logger.debug("OUT.result=" + result);
		return result;
	}

	/**
	 * This methods builds the in-line view that filters the original lov using the dependencies. For example, suppose the lov definition is SELECT country,
	 * state_province, city FROM REGION and there is a dependency that set country to be "USA", this method returns SELECT * FROM (SELECT country,
	 * state_province, city FROM REGION) T WHERE ( country = 'USA' )
	 *
	 * @param dependencies      The dependencies' configuration to be considered into the query
	 * @param executionInstance The execution instance (useful to retrieve dependencies values)
	 * @return the in-line view that filters the original lov using the dependencies.
	 */
	public String getWrappedStatement(List<? extends AbstractParuse> dependencies, List<? extends AbstractDriver> drivers) {
		logger.debug("IN");
		String result = getQueryDefinition();
		if (dependencies != null && dependencies.size() > 0 && drivers != null) {
			StringBuffer buffer = new StringBuffer();
			if (result.contains("order by")) {
				int index = result.indexOf("order");
				String queryWithoutOrderBy = result.substring(0, index - 1);
				buffer.append("SELECT * FROM (" + queryWithoutOrderBy + ") LovTableForCache ");
				buildWhereClause(buffer, dependencies, drivers);
				buildOrderByClause(buffer, result, index);
			} else {
				buffer.append("SELECT * FROM (" + getQueryDefinition() + ") LovTableForCache ");
				buildWhereClause(buffer, dependencies, drivers);
			}
			result = buffer.toString();
		}
		logger.debug("OUT.result=" + result);
		return result;
	}

	private void buildOrderByClause(StringBuffer buffer, String result, int index) {
		String orderByClause = result.substring(index);
		buffer.append(orderByClause);
	}

	private String getRandomAlias() {
		return StringUtilities.getRandomString(8);
	}

	/**
	 * This method builds the WHERE clause for the wrapped statement (the statement that adds filters for correlations/dependencies) See getWrappedStatement
	 * method.
	 *
	 * @param buffer            The String buffer that contains query definition
	 * @param dependencies      The dependencies configuration
	 * @param executionInstance The execution instance
	 */
	private void buildWhereClause(StringBuffer buffer, List<? extends AbstractParuse> dependencies, List<? extends AbstractDriver> drivers) {
		buffer.append(" WHERE ");
		if (dependencies.size() == 1) {
			AbstractParuse dependency = dependencies.get(0);
			addFilter(buffer, dependency, drivers);
		} else if (dependencies.size() == 2) {
			AbstractParuse leftPart = dependencies.get(0);
			AbstractParuse rightPart = dependencies.get(1);
			String lo = leftPart.getLogicOperator();
			addFilter(buffer, leftPart, drivers);
			buffer.append(" " + lo + " ");
			addFilter(buffer, rightPart, drivers);
		} else {
			// build the expression
			Iterator iterOps = dependencies.iterator();
			while (iterOps.hasNext()) {
				AbstractParuse op = (AbstractParuse) iterOps.next();
				if (op.getPreCondition() != null) {
					buffer.append(" " + op.getPreCondition() + " ");
				}
				addFilter(buffer, op, drivers);
				if (op.getPostCondition() != null) {
					buffer.append(" " + op.getPostCondition() + " ");
				}
				if (iterOps.hasNext() && op.getLogicOperator() != null) {
					buffer.append(" " + op.getLogicOperator() + " ");
				}
			}
		}
	}

	/**
	 * This methods adds a single filter based on the input dependency's configuration. See buildWhereClause and getWrappedStatement methods.
	 *
	 * @param buffer            The String buffer that contains query definition
	 * @param dependency        The dependency's configuration
	 * @param executionInstance The execution instance
	 */
	private void addFilter(StringBuffer buffer, AbstractParuse dependency, List<? extends AbstractDriver> drivers) {
		AbstractDriver fatherParameter = getFatherParameter(dependency, drivers);
		if (isDateRange(fatherParameter)) {
			buffer.append(getDateRangeClause(dependency, fatherParameter));
			return;
		}

		String operator = findOperator(dependency, drivers);
		String value = findValue(dependency, drivers);
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

	@SuppressWarnings("rawtypes")
	protected String getDateRangeClause(AbstractParuse dependency, AbstractDriver driver) {
		Assert.assertNotNull(driver, "param must be present");
		List values = driver.getParameterValues();
		if (notContainsValue(values)) {
			return TRUE_CONDITION;
		}

		// Example: 21-10-2019_6Y
		String value = (String) values.get(0);
		String typeFilter = dependency.getFilterOperation();

		// operators
		String left = null;
		String right = null;
		String central = null;
		if (SpagoBIConstants.NOT_IN_RANGE_FILTER.equals(typeFilter)) {
			left = "<";
			right = ">";
		} else if (SpagoBIConstants.IN_RANGE_FILTER.equals(typeFilter)) {
			left = ">=";
			right = "<=";
		} else if (SpagoBIConstants.LESS_BEGIN_FILTER.equals(typeFilter) || SpagoBIConstants.LESS_END_FILTER.equals(typeFilter)) {
			central = "<";
		} else if (SpagoBIConstants.LESS_OR_EQUAL_BEGIN_FILTER.equals(typeFilter) || SpagoBIConstants.LESS_OR_EQUAL_END_FILTER.equals(typeFilter)) {
			central = "<=";
		} else if (SpagoBIConstants.GREATER_BEGIN_FILTER.equals(typeFilter) || SpagoBIConstants.GREATER_END_FILTER.equals(typeFilter)) {
			central = ">";
		} else if (SpagoBIConstants.GREATER_OR_EQUAL_BEGIN_FILTER.equals(typeFilter) || SpagoBIConstants.GREATER_OR_EQUAL_END_FILTER.equals(typeFilter)) {
			central = ">=";
		} else {
			Assert.assertUnreachable("filter not supported");
		}

		// valid format
		Date[] startEnd = DateRangeUtils.getDateRangeDates(value);
		boolean isDateFormat = isDateFormat(getDataSourceDialect());
		// formats accepted by #composeStringToDt
		String dateFormat = isDateFormat ? "dd/MM/YYYY" : "dd/MM/YYYY HH:mm:ss";
		DateFormat df = new SimpleDateFormat(dateFormat);
		Date startDate = startEnd[0];
		Date endDate = startEnd[1];
		if (central == null && !isDateFormat) {
			// add 1 day to end date because it will be transformed to timestamp
			endDate = DateRangeUtils.addDay(endDate);
			right = SpagoBIConstants.IN_RANGE_FILTER.equals(typeFilter) ? right = "<" : ">=";
		}
		String startDateS = df.format(startDate);
		String endDateS = df.format(endDate);

		// for query
		String startDateSQLValue = getSQLDateValue(startDateS, true);
		String endDateSQLValue = getSQLDateValue(endDateS, true);
		String columnSQLName = getColumnSQLName(dependency.getFilterColumn());
		String res = null;
		// result something line (column>=date start AND column<=date end)
		if (SpagoBIConstants.NOT_IN_RANGE_FILTER.equals(typeFilter) || SpagoBIConstants.IN_RANGE_FILTER.equals(typeFilter)) {
			res = String.format(" ( %s%s%s AND %s%s%s) ", columnSQLName, left, startDateSQLValue, columnSQLName, right, endDateSQLValue);
		} else if (SpagoBIConstants.LESS_BEGIN_FILTER.equals(typeFilter) || SpagoBIConstants.LESS_OR_EQUAL_BEGIN_FILTER.equals(typeFilter)
				|| SpagoBIConstants.GREATER_BEGIN_FILTER.equals(typeFilter) || SpagoBIConstants.GREATER_OR_EQUAL_BEGIN_FILTER.equals(typeFilter)) {
			res = String.format(" ( %s%s%s) ", columnSQLName, central, startDateSQLValue);
		} else if (SpagoBIConstants.LESS_END_FILTER.equals(typeFilter) || SpagoBIConstants.LESS_OR_EQUAL_END_FILTER.equals(typeFilter)
				|| SpagoBIConstants.GREATER_END_FILTER.equals(typeFilter) || SpagoBIConstants.GREATER_OR_EQUAL_END_FILTER.equals(typeFilter)) {
			res = String.format(" ( %s%s%s) ", columnSQLName, central, endDateSQLValue);
		} else {
			res = TRUE_CONDITION;
		}
		return res;
	}

	/**
	 * These dialacts use date formats. The others dialects use timestamp format
	 *
	 * @param dataSourceDialect
	 * @return
	 */
	private boolean isDateFormat(DatabaseDialect dialect) {
		return DatabaseDialect.TERADATA.equals(dialect);
	}

	@SuppressWarnings("rawtypes")
	private static boolean notContainsValue(List values) {
		return values == null || values.isEmpty() || (values.size() == 1 && values.get(0).equals(""));
	}

	public static boolean isDateRange(AbstractDriver driver) {
		if (driver != null) {
			Parameter parameter = driver.getParameter();
			return parameter != null && DATE_RANGE_TYPE.equals(parameter.getType());
		}
		return false;
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
	 * @param dependency        The dependency's configuration
	 * @param executionInstance The execution instance
	 * @return the value to be used in the wrapped statement
	 */
	private String findValue(AbstractParuse dependency, List<? extends AbstractDriver> drivers) {
		String typeFilter = dependency.getFilterOperation();
		AbstractDriver fatherPar = getFatherParameter(dependency, drivers);
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
	 * @param biparam The BIObjectParameter in the dependency
	 * @param values  The values to be concatenated
	 * @return the values concatenated by ','
	 */
	private String concatenateValues(AbstractDriver driver, List values) {
		StringBuffer buffer = new StringBuffer();
		Iterator it = values.iterator();
		while (it.hasNext()) {
			String aValue = (String) it.next();
			buffer.append(getSQLValue(driver, aValue));
			if (it.hasNext()) {
				buffer.append(",");
			}
		}
		return buffer.toString();
	}

	/**
	 * Finds the suitable SQL value for the input value. A number is not changed. A String is surrounded by single-quotes. A date is put inside a
	 * database-dependent function. The date must respect the format returned by GeneralUtilities.getServerDateFormat() Input values are validated.
	 *
	 * @param biparam The BIObjectParameter in the dependency
	 * @param value   The value of the parameter
	 * @return the SQL value suitable for the input value
	 */
	private String getSQLValue(AbstractDriver driver, String value) {
		String parameterType = driver.getParameter().getType();
		if (parameterType.equals(SpagoBIConstants.NUMBER_TYPE_FILTER)) {
			validateNumber(value);
			return value;
		} else if (parameterType.equals(SpagoBIConstants.STRING_TYPE_FILTER)) {
			return "'" + escapeString(value) + "'";
		} else if (parameterType.equals(SpagoBIConstants.DATE_TYPE_FILTER)) {
			validateDate(value);
			DatabaseDialect dialect = getDataSourceDialect();
			String toReturn = composeStringToDt(dialect, value);
			return toReturn;
		} else {
			logger.error("Parameter type not supported: [" + parameterType + "]");
			throw new SpagoBIRuntimeException("Parameter type not supported: [" + parameterType + "]");
		}
	}

	private String getSQLDateValue(String value, boolean notValidate) {
		if (!notValidate) {
			validateDate(value);
		}
		DatabaseDialect dialect = getDataSourceDialect();
		String toReturn = composeStringToDt(dialect, value);
		return toReturn;
	}

	private void validateNumber(String value) {
		if (!(GenericValidator.isInt(value) || GenericValidator.isFloat(value) || GenericValidator.isDouble(value) || GenericValidator.isShort(value)
				|| GenericValidator.isLong(value))) {
			throw new SecurityException("Input value " + value + " is not a valid number");
		}
	}

	private void validateDate(String value) {
		String dateFormat = GeneralUtilities.getServerDateFormat();
		String timestampFormat = GeneralUtilities.getServerTimeStampFormat();
		if (!GenericValidator.isDate(value, dateFormat, true) && !GenericValidator.isDate(value, timestampFormat, true)) {
			throw new SecurityException(
					"Input value " + value + " is not a valid date according to the date format " + dateFormat + " or timestamp format " + timestampFormat);
		}
	}

	private DatabaseDialect getDataSourceDialect() {
		return databaseDialect;
	}

	private void setDataSourceDialect() {
		IDataSource ds;
		try {
			ds = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(dataSource);

			if (ds != null) {
				IDataBase dataBase = DataBaseFactory.getDataBase(ds);
				databaseDialect = dataBase.getDatabaseDialect();
				ALIAS_DELIMITER = dataBase.getAliasDelimiter();
			}
		} catch (EMFUserError | DataBaseException e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	private String escapeString(String value) {
		if (value == null) {
			return null;
		}
		return StringEscapeUtils.escapeSql(value);
	}

	private String composeStringToDt(DatabaseDialect dialect, String date) {
		String toReturn = "";
		date = escapeString(date); // for security reasons
		if (dialect != null) {
			if (dialect.equals(DatabaseDialect.MYSQL) || dialect.equals(DatabaseDialect.MYSQL_INNODB)) {
				if (date.startsWith("'") && date.endsWith("'")) {
					toReturn = " STR_TO_DATE(" + date + ",'%d/%m/%Y %h:%i:%s') ";
				} else {
					toReturn = " STR_TO_DATE('" + date + "','%d/%m/%Y %h:%i:%s') ";
				}
			} else if (dialect.equals(DatabaseDialect.ORACLE)) {
				if (date.startsWith("'") && date.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + date + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + date + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equals(DatabaseDialect.ORACLE_9I10G)) {
				if (date.startsWith("'") && date.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + date + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + date + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equals(DatabaseDialect.POSTGRESQL)) {
				if (date.startsWith("'") && date.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + date + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + date + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equals(DatabaseDialect.SQLSERVER)) {
				if (date.startsWith("'") && date.endsWith("'")) {
					toReturn = date;
				} else {
					toReturn = "'" + date + "'";
				}
			} else if (dialect.equals(DatabaseDialect.TERADATA)) {
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
	 * @param dependency        The dependency's configuration
	 * @param executionInstance The Execution instance
	 * @return the suitable operator for the input dependency
	 */
	private String findOperator(AbstractParuse dependency, List<? extends AbstractDriver> drivers) {
		String typeFilter = dependency.getFilterOperation();
		if (typeFilter.equalsIgnoreCase(SpagoBIConstants.START_FILTER)) {
			return "LIKE";
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.END_FILTER)) {
			return "LIKE";
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.CONTAIN_FILTER)) {
			return "LIKE";
		} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.EQUAL_FILTER)) {
			AbstractDriver fatherPar = getFatherParameter(dependency, drivers);
			Assert.assertNotNull(fatherPar, "Parent parameter cannot be null");
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

	private AbstractDriver getFatherParameter(AbstractParuse dependency, List<? extends AbstractDriver> drivers) {
		Integer fatherId = dependency.getParFatherId();
		Iterator it = drivers.iterator();
		while (it.hasNext()) {
			AbstractDriver temp = (AbstractDriver) it.next();
			if (temp.getId().equals(fatherId)) {
				return temp;
			}
		}
		return null;
	}

	/**
	 * Gets the values and return them as an xml structure
	 *
	 * @param statement the query statement to execute
	 * @return the xml string containing values
	 * @throws Exception
	 */

	private String getLovResult(IEngUserProfile profile, String statement, boolean getAllColumns) throws Exception {
		String resStr = null;
		DataConnection dataConnection = null;
		SQLCommand sqlCommand = null;
		DataResult dataResult = null;
		try (Connection conn = getConnection(profile, dataSource)) {
			dataConnection = getDataConnection(conn);
			sqlCommand = dataConnection.createSelectCommand(statement, false);
			dataResult = sqlCommand.execute();
			ScrollableDataResult scrollableDataResult = (ScrollableDataResult) dataResult.getDataObject();
			SourceBean result = scrollableDataResult.getSourceBean();
			List<String> colNames = Arrays.asList(scrollableDataResult.getColumnNames());
			List rows = result.getAttributeAsList(DataRow.ROW_TAG);
			// insert all the columns name in the first row, so after all the
			// columns will be present and returned to the client
			if (getAllColumns && rows.size() > 0) {
				SourceBean rowBean = (SourceBean) rows.get(0);
				for (int i = 0; i < colNames.size(); i++) {
					String col = colNames.get(i).toString();
					if (!rowBean.containsAttribute(col)) {
						rowBean.setAttribute(col, "");
					}
				}
			}
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
	 * This methods find out if the input parameters' values are admissible for this QueryDetail instance, i.e. if the values are contained in the query result.
	 *
	 * @param profile The user profile
	 * @param biparam The BIObjectParameter with the values that must be validated
	 * @return a list of errors: it is empty if all values are admissible, otherwise it will contain a EMFUserError for each wrong value
	 * @throws Exception
	 */
	public List validateValues(IEngUserProfile profile, AbstractDriver driver) throws Exception {
		List toReturn = new ArrayList();
		List<String> values = driver.getParameterValues();
		List parameterValuesDescription = new ArrayList();
		DataConnection dataConnection = null;
		SQLCommand sqlCommand = null;
		DataResult dataResult = null;
		String statement = null;
		SourceBean result = null;
		try {
			statement = getValidationQuery(profile, driver, values);
			logger.debug("Executing validation statement [" + statement + "] ...");
			// gets connection
			try (Connection conn = getConnection(profile, dataSource)) {
				dataConnection = getDataConnection(conn);
				sqlCommand = dataConnection.createSelectCommand(statement, false);
				dataResult = sqlCommand.execute();
				ScrollableDataResult scrollableDataResult = (ScrollableDataResult) dataResult.getDataObject();
				result = scrollableDataResult.getSourceBean();
			}
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
				logger.error("Parameter '" + driver.getLabel() + "' cannot assume value '" + aValue + "'" + " for user '"
						+ ((UserProfile) profile).getUserId().toString() + "'.");
				List l = new ArrayList();
				l.add(driver.getLabel());
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
		driver.setParameterValuesDescription(parameterValuesDescription);
		return toReturn;
	}

	/**
	 * This methods builds the validation query, see validateValues method.
	 */
	private String getValidationQuery(IEngUserProfile profile, AbstractDriver driver, List<String> values) throws Exception {
		String statement = getQueryDefinition();
		statement = StringUtilities.substituteProfileAttributesInString(statement, profile);
		StringBuffer buffer = new StringBuffer();

		if (!lovType.equals("treeinner")) {
			buffer.append("SELECT ");
			buffer.append(getColumnSQLName(this.valueColumnName) + " AS \"" + VALUE_ALIAS + "\", ");
			buffer.append(getColumnSQLName(this.descriptionColumnName) + " AS \"" + DESCRIPTION_ALIAS + "\" ");
			buffer.append("FROM (");
			buffer.append(statement);
			buffer.append(") " + getRandomAlias() + " WHERE ");

			if (values.size() == 1) {
				buffer.append(getColumnSQLName(this.valueColumnName) + " = ");
				buffer.append(getSQLValue(driver, values.get(0)));
			} else {
				buffer.append(getColumnSQLName(this.valueColumnName) + " IN (");
				buffer.append(concatenateValues(driver, values));
				buffer.append(")");
			}
		}

		return buffer.toString();
	}

	/**
	 * Gets the list of names of the profile attributes required.
	 *
	 * @return list of profile attribute names
	 * @throws Exception the exception
	 */
	@Override
	public List getProfileAttributeNames() {
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

	public String convertSpecialChars(String query) {

		query = org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4(query);
		return query;

	}

	/**
	 * Checks if the lov requires one or more profile attributes.
	 *
	 * @return true if the lov require one or more profile attributes, false otherwise
	 * @throws Exception the exception
	 */
	@Override
	public boolean requireProfileAttributes() {
		boolean contains = false;
		String query = getQueryDefinition();
		if (query.indexOf("${") != -1) {
			contains = true;
		}
		return contains;
	}

	/**
	 * Splits an XML string by using some <code>SourceBean</code> object methods in order to obtain the source <code>QueryDetail</code> objects whom XML has
	 * been built.
	 *
	 * @param dataDefinition The XML input String
	 * @return The corrispondent <code>QueryDetail</code> object
	 * @throws SourceBeanException If a SourceBean Exception occurred
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
	 * @param dataSource the new data source
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
	 * @param queryDefinition the new query definition
	 */
	public void setQueryDefinition(String queryDefinition) {
		this.queryDefinition = queryDefinition;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail# getDescriptionColumnName ()
	 */
	@Override
	public String getDescriptionColumnName() {
		return descriptionColumnName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail# setDescriptionColumnName (java.lang.String)
	 */
	@Override
	public void setDescriptionColumnName(String descriptionColumnName) {
		this.descriptionColumnName = descriptionColumnName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getInvisibleColumnNames ()
	 */
	@Override
	public List getInvisibleColumnNames() {
		return invisibleColumnNames;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#setInvisibleColumnNames (java.util.List)
	 */
	@Override
	public void setInvisibleColumnNames(List invisibleColumnNames) {
		this.invisibleColumnNames = invisibleColumnNames;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getValueColumnName()
	 */
	@Override
	public String getValueColumnName() {
		return valueColumnName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#setValueColumnName( java.lang.String)
	 */
	@Override
	public void setValueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getVisibleColumnNames()
	 */
	@Override
	public List getVisibleColumnNames() {
		return visibleColumnNames;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#setVisibleColumnNames (java.util.List)
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
	public List<Couple<String, String>> getTreeLevelsColumns() {
		return this.treeLevelsColumns;
	}

	@Override
	public void setTreeLevelsColumns(List<Couple<String, String>> treeLevelsColumns) {
		this.treeLevelsColumns = treeLevelsColumns;
	}

	/*
	 * Methods copied from DataSourceSupplier for DAO refactoring
	 */

	/**
	 * Gets the data source by label.
	 *
	 * @param dsLabel the ds label
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
		Domain doDialect = domaindao.loadDomainByCodeAndValue("DIALECT_HIB", ds.getDialectName());
		sbds.setHibDialectClass(doDialect.getValueCd());
		sbds.setReadOnly(ds.checkIsReadOnly());
		sbds.setWriteDefault(ds.checkIsWriteDefault());
		sbds.setUseForDataprep(ds.checkUseForDataprep());
		return sbds;
	}

	/*
	 * Methods copied from DataSourceUtilities for DAO refactoring
	 */

	/**
	 * use this method in service implementation. If RequestContainer isn't correct.
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
	 * @param con Connection to the export database
	 * @return The Spago DataConnection Object
	 * @throws EMFInternalError the EMF internal error
	 */
	public DataConnection getDataConnection(Connection con) throws EMFInternalError {
		DataConnection dataCon = null;
		try {
			Class mapperClass = Class.forName("it.eng.spago.dbaccess.sql.mappers.OracleSQLMapper");
			SQLMapper sqlMapper = (SQLMapper) mapperClass.newInstance();
			dataCon = new DataConnection(con, "2.1", sqlMapper);
		} catch (Exception e) {
			String conAsString = Optional.ofNullable(con).map(Connection::toString).orElse("null");
			logger.error("Error while getting Data Source from connection " + conAsString, e);
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "cannot build spago DataConnection object");
		}
		return dataCon;
	}

	/**
	 * Gets the set of names of the parameters required.
	 *
	 * @return set of parameter names
	 * @throws Exception the exception
	 */
	@Override
	public Set<String> getParameterNames() {
		Set<String> names = new HashSet<String>();
		String query = getQueryDefinition();
		while (query.indexOf(StringUtilities.START_PARAMETER) != -1) {
			int startind = query.indexOf(StringUtilities.START_PARAMETER);
			int endind = query.indexOf("}", startind);
			String parameterDef = query.substring(startind + 3, endind);
			if (parameterDef.indexOf("(") != -1) {
				int indroundBrack = query.indexOf("(", startind);
				String nameParam = query.substring(startind + 3, indroundBrack);
				names.add(nameParam);
			} else {
				names.add(parameterDef);
			}
			query = query.substring(endind);
		}
		return names;
	}

	@Override
	public boolean isSimpleLovType() {
		return this.getLovType() == null || this.getLovType().equalsIgnoreCase("simple");
	}
}
