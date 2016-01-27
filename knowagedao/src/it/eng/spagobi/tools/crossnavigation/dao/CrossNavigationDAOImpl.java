package it.eng.spagobi.tools.crossnavigation.dao;

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.tools.crossnavigation.bo.NavigationDetail;
import it.eng.spagobi.tools.crossnavigation.bo.SimpleNavigation;
import it.eng.spagobi.tools.crossnavigation.bo.SimpleParameter;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigation;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigationPar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONException;

public class CrossNavigationDAOImpl extends AbstractHibernateDAO implements ICrossNavigationDAO {

	@Override
	public List<SimpleNavigation> listNavigation() {
		final List<SimpleNavigation> lst = new ArrayList<>();
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			/* .sbiCrossNavigation.id, p.sbiCrossNavigation.name, p.fromKey.sbiObject.biobjId, p.toKey.sbiObject.biobjId */
			@Override
			public Boolean execute(Session session) throws JSONException {
				String hql = "select p ";
				hql += " from it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigationPar p ";
				hql += " group by p.sbiCrossNavigation.id, p.sbiCrossNavigation.name, p.fromKey.sbiObject.biobjId, p.toKey.sbiObject.biobjId ";
				hql += " order by p.sbiCrossNavigation.name ";
				Query q = session.createQuery(hql);
				/*
				 * Criteria q = session.createCriteria(SbiCrossNavigationPar.class).createAlias("sbiCrossNavigation", "sbiCrossNavigation")
				 * .addOrder(Order.asc("sbiCrossNavigation.name"));
				 */
				for (Object o : q.list()) {
					SbiCrossNavigationPar cn = (SbiCrossNavigationPar) o;
					String fromDoc = cn.getFromKey().getSbiObject().getLabel();
					String toDoc = cn.getToKey().getSbiObject().getLabel();
					lst.add(new SimpleNavigation(cn.getSbiCrossNavigation().getId(), cn.getSbiCrossNavigation().getName(), fromDoc, toDoc));
				}
				return Boolean.TRUE;
			}

		});

		return lst;
	}

	@Override
	public void insert(final NavigationDetail nd) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws JSONException {
				SbiCrossNavigation cn = new SbiCrossNavigation();
				cn.setName(nd.getSimpleNavigation().getName());
				cn.setSbiCrossNavigationPars(new HashSet<SbiCrossNavigationPar>());
				if (nd.getToPars() != null) {
					for (SimpleParameter sp : nd.getToPars()) {
						if (sp.getLinks() != null && !sp.getLinks().isEmpty()) {
							SbiCrossNavigationPar cnp = new SbiCrossNavigationPar();
							cnp.setFromKey(new SbiObjPar(sp.getLinks().get(0).getId()));
							cnp.setToKey(new SbiObjPar(sp.getId()));
							updateSbiCommonInfo4Insert(cnp);
							cnp.setSbiCrossNavigation(cn);
							cn.getSbiCrossNavigationPars().add(cnp);
						}
					}
				}
				updateSbiCommonInfo4Insert(cn);
				session.save(cn);
				return Boolean.TRUE;
			}
		});
	}

	@Override
	public void update(final NavigationDetail nd) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws JSONException {
				SbiCrossNavigation cn = (SbiCrossNavigation) session.get(SbiCrossNavigation.class, nd.getSimpleNavigation().getId());
				if (cn == null) {
					throw new SpagoBIDOAException("Write error: record not found");
				}
				cn.setName(nd.getSimpleNavigation().getName());
				if (cn.getSbiCrossNavigationPars() != null) {
					cn.getSbiCrossNavigationPars().clear();
				} else {
					cn.setSbiCrossNavigationPars(new HashSet<SbiCrossNavigationPar>());
				}
				if (nd.getToPars() != null) {
					for (SimpleParameter sp : nd.getToPars()) {
						if (sp.getLinks() != null && !sp.getLinks().isEmpty()) {
							SbiCrossNavigationPar cnp = new SbiCrossNavigationPar();
							cnp.setFromKey(new SbiObjPar(sp.getLinks().get(0).getId()));
							cnp.setToKey(new SbiObjPar(sp.getId()));
							cnp.setSbiCrossNavigation(cn);
							updateSbiCommonInfo4Insert(cnp);
							cn.getSbiCrossNavigationPars().add(cnp);
						}
					}
				}
				updateSbiCommonInfo4Update(cn);
				session.update(cn);
				return Boolean.TRUE;
			}
		});
	}

	@Override
	public NavigationDetail loadNavigation(final Integer id) {
		return executeOnTransaction(new IExecuteOnTransaction<NavigationDetail>() {
			@Override
			public NavigationDetail execute(Session session) throws JSONException {
				SbiCrossNavigation cn = (SbiCrossNavigation) session.get(SbiCrossNavigation.class, id);
				if (cn == null)
					throw new SpagoBIDOAException("Record not found in SbiCrossNavigation with id[" + id + "]");
				NavigationDetail nd = new NavigationDetail();
				if (cn.getSbiCrossNavigationPars() != null && !cn.getSbiCrossNavigationPars().isEmpty()) {
					SbiObjects fromDoc = cn.getSbiCrossNavigationPars().iterator().next().getFromKey().getSbiObject();
					SbiObjects toDoc = cn.getSbiCrossNavigationPars().iterator().next().getToKey().getSbiObject();

					nd.setSimpleNavigation(new SimpleNavigation(cn.getId(), cn.getName(), fromDoc.getLabel(), toDoc.getLabel()));

					for (Object o : fromDoc.getSbiObjPars()) {
						SbiObjPar op = (SbiObjPar) o;
						nd.getFromPars().add(new SimpleParameter(op.getObjParId(), op.getLabel()));
					}

					for (Object o : toDoc.getSbiObjPars()) {
						SbiObjPar op = (SbiObjPar) o;
						nd.getToPars().add(new SimpleParameter(op.getObjParId(), op.getLabel()));
					}

					for (SbiCrossNavigationPar cnp : cn.getSbiCrossNavigationPars()) {
						SimpleParameter fromSp = new SimpleParameter(cnp.getFromKey().getObjParId(), cnp.getFromKey().getLabel());
						SimpleParameter toSp = new SimpleParameter(cnp.getToKey().getObjParId(), cnp.getToKey().getLabel());
						int i = nd.getToPars().indexOf(toSp);
						if (i < 0) {
							nd.getToPars().add(fromSp);
						} else {
							nd.getToPars().get(i).getLinks().clear();
							nd.getToPars().get(i).getLinks().add(fromSp);
						}
					}
				}
				return nd;
			}
		});
	}

	@Override
	public void delete(Integer id) {
		delete(SbiCrossNavigation.class, id);
	}
}
