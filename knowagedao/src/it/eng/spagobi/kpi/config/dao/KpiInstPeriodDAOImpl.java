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
package it.eng.spagobi.kpi.config.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.kpi.config.bo.KpiInstPeriod;
import it.eng.spagobi.kpi.config.metadata.SbiKpiInstPeriod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class KpiInstPeriodDAOImpl extends AbstractHibernateDAO implements
IKpiInstPeriodDAO {

	static private Logger logger = Logger.getLogger(KpiInstPeriodDAOImpl.class);
	
	
	public List loadKpiInstPeriodId(Integer kpiInstId)
			throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiKpiInstPeriod where sbiKpiInstance = " + kpiInstId);
			
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				KpiInstPeriod kpiInstPeriod=toKpiInstPeriod((SbiKpiInstPeriod) it.next());
				realResult.add(kpiInstPeriod);
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading all kpi Inst Period ", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();				
			}
		}
		logger.debug("OUT");
		return realResult;
	}
	
	
	private KpiInstPeriod toKpiInstPeriod(SbiKpiInstPeriod sbiK) {
		logger.debug("IN");

		 KpiInstPeriod toReturn = new KpiInstPeriod();

		Integer id=sbiK.getKpiInstPeriodId();
		
		Integer kpiInstId=null;
		if(sbiK.getSbiKpiInstance()!=null){
			kpiInstId=sbiK.getSbiKpiInstance().getIdKpiInstance();
		}

		Integer periodId=null;
		if(sbiK.getSbiKpiPeriodicity()!=null){
			periodId=sbiK.getSbiKpiPeriodicity().getIdKpiPeriodicity();
		}
		
		Boolean defaultValue=sbiK.isDefault_();
		
		
		toReturn.setId(id);
		toReturn.setKpiInstId(kpiInstId);
		toReturn.setPeriodicityId(periodId);
		toReturn.setDefaultValue(defaultValue);

		logger.debug("OUT");
		return toReturn;
	}

}
