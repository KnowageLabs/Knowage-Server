package it.eng.spagobi.whatif.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.whatif.metadata.SbiWhatifWorkflow;
import it.eng.spagobi.workspace.dao.FunctionsOrganizerDAOHibImpl;

public class WhatifWorkflowDAOHibImpl extends AbstractHibernateDAO implements IWhatifWorkflowDAO {

	private static transient Logger logger = Logger.getLogger(FunctionsOrganizerDAOHibImpl.class);

	@Override
	public void createNewWorkflow(List<SbiWhatifWorkflow> newWorkflow) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			for (int i = 0; i < newWorkflow.size(); i++)
				aSession.save(newWorkflow.get(i));

			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public SbiWhatifWorkflow loadUsersWorkflow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> getWorkflowUsersOfModel(int modelId) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List list;
		List<Integer> resultArray = new ArrayList<>();

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criteria criteria = getSession().createCriteria(SbiWhatifWorkflow.class);
			Criterion rest1 = Restrictions.eq("modelId", modelId);
			criteria.add(rest1);
			criteria.addOrder(Order.asc("sequcence"));
			list = criteria.list();
			Iterator it = list.iterator();

			while (it.hasNext()) {
				SbiWhatifWorkflow el = (SbiWhatifWorkflow) it.next();
				resultArray.add(el.getUserId());
			}

		} catch (HibernateException he) {
			logger.error("HibernateException", he);
			// throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return resultArray;
	}

	@Override
	public int isWorkflowStarted(int modelId) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criteria criteria = getSession().createCriteria(SbiWhatifWorkflow.class);
			Criterion rest1 = Restrictions.eq("modelId", modelId);
			Criterion rest2 = Restrictions.eq("state", "inprogress");
			criteria.add(Restrictions.and(rest1, rest2));

			List list = criteria.list();
			if (list.size() > 1) {
				return -1;
			} else if (list.size() < 1) {
				return 0;
			} else {
				SbiWhatifWorkflow el = (SbiWhatifWorkflow) list.get(0);
				return el.getUserId();
			}
		} catch (HibernateException he) {
			logger.error("HibernateException", he);
			// throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

		return 0;
	}

	@Override
	public void startWorkflow(int modelId) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query q = aSession.createSQLQuery("UPDATE sbi_whatif_workflow SET state = :stateValue WHERE model_id = :mId AND sequence = 0");
			q.setParameter("stateValue", "inprogress");
			q.setParameter("mId", modelId);
			q.executeUpdate();
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

	}

	@Override
	public void updateWorkflow(List<SbiWhatifWorkflow> workflow) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		int mId = workflow.get(0).getModelId();
		List<Integer> existing = getWorkflowUsersOfModel(mId);
		List<Integer> toRemove = toRemove(workflow, existing);
		List<Integer> toAdd = toAdd(workflow, existing);
		List<SbiWhatifWorkflow> wfToAdd = new ArrayList<>();
		if (toRemove.size() > 0)
			removeUsersForUpdate(toRemove, mId);

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			for (int i = 0; i < workflow.size(); i++) {
				if (!toAdd.contains(workflow.get(i).getUserId())) {
					Query q = aSession.createSQLQuery("UPDATE sbi_whatif_workflow SET sequence = :seq WHERE model_id = :mId AND user_id = :uId");
					q.setParameter("seq", i);
					q.setParameter("mId", workflow.get(i).getModelId());
					q.setParameter("uId", workflow.get(i).getUserId());
					q.executeUpdate();
				} else {
					wfToAdd.add(workflow.get(i));
				}

			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}

			if (!wfToAdd.isEmpty())
				createNewWorkflow(wfToAdd);
		}
	}

	public void removeUsersForUpdate(List<Integer> userIds, int modelId) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			for (int i = 0; i < userIds.size(); i++) {
				Query q = aSession.createQuery("DELETE SbiWhatifWorkflow WHERE model_id= :mId AND user_id = :uId");
				q.setParameter("mId", modelId);
				q.setParameter("uId", userIds.get(i));

				q.executeUpdate();
			}

			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	private List<Integer> toRemove(List<SbiWhatifWorkflow> workflow, List<Integer> existing) {
		List<Integer> wfUserIds = userIdsFromWorkflow(workflow);
		List<Integer> result = new ArrayList<>();

		for (int i = 0; i < existing.size(); i++) {
			int curId = existing.get(i);

			if (!wfUserIds.contains(curId))
				result.add(curId);
		}

		return result;
	}

	private List<Integer> toAdd(List<SbiWhatifWorkflow> workflow, List<Integer> existing) {
		List<Integer> result = new ArrayList<>();

		for (int i = 0; i < workflow.size(); i++) {
			int curId = workflow.get(i).getUserId();

			if (!existing.contains(curId))
				result.add(curId);
		}

		return result;
	}

	private List<Integer> userIdsFromWorkflow(List<SbiWhatifWorkflow> workflow) {
		List<Integer> toReturn = new ArrayList<>();
		for (int i = 0; i < workflow.size(); i++) {
			toReturn.add(workflow.get(i).getUserId());
		}

		return toReturn;
	}
}
