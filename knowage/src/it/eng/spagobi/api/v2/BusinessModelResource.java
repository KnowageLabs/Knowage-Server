package it.eng.spagobi.api.v2;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/2.0/businessmodels")
@ManageAuthorization
public class BusinessModelResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(BusinessModelResource.class);

	/**
	 * Get all business models
	 **/
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<MetaModel> getBusinessModels() {
		logger.debug("IN");
		List<MetaModel> businessModelList = null;

		try {

			businessModelList = DAOFactory.getMetaModelsDAO().loadAllMetaModels();

			return businessModelList;

		} catch (Exception e) {
			logger.error("", e);
			throw new SpagoBIRestServiceException("", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * Get all versions of business model with specified id
	 **/
	@GET
	@Path("{bmId}/versions")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<Content> getBusinessModelVersions(@PathParam("bmId") Integer bmId) {
		logger.debug("IN");
		List<Content> versions = null;

		try {

			versions = DAOFactory.getMetaModelsDAO().loadMetaModelVersions(bmId);

			return versions;

		} catch (Exception e) {
			logger.error("", e);
			throw new SpagoBIRestServiceException("", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Get business model with specified id
	 **/
	@GET
	@Path("{bmId}")
	@Produces(MediaType.APPLICATION_JSON)
	public MetaModel getBusinessModelById(@PathParam("bmId") Integer bmId) {
		logger.debug("IN");
		MetaModel businessModel;

		try {
			businessModel = DAOFactory.getMetaModelsDAO().loadMetaModelById(bmId);

			return businessModel;
		} catch (Exception e) {
			logger.error("", e);
			throw new SpagoBIRestServiceException("", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * Get version of business model with {bmId} and with specified version id
	 * {vId}
	 **/
	@GET
	@Path("{bmId}/versions/{vId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Content getBusinessModelVersionById(@PathParam("bmId") Integer bmId, @PathParam("vId") Integer vId) {
		logger.debug("IN");
		Content content = null;

		try {
			content = DAOFactory.getMetaModelsDAO().loadMetaModelContentById(vId);

			return content;
		} catch (Exception e) {
			logger.error("", e);
			throw new SpagoBIRestServiceException("", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

	// in progress
	@POST
	@Path("/{bmId}/versions")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON })
	public Response uploadFile(@MultipartForm MultipartFormDataInput input, @PathParam("bmId") int bmId) {
		logger.debug("IN");

		Content content = new Content();
		IMetaModelsDAO businessModelDAO = DAOFactory.getMetaModelsDAO();
		businessModelDAO.setUserProfile(getUserProfile());

		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		for (String key : uploadForm.keySet()) {

			List<InputPart> inputParts = uploadForm.get(key);

			for (InputPart inputPart : inputParts) {

				try {

					MultivaluedMap<String, String> header = inputPart.getHeaders();
					content.setFileName(getFileName(header));

					// convert the uploaded file to input stream
					InputStream inputStream = inputPart.getBody(InputStream.class, null);

					byte[] bytes = IOUtils.toByteArray(inputStream);

					content.setContent(bytes);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			content.setCreationDate(new Date());
			content.setCreationUser(getUserProfile().getUserName().toString());
			content.setDimension(String.valueOf(content.getContent().length));
			businessModelDAO.insertMetaModelContent(bmId, content);

		}

		return Response.status(200).entity("uploadFile is called, Uploaded file name : ").build();

	}

	///////////////// in progress/////////////////////////////////
	@GET
	@Path("{bmId}/versions/{vId}/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadFile(@PathParam("vId") Integer vId) {
		Content c = DAOFactory.getMetaModelsDAO().loadMetaModelContentById(vId);
		byte[] f = c.getContent();

		// File fi = new File("C:\\Users\\adujic\\Desktop\\hello.txt");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("dsa.txt");
			fos.write(f);
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ResponseBuilder response = Response.ok(fos);
		response.header("Content-Disposition", "attachment; filename=\"test.txt\"");
		return response.build();
	}

	//////////////////////////////////////////////
	/**
	 * Insert new business model POST
	 **/
	@POST
	@Path("/")
	@Consumes("application/json")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public MetaModel insertNewBusinessModel(@Valid MetaModel body) {
		logger.debug("IN");
		MetaModel bm = body;

		try {
			if (bm.getId() != null) {
				logger.error("New business model should not have id");
				bm = new MetaModel();
				return bm;
			}

			DAOFactory.getMetaModelsDAO().insertMetaModel(bm);
			MetaModel insertedBM = DAOFactory.getMetaModelsDAO().loadMetaModelByName(bm.getName());

			return insertedBM;
		} catch (Exception e) {
			logger.error("", e);
			throw new SpagoBIRestServiceException("", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * Edit existing business model with specified id PUT
	 **/
	@PUT
	@Path("/{bmId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public MetaModel updateBusinessModel(@PathParam("bmId") Integer bmId, @Valid MetaModel body) {
		logger.debug("IN");
		MetaModel bm = body;

		try {
			DAOFactory.getMetaModelsDAO().modifyMetaModel(bm);

			return bm;
		} catch (Exception e) {
			logger.error("", e);
			throw new SpagoBIRestServiceException("", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * Delete business model with specified id
	 **/
	@DELETE
	@Path("/{bmId}")
	public Response deleteBusinessModel(@PathParam("bmId") Integer bmId) {
		logger.debug("IN");

		try {
			DAOFactory.getMetaModelsDAO().eraseMetaModel(bmId);

			return Response.ok().build();
		} catch (Exception e) {
			logger.error("", e);
			throw new SpagoBIRestServiceException("", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Deleting many business models
	 **/
	@DELETE
	@Path("/deletemany")
	public Response deleteBusinessModels(@QueryParam("id") int[] ids) {
		try {
			for (int i = 0; i < ids.length; i++) {
				DAOFactory.getMetaModelsDAO().eraseMetaModel(ids[i]);
			}

			return Response.ok().build();
		} catch (Exception e) {
			logger.error("", e);
			throw new SpagoBIRestServiceException("", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Delete version with id {vId} of business model with id {bmId}
	 **/
	@DELETE
	@Path("{bmId}/versions/{vId}")
	public Response deleteBusinessModelVersion(@PathParam("bmId") Integer bmId, @PathParam("vId") Integer vId) {
		try {
			DAOFactory.getMetaModelsDAO().eraseMetaModelContent(vId);
			return Response.ok().build();
		} catch (Exception e) {
			logger.error("", e);
			throw new SpagoBIRestServiceException("", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Delete many versions of business model with id {bmId}
	 **/
	@DELETE
	@Path("{bmId}/deleteManyVersions")
	public Response deleteBusinessModelVersions(@PathParam("bmId") Integer bmId, @QueryParam("id") int[] ids) {
		try {
			for (int i = 0; i < ids.length; i++) {
				DAOFactory.getMetaModelsDAO().eraseMetaModelContent(ids[i]);
			}

			return Response.ok().build();
		} catch (Exception e) {
			logger.error("", e);
			throw new SpagoBIRestServiceException("", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

	private String getFileName(MultivaluedMap<String, String> header) {
		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");

				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return "unknown";
	}
}
