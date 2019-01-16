/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.tools.tag.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetId;
import it.eng.spagobi.tools.tag.SbiDatasetTag;
import it.eng.spagobi.tools.tag.SbiDatasetTagId;
import it.eng.spagobi.tools.tag.SbiTag;

public class SbiTagDAOImpl extends AbstractHibernateDAO implements ISbiTagDAO {

	static private Logger logger = Logger.getLogger(SbiTagDAOImpl.class);

	@Override
	public List<SbiTag> loadTags() {
		logger.debug("IN");
		Session session = null;
		List<SbiTag> toReturn = new ArrayList<>();
		try {
			session = getSession();
			Query query = session.createQuery("from SbiTag");
			toReturn = query.list();
		} catch (HibernateException e) {
			logException(e);
			throw new RuntimeException(e);
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public List<SbiTag> loadTagsByDatasetId(SbiDataSetId dsId) {
		logger.debug("IN");
		Session session = null;
		List<SbiTag> toReturn = new ArrayList<>();
		try {
			session = getSession();
			List<Integer> tagIds = session.createQuery(
					"select dst.dsTagId.tagId from SbiDatasetTag dst where dst.dsTagId.dsId = :dsId and dst.dsTagId.versionNum = :versionNum and dst.dsTagId.organization = :organization")
					.setInteger("dsId", dsId.getDsId()).setInteger("versionNum", dsId.getVersionNum()).setString("organization", dsId.getOrganization()).list();
			if (!tagIds.isEmpty()) {
				Query query = session.createQuery("from SbiTag t where t.tagId in (:tagIds)");
				query.setParameterList("tagIds", tagIds);
				toReturn = query.list();
			}
		} catch (HibernateException e) {
			logException(e);
			throw new RuntimeException(e);
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Returns SbiTag object or null
	 */
	@Override
	public SbiTag loadTagById(Integer id) {
		logger.debug("IN");
		SbiTag toReturn = null;
		Session session = null;
		try {
			session = getSession();
			Query query = session.createQuery("from SbiTag t where t.tagId = :tagId");
			query.setInteger("tagId", id);
			toReturn = (SbiTag) query.uniqueResult();
		} catch (HibernateException e) {
			logException(e);
			throw new RuntimeException(e);
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Returns SbiTag object or null
	 */
	@Override
	public SbiTag loadTagByName(String name) {
		logger.debug("IN");
		SbiTag toReturn = null;
		Session session = null;
		try {
			session = getSession();
			Query query = session.createQuery("from SbiTag t where t.name = :name");
			query.setString("name", name);
			toReturn = (SbiTag) query.uniqueResult();
		} catch (HibernateException e) {
			logException(e);
			throw new RuntimeException(e);
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public List<SbiDataSet> loadDatasetsByTagId(Integer tagId) {
		logger.debug("IN");
		List<SbiDataSet> toReturn = new ArrayList<>();
		Session session = null;
		try {
			session = getSession();
			Query query = session.createQuery("select dst.dataSet from SbiDatasetTag dst where dst.dsTagId.tagId = :tagId");
			query.setInteger("tagId", tagId);
			toReturn = query.list();
		} catch (Exception e) {
			logException(e);
			throw new RuntimeException(e);
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public void insertTag(SbiTag tag) {
		logger.debug("IN");
		Session session = null;
		Transaction tx = null;
		try {
			session = getSession();
			tx = session.beginTransaction();
			updateSbiCommonInfo4Insert(tag);
			session.save(tag);
			tx.commit();
			session.flush();
		} catch (HibernateException e) {
			logException(e);
			if (tx != null)
				tx.rollback();
			throw new RuntimeException(e);
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}
		logger.debug("OUT");
	}

	@Override
	public List<SbiDatasetTag> loadDatasetTags(Integer dsId) {
		logger.debug("IN");
		List<SbiDatasetTag> toReturn = new ArrayList<>();
		Session session = null;
		try {
			session = getSession();
			Query query = session.createQuery("from SbiDatasetTag dst where dst.dsTagId.dsId = :dsId");
			query.setInteger("dsId", dsId);
			toReturn = query.list();
		} catch (Exception e) {
			logException(e);
			throw new RuntimeException(e);
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * @return List of new inserted Tags
	 */
	@Override
	public List<SbiTag> associateTagsToDatasetVersion(SbiDataSetId dsId, JSONArray tagsToAdd) {
		logger.debug("IN");
		Session session = null;
		Transaction tx = null;
		List<SbiTag> toReturn = new ArrayList<>();
		List<SbiTag> tagsToAssociate = new ArrayList<>();
		try {
			List<String> tagsToInsert = new ArrayList<>();
			SbiDataSet dataSet = new SbiDataSet(dsId);
			for (int i = 0; i < tagsToAdd.length(); i++) {
				JSONObject tagObj = tagsToAdd.getJSONObject(i);
				String tagName = tagObj.getString("name");

				Integer tagId = tagObj.optInt("tagId");
				if (tagId == 0) {
					tagsToInsert.add(tagName);
				} else {
					SbiTag existingTag = new SbiTag(tagId, tagName);
					tagsToAssociate.add(existingTag);
				}
			}

			session = getSession();
			tx = session.beginTransaction();

			if (!tagsToInsert.isEmpty()) {
				toReturn = insertTags(tagsToInsert, session);
				for (SbiTag newTag : toReturn) {
					tagsToAssociate.add(newTag);
				}
			}

			for (SbiTag tag : tagsToAssociate) {
				Integer tagId = tag.getTagId();
				SbiDatasetTagId dsTagId = new SbiDatasetTagId(dsId.getDsId(), dsId.getVersionNum(), dsId.getOrganization(), tagId);
				SbiDatasetTag dsTag = new SbiDatasetTag(dsTagId);
				dsTag.setDataSet(dataSet);
				dsTag.setTag(tag);
				session.save(dsTag);
				session.flush();
			}

			tx.commit();
		} catch (Exception e) {
			logException(e);
			if (tx != null)
				tx.rollback();
			throw new RuntimeException(e);
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}
		logger.debug("OUT");
		return toReturn;
	}

	private List<SbiTag> insertTags(List<String> tagsToInsert, Session curSession) {
		logger.debug("IN");
		List<SbiTag> toReturn = new ArrayList<>();
		Iterator<String> it;
		try {
			it = tagsToInsert.iterator();
			while (it.hasNext()) {
				String tagName = it.next();
				SbiTag tag = new SbiTag(tagName);
				updateSbiCommonInfo4Insert(tag);
				curSession.save(tag);
				toReturn.add(tag);
			}
			curSession.flush();
		} catch (HibernateException e) {
			logException(e);
			throw new RuntimeException(e);
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public void removeDatasetTags(Integer dsId, Session curSession) {
		logger.debug("IN");
		Transaction tx = null;
		try {
			tx = curSession.beginTransaction();
			Query query = curSession.createQuery("from SbiDatasetTag dst where dst.dsTagId.dsId = :dsId");
			query.setInteger("dsId", dsId);
			List<SbiDatasetTag> dsTags = query.list();
			if (!dsTags.isEmpty()) {
				for (SbiDatasetTag dsTag : dsTags) {
					curSession.delete(dsTag);
					curSession.flush();
				}
				tx.commit();
			}
		} catch (Exception e) {
			logException(e);
			if (tx != null)
				tx.rollback();
			throw new RuntimeException(e);
		}
		logger.debug("OUT");
	}

	@Override
	public void deleteDatasetTag(SbiDatasetTag dsTag) {
		logger.debug("IN");
		Session session = null;
		Transaction tx = null;
		try {
			session = getSession();
			tx = session.beginTransaction();
			session.delete(dsTag);
			session.flush();
			tx.commit();
		} catch (Exception e) {
			logException(e);
			if (tx != null)
				tx.rollback();
			throw new RuntimeException(e);
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}
		logger.debug("OUT");
	}

	@Override
	public void deleteTag(Integer tagId) {
		logger.debug("IN");
		Session session = null;
		Transaction tx = null;
		SbiTag tagToDelete = null;
		try {
			session = getSession();
			tx = session.beginTransaction();
			Query query = session.createQuery("from SbiTag t where t.tagId = :tagId");
			query.setInteger("tagId", tagId);
			tagToDelete = (SbiTag) query.uniqueResult();

			if (tagToDelete != null) {
				List<SbiDatasetTag> dsTags = session.createQuery("from SbiDatasetTag dst where dst.dsTagId.tagId = :tagId").setInteger("tagId", tagId).list();
				for (SbiDatasetTag dsTag : dsTags) {
					session.delete(dsTag);
				}
				session.delete(tagToDelete);
				session.flush();
				tx.commit();
			}
		} catch (HibernateException e) {
			logException(e);
			if (tx != null)
				tx.rollback();
			throw new RuntimeException(e);
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}
		logger.debug("OUT");
	}

}
