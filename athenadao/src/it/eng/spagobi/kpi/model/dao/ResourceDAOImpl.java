/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.model.dao;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModelResources;
import it.eng.spagobi.kpi.model.metadata.SbiResources;
import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.profiling.bean.SbiExtUserRolesId;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bean.SbiUserAttributesId;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.JDBCSharedConnectionDataProxy;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.exception.ConstraintViolationException;

public class ResourceDAOImpl extends AbstractHibernateDAO implements
		IResourceDAO {

	static private Logger logger = Logger.getLogger(ResourceDAOImpl.class);

	public void modifyResource(Resource resource) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Integer resourceId = resource.getId();
			String tableName = resource.getTable_name();
			String columnName = resource.getColumn_name();
			String resourceName = resource.getName();
			String resourceDescription = resource.getDescr();
			String resourceCode = resource.getCode();

			SbiDomains sbiDomain = (SbiDomains) aSession.load(SbiDomains.class,
					resource.getTypeId());
			SbiResources sbiResource = (SbiResources) aSession.load(
					SbiResources.class, resource.getId());

			sbiResource.setTableName(tableName);
			sbiResource.setColumnName(columnName);
			sbiResource.setResourceName(resourceName);
			sbiResource.setResourceDescr(resourceDescription);
			sbiResource.setResourceCode(resourceCode);
			sbiResource.setType(sbiDomain);
			updateSbiCommonInfo4Update(sbiResource);
			aSession.update(sbiResource);
			tx.commit();

		} catch (ConstraintViolationException cve) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.info("Impossible to modify the resource", cve);
			throw new EMFUserError(EMFErrorSeverity.WARNING, 10118);

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
	}

	public Integer insertResource(Resource toCreate) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer idToReturn;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiResources hibResource = new SbiResources();
			hibResource.setResourceName(toCreate.getName());
			hibResource.setResourceDescr(toCreate.getDescr());
			hibResource.setResourceCode(toCreate.getCode());
			hibResource.setTableName(toCreate.getTable_name());
			hibResource.setColumnName(toCreate.getColumn_name());
			SbiDomains sbiDomains = (SbiDomains) aSession.load(
					SbiDomains.class, toCreate.getTypeId());
			hibResource.setType(sbiDomains);
			updateSbiCommonInfo4Insert(hibResource);
			idToReturn = (Integer) aSession.save(hibResource);
			tx.commit();

		} catch (ConstraintViolationException cve) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.info("Impossible to insert the resource", cve);
			throw new EMFUserError(EMFErrorSeverity.WARNING, 10118);

		} catch (HibernateException he) {
			logger.error("Error while inserting the KpiResource ", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10117);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return idToReturn;
	}

	public Resource loadResourceById(Integer id) throws EMFUserError {
		logger.debug("IN");
		Resource toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiResources hibSbiResource = (SbiResources) aSession.load(
					SbiResources.class, id);
			toReturn = toResource(hibSbiResource);
		} catch (HibernateException he) {
			logger.error("Error while loading the Resource with id "
					+ ((id == null) ? "" : id.toString()), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10113);

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

	public Resource toResource(SbiResources r) {

		logger.debug("IN");
		Resource toReturn = new Resource();

		String coumn_name = r.getColumnName();
		String name = r.getResourceName();
		String table_name = r.getTableName();
		String descr = r.getResourceDescr();
		String code = r.getResourceCode();
		SbiDomains d = r.getType();
		String type = d.getValueCd();
		Integer resourceId = r.getResourceId();
		Integer typeId = d.getValueId();

		toReturn.setColumn_name(coumn_name);
		logger.debug("Resource columnName setted:" + coumn_name);
		toReturn.setName(name);
		logger.debug("Resource name setted:" + name);
		toReturn.setDescr(descr);
		logger.debug("Resource description setted:" + descr);
		toReturn.setCode(code);
		logger.debug("Resource code setted:" + code);
		toReturn.setTable_name(table_name);
		logger.debug("Resource table_name setted:" + table_name);
		toReturn.setType(type);
		logger.debug("Resource type setted:" + type);
		toReturn.setTypeId(typeId);
		logger.debug("Resource typeID setted");
		toReturn.setId(resourceId);
		logger.debug("Resource ID setted");

		logger.debug("OUT");
		return toReturn;
	}

	public Resource toResource(SbiKpiModelResources re) {

		logger.debug("IN");
		Resource toReturn = new Resource();

		SbiResources r = re.getSbiResources();
		toReturn = toResource(r);
		logger.debug("OUT");
		return toReturn;
	}

	public SbiResources toSbiResource(Resource r) throws EMFUserError {

		logger.debug("IN");
		SbiResources toReturn = new SbiResources();
		String columnName = r.getColumn_name();
		String resourceName = r.getName();
		String resourceDescr = r.getDescr();
		String resourceCode = r.getCode();
		String tableName = r.getTable_name();
		Integer resourceId = r.getId();
		String type = r.getType();
		Domain domain = DAOFactory.getDomainDAO().loadDomainByCodeAndValue(
				"RESOURCE", type);
		SbiDomains sbiDomains = new SbiDomains();
		sbiDomains.setDomainCd(domain.getDomainCode());
		sbiDomains.setDomainNm(domain.getDomainName());
		sbiDomains.setValueCd(domain.getValueCd());
		sbiDomains.setValueDs(domain.getValueDescription());
		sbiDomains.setValueId(domain.getValueId());
		sbiDomains.setValueNm(domain.getValueName());

		toReturn.setColumnName(columnName);
		logger.debug("SbiResource columnName setted");
		toReturn.setResourceId(resourceId);
		logger.debug("SbiResource ID setted");
		toReturn.setResourceName(resourceName);
		logger.debug("SbiResource resourceName setted");
		toReturn.setResourceDescr(resourceDescr);
		logger.debug("SbiResource resourceDescr setted");
		toReturn.setResourceCode(resourceCode);
		logger.debug("SbiResource resourceCode setted");
		toReturn.setType(sbiDomains);
		logger.debug("SbiResource sbiDomains setted");
		toReturn.setTableName(tableName);
		logger.debug("SbiResource tableName setted");

		logger.debug("OUT");
		return toReturn;
	}

	public void deleteResource(Integer resouceId) throws EMFUserError {
		Session aSession = getSession();
		Transaction tx = null;
		try {
			tx = aSession.beginTransaction();
			SbiResources sbiResource = (SbiResources) aSession.load(
					SbiResources.class, resouceId);
			aSession.delete(sbiResource);

			tx.commit();

		} catch (ConstraintViolationException cve) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.error("Impossible to delete a Resource", cve);
			throw new EMFUserError(EMFErrorSeverity.WARNING, 10014);

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.error("Error while delete a Resource ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

		} finally {
			aSession.close();
		}
	}

	public List loadResourcesList(String fieldOrder, String typeOrder)
			throws EMFUserError {
		logger.debug("IN");
		List toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = new ArrayList();
			List toTransform = null;

			if (fieldOrder != null && typeOrder != null) {
				Criteria crit = aSession.createCriteria(SbiResources.class);
				if (typeOrder.toUpperCase().trim().equals("ASC"))
					crit.addOrder(Order.asc(getResourcesProperty(fieldOrder)));
				if (typeOrder.toUpperCase().trim().equals("DESC"))
					crit.addOrder(Order.desc(getResourcesProperty(fieldOrder)));
				toTransform = crit.list();
			} else {
				toTransform = aSession.createQuery("from SbiResources order by resourceName").list();
			}

			if(toTransform != null){
				for (Iterator iterator = toTransform.iterator(); iterator.hasNext();) {
					SbiResources hibResource = (SbiResources) iterator.next();
					Resource resource = toResource(hibResource);
					toReturn.add(resource);
				}
			}

		} catch (HibernateException he) {
			logger.error("Error while loading the list of Resources", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return toReturn;
	}
	
	public List loadPagedResourcesList(Integer offset, Integer fetchSize)throws EMFUserError {
	logger.debug("IN");
	List toReturn = null;
	Session aSession = null;
	Transaction tx = null;
	Integer resultNumber;
	Query hibernateQuery;
	
	try {
		aSession = getSession();
		tx = aSession.beginTransaction();
		toReturn = new ArrayList();
		List toTransform = null;
	
		String hql = "select count(*) from SbiResources ";
		Query hqlQuery = aSession.createQuery(hql);
		resultNumber = new Integer(((Long)hqlQuery.uniqueResult()).intValue());
		offset = offset < 0 ? 0 : offset;
		if(resultNumber > 0) {
			fetchSize = (fetchSize > 0)? Math.min(fetchSize, resultNumber): resultNumber;
		}
		
		hibernateQuery = aSession.createQuery("from SbiResources order by resourceName");
		hibernateQuery.setFirstResult(offset);
		if(fetchSize > 0) hibernateQuery.setMaxResults(fetchSize);			

		toTransform = hibernateQuery.list();	
		
	
		if(toTransform != null){
			for (Iterator iterator = toTransform.iterator(); iterator.hasNext();) {
				SbiResources hibResource = (SbiResources) iterator.next();
				Resource resource = toResource(hibResource);
				toReturn.add(resource);
			}
		}	
	} catch (HibernateException he) {
		logger.error("Error while loading the list of Resources", he);	
		if (tx != null)
			tx.rollback();	
		throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);
	
	} finally {
		if (aSession != null) {
			if (aSession.isOpen())
				aSession.close();
			logger.debug("OUT");
		}
	}
	return toReturn;
	}
	
	public Integer countResources()throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
		
			String hql = "select count(*) from SbiResources ";
			Query hqlQuery = aSession.createQuery(hql);
			resultNumber = new Integer(((Long)hqlQuery.uniqueResult()).intValue());

		} catch (HibernateException he) {
			logger.error("Error while loading the list of Resources", he);	
			if (tx != null)
				tx.rollback();	
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);
		
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return resultNumber;
	}
	
	public Resource loadResourcesByNameAndModelInst(String resourceName)
			throws EMFUserError {
		logger.debug("IN");
		Resource toReturn = new Resource();
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria finder = aSession.createCriteria(SbiResources.class);
			finder.add(Expression.eq("resourceName", resourceName));

			List l = finder.list();
			if (l != null && !l.isEmpty()) {
				if (l.size() == 1) {
					SbiResources hibResource = (SbiResources) l.get(0);
					toReturn = toResource(hibResource);
				} else {
					logger
							.debug("More Resources with same resourceName exist check the DB tables sbi_resources and sbi_kpi_model_resources");
				}
			}

		} catch (HibernateException he) {
			logger.error("Error while loading the Resource", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return toReturn;
	}

	public Resource loadResourceByCode(String resourceCode) throws EMFUserError {
		// TODO to be controlled
		logger.debug("IN");
		Resource toReturn = new Resource();
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria finder = aSession.createCriteria(SbiResources.class);
			finder.add(Expression.eq("resourceCode", resourceCode));

			SbiResources hibResource = (SbiResources) finder.uniqueResult();
			if(hibResource != null){
				toReturn = toResource(hibResource);
			}

		} catch (HibernateException he) {
			logger.error("Error while loading the Resource", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return toReturn;
	}

	private String getResourcesProperty(String property) {
		String toReturn = null;
		if (property != null && property.equals("NAME"))
			toReturn = "resourceName";
		if (property != null && property.equals("SELECTED"))
			toReturn = "resourceName";
		return toReturn;
	}

}
