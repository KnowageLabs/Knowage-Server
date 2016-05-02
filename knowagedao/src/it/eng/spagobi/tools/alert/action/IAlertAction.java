package it.eng.spagobi.tools.alert.action;

import it.eng.spagobi.utilities.exceptions.SpagoBIException;

public interface IAlertAction {

	public String ACTION_PARAMS = "ACTION_PARAMS";

	public void execute(String jsonOptions) throws SpagoBIException;

}
