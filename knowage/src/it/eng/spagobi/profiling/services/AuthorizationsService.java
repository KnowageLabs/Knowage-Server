package it.eng.spagobi.profiling.services;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.dao.ITenantsDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("/authorizations")
public class AuthorizationsService {

	static private Logger logger = Logger.getLogger(AuthorizationsService.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getAuthorizations(@Context HttpServletRequest req) {
		JSONObject documentJSON = new JSONObject();

		try {
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			if (profile instanceof UserProfile) {
				UserProfile spagoBIUserProfile = (UserProfile) profile;
				String organization = spagoBIUserProfile.getOrganization();

				ITenantsDAO tenantsDAO = DAOFactory.getTenantsDAO();
				List<Integer> productTypeIds = tenantsDAO.loadSelectedProductTypesIds(organization);
				IRoleDAO roleDAO = DAOFactory.getRoleDAO();
				List<String> authorizationsWithDuplicates = roleDAO.loadAllAuthorizationsNamesByProductTypes(productTypeIds);
				Set<String> authorizations = new HashSet<String>(authorizationsWithDuplicates);
				JSONArray authorizationsJSONArray = new JSONArray();

				for (String authorization : authorizations) {
					JSONObject authorizationJSONObject = new JSONObject();
					authorizationJSONObject.put("name", authorization);
					authorizationsJSONArray.put(authorizationJSONObject);
				}

				documentJSON.put("root", authorizationsJSONArray);
			}
			return documentJSON.toString();
		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving authorizations");
			throw new SpagoBIServiceException("An unexpected error occured while authorizations", t);
		}

	}

}
