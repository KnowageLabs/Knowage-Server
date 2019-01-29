package it.eng.spagobi.behaviouralmodel.analyticaldriver.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.MetaModelParview;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiMetaModelParview;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

public class MetaModelParviewDAOHibImpl extends AbstractHibernateDAO implements IMetaModelParviewDAO {

	@Override
	public List loadMetaModelParviewsByMetaModelParameterId(Integer MetaModelParameterId) {

		List<MetaModelParview> toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "from SbiMetaModelParview s where s.sbiMetaModelPar.metaModelParId = ? order by s.prog";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, MetaModelParameterId.intValue());
			List sbiMetaModelParviews = hqlQuery.list();
			Iterator it = sbiMetaModelParviews.iterator();
			while (it.hasNext()) {
				toReturn.add(toMetaModelParview((SbiMetaModelParview) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return toReturn;
	}

	@Override
	public void modifyMetaModelParview(MetaModelParview metaModelParview) {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// get the existing object
			String hql = "from SbiMetaModelParview s where s.parviewId= ? ";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, metaModelParview.getId().intValue());

			SbiMetaModelParview sbiMetaModelParview = (SbiMetaModelParview) hqlQuery.uniqueResult();
			if (sbiMetaModelParview == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "modifyMetaModelParview",
						"the MetaModelParview with " + "id=" + metaModelParview.getId() + "  does not exist.");

			}
			// delete the existing object
			aSession.delete(sbiMetaModelParview);
			// create the new object
			SbiMetaModelParameter sbiMetaModelPar = (SbiMetaModelParameter) aSession.load(SbiMetaModelParameter.class, metaModelParview.getParId());
			SbiMetaModelParameter sbiMetaModelParFather = (SbiMetaModelParameter) aSession.load(SbiMetaModelParameter.class, metaModelParview.getParFatherId());
			if (sbiMetaModelParFather == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "modifyMetaModelParview",
						"the BIMetaModelParameter with " + " does not exist.");

			}
			SbiMetaModelParview view = new SbiMetaModelParview();
			view.setParviewId(metaModelParview.getId());
			view.setSbiMetaModelPar(sbiMetaModelPar);
			view.setSbiMetaModelFather(sbiMetaModelParFather);
			view.setOperation(metaModelParview.getOperation());
			view.setCompareValue(metaModelParview.getCompareValue());
			view.setProg(metaModelParview.getProg());
			view.setViewLabel(metaModelParview.getViewLabel());

			// save new object
			updateSbiCommonInfo4Insert(view);
			aSession.save(view);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

	}

	@SuppressWarnings("finally")
	@Override
	public Integer insertMetaModelParview(MetaModelParview metaModelParview) throws HibernateException {
		Integer id = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiMetaModelParameter sbiMetaModelPar = (SbiMetaModelParameter) aSession.load(SbiMetaModelParameter.class, metaModelParview.getParId());
			SbiMetaModelParameter sbiMetaModelParFather = (SbiMetaModelParameter) aSession.load(SbiMetaModelParameter.class, metaModelParview.getParFatherId());
			if (sbiMetaModelParFather == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "modifyMetaModelParview",
						"the MetaModelParameter with " + "id=" + metaModelParview.getParFatherId() + " does not exist.");

			}
			SbiMetaModelParview view = new SbiMetaModelParview();
			view.setSbiMetaModelPar(sbiMetaModelPar);
			view.setSbiMetaModelFather(sbiMetaModelParFather);
			view.setOperation(metaModelParview.getOperation());
			view.setCompareValue(metaModelParview.getCompareValue());

			view.setProg(metaModelParview.getProg());
			view.setViewLabel(metaModelParview.getViewLabel());
			updateSbiCommonInfo4Insert(view);
			id = (Integer) aSession.save(view);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			return id;
		}

	}

	@Override
	public void eraseMetaModelParview(Integer parviewId) throws HibernateException {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "from SbiMetaModelParview s where s.parviewId = ? ";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, parviewId.intValue());

			SbiMetaModelParview sbiMetaModelParview = (SbiMetaModelParview) hqlQuery.uniqueResult();
			if (sbiMetaModelParview == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "eraseMetaModelParview",
						"the MetaModelParview  with " + "id=" + parviewId + " does not exist.");
			}
			aSession.delete(sbiMetaModelParview);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public List loadMetaModelParviewByID(Integer parviewId) {
		List<MetaModelParview> toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "from SbiMetaModelParview s where s.parviewId = ? order by s.prog";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, parviewId.intValue());
			List sbiMetaModelParviews = hqlQuery.list();
			Iterator it = sbiMetaModelParviews.iterator();
			while (it.hasNext()) {
				toReturn.add(toMetaModelParview((SbiMetaModelParview) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return toReturn;
	}

	public MetaModelParview toMetaModelParview(SbiMetaModelParview aSbiMetaModelParview) {
		if (aSbiMetaModelParview == null)
			return null;
		MetaModelParview toReturn = new MetaModelParview();
		toReturn.setId(aSbiMetaModelParview.getParviewId());
		toReturn.setParId(aSbiMetaModelParview.getSbiMetaModelPar().getMetaModelParId());
		toReturn.setParFatherId(aSbiMetaModelParview.getSbiMetaModelFather().getMetaModelParId());
		toReturn.setParFatherUrlName(aSbiMetaModelParview.getSbiMetaModelFather().getParurlNm());
		toReturn.setOperation(aSbiMetaModelParview.getOperation());
		toReturn.setCompareValue(aSbiMetaModelParview.getCompareValue());
		toReturn.setProg(aSbiMetaModelParview.getProg());
		toReturn.setViewLabel(aSbiMetaModelParview.getViewLabel());

		return toReturn;
	}

	@Override
	public List loadMetaModelParviews(Integer metaModelParId) {
		List<MetaModelParview> toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "from SbiMetaModelParview s where s.sbiMetaModelPar.metaModelParId = ? order by s.prog";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, metaModelParId.intValue());
			List sbiMetaModelParviews = hqlQuery.list();
			Iterator it = sbiMetaModelParviews.iterator();
			while (it.hasNext()) {
				toReturn.add(toMetaModelParview((SbiMetaModelParview) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return toReturn;
	}

	@Override
	public List loadMetaModelParviewsFather(Integer metaModelParId) {
		List<MetaModelParview> toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "from SbiMetaModelParview s where s.sbiMetaModelFather.metaModelParId = ? order by s.prog";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, metaModelParId.intValue());
			List sbiMetaModelParviews = hqlQuery.list();
			Iterator it = sbiMetaModelParviews.iterator();
			while (it.hasNext()) {
				toReturn.add(toMetaModelParview((SbiMetaModelParview) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return toReturn;
	}
}
