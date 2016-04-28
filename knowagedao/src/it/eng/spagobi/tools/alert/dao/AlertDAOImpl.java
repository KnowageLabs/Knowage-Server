package it.eng.spagobi.tools.alert.dao;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.tools.alert.bo.Alert;
import it.eng.spagobi.tools.alert.bo.AlertAction;
import it.eng.spagobi.tools.alert.bo.AlertListener;
import it.eng.spagobi.tools.alert.metadata.SbiAlertAction;
import it.eng.spagobi.tools.alert.metadata.SbiAlertListener;

import java.util.ArrayList;
import java.util.List;

public class AlertDAOImpl extends AbstractHibernateDAO implements IAlertDAO {

	static final List<Alert> mockAlerts = new ArrayList<>();

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
		int max = 0;
		for (Alert al : mockAlerts) {
			if (al.getId() > max)
				max = al.getId();
		}
		Integer id = max + 1;
		alert.setId(id);
		mockAlerts.add(alert);
		return id;
	}

	@Override
	public void update(Alert alert) {
		int i = mockAlerts.indexOf(alert);
		mockAlerts.remove(i);
		mockAlerts.add(i, alert);
	}

	@Override
	public List<Alert> listAlert() {
		return mockAlerts;
	}

	@Override
	public Alert loadAlert(Integer id) {
		int i = mockAlerts.indexOf(new Alert(id));
		return mockAlerts.get(i);
	}

	@Override
	public void remove(Integer id) {
		int i = mockAlerts.indexOf(new Alert(id));
		mockAlerts.remove(i);
	}

}
