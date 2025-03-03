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
package it.eng.spagobi.commons.dao;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;

import it.eng.spagobi.commons.metadata.SbiDashboardTheme;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.hibernate.criterion.Restrictions;

/**
 * @author Marco Libanori
 */
@SuppressWarnings("all")
public class DashboardThemeDAOHibImpl extends AbstractHibernateDAO implements IDashboardThemeDAO {

	private static final Logger LOGGER = LogManager.getLogger(DashboardThemeDAOHibImpl.class);

	@Override
	public List<SbiDashboardTheme> read() {
		Session session = null;
		List<SbiDashboardTheme> ret = Collections.emptyList();
		try {
			session = getSession();

			Criteria c = session.createCriteria(SbiDashboardTheme.class);
			c.addOrder(Order.asc("themeName"));

			ret = c.list();
		} catch (Exception ex) {
			LOGGER.error("Error getting all dashboard theme: {}", ex);
			throw new SpagoBIRuntimeException(ex);
		} finally {
			closeSessionIfOpen(session);
		}
		return ret;
	}

	@Override
	public Optional<SbiDashboardTheme> readById(UUID id) {
		Session session = null;
		Optional<SbiDashboardTheme> ret = null;

		try {
			session = getSession();

			ret = Optional.ofNullable((SbiDashboardTheme) session.get(SbiDashboardTheme.class, id));

		} catch (Exception ex) {
			LOGGER.error("Error getting following dashboard theme: {}", id, ex);
			throw new SpagoBIRuntimeException(ex);
		} finally {
			closeSessionIfOpen(session);
		}
		return ret;
	}

	@Override
	public Optional<SbiDashboardTheme> readByThemeName(String themeName) {
		Session session = null;
		Optional<SbiDashboardTheme> ret = null;

		try {
			session = getSession();

//			ret = Optional.ofNullable((SbiDashboardTheme) session.get(SbiDashboardTheme.class, themeName));

			//GET BY THEMENAME
			Criteria c = session.createCriteria(SbiDashboardTheme.class);
			c.add(Restrictions.eq("themeName", themeName));
			ret = Optional.ofNullable((SbiDashboardTheme) c.uniqueResult());

		} catch (Exception ex) {
			LOGGER.error("Error getting following dashboard theme: {}", themeName, ex);
			throw new SpagoBIRuntimeException(ex);
		} finally {
			closeSessionIfOpen(session);
		}
		return ret;
	}

	@Override
	public SbiDashboardTheme update(SbiDashboardTheme e) {
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
			LOGGER.error("Error updating following dashboard theme: {}", e, ex);
			throw new SpagoBIRuntimeException(ex);
		} finally {
			closeSessionIfOpen(session);
		}
		return e;
	}

	@Override
	public void delete(SbiDashboardTheme e) {
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
			LOGGER.error("Error deleting following dashboard theme: {}", e, ex);
			throw new SpagoBIRuntimeException(ex);
		} finally {
			closeSessionIfOpen(session);
		}
	}

	@Override
	public SbiDashboardTheme create(SbiDashboardTheme e) {
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
			LOGGER.error("Error creating following dashboard theme: {}", e, ex);
			throw new SpagoBIRuntimeException(ex);
		} finally {
			closeSessionIfOpen(session);
		}
		return e;
	}

}
