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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParameters;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuse;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.metadata.SbiLov;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;

/**
 * Defines the Hibernate implementations for all DAO methods, for a parameter
 *
 * @author zoppello
 */
public class ParameterDAOHibImpl extends AbstractHibernateDAO implements IParameterDAO {
	private static Logger logger = Logger.getLogger(ParameterDAOHibImpl.class);

	/**
	 * Load for detail by parameter id.
	 *
	 * @param parameterID the parameter id
	 *
	 * @return the parameter
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO#loadForDetailByParameterID(java.lang.Integer)
	 */
	@Override
	public Parameter loadForDetailByParameterID(Integer parameterID) throws EMFUserError {
		Parameter toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiParameters hibParameters = (SbiParameters) aSession.load(SbiParameters.class, parameterID);

			toReturn = toParameter(hibParameters);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			rollbackIfActive(tx);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSessionIfOpen(aSession);
		}
		return toReturn;
	}

	@Override
	public Parameter loadForDetailByParameterLabel(String label) throws EMFUserError {
		logger.debug("IN");
		Parameter parameter = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion labelCriterrion = Restrictions.eq("label", label);
			Criteria criteria = aSession.createCriteria(SbiParameters.class);
			criteria.add(labelCriterrion);
			SbiParameters hibPar = (SbiParameters) criteria.uniqueResult();
			if (hibPar == null)
				return null;
			parameter = toParameter(hibPar);
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		logger.debug("OUT");
		return parameter;
	}

	/**
	 * Load for execution by parameter i dand role name.
	 *
	 * @param parameterID the parameter id
	 * @param roleName    the role name
	 *
	 * @return the parameter
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO#loadForExecutionByParameterIDandRoleName(java.lang.Integer, java.lang.String)
	 */
	@Override
	public Parameter loadForExecutionByParameterIDandRoleName(Integer parameterID, String roleName,
			Boolean loadDSwithDrivers) throws EMFUserError {

		Session aSession = null;
		Transaction tx = null;
		Parameter parameter = null;

		try {

			parameter = loadForDetailByParameterID(parameterID);
			Role role = DAOFactory.getRoleDAO().loadByName(roleName);

			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession
					.createQuery("select pu from SbiParuse pu, SbiParuseDet pud where pu.sbiParameters.parId = ? "
							+ " and pud.id.sbiParuse.useId = pu.useId and pud.id.sbiExtRoles.extRoleId = ?");
			hibQuery.setInteger(0, parameter.getId());
			hibQuery.setInteger(1, role.getId());

			List<SbiParuse> results = hibQuery.list();

			if (results == null || results.isEmpty()) {
				if (loadDSwithDrivers) {
					return parameter;
				} else {
					logger.error(
							"No parameteruse for association among parameter " + parameterID + " and role " + roleName);
					List<Object> v = Arrays.asList(roleName);
					throw new EMFUserError(EMFErrorSeverity.ERROR, 1078, v);
				}
			}

			if (results.size() > 1) {
				// this part of code wouldn't never be executed because one role
				// can have only one parameteruse
				// for each parameter. The control is executed before the load
				// of the object so
				// the list would have to contain only one element but if the
				// list contains more than one
				// object it's an error
				logger.error("the parameter with id " + parameterID + " has more than one parameteruse for the role "
						+ roleName);
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			}

			SbiParuse hibParuse = results.get(0);

			// if modval is null, then the parameter always has a man_in
			// modality
			// force the man_in modality to the parameter
			Integer manIn = hibParuse.getManualInput();
			Boolean isMapDriver = (hibParuse.getSelectedLayer() == null || hibParuse.getSelectedLayer().equals(""))
					? false
					: true;
			if (isMapDriver || manIn.intValue() == 1) {
				ModalitiesValue manInModVal = new ModalitiesValue();
				manInModVal.setITypeCd("MAN_IN");
				manInModVal.setITypeId("37"); // why is setted a FIX id?? Isn't possible get it by other dao method ??
				parameter.setModalityValue(manInModVal);
			} else {
				ModalitiesValue modVal = DAOFactory.getModalitiesValueDAO()
						.loadModalitiesValueByID(hibParuse.getSbiLov().getLovId());
				modVal.setSelectionType(hibParuse.getSelectionType());
				modVal.setMultivalue(hibParuse.getMultivalue() != null && hibParuse.getMultivalue().intValue() > 0);
				parameter.setModalityValue(modVal);
			}

			SbiLov currLov = hibParuse.getSbiLovForDefault();
			if (currLov != null) {
				ModalitiesValue lov = DAOFactory.getModalitiesValueDAO().loadModalitiesValueByID(currLov.getLovId());
				parameter.setModalityValueForDefault(lov);
				parameter.setDefaultFormula(null);
			} else {
				parameter.setModalityValueForDefault(null);
				parameter.setDefaultFormula(hibParuse.getDefaultFormula());
			}

			currLov = hibParuse.getSbiLovForMax();
			if (hibParuse.getSbiLovForMax() != null) {
				ModalitiesValue lov = DAOFactory.getModalitiesValueDAO().loadModalitiesValueByID(currLov.getLovId());
				parameter.setModalityValueForMax(lov);
			} else {
				parameter.setModalityValueForMax(null);
			}

			ParameterUseDAOHibImpl dao = new ParameterUseDAOHibImpl();
			List<Check> checks = dao.getAssociatedChecks(hibParuse);
			parameter.setChecks(checks);

			tx.commit();
			return parameter;

		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (EMFUserError emfue) {
			rollbackIfActive(tx);
			throw emfue;
		} finally {
			closeSessionIfOpen(aSession);
		}

	}

	/**
	 * Load all parameters.
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO#loadAllParameters()
	 */
	@Override
	public List<Parameter> loadAllParameters() throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		List<Parameter> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiParameters");
			List<SbiParameters> hibList = hibQuery.list();

			Iterator<SbiParameters> it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toParameter(it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			rollbackIfActive(tx);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSessionIfOpen(aSession);
		}
		return realResult;
	}

	/**
	 * Load all sbiparameters.
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO#loadAllSbiParameters()
	 */
	@Override
	public List<SbiParameters> loadAllSbiParameters() throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		List<SbiParameters> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiParameters");
			List<SbiParameters> hibList = hibQuery.list();

			Iterator<SbiParameters> it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(it.next());
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			rollbackIfActive(tx);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSessionIfOpen(aSession);
		}
		return realResult;
	}

	@Override
	public List<Parameter> loadParametersByLovId(Integer lovId) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		List<Parameter> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(("select distinct params from   SbiParameters as params "
					+ "inner join params.sbiParuses as paruses " + "inner join paruses.sbiLov as lov "
					+ "where  lov.lovId = :lovId"));
			hibQuery.setParameter("lovId", lovId);
			List<SbiParameters> hibList = hibQuery.list();

			Iterator<SbiParameters> it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toParameter(it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			rollbackIfActive(tx);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSessionIfOpen(aSession);
		}
		return realResult;
	}

	@Override
	public List<Parameter> loadParametersByBIObjectLabel(String label) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		List<Parameter> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery("select distinct params from   SbiParameters as params "
					+ "inner join params.sbiObjPars as objpars " + "inner join objpars.sbiObject as obj "
					+ "where  obj.label =  :label");
			hibQuery.setParameter("label", label);
			List<SbiParameters> hibList = hibQuery.list();

			Iterator<SbiParameters> it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toParameter(it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			rollbackIfActive(tx);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSessionIfOpen(aSession);
		}
		return realResult;
	}

	/**
	 * Modify parameter.
	 *
	 * @param aParameter the a parameter
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO#modifyParameter(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter)
	 */
	@Override
	public void modifyParameter(Parameter aParameter) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String[] info = aParameter.getModality().split(",");
			List<String> list = Arrays.asList(info);
			String inputTypeCd = null;
			String inputTypeId = null;

			Iterator<String> iterator = list.iterator();
			while (iterator.hasNext()) {
				inputTypeCd = iterator.next();
				inputTypeId = iterator.next();

			}

			Integer typeId = Integer.valueOf(inputTypeId);
			SbiDomains parameterType = (SbiDomains) aSession.load(SbiDomains.class, typeId);

			SbiParameters hibParameters = (SbiParameters) aSession.load(SbiParameters.class, aParameter.getId());
			updateSbiCommonInfo4Update(hibParameters);
			hibParameters.setDescr(aParameter.getDescription());
			hibParameters.setLength(aParameter.getLength().shortValue());
			hibParameters.setLabel(aParameter.getLabel());
			hibParameters.setName(aParameter.getName());
			hibParameters.setParameterTypeCode(inputTypeCd);
			hibParameters.setMask(aParameter.getMask());
			hibParameters.setParameterType(parameterType);
			hibParameters.setValueSelection(aParameter.getValueSelection());
			hibParameters.setSelectedLayer(aParameter.getSelectedLayer());
			hibParameters.setSelectedLayerProp(aParameter.getSelectedLayerProp());

			if (aParameter.isFunctional())
				hibParameters.setFunctionalFlag((short) 1);
			else
				hibParameters.setFunctionalFlag((short) 0);

			if (aParameter.isTemporal())
				hibParameters.setTemporalFlag((short) 1);
			else
				hibParameters.setTemporalFlag((short) 0);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			rollbackIfActive(tx);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSessionIfOpen(aSession);
		}

	}

	/**
	 * Insert parameter.
	 *
	 * @param aParameter the a parameter
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO#insertParameter(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter)
	 */
	@Override
	public Parameter insertParameter(Parameter aParameter) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String[] info = aParameter.getModality().split(",");
			List<String> list = Arrays.asList(info);
			String inputTypeCd = null;
			String inputTypeId = null;

			Iterator<String> iterator = list.iterator();
			while (iterator.hasNext()) {
				inputTypeCd = iterator.next();
				inputTypeId = iterator.next();

			}

			Integer typeId = Integer.valueOf(inputTypeId);
			SbiDomains parameterType = (SbiDomains) aSession.load(SbiDomains.class, typeId);

			SbiParameters hibParameters = new SbiParameters();
			hibParameters.setDescr(aParameter.getDescription());
			hibParameters.setLength(aParameter.getLength().shortValue());
			hibParameters.setLabel(aParameter.getLabel());
			hibParameters.setName(aParameter.getName());
			hibParameters.setParameterTypeCode(inputTypeCd);
			hibParameters.setMask(aParameter.getMask());
			hibParameters.setParameterType(parameterType);
			if (aParameter.isFunctional())
				hibParameters.setFunctionalFlag((short) 1);
			else
				hibParameters.setFunctionalFlag((short) 0);
			if (aParameter.isTemporal())
				hibParameters.setTemporalFlag((short) 1);
			else
				hibParameters.setTemporalFlag((short) 0);
			updateSbiCommonInfo4Insert(hibParameters);
			aSession.save(hibParameters);
			tx.commit();
			return toParameter(hibParameters);
		} catch (HibernateException he) {
			logException(he);

			rollbackIfActive(tx);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSessionIfOpen(aSession);
		}
	}

	/**
	 * Erase parameter.
	 *
	 * @param aParameter the a parameter
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO#eraseParameter(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter)
	 */
	@Override
	public void eraseParameter(Parameter aParameter) throws EMFUserError {

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiParameters hibParameters = (SbiParameters) aSession.load(SbiParameters.class, aParameter.getId());
			aSession.delete(hibParameters);

			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			rollbackIfActive(tx);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSessionIfOpen(aSession);
		}
	}

	/**
	 * From the hibernate parametes at input, gives the corrispondent <code>Parameter</code> object.
	 *
	 * @param hibParameters The hybernate parameter
	 *
	 * @return The corrispondent <code>Parameter</code> object
	 */
	public Parameter toParameter(SbiParameters hibParameters) {
		Parameter aParameter = new Parameter();
		aParameter.setDescription(hibParameters.getDescr());
		aParameter.setId(hibParameters.getParId());
		aParameter.setLabel(hibParameters.getLabel());
		aParameter.setName(hibParameters.getName());
		aParameter.setLength(hibParameters.getLength().intValue());
		aParameter.setMask(hibParameters.getMask());
		aParameter.setType(hibParameters.getParameterTypeCode());
		aParameter.setTypeId(hibParameters.getParameterType().getValueId());
		if (hibParameters.getFunctionalFlag().intValue() == 0)
			aParameter.setIsFunctional(false);
		else
			aParameter.setIsFunctional(true);
		if (hibParameters.getTemporalFlag().intValue() == 0)
			aParameter.setIsTemporal(false);
		else
			aParameter.setIsTemporal(true);

		aParameter.setSelectedLayer(hibParameters.getSelectedLayer());
		aParameter.setSelectedLayerProp(hibParameters.getSelectedLayerProp());
		aParameter.setValueSelection(hibParameters.getValueSelection());

		return aParameter;
	}

}
