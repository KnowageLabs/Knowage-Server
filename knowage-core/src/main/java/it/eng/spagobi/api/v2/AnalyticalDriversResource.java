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
package it.eng.spagobi.api.v2;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.esapi.Encoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.knowage.analyticalDriver.api.AnalyticalDriverManagementAPI;
import it.eng.knowage.monitor.IKnowageMonitor;
import it.eng.knowage.monitor.KnowageMonitorFactory;
import it.eng.knowage.security.OwaspDefaultEncoderFactory;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleBO;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/analyticalDrivers")
@ManageAuthorization
public class AnalyticalDriversResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(AnalyticalDriversResource.class);

	private final String charset = "; charset=UTF-8";

	@GET
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PARAMETER_MANAGEMENT })
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getDrivers() {
		IParameterDAO driversDao = null;
		List<Parameter> fullList = null;

		IKnowageMonitor monitor = KnowageMonitorFactory.getInstance().start("knowage.analyticaldrivers.list");

		try {

			driversDao = DAOFactory.getParameterDAO();
			driversDao.setUserProfile(getUserProfile());
			fullList = driversDao.loadAllParameters();
			Response response = Response.ok(fullList).build();

			monitor.stop();

			return response;
		} catch (Exception e) {
			LOGGER.error("Error with loading resource", e);
			monitor.stop(e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}

	}

	// @GET
	// @UserConstraint(functionalities = { SpagoBIConstants.PARAMETER_MANAGEMENT })
	// @Path("/layers")
	// @Produces(MediaType.APPLICATION_JSON + charset)
	// public Response getLayers() {
	// ISbiGeoLayersDAO layersDao = DAOFactory.getSbiGeoLayerDao();
	// List<GeoLayer> fullList = null;
	//
	// try {
	//
	// layersDao = DAOFactory.getSbiGeoLayerDao();
	// layersDao.setUserProfile(getUserProfile());
	// fullList = layersDao.loadAllLayers(null, getUserProfile());
	// return Response.ok(fullList).build();
	// } catch (Exception e) {
	// logger.error("Error with loading resource", e);
	// throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
	// }
	//
	// }

	@GET
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PARAMETER_MANAGEMENT })
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
			LOGGER.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}

	}

	@GET
	@Path("/{id}")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PARAMETER_MANAGEMENT })
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
			LOGGER.error("Driver with selected id {} doesn't exists", id, e);
			throw new SpagoBIRestServiceException("Item with selected id: " + id + " doesn't exists",
					buildLocaleFromSession(), e);
		}

	}

	@GET
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PARAMETER_MANAGEMENT })
	@Path("/{id}/modes")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getParModesForDriver(@PathParam("id") Integer id) {

		try {

			IParameterUseDAO useModesDao = DAOFactory.getParameterUseDAO();
			useModesDao.setUserProfile(getUserProfile());
			List<ParameterUse> fullList = useModesDao.loadParametersUseByParId(id);
			return Response.ok(fullList).build();
		} catch (Exception e) {
			LOGGER.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}

	}

	@GET
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PARAMETER_MANAGEMENT })
	@Path("/{id}/lovs")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getLovsForDriver(@PathParam("id") Integer idParameter) {

		LOGGER.debug("IN");

		List<ModalitiesValue> modalitiesValues = null;
		IModalitiesValueDAO modalitiesValueDAO = null;

		try {

			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			modalitiesValues = modalitiesValueDAO.loadModalitiesValueByParamaterId(idParameter);

			return Response.ok(modalitiesValues).build();
		} catch (Exception e) {
			LOGGER.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}

	}

	@GET
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PARAMETER_MANAGEMENT })
	@Path("/{id}/documents")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getDocumentsById(@PathParam("id") Integer idParameter) {

		IBIObjectDAO documentsDao = null;
		List<BIObject> documents = null;
		LOGGER.debug("IN");

		try {
			documentsDao = DAOFactory.getBIObjectDAO();
			documentsDao.setUserProfile(getUserProfile());
			documents = documentsDao.loadBIObjectsByParamterId(idParameter);

			return Response.ok(documents).build();
		} catch (Exception e) {
			LOGGER.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}

	}

	@POST
	@Path("/")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PARAMETER_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertDriver(@Valid Parameter driver) {

		driver.setModality(driver.getType() + "," + driver.getTypeId().toString());
		if (driver.getId() != null) {
			LOGGER.error("Error paramters. New check should not have ID value");
			throw new SpagoBIRuntimeException("Error paramters. New check should not have ID value");
		}
		if (parameterLabelNameControl(driver, "INSERT", "label")) {
			LOGGER.error("Error while inserting AD. Analytical Driver with the same label already exists.");
			throw new SpagoBIRuntimeException(
					"Error while inserting AD. Analytical Driver with the same label already exists.");
		}
		if (parameterLabelNameControl(driver, "INSERT", "name")) {
			LOGGER.error("Error while inserting AD. Analytical Driver with the same name already exists.");
			throw new SpagoBIRuntimeException(
					"Error while inserting AD. Analytical Driver with the same name already exists.");
		}
		try {
			IParameterDAO driversDao = DAOFactory.getParameterDAO();
			driversDao.setUserProfile(getUserProfile());
			Parameter toReturn = driversDao.insertParameter(driver);
			return Response.ok(toReturn).build();
		} catch (Exception e) {
			LOGGER.error("Error while inserting resource", e);
			throw new SpagoBIRestServiceException("Error while inserting resource", buildLocaleFromSession(), e);
		}
	}

	@POST
	@Path("/modes")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PARAMETER_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertUseMode(ParameterUse useMode) {
		ObjectMapper mapper = new ObjectMapper();
		IParameterUseDAO useModesDao = null;

		List<Role> roles = useMode.getAssociatedRoles();
		List<Check> checks = useMode.getAssociatedChecks();
		if (useMode.getUseID() != null) {
			LOGGER.error("Error paramters. New check should not have ID value");
			throw new SpagoBIRuntimeException("Error paramters. New check should not have ID value");
		}
		if (parameterUseLabelControl(useMode, "INSERT")) {
			LOGGER.error("Error inserting parameter.Same nqame already exists");
			throw new SpagoBIRuntimeException("Error inserting use mode.Same name already exists");
		}

		List<Role> formatedRoles = new ArrayList<>();
		List<Check> formatedChecks = new ArrayList<>();
		for (Role temp : roles) {
			RoleBO role = mapper.convertValue(temp, RoleBO.class);
			formatedRoles.add(BOtoRole(role));
		}
		for (Check temp : checks) {
			Check check = mapper.convertValue(temp, Check.class);
			formatedChecks.add(check);
		}
		useMode.setAssociatedRoles(formatedRoles);
		useMode.setAssociatedChecks(formatedChecks);
		try {
			useModesDao = DAOFactory.getParameterUseDAO();
			useModesDao.setUserProfile(getUserProfile());
			useModesDao.insertParameterUse(useMode);

			Encoder encoder = OwaspDefaultEncoderFactory.getInstance().getEncoder();
			String encodedUseMode = encoder.encodeForURL("" + useMode.getUseID());
			return Response.created(new URI("2.0/analyticalDrivers/" + encodedUseMode)).entity(encodedUseMode).build();
		} catch (Exception e) {
			LOGGER.error("Error while inserting resource", e);
			throw new SpagoBIRestServiceException("Error while inserting resource", buildLocaleFromSession(), e);
		}
	}

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PARAMETER_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDriver(@PathParam("id") Integer id, @Valid Parameter driver) {

		driver.setModality(driver.getType() + "," + driver.getTypeId().toString());
		if (driver.getId() == null) {
			LOGGER.error("The check with ID {} doesn't exist", id);
			throw new SpagoBIRuntimeException("The check with ID " + id + " doesn't exist");
		}

		try {
			JSONObject response = new JSONObject();
			JSONArray warnings = new JSONArray();

			IParameterDAO driversDao = DAOFactory.getParameterDAO();
			driversDao.setUserProfile(getUserProfile());

			Parameter oldDriver = driversDao.loadForDetailByParameterID(driver.getId());
			if (oldDriver.getTypeId().compareTo(driver.getTypeId()) != 0) {
				AnalyticalDriverManagementAPI analyticalDriverManagementAPI = new AnalyticalDriverManagementAPI();
				if (analyticalDriverManagementAPI.isUsedInCrossNavigations(driver)) {
					warnings.put("Analitycal driver " + driver.getName() + " is used in one or more cross navigations");
				}
				response.put("warnings", warnings);
			}

			driversDao.modifyParameter(driver);

			Encoder encoder = OwaspDefaultEncoderFactory.getInstance().getEncoder();
			String encodedDriver = encoder.encodeForURL("" + driver.getId());

			JSONArray jsonArray = new JSONArray();
			response.put("encodedDriver", encodedDriver);

			jsonArray.put(response);

			return Response.created(new URI("2.0/analyticalDrivers/" + encodedDriver)).entity(response.toString())
					.build();
		} catch (Exception e) {
			LOGGER.error("Error while modifying resource with id: {}", id, e);
			throw new SpagoBIRestServiceException("Error while modifying resource with id: " + id,
					buildLocaleFromSession(), e);
		}
	}

	@PUT
	@Path("/modes/{id}")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PARAMETER_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUseMode(@PathParam("id") Integer id, String body) {

		IParameterUseDAO useModesDao = null;
		ParameterUse useMode = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			useMode = mapper.readValue(body, ParameterUse.class);
		} catch (Exception e1) {
			LOGGER.error(e1);
			throw new SpagoBIRestServiceException("Error while inserting resource", buildLocaleFromSession(), e1);
		}
		List<Role> roles = useMode.getAssociatedRoles();
		List<Check> checks = useMode.getAssociatedChecks();
		if (useMode.getUseID() == null) {
			LOGGER.error("The check with ID {} doesn't exist", id);
			throw new SpagoBIRuntimeException("The check with ID " + id + " doesn't exist");
		}
		List<Role> formatedRoles = new ArrayList<>();
		List<Check> formatedChecks = new ArrayList<>();
		for (Role temp : roles) {
			RoleBO role = mapper.convertValue(temp, RoleBO.class);
			formatedRoles.add(BOtoRole(role));
		}
		for (Check temp : checks) {
			Check check = mapper.convertValue(temp, Check.class);
			formatedChecks.add(check);
		}
		useMode.setAssociatedRoles(formatedRoles);
		useMode.setAssociatedChecks(formatedChecks);
		List documents = null;
		try {
			useModesDao = DAOFactory.getParameterUseDAO();
			IObjParuseDAO objParuseDAO = DAOFactory.getObjParuseDAO();
			useModesDao.setUserProfile(getUserProfile());
			ParameterUse useModeFromDao = useModesDao.loadByUseID(useMode.getUseID());
			documents = objParuseDAO.getDocumentLabelsListWithAssociatedDependencies(useMode.getUseID());
			if (!documents.isEmpty()) {
				// there are some correlations
				if (useMode.getManualInput() == 1
						|| useMode.getIdLov().intValue() != useModeFromDao.getIdLov().intValue()) {
					// the ParameterUse was changed to manual input or the lov id was changed
					LOGGER.error("Cant modify use mode because it is used in some documents");
					throw new SpagoBIRuntimeException("Cant modify use mode because it is used");
				}
			}
			useModesDao.modifyParameterUse(useMode);
			Encoder encoder = OwaspDefaultEncoderFactory.getInstance().getEncoder();
			String encodedUseMode = encoder.encodeForURL("" + useMode.getUseID());
			return Response.created(new URI("2.0/analyticalDrivers/" + encodedUseMode)).entity(encodedUseMode).build();
		} catch (Exception e) {
			LOGGER.error("Error while modifying resource with id: {}", id, e);
			throw new SpagoBIRestServiceException("Error while modifying use mode : " + useMode.getName() + ", check correlations inside documents with label " + (documents != null ? documents.toString() : ""),
					buildLocaleFromSession(), e);
		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PARAMETER_MANAGEMENT })
	public Response deleteDriver(@PathParam("id") Integer id) {

		try {
			Parameter driver = new Parameter();
			driver.setId(id);
			IParameterDAO driversDao = DAOFactory.getParameterDAO();
			IParameterUseDAO useModesDao = DAOFactory.getParameterUseDAO();
			driversDao.setUserProfile(getUserProfile());
			List<ParameterUse> fullList = useModesDao.loadParametersUseByParId(id);
			List<String> objectsLabels = DAOFactory.getBIObjectParameterDAO().getDocumentLabelsListUsingParameter(id);
			if (objectsLabels != null && !objectsLabels.isEmpty()) {
				LOGGER.error("Driver in use");
				throw new SpagoBIRuntimeException("Driver in use");
			}
			if (fullList != null) {
				for (ParameterUse parameterUse : fullList) {
					useModesDao.eraseParameterUse(parameterUse);
				}
			}
			driversDao.eraseParameter(driver);

			Encoder encoder = OwaspDefaultEncoderFactory.getInstance().getEncoder();
			String encodedDriver = encoder.encodeForURL("" + driver.getId());
			return Response.ok().entity(encodedDriver).build();
		} catch (Exception e) {
			LOGGER.error("Error with deleting resource with id: {}", id, e);
			throw new SpagoBIRestServiceException("Error with deleting resource with id: " + id,
					buildLocaleFromSession(), e);
		}
	}

	@DELETE
	@Path("/modes/{id}")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PARAMETER_MANAGEMENT })
	public Response deleteUseMode(@PathParam("id") Integer id) {

		IParameterUseDAO useModesDao = null;

		try {
			ParameterUse mode = new ParameterUse();
			mode.setUseID(id);
			useModesDao = DAOFactory.getParameterUseDAO();
			useModesDao.setUserProfile(getUserProfile());
			useModesDao.eraseParameterUse(mode);

			Encoder encoder = OwaspDefaultEncoderFactory.getInstance().getEncoder();
			String encodedMode = encoder
					.encodeForURL("" + mode.getUseID());
			return Response.ok().entity(encodedMode).build();
		} catch (Exception e) {
			LOGGER.error("Error with deleting resource with id: {}", id, e);
			throw new SpagoBIRestServiceException("Error with deleting resource with id: " + id,
					buildLocaleFromSession(), e);
		}
	}

	public Role BOtoRole(RoleBO bo) {
		Role role = new Role();
		role.setId(bo.getId());
		role.setName(bo.getName());
		role.setCode(bo.getCode());
		role.setDescription(bo.getDescription());
		role.setRoleTypeCD(bo.getRoleTypeCD());
		role.setRoleTypeID(bo.getRoleTypeID());

		role.setAbleToSaveIntoPersonalFolder(bo.isAbleToSaveIntoPersonalFolder());
		role.setAbleToEnableDatasetPersistence(bo.isAbleToEnableDatasetPersistence());
		role.setAbleToEnableFederatedDataset(bo.isAbleToEnableFederatedDataset());
		role.setAbleToEnableRate(bo.isAbleToEnableRate());
		role.setAbleToEnablePrint(bo.isAbleToEnablePrint());
		role.setAbleToEnableCopyAndEmbed(bo.isAbleToEnableCopyAndEmbed());
		role.setAbleToManageGlossaryBusiness(bo.isAbleToManageGlossaryBusiness());
		role.setAbleToManageGlossaryTechnical(bo.isAbleToManageGlossaryTechnical());
		role.setAbleToManageKpiValue(bo.isAbleToManageKpiValue());
		role.setAbleToManageCalendar(bo.isAbleToManageCalendar());
		role.setAbleToUseFunctionsCatalog(bo.isAbleToUseFunctionsCatalog());
		role.setAbleToEditPythonScripts(bo.isAbleToEditPythonScripts());
		role.setAbleToCreateCustomChart(bo.isAbleToCreateCustomChart());
		role.setAbleToSaveSubobjects(bo.isAbleToSaveSubobjects());
		role.setAbleToSeeSubobjects(bo.isAbleToSeeSubobjects());
		role.setAbleToSeeViewpoints(bo.isAbleToSeeViewpoints());
		role.setAbleToSeeSnapshots(bo.isAbleToSeeSnapshots());
		role.setAbleToRunSnapshots(bo.isAbleToRunSnapshots());
		role.setAbleToSeeNotes(bo.isAbleToSeeNotes());
		role.setAbleToSendMail(bo.isAbleToSendMail());
		role.setAbleToSaveRememberMe(bo.isAbleToSaveRememberMe());
		role.setAbleToSeeMetadata(bo.isAbleToSeeMetadata());
		role.setAbleToSaveMetadata(bo.isAbleToSaveMetadata());
		role.setAbleToBuildQbeQuery(bo.isAbleToBuildQbeQuery());
		role.setAbleToDoMassiveExport(bo.isAbleToDoMassiveExport());
		role.setAbleToManageUsers(bo.isAbleToManageUsers());
		role.setAbleToSeeDocumentBrowser(bo.isAbleToSeeDocumentBrowser());
		role.setAbleToSeeFavourites(bo.isAbleToSeeFavourites());
		role.setAbleToSeeSubscriptions(bo.isAbleToSeeSubscriptions());
		role.setAbleToSeeMyData(bo.isAbleToSeeMyData());
		role.setAbleToSeeMyWorkspace(bo.isAbleToSeeMyWorkspace());
		role.setAbleToSeeToDoList(bo.isAbleToSeeToDoList());
		role.setAbleToCreateDocuments(bo.isAbleToCreateDocuments());
		role.setAbleToCreateSocialAnalysis(bo.isAbleToCreateSocialAnalysis());
		role.setAbleToViewSocialAnalysis(bo.isAbleToViewSocialAnalysis());
		role.setAbleToHierarchiesManagement(bo.isAbleToHierarchiesManagement());
		role.setAbleToEditAllKpiComm(bo.isAbleToEditAllKpiComm());
		role.setAbleToEditMyKpiComm(bo.isAbleToEditMyKpiComm());
		role.setAbleToDeleteKpiComm(bo.isAbleToDeleteKpiComm());

		return role;
	}

	private boolean parameterLabelNameControl(Parameter parameter, String operation, String comparator) {
		String labelToCheck = parameter.getLabel();
		String nameToCheck = parameter.getName();
		List allparameters = null;
		try {
			allparameters = DAOFactory.getParameterDAO().loadAllParameters();
		} catch (EMFUserError e) {
			LOGGER.error("Error loading Analytical Driver for label testing");
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
		if (operation.equalsIgnoreCase("INSERT")) {
			Iterator i = allparameters.iterator();
			while (i.hasNext()) {
				Parameter aParameter = (Parameter) i.next();
				if (comparator.equalsIgnoreCase("label")) {
					String label = aParameter.getLabel();
					if (label.equals(labelToCheck)) {
						return true;
					}
				} else if (comparator.equalsIgnoreCase("name")) {
					String name = aParameter.getName();
					if (name.equals(nameToCheck)) {
						return true;
					}
				}
			}
		} else {
			Integer currentId = parameter.getId();
			Iterator i = allparameters.iterator();
			while (i.hasNext()) {
				Parameter aParameter = (Parameter) i.next();
				Integer id = aParameter.getId();
				if (comparator.equalsIgnoreCase("label")) {
					String label = aParameter.getLabel();
					if (label.equals(labelToCheck) && (!id.equals(currentId))) {
						return true;
					}
				} else if (comparator.equalsIgnoreCase("name")) {
					String name = aParameter.getName();
					if (name.equals(nameToCheck) && (!id.equals(currentId))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Controls if the name of the ParameterUse is already in use.
	 *
	 * @param paruse    The paruse to check
	 * @param operation Defines if the operation is of insertion or modify
	 * @throws EMFUserError If any Exception occurred
	 */
	private boolean parameterUseLabelControl(ParameterUse paruse, String operation) {

		Integer parId = paruse.getId();
		String labelToCheck = paruse.getLabel();
		List<ParameterUse> allParametersUse = null;
		try {
			allParametersUse = DAOFactory.getParameterUseDAO().loadParametersUseByParId(parId);
		} catch (EMFUserError e) {
			LOGGER.error("Error loading Use Modes for label testing");
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
		// cannot have two ParametersUse with the same label and the same par_id
		if (operation.equalsIgnoreCase("INSERT")) {
			Iterator<ParameterUse> i = allParametersUse.iterator();
			while (i.hasNext()) {
				ParameterUse aParameterUse = i.next();
				String label = aParameterUse.getLabel();
				if (label.equals(labelToCheck)) {
					return true;
				}
			}
		} else {
			Integer currentUseId = paruse.getUseID();
			Iterator<ParameterUse> i = allParametersUse.iterator();
			while (i.hasNext()) {
				ParameterUse aParameterUse = i.next();
				String label = aParameterUse.getLabel();
				Integer useId = aParameterUse.getUseID();

				if (label.equals(labelToCheck) && (!useId.equals(currentUseId))) {
					return true;
				}
			}
		}
		return false;
	}
}
