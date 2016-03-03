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
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.kpi.config.bo.MeasureUnit;
import it.eng.spagobi.kpi.config.metadata.SbiMeasureUnit;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;

public class MeasureUnitDAOImpl extends AbstractHibernateDAO implements
IMeasureUnitDAO {

	static private Logger logger = Logger.getLogger(MeasureUnitDAOImpl.class);
	
	public MeasureUnit loadMeasureUnitById(Integer id) throws EMFUserError {
		logger.debug("IN");
		MeasureUnit toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiMeasureUnit hibMeasureUnit = (SbiMeasureUnit) aSession.load(
					SbiMeasureUnit.class, id);
			toReturn = new MeasureUnit();
			toReturn.setId(hibMeasureUnit.getIdMeasureUnit());
			toReturn.setName(hibMeasureUnit.getName());
			toReturn.setScaleCd(hibMeasureUnit.getScaleCd());
			toReturn.setScaleNm(hibMeasureUnit.getScaleNm());
			toReturn.setScaleTypeId(hibMeasureUnit.getScaleType().getValueId());
			
		} catch (HibernateException he) {
			logger.error("Error while loading the MeasureUnit with id "
					+ ((id == null) ? "" : id.toString()), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10101);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return toReturn;
	}
	
	
	
	public MeasureUnit loadMeasureUnitByCd(String cd) throws EMFUserError {
		logger.debug("IN");
		MeasureUnit toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion labelCriterrion = Expression.eq("scaleCd",cd);
			Criteria criteria = aSession.createCriteria(SbiMeasureUnit.class);
			criteria.add(labelCriterrion);
			SbiMeasureUnit hibMu = (SbiMeasureUnit) criteria.uniqueResult();
		
			
			toReturn = new MeasureUnit();
			toReturn.setId(hibMu.getIdMeasureUnit());
			toReturn.setName(hibMu.getName());
			toReturn.setScaleCd(hibMu.getScaleCd());
			toReturn.setScaleNm(hibMu.getScaleNm());
			toReturn.setScaleTypeId(hibMu.getScaleType().getValueId());
			
		} catch (HibernateException he) {
			logger.error("Error while loading the MeasureUnit with id "+ cd , he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10101);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return toReturn;
	}

	
}
