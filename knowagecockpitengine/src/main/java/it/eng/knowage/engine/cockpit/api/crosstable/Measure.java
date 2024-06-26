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
package it.eng.knowage.engine.cockpit.api.crosstable;

import org.json.JSONObject;

import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;

public class Measure extends Field {
	Boolean excludeFromTotalAndSubtotal;
	IAggregationFunction function = null;
	String variable = null;

	public Measure(String entityId, String alias, String sortingId, String iconCls, String nature, String function, String variable, Boolean excludeFromTotalAndSubtotal,
			JSONObject config) {
		super(entityId, alias, sortingId, iconCls, nature, config);
		this.function = AggregationFunctions.get(function);
		this.excludeFromTotalAndSubtotal = excludeFromTotalAndSubtotal;
		this.variable = variable;
	}

	public IAggregationFunction getAggregationFunction() {
		return function;
	}
	
	public JSONObject getConfig () {
		return config;
	}
	
	public String getVariable() {
		return variable;
	}

	public Boolean getExcludeFromTotalAndSubtotal() {
		return excludeFromTotalAndSubtotal;
	}

	public void setExcludeFromTotalAndSubtotal(Boolean excludeFromTotalAndSubtotal) {
		this.excludeFromTotalAndSubtotal = excludeFromTotalAndSubtotal;
	}
}