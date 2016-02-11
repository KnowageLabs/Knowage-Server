package it.eng.spagobi.analiticalmodel.document.dao;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.OutputParameter;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiOutputParameter;

import java.util.ArrayList;
import java.util.List;

public class OutputParameterDAOImpl extends AbstractHibernateDAO implements IOutputParameterDAO {

	@Override
	public List<OutputParameter> getOutputParametersByObjId(Integer id) {
		List<OutputParameter> ret = new ArrayList<>();

		for (SbiOutputParameter op : list(SbiOutputParameter.class)) {
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
