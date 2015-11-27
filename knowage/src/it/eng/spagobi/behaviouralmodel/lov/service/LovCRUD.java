/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.lov.service;

import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.behaviouralmodel.lov.bo.DatasetDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */

@Path("/2.0/lovs")
@ManageAuthorization
public class LovCRUD extends AbstractSpagoBIResource {

	static private Logger logger = Logger.getLogger(LovCRUD.class);

	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public List<ModalitiesValue> getAllListOfValues() {

		logger.debug("IN");

		List<ModalitiesValue> modalitiesValues = null;
		IModalitiesValueDAO modalitiesValueDAO = null;

		try {

			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			modalitiesValues = modalitiesValueDAO.loadAllModalitiesValue();

			logger.debug("Getting the list of all LOVs - done successfully");

		} catch (Exception exception) {

			logger.error("Error while getting the list of LOVs", exception);
			throw new SpagoBIServiceException("Error while getting the list of LOVs", exception);

		} finally {

			LogMF.debug(logger, "OUT: returning [{0}]", modalitiesValues.toString());

		}

		return modalitiesValues;

	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public ModalitiesValue getOnlyOneListOfValue(@PathParam("id") Integer idLOV) {

		logger.debug("IN: input id = " + idLOV);

		IModalitiesValueDAO modalitiesValueDAO;
		ModalitiesValue listOfValues = null;

		try {

			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			listOfValues = modalitiesValueDAO.loadModalitiesValueByID(idLOV);

			// JSONObject lovJSONObject = serializeModalitiesValues(listOfValues);

			// toReturn = JsonConverter.objectToJson(listOfValues, null);

			// toReturn = lovJSONObject.toString();
			logger.debug(String.format("Getting the LOV with ID=%d - done successfully", idLOV));

		} catch (Exception exception) {

			String messageToSend = String.format("Error while getting LOV with ID : %d", idLOV);
			logger.error(messageToSend, exception);
			throw new SpagoBIServiceException(messageToSend, exception);

		} finally {

			LogMF.debug(logger, "OUT: returning [{0}]", listOfValues.toString());

		}

		return listOfValues;

	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public String post(@Valid ModalitiesValue modValue) {
		IModalitiesValueDAO modalitiesValueDAO;
		try {

			// JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(servletRequest);
			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			// ModalitiesValue modVal = recoverModalitiesValueDetails(requestBodyJSON);
			modalitiesValueDAO.insertModalitiesValue(modValue);

			logger.debug("OUT: Posting the LOV - done successfully");

			int newID = modalitiesValueDAO.loadModalitiesValueByLabel(modValue.getLabel()).getId();

			// return Response.ok().build();
			return Integer.toString(newID);

		} catch (Exception exception) {

			logger.error("Error while posting LOV", exception);
			throw new SpagoBIServiceException("Error while posting LOV", exception);

		}
	}

	@POST
	@Path("/deleteSmth")
	// @Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public void remove(@Context HttpServletRequest servletRequest) {

		try {

			JSONObject requestJSONObject = RestUtilities.readBodyAsJSONObject(servletRequest);
			ModalitiesValue modVal = recoverModalitiesValueDetails(requestJSONObject);
			IModalitiesValueDAO modalitiesDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesDAO.setUserProfile(getUserProfile());
			modalitiesDAO.eraseModalitiesValue(modVal);

			logger.debug("OUT: Posting the LOV - done successfully");

			// int newID = modalitiesDAO.loadModalitiesValueByLabel(modVal.getLabel()).getId();

			// return Response.ok().build();
			System.out.println(modalitiesDAO.loadAllModalitiesValue().toString());
			// return modalitiesDAO.loadAllModalitiesValue().toString();

		} catch (Exception exception) {

			logger.error("Error while posting LOV", exception);
			throw new SpagoBIServiceException("Error while posting LOV", exception);

		}
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public String put(@Context HttpServletRequest servletRequest) {

		logger.debug("IN");

		try {

			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(servletRequest);
			IModalitiesValueDAO modalitiesDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesDAO.setUserProfile(getUserProfile());
			// ModalitiesValue modVal = recoverModalitiesValueDetails(requestBodyJSON);
			ModalitiesValue modVal = (ModalitiesValue) JsonConverter.jsonToObject(requestBodyJSON.toString(), ModalitiesValue.class);
			modalitiesDAO.modifyModalitiesValue(modVal);
			logger.debug("OUT: Putting the LOV - done successfully");

			int newID = modalitiesDAO.loadModalitiesValueByLabel(modVal.getLabel()).getId();

			// return Response.ok().build();
			return Integer.toString(newID);

			// return Response.status(200).build();

		} catch (Exception exception) {

			logger.error("Error while putting LOV", exception);
			throw new SpagoBIServiceException("Error while putting LOV", exception);

		}

	}

	@DELETE
	// @Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public Response delete(@Context HttpServletRequest servletRequest) {

		logger.debug("IN");

		try {

			JSONObject requestJSONObject = RestUtilities.readBodyAsJSONObject(servletRequest);
			ModalitiesValue modVal = recoverModalitiesValueDetails(requestJSONObject);
			IModalitiesValueDAO modalitiesDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesDAO.setUserProfile(getUserProfile());
			modalitiesDAO.eraseModalitiesValue(modVal);

			logger.debug("OUT: Deleting the LOV - done successfully");

			return Response.ok().build();

		} catch (Exception exception) {

			logger.error("Error while deleting LOV", exception);
			throw new SpagoBIServiceException("Error while deleting LOV", exception);
			// return Response.status(Status.NOT_MODIFIED).build();

		}

	}

	// private JSONArray serializeModalitiesValues(List<ModalitiesValue> modalitiesValues) {
	//
	// logger.debug("IN");
	//
	// JSONArray modalitiesValuesJSONArray = new JSONArray();
	//
	// Assert.assertNotNull(modalitiesValues, "Input object cannot be null");
	//
	// try {
	//
	// modalitiesValuesJSONArray = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(modalitiesValues, null);
	// logger.debug("OUT: Serializing the list of LOVs - done successfully");
	//
	// } catch (SerializationException exception) {
	//
	// logger.error("Error while serializing list of LOVs", exception);
	// throw new SpagoBIServiceException("Error while serializing list of LOVs", exception);
	//
	// }
	//
	// return modalitiesValuesJSONArray;
	//
	// }
	//
	// private JSONObject serializeModalitiesValues(ModalitiesValue modalitiesValue) {
	//
	// logger.debug("IN");
	//
	// JSONObject modalitiesValuesJSON = new JSONObject();
	// Assert.assertNotNull(modalitiesValue, "Input object cannot be null");
	//
	// try {
	// modalitiesValuesJSON = (JSONObject) SerializerFactory.getSerializer("application/json").serialize(modalitiesValue, null);
	// logger.debug("OUT: Serializing one LOV - done successfully");
	//
	// } catch (SerializationException exception) {
	//
	// logger.error("Error while serializing list one LOV", exception);
	// throw new SpagoBIServiceException("Error while serializing one LOV", exception);
	//
	// }
	//
	// return modalitiesValuesJSON;
	//
	// }

	private ModalitiesValue recoverModalitiesValueDetails(JSONObject requestBodyJSON) throws EMFUserError, SerializationException, SourceBeanException {

		logger.debug("IN");

		ModalitiesValue lovToReturn = new ModalitiesValue();

		Integer id = -1;

		if (requestBodyJSON.opt("LOV_ID").getClass() == Integer.class) {
			// idString = Integer.toString((int) requestBodyJSON.opt("LOV_ID"));
			id = (Integer) requestBodyJSON.opt("LOV_ID");
		} else {
			id = new Integer((String) requestBodyJSON.opt("LOV_ID"));
		}

		// if (idString != null && idString != "") {
		// id = new Integer(idString);
		// }

		String lovName = (String) requestBodyJSON.opt("LOV_NAME");
		String lovDecription = (String) requestBodyJSON.opt("LOV_DESCRIPTION");
		String lovLabel = (String) requestBodyJSON.opt("LOV_LABEL");
		String lovSelType = (String) requestBodyJSON.opt("SELECTION_TYPE");

		String lovProvider = (String) requestBodyJSON.opt("LOV_PROVIDER");
		String lovInputTypeCD = (String) requestBodyJSON.opt("I_TYPE_CD");
		String lovInputTypeID = (String) requestBodyJSON.opt("I_TYPE_ID");

		Integer dataSetID = -1;

		Assert.assertNotNull(lovName, "LOV name cannot be null");
		Assert.assertNotNull(lovDecription, "LOV description cannot be null");
		Assert.assertNotNull(lovProvider, "LOV provider cannot be null");
		Assert.assertNotNull(lovInputTypeCD, "LOV input type cannot be null");
		Assert.assertNotNull(lovInputTypeID, "LOV input type ID cannot be null");
		Assert.assertNotNull(lovLabel, "LOV label cannot be null");
		Assert.assertNotNull(lovSelType, "LOV selection type cannot be null");

		if (lovInputTypeCD.equalsIgnoreCase("DATASET")) {

			DatasetDetail dataSetDetail = (DatasetDetail) LovDetailFactory.getLovFromXML(lovProvider);
			dataSetID = Integer.parseInt(dataSetDetail.getDatasetId());

			logger.debug(dataSetDetail.getDatasetId());
		}

		lovToReturn.setId(id);
		lovToReturn.setName(lovName);
		lovToReturn.setDescription(lovDecription);
		lovToReturn.setLovProvider(lovProvider);
		lovToReturn.setITypeCd(lovInputTypeCD);
		lovToReturn.setITypeId(lovInputTypeID);
		lovToReturn.setLabel(lovLabel);
		lovToReturn.setSelectionType(lovSelType);
		lovToReturn.setDatasetID(dataSetID);

		logger.debug("OUT: Recovering data of the LOV from JSON object - done successfully");

		return lovToReturn;

	}
}