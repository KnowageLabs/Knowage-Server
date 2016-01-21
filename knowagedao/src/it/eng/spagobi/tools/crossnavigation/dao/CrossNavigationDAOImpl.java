package it.eng.spagobi.tools.crossnavigation.dao;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.tools.crossnavigation.bo.NavigationDetail;
import it.eng.spagobi.tools.crossnavigation.bo.SimpleNavigation;
import it.eng.spagobi.tools.crossnavigation.bo.SimpleParameter;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigation;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigationPar;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.json.JSONException;

public class CrossNavigationDAOImpl extends AbstractHibernateDAO implements ICrossNavigationDAO {

	@Override
	public List<SimpleNavigation> listNavigation() {
		final List<SimpleNavigation> lst = new ArrayList<>();
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {

			@Override
			public Boolean execute(Session session) throws JSONException {
				Criteria c = session.createCriteria(SbiCrossNavigation.class);
				c.addOrder(Order.asc("name"));
				for (Object o : c.list()) {
					SbiCrossNavigation cn = (SbiCrossNavigation) o;
					lst.add(toSimpleNavigation(cn));
				}
				return Boolean.TRUE;
			}

		});

		return lst;
	}

	@Override
	public void save(NavigationDetail nd) {
		/*
		 * SbiCrossNavigation cn = load(nd.get); if (nd.isNewRecord()) { insert(cn); } else { update(cn); }
		 */
	}

	@Override
	public NavigationDetail loadNavigation(final Integer id) {
		return executeOnTransaction(new IExecuteOnTransaction<NavigationDetail>() {
			@Override
			public NavigationDetail execute(Session session) throws JSONException {
				SbiCrossNavigation cn = (SbiCrossNavigation) session.load(SbiCrossNavigation.class, id);
				NavigationDetail nd = new NavigationDetail();
				nd.setSimpleNavigation(toSimpleNavigation(cn));
				if (cn.getSbiCrossNavigationPars() != null && !cn.getSbiCrossNavigationPars().isEmpty()) {
					// nd.setFromDoc(cn.getSbiCrossNavigationPars().iterator().next().getFromKey().getSbiObject().getLabel());
					// nd.setToDoc(cn.getSbiCrossNavigationPars().iterator().next().getToKey().getSbiObject().getLabel());
					for (SbiCrossNavigationPar cnp : cn.getSbiCrossNavigationPars()) {
						nd.getFromPars().add(new SimpleParameter(cnp.getFromKey().getLabel(), "input"));
						nd.getToPars().add(new SimpleParameter(cnp.getToKey().getLabel(), "input"));
					}
				}
				return nd;
			}
		});
	}

	private SimpleNavigation toSimpleNavigation(SbiCrossNavigation cn) {
		SbiCrossNavigationPar cnp = cn.getSbiCrossNavigationPars().iterator().next();
		return new SimpleNavigation(cn.getId(), cn.getName(), cnp.getFromKey().getSbiObject().getLabel(), cnp.getToKey().getSbiObject().getLabel());
	}

}
