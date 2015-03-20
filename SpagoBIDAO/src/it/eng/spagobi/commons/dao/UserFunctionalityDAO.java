/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.metadata.SbiExtRoles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserFunctionalityDAO extends AbstractHibernateDAO implements
		IUserFunctionalityDAO {

	static private Logger logger = Logger.getLogger(UserFunctionalityDAO.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spagobi.commons.dao.IUserFunctionalityDAO#readUserFunctionality
	 * (java.lang.String[])
	 */
	public String[] readUserFunctionality(String[] roles) throws Exception {
		logger.debug("IN");
		if (roles == null || roles.length == 0) {
			logger.warn("The array of roles is empty...");
			return new String[0];
		}
		ArrayList toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			List roleTypes = new ArrayList();

			for (int i = 0; i < roles.length; i++) {
				String hql = "from SbiExtRoles ser where ser.name=?";
				Query query = aSession.createQuery(hql);
				query.setParameter(0, roles[i]);
				logger.debug("Read role of=" + roles[i]);
				SbiExtRoles spaobiRole = (SbiExtRoles) query.uniqueResult();
				if (spaobiRole != null) {
					String roleTypeCode = spaobiRole.getRoleType().getValueCd();
					if (!roleTypes.contains(roleTypeCode))
						roleTypes.add(roleTypeCode);
				} else {
					logger.warn("The role " + roles[i]
							+ "doesn't exist in SBI_EXT_ROLES");
				}
			}
			logger.debug("Role type=" + roleTypes);
			if (roleTypes.size() == 0)
				logger.warn("No role types found for the user...!!!!!");

			// String hql =
			// "from SbiRolesUserFunctionality suf where suf.userFunctionality.domainCd = 'USER_FUNCTIONALITY'"
			// +
			// " and suf.roleType.valueCd in ("+strRoles+")";
			// String hql =
			// "Select distinct suf.name from SbiUserFunctionality suf where suf.roleType.valueCd in ("+strRoles+") and suf.roleType.domainCd='ROLE_TYPE'";
			String hql = "Select distinct suf.name from SbiUserFunctionality suf left join suf.roleType rt "
					+ "where rt.valueCd IN (:ROLE_TYPES) "
					+ "and rt.domainCd='ROLE_TYPE'";
			Query query = aSession.createQuery(hql);
			query.setParameterList("ROLE_TYPES", roleTypes);
			List userFuncList = query.list();
			Iterator iter = userFuncList.iterator();
			while (iter.hasNext()) {
				String tmp = (String) iter.next();
				toReturn.add(tmp);
				logger.debug("Add Functionality=" + tmp);
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException during query", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}
		String[] ris = new String[toReturn.size()];
		toReturn.toArray(ris);
		return ris;

	}

}
