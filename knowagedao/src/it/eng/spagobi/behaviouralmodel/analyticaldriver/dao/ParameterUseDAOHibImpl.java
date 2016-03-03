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
package it.eng.spagobi.behaviouralmodel.analyticaldriver.dao;


import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParameters;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuseCk;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuseCkId;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuseDet;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuseDetId;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.behaviouralmodel.check.dao.CheckDAOHibImpl;
import it.eng.spagobi.behaviouralmodel.check.metadata.SbiChecks;
import it.eng.spagobi.behaviouralmodel.lov.metadata.SbiLov;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.RoleDAOHibImpl;
import it.eng.spagobi.commons.metadata.SbiExtRoles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Defines the Hibernate implementations for all DAO methods,
 * for a parameter use mode.
 * 
 * @author zoppello
 */
public class ParameterUseDAOHibImpl extends AbstractHibernateDAO implements IParameterUseDAO {

	static private Logger logger = Logger.getLogger(ParameterUseDAOHibImpl.class);
	/**
	 * Load by id.
	 * 
	 * @param id the id
	 * 
	 * @return the sbi paruse
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO#loadById(java.lang.Integer)
	 */
	public SbiParuse loadById(Integer id) throws EMFUserError {
		SbiParuse toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try{
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = (SbiParuse)aSession.load(SbiParuse.class, id);
			tx.commit();
		}catch(HibernateException he){
			logException(he);
			if (tx != null) tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  
		}finally{
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return toReturn;
	}


	/**
	 * Load by use id.
	 * 
	 * @param useID the use id
	 * 
	 * @return the parameter use
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO#loadByUseID(java.lang.Integer)
	 */
	public ParameterUse loadByUseID(Integer useID) throws EMFUserError {
		ParameterUse toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try{
			aSession = getSession();
			tx = aSession.beginTransaction();


			SbiParuse hibParuse = (SbiParuse)aSession.load(SbiParuse.class,  useID);

			toReturn = toParameterUse(hibParuse);
			tx.commit();

		}catch(HibernateException he){
			logException(he);

			if (tx != null) tx.rollback();	

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  

		}finally{
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}

		return toReturn;


	}



	/**
	 * Load by parameter idand role.
	 * 
	 * @param parameterId the parameter id
	 * @param roleName the role name
	 * 
	 * @return the parameter use
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO#loadByParameterIdandRole(java.lang.Integer, java.lang.String)
	 */
	public ParameterUse loadByParameterIdandRole(Integer parameterId, String roleName) throws EMFUserError {
		ParameterUse toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*String hql = "select s from SbiParuse s, SbiParuseDet spd where s.sbiParameters.parId="+parameterId+ " and " +
					     "s.useId = spd.id.sbiParuse.useId and " +
					     "spd.id.sbiExtRoles.name='"+roleName+"'";*/

			String hql = "select s from SbiParuse s, SbiParuseDet spd where s.sbiParameters.parId=?  and " +
			"s.useId = spd.id.sbiParuse.useId and " +
			"spd.id.sbiExtRoles.name=? ";

			Query query = aSession.createQuery(hql);
			query.setInteger(0, parameterId.intValue());
			query.setString(1, roleName);

			SbiParuse hibParuse = (SbiParuse)query.uniqueResult();	
			
			if(hibParuse == null){
				logger.error("Par Use not found for role "+roleName+" adn parameter with id "+parameterId);
				return null;
			}
			
			toReturn = toParameterUse(hibParuse);
			tx.commit();
		}catch(HibernateException he){
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  
		}finally{
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return toReturn;
	}



	/**
	 * Fill associated checks for par use.
	 * 
	 * @param aParameterUse the a parameter use
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO#fillAssociatedChecksForParUse(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse)
	 */
	public void fillAssociatedChecksForParUse(ParameterUse aParameterUse)
	throws EMFUserError {

		Session aSession = null;
		Transaction tx = null;
		Integer useID = aParameterUse.getUseID();

		try{
			aSession = getSession();
			tx = aSession.beginTransaction();


			SbiParuse hibParuse = (SbiParuse)aSession.load(SbiParuse.class,  useID);

			fillParameterUse(aParameterUse, hibParuse);

			tx.commit();

		}catch(HibernateException he){
			logException(he);

			if (tx != null) tx.rollback();	

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  

		}finally{
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	}

	/**
	 * Fill roles for par use.
	 * 
	 * @param aParameterUse the a parameter use
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO#fillRolesForParUse(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse)
	 */
	public void fillRolesForParUse(ParameterUse aParameterUse)
	throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		Integer useID = aParameterUse.getUseID();

		try{
			aSession = getSession();
			tx = aSession.beginTransaction();


			SbiParuse hibParuse = (SbiParuse)aSession.load(SbiParuse.class,  useID);

			fillParameterUse(aParameterUse, hibParuse);

			tx.commit();

		}catch(HibernateException he){
			logException(he);

			if (tx != null) tx.rollback();	

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  

		}finally{
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}

	}

	/**
	 * Modify parameter use.
	 * 
	 * @param aParameterUse the a parameter use
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO#modifyParameterUse(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse)
	 */
	public void modifyParameterUse(ParameterUse aParameterUse)
	throws EMFUserError {

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiParuse hibParuse = (SbiParuse) aSession.load(SbiParuse.class,
					aParameterUse.getUseID());
			updateSbiCommonInfo4Update(hibParuse);
			hibParuse.setLabel(aParameterUse.getLabel());
			hibParuse.setName(aParameterUse.getName());
			hibParuse.setDescr(aParameterUse.getDescription());
			hibParuse.setSelectionType(aParameterUse.getSelectionType());
			hibParuse.setMultivalue(aParameterUse.isMultivalue()? new Integer(1): new Integer(0));
			hibParuse.setManualInput(aParameterUse.getManualInput());
			hibParuse.setMaximizerEnabled(aParameterUse.isMaximizerEnabled());
			hibParuse.setValueSelection(aParameterUse.getValueSelection());
			hibParuse.setSelectedLayer(aParameterUse.getSelectedLayer());;
			hibParuse.setSelectedLayerProp(aParameterUse.getSelectedLayerProp());

			SbiLov hibSbiLov = (SbiLov)aSession.load(SbiLov.class, aParameterUse.getIdLov());
			//if the lov id is 0 (-1) then the modality is manual input
			//insert into the DB a null lov_id
			//if the user selected modality is manual input,and before it was a 
			//lov, we don't need a lov_id and so we can delete it
			if (hibSbiLov.getLovId().intValue() == -1
					|| aParameterUse.getManualInput().intValue() == 1) {
				hibParuse.setSbiLov(null);
			} else {
				hibParuse.setSbiLov(hibSbiLov);
			}
			
			SbiLov hibSbiLovForDefault = (SbiLov)aSession.load(SbiLov.class, aParameterUse.getIdLovForDefault());
			if (hibSbiLovForDefault.getLovId().intValue() == -1) {
				hibParuse.setSbiLovForDefault(null);
			} else {
				hibParuse.setSbiLovForDefault(hibSbiLovForDefault);
			}
			hibParuse.setDefaultFormula(aParameterUse.getDefaultFormula());

			Set parUseDets = hibParuse.getSbiParuseDets();
			for (Iterator it = parUseDets.iterator(); it.hasNext();) {
				aSession.delete((SbiParuseDet) it.next());
			}

			Set parUseCks = hibParuse.getSbiParuseCks();
			for (Iterator it = parUseCks.iterator(); it.hasNext();) {
				aSession.delete((SbiParuseCk) it.next());
			}

			// Recreate Relations with sbi_paruse_det
			List newRoles = aParameterUse.getAssociatedRoles();
			SbiParuseDet hibParUseDet = null;
			SbiParuseDetId hibParUseDetId = null;

			SbiExtRoles tmpExtRole = null;
			Set parUseDetsToSave = new HashSet();
			for (int i = 0; i < newRoles.size(); i++) {
				hibParUseDetId = new SbiParuseDetId();
				hibParUseDetId.setSbiParuse(hibParuse);
				tmpExtRole = (SbiExtRoles) aSession.load(SbiExtRoles.class,
						((Role) newRoles.get(i)).getId());
				hibParUseDetId.setSbiExtRoles(tmpExtRole);
				hibParUseDet = new SbiParuseDet(hibParUseDetId);
				updateSbiCommonInfo4Insert(hibParUseDet);
				aSession.save(hibParUseDet);
				parUseDetsToSave.add(hibParUseDet);
			}

			hibParuse.getSbiParuseDets();
			hibParuse.setSbiParuseDets(parUseDetsToSave);

			// Recreate Relations with sbi_paruse_ck
			List newChecks = aParameterUse.getAssociatedChecks();
			SbiParuseCk hibParUseCk = null;
			SbiParuseCkId hibParUseCkId = null;

			SbiChecks tmpCheck = null;
			Set parUseCkToSave = new HashSet();
			for (int i = 0; i < newChecks.size(); i++) {
				hibParUseCkId = new SbiParuseCkId();
				hibParUseCkId.setSbiParuse(hibParuse);
				tmpCheck = (SbiChecks) aSession.load(SbiChecks.class,
						((Check) newChecks.get(i)).getCheckId());
				hibParUseCkId.setSbiChecks(tmpCheck);
				hibParUseCk = new SbiParuseCk(hibParUseCkId);
				aSession.save(hibParUseCk);
				parUseCkToSave.add(hibParUseCk);
			}

			hibParuse.getSbiParuseCks();
			hibParuse.setSbiParuseCks(parUseCkToSave);
			tx.commit();

		}catch(HibernateException he){
			logException(he);

			if (tx != null) tx.rollback();	

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  

		}finally{

			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	}

	/**
	 * Insert parameter use.
	 * 
	 * @param aParameterUse the a parameter use
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO#insertParameterUse(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse)
	 */
	public void insertParameterUse(ParameterUse aParameterUse)
	throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try{
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiParuse hibParuse = new SbiParuse();
			// Set the relation with parameter
			SbiParameters hibParameters = (SbiParameters)aSession.load(SbiParameters.class, aParameterUse.getId());
			hibParuse.setSbiParameters(hibParameters);

			// Set the relation with idLov 
			SbiLov hibLov = (SbiLov)aSession.load(SbiLov.class, aParameterUse.getIdLov());
			//if the lov id is 0 (-1) then the modality is manual input
			//insert into the DB a null lov_id
			if (hibLov.getLovId().intValue() == -1) {
				hibParuse.setSbiLov(null);
			} else {
				hibParuse.setSbiLov(hibLov);
			}
			
			SbiLov hibSbiLovForDefault = (SbiLov)aSession.load(SbiLov.class, aParameterUse.getIdLovForDefault());
			if (hibSbiLovForDefault.getLovId().intValue() == -1) {
				hibParuse.setSbiLovForDefault(null);
			} else {
				hibParuse.setSbiLovForDefault(hibSbiLovForDefault);
			}
			hibParuse.setDefaultFormula(aParameterUse.getDefaultFormula());

			hibParuse.setLabel(aParameterUse.getLabel());
			hibParuse.setName(aParameterUse.getName());
			hibParuse.setDescr(aParameterUse.getDescription());
			hibParuse.setSelectionType(aParameterUse.getSelectionType());
			hibParuse.setMultivalue(aParameterUse.isMultivalue()? new Integer(1): new Integer(0));
			hibParuse.setManualInput(aParameterUse.getManualInput());
			hibParuse.setMaximizerEnabled(aParameterUse.isMaximizerEnabled());
			hibParuse.setValueSelection(aParameterUse.getValueSelection());
			hibParuse.setSelectedLayer(aParameterUse.getSelectedLayer());
			hibParuse.setSelectedLayerProp(aParameterUse.getSelectedLayerProp());
			updateSbiCommonInfo4Insert(hibParuse);
			Integer useId = (Integer)aSession.save(hibParuse);


			hibParuse = (SbiParuse)aSession.load(SbiParuse.class, useId);
			// Recreate Relations with sbi_paruse_det
			List newRoles = aParameterUse.getAssociatedRoles();
			SbiParuseDet hibParUseDet = null;
			SbiParuseDetId hibParUseDetId = null;

			SbiExtRoles tmpExtRole = null;
			Set parUseDetsToSave = new HashSet();
			for (int i=0; i < newRoles.size(); i++){
				hibParUseDetId = new SbiParuseDetId();
				hibParUseDetId.setSbiParuse(hibParuse);
				tmpExtRole = (SbiExtRoles)aSession.load(SbiExtRoles.class, ((Role)newRoles.get(i)).getId());
				hibParUseDetId.setSbiExtRoles(tmpExtRole);
				hibParUseDet = new SbiParuseDet(hibParUseDetId);
				updateSbiCommonInfo4Insert(hibParUseDet);
				aSession.save(hibParUseDet);
				parUseDetsToSave.add(hibParUseDet);
			}

			hibParuse.setSbiParuseDets(parUseDetsToSave);


			//			 Recreate Relations with sbi_paruse_ck
			List newChecks = aParameterUse.getAssociatedChecks();
			SbiParuseCk hibParUseCk = null;
			SbiParuseCkId hibParUseCkId = null;

			SbiChecks tmpCheck = null;
			Set parUseCkToSave = new HashSet();
			for (int i=0; i < newChecks.size(); i++){
				hibParUseCkId = new SbiParuseCkId();
				hibParUseCkId.setSbiParuse(hibParuse);
				tmpCheck = (SbiChecks)aSession.load(SbiChecks.class, ((Check)newChecks.get(i)).getCheckId());
				hibParUseCkId.setSbiChecks(tmpCheck);
				hibParUseCk = new SbiParuseCk(hibParUseCkId);
				updateSbiCommonInfo4Insert(hibParUseCk);
				aSession.save(hibParUseCk);
				parUseCkToSave.add(hibParUseCk);
			}
			hibParuse.setSbiParuseCks(parUseCkToSave);

			tx.commit();

		}catch(HibernateException he){
			logException(he);

			if (tx != null) tx.rollback();	

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  

		}finally{
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	}

	/**
	 * Erase parameter use.
	 * 
	 * @param aParameterUse the a parameter use
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO#eraseParameterUse(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse)
	 */
	public void eraseParameterUse(ParameterUse aParameterUse)
	throws EMFUserError {

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiParuse hibParuse = (SbiParuse)aSession.load(SbiParuse.class, aParameterUse.getUseID());


			Set parUseDets = hibParuse.getSbiParuseDets();
			for (Iterator it = parUseDets.iterator(); it.hasNext();){
				aSession.delete((SbiParuseDet)it.next());	
			}

			Set parUseCks = hibParuse.getSbiParuseCks();
			for (Iterator it = parUseCks.iterator(); it.hasNext();){
				aSession.delete((SbiParuseCk)it.next());	
			}

			aSession.delete(hibParuse);

			tx.commit();
		}catch(HibernateException he){

			logException(he);

			if (tx != null) tx.rollback();	

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  

		}finally{
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	}

	/**
	 * Checks for par use modes.
	 * 
	 * @param parId the par id
	 * 
	 * @return true, if checks for par use modes
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO#hasParUseModes(java.lang.String)
	 */
	public boolean hasParUseModes(String parId) throws EMFUserError {
		List parameterUseForParameter = loadParametersUseByParId(Integer.valueOf(parId));

		if (parameterUseForParameter.size() > 0) 
			return true;
		else
			return false;
	}


	/**
	 * Load parameters use by par id.
	 * 
	 * @param parId the par id
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO#loadParametersUseByParId(java.lang.Integer)
	 */
	public List loadParametersUseByParId(Integer parId) throws EMFUserError {
		List realResult = new ArrayList();

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			//String hql = "from SbiParuse s where s.sbiParameters.parId="+parId;
			String hql = "from SbiParuse s where s.sbiParameters.parId=? ";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, parId.intValue());
			List result = query.list();

			Iterator it = result.iterator();
			while (it.hasNext()){
				realResult.add(toParameterUse((SbiParuse)it.next()));
			}

			tx.commit();

		}catch(HibernateException he){

			logException(he);

			if (tx != null) tx.rollback();	

			logger.error("HibernateException",he);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  

		}finally{

			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}

		}

		return realResult;
	}


	/**
	 * From the hibernate Parameter Use mode  at input, gives
	 * the corrispondent <code>ParameterUse</code> object.
	 * 
	 * @param hibParUse The hybernate parameter use mode
	 * 
	 * @return The corrispondent <code>ParameterUse</code> object
	 */
	public ParameterUse toParameterUse(SbiParuse hibParUse){
		ParameterUse aParameterUse = new ParameterUse();
		fillParameterUse(aParameterUse, hibParUse);
		return aParameterUse;
	}

	/**
	 * Fill an empty ParameterUse object extracting data from a SbiParuse object.
	 * 
	 * @param aParameterUse the destionation ParameterUse object
	 * @param hibParUse the source SbiParuse object
	 */
	public void fillParameterUse(ParameterUse aParameterUse, SbiParuse hibParUse){
		aParameterUse.setUseID(hibParUse.getUseId());
		aParameterUse.setDescription(hibParUse.getDescr());
		aParameterUse.setId(hibParUse.getSbiParameters().getParId());
		aParameterUse.setLabel(hibParUse.getLabel());
		aParameterUse.setName(hibParUse.getName());

		aParameterUse.setSelectionType(hibParUse.getSelectionType());
		aParameterUse.setMultivalue(hibParUse.getMultivalue() != null && hibParUse.getMultivalue().intValue() > 0);
		aParameterUse.setValueSelection(hibParUse.getValueSelection());
		aParameterUse.setSelectedLayer(hibParUse.getSelectedLayer());
		aParameterUse.setSelectedLayerProp(hibParUse.getSelectedLayerProp());


		//if the sbi_lov is null, then we have a man in modality
		if (hibParUse.getSbiLov() == null) {
			aParameterUse.setIdLov(null);
		} else {
			aParameterUse.setIdLov(hibParUse.getSbiLov().getLovId());
		}
		aParameterUse.setManualInput(hibParUse.getManualInput());
		aParameterUse.setMaximizerEnabled(hibParUse.getMaximizerEnabled());
		
		if (hibParUse.getSbiLovForDefault() == null) {
			aParameterUse.setIdLovForDefault(null);
		} else {
			aParameterUse.setIdLovForDefault(hibParUse.getSbiLovForDefault().getLovId());
		}
		aParameterUse.setDefaultFormula(hibParUse.getDefaultFormula());
		
		List checkList = getAssociatedChecks(hibParUse);
		aParameterUse.setAssociatedChecks(checkList);
		List roleList = getAssociatedRoles(hibParUse);
		aParameterUse.setAssociatedRoles(roleList);
	}
	
	public List getAssociatedChecks(SbiParuse hibParUse) {
		Set hibParUseCheks = hibParUse.getSbiParuseCks();
		SbiParuseCk aSbiParuseCk = null; 
		CheckDAOHibImpl checkDAOHibImpl = new CheckDAOHibImpl();
		Check tmpCheck = null;

		List checkList = new ArrayList();
		for (Iterator itParUseCk = hibParUseCheks.iterator(); itParUseCk.hasNext();){
			aSbiParuseCk = (SbiParuseCk)itParUseCk.next();
			tmpCheck = checkDAOHibImpl.toCheck(aSbiParuseCk.getId().getSbiChecks());
			checkList.add(tmpCheck);
		}
		
		return checkList;
	}
	
	public List getAssociatedRoles(SbiParuse hibParUse) {
		Set hibParUseDets = hibParUse.getSbiParuseDets();
		SbiParuseDet aSbiParuseDet = null; 
		RoleDAOHibImpl roleDAOHibImpl = new RoleDAOHibImpl();
		Role tmpRole = null;

		List roleList = new ArrayList();
		for (Iterator itParUseDet = hibParUseDets.iterator(); itParUseDet.hasNext();){
			aSbiParuseDet = (SbiParuseDet)itParUseDet.next();
			tmpRole = roleDAOHibImpl.toRole(aSbiParuseDet.getId().getSbiExtRoles());
			roleList.add(tmpRole);
		}
		
		return roleList;
	}

	/**
	 * Erase parameter use by par id.
	 * 
	 * @param parId the par id
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO#eraseParameterUseByParId(java.lang.Integer)
	 */
	public void eraseParameterUseByParId(Integer parId) throws EMFUserError {
		List parUseList = null;
		IParameterUseDAO parUseDAO = DAOFactory.getParameterUseDAO();
		parUseList = parUseDAO.loadParametersUseByParId(parId);
		Iterator i = parUseList.iterator();
		while (i.hasNext()){
			ParameterUse parUse = (ParameterUse) i.next();
			parUseDAO.eraseParameterUse(parUse);
		}
	}


	/**
	 * Gets the parameter uses associated to lov.
	 * 
	 * @param lovId the lov id
	 * 
	 * @return the parameter uses associated to lov
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO#getParameterUsesAssociatedToLov(java.lang.Integer)
	 */
	public List getParameterUsesAssociatedToLov(Integer lovId) throws EMFUserError {
		List realResult = new ArrayList();

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			//String hql = "from SbiParuse s where s.sbiLov.lovId="+lovId;
			String hql = "from SbiParuse s where s.sbiLov.lovId=? or s.sbiLovForDefault.lovId=?";

			Query query = aSession.createQuery(hql);
			query.setInteger(0, lovId.intValue());
			query.setInteger(1, lovId.intValue());
			List result = query.list();

			Iterator it = result.iterator();
			while (it.hasNext()){
				realResult.add(toParameterUse((SbiParuse)it.next()));
			}

			tx.commit();

		} catch(HibernateException he) {
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}

		return realResult;
	}




	/**
	 * Erase from hibSession all things related to parameter with parId
	 * 
	 * @param parId the par id
	 * @param sSession the hibernate session
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO#eraseParameterUseByParId(java.lang.Integer)
	 */
	public void eraseParameterUseByParIdSameSession(Integer parId, Session sessionCurrDB) throws EMFUserError {
		logger.debug("IN");
		List parUseList = null;
		IParameterUseDAO parUseDAO = DAOFactory.getParameterUseDAO();
		parUseList = parUseDAO.loadParametersUseByParId(parId);
		Iterator i = parUseList.iterator();
		// run all parameters Use related to Parameter
		try{

			for (Iterator iterator = parUseList.iterator(); iterator.hasNext();) {

				Object o =  iterator.next();
				ParameterUse parameterUse = (ParameterUse) o;
				SbiParuse sbiParuse = (SbiParuse) sessionCurrDB.load(SbiParuse.class, parameterUse.getUseID());

				Set checks = sbiParuse.getSbiParuseCks();
				Set dets =	sbiParuse.getSbiParuseDets();

				logger.debug("Delete details");

				for (Iterator iterator2 = dets.iterator(); iterator2.hasNext();) {
					SbiParuseDet det = (SbiParuseDet) iterator2.next();
					sessionCurrDB.delete(det);
				}
				logger.debug("Delete checks");
				for (Iterator iterator2 = checks.iterator(); iterator2.hasNext();) {
					SbiParuseCk check = (SbiParuseCk) iterator2.next();
					sessionCurrDB.delete(check);
				}

				logger.debug("Delete obj Paruse used on correlation parameters");
				eraseParameterObjUseByParuseIdSameSession(sbiParuse.getUseId(), sessionCurrDB);
				
				sessionCurrDB.delete(sbiParuse);
				sessionCurrDB.flush();
				logger.debug("OUT");

			}
		}
		catch(Exception ex){
			logException(ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  
		}
	}

	/*
	 *  delete all objects paruse related to parUSe that must be deleted
	 */
	public void eraseParameterObjUseByParuseIdSameSession(Integer parUseId, Session sessionCurrDB) throws EMFUserError {
		logger.debug("IN");
		String q = "from SbiObjParuse use where use.id.sbiParuse.useId = '" + parUseId + "' order by use.prog desc";
		Query hqlQuery = sessionCurrDB.createQuery(q);
		try{
			List hibUse = (List) hqlQuery.list();

			for (Iterator iterator = hibUse.iterator(); iterator.hasNext();) {
				SbiObjParuse object = (SbiObjParuse) iterator.next();
				sessionCurrDB.delete(object);
			}

		}
		catch (Exception e) {
			logger.error("Error in deleting SbiObjParuse associated to paruse with id "+parUseId+": go on anyway; exception will be thrown when trying to erase paruse");
		}
		logger.debug("OUT");

	}
	
	
	/**
	 * Delete from hibernate session a parameter use
	 * 
	 * @param Session hibernate Session
	 * 
	 * @return The list of parameter uses associated to the lov identified by the lovId at input
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void eraseParameterUseByIdSameSession(Integer parUseId, Session sessionCurrDB) throws EMFUserError
	{
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		try {
//			aSession = getSession();
//			tx = aSession.beginTransaction();

			SbiParuse hibParuse = (SbiParuse)sessionCurrDB.load(SbiParuse.class, parUseId);

			logger.debug("delete ParUSeDet");
			Set parUseDets = hibParuse.getSbiParuseDets();
			for (Iterator it = parUseDets.iterator(); it.hasNext();){
				sessionCurrDB.delete((SbiParuseDet)it.next());	
			}

			logger.debug("delete ParUSeCk");
			Set parUseCks = hibParuse.getSbiParuseCks();
			for (Iterator it = parUseCks.iterator(); it.hasNext();){
				sessionCurrDB.delete((SbiParuseCk)it.next());	
			}
			logger.debug("delete ParObjUse");
			eraseParameterObjUseByParuseIdSameSession(parUseId, sessionCurrDB);
			
			sessionCurrDB.delete(hibParuse);
			
//			aSession.delete(hibParuse);
//
//			tx.commit();
		}catch(HibernateException he){

			logException(he);

			if (tx != null) tx.rollback();	
			logger.error("Error in deleting SbiParuse with id "+parUseId);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  

		}finally{
//			if (aSession!=null){
//				if (aSession.isOpen()) aSession.close();
//			}
		}
		
		logger.debug("OUT");

	}
	

	
	public void eraseParameterUseDetAndCkSameSession(Integer parUseId, Session sessionCurrDB) throws EMFUserError
	{
		logger.debug("IN");

		//SbiParuse hibParuse = (SbiParuse)sessionCurrDB.load(SbiParuseCk.class, parUseId);

		String qCk = "from SbiParuseCk ck where ck.id.sbiParuse.useId = ?";
		Query hqlQueryCk = sessionCurrDB.createQuery(qCk);
		hqlQueryCk.setInteger(0, parUseId);

		String qDet = "from SbiParuseDet det where det.id.sbiParuse.useId = ?";
		Query hqlQueryDet = sessionCurrDB.createQuery(qDet);
		hqlQueryDet.setInteger(0, parUseId);

		try{
			logger.debug("delete ParUSeDet for paruse ");
			List hibDet = (List) hqlQueryDet.list();
			for (Iterator iterator = hibDet.iterator(); iterator.hasNext();) {
				SbiParuseDet  hibParuseDet = (SbiParuseDet) iterator.next();
				sessionCurrDB.delete(hibParuseDet);	
			}
			
			logger.debug("delete ParUSeCk for paruse ");
			List hibCk = (List) hqlQueryCk.list();
			for (Iterator iterator = hibCk.iterator(); iterator.hasNext();) {
				SbiParuseCk  hibParuseCk = (SbiParuseCk) iterator.next();
				sessionCurrDB.delete(hibParuseCk);	
			}


		}catch(HibernateException he){

			logException(he);

			logger.error("Error in deleting checks and dets associated to SbiParuse with id "+parUseId, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  

		}finally{
		}

		logger.debug("OUT");

	}
	
}
