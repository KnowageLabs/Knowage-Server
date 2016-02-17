package it.eng.spagobi.engines.qbe.services.initializers;

import it.eng.qbe.dataset.FederationUtils;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.qbe.federation.FederationClient;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class QbeEngineFromFederationStartAction extends QbeEngineStartAction {

	// INPUT PARAMETERS

	// OUTPUT PARAMETERS
	public static final String LANGUAGE = "LANGUAGE";
	public static final String COUNTRY = "COUNTRY";

	// SESSION PARAMETRES
	public static final String ENGINE_INSTANCE = EngineConstants.ENGINE_INSTANCE;
	public static final String REGISTRY_CONFIGURATION = "REGISTRY_CONFIGURATION";

	// INPUT PARAMETERS

	// The passed dataset label
	public static final String DATASET_LABEL = "dataset_label";
	public static final String FEDERATED_DATASET = "FEDERATION_ID";
	// label of default datasource associated to Qbe Engine
	public static final String DATASOURCE_LABEL = "datasource_label";

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(QbeEngineFromFederationStartAction.class);

	public static final String ENGINE_NAME = "SpagoBIQbeEngine";

	private IDataSet dataSet;

	@Override
	public IDataSet getDataSet() {
		logger.debug("IN");
		if (dataSet == null) {
			// dataset information is coming with the request
			String datasetLabel = this.getAttributeAsString(DATASET_LABEL);
			logger.debug("Parameter [" + DATASET_LABEL + "]  is equal to [" + datasetLabel + "]");
			Assert.assertNotNull(datasetLabel, "Dataset not specified");
			dataSet = getDataSetServiceProxy().getDataSetByLabel(datasetLabel);
		}
		logger.debug("OUT");
		return dataSet;
	}

	@Override
	public IDataSource getDataSource() {
		logger.debug("IN");
		IDataSource datasource;
		if(super.getDataSource()==null){
			IDataSet dataset = this.getDataSet();
			datasource = dataset.getDataSource();

			if (datasource == null) {
				// if dataset has no datasource associated take it from request
				String dataSourceLabel = getSpagoBIRequestContainer().get(DATASOURCE_LABEL) != null ? getSpagoBIRequestContainer().get(DATASOURCE_LABEL).toString()
						: null;
				logger.debug("passed from server datasource " + dataSourceLabel);
				datasource = getDataSourceServiceProxy().getDataSourceByLabel(dataSourceLabel);
			}

		}else{
			datasource = super.getDataSource();
		}

		logger.debug("OUT : returning [" + datasource + "]");
		return datasource;
	}

	@Override
	public String getDocumentId() {
		// there is no document at the time
		return null;
	}

	// no template in this use case
	@Override
	public SourceBean getTemplateAsSourceBean() {
		SourceBean templateSB = null;
		return templateSB;
	}

	public IDataSet getDataSet(String datasetLabel) {
		logger.debug("IN");

		logger.debug("OUT");
		return getDataSetServiceProxy().getDataSetByLabel(datasetLabel);
	}

	@Override
	public Map addDatasetsToEnv() {
		String federatedDatasetId = this.getAttributeAsString(FEDERATED_DATASET);
		if(federatedDatasetId== null || federatedDatasetId.length()==0){
			logger.debug("Not Found a federated dataset on the request");
			return addSimpleDataSetToEnv();
		}
		
		//loading federation
		FederationDefinition dsf = loadFederationDefinition(federatedDatasetId);
		logger.debug("Found a federated dataset on the request");
		return addFederatedDatasetsToEnv(dsf, null);
	}

	public Map addSimpleDataSetToEnv() {

		logger.debug("getting the dataset");
		IDataSet dataset = this.getDataSet();

		logger.debug("Creating a federation to link to the dataset");
		FederationDefinition federationDefinition = new FederationDefinition();
		federationDefinition.setDescription(dataset.getDescription());
		federationDefinition.setName(dataset.getName());
		federationDefinition.setLabel(StringUtilities.left(FederationUtils.getDatasetFederationLabelSuffix()+((System.currentTimeMillis()%10000)),60));
		federationDefinition.setDegenerated(true);
		Set<IDataSet> sourceDatasets = new java.util.HashSet<IDataSet>();
		dataset.setOrganization(getUserProfile().getOrganization());
		sourceDatasets.add(dataset);
		
		federationDefinition.setSourceDatasets(sourceDatasets);

//		logger.debug("send request to server for the federation creation");
//		FederationClient fc = new FederationClient();
//		try {
//			federationDefinition = fc.addFederation(federationDefinition, getUserId());
//		} catch (Exception e) {
//			logger.error("Error saving the federated definition automatically generated in order to manage the creation datasets on the dataset "+dataset.getLabel(),e);
//			throw new SpagoBIRuntimeException("Error saving the federated definition automatically generated in order to manage the creation datasets on the dataset "+dataset.getLabel(),e);
//		}
//		logger.debug("Federation created");
		
		return addFederatedDatasetsToEnv(federationDefinition, dataset);
	}

	/**
	 * Loading the federation definition from the service
	 * @param federationId
	 */
	public FederationDefinition loadFederationDefinition(String federationId){
		logger.debug("Loading federation with id "+federationId);
		FederationClient fc = new FederationClient();
		try {
			return fc.getFederation(federationId,getUserId(), getDataSetServiceProxy());
		} catch (Exception e) {
			logger.error("Error loading the federation definition");
			throw new SpagoBIEngineRuntimeException("Error loading the federation definition", e);
		}
	}

	public Map addFederatedDatasetsToEnv(FederationDefinition dsf , IDataSet dataset) {

		Assert.assertNotNull(dsf, "The federation id has to be not null");

		IDataSource cachedDataSource = getCacheDataSource();

		// substitute default engine's datasource with dataset one
		this.setDataSource(cachedDataSource);
		String datasetLabel = this.getAttributeAsString(DATASET_LABEL);
		logger.debug("The label of the source dataset is "+datasetLabel);
		
		Map env = super.getEnv();
		

		// update parameters into the dataset
		logger.debug("The dataset is federated");
		logger.debug("Getting the configuration");
		String configurationJson = "";
		logger.debug("The configuration is " + configurationJson);

		//loading the source datasets
		logger.debug("Loading source datasets");
		List<IDataSet> dataSets = new ArrayList<IDataSet>();
		List<IDataSet> originalDataSets = new ArrayList<IDataSet>();
		List<String> dsLabels = new ArrayList<String>();

		if(dataset!=null){
			//in case of qbe on a single dataset
			dsLabels.add(dataset.getLabel());
			originalDataSets.add(dataset);
			
		}else{
			//in case of qbe on federation
			Iterator<IDataSet> sourceDatasets = dsf.getSourceDatasets().iterator();
			while (sourceDatasets.hasNext()) {
				IDataSet iDataSet = (IDataSet) sourceDatasets.next();
				dsLabels.add(iDataSet.getLabel());
				originalDataSets.add(iDataSet);
			
			}
		}

		// update profile attributes into dataset
		Map<String, Object> userAttributes = new HashMap<String, Object>();
		Map<String, String> mapNameTable = new HashMap<String, String>();
		UserProfile profile = (UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE);
		userAttributes.putAll(profile.getUserAttributes());
		userAttributes.put(SsoServiceInterface.USER_ID, profile.getUserId().toString());
		logger.debug("Setting user profile attributes into dataset...");
		logger.debug(userAttributes);

		//save in cache the derived datasets
		logger.debug("Saving the datasets on cache");

		JSONObject datasetPersistedLabels= null;
		try {
			datasetPersistedLabels = FederationUtils.createDatasetsOnCache(dsf.getDataSetRelationKeysMap(), getUserId());
		} catch (JSONException e1) {
			logger.error("Error loading the dataset. Please check that all the dataset linked to this federation are still working", e1);
			throw new SpagoBIEngineRuntimeException("Error loading the dataset. Please check that all the dataset linked to this federation are still working", e1);
		}
		for (int i = 0; i < dsLabels.size(); i++) {
			String dsLabel = dsLabels.get(i);
			//adds the link between dataset and cached table name
			Assert.assertNotNull(datasetPersistedLabels.optString(dsLabel), "Not found the label name of the cache table for the datase "+dsLabel);
			try {
				mapNameTable.put(dsLabel, datasetPersistedLabels.getString(dsLabel));
			} catch (Exception e) {
				logger.error("Error loading the dataset. Please check tha all the dataset linked to this federation are still working",e);
				throw new SpagoBIEngineRuntimeException("Error loading the dataset. Please check that all the dataset linked to this federation are still working");
			}

			IDataSet originalDataset = originalDataSets.get(i);
			
			
			IDataSet cachedDataSet = FederationUtils.createDatasetOnCache(mapNameTable.get(dsLabel), originalDataset,cachedDataSource);
			cachedDataSet.setUserProfileAttributes(userAttributes);
			cachedDataSet.setPersistTableName(mapNameTable.get(dsLabel));
			cachedDataSet.setParamsMap(env);
			cachedDataSet.setDsMetadata(originalDataset.getDsMetadata());
			cachedDataSet.setDataSourceForReading(cachedDataSource);
			dataSets.add(cachedDataSet);
		}

		
		logger.debug("Adding relationships on envinronment");
		JSONObject relations = dsf.getRelationshipsAsJSONObject();

		env.put(EngineConstants.ENV_RELATIONS, relations);
		env.put(EngineConstants.ENV_DATASET_CACHE_MAP,mapNameTable);
		env.put(EngineConstants.ENV_RELATIONS,relations);
		env.put(EngineConstants.ENV_DATASETS, dataSets);
		env.put(EngineConstants.ENV_DATASET_LABEL, datasetLabel);
		env.put(EngineConstants.ENV_DATASET_LABEL, datasetLabel);
		env.put(EngineConstants.ENV_FEDERATION,dsf);
		env.put(EngineConstants.ENV_DATASOURCE, cachedDataSource);

		logger.debug(env);
		env.put(EngineConstants.ENV_LOCALE, getLocale());


		return env;
	}



	/**
	 * Gets the datasource of the cache
	 * 
	 * @return
	 */
	private IDataSource getCacheDataSource() {
		logger.debug("Loading the cache datasource");
		String datasourceLabel = (String)getSpagoBIRequestContainer().get(EngineConstants.ENV_DATASOURCE_FOR_CACHE);
		logger.debug("The datasource for cahce is "+datasourceLabel);
		IDataSource dataSource = getDataSourceServiceProxy().getDataSourceByLabel(datasourceLabel);
		logger.debug("cache datasource loaded");
		return dataSource;
	}

	/**
	 * This method solves the following issue: SQLDataSet defines the SQL statement directly considering the names' of the wrapped dataset fields, but, in case
	 * of QbeDataSet, the fields' names are "it.eng.spagobi......Entity.fieldName" and not the name of the persistence table!!! We modify the dataset's metadata
	 * in order to fix this.
	 *
	 * @param dataset
	 *            The persisted Qbe dataset
	 * @param descriptor
	 *            The persistence table descriptor
	 */
	// TODO move this logic inside the SQLDataSet: when building the
	// SQL statement, the SQLDataSet should get the columns' names
	// from the IDataSetTableDescriptor. Replace
	// IDataSet.getPersistTableName with
	// IDataSet.getPersistTableDescriptor in order to permit the
	// IDataSetTableDescriptor to go with its dataset.
	// TODO merge with it.eng.spagobi.engines.worksheet.services.initializers.WorksheetEngineStartAction.adjustMetadataForQbeDataset
//	private void adjustMetadataForQbeDataset(IDataSet dataset, IDataSetTableDescriptor descriptor) {
//		IMetaData metadata = dataset.getMetadata();
//		int columns = metadata.getFieldCount();
//		for (int i = 0; i < columns; i++) {
//			IFieldMetaData fieldMetadata = metadata.getFieldMeta(i);
//			String newName = descriptor.getColumnName(fieldMetadata.getName());
//			fieldMetadata.setName(newName);
//			fieldMetadata.setProperty("uniqueName", newName);
//		}
//		dataset.setMetadata(metadata);
//	}

	@Override
	protected boolean tolerateMissingDatasource() {
		return true;
	}

}
