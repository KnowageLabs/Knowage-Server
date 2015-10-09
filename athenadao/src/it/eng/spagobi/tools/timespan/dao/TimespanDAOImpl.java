package it.eng.spagobi.tools.timespan.dao;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.tools.timespan.metadata.SbiTimespan;

import java.util.List;

public class TimespanDAOImpl extends AbstractHibernateDAO implements ITimespanDAO {

	@Override
	public SbiTimespan loadTimespan(Integer timespanId) {
		return load(SbiTimespan.class, timespanId);
	}

	@Override
	public List<SbiTimespan> listTimespan() {
		return list(SbiTimespan.class);
	}

	@Override
	public Integer insertTimespan(SbiTimespan timespan) {
		return (Integer) insert(timespan);
	}

	@Override
	public void modifyTimespan(SbiTimespan timespan) {
		update(timespan);
	}

	@Override
	public void deleteTimespan(Integer timespanId) {
		delete(SbiTimespan.class, timespanId);
	}

	@Override
	public void cloneSpan(Integer timespanId, Integer delay) {
	}

}
