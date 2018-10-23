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

package it.eng.spagobi.tools.dataset.metasql.query.item;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.metasql.query.visitor.ISelectQueryVisitor;
import it.eng.spagobi.utilities.assertion.Assert;

public class UnaryFilter extends SingleProjectionSimpleFilter {

	private Object operand;

	public UnaryFilter(IDataSet dataSet, String columnAliasOrName, SimpleFilterOperator operatorType, Object operand) {
		this(new Projection(dataSet, columnAliasOrName), operatorType, operand);
	}

	public UnaryFilter(IAggregationFunction aggregationFunction, IDataSet dataSet, String columnAliasOrName, SimpleFilterOperator operatorType,
			Object operand) {
		this(new Projection(aggregationFunction, dataSet, columnAliasOrName), operatorType, operand);
	}

	public UnaryFilter(Projection projection, SimpleFilterOperator operator, Object operand) {
		Assert.assertNotNull(projection, "Projection can't be null");
		Assert.assertNotNull(operator, "Operator can't be null");
		Assert.assertTrue(operator.isUnary(), "Operator in not unary");

		this.projection = projection;
		this.operator = operator;
		this.operand = operand;
	}

	public Object getOperand() {
		return operand;
	}

	@Override
	public void accept(ISelectQueryVisitor v) {
		v.visit(this);
	}

	@Override
	public IDataSet getDataset() {
		return projection.getDataset();
	}

}
