/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 20-giu-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.tools.datasource.dao;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.behaviouralmodel.lov.metadata.SbiLov;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasource;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasourceId;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.bo.DataSourceModel;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;
import it.eng.spagobi.utilities.json.JSONUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.json.JSONObject;

/**
 * Defines the Hibernate implementations for all DAO methods, for a data source.
 */
public class DataSourceDAOHibImpl extends AbstractHibernateDAO implements IDataSourceDAO {
	static private Logger logger = Logger.getLogger(DataSourceDAOHibImpl.class);

	/**
	 * Load data source by id.
	 *
	 * @param dsID
	 *            the ds id
	 *
	 * @return the data source
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.tools.datasource.dao.IDataSourceDAO#loadDataSourceByID(java.lang.Integer)
	 */
	@Override
	public DataSource loadDataSourceByID(Integer dsID) throws EMFUserError {
		logger.debug("IN");
		DataSource toReturn = null;
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
	 * @param label
	 *            the label
	 *
	 * @return the data source
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
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

			hibQuery = tmpSession
					.createQuery("select ds.sbiDataSource from SbiOrganizationDatasource ds where ds.sbiOrganizations.name = :tenantName and ds.sbiDataSource.label = :dsLabel");
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

	/**
	 * Load all data sources.
	 *
	 * @return the list
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.tools.datasource.dao.IDataSourceDAO#loadAllDataSources()
	 */
	@Override
	public List loadAllDataSources() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {

			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = null;

			// superadmin task
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
	public List loadDataSourcesForSuperAdmin() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {

			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiDataSource");

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

	/**
	 * Modify data source.
	 *
	 * @param aDataSource
	 *            the a data source
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.tools.datasource.dao.IDataSourceDAO#modifyDataSource(it.eng.spagobi.tools.datasource.bo.DataSource)
	 */
	@Override
	public void modifyDataSource(IDataSource aDataSource) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criterion aCriterion = Expression.eq("valueId", aDataSource.getDialectId());
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(aCriterion);

			SbiDomains dialect = (SbiDomains) criteria.uniqueResult();

			if (dialect == null) {
				logger.error("The Domain with value_id= " + aDataSource.getDialectId() + " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1035);
			}

			// If DataSource Label has changed all LOVS with that DS need to be changed
			SbiDataSource hibDataSource = (SbiDataSource) aSession.load(SbiDataSource.class, new Integer(aDataSource.getDsId()));
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
							SourceBean sb = SourceBean.fromXMLString(prov);
							SourceBean conn = (SourceBean) sb.getAttribute("CONNECTION");
							String conne = conn.getCharacters();
							if (conne.equals(hibDataSource.getLabel())) {
								int cutStart = prov.indexOf("<CONNECTION>");
								cutStart = cutStart + 12;
								int cutEnd = prov.indexOf("</CONNECTION>");
								String firstPart = prov.substring(0, cutStart);
								String secondPart = prov.substring(cutEnd, prov.length());
								prov = firstPart + aDataSource.getLabel() + secondPart;
								lov.setLovProvider(prov);
								aSession.update(lov);
							}

						}
					}

					// If DataSource Label has changed update all dataset referring to it

					String previousDataSourceLabel = hibDataSource.getLabel();
					String newDataSOurceLabel = aDataSource.getLabel();

					Query listQuery = aSession.createQuery("from SbiDataSet h where h.active = ? order by h.name ");
					listQuery.setBoolean(0, true);
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
			hibDataSource.setReadOnly(aDataSource.checkIsReadOnly());

			disableOtherWriteDefault(aDataSource, hibDataSource, aSession);

			hibDataSource.setWriteDefault(aDataSource.checkIsWriteDefault());

			hibDataSource.setSchemaAttribute(aDataSource.getSchemaAttribute());
			updateSbiCommonInfo4Update(hibDataSource);

			aSession.update(hibDataSource);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while modifing the data source with id " + ((aDataSource == null) ? "" : String.valueOf(aDataSource.getDsId())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} catch (SourceBeanException e) {
			logger.error("Error while modifing the data source with id " + ((aDataSource == null) ? "" : String.valueOf(aDataSource.getDsId())), e);

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
		// if writeDefault is going to be set to true than must be disabled in others
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

	private SbiDataSource loadSbiDataSourceWriteDefault(Session aSession) {
		Criterion labelCriterrion = Expression.eq("writeDefault", true);
		Criteria criteria = aSession.createCriteria(SbiDataSource.class);
		criteria.add(labelCriterrion);
		SbiDataSource hibDataSource = (SbiDataSource) criteria.uniqueResult();
		logger.debug("Hibernate datasource write default found in session: " + hibDataSource);
		return hibDataSource;
	}

	/**
	 * Insert data source.
	 *
	 * @param aDataSource
	 *            the a data source
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.tools.datasource.dao.IDataSourceDAO#insertDataSource(it.eng.spagobi.tools.datasource.bo.DataSource)
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

			Criterion aCriterion = Expression.eq("valueId", aDataSource.getDialectId());
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(aCriterion);

			SbiDomains dialect = (SbiDomains) criteria.uniqueResult();

			if (dialect == null) {
				logger.error("The Domain with value_id=" + aDataSource.getDialectId() + " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1035);
			}
			SbiDataSource hibDataSource = new SbiDataSource();
			hibDataSource.setDialect(dialect);
			hibDataSource.setDialectDescr(dialect.getValueNm());
			hibDataSource.setLabel(aDataSource.getLabel());
			hibDataSource.setDescr(aDataSource.getDescr());
			hibDataSource.setJndi(aDataSource.getJndi());
			hibDataSource.setUrl_connection(aDataSource.getUrlConnection());
			hibDataSource.setUser(aDataSource.getUser());
			hibDataSource.setPwd(aDataSource.getPwd());
			hibDataSource.setDriver(aDataSource.getDriver());
			hibDataSource.setMultiSchema(aDataSource.getMultiSchema());
			hibDataSource.setSchemaAttribute(aDataSource.getSchemaAttribute());
			hibDataSource.setReadOnly(aDataSource.checkIsReadOnly());

			disableOtherWriteDefault(aDataSource, hibDataSource, aSession);

			hibDataSource.setWriteDefault(aDataSource.checkIsWriteDefault());

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
			return id;
		}
	}

	/**
	 * Erase data source.
	 *
	 * @param aDataSource
	 *            the a data source
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
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
			SbiDataSource hibDataSource = (SbiDataSource) aSession.load(SbiDataSource.class, new Integer(aDataSource.getDsId()));
			aSession.delete(hibDataSource);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while erasing the data source with id " + ((aDataSource == null) ? "" : String.valueOf(aDataSource.getDsId())), he);

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

	/**
	 * From the hibernate DataSource at input, gives the corrispondent <code>DataSource</code> object.
	 *
	 * @param hibDataSource
	 *            The hybernate data source
	 *
	 * @return The corrispondent <code>DataSource</code> object
	 */
	public static DataSourceModel toDataSource(SbiDataSource hibDataSource) {
		DataSourceModel ds = new DataSourceModel();

		ds.setDsId(hibDataSource.getDsId());
		ds.setLabel(hibDataSource.getLabel());
		ds.setDescr(hibDataSource.getDescr());
		ds.setJndi(hibDataSource.getJndi());
		ds.setUrlConnection(hibDataSource.getUrl_connection());
		ds.setUser(hibDataSource.getUser());
		ds.setPwd(hibDataSource.getPwd());
		ds.setDriver(hibDataSource.getDriver());
		ds.setDialectId(hibDataSource.getDialect().getValueId());
		ds.setEngines(hibDataSource.getSbiEngineses());
		ds.setObjects(hibDataSource.getSbiObjectses());
		ds.setSchemaAttribute(hibDataSource.getSchemaAttribute());
		ds.setMultiSchema(hibDataSource.getMultiSchema());
		ds.setHibDialectClass(hibDataSource.getDialect().getValueCd());
		ds.setHibDialectName(hibDataSource.getDialect().getValueNm());
		ds.setReadOnly(hibDataSource.getReadOnly());
		ds.setWriteDefault(hibDataSource.getWriteDefault());
		ds.setUserIn(hibDataSource.getCommonInfo().getUserIn());
		return ds;
	}

	/**
	 * Checks for bi obj associated.
	 *
	 * @param dsId
	 *            the ds id
	 *
	 * @return true, if checks for bi obj associated
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
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

			// String hql = " from SbiObjects s where s.dataSource.dsId = "+ dsIdInt;
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

	// /**
	// * Checks for bi engine associated.
	// *
	// * @param dsId the ds id
	// *
	// * @return true, if checks for bi engine associated
	// *
	// * @throws EMFUserError the EMF user error
	// *
	// * @see it.eng.spagobi.tools.datasource.dao.IDataSourceDAO#hasEngineAssociated(java.lang.String)
	// */
	// public boolean hasBIEngineAssociated (String dsId) throws EMFUserError{
	// logger.debug("IN");
	// boolean bool = false;
	//
	//
	// Session aSession = null;
	// Transaction tx = null;
	// try {
	// aSession = getSession();
	// tx = aSession.beginTransaction();
	// Integer dsIdInt = Integer.valueOf(dsId);
	//
	// //String hql = " from SbiEngines s where s.dataSource.dsId = "+ dsIdInt;
	// String hql = " from SbiEngines s where s.dataSource.dsId = ?";
	// Query aQuery = aSession.createQuery(hql);
	// aQuery.setInteger(0, dsIdInt.intValue());
	// List biObjectsAssocitedWithEngine = aQuery.list();
	// if (biObjectsAssocitedWithEngine.size() > 0)
	// bool = true;
	// else
	// bool = false;
	// tx.commit();
	// } catch (HibernateException he) {
	// logger.error("Error while getting the engines associated with the data source with id " + dsId, he);
	//
	// if (tx != null)
	// tx.rollback();
	//
	// throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
	//
	// } finally {
	// if (aSession!=null){
	// if (aSession.isOpen()) aSession.close();
	// }
	// }
	// logger.debug("OUT");
	// return bool;
	//
	// }

}
