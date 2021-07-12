/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.
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

/**
 *
 */
package it.eng.spagobi.functions.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.functions.metadata.SbiFunctionOutputColumn;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * @author albnale
 *
 */
public class FunctionOutputColumnDAOImpl extends AbstractHibernateDAO implements IFunctionOutputColumnDAO {

	static private Logger logger = Logger.getLogger(FunctionOutputColumnDAOImpl.class);

	@Override
	public List<SbiFunctionOutputColumn> loadFunctionOutputColumnByFunctionUuid(String functionUuid) {

		logger.debug("IN");
		Session session = null;
		List<SbiFunctionOutputColumn> sbiFunctionOutputColumnList = null;
		try {
			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");

			StringBuilder query = new StringBuilder();
			query.append("from SbiFunctionOutputColumn sfiv ");
			query.append("where sfiv.sbiCatalogFunction.functionUuid =" + functionUuid);

			Query hibQuery = session.createQuery(query.toString());

			sbiFunctionOutputColumnList = hibQuery.list();
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An error occured while reading Function output columns from DB", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return sbiFunctionOutputColumnList;

	}
}
