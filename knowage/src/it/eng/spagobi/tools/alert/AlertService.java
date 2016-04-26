package it.eng.spagobi.tools.alert;

import it.eng.spagobi.kpi.bo.AlertListener;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.serialization.JsonConverter;

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
	{
		mockListeners.clear();
		AlertListener l = new AlertListener();
		l.setId(1);
		l.setName("kpi listener");
		l.setClassName("it.eng.spagobi.kpi.alert.KpiListener");
		l.setTemplate("/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/alert/listeners/kpiListener.jsp");
		mockListeners.add(l);
	}

	@GET
	@Path("/listListener")
	public Response listListener() {
		// TODO call dao service
		List<AlertListener> listeners = mockListeners;
		return Response.ok(JsonConverter.objectToJson(listeners, listeners.getClass())).build();
	}

	@GET
	@Path("/{idListener}/loadTemplate")
	public Response loadTemplate(@PathParam("idListener") Integer idListener) {
		// TODO call dao service
		int i = mockListeners.indexOf(new AlertListener(idListener));
		return Response.ok(JsonConverter.objectToJson(mockListeners.get(i), AlertListener.class)).build();
	}

	@POST
	@Path("/save")
	public Response save() {
		// TODO
		return Response.ok().build();
	}

}
