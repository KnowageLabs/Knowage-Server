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
package it.eng.spagobi;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.eclipse.persistence.internal.jpa.EJBQueryImpl;

public class TestJPA {

	private static EntityManagerFactory emf;

	static protected Logger logger = Logger.getLogger(TestJPA.class);

	private EntityManagerFactory createEMF() {
		try {

			EntityManagerFactory emf = Persistence.createEntityManagerFactory("foodmart");

			return emf;

		} catch (RuntimeException e) {

			e.printStackTrace();
			return null;
		}
	}

	protected final EntityManagerFactory getEMF() {
		if (emf == null) {
			emf = createEMF();
		}

		return emf;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		TestJPA t = new TestJPA();
		t.createEMF();
		EntityManager em = t.getEMF().createEntityManager();
		String query = " SELECT t_0.storeCost, t_0.storeId.storeCountry FROM  SalesFact1998 t_0 WHERE t_0.storeId.storeCountry='USA' AND  t_0.promotionId=t_0.promotionId";
		Query q = em.createQuery(query);

		List<String> queryParameters = new ArrayList<String>();
		queryParameters.add("USA");
		EJBQueryImpl qi = (EJBQueryImpl) q;
		String sqlQueryString = qi.getDatabaseQuery().getSQLString();
		logger.debug(sqlQueryString);
		EJBQueryImpl countQuery = (EJBQueryImpl) em.createNativeQuery("SELECT COUNT(*) FROM (" + sqlQueryString + ") temp");
		for (int i = 0; i < queryParameters.size(); i++) {
			countQuery.setParameter(1 + i, queryParameters.get(i));
		}
		logger.debug("result " + countQuery.getDatabaseQuery().getSQLString());

		logger.debug(((Long) countQuery.getResultList().get(0)).intValue());

	}

}
