package it.eng.spagobi.tools.alert.action;

public interface IAlertAction {

	public String ACTION_PARAMS = "ACTION_PARAMS";

	public void execute(String jsonOptions);

}
