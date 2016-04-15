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

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.OutputParameter;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiOutputParameter;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
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
			ret.add(new OutputParameter(op.getId(), op.getLabel(), op.getParameterTypeId(), op.getParameterType().getValueNm(), op.getBiobjId()));
		}
		return ret;
	}

	@Override
	public void saveParameter(OutputParameter outputParameter) {
		SbiOutputParameter sop = new SbiOutputParameter();
		sop.setLabel(outputParameter.getName());
		sop.setBiobjId(outputParameter.getBiObjectId());
		sop.setParameterTypeId(outputParameter.getTypeId());
		if (outputParameter.isNewRecord()) {
			insert(sop);
		} else {
			sop.setId(outputParameter.getId());
			update(sop);
		}
	}

	@Override
	public void removeParameter(Integer id) {
		delete(SbiOutputParameter.class, id);
	}

	@Override
	public OutputParameter getOutputParameter(Integer id) {
		try {
			SbiOutputParameter sop = load(SbiOutputParameter.class, id);
			BIObject obj = new BIObject();
			obj.setId(sop.getBiobjId());
			Domain dom = new Domain();
			dom.setValueId(sop.getParameterTypeId());
			return new OutputParameter(id, sop.getLabel(), dom.getValueId(), dom.getValueName(), obj.getId());
		} catch (SpagoBIDOAException e) {
			return null;
		}
	}

	/*
	 * @Override public void saveParameterList(final List<OutputParameter> list, final Integer biobjId) throws EMFUserError { executeOnTransaction(new
	 * IExecuteOnTransaction<Boolean>() {
	 * 
	 * @Override public Boolean execute(Session session) throws JSONException { List<Integer> ids = new ArrayList<>(); for (OutputParameter o : list) {
	 * SbiOutputParameter op = new SbiOutputParameter(); if (o.getId() != null) { ids.add(o.getId());
	 * 
	 * op.setId(o.getId()); updateSbiCommonInfo4Update(op); } else { updateSbiCommonInfo4Insert(op); } op.setBiobjId(biobjId); op.setLabel(o.getLabel());
	 * op.setParTypeId(o.getType().getValueId()); session.save(op); }
	 * 
	 * // deleting orphans String deleteHql = "delete from it.eng.spagobi.tools.crossnavigation.metadata.SbiOutputParameter p where not p.id in (:ids) "; Query
	 * q = session.createQuery(deleteHql); q.setParameterList("ids", ids); q.executeUpdate();
	 * 
	 * return Boolean.TRUE; } }); }
	 */
}
