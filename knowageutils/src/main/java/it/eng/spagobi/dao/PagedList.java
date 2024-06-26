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
import java.util.List;

/**
 * This class is useful for server-side paginated list services. It represents the result of a paginated list service, providing items of a particular page,
 * plus the total number of items as returned by the query and the starting index of the provided results with respect to the complete list. Example: query is
 * returning 5000 rows, but required page should display records from 150 to 200, then results property should contain items from 150 to 200, total property
 * should be 5000 and start should be 150.
 *
 * @author zerbetto
 *
 * @param <T>
 *
 */
public class PagedList<T> {

	private List<T> results = null;

	private int total;

	private int start;

	public PagedList() {
		this.results = new ArrayList<T>();
		this.total = 0;
		this.start = 0;
	}

	public PagedList(List<T> results, int total, int start) {
		this.results = results;
		this.total = total;
		this.start = start;
	}

	public List<T> getResults() {
		return results;
	}

	public void setResults(List<T> results) {
		this.results = results;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public static <T> PagedList<T> emptyList(Class<T> type) {
		return new PagedList<T>(new ArrayList<T>(), 0, 0);
	}

}
