package it.eng.spagobi.api.v2;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.metadata.dao.ISbiMetaDocTabRelDAO;
import it.eng.spagobi.metadata.metadata.SbiMetaDocTabRel;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("2.0/metaDocumetRelationResource")
@ManageAuthorization
public class MetaDocumetRelationResource extends AbstractSpagoBIResource {

	private ISbiMetaDocTabRelDAO sbiMetaDocTabRelDAO = null;

	private final static String SERVICE_NAME = "MetaDocumetRelationResource";

	public MetaDocumetRelationResource() {

	}

	private void init() {
		try {
			sbiMetaDocTabRelDAO = DAOFactory.getSbiMetaDocTabRelDAO();

		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	public List<SbiMetaDocTabRel> getAll() throws EMFUserError {
		init();
		List<SbiMetaDocTabRel> documentRelations = null;

		documentRelations = sbiMetaDocTabRelDAO.loadAllDocRelations();

		return documentRelations;
	}
	/*
	 * @GET
	 * 
	 * @Path("/{sourceId}")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON)
	 * 
	 * @UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	 * public SbiMetaSource getById(@PathParam("sourceId") Integer sourceId)
	 * throws EMFUserError { init(); SbiMetaSource sbiMetaSource =
	 * sbiMetaSourceDAO.loadSourceByID(sourceId); return sbiMetaSource; }
	 * 
	 * @GET
	 * 
	 * @Path("/{sourceId}/metatables")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON)
	 * 
	 * @UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	 * public List<SbiMetaTable> getAllTables(@PathParam("sourceId") Integer
	 * sourceId) throws EMFUserError { List<SbiMetaTable> metaTables = null;
	 * sbiMetaSourceDAO = DAOFactory.getSbiMetaSourceDAO(); try {
	 * 
	 * metaTables = sbiMetaSourceDAO.loadMetaTables(sourceId);
	 * 
	 * } catch (EMFUserError e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } System.out.println(metaTables); return metaTables;
	 * }
	 * 
	 * @GET
	 * 
	 * @Path("/{sourceId}/metatables/{tableId}")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON)
	 * 
	 * @UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	 * public SbiMetaTable getTableById(@PathParam("sourceId") Integer sourceId,
	 * @PathParam("tableId") Integer tableId) { init();
	 * 
	 * try { List<SbiMetaTable> metaTables =
	 * sbiMetaSourceDAO.loadMetaTables(sourceId); for (SbiMetaTable metaTable :
	 * metaTables) { if (metaTable.getTableId().equals(tableId)) { return
	 * metaTable; } } } catch (EMFUserError e) { // TODO Auto-generated catch
	 * block e.printStackTrace(); } return null; }
	 * 
	 * @POST
	 * 
	 * @Path("/") public Integer insert(SbiMetaSource sbiMetaSource) throws
	 * EMFUserError {
	 * 
	 * sbiMetaSourceDAO = DAOFactory.getSbiMetaSourceDAO();
	 * 
	 * Integer newSourceId = sbiMetaSourceDAO.insertSource(sbiMetaSource);
	 * return newSourceId;
	 * 
	 * }
	 * 
	 * @POST
	 * 
	 * @Path("/{sourceId}/metatables") public Integer
	 * insertTable(@PathParam("sourceId") Integer sourceId, SbiMetaTable
	 * sbiMetaTable) throws EMFUserError { init(); Integer metaTableId = null;
	 * SbiMetaSource sbiMetaSource = sbiMetaSourceDAO.loadSourceByID(sourceId);
	 * if (sbiMetaSource != null) {
	 * sbiMetaTable.setSbiMetaSource(sbiMetaSource); metaTableId =
	 * sbiMetaTableDAO.insertTable(sbiMetaTable); }
	 * 
	 * return metaTableId; }
	 * 
	 * @PUT
	 * 
	 * @Path("/{sourceId}")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON)
	 * 
	 * @Consumes(MediaType.APPLICATION_JSON)
	 * 
	 * @UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	 * public SbiMetaSource modify(@Valid SbiMetaSource sbiMetaSource,
	 * @PathParam("sourceId") Integer sourceId) { logger.debug("IN"); init(); if
	 * (sbiMetaSource == null) { logger.debug("sbiMetasource is null"); }
	 * 
	 * SbiMetaSource temp = null;
	 * 
	 * try { temp = sbiMetaSourceDAO.loadSourceByID(sourceId);
	 * sbiMetaSourceDAO.modifySource(sbiMetaSource); temp =
	 * sbiMetaSourceDAO.loadSourceByID(sourceId); } catch (SpagoBIDOAException
	 * e) { throw new SpagoBIServiceException(SERVICE_NAME, e.getMessage()); }
	 * catch (Exception e) { throw new SpagoBIServiceException(SERVICE_NAME,
	 * e.getLocalizedMessage()); } logger.debug("OUT"); return temp;
	 * 
	 * }
	 * 
	 * @PUT
	 * 
	 * @Path("/{sourceId}/metatables/{tableId}")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON)
	 * 
	 * @Consumes(MediaType.APPLICATION_JSON) public SbiMetaTable
	 * modifyTable(@PathParam("sourceId") Integer sourceId, @Valid SbiMetaTable
	 * sbiMetaTable, @PathParam("tableId") Integer tableId) throws EMFUserError
	 * { init(); SbiMetaTable toReturnTable = null;
	 * 
	 * SbiMetaSource sbiMetaSource = sbiMetaSourceDAO.loadSourceByID(sourceId);
	 * if (sbiMetaSource != null) {
	 * sbiMetaTable.setSbiMetaSource(sbiMetaSource);
	 * sbiMetaTableDAO.modifyTable(sbiMetaTable); toReturnTable =
	 * sbiMetaTableDAO.loadTableByID(tableId);
	 * 
	 * } return toReturnTable;
	 * 
	 * }
	 * 
	 * @DELETE
	 * 
	 * @Path("/{sourceId}") public Integer delete(@PathParam("sourceId") Integer
	 * sourceId) { return null; }
	 * 
	 * public Integer deleteTable(Integer id, Integer tableId) {
	 * 
	 * return null;
	 * 
	 * }
	 */
}
