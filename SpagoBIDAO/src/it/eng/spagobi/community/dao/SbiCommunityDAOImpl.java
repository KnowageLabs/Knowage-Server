/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.community.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISubObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISubreportDAO;
import it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjFunc;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjTemplates;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.LowFunctionalityDAOHibImpl;
import it.eng.spagobi.analiticalmodel.functionalitytree.metadata.SbiFuncRole;
import it.eng.spagobi.analiticalmodel.functionalitytree.metadata.SbiFunctions;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.BIObjectParameterDAOHibImpl;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiBinContents;
import it.eng.spagobi.community.mapping.SbiCommunity;
import it.eng.spagobi.community.mapping.SbiCommunityUsers;
import it.eng.spagobi.community.mapping.SbiCommunityUsersId;
import it.eng.spagobi.engines.dossier.dao.IDossierPartsTempDAO;
import it.eng.spagobi.engines.dossier.dao.IDossierPresentationsDAO;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class SbiCommunityDAOImpl extends AbstractHibernateDAO implements ISbiCommunityDAO {
	
	static private Logger logger = Logger.getLogger(SbiCommunityDAOImpl.class);
	
	public void setUserProfile(IEngUserProfile profile) {
		// TODO Auto-generated method stub

	}

	public void setUserID(String user) {
		// TODO Auto-generated method stub

	}

	public IEngUserProfile getUserProfile() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTenant(String tenant) {
		// TODO Auto-generated method stub

	}

	public Integer saveSbiComunity(SbiCommunity community) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer id = null;
		try {

			aSession = getSession();
			tx = aSession.beginTransaction();
			community.setCreationDate(new Date());
			community.setLastChangeDate(new Date());
			updateSbiCommonInfo4Insert(community, true);
			id = (Integer)aSession.save(community);

			tx.commit();

			logger.debug("OUT");
			return id;
		} catch (HibernateException he) {
			logger.error(he.getMessage(),he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} 
		catch (RuntimeException re) {
			logger.error(re.getMessage(),re);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	public List<SbiCommunity> loadSbiCommunityByUser(String userId)
			throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		List<SbiCommunity> result = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String q = "select cu.sbiCommunity from SbiCommunityUsers cu where cu.id.userId = :userId";
			Query query = aSession.createQuery(q);
			query.setString("userId", userId);

			result = query.list();

			return result;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	}

	public List<SbiCommunity> loadSbiCommunityByOwner(String owner)
			throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		List<SbiCommunity> result = null;
		Transaction tx = null;
		try {

			aSession = getSession();
			tx = aSession.beginTransaction();
			String q = "from SbiCommunity c where c.owner = :userId";
			Query query = aSession.createQuery(q);
			query.setString("userId", owner);

			result = query.list();

			return result;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	}

	public SbiCommunity loadSbiCommunityByName(String name) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		SbiCommunity result = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String q = "from SbiCommunity c where c.name = :name";
			Query query = aSession.createQuery(q);
			query.setString("name", name);

			result = (SbiCommunity) query.uniqueResult();

			return  result;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	}

	public Integer saveSbiComunityUsers(SbiCommunity community, String userID)
			throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer res = null;
		SbiCommunityUsers commUsers = new SbiCommunityUsers();
		try {
			
			res = saveSbiComunity(community);
			if(res != null){
			
				aSession = getSession();
				tx = aSession.beginTransaction();
				
				commUsers.setCreationDate(new Date());
				commUsers.setLastChangeDate(new Date());
				SbiCommunityUsersId id = new SbiCommunityUsersId();
				id.setCommunityId(community.getCommunityId());
				id.setUserId(userID);
				
				commUsers.setId(id);
				
				updateSbiCommonInfo4Insert(commUsers, true);
				aSession.save(commUsers);
	
				tx.commit();
			}
			logger.debug("OUT");

		} catch (HibernateException he) {
			logger.error(he.getMessage(),he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}catch (RuntimeException re) {
			logger.error(re.getMessage(),re);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return res;
	}

	public void addCommunityMember(SbiCommunity community, String userID)
			throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		SbiCommunityUsers commUsers = new SbiCommunityUsers();
		try {
			
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			commUsers.setCreationDate(new Date());
			commUsers.setLastChangeDate(new Date());
			SbiCommunityUsersId id = new SbiCommunityUsersId();
			id.setCommunityId(community.getCommunityId());
			id.setUserId(userID);
			
			commUsers.setId(id);
			
			updateSbiCommonInfo4Insert(commUsers,true);
			aSession.save(commUsers);

			tx.commit();
			logger.debug("OUT");

		} catch (HibernateException he) {
			logger.error(he.getMessage(),he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	public List<SbiCommunity> loadAllSbiCommunities() throws EMFUserError {
		logger.debug("IN");
		List<SbiCommunity> result = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String q = "from SbiCommunity";
			Query query = aSession.createQuery(q);

			result = query.list();


			return result;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	}

	public void deleteCommunityById(Integer id) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiCommunity hibComm = (SbiCommunity) aSession.load(SbiCommunity.class,id);
			//get community users
			String q = "from SbiCommunityUsers cu where cu.id.communityId = :id";
			Query query = aSession.createQuery(q);
			query.setInteger("id", id);
			List hibList = query.list();
			Iterator it = hibList.iterator();
			//delete all users for community
			while(it.hasNext()){
				SbiCommunityUsers scu = (SbiCommunityUsers)it.next();
				aSession.delete(scu);
			}
			aSession.flush();
			aSession.refresh(hibComm);
			//get functionalities
			String fq = "from SbiFunctions f where f.code = :code and f.functTypeCd = 'COMMUNITY_FUNCT'";
			Query queryF = aSession.createQuery(fq);
			queryF.setString("code", hibComm.getFunctCode());		
			
			List functs = queryF.list();
			if(functs != null && !functs.isEmpty()){
			Iterator itF = functs.iterator();
			//delete all functions for community
			ILowFunctionalityDAO functDao = DAOFactory.getLowFunctionalityDAO();
			IBIObjectDAO objDao = DAOFactory.getBIObjectDAO();
			while(itF.hasNext()){
				SbiFunctions function = (SbiFunctions)itF.next();
				
				//find relation with documents associated to community folder
				String hql1 = "from SbiObjFunc a " +
						"where a.id.sbiFunctions in (:functions)";

				Query queryDoc = aSession.createQuery(hql1);
				queryDoc.setParameterList("functions", functs);
				List<SbiObjFunc> objsF = queryDoc.list();
				Iterator itdF = objsF.iterator();

				while(itdF.hasNext()){
					SbiObjFunc rel = (SbiObjFunc)itdF.next();
					if(rel.getId().getSbiFunctions().getCode().equals(function.getCode())){
						aSession.delete(rel);
						aSession.flush();
						
						aSession.refresh(function);
						
						//delete the object only if there are no other relations:						
						BIObject objBI = objDao.loadBIObjectById(rel.getId().getSbiObjects().getBiobjId());
						SbiObjects hibBIObject = rel.getId().getSbiObjects();
						ArrayList otherFuncts = (ArrayList)objBI.getFunctionalities();
						
						otherFuncts.remove(rel.getId().getSbiFunctions().getFunctId());
						if(otherFuncts.size() == 0){
							// delete templates
							String hql = "from SbiObjTemplates sot where sot.sbiObject.biobjId="+hibBIObject.getBiobjId();
							Query query5 = aSession.createQuery(hql);
							List templs = query5.list();
							Iterator iterTempls = templs.iterator();
							while(iterTempls.hasNext()) {
								SbiObjTemplates hibObjTemp = (SbiObjTemplates)iterTempls.next();
								SbiBinContents hibBinCont = hibObjTemp.getSbiBinContents();
								aSession.delete(hibObjTemp);
								aSession.delete(hibBinCont);

							}

							//delete subobjects eventually associated
							ISubObjectDAO subobjDAO = DAOFactory.getSubObjectDAO();
							List subobjects =  subobjDAO.getSubObjects(hibBIObject.getBiobjId());
							for (int i=0; i < subobjects.size(); i++){
								SubObject s = (SubObject) subobjects.get(i);
								//subobjDAO.deleteSubObject(s.getId());
								subobjDAO.deleteSubObjectSameConnection(s.getId(), aSession);
							}

							//delete viewpoints eventually associated
							List viewpoints = new ArrayList();
							IViewpointDAO biVPDAO = DAOFactory.getViewpointDAO();
							viewpoints =  biVPDAO.loadAllViewpointsByObjID(hibBIObject.getBiobjId());
							for (int i=0; i<viewpoints.size(); i++){
								Viewpoint vp =(Viewpoint)viewpoints.get(i);
								biVPDAO.eraseViewpoint(vp.getVpId());
							}

							//delete snapshots eventually associated
							ISnapshotDAO snapshotsDAO = DAOFactory.getSnapshotDAO();
							List snapshots = snapshotsDAO.getSnapshots(hibBIObject.getBiobjId());
							for (int i=0; i < snapshots.size(); i++){
								Snapshot aSnapshots = (Snapshot) snapshots.get(i);
								snapshotsDAO.deleteSnapshot(aSnapshots.getId());
							}

							//delete notes eventually associated
							IObjNoteDAO objNoteDAO = DAOFactory.getObjNoteDAO();
							objNoteDAO.eraseNotes(hibBIObject.getBiobjId());

							//delete metadata eventually associated
							List metadata = DAOFactory.getObjMetadataDAO().loadAllObjMetadata();
							IObjMetacontentDAO objMetaContentDAO = DAOFactory.getObjMetacontentDAO();
							if (metadata != null && !metadata.isEmpty()) {
								Iterator itM = metadata.iterator();
								while (itM.hasNext()) {
									ObjMetadata objMetadata = (ObjMetadata) itM.next();
									ObjMetacontent objMetacontent = (ObjMetacontent) DAOFactory.getObjMetacontentDAO().loadObjMetacontent(objMetadata.getObjMetaId(), hibBIObject.getBiobjId(), null);
									if(objMetacontent!=null){
										objMetaContentDAO.eraseObjMetadata(objMetacontent);
									}
								}
							}			


							// delete parameters associated
							// before deleting parameters associated is needed to delete all dependencies,
							// otherwise in case there could be error if is firstly deleted a parameter from wich some else is dependant
							// (thought priority parameter is not costraining dependencies definition)
							
							Set objPars = hibBIObject.getSbiObjPars();
							
							Iterator itObjParDep = objPars.iterator();
							BIObjectParameterDAOHibImpl objParDAO = new BIObjectParameterDAOHibImpl();
							while (itObjParDep.hasNext()) {
								SbiObjPar aSbiObjPar = (SbiObjPar) itObjParDep.next();
								BIObjectParameter aBIObjectParameter = new BIObjectParameter();
								aBIObjectParameter.setId(aSbiObjPar.getObjParId());			
								objParDAO.eraseBIObjectParameterDependencies(aBIObjectParameter, aSession);
							}
								
							Iterator itObjPar = objPars.iterator();
							while (itObjPar.hasNext()) {
								SbiObjPar aSbiObjPar = (SbiObjPar) itObjPar.next();
								BIObjectParameter aBIObjectParameter = new BIObjectParameter();
								aBIObjectParameter.setId(aSbiObjPar.getObjParId());
								
								objParDAO.eraseBIObjectParameter(aBIObjectParameter, aSession, false);
							}

							// delete dossier temp parts eventually associated
							IDossierPartsTempDAO dptDAO = DAOFactory.getDossierPartsTempDAO();
							dptDAO.eraseDossierParts(hibBIObject.getBiobjId());
							// delete dossier presentations eventually associated
							IDossierPresentationsDAO dpDAO = DAOFactory.getDossierPresentationDAO();
							dpDAO.deletePresentations(hibBIObject.getBiobjId());

							// update subreports table 
							ISubreportDAO subrptdao = DAOFactory.getSubreportDAO();
							subrptdao.eraseSubreportByMasterRptId(hibBIObject.getBiobjId());
							subrptdao.eraseSubreportBySubRptId(hibBIObject.getBiobjId());

							// delete object
							aSession.delete(hibBIObject);
							logger.debug("OUT");
							aSession.flush();
							aSession.refresh(function);
						}
					}
				}


				try{								

					Set oldRoles = function.getSbiFuncRoles();
					Iterator iterOldRoles = oldRoles.iterator();
					while (iterOldRoles.hasNext()) {
						SbiFuncRole role = (SbiFuncRole) iterOldRoles.next();
						aSession.delete(role);
					}

					// update prog column in other functions

					if(function.getParentFunct()!=null){
						String hqlUpdateProg = "update SbiFunctions s set s.prog = (s.prog - 1) where s.prog > ? " 
							+ " and s.parentFunct.functId = ?" ;
						Query query4 = aSession.createQuery(hqlUpdateProg);
						query4.setInteger(0, function.getProg().intValue());
						query4.setInteger(1, function.getParentFunct().getFunctId().intValue());
						query4.executeUpdate();
					}

					aSession.delete(function);
					
				}catch(Exception e){
					logger.debug("No such functionality element ");
				}
				}
				aSession.delete(hibComm);
				tx.commit();
			}
			
			
		} catch (HibernateException he) {
			logger.error("Error while erasing the community with id " + id, he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} catch (Exception e) {
			logger.error("Error while erasing the community with id " + id, e);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				logger.debug("OUT");
			}
		}
		
	}

	public Integer updateSbiComunity(SbiCommunity community)
			throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer id = community.getCommunityId();
		try {

			aSession = getSession();
			tx = aSession.beginTransaction();
			community.setCreationDate(new Date());
			community.setLastChangeDate(new Date());
			updateSbiCommonInfo4Insert(community);
			aSession.update(community);

			tx.commit();

			logger.debug("OUT");
			return id;
		} catch (HibernateException he) {
			logger.error(he.getMessage(),he);
			
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	public List<SbiCommunityUsers> loadCommunitieMembersByName(SbiCommunity community, SbiUser owner)
			throws EMFUserError {
		logger.debug("IN");
		List<SbiCommunityUsers> result = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Integer id = community.getCommunityId();
			String q = "from SbiCommunityUsers cu where cu.id.communityId = ? and cu.id.userId != ? order by creationDate desc";
			Query query = aSession.createQuery(q);
			query.setInteger(0, id);
			query.setString(1, owner.getUserId());
			result = query.list();

			return result;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	}

	public void deleteCommunityMembership(String userID)
			throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String q = "from SbiCommunityUsers cu where cu.id.userId = ?";
			Query query = aSession.createQuery(q);
			query.setString(0, userID);
			List <SbiCommunityUsers> result = query.list();
			if(result != null && !result.isEmpty()){
				for(int i=0; i<result.size(); i++){
					aSession.delete(result.get(i));
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		
	}
	
	public void deleteMemberFromCommunity(String userID, Integer communityId)
			throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String q = "from SbiCommunityUsers cu where cu.id.userId = ? and  cu.id.communityId = ? ";
			Query query = aSession.createQuery(q);
			query.setString(0, userID);
			query.setInteger(1, communityId);
			List <SbiCommunityUsers> result = query.list();
			if(result != null && !result.isEmpty()){
				for(int i=0; i<result.size(); i++){
					aSession.delete(result.get(i));
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		
	}

}
