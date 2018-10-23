package it.eng.spagobi.tools.dataset.cache.client;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import it.eng.spagobi.tools.dataset.common.datareader.JSONDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.engines.rest.SimpleRestClient;

public class CacheClient extends SimpleRestClient {

	private static final String SERVICE_URL = "/restful-services/1.0/cache";

	public IDataStore updateDataSet(String signature, IDataStore dataStore, boolean realtimeNgsiConsumer, String userId) throws Exception {
		Map<String, Object> parameters = new HashMap<>(1);
		parameters.put("realtimeNgsiConsumer", realtimeNgsiConsumer);
		Response response = executePutService(parameters, SERVICE_URL + "/dataset/" + Helper.sha256(signature), userId, MediaType.APPLICATION_JSON,
				new JSONDataWriter().write(dataStore));
		if (response.getStatusInfo() != Status.OK) {
			logger.error("Impossible to apply updates. Error while executing dataset with signature [" + signature + "] in cache for user [" + userId + "]");
			logger.error("Response status " + response.getStatus());
			logger.error("Response reason " + response.getStatusInfo().getReasonPhrase());
			return null;
		}
		String responseString = response.readEntity(String.class);
		IDataStore result = new JSONDataReader().read(responseString);
		return result;

	}

}
