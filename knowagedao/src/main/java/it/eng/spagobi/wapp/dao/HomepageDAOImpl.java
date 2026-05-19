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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.wapp.bo.Homepage;
import it.eng.spagobi.wapp.bo.HomepageTemplate;
import it.eng.spagobi.wapp.bo.MenuPlaceholder;
import it.eng.spagobi.wapp.metadata.SbiHomepage;

public class HomepageDAOImpl extends AbstractHibernateDAO implements IHomepageDAO {

	private static final Logger LOGGER = Logger.getLogger(HomepageDAOImpl.class);

	@Override
	public Homepage saveHomepage(Homepage homepage) throws EMFUserError {
		LOGGER.debug("IN");
		Session session = null;
		Transaction tx = null;
		try {
			session = getSession();
			tx = session.beginTransaction();

			SbiHomepage hibHomepage;
			boolean insert = homepage.getId() == null;
			if (insert) {
				hibHomepage = new SbiHomepage();
			} else {
				hibHomepage = (SbiHomepage) session.get(SbiHomepage.class, homepage.getId());
				if (hibHomepage == null) {
					throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
				}
			}

			applyHomepage(session, homepage, hibHomepage);

			if (Boolean.TRUE.equals(hibHomepage.getDefaultHomepage())) {
				clearExistingDefaultHomepage(session, hibHomepage.getId());
			}

			if (insert) {
				updateSbiCommonInfo4Insert(hibHomepage);
				session.save(hibHomepage);
			} else {
				updateSbiCommonInfo4Update(hibHomepage);
				session.saveOrUpdate(hibHomepage);
			}

			tx.commit();
			return toHomepage(hibHomepage);
		} catch (HibernateException | JSONException e) {
			LOGGER.error("Error while saving homepage", e);
			if (tx != null) {
				tx.rollback();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			LOGGER.debug("OUT");
		}
	}

	@Override
	public Homepage loadHomepageByRoleName(String roleName) throws EMFUserError {
		LOGGER.debug("IN");
		Session session = null;
		try {
			session = getSession();
			Integer homepageId = loadHomepageIdByRoleName(session, roleName);
			SbiHomepage hibHomepage = loadSbiHomepageById(session, homepageId);
			if (hibHomepage != null) {
				return toHomepage(hibHomepage);
			}
			return loadDefaultHomepage(session);
		} catch (HibernateException | JSONException e) {
			LOGGER.error("Error while loading homepage by role name", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			LOGGER.debug("OUT");
		}
	}

	@Override
	public Homepage loadDefaultHomepage() throws EMFUserError {
		LOGGER.debug("IN");
		Session session = null;
		try {
			session = getSession();
			return loadDefaultHomepage(session);
		} catch (HibernateException | JSONException e) {
			LOGGER.error("Error while loading default homepage", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			LOGGER.debug("OUT");
		}
	}

	private Homepage loadDefaultHomepage(Session session) throws JSONException {
		Query query = session.createQuery("from SbiHomepage h where h.defaultHomepage = true and h.commonInfo.timeDe is null");
		query.setMaxResults(1);
		SbiHomepage hibHomepage = (SbiHomepage) query.uniqueResult();
		return hibHomepage == null ? null : toHomepage(hibHomepage);
	}

	private void applyHomepage(Session session, Homepage homepage, SbiHomepage hibHomepage) throws EMFUserError, JSONException {
		hibHomepage.setType(homepage.getType());
		hibHomepage.setDocument(loadDocument(session, homepage.getDocumentId()));
		hibHomepage.setImageUrl(trimToNull(homepage.getImageUrl()));
		hibHomepage.setStaticPage(trimToNull(homepage.getStaticPage()));
		hibHomepage.setDefaultHomepage(homepage.isDefaultHomepage());
		hibHomepage.setHtml(trimToNull(homepage.getTemplate() != null ? homepage.getTemplate().getHtml() : null));
		hibHomepage.setCss(trimToNull(homepage.getTemplate() != null ? homepage.getTemplate().getCss() : null));
		hibHomepage.setMenuPlaceholders(serializeMenuPlaceholders(homepage.getTemplate()));

		List<String> roleNames = homepage.isDefaultHomepage()
				? new ArrayList<>()
				: homepage.getRoleNames().stream().distinct().collect(Collectors.toList());
		if (!roleNames.isEmpty()) {
			clearRoleAssignmentsOnOtherHomepages(session, roleNames, hibHomepage.getId());
		}
		hibHomepage.setSbiHomepageRoles(loadRoles(session, roleNames));
	}

	private void clearExistingDefaultHomepage(Session session, Integer currentHomepageId) {
		Query query = session.createQuery("from SbiHomepage h where h.defaultHomepage = true and h.commonInfo.timeDe is null");
		List<SbiHomepage> homepages = query.list();
		for (SbiHomepage homepage : homepages) {
			if (currentHomepageId != null && currentHomepageId.equals(homepage.getId())) {
				continue;
			}
			homepage.setDefaultHomepage(false);
			updateSbiCommonInfo4Update(homepage);
			session.saveOrUpdate(homepage);
		}
	}

	private void clearRoleAssignmentsOnOtherHomepages(Session session, List<String> roleNames, Integer currentHomepageId) {
		List<SbiHomepage> homepages = loadHomepagesByRoleNames(session, roleNames);
		for (SbiHomepage homepage : homepages) {
			if (currentHomepageId != null && currentHomepageId.equals(homepage.getId())) {
				continue;
			}
			Set<SbiExtRoles> filteredRoles = homepage.getSbiHomepageRoles().stream()
					.filter(role -> !roleNames.contains(role.getName()))
					.collect(Collectors.toSet());
			if (filteredRoles.size() != homepage.getSbiHomepageRoles().size()) {
				homepage.setSbiHomepageRoles(filteredRoles);
				updateSbiCommonInfo4Update(homepage);
				session.saveOrUpdate(homepage);
			}
		}
	}

	private Integer loadHomepageIdByRoleName(Session session, String roleName) {
		Query query = session.createQuery("select distinct h.id from SbiHomepage h join h.sbiHomepageRoles r "
				+ "where r.name = :roleName and h.commonInfo.timeDe is null");
		query.setString("roleName", roleName);
		query.setMaxResults(1);
		return (Integer) query.uniqueResult();
	}

	List<SbiHomepage> loadHomepagesByRoleNames(Session session, List<String> roleNames) {
		if (roleNames == null || roleNames.isEmpty()) {
			return Collections.emptyList();
		}
		Query query = session.createQuery("select distinct h.id from SbiHomepage h join h.sbiHomepageRoles r "
				+ "where r.name in (:roleNames) and h.commonInfo.timeDe is null");
		query.setParameterList("roleNames", roleNames);
		List<Integer> homepageIds = query.list();
		return loadHomepagesByIds(session, homepageIds);
	}

	private SbiHomepage loadSbiHomepageById(Session session, Integer homepageId) {
		if (homepageId == null) {
			return null;
		}
		Query query = session.createQuery("from SbiHomepage h where h.id = :homepageId and h.commonInfo.timeDe is null");
		query.setInteger("homepageId", homepageId);
		return (SbiHomepage) query.uniqueResult();
	}

	private List<SbiHomepage> loadHomepagesByIds(Session session, List<Integer> homepageIds) {
		if (homepageIds == null || homepageIds.isEmpty()) {
			return Collections.emptyList();
		}
		Query query = session.createQuery("from SbiHomepage h where h.id in (:homepageIds) and h.commonInfo.timeDe is null");
		query.setParameterList("homepageIds", homepageIds);
		return query.list();
	}

	Set<SbiExtRoles> loadRoles(Session session, List<String> roleNames) throws EMFUserError {
		if (roleNames == null || roleNames.isEmpty()) {
			return new LinkedHashSet<>();
		}

		Query query = session.createQuery("from SbiExtRoles r where r.name in (:roleNames)");
		query.setParameterList("roleNames", roleNames);
		List<SbiExtRoles> loadedRoles = query.list();

		Map<String, SbiExtRoles> rolesByName = new HashMap<>();
		for (SbiExtRoles role : loadedRoles) {
			rolesByName.put(role.getName(), role);
		}

		Set<SbiExtRoles> roles = new LinkedHashSet<>();
		for (String roleName : roleNames) {
			SbiExtRoles role = rolesByName.get(roleName);
			if (role == null) {
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			}
			roles.add(role);
		}
		return roles;
	}

	private SbiObjects loadDocument(Session session, Integer documentId) throws EMFUserError {
		if (documentId == null) {
			return null;
		}
		SbiObjects document = (SbiObjects) session.get(SbiObjects.class, documentId);
		if (document == null) {
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		return document;
	}

	private Homepage toHomepage(SbiHomepage hibHomepage) throws JSONException {
		Homepage homepage = new Homepage();
		homepage.setId(hibHomepage.getId());
		homepage.setDefaultHomepage(Boolean.TRUE.equals(hibHomepage.getDefaultHomepage()));
		homepage.setType(hibHomepage.getType());
		homepage.setDocumentId(hibHomepage.getDocument() != null ? hibHomepage.getDocument().getBiobjId() : null);
		homepage.setImageUrl(hibHomepage.getImageUrl());
		homepage.setStaticPage(hibHomepage.getStaticPage());

		HomepageTemplate template = new HomepageTemplate();
		template.setHtml(hibHomepage.getHtml());
		template.setCss(hibHomepage.getCss());
		template.setMenuPlaceholders(deserializeMenuPlaceholders(hibHomepage.getMenuPlaceholders()));
		if (template.getHtml() != null || template.getCss() != null || !template.getMenuPlaceholders().isEmpty()) {
			homepage.setTemplate(template);
		}

		List<String> roleNames = hibHomepage.getSbiHomepageRoles().stream()
				.map(SbiExtRoles::getName)
				.collect(Collectors.toList());
		homepage.setRoleNames(roleNames);
		return homepage;
	}

	private String serializeMenuPlaceholders(HomepageTemplate template) throws JSONException {
		if (template == null || template.getMenuPlaceholders() == null || template.getMenuPlaceholders().isEmpty()) {
			return null;
		}
		JSONArray placeholders = new JSONArray();
		for (MenuPlaceholder placeholder : template.getMenuPlaceholders()) {
			JSONObject placeholderObject = new JSONObject();
			placeholderObject.put("index", placeholder.getIndex());
			JSONArray menuIds = new JSONArray();
			for (Integer menuId : placeholder.getMenuIds()) {
				menuIds.put(menuId);
			}
			placeholderObject.put("menuIds", menuIds);
			placeholders.put(placeholderObject);
		}
		return placeholders.toString();
	}

	private List<MenuPlaceholder> deserializeMenuPlaceholders(String menuPlaceholders) throws JSONException {
		List<MenuPlaceholder> placeholders = new ArrayList<>();
		if (menuPlaceholders == null || menuPlaceholders.trim().isEmpty()) {
			return placeholders;
		}
		JSONArray jsonArray = new JSONArray(menuPlaceholders);
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject placeholderObject = jsonArray.getJSONObject(i);
			MenuPlaceholder placeholder = new MenuPlaceholder();
			placeholder.setIndex(placeholderObject.getInt("index"));
			JSONArray menuIds = placeholderObject.optJSONArray("menuIds");
			List<Integer> ids = new ArrayList<>();
			if (menuIds != null) {
				for (int j = 0; j < menuIds.length(); j++) {
					ids.add(menuIds.getInt(j));
				}
			}
			placeholder.setMenuIds(ids);
			placeholders.add(placeholder);
		}
		return placeholders;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

}
