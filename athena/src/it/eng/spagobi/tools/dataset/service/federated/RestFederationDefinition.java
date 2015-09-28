/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.dataset.service.federated;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.federateddataset.dao.ISbiFederationDefinitionDAO;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


@Path("/federateddataset")
public class RestFederationDefinition {

	static private Logger logger = Logger.getLogger(RestFederationDefinition.class);

	@POST
	@Path("/post")
	public String createTrackInJSON(@Context HttpServletRequest req) {



		try {

			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			ISbiFederationDefinitionDAO federatedDatasetDao = DAOFactory.getFedetatedDatasetDAO();

			FederationDefinition fdsNew = recoverFederatedDatasetDetails(requestBodyJSON);

			federatedDatasetDao.saveSbiFederationDefinition(fdsNew);
			return "ok";
		} catch (Exception e) {

			e.printStackTrace();
			return "not ok";
		}



	}

	private FederationDefinition recoverFederatedDatasetDetails (JSONObject requestBodyJSON) {

		FederationDefinition fds = new FederationDefinition();
		Integer id = -1;
		String idStr = (String) requestBodyJSON.opt("FEDERATION_ID");
		if (idStr != null && !idStr.equals("")) {
			id = new Integer(idStr);
		}

		String label = (String) requestBodyJSON.opt("label");
		String name = (String) requestBodyJSON.opt("name");
		String description = (String) requestBodyJSON.opt("description");
		String relationships = requestBodyJSON.optJSONArray("relationships").toString();

		fds.setFederation_id(id.intValue());
		fds.setLabel(label);
		fds.setName(name);
		fds.setDescription(description);
		fds.setRelationships(relationships);

		fds.setSourceDatasets(deserializeDatasets(relationships));

		return fds;
	}


	private Set<IDataSet> deserializeDatasets(String relationships){

		Set<String> datasetNames = new HashSet<String>();
		Set<IDataSet> datasets = new HashSet<IDataSet>();

		//loading the datsets
		try {
			JSONArray array = new JSONArray(relationships);
			JSONArray innerArray = array.getJSONArray(0);



			for(int i=0; i<innerArray.length(); i++){
				JSONObject relation = innerArray.getJSONObject(i);
				JSONObject startRel = relation.getJSONObject("sourceTable");
				JSONObject destinationRel = relation.getJSONObject("destinationTable");

				datasetNames.add(startRel.getString("name"));
				datasetNames.add(destinationRel.getString("name"));

			}


		} catch (JSONException e) {
			logger.error("Error loading the datset");
			throw new SpagoBIRuntimeException("Error loading linked datasets",e);
		}

		IDataSetDAO dsDao;
		try {
			dsDao = DAOFactory.getDataSetDAO();

			Iterator<String> iter = datasetNames.iterator();
			while (iter.hasNext()) {
				String string = (String) iter.next();
				datasets.add(dsDao.loadDataSetByLabel(string));
			}

		} catch (EMFUserError e) {
			logger.error("Error loading the datset");
			throw new SpagoBIRuntimeException("Error loading linked datasets",e);
		}
		
		return datasets;

	}


}
