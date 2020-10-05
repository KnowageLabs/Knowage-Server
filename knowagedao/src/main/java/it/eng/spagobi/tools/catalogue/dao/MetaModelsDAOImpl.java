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
package it.eng.spagobi.tools.catalogue.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.BIMetaModelParameterDAOHibImpl;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiMetaModelParameter;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.metadata.SbiMetaModel;
import it.eng.spagobi.tools.catalogue.metadata.SbiMetaModelContent;
import it.eng.spagobi.tools.datasource.dao.DataSourceDAOHibImpl;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;
import it.eng.spagobi.utilities.assertion.Assert;

public class MetaModelsDAOImpl extends AbstractHibernateDAO implements IMetaModelsDAO {

	static private Logger logger = Logger.getLogger(MetaModelsDAOImpl.class);
	private static final String LOG_SUFFIX = ".log";

	@Override
	public MetaModel loadMetaModelForExecutionByNameAndRole(String name, String role, Boolean loadDSwithDrivers) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		MetaModel businessModel = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			businessModel = loadMetaModelByName(name);
			String hql = "from SbiMetaModelParameter s where s.sbiMetaModel.name = ? order by s.priority asc";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setString(0, businessModel.getName());
			List hibMetaModelPars = hqlQuery.list();
			SbiMetaModelParameter hibMetaModelPar = null;
			Iterator it = hibMetaModelPars.iterator();
			BIMetaModelParameter tmpBIMetaModelParameter = null;
			BIMetaModelParameterDAOHibImpl aBIMetaModelParameterDAOHibImpl = new BIMetaModelParameterDAOHibImpl();
			IParameterDAO aParameterDAO = DAOFactory.getParameterDAO();
			List metaModelParameters = new ArrayList();
			Parameter aParameter = null;
			int count = 1;
			while (it.hasNext()) {
				hibMetaModelPar = (SbiMetaModelParameter) it.next();
				tmpBIMetaModelParameter = aBIMetaModelParameterDAOHibImpl.toBIMetaModelParameter(hibMetaModelPar);

				// *****************************************************************
				// **************** START PRIORITY RECALCULATION
				// *******************
				// *****************************************************************
				Integer priority = tmpBIMetaModelParameter.getPriority();
				if (priority == null || priority.intValue() != count) {
					logger.warn("The priorities of the biparameters for the business model with name = " + businessModel.getName()
							+ " are not sorted. Priority recalculation starts.");
					aBIMetaModelParameterDAOHibImpl.recalculateBiParametersPriority(businessModel.getId(), aSession);
					tmpBIMetaModelParameter.setPriority(new Integer(count));
				}
				count++;
				// *****************************************************************
				// **************** END PRIORITY RECALCULATION
				// *******************
				// *****************************************************************

				aParameter = aParameterDAO.loadForExecutionByParameterIDandRoleName(tmpBIMetaModelParameter.getParID(), role, loadDSwithDrivers);
				if (aParameter.getModalityValue() == null) {
					return null;
				}
				tmpBIMetaModelParameter.setParID(aParameter.getId());
				tmpBIMetaModelParameter.setParameter(aParameter);
				metaModelParameters.add(tmpBIMetaModelParameter);
			}
			businessModel.setDrivers(metaModelParameters);
			tx.commit();
		} catch (Exception e) {
			logger.error(e);
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("An unexpected error occured while loading model with name [" + name + "]", e);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return businessModel;
	}

	@Override
	public MetaModel loadMetaModelForExecutionByIdAndRole(Integer id, String role) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		MetaModel businessModel = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			businessModel = loadMetaModelForDetail(id);
			// String hql = "from SbiObjPar s where s.sbiObject.biobjId = " +
			// biObject.getId() + " order by s.priority asc";
			String hql = "from SbiMetaModelParameter s where s.sbiMetaModel.id = ? order by s.priority asc";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, businessModel.getId().intValue());
			List hibMetaModelPars = hqlQuery.list();
			SbiMetaModelParameter hibMetaModelPar = null;
			Iterator it = hibMetaModelPars.iterator();
			BIMetaModelParameter tmpBIMetaModelParameter = null;
			BIMetaModelParameterDAOHibImpl aBIMetaModelParameterDAOHibImpl = new BIMetaModelParameterDAOHibImpl();
			IParameterDAO aParameterDAO = DAOFactory.getParameterDAO();
			List metamodelParameters = new ArrayList();
			Parameter aParameter = null;
			int count = 1;
			while (it.hasNext()) {
				hibMetaModelPar = (SbiMetaModelParameter) it.next();
				tmpBIMetaModelParameter = aBIMetaModelParameterDAOHibImpl.toBIMetaModelParameter(hibMetaModelPar);

				// *****************************************************************
				// **************** START PRIORITY RECALCULATION
				// *******************
				// *****************************************************************
				Integer priority = tmpBIMetaModelParameter.getPriority();
				if (priority == null || priority.intValue() != count) {
					logger.warn("The priorities of the biparameters for the business model with id = " + businessModel.getId()
							+ " are not sorted. Priority recalculation starts.");
					aBIMetaModelParameterDAOHibImpl.recalculateBiParametersPriority(businessModel.getId(), aSession);
					tmpBIMetaModelParameter.setPriority(new Integer(count));
				}
				count++;
				// *****************************************************************
				// **************** END PRIORITY RECALCULATION
				// *******************
				// *****************************************************************

				aParameter = aParameterDAO.loadForExecutionByParameterIDandRoleName(tmpBIMetaModelParameter.getParID(), role, false);
				tmpBIMetaModelParameter.setParID(aParameter.getId());
				tmpBIMetaModelParameter.setParameter(aParameter);
				metamodelParameters.add(tmpBIMetaModelParameter);
			}
			businessModel.setDrivers(metamodelParameters);
			tx.commit();
		} catch (Exception e) {
			logger.error(e);
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("An unexpected error occured while loading model with id [" + id + "]", e);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return businessModel;
	}

	@Override
	public MetaModel loadMetaModelForDetail(Integer id) {
		Monitor monitor = MonitorFactory.start("it.eng.spagobi.tools.catalogue.dao.MetaModelsDAOImpl.loadMetaModelForDetail(Integer id)");
		logger.debug("IN");
		MetaModel businessModel = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// String hql = " from SbiMetaModel where id = " + id;
			String hql = " from SbiMetaModel where id = ?";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, id.intValue());
			SbiMetaModel hibObject = (SbiMetaModel) hqlQuery.uniqueResult();
			if (hibObject != null) {
				businessModel = toModel(hibObject);
			} else {
				logger.warn("Unable to load business model whose id is equal to [" + id + "]");
			}
			tx.commit();
		} catch (Exception e) {
			logger.error(e);
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("An unexpected error occured while loading model with id [" + id + "]", e);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		monitor.stop();
		return businessModel;
	}

	@Override
	public MetaModel loadMetaModelById(Integer id) {
		LogMF.debug(logger, "IN: id = [{0}]", id);

		MetaModel toReturn = null;
		Session session = null;
		Transaction transaction = null;

		try {
			if (id == null) {
				throw new IllegalArgumentException("Input parameter [id] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			SbiMetaModel hibModel = (SbiMetaModel) session.load(SbiMetaModel.class, id);
			logger.debug("Model loaded");

			toReturn = toModel(hibModel);

			transaction.rollback();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading model with id [" + id + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	@Override
	public MetaModel loadMetaModelByName(String name) {
		LogMF.debug(logger, "IN: name = [{0}]", name);

		MetaModel toReturn = null;
		Session session = null;
		Transaction transaction = null;

		try {
			if (name == null) {
				throw new IllegalArgumentException("Input parameter [name] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}
			Query query = null;
			query = session.createQuery(" from SbiMetaModel m where m.name = ?");
			query.setString(0, name);

			SbiMetaModel hibModel = (SbiMetaModel) query.uniqueResult();
			logger.debug("Model loaded");

			toReturn = toModel(hibModel);

			transaction.rollback();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading model with name [" + name + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	@Override
	public List<MetaModel> loadMetaModelByCategories(List<Integer> categories) {
		LogMF.debug(logger, "IN: category = [{0}]", categories);

		List<MetaModel> toReturn = new ArrayList<MetaModel>();
		Session session = null;
		Transaction transaction = null;

		try {
			if (categories == null || categories.size() == 0) {
				throw new IllegalArgumentException("Input parameter [category] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			String queryString = "from SbiMetaModel m where m.category in (";
			for (int i = 0; i < categories.size(); i++) {
				queryString = queryString + " ? ,";
			}
			queryString = queryString.substring(0, queryString.length() - 2) + ")";

			Query query = session.createQuery(queryString);

			for (int i = 0; i < categories.size(); i++) {
				query.setInteger(i, categories.get(i));
			}

			List list = query.list();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				toReturn.add(toModel((SbiMetaModel) it.next()));
			}
			logger.debug("Models loaded");

			transaction.rollback();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading model with categories [" + categories + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	@Override
	public List<MetaModel> loadMetaModelByFilter(String filter) {
		return loadMetaModelByFilter(filter, null);
	}

	@Override
	public List<MetaModel> loadMetaModelByFilter(String filter, List<Integer> categories) {
		LogMF.debug(logger, "IN: filter = [{0}]", filter);

		List<MetaModel> toReturn = new ArrayList<MetaModel>();
		Session session = null;
		Transaction transaction = null;

		try {
			if (filter == null) {
				throw new IllegalArgumentException("Input parameter [category] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			String queryString = " from SbiMetaModel m where " + filter;

			if (categories != null && categories.size() > 0) {
				queryString = queryString + " and   m.category in (";
				for (int i = 0; i < categories.size(); i++) {
					queryString = queryString + " ? ,";
				}
				queryString = queryString.substring(0, queryString.length() - 2) + ")";
			}

			Query query = session.createQuery(queryString);

			if (categories != null && categories.size() > 0) {
				for (int i = 0; i < categories.size(); i++) {
					query.setInteger(i, categories.get(i));
				}
			}

			List list = query.list();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				toReturn.add(toModel((SbiMetaModel) it.next()));
			}
			logger.debug("Models loaded");

			transaction.rollback();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading model with filter [" + filter + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	@Override
	public List<MetaModel> loadAllMetaModels() {
		logger.debug("IN");

		List<MetaModel> toReturn = new ArrayList<MetaModel>();
		Session session = null;
		Transaction transaction = null;

		try {

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query query = session.createQuery(" from SbiMetaModel");
			List list = query.list();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				toReturn.add(toModel((SbiMetaModel) it.next()));
			}
			logger.debug("Models loaded");

			transaction.rollback();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading models' list", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	@Override
	public List<SbiMetaModel> loadAllSbiMetaModels() {
		logger.debug("IN");

		List<SbiMetaModel> toReturn = new ArrayList<SbiMetaModel>();
		Session session = null;
		Transaction transaction = null;

		try {

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query query = session.createQuery(" from SbiMetaModel");
			List list = query.list();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				toReturn.add((SbiMetaModel) it.next());
			}
			logger.debug("Models loaded");

			transaction.rollback();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading models' list", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	@Override
	public void modifyMetaModel(MetaModel model) {
		LogMF.debug(logger, "IN: model = [{0}]", model);

		Session session = null;
		Transaction transaction = null;

		try {
			if (model == null) {
				throw new IllegalArgumentException("Input parameter [model] cannot be null");
			}
			if (model.getId() == null) {
				throw new IllegalArgumentException("Input model's id cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			SbiMetaModel hibModel = (SbiMetaModel) session.load(SbiMetaModel.class, model.getId());
			logger.debug("Model loaded");
			hibModel.setName(model.getName());
			hibModel.setDescription(model.getDescription());
			hibModel.setCategory(model.getCategory());
			hibModel.setTablePrefixLike(model.getTablePrefixLike());
			hibModel.setTablePrefixNotLike(model.getTablePrefixNotLike());
			hibModel.setModelLocker(model.getModelLocker());
			hibModel.setModelLocked(model.getModelLocked());
			hibModel.setSmartView(model.getSmartView());
			if (model.getDataSourceLabel() != null && !model.getDataSourceLabel().equals("")) {
				Criterion aCriterion = Expression.eq("label", model.getDataSourceLabel());
				Criteria criteria = session.createCriteria(SbiDataSource.class);
				criteria.add(aCriterion);

				SbiDataSource datasource = (SbiDataSource) criteria.uniqueResult();

				hibModel.setDataSource(datasource);
			}

			updateSbiCommonInfo4Update(hibModel);
			session.save(hibModel);

			transaction.commit();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while saving model [" + model + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");

	}

	@Override
	public void insertMetaModel(MetaModel model) {
		LogMF.debug(logger, "IN: model = [{0}]", model);

		Session session = null;
		Transaction transaction = null;

		try {
			if (model == null) {
				throw new IllegalArgumentException("Input parameter [model] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");

			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			SbiMetaModel hibModel = new SbiMetaModel();
			hibModel.setName(model.getName());
			hibModel.setDescription(model.getDescription());
			hibModel.setCategory(model.getCategory());
			hibModel.setTablePrefixLike(model.getTablePrefixLike());
			hibModel.setTablePrefixNotLike(model.getTablePrefixNotLike());
			hibModel.setModelLocker(model.getModelLocker());
			hibModel.setModelLocked(model.getModelLocked());
			hibModel.setSmartView(model.getSmartView());
			if (model.getDataSourceLabel() != null && !model.getDataSourceLabel().equals("")) {
				// Criterion aCriterion = Expression.eq("label", model.getDataSourceLabel());
				// Criteria criteria = session.createCriteria(SbiDataSource.class);
				// criteria.add(aCriterion);
				// criteria.add(Restrictions.eq("commonInfo.organization", getTenant()));
				// SbiDataSource datasource = (SbiDataSource) criteria.uniqueResult();
				Query hibQuery = null;
				hibQuery = session.createQuery(
						"select ds.sbiDataSource from SbiOrganizationDatasource ds where ds.sbiOrganizations.name = :tenantName and ds.sbiDataSource.label=:dataSourceLabel");
				hibQuery.setString("tenantName", getTenant());
				hibQuery.setString("dataSourceLabel", model.getDataSourceLabel());
				SbiDataSource datasource = (SbiDataSource) hibQuery.list().get(0);

				hibModel.setDataSource(datasource);
			}

			updateSbiCommonInfo4Insert(hibModel);
			session.save(hibModel);

			transaction.commit();

			model.setId(hibModel.getId());

		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while saving model [" + model + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");

	}

	@Override
	public void eraseMetaModel(Integer modelId) {
		LogMF.debug(logger, "IN: model = [{0}]", modelId);

		Session session = null;
		Transaction transaction = null;

		try {
			if (modelId == null) {
				throw new IllegalArgumentException("Input model's id cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			SbiMetaModel hibModel = (SbiMetaModel) session.load(SbiMetaModel.class, modelId);

			Set<SbiMetaModelParameter> metaModelParameters = hibModel.getSbiMetaModelParameters();
			if (metaModelParameters != null) {
				// delete dependencies between drivers
				Iterator itMetaModelParDep = metaModelParameters.iterator();
				BIMetaModelParameterDAOHibImpl metaModelParDAO = new BIMetaModelParameterDAOHibImpl();
				while (itMetaModelParDep.hasNext()) {
					SbiMetaModelParameter aSbiObjPar = (SbiMetaModelParameter) itMetaModelParDep.next();
					BIMetaModelParameter aBIMetaModelParameter = new BIMetaModelParameter();
					aBIMetaModelParameter.setId(aSbiObjPar.getMetaModelParId());
					metaModelParDAO.eraseBIMetaModelParameterDependencies(aBIMetaModelParameter, session);
				}

				// delete drivers associated to business model
				Iterator itMetaModelPar = metaModelParameters.iterator();
				while (itMetaModelPar.hasNext()) {
					SbiMetaModelParameter aSbiObjPar = (SbiMetaModelParameter) itMetaModelPar.next();
					BIMetaModelParameter aBIMetaModelParameter = new BIMetaModelParameter();
					aBIMetaModelParameter.setId(aSbiObjPar.getMetaModelParId());
					metaModelParDAO.eraseBIMetaModelParameter(aBIMetaModelParameter);
				}
			}

			session.delete(hibModel);
			transaction.commit();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while deleting model with id [" + modelId + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");

	}

	@Override
	public MetaModel toModel(SbiMetaModel hibModel) {
		logger.debug("IN");
		MetaModel toReturn = null;
		if (hibModel != null) {
			toReturn = new MetaModel();
			toReturn.setId(hibModel.getId());
			toReturn.setName(hibModel.getName());
			toReturn.setDescription(hibModel.getDescription());
			toReturn.setCategory(hibModel.getCategory());
			toReturn.setTablePrefixLike(hibModel.getTablePrefixLike());
			toReturn.setTablePrefixNotLike(hibModel.getTablePrefixNotLike());

			List metaModelParameters = new ArrayList();
			Set<SbiMetaModelParameter> hibMetaModelPars = hibModel.getSbiMetaModelParameters();
			if (hibMetaModelPars != null) {
				for (Iterator it = hibMetaModelPars.iterator(); it.hasNext();) {
					SbiMetaModelParameter aSbiMetaModelPar = (SbiMetaModelParameter) it.next();
					BIMetaModelParameter par = toBIMetaModelParameter(aSbiMetaModelPar);
					metaModelParameters.add(par);
				}
				toReturn.setDrivers(metaModelParameters);
			}

			if (hibModel.getDataSource() != null) {
				toReturn.setDataSourceLabel(DataSourceDAOHibImpl.toDataSource(hibModel.getDataSource()).getLabel());
				toReturn.setDataSourceId(DataSourceDAOHibImpl.toDataSource(hibModel.getDataSource()).getDsId());
			}
			toReturn.setModelLocked(hibModel.getModelLocked());
			toReturn.setModelLocker(hibModel.getModelLocker());
			toReturn.setSmartView(hibModel.getSmartView());
		}
		logger.debug("OUT");
		return toReturn;
	}

	public BIMetaModelParameter toBIMetaModelParameter(SbiMetaModelParameter hibMetaModelPar) {
		BIMetaModelParameter aBIMetaModelParameter = new BIMetaModelParameter();
		aBIMetaModelParameter.setId(hibMetaModelPar.getMetaModelParId());
		aBIMetaModelParameter.setLabel(hibMetaModelPar.getLabel());
		aBIMetaModelParameter.setModifiable(new Integer(hibMetaModelPar.getModFl().intValue()));
		aBIMetaModelParameter.setMultivalue(new Integer(hibMetaModelPar.getMultFl().intValue()));
		aBIMetaModelParameter.setBiMetaModelID(hibMetaModelPar.getSbiMetaModel().getId());
		aBIMetaModelParameter.setParameterUrlName(hibMetaModelPar.getParurlNm());
		aBIMetaModelParameter.setParID(hibMetaModelPar.getSbiParameter().getParId());
		aBIMetaModelParameter.setRequired(new Integer(hibMetaModelPar.getReqFl().intValue()));
		aBIMetaModelParameter.setVisible(new Integer(hibMetaModelPar.getViewFl().intValue()));
		aBIMetaModelParameter.setPriority(hibMetaModelPar.getPriority());
		aBIMetaModelParameter.setProg(hibMetaModelPar.getProg());
		Parameter parameter = new Parameter();
		parameter.setId(hibMetaModelPar.getSbiParameter().getParId());
		parameter.setType(hibMetaModelPar.getSbiParameter().getParameterTypeCode());
		aBIMetaModelParameter.setParameter(parameter);
		return aBIMetaModelParameter;
	}

	@Override
	public void insertMetaModelContent(Integer modelId, Content content) {

		LogMF.debug(logger, "IN: content = [{0}]", content);

		Session session = null;
		Transaction transaction = null;

		try {
			if (content == null) {
				throw new IllegalArgumentException("Input parameter [content] cannot be null");
			}
			if (modelId == null) {
				throw new IllegalArgumentException("Input parameter [modelId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			// set to not active the current active template
			String hql = " update SbiMetaModelContent mmc set mmc.active = ? where mmc.active = ? and mmc.model.id = ? ";
			Query query = session.createQuery(hql);
			query.setBoolean(0, false);
			query.setBoolean(1, true);
			query.setInteger(2, modelId.intValue());

			logger.debug("Updates the current content of model " + modelId + " with active = false.");
			query.executeUpdate();
			// get the next prog for the new content
			Integer maxProg = null;
			Integer nextProg = null;
			hql = " select max(mmc.prog) as maxprog from SbiMetaModelContent mmc where mmc.model.id = ? ";
			query = session.createQuery(hql);
			query.setInteger(0, modelId.intValue());
			List result = query.list();
			Iterator it = result.iterator();
			while (it.hasNext()) {
				maxProg = (Integer) it.next();
			}
			logger.debug("Current max prog : " + maxProg);
			if (maxProg == null) {
				nextProg = new Integer(1);
			} else {
				nextProg = new Integer(maxProg.intValue() + 1);
			}
			logger.debug("Next prog: " + nextProg);

			// store the model content
			SbiMetaModelContent hibContent = new SbiMetaModelContent();
			hibContent.setActive(new Boolean(true));
			hibContent.setCreationDate(content.getCreationDate());
			hibContent.setCreationUser(content.getCreationUser());
			hibContent.setFileName(content.getFileName());
			hibContent.setProg(nextProg);
			hibContent.setFileModel(content.getFileModel());
			hibContent.setContent(content.getContent());
			hibContent.setDimension(content.getDimension());
			SbiMetaModel sbiModel = (SbiMetaModel) session.load(SbiMetaModel.class, modelId);
			hibContent.setModel(sbiModel);
			updateSbiCommonInfo4Insert(hibContent);
			session.save(hibContent);
			transaction.commit();

		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while saving model content [" + content + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");

	}

	@Override
	public void modifyMetaModelContent(Integer modelId, Content content, Integer metaModelContentId) {

		LogMF.debug(logger, "IN: content = [{0}]", content);

		Session session = null;
		Transaction transaction = null;

		try {
			if (content == null) {
				throw new IllegalArgumentException("Input parameter [content] cannot be null");
			}
			if (modelId == null) {
				throw new IllegalArgumentException("Input parameter [modelId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			// set to not active the current active template
			String hql = " update SbiMetaModelContent mmc set mmc.active = ? where mmc.active = ? and mmc.model.id = ? ";
			Query query = session.createQuery(hql);
			query.setBoolean(0, false);
			query.setBoolean(1, true);
			query.setInteger(2, modelId.intValue());
			logger.debug("Updates the current content of model " + modelId + " with active = false.");
			query.executeUpdate();
			// get the next prog for the new content
			Integer maxProg = null;
			Integer nextProg = null;
			hql = " select max(mmc.prog) as maxprog from SbiMetaModelContent mmc where mmc.model.id = ? ";
			query = session.createQuery(hql);
			query.setInteger(0, modelId.intValue());
			List result = query.list();
			Iterator it = result.iterator();
			while (it.hasNext()) {
				maxProg = (Integer) it.next();
			}
			logger.debug("Current max prog : " + maxProg);
			if (maxProg == null) {
				nextProg = new Integer(1);
			} else {
				nextProg = new Integer(maxProg.intValue() + 1);
			}
			logger.debug("Next prog: " + nextProg);

			// store the model content
			SbiMetaModelContent hibContent = (SbiMetaModelContent) session.load(SbiMetaModelContent.class, metaModelContentId);
			hibContent.setActive(new Boolean(true));
			hibContent.setCreationDate(content.getCreationDate());
			hibContent.setCreationUser(content.getCreationUser());
			hibContent.setFileName(content.getFileName());
			hibContent.setProg(nextProg);
			hibContent.setContent(content.getContent());
			hibContent.setFileModel(content.getFileModel());

			hibContent.setDimension(content.getDimension());
			SbiMetaModel sbiModel = (SbiMetaModel) session.load(SbiMetaModel.class, modelId);
			hibContent.setModel(sbiModel);
			updateSbiCommonInfo4Insert(hibContent);
			session.save(hibContent);
			transaction.commit();

		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while saving model content [" + content + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");

	}

	@Override
	public void eraseMetaModelContent(Integer contentId) {
		LogMF.debug(logger, "IN: content = [{0}]", contentId);

		Session session = null;
		Transaction transaction = null;

		try {
			if (contentId == null) {
				throw new IllegalArgumentException("Input content's id cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			SbiMetaModelContent hibContent = (SbiMetaModelContent) session.load(SbiMetaModelContent.class, contentId);
			if (hibContent == null) {
				logger.warn("Content [" + hibContent + "] not found");
			} else {
				Integer modelId = hibContent.getModel().getId();
				boolean itWasActive = hibContent.getActive();
				session.delete(hibContent);
				if (itWasActive) {
					Query query = session.createQuery(" from SbiMetaModelContent mmc where mmc.model.id = " + modelId + " order by prog desc");
					List<SbiMetaModelContent> list = query.list();
					if (list != null && !list.isEmpty()) {
						SbiMetaModelContent first = list.get(0);
						first.setActive(true);
						session.save(first);
					}
				}
			}

			transaction.commit();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while deleting content with id [" + contentId + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");

	}

	@Override
	public Content loadMetaModelContentById(Integer contendId) {
		LogMF.debug(logger, "IN: id = [{0}]", contendId);

		Content toReturn = null;
		Session session = null;
		Transaction transaction = null;

		try {
			if (contendId == null) {
				throw new IllegalArgumentException("Input parameter [contendId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			SbiMetaModelContent hibContent = (SbiMetaModelContent) session.load(SbiMetaModelContent.class, contendId);
			logger.debug("Content loaded");

			toReturn = toContent(hibContent, true);

			transaction.rollback();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading content with id [" + contendId + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	@Override
	public Content loadActiveMetaModelContentById(Integer modelId) {
		LogMF.debug(logger, "IN: id = [{0}]", modelId);

		Content toReturn = null;
		Session session = null;
		Transaction transaction = null;

		try {
			if (modelId == null) {
				throw new IllegalArgumentException("Input parameter [modelId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query query = session.createQuery(" from SbiMetaModelContent mmc where mmc.model.id = ? and mmc.active = ? ");
			query.setInteger(0, modelId);
			query.setBoolean(1, true);
			SbiMetaModelContent hibContent = (SbiMetaModelContent) query.uniqueResult();
			logger.debug("Content loaded");

			if (hibContent != null) {
				toReturn = toContent(hibContent, true);
			}

			transaction.rollback();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading active content for model with id [" + modelId + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	@Override
	public Content loadActiveMetaModelContentByName(String modelName) {
		LogMF.debug(logger, "IN: name = [{0}]", modelName);

		Content toReturn = null;
		Session session = null;
		Transaction transaction = null;

		try {
			if (modelName == null) {
				throw new IllegalArgumentException("Input parameter [modelName] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query query = session.createQuery(" from SbiMetaModelContent mmc where mmc.model.name = ? and mmc.active = ? ");
			query.setString(0, modelName);
			query.setBoolean(1, true);
			SbiMetaModelContent hibContent = (SbiMetaModelContent) query.uniqueResult();
			logger.debug("Content loaded");

			if (hibContent != null && !hibContent.getFileName().endsWith(LOG_SUFFIX)) {
				toReturn = toContent(hibContent, true);
			}

			transaction.rollback();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading active content for model with id [" + modelName + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	@Override
	public long getActiveMetaModelContentLastModified(String modelName) {
		long lastModified = -1;

		Content content = loadActiveMetaModelContentByName(modelName);
		if (content != null && content.getCreationDate() != null) {
			lastModified = content.getCreationDate().getTime();
		}

		return lastModified;
	}

	@Override
	public List<Content> loadMetaModelVersions(Integer modelId) {
		LogMF.debug(logger, "IN: id = [{0}]", modelId);

		List<Content> toReturn = new ArrayList<Content>();
		Session session = null;
		Transaction transaction = null;

		try {
			if (modelId == null) {
				throw new IllegalArgumentException("Input parameter [modelId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query query = session.createQuery(" from SbiMetaModelContent mmc where mmc.model.id = ? order by mmc.prog desc");
			query.setInteger(0, modelId);
			List<SbiMetaModelContent> list = query.list();
			Iterator<SbiMetaModelContent> it = list.iterator();
			while (it.hasNext()) {
				toReturn.add(toContent(it.next(), false));
			}
			logger.debug("Contents loaded");

			transaction.rollback();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading active content for model with id [" + modelId + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	@Override
	public Content toContent(SbiMetaModelContent hibContent, boolean loadByteContent) {
		logger.debug("IN");
		Content toReturn = null;
		if (hibContent != null) {
			toReturn = new Content();
			toReturn.setId(hibContent.getId());
			toReturn.setCreationUser(hibContent.getCreationUser());
			toReturn.setCreationDate(hibContent.getCreationDate());
			toReturn.setActive(hibContent.getActive());
			toReturn.setFileName(hibContent.getFileName());
			toReturn.setDimension(hibContent.getDimension());
			toReturn.setProg(hibContent.getProg());
			if (loadByteContent) {
				toReturn.setContent(hibContent.getContent());
				toReturn.setFileModel(hibContent.getFileModel());
			}

			toReturn.setHasContent((hibContent.getContent() != null) ? true : false);
			toReturn.setHasFileModel((hibContent.getFileModel() != null) ? true : false);
			toReturn.setHasLog((hibContent.getContent() != null && hibContent.getFileName().endsWith(".log")) ? true : false);
		}
		logger.debug("OUT");
		return toReturn;
	}

	// redundant
	// private Content toContentWeb(SbiMetaModelContent hibContent, boolean loadByteContent) {
	// logger.debug("IN");
	// Content toReturn = null;
	// if (hibContent != null) {
	// toReturn = new Content();
	// toReturn.setId(hibContent.getId());
	// toReturn.setCreationUser(hibContent.getCreationUser());
	// toReturn.setCreationDate(hibContent.getCreationDate());
	// toReturn.setActive(hibContent.getActive());
	// toReturn.setFileName(hibContent.getFileName());
	// toReturn.setDimension(hibContent.getDimension());
	// if (loadByteContent) {
	// toReturn.setContent(hibContent.getFileModel());
	// }
	// }
	// logger.debug("OUT");
	// return toReturn;
	// }

	@Override
	public void setActiveVersion(Integer modelId, Integer contentId) {
		LogMF.debug(logger, "IN: modelId = [{0}], contentId = [{1}]", modelId, contentId);

		Session session = null;
		Transaction transaction = null;

		try {
			if (modelId == null) {
				throw new IllegalArgumentException("Input parameter [modelId] cannot be null");
			}
			if (contentId == null) {
				throw new IllegalArgumentException("Input parameter [contentId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			// set to not active the current active template
			String hql = " update SbiMetaModelContent mmc set mmc.active = ? where mmc.active = ? and mmc.model.id = ? ";
			Query query = session.createQuery(hql);
			query.setBoolean(0, false);
			query.setBoolean(1, true);
			query.setInteger(2, modelId.intValue());
			logger.debug("Updates the current content of model " + modelId + " with active = false.");
			query.executeUpdate();

			// set to active the new active template
			hql = " update SbiMetaModelContent mmc set mmc.active = ? where mmc.id = ? and mmc.model.id = ? ";
			query = session.createQuery(hql);
			query.setBoolean(0, true);
			query.setInteger(1, contentId);
			query.setInteger(2, modelId.intValue());
			logger.debug("Updates the current content " + contentId + " of model " + modelId + " with active = true.");
			query.executeUpdate();

			transaction.commit();

		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while saving active content [" + contentId + "] for model [" + modelId + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");

	}

	/**
	 * Locks model designed by Model id, returns the userId that locks the model (that could be different from current user if it was already blocked)
	 */
	@Override
	public String lockMetaModel(Integer metaModelId, String userId) {
		logger.debug("IN");
		String userBlocking = null;
		Session session = null;
		Transaction transaction = null;

		try {
			if (metaModelId == null) {
				throw new IllegalArgumentException("Input parameter [metaModelId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			// set to "not active" the current active model
			String hql = " update SbiMetaModel ar set ar.modelLocked = ?, ar.modelLocker = ? where (ar.modelLocked = ? OR ar.modelLocked is null)  and ar.id = ? ";
			Query query = session.createQuery(hql);
			query.setBoolean(0, true);
			query.setString(1, userId);
			query.setBoolean(2, false);
			query.setInteger(3, metaModelId);

			logger.debug("Lock the metamodel with id " + metaModelId + "");
			query.executeUpdate();
			transaction.commit();

			// check if current user has the lock
			SbiMetaModel hibMetaModel = (SbiMetaModel) session.load(SbiMetaModel.class, metaModelId);
			logger.debug("MetaModel loaded");
			MetaModel model = toModel(hibMetaModel);

			userBlocking = model.getModelLocker();
			if (model.getModelLocker() != null && model.getModelLocker().equals(userId)) {
				logger.debug("Model was locked by current user");

			} else {
				logger.warn("Model was already blocked by user " + model.getModelLocker());
			}

		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while locking for user[" + userId + "] the metamodel [" + metaModelId + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");
		return userBlocking;
	}

	/**
	 * Unlock model designed by MetaModel id, returns user currently locking the model, that will be null if method has success, but could be from a different
	 * user if fails
	 */
	@Override
	public String unlockMetaModel(Integer metaModelId, String userId) {
		logger.debug("IN");
		String userLocking = null;

		Session session = null;
		Transaction transaction = null;

		try {

			if (metaModelId == null) {
				logger.error("Input parameter [metaModelId] cannot be null");
				throw new IllegalArgumentException("Input parameter [metaModelId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			// check if current user has the lock
			SbiMetaModel hibMetaModel = (SbiMetaModel) session.load(SbiMetaModel.class, metaModelId);

			if (hibMetaModel == null) {
				logger.error("Could not find metamodel for id " + hibMetaModel);
				throw new SpagoBIDAOException("Could not find metamodel for id " + hibMetaModel);
			}

			logger.debug("MetaModel loaded");
			MetaModel model = toModel(hibMetaModel);
			// Admin can force unlock from other
			// boolean isAdmin = UserUtilities.isAdministrator(UserUtilities.getUserProfile(userId));

			if ((model.getModelLocked().equals(true) && model.getModelLocker().equals(userId))) {
				// set to "not active" the current active model
				String hql = " update SbiMetaModel ar set ar.modelLocked = ?, ar.modelLocker = ? where ar.modelLocked = ?  and ar.id = ? ";
				Query query = session.createQuery(hql);
				query.setBoolean(0, false);
				query.setString(1, null);
				query.setBoolean(2, true);
				query.setInteger(3, metaModelId);

				logger.debug("Unlock the metamodel with id " + metaModelId + "");
				query.executeUpdate();

				userLocking = null;
				transaction.commit();

			} else {
				logger.warn("Could not unlock model because it is locked by another user than current one: " + model.getModelLocker());
				userLocking = model.getModelLocker();
			}

		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while unlocking for user[" + userId + "] the metamodel [" + metaModelId + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");
		return userLocking;
	}

	@Override
	public Content loadActiveMetaModelWebContentByName(String modelName) {
		LogMF.debug(logger, "IN: name = [{0}]", modelName);

		Content toReturn = null;
		Session session = null;
		Transaction transaction = null;

		try {
			if (modelName == null) {
				throw new IllegalArgumentException("Input parameter [modelName] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query query = session.createQuery(" from SbiMetaModelContent mmc where mmc.model.name = ? and mmc.active = ? ");
			query.setString(0, modelName);
			query.setBoolean(1, true);
			SbiMetaModelContent hibContent = (SbiMetaModelContent) query.uniqueResult();
			logger.debug("Content loaded");

			toReturn = toContent(hibContent, true);

			transaction.rollback();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading active content for model with id [" + modelName + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	@Override
	public Content lastFileModelMeta(Integer modelId) {
		LogMF.debug(logger, "IN: id = [{0}]", modelId);

		Content toReturn = new Content();
		Session session = null;
		Transaction transaction = null;

		try {
			if (modelId == null) {
				throw new IllegalArgumentException("Input parameter [modelId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query query = session.createQuery(" from SbiMetaModelContent mmc where mmc.model.id = ? and mmc.fileModel is not null order by mmc.prog desc");
			query.setInteger(0, modelId);
			List<SbiMetaModelContent> list = query.list();
			if (list != null && list.size() > 0) {
				toReturn = toContent(list.get(0), true);
			}
			transaction.rollback();
		} catch (Exception e) {
			logException(e);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading active content for model with id [" + modelId + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

}