package it.eng.spagobi.tools.dataset.cache.client;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spagobi.tools.dataset.common.datareader.JSONDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.engines.rest.SimpleRestClient;

public class CacheClient extends SimpleRestClient {

	private static final Logger LOGGER = LogManager.getLogger(CacheClient.class);
	private static final String SERVICE_URL = "/restful-services/1.0/cache";

	public IDataStore updateDataSet(String signature, IDataStore dataStore, boolean realtimeNgsiConsumer, String userId)
			throws Exception {
		Map<String, Object> parameters = new HashMap<>(1);
		parameters.put("realtimeNgsiConsumer", realtimeNgsiConsumer);
		Response response = executePutService(parameters, SERVICE_URL + "/dataset/" + Helper.sha256(signature), userId,
				MediaType.APPLICATION_JSON, new JSONDataWriter().write(dataStore));
		if (response.getStatusInfo() != Status.OK) {
			LOGGER.error(
					"Impossible to apply updates. Error while executing dataset with signature [{}] in cache for user [{}]",
					signature, userId);
			LOGGER.error("Response status {}", response.getStatus());
			LOGGER.error("Response reason {}", response.getStatusInfo().getReasonPhrase());
			return null;
		}
		String responseString = response.readEntity(String.class);
		return new JSONDataReader().read(responseString);

	}

}
