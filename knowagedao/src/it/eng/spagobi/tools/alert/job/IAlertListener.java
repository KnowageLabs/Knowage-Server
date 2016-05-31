package it.eng.spagobi.tools.alert.job;

import it.eng.spagobi.tools.alert.exception.AlertListenerException;

public interface IAlertListener {

	public String LISTENER_PARAMS = "LISTENER_PARAMS";
	public String LISTENER_ID = "LISTENER_ID";

	public void executeListener(String jsonOptions) throws AlertListenerException;

}
