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

import it.eng.spagobi.analiticalmodel.document.bo.OutputParameter;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.commons.metadata.SbiDomains;
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
	public void removeParameter(Integer id) {
		delete(SbiOutputParameter.class, id);
	}

	@Override
	public OutputParameter getOutputParameter(Integer id) {
		try {
			SbiOutputParameter sop = load(SbiOutputParameter.class, id);
			return from(sop);
		} catch (SpagoBIDOAException e) {
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

}
