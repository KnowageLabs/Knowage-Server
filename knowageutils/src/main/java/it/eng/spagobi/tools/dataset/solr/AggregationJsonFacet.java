/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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
 *
 */

package it.eng.spagobi.tools.dataset.solr;

import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AggregationJsonFacet extends JsonFacet {

    private static final Logger logger = Logger.getLogger(AggregationJsonFacet.class);

    private final String sort;
    private final Map<String,String> facet = new HashMap<>(1);
    protected int limit = 10;
    
    public AggregationJsonFacet(String field, IAggregationFunction function, String columnToAggregate, int limit) {
        super(field);
        facet.put(function.getName().toLowerCase(), function.getName().toLowerCase() + "(" + columnToAggregate + ")");
        this.sort = function.getName().toLowerCase() + " desc";
        this.limit = limit;
    }

    public Map<String, String> getFacet() {
        return facet;
    }

    public String getSort() {
        return sort;
    }
	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

}
