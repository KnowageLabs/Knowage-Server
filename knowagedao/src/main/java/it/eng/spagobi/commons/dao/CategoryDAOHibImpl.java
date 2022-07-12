/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.commons.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Marco Libanori
 *
 */
public class CategoryDAOHibImpl extends AbstractHibernateDAO implements ICategoryDAO {

	@Override
	public List<SbiCategory> getCategories(String type) {

		Session aSession = null;

		aSession = getSession();

		Criteria criteria = aSession.createCriteria(SbiCategory.class);

		Criterion restrictionOnType = Restrictions.eq("type", type);
		Order orderOnName = Order.asc("name");

		criteria.add(restrictionOnType);
		criteria.addOrder(orderOnName);

		return criteria.list();
	}

	@Override
	public SbiCategory getCategory(int id) {

		Session aSession = null;

		aSession = getSession();

		SbiCategory ret = getCategory(aSession, id);

		return ret;
	}

	@Override
	public SbiCategory getCategory(Session aSession, int id) {
		Criteria criteria = aSession.createCriteria(SbiCategory.class);

		Criterion restrictionOnId = Restrictions.eq("id", id);

		criteria.add(restrictionOnId);

		SbiCategory ret = null;

		try {
			ret = (SbiCategory) criteria.uniqueResult();
		} catch (HibernateException e) {
			throw new SpagoBIRuntimeException("Expected one category for id " + id, e);
		}
		return ret;
	}

	@Override
	public SbiCategory getCategory(String type, String name) {

		Session aSession = null;

		aSession = getSession();

		Criteria criteria = aSession.createCriteria(SbiCategory.class);

		Criterion restrictionOnId = Restrictions.eq("name", name);
		Criterion restrictionOnType = Restrictions.eq("type", type);

		Criterion andOfRestrictions = Restrictions.and(restrictionOnId, restrictionOnType);

		criteria.add(andOfRestrictions);

		SbiCategory ret = null;

		try {
			ret = (SbiCategory) criteria.uniqueResult();
		} catch (HibernateException e) {
			throw new SpagoBIRuntimeException("Expected one category for type " + type + " and name " + name, e);
		}

		return ret;
	}

	@Override
	public SbiCategory create(SbiCategory category) throws EMFUserError {

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			category = create(aSession, category);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null) {
				tx.rollback();
			}

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}

		return category;
	}

	@Override
	public void update(SbiCategory category) throws EMFUserError {

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			category = update(aSession, category);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null) {
				tx.rollback();
			}

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}

	}

	@Override
	public void delete(SbiCategory category) throws EMFUserError {

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			delete(aSession, category);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null) {
				tx.rollback();
			}

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}

	}

	private SbiCategory create(Session aSession, SbiCategory category) {

		updateSbiCommonInfo4Insert(category);

		aSession.persist(category);

		return category;
	}

	private SbiCategory update(Session aSession, SbiCategory category) {

		updateSbiCommonInfo4Update(category);

		aSession.update(category);

		return category;
	}

	private void delete(Session aSession, SbiCategory category) {

		aSession.delete(category);

	}

}
