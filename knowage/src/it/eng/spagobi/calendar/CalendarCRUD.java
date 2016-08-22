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

package it.eng.spagobi.calendar;

import java.io.IOException;
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

import org.apache.log4j.Logger;
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
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@ManageAuthorization
@Path("/calendar")
public class CalendarCRUD {

	private static Logger logger = Logger.getLogger(CalendarCRUD.class);

	@GET
	@Path("/{id}/getCalendarById")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public void getCalendarById(@PathParam("id") Integer id, @Context HttpServletRequest req) {
		SessionFactory aidaSession = null;
		Session session = null;
		try {
			aidaSession = CalendarUtilities.getHibSessionAida();

			session = aidaSession.openSession();
			ICalendarDAO dao = getCalendarDAO(req);
			logger.debug("load Calendar by Id");
			Calendar cal = dao.loadCalendarById(id, session);

		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			logger.error("getCalendarById error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} finally {

			if (session != null) {
				if (session.isOpen())
					session.close();
			}
			if (aidaSession != null) {
				aidaSession.close();
			}
		}
	}

	@GET
	@Path("/{id}/getInfoCalendarById")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getInfoCalendarById(@PathParam("id") Integer id, @Context HttpServletRequest req) {
		SessionFactory aidaSession = null;
		Session session = null;
		logger.debug("open Session");
		try {
			aidaSession = CalendarUtilities.getHibSessionAida();

			session = aidaSession.openSession();

			ICalendarDAO dao = getCalendarDAO(req);
			logger.debug("load Calendar by Id");
			Calendar cal = dao.loadCalendarById(id, session);
			logger.debug("load Calendar Days");
			List<CalendarConfiguration> listDays = dao.loadCalendarDays(cal.getCalendarId(), session);

			Response ret = Response.ok(JsonConverter.objectToJson(listDays, listDays.getClass())).build();

			return ret;

		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			logger.error("getInfoCalendarById error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} finally {

			if (session != null) {
				if (session.isOpen())
					session.close();
			}
			if (aidaSession != null) {
				aidaSession.close();
			}
		}
	}

	@GET
	@Path("/getDomains")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDomains(@Context HttpServletRequest req) {
		SessionFactory aidaSession = null;
		Session session = null;

		try {
			aidaSession = CalendarUtilities.getHibSessionAida();

			session = aidaSession.openSession();
			ICalendarDAO dao = getCalendarDAO(req);
			logger.debug("load Calendar Attribute Domains");
			List<CalendarAttributeDomain> domains = dao.loadCalendarDomains(session);

			return Response.ok(JsonConverter.objectToJson(domains, domains.getClass())).build();
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			logger.error("getDomains error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} finally {

			if (session != null) {
				if (session.isOpen())
					session.close();
			}
			if (aidaSession != null) {
				aidaSession.close();
			}
		}
	}

	@GET
	@Path("/getCalendarList")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getCalendarList(@Context HttpServletRequest req) {
		SessionFactory aidaSession = null;
		Session session = null;
		try {
			aidaSession = CalendarUtilities.getHibSessionAida();

			session = aidaSession.openSession();
			ICalendarDAO dao = getCalendarDAO(req);

			logger.debug("load Calendar List");
			List<Calendar> cal = dao.loadCalendarList(session);

			return Response.ok(JsonConverter.objectToJson(cal, cal.getClass())).build();
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			logger.error("getCalendarList error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} finally {

			if (session != null) {
				if (session.isOpen())
					session.close();
			}
			if (aidaSession != null) {
				aidaSession.close();
			}
		}
	}

	@POST
	@Path("{id}/generateCalendarDays")
	public Response generateCalendarDays(@PathParam("id") Integer id, @Context HttpServletRequest req) {
		SessionFactory aidaSession = null;
		Session session = null;
		try {
			aidaSession = CalendarUtilities.getHibSessionAida();

			session = aidaSession.openSession();
			ICalendarDAO dao = getCalendarDAO(req);
			logger.debug("Generate Calendar Days");
			List<CalendarConfiguration> daysGenerated = dao.generateCalendarDays(id, session);
			logger.debug("load Calendar days");
			List<CalendarConfiguration> listDays = dao.loadCalendarDays(id, session);

			return Response.ok(JsonConverter.objectToJson(listDays, listDays.getClass())).build();
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			logger.error("generateCalendarDays error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} finally {

			if (session != null) {
				if (session.isOpen())
					session.close();
			}
			if (aidaSession != null) {
				aidaSession.close();
			}
		}
	}

	@POST
	@Path("/saveCalendar")
	public String saveCalendar(@Context HttpServletRequest req) {
		JSONObject requestBodyJSON;
		SessionFactory aidaSession = null;
		Session session = null;
		try {
			aidaSession = CalendarUtilities.getHibSessionAida();
			session = aidaSession.openSession();
			ICalendarDAO dao = getCalendarDAO(req);

			requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			logger.debug("save Calendar");
			Integer idInserted = dao.saveCalendar(requestBodyJSON, session);

			return "" + idInserted + "";

		} catch (IOException e) {
			logger.error("saveCalendar error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} catch (EMFUserError e) {
			logger.error("saveCalendar error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} catch (JSONException e) {
			logger.error("saveCalendar error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} finally {

			if (session != null) {
				if (session.isOpen())
					session.close();
			}
			if (aidaSession != null) {
				aidaSession.close();
			}
		}

	}

	@POST
	@Path("{id}/updateDaysGenerated")
	public String updateDaysGenerated(@PathParam("id") Integer id, @Context HttpServletRequest req) {
		JSONArray requestBodyJSON;
		SessionFactory aidaSession = null;
		Session session = null;
		try {
			aidaSession = CalendarUtilities.getHibSessionAida();
			session = aidaSession.openSession();
			ICalendarDAO dao = getCalendarDAO(req);

			requestBodyJSON = RestUtilities.readBodyAsJSONArray(req);
			logger.debug("Update Days Generated");
			dao.updateDaysGenerated(requestBodyJSON, session, id);

			return "";

		} catch (IOException e) {
			logger.error("updateDaysGenerated error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} catch (EMFUserError e) {
			logger.error("updateDaysGenerated error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} catch (JSONException e) {
			logger.error("updateDaysGenerated error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} finally {

			if (session != null) {
				if (session.isOpen())
					session.close();
			}
			if (aidaSession != null) {
				aidaSession.close();
			}
		}
	}

	@POST
	@Path("{id}/deleteCalendar")
	public void deleteCalendar(@PathParam("id") Integer id, @Context HttpServletRequest req) {
		SessionFactory aidaSession = null;
		Session session = null;
		try {
			aidaSession = CalendarUtilities.getHibSessionAida();

			session = aidaSession.openSession();
			ICalendarDAO dao = getCalendarDAO(req);
			logger.debug("delete Calendar");
			dao.deleteCalendar(id, session);

		} catch (EMFUserError e) {
			logger.error("deleteCalendar error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} finally {

			if (session != null) {
				if (session.isOpen())
					session.close();
			}
			if (aidaSession != null) {
				aidaSession.close();
			}
		}

	}

	@POST
	@Path("{id}/deleteDayofCalendar")
	public void deleteDayofCalendar(@PathParam("id") Integer id, @Context HttpServletRequest req) {
		SessionFactory aidaSession = null;
		Session session = null;
		try {
			aidaSession = CalendarUtilities.getHibSessionAida();

			session = aidaSession.openSession();
			ICalendarDAO dao = getCalendarDAO(req);
			logger.debug("Delete Days of Calendar");
			dao.deleteDayofCalendar(id, session);

		} catch (EMFUserError e) {
			logger.error("deleteDayofCalendar error ", e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} finally {

			if (session != null) {
				if (session.isOpen())
					session.close();
			}
			if (aidaSession != null) {
				aidaSession.close();
			}
		}
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

}
