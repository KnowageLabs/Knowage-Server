package it.eng.spagobi.tools.alert;

import static it.eng.spagobi.tools.scheduler.utils.SchedulerUtilitiesV2.getJobTriggerInfo;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.kpi.utils.JSError;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.alert.bo.Alert;
import it.eng.spagobi.tools.alert.bo.AlertAction;
import it.eng.spagobi.tools.alert.bo.AlertListener;
import it.eng.spagobi.tools.alert.dao.IAlertDAO;
import it.eng.spagobi.tools.scheduler.bo.Frequency;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.bo.TriggerPaused;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.to.JobTrigger;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Salvatore Lupo (Salvatore.Lupo@eng.it)
 * 
 */
@Path("/1.0/alert")
@ManageAuthorization
public class AlertService {

	private static Logger logger = Logger.getLogger(AlertService.class);
	private static final String ALERT_JOB_GROUP = "ALERT_JOB_GROUP";

	@GET
	@Path("/{id}/pause")
	public Response pause(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		// TODO fake code: this has to be developed
		ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();
		IAlertDAO dao = getDao(req);
		Alert alert = dao.loadAlert(id);
		String name = "" + alert.getId();
		Trigger trigger = schedulerDAO.loadTrigger(ALERT_JOB_GROUP, name);
		if (!schedulerDAO.isTriggerPaused(ALERT_JOB_GROUP, name, ALERT_JOB_GROUP, name)) {
			TriggerPaused triggerPaused = new TriggerPaused();
			triggerPaused.setId(id);
			schedulerDAO.pauseTrigger(triggerPaused);
		}
		return Response.ok().build();
	}

	@GET
	@Path("/listListener")
	public Response listListener(@Context HttpServletRequest req) throws EMFUserError {
		IAlertDAO dao = getDao(req);
		List<AlertListener> listeners = dao.listListener();
		return Response.ok(JsonConverter.objectToJson(listeners, listeners.getClass())).build();
	}

	@GET
	@Path("/listAction")
	public Response listAction(@Context HttpServletRequest req) throws EMFUserError {
		IAlertDAO dao = getDao(req);
		List<AlertAction> actions = dao.listAction();
		return Response.ok(JsonConverter.objectToJson(actions, actions.getClass())).build();
	}

	@GET
	@Path("/listAlert")
	public Response listAlert(@Context HttpServletRequest req) throws EMFUserError {
		IAlertDAO dao = getDao(req);
		List<Alert> alert = dao.listAlert();
		return Response.ok(JsonConverter.objectToJson(alert, alert.getClass())).build();
	}

	@GET
	@Path("/{id}/load")
	public Response load(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IAlertDAO dao = getDao(req);
		Alert alert = dao.loadAlert(id);
		// loading trigger
		try {
			JobTrigger triggerInfo = getJobTriggerInfo("" + id, ALERT_JOB_GROUP, "" + id, ALERT_JOB_GROUP);
			Frequency frequency = new Frequency();
			frequency.setStartTime(triggerInfo.getStartTime());
			frequency.setEndTime(triggerInfo.getEndTime());
			frequency.setCron(triggerInfo.getChrono() != null ? new JSONObject(triggerInfo.getChrono()).toString() : null);
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			if (triggerInfo.getStartDate() != null) {
				frequency.setStartDate(df.parse(triggerInfo.getStartDate()).getTime());
			}
			if (triggerInfo.getEndDate() != null) {
				frequency.setEndDate(df.parse(triggerInfo.getEndDate()).getTime());
			}
			alert.setFrequency(frequency);
			return Response.ok(JsonConverter.objectToJson(alert, Alert.class)).build();
		} catch (Throwable e) {
			logger.error(req.getPathInfo(), e);
		}
		return Response.ok().build();
	}

	@POST
	@Path("/save")
	public Response save(@Context HttpServletRequest req) throws EMFUserError {
		try {
			String str = RestUtilities.readBody(req);
			Alert alert = (Alert) JsonConverter.jsonToObject(str, Alert.class);
			JSError jsError = new JSError();
			check(alert, jsError);
			if (!jsError.hasErrors()) {
				IAlertDAO dao = getDao(req);
				Integer id = alert.getId();
				if (id == null) {
					id = dao.insert(alert);
				} else {
					dao.update(alert);
				}
				return Response.ok(new JSONObject().put("id", id).toString()).build();
			} else {
				return Response.ok(jsError.toString()).build();
			}
		} catch (IOException | JSONException e) {
			logger.error(req.getPathInfo(), e);
		}
		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}/delete")
	public Response delete(@PathParam("id") Integer id, @Context HttpServletRequest req) throws EMFUserError {
		IAlertDAO dao = getDao(req);
		dao.remove(id);
		return Response.ok().build();
	}

	private void check(Alert alert, JSError jsError) {
		AlertListener alertListener = alert.getAlertListener();
		if (alertListener == null) {
			jsError.addError("Listener is mandatory");
		} else {
			if (alertListener.getClassName() == null) {
				jsError.addError("Listener error");
				logger.error("Listener name[" + alertListener.getName() + "] has null class");
			} else {
				try {
					Class.forName(alertListener.getClassName());
				} catch (ClassNotFoundException e) {
					logger.error("Listener name[" + alertListener.getName() + "] has not valid class", e);
					jsError.addError("Listener error");
				}
			}
		}

	}

	private IAlertDAO getDao(HttpServletRequest req) throws EMFUserError {
		IAlertDAO dao = DAOFactory.getAlertDAO();
		dao.setUserProfile((IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE));
		return dao;
	}

}
