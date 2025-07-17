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
package it.eng.spagobi.wapp.dao;

import java.util.*;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.wapp.metadata.SbiMenu;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.wapp.bo.Menu;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public class MenuRolesDAOImpl extends AbstractHibernateDAO implements IMenuRolesDAO {
	private static final Logger LOGGER = Logger.getLogger(MenuRolesDAOImpl.class);

	/**
	 * Load menu by role id.
	 *
	 * @param roleId
	 *            the role id
	 *
	 * @return the list
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.wapp.dao.IMenuRolesDAO#loadMenuByRoleId(java.lang.Integer)
	 */
	@Override
	public List loadMenuByRoleId(Integer roleId) throws EMFUserError {
		LOGGER.debug("IN");
		if (roleId != null)
			LOGGER.debug("roleId=" + roleId.toString());
		Session aSession = null;
		Transaction tx = null;
		List<Menu> realResult = new ArrayList<>();
		String hql = null;
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hql = " select mf.id.menuId, mf.id.extRoleId from SbiMenuRole as mf, SbiMenu m " + " where mf.id.menuId = m.menuId " + " and mf.id.extRoleId = ? "
					+ " order by m.parentId, m.prog";

			hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, roleId.intValue());
			List<Object[]> hibList = hqlQuery.list();

			Iterator<Object[]> it = hibList.iterator();
			IMenuDAO menuDAO = DAOFactory.getMenuDAO();
			Menu tmpMenu = null;
			while (it.hasNext()) {
				Object[] tmpLst = it.next();
				Integer menuId = (Integer) tmpLst[0];
				tmpMenu = menuDAO.loadMenuByID(menuId, roleId);
				if (tmpMenu != null) {
					LOGGER.debug("Add Menu:" + tmpMenu.getName());
					realResult.add(tmpMenu);
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			LOGGER.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		LOGGER.debug("OUT");
		return realResult;
	}
	/**
	 * Load menu by role id.
	 *
	 * @param roleId
	 *            the role id
	 *
	 * @param userProfile
	 *            the user profile
	 *
	 * @return the list
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.wapp.dao.IMenuRolesDAO#loadMenuByRoleId(java.lang.Integer)
	 */
	@Override
	public List loadMenuByRoleId(Integer roleId, IEngUserProfile userProfile) throws EMFUserError {
		LOGGER.debug("IN");
		if (roleId != null)
			LOGGER.debug("roleId=" + roleId.toString());
		Session aSession = null;
		Transaction tx = null;
		List<Menu> realResult = new ArrayList<>();
		String hql = null;
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hql = " select mf.id.menuId, mf.id.extRoleId from SbiMenuRole as mf, SbiMenu m " + " where mf.id.menuId = m.menuId " + " and mf.id.extRoleId = ? "
					+ " order by m.parentId, m.prog";

			hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, roleId.intValue());
			List<Object[]> hibList = hqlQuery.list();

			Iterator<Object[]> it = hibList.iterator();
			IMenuDAO menuDAO = DAOFactory.getMenuDAO();
			Menu tmpMenu = null;
			while (it.hasNext()) {
				Object[] tmpLst = it.next();
				Integer menuId = (Integer) tmpLst[0];
				tmpMenu = menuDAO.loadMenuByIDV2(menuId, roleId);
				if (tmpMenu != null) {
					LOGGER.debug("Add Menu:" + tmpMenu.getName());
					hql = " select distinct ser.name from SbiExtRoles as ser, SbiMenuRole smr " + " where ser.extRoleId = smr.id.extRoleId " + " and smr.id.menuId = ? ";
					hqlQuery = aSession.createQuery(hql);
					hqlQuery.setInteger(0, tmpMenu.getMenuId().intValue());
					List<String> roles = hqlQuery.list();
					if (userCanSeeTheMenu(tmpMenu, userProfile, roles)) {
						realResult.add(tmpMenu);
					}
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			LOGGER.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	@Override
	public Map<Integer, List<SbiMenu>> loadMenusByRoleIds(List<Integer> roleIds, IEngUserProfile userProfile) throws EMFUserError {

		Map<Integer, List<SbiMenu>> result = new HashMap<>();

		if (roleIds == null || roleIds.isEmpty()) {
			return result;
		}

		Session session = null;
		Transaction tx = null;

		try {
			session = getSession();
			tx = session.beginTransaction();

			// Single HQL query to get all menu-role associations
			String hql = "SELECT mr.id.extRoleId, m " +
					"FROM SbiMenuRole mr " +
					"JOIN mr.sbiMenu m " +
					"WHERE mr.id.extRoleId IN (:roleIds) " +
					"ORDER BY mr.id.extRoleId, m.parentId, m.prog";

			Query query = session.createQuery(hql);
			query.setParameterList("roleIds", roleIds);

			List<Object[]> results = query.list();

			for (Object[] row : results) {
				Integer roleId = (Integer) row[0];
				SbiMenu sbiMenu = (SbiMenu) row[1];

				// Apply user profile filtering if needed
				result.computeIfAbsent(roleId, k -> new ArrayList<>()).add(sbiMenu);

			}


			// Ensure all requested role IDs are present in the result map
			for (Integer roleId : roleIds) {
				result.putIfAbsent(roleId, new ArrayList<>());
				LOGGER.debug("Add roleId=" + roleId.toString());
			}


			tx.commit();

		} catch (HibernateException he) {
			if (tx != null) tx.rollback();
			LOGGER.error("Error loading menus by role IDs", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		return result;

	}

	public static boolean userCanSeeTheMenu(Menu menu, IEngUserProfile userProfile, List<String> roles) {
		boolean canView;
		if (menu.getCode() == null)
			canView = canViewV2(menu, userProfile, roles);
		else
			canView = true;
		return canView;
	}

	public static boolean canViewV2(Menu menu, IEngUserProfile profile, List<String> menuRoles) {
		LOGGER.debug("IN");
		Collection profileRoles = null;

		try {
			profileRoles = ((UserProfile) profile).getRolesForUse();
		} catch (EMFInternalError e) {
			return false;
		}

		boolean found = false;
		for (Iterator iterator = profileRoles.iterator(); iterator.hasNext() && !found;) {
			String profileRole = (String) iterator.next();
			for (int i = 0; i < menuRoles.size() && !found; i++) {
				String menuRoleName = menuRoles.get(i);
				if (menuRoleName.equals(profileRole)) {
					found = true;
					break;
				}
			}

		}
		LOGGER.debug("OUT");

		return found;

	}

}
