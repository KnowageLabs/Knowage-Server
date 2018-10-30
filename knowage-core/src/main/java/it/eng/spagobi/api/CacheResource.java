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
package it.eng.spagobi.api;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.common.datareader.JSONDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
@Path("/1.0/cache")
public class CacheResource extends AbstractSpagoBIResource {

	static private Logger logger = Logger.getLogger(CacheResource.class);

	@POST
	@Path("/clean-datasets")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteDatasets(String body) {
		logger.debug("IN");

		try {
			IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
			dataSetDAO.setUserProfile(getUserProfile());

			JSONObject jsonBody = new JSONObject(body);
			Iterator<String> it = jsonBody.keys();
			while (it.hasNext()) {
				String label = it.next();
				logger.debug("Dataset with label [" + label + "] must be deleted from cache.");
				IDataSet dataSet = dataSetDAO.loadDataSetByLabel(label);

				String params = jsonBody.optString(label, null);
				logger.debug("Dataset with label [" + label + "] has the following parameters [" + params + "].");

				Map<String, String> parametersValues = DataSetUtilities.getParametersMap(params);
				dataSet.setParametersMap(parametersValues);
				dataSet.resolveParameters();
				ICache cache = CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
				if (cache.delete(dataSet)) {
					logger.debug("Dataset with label [" + label + "] found in cache and deleted.");
				}
			}
		} catch (Exception e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(),
					"An unexpected error occurred while deleting datasets from cache (deleteItem REST service)", e);
		}
		return Response.ok().build();
	}

	@PUT
	@Path("/dataset/{hashedSignature}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDataset(@PathParam("hashedSignature") String hashedSignature, @QueryParam("realtimeNgsiConsumer") boolean realtimeNgsiConsumer,
			String body) {
		logger.debug("IN");
		Helper.checkNotNullNotTrimNotEmpty(hashedSignature, "hashedSignature");
		Helper.checkNotNull(body, "body");

		IDataStore result = null;
		try {
			ICache cache = CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
			IDataStore dataStore = new JSONDataReader().read(body);
			cache.update(hashedSignature, dataStore);
			logger.debug("Dataset with hashed signature [" + hashedSignature + "] found in cache and updated.");

			if (realtimeNgsiConsumer) {
				result = cache.get(hashedSignature, true);
			} else {
				result = new DataStore();
			}
		} catch (Exception e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occurred while updating dataset on cache", e);
		}

		if (result != null) {
			return Response.ok(new JSONDataWriter().write(result).toString()).build();
		} else {
			return Response.serverError().build();
		}
	}
}
