/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.dataset;

import it.eng.qbe.datasource.DriverManager;
import it.eng.qbe.datasource.configuration.CompositeDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.DataSetDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.FileDataSourceConfiguration;
import it.eng.qbe.datasource.dataset.DataSetDataSource;
import it.eng.qbe.datasource.dataset.DataSetDriver;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.catalogue.QueryCatalogue;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.bo.ConfigurableDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStoreFilter;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.exceptions.DataSetNotLoadedYetException;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.dataset.utils.datamart.IQbeDataSetDatamartRetriever;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it), Alberto Ghedin (alberto.ghedin@eng.it)
 */
public class QbeDataSet extends ConfigurableDataSet {
	
	public static String DS_TYPE = "SbiQbeDataSet";
	
	private static transient Logger logger = Logger.getLogger(QbeDataSet.class);
	
	public static final String QBE_DATA_SOURCE = "qbeDataSource";
	public static final String QBE_DATAMARTS = "qbeDatamarts";
	public static final String QBE_JSON_QUERY = "qbeJSONQuery";
	public static final String QBE_SQL_QUERY = "qbeSQLQuery";
	
	protected IDataSet ds = null;
	protected String jsonQuery = null;
	protected String datamarts = null;
	protected Map attributes = null;
	protected Map params = null;
	
	protected boolean useCache = false;
	
	protected IDataSource dataSource = null;
	
	private IDataSet sourceDataset = null;
	
	public QbeDataSet() {}
	
    public QbeDataSet(SpagoBiDataSet dataSetConfig) {

    	super(dataSetConfig);
    	try{
    		JSONObject jsonConf  = ObjectUtils.toJSONObject(dataSetConfig.getConfiguration());
    		this.setDatamarts((jsonConf.get(QBE_DATAMARTS) != null)?jsonConf.get(QBE_DATAMARTS).toString():"");
        	this.setJsonQuery((jsonConf.get(QBE_JSON_QUERY)!=null)?jsonConf.get(QBE_JSON_QUERY).toString():"");
		}catch (Exception e){
			logger.error("Error while defining dataset configuration.  Error: " + e.getMessage());
		}
    	//this.setDatamarts(dataSetConfig.getDatamarts());
    	//this.setJsonQuery(dataSetConfig.getJsonQuery());
    	
		IDataSource dataSource = DataSourceFactory.getDataSource( dataSetConfig.getDataSource() ) ;
		this.setDataSource(dataSource);
		
//		if (dataSetConfig.getDataSourcePersist() != null) {
//			IDataSource dataSourcePersist = DataSourceFactory.getDataSource( dataSetConfig.getDataSourcePersist() ) ;
//			this.setDataSourcePersist(dataSourcePersist);
//		}

	}
    
    public QbeDataSet(AbstractQbeDataSet ds) {
    	this.ds = ds;
	}
    
    private void init() {
    	if (ds == null) {
    		it.eng.qbe.datasource.IDataSource qbeDataSource = getQbeDataSource();
    		QueryCatalogue catalogue = getCatalogue(jsonQuery, qbeDataSource);
    		Query query = catalogue.getFirstQuery();
    		
    		ds = QbeDatasetFactory.createDataSet(qbeDataSource.createStatement(query));
    		ds.setUserProfileAttributes(attributes);
    		ds.setParamsMap(params);
    		ds.setTransformerId(transformerId);
    		ds.setTransformerCd(transformerCd);
    		ds.setPivotColumnName(pivotColumnName);
    		ds.setPivotColumnValue(pivotColumnValue);
    		ds.setPivotRowName(pivotRowName);
    		ds.setNumRows(numRows);
    		ds.setDataStoreTransformer(dataSetTransformer);
    		ds.setPersisted(persisted);
    		ds.setPersistTableName(persistTableName);
    		ds.setDataSourceForReading(this.getDataSourceForReading());
    		ds.setDataSourceForWriting(this.getDataSourceForWriting());
    	}
    }
    
    public void loadData(int offset, int fetchSize, int maxResults) {
    	init();
    	ds.loadData(offset, fetchSize, maxResults);
    }
    
    public void loadData() {
    	init();
    	ds.loadData();
    }
    
    public void setUserProfileAttributes(Map attributes) {
    	this.attributes = attributes;
    }
    
    public void setParamsMap(Map params) {
    	if (this.params == null || this.params.isEmpty()) {
    		this.params = params;
    		return;
    	}
    	
    	// keeping previous datamart retriever, if input map doesn't contain any
		IQbeDataSetDatamartRetriever previousRetriever = this.params == null ? null
				: (IQbeDataSetDatamartRetriever) this.params
						.get(SpagoBIConstants.DATAMART_RETRIEVER);
		IQbeDataSetDatamartRetriever newRetriever = params == null ? null
				: (IQbeDataSetDatamartRetriever) params
				.get(SpagoBIConstants.DATAMART_RETRIEVER);
		
		this.params = params;
		if (this.params == null) {
			this.params = new HashMap();
		}
    	
    	if (previousRetriever != null && newRetriever == null) {
    		this.params.put(SpagoBIConstants.DATAMART_RETRIEVER, previousRetriever);
    	}
    }
    
    public Map getParamsMap() {
    	return this.params;
    }
    
    public IDataStore getDataStore() {
    	if (ds == null) {
    		throw new DataSetNotLoadedYetException();
    	}
    	return ds.getDataStore();
    }
    
    public String getJsonQuery() {
    	return this.jsonQuery;
    }
    
    public void setJsonQuery(String jsonQuery) {
    	this.jsonQuery = jsonQuery;
    }
    
    public String getDatamarts() {
    	return this.datamarts;
    }
    public void setDatamarts(String datamarts) {
    	this.datamarts = datamarts;
    }
    
	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public IDataSource getDataSource() {
		return this.dataSource;
	}
    
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet sbd;

		sbd = super.toSpagoBiDataSet();

		sbd.setType(DS_TYPE);		
		if(getDataSource() != null) {
			sbd.setDataSource(getDataSource().toSpagoBiDataSource());
		}
		
//		if (getDataSourcePersist() != null) {
//			sbd.setDataSourcePersist(getDataSourcePersist().toSpagoBiDataSource());
//		}
		
		/* next informations are already loaded in method super.toSpagoBiDataSet() through the table field configuration 
		try{
			JSONObject jsonConf  = new JSONObject();
			jsonConf.put(QBE_JSON_QUERY, getJsonQuery());
			jsonConf.put(QBE_DATAMARTS, getDatamarts());
			sbd.setConfiguration(jsonConf.toString());
		}catch (Exception e){
			logger.error("Error while defining dataset configuration.  Error: " + e.getMessage());
		}*/
		//sbd.setJsonQuery(getJsonQuery());
		//sbd.setDatamarts(getDatamarts());

		return sbd;
	}
	
	@Override
	public void setPersisted(boolean persisted) {
		super.setPersisted(persisted);
		if (ds != null) {
			ds.setPersisted(persisted);
		}
	}
	
	public it.eng.qbe.datasource.IDataSource getQbeDataSource() {

        Map<String, Object> dataSourceProperties = new HashMap<String, Object>();
       
        String modelName = getDatamarts();
        List<String> modelNames = new ArrayList<String>();
        modelNames.add( modelName );
        dataSourceProperties.put("datasource", dataSource);
        dataSourceProperties.put("dblinkMap", new HashMap());
        
		if (this.getSourceDataset() != null) {
			List<IDataSet> dataSets = new ArrayList<IDataSet>();
			dataSets.add(this.getSourceDataset());
			dataSourceProperties.put(EngineConstants.ENV_DATASETS, dataSets);
		}
        
		if (dataSourceProperties.get(EngineConstants.ENV_DATASETS) != null) {
			return getDataSourceFromDataSet(dataSourceProperties, useCache);
		} else {
			return getORMDataSource(modelNames, dataSourceProperties,
					useCache);
		}

	}
	
	public it.eng.qbe.datasource.IDataSource getDataSourceFromDataSet(Map<String, Object> dataSourceProperties, boolean useCache) {
		
		it.eng.qbe.datasource.IDataSource dataSource;
		List<IDataSet> dataSets = (List<IDataSet>)dataSourceProperties.get(EngineConstants.ENV_DATASETS);
		dataSourceProperties.remove(EngineConstants.ENV_DATASETS);
		
		CompositeDataSourceConfiguration compositeConfiguration = new CompositeDataSourceConfiguration(DataSetDataSource.EMPTY_MODEL_NAME);
		Iterator<String> it = dataSourceProperties.keySet().iterator();
		while(it.hasNext()) {
			String propertyName = it.next();
			compositeConfiguration.loadDataSourceProperties().put(propertyName, dataSourceProperties.get(propertyName));
		}

		for(int i = 0; i < dataSets.size(); i++) {
			DataSetDataSourceConfiguration c = new DataSetDataSourceConfiguration((dataSets.get(i)).getLabel(), dataSets.get(i));
			compositeConfiguration.addSubConfiguration(c);
		}

		dataSource = DriverManager.getDataSource(DataSetDriver.DRIVER_ID, compositeConfiguration, useCache);
		
		return dataSource;
	}
	
	private it.eng.qbe.datasource.IDataSource getORMDataSource(List<String> dataMartNames, Map<String, Object> dataSourceProperties, boolean useCache) {
		
	    File modelJarFile = null;
	    List<File> modelJarFiles = new ArrayList<File>();
	    CompositeDataSourceConfiguration compositeConfiguration = new CompositeDataSourceConfiguration();
	    compositeConfiguration.loadDataSourceProperties().putAll( dataSourceProperties);

//	    String resourcePath = getResourcePath();
//	    modelJarFile = new File(resourcePath+File.separator+"qbe" + File.separator + "datamarts" + File.separator + modelNames.get(0)+File.separator+"datamart.jar");
	    IQbeDataSetDatamartRetriever retriever = this.getDatamartRetriever();
	    if (retriever == null) {
	    	throw new SpagoBIRuntimeException("Missing datamart retriever, cannot proceed.");
	    }
	    modelJarFile = retriever.retrieveDatamartFile(dataMartNames.get(0));
	    modelJarFiles.add(modelJarFile);
	    compositeConfiguration.addSubConfiguration(new FileDataSourceConfiguration(dataMartNames.get(0), modelJarFile));
	
	    logger.debug("OUT: Finish to load the data source for the model names "+dataMartNames+"..");
	    return DriverManager.getDataSource(getDriverName(modelJarFile), compositeConfiguration, this.useCache);
	}
	
		
    private IQbeDataSetDatamartRetriever getDatamartRetriever() {
    	if (this.params == null || this.params.isEmpty()) {
    		return null;
    	}
    	IQbeDataSetDatamartRetriever retriever = (IQbeDataSetDatamartRetriever) this.params.get(SpagoBIConstants.DATAMART_RETRIEVER);
    	return retriever;
	}

	/**
     * Get the driver name (hibernate or jpa). It checks if the passed jar file contains the persistence.xml
     * in the META-INF folder
     * @param jarFile a jar file with the model definition
     * @return jpa if the persistence provder is JPA o hibernate otherwise
     */
    private static String getDriverName(File jarFile){
        logger.debug("IN: Check the driver name. Looking if "+jarFile+" is a jpa jar file..");
        JarInputStream zis;
        JarEntry zipEntry;
        String dialectName = null;
        boolean isJpa = false;
           
        try {
            FileInputStream fis = new FileInputStream(jarFile);
            zis = new JarInputStream(fis);
            while((zipEntry=zis.getNextJarEntry())!=null){
                logger.debug("Zip Entry is [" + zipEntry.getName() + "]");
                if(zipEntry.getName().equals("META-INF/persistence.xml") ){
                    isJpa = true;
                    break;
                }
                zis.closeEntry();
            }
            zis.close();
            if(isJpa){
                dialectName = "jpa";
            } else{
                dialectName = "hibernate";
            }
        } catch (Throwable t) {
            logger.error("Impossible to read jar file [" + jarFile + "]",t);
            throw new SpagoBIRuntimeException("Impossible to read jar file [" + jarFile + "]", t);
        }


        logger.debug("OUT: "+jarFile+" has the dialect: "+dialectName);
        return dialectName;
    }
    
    
	public QueryCatalogue getCatalogue(String json, it.eng.qbe.datasource.IDataSource dataSource) {
		QueryCatalogue catalogue;
		JSONObject catalogueJSON;
		JSONArray queriesJSON;
		JSONObject queryJSON;
		Query query;
		
		catalogue = new QueryCatalogue();
		try {
			catalogueJSON = new JSONObject(json).getJSONObject("catalogue");
			queriesJSON = catalogueJSON.getJSONArray("queries");
		
			for(int i = 0; i < queriesJSON.length(); i++) {
				queryJSON = queriesJSON.getJSONObject(i);
				query = it.eng.qbe.query.serializer.SerializerFactory.getDeserializer("application/json").deserializeQuery(queryJSON, dataSource);
				catalogue.addQuery(query);
			}
		} catch (Throwable e) {
			throw new SpagoBIEngineRuntimeException("Impossible to deserialize catalogue", e);
		}
		
		return catalogue;
	}
	
	@Override
	public IDataSetTableDescriptor persist(String tableName, IDataSource dataSource) {
		init();
		return((AbstractQbeDataSet)ds).persist(tableName, dataSource);
	}

	@Override
	public IDataStore getDomainValues(String fieldName, Integer start, Integer limit, IDataStoreFilter filter ) {
		init();
		return ((AbstractQbeDataSet)ds).getDomainValues(fieldName, start, limit, filter);
	}
	
	public String getSignature() {
		init();
		return ((AbstractQbeDataSet)ds).getSignature();
	}
	
	public boolean isUseCache() {
		return useCache;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}
	
	/**
	* TODO check this
	*/
	@Override
	public IMetaData getMetadata() {
		IMetaData metadata = null;
		try {
			DatasetMetadataParser dsp = new DatasetMetadataParser();
			metadata = dsp.xmlToMetadata( getDsMetadata() );
		} catch (Exception e) {
			logger.error("Error loading the metadata",e);
			throw new SpagoBIEngineRuntimeException("Error loading the metadata",e);
		}
		return metadata;
	}
	
	public void setMetadata(IMetaData metadata) {
		String metadataStr = null;
		DatasetMetadataParser dsp = new DatasetMetadataParser();
		metadataStr = dsp.metadataToXML( metadata );
		this.setDsMetadata( metadataStr );
	}
	
	public IDataSet getSourceDataset() {
		return sourceDataset;
	}

	public void setSourceDataset(IDataSet sourceDataset) {
		this.sourceDataset = sourceDataset;
	}
	
	@Override
	public String getDsType() {
		return DS_TYPE;
	}
	
}
