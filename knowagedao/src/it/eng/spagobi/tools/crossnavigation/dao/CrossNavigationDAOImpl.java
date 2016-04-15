/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.tools.crossnavigation.dao;

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.tools.crossnavigation.bo.NavigationDetail;
import it.eng.spagobi.tools.crossnavigation.bo.SimpleNavigation;
import it.eng.spagobi.tools.crossnavigation.bo.SimpleParameter;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigation;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigationPar;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiOutputParameter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;

public class CrossNavigationDAOImpl extends AbstractHibernateDAO implements ICrossNavigationDAO {

	public static final int TYPE_FIXED = 2;
	public static final int TYPE_INPUT = 1;
	public static final int TYPE_OUTPUT = 0;

	@Override
	public List<SimpleNavigation> listNavigation() {
		final List<SimpleNavigation> lst = new ArrayList<>();
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws JSONException {
				Criteria c = session.createCriteria(SbiCrossNavigation.class).addOrder(Order.asc("name"));

				for (Object o : c.list()) {
					SbiCrossNavigation cn = (SbiCrossNavigation) o;
					SimpleNavigation sn = new SimpleNavigation();
					sn.setId(cn.getId());
					sn.setName(cn.getName());
					if (cn.getSbiCrossNavigationPars() != null && !cn.getSbiCrossNavigationPars().isEmpty()) {
						SbiCrossNavigationPar cnp = cn.getSbiCrossNavigationPars().iterator().next();
						sn.setToDoc(cnp.getToKey().getLabel());
						switch (cnp.getFromType()) {
						case TYPE_INPUT:
							SbiObjPar op = (SbiObjPar) session.get(SbiObjPar.class, cnp.getFromKeyId());
							sn.setFromDoc(op.getSbiObject().getLabel());
							break;
						case TYPE_OUTPUT:
							SbiOutputParameter o2 = (SbiOutputParameter) session.get(SbiOutputParameter.class, cnp.getFromKeyId());
							sn.setFromDoc(o2.getSbiObject().getLabel());
							break;
						case TYPE_FIXED:
							SbiObjects obj = (SbiObjects) session.load(SbiObjects.class, cnp.getFromKeyId());
							sn.setFromDoc(obj.getLabel());
							break;
						}
					}
					lst.add(sn);
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
				if (nd.getSimpleNavigation().getId() != null) {
					throw new SpagoBIDOAException("Write error: record not valid");
				}
				SbiCrossNavigation cn = new SbiCrossNavigation();
				cn.setName(nd.getSimpleNavigation().getName());
				cn.setSbiCrossNavigationPars(new HashSet<SbiCrossNavigationPar>());
				if (nd.getToPars() != null) {
					for (SimpleParameter sp : nd.getToPars()) {
						if (sp.getLinks() != null && !sp.getLinks().isEmpty()) {
							SbiCrossNavigationPar cnp = from(sp, cn);
							updateSbiCommonInfo4Insert(cnp);
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
							SbiCrossNavigationPar cnp = from(sp, cn);
							updateSbiCommonInfo4Update(cnp);
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
				if (cn == null) {
					throw new SpagoBIDOAException("Object of type SbiCrossNavigation with id[" + id + "] not found");
				}
				NavigationDetail nd = new NavigationDetail();
				if (cn.getSbiCrossNavigationPars() != null && !cn.getSbiCrossNavigationPars().isEmpty()) {
					// All parameters belong to two docs only
					SbiObjects fromDoc = null;
					SbiObjects toDoc = null;
					for (SbiCrossNavigationPar cnp : cn.getSbiCrossNavigationPars()) {
						SimpleParameter fromSp = new SimpleParameter();
						fromSp.setId(cnp.getFromKeyId());
						fromSp.setType(cnp.getFromType());
						switch (cnp.getFromType().intValue()) {
						case TYPE_INPUT:
							SbiObjPar inputParameter = (SbiObjPar) session.get(SbiObjPar.class, cnp.getFromKeyId());
							fromSp.setName(inputParameter.getLabel());
							if (fromDoc == null) {
								fromDoc = inputParameter.getSbiObject();
							}
							break;
						case TYPE_OUTPUT:
							SbiOutputParameter outputParameter = (SbiOutputParameter) session.get(SbiOutputParameter.class, cnp.getFromKeyId());
							fromSp.setName(outputParameter.getLabel());
							if (fromDoc == null) {
								fromDoc = outputParameter.getSbiObject();
							}
							break;
						case TYPE_FIXED:
							fromSp.setFixedValue(cnp.getFixedValue());
							fromSp.setName(cnp.getFixedValue());
							if (fromDoc == null) {
								fromDoc = (SbiObjects) session.get(SbiObjects.class, cnp.getFromKeyId());
							}
							break;
						}
						SimpleParameter toSp = new SimpleParameter(cnp.getToKey().getObjParId(), cnp.getToKey().getLabel(), TYPE_INPUT);
						if (toDoc == null) {
							toDoc = cnp.getToKey().getSbiObject();
						}
						for (Object o : fromDoc.getSbiObjPars()) {
							SbiObjPar op = (SbiObjPar) o;
							nd.getFromPars().add(new SimpleParameter(op.getObjParId(), op.getLabel(), TYPE_INPUT));
						}
						List outputParameterList = session.createCriteria(SbiOutputParameter.class).add(Restrictions.eq("biobjId", fromDoc.getBiobjId()))
								.list();
						for (Object object : outputParameterList) {
							SbiOutputParameter outPar = (SbiOutputParameter) object;
							nd.getFromPars().add(new SimpleParameter(outPar.getId(), outPar.getLabel(), TYPE_OUTPUT));
						}
						for (Object o : toDoc.getSbiObjPars()) {
							SbiObjPar op = (SbiObjPar) o;
							nd.getToPars().add(new SimpleParameter(op.getObjParId(), op.getLabel(), TYPE_INPUT));
						}

						int i = nd.getToPars().indexOf(toSp);
						if (i < 0) {
							nd.getToPars().add(fromSp);
						} else {
							nd.getToPars().get(i).getLinks().clear();
							nd.getToPars().get(i).getLinks().add(fromSp);
						}
					}

					nd.setSimpleNavigation(new SimpleNavigation(cn.getId(), cn.getName(), fromDoc.getLabel(), fromDoc.getBiobjId(), toDoc.getLabel()));

				}
				return nd;
			}
		});
	}

	@Override
	public void delete(Integer id) {
		delete(SbiCrossNavigation.class, id);
	}

	@Override
	public Object test(Object... objects) {
		return list(new ICriterion<SbiCrossNavigationPar>() {
			@Override
			public Criteria evaluate(Session session) {

				return null;
			}
		});
	}

	private SbiCrossNavigationPar from(SimpleParameter sp, SbiCrossNavigation cn) {
		SbiCrossNavigationPar cnp = new SbiCrossNavigationPar();
		SimpleParameter linkedParam = sp.getLinks().get(0);
		cnp.setFromKeyId(linkedParam.getId());
		cnp.setFromType(linkedParam.getType());
		cnp.setToKeyId(sp.getId());
		if (linkedParam.getType().equals(TYPE_FIXED)) {
			cnp.setFixedValue(linkedParam.getFixedValue());
		}
		cnp.setSbiCrossNavigation(cn);
		return cnp;
	}
}
