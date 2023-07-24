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
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.view.metadata.SbiViewForDoc;

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

}
