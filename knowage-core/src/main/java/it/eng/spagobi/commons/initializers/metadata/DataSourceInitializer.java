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
package it.eng.spagobi.commons.initializers.metadata;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasource;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasourceId;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Alberto Nale
 * @since 2020/03/31
 */
public class DataSourceInitializer extends SpagoBIInitializer {

	static private Logger logger = Logger.getLogger(DataSourceInitializer.class);

	public DataSourceInitializer() {
		targetComponentName = "DataSource";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/dataSource.xml";
	}

	@Override
	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
			String hql = "from SbiDataSource";
			Query hqlQuery = hibernateSession.createQuery(hql);
			List dataSources = hqlQuery.list();
			if (dataSources.isEmpty()) {
				logger.info("DataSource table is empty. Starting populating DataSources...");
				writeDefaultDatasources(hibernateSession);
			} else {
				logger.debug("DataSource table is already populated. No operations needed.");
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while initializing DataSources", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private void writeDefaultDatasources(Session aSession) throws Exception {
		logger.debug("IN");
		SourceBean dataSourcesSB = getConfiguration();
		if (dataSourcesSB == null) {
			throw new Exception("DataSources configuration file not found!!!");
		}
		List dataSourcesList = dataSourcesSB.getAttributeAsList("DATASOURCE");
		if (dataSourcesList == null || dataSourcesList.isEmpty()) {
			throw new Exception("No predefined DataSources found!!!");
		}

		Iterator it = dataSourcesList.iterator();
		while (it.hasNext()) {
			SourceBean dataSourceSB = (SourceBean) it.next();

			String jndi = (String) dataSourceSB.getAttribute("jndi");
			SbiDataSource aDataSource = new SbiDataSource();
			aDataSource.setDescr((String) dataSourceSB.getAttribute("descr"));
			aDataSource.setLabel((String) dataSourceSB.getAttribute("label"));
			aDataSource.setJndi(jndi);
			aDataSource.setUrl_connection((String) dataSourceSB.getAttribute("urlConnection"));
			aDataSource.setUser((String) dataSourceSB.getAttribute("username"));
			aDataSource.setPwd((String) dataSourceSB.getAttribute("pwd"));
			aDataSource.setDriver((String) dataSourceSB.getAttribute("driver"));

			String dialect = HibernateSessionManager.determineDialectFromJNDIResource(jndi);
			SbiDomains sbiDomainsDialect = getDialect(aSession, dialect);
			if (sbiDomainsDialect == null) {
				String defaultDialect = HibernateSessionManager.JDBC_URL_PREFIX_2_DIALECT.get("jdbc:mysql");
				logger.warn("No domain found for dialect [" + dialect + "]. Probably no JNDI resource for ds_cache is defined. Dialect set to " + defaultDialect
						+ ".");
				sbiDomainsDialect = getDialect(aSession, defaultDialect);
			}
			aDataSource.setDialect(sbiDomainsDialect);

			aDataSource.setMultiSchema(((String) dataSourceSB.getAttribute("multiSchema")).equals("false") ? false : true);
			aDataSource.setSchemaAttribute((String) dataSourceSB.getAttribute("attrSchema"));
			aDataSource.setReadOnly(((String) dataSourceSB.getAttribute("readOnly")).equals("false") ? false : true);
			aDataSource.setWriteDefault(((String) dataSourceSB.getAttribute("writeDefault")).equals("false") ? false : true);
			aDataSource.setUseForDataprep(((String) dataSourceSB.getAttribute("useForDataprep")).equals("false") ? false : true);
			aDataSource.setJdbcPoolConfiguration((String) dataSourceSB.getAttribute("jdbcAdvancedConfiguration"));

			String organization = (String) dataSourceSB.getAttribute("organization");
			SbiCommonInfo sbiCommonInfo = new SbiCommonInfo();
			sbiCommonInfo.setOrganization(organization);
			aDataSource.setCommonInfo(sbiCommonInfo);

			logger.debug("Inserting DataSource " + aDataSource.toString() + " ...");
			Integer newId = (Integer) aSession.save(aDataSource);

			SbiTenant tenant = (SbiTenant) aSession.createCriteria(SbiTenant.class).add(Restrictions.eq("name", organization)).uniqueResult();

			if (tenant == null)
				throw new Exception("DataSources configuration tenant not found!!!");

			SbiOrganizationDatasource sbiOrganizationDatasource = new SbiOrganizationDatasource();
			sbiOrganizationDatasource.setSbiDataSource(aDataSource);
			sbiOrganizationDatasource.setSbiOrganizations(tenant);
			SbiOrganizationDatasourceId idRel = new SbiOrganizationDatasourceId();
			idRel.setDatasourceId(newId);
			idRel.setOrganizationId(tenant.getId());
			sbiOrganizationDatasource.setCommonInfo(sbiCommonInfo);
			sbiOrganizationDatasource.setId(idRel);

			aSession.save(sbiOrganizationDatasource);

		}
		logger.debug("OUT");
	}

	private SbiDomains getDialect(Session aSession, String dialectString) {
		Criteria criteria = aSession.createCriteria(SbiDomains.class);
		criteria.add(Restrictions.eq("domainCd", "DIALECT_HIB"));
		criteria.add(Restrictions.eq("valueCd", dialectString));
		return (SbiDomains) criteria.uniqueResult();
	}

}
