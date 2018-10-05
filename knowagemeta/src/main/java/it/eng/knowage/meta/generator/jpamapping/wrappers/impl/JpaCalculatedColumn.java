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
package it.eng.knowage.meta.generator.jpamapping.wrappers.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.knowage.meta.exception.KnowageMetaException;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaCalculatedColumn;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaColumn;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaTable;
import it.eng.knowage.meta.model.ModelProperty;
import it.eng.knowage.meta.model.business.CalculatedBusinessColumn;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;
import it.eng.qbe.utility.CustomFunctionsSingleton;
import it.eng.qbe.utility.CustomizedFunctionsReader;
import it.eng.qbe.utility.DbTypeThreadLocal;
import it.eng.qbe.utility.bo.CustomizedFunction;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JpaCalculatedColumn implements IJpaCalculatedColumn {
	public static final String CALCULATED_COLUMN_EXPRESSION = "structural.expression";
	public static final String CALCULATED_COLUMN_DATATYPE = "structural.datatype";
	public static final String CALCULATED_COLUMN_NATURE = "structural.columntype";

	CalculatedBusinessColumn businessCalculatedColumn;
	AbstractJpaTable jpaTable;

	private static Logger logger = LoggerFactory.getLogger(JpaCalculatedColumn.class);

	/**
	 *
	 * @param parentTable
	 *            the jpaTable that contains this column
	 * @param businessColumn
	 *            the wrapped business column
	 */
	protected JpaCalculatedColumn(AbstractJpaTable parentTable, CalculatedBusinessColumn businessCalculatedColumn) {
		this.jpaTable = parentTable;
		this.businessCalculatedColumn = businessCalculatedColumn;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaColumn#getName()
	 */
	@Override
	public String getName() {
		return businessCalculatedColumn.getName();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaColumn#getDescription()
	 */
	@Override
	public String getDescription() {
		return businessCalculatedColumn.getDescription() != null ? businessCalculatedColumn.getDescription() : getName();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.meta.generator.jpamapping.wrappers.impl.IJpaColumn#getJpaTable()
	 */
	@Override
	public IJpaTable getJpaTable() {
		return jpaTable;
	}

	public String getParentUniqueNameWithDoubleDots() {
		return jpaTable.getUniqueNameWithDoubleDots();
	}

	@Override
	public String getAttribute(String name) {
		ModelProperty property = businessCalculatedColumn.getProperties().get(name);
		return property != null ? property.getValue() : "";
	}

	@Override
	public String getExpression() {
		ModelProperty property = businessCalculatedColumn.getProperties().get(CALCULATED_COLUMN_EXPRESSION);
		return property != null ? property.getValue() : "";
	}

	@Override
	public String getDataType() {
		ModelProperty property = businessCalculatedColumn.getProperties().get(CALCULATED_COLUMN_DATATYPE);
		return property != null ? property.getValue() : "";
	}

	public Set<IJpaColumn> getReferencedColumns() {
		Set<IJpaColumn> jpaColumns = new HashSet<IJpaColumn>();

		try {
			Set<SimpleBusinessColumn> businessColumns = businessCalculatedColumn.getReferencedColumns();
			if (!businessColumns.isEmpty()) {
				for (SimpleBusinessColumn businessColumn : businessColumns) {
					JpaColumn jpaColumn = new JpaColumn(jpaTable, businessColumn);
					jpaColumns.add(jpaColumn);
					logger.debug("Calculated Column [{}] references column [{}]", this.getName(), businessColumn.getName());
				}
			}
		} catch (KnowageMetaException e) {
			logger.error("Calculated Column in JpaCalculatedColumn error: ");
			logger.error(e.getMessage());
		}
		return jpaColumns;
	}

	public String getExpressionWithUniqueNames() {
		String expression = getExpression();
		Set<IJpaColumn> jpaColumns = this.getReferencedColumns();
		Set<String> operands = new HashSet<String>();

		if (!jpaColumns.isEmpty()) {
			// retrieve operands from string
			// StringTokenizer stk = new StringTokenizer(expression, "+-|*/()");

			String regularExpression = "(\\,|\\+|\\-|\\*|\\(|\\)|\\|\\||\\/|GG_between_dates|MM_between_dates|AA_between_dates|GG_up_today|MM_up_today|AA_up_today|current_date|current_time|length|substring|concat|year|month|mod|bit_length|upper|lower|trim|current_timestamp|hour|minute|second|day";

			// add custom functions if present
			String customs = "";
			JSONObject json = CustomFunctionsSingleton.getInstance().getCustomizedFunctionsJSON();
			// check there really are some custom functions
			if (json != null && !json.toString().equals("{}")) {
				String dbType = DbTypeThreadLocal.getDbType();
				if (dbType == null) {
					logger.error("Db Type not found");
					throw new RuntimeException("Db Type name could not be found in current Thread Locale, check stack of calls");
				}
				CustomizedFunctionsReader reader = new CustomizedFunctionsReader();
				List<CustomizedFunction> list = reader.getCustomDefinedFunctionListFromJSON(json, dbType);
				if (list != null && list.size() > 0) {
					customs = reader.getStringFromOrderedList(list);
				}
			}

			logger.debug("Customs functions definition " + customs);
			regularExpression += customs;

			regularExpression += ")";

			String[] splittedExpr = expression.split(regularExpression);
			for (String operand : splittedExpr) {
				operand = operand.trim();

				if (NumberUtils.isNumber(operand)) {
					continue;
				}

				logger.debug("Found Operand " + operand);
				operands.add(operand);
			}
			operands.removeAll(Arrays.asList("", null));
		}

		for (String operand : operands) {
			// search if the operand is a column of the metamodel (it could be a fixed value)
			String uniqueName = getUniqueNameOfOperand(jpaColumns, operand);
			if (uniqueName != null) {
				logger.debug("Replacing " + operand + " with " + uniqueName);
				expression = expression.replace(operand, uniqueName);
			}

		}
		return expression;
	}

	public String getUniqueNameOfOperand(Set<IJpaColumn> jpaColumns, String operandToFind) {
		for (IJpaColumn jpaColumn : jpaColumns) {
			if (jpaColumn.getName().equals(operandToFind)) {
				return jpaColumn.getUniqueName().replaceAll("/", ":");
			}
		}
		return null;
	}

	@Override
	public String getNature() {
		ModelProperty property = businessCalculatedColumn.getProperties().get(CALCULATED_COLUMN_NATURE);
		return property != null ? property.getValue() : "";
	}

}
