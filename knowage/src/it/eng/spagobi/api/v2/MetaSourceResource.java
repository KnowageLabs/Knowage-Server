package it.eng.spagobi.api.v2;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.metadata.dao.ISbiMetaSourceDAO;
import it.eng.spagobi.metadata.dao.ISbiMetaTableDAO;
import it.eng.spagobi.metadata.metadata.SbiMetaSource;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("2.0/metaSourceResource")
@ManageAuthorization
public class MetaSourceResource extends AbstractSpagoBIResource {

	private ISbiMetaSourceDAO sbiMetaSourceDAO = null;
	private ISbiMetaTableDAO sbiMetaTableDao = null;

	public MetaSourceResource() {

	}

	private void init() {
		try {
			sbiMetaSourceDAO = DAOFactory.getSbiMetaSourceDAO();

		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	public List<SbiMetaSource> getAll() {
		List<SbiMetaSource> sources = null;
		try {
			sbiMetaSourceDAO = DAOFactory.getSbiMetaSourceDAO();

		} catch (EMFUserError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			sources = sbiMetaSourceDAO.loadAllSources();
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sources;
	}

	public SbiMetaSource getById(Integer id) {
		return null;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(metaTables);
		return metaTables;
	}

	public SbiMetaTable getTableById(Integer id, Integer tableId) {
		return null;
	}

	@POST
	@Path("/")
	public void insert(SbiMetaSource sbiMetaSource) {
		try {
			sbiMetaSourceDAO = DAOFactory.getSbiMetaSourceDAO();

		} catch (EMFUserError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			sbiMetaSourceDAO.insertSource(sbiMetaSource);
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void insertTable(Integer id, SbiMetaTable sbiMetaTable) {

	}

	public void modify(SbiMetaSource sbiMetaSource, Integer id) {

	}

	public void modifyTable(Integer id, SbiMetaTable sbiMetaTable) {

	}

	public Integer delete(Integer id) {
		return null;
	}

	public Integer deleteTable(Integer id, Integer tableId) {

		return null;

	}

}
