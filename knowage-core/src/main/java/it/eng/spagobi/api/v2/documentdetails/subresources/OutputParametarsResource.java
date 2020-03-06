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
package it.eng.spagobi.api.v2.documentdetails.subresources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.OutputParameter;
import it.eng.spagobi.analiticalmodel.document.dao.IOutputParameterDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/")
public class OutputParametarsResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(OutputParametarsResource.class);

	@GET
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public List<OutputParameter> getDocumentOutputParameters(@PathParam("id") Integer id) {
		logger.debug("IN");
		IOutputParameterDAO outputParameterDAO = DAOFactory.getOutputParameterDAO();
		List<OutputParameter> documentOutputParameters = outputParameterDAO.getOutputParametersByObjId(id);
		Assert.assertNotNull(documentOutputParameters, "Output Parameters can not be null");
		logger.debug("OUT");
		return documentOutputParameters;
	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public OutputParameter addDocumentOutputParameters(@PathParam("id") Integer id, OutputParameter outputParameter) {
		logger.debug("IN");
		Assert.assertNotNull(outputParameter, "Output Parameters can not be null");

		IOutputParameterDAO outputParameterDAO = DAOFactory.getOutputParameterDAO();
		outputParameterDAO.saveParameter(outputParameter);
		logger.debug("OUT");
		return outputParameter;
	}

	@PUT
	@Path("{outParId}")
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public String updateOutputParameter(@PathParam("id") Integer id, @PathParam("outParId") Integer outParId, OutputParameter outputParameter) {
		logger.debug("IN");
		try {
			IOutputParameterDAO outputParameterDao;
			outputParameterDao = DAOFactory.getOutputParameterDAO();

			Boolean found = null;
			if (outputParameter != null) {
				OutputParameter oldParam = outputParameterDao.getOutputParameter(outputParameter.getId());

				/* Check if value changed and parameter is used in cross nav */
				OutputParameterResourseAPI outputParameterResourseAPI = new OutputParameterResourseAPI();
				if (!oldParam.getType().getValueCd().equals(outputParameter.getType().getValueCd())) {
					found = outputParameterResourseAPI.isUsedInCrossNavigations(outputParameter);
				}
				outputParameter.setBiObjectId(id);
				outputParameterDao.saveParameter(outputParameter);
			}

//			String toReturn = JsonConverter.objectToJson(outputParameter, null);
			Gson gson = new Gson();
			JsonElement lfJsonElement = gson.toJsonTree(outputParameter);

			if (found != null && found)
				lfJsonElement.getAsJsonObject().addProperty("usedInCrossNavigations", found);

			return gson.toJson(lfJsonElement);

		} catch (Exception e) {
			logger.error("Error while updating a output parameter", e);
			throw new SpagoBIRestServiceException("Error while updating a output parameter", buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@DELETE
	@Path("{outputparId}")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public Integer deleteDocumentOutputParameters(@PathParam("id") Integer id, @PathParam("outputparId") Integer outputparId) {
		logger.debug("IN");

		IOutputParameterDAO outputParameterDAO;
		try {
			outputParameterDAO = DAOFactory.getOutputParameterDAO();
			outputParameterDAO.removeParameter(outputparId);

		} catch (EMFUserError e) {
			logger.error("Error while deleting driver", e);
			throw new SpagoBIRestServiceException("Deleting driver has failed", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return outputparId;
	}

}
