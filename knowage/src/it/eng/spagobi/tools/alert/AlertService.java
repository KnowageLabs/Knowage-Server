package it.eng.spagobi.tools.alert;

import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.alert.bo.AlertAction;
import it.eng.spagobi.tools.alert.bo.AlertListener;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * @authors Salvatore Lupo (Salvatore.Lupo@eng.it)
 * 
 */
@Path("/1.0/alert")
@ManageAuthorization
public class AlertService {

	static final List<AlertListener> mockListeners = new ArrayList<>();
	static final List<AlertAction> mockActions = new ArrayList<>();
	{
		mockListeners.clear();
		AlertListener l = new AlertListener();
		l.setId(1);
		l.setName("KPI Listener");
		l.setClassName("it.eng.spagobi.tools.alert.listener.KpiListener");
		l.setTemplate("/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/alert/listeners/kpiListener.jsp");
		mockListeners.add(l);

		mockActions.clear();
		AlertAction a = new AlertAction();
		a.setName("Send mail");
		a.setClassName("it.eng.spagobi.tools.alert.action.SendMail");
		a.setTemplate("/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/alert/actions/sendMail.jsp");
		mockActions.add(a);
	}

	@GET
	@Path("/listListener")
	public Response listListener() {
		// TODO call dao service
		List<AlertListener> listeners = mockListeners;
		return Response.ok(JsonConverter.objectToJson(listeners, listeners.getClass())).build();
	}

	@GET
	@Path("/listAction")
	public Response listAction() {
		// TODO call dao service
		List<AlertAction> actions = mockActions;
		return Response.ok(JsonConverter.objectToJson(actions, actions.getClass())).build();
	}

	@GET
	@Path("/{idListener}/loadListenerTemplate")
	public Response loadListenerTemplate(@PathParam("idListener") Integer idListener) {
		// TODO call dao service
		int i = mockListeners.indexOf(new AlertListener(idListener));
		return Response.ok(JsonConverter.objectToJson(mockListeners.get(i), AlertListener.class)).build();
	}

	@GET
	@Path("/{idAction}/loadActionTemplate")
	public Response loadActionTemplate(@PathParam("idListener") Integer idListener) {
		// TODO call dao service
		int i = mockActions.indexOf(new AlertAction(idListener));
		return Response.ok(JsonConverter.objectToJson(mockListeners.get(i), AlertListener.class)).build();
	}

	@POST
	@Path("/save")
	public Response save() {
		// TODO
		return Response.ok().build();
	}

}
