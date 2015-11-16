package it.eng.spagobi.tools.timespan;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.timespan.dao.ITimespanDAO;
import it.eng.spagobi.tools.timespan.metadata.SbiTimespan;
import it.eng.spagobi.tools.timespan.util.Util;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.Iterator;
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

@Path("/1.0/timespan")
@ManageAuthorization
public class TimespanService {

	@GET
	@Path("/listDynTimespan")
	@Produces(MediaType.APPLICATION_JSON)
//	@UserConstraint(functionalities = { SpagoBIConstants.CREATE_TIMESPAN })
	public String listDynTimespan(@Context HttpServletRequest req) {
		try {
			ITimespanDAO dao = DAOFactory.getTimespanDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);

			List<SbiTimespan> lst = dao.listDynTimespan();
			JSONArray ja = new JSONArray();
			if (lst != null) {
				for (Iterator<SbiTimespan> iterator = lst.iterator(); iterator.hasNext();) {
					SbiTimespan sbiTimespan = iterator.next();
					ja.put(Util.getAsJSON(sbiTimespan));
				}
			}
			return ja.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}

	@GET
	@Path("/listTimespan")
	@Produces(MediaType.APPLICATION_JSON)
//	@UserConstraint(functionalities = { SpagoBIConstants.CREATE_TIMESPAN })
	public String listAllTimespan(@Context HttpServletRequest req) {
		try {
			ITimespanDAO dao = DAOFactory.getTimespanDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);

			List<SbiTimespan> lst = dao.listTimespan();
			JSONArray ja = new JSONArray();
			if (lst != null) {
				for (Iterator<SbiTimespan> iterator = lst.iterator(); iterator.hasNext();) {
					SbiTimespan sbiTimespan = iterator.next();
					ja.put(Util.getAsJSON(sbiTimespan));
				}
			}
			JSONObject root = new JSONObject();
			root.put("data", ja);
			return root.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}


	@GET
	@Path("/loadTimespan")
	@Produces(MediaType.APPLICATION_JSON)
//	@UserConstraint(functionalities = { SpagoBIConstants.CREATE_TIMESPAN })
	public String loadTimespan(@Context HttpServletRequest req) {
		ITimespanDAO dao = null;
		try {
			dao = DAOFactory.getTimespanDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);
			SbiTimespan ts = dao.loadTimespan(Integer.parseInt(req.getParameter("ID")));
			JSONObject jo = Util.getAsJSON(ts);
			jo.put("Status", "OK");
			return jo.toString();

		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}


	@POST
	@Path("/saveTimespan")
	@Produces(MediaType.APPLICATION_JSON)
//	@UserConstraint(functionalities = { SpagoBIConstants.CREATE_TIMESPAN })
	public String saveTimespan(@Context HttpServletRequest req) {
		JSONObject jo = new JSONObject();
		try {
			ITimespanDAO dao = DAOFactory.getTimespanDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);
			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			SbiTimespan ts = new SbiTimespan();
			ts.setName((String) requestVal.opt("name"));
			ts.setType((String) requestVal.opt("type"));
			ts.setCategory((String) requestVal.opt("category"));
			ts.setDefinition(((JSONArray) requestVal.opt("definition")).toString());

			if ((Boolean) requestVal.opt("isnew")) {
				Integer id = dao.insertTimespan(ts);
				jo.put("id", id);
			} else {
				ts.setId((Integer) requestVal.opt("id"));
				dao.modifyTimespan(ts);
			}

			return jo.toString();

		} catch (Throwable t) {
			// throw new SpagoBIServiceException(req.getPathInfo(),
			// "An unexpected error occured while executing service", t);
			try {
				jo.put("Status", "KO");
				jo.put("Message", t.getCause().getCause().getMessage());
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return jo.toString();
		}
	}

	@POST
	@Path("/deleteTimespan")
	@Produces(MediaType.APPLICATION_JSON)
//	@UserConstraint(functionalities = { SpagoBIConstants.CREATE_TIMESPAN })
	public String deleteTimespan(@Context HttpServletRequest req) {
		try {
			ITimespanDAO dao = DAOFactory.getTimespanDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);

			Integer timespanId = Integer.parseInt(req.getParameter("ID"));

			dao.deleteTimespan(timespanId);

			JSONObject jo = new JSONObject();
			jo.put("Status", "OK");
			return jo.toString();

		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}
}
