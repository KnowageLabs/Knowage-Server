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

import java.util.Arrays;
import java.util.List;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.metasql.query.visitor.ISelectQueryVisitor;
import it.eng.spagobi.utilities.assertion.Assert;

public class NotInFilter extends MultipleProjectionSimpleFilter {

	private List<Object> operands;

	public NotInFilter(Projection projection, Object value) {
		this(Arrays.asList(projection), Arrays.asList(value));
	}

	public NotInFilter(Projection projection, List<Object> values) {
		this(Arrays.asList(projection), values);
	}

	public NotInFilter(List<Projection> projections, List<Object> values) {
		Assert.assertNotNull(projections, "Projections can't be null");
		Assert.assertTrue(!projections.isEmpty(), "Projections can't be empty");
		Assert.assertNotNull(values, "Values can't be null");
		Assert.assertTrue(values.size() % projections.size() == 0, "Missing values respect to count of projections");

		this.projections = projections;
		this.operator = SimpleFilterOperator.NOT_IN;
		this.operands = values;
	}

	public List<Object> getOperands() {
		return operands;
	}

	@Override
	public void accept(ISelectQueryVisitor v) {
		v.visit(this);
	}

	@Override
	public IDataSet getDataset() {
		return projections.get(0).getDataset();
	}
}
