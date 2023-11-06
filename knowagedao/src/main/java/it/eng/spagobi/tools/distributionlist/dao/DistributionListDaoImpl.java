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
package it.eng.spagobi.tools.distributionlist.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.distributionlist.bo.DistributionList;
import it.eng.spagobi.tools.distributionlist.bo.Email;
import it.eng.spagobi.tools.distributionlist.metadata.SbiDistributionList;
import it.eng.spagobi.tools.distributionlist.metadata.SbiDistributionListUser;
import it.eng.spagobi.tools.distributionlist.metadata.SbiDistributionListsObjects;

/**
 * @author Chiarelli Chiara (chiara.chiarelli@eng.it)
 */

public class DistributionListDaoImpl extends AbstractHibernateDAO implements IDistributionListDAO {

	private static Logger logger = Logger.getLogger(DistributionListDaoImpl.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#eraseDistributionList(it.eng.spagobi.tools.distributionlist.bo.DistributionList)
	 */
	@Override
	public void eraseDistributionList(DistributionList aDistributionList) throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiDistributionList hibDistributionList = (SbiDistributionList) aSession.load(SbiDistributionList.class,
					aDistributionList.getId());

			aSession.delete(hibDistributionList);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while erasing the distribution list with id "
					+ ((aDistributionList == null) ? "" : String.valueOf(aDistributionList.getId())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9101);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#eraseDistributionListObjects(it.eng.spagobi.tools.distributionlist.bo.DistributionList,
	 * int, java.lang.String)
	 */
	@Override
	public void eraseDistributionListObjects(DistributionList dl, int biobId, String triggername) throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiDistributionListsObjects hibDistributionListsObjects = new SbiDistributionListsObjects();

			String hql = "from SbiDistributionListsObjects sdlo where sdlo.sbiDistributionList.dlId=? and sdlo.sbiObjects.biobjId=?";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, dl.getId());
			query.setInteger(1, biobId);
			List<SbiDistributionListsObjects> l = query.list();
			if (!l.isEmpty()) {
				Iterator<SbiDistributionListsObjects> it = l.iterator();
				while (it.hasNext()) {
					SbiDistributionListsObjects temp = it.next();
					String xmlstr = temp.getXml();
					SourceBean sb = SourceBean.fromXMLString(xmlstr);
					String trigName = (String) sb.getAttribute("triggerName");
					if (trigName != null && trigName.equals(triggername)) {
						hibDistributionListsObjects = temp;
						aSession.delete(hibDistributionListsObjects);
					}
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while erasing Distribution List objects related to the Distribution List "
					+ ((dl == null) ? "" : String.valueOf(dl.getId())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9106);

		} catch (SourceBeanException e) {
			logger.error("Error while generating Source Bean");
			e.printStackTrace();
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#eraseAllRelatedDistributionListObjects(java.lang.String)
	 */
	@Override
	public void eraseAllRelatedDistributionListObjects(String triggername) throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "from SbiDistributionListsObjects ";
			Query query = aSession.createQuery(hql);

			List<SbiDistributionListsObjects> l = query.list();
			if (!l.isEmpty()) {
				Iterator<SbiDistributionListsObjects> it = l.iterator();
				while (it.hasNext()) {
					SbiDistributionListsObjects temp = it.next();
					String xmlstr = temp.getXml();
					SourceBean sb = SourceBean.fromXMLString(xmlstr);
					String trigName = (String) sb.getAttribute("triggerName");
					if (trigName != null && trigName.equals(triggername)) {
						aSession.delete(temp);
					}
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while erasing Distribution List objects with triggername "
					+ ((triggername == null) ? "" : triggername), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9106);

		} catch (SourceBeanException e) {
			logger.error("Error while generating Source Bean");
			e.printStackTrace();
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#insertDistributionList(it.eng.spagobi.tools.distributionlist.bo.DistributionList)
	 */
	@Override
	public void insertDistributionList(DistributionList aDistributionList) throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiDistributionList hibDistributionList = new SbiDistributionList();

			hibDistributionList.setName(aDistributionList.getName());
			hibDistributionList.setDescr(aDistributionList.getDescr());
			updateSbiCommonInfo4Insert(hibDistributionList);
			aSession.save(hibDistributionList);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while inserting the Distribution List with name "
					+ ((aDistributionList == null) ? "" : String.valueOf(aDistributionList.getName())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#loadAllDistributionLists()
	 */
	@Override
	public List<DistributionList> loadAllDistributionLists() throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<DistributionList> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiDistributionList");

			List<SbiDistributionList> hibList = hibQuery.list();
			Iterator<SbiDistributionList> it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toDistributionList(it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading all Distribution Lists ", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return realResult;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#loadDistributionListById(java.lang.Integer)
	 */
	@Override
	public DistributionList loadDistributionListById(Integer id) throws EMFUserError {

		logger.debug("IN");
		DistributionList toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiDistributionList hibDistributionList = (SbiDistributionList) aSession.load(SbiDistributionList.class,
					id);
			toReturn = toDistributionList(hibDistributionList);
			tx.commit();

		} catch (HibernateException he) {
			logger.error("Error while loading the Distribution List with id " + ((id == null) ? "" : id.toString()),
					he);

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
		logger.debug("OUT");
		return toReturn;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#loadDistributionListByName(java.lang.String)
	 */
	@Override
	public DistributionList loadDistributionListByName(String name) throws EMFUserError {
		logger.debug("IN");
		DistributionList biDL = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion nameCriterrion = Restrictions.eq("name", name);
			Criteria criteria = tmpSession.createCriteria(SbiDistributionList.class);
			criteria.add(nameCriterrion);
			SbiDistributionList hibDL = (SbiDistributionList) criteria.uniqueResult();
			if (hibDL == null)
				return null;
			biDL = toDistributionList(hibDL);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading the Distribution List with name " + name, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		logger.debug("OUT");
		return biDL;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#modifyDistributionList(it.eng.spagobi.tools.distributionlist.bo.DistributionList)
	 */
	@Override
	public void modifyDistributionList(DistributionList aDistributionList) throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiDistributionList hibDistributionList = (SbiDistributionList) aSession.load(SbiDistributionList.class,
					aDistributionList.getId());
			hibDistributionList.setName(aDistributionList.getName());
			hibDistributionList.setDescr(aDistributionList.getDescr());
			updateSbiCommonInfo4Update(hibDistributionList);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while modifing the distribution list with id "
					+ ((aDistributionList == null) ? "" : String.valueOf(aDistributionList.getId())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9105);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}

	}

	/**
	 * To distribution list.
	 *
	 * @param hibDistributionList the hib distribution list
	 *
	 * @return the distribution list
	 */
	public DistributionList toDistributionList(SbiDistributionList hibDistributionList) {
		DistributionList dl = new DistributionList();

		dl.setId((hibDistributionList.getDlId()).intValue());
		dl.setName(hibDistributionList.getName());
		dl.setDescr(hibDistributionList.getDescr());

		// Gets all userids and respective emails and puts them into a list of Emails
		List<Email> emails = new ArrayList<>();
		Set<SbiDistributionListUser> s = hibDistributionList.getSbiDistributionListUsers();
		Iterator<SbiDistributionListUser> i = s.iterator();
		while (i.hasNext()) {
			SbiDistributionListUser dls = i.next();
			String userId = dls.getUserId();
			String eMail = dls.getEMail();
			Email email = new Email();
			email.setUserId(userId);
			email.setEmail(eMail);
			emails.add(email);
		}

		dl.setEmails(emails);

		// Gets all documents related to the distribution list and puts them into a list of documents
		List<BIObject> documents = new ArrayList<>();
		Set<SbiDistributionListsObjects> d = hibDistributionList.getSbiDistributionListsObjectses();
		Iterator<SbiDistributionListsObjects> it = d.iterator();
		while (it.hasNext()) {
			SbiDistributionListsObjects dlo = it.next();
			SbiObjects so = dlo.getSbiObjects();
			IBIObjectDAO objDAO = null;
			try {
				objDAO = DAOFactory.getBIObjectDAO();
				BIObject obj = objDAO.toBIObject(so, null);
				documents.add(obj);
			} catch (EMFUserError e) {

			}

		}

		dl.setDocuments(documents);

		return dl;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#hasBIObjAssociated(java.lang.String)
	 */
	@Override
	public boolean hasBIObjAssociated(String dlId) throws EMFUserError {
		logger.debug("IN");
		boolean bool = false;

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Integer dlIdInt = Integer.valueOf(dlId);

			String hql = " from SbiObjects s where s.distributionList.dlId = ?";
			Query aQuery = aSession.createQuery(hql);
			aQuery.setInteger(0, dlIdInt);
			List biObjectsAssocitedWithDl = aQuery.list();
			if (!biObjectsAssocitedWithDl.isEmpty())
				bool = true;
			else
				bool = false;
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while getting the objects associated with the distribution list with id " + dlId, he);

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

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#subscribeToDistributionList(it.eng.spagobi.tools.distributionlist.bo.DistributionList,
	 * it.eng.spagobi.tools.distributionlist.bo.Email)
	 */
	@Override
	public void subscribeToDistributionList(DistributionList aDistributionList, Email user) throws EMFUserError {

		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiDistributionList hibDistributionList = (SbiDistributionList) aSession.load(SbiDistributionList.class,
					aDistributionList.getId());

			SbiDistributionListUser hibDistributionListUser = new SbiDistributionListUser();
			hibDistributionListUser.setUserId(user.getUserId());
			hibDistributionListUser.setEMail(user.getEmail());
			hibDistributionListUser.setSbiDistributionList(hibDistributionList);
			updateSbiCommonInfo4Insert(hibDistributionListUser);
			aSession.save(hibDistributionListUser);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while subscribing to the distribution list with id "
					+ ((aDistributionList == null) ? "" : String.valueOf(aDistributionList.getId())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9102);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#unsubscribeFromDistributionList(it.eng.spagobi.tools.distributionlist.bo.DistributionList,
	 * java.lang.String)
	 */
	@Override
	public void unsubscribeFromDistributionList(DistributionList aDistributionList, String user) throws EMFUserError {

		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiDistributionList hibDistributionList = (SbiDistributionList) aSession.load(SbiDistributionList.class,
					aDistributionList.getId());

			Set<SbiDistributionListUser> s = hibDistributionList.getSbiDistributionListUsers();
			Iterator<SbiDistributionListUser> i = s.iterator();
			while (i.hasNext()) {
				SbiDistributionListUser dls = i.next();
				String userId = dls.getUserId();
				if (userId.equals(user)) {
					aSession.delete(dls);
				}
			}

			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while unsubscribing to the distribution list with id "
					+ ((aDistributionList == null) ? "" : String.valueOf(aDistributionList.getId())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9103);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#insertDLforDocument(it.eng.spagobi.tools.distributionlist.bo.DistributionList, int,
	 * java.lang.String)
	 */
	@Override
	public void insertDLforDocument(DistributionList dl, int objId, String xml) throws EMFUserError {
		logger.debug("IN");
		boolean otherSchedule = false;
		Session aSession = null;
		Transaction tx = null;
		try {
			if (DAOFactory.getDistributionListDAO().isDocScheduleAlreadyLinkedToDL(dl, objId, xml)) {
				logger.debug("");
				return;
			}
			if (DAOFactory.getDistributionListDAO().isDocScheduledInOtherTime(dl, objId, xml)) {
				otherSchedule = true;
			}
			SourceBean sbOrig = SourceBean.fromXMLString(xml);
			String trigNameOrig = (String) sbOrig.getAttribute("triggerName");

			List listRowsOrig = sbOrig.getAttributeAsList("PARAMETERS.PARAMETER");
			SourceBean tmpSBOrig = (SourceBean) listRowsOrig.get(0);
			String parvaluesOrig = (String) tmpSBOrig.getAttribute("value");

			SbiDistributionListsObjects hibDistributionListsObjects = new SbiDistributionListsObjects();

			aSession = getSession();
			tx = aSession.beginTransaction();
			if (otherSchedule) {

				String hql = "from SbiDistributionListsObjects sdlo where sdlo.sbiDistributionList.dlId=:dlId  and sdlo.sbiObjects.biobjId=:objId";
				Query query = aSession.createQuery(hql);
				query.setParameter("dlId", dl.getId());
				query.setParameter("objId", objId);

				List<SbiDistributionListsObjects> l = query.list();

				Iterator<SbiDistributionListsObjects> it = l.iterator();
				while (it.hasNext()) {
					SbiDistributionListsObjects temp = it.next();
					String xmlstr = temp.getXml();
					SourceBean sb = SourceBean.fromXMLString(xmlstr);
					String trigName = (String) sb.getAttribute("triggerName");

					List listRows = sb.getAttributeAsList("PARAMETERS.PARAMETER");
					SourceBean tmpSB = (SourceBean) listRows.get(0);
					String parvalues = (String) tmpSB.getAttribute("value");

					if (trigName != null && trigName.equals(trigNameOrig) && parvalues != null
							&& parvalues.equals(parvaluesOrig)) {
						hibDistributionListsObjects = temp;
					}
				}
			}

			SbiDistributionList hibDistributionList = (SbiDistributionList) aSession.load(SbiDistributionList.class,
					dl.getId());
			SbiObjects hibObj = (SbiObjects) aSession.load(SbiObjects.class, objId);

			hibDistributionListsObjects.setSbiDistributionList(hibDistributionList);
			hibDistributionListsObjects.setSbiObjects(hibObj);
			hibDistributionListsObjects.setXml(xml);
			updateSbiCommonInfo4Insert(hibDistributionListsObjects);
			aSession.save(hibDistributionListsObjects);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while inserting the document to the distribution list with name "
					+ ((dl == null) ? "" : String.valueOf(dl.getName())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9106);

		} catch (SourceBeanException e) {
			logger.error("Error while generating Source Bean");
			e.printStackTrace();
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#isDocScheduleAlreadyLinkedToDL(it.eng.spagobi.tools.distributionlist.bo.DistributionList,
	 * int, java.lang.String)
	 */
	@Override
	public boolean isDocScheduleAlreadyLinkedToDL(DistributionList dl, int objId, String xml) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			String hql = "from SbiDistributionListsObjects sdlo where sdlo.sbiDistributionList.dlId=? and sdlo.sbiObjects.biobjId=? and sdlo.xml=?";
			Query query = tmpSession.createQuery(hql);
			query.setInteger(0, dl.getId());
			query.setInteger(1, objId);
			query.setString(2, xml);

			SbiDistributionListsObjects hibDL = (SbiDistributionListsObjects) query.uniqueResult();
			if (hibDL == null)
				return false;
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading the distribution list documents ", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9106);
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		logger.debug("OUT");
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#isDocScheduledInOtherTime(it.eng.spagobi.tools.distributionlist.bo.DistributionList, int,
	 * java.lang.String)
	 */
	@Override
	public boolean isDocScheduledInOtherTime(DistributionList dl, int objId, String xml) throws EMFUserError {
		logger.debug("IN");

		Session tmpSession = null;
		Transaction tx = null;
		try {
			SourceBean sbOrig = SourceBean.fromXMLString(xml);
			String trigNameOrig = (String) sbOrig.getAttribute("triggerName");

			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			String hql = "from SbiDistributionListsObjects sdlo where sdlo.sbiDistributionList.dlId=? and sdlo.sbiObjects.biobjId=?";
			Query query = tmpSession.createQuery(hql);
			query.setInteger(0, dl.getId());
			query.setInteger(1, objId);
			List<SbiDistributionListsObjects> l = query.list();
			if (!l.isEmpty()) {
				Iterator<SbiDistributionListsObjects> it = l.iterator();
				while (it.hasNext()) {
					SbiDistributionListsObjects temp = it.next();
					String xmlstr = temp.getXml();
					SourceBean sb = SourceBean.fromXMLString(xmlstr);
					String trigName = (String) sb.getAttribute("triggerName");
					if (trigName != null && trigName.equals(trigNameOrig))
						return true;
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading the distribution list documents ", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9106);
		} catch (SourceBeanException e) {
			logger.error("Error while generating Source Bean");
			e.printStackTrace();
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		logger.debug("OUT");
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO#getXmlRelated(it.eng.spagobi.tools.distributionlist.bo.DistributionList, int)
	 */
	@Override
	public List<String> getXmlRelated(DistributionList dl, int objId) throws EMFUserError {
		logger.debug("IN");
		List<String> xmls = new ArrayList<>();
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			String hql = "from SbiDistributionListsObjects sdlo where sdlo.sbiDistributionList.dlId=? and sdlo.sbiObjects.biobjId=?";
			Query query = tmpSession.createQuery(hql);
			query.setInteger(0, dl.getId());
			query.setInteger(1, objId);
			List<SbiDistributionListsObjects> l = query.list();

			if (!l.isEmpty()) {
				Iterator<SbiDistributionListsObjects> it = l.iterator();
				while (it.hasNext()) {
					SbiDistributionListsObjects temp = it.next();
					String xmlstr = temp.getXml();
					xmls.add(xmlstr);
				}
			}
		} catch (HibernateException he) {
			logger.error("Error while loading the distribution list documents ", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9106);
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		logger.debug("OUT");
		return xmls;
	}

}
