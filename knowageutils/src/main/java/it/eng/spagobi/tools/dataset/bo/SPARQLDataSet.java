package it.eng.spagobi.tools.dataset.bo;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.SPARQLDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.SPARQLDataReader;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.utils.ParametersResolver;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.ConfigurationException;

public class SPARQLDataSet extends ConfigurableDataSet {

	private static final Logger logger = Logger.getLogger(SPARQLDataSet.class);

	public static final String DATASET_TYPE = "SbiSPARQLDataSet";

	private static final String SPARQL_QUERY = "sparqlQuery";

	private final ParametersResolver parametersResolver = new ParametersResolver();

	public SPARQLDataSet() {

	};

	public SPARQLDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);

		initConf(false);
	}

	protected void initConf(boolean resolveParams) {
		// configuration already set
		JSONObject jsonConf = getJSONConfig();
		Assert.assertNotNull(jsonConf, "configuration is null");

		initConf(jsonConf, resolveParams);
	}

	public SPARQLDataSet(JSONObject jsonConf) {
		Helper.checkNotNull(jsonConf, "jsonConf");

		setConfiguration(jsonConf.toString());
		initConf(jsonConf, false);
	}

	public void initConf(JSONObject jsonConf, boolean resolveParams) {
		initDataProxy(jsonConf, resolveParams);
		initDataReader(jsonConf, resolveParams);
	}

	protected String getProp(String propName, JSONObject conf, boolean optional, boolean resolveParams) {
		if (!optional) {
			checkPropExists(propName, conf);
		} else {
			if (!conf.has(propName)) {
				return null;
			}
		}
		try {
			Object res = conf.get(propName);
			if (!(res instanceof String)) {
				throw new ConfigurationException(String.format("%s is not a string in configuration: %s", propName, conf.toString()));
			}
			Assert.assertNotNull(res, "property is null");
			String r = (String) res;
			r = r.trim();
			if (r.isEmpty()) {
				if (optional) {
					return null;
				}
				throw new ConfigurationException(String.format("%s is empty in configuration: %s", propName, conf.toString()));
			}
			// resolve parameters and profile attributes
			if (resolveParams) {
				r = parametersResolver.resolveAll(r, this);
			}
			return r;
		} catch (Exception e) {
			throw new ConfigurationException(String.format("Error during configuration: %s", conf.toString()), e);
		}
	}

	private static void checkPropExists(String propName, JSONObject conf) {
		if (!conf.has(propName)) {
			throw new ConfigurationException(String.format("%s is not present in configuration: %s", propName, conf.toString()));
		}
	}

	private void initDataReader(JSONObject jsonConf, boolean resolveParams) {

		setDataReader(new SPARQLDataReader());

	}

	private void initDataProxy(JSONObject jsonConf, boolean resolveParams) {
		String sparqlEndpoint = getProp(DataSetConstants.SPARQL_ENDPOINT, jsonConf, false, resolveParams);
		String sparqlQuery = getProp(DataSetConstants.SPARQL_QUERY, jsonConf, false, resolveParams);
		String defaultGraphIRI = getProp(DataSetConstants.SPARQL_DEFAULT_GRAPH_IRI, jsonConf, true, resolveParams);
		int executionTimeout = Integer.parseInt(getProp(DataSetConstants.SPARQL_EXECUTION_TIMEOUT, jsonConf, true, resolveParams));

		setDataProxy(new SPARQLDataProxy(sparqlEndpoint, sparqlQuery, defaultGraphIRI, executionTimeout));
	}

	private JSONObject getJSONConfig() {
		JSONObject jsonConf = ObjectUtils.toJSONObject(getConfiguration());
		return jsonConf;
	}

	@Override
	public IDataSource getDataSource() {
		return null;
	}

	@Override
	public void setDataSource(IDataSource dataSource) {
		throw new IllegalStateException(RESTDataSet.class.getSimpleName() + " doesn't need the dataSource");
	}
}
