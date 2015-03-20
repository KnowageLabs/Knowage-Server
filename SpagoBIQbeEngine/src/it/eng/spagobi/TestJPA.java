/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.eclipse.persistence.internal.jpa.EJBQueryImpl;

public class TestJPA {

	private static EntityManagerFactory emf;
	
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
		
		TestJPA t= new TestJPA();
		t.createEMF();
		EntityManager em = t.getEMF().createEntityManager();
		String query = " SELECT t_0.storeCost, t_0.storeId.storeCountry FROM  SalesFact1998 t_0 WHERE t_0.storeId.storeCountry='USA' AND  t_0.promotionId=t_0.promotionId";
		Query q  = em.createQuery(query);
		
		
		List<String> queryParameters = new ArrayList<String>();
		queryParameters.add("USA");
		EJBQueryImpl qi = (EJBQueryImpl)q;
		String sqlQueryString = qi.getDatabaseQuery().getSQLString();
		System.out.println(sqlQueryString);
		EJBQueryImpl countQuery = (EJBQueryImpl)em.createNativeQuery("SELECT COUNT(*) FROM (" + sqlQueryString + ") temp");
		for(int i=0; i<queryParameters.size(); i++ ){
			countQuery.setParameter(1+i, queryParameters.get(i));
		}
		System.out.println("result "+countQuery.getDatabaseQuery().getSQLString());


		System.out.println(((Long)countQuery.getResultList().get(0)).intValue());

	}

}
