package it.eng.spagobi.engine.chart.api;

import it.eng.spagobi.engine.chart.ChartEngineConfig;
import it.eng.spagobi.engine.chart.model.conf.ChartConfig;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
