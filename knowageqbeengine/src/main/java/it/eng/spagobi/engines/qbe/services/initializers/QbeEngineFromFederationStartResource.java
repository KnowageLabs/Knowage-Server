/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.engines.qbe.services.initializers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import it.eng.qbe.dataset.FederationUtils;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.QbeEngine;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.federation.FederationClient;
import it.eng.spagobi.engines.qbe.template.QbeTemplateParseException;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;

@Path("/start-federation")
public class QbeEngineFromFederationStartResource extends QbeEngineStartResource {

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startFederation(
			@QueryParam("federationId") String federationId,
			@QueryParam("drivers") String drivers) {

		Objects.nonNull(federationId);
		Objects.nonNull(drivers);

		QbeEngineInstance qbeEngineInstance = null;

		logger.debug("IN");

		try {
			SourceBean templateBean = getTemplateAsSourceBean();
			logger.debug("Template: " + templateBean);
			logger.debug("Creating engine instance ...");

			Map<Object, Object> env = getEnv(federationId, drivers);

			try {
				qbeEngineInstance = QbeEngine.createInstance(templateBean, env);
			} catch (Throwable t) {
				SpagoBIEngineStartupException serviceException;
				Throwable rootException = t;
				while (rootException.getCause() != null) {
					rootException = rootException.getCause();
				}
				String str = rootException.getMessage() != null ? rootException.getMessage() : rootException.getClass().getName();
				serviceException = new SpagoBIEngineStartupException(ENGINE_NAME, str, t);

				if (rootException instanceof QbeTemplateParseException) {
					QbeTemplateParseException e = (QbeTemplateParseException) rootException;
					serviceException.setDescription(e.getDescription());
					serviceException.setHints(e.getHints());
				}

				throw serviceException;
			}
			logger.debug("Engine instance succesfully created");

			setAttribute(ENGINE_INSTANCE, qbeEngineInstance);

		} catch (Throwable e) {
			SpagoBIEngineStartupException serviceException = null;

			if (e instanceof SpagoBIEngineStartupException) {
				serviceException = (SpagoBIEngineStartupException) e;
			} else {
				Throwable rootException = e;
				while (rootException.getCause() != null) {
					rootException = rootException.getCause();
				}
				String str = rootException.getMessage() != null ? rootException.getMessage() : rootException.getClass().getName();
				String message = "An unpredicted error occurred while executing " + ENGINE_NAME + " service." + "\nThe root cause of the error is: " + str;

				serviceException = new SpagoBIEngineStartupException(ENGINE_NAME, message, e);
			}

			throw serviceException;

		} finally {
			logger.debug("OUT");
		}

		return Response.ok().build();
	}

	private SourceBean getTemplateAsSourceBean() {
		SourceBean templateSB = null;
		return templateSB;
	}

	public Map<Object, Object> getEnv(String federationName, String drivers) {
		String federatedDatasetId = federationName;

		// loading federation
		FederationDefinition dsf = loadFederationDefinition(federatedDatasetId);
		logger.debug("Found a federated dataset on the request");
		Map<Object, Object> env = addFederatedDatasetsToEnv(dsf);

		env.put("DRIVERS", decodeParameterValue(drivers));

		return env;
	}

	/**
	 * Loading the federation definition from the service
	 *
	 * @param federationId
	 */
	public FederationDefinition loadFederationDefinition(String federationId) {
		logger.debug("Loading federation with id " + federationId);
		FederationClient fc = new FederationClient();
		try {
			return fc.getFederation(federationId, getUserIdentifier(), getDataSetServiceProxy());
		} catch (Exception e) {
			logger.error("Error loading the federation definition");
			throw new SpagoBIEngineRuntimeException("Error loading the federation definition", e);
		}
	}

	private IDataSource getCacheDataSource() {
		logger.debug("Loading the cache datasource");
		IDataSource dataSource = getDataSourceServiceProxy().getDataSourceForCache();
		logger.debug("cache datasource loaded");
		return dataSource;
	}

	public Map<Object, Object> addFederatedDatasetsToEnv(FederationDefinition dsf) {

		Assert.assertNotNull(dsf, "The federation id has to be not null");

		IDataSource cachedDataSource = getCacheDataSource();

		Map<Object, Object> env = super.getEnv();

		// update parameters into the dataset
		logger.debug("The dataset is federated");
		logger.debug("Getting the configuration");
		String configurationJson = "";
		logger.debug("The configuration is " + configurationJson);

		// loading the source datasets
		logger.debug("Loading source datasets");
		List<IDataSet> dataSets = new ArrayList<IDataSet>();
		List<IDataSet> originalDataSets = new ArrayList<IDataSet>();
		List<String> dsLabels = new ArrayList<String>();

		Iterator<IDataSet> sourceDatasets = dsf.getSourceDatasets().iterator();
		while (sourceDatasets.hasNext()) {
			IDataSet iDataSet = sourceDatasets.next();
			dsLabels.add(iDataSet.getLabel());
			originalDataSets.add(iDataSet);
		}

		// update profile attributes into dataset
		Map<String, Object> userAttributes = new HashMap<String, Object>();
		Map<String, String> mapNameTable = new HashMap<String, String>();
		UserProfile profile = (UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE);
		userAttributes.putAll(profile.getUserAttributes());
		userAttributes.put(SsoServiceInterface.USER_ID, profile.getUserId().toString());
		logger.debug("Setting user profile attributes into dataset...");
		logger.debug(userAttributes);

		// save in cache the derived datasets
		logger.debug("Saving the datasets on cache");

		JSONObject datasetPersistedLabels = null;
		try {
			datasetPersistedLabels = FederationUtils.createDatasetsOnCache(dsf.getDataSetRelationKeysMap(), getUserIdentifier());
		} catch (JSONException e1) {
			logger.error("Error loading the dataset. Please check that all the dataset linked to this federation are still working", e1);
			throw new SpagoBIEngineRuntimeException("Error loading the dataset. Please check that all the dataset linked to this federation are still working",
					e1);
		}
		for (int i = 0; i < dsLabels.size(); i++) {
			String dsLabel = dsLabels.get(i);
			// adds the link between dataset and cached table name
			Assert.assertNotNull(datasetPersistedLabels.optString(dsLabel), "Not found the label name of the cache table for the datase " + dsLabel);
			try {
				mapNameTable.put(dsLabel, datasetPersistedLabels.getString(dsLabel));
			} catch (Exception e) {
				logger.error("Error loading the dataset. Please check tha all the dataset linked to this federation are still working", e);
				throw new SpagoBIEngineRuntimeException(
						"Error loading the dataset. Please check that all the dataset linked to this federation are still working");
			}

			IDataSet originalDataset = originalDataSets.get(i);

			IDataSet cachedDataSet = FederationUtils.createDatasetOnCache(mapNameTable.get(dsLabel), originalDataset, cachedDataSource);
			cachedDataSet.setUserProfileAttributes(userAttributes);
			cachedDataSet.setPersistTableName(mapNameTable.get(dsLabel));
			cachedDataSet.setParamsMap(env);
			cachedDataSet.setDsMetadata(originalDataset.getDsMetadata());
			cachedDataSet.setDataSourceForReading(cachedDataSource);
			dataSets.add(cachedDataSet);
		}

		logger.debug("Adding relationships on envinronment");
		JSONObject relations = dsf.getRelationshipsAsJSONObject();

		env.put(EngineConstants.ENV_RELATIONS, relations);
		env.put(EngineConstants.ENV_DATASET_CACHE_MAP, mapNameTable);
		env.put(EngineConstants.ENV_RELATIONS, relations);
		env.put(EngineConstants.ENV_DATASETS, dataSets);
		env.put(EngineConstants.ENV_FEDERATION, dsf);
		env.put(EngineConstants.ENV_DATASOURCE, cachedDataSource);

		// TODO : Check this
		env.put(EngineConstants.DATASOURCE_FOR_WRITING, cachedDataSource);

		logger.debug(env);
		env.put(EngineConstants.ENV_LOCALE, getLocale());

		return env;
	}
}
