package it.eng.spagobi.mapcatalogue.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectRating;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/documentrating")
@ManageAuthorization
public class DocumentRatingCRUD {
	static protected Logger logger = Logger.getLogger(DocumentRatingCRUD.class);

	@POST
	@Path("/vote")
	// BIObject obj,String userid, String rating
	public void voteBIObject(@Context HttpServletRequest req) throws EMFUserError {
		JSONObject requestVal;
		UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		try {
			requestVal = RestUtilities.readBodyAsJSONObject(req);
			String userId = profile.getUserId().toString();
			Integer rating = requestVal.getInt("rating");
			int id = requestVal.getInt("obj");

			IBIObjectDAO daoObject = DAOFactory.getBIObjectDAO();
			BIObject bioObject = daoObject.loadBIObjectById(id);

			IBIObjectRating dao = DAOFactory.getBIObjectRatingDAO();
			dao.voteBIObject(bioObject, userId, rating.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@POST
	@Path("/getvote")
	public Double getVote(@Context HttpServletRequest req) {
		// calculateBIObjectRating
		JSONObject requestVal;
		Double value = 0.0;
		UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		try {
			requestVal = RestUtilities.readBodyAsJSONObject(req);
			String userId = profile.getUserId().toString();
			int id = requestVal.getInt("obj");

			IBIObjectDAO daoObject = DAOFactory.getBIObjectDAO();
			BIObject bioObject = daoObject.loadBIObjectById(id);

			IBIObjectRating dao = DAOFactory.getBIObjectRatingDAO();
			value = dao.calculateBIObjectRating(bioObject);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return value;
	}

}
