/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.tools.news.manager;

import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.news.bo.BasicNews;
import it.eng.spagobi.tools.news.dao.ISbiNewsDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class NewsManagerImpl implements INewsManager {

	static protected Logger logger = Logger.getLogger(NewsManagerImpl.class);

	/*
	 * public List<BasicNews> getAllNewsUser(UserProfile userProf) { try { List listOfRoles = (List) userProf.getRoles(); IRoleDAO roleDao =
	 * DAOFactory.getRoleDAO(); ISbiNewsDAO newsDao = DAOFactory.getSbiNewsDAO();
	 *
	 * Set tmpSet = new HashSet<>(); List listOfNews = new ArrayList<>();
	 *
	 * for (int i = 0; i < listOfRoles.size(); i++) { Role role = roleDao.loadByName((String) listOfRoles.get(i)); SbiExtRoles extRoles =
	 * roleDao.loadSbiExtRoleById(role.getId()); Set setOfNews = extRoles.getSbiNewsRoles();
	 *
	 * tmpSet.addAll(setOfNews);
	 *
	 * } Iterator<SbiNews> iterator = tmpSet.iterator(); while (iterator.hasNext()) { SbiNews sbiNews = iterator.next(); BasicNews bussinesNews; try {
	 *
	 * bussinesNews = new BasicNews(sbiNews.getId(), sbiNews.getName(), sbiNews.getDescription(), sbiNews.getCategoryId()); // get for sbiNewsRead by sbiNeswId
	 * by user // bussinesNews.setType(sbiNews.getCategoryId()); } catch (Exception e) {
	 *
	 * throw new SpagoBIRuntimeException("Error occured while getting all news", e); } listOfNews.add(bussinesNews);
	 *
	 * }
	 *
	 * return listOfNews;
	 *
	 * } catch (Exception e) { throw new SpagoBIRuntimeException("Cannot get all news", e); } }
	 */

	public List<BasicNews> getAllNewsUser(UserProfile userProf) {

		logger.debug("IN");

		try {

			ISbiNewsDAO dao = DAOFactory.getSbiNewsDAO();
			dao.setUserProfile(userProf);
			List<BasicNews> allNews = dao.getAllNews(userProf);

			return allNews;

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot get all news for user", e);

		} finally {
			logger.debug("OUT");
		}
	}

	public List<BasicNews> getAllNewsTech(UserProfile userProf) {
		logger.debug("IN");

		try {
			ISbiNewsDAO dao = DAOFactory.getSbiNewsDAO();
			dao.setUserProfile(userProf);
			List<BasicNews> allNews = dao.getAllNews();
			logger.debug("OUT");

			return allNews;

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot get all news for technical users", e);

		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public List<BasicNews> getAllNews(UserProfile userProf) {

		if (UserUtilities.isTechnicalUser(userProf)) {
			return getAllNewsTech(userProf);

		} else {
			return getAllNewsUser(userProf);
		}
	}

}
