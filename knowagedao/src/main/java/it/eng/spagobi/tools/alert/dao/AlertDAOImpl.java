package it.eng.spagobi.tools.alert.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.tools.alert.bo.Alert;
import it.eng.spagobi.tools.alert.bo.AlertAction;
import it.eng.spagobi.tools.alert.bo.AlertListener;
import it.eng.spagobi.tools.alert.job.AbstractSuspendableJob.JOB_STATUS;
import it.eng.spagobi.tools.alert.job.IAlertListener;
import it.eng.spagobi.tools.alert.metadata.SbiAlert;
import it.eng.spagobi.tools.alert.metadata.SbiAlertAction;
import it.eng.spagobi.tools.alert.metadata.SbiAlertListener;
import it.eng.spagobi.tools.alert.metadata.SbiAlertLog;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class AlertDAOImpl extends AbstractHibernateDAO implements IAlertDAO {

	private static Logger logger = Logger.getLogger(AlertDAOImpl.class);

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
//        alertListener.setTemplate(sbiAlertListener.getTemplate());
		return alertListener;
	}

	@Override
	public List<AlertAction> listAction() {
		List<SbiAlertAction> lst = list(SbiAlertAction.class);
		List<AlertAction> ret = new ArrayList<>();
		try {
			for (SbiAlertAction sbiAlertAction : lst) {
				AlertAction action = from(sbiAlertAction);
				try {
					Class cls = Class.forName(action.getClassName());
					if (getUserProfile().getFunctionalities().contains(cls.getSimpleName() + "Action")) {
						ret.add(action);
					}
				} catch (ClassNotFoundException e) {
					logger.info("Class [" + action.getClassName() + "] not loaded.");
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		}
		return ret;
	}

	private AlertAction from(SbiAlertAction sbiAlertAction) {
		AlertAction alertAction = new AlertAction();
		alertAction.setId(sbiAlertAction.getId());
		alertAction.setName(sbiAlertAction.getName());
		alertAction.setClassName(sbiAlertAction.getClassName());
//		alertAction.setTemplate(sbiAlertAction.getTemplate());
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
		} catch (ClassNotFoundException e) {
			throw new SpagoBIDAOException(e);
		}
		return id;
	}

	private SbiAlert from(Alert alert) {
		SbiAlert sbiAlert = new SbiAlert();
		sbiAlert.setId(alert.getId());
		sbiAlert.setName(alert.getName());
		sbiAlert.setSingleExecution(alert.isSingleExecution() ? 'T' : 'F');
		sbiAlert.setEventBeforeTriggerAction(alert.getEventBeforeTriggerAction());
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
			parameters.put(IAlertListener.LISTENER_ID, "" + alert.getAlertListener().getId());
			ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();
			schedulerDAO.createOrUpdateJobAndTrigger(name, Class.forName(alert.getAlertListener().getClassName()), ALERT_JOB_GROUP, ALERT_JOB_GROUP,
					alert.getFrequency(), parameters);
		} catch (ClassNotFoundException e) {
			throw new SpagoBIDAOException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Alert> listAlert() {
		final List<String> suspendedTriggers = DAOFactory.getSchedulerDAO().listTriggerPausedByGroup(ALERT_JOB_GROUP, ALERT_JOB_GROUP);
		return executeOnTransaction(new IExecuteOnTransaction<List<Alert>>() {
			@Override
			public List<Alert> execute(Session session) throws Exception {

				List<SbiAlert> alertList = session.createCriteria(SbiAlert.class).list();

				List<Alert> ret = new ArrayList<>();
				for (SbiAlert sbiAlert : alertList) {
					Alert alert = from(sbiAlert, false);
					if (DAOFactory.getSchedulerDAO().loadTrigger(ALERT_JOB_GROUP, alert.getId().toString()) == null) {
						// trigger expired
						alert.setJobStatus(JOB_STATUS.EXPIRED);
					} else {
						alert.setJobStatus(suspendedTriggers.contains("" + sbiAlert.getId()) ? JOB_STATUS.SUSPENDED : JOB_STATUS.ACTIVE);
					}
					ret.add(alert);
				}
				return ret;
			}
		});
	}

	private Alert from(SbiAlert sbiAlert, boolean checkStatus) {
		Alert alert = new Alert();
		alert.setId(sbiAlert.getId());
		alert.setName(sbiAlert.getName());
		alert.setEventBeforeTriggerAction(sbiAlert.getEventBeforeTriggerAction());
		alert.setSingleExecution(Character.valueOf('T').equals(sbiAlert.getSingleExecution()));
		alert.setJsonOptions(sbiAlert.getListenerOptions());
		alert.setAlertListener(from(sbiAlert.getSbiAlertListener()));
		ISchedulerDAO schedulerDao = DAOFactory.getSchedulerDAO();
		if (checkStatus) {
			String name = "" + sbiAlert.getId();
			try {
				// loading trigger
				ISchedulerDAO daoScheduler = DAOFactory.getSchedulerDAO();
				Trigger tr = daoScheduler.loadTrigger(ALERT_JOB_GROUP, name);
				if (tr == null) {
					// Calendar now = GregorianCalendar.getInstance(); // creates a new calendar instance
					alert.getFrequency().setStartTime("00:00");
					alert.getFrequency().setCron(null);
					alert.setJobStatus(JOB_STATUS.EXPIRED);
				} else {
					alert.setJobStatus(schedulerDao.isTriggerPaused(ALERT_JOB_GROUP, name, ALERT_JOB_GROUP, name) ? JOB_STATUS.SUSPENDED : JOB_STATUS.ACTIVE);
					Date startTime = tr.getStartTime();
					Calendar dateStartFreq = GregorianCalendar.getInstance(); // creates a new calendar instance
					dateStartFreq.setTime(startTime); // assigns calendar to given date
					alert.getFrequency().setStartTime(dateStartFreq.get(Calendar.HOUR_OF_DAY) + ":" + dateStartFreq.get(Calendar.MINUTE));
					alert.getFrequency().setStartDate(dateStartFreq.getTime().getTime());
					if (tr.getEndTime() != null) {
						Date endTime = tr.getEndTime();
						Calendar dateEndFreq = GregorianCalendar.getInstance(); // creates a new calendar instance
						dateEndFreq.setTime(endTime); // assigns calendar to given date
						alert.getFrequency().setEndTime(dateEndFreq.get(Calendar.HOUR_OF_DAY) + ":" + dateEndFreq.get(Calendar.MINUTE));
						alert.getFrequency().setEndDate(dateEndFreq.getTime().getTime());
					}
					alert.getFrequency().setCron(tr.getChronExpression() != null ? tr.getChronExpression().getExpression().replace("'", "\"") : null);
				}
			} catch (Throwable e) {
				throw new SpagoBIDAOException(e);
			}
		}
		return alert;
	}

	@Override
	public Alert loadAlert(Integer id) {
		SbiAlert sbiAlert = load(SbiAlert.class, id);
		Alert alert = from(sbiAlert, true);
		return alert;
	}

	@Override
	public void remove(Integer id) {
		ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();
		schedulerDAO.deleteJob("" + id, ALERT_JOB_GROUP);

		delete(SbiAlert.class, id);
	}

	@Override
	public Integer insertAlertLog(SbiAlertLog alertLog) {
		return (Integer) insert(alertLog);
	}

	@Override
	public void suspendAlert(Integer id) throws EMFUserError {
		DAOFactory.getSchedulerDAO().pauseTrigger(ALERT_JOB_GROUP, "" + id, ALERT_JOB_GROUP, "" + id);
	}

}
