package it.eng.spagobi.tools.news.dao;

import java.util.List;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

public interface ISbiNewsReadDAO extends ISpagoBIDao {

	public Integer insert(Integer id, UserProfile userProfile);

	public List<Integer> getReadNews(UserProfile userProfile);

}
