package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.dataproxy.IDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.MongoDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.MongoDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class MongoDataSet extends ConfigurableDataSet {

	public static String DS_TYPE = "SbiQueryDataSet";
	private static final String QUERY = "query";
	private static final String QUERY_SCRIPT = "queryScript";
	private static final String QUERY_SCRIPT_LANGUAGE = "queryScriptLanguage";

	private static transient Logger logger = Logger.getLogger(AbstractJDBCDataset.class);

	/**
	 * Instantiates a new empty JDBC data set.
	 */
	public MongoDataSet() {
		super();
		setDataProxy(new MongoDataProxy());
		setDataReader(new MongoDataReader());
		addBehaviour(new QuerableBehaviour(this));
	}

	public MongoDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);

		setDataProxy(new MongoDataProxy());
		setDataReader(new MongoDataReader());

		try {
			setDataSource(DataSourceFactory.getDataSource(dataSetConfig.getDataSource()));
		} catch (Exception e) {
			throw new RuntimeException("Missing right exstension", e);
		}
		try {
			String config = JSONUtils.escapeJsonString(dataSetConfig.getConfiguration());
			JSONObject jsonConf = ObjectUtils.toJSONObject(config);
			setQuery((jsonConf.get(QUERY) != null) ? jsonConf.get(QUERY).toString() : "");
			setQueryScript((jsonConf.get(QUERY_SCRIPT) != null) ? jsonConf.get(QUERY_SCRIPT).toString() : "");
			setQueryScriptLanguage((jsonConf.get(QUERY_SCRIPT_LANGUAGE) != null) ? jsonConf.get(QUERY_SCRIPT_LANGUAGE).toString() : "");
		} catch (Exception e) {
			logger.error("Error while defining dataset configuration. Error:", e);
			throw new SpagoBIRuntimeException("Error while defining dataset configuration", e);
		}

		addBehaviour(new QuerableBehaviour(this));
	}

	@Override
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet toReturn;
		MongoDataProxy dataProxy;

		toReturn = super.toSpagoBiDataSet();

		toReturn.setType(DS_TYPE);

		dataProxy = this.getDataProxy();
		toReturn.setDataSource(dataProxy.getDataSource().toSpagoBiDataSource());

		try {
			JSONObject jsonConf = new JSONObject();
			jsonConf.put(QUERY, (getQuery() == null) ? "" : getQuery());
			jsonConf.put(QUERY_SCRIPT, (getQueryScript() == null) ? "" : getQueryScript());
			jsonConf.put(QUERY_SCRIPT_LANGUAGE, (getQueryScriptLanguage() == null) ? "" : getQueryScriptLanguage());
			toReturn.setConfiguration(jsonConf.toString());
		} catch (Exception e) {
			logger.error("Error while defining dataset configuration. Error:", e);
			throw new SpagoBIRuntimeException("Error while defining dataset configuration. Error:", e);
		}

		return toReturn;
	}

	@Override
	public MongoDataProxy getDataProxy() {
		IDataProxy dataProxy;

		dataProxy = super.getDataProxy();

		if (dataProxy == null) {
			setDataProxy(new MongoDataProxy());
			dataProxy = getDataProxy();
		}

		if (!(dataProxy instanceof MongoDataProxy)) {
			throw new RuntimeException("DataProxy cannot be of type [" + dataProxy.getClass().getName() + "] in MongoDataSet");
		}

		return (MongoDataProxy) dataProxy;
	}

	@Override
	public void setDataSource(IDataSource dataSource) {
		getDataProxy().setDataSource(dataSource);
	}

	@Override
	public IDataSource getDataSource() {
		return getDataProxy().getDataSource();
	}

	@Override
	public IMetaData getMetadata() {
		IMetaData metadata = null;
		try {
			DatasetMetadataParser dsp = new DatasetMetadataParser();
			metadata = dsp.xmlToMetadata(getDsMetadata());
		} catch (Exception e) {
			logger.error("Error loading the metadata", e);
			throw new SpagoBIEngineRuntimeException("Error loading the metadata", e);
		}
		return metadata;
	}

	@Override
	public void setMetadata(IMetaData metadata) {
		// TODO Auto-generated method stub

	}

	@Override
	public IDataStore test() {
		loadData();
		return getDataStore();
	}

	@Override
	public IDataSetTableDescriptor persist(String tableName, IDataSource dataSource) {
		IDataSource datasetDataSource = getDataSource();
		if (datasetDataSource.getLabel().equals(dataSource.getLabel())) {
			logger.error("At this moment it's not possible to persist data into MongoDB");
			throw new SpagoBIRuntimeException("At this moment it's not possible to persist data into MongoDB");
		} else {
			logger.debug("Specified datasource is NOT the dataset's datasource");
			return super.persist(tableName, dataSource);
		}
	}

	@Override
	public String getSignature() {
		logger.debug("IN");
		String toReturn = null;

		// get the query with parameters and profile attributes substituted by
		// their values
		QuerableBehaviour querableBehaviour = (QuerableBehaviour) this.getBehaviour(QuerableBehaviour.class.getName());
		String statement = querableBehaviour.getStatement();

		// puts also the data-source signature
		String datasourceSignature = null;
		IDataSource dataSource = this.getDataSource();
		datasourceSignature = dataSource.getUrlConnection() + "_" + dataSource.getUser();

		toReturn = statement + "_" + datasourceSignature;
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

}