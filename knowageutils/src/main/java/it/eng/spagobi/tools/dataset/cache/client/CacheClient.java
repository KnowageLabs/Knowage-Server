package it.eng.spagobi.tools.dataset.cache.client;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.engines.rest.SimpleRestClient;

public class CacheClient extends SimpleRestClient {

	private static final String SERVICE_URL = "/restful-services/1.0/cache";

	public IDataStore updateDataSet(String signature, IDataStore dataStore, boolean realtimeNgsiConsumer, String userId) throws Exception {
		Map<String, Object> parameters = new HashMap<>(1);
		parameters.put("realtimeNgsiConsumer", realtimeNgsiConsumer);
		Response response = executePostService(parameters, SERVICE_URL + "/dataset/" + Helper.sha256(signature), userId, MediaType.APPLICATION_JSON,
				JsonConverter.objectToJson(dataStore, IDataStore.class));
		if (response.getStatusInfo() != Status.OK) {
			logger.error("Impossible to apply updates. Error while executing dataset with signature [" + signature + "] in cache for user [" + userId + "]");
			logger.debug("Response status " + response.getStatus());
			logger.debug("Response reason " + response.getStatusInfo().getReasonPhrase());
			return null;
		}
		return response.readEntity(IDataStore.class);

	}

}
