package it.eng.spagobi.api.v2;

import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.metadata.dao.ISbiMetaSourceDAO;
import it.eng.spagobi.metadata.dao.ISbiMetaTableDAO;
import it.eng.spagobi.metadata.metadata.SbiMetaSource;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("2.0/metaSourceResource")
@ManageAuthorization
public class MetaSourceResource extends AbstractSpagoBIResource {

	private ISbiMetaSourceDAO sbiMetaSourceDAO = null;
	private ISbiMetaTableDAO sbiMetaTableDAO = null;
	private final static String SERVICE_NAME = "MetaSourceResource";

	public MetaSourceResource() {

	}

	private void init() {
		try {
			sbiMetaSourceDAO = DAOFactory.getSbiMetaSourceDAO();
			sbiMetaTableDAO = DAOFactory.getSbiMetaTableDAO();
		} catch (EMFUserError e) {
			logger.debug(e.getMessage());
		}
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	public List<SbiMetaSource> getAll() throws EMFUserError {
		List<SbiMetaSource> sources = null;

		sbiMetaSourceDAO = DAOFactory.getSbiMetaSourceDAO();

		sources = sbiMetaSourceDAO.loadAllSources();

		return sources;
	}

	@GET
	@Path("/{sourceId}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	public SbiMetaSource getById(@PathParam("sourceId") Integer sourceId) throws EMFUserError {
		init();
		SbiMetaSource sbiMetaSource = sbiMetaSourceDAO.loadSourceByID(sourceId);
		return sbiMetaSource;
	}

	@GET
	@Path("/{sourceId}/metatables")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	public List<SbiMetaTable> getAllTables(@PathParam("sourceId") Integer sourceId) throws EMFUserError {
		List<SbiMetaTable> metaTables = null;
		sbiMetaSourceDAO = DAOFactory.getSbiMetaSourceDAO();
		try {

			metaTables = sbiMetaSourceDAO.loadMetaTables(sourceId);

		} catch (EMFUserError e) {
			logger.debug(e.getMessage());
		}
		return metaTables;
	}

	@GET
	@Path("/{sourceId}/metatables/{tableId}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	public SbiMetaTable getTableById(@PathParam("sourceId") Integer sourceId, @PathParam("tableId") Integer tableId) {
		init();

		try {
			List<SbiMetaTable> metaTables = sbiMetaSourceDAO.loadMetaTables(sourceId);
			for (SbiMetaTable metaTable : metaTables) {
				if (metaTable.getTableId().equals(tableId)) {
					return metaTable;
				}
			}
		} catch (EMFUserError e) {
			logger.debug(e.getMessage());
		}
		return null;
	}

	@POST
	@Path("/")
	public Integer insert(SbiMetaSource sbiMetaSource) throws EMFUserError {

		sbiMetaSourceDAO = DAOFactory.getSbiMetaSourceDAO();

		Integer newSourceId = sbiMetaSourceDAO.insertSource(sbiMetaSource);
		return newSourceId;

	}

	@POST
	@Path("/{sourceId}/metatables")
	public Integer insertTable(@PathParam("sourceId") Integer sourceId, SbiMetaTable sbiMetaTable) throws EMFUserError {
		init();
		Integer metaTableId = null;
		SbiMetaSource sbiMetaSource = sbiMetaSourceDAO.loadSourceByID(sourceId);
		if (sbiMetaSource != null) {
			sbiMetaTable.setSbiMetaSource(sbiMetaSource);
			metaTableId = sbiMetaTableDAO.insertTable(sbiMetaTable);
		}

		return metaTableId;
	}

	@PUT
	@Path("/{sourceId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	public SbiMetaSource modify(@Valid SbiMetaSource sbiMetaSource, @PathParam("sourceId") Integer sourceId) {
		logger.debug("IN");
		init();
		if (sbiMetaSource == null) {
			logger.debug("sbiMetasource is null");
		}

		SbiMetaSource temp = null;

		try {
			temp = sbiMetaSourceDAO.loadSourceByID(sourceId);
			sbiMetaSourceDAO.modifySource(sbiMetaSource);
			temp = sbiMetaSourceDAO.loadSourceByID(sourceId);
		} catch (SpagoBIDOAException e) {
			throw new SpagoBIServiceException(SERVICE_NAME, e.getMessage());
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, e.getLocalizedMessage());
		}
		logger.debug("OUT");
		return temp;

	}

	@PUT
	@Path("/{sourceId}/metatables/{tableId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public SbiMetaTable modifyTable(@PathParam("sourceId") Integer sourceId, @Valid SbiMetaTable sbiMetaTable, @PathParam("tableId") Integer tableId)
			throws EMFUserError {
		init();
		SbiMetaTable toReturnTable = null;

		SbiMetaSource sbiMetaSource = sbiMetaSourceDAO.loadSourceByID(sourceId);
		if (sbiMetaSource != null) {
			sbiMetaTable.setSbiMetaSource(sbiMetaSource);
			sbiMetaTableDAO.modifyTable(sbiMetaTable);
			toReturnTable = sbiMetaTableDAO.loadTableByID(tableId);

		}
		return toReturnTable;

	}

	@DELETE
	@Path("/{sourceId}")
	public Integer delete(@PathParam("sourceId") Integer sourceId) {
		return null;
	}

	public Integer deleteTable(Integer id, Integer tableId) {

		return null;

	}

}
