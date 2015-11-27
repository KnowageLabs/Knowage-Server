package it.eng.spagobi.api.v2;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/businessmodels")
@ManageAuthorization
public class BusinessModelResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(BusinessModelResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<MetaModel> getBusinessModels() {
		logger.debug("IN");

		IMetaModelsDAO businessModelDao = null;
		List<MetaModel> businessModelList = null;

		try {
			businessModelDao = DAOFactory.getMetaModelsDAO();
			businessModelList = businessModelDao.loadAllMetaModels();

			if (!businessModelList.isEmpty() && businessModelList != null) {
				return businessModelList;
			}

		} catch (Exception e) {
			logger.error("Error while getting business model catalogue", e);
			throw new SpagoBIRuntimeException("Error while getting business model catalogue", e);
		} finally {
			logger.debug("OUT");
		}

		return new ArrayList<MetaModel>();

	}

	@GET
	@Path("/{bmId}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<Content> getBusinessModelVersions(@PathParam("bmId") Integer bmId) {
		logger.debug("IN");

		IMetaModelsDAO businessModelDao = null;
		List<Content> versions = null;

		try {
			businessModelDao = DAOFactory.getMetaModelsDAO();
			versions = businessModelDao.loadMetaModelVersions(bmId);

			if (!versions.isEmpty() && versions != null) {
				return versions;
			}

		} catch (Exception e) {

			logger.error("Error while getting business model catalogue", e);
			throw new SpagoBIRuntimeException("Error while getting business model catalogue", e);
		} finally {
			logger.debug("OUT");
		}

		return new ArrayList<Content>();
	}

	@POST
	@Path("/")
	@Consumes("application/json")
	public Response insertNewBusinessModel(MetaModel body) {

		MetaModel bm = body;

		if (bm == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (bm.getId() != null) {
			return Response.status(Status.BAD_REQUEST).entity("Error paramters. New business model should not have ID value").build();
		}

		try {
			DAOFactory.getMetaModelsDAO().insertMetaModel(bm);
			return Response.ok().build();

		} catch (Exception e) {
			logger.error("Error while creating url of the new resource", e);

		}

		return null;
	}

	@PUT
	@Path("/{bmId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateBusinessModel(@PathParam("bmId") Integer bmId, MetaModel body) {

		MetaModel bm = body;

		if (bm == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (bm.getId() == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error, Business model with id:" + bmId + " does not exist.").build();
		}

		try {
			DAOFactory.getMetaModelsDAO().modifyMetaModel(body);
			return Response.ok().build();

		} catch (Exception e) {
			logger.error("Error while creating url of the new resource", e);

		}

		return null;
	}
}
