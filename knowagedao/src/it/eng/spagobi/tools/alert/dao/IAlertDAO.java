package it.eng.spagobi.tools.alert.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.alert.bo.Alert;
import it.eng.spagobi.tools.alert.bo.AlertAction;
import it.eng.spagobi.tools.alert.bo.AlertListener;
import it.eng.spagobi.tools.alert.metadata.SbiAlertLog;

import java.util.List;

public interface IAlertDAO extends ISpagoBIDao {

	public static final String ALERT_JOB_GROUP = "ALERT_JOB_GROUP";

	public List<AlertListener> listListener();

	public List<AlertAction> listAction();

	public AlertListener loadListener(Integer id);

	public AlertAction loadAction(Integer id);

	public Integer insert(Alert alert);

	public void update(Alert alert);

	public List<Alert> listAlert();

	public Alert loadAlert(Integer id);

	public void remove(Integer id);

	public Integer insertAlertLog(SbiAlertLog alertLog);

	public void suspendAlert(Integer id) throws EMFUserError;
}
