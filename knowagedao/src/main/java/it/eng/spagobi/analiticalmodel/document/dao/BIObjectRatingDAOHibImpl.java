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
package it.eng.spagobi.analiticalmodel.document.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjectsRating;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjectsRatingId;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class BIObjectRatingDAOHibImpl extends AbstractHibernateDAO implements
		IBIObjectRating {
    
    static private Logger logger = Logger.getLogger(BIObjectRatingDAOHibImpl.class);
    
    /* (non-Javadoc)
     * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectRating#voteBIObject(it.eng.spagobi.analiticalmodel.document.bo.BIObject, java.lang.String, java.lang.String)
     */
    public void voteBIObject(BIObject obj,String userid, String rating) throws EMFUserError{
    	logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
				
			SbiObjects hibBIObject = (SbiObjects)aSession.load(SbiObjects.class,  obj.getId());
			SbiObjectsRating hibBIObjectsRating = new SbiObjectsRating();
			
			hibBIObjectsRating = loadBIObjectRatingById(obj, userid);
			Integer newRating = new Integer(rating);
			if(newRating!= null && newRating.intValue()> 5){
				newRating = new Integer(5);
			}else if(newRating!= null && newRating.intValue()< 0){
				newRating = new Integer(0);
			}
			if (hibBIObjectsRating != null){
				
				hibBIObjectsRating.setRating(newRating);
				updateSbiCommonInfo4Update(hibBIObjectsRating);
				aSession.update(hibBIObjectsRating);

			}else {
				SbiObjectsRating hibBIObjectsRating1 = new SbiObjectsRating();
				SbiObjectsRatingId hibBIObjectsRatingId1 = new SbiObjectsRatingId();
				hibBIObjectsRatingId1.setObjId(obj.getId());
				hibBIObjectsRatingId1.setUserId(userid);
				hibBIObjectsRating1.setId(hibBIObjectsRatingId1);
				hibBIObjectsRating1.setRating(newRating);
				hibBIObjectsRating1.setSbiObjects(hibBIObject);
				hibBIObjectsRating = hibBIObjectsRating1 ;
				updateSbiCommonInfo4Insert(hibBIObjectsRating);
				aSession.save(hibBIObjectsRating);
				
				
			}		
					
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while inserting the Distribution List with name " , he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				logger.debug("OUT");
			}
		}
    	
    }
    
    /* (non-Javadoc)
     * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectRating#calculateBIObjectRating(it.eng.spagobi.analiticalmodel.document.bo.BIObject)
     */
    public Double calculateBIObjectRating(BIObject obj) throws EMFUserError{
    	Double rating = new Double(0);
    
    	
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiObjectsRating s where s.id.objId = " + obj.getId();
			String hql = "from SbiObjectsRating s where s.id.objId = ?";
			Query query = aSession.createQuery(hql);
			query.setInteger(0,  obj.getId().intValue());
			List l = query.list();
			double totalVotes = 0 ;
			double sumVotes = 0 ;
			
		    Iterator it = l.iterator();
		    while(it.hasNext()){
		    	SbiObjectsRating temp = (SbiObjectsRating)it.next();
		    	Integer rat = temp.getRating();
		    	sumVotes = sumVotes + rat.doubleValue();
		    	totalVotes ++ ;
		    }
		    if (totalVotes != 0){
		    	rating = new Double (sumVotes / totalVotes);
		    }
			tx.commit();
			return rating ;
		} catch (HibernateException he) {
			logger.error(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
    	
    }
    
    private SbiObjectsRating loadBIObjectRatingById(BIObject obj, String userid) throws EMFUserError {

    	SbiObjectsRating hibBIObjectsRating = new SbiObjectsRating();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			//String hql = " from SbiObjectsRating s where " +
			//             " s.id.objId = "+  obj.getId()+ " and s.id.userId = '"+ userid +"'";
			
			String hql = " from SbiObjectsRating s where " +
            " s.id.objId = ? and s.id.userId = ?";
			
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0,  obj.getId().intValue());
			hqlQuery.setString(1, userid);
			hibBIObjectsRating = (SbiObjectsRating)hqlQuery.uniqueResult();
			tx.commit();
			return hibBIObjectsRating ;
		} catch (HibernateException he) {
			logger.error(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
    	
    }


}
