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
package it.eng.qbe.dataset;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.query.ExpressionNode;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.WhereField.Operand;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.qbe.statement.AbstractStatement;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class QueryTransformer {

	public static Query transform(Query query, IDataSource dataSource, Locale locale, Map<String, List<String>> filters) throws Exception {
		String filterKey;
		List<String> fliterValues;
		Operand filterLeftOperand, filterRightOperand;
		String[] filterLeftOperandValues, filterRightOperandValues;
		Iterator<String> keyIter = filters.keySet().iterator();
		int i = 0;
		String operator;
		String store = ((JSONObject) SerializerFactory.getSerializer("application/json").serialize(query, dataSource, locale)).toString();
		Query clonedQuery = SerializerFactory.getDeserializer("application/json").deserializeQuery(store, dataSource);

		while (keyIter.hasNext()) {
			filterKey = keyIter.next();
			fliterValues = filters.get(filterKey);
			filterLeftOperandValues = new String[] { filterKey };
			filterRightOperandValues = fliterValues.toArray(new String[0]);
			filterLeftOperand = new Operand(filterLeftOperandValues, filterKey, AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD, null, null, "");
			filterRightOperand = new Operand(filterRightOperandValues, "values for [" + filterKey + "]", AbstractStatement.OPERAND_TYPE_STATIC, null, null, "");
			operator = "EQUALS TO";
			if (filterRightOperandValues.length > 1) {
				operator = "IN";
			}
			WhereField wf = clonedQuery.addWhereField("DataSetFilter" + i, "DataSetFilter" + i, false, filterLeftOperand, operator, filterRightOperand, "AND");
			updateWhereClauseStructure(clonedQuery, wf.getName(), "AND");
		}

		return clonedQuery;
	}

	private static void updateWhereClauseStructure(Query query, String filterId, String booleanConnector) {
		ExpressionNode node = query.getWhereClauseStructure();
		ExpressionNode newFilterNode = new ExpressionNode("NODE_CONST", "$F{" + filterId + "}");
		if (node == null) {
			node = newFilterNode;
			query.setWhereClauseStructure(node);
		} else {
			if (node.getType() == "NODE_OP" && node.getValue().equals(booleanConnector)) {
				node.addChild(newFilterNode);
			} else {
				ExpressionNode newNode = new ExpressionNode("NODE_OP", booleanConnector);
				newNode.addChild(node);
				newNode.addChild(newFilterNode);
				query.setWhereClauseStructure(newNode);
			}
		}
	}
}
