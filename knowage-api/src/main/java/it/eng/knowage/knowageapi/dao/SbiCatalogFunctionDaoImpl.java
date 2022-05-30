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
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.dao.AbstractDaoImpl;
import it.eng.knowage.boot.error.KnowageBusinessException;
import it.eng.knowage.knowageapi.dao.dto.SbiCatalogFunction;

/**
 * @author Marco Libanori
 */
@Component
public class SbiCatalogFunctionDaoImpl extends AbstractDaoImpl implements SbiCatalogFunctionDao {

	@Autowired
	private BusinessRequestContext businessRequestContext;

	@PersistenceContext(unitName = "knowage-functioncatalog")
	private EntityManager em;

	@Override
	public List<SbiCatalogFunction> findAll(String searchStr) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SbiCatalogFunction> q = cb.createQuery(SbiCatalogFunction.class);
		Root<SbiCatalogFunction> root = q.from(SbiCatalogFunction.class);

		q = q.select(root);

		Path<String> organizationCol = root.get("id").get("organization");

		Predicate searchPredicate = null;
		if (isNotEmpty(searchStr)) {
			Path<String> nameCol = root.get("name");
			Path<String> labelCol = root.get("label");

			String val = String.format("%%%s%%", searchStr);

			Predicate nameLike = cb.like(nameCol, val);
			Predicate labelLike = cb.like(labelCol, val);

			searchPredicate = cb.or(nameLike, labelLike);

		} else {
			searchPredicate = cb.and();
		}

		Predicate organizationEquals = cb.equal(organizationCol, businessRequestContext.getOrganization());

		q = q.where(cb.and(organizationEquals, searchPredicate));

		Query query = em.createQuery(q);
		List<SbiCatalogFunction> ret = query.getResultList();

		return ret;
	}

	@Override
	public SbiCatalogFunction find(SbiCatalogFunction.Pk id) {

		id.setOrganization(businessRequestContext.getOrganization());

		return em.find(SbiCatalogFunction.class, id);
	}

	@Override
	public void delete(SbiCatalogFunction function) throws KnowageBusinessException {

		function.getId().setOrganization(businessRequestContext.getOrganization());

		preDelete(function);
		function.getInputColumns().forEach(this::preDelete);
		function.getInputVariables().forEach(this::preDelete);
		function.getOutputColumns().forEach(this::preDelete);

		if (!function.getObjFunctions().isEmpty()) {
			throw new KnowageBusinessException("Function with id " + function.getId() + " cannot be deleted because it's referenced by other objects");
		}

		em.remove(function);
	}

	@Override
	public SbiCatalogFunction update(SbiCatalogFunction function) {

		function.getId().setOrganization(businessRequestContext.getOrganization());

		preUpdate(function);
		function.getInputColumns().forEach(this::preInsert);
		function.getInputVariables().forEach(this::preInsert);
		function.getOutputColumns().forEach(this::preInsert);

		function.getInputColumns().forEach(this::preUpdate);
		function.getInputVariables().forEach(this::preUpdate);
		function.getOutputColumns().forEach(this::preUpdate);

		em.merge(function);

		return function;
	}

	@Override
	public SbiCatalogFunction create(SbiCatalogFunction function) {

		function.getId().setOrganization(businessRequestContext.getOrganization());

		preInsert(function);
		function.getInputColumns().forEach(this::preInsert);
		function.getInputVariables().forEach(this::preInsert);
		function.getOutputColumns().forEach(this::preInsert);

		em.persist(function);

		return function;
	}

}
