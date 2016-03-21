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
package it.eng.spagobi.engine.chart.api;

import it.eng.spagobi.engine.chart.ChartEngineConfig;
import it.eng.spagobi.engine.chart.model.conf.ChartConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

@Path("1.0/pages/types")
public class ChartTypeResource {

	static private Logger logger = Logger.getLogger(ChartTypeResource.class);

	@GET
	public JSONObject getChartTypes() {

		Map<String, ChartConfig> chartLibConf = ChartEngineConfig.getChartLibConf();
		Object chartTypes = chartLibConf.keySet().toArray();

		Map<String, Object> types = new HashMap<>();
		types.put("types", chartTypes);

		JSONObject jo = null;

		try {

			jo = new JSONObject(types);

		} catch (JSONException e) {

			// TODO Auto-generated catch block
			logger.error("Failed fatching chart types");

		}

		return jo;
	}

}
