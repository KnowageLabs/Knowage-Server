/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

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
package it.eng.knowage.knowageapi.dao;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import it.eng.knowage.knowageapi.context.BusinessRequestContext;
import it.eng.knowage.knowageapi.dao.dto.SbiCatalogFunction;

/**
 * @author Marco Libanori
 */
@Component
public class SbiCatalogFunctionDaoImpl implements SbiCatalogFunctionDao {

	@Autowired
	private BusinessRequestContext business;

	@Autowired
	@Qualifier("knowage-functioncatalog")
	private EntityManager em;

	@Override
	public List<SbiCatalogFunction> findAll(String searchStr) {

		init();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SbiCatalogFunction> q = cb.createQuery(SbiCatalogFunction.class);
		Root<SbiCatalogFunction> root = q.from(SbiCatalogFunction.class);

		Path<String> nameCol = root.get("name");

		q = q.select(root);

		if (isNotEmpty(searchStr)) {
			String val = String.format("%%%s%%", searchStr);

			q = q.where(cb.like(nameCol, val));
		}

		Query query = em.createQuery(q);
		List<SbiCatalogFunction> ret = query.getResultList();

		return ret;
	}

	@Override
	public SbiCatalogFunction find(String id) {
		return em.find(SbiCatalogFunction.class, id);
	}

	@Override
	public void delete(String id) {
		init();

		SbiCatalogFunction function = em.find(SbiCatalogFunction.class, id);

		em.remove(function);
	}

	@Override
	public SbiCatalogFunction update(SbiCatalogFunction function) {
		// TODO
		return null;
	}

	private void init() {
		Session session = em.unwrap(Session.class);
		Filter filter = session.enableFilter("organization");
		filter.setParameter("organization", business.getOrganization());
	}

	@Override
	public SbiCatalogFunction create(SbiCatalogFunction function) {
		em.persist(function);
		return function;
	}

}
