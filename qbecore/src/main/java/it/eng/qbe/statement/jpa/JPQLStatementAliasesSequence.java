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
package it.eng.qbe.statement.jpa;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JPQLStatementAliasesSequence {
	
	// one map of entity aliases for each queries (master query + subqueries)
	// each map is indexed by the query id
	Map<String, Map<String, String>> entityAliasesMaps;
	
	
	protected JPQLStatementAliasesSequence() {
		entityAliasesMaps = new HashMap<String, Map<String, String>>();
	}
	
	public void clear() {
		entityAliasesMaps = new HashMap<String, Map<String, String>>();
	}
	
	public String getNextAlias() {
		int aliasesCount = 0;
		Iterator it = entityAliasesMaps.keySet().iterator();
		while(it.hasNext()) {
			String key = (String)it.next();
			Map entityAliases = (Map)entityAliasesMaps.get(key);
			aliasesCount += entityAliases.keySet().size();
		}
		
		return "t_" + aliasesCount;
	}
}
