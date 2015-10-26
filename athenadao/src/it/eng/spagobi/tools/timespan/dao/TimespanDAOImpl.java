package it.eng.spagobi.tools.timespan.dao;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.timespan.metadata.SbiTimespan;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class TimespanDAOImpl extends AbstractHibernateDAO implements ITimespanDAO {

	@Override
	public SbiTimespan loadTimespan(Integer timespanId) {
		return load(SbiTimespan.class, timespanId);
	}

	@Override
	public List<SbiTimespan> listTimespan() {
		return list(new ICriterion<SbiTimespan>() {
			@Override
			public Criteria evaluate(Session session) {
				Criteria c = session.createCriteria(SbiTimespan.class);
				c.addOrder(Order.desc("staticFilter"));
				return c;
			}
		});
	}

	@Override
	public List<SbiTimespan> listDynTimespan() {
		return list(new ICriterion<SbiTimespan>() {
			@Override
			public Criteria evaluate(Session session) {
				Criteria c = session.createCriteria(SbiTimespan.class);
				c.add(Restrictions.eq("staticFilter", false));
				return c;
			}
		});
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
