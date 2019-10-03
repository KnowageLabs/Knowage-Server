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

import org.apache.log4j.Logger;

class CountJsonFacet extends JsonFacet {

    private static final Logger logger = Logger.getLogger(CountJsonFacet.class);

    private int mincount = 1;

    private int limit = 10;

    public CountJsonFacet(String field) {
        super(field);

    }

    public CountJsonFacet(String field, int limit) {
        super(field,limit);
        this.limit = limit;
    }

    public CountJsonFacet(String field, int limit, int mincount) {
        super(field,limit);
        this.limit = limit;
        this.mincount = mincount;
    }

	@Override
	public int getLimit() {
		return limit;
	}


	public int getMincount() {
		return mincount;
	}

	public void setMincount(int mincount) {
		this.mincount = mincount;
	}

	@Override
	public void setLimit(int limit) {
		this.limit = limit;
	}

}
