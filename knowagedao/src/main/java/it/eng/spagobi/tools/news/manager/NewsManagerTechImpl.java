package it.eng.spagobi.tools.news.manager;

import java.util.List;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.news.bo.BasicNews;
import it.eng.spagobi.tools.news.dao.ISbiNewsDAO;

public class NewsManagerTechImpl implements INewsManager {

	@Override
	public List getAllNews(UserProfile userProf) {

		ISbiNewsDAO dao = DAOFactory.getSbiNewsDAO();
		dao.setUserProfile(userProf);

		List<BasicNews> allNews = dao.getAllNews();

		return allNews;

	}

}
