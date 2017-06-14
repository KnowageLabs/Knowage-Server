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
package it.eng.spagobi.engines.qbe.query;

import it.eng.spagobi.utilities.assertion.Assert;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto
 */
public class FormViewerTemplateBuilder {
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(FormViewerTemplateBuilder.class);
	
	/*
	private JSONArray nodes = null;
	private String baseTemplate = null;
	private String analysisState = null;
	private JSONArray datamartsName = null;
	
	public FormViewerTemplateBuilder(JSONArray nodes, String analysisState, JSONArray datamartsName) {
		logger.debug("IN");
		
		try {
			Assert.assertNotNull(analysisState, "Input query cannot be empty");
			Assert.assertNotNull(datamartsName, "Input datamarts' names list cannot be empty");
			
			this.nodes = nodes;
			this.analysisState = analysisState;
			this.datamartsName = datamartsName;
			
			try {
				StringBuffer buffer = new StringBuffer();
				InputStream is = getClass().getClassLoader().getResourceAsStream("template.json");
				if (is == null) {
					throw new ExportException("Could not find base template file: template.json");
				}
				BufferedReader reader = new BufferedReader( new InputStreamReader(is) );
				String line = null;
				while( (line = reader.readLine()) != null) {
					buffer.append(line + "\n");
				}
				baseTemplate = buffer.toString();
			} catch (ExportException e) {
				throw e;
			} catch (Exception e) {
				throw new ExportException("Cannot create template builder", e);
			}
		} finally {
			logger.debug("OUT");
		}
		
	}
	
	public String buildTemplate() {
		String toReturn = null;
		logger.debug("IN");
		try {
			
			toReturn = baseTemplate.replaceAll("\\$\\{datamartsName\\}", datamartsName.toString());
			toReturn = toReturn.replaceAll("\\$\\{query\\}", analysisState);
			
			JSONArray fields = new JSONArray();
			getFields(nodes, fields);
			toReturn = toReturn.replaceAll("\\$\\{fields\\}", fields.toString(3));
			
		} catch (Exception e) {
			throw new ExportException("Cannot create template", e);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}
	*/
	
	private void getFields(JSONArray nodes, JSONArray container) throws Exception {
		for (int i = 0; i < nodes.length(); i++) {
			JSONObject node = (JSONObject) nodes.get(i);
			
			JSONArray children = (JSONArray) node.opt("children");
			
			if (children != null && children.length() > 0) {
				// entity node
				getFields(children, container);
			} else {
				// field leaf
				container.put(node.get("id"));
			}
		}
	}	
	
	
	private JSONArray nodes = null;
	private JSONObject baseTemplate = null;
	private JSONObject queryJSON = null;
	private JSONArray datamartsName = null;
	
	public FormViewerTemplateBuilder(JSONArray nodes, JSONObject queryJSON, JSONArray datamartsName) {
		logger.debug("IN");
		
		try {
			Assert.assertNotNull(queryJSON, "Input query cannot be empty");
			Assert.assertNotNull(datamartsName, "Input datamarts' names list cannot be empty");
			
			this.nodes = nodes;
			this.queryJSON = queryJSON;
			this.datamartsName = datamartsName;
			
			try {
				StringBuffer buffer = new StringBuffer();
				InputStream is = getClass().getClassLoader().getResourceAsStream("template.json");
				if (is == null) {
					throw new ExportException("Could not find base template file: template.json");
				}
				BufferedReader reader = new BufferedReader( new InputStreamReader(is) );
				String line = null;
				while( (line = reader.readLine()) != null) {
					buffer.append(line + "\n");
				}
				baseTemplate = new JSONObject(buffer.toString());
			} catch (ExportException e) {
				throw e;
			} catch (JSONException e) {
				throw new ExportException("Error parsing base template file: template.json", e);
			} catch (Exception e) {
				throw new ExportException("Cannot create template builder", e);
			}
		} finally {
			logger.debug("OUT");
		}
		
	}
	
	public String buildTemplate() {
		String toReturn = null;
		logger.debug("IN");
		try {
			StringBuffer buffer = new StringBuffer();
			JSONObject qbeConf = new JSONObject();
			qbeConf.put("datamartsName", datamartsName);
			qbeConf.put("query", this.queryJSON);
			baseTemplate.put("qbeConf", qbeConf);
			
			JSONArray fields = new JSONArray();
			getFields(nodes, fields);
			baseTemplate.put("fields", fields);
			
			buffer.append("{");
			buffer.append("\n\n\"fields\":\n");
			buffer.append(((JSONArray) baseTemplate.get("fields")).toString(2));
			buffer.append("\n\n\"staticClosedFilters\":\n");
			buffer.append(((JSONArray) baseTemplate.get("staticClosedFilters")).toString(2));
			buffer.append("\n\n\"staticOpenFilters\":\n");
			buffer.append(((JSONArray) baseTemplate.get("staticOpenFilters")).toString(2));
			buffer.append("\n\n\"dynamicFilters\":\n");
			buffer.append(((JSONArray) baseTemplate.get("dynamicFilters")).toString(2));
			buffer.append("\n\n\"groupingVariables\":\n");
			buffer.append(((JSONArray) baseTemplate.get("groupingVariables")).toString(2));
			buffer.append("\n\n\"qbeConf\":\n");
			buffer.append(((JSONObject) baseTemplate.get("qbeConf")).toString(2));
			buffer.append("\n}");
			toReturn = buffer.toString();
			
		} catch (Exception e) {
			throw new ExportException("Cannot create template", e);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}
	
}
