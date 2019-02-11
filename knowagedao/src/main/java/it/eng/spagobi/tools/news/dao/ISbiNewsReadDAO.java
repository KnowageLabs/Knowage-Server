package it.eng.spagobi.tools.news.dao;

import java.util.List;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.news.metadata.SbiNewsRead;

public interface ISbiNewsReadDAO extends ISpagoBIDao {

	public Integer insertNewsRead(Integer id, UserProfile userProfile);

	public List<Integer> getReadNews(UserProfile userProfile);

	public SbiNewsRead getNewsReadByIdAndUser(Integer id, String user);

}
