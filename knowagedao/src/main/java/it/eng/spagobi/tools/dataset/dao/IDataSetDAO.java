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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.json.JSONObject;

import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.dataset.bo.DataSetBasicInfo;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;

/**
 * Defines the interfaces for all methods needed to create, read, update and delete a dataset (CRUD operations).
 */
public interface IDataSetDAO extends ISpagoBIDao {

	// ========================================================================================
	// READ operations (cRud)
	// ========================================================================================

	public IDataSet loadDataSetByLabel(String label);

	public IDataSet loadDataSetByName(String label);

	public IDataSet loadDataSetById(Integer id);

	public SbiDataSet loadSbiDataSetById(Integer id, Session session);

	public List<IDataSet> loadDataSetsByOwner(UserProfile user, Boolean includeOwned, Boolean includePublic, Boolean showDerivedDatasets);

	/**
	 * @deprecated Use {@link ISbiDataSetDAO#loadEnterpriseDataSets(int, int, UserProfile)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	public List<IDataSet> loadEnterpriseDataSets(UserProfile user);

	public List<IDataSet> loadUserDataSets(String user);

	public List<IDataSet> loadNotDerivedUserDataSets(UserProfile user);

	public List<IDataSet> loadNotDerivedDataSets(UserProfile user);

	public List<IDataSet> loadFlatDatasets();

	/**
	 * @deprecated Use {@link ISbiDataSetDAO#loadDataSetsOwnedByUser(int, int, UserProfile, boolean)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	public List<IDataSet> loadDataSetsOwnedByUser(UserProfile user, Boolean showDerivedDatasets);

	/**
	 * @deprecated Use {@link ISbiDataSetDAO#loadDatasetsSharedWithUser(int, int, UserProfile, boolean)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	public List<IDataSet> loadDatasetsSharedWithUser(UserProfile user, Boolean showDerivedDatasets);

	/**
	 * @deprecated Use {@link ISbiDataSetDAO#loadDatasetOwnedAndShared(int, int, UserProfile)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	public List<IDataSet> loadDatasetOwnedAndShared(UserProfile user);

	public List<IDataSet> loadNotDerivedDatasetOwnedAndShared(UserProfile user);

	public List<IDataSet> loadCkanDataSets(UserProfile user);

	/**
	 * @deprecated Use {@link ISbiDataSetDAO#loadMyDataSets(int, int, UserProfile)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	public List<IDataSet> loadMyDataDataSets(UserProfile owner);

	public List<DataSetBasicInfo> loadFederatedDataSetsByFederatoinId(Integer id);

	public List<DataSetBasicInfo> loadDatasetsBasicInfoForLov();

	public List<IDataSet> loadDataSets(String owner, Boolean includeOwned, Boolean includePublic, String scope, String type, Set<Domain> categoryList,
			String implementation, Boolean showDerivedDatasets);

	public List<IDataSet> loadDataSets();

	public List<IDataSet> loadDataSetOlderVersions(Integer dsId);

	public List<IDataSet> loadDatasetsByTags(UserProfile user, List<Integer> tagIds, String type);

	// ========================================================================================
	// CEATE operations (Crud)
	// ========================================================================================
	public Integer insertDataSet(IDataSet dataSet);

	public Integer insertDataSet(IDataSet dataSet, Session optionalSession);

	// ========================================================================================
	// ???
	// ========================================================================================

	public List<DataSetBasicInfo> loadDatasetsBasicInfo();

	public List<IDataSet> loadFilteredDatasetList(String hsql, Integer offset, Integer fetchSize);

	public List<IDataSet> loadPagedDatasetList(Integer offset, Integer fetchSize);

	/**
	 * @deprecated Replaced by {@link #loadFilteredDatasetList(int, int, String, String, boolean, List)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	public List<IDataSet> loadFilteredDatasetList(String hsql, Integer offset, Integer fetchSize, String owner);

	/**
	 * @deprecated Replaced by {@link #loadFilteredDatasetList(int, int, String, String, boolean, List)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	public List<IDataSet> loadPagedDatasetList(Integer offset, Integer fetchSize, String owner);

	/**
	 * @deprecated Replaced by {@link #loadFilteredDatasetList(int, int, String, String, boolean, List)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	public List<IDataSet> loadFilteredDatasetList(Integer offset, Integer fetchSize, String owner, JSONObject filters, JSONObject ordering,
			List<Integer> tagIds);

	/**
	 * @deprecated Replaced by {@link #loadFilteredDatasetList(int, int, String, String, boolean, List)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	public List<SbiDataSet> loadPagedSbiDatasetConfigList(Integer offset, Integer fetchSize);

	public default List<IDataSet> loadFilteredDatasetList() {
		return loadFilteredDatasetList(0, 15);
	}

	public default List<IDataSet> loadFilteredDatasetList(int offset, int fetchSize) {
		return loadFilteredDatasetList(offset, fetchSize, null, null, false, Collections.EMPTY_LIST);
	}

	/**
	 *
	 * @param offset
	 * @param fetchSize
	 * @param owner
	 * @param sortByColumn
	 * @param reverse
	 * @param tagIds
	 * @return
	 */
	public List<IDataSet> loadFilteredDatasetList(int offset, int fetchSize, String owner, String sortByColumn, boolean reverse, List<Integer> tagIds);

	public Integer countBIObjAssociated(Integer dsId);

	public Integer countDatasets();

	public boolean hasBIObjAssociated(String dsId);

	public boolean hasBILovAssociated(String dsId);

	// ========================================================================================
	// UPDATE operations (crUd)
	// ========================================================================================

	public void modifyDataSet(IDataSet dataSet);

	public void modifyDataSet(IDataSet dataSet, Session optionalSession);

	public IDataSet restoreOlderDataSetVersion(Integer dsId, Integer dsVersion);

	public Integer getHigherVersionNumForDS(Integer dsId);

	public void updateDatasetOlderVersion(IDataSet dataSet);

	// ========================================================================================
	// DELETE operations (cruD)
	// ========================================================================================
	public void deleteDataSet(Integer dsID);

	public boolean deleteInactiveDataSetVersion(Integer dsVerdionID, Integer dsId);

	public boolean deleteAllInactiveDataSetVersions(Integer dsID);

	public void deleteDataSetNoChecks(Integer dsID);

	// ========================================================================================
	// UTILITY methods
	// ========================================================================================
	/**
	 * @deprecated
	 */
	@Deprecated
	public IDataSet toGuiGenericDataSet(IDataSet iDataSet);

	/**
	 * @deprecated
	 */
	@Deprecated
	public SbiDataSet copyDataSet(SbiDataSet hibDataSet);

	public List<IDataSet> loadFilteredDatasetByTypeList(String string, String qbeDsType);
}
