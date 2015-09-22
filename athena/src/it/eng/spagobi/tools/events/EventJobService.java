package it.eng.spagobi.tools.events;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.events.dao.IEventJobDAO;
import it.eng.spagobi.tools.events.metadata.SbiEventJob;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.util.JSON;

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */
@Path("/1.0/eventJob")
public class EventJobService {

	@POST
	@Path("/addEvent")
	@Produces(MediaType.APPLICATION_JSON)
	public String addEvent(@Context HttpServletRequest req) {
		JSONObject jo = new JSONObject();
		try {

			IEventJobDAO dao = DAOFactory.getEventJobDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			SbiEventJob eve = new SbiEventJob();

			boolean update = false;

			if (requestVal.getInt("event_id") != -1) {
				// update event
				update = true;
				eve = dao.loadEvent(requestVal.getInt("event_id"));
			}

			eve.setDescription(requestVal.optString("description"));
			eve.setName(requestVal.optString("name"));
			eve.setEvent_type(requestVal.optString("event_type"));
			eve.setIs_suspended(requestVal.getBoolean("is_suspended"));

			if ("dataset".compareTo(requestVal.optString("event_type")) == 0) {
				eve.setDataset(requestVal.optInt("dataset"));
				eve.setFrequency(requestVal.optInt("frequency"));
			}

			if (update) {
				dao.updateEvent(eve);
			} else {
				Integer id = dao.addEvent(eve);
				jo.put("ID", id);
			}

			jo.put("Status", "OK");

		} catch (Throwable t) {
			try {
				jo.put("Status", "NON OK");
				jo.put("Message", "sbi.glossary.word.save.error");
				jo.put("Error_text", t.getMessage());
				t.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jo.toString();

	}

	@GET
	@Path("/listEvent")
	@Produces(MediaType.APPLICATION_JSON)
	public String listEvent(@Context HttpServletRequest req) {
		JSONObject jo = new JSONObject();
		try {

			IEventJobDAO dao = DAOFactory.getEventJobDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);

			List<SbiEventJob> le = dao.listEvent();

			JSONArray ja = new JSONArray();
			for (SbiEventJob se : le) {
				ja.put(JSON.parse(JsonConverter.objectToJson(se, se.getClass())));
			}
			jo.put("item", ja);

		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}

		return jo.toString();
	}

	@POST
	@Path("/deleteEvent")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.MANAGE_GLOSSARY_BUSINESS })
	public String deleteWord(@Context HttpServletRequest req) {
		JSONObject jo = new JSONObject();
		try {
			IEventJobDAO dao = DAOFactory.getEventJobDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			Integer id = requestVal.getInt("event_id");

			if (id == null) {
				jo.put("Status", "NON OK");
				jo.put("Message", "null.id");
				return jo.toString();
			}

			// dao.deleteWordReferences(wordId);
			dao.deleteEvent(id);

			jo.put("Status", "OK");
			return jo.toString();

		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}

}
