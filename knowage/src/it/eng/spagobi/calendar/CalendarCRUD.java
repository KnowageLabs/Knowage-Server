package it.eng.spagobi.calendar;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.calendar.utils.CalendarUtilities;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.calendar.dao.ICalendarDAO;
import it.eng.spagobi.tools.calendar.metadata.Calendar;
import it.eng.spagobi.tools.calendar.metadata.CalendarAttributeDomain;
import it.eng.spagobi.tools.calendar.metadata.CalendarConfiguration;
import it.eng.spagobi.utilities.rest.RestUtilities;

@ManageAuthorization
@Path("/calendar")
public class CalendarCRUD {
	private SessionFactory aidaSession = null;
	private Session session = null;

	@GET
	@Path("/{id}/getCalendarById")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public void getCalendarById(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError, UnsupportedEncodingException, JSONException {
		aidaSession = CalendarUtilities.getHibSessionAida();
		session = aidaSession.openSession();
		ICalendarDAO dao = getCalendarDAO(req);
		Calendar cal = dao.loadCalendarById(id, session);

		System.out.println(cal.getCalendar());
		closeSession();
	}

	@GET
	@Path("/{id}/getInfoCalendarById")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getInfoCalendarById(@PathParam("id") Integer id, @Context HttpServletRequest req)
			throws EMFUserError, UnsupportedEncodingException, JSONException {
		aidaSession = CalendarUtilities.getHibSessionAida();
		session = aidaSession.openSession();
		ICalendarDAO dao = getCalendarDAO(req);
		Calendar cal = dao.loadCalendarById(id, session);
		List<CalendarConfiguration> listDays = dao.loadCalendarDays(cal.getCalendarId(), session);

		Response ret = Response.ok(JsonConverter.objectToJson(listDays, listDays.getClass())).build();
		closeSession();
		return ret;
	}

	@GET
	@Path("/getDomains")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDomains(@Context HttpServletRequest req) throws EMFUserError, UnsupportedEncodingException, JSONException {
		aidaSession = CalendarUtilities.getHibSessionAida();
		session = aidaSession.openSession();
		ICalendarDAO dao = getCalendarDAO(req);
		List<CalendarAttributeDomain> domains = dao.loadCalendarDomains(session);
		closeSession();
		return Response.ok(JsonConverter.objectToJson(domains, domains.getClass())).build();
	}

	@GET
	@Path("/getCalendarList")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getCalendarList(@Context HttpServletRequest req) throws EMFUserError, UnsupportedEncodingException, JSONException {
		aidaSession = CalendarUtilities.getHibSessionAida();
		session = aidaSession.openSession();
		ICalendarDAO dao = getCalendarDAO(req);
		List<Calendar> cal = dao.loadCalendarList(session);

		closeSession();
		return Response.ok(JsonConverter.objectToJson(cal, cal.getClass())).build();
	}

	@POST
	@Path("{id}/generateCalendarDays")
	public Response generateCalendarDays(@PathParam("id") Integer id, @Context HttpServletRequest req)
			throws EMFUserError, UnsupportedEncodingException, JSONException {
		aidaSession = CalendarUtilities.getHibSessionAida();
		session = aidaSession.openSession();
		ICalendarDAO dao = getCalendarDAO(req);

		List<CalendarConfiguration> daysGenerated = dao.generateCalendarDays(id, session);
		List<CalendarConfiguration> listDays = dao.loadCalendarDays(id, session);
		closeSession();
		return Response.ok(JsonConverter.objectToJson(listDays, listDays.getClass())).build();

	}

	@POST
	@Path("/saveCalendar")
	public String saveCalendar(@Context HttpServletRequest req) throws EMFUserError, UnsupportedEncodingException, JSONException {
		JSONObject requestBodyJSON;
		aidaSession = CalendarUtilities.getHibSessionAida();
		session = aidaSession.openSession();
		ICalendarDAO dao = getCalendarDAO(req);
		try {
			requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			Integer idInserted = dao.saveCalendar(requestBodyJSON, session);

			closeSession();

			return "" + idInserted + "";

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@POST
	@Path("{id}/updateDaysGenerated")
	public String updateDaysGenerated(@PathParam("id") Integer id, @Context HttpServletRequest req)
			throws EMFUserError, UnsupportedEncodingException, JSONException {
		JSONArray requestBodyJSON;
		aidaSession = CalendarUtilities.getHibSessionAida();
		session = aidaSession.openSession();
		ICalendarDAO dao = getCalendarDAO(req);
		try {
			requestBodyJSON = RestUtilities.readBodyAsJSONArray(req);
			dao.updateDaysGenerated(requestBodyJSON, session, id);

			closeSession();

			return "";

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@POST
	@Path("{id}/deleteCalendar")
	public void deleteCalendar(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError, UnsupportedEncodingException, JSONException {
		aidaSession = CalendarUtilities.getHibSessionAida();
		session = aidaSession.openSession();
		ICalendarDAO dao = getCalendarDAO(req);

		dao.deleteCalendar(id, session);
		closeSession();

	}

	@POST
	@Path("{id}/deleteDayofCalendar")
	public void deleteDayofCalendar(@PathParam("id") Integer id, @Context HttpServletRequest req)
			throws EMFUserError, UnsupportedEncodingException, JSONException {
		aidaSession = CalendarUtilities.getHibSessionAida();
		session = aidaSession.openSession();
		ICalendarDAO dao = getCalendarDAO(req);

		dao.deleteDayofCalendar(id, session);
		closeSession();

	}

	private static IEngUserProfile getProfile(HttpServletRequest req) {
		return (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	}

	private static void setProfile(HttpServletRequest req, ISpagoBIDao dao) {
		dao.setUserProfile(getProfile(req));
	}

	private static ICalendarDAO getCalendarDAO(HttpServletRequest req) throws EMFUserError {
		return DAOFactory.geCalendarDAO();

	}

	private void closeSession() {

		if (session != null) {
			if (session.isOpen())
				session.close();
		}
		if (aidaSession != null) {
			aidaSession.close();
		}
	}
}
