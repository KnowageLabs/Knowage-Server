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
package it.eng.spagobi.tools.dataset.bo;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.IDataSetBehaviour;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStoreFilter;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.transformer.IDataStoreTransformer;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

public interface IDataSet extends Iterable<IRecord> {

	String ROWS = "ROWS";
	String ROW = "ROW";
	String NAME = "NAME";
	String TYPE = "TYPE";
	String MULTIVALUE = "MULTIVALUE";

	String getDsMetadata();

	void setDsMetadata(String dsMetadata);

	IMetaData getMetadata();

	void setMetadata(IMetaData metadata);

	// general properties ....
	int getId();

	void setId(int id);

	String getName();

	void setName(String name);

	String getDescription();

	void setDescription(String description);

	String getLabel();

	void setLabel(String label);

	Integer getCategoryId();

	void setCategoryId(Integer categoryId);

	String getCategoryCd();

	void setCategoryCd(String categoryCd);

	String getDsType();

	void setDsType(String dsType);

	String getConfiguration();

	void setConfiguration(String configuration);

	Map getProperties();

	void setProperties(Map map);

	String getOwner();

	void setOwner(String owner);

	String getUserIn();

	void setUserIn(String userIn);

	Date getDateIn();

	void setDateIn(Date dateIn);

	Integer getScopeId();

	void setScopeId(Integer scopeId);

	String getScopeCd();

	void setScopeCd(String scopeCd);

	// parametrization ....
	// --------------------------------------------------------------------------------------------------
	// INVESTIGATE: why this 2 similar methods ??? FIND OUT & REFACTOR !
	String getParameters();

	void setParameters(String parameters);

	Map getParamsMap();

	void setParamsMap(Map params);

	List<JSONObject> getDataSetParameters();

	void setParametersMap(Map<String, String> paramValues) throws JSONException;

	Map<String, Object> getDrivers();

	void setDrivers(Map<String, Object> drivers);
	// --------------------------------------------------------------------------------------------------

	// profilation ...
	Map getUserProfileAttributes();

	void setUserProfileAttributes(Map<String, Object> attributes);

	// execution ...
	// --------------------------------------------------------------------------------------------------
	void loadData();

	void loadData(int offset, int fetchSize, int maxResults);

	// --------------------------------------------------------------------------------------------------

	IDataStore getDataStore();

	// extension points ...
	boolean hasBehaviour(String behaviourId);

	Object getBehaviour(String behaviourId);

	void addBehaviour(IDataSetBehaviour behaviour);

	// =================================================================================================
	// TO BE DEPRECATED ( do not cross this line ;-) )
	// =================================================================================================
	Integer getTransformerId();

	void setTransformerId(Integer transformerId);

	String getTransformerCd();

	void setTransformerCd(String transfomerCd);

	void setAbortOnOverflow(boolean abortOnOverflow);

	void addBinding(String bindingName, Object bindingValue);

	boolean isPersisted();

	void setPersisted(boolean persisted);

	boolean isPersistedHDFS();

	void setPersistedHDFS(boolean persistedHDFS);

	boolean isScheduled();

	void setScheduled(boolean scheduled);

	boolean isFlatDataset();

	boolean isPreparedDataSet();

	String getFlatTableName();

	String getPreparedTableName();

	List getNoActiveVersions();

	void setNoActiveVersions(List noActiveVersions);

	String getPersistTableName();

	void setPersistTableName(String persistTableName);

	// TODO these methods do NOT belong to the dataset interface. remove them and refactor the code.
	// --------------------------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------------------------
	// TODO these methods must be moved into a proper factory that convert SpagoBI BO into data bean passed
	// to the DAO. For the conversion from data bean and SpagoBI BO such a factory alredy exist
	// NOTE: SpagoBiDataSet: change when possible the name SpagoBiDataSet following a convention common to
	// all data bean
	SpagoBiDataSet toSpagoBiDataSet();

	// --------------------------------------------------------------------------------------------------

	IDataStore test();

	IDataStore test(int offset, int fetchSize, int maxResults);

	String getSignature();

	IDataSetTableDescriptor persist(String tableName, IDataSource dataSource);

	/**
	 * Get the values for a certain dataset's field, considering a optional filter.
	 *
	 * @param fieldName The dataset's field
	 * @param start     The offset on results
	 * @param limit     The limit on result
	 * @param filter    The optional filter
	 * @return The datastore containing the values for the dataset's field
	 */
	IDataStore getDomainValues(String fieldName, Integer start, Integer limit, IDataStoreFilter filter);

	IDataStore decode(IDataStore datastore);

	boolean isCalculateResultNumberOnLoadEnabled();

	void setCalculateResultNumberOnLoad(boolean enabled);

	void setDataSource(IDataSource dataSource);

	IDataSource getDataSource();

	String getTableNameForReading();

	IDataSource getDataSourceForReading();

	void setDataSourceForReading(IDataSource dataSource);

	String getOrganization();

	void setOrganization(String organization);

	IDataSource getDataSourceForWriting();

	void setDataSourceForWriting(IDataSource dataSource);

	FederationDefinition getDatasetFederation();

	void setDatasetFederation(FederationDefinition datasetFederation);

	String getStartDateField();

	void setStartDateField(String startDateField);

	String getEndDateField();

	void setEndDateField(String endDateField);

	String getSchedulingCronLine();

	void setSchedulingCronLine(String schedulingCronLine);

	Map<String, ?> getDefaultValues();

	@Override
	DataIterator iterator();

	boolean isIterable();

	boolean isRealtime();

	boolean isCachingSupported();

	DatasetEvaluationStrategyType getEvaluationStrategy(boolean isNearRealtime);

	UserProfile getUserProfile();

	void setUserProfile(UserProfile profile);

	void resolveParameters();

	<T> T getImplementation(Class<T> clazz);

	Set getTags();

	void setTags(Set tags);

	// Data store transformer
	boolean hasDataStoreTransformers();

	void clearDataStoreTransformers();

	List<IDataStoreTransformer> getDataStoreTransformers();

	void addDataStoreTransformer(IDataStoreTransformer transformer);

	void addDataStoreTransformers(Collection<IDataStoreTransformer> transformers);

	void executeDataStoreTransformers(IDataStore dataStore);

	DataIterator iterator(IMetaData dsMetadata);
}