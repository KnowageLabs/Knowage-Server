package it.eng.spagobi.api.v2;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.metadata.dao.ISbiMetaDsTabRel;
import it.eng.spagobi.metadata.dao.ISbiMetaTableDAO;
import it.eng.spagobi.metadata.metadata.SbiMetaDsTabRel;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("2.0/metaDsRelationResource")
@ManageAuthorization
public class MetaDsRelationResource extends AbstractSpagoBIResource {

	private ISbiMetaDsTabRel sbiMetaDsTabRelDAO = null;
	private ISbiMetaTableDAO sbiMetaTableDao = null;

	public MetaDsRelationResource() {

	}

	private void init() {
			sbiMetaDsTabRelDAO = DAOFactory.getDsTableRelDAO();
			sbiMetaTableDao = DAOFactory.getSbiMetaTableDAO();
	}

	@UserConstraint(functionalities = { SpagoBIConstants.DATASET_MANAGEMENT })
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SbiMetaDsTabRel> getAll() {
		List<SbiMetaDsTabRel> relations = null;

		try {
			relations = sbiMetaDsTabRelDAO.loadAllRelations();
		} catch (EMFUserError e) {
			logger.error("Error getting all relations", e);
		}
		return relations;
	}

	@UserConstraint(functionalities = { SpagoBIConstants.DATASET_MANAGEMENT })
	@GET
	@Path("/dataset/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SbiMetaTable> getByDsId(@PathParam("id") Integer datasetId) {
		init();
		List<SbiMetaDsTabRel> relations = new ArrayList<>();
		List<SbiMetaTable> tables = new ArrayList<>();
		try {

			relations = sbiMetaDsTabRelDAO.loadByDatasetId(datasetId);
			for (SbiMetaDsTabRel sbiMetaDsTabRel : relations) {
				SbiMetaTable table = sbiMetaTableDao.loadTableByID(sbiMetaDsTabRel.getTableId());
				tables.add(table);
			}
		} catch (EMFUserError e) {
			logger.error("Error getting relation with id: " + datasetId, e);
		}
		return tables;
	}

	@UserConstraint(functionalities = { SpagoBIConstants.DATASET_MANAGEMENT })
	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void insert(@PathParam("id") Integer id, SbiMetaTable table) throws EMFUserError {

		sbiMetaDsTabRelDAO = DAOFactory.getDsTableRelDAO();

		SbiMetaDsTabRel relation = new SbiMetaDsTabRel();
		relation.setDatasetId(id);
		relation.setTableId(table.getTableId());
		sbiMetaDsTabRelDAO.insertRelation(relation);

	}

	@UserConstraint(functionalities = { SpagoBIConstants.DATASET_MANAGEMENT })
	@DELETE
	@Path("/{id}/{tableID}")
	public void delete(@PathParam("id") Integer id, @PathParam("tableID") Integer tableID) {

		init();

		try {

			SbiMetaDsTabRel relation = sbiMetaDsTabRelDAO.loadDsIdandTableId(id, tableID);
			sbiMetaDsTabRelDAO.deleteRelation(relation);

		} catch (EMFUserError e) {

			logger.error("Error deleting relation", e);
		}
	}

}
