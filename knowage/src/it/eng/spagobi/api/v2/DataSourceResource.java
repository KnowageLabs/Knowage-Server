package it.eng.spagobi.api.v2;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.bo.DataSourceModel;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/2.0/datasources")
@ManageAuthorization
public class DataSourceResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DataSourceResource.class);

	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public List<DataSource> getListOfAllDataSources() {

		logger.debug("IN");

		List<DataSource> dataSource = null;
		IDataSourceDAO dataSourceDAO = null;

		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			// dataSourceDAO.setUserProfile(getUserProfile());
			dataSource = dataSourceDAO.loadAllDataSources();

			logger.debug("Getting the list of all DS - done successfully");

		} catch (Exception exception) {

			logger.error("Error while getting the list of DS", exception);
			throw new SpagoBIServiceException("Error while getting the list of DS", exception);

		} finally {

			LogMF.debug(logger, "OUT: returning [{0}]", dataSource.toString());

		}

		return dataSource;

	}

	@GET
	@Path("/{dsId}")
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public String getDataSourceById(@PathParam("dsId") Integer dsId) {

		logger.debug("IN");

		IDataSourceDAO dataSourceDAO = null;
		DataSource dataSource = null;

		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();

			dataSourceDAO.setUserProfile(getUserProfile());
			dataSource = dataSourceDAO.loadDataSourceByID(dsId);

		} catch (Exception e) {
			logger.error("Error while loading a single data set", e);
			throw new SpagoBIRuntimeException("Error while loading a single data set", e);

		}
		return JsonConverter.objectToJson(dataSource, null);

	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public String saveDataSource(DataSource dataSource) {

		IDataSourceDAO dataSourceDAO;

		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());

			dataSourceDAO.insertDataSource(dataSource, getUserProfile().getOrganization());

			logger.debug("OUT: Posting the DS - done successfully");

			DataSource newID = (DataSource) dataSourceDAO.loadDataSourceByLabelNew(dataSource.getLabel());
			int bla = newID.getDsId();

			return Integer.toString(bla);

		} catch (Exception exception) {

			logger.error("Error while posting DS", exception);
			throw new SpagoBIServiceException("Error while posting DS", exception);

		}
	}

	@POST
	@Path("/deleteDataSource")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public String deleteOneDataSource(@Context HttpServletRequest req) throws JSONException, EMFUserError, IOException {
		Object id = null;
		Integer dataSourceId = null;
		JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
		System.out.println(requestBodyJSON);
		try {

			id = requestBodyJSON.opt("DATASOURCE_ID");
			System.out.println(id);
			if (id == null || id.equals("")) {
				throw new SpagoBIRuntimeException("The DS id passed in the request is null or empty");
			}
			dataSourceId = new Integer(id.toString());
		} catch (Exception e) {
			logger.error("error loading the DS to delete from the request", e);
			throw new SpagoBIRuntimeException("error loading the DS to delete from the request", e);
		}

		logger.debug("Deleting the DS");
		IDataSourceDAO dao = DAOFactory.getDataSourceDAO();

		try {
			dao.deleteDataSourceById(dataSourceId);
		} catch (EMFUserError e) {
			logger.error("Error delationg the DS with id " + id, e);
			throw new SpagoBIRuntimeException("Error delationg the DS with id " + id, e);
		}
		return "{}";
	}

	@PUT
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDataSource(DataSourceModel dataSource) {

		logger.debug("IN");

		try {

			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();

			dataSourceDAO.setUserProfile(getUserProfile());
			dataSourceDAO.modifyDataSource(dataSource);

		} catch (Exception e) {
			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while updating url of the new resource", e);
		}
		return null;
	}

	@DELETE
	@Path("/{dsId}")
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public List<DataSource> deleteDataSource(@PathParam("dsId") Integer dsId) throws EMFUserError {

		logger.debug("IN");

		try {

			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();

			dataSourceDAO.setUserProfile(getUserProfile());
			dataSourceDAO.deleteDataSourceById(dsId);

		} catch (Exception e) {
			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while updating url of the new resource", e);

		}
		return DAOFactory.getDataSourceDAO().loadAllDataSources();

	}

}
