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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import it.eng.spagobi.commons.metadata.SbiProductType;
import it.eng.spagobi.commons.metadata.SbiProductTypeEngine;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class ProductTypeDAOHibImpl extends AbstractHibernateDAO implements IProductTypeDAO {
	private static Logger logger = Logger.getLogger(ProductTypeDAOHibImpl.class);

	@Override
	public List<SbiProductType> loadAllProductType() {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String q = "from SbiProductType";
			Query query = aSession.createQuery(q);
			ArrayList<SbiProductType> result = (ArrayList<SbiProductType>) query.list();
			return result;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			rollbackIfActive(tx);
			throw new SpagoBIRuntimeException("Error getting product types", he);
		} finally {
			closeSessionIfOpen(aSession);
			logger.debug("OUT");
		}
	}

	@Override
	public List<SbiProductTypeEngine> loadSelectedEngines(String productType) {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession
					.createQuery("from SbiProductTypeEngine pe where pe.sbiProductType.label = :productTypeLabel");
			hibQuery.setString("productTypeLabel", productType);
			ArrayList<SbiProductTypeEngine> result = (ArrayList<SbiProductTypeEngine>) hibQuery.list();
			return result;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			rollbackIfActive(tx);
			throw new SpagoBIRuntimeException("Error getting Product type Engines", he);
		} finally {
			closeSessionIfOpen(aSession);
			logger.debug("OUT");
		}
	}

	@Override
	public List loadOrganizzationEngines(final String tenant) {

		List orgEngs = list(new ICriterion() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiEngines.class)
						.createAlias("sbiProductTypeEngine", "_sbiProductTypeEngine")
						.createAlias("_sbiProductTypeEngine.sbiProductType", "_sbiProductType")
						.createAlias("_sbiProductType.sbiOrganizationProductType", "_sbiOrganizationProductType")
						.createAlias("_sbiOrganizationProductType.sbiOrganizations", "_sbiOrganizations")
						.add(Restrictions.eq("_sbiOrganizations.name", tenant))
						.setProjection(Projections.projectionList()
								.add(org.hibernate.criterion.Property.forName("label").as("engineLabel"))
								.add(org.hibernate.criterion.Property.forName("_sbiProductType.label")
										.as("productTypeLabel")))
						.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			}
		});
		return orgEngs;
	}

	@Override
	public List<String> loadOrganizzationProductTypeEngines(final String tenant, final String productTypeLabel) {

		List<String> orgEngs = list(new ICriterion() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiEngines.class)
						.createAlias("sbiProductTypeEngine", "_sbiProductTypeEngine")
						.createAlias("_sbiProductTypeEngine.sbiProductType", "_sbiProductType")
						.createAlias("_sbiProductType.sbiOrganizationProductType", "_sbiOrganizationProductType")
						.createAlias("_sbiOrganizationProductType.sbiOrganizations", "_sbiOrganizations")
						.add(Restrictions.eq("_sbiOrganizations.name", tenant))
						.add(Restrictions.eq("_sbiProductType.label", productTypeLabel))
						.setProjection(Projections.projectionList()
								.add(org.hibernate.criterion.Property.forName("label").as("engineLabel")));
			}
		});
		return orgEngs;

	}

	@Override
	public List<String> loadCurrentTenantProductTypes() {

		List<String> orgEngs = list(new ICriterion() {
			@Override
			public Criteria evaluate(Session session) {
				Criteria criteria = session.createCriteria(SbiProductType.class);
				criteria.createAlias("sbiOrganizationProductType", "_sbiOrganizationProductType");
				criteria.createAlias("_sbiOrganizationProductType.sbiOrganizations", "_sbiOrganizations");
				criteria.add(Restrictions.eq("_sbiOrganizations.name", getTenant()));
				return criteria.setProjection(Projections.projectionList()
						.add(org.hibernate.criterion.Property.forName("label").as("productLabel")));
			}
		});
		return orgEngs;

	}

}
