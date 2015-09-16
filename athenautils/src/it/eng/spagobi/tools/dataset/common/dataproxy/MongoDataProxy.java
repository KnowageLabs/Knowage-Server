package it.eng.spagobi.tools.dataset.common.dataproxy;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.MongoDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;

public class MongoDataProxy extends AbstractDataProxy {

	IDataSource dataSource;
	String statement;

	private static final String SINGLE_RESULT = "SINGLE_DOCUMENT_QUERY";
	private static final String LIST_RESULT = "LIST_DOCUMENTS_QUERY";

	private static transient Logger logger = Logger.getLogger(MongoDataProxy.class);
	/**
	 * Override the tojsonObject to serialize the MongoObject like ObjectID as String. For example {_id: ObjectID("313213")} will be serialized in {_id:
	 * "313213"}
	 */
	private StringBuffer overridenToJSONObject;

	/**
	 * Decorate the js adding a function that navigates the cursor resulting from the query
	 */
	private StringBuffer decorateFunction;

	public MongoDataProxy() {
		this.setCalculateResultNumberOnLoad(true);

		overridenToJSONObject = new StringBuffer();
		overridenToJSONObject = overridenToJSONObject.append("var tojsonObject2 = tojsonObject;");
		overridenToJSONObject = overridenToJSONObject.append("tojsonObject = function (x, indent, nolint){");
		overridenToJSONObject = overridenToJSONObject.append(" assert.eq((typeof x), \"object\", \"tojsonObject needs object, not [\" + (typeof x) + \"]\");");
		overridenToJSONObject = overridenToJSONObject.append("if (typeof(x.tojson) == \"function\" && x.tojson != tojson) {");
		overridenToJSONObject = overridenToJSONObject.append("return '\"'+x+'\"' ;");
		overridenToJSONObject = overridenToJSONObject.append("}else{");
		overridenToJSONObject = overridenToJSONObject.append("return tojsonObject2(x, indent, nolint);");
		overridenToJSONObject = overridenToJSONObject.append("}");
		overridenToJSONObject = overridenToJSONObject.append("};");

		decorateFunction = new StringBuffer();
		decorateFunction = decorateFunction.append("var serializeResult = function(cursor){");
		decorateFunction = decorateFunction.append("result='[';");
		decorateFunction = decorateFunction.append(" cursor.forEach(function(c){result=result+(tojson(c))+',';});");
		decorateFunction = decorateFunction.append("var length = result.length;");
		decorateFunction = decorateFunction.append("print(result[length-1]);");
		decorateFunction = decorateFunction.append("if(result[length-1]==','){");
		decorateFunction = decorateFunction.append("result= result.substring(0,length-1);");
		decorateFunction = decorateFunction.append("} ");
		decorateFunction = decorateFunction.append("result=result+']';");
		decorateFunction = decorateFunction.append(" return result;};");
		decorateFunction = decorateFunction.append("return serializeResult(query); }");
	}

	public MongoDataProxy(IDataSource dataSource, String statement) {
		this();
		setDataSource(dataSource);
		setStatement(statement);
	}

	public IDataStore load(String statement, IDataReader dataReader) throws EMFUserError {
		if (statement != null) {
			setStatement(statement);
		}
		return load(dataReader);
	}

	public IDataStore load(IDataReader dataReader) {
		logger.debug("IN");

		IDataStore dataStore = null;

		CommandResult result = loadData();

		try {
			// read data
			dataReader.setFetchSize(getFetchSize());
			dataReader.setOffset(getOffset());
			dataReader.setMaxResults(getMaxResults());
			((MongoDataReader) dataReader).setAggregatedQuery(isAggregatedQuery());
			dataStore = dataReader.read(result);
		} catch (Throwable t) {
			logger.error("An error occurred while parsing resultset", t);
			throw new SpagoBIRuntimeException("An error occurred while parsing resultset", t);
		}

		logger.debug("OUT");
		return dataStore;
	}

	private CommandResult loadData() {
		logger.debug("IN");
		MongoClient mongoClient;
		CommandResult result = null;

		String clientUrl = dataSource.getUrlConnection();

		logger.debug("Getting the connection URL and db name");

		int databaseNameStart = clientUrl.lastIndexOf("/");
		String databaseUrl = clientUrl.substring(0, databaseNameStart);
		String databaseName = clientUrl.substring(databaseNameStart + 1);

		logger.debug("Connection URL: " + databaseUrl);
		logger.debug("Database name: " + databaseName);

		if (dataSource.getUser() != null && dataSource.getPwd() != null && dataSource.getUser().length() > 0 && dataSource.getPwd().length() > 0) {
			MongoCredential credential = MongoCredential.createMongoCRCredential(dataSource.getUser(), databaseName, dataSource.getPwd().toCharArray());
			// mongoClient = new MongoClient(new ServerAddress(clientUrl),
			// Arrays.asList(credential));
			logger.error("No autentication available yet");
			throw new SpagoBIRuntimeException("No autentication available yet");
		}

		try {
			logger.debug("Connecting to mongodb");
			mongoClient = new MongoClient(databaseUrl);

		} catch (UnknownHostException e) {
			logger.error("Error connectiong to the MongoClient", e);
			throw new SpagoBIRuntimeException("Error connectiong to the MongoClient", e);
		}

		try {
			logger.debug("Connecting to the db " + databaseName);
			DB database = mongoClient.getDB(databaseName);

			logger.debug("Executing the statement" + statement);
			result = database.doEval(getDecoredStatement());

		} catch (Exception e) {
			logger.error("Exception executing the MongoDataset", e);
			throw new SpagoBIRuntimeException("Exception executing the MongoDataset", e);
		} finally {
			logger.debug("Closing connection");
			mongoClient.close();
		}

		logger.debug("OUT");
		return result;

	}

	/**
	 * Decorates the user defined statement.. The result of a non single document query (single document queries are aggregations and findOne) is a cursor so we
	 * need to navigate it to get all the documents.
	 *
	 * @return
	 */
	private String getDecoredStatement() {

		String decored = "";

		/**
		 * The result is a fixed value (for example return 55). In this case we envelop the result in a object. If the result is already an object return it
		 */

		if (isSingleValue()) {// the result is a fixed value
			decored = " function(){ " + overridenToJSONObject.toString() + " " + this.statement
					+ " if(typeof(sbiDatasetfixedResult)==\"object\"){return sbiDatasetfixedResult}else{return {\"result\": sbiDatasetfixedResult}};}";
		}

		/**
		 * The result is not a cursor (a document or a list of documents)
		 */
		else if (isSingleDocumentQuery() || isAggregatedQuery()) {
			decored = " function(){ " + overridenToJSONObject.toString() + " " + this.statement + " return query};";
		}

		/**
		 * The result is a cursor so navigate it using decorateFunction
		 */
		else {
			decored = " function(){" + overridenToJSONObject.toString() + " " + this.statement + decorateFunction.toString();
			// "var serializeResult = function(cursor){	result='['; cursor.forEach(function(c){result=result+(tojson(c))+',';});	var length = result.length;	print(result[length-1]);	if(result[length-1]==','){	result= result.substring(0,length-1);	} result=result+']'; return result;}; return serializeResult(query); }";
		}

		logger.debug("Mongo decorated query:");
		logger.debug(decored);
		return decored;
	}

	/**
	 * findOne and aggregation queries returns a single document. The normal find returns a cursor
	 *
	 * @return
	 */
	private boolean isSingleDocumentQuery() {
		if (this.statement != null && !this.statement.contains(LIST_RESULT)) {
			if (this.statement.contains(SINGLE_RESULT) || this.statement.contains("findOne")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * findOne and aggregation queries returns a single document. The normal find returns a cursor
	 *
	 * @return
	 */
	private boolean isSingleValue() {
		if (this.statement != null) {
			if (this.statement.contains("sbiDatasetfixedResult")) {
				return true;
			}
		}
		return false;
	}

	private boolean isAggregatedQuery() {
		if (this.statement != null) {
			if (this.statement.contains(SINGLE_RESULT) || this.statement.contains(LIST_RESULT) || isSingleValue()) {
				return false;
			}
			if (this.statement.contains("aggregate")) {
				return true;
			}
		}
		return false;
	}

	public IDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void setStatement(String statement) {
		if (!statement.equals(this.statement)) {
			this.statement = statement;
		}
	}

	@Override
	public boolean isOffsetSupported() {
		return true;
	}

	@Override
	public boolean isFetchSizeSupported() {
		return true;
	}
}
