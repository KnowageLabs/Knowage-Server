package it.eng.spagobi.tools.timespan.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.timespan.metadata.SbiTimespan;

import java.util.List;

public interface ITimespanDAO extends ISpagoBIDao {

	public SbiTimespan loadTimespan(Integer timespanId);

	public List<SbiTimespan> listTimespan();

	public Integer insertTimespan(SbiTimespan timespan);

	public void modifyTimespan(SbiTimespan timespan);

	public void deleteTimespan(Integer timespanId);

	public void cloneSpan(Integer timespanId, Integer delay);

}
