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
package it.eng.spagobi.tools.objmetadata.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.objmetadata.metadata.SbiObjMetacontents;
import it.eng.spagobi.tools.objmetadata.metadata.SbiObjMetadata;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Defines the Hibernate implementations for all DAO methods, for a object metadata.
 */
public class ObjMetadataDAOHibImpl extends AbstractHibernateDAO implements IObjMetadataDAO {
	private static Logger logger = Logger.getLogger(ObjMetadataDAOHibImpl.class);

	/**
	 * Load object's metadata by type
	 *
	 * @param type the type(SHORT_TEXT or LONG_TEXT)
	 *
	 * @return the metadata
	 *
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List<ObjMetadata> loadObjMetaDataListByType(String type) throws EMFUserError {
		logger.debug("IN");
		List<ObjMetadata> toReturn = new ArrayList<>();
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery(" from SbiObjMetadata meta where meta.dataType.valueCd = ? and meta.dataType.domainCd='OBJMETA_DATA_TYPE'");
			hibQuery.setString(0, type);

			logger.debug("Type setted: " + (type != null ? type : ""));

			List<SbiObjMetadata> hibList = hibQuery.list();
			if (hibList != null && !hibList.isEmpty()) {
				Iterator<SbiObjMetadata> it = hibList.iterator();

				while (it.hasNext()) {
					toReturn.add(toObjMetadata(it.next()));
				}
			}

		} catch (HibernateException he) {
			logger.error("Error while loading the metadata with type " + (type != null ? type : ""), he);

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
	 * Load object's metadata by id.
	 *
	 * @param id the identifier
	 *
	 * @return the metadata
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#loadObjMetaDataByID(java.lang.Integer)
	 */
	@Override
	public ObjMetadata loadObjMetaDataByID(Integer id) throws EMFUserError {

		logger.debug("IN");
		ObjMetadata toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjMetadata hibDataSource = (SbiObjMetadata) aSession.load(SbiObjMetadata.class, id);
			toReturn = toObjMetadata(hibDataSource);
			tx.commit();

		} catch (HibernateException he) {
			logger.error("Error while loading the metadata with id " + id.toString(), he);

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
	 * Load object's metadata by label.
	 *
	 * @param label the label
	 *
	 * @return the metadata
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#loadObjMetadataByLabel(java.lang.String)
	 */
	@Override
	public ObjMetadata loadObjMetadataByLabel(String label) throws EMFUserError {
		logger.debug("IN");
		ObjMetadata toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterion = null;
			labelCriterion = Restrictions.eq("label", label);
			Criteria criteria = tmpSession.createCriteria(SbiObjMetadata.class);
			criteria.add(labelCriterion);
			SbiObjMetadata hibMeta = (SbiObjMetadata) criteria.uniqueResult();
			if (hibMeta == null)
				return null;
			toReturn = toObjMetadata(hibMeta);

			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading the metadata with label " + label, he);
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
	public List<ObjMetadata> loadObjMetadataByBIObjectID(Integer biobjId) throws EMFUserError {

		logger.debug("IN");
		List<ObjMetadata> toReturn = new ArrayList<>();
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			StringBuilder sb = new StringBuilder();
			sb.append(" select t1 ");
			sb.append(" from SbiObjMetadata t1, SbiObjMetacontents t2");
			sb.append(" where t1.objMetaId = t2.objmetaId");
			sb.append(" and t2.sbiObjects.biobjId = :biobjId");

			Query query = aSession.createQuery(sb.toString());
			query.setInteger("biobjId", biobjId);

			List<SbiObjMetadata> tmpList = query.list();
			for (SbiObjMetadata obj : tmpList) {
				SbiObjMetadata som = obj;
				toReturn.add(toObjMetadata(som));
			}
			tx.commit();

		} catch (HibernateException he) {
			logger.error("Error while loading the metadata with biobjId " + biobjId.toString(), he);

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
	 * Load all metadata.
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#loadAllObjMetadata()
	 */
	@Override
	public List<ObjMetadata> loadAllObjMetadata() throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<ObjMetadata> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiObjMetadata");

			List<SbiObjMetadata> hibList = hibQuery.list();
			Iterator<SbiObjMetadata> it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toObjMetadata(it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading all metadata ", he);

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
	 * Load all metadata filtered by label comparison.
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#loadAllObjMetadata()
	 */
	@Override
	public List<ObjMetadata> loadAllObjMetadataByLabelAndCase(String label, boolean caseSensitive) throws EMFUserError {
		logger.debug("IN");
		List<ObjMetadata> toReturn = new ArrayList<>();
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterion = null;
			if (caseSensitive) {
				labelCriterion = Restrictions.eq("label", label);
			} else {
				labelCriterion = Restrictions.eq("label", label).ignoreCase();

			}
			Criteria criteria = tmpSession.createCriteria(SbiObjMetadata.class);
			criteria.add(labelCriterion);
			List<SbiObjMetadata> hibMeta = criteria.list();
			if (hibMeta == null)
				return null;

			for (SbiObjMetadata object : hibMeta) {

				toReturn.add(toObjMetadata(object));
			}

			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading the metadata with label " + label, he);
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

	/**
	 * Modify metadata.
	 *
	 * @param aObjMetadata the metadata
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#modifyObjMetadata(it.eng.spagobi.tools.objmetadata.bo.ObjMetadata)
	 */
	@Override
	public void modifyObjMetadata(ObjMetadata aObjMetadata) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion aCriterion = Restrictions.eq("valueId", aObjMetadata.getDataType());
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(aCriterion);

			SbiDomains dataType = (SbiDomains) criteria.uniqueResult();

			if (dataType == null) {
				logger.error("The Domain with value_id= " + aObjMetadata.getDataType() + " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1035);
			}

			SbiObjMetadata hibMeta = (SbiObjMetadata) aSession.load(SbiObjMetadata.class, aObjMetadata.getObjMetaId());
			hibMeta.setLabel(aObjMetadata.getLabel());
			hibMeta.setName(aObjMetadata.getName());
			hibMeta.setDescription(aObjMetadata.getDescription());
			hibMeta.setDataType(dataType);
			updateSbiCommonInfo4Update(hibMeta);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			if (he instanceof ObjectNotFoundException)
				throw he;
			else
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}

	}

	/**
	 * Insert object's metadata.
	 *
	 * @param aObjMetadata the metadata
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#insertObjMetadata(it.eng.spagobi.tools.objmetadata.bo.ObjMetadata)
	 */
	@Override
	public int insertObjMetadata(ObjMetadata aObjMetadata) throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		int toReturn;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criterion aCriterion = Restrictions.eq("valueId", aObjMetadata.getDataType());
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(aCriterion);

			SbiDomains dataType = (SbiDomains) criteria.uniqueResult();

			if (dataType == null) {
				logger.error("The Domain with value_id= " + aObjMetadata.getDataType() + " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1035);
			}
			Date now = new Date();
			// store the object note
			SbiObjMetadata hibMeta = new SbiObjMetadata();
			hibMeta.setLabel(aObjMetadata.getLabel());
			hibMeta.setName(aObjMetadata.getName());
			hibMeta.setDescription(aObjMetadata.getDescription());
			hibMeta.setDataType(dataType);
			hibMeta.setCreationDate(now);
			updateSbiCommonInfo4Insert(hibMeta);
			toReturn = (int) aSession.save(hibMeta);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
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
		return toReturn;
	}

	private Integer loadDataTypeIdByDomainValue(String domain) throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer toReturn;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criterion valueCriterion = Restrictions.eq("valueCd", domain);
			Criterion domainCriterion = Restrictions.eq("domainCd", "OBJMETA_DATA_TYPE");
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(valueCriterion);
			criteria.add(domainCriterion);

			SbiDomains dataType = (SbiDomains) criteria.uniqueResult();

			if (dataType == null) {
				logger.error("The Domain with valueCd= " + domain + " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1035);
			}

			toReturn = dataType.getValueId();
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
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
		return toReturn;
	}

	@Override
	public int insertOrUpdateObjMetadata(ObjMetadata objMetadata) throws EMFUserError {
		Integer toReturn = -1;
		objMetadata.setDataType(loadDataTypeIdByDomainValue(objMetadata.getDataTypeCode()));
		if (objMetadata.getObjMetaId() != null) {
			try {
				modifyObjMetadata(objMetadata);
				toReturn = objMetadata.getObjMetaId();
			} catch (ObjectNotFoundException e) {
				String message = "Cannot update item with id {" + objMetadata.getObjMetaId() + "} because it doesn't exist";
				logger.error(message);
				throw new SpagoBIRuntimeException(message);
			}
		} else {
			toReturn = insertObjMetadata(objMetadata);
		}
		return toReturn;
	}

	/**
	 * Erase object's metadata
	 *
	 * @param aObjMetadata the metadata
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#eraseObjMetadata(it.eng.spagobi.tools.objmetadata.bo.ObjMetadata)
	 */
	@Override
	public void eraseObjMetadata(ObjMetadata aObjMetadata) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjMetadata hibMeta = (SbiObjMetadata) aSession.load(SbiObjMetadata.class, aObjMetadata.getObjMetaId());

			// delete metadatacontents eventually associated
			List<ObjMetacontent> metaContents = DAOFactory.getObjMetacontentDAO().loadAllObjMetacontent();
			IObjMetacontentDAO objMetaContentDAO = DAOFactory.getObjMetacontentDAO();
			if (metaContents != null && !metaContents.isEmpty()) {
				Iterator<ObjMetacontent> it = metaContents.iterator();
				while (it.hasNext()) {
					ObjMetacontent objMetadataCont = it.next();
					if (objMetadataCont != null && objMetadataCont.getObjmetaId().equals(hibMeta.getObjMetaId())) {
						objMetaContentDAO.eraseObjMetadata(objMetadataCont);
					}
				}
			}

			aSession.delete(hibMeta);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while erasing the metadata with id " + ((aObjMetadata == null) ? "" : String.valueOf(aObjMetadata.getObjMetaId())), he);

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

	@Override
	public void eraseObjMetadataById(int id) throws EMFUserError {
		ObjMetadata meta = this.loadObjMetaDataByID(id);
		this.eraseObjMetadata(meta);
	}

	/**
	 * Checks for bi obj associated.
	 *
	 * @param id the metadata id
	 *
	 * @return true, if checks for bi obj associated
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#hasBIObjAssociated(java.lang.String)
	 */
	@Override
	public boolean hasBIObjAssociated(String id) throws EMFUserError {

		logger.debug("IN");
		boolean bool = false;

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Integer idInt = Integer.parseInt(id);

			String hql = " from SbiObjMetacontents c where c.objmetaId = ? and c.sbiObjects is not null";
			Query aQuery = aSession.createQuery(hql);
			aQuery.setInteger(0, idInt.intValue());
			List<SbiObjMetacontents> biObjectsAssocitedWithObj = aQuery.list();
			if (!biObjectsAssocitedWithObj.isEmpty())
				bool = true;
			else
				bool = false;
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while getting the objects associated with the metadata with id " + id, he);

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
	 * Checks for bi subobject associated.
	 *
	 * @param id the metadata id
	 *
	 * @return true, if checks for bi subobjects associated
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#hasSubObjAssociated(java.lang.String)
	 */
	@Override
	public boolean hasSubObjAssociated(String id) throws EMFUserError {
		logger.debug("IN");
		boolean bool = false;

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Integer idInt = Integer.parseInt(id);

			String hql = " from SbiObjMetacontents c where c.objmetaId = ? and c.sbiSubObjects is not null";
			Query aQuery = aSession.createQuery(hql);
			aQuery.setInteger(0, idInt.intValue());
			List<SbiObjMetacontents> biObjectsAssocitedWithSubobj = aQuery.list();
			if (!biObjectsAssocitedWithSubobj.isEmpty())
				bool = true;
			else
				bool = false;
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while getting the engines associated with the data source with id " + id, he);

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
	 * From the hibernate SbiObjMetadata at input, gives the corrispondent <code>ObjMetadata</code> object.
	 *
	 * @param hibObjMetadata The hybernate metadata
	 *
	 * @return The corrispondent <code>ObjMetadata</code> object
	 */
	private ObjMetadata toObjMetadata(SbiObjMetadata hibObjMetadata) {
		ObjMetadata meta = new ObjMetadata();

		meta.setObjMetaId(hibObjMetadata.getObjMetaId());
		meta.setLabel(hibObjMetadata.getLabel());
		meta.setName(hibObjMetadata.getName());
		meta.setDescription(hibObjMetadata.getDescription());
		meta.setDataType(hibObjMetadata.getDataType().getValueId());
		meta.setDataTypeCode(hibObjMetadata.getDataType().getValueCd());
		meta.setCreationDate(hibObjMetadata.getCreationDate());

		return meta;
	}
}
