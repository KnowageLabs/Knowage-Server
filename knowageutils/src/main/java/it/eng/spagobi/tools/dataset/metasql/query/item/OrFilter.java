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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.eng.spagobi.tools.dataset.metasql.query.visitor.ISelectQueryVisitor;

public class OrFilter extends CompoundFilter {

	public OrFilter(Filter... filters) {
		this(Arrays.asList(filters));
	}

	public OrFilter(List<? extends Filter> filters) {
		this.compositionOperator = CompoundFilterOperator.OR;
		this.filters = new ArrayList<Filter>(filters);
	}

	public OrFilter or(Filter... filters) {
		return or(Arrays.asList(filters));
	}

	public OrFilter or(List<Filter> filters) {
		this.filters.addAll(filters);
		return this;
	}

	@Override
	public void accept(ISelectQueryVisitor v) {
		v.visit(this);
	}
}
