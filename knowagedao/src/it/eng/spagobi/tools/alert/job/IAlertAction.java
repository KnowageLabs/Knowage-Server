package it.eng.spagobi.tools.alert.job;

import it.eng.spagobi.tools.alert.exception.AlertActionException;

import java.util.Map;

public interface IAlertAction {

	public String ACTION_PARAMS = "ACTION_PARAMS";

	public void executeAction(String jsonPageField, Map<String, String> parameterMapFromListener) throws AlertActionException;

}
