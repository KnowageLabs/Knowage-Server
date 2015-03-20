/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.i18n.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.i18n.metadata.SbiI18NMessages;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class I18NMessagesDAOHibImpl extends AbstractHibernateDAO implements
I18NMessagesDAO {

	static private Logger logger = Logger
	.getLogger(I18NMessagesDAOHibImpl.class);

	public String getI18NMessages(Locale locale, String code) throws EMFUserError {
		logger.debug("IN. code="+code);
		String toReturn = null;

		Session aSession = null;
		Transaction tx = null;

		if(locale == null){
			logger.warn("No I18n conversion because locale passed as parameter is null");
			return code;
		}

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			String qDom = "from SbiDomains dom where dom.valueCd = :valueCd AND dom.domainCd = 'LANG'";
			Query queryDom = aSession.createQuery(qDom);
			String localeId = locale.getISO3Language().toUpperCase();
			logger.debug("localeId="+localeId);
			queryDom.setString("valueCd", localeId);
			Object objDom = queryDom.uniqueResult();
			if(objDom == null){
				logger.error("Could not find domain for locale "+locale.getISO3Language());	
				return code;				
			}
			Integer domId = ((SbiDomains)objDom).getValueId();
			
			String q = "from SbiI18NMessages att where att.id.languageCd = :languageCd AND att.id.label = :label";
			Query query = aSession.createQuery(q);
	
			query.setInteger("languageCd", domId);
			query.setString("label", code);

			Object obj = query.uniqueResult();
			if(obj != null){
				SbiI18NMessages SbiI18NMessages = (SbiI18NMessages)obj;
				toReturn = SbiI18NMessages.getMessage();
			}

			tx.commit();
		}
		catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT.toReturn="+toReturn);
		return toReturn;
	}




}
