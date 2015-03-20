package it.eng.spagobi.engines.datamining.api;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.common.AbstractDataMiningEngineService;
import it.eng.spagobi.engines.datamining.compute.DataMiningExecutor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;

@Path("/1.0/execute")
public class ExternalResource extends AbstractDataMiningEngineService {
	public static transient Logger logger = Logger.getLogger(ExternalResource.class);

	/**
	 * Service to execute an external script *
	 *
	 * @return
	 *
	 */
	// http://localhost:8080/SpagoBIDataMiningEngine/restful-services/1.0/execute/test1.R?user_id=biadmin&search_id=14&libraries=vegan,ellipse
	@GET
	@Path("/{fileName}")
	@Produces("text/html; charset=UTF-8")
	public String executeScript(@PathParam("fileName") String fileName, @Context HttpServletRequest request) {
		logger.debug("IN");
		HashMap<String, String> params = new HashMap<String, String>();
		Map<String, String[]> map = request.getParameterMap();

		if (map != null && !map.isEmpty()) {
			logger.debug("Got parameters map");
			Set set = map.entrySet();
			Iterator it = set.iterator();
			while (it.hasNext()) {
				Map.Entry<String, String[]> entry = (Entry<String, String[]>) it.next();
				String paramName = entry.getKey();
				String[] paramValues = entry.getValue();
				String paramValue = "";
				if (paramValues.length == 1) {
					paramValue = paramValues[0];
				} else {
					for (int i = 0; i < paramValues.length; i++) {
						paramValue += paramValues[i] + ",";
					}
				}
				params.put(paramName, paramValue);
			}
		}
		DataMiningEngineInstance dataMiningEngineInstance = getDataMiningEngineInstance();

		DataMiningExecutor executor = new DataMiningExecutor(dataMiningEngineInstance, getUserProfile());
		try {
			UserProfile profile = getUserProfile();
			logger.debug("Got user profile");
			// if(profile != null){
			executor.externalExecution(fileName, getUserProfile(), params);
			logger.debug("Executed script for file " + fileName);
			// }else{
			// logger.error("Missing authentication");
			// return getJsonKo();
			// }

		} catch (Exception e) {
			logger.error(e);
			return getJsonKo();
		}

		logger.debug("OUT");
		return getJsonOk();
	}
}
