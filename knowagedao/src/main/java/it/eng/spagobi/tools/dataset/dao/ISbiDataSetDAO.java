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
package it.eng.spagobi.tools.dataset.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetFilter;

public interface ISbiDataSetDAO extends ISpagoBIDao {

	SbiDataSet loadSbiDataSetByLabel(String label);

	List<SbiDataSet> loadSbiDataSets();

	List<SbiDataSet> loadDataSets(String owner, Boolean includeOwned, Boolean includePublic, String scope, String type, String category,
			String implementation, Boolean showDerivedDatasets);

	List<SbiDataSet> loadPaginatedSearchSbiDataSet(String search, Integer page, Integer item_per_page, IEngUserProfile finalUserProfile,
			Boolean seeTechnical, Integer[] ids, boolean spatialOnly);

	Integer countSbiDataSet(String search) throws EMFUserError;

	Integer countSbiDataSet(String search, Integer[] ids) throws EMFUserError;

	SbiDataSet loadSbiDataSetByIdAndOrganiz(Integer id, String organiz);

	SbiDataSet loadSbiDataSetByIdAndOrganiz(Integer id, String organiz, Session session);

	default List<SbiDataSet> list() {
		return list(0, 15);
	}

	default List<SbiDataSet> list(int offset, int fetchSize) {
		return list(offset, fetchSize, null, null, false, Collections.EMPTY_LIST, null);
	}

	List<SbiDataSet> list(int offset, int fetchSize, String owner, String sortByColumn, boolean reverse, List<Integer> tagIds, List<SbiDataSetFilter> filter);

	List<SbiDataSet> workspaceList(int offset, int fetchSize, String owner, boolean includeOwned, boolean includePublic, String scope, String type, Set<Domain> categoryList, String implementation, boolean showDerivedDatasets);

	default List<SbiDataSet> workspaceList(int offset, int fetchSize) {
		return workspaceList(offset, fetchSize, null, false, false, null, null, null, null, true);
	}

	default List<SbiDataSet> loadDataSetsOwnedByUser(int offset, int fetchSize, UserProfile user, boolean showDerivedDatasets) {
		return loadDataSetsByOwner(offset, fetchSize, user, true, false, showDerivedDatasets);
	}

	default List<SbiDataSet> loadDataSetsByOwner(int offset, int fetchSize, UserProfile user, boolean includeOwned, boolean includePublic, boolean showDerivedDatasets) {
		return workspaceList(offset, fetchSize, user.getUserId().toString(), includeOwned, includePublic, null, null, UserUtilities.getDataSetCategoriesByUser(user), null,
				showDerivedDatasets);
	}

	default List<SbiDataSet> loadDatasetsSharedWithUser(int offset, int fetchSize, UserProfile profile, boolean showDerivedDataset) {
		return workspaceList(offset, fetchSize, profile.getUserId().toString(), false, false, "PUBLIC", "USER", UserUtilities.getDataSetCategoriesByUser(profile), null,
				showDerivedDataset);
	}

	default List<SbiDataSet> loadEnterpriseDataSets(int offset, int fetchSize, UserProfile profile) {
		return workspaceList(offset, fetchSize, null, false, false, null, "ENTERPRISE", UserUtilities.getDataSetCategoriesByUser(profile), null, true);
	}

	default List<SbiDataSet> loadDatasetOwnedAndShared(int offset, int fetchSize, UserProfile user) {
		List<SbiDataSet> results = new ArrayList<>();

		List<SbiDataSet> owened = loadDataSetsOwnedByUser(offset, fetchSize, user, true);
		results.addAll(owened);
		List<SbiDataSet> shared = loadDatasetsSharedWithUser(offset, fetchSize, user, true);
		results.addAll(shared);

		return results;
	}

	List<SbiDataSet> loadMyDataSets(int offset, int fetchSize, UserProfile userProfile);

	static SbiDataSetFilter createFilter(String columnFilter, String typeFilter, String valueFilter) {
		SbiDataSetFilter daoFilter = new SbiDataSetFilter();

		daoFilter.setColumn(columnFilter);
		daoFilter.setType(typeFilter);
		daoFilter.setValue(valueFilter);

		return daoFilter;
	}
}
