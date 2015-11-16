package it.eng.spagobi.analiticalmodel.execution.service;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class ExecuteAdHocUtility {

	// logger component
	private static Logger logger = Logger.getLogger(ExecuteAdHocUtility.class);
	
	public static Engine getWorksheetEngine() {
		return getEngineByDriver("it.eng.spagobi.engines.drivers.worksheet.WorksheetDriver");
	}
	
	public static Engine getQbeEngine() {
		return getEngineByDriver("it.eng.spagobi.engines.drivers.qbe.QbeDriver");
	}
	
	public static Engine getGeoreportEngine() {
		return getEngineByDriver("it.eng.spagobi.engines.drivers.gis.GisDriver");
		
	}
	
	public static Engine getCockpitEngine() {
		return getEngineByDriver("it.eng.spagobi.engines.drivers.cockpit.CockpitDriver");
	}
	
	public static Engine getEngineByDriver(String driver) {
		Engine engine;
		
		engine = null;
		try {
			Assert.assertNotNull(DAOFactory.getEngineDAO(), "EngineDao cannot be null");
			engine = DAOFactory.getEngineDAO().loadEngineByDriver(driver);
			if (engine == null) {
				throw new SpagoBIRuntimeException("There are no engines with driver equal to [" + driver + "] available");
			}
		} catch(Throwable t) {
			throw new SpagoBIRuntimeException( "Impossible to load a valid engine whose drover is equal to [" + driver + "]", t);				
		} finally {
			logger.debug("OUT");
		}
		
		return engine;
	}
	
	public static Engine getEngineByDocumentType(String type) {
		Engine engine;
		List<Engine> engines;
		
		engine = null;
		try {
			Assert.assertNotNull(DAOFactory.getEngineDAO(), "EngineDao cannot be null");
			engines = DAOFactory.getEngineDAO().loadAllEnginesForBIObjectType(type);
			if (engines == null || engines.size() == 0) {
				throw new SpagoBIRuntimeException("There are no engines for documents of type [" + type + "] available");
			} else {
				engine = (Engine) engines.get(0);
				LogMF.warn(logger, "There are more than one engine for document of type [" + type + "]. We will use the one whose label is equal to [{0}]", engine.getLabel());
			}
		} catch(Throwable t) {
			throw new SpagoBIRuntimeException( "Impossible to load a valid engine for document of type [" + type + "]", t);				
		} finally {
			logger.debug("OUT");
		}
		
		return engine;
	}
	
	public static String createNewExecutionId() {
		String executionId;
		
		logger.debug("IN");
		
		executionId = null;
		try {
			UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
			UUID uuidObj = uuidGen.generateTimeBasedUUID();
			executionId = uuidObj.toString();
			executionId = executionId.replaceAll("-", "");
		} catch(Throwable t) {
			
		} finally {
			logger.debug("OUT");
		}
		
		return executionId;
	}
	
	//returns true if the dataset is geospazial
	public static boolean hasGeoHierarchy(String meta) 
			throws JsonMappingException, JsonParseException, JSONException, IOException{
		
		JSONObject metadataObject = JSONUtils.toJSONObject(meta);
		if (metadataObject == null) return false;
		
		JSONArray columnsMetadataArray = metadataObject.getJSONArray("columns");

		for (int j = 0; j < columnsMetadataArray.length(); j++) {
			JSONObject columnJsonObject = columnsMetadataArray
					.getJSONObject(j);
//				String columnName = columnJsonObject.getString("column");
			String propertyName = columnJsonObject.getString("pname");
			String propertyValue = columnJsonObject.getString("pvalue");

			if (propertyName.equalsIgnoreCase("hierarchy")) {
				if (propertyValue.equalsIgnoreCase("geo")){
					return true;
				}
			}	
		}
		return false;
	}

}
