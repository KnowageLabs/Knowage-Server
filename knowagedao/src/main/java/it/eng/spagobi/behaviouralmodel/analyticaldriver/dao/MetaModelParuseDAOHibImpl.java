package it.eng.spagobi.behaviouralmodel.analyticaldriver.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.MetaModelParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiMetamodelParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuse;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class MetaModelParuseDAOHibImpl extends AbstractHibernateDAO implements IMetaModelParuseDAO {

	@Override
	public List loadMetaModelParuseById(Integer metaModelParuseId) {
		List<MetaModelParuse> metaModelParuses = new ArrayList();
		MetaModelParuse toReturn = null;
		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();

			String hql = "from SbiMetamodelParuse s where s.sbiMetaModelPar=? " + " order by s.prog";

			Query query = session.createQuery(hql);
			query.setInteger(0, metaModelParuseId.intValue());

			List sbiMetaModelParuses = query.list();
			if (sbiMetaModelParuses == null)
				return metaModelParuses;
			Iterator itersbiOP = sbiMetaModelParuses.iterator();
			while (itersbiOP.hasNext()) {
				SbiMetamodelParuse sbiMetamodelParuse = (SbiMetamodelParuse) itersbiOP.next();
				MetaModelParuse metaModelParuse = toMetaModelParuse(sbiMetamodelParuse);
				metaModelParuses.add(metaModelParuse);
			}
			transaction.commit();
		} catch (HibernateException he) {
			logException(he);
			if (transaction != null)
				transaction.rollback();
			throw new SpagoBIRuntimeException(he.getMessage(), he);
		} finally {
			if (session != null) {
				if (session.isOpen())
					session.close();
			}
		}
		return metaModelParuses;
	}

	/**
	 * Load obj paruse.
	 *
	 * @param objParId
	 *            the obj par id
	 * @param paruseId
	 *            the paruse id
	 *
	 * @return the list
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#loadObjParuse(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List loadMetaModelParuse(Integer metaModelParId, Integer paruseId) throws HibernateException {
		List metaModelParuses = new ArrayList();
		MetaModelParuse toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "from SbiMetamodelParuse s where s.sbiMetaModelPar.metaModelParId=? " + " and s.sbiParuse.useId=? " + " order by s.prog";

			Query query = aSession.createQuery(hql);
			query.setInteger(0, metaModelParId.intValue());
			query.setInteger(1, paruseId.intValue());

			List sbiMetaModelParuses = query.list();
			if (sbiMetaModelParuses == null)
				return metaModelParuses;
			Iterator itersbiOP = sbiMetaModelParuses.iterator();
			while (itersbiOP.hasNext()) {
				SbiMetamodelParuse sbiop = (SbiMetamodelParuse) itersbiOP.next();
				MetaModelParuse op = toMetaModelParuse(sbiop);
				metaModelParuses.add(op);
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
		return metaModelParuses;
	}

	@Override
	public void modifyMetaModelParuse(MetaModelParuse aMetaModelParuse) throws HibernateException {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "from SbiMetamodelParuse s  where s.id=? ";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, aMetaModelParuse.getId().intValue());

			SbiMetamodelParuse sbiMetamodelParuse = (SbiMetamodelParuse) hqlQuery.uniqueResult();
			if (sbiMetamodelParuse == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "modifyMetaModelParuse",
						"the MetaModelParuse with id " + aMetaModelParuse.getId() + " does not exist.");
			}

			SbiMetaModelParameter metaModelParameter = (SbiMetaModelParameter) aSession.load(SbiMetaModelParameter.class, aMetaModelParuse.getParId());
			SbiParuse sbiParuse = (SbiParuse) aSession.load(SbiParuse.class, aMetaModelParuse.getId());
			SbiMetaModelParameter sbiMetaModelParFather = (SbiMetaModelParameter) aSession.load(SbiMetaModelParameter.class, aMetaModelParuse.getParFatherId());

			sbiMetamodelParuse.setFilterColumn(aMetaModelParuse.getFilterColumn());
			sbiMetamodelParuse.setFilterOperation(aMetaModelParuse.getFilterOperation());
			sbiMetamodelParuse.setLogicOperator(aMetaModelParuse.getLogicOperator());
			sbiMetamodelParuse.setPostCondition(aMetaModelParuse.getPostCondition());
			sbiMetamodelParuse.setPreCondition(aMetaModelParuse.getPreCondition());
			sbiMetamodelParuse.setProg(aMetaModelParuse.getProg());
			sbiMetamodelParuse.setSbiMetaModelPar(metaModelParameter);

			if (sbiMetaModelParFather == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "modifyMetaModelParuse",
						"the MetaModelParameter with " + "id=" + aMetaModelParuse.getParFatherId() + " does not exist.");
			}

			sbiMetamodelParuse.setSbiMetaModelParFather(sbiMetaModelParFather);
			sbiMetamodelParuse.setSbiParuse(sbiParuse);

			updateSbiCommonInfo4Update(sbiMetamodelParuse);
			aSession.update(sbiMetamodelParuse);
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
	public void insertMetaModelParuse(MetaModelParuse aMetaModelParuse) throws HibernateException {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiMetaModelParameter sbiMetamodelPar = (SbiMetaModelParameter) aSession.load(SbiMetaModelParameter.class, aMetaModelParuse.getParId());
			SbiParuse sbiParuse = (SbiParuse) aSession.load(SbiParuse.class, aMetaModelParuse.getUseModeId());
			SbiMetaModelParameter sbiMetamodelParFather = (SbiMetaModelParameter) aSession.load(SbiMetaModelParameter.class, aMetaModelParuse.getParFatherId());
			if (sbiMetamodelParFather == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "modifyMetaModelParuse",
						"the BIMetaModelParameter with " + "id=" + aMetaModelParuse.getParFatherId() + " does not exist.");

			}
			SbiMetamodelParuse newHibMetaModel = new SbiMetamodelParuse();
			newHibMetaModel.setSbiMetaModelPar(sbiMetamodelPar);
			newHibMetaModel.setSbiParuse(sbiParuse);
			newHibMetaModel.setSbiMetaModelParFather(sbiMetamodelParFather);
			newHibMetaModel.setFilterOperation(aMetaModelParuse.getFilterOperation());

			newHibMetaModel.setProg(aMetaModelParuse.getProg());
			newHibMetaModel.setFilterColumn(aMetaModelParuse.getFilterColumn());
			newHibMetaModel.setPreCondition(aMetaModelParuse.getPreCondition());
			newHibMetaModel.setPostCondition(aMetaModelParuse.getPostCondition());
			newHibMetaModel.setLogicOperator(aMetaModelParuse.getLogicOperator());
			updateSbiCommonInfo4Insert(newHibMetaModel);
			Integer paruseId = (Integer) aSession.save(newHibMetaModel);
			aMetaModelParuse.setId(paruseId);
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
	public void eraseMetaModelParuse(MetaModelParuse aMetaModelParuse) throws HibernateException {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "from SbiMetamodelParuse s where s.id = ? ";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, aMetaModelParuse.getId().intValue());

			SbiMetamodelParuse sbiMetamodelParuse = (SbiMetamodelParuse) hqlQuery.uniqueResult();
			if (sbiMetamodelParuse == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "eraseMetaModelParuse",
						"the MetaModelParuse with " + "id=" + aMetaModelParuse.getId() + " does not exist.");
			}
			aSession.delete(sbiMetamodelParuse);
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
	public List loadAllParuses(Integer metaModelParId) {

		List toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "from SbiMetamodelParuse s where s.sbiMetaModelPar.metaModelParId = ? order by s.prog";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, metaModelParId.intValue());
			List sbiMetaModelParuses = hqlQuery.list();
			Iterator it = sbiMetaModelParuses.iterator();
			while (it.hasNext()) {
				toReturn.add(toMetaModelParuse((SbiMetamodelParuse) it.next()));
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
	public List loadMetaModelParusesFather(Integer metaModelParId) throws HibernateException {
		List toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "from SbiMetamodelParuse s where s.sbiMetaModelParFather.metaModelParId = ? order by s.prog";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, metaModelParId.intValue());
			List sbiMetaModelParuses = hqlQuery.list();
			Iterator it = sbiMetaModelParuses.iterator();
			while (it.hasNext()) {
				toReturn.add(toMetaModelParuse((SbiMetamodelParuse) it.next()));
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

	public MetaModelParuse toMetaModelParuse(SbiMetamodelParuse aSbiMetamodelParuse) {
		if (aSbiMetamodelParuse == null)
			return null;
		MetaModelParuse toReturn = new MetaModelParuse();
		toReturn.setId(aSbiMetamodelParuse.getId());
		toReturn.setParId(aSbiMetamodelParuse.getSbiMetaModelPar().getMetaModelParId());
		toReturn.setUseModeId(aSbiMetamodelParuse.getSbiParuse().getUseId());
		toReturn.setProg(aSbiMetamodelParuse.getProg());
		toReturn.setParFatherId(aSbiMetamodelParuse.getSbiMetaModelParFather().getMetaModelParId());
		toReturn.setFilterColumn(aSbiMetamodelParuse.getFilterColumn());
		toReturn.setFilterOperation(aSbiMetamodelParuse.getFilterOperation());
		toReturn.setPreCondition(aSbiMetamodelParuse.getPreCondition());
		toReturn.setPostCondition(aSbiMetamodelParuse.getPostCondition());
		toReturn.setLogicOperator(aSbiMetamodelParuse.getLogicOperator());
		toReturn.setParFatherUrlName(aSbiMetamodelParuse.getSbiMetaModelParFather().getParurlNm());
		return toReturn;
	}

}
