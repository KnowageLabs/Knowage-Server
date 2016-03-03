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

import java.util.List;

import org.hibernate.Session;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
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

	public IDataSet loadDataSetById(Integer id);

	public SbiDataSet loadSbiDataSetById(Integer id, Session session);

	public List<IDataSet> loadDataSetsByOwner(String owner, Boolean includeOwned, Boolean includePublic, Boolean showDerivedDatasets);

	public List<IDataSet> loadEnterpriseDataSets();

	public List<IDataSet> loadUserDataSets(String user);

	public List<IDataSet> loadNotDerivedUserDataSets(String user);

	public List<IDataSet> loadFlatDatasets();

	public List<IDataSet> loadDataSetsOwnedByUser(String user, Boolean showDerivedDatasets);

	public List<IDataSet> loadDatasetsSharedWithUser(String user, Boolean showDerivedDatasets);

	public List<IDataSet> loadDatasetOwnedAndShared(String user);

	public List<IDataSet> loadNotDerivedDatasetOwnedAndShared(String user);

	public List<IDataSet> loadCkanDataSets(String user);

	public List<IDataSet> loadMyDataDataSets(String owner);

	public List<IDataSet> loadDataSets(String owner, Boolean includeOwned, Boolean includePublic, String visibility, String type, String category,
			String implementation, Boolean showDerivedDatasets);

	public List<IDataSet> loadDataSets();

	// ========================================================================================
	// CEATE operations (Crud)
	// ========================================================================================
	public Integer insertDataSet(IDataSet dataSet);

	// ========================================================================================
	// ???
	// ========================================================================================

	public List<IDataSet> loadFilteredDatasetList(String hsql, Integer offset, Integer fetchSize);

	public List<IDataSet> loadPagedDatasetList(Integer offset, Integer fetchSize);

	public List<IDataSet> loadFilteredDatasetList(String hsql, Integer offset, Integer fetchSize, String owner);

	public List<IDataSet> loadPagedDatasetList(Integer offset, Integer fetchSize, String owner, Boolean isPublic);

	/**
	 * @deprecated
	 */
	@Deprecated
	public List<SbiDataSet> loadPagedSbiDatasetConfigList(Integer offset, Integer fetchSize);

	public Integer countBIObjAssociated(Integer dsId);

	public Integer countDatasets();

	public boolean hasBIObjAssociated(String dsId);

	public boolean hasBIKpiAssociated(String dsId);

	public boolean hasBILovAssociated(String dsId);

	// ========================================================================================
	// UPDATE operations (crUd)
	// ========================================================================================

	public void modifyDataSet(IDataSet dataSet);

	public IDataSet restoreOlderDataSetVersion(Integer dsId, Integer dsVersion);

	public Integer getHigherVersionNumForDS(Integer dsId);

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
}
