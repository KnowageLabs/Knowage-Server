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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.RoleDAOHibImpl;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.wapp.bo.Menu;
import it.eng.spagobi.wapp.bo.MenuIcon;
import it.eng.spagobi.wapp.metadata.SbiMenu;
import it.eng.spagobi.wapp.metadata.SbiMenuRole;
import it.eng.spagobi.wapp.metadata.SbiMenuRoleId;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public class MenuDAOImpl extends AbstractHibernateDAO implements IMenuDAO {
	private static transient Logger logger = Logger.getLogger(MenuDAOImpl.class);

	/**
	 * Load menu by id.
	 *
	 * @param menuID the menu id
	 *
	 * @return the menu
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.wapp.dao.IMenuDAO#loadMenuByID(integer)
	 */
	@Override
	public Menu loadMenuByID(Integer menuID) throws EMFUserError {
		Menu toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Criterion domainCdCriterrion = Expression.eq("menuId", menuID);
			Criteria criteria = tmpSession.createCriteria(SbiMenu.class);
			criteria.add(domainCdCriterrion);
			SbiMenu hibMenu = (SbiMenu) criteria.uniqueResult();
			if (hibMenu == null)
				return null;

			// SbiMenu hibMenu = (SbiMenu)tmpSession.load(SbiMenu.class,
			// menuID);
			toReturn = toMenu(hibMenu, null);
			// toReturn = toMenu(loadSbiMenuByID(menuID), null);

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();

			}
		}
		return toReturn;
	}

	/**
	 * Load sbiMenu by id.
	 *
	 * @param menuID the menu id
	 *
	 * @return the sbiMenu
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.wapp.dao.IMenuDAO#loadMenuByID(integer)
	 */
	@Override
	public SbiMenu loadSbiMenuByID(Integer menuID) throws EMFUserError {
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Criterion domainCdCriterrion = Expression.eq("menuId", menuID);
			Criteria criteria = tmpSession.createCriteria(SbiMenu.class);
			criteria.add(domainCdCriterrion);
			SbiMenu hibMenu = (SbiMenu) criteria.uniqueResult();

			return hibMenu;
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
	}

	@Override
	public SbiMenu loadSbiMenuByNameAndParentId(String name, Integer parentId) throws EMFUserError {
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Criteria criteria = tmpSession.createCriteria(SbiMenu.class);
			criteria.add(Restrictions.eq("name", name));
			if (parentId == null)
				criteria.add(Restrictions.isNull("parentId"));
			else
				criteria.add(Restrictions.eq("parentId", parentId));

			SbiMenu hibMenu = (SbiMenu) criteria.uniqueResult();

			return hibMenu;
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
	}

	/**
	 * Load menu by id.
	 *
	 * @param menuID the menu id
	 * @param roleId the user's role id
	 *
	 * @return the menu
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.wapp.dao.IMenuDAO#loadMenuByID(integer)
	 */
	@Override
	public Menu loadMenuByID(Integer menuID, Integer roleID) throws EMFUserError {
		Menu toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Criterion domainCdCriterrion = Expression.eq("menuId", menuID);
			Criteria criteria = tmpSession.createCriteria(SbiMenu.class);
			criteria.add(domainCdCriterrion);
			SbiMenu hibMenu = (SbiMenu) criteria.uniqueResult();
			if (hibMenu == null)
				return null;

			// SbiMenu hibMenu = (SbiMenu)tmpSession.load(SbiMenu.class,
			// menuID);
			toReturn = toMenu(hibMenu, roleID);

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();

			}
		}
		return toReturn;
	}

	/**
	 * Load menu by name.
	 *
	 * @param name the name
	 *
	 * @return the menu
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.wapp.dao.IMenuDAO#loadMenuByName(string)
	 */
	@Override
	public Menu loadMenuByName(String name) throws EMFUserError {
		Menu biMenu = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Expression.eq("name", name);
			Criteria criteria = tmpSession.createCriteria(SbiMenu.class);
			criteria.add(labelCriterrion);
			SbiMenu hibMenu = (SbiMenu) criteria.uniqueResult();
			if (hibMenu == null)
				return null;
			biMenu = toMenu(hibMenu, null);

			// tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		return biMenu;
	}

	/**
	 * Modify menu.
	 *
	 * @param aMenu the a menu
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.wapp.dao.IMenuDAO#modifyMenu(it.eng.spagobi.wapp.bo.Menu)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void modifyMenu(Menu aMenu) throws EMFUserError {

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiMenu hibMenu = (SbiMenu) tmpSession.load(SbiMenu.class, aMenu.getMenuId());
			hibMenu.setName(aMenu.getName());
			hibMenu.setDescr(aMenu.getDescr());
			hibMenu.setParentId(aMenu.getParentId());
			hibMenu.setObjId(aMenu.getObjId());
			hibMenu.setObjParameters(aMenu.getObjParameters());
			hibMenu.setSubObjName(aMenu.getSubObjName());
			hibMenu.setSnapshotName(aMenu.getSnapshotName());
			hibMenu.setSnapshotHistory(aMenu.getSnapshotHistory());
			hibMenu.setFunctionality(aMenu.getFunctionality());
			hibMenu.setInitialPath(aMenu.getInitialPath());

			// Modify Roles Associated
			// delete all roles previously associated
			Set<SbiMenuRole> oldRoles = hibMenu.getSbiMenuRoles();
			Iterator<SbiMenuRole> iterOldRoles = oldRoles.iterator();
			while (iterOldRoles.hasNext()) {
				SbiMenuRole role = iterOldRoles.next();
				tmpSession.delete(role);
			}
			// save roles functionality
			Set<SbiMenuRole> menuRoleToSave = new HashSet<>();
			menuRoleToSave.addAll(saveRolesMenu(tmpSession, hibMenu, aMenu));
			// set new roles into sbiFunctions
			hibMenu.setSbiMenuRoles(menuRoleToSave);

			// delete incongruous roles associations
			deleteIncongruousRoles(tmpSession, hibMenu);

			hibMenu.setViewIcons(new Boolean(aMenu.isViewIcons()));
			hibMenu.setHideToolbar(new Boolean(aMenu.getHideToolbar()));
			hibMenu.setHideSliders(new Boolean(aMenu.getHideSliders()));

			hibMenu.setStaticPage(aMenu.getStaticPage());
			hibMenu.setExternalApplicationUrl(aMenu.getExternalApplicationUrl());

			if (aMenu.getIcon() == null) {
				hibMenu.setIcon(null);

			} else {
				hibMenu.setIcon(new Gson().toJson(aMenu.getIcon()).toString());
			}

			if (aMenu.getCustIcon() == null) {
				hibMenu.setCustIcon(null);
			} else {
				hibMenu.setCustIcon(new Gson().toJson(aMenu.getCustIcon()).toString());
			}
			updateSbiCommonInfo4Update(hibMenu);
			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}

	}

	private void deleteIncongruousRoles(Session aSession, SbiMenu hibMenu) {
		// delete incongruous roles children of the current menu node
		Integer menuId = hibMenu.getMenuId();
		String getIncongruousRolesHqlQuery = "select mr FROM SbiMenuRole mr, SbiMenu m where mr.id.menuId = m.menuId and m.parentId = :MENU_ID "
				+ " and mr.id.extRoleId not in (select id.extRoleId from SbiMenuRole where id.menuId = :MENU_ID)";
		Query getIncongruousRolesQuery = aSession.createQuery(getIncongruousRolesHqlQuery);
		getIncongruousRolesQuery.setParameter("MENU_ID", menuId);
		List incongruousRoles = getIncongruousRolesQuery.list();
		if (incongruousRoles != null && !incongruousRoles.isEmpty()) {
			Iterator it = incongruousRoles.iterator();
			while (it.hasNext()) {
				SbiMenuRole role = (SbiMenuRole) it.next();
				aSession.delete(role);
			}
		}
		// recursion on children
		String getChildrenMenuNodesHqlQuery = " from SbiMenu s where s.id.parentId = ?";
		Query getChildrenMenuNodesQuery = aSession.createQuery(getChildrenMenuNodesHqlQuery);
		getChildrenMenuNodesQuery.setInteger(0, menuId.intValue());
		List childrenMenuNodes = getChildrenMenuNodesQuery.list();
		if (childrenMenuNodes != null && !childrenMenuNodes.isEmpty()) {
			Iterator it = childrenMenuNodes.iterator();
			while (it.hasNext()) {
				SbiMenu menu = (SbiMenu) it.next();
				deleteIncongruousRoles(aSession, menu);
			}
		}
	}

	/**
	 * Insert menu.
	 *
	 * @param aMenu the a menu
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.wapp.dao.IMenuDAO#insertMenu(it.eng.spagobi.wapp.bo.Menu)
	 */
	@Override
	public Menu insertMenu(Menu aMenu) throws EMFUserError {
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiMenu hibMenu = new SbiMenu();
			hibMenu.setName(aMenu.getName());
			hibMenu.setDescr(aMenu.getDescr());
			hibMenu.setParentId(aMenu.getParentId());
			hibMenu.setObjId(aMenu.getObjId());
			hibMenu.setObjParameters(aMenu.getObjParameters());
			hibMenu.setSubObjName(aMenu.getSubObjName());
			hibMenu.setSnapshotName(aMenu.getSnapshotName());
			hibMenu.setSnapshotHistory(aMenu.getSnapshotHistory());
			hibMenu.setFunctionality(aMenu.getFunctionality());
			hibMenu.setInitialPath(aMenu.getInitialPath());
			hibMenu.setViewIcons(new Boolean(aMenu.isViewIcons()));
			hibMenu.setHideToolbar(new Boolean(aMenu.getHideToolbar()));
			hibMenu.setHideSliders(new Boolean(aMenu.getHideSliders()));
			hibMenu.setStaticPage(aMenu.getStaticPage());
			hibMenu.setExternalApplicationUrl(aMenu.getExternalApplicationUrl());

			// manages prog column that determines the menu order
			Query hibQuery = null;
			if (aMenu.getParentId() == null || aMenu.getParentId().intValue() == 0) // hibMenu.setProg(new
																					// Integer(1));
				hibQuery = tmpSession.createQuery("select max(s.prog) from SbiMenu s where s.parentId is null ");
			else {
				// loads sub menu
				// hibQuery =
				// tmpSession.createQuery("select max(s.prog) from SbiMenu s where s.parentId = "
				// + aMenu.getParentId());
				hibQuery = tmpSession.createQuery("select max(s.prog) from SbiMenu s where s.parentId = ?");
				hibQuery.setInteger(0, aMenu.getParentId().intValue());
			}
			Integer maxProg = (Integer) hibQuery.uniqueResult();
			if (maxProg != null)
				hibMenu.setProg(new Integer(maxProg.intValue() + 1));
			else
				hibMenu.setProg(new Integer(1));

			if (aMenu.getIcon() == null) {
				hibMenu.setIcon(null);

			} else {
				hibMenu.setIcon(new Gson().toJson(aMenu.getIcon()).toString());
			}

			if (aMenu.getCustIcon() == null) {
				hibMenu.setCustIcon(null);
			} else {
				hibMenu.setCustIcon(new Gson().toJson(aMenu.getCustIcon()).toString());
			}

			updateSbiCommonInfo4Insert(hibMenu);
			tmpSession.save(hibMenu);
			aMenu.setMenuId(hibMenu.getMenuId());
			aMenu.setParentId(hibMenu.getParentId());
			aMenu.setProg(hibMenu.getProg());
			Set menuRoleToSave = new HashSet();
			Set temp = saveRolesMenu(tmpSession, hibMenu, aMenu);
			menuRoleToSave.addAll(temp);
			// set new roles into sbiFunctions
			hibMenu.setSbiMenuRoles(menuRoleToSave);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
		return aMenu;
	}

	/*
	 * Import Menu
	 */

	@Override
	public Integer importMenu(SbiMenu hibMenu) throws EMFUserError {
		Session tmpSession = null;
		Transaction tx = null;
		Integer menuId = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			this.updateSbiCommonInfo4Update(hibMenu);
			menuId = (Integer) tmpSession.save(hibMenu);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}

		return menuId;
	}

	@Override
	public void updateMenu(SbiMenu hibMenu) throws EMFUserError {
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			this.updateSbiCommonInfo4Update(hibMenu);
			tmpSession.evict(hibMenu);
			tmpSession.update(hibMenu);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
	}

	@Override
	public SbiMenu loadSbiMenubyName(String name) {
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Criterion domainCdCriterrion = Expression.eq("name", name);
			Criteria criteria = tmpSession.createCriteria(SbiMenu.class);
			criteria.add(domainCdCriterrion);
			SbiMenu hibMenu = (SbiMenu) criteria.uniqueResult();

			return hibMenu;
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();

		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		return null;
	}

	@Override
	public void importRoles(List<SbiExtRoles> roles, SbiMenu hibMenu) {

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			for (int k = 0; k < roles.size(); k++) {
				SbiMenuRoleId id = new SbiMenuRoleId(hibMenu.getMenuId(), roles.get(k).getExtRoleId());
				// SbiExtUserRoles useRole = new SbiExtUserRoles(id, us);
				SbiMenuRole menuRole = new SbiMenuRole(id, hibMenu, roles.get(k));
				this.updateSbiCommonInfo4Insert(menuRole);
				tmpSession.save(menuRole);
			}

			// set new roles into sbiFunctions

			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
	}

	/**
	 * Erase menu.
	 *
	 * @param aMenu the a menu
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.wapp.dao.IMenuDAO#eraseMenu(it.eng.spagobi.wapp.bo.Menu)
	 */
	@Override
	public void eraseMenu(Menu aMenu) throws EMFUserError {

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiMenu hibMenu = (SbiMenu) tmpSession.load(SbiMenu.class, aMenu.getMenuId());

			Set oldRoles = hibMenu.getSbiMenuRoles();
			Iterator iterOldRoles = oldRoles.iterator();
			while (iterOldRoles.hasNext()) {
				SbiMenuRole role = (SbiMenuRole) iterOldRoles.next();
				tmpSession.delete(role);
			}
			Integer eventualFatherId = aMenu.getMenuId();
			eraseMenuSons(eventualFatherId, tmpSession);

			// update prog column in other menu
			// String hqlUpdateProg =
			// "update SbiMenu s set s.prog = (s.prog - 1) where s.prog > "
			// + hibMenu.getProg() + " and s.parentId = " +
			// hibMenu.getParentId();

			Integer parentId = hibMenu.getParentId();
			String hqlUpdateProg = null;
			Query query = null;
			if (parentId != null) {
				hqlUpdateProg = "update SbiMenu s set s.prog = (s.prog - 1) where s.prog > ?" + " and s.parentId = ? ";
				query = tmpSession.createQuery(hqlUpdateProg);
				query.setInteger(0, hibMenu.getProg().intValue());
				query.setInteger(1, hibMenu.getParentId().intValue());
			} else {
				hqlUpdateProg = "update SbiMenu s set s.prog = (s.prog - 1) where s.prog > ?" + " and s.parentId = null";
				query = tmpSession.createQuery(hqlUpdateProg);
				query.setInteger(0, hibMenu.getProg().intValue());
			}

			query.executeUpdate();

			tmpSession.delete(hibMenu);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
	}

	private void eraseMenuSons(Integer eventualFatherId, Session tmpSession) throws EMFUserError {

		String getSons = null;
		Query queryD = null;
		if (eventualFatherId != null) {
			getSons = "from SbiMenu s where s.parentId = ?";
			queryD = tmpSession.createQuery(getSons);
			queryD.setInteger(0, eventualFatherId);
		}

		List sons = queryD.list();
		if (sons != null) {
			Iterator it = sons.iterator();
			while (it.hasNext()) {
				SbiMenu toDel = (SbiMenu) it.next();
				eraseMenuSons(toDel.getMenuId(), tmpSession);
				tmpSession.delete(toDel);
				Integer parentId = toDel.getParentId();
				String hqlUpdateProg = null;
				Query query = null;
				if (parentId != null) {
					hqlUpdateProg = "update SbiMenu s set s.prog = (s.prog - 1) where s.prog > ?" + " and s.parentId = ? ";
					query = tmpSession.createQuery(hqlUpdateProg);
					query.setInteger(0, toDel.getProg().intValue());
					query.setInteger(1, toDel.getParentId().intValue());
				} else {
					hqlUpdateProg = "update SbiMenu s set s.prog = (s.prog - 1) where s.prog > ?" + " and s.parentId = null";
					query = tmpSession.createQuery(hqlUpdateProg);
					query.setInteger(0, toDel.getProg().intValue());
				}

				query.executeUpdate();
			}
		}
	}

	/**
	 * Load all menues.
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.wapp.dao.IMenuDAO#loadAllMenues()
	 */
	@Override
	public List loadAllMenues() throws EMFUserError {
		logger.debug("IN");
		Session tmpSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hibQuery = tmpSession.createQuery(" from SbiMenu s order by s.parentId, s.prog");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiMenu hibMenu = (SbiMenu) it.next();
				if (hibMenu != null) {
					Menu biMenu = toMenu(hibMenu, null);
					logger.debug("Add Menu:" + biMenu.getName());
					realResult.add(biMenu);
				}
			}
			// tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
		logger.debug("OUT");
		return realResult;
	}

	@Override
	public List<Menu> loadAllFather() throws EMFUserError {
		logger.debug("IN");
		Session tmpSession = null;
		Transaction tx = null;
		List<Menu> realResult = new ArrayList<>();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hibQuery = tmpSession.createQuery(" from SbiMenu s where s.parentId = null order by s.parentId, s.prog");

			@SuppressWarnings("unchecked")
			List<SbiMenu> hibList = hibQuery.list();
			Iterator<SbiMenu> it = hibList.iterator();
			while (it.hasNext()) {
				SbiMenu hibMenu = it.next();
				if (hibMenu != null) {
					Menu biMenu = toMenu(hibMenu, null);
					logger.debug("Add Menu:" + biMenu.getName());
					realResult.add(biMenu);
				}
			}
			// tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
		logger.debug("OUT");
		return realResult;
	}

	/**
	 * Checks for roles associated.
	 *
	 * @param menuId the menu id
	 *
	 * @return true, if checks for roles associated
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.wapp.dao.IMenuDAO#hasRolesAssociated(java.lang.Integer)
	 */
	@Override
	public boolean hasRolesAssociated(Integer menuId) throws EMFUserError {
		boolean bool = false;

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			// String hql = " from SbiMenuRole s where s.id.menuId = "+ menuId;
			String hql = " from SbiMenuRole s where s.id.menuId = ? ";
			Query aQuery = tmpSession.createQuery(hql);
			aQuery.setInteger(0, menuId.intValue());
			List biFeaturesAssocitedWithMap = aQuery.list();
			if (biFeaturesAssocitedWithMap.size() > 0)
				bool = true;
			else
				bool = false;
			// tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		return bool;

	}

	/**
	 * Gets the children menu.
	 *
	 * @param menuId the menu id
	 * @param roleId the user's role id
	 *
	 * @return the children menu
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.wapp.dao.IMenuDAO#getChildrenMenu(java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Menu> getChildrenMenu(Integer menuId, Integer roleID) throws EMFUserError {
		List<Menu> lstChildren = new ArrayList<>();
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			String hql = " from SbiMenu s where s.id.parentId = ? order by s.prog";

			Query aQuery = tmpSession.createQuery(hql);
			aQuery.setInteger(0, menuId.intValue());

			List<SbiMenu> hibList = aQuery.list();
			Iterator<SbiMenu> it = hibList.iterator();
			while (it.hasNext()) {
				SbiMenu hibMenu = it.next();
				if (hibMenu != null) {
					if (roleID != null) {
						// check if the child can be visualized from the user
						hql = " from SbiMenuRole as mf  where mf.id.menuId = ? and mf.id.extRoleId = ? ";

						aQuery = tmpSession.createQuery(hql);
						aQuery.setInteger(0, hibMenu.getMenuId());
						aQuery.setInteger(1, roleID);

						List<SbiMenuRole> hibListRoles = aQuery.list();
						if (hibListRoles.size() > 0) {
							Menu biMenu = toMenu(hibMenu, roleID);
							lstChildren.add(biMenu);
						}
					}
				} else {
					Menu biMenu = toMenu(hibMenu, roleID);
					lstChildren.add(biMenu);
				}
			}

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		return lstChildren;
	}

	/**
	 * From the Hibernate Menu object at input, gives the corrispondent <code>Menu</code> object.
	 *
	 * @param hibMenu The Hibernate Menu object
	 * @return the corrispondent output <code>Menu</code>
	 */
	private Menu toMenu(SbiMenu hibMenu, Integer roleId) throws EMFUserError {

		Menu menu = new Menu();
		menu.setMenuId(hibMenu.getMenuId());
		menu.setName(hibMenu.getName());
		menu.setDescr(hibMenu.getDescr());
		menu.setParentId(hibMenu.getParentId());
		menu.setObjId(hibMenu.getObjId());
		menu.setObjParameters(hibMenu.getObjParameters());
		menu.setSubObjName(hibMenu.getSubObjName());
		menu.setSnapshotName(hibMenu.getSnapshotName());
		menu.setSnapshotHistory(hibMenu.getSnapshotHistory());
		menu.setFunctionality(hibMenu.getFunctionality());
		menu.setInitialPath(hibMenu.getInitialPath());
		menu.setLevel(getLevel(menu.getParentId(), menu.getObjId()));
		menu.setProg(hibMenu.getProg());

		if (hibMenu.getViewIcons() != null) {
			menu.setViewIcons(hibMenu.getViewIcons().booleanValue());
		} else
			menu.setViewIcons(false);

		if (hibMenu.getHideToolbar() != null) {
			menu.setHideToolbar(hibMenu.getHideToolbar().booleanValue());
		} else
			menu.setHideToolbar(false);

		if (hibMenu.getHideSliders() != null) {
			menu.setHideSliders(hibMenu.getHideSliders().booleanValue());
		} else
			menu.setHideSliders(false);

		menu.setStaticPage(hibMenu.getStaticPage());
		menu.setExternalApplicationUrl(hibMenu.getExternalApplicationUrl());

		MenuIcon icon = null;
		if (hibMenu.getIcon() != null && !hibMenu.getIcon().equals("")) {
			MenuIcon menuIcon = new MenuIcon();
			try {
				JSONObject jsonObject = new JSONObject(hibMenu.getIcon());
				menuIcon.setId(jsonObject.getString("id"));
				menuIcon.setCategory(jsonObject.getString("category"));
				menuIcon.setLabel(jsonObject.getString("label"));
				menuIcon.setClassName(jsonObject.getString("className"));
				menuIcon.setSrc(null);
				menuIcon.setUnicode(null);
				menuIcon.setVisible(jsonObject.getBoolean("visible"));
			} catch (JSONException e) {
				// Error parsing JSON object
				e.printStackTrace();
			}

			icon = menuIcon;
		}
		menu.setIcon(icon);

		MenuIcon custIcon = null;
		if (hibMenu.getCustIcon() != null && !hibMenu.getCustIcon().equals("")) {
			MenuIcon menuIcon = new MenuIcon();
			try {
				JSONObject jsonObject = new JSONObject(new String(hibMenu.getCustIcon()));
				menuIcon.setId(null);
				menuIcon.setCategory(jsonObject.getString("category"));
				menuIcon.setLabel(jsonObject.getString("label"));
				menuIcon.setClassName(jsonObject.getString("className"));
				menuIcon.setSrc(jsonObject.getString("src"));
				// unicode value not used. Set to null because of problems to read
				menuIcon.setUnicode(null);
				menuIcon.setVisible(jsonObject.getBoolean("visible"));
			} catch (JSONException e) {
				// Error parsing JSON object
				e.printStackTrace();
			}
			custIcon = menuIcon;
		}
		menu.setCustIcon(custIcon);

		// set the dephts
		/*
		 * if(menu.getParentId()!=null){ Menu parent=loadMenuByID(menu.getParentId()); if(parent!=null){ Integer depth=parent.getDepth(); menu.setDepth(new
		 * Integer(depth.intValue()+1)); } } else{ menu.setDepth(new Integer(0)); }
		 */

		List rolesList = new ArrayList();
		Set roles = hibMenu.getSbiMenuRoles(); // roles of menu in database
		Iterator iterRoles = roles.iterator();
		while (iterRoles.hasNext()) { // for each role retrieved in database
			SbiMenuRole hibMenuRole = (SbiMenuRole) iterRoles.next();

			SbiExtRoles hibRole = hibMenuRole.getSbiExtRoles();

			RoleDAOHibImpl roleDAO = new RoleDAOHibImpl();
			Role role = roleDAO.toRole(hibRole);

			rolesList.add(role);
		}

		Role[] rolesD = new Role[rolesList.size()];

		for (int i = 0; i < rolesList.size(); i++)
			rolesD[i] = (Role) rolesList.get(i);

		menu.setRoles(rolesD);

		// set children
		try {
			List tmpLstChildren = (DAOFactory.getMenuDAO().getChildrenMenu(menu.getMenuId(), roleId));
			boolean hasCHildren = (tmpLstChildren.size() == 0) ? false : true;
			menu.setLstChildren(tmpLstChildren);
			menu.setHasChildren(hasCHildren);
		} catch (Exception ex) {
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}

		return menu;
	}

	/**
	 * Return the level of menu element: 1 - first, 2 - second|third, 4 - last, 0 other
	 */
	private Integer getLevel(Integer parentId, Integer objId) {
		if ((parentId == null || parentId.intValue() == 0) && objId != null)
			return new Integer("1");
		else if (parentId != null && parentId.intValue() > 0 && objId == null)
			return new Integer("2");
		else if (parentId != null && parentId.intValue() > 0 && objId != null)
			return new Integer("4");
		else
			return new Integer("1");
	}

	/**
	 * Saves all roles for a menu, using session and state information.
	 *
	 *
	 * @param aSession          The current session object
	 * @param hibFunct          The functionality hibernate object
	 * @param aLowFunctionality The Low Functionality object
	 * @return A collection object containing all roles
	 * @throws EMFUserError
	 *
	 */
	private Set saveRolesMenu(Session aSession, SbiMenu hibMenu, Menu aMenu) throws EMFUserError {
		Set menuRoleToSave = new HashSet();

		Criterion domainCdCriterrion = null;
		Criteria criteria = null;
		criteria = aSession.createCriteria(SbiMenuRole.class);
		Role[] roles = null;
		roles = aMenu.getRoles();

		for (int i = 0; i < roles.length; i++) {
			Role role = roles[i];
			domainCdCriterrion = Expression.eq("extRoleId", role.getId());
			criteria = aSession.createCriteria(SbiExtRoles.class);
			criteria.add(domainCdCriterrion);
			SbiExtRoles hibRole = (SbiExtRoles) criteria.uniqueResult();

			SbiMenuRoleId sbiMenuRoleId = new SbiMenuRoleId();
			sbiMenuRoleId.setMenuId(hibMenu.getMenuId());
			sbiMenuRoleId.setExtRoleId(role.getId());

			SbiMenuRole sbiMenuRole = new SbiMenuRole();
			sbiMenuRole.setId(sbiMenuRoleId);
			sbiMenuRole.setSbiMenu(hibMenu);
			sbiMenuRole.setSbiExtRoles(hibRole);
			updateSbiCommonInfo4Insert(sbiMenuRole);
			aSession.save(sbiMenuRole);
			menuRoleToSave.add(sbiMenuRole);
		}
		return menuRoleToSave;
	}

	/**
	 * Substitution between the current node and his father
	 *
	 * @param menuID
	 * @throws EMFUserError
	 */

	@Override
	public void createMasterMenu(Integer menuID) throws EMFUserError {
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiMenu sbiMenu = (SbiMenu) tmpSession.load(SbiMenu.class, menuID);

			Integer fatherId = sbiMenu.getParentId();
			SbiMenu sbiFatherMenu = (SbiMenu) tmpSession.load(SbiMenu.class, fatherId);

			Integer grandFatherId = sbiFatherMenu.getParentId();

			// Change children:

			// get the children of old father, they will point to new father
			Criterion parentCriterrion = Expression.eq("parentId", fatherId);
			Criteria criteria = tmpSession.createCriteria(SbiMenu.class);
			criteria.add(parentCriterrion);

			// Get the list of children from the old father
			List oldFatherChildren = criteria.list();

			// I can retrieve all the children now and save all them
			for (Iterator iterator = oldFatherChildren.iterator(); iterator.hasNext();) {
				SbiMenu sbiMenuO = (SbiMenu) iterator.next();
				sbiMenuO.setParentId(menuID);
			}

			Criterion childCriterrion = Expression.eq("parentId", menuID);
			Criteria childCriteria = tmpSession.createCriteria(SbiMenu.class);
			childCriteria.add(childCriterrion);

			// Get the list of children from the new father
			List newFatherChildren = childCriteria.list();

			for (Iterator iterator = newFatherChildren.iterator(); iterator.hasNext();) {
				SbiMenu sbiMenuO = (SbiMenu) iterator.next();
				if (!(oldFatherChildren.contains(sbiMenuO)))
					sbiMenuO.setParentId(fatherId);
			}

			Integer fatherProg = sbiFatherMenu.getProg();
			Integer menuProg = sbiMenu.getProg();
			sbiMenu.setParentId(grandFatherId);
			sbiMenu.setProg(fatherProg);
			sbiFatherMenu.setParentId(menuID);
			sbiFatherMenu.setProg(menuProg);

			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
	}

	/**
	 * Move up the current folder
	 *
	 * @param menuID
	 * @throws EMFUserError
	 */

	@Override
	public void moveUpMenu(Integer menuID) throws EMFUserError {
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiMenu hibMenu = (SbiMenu) tmpSession.load(SbiMenu.class, menuID);
			Integer oldProg = hibMenu.getProg();
			Integer newProg = new Integer(oldProg.intValue() - 1);
			String upperMenuHql = "";
			Query query = null;
			if (hibMenu.getParentId() == null || hibMenu.getParentId().intValue() == 0) {
				// upperMenuHql = "from SbiMenu s where s.prog = " +
				// newProg.toString() +
				// " and s.parentId is null ";
				upperMenuHql = "from SbiMenu s where s.prog = ? " + " and (s.parentId is null or s.parentId = 0)";
				query = tmpSession.createQuery(upperMenuHql);
				query.setInteger(0, newProg.intValue());
			} else {
				// upperMenuHql = "from SbiMenu s where s.prog = " +
				// newProg.toString() +
				// " and s.parentId = " + hibMenu.getParentId().toString();
				upperMenuHql = "from SbiMenu s where s.prog = ? " + " and s.parentId = ? ";
				query = tmpSession.createQuery(upperMenuHql);
				query.setInteger(0, newProg.intValue());
				query.setInteger(1, hibMenu.getParentId().intValue());
			}

			SbiMenu hibUpperMenu = (SbiMenu) query.uniqueResult();
			if (hibUpperMenu == null) {
				logger.error("The menu with prog [" + newProg + "] does not exist.");
				return;
			}

			hibMenu.setProg(newProg);
			hibUpperMenu.setProg(oldProg);
			updateSbiCommonInfo4Update(hibMenu);
			updateSbiCommonInfo4Update(hibUpperMenu);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Hibernate error: " + he.getMessage());
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} catch (Exception e) {
			logger.error("Error: " + e.getMessage());
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
	}

	/**
	 * Move down the current folder
	 *
	 * @param menuID
	 * @throws EMFUserError
	 */

	@Override
	public void moveDownMenu(Integer menuID) throws EMFUserError {
		Session tmpSession = null;
		Transaction tx = null;
		try {

			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiMenu hibMenu = (SbiMenu) tmpSession.load(SbiMenu.class, menuID);
			Integer oldProg = hibMenu.getProg();
			Integer newProg = new Integer(oldProg.intValue() + 1);

			String upperMenuHql = "";
			Query query = null;
			if (hibMenu.getParentId() == null || hibMenu.getParentId().intValue() == 0) {
				// upperMenuHql = "from SbiMenu s where s.prog = " +
				// newProg.toString() +
				// " and s.parentId is null ";
				upperMenuHql = "from SbiMenu s where s.prog = ? " + " and (s.parentId is null or s.parentId = 0)";
				query = tmpSession.createQuery(upperMenuHql);
				query.setInteger(0, newProg.intValue());
			} else {
				// upperMenuHql = "from SbiMenu s where s.prog = " +
				// newProg.toString() +
				// " and s.parentId = " + hibMenu.getParentId().toString();
				upperMenuHql = "from SbiMenu s where s.prog = ? " + " and s.parentId = ? ";
				query = tmpSession.createQuery(upperMenuHql);
				query.setInteger(0, newProg.intValue());
				query.setInteger(1, hibMenu.getParentId().intValue());
			}
			// Query query = tmpSession.createQuery(upperMenuHql);
			SbiMenu hibUpperMenu = (SbiMenu) query.uniqueResult();
			if (hibUpperMenu == null) {
				logger.error("The menu with prog [" + newProg + "] does not exist.");
				return;
			}

			hibMenu.setProg(newProg);
			hibUpperMenu.setProg(oldProg);
			updateSbiCommonInfo4Update(hibMenu);
			updateSbiCommonInfo4Update(hibUpperMenu);
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
	}

}