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

package it.eng.qbe.statement.sql;

import java.util.HashMap;
import java.util.Map;

import it.eng.qbe.query.CriteriaConstants;
import it.eng.qbe.statement.IConditionalOperator;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class SQLStatementConditionalOperators {
	private static Map<String, IConditionalOperator> conditionalOperators;

	public static IConditionalOperator getOperator(String operatorName) {
		return conditionalOperators.get(operatorName);
	}

	static {
		conditionalOperators = new HashMap<String, IConditionalOperator>();
		conditionalOperators.put(CriteriaConstants.EQUALS_TO, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.EQUALS_TO;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + "=" + rightHandValues[0];
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_EQUALS_TO, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.NOT_EQUALS_TO;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + "!=" + rightHandValues[0];
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.GREATER_THAN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.GREATER_THAN;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + ">" + rightHandValues[0];
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.EQUALS_OR_GREATER_THAN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.EQUALS_OR_GREATER_THAN;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + ">=" + rightHandValues[0];
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.LESS_THAN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.LESS_THAN;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + "<" + rightHandValues[0];
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.EQUALS_OR_LESS_THAN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.EQUALS_OR_LESS_THAN;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				return leftHandValue + "<=" + rightHandValues[0];
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.STARTS_WITH, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.STARTS_WITH;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				if (rightHandValue.startsWith("'")) {
					rightHandValue = rightHandValue.substring(1, rightHandValue.length() - 1);
					rightHandValue = rightHandValue + "%";
				} else {
					// field reference
					rightHandValue = "' || " + rightHandValue + " || '%";
				}
				return leftHandValue + " like '" + rightHandValue + "'";
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_STARTS_WITH, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.NOT_STARTS_WITH;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				if (rightHandValue.startsWith("'")) {
					rightHandValue = rightHandValue.substring(1, rightHandValue.length() - 1);
					rightHandValue = rightHandValue + "%";
				} else {
					// field reference
					rightHandValue = "' || " + rightHandValue + " || '%";
				}
				return leftHandValue + " not like '" + rightHandValue + "'";
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.ENDS_WITH, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.ENDS_WITH;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				if (rightHandValue.startsWith("'")) {
					rightHandValue = rightHandValue.substring(1, rightHandValue.length() - 1);
					rightHandValue = "%" + rightHandValue;
				} else {
					// field reference
					rightHandValue = "%' || " + rightHandValue + " || '";
				}
				return leftHandValue + " like '" + rightHandValue + "'";
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_ENDS_WITH, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.NOT_ENDS_WITH;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				if (rightHandValue.startsWith("'")) {
					rightHandValue = rightHandValue.substring(1, rightHandValue.length() - 1);
					rightHandValue = "%" + rightHandValue;
				} else {
					// field reference
					rightHandValue = "%' || " + rightHandValue + " || '";
				}
				return leftHandValue + " not like '" + rightHandValue + "'";
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.CONTAINS, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.CONTAINS;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				if (rightHandValue.startsWith("'")) {
					rightHandValue = rightHandValue.substring(1, rightHandValue.length() - 1);
					rightHandValue = "%" + rightHandValue + "%";
				} else {
					// field reference
					rightHandValue = "%' || " + rightHandValue + " || '%";
				}
				return leftHandValue + " like '" + rightHandValue + "'";
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_CONTAINS, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.NOT_CONTAINS;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues[0] != null, "Operand cannot be null when the operator is " + getName());
				String rightHandValue = rightHandValues[0].trim();
				if (rightHandValue.startsWith("'")) {
					rightHandValue = rightHandValue.substring(1, rightHandValue.length() - 1);
					rightHandValue = "%" + rightHandValue + "%";
				} else {
					// field reference
					rightHandValue = "%' || " + rightHandValue + " || '%";
				}
				return leftHandValue + " not like '" + rightHandValue + "'";
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.IS_NULL, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.IS_NULL;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValue) {
				return leftHandValue + " IS NULL";
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_NULL, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.NOT_NULL;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValue) {
				return leftHandValue + " IS NOT NULL";
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});

		conditionalOperators.put(CriteriaConstants.BETWEEN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.BETWEEN;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues.length == 2,
						"When BEETWEEN operator is used the operand must contain minValue and MaxValue");
				return leftHandValue + " BETWEEN " + rightHandValues[0] + " AND " + rightHandValues[1];
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_BETWEEN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.NOT_BETWEEN;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				Assert.assertTrue(rightHandValues != null && rightHandValues.length == 2,
						"When BEETWEEN operator is used the operand must contain minValue and MaxValue");
				return leftHandValue + " NOT BETWEEN " + rightHandValues[0] + " AND " + rightHandValues[1];
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});

		conditionalOperators.put(CriteriaConstants.IN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.IN;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				String rightHandValue = StringUtils.join(rightHandValues, ",");
				return leftHandValue + " IN (" + rightHandValue + ")";
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
		conditionalOperators.put(CriteriaConstants.NOT_IN, new IConditionalOperator() {
			public String getName() {
				return CriteriaConstants.NOT_IN;
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues) {
				String rightHandValue = StringUtils.join(rightHandValues, ",");
				return leftHandValue + " NOT IN (" + rightHandValue + ")";
			}

			@Override
			public String apply(String leftHandValue, String[] rightHandValues, String param) {
				return this.apply(leftHandValue, rightHandValues);
			}
		});
	}
}
