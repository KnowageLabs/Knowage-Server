package it.eng.spagobi.analiticalmodel.execution.service.v2;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.hotlink.rememberme.bo.RememberMe;
import it.eng.spagobi.hotlink.rememberme.dao.IRememberMeDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;

import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

@Path("/2.0/analyticalmodel")
@ManageAuthorization
public class FavouritesResource extends AbstractSpagoBIAction {

	static protected Logger logger = Logger.getLogger(FavouritesResource.class);

	RememberMe rememberMe = null;
	List<RememberMe> rememberMeList;

	@GET
	@Path("/getrememberme/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public RememberMe getFavouritesById(@PathParam("id") Integer id) {
		logger.debug("IN");
		try {
			rememberMe = DAOFactory.getRememberMeDAO().getRememberMe(id);
		} catch (Exception e) {
			logger.error("Error while recovering favourites with id [" + id + "]", e);
		} finally {
			logger.debug("OUT");
		}
		return rememberMe;
	}

	@GET
	@Path("/getmyrememberme/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<RememberMe> getFavouritesOfUserByUserId(@PathParam("userId") String userId) {
		logger.debug("IN");
		try {
			rememberMeList = DAOFactory.getRememberMeDAO().getMyRememberMe(userId);
		} catch (Exception e) {
			logger.error("Error while recovering favourites of user [" + userId + "]", e);
		} finally {
			logger.debug("OUT");
		}
		return rememberMeList;
	}

	@DELETE
	@Path("/{id}")
	public void deleteFavoriteById(@PathParam("id") Integer id) {
		logger.debug("IN");
		try {
			DAOFactory.getRememberMeDAO().delete(id);
		} catch (Exception e) {
			logger.error("Error deleting a favorite documetn execution", e);
		} finally {
			logger.debug("OUT");
		}
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public String saveRememberMe(@Valid RememberMe rememberMe) {
		logger.debug("IN");
		try {
			// retrieving execution instance from session, no need to check if
			// user is able to execute the current document
			ExecutionInstance executionInstance = getContext().getExecutionInstance(ExecutionInstance.class.getName());
			BIObject obj = executionInstance.getBIObject();
			String name = getAttributeAsString("name");
			String description = getAttributeAsString("description");
			UserProfile profile = (UserProfile) this.getUserProfile();
			SubObject subobject = executionInstance.getSubObject();
			Integer subobjectId = null;
			if (subobject != null) {
				subobjectId = subobject.getId();
			}
			String parameters = getParametersQueryString(executionInstance);
			boolean inserted;
			try {
				IRememberMeDAO dao = DAOFactory.getRememberMeDAO();
				dao.setUserProfile(profile);
				inserted = dao.saveRememberMe(name, description, obj.getId(), subobjectId, profile.getUserId().toString(), parameters);
			} catch (Exception e) {
				logger.error("Cannot save remember me", e);
			}
		} finally {
			logger.debug("OUT");
		}
		return "";
	}

	private String getParametersQueryString(ExecutionInstance executionInstance) {
		logger.debug("IN");
		try {
			StringBuffer documentParametersStr = new StringBuffer();
			BIObject obj = executionInstance.getBIObject();
			List parametersList = obj.getBiObjectParameters();
			ParameterValuesEncoder parValuesEncoder = new ParameterValuesEncoder();
			if (parametersList != null && parametersList.size() > 0) {
				for (int i = 0; i < parametersList.size(); i++) {
					BIObjectParameter parameter = (BIObjectParameter) parametersList.get(i);
					if (parameter.getParameterValues() != null) {
						String value = parValuesEncoder.encode(parameter);
						documentParametersStr.append(parameter.getParameterUrlName() + "=" + value);
						if (i < parametersList.size() - 1)
							documentParametersStr.append("&");
					}
				}
			}
			if (documentParametersStr.length() > 1 && documentParametersStr.charAt(documentParametersStr.length() - 1) == '&') {
				documentParametersStr.deleteCharAt(documentParametersStr.length() - 1);
			}
			return documentParametersStr.toString();
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public void doService() {
		// TODO Auto-generated method stub

	}

}
