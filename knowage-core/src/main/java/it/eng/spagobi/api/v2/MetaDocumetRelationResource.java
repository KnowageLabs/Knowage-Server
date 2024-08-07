package it.eng.spagobi.api.v2;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.metadata.dao.ISbiMetaDocTabRelDAO;
import it.eng.spagobi.metadata.dao.ISbiMetaTableDAO;
import it.eng.spagobi.metadata.metadata.SbiMetaDocTabRel;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;

@Path("2.0/metaDocumetRelationResource")
@ManageAuthorization
public class MetaDocumetRelationResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(MetaDocumetRelationResource.class);

	private ISbiMetaTableDAO sbiMetaTableDao = null;
	private ISbiMetaDocTabRelDAO sbiMetaDocTabRelDAO = null;

	public MetaDocumetRelationResource() {

	}

	private void init() {
		sbiMetaDocTabRelDAO = DAOFactory.getSbiMetaDocTabRelDAO();
		sbiMetaTableDao = DAOFactory.getSbiMetaTableDAO();
	}

	@UserConstraint(functionalities = {CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT})
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SbiMetaDocTabRel> getAll() throws EMFUserError {
		init();
		List<SbiMetaDocTabRel> documentRelations = null;

		documentRelations = sbiMetaDocTabRelDAO.loadAllDocRelations();

		return documentRelations;
	}

	@UserConstraint(functionalities = {CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT})
	@GET
	@Path("/document/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SbiMetaTable> getByDocId(@PathParam("id") Integer documentId) {
		init();
		List<SbiMetaDocTabRel> relations = new ArrayList<>();
		List<SbiMetaTable> tables = new ArrayList<>();
		try {

			relations = sbiMetaDocTabRelDAO.loadByDocumentId(documentId);
			for (SbiMetaDocTabRel sbiMetaDsTabRel : relations) {
				SbiMetaTable table = sbiMetaTableDao.loadTableByID(sbiMetaDsTabRel.getTableId());
				tables.add(table);
			}
		} catch (EMFUserError e) {
			LOGGER.error("Error getting relation with id: " + documentId, e);
		}
		return tables;
	}

	@UserConstraint(functionalities = {CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT})
	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void insert(@PathParam("id") Integer id, SbiMetaTable table) throws EMFUserError {

		sbiMetaDocTabRelDAO = DAOFactory.getSbiMetaDocTabRelDAO();

		SbiMetaDocTabRel relation = new SbiMetaDocTabRel();
		relation.setDocumentId(id);
		relation.setTableId(table.getTableId());
		sbiMetaDocTabRelDAO.insertDocRelation(relation);

	}

	@UserConstraint(functionalities = {CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT})
	@DELETE
	@Path("/{id}/{tableID}")
	public void delete(@PathParam("id") Integer id, @PathParam("tableID") Integer tableID) {

		init();

		try {

			SbiMetaDocTabRel relation = sbiMetaDocTabRelDAO.loadDocIdandTableId(id, tableID);
			sbiMetaDocTabRelDAO.deleteDocRelation(relation);

		} catch (EMFUserError e) {

			LOGGER.error("Error deleting relation", e);
		}
	}
}
