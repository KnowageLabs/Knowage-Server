/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v3.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spagobi.api.dto.CategoryDTO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public class CategoryService {
	private static final Logger logger = Logger.getLogger(CategoryService.class);

	public List<CategoryDTO> getCategories(UserProfile profile) {
		logger.debug("IN");
		ICategoryDAO categoryDAO = null;
		List<CategoryDTO> listToReturn = new ArrayList<CategoryDTO>();
		try {
			categoryDAO = DAOFactory.getCategoryDAO();
			categoryDAO.setUserProfile(profile);
			List<SbiCategory> listToAnalyze = categoryDAO.getCategories().stream().collect(Collectors.toList());
			CategoryAPIImpl catAPI = new CategoryAPIImpl();
			for (SbiCategory sbiCategory : listToAnalyze) {
				listToReturn.add(
						new CategoryDTO(sbiCategory.getCode(), sbiCategory.getName(), sbiCategory.getType(), catAPI.getNumberOfCategoryUsages(sbiCategory)));
			}
		} catch (Exception ex) {
			LogMF.error(logger, "Cannot get available categories for user {0}", new Object[] { profile.getUserName() });
			throw new SpagoBIServiceException("An unexpected error occured while executing service", ex);
		} finally {
			logger.debug("OUT");
		}
		return listToReturn;
	}
}
