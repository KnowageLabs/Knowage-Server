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

package it.eng.spagobi.tools.dataset.service.federated;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.federateddataset.dao.ISbiFederationDefinitionDAO;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/federateddataset")
public class RestFederationDefinition extends AbstractSpagoBIResource {

	@Context
	protected HttpServletRequest request;

	static private Logger logger = Logger.getLogger(RestFederationDefinition.class);
	public static final String DATASET_ID = "id";
	public static final String VERSION_NUM = "versionNum";
	ISbiFederationDefinitionDAO fdsDAO;
	List<FederationDefinition> listOfFederations;

	@GET
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.FEDERATED_DATASET_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public List<FederationDefinition> get() {
		try {
			// List<FederationDefinition> allFederatedDatasets = null;
			fdsDAO = DAOFactory.getFedetatedDatasetDAO();
			fdsDAO.setUserProfile(this.getUserProfile());

			ISbiFederationDefinitionDAO federDsDao = DAOFactory.getFedetatedDatasetDAO();
			federDsDao.setUserProfile(this.getUserProfile());

			listOfFederations = federDsDao.loadNotDegeneratedFederatedDataSets();
			if (listOfFederations == null) {
				listOfFederations = new ArrayList<FederationDefinition>();
			}
			// needs serialization
			return listOfFederations;
		} catch (EMFUserError e) {
			logger.error("Error while loading federations", e);
			throw new SpagoBIRuntimeException("Error while loading federations", e);
		}
	}

	@GET
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public FederationDefinition getFederationByID(@PathParam("id") Integer id) {
		try {
			FederationDefinition federationDefinition;
			fdsDAO = DAOFactory.getFedetatedDatasetDAO();
			federationDefinition = fdsDAO.loadFederationDefinition(id);
			return federationDefinition;
		} catch (EMFUserError e) {
			logger.error("Error while getting federation by id", e);
			throw new SpagoBIRuntimeException("Error getting federation by id", e);
		}
	}

	/**
	 * Saves the federation definition in the db. Gets the definition from the
	 * body of the request
	 *
	 * @param req
	 * @return
	 */
	@POST
	@Path("/post")
	public Integer insertFederation(@Context HttpServletRequest req) {
		try {
			logger.debug("Saving the federation");
			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			FederationDefinition fdsNew = recoverFederatedDatasetDetails(requestBodyJSON);
			logger.debug("The federation definition label is " + fdsNew.getLabel());

			Integer id = insertFederationDefinition(fdsNew, true);

			logger.debug("Saving OK");
			logger.debug("OUT");
			return id;
		} catch (Exception e) {

			String state = ((SQLException) e.getCause().getCause()).getSQLState();
			String sqle = ((SQLException) e.getCause().getCause()).getMessage();

			if (state.equals("23000")) {

				if (sqle.contains("LABEL")) {
					logger.error("Duplicate key endtry while saving federation", e);
					throw new SpagoBIRuntimeException("There is already a federation with same label", e);
				} else if (sqle.contains("NAME")) {
					logger.error("Duplicate key endtry while saving federation", e);
					throw new SpagoBIRuntimeException("There is already a federation with same name!", e);
				}

				logger.error("Duplicate key endtry while saving federation", e);
				throw new SpagoBIRuntimeException("There is already a federation with same label and/or name!", e);

			} else {
				logger.error("Error saving federation", e);
				throw new SpagoBIRuntimeException("Error saving federation", e);
			}

		}
	}

	/**
	 * Saves the federation definition in the db. Gets the definition from the
	 * body of the request
	 *
	 * @param req
	 * @return
	 */
	@POST
	@Path("/insertNoDup")
	public String insertFederationNoDuplicate(@Context HttpServletRequest req) {
		try {
			logger.debug("Saving the federation");
			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			FederationDefinition fdsNew = recoverFederatedDatasetDetails(requestBodyJSON);
			logger.debug("The federation definition label is " + fdsNew.getLabel());

			insertFederationDefinition(fdsNew, false);

			logger.debug("Saving OK");
			logger.debug("OUT");
			return fdsNew.getFederation_id() + "";
		} catch (Exception e) {
			logger.error("Error saving federation", e);
			throw new SpagoBIRuntimeException("Error saving federation", e);
		}
	}

	@PUT
	@Path("/{id}")
	public Integer modifyFederation(@Context HttpServletRequest req) {
		try {
			logger.debug("Editing the federation");
			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			FederationDefinition fdsNew = recoverFederatedDatasetDetails(requestBodyJSON);
			logger.debug("The federation definition label is " + fdsNew.getLabel());
			logger.debug("The federation definition ID is " + fdsNew.getFederation_id());

			ISbiFederationDefinitionDAO federatedDatasetDao = DAOFactory.getFedetatedDatasetDAO();
			Integer id = federatedDatasetDao.modifyFederation(fdsNew);

			logger.debug("Saving OK");
			logger.debug("OUT");
			return id;
		} catch (Exception e) {
			logger.error("Error saving federation", e);
			throw new SpagoBIRuntimeException("Error saving federation", e);
		}
	}

	//
	// /**
	// * Saves the federation definition in the db. Gets the definition from the
	// body of the request
	// * @param req
	// * @return
	// */
	// @POST
	// @Path("/post2")
	// public String insertFederation2(@Context HttpServletRequest req) {
	// try {
	// logger.debug("Saving the federation");
	// JSONObject requestBodyJSON = new JSONObject();
	// requestBodyJSON.put("label", "dataset"+System.currentTimeMillis());
	// requestBodyJSON.put("name", "dataset"+System.currentTimeMillis());
	//
	// JSONArray ja = new JSONArray();
	// JSONObject jo = new JSONObject();
	// jo.put("id", 2);
	// jo.put(VERSION_NUM, 1);
	// ja.put(jo);
	//
	// requestBodyJSON.put("sourcesDataset", ja);
	//
	// FederationDefinition fdsNew =
	// recoverFederatedDatasetDetails(requestBodyJSON);
	// logger.debug("The federation definition label is "+fdsNew.getLabel());
	//
	// insertFederationDefinition(fdsNew);
	//
	// logger.debug("Saving OK");
	// logger.debug("OUT");
	// return fdsNew.getFederation_id()+"";
	// } catch (Exception e) {
	// logger.error("Error saving federation",e);
	// throw new SpagoBIRuntimeException("Error saving federation",e);
	// }
	// }

	/**
	 * Saves the federation definition on db
	 *
	 * @param federation
	 * @throws EMFUserError
	 */
	public int insertFederationDefinition(FederationDefinition federation, boolean duplicated) throws EMFUserError {
		logger.debug("The federation definition label is " + federation.getLabel());

		ISbiFederationDefinitionDAO federatedDatasetDao = DAOFactory.getFedetatedDatasetDAO();
		if (duplicated) {
			return federatedDatasetDao.saveSbiFederationDefinition(federation);
		} else {
			return federatedDatasetDao.saveSbiFederationDefinitionNoDuplicated(federation);
		}

	}

	/**
	 * Gets a specific federation definition
	 *
	 * @param req
	 * @param federationId
	 *            {int} the id of the federation definition
	 * @return the serialization of the federation definition
	 */
	@POST
	@Path("/federation")
	public String getFederation(@QueryParam("federationId") String federationId) {
		try {
			logger.debug("Loading the federation with id " + federationId);
			ISbiFederationDefinitionDAO federatedDatasetDao = DAOFactory.getFedetatedDatasetDAO();
			FederationDefinition federation = federatedDatasetDao.loadFederationDefinition(new Integer(federationId));
			logger.debug("retrived federaion. the label is " + federation.getLabel());

			JSONObject federationSerialized = (JSONObject) SerializerFactory.getSerializer("application/json").serialize(federation, request.getLocale());

			logger.debug("Sending serialization of federation definition " + federation.getLabel());
			return federationSerialized.toString();
		} catch (Exception e) {
			logger.error("Error retriving federation with id " + federationId, e);
			throw new SpagoBIRuntimeException("Error retriving federation with id " + federationId, e);
		}
	}

	private FederationDefinition recoverFederatedDatasetDetails(JSONObject requestBodyJSON) {

		FederationDefinition fds = new FederationDefinition();
		Integer id = -1;
		Integer idStr = (Integer) requestBodyJSON.opt("federation_id");
		if (idStr != null && !idStr.equals("")) {
			id = new Integer(idStr);
		}

		String label = (String) requestBodyJSON.opt("label");
		String name = (String) requestBodyJSON.opt("name");
		String description = "";
		if (!requestBodyJSON.isNull("description")) {
			description = (String) requestBodyJSON.opt("description");
		}
		Boolean degenerated = requestBodyJSON.optBoolean("degenerated");
		JSONArray relationsJa = requestBodyJSON.optJSONArray("relationships");
		String relationships = null;
		if (relationsJa != null) {
			relationships = requestBodyJSON.optJSONArray("relationships").toString();
		}

		JSONArray sourcesDataset = requestBodyJSON.optJSONArray("sourcesDataset");

		fds.setFederation_id(id.intValue());
		fds.setLabel(label);
		fds.setName(name);
		fds.setDescription(description);
		fds.setDegenerated(degenerated);

		if (relationships != null && relationships.length() > 0) {
			fds.setRelationships(relationships);
			fds.setSourceDatasets(deserializeDatasets(relationships));
		}

		if (sourcesDataset != null) {
			fds.setSourceDatasets(deserializeDatasets(sourcesDataset));
		}

		return fds;
	}

	private Set<IDataSet> deserializeDatasets(JSONArray datasets) {
		Set<IDataSet> iDataSet = new HashSet<IDataSet>();

		try {
			for (int i = 0; i < datasets.length(); i++) {
				JSONObject jo = datasets.getJSONObject(i);

				JDBCDataSet ds = new JDBCDataSet();
				ds.setId(jo.getInt(DATASET_ID));
				// VersionedDataSet vd = new
				// VersionedDataSet(ds,jo.getInt(VERSION_NUM),true);
				ds.setOrganization(TenantManager.getTenant().getName());
				iDataSet.add(ds);

			}

		} catch (Exception e) {
			logger.error("Error deserializing datasets for new federaion", e);
			throw new SpagoBIEngineRuntimeException("Error deserializing datasets for new federaion", e);
		}

		return iDataSet;

	}

	private Set<IDataSet> deserializeDatasets(String relationships) {

		Set<String> datasetNames = new HashSet<String>();
		Set<IDataSet> datasets = new HashSet<IDataSet>();

		// loading the datsets
		try {
			JSONArray array = new JSONArray(relationships);

			for (int j = 0; j < array.length(); j++) {
				JSONArray innerArray = array.getJSONArray(j);
				for (int i = 0; i < innerArray.length(); i++) {
					JSONObject relation = innerArray.getJSONObject(i);
					JSONObject startRel = relation.getJSONObject("sourceTable");
					JSONObject destinationRel = relation.getJSONObject("destinationTable");

					datasetNames.add(startRel.getString("name"));
					datasetNames.add(destinationRel.getString("name"));
				}
			}

		} catch (JSONException e) {
			logger.error("Error loading the datset");
			throw new SpagoBIRuntimeException("Error loading linked datasets", e);
		}

		IDataSetDAO dsDao;
		try {
			dsDao = DAOFactory.getDataSetDAO();

			Iterator<String> iter = datasetNames.iterator();
			while (iter.hasNext()) {
				String string = iter.next();
				datasets.add(dsDao.loadDataSetByLabel(string));
			}

		} catch (EMFUserError e) {
			logger.error("Error loading the datset");
			throw new SpagoBIRuntimeException("Error loading linked datasets", e);
		}

		return datasets;

	}

}
