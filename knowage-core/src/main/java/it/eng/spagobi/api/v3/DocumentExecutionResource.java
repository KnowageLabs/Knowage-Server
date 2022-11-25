package it.eng.spagobi.api.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonSyntaxException;

import it.eng.knowage.security.ProductProfiler;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * 
 * @author albnale
 *
 *         This service was created while porting Knowage Vue. It is used to retrieve the list of roles available for the user to execute the document you are
 *         trying to execute.
 */

@Path("/3.0/documentexecution")
public class DocumentExecutionResource {

	private static final String DOCUMENT = "DOCUMENT";
	private static final String DATASET = "DATASET";
	private static final String DATAMART = "DATAMART";
	private static final String FEDERATED_DATASET = "FEDERATED_DATASET";
	private static final String QBE_DATASET = "QBE_DATASET";

	static protected Logger logger = Logger.getLogger(DocumentExecutionResource.class);

	@GET
	@Path("/correctRolesForExecution")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getCorrectRolesForExecution(@QueryParam("typeCode") String typeCode, @QueryParam("id") Integer id, @QueryParam("label") String label) {
		logger.debug("IN");

		UserProfile userProfile = UserProfileManager.getProfile();

		List<String> correctRoles = new ArrayList<String>();
		try {

			List<String> userRoles = new ArrayList<String>();
			userProfile.getRolesForUse().forEach(x -> userRoles.add((String) x));

			ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();

			if (DATAMART.equals(typeCode)) {
				MetaModel model = DAOFactory.getMetaModelsDAO().loadMetaModelById(id);
				List<String> rolesByCategory = getRolesByCategory(categoryDao, model.getCategory());
				userRoles.retainAll(rolesByCategory);
				correctRoles = userRoles;

				List<BIMetaModelParameter> drivers = model.getDrivers();
				if (correctRoles.size() > 0 && drivers.size() > 0) {
					List<String> rolesByModel = getModelRoles(userProfile, model);
					correctRoles.retainAll(rolesByModel);
				}
			} else if (DATASET.equals(typeCode) || FEDERATED_DATASET.equals(typeCode)) {

				IDataSet dataset = null;
				if (DATASET.equals(typeCode)) {
					dataset = id != null ? DAOFactory.getDataSetDAO().loadDataSetById(id) : DAOFactory.getDataSetDAO().loadDataSetByLabel(label);
				} else {

					FederationDefinition federationDefinition = DAOFactory.getFedetatedDatasetDAO().loadFederationDefinition(id);

					Set<IDataSet> fedSourceDatasets = federationDefinition.getSourceDatasets();
					Optional<IDataSet> fedSourceDataset = fedSourceDatasets.stream().findFirst();
					if (!fedSourceDataset.isPresent()) {
						throw new SpagoBIRuntimeException("Error while getting the dataset info");
					}
					dataset = fedSourceDataset.get();

				}
				Integer categoryId = dataset.getCategoryId();
				if (categoryId != null) {
					List<String> rolesByCategory = getRolesByCategory(categoryDao, categoryId);
					userRoles.retainAll(rolesByCategory);
					correctRoles = userRoles;
				} else {
					correctRoles = userRoles.stream().collect(Collectors.toList());
				}
			}

			else if (QBE_DATASET.equals(typeCode)) {
				IDataSet dataset = id != null ? DAOFactory.getDataSetDAO().loadDataSetById(id) : DAOFactory.getDataSetDAO().loadDataSetByLabel(label);

				String conf = dataset.getConfiguration();
				try {
					String modelLabel = new JSONObject(conf).getString("qbeDatamarts");

					MetaModel model = DAOFactory.getMetaModelsDAO().loadMetaModelByName(modelLabel);
					List<String> rolesByCategory = getRolesByCategory(categoryDao, model.getCategory());
					userRoles.retainAll(rolesByCategory);
					correctRoles = userRoles;

					List<BIMetaModelParameter> drivers = model.getDrivers();
					if (correctRoles.size() > 0 && drivers.size() > 0) {
						List<String> rolesByModel = getModelRoles(userProfile, model);
						correctRoles.retainAll(rolesByModel);
					}
				} catch (JsonSyntaxException | JSONException e) {
					logger.error("An error occurred while parsing dataset configuration", e);
					throw new SpagoBIRuntimeException(e.getMessage(), e);
				}

			}

			else if (DOCUMENT.equals(typeCode)) {
				ObjectsAccessVerifier oav = new ObjectsAccessVerifier();
				checkExecRightsByProducts(id, label);
				if (id != null) {
					correctRoles = oav.getCorrectRolesForExecution(id, userProfile);
				} else {
					correctRoles = oav.getCorrectRolesForExecution(label, userProfile);
				}
			}

		} catch (EMFInternalError e) {
			logger.error("Cannot retrieve correct roles for execution", e);
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		} catch (EMFUserError e) {
			logger.error("Cannot retrieve correct roles for execution", e);
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		}

		logger.debug("OUT");
		return Response.ok().entity(correctRoles).build();
	}

	private List<String> getRolesByCategory(ICategoryDAO categoryDao, Integer categoryId) throws EMFUserError {
		List<String> rolesByCategory = categoryDao.getRolesByCategory(categoryId).stream().map(SbiExtRoles::getName).collect(Collectors.toList());
		return rolesByCategory;
	}

	private List<String> getModelRoles(UserProfile userProfile, MetaModel model) throws EMFInternalError {
		List<String> modelsRoles = new ArrayList<String>();
		List<BIMetaModelParameter> drivers = model.getDrivers();

		for (BIMetaModelParameter biMetaModelParameter : drivers) {
			Integer parId = biMetaModelParameter.getParameter().getId();
			for (Object role : userProfile.getRolesForUse()) {

				try {
					DAOFactory.getParameterDAO().loadForExecutionByParameterIDandRoleName(parId, (String) role, false);

					if (!modelsRoles.contains(role)) {
						modelsRoles.add(String.valueOf(role));
					}
				} catch (Exception e) {
					logger.debug(
							"Role " + role + " is not valid for model [" + model.getName() + "] execution. It will be not added to the available roles list.");
				}

			}
		}
		return modelsRoles;
	}

	private void checkExecRightsByProducts(Integer id, String label) throws EMFUserError {
		BIObject biobj = null;
		if (id != null) {
			biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(id);
		} else {
			biobj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(label);
		}
		if (!ProductProfiler.canExecuteDocument(biobj)) {
			throw new SpagoBIRuntimeException("This document cannot be executed within the current product");
		}
	}

}
