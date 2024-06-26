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

package it.eng.spagobi.tools.dataset.metasql.query;

import java.util.ArrayList;
import java.util.List;

public class PreparedStatementData {

	private String query;
	private List<Object> parameters;

	public PreparedStatementData(String query, List<Object> values) {
		this.query = query;

		if (values == null) {
			this.parameters = new ArrayList<Object>(0);
		} else {
			this.parameters = values;
		}
	}

	public String getQuery() {
		return query;
	}

	public List<Object> getValues() {
		return parameters;
	}

	@Override
	public String toString() {
		return "PreparedStatementData [query=" + query + ", parameters=" + parameters + "]";
	}

}
