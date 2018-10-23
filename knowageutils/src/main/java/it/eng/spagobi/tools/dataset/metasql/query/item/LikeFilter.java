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
import it.eng.spagobi.tools.dataset.metasql.query.visitor.ISelectQueryVisitor;
import it.eng.spagobi.utilities.assertion.Assert;

public class LikeFilter extends SingleProjectionSimpleFilter {

	private String value;

	private TYPE valueType;

	public LikeFilter(Projection projection, String value, TYPE valueType) {
		Assert.assertNotNull(projection, "Projection can't be null");
		Assert.assertNotNull(value, "Value can't be null");
		Assert.assertNotNull(valueType, "Value type can't be null");
		this.projection = projection;
		this.operator = SimpleFilterOperator.LIKE;
		this.value = value;
		this.valueType = valueType;
	}

	public String getValue() {
		return value;
	}

	public TYPE getValueType() {
		return valueType;
	}

	public boolean isPattern() {
		return valueType.equals(TYPE.PATTERN);
	}

	@Override
	public void accept(ISelectQueryVisitor v) {
		v.visit(this);
	}

	@Override
	public IDataSet getDataset() {
		return projection.getDataset();
	}

	public enum TYPE {SIMPLE, PATTERN}
}
