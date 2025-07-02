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

	IDataSet loadDataSetByLabel(String label);

	IDataSet loadDataSetByName(String label);

	IDataSet loadDataSetById(Integer id);

	SbiDataSet loadSbiDataSetById(Integer id, Session session);

	List<IDataSet> loadDataSetsByOwner(UserProfile user, Boolean includeOwned, Boolean includePublic,
			Boolean showDerivedDatasets);

	/**
	 * @deprecated Use {@link ISbiDataSetDAO#loadEnterpriseDataSets(int, int, UserProfile)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	List<IDataSet> loadEnterpriseDataSets(UserProfile user);

	List<IDataSet> loadUserDataSets(String user);

	List<IDataSet> loadNotDerivedUserDataSets(UserProfile user);

	List<IDataSet> loadNotDerivedDataSets(UserProfile user);

	List<IDataSet> loadFlatDatasets();

	/**
	 * @deprecated Use {@link ISbiDataSetDAO#loadDataSetsOwnedByUser(int, int, UserProfile, boolean)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	List<IDataSet> loadDataSetsOwnedByUser(UserProfile user, Boolean showDerivedDatasets);

	/**
	 * @deprecated Use {@link ISbiDataSetDAO#loadDatasetsSharedWithUser(int, int, UserProfile, boolean)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	List<IDataSet> loadDatasetsSharedWithUser(UserProfile user, Boolean showDerivedDatasets);

	/**
	 * @deprecated Use {@link ISbiDataSetDAO#loadDatasetOwnedAndShared(int, int, UserProfile)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	List<IDataSet> loadDatasetOwnedAndShared(UserProfile user);

	List<IDataSet> loadNotDerivedDatasetOwnedAndShared(UserProfile user);

	/**
	 * @deprecated Use {@link ISbiDataSetDAO#loadMyDataSets(int, int, UserProfile)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	List<IDataSet> loadMyDataDataSets(UserProfile owner);

	List<DataSetBasicInfo> loadFederatedDataSetsByFederatoinId(Integer id);

	List<DataSetBasicInfo> loadDatasetsBasicInfoForLov();

	List<DataSetBasicInfo> loadDatasetsBasicInfoForAI(List<Integer> idsObject);

	List<IDataSet> loadDataSets(String owner, Boolean includeOwned, Boolean includePublic, String scope, String type,
			Set<Domain> categoryList, String implementation, Boolean showDerivedDatasets);

	List<IDataSet> loadDataSets();

	List<IDataSet> loadDataSetOlderVersions(Integer dsId);

	List<IDataSet> loadDatasetsByTags(UserProfile user, List<Integer> tagIds, String type);

	// ========================================================================================
	// CEATE operations (Crud)
	// ========================================================================================
	Integer insertDataSet(IDataSet dataSet);

	Integer insertDataSet(IDataSet dataSet, Session optionalSession);

	// ========================================================================================
	// ???
	// ========================================================================================

	List<IDataSet> loadFilteredDatasetList(String hsql, Integer offset, Integer fetchSize);

	List<IDataSet> loadPagedDatasetList(Integer offset, Integer fetchSize);

	/**
	 * @deprecated Replaced by {@link #loadFilteredDatasetList(int, int, String, String, boolean, List)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	List<IDataSet> loadFilteredDatasetList(String hsql, Integer offset, Integer fetchSize, String owner);

	/**
	 * @deprecated Replaced by {@link #loadFilteredDatasetList(int, int, String, String, boolean, List)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	List<IDataSet> loadPagedDatasetList(Integer offset, Integer fetchSize, String owner);

	/**
	 * @deprecated Replaced by {@link #loadFilteredDatasetList(int, int, String, String, boolean, List)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	List<IDataSet> loadFilteredDatasetList(Integer offset, Integer fetchSize, String owner, JSONObject filters,
			JSONObject ordering, List<Integer> tagIds);

	/**
	 * @deprecated Replaced by {@link #loadFilteredDatasetList(int, int, String, String, boolean, List)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	List<SbiDataSet> loadPagedSbiDatasetConfigList(Integer offset, Integer fetchSize);

	default List<IDataSet> loadFilteredDatasetList() {
		return loadFilteredDatasetList(0, 15);
	}

	default List<IDataSet> loadFilteredDatasetList(int offset, int fetchSize) {
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
	List<IDataSet> loadFilteredDatasetList(int offset, int fetchSize, String owner, String sortByColumn,
			boolean reverse, List<Integer> tagIds);

	Integer countBIObjAssociated(Integer dsId);

	Integer countDatasets();

	boolean hasBIObjAssociated(String dsId);

	boolean hasBILovAssociated(String dsId);

	// ========================================================================================
	// UPDATE operations (crUd)
	// ========================================================================================

	void modifyDataSet(IDataSet dataSet);

	void modifyDataSet(IDataSet dataSet, Session optionalSession);

	IDataSet restoreOlderDataSetVersion(Integer dsId, Integer dsVersion);

	Integer getHigherVersionNumForDS(Integer dsId);

	void updateDatasetOlderVersion(IDataSet dataSet);

	// ========================================================================================
	// DELETE operations (cruD)
	// ========================================================================================
	void deleteDataSet(Integer dsID);

	boolean deleteInactiveDataSetVersion(Integer dsVerdionID, Integer dsId);

	boolean deleteAllInactiveDataSetVersions(Integer dsID);

	void deleteDataSetNoChecks(Integer dsID);

	// ========================================================================================
	// UTILITY methods
	// ========================================================================================
	/**
	 * @deprecated
	 */
	@Deprecated
	IDataSet toGuiGenericDataSet(IDataSet iDataSet);

	/**
	 * @deprecated
	 */
	@Deprecated
	SbiDataSet copyDataSet(SbiDataSet hibDataSet);

	List<IDataSet> loadFilteredDatasetByTypeList(String string, String qbeDsType);

	IDataSet loadDataSetByLabelAndUserCategories(String label);

	List<IDataSet> loadDerivedDataSetByLabel(String label);

	Integer countCategories(Integer catId);

	List<IDataSet> loadDataSetByCategoryId(Integer catId);

	List<String> loadPersistenceTableNames(Integer dsId);
}
