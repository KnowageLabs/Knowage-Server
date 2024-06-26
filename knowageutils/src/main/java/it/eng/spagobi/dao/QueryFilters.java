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
package it.eng.spagobi.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class QueryFilters {

	private List<QueryFilter> filters = null;
	
	public QueryFilters() {
		this.filters = new ArrayList<QueryFilter>();
	}
	
	public QueryFilters add(QueryFilter filter) {
		filters.add(filter);
		return this;
	}
	
	public Iterator<QueryFilter> iterator() {
		return filters.iterator();
	}
	
	public boolean isEmpty() {
		return filters.isEmpty();
	}
}
