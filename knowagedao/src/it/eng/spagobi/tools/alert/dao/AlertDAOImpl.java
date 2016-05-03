package it.eng.spagobi.tools.alert.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.tools.alert.bo.Alert;
import it.eng.spagobi.tools.alert.bo.Alert.JOB_STATUS;
import it.eng.spagobi.tools.alert.bo.AlertAction;
import it.eng.spagobi.tools.alert.bo.AlertListener;
import it.eng.spagobi.tools.alert.listener.IAlertListener;
import it.eng.spagobi.tools.alert.metadata.SbiAlert;
import it.eng.spagobi.tools.alert.metadata.SbiAlertAction;
import it.eng.spagobi.tools.alert.metadata.SbiAlertListener;
import it.eng.spagobi.tools.alert.metadata.SbiAlertLog;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.metadata.SbiTriggerPaused;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

public class AlertDAOImpl extends AbstractHibernateDAO implements IAlertDAO {

	private static final String ALERT_JOB_GROUP = "ALERT_JOB_GROUP";

	@Override
	public List<AlertListener> listListener() {
		List<SbiAlertListener> lst = list(SbiAlertListener.class);
		List<AlertListener> ret = new ArrayList<>();
		for (SbiAlertListener sbiAlertListener : lst) {
			ret.add(from(sbiAlertListener));
		}
		return ret;
	}

	private AlertListener from(SbiAlertListener sbiAlertListener) {
		AlertListener alertListener = new AlertListener();
		alertListener.setId(sbiAlertListener.getId());
		alertListener.setName(sbiAlertListener.getName());
		alertListener.setClassName(sbiAlertListener.getClassName());
		alertListener.setTemplate(sbiAlertListener.getTemplate());
		return alertListener;
	}

	@Override
	public List<AlertAction> listAction() {
		List<SbiAlertAction> lst = list(SbiAlertAction.class);
		List<AlertAction> ret = new ArrayList<>();
		for (SbiAlertAction sbiAlertAction : lst) {
			ret.add(from(sbiAlertAction));
		}
		return ret;
	}

	private AlertAction from(SbiAlertAction sbiAlertAction) {
		AlertAction alertAction = new AlertAction();
		alertAction.setId(sbiAlertAction.getId());
		alertAction.setName(sbiAlertAction.getName());
		alertAction.setClassName(sbiAlertAction.getClassName());
		alertAction.setTemplate(sbiAlertAction.getTemplate());
		return alertAction;
	}

	@Override
	public AlertListener loadListener(Integer idListener) {
		return from(load(SbiAlertListener.class, idListener));
	}

	@Override
	public AlertAction loadAction(Integer idAction) {
		return from(load(SbiAlertAction.class, idAction));
	}

	@Override
	public Integer insert(Alert alert) {
		Integer id = (Integer) insert(from(alert));
		try {
			String name = "" + id;
			Map<String, String> parameters = new HashMap<>();
			parameters.put(IAlertListener.LISTENER_PARAMS, name);
			parameters.put(IAlertListener.LISTENER_ID, "" + alert.getAlertListener().getId());
			ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();
			schedulerDAO.createOrUpdateJobAndTrigger(name, Class.forName(alert.getAlertListener().getClassName()), ALERT_JOB_GROUP, ALERT_JOB_GROUP,
					alert.getFrequency(), parameters);
		} catch (EMFUserError | ClassNotFoundException e) {
			throw new SpagoBIDOAException(e);
		}
		return id;
	}

	private SbiAlert from(Alert alert) {
		SbiAlert sbiAlert = new SbiAlert();
		sbiAlert.setId(alert.getId());
		sbiAlert.setName(alert.getName());
		sbiAlert.setListenerId(alert.getAlertListener().getId());
		sbiAlert.setListenerOptions(alert.getJsonOptions());
		return sbiAlert;
	}

	@Override
	public void update(Alert alert) {
		update(from(alert));
		try {
			String name = "" + alert.getId();
			Map<String, String> parameters = new HashMap<>();
			parameters.put(IAlertListener.LISTENER_PARAMS, name);
			ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();
			schedulerDAO.createOrUpdateJobAndTrigger(name, Class.forName(alert.getAlertListener().getClassName()), ALERT_JOB_GROUP, ALERT_JOB_GROUP,
					alert.getFrequency(), parameters);
		} catch (EMFUserError | ClassNotFoundException e) {
			throw new SpagoBIDOAException(e);
		}
	}

	@Override
	public List<Alert> listAlert() {
		return executeOnTransaction(new IExecuteOnTransaction<List<Alert>>() {
			@Override
			public List<Alert> execute(Session session) throws Exception {
				List<String> suspendedTriggers = session.createCriteria(SbiTriggerPaused.class).add(Restrictions.eq("triggerGroup", ALERT_JOB_GROUP))
						.add(Restrictions.eq("jobGroup", ALERT_JOB_GROUP)).setProjection(Property.forName("triggerName")).list();

				List<SbiAlert> alertList = session.createCriteria(SbiAlert.class).list();

				List<Alert> ret = new ArrayList<>();
				for (SbiAlert sbiAlert : alertList) {
					JOB_STATUS status = JOB_STATUS.ACTIVE;
					for (String triggerName : suspendedTriggers) {
						if ((sbiAlert.getId() + "").equals(triggerName)) {
							status = JOB_STATUS.SUSPENDED;
							break;
						}
					}
					ret.add(from(sbiAlert, status));
				}
				return ret;
			}
		});
	}

	private Alert from(SbiAlert sbiAlert, JOB_STATUS jobStatus) {
		Alert alert = new Alert();
		alert.setId(sbiAlert.getId());
		alert.setName(sbiAlert.getName());
		alert.setJsonOptions(sbiAlert.getListenerOptions());
		alert.setAlertListener(from(sbiAlert.getSbiAlertListener()));
		alert.setJobStatus(jobStatus);
		return alert;
	}

	@Override
	public Alert loadAlert(Integer id) {
		SbiAlert sbiAlert = load(SbiAlert.class, id);
		ISchedulerDAO schedulerDao;
		try {
			schedulerDao = DAOFactory.getSchedulerDAO();
		} catch (EMFUserError e) {
			throw new SpagoBIDOAException(e);
		}
		// TODO set correct values
		String triggerGroup = "";
		String jobName = "";
		String triggerName = "";
		String jobGroup = "";
		if (schedulerDao.isTriggerPaused(triggerGroup, triggerName, jobGroup, jobName)) {
			return from(sbiAlert, JOB_STATUS.SUSPENDED);
		} else {
			return from(sbiAlert, JOB_STATUS.ACTIVE);
		}
	}

	@Override
	public void remove(Integer id) {
		try {
			ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();
			schedulerDAO.deleteJob("" + id, ALERT_JOB_GROUP);
		} catch (EMFUserError e) {
			throw new SpagoBIDOAException(e);
		}
		delete(SbiAlert.class, id);
	}

	@Override
	public void insertAlertLog(SbiAlertLog alertLog) {
		insert(alertLog);
	}

}
