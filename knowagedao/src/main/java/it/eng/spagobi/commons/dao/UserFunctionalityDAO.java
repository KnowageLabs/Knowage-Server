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

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiOrganizationProductType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserFunctionalityDAO extends AbstractHibernateDAO implements IUserFunctionalityDAO {

	static private Logger logger = Logger.getLogger(UserFunctionalityDAO.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.commons.dao.IUserFunctionalityDAO#readUserFunctionality (java.lang.String[])
	 */
	@Override
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
			List<String> roleTypesTenant = new ArrayList();

			for (int i = 0; i < roles.length; i++) {
				String hql = "from SbiExtRoles ser where ser.name=?";
				Query query = aSession.createQuery(hql);
				query.setParameter(0, roles[i]);
				logger.debug("Read role of=" + roles[i]);
				SbiExtRoles spaobiRole = (SbiExtRoles) query.uniqueResult();
				if (spaobiRole != null) {
					String roleTypeCode = spaobiRole.getRoleType().getValueCd();
					String roleTypeTenant = spaobiRole.getCommonInfo().getOrganization();
					if (!roleTypes.contains(roleTypeCode)) {
						roleTypes.add(roleTypeCode);
					}
					if (!roleTypesTenant.contains(roleTypeTenant)) {
						roleTypesTenant.add(roleTypeTenant);
					}
				} else {
					logger.warn("The role " + roles[i] + "doesn't exist in SBI_EXT_ROLES");
				}
			}
			logger.debug("Role type=" + roleTypes);
			if (roleTypes.size() == 0)
				logger.warn("No role types found for the user...!!!!!");

			// ADDED
			// Get corresponding Product Type Id for roles'tenants
			Set<Integer> productTypesId = new HashSet<Integer>();
			for (String tenant : roleTypesTenant) {
				String hql = "from SbiOrganizationProductType opt where opt.commonInfo.organization=?";
				Query query = aSession.createQuery(hql);
				query.setParameter(0, tenant);
				List productTypes = query.list();
				Iterator iter = productTypes.iterator();
				while (iter.hasNext()) {
					SbiOrganizationProductType sbiOrganizationProductType = (SbiOrganizationProductType) iter.next();
					productTypesId.add(sbiOrganizationProductType.getSbiProductType().getProductTypeId());
				}
			}

			if (productTypesId.isEmpty()) {
				logger.error("No Product Types found for the user!!!");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			}

			// String hql =
			// "from SbiRolesUserFunctionality suf where suf.userFunctionality.domainCd = 'USER_FUNCTIONALITY'"
			// +
			// " and suf.roleType.valueCd in ("+strRoles+")";
			// String hql =
			// "Select distinct suf.name from SbiUserFunctionality suf where suf.roleType.valueCd in ("+strRoles+") and suf.roleType.domainCd='ROLE_TYPE'";
			String hql = "Select distinct suf.name from SbiUserFunctionality suf left join suf.roleType rt " + "where rt.valueCd IN (:ROLE_TYPES) "
					+ " and rt.domainCd='ROLE_TYPE'" + " and suf.productType.productTypeId IN (:PRODUCT_TYPES) ";
			Query query = aSession.createQuery(hql);
			query.setParameterList("ROLE_TYPES", roleTypes);
			query.setParameterList("PRODUCT_TYPES", productTypesId);
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
