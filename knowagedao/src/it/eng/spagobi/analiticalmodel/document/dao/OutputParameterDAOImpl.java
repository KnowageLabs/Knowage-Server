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
import it.eng.spagobi.analiticalmodel.document.bo.OutputParameter;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.tools.crossnavigation.dao.ICrossNavigationDAO;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigation;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigationPar;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiOutputParameter;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

public class OutputParameterDAOImpl extends AbstractHibernateDAO implements IOutputParameterDAO {

	@Override
	public List<OutputParameter> getOutputParametersByObjId(final Integer id) {
		List<OutputParameter> ret = new ArrayList<>();

		List<SbiOutputParameter> paramList = list(new ICriterion<SbiOutputParameter>() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiOutputParameter.class).add(Restrictions.eq("biobjId", id));
			}
		});
		for (SbiOutputParameter op : paramList) {
			ret.add(from(op));
		}
		return ret;
	}

	private OutputParameter from(SbiOutputParameter op) {
		OutputParameter outp = new OutputParameter();
		outp.setId(op.getId());
		outp.setName(op.getLabel());
		outp.setBiObjectId(op.getBiobjId());
		if (op.getParameterType() != null) {
			outp.setType(from(op.getParameterType()));
		}
		outp.setFormatCode(op.getFormatCode());
		outp.setFormatValue(op.getFormatValue());
		outp.setIsUserDefined(op.getIsUserDefined());

		return outp;
	}

	@Override
	public Integer saveParameter(OutputParameter outputParameter) {
		SbiOutputParameter sop = null;
		Integer id = outputParameter.getId();
		if (id == null) {
			sop = new SbiOutputParameter();
		} else {
			sop = load(SbiOutputParameter.class, outputParameter.getId());
		}
		sop.setLabel(outputParameter.getName());
		sop.setBiobjId(outputParameter.getBiObjectId());
		sop.setParameterTypeId(outputParameter.getType().getValueId());
		sop.setIsUserDefined(outputParameter.getIsUserDefined());

		if (outputParameter.getType() != null && outputParameter.getType().getValueCd().equals("DATE")) {
			sop.setFormatCode(outputParameter.getFormatCode());
			sop.setFormatValue(outputParameter.getFormatValue());
		} else {
			sop.setFormatCode(null);
			sop.setFormatValue(null);
		}
		if (outputParameter.getId() == null) {
			id = (Integer) insert(sop);
		} else {
			update(sop);
		}
		return id;
	}

	@Override
	public void removeParameter(Integer id, Session aSession) throws EMFUserError {
		ICrossNavigationDAO crossNavigationDao = DAOFactory.getCrossNavigationDAO();
		List<SbiCrossNavigationPar> cnParToRemove = crossNavigationDao.listNavigationsByOutputParameters(id, aSession);
		List<Integer> crossNavigation = new ArrayList<Integer>();
		// Delete FROM CROSS_NAVIFATION_PAR
		for (SbiCrossNavigationPar cn : cnParToRemove) {
			aSession.delete(cn);
			if (!crossNavigation.contains(cn.getSbiCrossNavigation().getId()))
				crossNavigation.add(cn.getSbiCrossNavigation().getId());
		}
		// Delete FROM CROSS_NAVIGATION
		for (Integer scnId : crossNavigation) {
			List<SbiCrossNavigationPar> sbiCrossPar = crossNavigationDao.listNavigationsByCrossNavParId(scnId, aSession);
			if (sbiCrossPar == null || sbiCrossPar.size() == 0) {
				SbiCrossNavigation snc = crossNavigationDao.loadSbiCrossNavigationById(scnId, aSession);
				aSession.delete(snc);
			}
		}

		SbiOutputParameter sop = (SbiOutputParameter) aSession.load(SbiOutputParameter.class, id);
		aSession.delete(sop);
	}

	@Override
	public void removeParameter(Integer id) throws EMFUserError {

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			removeParameter(id, aSession);

			// commit all changes
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null && tx.isActive())
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception ex) {
			if (tx != null && tx.isActive())
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public OutputParameter getOutputParameter(Integer id) {
		try {
			SbiOutputParameter sop = load(SbiOutputParameter.class, id);
			return from(sop);
		} catch (SpagoBIDAOException e) {
			return null;
		}
	}

	private Domain from(SbiDomains sbiType) {
		Domain type = new Domain();
		type.setDomainCode(sbiType.getDomainCd());
		type.setDomainName(sbiType.getDomainNm());
		type.setValueCd(sbiType.getValueCd());
		type.setValueDescription(sbiType.getValueDs());
		type.setValueName(sbiType.getValueNm());
		type.setValueId(sbiType.getValueId());
		return type;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IOutputParameterDAO#removeParameters(java.lang.Integer, org.hibernate.Session)
	 */
	@Override
	public void removeParametersByBiobjId(Integer biobjId, Session session) throws EMFUserError {
		List<Integer> ids = session.createCriteria(SbiOutputParameter.class).add(Restrictions.eq("biobjId", biobjId)).setProjection(Property.forName("id"))
				.list();
		for (Integer id : ids) {
			removeParameter(id, session);
		}
	}

	@Override
	public void removeUserDefinedParametersByBiobjId(Integer biobjId, Session session) throws EMFUserError {
		List<Integer> ids = session.createCriteria(SbiOutputParameter.class).add(Restrictions.eq("biobjId", biobjId))
				.add(Restrictions.eq("isUserDefined", true)).setProjection(Property.forName("id")).list();
		for (Integer id : ids) {
			removeParameter(id, session);
		}
	}

	@Override
	public void removeSystemDefinedParametersByBiobjId(Integer biobjId, Session session) throws EMFUserError {

		List<Integer> ids = session.createCriteria(SbiOutputParameter.class).add(Restrictions.eq("biobjId", biobjId)).add(Restrictions.isNull("isUserDefined"))
				.setProjection(Property.forName("id")).list();
		// .add(Restrictions.eq("isUserDefined", false)).setProjection(Property.forName("id")).list();
		for (Integer id : ids) {
			removeParameter(id, session);
		}
	}

}
