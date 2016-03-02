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

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.ICacheMetadata;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.utilities.cache.CacheItem;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
@Path("/1.0/cache")
public class CacheResource extends AbstractSpagoBIResource {

	static private Logger logger = Logger.getLogger(CacheResource.class);

	
	
	@GET
	@Path("/remove")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_READ })
	public void removeCache() {

		logger.debug("IN");

		try {

			SpagoBICacheManager.removeCache();			
			

		} catch (Exception e) {

			logger.error("Error while loading a single data set", e);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), e);

		} finally {

			logger.debug("OUT");

		}
	}
	
	
	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getCache() {

		logger.debug("IN");
		try {
			ICache cache = SpagoBICacheManager.getCache();
			return serializeCache(cache);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@DELETE
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String deleteCache() {

		logger.debug("IN");
		try {
			ICache cache = SpagoBICacheManager.getCache();
			cache.deleteAll();
			return serializeCache(cache);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * datasetLabels is a string containing all dataset labels divided by ','
	 *
	 * @param datasetLabelsPar
	 */

	@DELETE
	@Path("/{datasetLabels}/cleanCache")
	public void deleteCacheByDatasetLabels(@PathParam("datasetLabels") String datasetLabelsPar) {
		logger.debug("IN");

		logger.debug("clean cache for dataset with labels " + datasetLabelsPar);

		UserProfile profile = this.getIOManager().getUserProfile();

		StringTokenizer st = new StringTokenizer(datasetLabelsPar, ",");

		Vector<String> datasetLabels = new Vector<String>();
		while (st.hasMoreElements()) {
			datasetLabels.add(st.nextElement().toString());
		}

		for (Iterator iterator = datasetLabels.iterator(); iterator.hasNext();) {
			String label = (String) iterator.next();
			try {
				IDataSet dataSet = (new DatasetManagementAPI(profile)).getDataSet(label);
				ICache cache = SpagoBICacheManager.getCache();
				logger.debug("Delete from cache dataset references with signature " + dataSet.getSignature());
				cache.delete(dataSet.getSignature());
			} catch (Throwable t) {
				throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occurred while cleaning cache for dataset with label "
						+ label, t);
			}
		}
		logger.debug("OUT");
		return;
	}

	//DeleteByName
	@PUT
	@Path("/deleteItems")
	@Consumes(MediaType.APPLICATION_JSON)
	public void deleteCacheByDatasetName(String body) {
		logger.debug("IN");
		logger.debug("Clean cache for dataset with names " + body);

		
		UserProfile profile = this.getIOManager().getUserProfile();

		JSONObject jsonObject=null;
		try {
			jsonObject = new JSONObject(body);
			JSONArray jsonArr= jsonObject.getJSONArray("namesArray");
			String[] namesArray=new String[jsonArr.length()];
			for(int i=0;i<jsonArr.length();i++)
			{
				String datasetToRemove=jsonArr.getString(i);
				
				ICache cache = SpagoBICacheManager.getCache();
				cache.delete(datasetToRemove);
								
			}	
		} 
		catch(Exception e)
		{
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occurred while deleting an item from cache (deleteItem REST service)", e);
		}
		
	}
	
	
	
	
	
	@PUT
	@Path("/enable")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateCache(String body) {	//JSON obj

		JSONObject jsonobject=null;
		URI uri=null;
		
		try {
			jsonobject = new JSONObject(body);
			ICache cache = SpagoBICacheManager.getCache();
			//cache.deleteAll();
			boolean enabled=jsonobject.getBoolean("enabled");
			cache.enable(enabled);
			uri = new URI("1.0/cache/enable");
			return Response.created(uri).build();
		} catch (Exception e) {
			logger.error("Error preparing response", e);
			//e.printStackTrace();
			return Response.notModified().build();

		}

	}
	
	

	@GET
	@Path("/meta")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getCacheMetadata() {

		logger.debug("IN");
		try {
			ICache cache = SpagoBICacheManager.getCache();
			ICacheMetadata cacheMetadata = cache.getMetadata();
			return serializeCacheMetadata(cacheMetadata);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/datasource")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getCacheDataSource() {

		logger.debug("IN");
		String label = "";
		try {
			ICache cache = SpagoBICacheManager.getCache();
			if (cache instanceof SQLDBCache) {
				logger.debug("The cache is a SQL cache so we have the datasource");
				label = ((SQLDBCache) cache).getDataSource().getLabel();
				logger.debug("The datasource is " + label);
			}
			return label;
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	private String serializeCacheMetadata(ICacheMetadata cacheMetadata) {
		try {
			JSONArray resultJSON = new JSONArray();
			List<CacheItem> cacheItems = cacheMetadata.getAllCacheItems();
			for (CacheItem item : cacheItems) {
				JSONObject itemJSON = new JSONObject();
				if (item.getName() != null)
					itemJSON.put("name", item.getName());
				if (item.getSignature() != null)
					itemJSON.put("signature", item.getSignature());
				if (item.getTable() != null)
					itemJSON.put("table", item.getTable());
				if (item.getDimension() != null)
					itemJSON.put("dimension", item.getDimension().longValue());
				resultJSON.put(itemJSON);
			}
			return resultJSON.toString();
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while serializing results", t);
		}
	}

	/**
	 * @param cache
	 * @return
	 */
	private String serializeCache(ICache cache) {
		try {
			JSONObject resultJSON = new JSONObject();
			resultJSON.put("enabled", cache.isEnabled());
			resultJSON.put("totalMemory", cache.getMetadata().getTotalMemory().longValue());
			resultJSON.put("availableMemory", cache.getMetadata().getAvailableMemory().longValue());
			resultJSON.put("availableMemoryPercentage", cache.getMetadata().getAvailableMemoryAsPercentage());
			resultJSON.put("cachedObjectsCount", cache.getMetadata().getNumberOfObjects());
			resultJSON.put("cleaningEnabled", cache.getMetadata().isCleaningEnabled());
			resultJSON.put("cleaningQuota", cache.getMetadata().getCleaningQuota() + "%");
			return resultJSON.toString();
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while serializing results", t);
		}
	}
}
