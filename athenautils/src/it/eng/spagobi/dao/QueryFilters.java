/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
