package it.eng.spagobi.api.v2;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.whatif.dao.IWhatifWorkflowDAO;
import it.eng.spagobi.whatif.metadata.SbiWhatifWorkflow;

@Path("/2.0/workflow")
@ManageAuthorization
public class WorkflowResource extends AbstractSpagoBIResource {

	public static transient Logger logger = Logger.getLogger(WorkflowResource.class);
	IWhatifWorkflowDAO iwfd = null;

	@POST
	@Path("/")
	@Produces("text/html; charset=UTF-8")
	public Response insertNewWorkflow(@javax.ws.rs.core.Context HttpServletRequest req) {
		logger.debug("IN");
		try {
			String params = RestUtilities.readBody(req);
			JSONObject paramsObj = new JSONObject(params);
			JSONArray ja = paramsObj.getJSONArray("workflowArr");
			int modelId = paramsObj.getInt("modelId");
			List<SbiWhatifWorkflow> newWorkflow = createWFListFromJson(ja, modelId);

			iwfd = DAOFactory.getWhatifWorkflowDAO();
			iwfd.createNewWorkflow(newWorkflow);
			return Response.status(200).build();
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("An error occured while creating workflow");
			return Response.status(500).build();
		}

	}

	@PUT
	@Path("/update")
	@Produces("text/html; charset=UTF-8")
	public Response updateWorkflow(@javax.ws.rs.core.Context HttpServletRequest req) {
		logger.debug("IN");
		try {
			String params = RestUtilities.readBody(req);
			JSONObject paramsObj = new JSONObject(params);
			JSONArray ja = paramsObj.getJSONArray("workflowArr");
			int modelId = paramsObj.getInt("modelId");
			List<SbiWhatifWorkflow> newWorkflow = createWFListFromJson(ja, modelId);

			iwfd = DAOFactory.getWhatifWorkflowDAO();
			iwfd.updateWorkflow(newWorkflow);
			return Response.status(200).build();
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("An error occured while creating workflow");
			return Response.status(500).build();
		}
	}

	@GET
	@Path("/{mId}")
	public List<Integer> getWorkflowOfModel(@PathParam("mId") int modelId) {
		logger.debug("IN");

		try {
			iwfd = DAOFactory.getWhatifWorkflowDAO();
			return iwfd.getWorkflowUsersOfModel(modelId);
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("An error occured while getting workflow");
		}

		return null;
	}

	@GET
	@Path("/isStarted/{mId}")
	public int isWorkflowStarted(@PathParam("mId") int modelId) {
		int userId = -1; // id of user in progres 0 if not started

		try {
			iwfd = DAOFactory.getWhatifWorkflowDAO();
			return iwfd.isWorkflowStarted(modelId);
		} catch (EMFUserError e) {
			e.printStackTrace();
			logger.error("An error occured while checking if workflow is started");
		}

		return userId;
	}

	@PUT
	@Path("startWorkflow/{mId}")
	public Response startWorkflow(@PathParam("mId") int modelId) {
		try {
			iwfd = DAOFactory.getWhatifWorkflowDAO();
			iwfd.startWorkflow(modelId);
			return Response.status(200).build();
		} catch (EMFUserError e) {
			e.printStackTrace();
			logger.error("An error occured while starting workflow");
			return Response.status(500).build();
		}
	}
	// @GET
	// @Path("/{wfId}")
	// public List<WhatifWorkflow> getWorkflowById(@PathParam("wfId") int id) {
	// logger.debug("IN");
	//
	// try {
	//
	// iwfd = DAOFactory.getWhatifWorkflowDAO();
	// return iwfd.loadWorkflowByDocumentId(id);
	// } catch (EMFUserError e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// logger.debug("OUT");
	// return null;
	// };

	private List<SbiWhatifWorkflow> createWFListFromJson(JSONArray array, int modelId) {
		List<SbiWhatifWorkflow> toReturn = new ArrayList<>();

		for (int i = 0; i < array.length(); i++) {
			SbiWhatifWorkflow sbiwf = new SbiWhatifWorkflow();
			JSONObject jo;
			try {
				jo = array.getJSONObject(i);
				sbiwf.setUserId(jo.getInt("id"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sbiwf.setModelId(modelId);
			sbiwf.setSequcence(i);
			sbiwf.setState("notstartedyet");

			toReturn.add(sbiwf);

		}

		return toReturn;
	}
}
