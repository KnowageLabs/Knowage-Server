package it.eng.spagobi.tools.alert.listener;

public interface IAlertListener {

	public String LISTENER_PARAMS = "LISTENER_PARAMS";

	public void execute(String jsonOptions);

}
