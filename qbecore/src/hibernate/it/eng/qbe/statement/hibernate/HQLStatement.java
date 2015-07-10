/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement.hibernate;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.hibernate.IHibernateDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.AbstractSelectField;
import it.eng.qbe.query.CriteriaConstants;
import it.eng.qbe.query.ExpressionNode;
import it.eng.qbe.query.HavingField;
import it.eng.qbe.query.InLineCalculatedSelectField;
import it.eng.qbe.query.Operand;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.SimpleSelectField;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.qbe.statement.AbstractStatement;
import it.eng.qbe.statement.StatementTockenizer;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * The Class HQLStatement.
 */
public class HQLStatement extends AbstractStatement {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(HQLStatement.class);

	public static interface IConditionalOperator {
		String apply(String leftHandValue, String[] rightHandValues);
	}

	public static Map conditionalOperators;

	static {
		conditionalOperators = new HashMap();
		conditionalOperators.put(CriteriaConstants.EQUALS_TO, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.EQUALS_TO;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + "=" + rightHandValues[0];
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_EQUALS_TO, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.NOT_EQUALS_TO;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + "!=" + rightHandValues[0];
			}
		});
		conditionalOperators.put(CriteriaConstants.GREATER_THAN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.GREATER_THAN;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + ">" + rightHandValues[0];
			}
		});
		conditionalOperators.put(CriteriaConstants.EQUALS_OR_GREATER_THAN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.EQUALS_OR_GREATER_THAN;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + ">=" + rightHandValues[0];
			}
		});
		conditionalOperators.put(CriteriaConstants.LESS_THAN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.LESS_THAN;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + "<" + rightHandValues[0];
			}
		});
		conditionalOperators.put(CriteriaConstants.EQUALS_OR_LESS_THAN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.EQUALS_OR_LESS_THAN;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + "<=" + rightHandValues[0];
			}
		});
		conditionalOperators.put(CriteriaConstants.STARTS_WITH, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.STARTS_WITH;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				rightHandValue = rightHandValue.substring(1, rightHandValue.length() - 1);
				rightHandValue = rightHandValue + "%";
				return leftHandValue + " like '" + rightHandValue + "'";
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_STARTS_WITH, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.NOT_STARTS_WITH;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				rightHandValue = rightHandValue.substring(1, rightHandValue.length() - 1);
				rightHandValue = rightHandValue + "%";
				return leftHandValue + " not like '" + rightHandValue + "'";
			}
		});
		conditionalOperators.put(CriteriaConstants.ENDS_WITH, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.ENDS_WITH;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				rightHandValue = rightHandValue.substring(1, rightHandValue.length() - 1);
				rightHandValue = "%" + rightHandValue;
				return leftHandValue + " like '" + rightHandValue + "'";
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_ENDS_WITH, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.NOT_ENDS_WITH;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				rightHandValue = rightHandValue.substring(1, rightHandValue.length() - 1);
				rightHandValue = "%" + rightHandValue;
				return leftHandValue + " not like '" + rightHandValue + "'";
			}
		});
		conditionalOperators.put(CriteriaConstants.CONTAINS, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.CONTAINS;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				rightHandValue = rightHandValue.substring(1, rightHandValue.length() - 1);
				rightHandValue = "%" + rightHandValue + "%";
				return leftHandValue + " like '" + rightHandValue + "'";
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_CONTAINS, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.NOT_CONTAINS;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				rightHandValue = rightHandValue.substring(1, rightHandValue.length() - 1);
				rightHandValue = "%" + rightHandValue + "%";
				return leftHandValue + " not like '" + rightHandValue + "'";
			}
		});
		conditionalOperators.put(CriteriaConstants.IS_NULL, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.IS_NULL;
			}

			public String apply(String leftHandValue, String[] rightHandValue) {
				return leftHandValue + " IS NULL";
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_NULL, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.NOT_NULL;
			}

			public String apply(String leftHandValue, String[] rightHandValue) {
				return leftHandValue + " IS NOT NULL";
			}
		});

		conditionalOperators.put(CriteriaConstants.BETWEEN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.BETWEEN;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues.length == 2,
						"When BEETWEEN operator is used the operand must contain minValue and MaxValue");
				return leftHandValue + " BETWEEN " + rightHandValues[0] + " AND " + rightHandValues[1];
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_BETWEEN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.NOT_BETWEEN;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues.length == 2,
						"When BEETWEEN operator is used the operand must contain minValue and MaxValue");
				return leftHandValue + " NOT BETWEEN " + rightHandValues[0] + " AND " + rightHandValues[1];
			}
		});

		conditionalOperators.put(CriteriaConstants.IN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.IN;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				String rightHandValue = StringUtils.join(rightHandValues, ",");
				return leftHandValue + " IN (" + rightHandValue + ")";
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_IN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.NOT_IN;
			}

			public String apply(String leftHandValue, String[] rightHandValues) {
				String rightHandValue = StringUtils.join(rightHandValues, ",");
				return leftHandValue + " NOT IN (" + rightHandValue + ")";
			}
		});
	}

	protected HQLStatement(IDataSource dataSource) {
		super(dataSource);
	}

	public HQLStatement(IDataSource dataSource, Query query) {
		super(dataSource, query);
	}

	public static final String DISTINCT = "DISTINCT";
	public static final String SELECT = "SELECT";
	public static final String FROM = "FROM";

	@Override
	public String getNextAlias(Map entityAliasesMaps) {
		int aliasesCount = 0;
		Iterator it = entityAliasesMaps.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Map entityAliases = (Map) entityAliasesMaps.get(key);
			aliasesCount += entityAliases.keySet().size();
		}

		return "t_" + aliasesCount;
	}

	private String buildSelectClause(Query query, Map entityAliasesMaps) {
		StringBuffer buffer;
		List selectFields;
		List allSelectFields;
		List<InLineCalculatedSelectField> selectInLineCalculatedFields = new ArrayList<InLineCalculatedSelectField>();
		AbstractSelectField selectAbstractField;
		SimpleSelectField selectField;
		InLineCalculatedSelectField selectInLineField;
		IModelEntity rootEntity;
		IModelField datamartField;
		String queryName;
		String rootEntityAlias;
		String selectClauseElement; // rootEntityAlias.queryName
		Map entityAliases;
		List<String> aliasEntityMapping;

		logger.debug("IN");
		buffer = new StringBuffer();
		try {
			selectFields = query.getSelectFields(true);

			if (selectFields == null || selectFields.size() == 0) {
				return "";
			}

			entityAliases = (Map) entityAliasesMaps.get(query.getId());

			buffer.append(SELECT);
			if (query.isDistinctClauseEnabled()) {
				buffer.append(" " + DISTINCT);
			}

			Iterator it = selectFields.iterator();
			if (it.hasNext()) {
				selectAbstractField = (AbstractSelectField) it.next();
				String[] idsForQuery = new String[selectFields.size() - query.getCalculatedSelectFields(true).size()];
				int index = 0;
				do {
					if (selectAbstractField.isSimpleField()) {

						selectField = (SimpleSelectField) selectAbstractField;

						logger.debug("select field unique name [" + selectField.getUniqueName() + "]");

						datamartField = getDataSource().getModelStructure().getField(selectField.getUniqueName());
						queryName = (String) datamartField.getQueryName().getFirst();
						logger.debug("select field query name [" + queryName + "]");

						rootEntity = datamartField.getParent().getRoot();
						logger.debug("select field root entity unique name [" + rootEntity.getUniqueName() + "]");

						rootEntityAlias = (String) entityAliases.get(rootEntity.getUniqueName());
						if (rootEntityAlias == null) {
							rootEntityAlias = getNextAlias(entityAliasesMaps);
							entityAliases.put(rootEntity.getUniqueName(), rootEntityAlias);
						}
						logger.debug("select field root entity alias [" + rootEntityAlias + "]");

						selectClauseElement = rootEntityAlias + "." + queryName;
						logger.debug("select clause element before aggregation [" + selectClauseElement + "]");

						selectClauseElement = selectField.getFunction().apply(selectClauseElement);
						logger.debug("select clause element after aggregation [" + selectClauseElement + "]");

						idsForQuery[index] = " " + selectClauseElement;
						index++;
						logger.debug("select clause element succesfully added to select clause");

					} else if (selectAbstractField.isInLineCalculatedField()) {
						selectInLineCalculatedFields.add((InLineCalculatedSelectField) selectAbstractField);
						index++;
					}

					if (it.hasNext()) {
						selectAbstractField = (AbstractSelectField) it.next();
					} else {
						break;
					}

				} while (true);

				aliasEntityMapping = new ArrayList<String>();
				for (int k = 0; k < selectInLineCalculatedFields.size(); k++) {
					selectInLineField = selectInLineCalculatedFields.get(k);

					String expr = selectInLineField.getExpression();// .replace("\'", "");
					expr = parseInLinecalculatedField(expr, query, entityAliasesMaps);
					expr = selectInLineField.getFunction().apply(expr);

					for (int y = 0; y < idsForQuery.length; y++) {
						if (idsForQuery[y] == null) {
							idsForQuery[y] = " " + expr;
							index = y;
							break;
						}
					}

					logger.debug("select clause element succesfully added to select clause");
				}

				for (int y = 0; y < idsForQuery.length - 1; y++) {
					buffer.append(idsForQuery[y] + ",");
				}
				buffer.append(idsForQuery[idsForQuery.length - 1]);

			}

		}

		finally {
			logger.debug("OUT");
		}

		return buffer.toString().trim();
	}

	private String buildFromClause(Query query, Map entityAliasesMaps) {
		StringBuffer buffer;

		logger.debug("IN");
		buffer = new StringBuffer();
		try {
			Map entityAliases = (Map) entityAliasesMaps.get(query.getId());

			if (entityAliases == null || entityAliases.keySet().size() == 0) {
				return "";
			}

			buffer.append(" " + FROM + " ");

			// outer join are not supported by hibernate
			// so this method is expected to return always an empty string
			// buffer.append( buildJoinClause(query, entityAliases) );

			Iterator it = entityAliases.keySet().iterator();
			while (it.hasNext()) {
				String entityUniqueName = (String) it.next();
				logger.debug("entity [" + entityUniqueName + "]");

				String entityAlias = (String) entityAliases.get(entityUniqueName);
				logger.debug("entity alias [" + entityAlias + "]");

				IModelEntity datamartEntity = getDataSource().getModelStructure().getEntity(entityUniqueName);
				String whereClauseElement = datamartEntity.getType() + " " + entityAlias;
				logger.debug("where clause element [" + whereClauseElement + "]");

				buffer.append(" " + whereClauseElement);
				if (it.hasNext()) {
					buffer.append(",");
				}
			}
		} finally {
			logger.debug("OUT");
		}

		return buffer.toString().trim();
	}

	private String[] buildStaticOperand(Operand operand) {
		String[] operandElement;

		logger.debug("IN");

		try {
			operandElement = operand.values;
		} finally {
			logger.debug("OUT");
		}

		return operandElement;
	}

	private String buildFieldOperand(Operand operand, Query query, Map entityAliasesMaps) {
		String operandElement;
		IModelField datamartField;
		IModelEntity rootEntity;
		String queryName;
		String rootEntityAlias;
		Map targetQueryEntityAliasesMap;

		logger.debug("IN");

		try {

			targetQueryEntityAliasesMap = (Map) entityAliasesMaps.get(query.getId());
			Assert.assertNotNull(targetQueryEntityAliasesMap, "Entity aliases map for query [" + query.getId()
					+ "] cannot be null in order to execute method [buildUserProvidedWhereField]");

			datamartField = getDataSource().getModelStructure().getField(operand.values[0]);
			Assert.assertNotNull(datamartField, "DataMart does not cantain a field named [" + operand.values[0] + "]");
			queryName = (String) datamartField.getQueryName().getFirst();
			logger.debug("where field query name [" + queryName + "]");

			rootEntity = datamartField.getParent().getRoot();
			logger.debug("where field root entity unique name [" + rootEntity.getUniqueName() + "]");

			if (!targetQueryEntityAliasesMap.containsKey(rootEntity.getUniqueName())) {
				logger.debug("Entity [" + rootEntity.getUniqueName() + "] require a new alias");
				rootEntityAlias = getNextAlias(entityAliasesMaps);
				logger.debug("A new alias has been generated [" + rootEntityAlias + "]");
				targetQueryEntityAliasesMap.put(rootEntity.getUniqueName(), rootEntityAlias);
			}
			rootEntityAlias = (String) targetQueryEntityAliasesMap.get(rootEntity.getUniqueName());
			logger.debug("where field root entity alias [" + rootEntityAlias + "]");

			if (operand instanceof HavingField.Operand) {
				HavingField.Operand havingFieldOperand = (HavingField.Operand) operand;
				IAggregationFunction function = havingFieldOperand.function;
				operandElement = function.apply(rootEntityAlias + "." + queryName);
			} else {
				operandElement = rootEntityAlias + "." + queryName;
			}
			logger.debug("where element operand value [" + operandElement + "]");
		} finally {
			logger.debug("OUT");
		}

		return operandElement;
	}

	private String buildParentFieldOperand(Operand operand, Query query, Map entityAliasesMaps) {
		String operandElement;

		String[] chunks;
		String parentQueryId;
		String fieldName;
		IModelField datamartField;
		IModelEntity rootEntity;
		String queryName;
		String rootEntityAlias;

		logger.debug("IN");

		try {

			// it comes directly from the client side GUI. It is a composition of the parent query id and filed name,
			// separated by a space
			logger.debug("operand  is equals to [" + operand.values[0] + "]");

			chunks = operand.values[0].split(" ");
			Assert.assertTrue(chunks.length >= 2, "Operand [" + chunks.toString()
					+ "] does not contains enougth informations in order to resolve the reference to parent field");

			parentQueryId = chunks[0];
			logger.debug("where right-hand field belonging query [" + parentQueryId + "]");
			fieldName = chunks[1];
			logger.debug("where right-hand field unique name [" + fieldName + "]");

			datamartField = getDataSource().getModelStructure().getField(fieldName);
			Assert.assertNotNull(datamartField, "DataMart does not cantain a field named [" + fieldName + "]");

			queryName = (String) datamartField.getQueryName().getFirst();
			logger.debug("where right-hand field query name [" + queryName + "]");

			rootEntity = datamartField.getParent().getRoot();
			logger.debug("where right-hand field root entity unique name [" + rootEntity.getUniqueName() + "]");

			Map parentEntityAliases = (Map) entityAliasesMaps.get(parentQueryId);
			if (parentEntityAliases != null) {
				if (!parentEntityAliases.containsKey(rootEntity.getUniqueName())) {
					Assert.assertUnreachable("Filter of subquery [" + query.getId() + "] refers to a non " + "existing parent query [" + parentQueryId
							+ "] entity [" + rootEntity.getUniqueName() + "]");
				}
				rootEntityAlias = (String) parentEntityAliases.get(rootEntity.getUniqueName());
			} else {
				rootEntityAlias = "unresoved_alias";
				logger.warn("Impossible to get aliases map for parent query [" + parentQueryId + "]. Probably the parent query ha not been compiled yet");
				logger.warn("Query [" + query.getId() + "] refers entities of its parent query [" + parentQueryId
						+ "] so the generated statement wont be executable until the parent query will be compiled");
			}
			logger.debug("where right-hand field root entity alias [" + rootEntityAlias + "]");

			operandElement = rootEntityAlias + "." + queryName;
			logger.debug("where element right-hand field value [" + operandElement + "]");
		} finally {
			logger.debug("OUT");
		}

		return operandElement;
	}

	private String buildQueryOperand(Operand operand) {
		String operandElement;

		logger.debug("IN");

		try {
			String subqueryId;

			logger.debug("where element right-hand field type [" + OPERAND_TYPE_SUBQUERY + "]");

			subqueryId = operand.values[0];
			logger.debug("Referenced subquery [" + subqueryId + "]");

			operandElement = "Q{" + subqueryId + "}";
			operandElement = "( " + operandElement + ")";
			logger.debug("where element right-hand field value [" + operandElement + "]");
		} finally {
			logger.debug("OUT");
		}

		return operandElement;
	}

	private String[] buildOperand(Operand operand, Query query, Map entityAliasesMaps) {
		String[] operandElement;

		logger.debug("IN");

		try {
			Assert.assertNotNull(operand, "Input parameter [operand] cannot be null in order to execute method [buildUserProvidedWhereField]");
			operandElement = new String[] { "" };

			if (OPERAND_TYPE_STATIC.equalsIgnoreCase(operand.type) || "Static Value".equalsIgnoreCase(operand.type)) {
				operandElement = buildStaticOperand(operand);
			} else if (OPERAND_TYPE_SUBQUERY.equalsIgnoreCase(operand.type)) {
				operandElement = new String[] { buildQueryOperand(operand) };
			} else if (OPERAND_TYPE_SIMPLE_FIELD.equalsIgnoreCase(operand.type) || OPERAND_TYPE_INLINE_CALCULATED_FIELD.equalsIgnoreCase(operand.type)) {
				operandElement = new String[] { buildFieldOperand(operand, query, entityAliasesMaps) };
			} else if (OPERAND_TYPE_PARENT_FIELD.equalsIgnoreCase(operand.type)) {
				operandElement = new String[] { buildParentFieldOperand(operand, query, entityAliasesMaps) };
			} else {
				Assert.assertUnreachable("Invalid operand type [" + operand.type + "]");
			}
		} finally {
			logger.debug("OUT");
		}
		return operandElement;
	}

	private String[] getTypeBoundedStaticOperand(Operand leadOperand, String operator, String[] operandValuesToBound) {
		String[] boundedValues = new String[operandValuesToBound.length];

		for (int i = 0; i < operandValuesToBound.length; i++) {

			String operandValueToBound = operandValuesToBound[i];
			String boundedValue = operandValueToBound;

			if (OPERAND_TYPE_INLINE_CALCULATED_FIELD.equalsIgnoreCase(leadOperand.type)) {
				int startType = leadOperand.values[0].indexOf("type\":") + 7;
				int endType = leadOperand.values[0].indexOf("\"", startType);
				String type = leadOperand.values[0].substring(startType, endType);
				boundedValue = getValueBounded(operandValueToBound, type);
			} else if (OPERAND_TYPE_SIMPLE_FIELD.equalsIgnoreCase(leadOperand.type) || OPERAND_TYPE_PARENT_FIELD.equalsIgnoreCase(leadOperand.type)) {

				IModelField datamartField = getDataSource().getModelStructure().getField(leadOperand.values[0]);
				boundedValue = getValueBounded(operandValueToBound, datamartField.getType());
			}

			boundedValues[i] = boundedValue;

		}

		return boundedValues;
	}

	@Override
	public String getValueBounded(String operandValueToBound, String operandType) {
		String boundedValue;
		Date operandValueToBoundDate;

		boundedValue = operandValueToBound;

		if (operandType.equalsIgnoreCase("STRING") || operandType.equalsIgnoreCase("CHARACTER") || operandType.equalsIgnoreCase("java.lang.String")
				|| operandType.equalsIgnoreCase("java.lang.Character")) {
			// if the value is already surrounded by quotes, does not neither add quotes nor escape quotes
			if (StringUtils.isBounded(operandValueToBound, "'")) {
				boundedValue = operandValueToBound;
			} else {
				operandValueToBound = StringUtils.escapeQuotes(operandValueToBound);
				return StringUtils.bound(operandValueToBound, "'");
			}
		} else if (operandType.equalsIgnoreCase("TIMESTAMP") || operandType.equalsIgnoreCase("DATE") || operandType.equalsIgnoreCase("java.sql.TIMESTAMP")
				|| operandType.equalsIgnoreCase("java.sql.date") || operandType.equalsIgnoreCase("java.util.date")) {

			if (operandValueToBound == null || operandValueToBound.equals("")) {
				boundedValue = operandValueToBound;
			} else {
				it.eng.spagobi.tools.datasource.bo.IDataSource connection = (it.eng.spagobi.tools.datasource.bo.IDataSource) getDataSource().getConfiguration()
						.loadDataSourceProperties().get("datasource");
				String dbDialect = connection.getHibDialectClass();

				String userDateFormatPattern = (String) getParameters().get(EngineConstants.ENV_USER_DATE_FORMAT);
				DateFormat userDataFormat = new SimpleDateFormat(userDateFormatPattern);
				try {
					operandValueToBoundDate = userDataFormat.parse(operandValueToBound);
				} catch (ParseException e) {
					logger.error("Error parsing the date " + operandValueToBound);
					throw new SpagoBIRuntimeException("Error parsing the date " + operandValueToBound + ". Check the format, it should be "
							+ userDateFormatPattern);
				}

				boundedValue = composeStringToDt(dbDialect, operandValueToBoundDate);
			}
		}

		return boundedValue;
	}

	public static String composeStringToDt(String dialect, Date date) {
		String toReturn = "";

		DateFormat stagingDataFormat = new SimpleDateFormat("dd/MM/yyyy");
		String dateStr = stagingDataFormat.format(date);

		if (dialect != null) {

			if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_MYSQL)) {
				if (dateStr.startsWith("'") && dateStr.endsWith("'")) {
					toReturn = " STR_TO_DATE(" + dateStr + ",'%d/%m/%Y %h:%i:%s') ";
				} else {
					toReturn = " STR_TO_DATE('" + dateStr + "','%d/%m/%Y %h:%i:%s') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_HSQL)) {
				try {
					DateFormat df;
					if (StringUtils.isBounded(dateStr, "'")) {
						df = new SimpleDateFormat("'dd/MM/yyyy HH:mm:SS'");
					} else {
						df = new SimpleDateFormat("dd/MM/yyyy HH:mm:SS");
					}

					Date myDate = df.parse(dateStr);
					df = new SimpleDateFormat("yyyy-MM-dd");
					toReturn = "'" + df.format(myDate) + "'";

				} catch (Exception e) {
					toReturn = "'" + dateStr + "'";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_INGRES)) {
				if (dateStr.startsWith("'") && dateStr.endsWith("'")) {
					toReturn = " STR_TO_DATE(" + dateStr + ",'%d/%m/%Y') ";
				} else {
					toReturn = " STR_TO_DATE('" + dateStr + "','%d/%m/%Y') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE)) {
				if (dateStr.startsWith("'") && dateStr.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + dateStr + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + dateStr + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE9i10g)) {
				if (dateStr.startsWith("'") && dateStr.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + dateStr + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + dateStr + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_POSTGRES)) {
				if (dateStr.startsWith("'") && dateStr.endsWith("'")) {
					toReturn = " TO_TIMESTAMP(" + dateStr + ",'DD/MM/YYYY HH24:MI:SS.FF') ";
				} else {
					toReturn = " TO_TIMESTAMP('" + dateStr + "','DD/MM/YYYY HH24:MI:SS.FF') ";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_SQLSERVER)) {
				if (dateStr.startsWith("'") && dateStr.endsWith("'")) {
					toReturn = dateStr;
				} else {
					toReturn = "'" + dateStr + "'";
				}
			} else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_TERADATA)) {
				/*
				 * Unfortunately we cannot use neither CAST(" + dateStr + " AS DATE FORMAT 'dd/mm/yyyy') nor CAST((" + dateStr + " (Date,Format 'dd/mm/yyyy'))
				 * As Date) because Hibernate does not recognize (and validate) those SQL functions. Therefore we must use a predefined date format
				 * (yyyy-MM-dd).
				 */
				try {
					DateFormat dateFormat;
					if (StringUtils.isBounded(dateStr, "'")) {
						dateFormat = new SimpleDateFormat("'dd/MM/yyyy'");
					} else {
						dateFormat = new SimpleDateFormat("dd/MM/yyyy");
					}
					Date myDate = dateFormat.parse(dateStr);
					dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					toReturn = "'" + dateFormat.format(myDate) + "'";
				} catch (Exception e) {
					logger.error("Error parsing the date " + dateStr, e);
					throw new SpagoBIRuntimeException("Error parsing the date " + dateStr + ".");
				}
			}
		}

		return toReturn;
	}

	private String buildUserProvidedWhereField(WhereField whereField, Query query, Map entityAliasesMaps) {

		String whereClauseElement = "";
		String[] rightOperandElements;
		String[] leftOperandElements;

		logger.debug("IN");

		try {
			IConditionalOperator conditionalOperator = null;
			conditionalOperator = (IConditionalOperator) conditionalOperators.get(whereField.getOperator());
			Assert.assertNotNull(conditionalOperator, "Unsopported operator " + whereField.getOperator() + " used in query definition");

			if (whereField.getLeftOperand().values[0].contains("expression")) {
				whereClauseElement = buildInLineCalculatedFieldClause(whereField.getOperator(), whereField.getLeftOperand(), whereField.isPromptable(),
						whereField.getRightOperand(), query, entityAliasesMaps, conditionalOperator);
			} else {

				leftOperandElements = buildOperand(whereField.getLeftOperand(), query, entityAliasesMaps);

				if ((OPERAND_TYPE_STATIC.equalsIgnoreCase(whereField.getRightOperand().type) || "Static Value"
						.equalsIgnoreCase(whereField.getRightOperand().type)) && whereField.isPromptable()) {
					// get last value first (the last value edited by the user)
					rightOperandElements = whereField.getRightOperand().lastValues;
				} else {

					rightOperandElements = buildOperand(whereField.getRightOperand(), query, entityAliasesMaps);
				}

				if (OPERAND_TYPE_STATIC.equalsIgnoreCase(whereField.getLeftOperand().type) || "Static Value".equalsIgnoreCase(whereField.getLeftOperand().type)) {
					leftOperandElements = getTypeBoundedStaticOperand(whereField.getRightOperand(), whereField.getOperator(), leftOperandElements);
				}

				if (OPERAND_TYPE_STATIC.equalsIgnoreCase(whereField.getRightOperand().type)
						|| "Static Value".equalsIgnoreCase(whereField.getRightOperand().type)) {
					rightOperandElements = getTypeBoundedStaticOperand(whereField.getLeftOperand(), whereField.getOperator(), rightOperandElements);
				}

				whereClauseElement = conditionalOperator.apply(leftOperandElements[0], rightOperandElements);

			}

			logger.debug("where element value [" + whereClauseElement + "]");
		} finally {
			logger.debug("OUT");
		}

		return whereClauseElement;
	}

	/**
	 * Builds the sql statement (for the having or the where clause) for the calculate fields.
	 * 
	 * @param operator
	 *            the operator of the clause
	 * @param leftOperand
	 *            the left operand
	 * @param isPromptable
	 * @param rightOperand
	 *            right operand
	 * @param query
	 *            the sql query
	 * @param entityAliasesMaps
	 *            the map of the entity involved in the query
	 * @return
	 */
	private String buildInLineCalculatedFieldClause(String operator, Operand leftOperand, boolean isPromptable, Operand rightOperand, Query query,
			Map entityAliasesMaps, IConditionalOperator conditionalOperator) {
		String[] rightOperandElements;

		String expr = leftOperand.values[0].substring(leftOperand.values[0].indexOf("\"expression\":\"") + 14);// .replace("\'", "");
		expr = expr.substring(0, expr.indexOf("\""));

		logger.debug("Left operand (of a inline calculated field) for the filter clause of the query: " + leftOperand.values[0]);
		logger.debug("Expression of a inline calculated field for the filter clause of the query: " + expr);

		// String expr = leftOperand.value.substring(15,leftOperand.value.indexOf("\",\"alias"));//.replace("\'", "");

		expr = parseInLinecalculatedField(expr, query, entityAliasesMaps);

		logger.debug("IN");

		if ((OPERAND_TYPE_STATIC.equalsIgnoreCase(rightOperand.type) || "Static Value".equalsIgnoreCase(rightOperand.type)) && isPromptable) {
			// get last value first (the last value edited by the user)
			rightOperandElements = rightOperand.lastValues;
		} else {
			rightOperandElements = buildOperand(rightOperand, query, entityAliasesMaps);
		}

		if (OPERAND_TYPE_STATIC.equalsIgnoreCase(rightOperand.type) || "Static Value".equalsIgnoreCase(rightOperand.type)) {
			rightOperandElements = getTypeBoundedStaticOperand(leftOperand, operator, rightOperandElements);
		}

		return conditionalOperator.apply("(" + expr + ")", rightOperandElements);
	}

	public String parseInLinecalculatedField(String expr, Query query, Map entityAliasesMaps) {
		List allSelectFields;
		IModelEntity rootEntity;
		IModelField datamartField;
		String queryName;
		String rootEntityAlias;
		Map entityAliases = (Map) entityAliasesMaps.get(query.getId());
		List<String> aliasEntityMapping = new ArrayList<String>();
		List<String> aliases = new ArrayList<String>();

		StatementTockenizer stk = new StatementTockenizer(expr);
		while (stk.hasMoreTokens()) {
			String cfExpressionField = stk.nextTokenInStatement().trim();
			// alias can contain "DISTINCT" HQL/SQL key: we have to remove it
			if (cfExpressionField.toUpperCase().startsWith("DISTINCT ")) {
				cfExpressionField = cfExpressionField.substring("DISTINCT ".length());
			}

			String uniqueName;
			allSelectFields = query.getSelectFields(false);
			for (int i = 0; i < allSelectFields.size(); i++) {
				if (allSelectFields.get(i).getClass().equals(SimpleSelectField.class)
						&& ((SimpleSelectField) allSelectFields.get(i)).getUniqueName().equals(cfExpressionField)) {
					uniqueName = ((SimpleSelectField) allSelectFields.get(i)).getUniqueName();
					datamartField = getDataSource().getModelStructure().getField(uniqueName);
					queryName = (String) datamartField.getQueryName().getFirst();
					rootEntity = datamartField.getParent().getRoot();
					rootEntityAlias = (String) entityAliases.get(rootEntity.getUniqueName());
					queryName = ((SimpleSelectField) allSelectFields.get(i)).getFunction().apply(rootEntityAlias + "." + queryName);
					aliasEntityMapping.add(queryName);
					aliases.add(cfExpressionField);
					break;
				}
			}
		}

		String freshExpr = expr;
		int ind = 0;
		int pos = 0;
		stk = new StatementTockenizer(expr.replace("\'", ""));
		while (stk.hasMoreTokens()) {
			String alias = stk.nextToken().trim();
			// alias can contain "DISTINCT" HQL/SQL key: we have to remove it
			if (alias.toUpperCase().startsWith("DISTINCT ")) {
				alias = alias.substring("DISTINCT ".length());
			}
			pos = freshExpr.indexOf(alias, pos);
			if (ind < aliases.size() && aliases.get(ind).equals(alias)) {
				freshExpr = freshExpr.substring(0, pos) + aliasEntityMapping.get(ind) + freshExpr.substring(pos + alias.length());
				pos = pos + aliasEntityMapping.get(ind).length();
				ind++;
			} else {
				// freshExpr = freshExpr.substring(0, pos)+ alias+freshExpr.substring(pos+alias.length());
				pos = pos + alias.length();
			}
		}
		return freshExpr;
	}

	private String buildUserProvidedWhereClause(ExpressionNode filterExp, Query query, Map entityAliasesMaps) {
		String str = "";

		String type = filterExp.getType();
		if ("NODE_OP".equalsIgnoreCase(type)) {
			for (int i = 0; i < filterExp.getChildNodes().size(); i++) {
				ExpressionNode child = (ExpressionNode) filterExp.getChildNodes().get(i);
				String childStr = buildUserProvidedWhereClause(child, query, entityAliasesMaps);
				if ("NODE_OP".equalsIgnoreCase(child.getType())) {
					childStr = "(" + childStr + ")";
				}
				str += (i == 0 ? "" : " " + filterExp.getValue());
				str += " " + childStr;
			}
		} else {
			WhereField whereField = query.getWhereFieldByName(filterExp.getValue());
			str += buildUserProvidedWhereField(whereField, query, entityAliasesMaps);
		}

		return str;
	}

	private String buildHavingClause(Query query, Map entityAliasesMaps) {

		StringBuffer buffer = new StringBuffer();

		if (query.getHavingFields().size() > 0) {
			buffer.append("HAVING ");
			Iterator it = query.getHavingFields().iterator();
			while (it.hasNext()) {
				HavingField field = (HavingField) it.next();

				if (field.getLeftOperand().values[0].contains("expression")) {
					IConditionalOperator conditionalOperator = null;
					conditionalOperator = (IConditionalOperator) conditionalOperators.get(field.getOperator());
					Assert.assertNotNull(conditionalOperator, "Unsopported operator " + field.getOperator() + " used in query definition");

					String havingClauseElement = buildInLineCalculatedFieldClause(field.getOperator(), field.getLeftOperand(), field.isPromptable(),
							field.getRightOperand(), query, entityAliasesMaps, conditionalOperator);
					buffer.append(havingClauseElement);
				} else {
					buffer.append(buildHavingClauseElement(field, query, entityAliasesMaps));
				}

				if (it.hasNext()) {
					buffer.append(" " + field.getBooleanConnector() + " ");
				}
			}
		}

		return buffer.toString().trim();
	}

	private String buildHavingClauseElement(HavingField havingField, Query query, Map entityAliasesMaps) {

		String havingClauseElement;
		String[] leftOperandElements;
		String[] rightOperandElements;

		logger.debug("IN");

		try {
			IConditionalOperator conditionalOperator = null;
			conditionalOperator = (IConditionalOperator) conditionalOperators.get(havingField.getOperator());
			Assert.assertNotNull(conditionalOperator, "Unsopported operator " + havingField.getOperator() + " used in query definition");

			leftOperandElements = buildOperand(havingField.getLeftOperand(), query, entityAliasesMaps);

			if ((OPERAND_TYPE_STATIC.equalsIgnoreCase(havingField.getRightOperand().type) || "Static Value"
					.equalsIgnoreCase(havingField.getRightOperand().type)) && havingField.isPromptable()) {
				// get last value first (the last value edited by the user)
				rightOperandElements = havingField.getRightOperand().lastValues;
			} else {
				rightOperandElements = buildOperand(havingField.getRightOperand(), query, entityAliasesMaps);
			}

			if ((OPERAND_TYPE_STATIC.equalsIgnoreCase(havingField.getLeftOperand().type) || "Static Value".equalsIgnoreCase(havingField.getLeftOperand().type))) {
				leftOperandElements = getTypeBoundedStaticOperand(havingField.getRightOperand(), havingField.getOperator(), leftOperandElements);
			}

			if (OPERAND_TYPE_STATIC.equalsIgnoreCase(havingField.getRightOperand().type) || "Static Value".equalsIgnoreCase(havingField.getRightOperand().type)) {
				rightOperandElements = getTypeBoundedStaticOperand(havingField.getLeftOperand(), havingField.getOperator(), rightOperandElements);
			}

			havingClauseElement = conditionalOperator.apply(leftOperandElements[0], rightOperandElements);
			logger.debug("Having clause element value [" + havingClauseElement + "]");
		} finally {
			logger.debug("OUT");
		}

		return havingClauseElement;
	}

	private String buildWhereClause(Query query, Map entityAliasesMaps) {

		StringBuffer buffer = new StringBuffer();

		Map entityAliases = (Map) entityAliasesMaps.get(query.getId());

		if (query.getWhereClauseStructure() != null) {
			buffer.append("WHERE ");
			buffer.append(buildUserProvidedWhereClause(query.getWhereClauseStructure(), query, entityAliasesMaps));
		}

		// IModelStructure dataMartModelStructure = getDataSource().getModelStructure();
		// IModelAccessModality dataMartModelAccessModality = getDataSource().getModelAccessModality();
		//
		// Iterator it = entityAliases.keySet().iterator();
		// while(it.hasNext()){
		// String entityUniqueName = (String)it.next();
		// IModelEntity entity = dataMartModelStructure.getEntity( entityUniqueName );
		//
		// // check for condition filter on this entity
		// List filters = dataMartModelAccessModality.getEntityFilterConditions(entity.getType());
		// for(int i = 0; i < filters.size(); i++) {
		// Filter filter = (Filter)filters.get(i);
		// Set fields = filter.getFields();
		// Properties props = new Properties();
		// Iterator fieldIterator = fields.iterator();
		// while(fieldIterator.hasNext()) {
		// String fieldName = (String)fieldIterator.next();
		// String entityAlias = (String)entityAliases.get(entityUniqueName);
		// props.put(fieldName, entityAlias + "." + fieldName);
		// }
		// String filterCondition = null;
		// try {
		// filterCondition = StringUtils.replaceParameters(filter.getFilterCondition(), "F", props);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// if(filterCondition != null) {
		// if(buffer.toString().length() > 0) {
		// buffer.append(" and ");
		// } else {
		// buffer.append("where ");
		// }
		// buffer.append(filterCondition + " ");
		// }
		// }
		//
		//
		//
		// if(dataMartModelAccessModality.getRecursiveFiltering() == null
		// || dataMartModelAccessModality.getRecursiveFiltering().booleanValue() == true) {
		// // check for condition filter on sub entities
		// List subEntities = entity.getAllSubEntities();
		// for(int i = 0; i < subEntities.size(); i++) {
		// IModelEntity subEntity = (IModelEntity)subEntities.get(i);
		// filters = dataMartModelAccessModality.getEntityFilterConditions(subEntity.getType());
		// for(int j = 0; j < filters.size(); j++) {
		// Filter filter = (Filter)filters.get(j);
		// Set fields = filter.getFields();
		// Properties props = new Properties();
		// Iterator fieldIterator = fields.iterator();
		// while(fieldIterator.hasNext()) {
		// String fieldName = (String)fieldIterator.next();
		// IModelField filed = null;
		// Iterator subEntityFields = subEntity.getAllFields().iterator();
		// while(subEntityFields.hasNext()) {
		// filed = (IModelField)subEntityFields.next();
		// if(( (String)filed.getQueryName().getFirst()).endsWith("." + fieldName)) break;
		// }
		// String entityAlias = (String)entityAliases.get(entityUniqueName);
		// props.put(fieldName, entityAlias + "." + filed.getQueryName().getFirst());
		// }
		// String filterCondition = null;
		// try {
		// filterCondition = StringUtils.replaceParameters(filter.getFilterCondition(), "F", props);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// if(filterCondition != null) {
		// if(buffer.toString().length() > 0) {
		// buffer.append(" and ");
		// } else {
		// buffer.append("where ");
		// }
		// buffer.append(filterCondition + " ");
		// }
		// }
		// }
		//
		// }
		// }

		return buffer.toString().trim();
	}

	private String buildGroupByClause(Query query, Map entityAliasesMaps) {
		StringBuffer buffer = new StringBuffer();
		List groupByFields = query.getGroupByFields();
		String fieldName;
		if (groupByFields == null || groupByFields.size() == 0) {
			return "";
		}

		buffer.append("GROUP BY");

		Map entityAliases = (Map) entityAliasesMaps.get(query.getId());

		Iterator<AbstractSelectField> it = groupByFields.iterator();
		while (it.hasNext()) {
			AbstractSelectField abstractSelectedField = it.next();

			if (abstractSelectedField.isInLineCalculatedField()) {
				InLineCalculatedSelectField icf = (InLineCalculatedSelectField) abstractSelectedField;
				fieldName = parseInLinecalculatedField(icf.getExpression(), query, entityAliasesMaps);
			} else {

				SimpleSelectField groupByField = (SimpleSelectField) abstractSelectedField;
				IModelField datamartField = getDataSource().getModelStructure().getField(groupByField.getUniqueName());
				IModelEntity entity = datamartField.getParent().getRoot();
				String queryName = (String) datamartField.getQueryName().getFirst();
				if (!entityAliases.containsKey(entity.getUniqueName())) {
					entityAliases.put(entity.getUniqueName(), getNextAlias(entityAliasesMaps));
				}
				String entityAlias = (String) entityAliases.get(entity.getUniqueName());
				fieldName = entityAlias + "." + queryName;
			}
			buffer.append(" " + fieldName);
			if (it.hasNext()) {
				buffer.append(",");
			}
		}

		return buffer.toString().trim();
	}

	private List getOrderByFields(Query query) {
		List orderByFields = new ArrayList();
		Iterator it = query.getSimpleSelectFields(false).iterator();
		while (it.hasNext()) {
			SimpleSelectField selectField = (SimpleSelectField) it.next();
			if (selectField.isOrderByField()) {
				orderByFields.add(selectField);
			}
		}
		return orderByFields;
	}

	private String buildOrderByClause(Query query, Map entityAliasesMaps) {
		StringBuffer buffer;
		Iterator it;
		SimpleSelectField selectField;

		it = getOrderByFields(query).iterator();
		if (!it.hasNext()) {
			return "";
		}

		buffer = new StringBuffer();
		buffer.append("ORDER BY");

		Map entityAliases = (Map) entityAliasesMaps.get(query.getId());

		while (it.hasNext()) {
			selectField = (SimpleSelectField) it.next();

			Assert.assertTrue(selectField.isOrderByField(), "Field [" + selectField.getUniqueName() + "] is not an orderBy filed");

			IModelField datamartField = getDataSource().getModelStructure().getField(selectField.getUniqueName());
			IModelEntity entity = datamartField.getParent().getRoot();
			String queryName = (String) datamartField.getQueryName().getFirst();
			if (!entityAliases.containsKey(entity.getUniqueName())) {
				entityAliases.put(entity.getUniqueName(), getNextAlias(entityAliasesMaps));
			}
			String entityAlias = (String) entityAliases.get(entity.getUniqueName());
			String fieldName = entityAlias + "." + queryName;
			buffer.append(" " + selectField.getFunction().apply(fieldName));
			buffer.append(" " + (selectField.isAscendingOrder() ? "ASC" : "DESC"));

			if (it.hasNext()) {
				buffer.append(",");
			}
		}

		return buffer.toString().trim();
	}

	public Set getSelectedEntities() {
		Set selectedEntities;
		Map entityAliasesMaps;
		Iterator entityUniqueNamesIterator;
		String entityUniqueName;
		IModelEntity entity;

		Assert.assertNotNull(getQuery(), "Input parameter 'query' cannot be null");
		Assert.assertTrue(!getQuery().isEmpty(), "Input query cannot be empty (i.e. with no selected fields)");

		selectedEntities = new HashSet();

		// one map of entity aliases for each queries (master query + subqueries)
		// each map is indexed by the query id
		entityAliasesMaps = new HashMap();

		// let's start with the query at hand
		entityAliasesMaps.put(getQuery().getId(), new HashMap());

		buildSelectClause(getQuery(), entityAliasesMaps);
		buildWhereClause(getQuery(), entityAliasesMaps);
		buildGroupByClause(getQuery(), entityAliasesMaps);
		buildOrderByClause(getQuery(), entityAliasesMaps);
		buildFromClause(getQuery(), entityAliasesMaps);

		Map entityAliases = (Map) entityAliasesMaps.get(getQuery().getId());
		entityUniqueNamesIterator = entityAliases.keySet().iterator();
		while (entityUniqueNamesIterator.hasNext()) {
			entityUniqueName = (String) entityUniqueNamesIterator.next();
			// entity = getDataMartModel().getDataMartModelStructure().getRootEntity( entityUniqueName );
			entity = getDataSource().getModelStructure().getEntity(entityUniqueName);
			selectedEntities.add(entity);
		}

		return selectedEntities;
	}

	/*
	 * internally used to generate the parametric statement string. Shared by the prepare method and the buildWhereClause method in order to recursively
	 * generate subquery statement string to be embedded in the parent query.
	 */
	private String compose(Query query, Map entityAliasesMaps) {
		String queryStr;
		String selectClause;
		String whereClause;
		String groupByClause;
		String orderByClause;
		String fromClause;
		String havingClause;

		Assert.assertNotNull(query, "Input parameter 'query' cannot be null");
		Assert.assertTrue(!query.isEmpty(), "Input query cannot be empty (i.e. with no selected fields)");

		// let's start with the query at hand
		entityAliasesMaps.put(query.getId(), new HashMap());

		selectClause = buildSelectClause(query, entityAliasesMaps);
		whereClause = buildWhereClause(query, entityAliasesMaps);
		groupByClause = buildGroupByClause(query, entityAliasesMaps);
		orderByClause = buildOrderByClause(query, entityAliasesMaps);
		fromClause = buildFromClause(query, entityAliasesMaps);
		havingClause = buildHavingClause(query, entityAliasesMaps);

		queryStr = selectClause + " " + fromClause + " " + whereClause + " " + groupByClause + " " + havingClause + " " + orderByClause;

		Set subqueryIds;
		try {
			subqueryIds = StringUtils.getParameters(queryStr, "Q");
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Impossible to set parameters in query", e);
		}

		Iterator it = subqueryIds.iterator();
		while (it.hasNext()) {
			String id = (String) it.next();
			Query subquery = query.getSubquery(id);

			String subqueryStr = compose(subquery, entityAliasesMaps);
			queryStr = queryStr.replaceAll("Q\\{" + subquery.getId() + "\\}", subqueryStr);
		}

		return queryStr;
	}

	public void prepare() {
		String queryStr;

		// one map of entity aliases for each queries (master query + subqueries)
		// each map is indexed by the query id
		Map entityAliasesMaps = new HashMap();

		queryStr = compose(getQuery(), entityAliasesMaps);

		if (getParameters() != null) {
			try {
				queryStr = StringUtils.replaceParameters(queryStr.trim(), "P", getParameters());
			} catch (IOException e) {
				throw new SpagoBIRuntimeException("Impossible to set parameters in query", e);
			}

		}

		setQueryString(queryStr);
	}

	@Override
	public String getQueryString() {
		if (super.getQueryString() == null) {
			this.prepare();
		}

		return super.getQueryString();
	}

	public String getSqlQueryString() {
		String sqlQuery = null;
		Session session = null;
		HQL2SQLStatementRewriter queryRewriter;
		try {
			session = ((IHibernateDataSource) getDataSource()).getHibernateSessionFactory().openSession();
			queryRewriter = new HQL2SQLStatementRewriter(session);
			sqlQuery = queryRewriter.rewrite(getQueryString());
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

		return sqlQuery;
	}

	@Override
	public String toString() {
		return this.getQueryString();
	}
}
