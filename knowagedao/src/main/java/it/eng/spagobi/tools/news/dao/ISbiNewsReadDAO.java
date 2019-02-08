package it.eng.spagobi.tools.news.dao;

import java.util.List;

import it.eng.spagobi.commons.dao.ISpagoBIDao;

public interface ISbiNewsReadDAO extends ISpagoBIDao {

	public Integer insert(Integer id);

	public List<Integer> getReadNews(String user);

}
