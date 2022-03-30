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
package it.eng.spagobi.tools.datasource.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.behaviouralmodel.lov.metadata.SbiLov;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasource;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasourceId;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.json.Xml;
import it.eng.spagobi.tools.catalogue.metadata.SbiMetaModel;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.bo.JDBCDataSourcePoolConfiguration;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

/**
 * Defines the Hibernate implementations for all DAO methods, for a data source.
 */
public class DataSourceDAOHibImpl extends AbstractHibernateDAO implements IDataSourceDAO {
	static private Logger logger = Logger.getLogger(DataSourceDAOHibImpl.class);

	/**
	 * Load data source by id.
	 *
	 * @param dsID the ds id
	 * @return the data source
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.tools.datasource.dao.IDataSourceDAO#loadDataSourceByID(java.lang.Integer)
	 */
	@Override
	public IDataSource loadDataSourceByID(Integer dsID) throws EMFUserError {
		logger.debug("IN");
		IDataSource toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiDataSource hibDataSource = (SbiDataSource) aSession.load(SbiDataSource.class, dsID);
			toReturn = toDataSource(hibDataSource);
			tx.commit();

		} catch (HibernateException he) {
			logger.error("Error while loading the data source with id " + dsID.toString(), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Load data source by label.
	 *
	 * @param label the label
	 * @return the data source
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.tools.datasource.dao.IDataSourceDAO#loadDataSourceByLabel(string)
	 */
	@Override
	public IDataSource loadDataSourceByLabel(String label) throws EMFUserError {
		logger.debug("IN");
		IDataSource biDS = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hibQuery = null;

			hibQuery = tmpSession.createQuery(
					"select ds.sbiDataSource from SbiOrganizationDatasource ds where ds.sbiOrganizations.name = :tenantName and ds.sbiDataSource.label = :dsLabel");
			hibQuery.setString("tenantName", getTenant());
			hibQuery.setString("dsLabel", label);

			SbiDataSource hibDS = (SbiDataSource) hibQuery.uniqueResult();

			if (hibDS == null)
				return null;
			biDS = toDataSource(hibDS);

			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading the data source with label " + label, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		logger.debug("OUT");
		return biDS;
	}

	@Override
	public IDataSource findDataSourceByLabel(String label) {
		logger.debug("IN");
		IDataSource biDS = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hibQuery = null;

			hibQuery = tmpSession.createQuery("from SbiDataSource ds where ds.label = :label");
			hibQuery.setString("label", label);

			SbiDataSource hibDS = (SbiDataSource) hibQuery.uniqueResult();
			if (hibDS == null) {
				return null;
			}

			biDS = toDataSource(hibDS);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			throw e;
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		logger.debug("OUT");
		return biDS;
	}

	@Override
	public IDataSource loadDataSourceWriteDefault() throws EMFUserError {
		logger.debug("IN");
		IDataSource toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			toReturn = loadDataSourceWriteDefault(tmpSession);
			logger.debug("Datasource write default found : " + toReturn);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading the data source with write default = true: check there are no more than one (incorrect situation)", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public IDataSource loadDataSourceWriteDefault(Session aSession) throws EMFUserError {
		logger.debug("IN");
		IDataSource toReturn = null;
		try {
			SbiDataSource hibDataSource = loadSbiDataSourceWriteDefault(aSession);
			if (hibDataSource == null)
				return null;
			toReturn = toDataSource(hibDataSource);
			logger.debug("Datasource write default found in session: " + toReturn);
		} catch (HibernateException he) {
			logger.error("Error while loading the data source with write default = true: check there are no more than one (incorrect situation)", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public IDataSource loadDataSourceUseForDataprep() throws EMFUserError {
		Session aSession = null;
		IDataSource toReturn;
		try {
			aSession = getSession();
			toReturn = loadDataSourceUseForDataprep(aSession);
		} catch (HibernateException he) {
			logger.error("Error while loading data source for data prep", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return toReturn;
	}

	@Override
	public IDataSource loadDataSourceUseForDataprep(Session aSession) throws EMFUserError {
		logger.debug("IN");
		IDataSource toReturn = null;
		try {
			SbiDataSource hibDataSource = loadSbiDataSourceUseForDataprep(aSession);
			if (hibDataSource == null)
				return null;
			toReturn = toDataSource(hibDataSource);
			logger.debug("Datasource write default found in session: " + toReturn);
		} catch (HibernateException he) {
			logger.error("Error while loading the data source with write default = true: check there are no more than one (incorrect situation)", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Load all data sources.
	 *
	 * @return the list
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.tools.datasource.dao.IDataSourceDAO#loadAllDataSources()
	 */
	@Override
	public List<IDataSource> loadAllDataSources() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<IDataSource> realResult = new ArrayList<>();

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = null;

			hibQuery = aSession.createQuery("select ds.sbiDataSource from SbiOrganizationDatasource ds where ds.sbiOrganizations.name = :tenantName");
			hibQuery.setString("tenantName", getTenant());

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toDataSource((SbiDataSource) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading all data sources ", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return realResult;
	}

	@Override
	public List<IDataSource> loadDataSourcesForSuperAdmin() {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<IDataSource> realResult = new ArrayList<IDataSource>();

		try {

			UserProfile profile = (UserProfile) this.getUserProfile();
			Assert.assertNotNull(profile, "User profile object is null; it must be provided for this method to continue");

			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(
					"select ds.sbiDataSource from SbiOrganizationDatasource ds where (ds.sbiOrganizations.name = :tenantName or ds.sbiDataSource.commonInfo.userIn = :userId) or length(ds.sbiDataSource.jndi) > 0");
			hibQuery.setString("tenantName", getTenant());
			hibQuery.setString("userId", profile.getUserId().toString());

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toDataSource((SbiDataSource) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading all data sources ", he);

			if (tx != null)
				tx.rollback();

			throw new SpagoBIRuntimeException("Error while loading data sources", he);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return realResult;
	}

	/**
	 * Load dialect by id.
	 *
	 * @param dialectId the dialect id
	 * @return the dialect
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public Domain loadDialect(int dialectId) throws EMFUserError {
		Session session = null;
		try {
			session = getSession();
			SbiDomains sd = (SbiDomains) session.load(SbiDomains.class, dialectId);
			Domain d = new Domain();
			d.setValueId(sd.getValueId());
			d.setDomainCode(sd.getDomainCd());
			d.setDomainName(sd.getDomainNm());
			d.setValueCd(sd.getValueCd());
			d.setValueName(sd.getValueNm());
			d.setValueDescription(sd.getValueDs());
			return d;
		} catch (HibernateException e) {
			logger.error("Error while loading the dialect with id " + dialectId, e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}
	}

	/**
	 * Modify data source.
	 *
	 * @param aDataSource the a data source
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.tools.datasource.dao.IDataSourceDAO#modifyDataSource(it.eng.spagobi.tools.datasource.bo.IDataSource)
	 */
	@Override
	public void modifyDataSource(IDataSource aDataSource) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criterion aCriterion = Expression.and(Expression.eq("domainCd", "DIALECT_HIB"), Expression.eq("valueCd", aDataSource.getDialectName()));
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(aCriterion);

			SbiDomains dialect = (SbiDomains) criteria.uniqueResult();

			if (dialect == null) {
				logger.error("The Domain with value_cd= " + aDataSource.getDialectName() + " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1035);
			}

			// If DataSource Label has changed all LOVS with that DS need to be
			// changed
			SbiDataSource hibDataSource = (SbiDataSource) aSession.load(SbiDataSource.class, new Integer(aDataSource.getDsId()));

			// If datasource has a null pwd, get the old value from DB
			if (StringUtils.isEmpty(aDataSource.getPwd())) {
				aDataSource.setPwd(hibDataSource.getPwd());
			}

			if (aDataSource.getLabel() != null && hibDataSource.getLabel() != null) {
				if (!aDataSource.getLabel().equals(hibDataSource.getLabel())) {
					logger.debug("DataSource label is changed- update lovs and dataset referring to it");

					Query hibQuery = aSession.createQuery(" from SbiLov s where s.inputTypeCd = 'QUERY'");
					List hibList = hibQuery.list();
					if (!hibList.isEmpty()) {
						Iterator it = hibList.iterator();
						while (it.hasNext()) {
							SbiLov lov = (SbiLov) it.next();
							String prov = lov.getLovProvider();
							String conne = null;

							prov = escapeXML(prov, true);

							try {
								String statementString;
								String queryString = null;
								statementString = Xml.xml2json(prov);
								JSONObject queryObject = new JSONObject(statementString);
								queryString = queryObject.getString("QUERY");
								JSONObject connectionObject = new JSONObject(queryString);
								conne = connectionObject.getString("CONNECTION");
							} catch (TransformerFactoryConfigurationError e) {
								logger.error("Problem with configuration of Transformer Factories during xml2json", e);
								throw new SpagoBIDAOException(e);
							} catch (TransformerException e) {
								logger.error("Error during xml to json transformation of provider from lov with id: " + lov.getLovId(), e);
								throw new SpagoBIDAOException(e);
							} catch (JSONException e) {
								logger.error("Error occured during json object creation from json string", e);
								throw new SpagoBIDAOException(e);
							}

							if (conne.equals(hibDataSource.getLabel())) {
								int cutStart = prov.indexOf("<CONNECTION>");
								cutStart = cutStart + 12;
								int cutEnd = prov.indexOf("</CONNECTION>");
								String firstPart = prov.substring(0, cutStart);
								String secondPart = prov.substring(cutEnd, prov.length());
								prov = firstPart + aDataSource.getLabel() + secondPart;

								prov = escapeXML(prov, false);

								lov.setLovProvider(prov);
								aSession.update(lov);
							}

						}
					}

					// If DataSource Label has changed update all dataset
					// referring to it

					String previousDataSourceLabel = hibDataSource.getLabel();
					String newDataSOurceLabel = aDataSource.getLabel();
					aSession.disableFilter("tenantFilter");
					Query listQuery = aSession.createQuery("from SbiDataSet h where h.active = ? and h.configuration like :previousDataSource");
					listQuery.setBoolean(0, true);
					listQuery.setParameter("previousDataSource", "%dataSource%:%" + previousDataSourceLabel + "\"%");
					List dsList = listQuery.list();

					// iterate the dataset, (only the active ones)
					for (Iterator iterator = dsList.iterator(); iterator.hasNext();) {
						SbiDataSet ds = (SbiDataSet) iterator.next();
						logger.debug("dataset - " + ds.getLabel());
						if (ds.getConfiguration() != null) {
							String config = JSONUtils.escapeJsonString(ds.getConfiguration());
							JSONObject jsonConf = ObjectUtils.toJSONObject(config);
							try {
								String selector = "";
								String dataSourceLabel = jsonConf.optString(DataSetConstants.DATA_SOURCE);

								if (dataSourceLabel == null || dataSourceLabel.equals("")) {
									dataSourceLabel = jsonConf.optString("qbeDataSource");
									selector = "qbeDataSource";
								} else {
									selector = DataSetConstants.DATA_SOURCE;
								}

								if (dataSourceLabel != null && dataSourceLabel.equals(previousDataSourceLabel)) {
									logger.debug("change " + dataSourceLabel + " from " + previousDataSourceLabel + " datasource to - " + newDataSOurceLabel);
									jsonConf.put(selector, newDataSOurceLabel);
									ds.setConfiguration(jsonConf.toString());
									aSession.update(ds);
									logger.debug("change made");
								}

							} catch (Exception e) {
								logger.error("Error while parsing dataset configuration for dataset " + ds.getLabel()
										+ ". Data Source will not be updated  Error: " + e.getMessage());
							}
						}

					}

				}
			}

			hibDataSource.setLabel(aDataSource.getLabel());
			hibDataSource.setDialect(dialect);
			hibDataSource.setDialectDescr(dialect.getValueNm());
			hibDataSource.setDescr(aDataSource.getDescr());
			hibDataSource.setJndi(aDataSource.getJndi());
			hibDataSource.setUrl_connection(aDataSource.getUrlConnection());
			hibDataSource.setUser(aDataSource.getUser());
			hibDataSource.setPwd(aDataSource.getPwd());
			hibDataSource.setDriver(aDataSource.getDriver());
			hibDataSource.setMultiSchema(aDataSource.getMultiSchema());

			if (aDataSource.getJdbcPoolConfiguration() != null) {
				String modifiedJdbcPoolConfig = modifySbiDataSourceJdbcPoolConfig(aDataSource);
				hibDataSource.setJdbcPoolConfiguration(modifiedJdbcPoolConfig);
			}

			hibDataSource.setReadOnly(aDataSource.checkIsReadOnly());

			disableOtherWriteDefault(aDataSource, hibDataSource, aSession);

			hibDataSource.setWriteDefault(aDataSource.checkIsWriteDefault());

			hibDataSource.setUseForDataprep(aDataSource.checkUseForDataprep());

			hibDataSource.setSchemaAttribute(aDataSource.getSchemaAttribute());
			updateSbiCommonInfo4Update(hibDataSource);

			aSession.update(hibDataSource);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while modifing the data source with id " + ((aDataSource == null) ? "" : String.valueOf(aDataSource.getDsId())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}

	}

	private void disableOtherWriteDefault(IDataSource aDataSource, SbiDataSource hibDataSource, Session aSession) {
		// if writeDefault is going to be set to true than must be disabled in
		// others
		logger.debug("IN");
		if (aDataSource.checkIsWriteDefault() == true) {
			logger.debug("searching for write default datasource to delete flag");
			SbiDataSource hibModify = loadSbiDataSourceWriteDefault(aSession);
			if (hibModify != null && !hibModify.getLabel().equals(hibDataSource.getLabel())) {
				logger.debug("previous write default data source was " + hibModify.getLabel());
				hibModify.setWriteDefault(false);
				aSession.update(hibModify);

				logger.debug("previous write default modified");
			} else {
				logger.debug("No previous write default datasource found");
			}
		}
		logger.debug("OUT");
	}

	private void disableOtherUseForDataprep(IDataSource aDataSource, SbiDataSource hibDataSource, Session aSession) {
		// if writeDefault is going to be set to true than must be disabled in
		// others
		logger.debug("IN");
		if (aDataSource.checkUseForDataprep() == true) {
			logger.debug("searching for write default datasource to delete flag");
			SbiDataSource hibModify = loadSbiDataSourceUseForDataprep(aSession);
			if (hibModify != null && !hibModify.getLabel().equals(hibDataSource.getLabel())) {
				logger.debug("previous write default data source was " + hibModify.getLabel());
				hibModify.setUseForDataprep(false);
				aSession.update(hibModify);

				logger.debug("previous write default modified");
			} else {
				logger.debug("No previous write default datasource found");
			}
		}
		logger.debug("OUT");
	}

	private SbiDataSource loadSbiDataSourceWriteDefault(Session aSession) {
		Criterion labelCriterrion = Expression.eq("writeDefault", true);
		Criteria criteria = aSession.createCriteria(SbiDataSource.class);
		criteria.add(labelCriterrion);
		SbiDataSource hibDataSource = (SbiDataSource) criteria.uniqueResult();
		logger.debug("Hibernate datasource write default found in session: " + hibDataSource);
		return hibDataSource;
	}

	private SbiDataSource loadSbiDataSourceUseForDataprep(Session aSession) {
		Criterion labelCriterrion = Expression.eq("useForDataprep", true);
		Criteria criteria = aSession.createCriteria(SbiDataSource.class);
		criteria.add(labelCriterrion);
		SbiDataSource hibDataSource = (SbiDataSource) criteria.uniqueResult();
		logger.debug("Hibernate datasource write default found in session: " + hibDataSource);
		return hibDataSource;
	}

	/**
	 * Insert data source.
	 *
	 * @param aDataSource the a data source
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.tools.datasource.dao.IDataSourceDAO#insertDataSource(it.eng.spagobi.tools.datasource.bo.IDataSource)
	 */
	@Override
	public Integer insertDataSource(IDataSource aDataSource, String organization) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer id = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criterion aCriterion = Expression.and(Expression.eq("domainCd", "DIALECT_HIB"), Expression.eq("valueCd", aDataSource.getDialectName()));
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(aCriterion);

			SbiDomains dialect = (SbiDomains) criteria.uniqueResult();

			if (dialect == null) {
				logger.error("The Domain with value_cd=" + aDataSource.getDialectName() + " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1035);
			}

			SbiDataSource hibDataSource = toSbiDataSource(aDataSource);
			hibDataSource.setDialect(dialect);
			hibDataSource.setDialectDescr(dialect.getValueNm());
			hibDataSource.setReadOnly(aDataSource.checkIsReadOnly());

			disableOtherWriteDefault(aDataSource, hibDataSource, aSession);

			disableOtherUseForDataprep(aDataSource, hibDataSource, aSession);

			hibDataSource.setWriteDefault(aDataSource.checkIsWriteDefault());
			hibDataSource.setUseForDataprep(aDataSource.checkUseForDataprep());

			hibDataSource.getCommonInfo().setOrganization(organization);

			updateSbiCommonInfo4Insert(hibDataSource);
			id = (Integer) aSession.save(hibDataSource);
			tx.commit();
			aSession.flush();
			tx.begin();
			SbiTenant sbiOrganizations = DAOFactory.getTenantsDAO().loadTenantByName(hibDataSource.getCommonInfo().getOrganization());

			SbiOrganizationDatasource sbiOrganizationDatasource = new SbiOrganizationDatasource();
			sbiOrganizationDatasource.setSbiDataSource(hibDataSource);
			sbiOrganizationDatasource.setSbiOrganizations(sbiOrganizations);
			SbiOrganizationDatasourceId idRel = new SbiOrganizationDatasourceId();
			idRel.setDatasourceId(id);
			idRel.setOrganizationId(sbiOrganizations.getId());
			sbiOrganizationDatasource.setId(idRel);

			sbiOrganizationDatasource.getCommonInfo().setOrganization(organization);

			updateSbiCommonInfo4Insert(sbiOrganizationDatasource);

			aSession.save(sbiOrganizationDatasource);

			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while inserting the data source with id " + ((aDataSource == null) ? "" : String.valueOf(aDataSource.getDsId())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}

		}
		return id;
	}

	/**
	 * Erase data source.
	 *
	 * @param aDataSource the a data source
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.tools.datasource.dao.IDataSourceDAO#eraseDataSource(it.eng.spagobi.tools.datasource.bo.DataSource)
	 */
	@Override
	public void eraseDataSource(IDataSource aDataSource) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			// delete first all associations with tenants
			Query hibQuery2 = aSession.createQuery("from SbiOrganizationDatasource ds where ds.id.datasourceId = :dsId");
			hibQuery2.setInteger("dsId", aDataSource.getDsId());
			ArrayList<SbiOrganizationDatasource> dsOrganizations = (ArrayList<SbiOrganizationDatasource>) hibQuery2.list();
			for (Iterator iterator = dsOrganizations.iterator(); iterator.hasNext();) {
				SbiOrganizationDatasource sbiOrganizationDatasource = (SbiOrganizationDatasource) iterator.next();
				aSession.delete(sbiOrganizationDatasource);
				aSession.flush();
			}

			SbiDataSource hibDataSource = (SbiDataSource) aSession.load(SbiDataSource.class, new Integer(aDataSource.getDsId()));
			aSession.delete(hibDataSource);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while erasing the data source with id " + ((aDataSource == null) ? "" : String.valueOf(aDataSource.getDsId())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 8007);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}

	}

	/**
	 * From the hibernate DataSource at input, gives the corrispondent <code>DataSource</code> object.
	 *
	 * @param hibDataSource The hybernate data source
	 * @return The corrispondent <code>DataSource</code> object
	 */
	public static IDataSource toDataSource(SbiDataSource hibDataSource) {
		logger.debug("IN");
		ObjectMapper mapper = new ObjectMapper();
		String jdbcAdvancedOptions = hibDataSource.getJdbcPoolConfiguration();
		IDataSource ds = DataSourceFactory.getDataSource();

		try {
			ds.setDsId(hibDataSource.getDsId());
			ds.setLabel(hibDataSource.getLabel());
			ds.setDescr(hibDataSource.getDescr());
			ds.setJndi(hibDataSource.getJndi());
			ds.setUrlConnection(hibDataSource.getUrl_connection());
			ds.setUser(hibDataSource.getUser());
			ds.setPwd(hibDataSource.getPwd());
			ds.setDriver(hibDataSource.getDriver());
			ds.setOwner(hibDataSource.getCommonInfo().getUserIn());
			ds.setDialectName(hibDataSource.getDialect().getValueCd());
			ds.setHibDialectClass(hibDataSource.getDialect().getValueCd());
			ds.setEngines(hibDataSource.getSbiEngineses());
			ds.setObjects(hibDataSource.getSbiObjectses());
			ds.setSchemaAttribute(hibDataSource.getSchemaAttribute());
			ds.setMultiSchema(hibDataSource.getMultiSchema());
			ds.setReadOnly(hibDataSource.getReadOnly());
			ds.setWriteDefault(hibDataSource.getWriteDefault());
			ds.setUseForDataprep(hibDataSource.getUseForDataprep());

			if (!ds.checkIsJndi()) {
				if (jdbcAdvancedOptions != null) {
					JDBCDataSourcePoolConfiguration jdbcPoolConfig = mapper.readValue(jdbcAdvancedOptions, JDBCDataSourcePoolConfiguration.class);
					ds.setJdbcPoolConfiguration(jdbcPoolConfig);
				} else {
					// retrocompatibility: in case of a previous knowage version (before 6.2.0), maybe database contains a JDBC datasource without any
					// information about connection pool parameters. We are setting a default JDBCDataSourcePoolConfiguration with default values
					ds.setJdbcPoolConfiguration(new JDBCDataSourcePoolConfiguration());
				}
			}

		} catch (JsonParseException e) {
			logger.error("Error with parsing JSON String to Object", e);
		} catch (JsonMappingException e) {
			logger.error("Error with mapping JSON object", e);
		} catch (IOException e) {
			logger.error("Error with mapping JSON object", e);
		}
		logger.debug("OUT");
		return ds;
	}

	public static SbiDataSource toSbiDataSource(IDataSource dataSource) {
		logger.debug("IN");
		ObjectMapper mapper = new ObjectMapper();
		JDBCDataSourcePoolConfiguration jdbcAdvancedOptionsObj = dataSource.getJdbcPoolConfiguration();

		SbiDataSource sbiDataSource = new SbiDataSource();

		try {
			if (jdbcAdvancedOptionsObj != null) {
				String jdbcPoolConfiguration = mapper.writeValueAsString(jdbcAdvancedOptionsObj);
				sbiDataSource.setJdbcPoolConfiguration(jdbcPoolConfiguration);
			}
			sbiDataSource.setDsId(dataSource.getDsId());
			sbiDataSource.setLabel(dataSource.getLabel());
			sbiDataSource.setDescr(dataSource.getDescr());
			sbiDataSource.setJndi(dataSource.getJndi());
			sbiDataSource.setUrl_connection(dataSource.getUrlConnection());
			sbiDataSource.setUser(dataSource.getUser());
			sbiDataSource.setPwd(dataSource.getPwd());
			sbiDataSource.setDriver(dataSource.getDriver());
			sbiDataSource.setDialectDescr(dataSource.getDialectName());
			sbiDataSource.setSbiEngineses(dataSource.getEngines());
			sbiDataSource.setSbiObjectses(dataSource.getObjects());
			sbiDataSource.setSchemaAttribute(dataSource.getSchemaAttribute());
			sbiDataSource.setMultiSchema(dataSource.getMultiSchema());

		} catch (JsonProcessingException e) {
			logger.error("Error with converting Object to JSON String", e);
		}
		logger.debug("OUT");
		return sbiDataSource;
	}

	public static String modifySbiDataSourceJdbcPoolConfig(IDataSource dataSource) {
		logger.debug("IN");
		ObjectMapper mapper = new ObjectMapper();
		JDBCDataSourcePoolConfiguration jdbcAdvancedOptionsObj = dataSource.getJdbcPoolConfiguration();
		String jdbcPoolConfiguration = null;
		try {
			jdbcPoolConfiguration = mapper.writeValueAsString(jdbcAdvancedOptionsObj);
		} catch (JsonProcessingException e) {
			logger.error("Error with converting Object to JSON String", e);
		}

		logger.debug("OUT");
		return jdbcPoolConfiguration;
	}

	/**
	 * Checks for bi obj associated.
	 *
	 * @param dsId the ds id
	 * @return true, if checks for bi obj associated
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.tools.datasource.dao.IDataSourceDAO#hasBIObjAssociated(java.lang.String)
	 */
	@Override
	public boolean hasBIObjAssociated(String dsId) throws EMFUserError {
		logger.debug("IN");
		boolean bool = false;

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Integer dsIdInt = Integer.valueOf(dsId);

			// String hql = " from SbiObjects s where s.dataSource.dsId = "+
			// dsIdInt;
			String hql = " from SbiObjects s where s.dataSource.dsId = ?";
			Query aQuery = aSession.createQuery(hql);
			aQuery.setInteger(0, dsIdInt.intValue());
			List biObjectsAssocitedWithDs = aQuery.list();
			if (biObjectsAssocitedWithDs.size() > 0)
				bool = true;
			else
				bool = false;
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while getting the objects associated with the data source with id " + dsId, he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return bool;

	}

	/**
	 * Checks for entities associated. Objects, Models, datasets and lovs
	 */

	@Override
	public Map<String, List<String>> returnEntitiesAssociated(Integer dsId) throws EMFUserError {
		logger.debug("IN");
		// map to return
		Map<String, List<String>> mapToReturn = new HashMap<String, List<String>>();

		List<String> objectNamesAssociatedWithDS = new ArrayList<>();
		logger.debug("Check for BIObject associated to datasource");

		Session aSession = null;
		Transaction tx = null;

		try {

			try {
				// check if there are objects using datasource
				aSession = getSession();
				tx = aSession.beginTransaction();
				logger.debug("Check for Objects associated to datasource");
				String hql = " from SbiObjects s where s.dataSource.dsId = ?";
				Query aQuery = aSession.createQuery(hql);
				aQuery.setInteger(0, dsId.intValue());
				List biObjectsAssocitedWithDs = aQuery.list();
				for (Iterator iterator = biObjectsAssocitedWithDs.iterator(); iterator.hasNext();) {
					SbiObjects sbiObj = (SbiObjects) iterator.next();
					objectNamesAssociatedWithDS.add(sbiObj.getName() != null ? sbiObj.getName() : sbiObj.getLabel());
				}

				if (objectNamesAssociatedWithDS.size() > 0) {
					mapToReturn.put("sbi.datasource.usedby.biobject", objectNamesAssociatedWithDS);
					logger.debug("there are objects using datasource, return them");
				}

				logger.debug("Check for Meta Model associated to datasource");
				List<String> metaModelNamesAssociatedWithDS = new ArrayList<>();
				hql = " from SbiMetaModel s where s.dataSource.dsId = ?";
				aQuery = aSession.createQuery(hql);
				aQuery.setInteger(0, dsId.intValue());
				List metaModelsAssocitedWithDs = aQuery.list();
				for (Iterator iterator = metaModelsAssocitedWithDs.iterator(); iterator.hasNext();) {
					SbiMetaModel sbiMetaModel = (SbiMetaModel) iterator.next();
					metaModelNamesAssociatedWithDS.add(sbiMetaModel.getName());
				}

				if (metaModelNamesAssociatedWithDS.size() > 0) {
					mapToReturn.put("sbi.datasource.usedby.metamodel", metaModelNamesAssociatedWithDS);
					logger.debug("there are meta models using datasource, return them");
				}

				logger.debug("Check for DataSet associated to datasource");
				String dataSourceLabel = null;

				List<String> dataSetNamesAssociatedWithDS = new ArrayList<>();

				SbiDataSource dSource = (SbiDataSource) aSession.load(SbiDataSource.class, dsId);
				dataSourceLabel = dSource.getLabel();

				hql = " from SbiDataSet s where s.active = ? AND s.type IN " + " ('" + DataSetConstants.DS_QUERY + "','" + DataSetConstants.DS_QBE + "')";
				aQuery = aSession.createQuery(hql);
				aQuery.setBoolean(0, true);
				try {
					List dataSetAssocitedWithDs = aQuery.list();
					for (Iterator iterator = dataSetAssocitedWithDs.iterator(); iterator.hasNext();) {
						SbiDataSet sbiDataSet = (SbiDataSet) iterator.next();
						String configuration = sbiDataSet.getConfiguration();
						JSONObject configurationJSON = new JSONObject(configuration);
						String ds = configurationJSON.optString("dataSource");
						if (ds == null || ds.equals(""))
							ds = configurationJSON.optString("qbeDataSource");
						if (ds != null && ds.equals(dataSourceLabel)) {
							dataSetNamesAssociatedWithDS.add(sbiDataSet.getName() != null ? sbiDataSet.getName() : sbiDataSet.getLabel());
						}
					}
				} catch (JSONException he) {
					logger.error("Error while converting dataset configuration to JSON: dataset id = " + dsId, he);
					if (tx != null)
						tx.rollback();
					throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
				}

				if (dataSetNamesAssociatedWithDS.size() > 0) {
					mapToReturn.put("sbi.datasource.usedby.dataset", dataSetNamesAssociatedWithDS);
					logger.debug("there are datasets using datasource, return them");
				}

				List<String> lovNamesAssociatedWithDS = new ArrayList<>();
				logger.debug("Check for Lov associated to datasource");

				hql = " from SbiLov s where inputTypeCd = ?";
				aQuery = aSession.createQuery(hql);
				aQuery.setString(0, "QUERY");

				List lovAssocitedWithDs = aQuery.list();
				SbiLov sbiLov = null;
				for (Iterator iterator = lovAssocitedWithDs.iterator(); iterator.hasNext();) {
					sbiLov = (SbiLov) iterator.next();
					String lovProvider = sbiLov.getLovProvider();
					lovProvider = escapeXML(lovProvider, true);
					lovProvider = removeStatement(lovProvider); // KNOWAGE-6312: removed statement for double quote character issue, if this character is
																// present, it is unescapable because of xml2json process will roll back it

					try {
						String statementString = Xml.xml2json(lovProvider);
						JSONObject queryObject = new JSONObject(statementString);
						String queryString = queryObject.getString("QUERY");
						JSONObject connectionObject = new JSONObject(queryString);
						String conne = connectionObject.getString("CONNECTION");

						if (conne.equals(dataSourceLabel)) {
							lovNamesAssociatedWithDS.add(sbiLov.getName() != null ? sbiLov.getName() : sbiLov.getLabel());
						}

						if (lovNamesAssociatedWithDS.size() > 0) {
							mapToReturn.put("sbi.datasource.usedby.lov", lovNamesAssociatedWithDS);
							logger.debug("there are lovs using datasource, return them");
						}

					} catch (TransformerFactoryConfigurationError e) {
						logger.error("Problem with configuration of Transformer Factories during xml2json", e);
						throw new SpagoBIDAOException(e);
					} catch (TransformerException e) {
						logger.error("Error during xml to json transformation of provider from lov with id: " + sbiLov.getLovId(), e);
						throw new SpagoBIDAOException(e);
					} catch (JSONException e) {
						logger.error("Error occured during json object creation from json string", e);
						throw new SpagoBIDAOException(e);
					}
				}

				tx.rollback();

			} catch (HibernateException he) {
				logger.error("Error while getting the entities associated with the data source with id " + dsId, he);
				if (tx != null)
					tx.rollback();
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

			}

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

		logger.debug("OUT");

		return mapToReturn;

	}

	@Override
	public void associateToTenant(Integer tenantId, Integer datasourceId) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiTenant sbiOrganizations = DAOFactory.getTenantsDAO().loadTenantById(tenantId);
			SbiDataSource datasource = (SbiDataSource) aSession.load(SbiDataSource.class, datasourceId);

			SbiOrganizationDatasource sbiOrganizationDatasource = new SbiOrganizationDatasource();
			sbiOrganizationDatasource.setSbiDataSource(datasource);
			sbiOrganizationDatasource.setSbiOrganizations(sbiOrganizations);
			SbiOrganizationDatasourceId idRel = new SbiOrganizationDatasourceId();
			idRel.setDatasourceId(datasourceId);
			idRel.setOrganizationId(sbiOrganizations.getId());
			sbiOrganizationDatasource.setId(idRel);
			updateSbiCommonInfo4Insert(sbiOrganizationDatasource);

			aSession.save(sbiOrganizationDatasource);

			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while inserting realationship for data source with id " + datasourceId + " and the tenant with id " + tenantId, he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}

	}

	private String escapeXML(String prov, boolean escape) {
		String statement = null;
		int cutStartIndex = prov.indexOf("<STMT>");
		cutStartIndex = cutStartIndex + 6;
		int cutEndIndex = prov.indexOf("</STMT>");
		statement = prov.substring(cutStartIndex, cutEndIndex);

		if (escape) {
			statement = StringEscapeUtils.escapeXml(statement);
		} else {
			statement = StringEscapeUtils.unescapeXml(statement);
		}

		int cutStart = prov.indexOf("<STMT>");
		cutStart = cutStart + 6;
		int cutEnd = prov.indexOf("</STMT>");
		String firstPart = prov.substring(0, cutStart);
		String secondPart = prov.substring(cutEnd, prov.length());
		prov = firstPart + statement + secondPart;
		return prov;
	}

	private String getStatement(String prov) {
		String statement = null;
		int cutStartIndex = prov.indexOf("<STMT>");
		cutStartIndex = cutStartIndex + 6;
		int cutEndIndex = prov.indexOf("</STMT>");
		statement = prov.substring(cutStartIndex, cutEndIndex);

		statement = StringEscapeUtils.escapeXml(statement);
		return statement;

	}

	private String removeStatement(String prov) {
		String statement = null;
		int cutStartIndex = prov.indexOf("<STMT>");
		cutStartIndex = cutStartIndex + 6;
		int cutEndIndex = prov.indexOf("</STMT>");
		statement = prov.substring(cutStartIndex, cutEndIndex);

		statement = StringEscapeUtils.escapeXml(statement);

		int cutStart = prov.indexOf("<STMT>");
		cutStart = cutStart + 6;
		int cutEnd = prov.indexOf("</STMT>");
		String firstPart = prov.substring(0, cutStart);
		String secondPart = prov.substring(cutEnd, prov.length());
		prov = firstPart + secondPart;
		return prov;
	}
}
