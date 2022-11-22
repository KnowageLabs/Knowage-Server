package it.eng.spagobi.api.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

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

	static protected Logger logger = Logger.getLogger(DocumentExecutionResource.class);

	@GET
	@Path("/correctRolesForExecution")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentExecutionFilterList(@QueryParam("typeCode") String typeCode, @QueryParam("id") Integer id, @QueryParam("label") String label) {
		logger.debug("IN");

		UserProfile userProfile = UserProfileManager.getProfile();

		List<String> correctRoles = new ArrayList<String>();
		try {

			List<String> userRoles = new ArrayList<String>();
			userProfile.getRolesForUse().forEach(x -> userRoles.add((String) x));

			ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();

			List<String> correctRolesForDocumentExecution = null;
			List<String> rolesByCategory = null;
			List<String> modelRoles = null;
			if ("DATAMART".equals(typeCode)) {
				MetaModel model = DAOFactory.getMetaModelsDAO().loadMetaModelById(id);
				rolesByCategory = getRolesByCategory(categoryDao, model.getCategory());
				modelRoles = getModelRoles(userProfile, model);
			} else if ("DATASET".equals(typeCode)) {
				IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(id);
				Integer categoryId = dataset.getCategoryId();
				rolesByCategory = getRolesByCategory(categoryDao, categoryId);
			} else if ("DOCUMENT".equals(typeCode)) {
				ObjectsAccessVerifier oav = new ObjectsAccessVerifier();
				checkExecRightsByProducts(id, label);
				if (id != null) {
					correctRolesForDocumentExecution = oav.getCorrectRolesForExecution(id, userProfile);
				} else {
					correctRolesForDocumentExecution = oav.getCorrectRolesForExecution(label, userProfile);
				}
			}

			correctRoles = userRoles;

			if (rolesByCategory != null && rolesByCategory.size() > 0) {
				correctRoles = correctRoles.stream().filter(rolesByCategory::contains).collect(Collectors.toList());
			}

			if (modelRoles != null && modelRoles.size() > 0) {
				correctRoles = correctRoles.stream().filter(modelRoles::contains).collect(Collectors.toList());
			}

			if (correctRolesForDocumentExecution != null && correctRolesForDocumentExecution.size() > 0) {
				correctRoles = correctRoles.stream().filter(correctRolesForDocumentExecution::contains).collect(Collectors.toList());
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
		List<String> rolesByCategory = new ArrayList<String>();
		if (categoryId != null) {
			rolesByCategory = categoryDao.getRolesByCategory(categoryId).stream().map(SbiExtRoles::getName).collect(Collectors.toList());
		}
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
			throw new SpagoBIRuntimeException("This document cannot be executed within the current product!");
		}
	}

}
