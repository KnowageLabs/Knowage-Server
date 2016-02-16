/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
