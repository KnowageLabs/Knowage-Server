/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.

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

package it.eng.spagobi.view.dao;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Filter;
import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.view.metadata.SbiViewForDoc;
import it.eng.spagobi.view.metadata.SbiViewHierarchy;

/**
 * @author Marco Libanori
 * @since KNOWAGE_TM-513
 */
public class SbiViewForDocHIBDAOImpl extends AbstractHibernateDAO implements ISbiViewForDocDAO {

	@Override
	public SbiViewForDoc create(SbiViewForDoc e) {
		Session session = null;
		Transaction tx = null;

		try {
			session = getSession();
			tx = session.beginTransaction();

			UserProfile profile = (UserProfile) getUserProfile();
			session.enableFilter(FILTER_USER).setParameter(FILTER_USER_PARAM_USER, profile.getUserId());
			// Lock an existing row to serialize concurrent imports, also across server nodes.
			SbiViewHierarchy parent = (SbiViewHierarchy) session.createCriteria(SbiViewHierarchy.class)
					.add(Restrictions.eq("id", e.getParent().getId()))
					.setLockMode(LockMode.PESSIMISTIC_WRITE)
					.uniqueResult();
			if (parent == null) {
				throw new IllegalArgumentException("Workspace folder does not belong to the current user");
			}
			List<SbiViewForDoc> existing = session.createCriteria(SbiViewForDoc.class)
					.add(Restrictions.eq("parent.id", parent.getId()))
					.add(Restrictions.eq("biObjId", e.getBiObjId()))
					.add(Restrictions.eq("commonInfo.userIn", profile.getUserId()))
					.addOrder(Order.asc("id"))
					.setMaxResults(1)
					.list();
			if (!existing.isEmpty()) {
				tx.commit();
				return existing.get(0);
			}
			e.setParent(parent);
			updateSbiCommonInfo4Insert(e);

			session.save(e);

			tx.commit();

		} catch (Exception ex) {
			rollbackIfActive(tx);
			throw ex;
		} finally {
			closeSessionIfOpen(session);
		}

		return e;
	}

	@Override
	public SbiViewForDoc delete(SbiViewForDoc e) {
		Session session = null;
		Transaction tx = null;

		try {
			session = getSession();
			tx = session.beginTransaction();

			updateSbiCommonInfo4Update(e);

			session.delete(e);

			tx.commit();

		} catch (Exception ex) {
			rollbackIfActive(tx);
			throw ex;
		} finally {
			closeSessionIfOpen(session);
		}

		return e;
	}

	@Override
	public SbiViewForDoc read(String id) {
		SbiViewForDoc e = null;
		Session session = null;

		try {
			session = getSession();

			e = (SbiViewForDoc) session.load(SbiViewForDoc.class, id);

		} finally {
			closeSessionIfOpen(session);
		}

		return e;
	}

	@Override
	public SbiViewForDoc update(SbiViewForDoc e) {
		Session session = null;
		Transaction tx = null;

		try {
			session = getSession();
			tx = session.beginTransaction();

			updateSbiCommonInfo4Update(e);

			session.update(e);

			tx.commit();

		} catch (Exception ex) {
			rollbackIfActive(tx);
			throw ex;
		} finally {
			closeSessionIfOpen(session);
		}

		return e;
	}

	@Override
	public Set<SbiViewForDoc> readByViewHierarchyId(String id) {
		Set<SbiViewForDoc> ret = new TreeSet<>();
		Session session = null;

		try {
			session = getSession();

			UserProfile userProfile = (UserProfile) getUserProfile();
			Filter filter = session.enableFilter(FILTER_USER);
			filter.setParameter(FILTER_USER_PARAM_USER, userProfile.getUserId());

			List<SbiViewForDoc> list = session.createCriteria(SbiViewForDoc.class).createAlias("parent", "parent").add(Restrictions.eq("parent.id", id)).list();

			ret.addAll(list);

		} finally {
			if (Objects.nonNull(session)) {
				session.close();
			}
		}

		return ret;
	}
	
	@Override
	public Set<SbiViewForDoc> readAllOwned() {
	    Session session = null;
	 
	    try {
	        session = getSession();
	 
	        UserProfile userProfile = (UserProfile) getUserProfile();
	        Filter filter = session.enableFilter(FILTER_USER);
	        filter.setParameter(FILTER_USER_PARAM_USER, userProfile.getUserId());
	 
	        List<SbiViewForDoc> list = session
	                .createCriteria(SbiViewForDoc.class)
	                .setFetchMode("parent", FetchMode.JOIN)
	                .list();
	 
	        return new TreeSet<>(list);
	    } finally {
	        closeSessionIfOpen(session);
	    }
	}

}
