package it.eng.spagobi.api.v2;

import java.net.URI;
import java.net.URLEncoder;
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
import javax.ws.rs.core.Response;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoLayersDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/analyticalDrivers")
@ManageAuthorization
public class AnalyticalDriversResource extends AbstractSpagoBIResource {
	private final String charset = "; charset=UTF-8";

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PARAMETER_MANAGEMENT })
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getDrivers() {
		IParameterDAO driversDao = null;
		List<Parameter> fullList = null;

		try {

			driversDao = DAOFactory.getParameterDAO();
			driversDao.setUserProfile(getUserProfile());
			fullList = driversDao.loadAllParameters();
			return Response.ok(fullList).build();
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}

	}

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PARAMETER_MANAGEMENT })
	@Path("/layers")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getLayers() {
		ISbiGeoLayersDAO layersDao = DAOFactory.getSbiGeoLayerDao();
		List<GeoLayer> fullList = null;

		try {

			layersDao = DAOFactory.getSbiGeoLayerDao();
			layersDao.setUserProfile(getUserProfile());
			fullList = layersDao.loadAllLayers(null);
			return Response.ok(fullList).build();
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}

	}

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PARAMETER_MANAGEMENT })
	@Path("/checks")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getChecks() {
		ICheckDAO checksDao = null;
		List<Check> fullList = null;

		try {

			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			fullList = checksDao.loadAllChecks();
			return Response.ok(fullList).build();
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}

	}

	@GET
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.PARAMETER_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getDriversById(@PathParam("id") Integer id) {
		IParameterDAO driversDao = null;

		try {
			Parameter driver = new Parameter();
			driversDao = DAOFactory.getParameterDAO();
			driversDao.setUserProfile(getUserProfile());
			driver = driversDao.loadForDetailByParameterID(id);
			return Response.ok(driver).build();

		} catch (Exception e) {
			logger.error("Driver with selected id: " + id + " doesn't exists", e);
			throw new SpagoBIRestServiceException("Item with selected id: " + id + " doesn't exists", buildLocaleFromSession(), e);
		}

	}

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PARAMETER_MANAGEMENT })
	@Path("/{id}/modes")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getParModesForDriver(@PathParam("id") Integer id) {
		IParameterUseDAO useModesDao = null;
		List<ParameterUse> fullList = null;

		try {

			useModesDao = DAOFactory.getParameterUseDAO();
			useModesDao.setUserProfile(getUserProfile());
			fullList = useModesDao.loadParametersUseByParId(id);
			return Response.ok(fullList).build();
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}

	}

	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.PARAMETER_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertDriver(@Valid Parameter body) {

		IParameterDAO driversDao = null;
		Parameter driver = body;
		driver.setModality(driver.getType() + "," + driver.getTypeId().toString());
		if (driver.getId() != null) {
			logger.error("Error paramters. New check should not have ID value");
			throw new SpagoBIRuntimeException("Error paramters. New check should not have ID value");
		}

		try {
			driversDao = DAOFactory.getParameterDAO();
			driversDao.setUserProfile(getUserProfile());
			driversDao.insertParameter(driver);
			String encodedDriver = URLEncoder.encode("" + driver.getId(), "UTF-8");
			return Response.created(new URI("2.0/analyticalDrivers/" + encodedDriver)).entity(encodedDriver).build();
		} catch (Exception e) {
			logger.error("Error while inserting resource", e);
			throw new SpagoBIRestServiceException("Error while inserting resource", buildLocaleFromSession(), e);
		}
	}

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.PARAMETER_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDriver(@PathParam("id") Integer id, @Valid Parameter body) {

		IParameterDAO driversDao = null;
		Parameter driver = body;
		driver.setModality(driver.getType() + "," + driver.getTypeId().toString());
		if (driver.getId() == null) {
			logger.error("The check with ID " + id + " doesn't exist");
			throw new SpagoBIRuntimeException("The check with ID " + id + " doesn't exist");
		}

		try {
			driversDao = DAOFactory.getParameterDAO();
			driversDao.setUserProfile(getUserProfile());
			driversDao.modifyParameter(driver);
			String encodedDriver = URLEncoder.encode("" + driver.getId(), "UTF-8");
			return Response.created(new URI("2.0/analyticalDrivers/" + encodedDriver)).entity(encodedDriver).build();
		} catch (Exception e) {
			logger.error("Error while modifying resource with id: " + id, e);
			throw new SpagoBIRestServiceException("Error while modifying resource with id: " + id, buildLocaleFromSession(), e);
		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.PARAMETER_MANAGEMENT })
	public Response deleteDriver(@PathParam("id") Integer id) {

		IParameterDAO driversDao = null;
		IParameterUseDAO useModesDao = null;
		List<ParameterUse> fullList = null;

		try {
			Parameter driver = new Parameter();
			driver.setId(id);
			driversDao = DAOFactory.getParameterDAO();
			useModesDao = DAOFactory.getParameterUseDAO();
			driversDao.setUserProfile(getUserProfile());
			fullList = useModesDao.loadParametersUseByParId(id);
			if (fullList != null) {
				for (ParameterUse parameterUse : fullList) {
					useModesDao.eraseParameterUse(parameterUse);
				}
			}
			driversDao.eraseParameter(driver);

			String encodedDriver = URLEncoder.encode("" + driver.getId(), "UTF-8");
			return Response.ok().entity(encodedDriver).build();
		} catch (Exception e) {
			logger.error("Error with deleting resource with id: " + id, e);
			throw new SpagoBIRestServiceException("Error with deleting resource with id: " + id, buildLocaleFromSession(), e);
		}
	}
}
