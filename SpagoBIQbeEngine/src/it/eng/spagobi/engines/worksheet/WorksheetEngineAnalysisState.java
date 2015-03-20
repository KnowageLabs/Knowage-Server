/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.catalogue.QueryCatalogue;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.commons.QbeEngineStaticVariables;
import it.eng.spagobi.engines.qbe.QbeEngineAnalysisState;
import it.eng.spagobi.engines.qbe.analysisstateloaders.IQbeEngineAnalysisStateLoader;
import it.eng.spagobi.engines.qbe.analysisstateloaders.QbeEngineAnalysisStateLoaderFactory;
import it.eng.spagobi.engines.worksheet.bo.WorkSheetDefinition;
import it.eng.spagobi.utilities.engines.EngineAnalysisState;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class WorksheetEngineAnalysisState extends EngineAnalysisState {
	

	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(QbeEngineAnalysisState.class);
	
	
	
	public WorksheetEngineAnalysisState() {
		super( );
	}

	public void load(byte[] rowData) throws SpagoBIEngineException {
		String str = null;
		JSONObject analysisStateJSON = null;
		JSONObject rowDataJSON = null;
		String encodingFormatVersion;
		
		logger.debug("IN");

		try {
			str = new String( rowData );
			logger.debug("loading analysis state from row data [" + str + "] ...");
			
			rowDataJSON = new JSONObject(str);
			try {
				encodingFormatVersion = rowDataJSON.getString("version");
			} catch (JSONException e) {
				encodingFormatVersion = "0";
			}
			
			logger.debug("Row data encoding version  [" + encodingFormatVersion + "]");

			
			if(encodingFormatVersion.equalsIgnoreCase(WorksheetEngineStaticVariables.CURRENT_VERSION)) {				
				analysisStateJSON = rowDataJSON;
			} else {
				logger.warn("Row data encoding version [" + encodingFormatVersion + "] does not match with the current version used by the engine [" + QbeEngineStaticVariables.CURRENT_QUERY_VERSION + "] ");
				logger.debug("Converting from encoding version [" + encodingFormatVersion + "] to encoding version [" + QbeEngineStaticVariables.CURRENT_QUERY_VERSION + "]....");
				IQbeEngineAnalysisStateLoader analysisStateLoader;
				analysisStateLoader = QbeEngineAnalysisStateLoaderFactory.getInstance().getLoader(encodingFormatVersion);
				if(analysisStateLoader == null) {
					throw new SpagoBIEngineException("Unable to load data stored in format [" + encodingFormatVersion + "] ");
				}
				analysisStateJSON = (JSONObject)analysisStateLoader.load(str);
				logger.debug("Encoding conversion has been executed succesfully");
			}
			
			JSONObject catalogueJSON = analysisStateJSON.getJSONObject("catalogue");
			JSONObject workSheetDefinitionJSON = analysisStateJSON.optJSONObject(QbeEngineStaticVariables.WORKSHEET_DEFINITION_LOWER);
			setProperty( QbeEngineStaticVariables.CATALOGUE,  catalogueJSON);
			if(workSheetDefinitionJSON!=null){
				setProperty( QbeEngineStaticVariables.WORKSHEET_DEFINITION,  workSheetDefinitionJSON);
			}
			logger.debug("analysis state loaded succsfully from row data");
		} catch (JSONException e) {
			throw new SpagoBIEngineException("Impossible to load analysis state from raw data", e);
		} finally {
			logger.debug("OUT");
		}
	}

	public byte[] store() throws SpagoBIEngineException {
		JSONObject catalogueJSON = null;
		JSONObject workSheetDefinitionJSON = null;
		JSONObject rowDataJSON = null;
		String rowData = null;	
		
		catalogueJSON = (JSONObject)getProperty( QbeEngineStaticVariables.CATALOGUE );
		workSheetDefinitionJSON = (JSONObject)getProperty( QbeEngineStaticVariables.WORKSHEET_DEFINITION );
		
		try {
			rowDataJSON = new JSONObject();
			rowDataJSON.put("version", QbeEngineStaticVariables.CURRENT_QUERY_VERSION);
			rowDataJSON.put("catalogue", catalogueJSON);
			rowDataJSON.put("crosstabdefinition", workSheetDefinitionJSON);
			
			rowData = rowDataJSON.toString();
		} catch (Throwable e) {
			throw new SpagoBIEngineException("Impossible to store analysis state from catalogue object", e);
		}
		
		return rowData.getBytes();
	}

	public QueryCatalogue getCatalogue() {
		QueryCatalogue catalogue;
		JSONObject catalogueJSON;
		JSONArray queriesJSON;
		JSONObject queryJSON;
		Query query;
		
		catalogue = new QueryCatalogue();
		catalogueJSON = (JSONObject)getProperty( QbeEngineStaticVariables.CATALOGUE );
		try {
			queriesJSON = catalogueJSON.getJSONArray("queries");
		
			for(int i = 0; i < queriesJSON.length(); i++) {
				queryJSON = queriesJSON.getJSONObject(i);
				query = SerializerFactory.getDeserializer("application/json").deserializeQuery(queryJSON, getDataSource());
								
				catalogue.addQuery(query);
			}
		} catch (Throwable e) {
			throw new SpagoBIEngineRuntimeException("Impossible to deserialize catalogue", e);
		}
		
		return catalogue;
	}

	public void setCatalogue(QueryCatalogue catalogue) {
		Set queries;
		Query query;
		JSONObject queryJSON;
		JSONArray queriesJSON;
		JSONObject catalogueJSON;
		
		catalogueJSON = new JSONObject();
		queriesJSON = new JSONArray();
		
		try {
			queries = catalogue.getAllQueries(false);
			Iterator it = queries.iterator();
			while(it.hasNext()) {
				query = (Query)it.next();
				queryJSON =  (JSONObject)SerializerFactory.getSerializer("application/json").serialize(query, getDataSource(), null);
				queriesJSON.put( queryJSON );
			}
			
			catalogueJSON.put("queries", queriesJSON);
		} catch (Throwable e) {
			throw new SpagoBIEngineRuntimeException("Impossible to serialize catalogue", e);
		}
		
		setProperty( QbeEngineStaticVariables.CATALOGUE, catalogueJSON );
	}

	public IDataSource getDataSource() {
		return (IDataSource)getProperty( QbeEngineStaticVariables.DATASOURCE );
	}

	public void setDataSource(IDataSource dataSource) {
		setProperty( QbeEngineStaticVariables.DATASOURCE, dataSource );
	}

	public void setWorkSheetDefinition(WorkSheetDefinition workSheetDefinition) {
		JSONObject workSheetDefinitionJSON = null;
		try {
			workSheetDefinitionJSON = (JSONObject)SerializationManager.serialize(workSheetDefinition, "application/json");
		} catch (Throwable e) {
			throw new SpagoBIEngineRuntimeException("Impossible to serialize workSheetDefinition definition", e);
		}
		setProperty( QbeEngineStaticVariables.WORKSHEET_DEFINITION, workSheetDefinitionJSON );
	}
	

	public WorkSheetDefinition getWorkSheetDefinition() {
		WorkSheetDefinition workSheetDefinition;
		JSONObject workSheetDefinitionJSON;
		
		workSheetDefinitionJSON = (JSONObject)getProperty( QbeEngineStaticVariables.WORKSHEET_DEFINITION );
		
		if(workSheetDefinitionJSON==null){
			return null;
		}
		
		try {
			workSheetDefinition = (WorkSheetDefinition)SerializationManager.deserialize(workSheetDefinitionJSON, "application/json", WorkSheetDefinition.class);
		} catch (Throwable e) {
			throw new SpagoBIEngineRuntimeException("Impossible to deserialize workSheetDefinition definition", e);
		}
		
		return workSheetDefinition;
		
	}
	
}
