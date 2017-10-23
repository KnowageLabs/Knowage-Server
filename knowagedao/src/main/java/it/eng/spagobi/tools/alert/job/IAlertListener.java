package it.eng.spagobi.tools.alert.job;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.alert.exception.AlertListenerException;

import java.util.List;

public interface IAlertListener {

	public String LISTENER_PARAMS = "LISTENER_PARAMS";
	public String LISTENER_ID = "LISTENER_ID";

	public void executeListener(String jsonOptions) throws AlertListenerException;

	public List<SbiHibernateModel> export(String jsonParameters);
}
