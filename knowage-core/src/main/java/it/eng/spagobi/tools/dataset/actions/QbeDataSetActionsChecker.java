/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

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
package it.eng.spagobi.tools.dataset.actions;

import java.util.List;

import org.apache.log4j.Logger;

import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.exceptions.ActionNotPermittedException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class QbeDataSetActionsChecker extends AbstractDatasetActionsChecker {

	static private Logger logger = Logger.getLogger(QbeDataSetActionsChecker.class);

	public QbeDataSetActionsChecker(UserProfile userProfile, IDataSet dataset) {
		super(userProfile, dataset);
	}

	@Override
	public void canLoadData() throws ActionNotPermittedException {
		canSeeUnderlyingModel();
	}

	@Override
	public void canEdit() throws ActionNotPermittedException {
		canSeeUnderlyingModel();
	}

	@Override
	public void canSave() throws ActionNotPermittedException {
		canSeeUnderlyingModel();
	}

	@Override
	public void canShare() throws ActionNotPermittedException {
		canSeeUnderlyingModel();
	}

	private void canSeeUnderlyingModel() throws ActionNotPermittedException {
		logger.debug("IN");
		try {
			QbeDataSet qbeDataSet = (QbeDataSet) getDataset();

			if (!qbeDataSet.getScopeCd().equals(SpagoBIConstants.DS_SCOPE_USER)) {
				// in case dataset is enterprise or technical, checking if user can see underlying model does not apply; it applies only in case dataset has
				// USER scope
				return;
			}

			String modelName = qbeDataSet.getDatamarts();

			List<MetaModel> businessModelList = null;
			IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
			businessModelsDAO.setUserProfile(getUserProfile());

			IRoleDAO roleDao = DAOFactory.getRoleDAO();
			roleDao.setUserProfile(getUserProfile());

			List<String> roleNames = UserUtilities.getCurrentRoleNames(getUserProfile());
			logger.warn("Roles: " + roleNames);
			List<Integer> categories = roleDao.getMetaModelCategoriesForRoles(roleNames);
			logger.warn("Categories founded: " + categories);
			logger.debug("Found the following categories [" + categories + "].");
			if (categories != null && !categories.isEmpty()) {
				businessModelList = businessModelsDAO.loadMetaModelByCategories(categories);
			}

			boolean canSeeUnderlyingModel = false;
			if (businessModelList != null && !businessModelList.isEmpty()) {
				for (int i = 0; i < businessModelList.size(); i++) {
					if (businessModelList.get(i).getName().equals(modelName)) {
						canSeeUnderlyingModel = true;
						break;
					}
				}
			}

			if (!canSeeUnderlyingModel) {
				throw new ActionNotPermittedException("User cannot see the underlying business model", "dataset.error.qbe.model.not.visible");
			}
		} catch (ActionNotPermittedException e) {
			throw e;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An error occurred while cheking if user can see business model", e);
		} finally {
			logger.debug("OUT");
		}
	}

}
