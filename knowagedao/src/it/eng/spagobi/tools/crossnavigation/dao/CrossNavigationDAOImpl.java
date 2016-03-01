package it.eng.spagobi.tools.crossnavigation.dao;

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
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

public class CrossNavigationDAOImpl extends AbstractHibernateDAO implements ICrossNavigationDAO {

	public static final int TYPE_INPUT = 1;
	public static final int TYPE_OUTPUT = 0;

	IMessageBuilder message;

	public CrossNavigationDAOImpl() {
		message = MessageBuilderFactory.getMessageBuilder();
	}

	@Override
	public List<SimpleNavigation> listNavigation() {
		final List<SimpleNavigation> lst = new ArrayList<>();
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) {
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
			public Boolean execute(Session session) {
				if (nd.getSimpleNavigation().getId() != null) {
					throw new SpagoBIDOAException("Write error: record not valid");
				}
				SbiCrossNavigation cn = new SbiCrossNavigation();
				cn.setName(nd.getSimpleNavigation().getName());
				cn.setSbiCrossNavigationPars(new HashSet<SbiCrossNavigationPar>());
				if (nd.getToPars() != null) {
					for (SimpleParameter sp : nd.getToPars()) {
						if (sp.getLinks() != null && !sp.getLinks().isEmpty()) {
							SbiCrossNavigationPar cnp = new SbiCrossNavigationPar();
							cnp.setFromKeyId(sp.getLinks().get(0).getId());
							cnp.setFromType(sp.getLinks().get(0).getType());
							cnp.setToKeyId(sp.getId());
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
			public Boolean execute(Session session) {
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
							cnp.setFromKeyId(sp.getLinks().get(0).getId());
							cnp.setFromType(sp.getLinks().get(0).getType());
							cnp.setToKeyId(sp.getId());
							cnp.setSbiCrossNavigation(cn);
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
			public NavigationDetail execute(Session session) {
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
						if (Integer.valueOf(1).equals(cnp.getFromType())) {
							// parameter of type "input"
							SbiObjPar inputParameter = (SbiObjPar) session.get(SbiObjPar.class, cnp.getFromKeyId());
							fromSp.setName(inputParameter.getLabel());
							if (fromDoc == null) {
								fromDoc = inputParameter.getSbiObject();
							}
						} else {
							// parameter of type "output"
							SbiOutputParameter outputParameter = (SbiOutputParameter) session.get(SbiOutputParameter.class, cnp.getFromKeyId());
							fromSp.setName(outputParameter.getLabel());
							if (fromDoc == null) {
								fromDoc = outputParameter.getSbiObject();
							}
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

					// SbiObjects fromDoc = cn.getSbiCrossNavigationPars().iterator().next().getFromKey().getSbiObject();
					// SbiObjects toDoc = cn.getSbiCrossNavigationPars().iterator().next().getToKey().getSbiObject();

					nd.setSimpleNavigation(new SimpleNavigation(cn.getId(), cn.getName(), fromDoc.getLabel(), toDoc.getLabel()));

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
