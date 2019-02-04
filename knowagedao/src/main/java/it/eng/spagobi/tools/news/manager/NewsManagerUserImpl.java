package it.eng.spagobi.tools.news.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.tools.news.bo.News;
import it.eng.spagobi.tools.news.dao.ISbiNewsDAO;
import it.eng.spagobi.tools.news.metadata.SbiNews;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class NewsManagerUserImpl implements INewsManager {

	@Override
	public List<News> getAllNews(UserProfile userProf) {
		try {
			List listOfRoles = (List) userProf.getRoles();
			IRoleDAO roleDao = DAOFactory.getRoleDAO();
			ISbiNewsDAO newsDao = DAOFactory.getSbiNewsDAO();

			Set tmpSet = new HashSet<>();
			List listOfNews = new ArrayList<>();

			for (int i = 0; i < listOfRoles.size(); i++) {
				Role role = roleDao.loadByName((String) listOfRoles.get(i));
				SbiExtRoles extRoles = roleDao.loadSbiExtRoleById(role.getId());
				Set setOfRoles = extRoles.getSbiNewsRoles();

				tmpSet.addAll(setOfRoles);

			}
			Iterator<SbiNews> iterator = tmpSet.iterator();
			while (iterator.hasNext()) {
				SbiNews sbiNews = iterator.next();
				News bussinesNews;
				try {
					bussinesNews = newsDao.toBasicNews(sbiNews);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new SpagoBIRuntimeException(e.getMessage(), e);
				}
				listOfNews.add(bussinesNews);

			}

			return listOfNews;

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot get all news", e);
		}
	}

}
