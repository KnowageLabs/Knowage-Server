package it.eng.spagobi.tools.alert.action;

import it.eng.spagobi.utilities.exceptions.SpagoBIException;

import java.util.Map;

public interface IAlertAction {

	public String ACTION_PARAMS = "ACTION_PARAMS";

	public void execute(String jsonPageField, Map<String, String> parameterMapFromListener) throws SpagoBIException;

}
