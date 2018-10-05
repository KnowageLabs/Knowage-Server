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

public class NullaryFilter extends SingleProjectionSimpleFilter {

	public NullaryFilter(IDataSet dataSet, String columnAliasOrName, SimpleFilterOperator operatorType) {
		this(new Projection(dataSet, columnAliasOrName), operatorType);
	}

	public NullaryFilter(IAggregationFunction aggregationFunction, IDataSet dataSet, String columnAliasOrName, SimpleFilterOperator operatorType) {
		this(new Projection(aggregationFunction, dataSet, columnAliasOrName), operatorType);
	}

	public NullaryFilter(Projection projection, SimpleFilterOperator operator) {
		Assert.assertNotNull(projection, "Projection can't be null");
		Assert.assertNotNull(operator, "Operator can't be null");
		Assert.assertTrue(operator.isNullary(), "Operator is not nullary");

		this.projection = projection;
		this.operator = operator;
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
