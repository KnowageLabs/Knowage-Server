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
package it.eng.spagobi.tools.dataset.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.IDataSetBehaviour;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStoreFilter;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.common.transformer.IDataStoreTransformer;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import junit.framework.TestCase;

public class DataStoreListenerOperatorTest extends TestCase {

	private final DummyDataSet curr = new DummyDataSet();
	private final DataStoreListenerOperator op = new DataStoreListenerOperator();

	private void assertEvent(DataStore prevStore, DataStore currStore, List<IRecord> added, List<IRecord> updated, List<IRecord> deleted, int idField) {

		MetaData metadata = new MetaData();
		metadata.setIdField(idField);
		if (prevStore != null) {
			prevStore.setMetaData(metadata);
		}
		currStore.setMetaData(metadata);

		DataStoreChangedEvent event = op.createEvent(prevStore, currStore, curr);
		assertRecords(added, event.getAdded());
		assertRecords(updated, event.getUpdated());
		assertRecords(deleted, event.getDeleted());
	}

	public void testCreateEvent() {
		// 1
		DataStore store = new DataStore();
		List<IRecord> records = getRecords(new String[] { "1", "2", "3" }, new String[] { "3", "2", "3" }, new String[] { "2", "1", "3" });
		store.setRecords(records);

		assertEvent(null, store, records, new ArrayList<IRecord>(0), new ArrayList<IRecord>(0), -1);

		// 2
		DataStore store2 = new DataStore();
		List<IRecord> records2 = getRecords(new String[] { "1", "10", "15" }, new String[] { "3", "2", "3" }, new String[] { "2", "A", "3" });
		store2.setRecords(records2);
		List<IRecord> updated = getRecords(new String[] { "1", "10", "15" }, new String[] { "2", "A", "3" });
		assertEvent(store, store2, new ArrayList<IRecord>(0), updated, new ArrayList<IRecord>(0), 0);

		// 3
		DataStore store3 = new DataStore();
		records2 = getRecords(new String[] { "3", "2", "3" }, new String[] { "2", "A", "4" }, new String[] { "Q", "4", "7" });
		store3.setRecords(records2);
		updated = getRecords(new String[] { "2", "A", "4" });
		List<IRecord> deleted = getRecords(new String[] { "1", "10", "15" });
		List<IRecord> added = getRecords(new String[] { "Q", "4", "7" });
		assertEvent(store2, store3, added, updated, deleted, 0);
	}

	private void assertRecords(List<IRecord> exps, List<IRecord> acts) {

		assertEquals(exps.size(), acts.size());
		for (int i = 0; i < exps.size(); i++) {
			IRecord exp = exps.get(i);
			IRecord act = acts.get(i);
			assertEquals(exp.getFields().size(), act.getFields().size());

			for (int j = 0; j < exp.getFields().size(); j++) {
				IField expField = exp.getFieldAt(j);
				IField actField = act.getFieldAt(j);

				assertEquals(expField.getValue(), actField.getValue());
			}
		}
	}

	private static List<IRecord> getRecords(String[]... recs) {
		List<IRecord> res = new ArrayList<IRecord>(recs.length);
		for (String[] recV : recs) {
			Record rec = new Record();
			rec.setFields(getFields(recV));
			res.add(rec);
		}

		assert res.size() == recs.length;
		return res;
	}

	private static List<IField> getFields(String... values) {
		List<IField> res = new ArrayList<IField>(values.length);
		for (String v : values) {
			Field f = new Field();
			f.setValue(v);
			res.add(f);
		}

		assert res.size() == values.length;
		return res;
	}

	private class DummyDataSet implements IDataSet {

		private IDataStore store;

		@Override
		public String getDsMetadata() {

			return null;
		}

		@Override
		public void setDsMetadata(String dsMetadata) {

		}

		@Override
		public IMetaData getMetadata() {

			return null;
		}

		@Override
		public void setMetadata(IMetaData metadata) {

		}

		@Override
		public int getId() {

			return 0;
		}

		@Override
		public void setId(int id) {

		}

		@Override
		public String getName() {

			return null;
		}

		@Override
		public void setName(String name) {

		}

		@Override
		public String getDescription() {

			return null;
		}

		@Override
		public void setDescription(String description) {

		}

		@Override
		public String getLabel() {

			return null;
		}

		@Override
		public void setLabel(String label) {

		}

		@Override
		public Integer getCategoryId() {

			return null;
		}

		@Override
		public void setCategoryId(Integer categoryId) {

		}

		@Override
		public String getCategoryCd() {

			return null;
		}

		@Override
		public void setCategoryCd(String categoryCd) {

		}

		@Override
		public String getDsType() {

			return null;
		}

		@Override
		public void setDsType(String dsType) {

		}

		@Override
		public String getConfiguration() {

			return null;
		}

		@Override
		public void setConfiguration(String configuration) {

		}

		@Override
		@SuppressWarnings("rawtypes")
		public Map getProperties() {

			return null;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public void setProperties(Map map) {

		}

		@Override
		public String getOwner() {

			return null;
		}

		@Override
		public void setOwner(String owner) {

		}

		@Override
		public boolean isPublic() {

			return false;
		}

		@Override
		public void setPublic(boolean isPublic) {

		}

		@Override
		public String getUserIn() {

			return null;
		}

		@Override
		public void setUserIn(String userIn) {

		}

		@Override
		public Date getDateIn() {

			return null;
		}

		@Override
		public void setDateIn(Date dateIn) {

		}

		@Override
		public Integer getScopeId() {

			return null;
		}

		@Override
		public void setScopeId(Integer scopeId) {

		}

		@Override
		public String getScopeCd() {

			return null;
		}

		@Override
		public void setScopeCd(String scopeCd) {

		}

		@Override
		public String getParameters() {

			return null;
		}

		@Override
		public void setParameters(String parameters) {

		}

		@Override
		@SuppressWarnings("rawtypes")
		public Map getParamsMap() {

			return null;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public void setParamsMap(Map params) {

		}

		@Override
		@SuppressWarnings("rawtypes")
		public Map getUserProfileAttributes() {

			return null;
		}

		@Override
		public void setUserProfileAttributes(Map<String, Object> attributes) {

		}

		@Override
		public void loadData() {

		}

		@Override
		public void loadData(int offset, int fetchSize, int maxResults) {

		}

		@Override
		public IDataStore getDataStore() {
			return this.store;
		}

		@Override
		public boolean hasBehaviour(String behaviourId) {

			return false;
		}

		@Override
		public Object getBehaviour(String behaviourId) {

			return null;
		}

		@Override
		public void addBehaviour(IDataSetBehaviour behaviour) {

		}

		@Override
		public Integer getTransformerId() {

			return null;
		}

		@Override
		public void setTransformerId(Integer transformerId) {

		}

		@Override
		public String getTransformerCd() {

			return null;
		}

		@Override
		public void setTransformerCd(String transfomerCd) {

		}

		@Override
		public String getPivotColumnName() {

			return null;
		}

		@Override
		public void setPivotColumnName(String pivotColumnName) {

		}

		@Override
		public String getPivotRowName() {

			return null;
		}

		@Override
		public void setPivotRowName(String pivotRowName) {

		}

		@Override
		public boolean isNumRows() {

			return false;
		}

		@Override
		public void setNumRows(boolean numRows) {

		}

		@Override
		public String getPivotColumnValue() {

			return null;
		}

		@Override
		public void setPivotColumnValue(String pivotColumnValue) {

		}

		@Override
		public boolean hasDataStoreTransformer() {

			return false;
		}

		@Override
		public void removeDataStoreTransformer() {

		}

		@Override
		public void setAbortOnOverflow(boolean abortOnOverflow) {

		}

		@Override
		public void addBinding(String bindingName, Object bindingValue) {

		}

		@Override
		public void setDataStoreTransformer(IDataStoreTransformer transformer) {

		}

		@Override
		public IDataStoreTransformer getDataStoreTransformer() {

			return null;
		}

		@Override
		public boolean isPersisted() {

			return false;
		}

		@Override
		public void setPersisted(boolean persisted) {

		}

		@Override
		public boolean isScheduled() {

			return false;
		}

		@Override
		public void setScheduled(boolean scheduled) {

		}

		@Override
		public boolean isFlatDataset() {

			return false;
		}

		@Override
		public String getFlatTableName() {

			return null;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public List getNoActiveVersions() {

			return null;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public void setNoActiveVersions(List noActiveVersions) {

		}

		@Override
		public String getPersistTableName() {

			return null;
		}

		@Override
		public void setPersistTableName(String persistTableName) {

		}

		@Override
		public SpagoBiDataSet toSpagoBiDataSet() {

			return null;
		}

		@Override
		public IDataStore test() {

			return null;
		}

		@Override
		public IDataStore test(int offset, int fetchSize, int maxResults) {

			return null;
		}

		@Override
		public String getSignature() {

			return null;
		}

		@Override
		public IDataSetTableDescriptor persist(String tableName, IDataSource dataSource) {

			return null;
		}

		@Override
		public IDataStore getDomainValues(String fieldName, Integer start, Integer limit, IDataStoreFilter filter) {

			return null;
		}

		@Override
		public IDataStore decode(IDataStore datastore) {

			return null;
		}

		@Override
		public boolean isCalculateResultNumberOnLoadEnabled() {

			return false;
		}

		@Override
		public void setCalculateResultNumberOnLoad(boolean enabled) {

		}

		@Override
		public void setDataSource(IDataSource dataSource) {

		}

		@Override
		public IDataSource getDataSource() {

			return null;
		}

		@Override
		public String getTableNameForReading() {

			return null;
		}

		@Override
		public IDataSource getDataSourceForReading() {

			return null;
		}

		@Override
		public void setDataSourceForReading(IDataSource dataSource) {

		}

		@Override
		public String getOrganization() {

			return null;
		}

		@Override
		public void setOrganization(String organization) {

		}

		@Override
		public IDataSource getDataSourceForWriting() {

			return null;
		}

		@Override
		public void setDataSourceForWriting(IDataSource dataSource) {

		}

		@Override
		public FederationDefinition getDatasetFederation() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setDatasetFederation(FederationDefinition datasetFederation) {
			// TODO Auto-generated method stub

		}

		@Override
		public String getStartDateField() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setStartDateField(String startDateField) {
			// TODO Auto-generated method stub

		}

		@Override
		public String getEndDateField() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setEndDateField(String endDateField) {
			// TODO Auto-generated method stub

		}

		@Override
		public String getSchedulingCronLine() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setSchedulingCronLine(String schedulingCronLine) {
			// TODO Auto-generated method stub

		}

		@Override
		public Map<String, ?> getDefaultValues() {
			// TODO Auto-generated method stub
			return null;
		}

	}

}
