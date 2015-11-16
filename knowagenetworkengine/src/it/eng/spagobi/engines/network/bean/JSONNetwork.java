/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.network.bean;



import it.eng.spagobi.engines.network.NetworkEngineRuntimeException;
import it.eng.spagobi.engines.network.serializer.SerializationException;
import it.eng.spagobi.engines.network.serializer.json.EdgeJSONSerializer;
import it.eng.spagobi.engines.network.serializer.json.NodeJSONSerializer;
import it.eng.spagobi.engines.network.template.NetworkTemplate;
import it.eng.spagobi.engines.network.template.NetworkXMLTemplateParser;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class JSONNetwork implements INetwork{

	private Set<Node> nodes; //list of nodes
	private Set<Edge> edges; //list of edges
	private JSONObject networkOptions;
	private static final String TYPE="json";
	//properties linked to a data set column
	private Map<String, String> targetNodeColumnProperties;//column-->property
	private Map<String, String> sourceNodeColumnProperties;//column-->property
	private Map<String, String> edgeColumnProperties;//column-->property
	//properties with the value directly set in the template
	private Map<String, String> targetNodeValueProperties;//property-->value
	private Map<String, String> sourceNodeValueProperties;//property-->value
	private Map<String, String> edgeValueProperties;//property-->value
	private Map<String, String> propertyType;//property-->type
	
	private Map<String,Set<JSONNetworkMappingMetadata>> dataSchema;//structure of the data for the nodes and edges. For example if the node has the property id,label,color the nodeMetadata are {label:string,color:string}. So all the property without the id
	private CrossNavigationLink networkCrossNavigation;//Cross navigation link structure
	
	private JSONObject info;//info of the networ. For example legend or other texts

	public static transient Logger logger = Logger.getLogger(JSONNetwork.class);
	
	public JSONNetwork() {
		super();
		nodes = new HashSet<Node>();
		edges = new HashSet<Edge>();
		targetNodeColumnProperties = new HashMap<String, String>();
		sourceNodeColumnProperties = new HashMap<String, String>();
		edgeColumnProperties = new HashMap<String, String>();
		targetNodeValueProperties = new HashMap<String, String>();
		sourceNodeValueProperties = new HashMap<String, String>();
		edgeValueProperties = new HashMap<String, String>();
		propertyType = new HashMap<String, String>();
		dataSchema = new HashMap<String,Set<JSONNetworkMappingMetadata>>();
		
	}
	
	
	public JSONNetwork(NetworkTemplate template) {
		this();
		this.networkCrossNavigation = template.getCrossNavigationLink();
		try {
			this.networkOptions = template.getNetworkJSON().optJSONObject(NetworkTemplate.OPTIONS);
			if(networkOptions==null){
				networkOptions = new JSONObject();
			}
			info = template.getInfo();
			if(info==null){
				info = new JSONObject();
			}
			parseDataSetMapping(template.getNetworkJSON().getJSONArray(NetworkTemplate.DATA_SET_MAPPING));
		} catch (Exception e) {
			logger.error("Error loading building the Network object from the json object", e);
			throw new NetworkEngineRuntimeException("Error loading building the Network object from the json object", e);
		}
	}
	
	/**
	 * Build the maps that map the properties and the columns of the dataset
	 * @param dataSetMapping
	 * @throws JSONException
	 */
	private void parseDataSetMapping(JSONArray dataSetMapping) throws JSONException{
		JSONObject mapping;
		String element, column, property,value, type;
		for (int i = 0; i < dataSetMapping.length(); i++) {
			mapping = dataSetMapping.getJSONObject(i);
			element = mapping.getString(NetworkTemplate.DATA_SET_MAPPING_ELEMENT);
			property = mapping.getString(NetworkTemplate.DATA_SET_MAPPING_PROPERTY);
			column = mapping.optString(NetworkTemplate.DATA_SET_MAPPING_COLUMN);
			type = mapping.optString(NetworkTemplate.DATA_SET_MAPPING_TYPE);
			if(type==null){
				type="string";
			}
			if(column==null || column.equals("")){//value of the property set directly in the template
				value = mapping.getString(NetworkTemplate.DATA_SET_MAPPING_VALUE);
				if(element.equalsIgnoreCase(NetworkTemplate.DATA_SET_MAPPING_SOURCE)){
					sourceNodeValueProperties.put(property,value);
				}else if(element.equalsIgnoreCase(NetworkTemplate.DATA_SET_MAPPING_TARGHET)){
					targetNodeValueProperties.put(property,value);
				}else if(element.equalsIgnoreCase(NetworkTemplate.DATA_SET_MAPPING_EDGE)){
					edgeValueProperties.put(property,value);
				}
			}else{//value of the property taken from the dataset
				if(element.equalsIgnoreCase(NetworkTemplate.DATA_SET_MAPPING_SOURCE)){
					sourceNodeColumnProperties.put(column,property);
				}else if(element.equalsIgnoreCase(NetworkTemplate.DATA_SET_MAPPING_TARGHET)){
					targetNodeColumnProperties.put(column,property);
				}else if(element.equalsIgnoreCase(NetworkTemplate.DATA_SET_MAPPING_EDGE)){
					edgeColumnProperties.put(column,property);
					
				}	
			}
			propertyType.put(property,type);

		}
		//remove the property id
		sourceNodeColumnProperties.remove("id");
		targetNodeColumnProperties.remove("id");
		edgeColumnProperties.remove("id");
		buildNetworkMetadata();
	} 


	
	
	/**
	 * Builds the schema for the nodes. 
	 * @return Set({name: "label", type: "string"},...)
	 */
	private void buildNetworkMetadata(){
		dataSchema = new HashMap<String, Set<JSONNetworkMappingMetadata>>();
		dataSchema.put(NetworkXMLTemplateParser.EDGES, buildEdgesMetadata());
		dataSchema.put(NetworkXMLTemplateParser.NODES, buildNodesMetadata());
	}
	

	/**
	 * Builds the schema for the nodes. 
	 * @return Set({name: "label", type: "string"},...)
	 */
	private Set<JSONNetworkMappingMetadata> buildNodesMetadata(){
		Set<JSONNetworkMappingMetadata> metadata = new HashSet<JSONNetworkMappingMetadata>();
		metadata.addAll(buildMapMetadata(sourceNodeColumnProperties.values()));
		metadata.addAll(buildMapMetadata(sourceNodeValueProperties.keySet()));
		metadata.addAll(buildMapMetadata(targetNodeColumnProperties.values()));
		metadata.addAll(buildMapMetadata(targetNodeValueProperties.keySet()));
		return metadata;
	}
	
	/**
	 * Builds the schema for the edges. 
	 * @return Set({name: "label", type: "string"},...)
	 */
	private Set<JSONNetworkMappingMetadata> buildEdgesMetadata(){
		Set<JSONNetworkMappingMetadata> metadata = new HashSet<JSONNetworkMappingMetadata>();
		metadata.addAll(buildMapMetadata(edgeColumnProperties.values()));
		metadata.addAll(buildMapMetadata(edgeValueProperties.keySet()));
		return metadata;
	}
	
	private Set<JSONNetworkMappingMetadata> buildMapMetadata(Collection<String> mapValues){
		String property;
		String type;
		Set<JSONNetworkMappingMetadata> metadata = new HashSet<JSONNetworkMappingMetadata>();
		Iterator<String> propertiesIterator = mapValues.iterator();
		while(propertiesIterator.hasNext()){
			property = propertiesIterator.next();
			type = propertyType.get(property);
			metadata.add(new JSONNetworkMappingMetadata(property,type));
		}
		return metadata;
	}
	
	//SERIALIZABLE PROPERTIES
	
	public Set<Node> getNodes() {
		return nodes;
	}
	
	public Set<Edge> getEdges() {
		return edges;
	}

	public void addNode(Node n){
		this.nodes.add(n);
	}
	public void addEdge(Edge e){
		this.edges.add(e);
	}

	
	public Map<String,Set<JSONNetworkMappingMetadata>> getDataSchema() {
		return dataSchema;
	}
	
	//NOT SERIALIZABLE PROPERTIES
	


	@JsonIgnore
	public String getNetworkType() {
		return TYPE;
	}

	@JsonIgnore
	public String getNetworkOptions(){
		return networkOptions.toString();
	}

	@JsonIgnore
	public String getMappingForNodeSource(String column){
		return sourceNodeColumnProperties.get(column);
	}
	@JsonIgnore
	public String getMappingForNodeTarget(String column){
		return targetNodeColumnProperties.get(column);
	}
	@JsonIgnore
	public String getMappingForEdge(String column){
		return edgeColumnProperties.get(column);
	}
	@JsonIgnore
	public String getNetworkInfo(){
		return info.toString();
	}	
	public void addTargetNodeValueProperties(Node node){
		String property;
		Iterator<String> propertiesIterator = targetNodeValueProperties.keySet().iterator();
		while(propertiesIterator.hasNext()){
			property = propertiesIterator.next();
			node.setProperty(property,targetNodeValueProperties.get(property));
		}
	}
	
	public void addSourceNodeValueProperties(Node node){
		String property;
		Iterator<String> propertiesIterator = sourceNodeValueProperties.keySet().iterator();
		while(propertiesIterator.hasNext()){
			property = propertiesIterator.next();
			node.setProperty(property,sourceNodeValueProperties.get(property));
		}
	}
	
	public void addEdgeValueProperties(Edge edge){
		String property;
		Iterator<String> propertiesIterator = edgeValueProperties.keySet().iterator();
		while(propertiesIterator.hasNext()){
			property = propertiesIterator.next();
			edge.setProperty(property,edgeValueProperties.get(property));
		}
	}
	
	public void setNetworkCrossNavigation(CrossNavigationLink networkCrossNavigation) {
		this.networkCrossNavigation = networkCrossNavigation;
	}


	/**
	 * JSON serializer for this object
	 * @return the network serialized
	 * @throws SerializationException
	 */
	@JsonIgnore
	public String getNetworkAsString() throws SerializationException{
		ObjectMapper mapper = new ObjectMapper();
		String s ="";
		try {
			SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1,0,0,null));
			simpleModule.addSerializer(Node.class, new NodeJSONSerializer());
			simpleModule.addSerializer(Edge.class, new EdgeJSONSerializer());
			mapper.registerModule(simpleModule);
			s = mapper.writeValueAsString((JSONNetwork)this);

			
		} catch (Exception e) {
			
			throw new SerializationException("Error serializing the network",e);
		}
		s = StringEscapeUtils.unescapeJavaScript(s);
		return  s; 
	}

	/**
	 * Serializer for the cross navigation structure
	 */
	@JsonIgnore
	public String getNetworkCrossNavigation() throws SerializationException{
		ObjectMapper mapper = new ObjectMapper();
		String s ="";
		try {
			
			s = mapper.writeValueAsString(networkCrossNavigation);

		} catch (Exception e) {
			
			throw new SerializationException("Error serializing the network",e);
		}
		return  s; 
	}
	
	

	
}
