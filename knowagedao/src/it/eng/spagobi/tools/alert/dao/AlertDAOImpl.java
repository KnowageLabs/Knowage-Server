package it.eng.spagobi.tools.alert.dao;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.tools.alert.bo.Alert;
import it.eng.spagobi.tools.alert.bo.AlertAction;
import it.eng.spagobi.tools.alert.bo.AlertListener;

import java.util.ArrayList;
import java.util.List;

public class AlertDAOImpl extends AbstractHibernateDAO implements IAlertDAO {

	static final List<AlertListener> mockListeners = new ArrayList<>();
	static final List<AlertAction> mockActions = new ArrayList<>();
	static final List<Alert> mockAlerts = new ArrayList<>();
	{
		mockListeners.clear();
		AlertListener l = new AlertListener();
		l.setId(1);
		l.setName("KPI Listener");
		l.setClassName("it.eng.spagobi.tools.alert.listener.KpiListener");
		l.setTemplate("/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/alert/listeners/kpiListener.jsp");
		mockListeners.add(l);

		mockActions.clear();
		AlertAction a = new AlertAction();
		a.setName("Send mail");
		a.setClassName("it.eng.spagobi.tools.alert.action.SendMail");
		a.setTemplate("/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/alert/actions/sendMail.jsp");
		mockActions.add(a);
	}

	@Override
	public List<AlertListener> listListener() {
		return mockListeners;
	}

	@Override
	public List<AlertAction> listAction() {
		return mockActions;
	}

	@Override
	public AlertListener loadListener(Integer idListener) {
		int i = mockListeners.indexOf(new AlertListener(idListener));
		return mockListeners.get(i);
	}

	@Override
	public AlertAction loadAction(Integer idAction) {
		int i = mockActions.indexOf(new AlertAction(idAction));
		return mockActions.get(i);
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
