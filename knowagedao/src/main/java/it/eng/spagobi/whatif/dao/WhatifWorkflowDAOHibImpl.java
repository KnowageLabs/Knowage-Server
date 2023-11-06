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

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.whatif.metadata.SbiWhatifWorkflow;

public class WhatifWorkflowDAOHibImpl extends AbstractHibernateDAO implements IWhatifWorkflowDAO {

	private static final Logger LOGGER = Logger.getLogger(WhatifWorkflowDAOHibImpl.class);
	private static final String STATE_DONE = "done";
	private static final String STATE_NOT_STARTED_YET = "notstartedyet";
	private static final String STATE_INPROGRESS = "inprogress";

	@Override
	public void createNewWorkflow(List<SbiWhatifWorkflow> newWorkflow) {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			for (int i = 0; i < newWorkflow.size(); i++) {
				updateSbiCommonInfo4Update(newWorkflow.get(i));
				aSession.save(newWorkflow.get(i));
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Exception creating workflow", he);
			throw new SpagoBIRuntimeException("Exception creating workflow", he);
		} finally {
			LOGGER.debug("Workflow created correctly");
			closeSessionIfOpen(aSession);
		}
	}

	@Override
	public SbiWhatifWorkflow loadUsersWorkflow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getActiveUserIdByModel(int modelId) {
		LOGGER.debug("IN");
		LOGGER.debug("loading the active user for the model " + modelId);
		Session aSession = null;
		try {
			aSession = getSession();
			Criteria criteria = aSession.createCriteria(SbiWhatifWorkflow.class);
			Criterion rest1 = Restrictions.eq("modelId", modelId);
			Criterion rest2 = Restrictions.eq("state", STATE_INPROGRESS);
			criteria.add(Restrictions.and(rest1, rest2));

			Object wf = criteria.uniqueResult();
			if (wf == null) {
				return null;
			} else {
				SbiWhatifWorkflow el = (SbiWhatifWorkflow) wf;
				int userId = el.getUserId();
				return getUserName(userId);
			}

		} catch (Exception he) {
			LOGGER.error("Exception loading the active user in the worflow", he);
			throw new SpagoBIRuntimeException("Exception loading the active user in the worflow", he);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("OUT");
		}
	}

	private String getUserName(int userId) throws EMFUserError {
		ISbiUserDAO userdao = DAOFactory.getSbiUserDAO();
		SbiUser user = userdao.loadSbiUserById(userId);
		return user.getUserId();
	}

	public List<SbiWhatifWorkflow> getWorkflowByModel(int modelId) {
		LOGGER.debug("IN");
		Session aSession = null;
		try {
			aSession = getSession();
			Criteria criteria = aSession.createCriteria(SbiWhatifWorkflow.class);
			Criterion rest1 = Restrictions.eq("modelId", modelId);
			criteria.add(rest1);
			criteria.addOrder(Order.asc("sequcence"));
			return criteria.list();

		} catch (HibernateException he) {
			LOGGER.error("Error loading workflow for model" + modelId, he);
			throw new SpagoBIRuntimeException("Error loading workflow for model" + modelId, he);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("OUT");
		}

	}

	@Override
	public List<Integer> getWorkflowUsersOfModel(int modelId) {
		LOGGER.debug("IN");
		List<Integer> resultArray = new ArrayList<>();
		Session aSession = null;
		try {
			aSession = getSession();
			Criteria criteria = aSession.createCriteria(SbiWhatifWorkflow.class);
			Criterion rest1 = Restrictions.eq("modelId", modelId);
			criteria.add(rest1);
			criteria.addOrder(Order.asc("sequcence"));
			List<SbiWhatifWorkflow> list = criteria.list();
			Iterator<SbiWhatifWorkflow> it = list.iterator();

			while (it.hasNext()) {
				SbiWhatifWorkflow el = it.next();
				resultArray.add(el.getUserId());
			}

		} catch (HibernateException he) {
			LOGGER.error("Exception loading workflow users", he);
			throw new SpagoBIRuntimeException("Exception loading workflow users", he);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("OUT");
		}
		return resultArray;
	}

	@Override
	public int isWorkflowStarted(int modelId) {
		LOGGER.debug("IN");
		Session aSession = null;
		try {
			aSession = getSession();
			Criteria criteria = aSession.createCriteria(SbiWhatifWorkflow.class);
			Criterion rest1 = Restrictions.eq("modelId", modelId);
			Criterion rest2 = Restrictions.eq("state", STATE_INPROGRESS);
			criteria.add(Restrictions.and(rest1, rest2));

			List<SbiWhatifWorkflow> list = criteria.list();
			if (list.size() > 1) {
				return -1;
			} else if (list.isEmpty()) {
				return 0;
			} else {
				SbiWhatifWorkflow el = list.get(0);
				return el.getUserId();
			}
		} catch (HibernateException he) {
			LOGGER.error("Exception loading workflow users", he);
			return 0;
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("OUT");
		}

	}

	@Override
	public void startWorkflow(int modelId) {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		List<SbiWhatifWorkflow> workflow = getWorkflowByModel(modelId);

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			for (int i = 0; i < workflow.size(); i++) {
				SbiWhatifWorkflow wf = workflow.get(i);
				if (wf.getSequcence() == 0) {
					wf.setState(STATE_INPROGRESS);
					updateSbiCommonInfo4Update(wf);
					aSession.update(wf);
					break;
				}

			}

			tx.commit();
		} catch (HibernateException he) {
			LOGGER.error(he.getMessage(), he);
			rollbackIfActive(tx);
		} finally {
			closeSessionIfOpen(aSession);
		}

	}

	@Override
	public void updateWorkflow(List<SbiWhatifWorkflow> workflow, int mId) {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		List<Integer> existing = getWorkflowUsersOfModel(mId);
		List<Integer> toRemove = toRemove(workflow, existing);
		List<Integer> toAdd = toAdd(workflow, existing);
		List<SbiWhatifWorkflow> wfToAdd = new ArrayList<>();
		if (!toRemove.isEmpty())
			removeUsersForUpdate(toRemove, mId);

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			for (int i = 0; i < workflow.size(); i++) {
				if (!toAdd.contains(workflow.get(i).getUserId())) {
					SbiWhatifWorkflow wf = workflow.get(i);

					wf.setSequcence(i);
					updateSbiCommonInfo4Update(wf);
					aSession.update(wf);

				} else {
					wfToAdd.add(workflow.get(i));
				}

			}
			tx.commit();
		} catch (HibernateException he) {
			LOGGER.error(he.getMessage(), he);
			rollbackIfActive(tx);
		} finally {
			closeSessionIfOpen(aSession);

			if (!wfToAdd.isEmpty())
				createNewWorkflow(wfToAdd);
		}
	}

	public void removeUsersForUpdate(List<Integer> userIds, int modelId) {
		LOGGER.debug("IN");
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
			LOGGER.error(he.getMessage(), he);
			rollbackIfActive(tx);
		} finally {
			closeSessionIfOpen(aSession);
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

	@Override
	public String goNextUserByModel(int modelId) {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<SbiWhatifWorkflow> existing = getWorkflowByModel(modelId);

		try {

			for (int i = 0; i < existing.size(); i++) {
				SbiWhatifWorkflow actual = existing.get(i);
				String state = actual.getState();
				if (state.equals(STATE_INPROGRESS)) {// if we've found the
														// active user
					// we set value to done

					aSession = getSession();
					tx = aSession.beginTransaction();
					LOGGER.debug("Actual active user is " + actual.getUserId());
					Criteria criteria = aSession.createCriteria(SbiWhatifWorkflow.class);
					Criterion rest1 = Restrictions.eq("modelId", modelId);
					Criterion rest2 = Restrictions.eq("userId", actual.getUserId());
					criteria.add(Restrictions.and(rest1, rest2));
					SbiWhatifWorkflow wf = (SbiWhatifWorkflow) criteria.uniqueResult();
					wf.setState(STATE_DONE);
					updateSbiCommonInfo4Update(wf);
					aSession.update(wf);

					LOGGER.debug("Done set state done to actual active user is " + actual.getUserId());
					// if actual is not last user in workflow we enable next
					// user
					if (i < existing.size() - 1) {
						SbiWhatifWorkflow next = existing.get(i + 1);
						LOGGER.debug("Actual active user is " + actual.getUserId());
						Criteria criteria2 = aSession.createCriteria(SbiWhatifWorkflow.class);
						Criterion rest2_1 = Restrictions.eq("modelId", modelId);
						Criterion rest2_2 = Restrictions.eq("userId", next.getUserId());
						criteria2.add(Restrictions.and(rest2_1, rest2_2));
						SbiWhatifWorkflow wf2 = (SbiWhatifWorkflow) criteria2.uniqueResult();
						wf2.setState(STATE_INPROGRESS);
						updateSbiCommonInfo4Update(wf2);
						aSession.saveOrUpdate(wf2);
						LOGGER.debug("Done set state inprogress to next active user is " + next.getUserId());
						tx.commit();

						return getUserName(next.getUserId());

					} else {
						LOGGER.debug("No other user in the workflow");
						tx.commit();
					}
					return null;
				}
			}

		} catch (Exception he) {
			LOGGER.error("Error setting passing the control to next user", he);
			rollbackIfActive(tx);
			throw new SpagoBIRuntimeException("Error setting passing the control to next user", he);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("Successfully pass controll to next user for model" + modelId);
		}
		return null;

	}

	@Override
	public int idByUserAndModel(int userId, int modelId) {
		LOGGER.debug("IN");
		LOGGER.debug("loading the active user for the model " + modelId);
		Session aSession = null;
		try {
			aSession = getSession();
			Criteria criteria = aSession.createCriteria(SbiWhatifWorkflow.class);
			Criterion rest1 = Restrictions.eq("modelId", modelId);
			Criterion rest2 = Restrictions.eq("userId", userId);
			criteria.add(Restrictions.and(rest1, rest2));

			List<SbiWhatifWorkflow> list = criteria.list();

			if (list.isEmpty())
				return -1; // in case there is new user inserted on update
							 // action
			else
				return list.get(0).getId();

		} catch (Exception he) {
			LOGGER.error("Exception loading the active user in the worflow", he);
			throw new SpagoBIRuntimeException("Exception loading the active user in the worflow", he);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("OUT");
		}
	}
}
