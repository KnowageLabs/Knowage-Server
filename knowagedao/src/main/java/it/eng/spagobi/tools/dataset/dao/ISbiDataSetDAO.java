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

	public SbiDataSet loadSbiDataSetByLabel(String label);

	public List<SbiDataSet> loadSbiDataSets();

	public List<SbiDataSet> loadDataSets(String owner, Boolean includeOwned, Boolean includePublic, String scope, String type, String category,
			String implementation, Boolean showDerivedDatasets);

	public List<SbiDataSet> loadPaginatedSearchSbiDataSet(String search, Integer page, Integer item_per_page, IEngUserProfile finalUserProfile,
			Boolean seeTechnical, Integer[] ids, boolean spatialOnly);

	public Integer countSbiDataSet(String search) throws EMFUserError;

	public Integer countSbiDataSet(String search, Integer[] ids) throws EMFUserError;

	public SbiDataSet loadSbiDataSetByIdAndOrganiz(Integer id, String organiz);

	public SbiDataSet loadSbiDataSetByIdAndOrganiz(Integer id, String organiz, Session session);

	public default List<SbiDataSet> list() {
		return list(0, 15);
	}

	public default List<SbiDataSet> list(int offset, int fetchSize) {
		return list(offset, fetchSize, null, null, false, Collections.EMPTY_LIST, null);
	}

	public List<SbiDataSet> list(int offset, int fetchSize, String owner, String sortByColumn, boolean reverse, List<Integer> tagIds, SbiDataSetFilter filter);

	public List<SbiDataSet> workspaceList(int offset, int fetchSize, String owner, boolean includeOwned, boolean includePublic, String scope, String type, Set<Domain> categoryList, String implementation, boolean showDerivedDatasets);

	public default List<SbiDataSet> workspaceList(int offset, int fetchSize) {
		return workspaceList(offset, fetchSize, null, false, false, null, null, null, null, true);
	}

	public default List<SbiDataSet> loadCkanDataSets(int offset, int fetchSize, UserProfile user) {
		return workspaceList(offset, fetchSize, user.getUserId().toString(), true, false, null, "USER", UserUtilities.getDataSetCategoriesByUser(user), "SbiCkanDataSet", false);
	}

	public default List<SbiDataSet> loadDataSetsOwnedByUser(int offset, int fetchSize, UserProfile user, boolean showDerivedDatasets) {
		return loadDataSetsByOwner(offset, fetchSize, user, true, false, showDerivedDatasets);
	}

	public default List<SbiDataSet> loadDataSetsByOwner(int offset, int fetchSize, UserProfile user, boolean includeOwned, boolean includePublic, boolean showDerivedDatasets) {
		return workspaceList(offset, fetchSize, user.getUserId().toString(), includeOwned, includePublic, null, null, UserUtilities.getDataSetCategoriesByUser(user), null,
				showDerivedDatasets);
	}

	public default List<SbiDataSet> loadDatasetsSharedWithUser(int offset, int fetchSize, UserProfile profile, boolean showDerivedDataset) {
		return workspaceList(offset, fetchSize, profile.getUserId().toString(), false, false, "PUBLIC", "USER", UserUtilities.getDataSetCategoriesByUser(profile), null,
				showDerivedDataset);
	}

	public default List<SbiDataSet> loadEnterpriseDataSets(int offset, int fetchSize, UserProfile profile) {
		return workspaceList(offset, fetchSize, null, false, false, null, "ENTERPRISE", UserUtilities.getDataSetCategoriesByUser(profile), null, true);
	}

	public default List<SbiDataSet> loadDatasetOwnedAndShared(int offset, int fetchSize, UserProfile user) {
		List<SbiDataSet> results = new ArrayList<>();

		List<SbiDataSet> owened = loadDataSetsOwnedByUser(offset, fetchSize, user, true);
		results.addAll(owened);
		List<SbiDataSet> shared = loadDatasetsSharedWithUser(offset, fetchSize, user, true);
		results.addAll(shared);

		return results;
	}

	public List<SbiDataSet> loadMyDataSets(int offset, int fetchSize, UserProfile userProfile);

	public static SbiDataSetFilter createFilter(String columnFilter, String typeFilter, String valueFilter) {
		SbiDataSetFilter daoFilter = new SbiDataSetFilter();

		daoFilter.setColumn(columnFilter);
		daoFilter.setType(typeFilter);
		daoFilter.setValue(valueFilter);

		return daoFilter;
	}
}
