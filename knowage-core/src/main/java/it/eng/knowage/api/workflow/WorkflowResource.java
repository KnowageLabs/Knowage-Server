package it.eng.knowage.api.workflow;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.whatif.dao.IWhatifWorkflowDAO;
import it.eng.spagobi.whatif.metadata.SbiWhatifWorkflow;

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

/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
            JSONObject paramsObj = RestUtilities.readBodyAsJSONObject(req);
            JSONArray ja = paramsObj.getJSONArray("workflowArr");
            int modelId = paramsObj.getInt("modelId");
            List<SbiWhatifWorkflow> newWorkflow = createWFListFromJson(ja, modelId, true);

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
            JSONObject paramsObj = RestUtilities.readBodyAsJSONObject(req);
            JSONArray ja = paramsObj.getJSONArray("workflowArr");
            int modelId = paramsObj.getInt("modelId");
            List<SbiWhatifWorkflow> newWorkflow = createWFListFromJson(ja, modelId, false);

            iwfd = DAOFactory.getWhatifWorkflowDAO();
            iwfd.updateWorkflow(newWorkflow, modelId);
            return Response.status(200).build();
        } catch (Exception e) {
            logger.error("An error occured while creating workflow", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/{mId}")
    public List<Integer> getWorkflowOfModel(@PathParam("mId") int modelId) {
        logger.debug("IN");
        iwfd = DAOFactory.getWhatifWorkflowDAO();
        return iwfd.getWorkflowUsersOfModel(modelId);
    }

    @GET
    @Path("/isStarted/{mId}")
    public int isWorkflowStarted(@PathParam("mId") int modelId) {

        iwfd = DAOFactory.getWhatifWorkflowDAO();
        return iwfd.isWorkflowStarted(modelId);

    }

    @PUT
    @Path("startWorkflow/{mId}")
    public Response startWorkflow(@PathParam("mId") int modelId) {
        iwfd = DAOFactory.getWhatifWorkflowDAO();
        iwfd.startWorkflow(modelId);
        return Response.status(200).build();
    }

    private List<SbiWhatifWorkflow> createWFListFromJson(JSONArray array, int modelId, boolean isNew) {
        List<SbiWhatifWorkflow> toReturn = new ArrayList<>();
        IWhatifWorkflowDAO wfdao = null;

        for (int i = 0; i < array.length(); i++) {
            SbiWhatifWorkflow sbiwf = new SbiWhatifWorkflow();
            JSONObject jo;
            try {
                jo = array.getJSONObject(i);
                sbiwf.setUserId(jo.getInt("id"));

                if (!isNew) {
                    wfdao = DAOFactory.getWhatifWorkflowDAO();
                    int id = wfdao.idByUserAndModel(sbiwf.getUserId(), modelId);
                    if (id > -1) // -1 if user is not in database but inserted
                        // after this update isNew = false in this
                        // case
                        sbiwf.setId(id);
                }
            } catch (JSONException e) {
                logger.error(e);
            }
            sbiwf.setModelId(modelId);
            sbiwf.setSequcence(i);
            sbiwf.setState("notstartedyet");

            toReturn.add(sbiwf);
        }
        return toReturn;
    }
}
