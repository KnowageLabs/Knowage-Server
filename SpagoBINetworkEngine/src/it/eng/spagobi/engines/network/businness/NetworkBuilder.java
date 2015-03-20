/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.network.businness;

import it.eng.spagobi.engines.network.bean.Edge;
import it.eng.spagobi.engines.network.bean.INetwork;
import it.eng.spagobi.engines.network.bean.JSONNetwork;
import it.eng.spagobi.engines.network.bean.Node;
import it.eng.spagobi.engines.network.bean.XMLNetwork;
import it.eng.spagobi.engines.network.template.NetworkTemplate;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

import java.util.List;

import org.json.JSONException;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 * Static class that build the Network from the Template
 */
public class NetworkBuilder {
	
	/**
	 * Create the Network Object from the Template and the DataSet
	 * @param dataSet
	 * @param template
	 * @return
	 * @throws JSONException
	 */
	public static INetwork buildNetwork(IDataSet dataSet, NetworkTemplate template) throws JSONException{
		INetwork net;
		String XMLNet = template.getNetworkDefinition().getNetworkXML();
		if(XMLNet==null){
			net = buildNetworkFromDataset(dataSet, template);
		}else{
			net = new XMLNetwork(XMLNet, template.getCrossNavigationLink());
			
		}

		return net;
	}

	/**
	 * Creates the Node/Edge structure from the DataSet
	 * @param dataSet
	 * @param template
	 * @return
	 * @throws JSONException
	 */
	public static JSONNetwork buildNetworkFromDataset(IDataSet dataSet, NetworkTemplate template) throws JSONException{
		dataSet.loadData();
		IDataStore dataStore = dataSet.getDataStore();
		IMetaData metaData = dataStore.getMetaData();
		IRecord record=null;
		Node src;
		Node dest;
		Edge edge;
		List<IField> fields;
		String metadataAlias;
		JSONNetwork net = new JSONNetwork(template);
		
		for(int index = 0; index<dataStore.getRecordsCount() ; index++){
			record = dataStore.getRecordAt(index);
			fields = record.getFields();
			src = new Node();
			dest = new Node();
			edge = new Edge();
			
			for(int fieldIndex = 0; fieldIndex<fields.size() ; fieldIndex++){
				IFieldMetaData fieldMetaData = metaData.getFieldMeta(fieldIndex);
				metadataAlias = fieldMetaData.getName();
				setElement(metadataAlias, ((fields.get(fieldIndex)).getValue()).toString(), net, src, dest, edge);
			}
			if(src.getId()!=null){
				net.addSourceNodeValueProperties(src);
				net.addNode(src);
			}
			if(dest.getId()!=null){
				net.addTargetNodeValueProperties(dest);
				net.addNode(dest);
			}
			if(edge.getId()!=null && dest.getId()!=null && src.getId()!=null){
				net.addEdgeValueProperties(edge);
				edge.setSourceNode(src);
				edge.setTargetNode(dest);
				net.addEdge(edge);
			}
		}
		
		return net;
	}
	
	/**
	 * Search in the mapping (column->property) the name of the property for the passed column columnName.
	 * Set the  columnValue as value of the property in the right object src/dest/edge
	 * @param columnName
	 * @param columnValue
	 * @param net
	 * @param src
	 * @param dest
	 * @param edge
	 */
	private static void setElement(String columnName, String columnValue, JSONNetwork net, Node src, Node dest, Edge edge){
		//look in the mapping, taken from the template, the property name for the column columnName
		String edgeVal = net.getMappingForEdge(columnName);
		String srcVal = net.getMappingForNodeSource(columnName);
		String destVal = net.getMappingForNodeTarget(columnName);
		if(edgeVal!=null){//if the column is a property for the edges..
			if(edgeVal.equalsIgnoreCase("id")){
				edge.setId(columnValue);
			}else{
				edge.setProperty(edgeVal, columnValue);
			}
		}else if(srcVal!=null){//if the column is a property for the source node..
			if(srcVal.equalsIgnoreCase("id")){
				src.setId(columnValue);
			}else{
				src.setProperty(srcVal, columnValue);
			}
		}else if(destVal!=null){//if the column is a property for the target node..
			if(destVal.equalsIgnoreCase("id")){
				dest.setId(columnValue);
			}else{
				dest.setProperty(destVal, columnValue);
			}
		}
	}
	
}
